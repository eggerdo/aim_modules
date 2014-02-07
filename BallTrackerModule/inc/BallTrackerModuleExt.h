/**
 * @file BallTrackerModuleExt.h
 * @brief BallTrackerModule extension
 *
 * This file is created at "DoBots". It is open-source software and part of "AIM". 
 * This software is published under the . license (.).
 *
 * Copyright © 2014 Dominik Egger <dominik@almende.org>
 *
 * @author                   Dominik Egger
 * @date                     Jan 28, 2014
 * @organisation             DoBots
 * @project                  AIM
 */

#include <BallTrackerModule.h>

#include <opencv/cv.h>

#include <opencv2/highgui/highgui.hpp>
#include <opencv/highgui.h>

namespace rur {

/**
 * Your Description of this module.
 */
class BallTrackerModuleExt: public BallTrackerModule {
private:
	CvMemStorage* mStorage;
	CvSeq* mContours;

	IplImage* mImgTracking;

	int mLastX, mLastY;
	double mLastAngle;

	bool mTracking;

	bool mDisplay;

	int mLowerH, mLowerS, mLowerV;
	int mUpperH, mUpperS, mUpperV;
	int mArea;

public:
	//! The constructor
	BallTrackerModuleExt();

	//! The destructor
	virtual ~BallTrackerModuleExt();

	//! The tick function is the "heart" of your module, it reads and writes to the ports
	void Tick();

	//! As soon as Stop() returns "true", the BallTrackerModuleMain will stop the module
	bool Stop();

	void extractDirection(IplImage* image, double angle, CvScalar color, int size, int thickness);

	IplImage* getThresholdedImage(IplImage* imgHSV);

	CvRect trackObject(IplImage* imgThresh);

	IplImage* decodeFrame(std::vector<int>* readVec);

	IplImage* prepareFrame(IplImage* frame);

	void setwindowSettings();
};

}

