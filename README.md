# Code4Africa Custom Camera
This is a custom camera app, built on top of the Camera2 api,  designed to take pictures in several scenes, using either the front or back cameras of the phone.
The user touches the capture button to take a picture, and holds down the capture button to record a video. A user also swipes right or left to change scenes.

## Supported Systems
The app is designed to work on Android OS

## Mininum SDK Version
22

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
* Record Videos in .mp4 format
* Support for ActivityForResult

## Screenshots
### Camera Preview Activity
![Camera Preview]()

### Preview of the picture captured
![Image Preview]()

### Video Recording interface
![Video Recording]()

## Known Issues
* Taking pictures could be faster
* File names could be better
* Video/Picture rotation/orientation needs to be looked into
