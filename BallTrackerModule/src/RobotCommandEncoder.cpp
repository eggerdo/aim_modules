#include "RobotCommandEncoder.h"

#include <boost/property_tree/ptree.hpp>
#include <boost/property_tree/json_parser.hpp>

#include <ctime>
#include <string>

using boost::property_tree::ptree;

RobotCommandEncoder::RobotCommandEncoder() :
	gTransactionId(0) {

	mRobotId = "";
}

void putHeader(ptree& pt, CommandType type) {

	#ifdef HEADER_SMALL
		pt.put("id", type);
	#elif HEADER_BIG
		pt.put("header.id", type);
		pt.put("header.tid", gTransactionId++);
		pt.put("header.timestamp", time(0));
		pt.put("header.robot_id", mRobotId);
		pt.put("header.version", VERSION)
	#endif

}

std::string RobotCommandEncoder::toString(MoveType move) {
	return std::string(move_str[move]);
}

MoveType RobotCommandEncoder::fromMove(std::string move) {
	MoveType eMove = mtFIRST;
	for ( ; eMove <= mtLAST; eMove = MoveType(eMove + 1)) {
		if (move_str[eMove] == move) {
			return eMove;
		}
	}
	return mtUNKNOWN;
}

std::string RobotCommandEncoder::toString(CameraCommandType cmd) {
	return std::string(cameraCommand_str[cmd]);
}

CameraCommandType RobotCommandEncoder::fromCameraCommand(std::string cmd) {
	CameraCommandType eCmd = cctFIRST;
	for ( ; eCmd <= cctLAST; eCmd = (CameraCommandType(eCmd + 1))) {
		if (cameraCommand_str[eCmd] == cmd) {
			return eCmd;
		}
	}
	return cctUNKNOWN;
}

// {"data":{"speed":-1,"radius":0,"move":"STRAIGHT_FORWARD"},"id":0}
std::string RobotCommandEncoder::createDriveCommand(MoveType move, double speed, double radius) {
	ptree pt;

	pt.put("data.move", toString(move));
	pt.put("data.speed", speed);
	pt.put("data.radius", radius);

	putHeader(pt, ctDRIVE);

	std::ostringstream buf;
	write_json(buf, pt, false);

//	std::cout << buf.str() << std::endl;

	return std::string(buf.str());
}

std::string RobotCommandEncoder::createCameraCommand(CameraCommandType cmd) {
	ptree pt;

	pt.put("data.move", toString(cmd));

	putHeader(pt, ctCAMERA);

	std::ostringstream buf;
	write_json(buf, pt, false);

	return std::string(buf.str());
}

// http://stackoverflow.com/questions/1657883/variable-number-of-arguments-in-c
std::string RobotCommandEncoder::createControlCommand(std::string cmd, int count, ...) {

}
