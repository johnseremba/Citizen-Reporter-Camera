# Code4Africa Custom Camera
This is a custom camera app, built on top of the Camera2 api,  designed to take pictures in several scenes, using either the front or back cameras of the phone.
The user touches the capture button to take a picture, and holds down the capture button to record a video. A user also swipes right or left to change scenes. You can touch/click the scene icons to preview other child scenes from which you can choose.

## Supported Systems
The app is designed to work on Android OS

## Mininum SDK Version
21

## Targeted SDK Version
26

## Installing the App
Open the app with Android Studio 2.3.3 or higer, build the project and run.
You can generate a signed APK to run on your device or you can run in any preferred emulator.

## OnActivityResult
When the camera app is called by another app, it returns the absolute path of either the picture taken, or the video taken. For pictures, the key is `imagePath` and the value shall be the absolute path to the image. For videos, the key `videoPath` is used to return the absolute path to the video file created.

## Storage
After a user has taken a picture, he/she decides whether to save the picture on not.
Pictures taken by this camera are stored under `.../DCIM/Code4Africa/` folder.
Videos are stored under `.../Movies/Code4Africa/` folder.

## Features
* Flash (Off, Always on, Automatic) modes.
* Preview pictures taken, decide whether or not to save.
* Switch between back and front cameras.
* Open default image gallery
* Swipe to change Scenes
* Swipe through child scenes
* Record Videos in .mp4 format
* Support for ActivityForResult
* Camera Color Effects {Sepia, Mono, Whiteboard, Posterize, Aqua, Blackboard, Negative}
* White Balance {Auto, Incadescent, Daylight, Fluorescent, Cloudy, Twilight, Shade}
* Adjust brightness
* Hide/Show overlays
* Touch to Focus

## Screenshots
### Camera Preview Activity (With Overlays)
Use the overlay toggle button to switch between a view with overlays, or one without.
![Camera Preview](https://github.com/SerryJohns/Code4Africa-Custom-Camera/blob/master/img/with_overlays.jpg)

### Swipe through Child scenes
When the user long presses the screen or clicks/touches any of the environment icons, the child scenes menu is displayed with a list of scenes belonging to a particular category. The user can choose from any of them.
![Child Scenes](https://github.com/SerryJohns/Code4Africa-Custom-Camera/blob/master/img/multiple_scenes.png)

### Camera Preview Activity (Without Overlays)
![Camera Preview 2](https://github.com/SerryJohns/Code4Africa-Custom-Camera/blob/master/img/no_overlays.jpg)

### Capture Preview Activity
The user can choose to save, or discard the picture taken.
![Capture preview](https://github.com/SerryJohns/Code4Africa-Custom-Camera/blob/master/img/preview.jpg)

### White Balance options
![White balance](https://github.com/SerryJohns/Code4Africa-Custom-Camera/blob/master/img/white_balance.jpg)

### Color Effects options
![Color Effects](https://github.com/SerryJohns/Code4Africa-Custom-Camera/blob/master/img/color_effects.jpg)


### Video Recording interface
Hold down the capture button and release to record. Tap the record button to stop.
![Video Recording](https://github.com/SerryJohns/Code4Africa-Custom-Camera/blob/master/img/record_video.jpg)

## Known Issues
* Taking pictures could be faster
* File names could be better
* Video/Picture rotation/orientation needs to be looked into
