# RomoModule

This is an android AIM Module (to be used in the Dodedodo framework, see www.dodedodo.com) which provides a controller for a Romo 1.0. It can be used in combination with other AIM modules.

It provides two ports:

1. cmd. (input) expects drive and control commands
2. video. (output) sends the camera video as base64 encoded frames.

Run this module on an android devices and place it on the Romo, then use a second android devices with the [RobotControl Module](https://play.google.com/store/apps/details?id=org.dobots.robotcontrol) to drive the Romo around.

Note: We have successfully run the Romo with the Nexus 4 and the LG Optimus 2x. Beside putting the media volume to maximum, on the Nexus 4 we used the SpeakerBoost App and put the boost to maximum. On the LG Optimus 2x we are running the CyanogenMod and had to put the 'Attenuation of set volume' to 0 dB in the CyanogenMod Sound settings.

The android module can be downloaded on the Google Play store [here](https://play.google.com/store/apps/details?id=org.dobots.romo).