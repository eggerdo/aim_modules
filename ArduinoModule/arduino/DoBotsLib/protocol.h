#ifndef PROTOCOL_H
#define PROTOCOL_H

#include "aJSON.h"

#undef HEADER_BIG
#define HEADER_SMALL

// DEFINE CONSTANTS

#define VERSION "v0.1"

// message types
#define CONTROL_COMMAND 0
#define DISCONNECT 		1
#define DRIVE_COMMAND 	2
#define SENSOR_REQUEST  3
#define SENSOR_DATA 	4

// PUBLIC FUNCTIONS

void setSerialLine(Stream *stream);

int getType(aJsonObject* json);

aJsonObject* createJsonBase(int type);
aJsonObject* createSensorData();

void addSensorValue(aJsonObject *json, char* name, int value);
void addSensorValue(aJsonObject *json, char* name, double value);
void addSensorValue(aJsonObject *json, char* name, char* value);
void addSensorValue(aJsonObject *json, char* name, boolean value);

aJsonObject* createDriveCommand(int left, int right);
void decodeDriveCommand(aJsonObject *json, int* left, int* right);
void decodeControlCommand(aJsonObject* json, boolean* enabled);

// sendMessage also deletes the json object after it is sent
void sendMessage(aJsonObject *json);
aJsonObject* readMessage();

#endif PROTOCOL_H
