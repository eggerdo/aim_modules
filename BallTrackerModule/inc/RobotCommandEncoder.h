/*
 * RobotCommandEncoder.h
 *
 *  Created on: Feb 3, 2014
 *      Author: dominik
 */

#ifndef ROBOTCOMMANDENCODER_H_
#define ROBOTCOMMANDENCODER_H_

#include <string>

#define HEADER_SMALL
#undef HEADER_BIG

#define VERSION "0.1"

enum MoveType {
	mtNONE,
	mtFIRST = mtNONE,
	mtFORWARD,
	mtSTRAIGHT_FORWARD,
	mtBACKWARD,
	mtSTRAIGHT_BACKWARD,
	mtROTATE_LEFT,
	mtROTATE_RIGHT,
	mtLEFT,
	mtRIGHT,
	mtUP,
	mtDOWN,
	mtLAST = mtDOWN,
	mtUNKNOWN
};

static const char* move_str[] = {
	"NONE",
	"FORWARD",
	"STRAIGHT_FORWARD",
	"BACKWARD",
	"STRAIGHT_BACKWARD",
	"ROTATE_LEFT",
	"ROTATE_RIGHT",
	"LEFT",
	"RIGHT",
	"UP",
	"DOWN"
};

enum CommandType {
	ctDRIVE 	= 0,
	ctCAMERA 	= 1,
	ctCONTROL	= 2
};

enum CameraCommandType {
	cctTOGGLE,
	cctFIRST = cctTOGGLE,
	cctOFF,
	cctON,
	cctUP,
	cctDOWN,
	cctSTOP,
	cctLAST = cctSTOP,
	cctUNKNOWN
};

static const char* cameraCommand_str[] = {
	"TOGGLE",
	"OFF",
	"ON",
	"UP",
	"DOWN"
	"STOP"
};

//#define SOME_ENUM(DO) \
//    DO(TOGGLE) \
//    DO(OFF) \
//    DO(ON) \
//    DO(UP) \
//    DO(DOWN) \
//    DO(STOP)
//
//#define MAKE_ENUM(VAR) VAR,
//enum CameraCommand{
//    SOME_ENUM(MAKE_ENUM)
//};
//
//#define MAKE_STRINGS(VAR) #VAR,
//const char* const cc_str[] = {
//    SOME_ENUM(MAKE_STRINGS)
//};

class RobotCommandEncoder {
private:
	long gTransactionId;
	std::string mRobotId;

public:
	RobotCommandEncoder();

	std::string toString(MoveType move);
	MoveType fromMove(std::string move);

	std::string toString(CameraCommandType cmd);
	CameraCommandType fromCameraCommand(std::string cmd);

	std::string createDriveCommand(MoveType move, double speed, double angle);
	std::string createCameraCommand(CameraCommandType cmd);

	std::string createControlCommand(std::string cmd, int count, ...);
};

#endif
