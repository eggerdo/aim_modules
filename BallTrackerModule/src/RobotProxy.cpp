/*
 * RobotProxy.cpp
 *
 *  Created on: Feb 10, 2014
 *      Author: dominik
 */

#include "RobotProxy.h"
#include "math.h"
#include <iostream>

#include <stdio.h>

RobotProxy::RobotProxy(Connection* connection) :
	mConnection(connection) {

	mEncoder = RobotCommandEncoder();
}

RobotProxy::~RobotProxy() {

}

template <typename T> int sgn(T val) {
    return (T(0) < val) - (val < T(0));
}

//////////////////////////////////////////////////////////////////////
// DRIVE COMMANDS
//////////////////////////////////////////////////////////////////////

double last_speed = 0;

std::string RobotProxy::drive(MoveType move, double speed, double radius) {
	// cap speed and radius values at their limits
	double orig_speed = speed;

//	speed = sgn(speed) * fmin(fmax(fabs(speed), 7), SPEED_LIMIT);
//	radius = sgn(radius) * fmin(fabs(radius), RADIUS_LIMIT);
//
//	if (fabs(last_speed) < 7 || sgn(speed) != sgn(last_speed)) {
//		speed = sgn(speed) * fmax(fabs(speed), 12);
//	}

	speed = sgn(speed) * fmin(fabs(speed), SPEED_LIMIT) + 10;
	radius = sgn(radius) * fmin(fabs(radius), RADIUS_LIMIT);

	std::string message = mEncoder.createDriveCommand(move, speed, radius);
//	std::cout << "[BallTracker] " << message << std::endl;
	if (mConnection) {
		mConnection->send(message);
	}

	last_speed = orig_speed;

	return message;
}

std::string RobotProxy::moveStop() {
	printf("[BallTracker] moveStop()\n");
//	std::string message = mEncoder.createDriveCommand(mtNONE, 0, 0);
//	if (mConnection) {
//		mConnection->send(message);
//	}
//	return message;
	return drive(mtNONE, 0, 0);
}

std::string RobotProxy::moveForward(double speed, double radius) {
	printf("[BallTracker] moveForward(%d, %d)\n", (int)speed, (int)radius);
	return drive(mtFORWARD, speed, radius);
}

std::string RobotProxy::moveForward(double speed) {
	printf("[BallTracker] moveForward(%d)\n", (int)speed);
	return drive(mtSTRAIGHT_FORWARD, speed, 0);
}

std::string RobotProxy::moveBackward(double speed, double radius) {
	printf("[BallTracker] moveBackward(%d, %d)\n", (int)speed, (int)radius);
	return drive(mtBACKWARD, speed, radius);
}

std::string RobotProxy::moveBackward(double speed) {
	printf("[BallTracker] moveBackward(%d)\n", (int)speed);
	return drive(mtSTRAIGHT_BACKWARD, speed, 0);
}

std::string RobotProxy::rotateClockwise(double speed) {
	printf("[BallTracker] rotateClockwise(%d)\n", (int)speed);
	return drive(mtROTATE_RIGHT, speed, 0);
}

std::string RobotProxy::rotateCounterClockwise(double speed) {
	printf("[BallTracker] rotateCounterClockwise(%d)\n", (int)speed);
	return drive(mtROTATE_LEFT, speed, 0);
}

std::string RobotProxy::moveLeft(double speed) {
	printf("[BallTracker] moveLeft(%d)\n", (int)speed);
	return drive(mtLEFT, speed, 0);
}

std::string RobotProxy::moveRight(double speed) {
	printf("[BallTracker] moveRight(%d)\n", (int)speed);
	return drive(mtRIGHT, speed, 0);
}

std::string RobotProxy::moveUp(double speed) {
	printf("[BallTracker] moveUp(%d)\n", (int)speed);
	return drive(mtUP, speed, 0);
}

std::string RobotProxy::moveDown(double speed) {
	printf("[BallTracker] moveDown(%d)\n", (int)speed);
	return drive(mtDOWN, speed, 0);
}

//////////////////////////////////////////////////////////////////////
// CAMERA COMMANDS
//////////////////////////////////////////////////////////////////////

std::string RobotProxy::cameraUp() {
	return mEncoder.createCameraCommand(cctUP);
}

std::string RobotProxy::cameraDown() {
	return mEncoder.createCameraCommand(cctDOWN);
}

std::string RobotProxy::cameraStop() {
	return mEncoder.createCameraCommand(cctSTOP);
}

std::string RobotProxy::cameraToggle() {
	return mEncoder.createCameraCommand(cctTOGGLE);
}

std::string RobotProxy::cameraOn() {
	return mEncoder.createCameraCommand(cctON);
}

std::string RobotProxy::cameraOff() {
	return mEncoder.createCameraCommand(cctOFF);
}
