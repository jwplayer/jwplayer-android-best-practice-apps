# JW Player SDK for Android Best Practice Apps

This repository contains samples relating to the JW Player SDK for Android.

Every subdirectory in this repository is a gradle project that can be imported into Android Studio, or IntelliJ IDEA.
See the README in the subdirectory for specific usage instructions.

## Sample Apps

Currently this repository contains one single sample app, the 'Movable Player Demo', this demo showcases how you could use the JW Player SDK for Android to create an
app that allows you to display a video that can be dragged around within an Activity.

## Usage instructions

In general the following usage instructions apply to sample projects:

- Clone this repository.
- In Android Studio choose 'Open an Existing Android Studio Project' or File / Open...
- Navigate to the location of the sample app that you want to open and select the `build.gradle` file in that directory, and click OK.
- Once the project opens, go to File / New / New Module ...
- Select Import .JAR/.AAR Package from the list
- Navigate to the JW Player SDK for Android .aar file, select it, and click OK
- Right-click on "app" in the Project view and choose Open Module Settings
- Click on the Dependencies tab, then click on the green plus sign and choose Module dependency
- Select 'jwplayer-android-sdk-x.y.z' and click OK
- Open the AndroidManifest.xml file and replace {YOUR_LICENSE_KEY} with your license key
