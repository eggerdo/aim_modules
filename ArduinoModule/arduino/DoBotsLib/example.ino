#include "messenger.h"

// if one of the callbacks is not needed supply NULL instead
Messenger messenger(onControl, onDisconnect, onSensorRequest, onDrive);

/*
 * assumes 2 hardware serials. serial 1 is used with a bluetooth module
 * to communicate with the android device
 * serial 2 is used as a debug output
 */
void setup() {
	Serial2.begin(115200);
	Serial.begin(115200);

	setSerialLine(&Serial);
}

void loop() {
	messenger.handleMessages();
}

void onControl(boolean enabled) {
	Serial2.print("onControl: ");
	Serial2.println(enabled ? "true" : "false");
}

void onDisconnect(aJsonObject* json) {
	Serial2.println("onDisconnect");
}

void onSensorRequest(aJsonObject* json) {
	Serial2.println("onSensorRequest");
	sendData();
}

void onDrive(int left, int right) {
	Serial2.print("onDrive(");
	Serial2.print(left);
	Serial2.print(", ");
	Serial2.print(right);
	Serial2.println(")");
}

void sendData() {
	aJsonObject *sensorData;
	sensorData = createSensorData();
	addSensorValue(sensorData, "current", (int)random(300));
	addSensorValue(sensorData, "voltage", (double)random(500));
	addSensorValue(sensorData, "state", "busy");
	addSensorValue(sensorData, "running", (boolean)false);
	sendMessage(sensorData);
}
