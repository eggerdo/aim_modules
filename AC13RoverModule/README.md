# AC13RoverModule

This is an android AIM Module (to be used in the Dodedodo framework, see www.dodedodo.com) which controls a Brookstone AC13 Rover. It can be used stand-alone or in combination with other AIM modules.

It provides two ports:

1. cmd. (input) expects drive and control commands
2. video. (output) forwards the video received from the rover as base64 encoded frames.

Note: By default, android won't connect to the AC13's network, because the AC13 creates an Ad Hoc network, and Ad Hoc networks won't show up on android. There are several solutions to enable Ad Hoc for android. Our approach was to install the custom ROM CyanogenMod which, among other features, enables Ad Hoc on android.

The android module can be downloaded on the Google Play store [here](https://play.google.com/store/apps/details?id=org.dobots.ac13).