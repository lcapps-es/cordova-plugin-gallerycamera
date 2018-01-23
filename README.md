# cordova-plugin-gallerycamera

Plugin that extends input files allowing you to choose between camera or gallery

##Installation

From master
```
cordova plugin add https://github.com/lmmartinb/cordova-plugin-gallerycamera
```

Latest stable version
```
cordova plugin add cordova-plugin-gallerycamera
```

## Usage

You only need to change:
```
document.getElementById('###').files[0]
```
to
```
navigator.galleryCamera.getFile( inputId )
```

## How it works

PENDING

## License
```
MIT License

Copyright (c) 2017 Luis Miguel Martín Bardanca

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

```
## To-Do
- Call onclick before get the image if we have it defined. 
- Only works with simple inputs. We need to implement multiple logic.
- Check it in iOS. (OK)
- Multi-Language support.

## History of Changes.

### v0.0.9
### v0.0.8
### v0.0.7
### v0.0.6
Fix some problems with permissions and last android version.

### v0.0.5
iOS Compatibility

### v0.0.4
isAndroid Validation

### v0.0.3
Fix problems with 7.1 or greater Android version.

### v0.0.2
Check Android Permissions

### v0.0.1
First changes. Basic functionality working.
