/*
 * RobotProxy.h
 *
 *  Created on: Feb 10, 2014
 *      Author: dominik
 */

#ifndef ROBOTPROXY_H_
#define ROBOTPROXY_H_

#include <RobotCommandEncoder.h>
#include <Connection.h>

#include <string>

#define SPEED_LIMIT 100
#define RADIUS_LIMIT 90

class RobotProxy {

private:
	RobotCommandEncoder mEncoder;

	Connection* mConnection;

public:
	RobotProxy(Connection *connection);
	~RobotProxy();

	// speed between -100 and +100 (percentage)
	// radius between -100 and +100 (percentage)

	std::string moveStop();

	std::string moveForward(double speed, double radius);
	std::string moveForward(double speed);

	std::string moveBackward(double speed, double radius);
	std::string moveBackward(double speed);

	std::string rotateClockwise(double speed);
	std::string rotateCounterClockwise(double speed);

	std::string moveLeft(double speed);
	std::string moveRight(double speed);

	std::string moveUp(double speed);
	std::string moveDown(double speed);

	std::string cameraUp();
	std::string cameraDown();
	std::string cameraStop();

	std::string cameraToggle();
	std::string cameraOn();
	std::string cameraOff();

private:
	std::string drive(MoveType move, double speed, double radius);

};

#endif
