#include "protocol.h"

Stream *serial_stream;
aJsonStream *jsonStream;

#ifdef HEADER_BIG
int mTransactionId = 0;
#endif

// set the serial line used for communication
void setSerialLine(Stream *stream) {
	serial_stream = stream;
	jsonStream = new aJsonStream(stream);
}

// get the message type. see header for the available types
int getType(aJsonObject* json) {
	aJsonObject *header, *type;

#ifdef HEADER_BIG
	header = aJson.getObjectItem(json, "header");
	if (header == NULL) {
		return -1;
	}

	type = aJson.getObjectItem(header, "type");
	if (type == NULL) {
		return -1;
	}
#endif
#ifdef HEADER_SMALL
	type = aJson.getObjectItem(json, "type");
	if (type == NULL) {
		return -1;
	}
#endif

	return type->valueint;
}

// create the basic json structure. different if
// header_big or header_small is defined
aJsonObject* createJsonBase(int type) {
  aJsonObject *json = aJson.createObject();

#ifdef HEADER_BIG
  aJsonObject *header = aJson.createObject();
  aJson.addNumberToObject(header, "tid", mTransactionId++);
  aJson.addNumberToObject(header, "timestamp", 0);
  aJson.addNumberToObject(header, "type", type);
  aJson.addStringToObject(header, "version", VERSION);
  aJson.addItemToObject(json, "header", header);
#endif
#ifdef HEADER_SMALL
  aJson.addNumberToObject(json, "type", type);
#endif

  return json;
}

// create a sensor data message
aJsonObject* createSensorData() {
	aJsonObject* json = createJsonBase(SENSOR_DATA);

	aJsonObject *data = aJson.createObject();
	aJson.addItemToObject(json, "data", data);

	return json;
}

// add an integer value to the sensor data message
void addSensorValue(aJsonObject *json, char* name, int value) {
	aJsonObject* data = aJson.getObjectItem(json, "data");

	aJson.addNumberToObject(data, name, value);
}

// add a double value to the sensor data message
void addSensorValue(aJsonObject *json, char* name, double value) {
	aJsonObject* data = aJson.getObjectItem(json, "data");

	aJson.addNumberToObject(data, name, value);
}

// add a string value to the sensor data message
void addSensorValue(aJsonObject *json, char* name, char* value) {
	aJsonObject* data = aJson.getObjectItem(json, "data");

	aJson.addStringToObject(data, name, value);
}

// add a boolean value to the sensor data message
void addSensorValue(aJsonObject *json, char* name, boolean value) {
	aJsonObject* data = aJson.getObjectItem(json, "data");

	if (value) {
		aJson.addTrueToObject(data, name);
	} else {
		aJson.addFalseToObject(data, name);
	}
}

// create a drive command message with left and right speed setpoints
// the speed values can be in the range [-255, 255], where negative
// values corresponds to backward motion
aJsonObject* createDriveCommand(int left, int right) {
	aJsonObject* json = createJsonBase(DRIVE_COMMAND);

	aJsonObject* data = aJson.createObject();
	aJson.addNumberToObject(data, "left", left);
	aJson.addNumberToObject(data, "right", right);
	aJson.addItemToObject(json, "data", data);

	return json;
}

// decode a drive command to get the left and right speed setpoints
void decodeDriveCommand(aJsonObject* json, int* left, int* right) {
	aJsonObject* data = aJson.getObjectItem(json, "data");

	aJsonObject* left_j = aJson.getObjectItem(data, "left");
	*left = left_j->valueint;

	aJsonObject* right_j = aJson.getObjectItem(data, "right");
	*right = right_j->valueint;
}

void decodeControlCommand(aJsonObject* json, boolean* enabled) {
	aJsonObject* data = aJson.getObjectItem(json, "data");

	aJsonObject* enabled_j = aJson.getObjectItem(data, "enabled");
	*enabled = enabled_j->valuebool;
}

// send a message
void sendMessage(aJsonObject* json) {
  aJson.print(json, jsonStream);
  serial_stream->write("\n");
  aJson.deleteItem(json);
}

// listen for incoming messages and return the message
// NULL is returned if nothing is available or if the message is
// not a valid json string
aJsonObject* readMessage() {
	aJsonObject* json;
	if (jsonStream->available()) {
		json = aJson.parse(jsonStream);
		if (json == NULL) {
			jsonStream->flush();
			return NULL;
		}
		return json;
	}
	return NULL;
}
