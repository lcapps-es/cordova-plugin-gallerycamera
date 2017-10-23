/**
 * @file Main JS Plugin file. All possible actions are defined here.
 * 
 * @author Luis Miguel Martín <lm.martinb@gmail.com>
 */
var exec = require( 'cordova/exec' );

module.exports = {
		/**
		 * Array to store files.
		 */
		files: [],

		/**
		 * This method call Android code.
		 */
		getPicture: function( success, error, options ){
			exec( success, error, 'CordovaPluginGalleryCamera', 'getPicture', [] );
		},

		/**
		 * File handler.
		 */
		getFile: function( id ){
			var input = document.getElementById( id );
			if ( input.files.length > 0 ){ // Common input.
				return input.files[0];
			}else{ // GalleryCamera array.
			    return (this.files[0] !== undefined ? this.files[0] : null )
			}
		},

		/**
		 * Clean array.
		 */
		cleanFiles: function() {
		    this.files = [];
		},

		/**
		 * Append File to array.
		 */
		addFile: function ( file ){
		    this.files.push( file );
		},
};


// Now, we'll define click event for every <input type="file"/>
// @TODO: Detect if fileChooser has onclick event and it's not ours (execute before ours).
document.addEventListener("deviceready", function() {

	// For every change in DOM...
	var observer = new MutationObserver(function(mutation) {
		// Get every <input type="file"/>.
        var fileChoosers = document.querySelectorAll("input[type=file]");

        for( var i = 0; i < fileChoosers.length; i++ ){
        	
        	// Check if onclick is overrided.
            var clickChanged = fileChoosers[i].getAttribute( 'data-gallerycamera');
            if ( clickChanged ) continue;
            
            // Override onclick.
            fileChoosers[i].addEventListener( 'click', function(e){
                e.preventDefault();

                var targetElement = e.target || e.srcElement;

                // Choose or take the picture.
                navigator.galleryCamera.getPicture( function( resp ){

					function readFromFile(fileName) {
						var errorHandler = function (fileName, e) {
						    var msg = '';
						
							switch (e.code) {
							    case FileError.QUOTA_EXCEEDED_ERR:
							        msg = 'Storage quota exceeded';
							    break;
							case FileError.NOT_FOUND_ERR:
							    msg = 'File not found';
							    break;
							case FileError.SECURITY_ERR:
							    msg = 'Security error';
							    break;
							case FileError.INVALID_MODIFICATION_ERR:
							    msg = 'Invalid modification';
							    break;
							case FileError.INVALID_STATE_ERR:
							    msg = 'Invalid state';
							    break;
							default:
							    msg = 'Unknown error';
							        break;
							};
							
							console.log('Error (' + fileName + '): ' + msg);
						}
	                            
						var pathToFile = fileName;
	                    window.resolveLocalFileSystemURL(pathToFile, function (fileEntry) {
	                    	fileEntry.file(function (file) {
	                    		// Store file.
	                    		navigator.galleryCamera.cleanFiles();
	                    		navigator.galleryCamera.addFile( file );
	                    		
	                    		// Trigger onchange event.
	                    		var event = new Event('change');
	                    		targetElement.dispatchEvent(event);
	
	                    	}, errorHandler.bind(null, fileName));
	                    }, errorHandler.bind(null, fileName));
					}
	
					readFromFile('file://' + resp);
                    
                }, alert, {} );
                return false;
            });
            
            fileChoosers[i].setAttribute( 'data-gallerycamera', true );
        }
    });

    var container = document.documentElement || document.body;
    var config = { attributes: true, childList: true, characterData: true,subtree: true };

    observer.observe(container, config);

}, false);

