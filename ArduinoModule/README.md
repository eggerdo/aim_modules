# ArduinoModule

This is an android AIM Module (to be used in the Dodedodo framework, see www.dodedodo.com) which controls an arduino robot over bluetooth. It can be used stand-alone or in combination with other AIM modules.

It provides two port:

1. cmd. (input) expects drive and control commands
2. sensors. (output) forwards the sensor values received from the arduino

Use the library in ./arduino/DoBotsLib as the communication library on the arduino. It provides the protocol used between android device and arduino and an example sketch (example.ino)

The android module can be downloaded on the Google Play store [here]().