/**
 * @file Android Plugin Main Class
 * 
 * @author Luis Miguel Martín <lm.martinb@gmail.com>
 */
package es.lmmartin.cordova.camera;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.os.Build;
import android.database.Cursor;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.Manifest;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.lang.String;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PermissionHelper;

import org.json.JSONArray;
import org.json.JSONException;

public class GalleryCameraLauncher extends CordovaPlugin{

	public CallbackContext callbackContext;

	private static final String LOG_TAG = "GalleryCamera";
	private static final int CHOOSE_IMAGE_ID = 1;
	private static final String TEMP_IMAGE_NAME = "tempImage";
	private static final String IMAGE_JPEG = "image/jpeg";
	private static final int JPEG = 0;                  // Take a picture of type JPEG
	private static final int PNG = 1;                   // Take a picture of type PNG
	public static final int TAKE_PIC_SEC = 0;
	protected final static String[] permissions = {
			Manifest.permission.CAMERA,
			Manifest.permission.READ_EXTERNAL_STORAGE
	};


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
	    if ( requestCode == CHOOSE_IMAGE_ID && data != null ) {
			Uri uri = data.getData();
			if( uri == null ) {
				Bitmap photo = (Bitmap) data.getExtras().get("data");

				ContentResolver contentResolver = this.cordova.getActivity().getContentResolver();
				ContentValues cv = new ContentValues();
				cv.put(MediaStore.Images.Media.MIME_TYPE, IMAGE_JPEG);
				uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
				//ByteArrayOutputStream bos = new ByteArrayOutputStream();

				try {
					OutputStream imageOut = contentResolver.openOutputStream(uri);
					photo.compress(Bitmap.CompressFormat.JPEG, 100, imageOut);

					String fileLocation = this.getRealPath(uri, this.cordova);

					this.callbackContext.success(fileLocation);
				} catch (Exception e) {
					Log.e(LOG_TAG, e.getMessage());
				}
			}else{
				String fileLocation = this.getRealPath(uri, this.cordova);

				this.callbackContext.success( fileLocation );
			}
	    }
	}

	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;
		LOG.d(LOG_TAG, "executing action");
		
		if( action.equals( "getPicture" ) ){
			LOG.d( LOG_TAG, "nice! found our action" );

			Context context = this.cordova.getActivity().getApplicationContext();
			Intent chooserIntent = this.getImageChooser(context);

			if( chooserIntent != null ) {
				this.cordova.setActivityResultCallback(this);
				this.cordova.getActivity().startActivityForResult( chooserIntent, CHOOSE_IMAGE_ID );
				PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
				r.setKeepCallback(true);
				callbackContext.sendPluginResult(r);

				return true;
			}else{
				return false;
			}
		}
		
		callbackContext.error( "return error action" );
		
		return false;
	}
	
	protected Intent getImageChooser(Context context){
		boolean saveAlbumPermission = PermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
		boolean takePicturePermission = PermissionHelper.hasPermission(this, Manifest.permission.CAMERA);

		if (takePicturePermission && saveAlbumPermission) {
			Intent photoGalleryIntent = new Intent( Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI );

			Intent takePhotoIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

			/*ContentResolver contentResolver = this.cordova.getActivity().getContentResolver();
			ContentValues cv = new ContentValues();
			cv.put(MediaStore.Images.Media.MIME_TYPE, IMAGE_JPEG);
			Uri imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
			LOG.d(LOG_TAG, "Taking a picture and saving to: " + imageUri.toString());*/

			//takePhotoIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri);


			List<Intent> intentList = new ArrayList();
			intentList = addIntentsToList(context, intentList, photoGalleryIntent);
			intentList = addIntentsToList(context, intentList, takePhotoIntent);

			Intent chooserIntent = null;

			if (intentList.size() > 0) {
				chooserIntent = Intent.createChooser( intentList.remove( intentList.size() - 1 ),
						context.getString( cordova.getActivity().getResources().getIdentifier("pick_any_option", "string", cordova.getActivity().getPackageName()) ));
				chooserIntent.putExtra( Intent.EXTRA_INITIAL_INTENTS,
						intentList.toArray( new Parcelable[]{} ) );
			}

			return chooserIntent;
		} else if (saveAlbumPermission && !takePicturePermission) {
			PermissionHelper.requestPermission(this, TAKE_PIC_SEC, Manifest.permission.CAMERA);
			return null;
		} else if (!saveAlbumPermission && takePicturePermission) {
			PermissionHelper.requestPermission(this, TAKE_PIC_SEC, Manifest.permission.READ_EXTERNAL_STORAGE);
			return null;
		} else {
			PermissionHelper.requestPermissions(this, TAKE_PIC_SEC, permissions);
			return null;
		}

	}

	private static List<Intent> addIntentsToList( Context context, List<Intent> list, Intent intent ) {
	    List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
	    
	    for ( ResolveInfo resolveInfo : resInfo ) {
	        String packageName = resolveInfo.activityInfo.packageName;
	        Intent targetedIntent = new Intent( intent );
	        targetedIntent.setPackage( packageName );
	        list.add( targetedIntent );
	    }
	    
	    return list;
	}

	/**
	 * METHODS FROM CORDOVA-PLUGIN-CAMERA
	 */
	@SuppressWarnings("deprecation")
	public String getRealPath(Uri uri, CordovaInterface cordova) {
		String realPath = null;

		if (Build.VERSION.SDK_INT < 11)
			realPath = this.getRealPathFromURI_BelowAPI11(cordova.getActivity(), uri);

			// SDK >= 11
		else
			realPath = this.getRealPathFromURI_API11_And_Above(cordova.getActivity(), uri);

		return realPath;
	}

	public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		String result = null;

		try {
			Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			result = cursor.getString(column_index);

		} catch (Exception e) {
			result = null;
		}
		return result;
	}

	@SuppressLint("NewApi")
	public static String getRealPathFromURI_API11_And_Above(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] {
						split[1]
				};

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {

			// Return the remote address
			if (isGooglePhotosUri(uri))
				return uri.getLastPathSegment();

			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}
	
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}
	
	public static String getDataColumn(Context context, Uri uri, String selection,
									   String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {
				column
		};

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst()) {

				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} catch (Exception e) {
			return null;
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}
	
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}
	
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
	
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}

	public void onRequestPermissionResult(int requestCode, String[] permissions,
					int[] grantResults) throws JSONException {
		
		for (int r : grantResults) {
			if (r == PackageManager.PERMISSION_DENIED) {
				this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, 20));
				return;
			}
		}

		Context context = this.cordova.getActivity().getApplicationContext();
		Intent chooserIntent = this.getImageChooser(context);

		if( chooserIntent != null ) {
			this.cordova.setActivityResultCallback(this);
			this.cordova.getActivity().startActivityForResult( chooserIntent, CHOOSE_IMAGE_ID );
			PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
			r.setKeepCallback(true);
			this.callbackContext.sendPluginResult(r);
		}else{
			this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, 20));
		}
	}
}
