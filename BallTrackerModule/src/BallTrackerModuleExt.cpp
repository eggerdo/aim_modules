/**
 * @file BallTrackerModuleExt.cpp
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

#include <BallTrackerModuleExt.h>

#include <unistd.h>

#include <CTime.h> // For profiling

//#include <highgui.h>

using namespace cv;
using namespace rur;

#define PI 3.1416
#define MOVE_THRESHOLD 5

#define MIN_DISTANCE 2
#define MAX_DISTANCE 80

bool dummySendCommand(std::string cmd) {
	printf("[BallTracker] sendCommand: %s", cmd.c_str());
}

//! Replace with your own code
BallTrackerModuleExt::BallTrackerModuleExt() {

	std::cout << "[BallTracker] starting..." << std::endl;

	mStorage = cvCreateMemStorage(0);

	mDisplay = true;

	// YELLOW BALL
	//	mLowerH	= 20;
	//	mLowerS	= 140;
	//	mLowerV	= 100;
	//
	//	mUpperH	= 38;
	//	mUpperS	= 256;
	//	mUpperV	= 256;

	// BLUE BALL
	mLowerH	= 75;
	mLowerS	= 105;
	mLowerV	= 50;

	mUpperH	= 130;
	mUpperS	= 256;
	mUpperV	= 256;

	mArea = 1000;

	setWindowSettings();

	mRobot = new RobotProxy(this);
//	mRobot = new RobotProxy(NULL);
}

//! Replace with your own code
BallTrackerModuleExt::~BallTrackerModuleExt() {

	if (mDisplay) {
		cvDestroyAllWindows() ;
	}

	cvReleaseImage(&mImgTracking);
}

bool BallTrackerModuleExt::send(std::string message) {
	return writeCommand(message);
}

void BallTrackerModuleExt::extractDirection(IplImage* image, double angle, CvScalar color, int size, int thickness) {
	CvSize imageSize = cvGetSize(image);

	CvPoint ptCenter = cvPoint(imageSize.width / 2, imageSize.height / 2);

	CvPoint ptEnd, ptFront;

	int length = size;

	int dx = (int)(length * cos(angle));
	int dy = (int)(length * sin(angle));

	ptFront.x = ptCenter.x + dx;
	ptFront.y = ptCenter.y + dy;

	ptEnd.x = ptCenter.x - dx;
	ptEnd.y = ptCenter.y - dy;

	cvLine(image, ptFront, ptEnd, color, thickness);

	CvPoint p;
	if (angle == PI/2 && ptEnd.y > ptFront.y) {
		p.x = (int)(size * cos(angle) - size * sin(angle) + ptFront.x);
		p.y = (int)(size * sin(angle) + size * cos(angle) + ptFront.y);
		cvLine(image, ptFront, p, color, thickness);

		p.x = (int)(size * cos(angle) + size * sin(angle) + ptFront.x);
		p.y = (int)(size * sin(angle) - size * cos(angle) + ptFront.y);
		cvLine(image, ptFront, p, color, thickness);
	} else {
		p.x = (int)(-size * cos(angle) - size * sin(angle) + ptFront.x);
		p.y = (int)(-size * sin(angle) + size * cos(angle) + ptFront.y);
		cvLine(image, ptFront, p, color, thickness);

		p.x = (int)(-size * cos(angle) + size * sin(angle) + ptFront.x);
		p.y = (int)(-size * sin(angle) - size * cos(angle) + ptFront.y);
		cvLine(image, ptFront, p, color, thickness);
	}
}

//This function create two windows and 6 trackbars for the "Ball" window
void BallTrackerModuleExt::setWindowSettings() {
	cvNamedWindow("Video");
	cvNamedWindow("Ball");

	cvCreateTrackbar("mLowerH", "Ball", &mLowerH, 180, NULL);
	cvCreateTrackbar("mUpperH", "Ball", &mUpperH, 180, NULL);

	cvCreateTrackbar("mLowerS", "Ball", &mLowerS, 256, NULL);
	cvCreateTrackbar("mUpperS", "Ball", &mUpperS, 256, NULL);

	cvCreateTrackbar("mLowerV", "Ball", &mLowerV, 256, NULL);
	cvCreateTrackbar("mUpperV", "Ball", &mUpperV, 256, NULL);

	cvCreateTrackbar("mArea", "Ball", &mArea, 5000, NULL);
}

//This function threshold the HSV image and create a binary image
IplImage* BallTrackerModuleExt::getThresholdedImage(IplImage* imgHSV) {

	IplImage* imgThresh=cvCreateImage(cvGetSize(imgHSV),IPL_DEPTH_8U, 1);
	cvInRangeS(imgHSV, cvScalar(mLowerH,mLowerS,mLowerV), cvScalar(mUpperH,mUpperS,mUpperV), imgThresh);

	return imgThresh;
}

CvRect BallTrackerModuleExt::trackObject(IplImage* imgThresh) {
	// Calculate the moments of 'imgThresh'
	CvMoments *moments = (CvMoments*)malloc(sizeof(CvMoments));
	cvMoments(imgThresh, moments, 1);
	double moment10 = cvGetSpatialMoment(moments, 1, 0);
	double moment01 = cvGetSpatialMoment(moments, 0, 1);
	double area = cvGetCentralMoment(moments, 0, 0);

	cvSetZero(mImgTracking); //covert the image, 'imgTracking' to black

	CvRect boundingRect;

	// if the area<1000, I consider that the there are no object in the image and it's because of the noise, the area is not zero
	if(area>mArea){
		// calculate the position of the ball
		int posX = moment10/area;
		int posY = moment01/area;

		//		printf("[BallTracker] moments > x: %d, y: %d, m10: %.3f, m01: %.3f, m00: %.3f\n", posX, posY, moment10, moment01, area);

		if(mLastX>=0 && mLastY>=0 && posX>=0 && posY>=0)
		{
			IplImage* imgTemp = cvCloneImage(imgThresh);

			// find the contours
			cvFindContours(imgTemp, mStorage, &mContours, sizeof(CvContour));

			//			contours = cvApproxPoly( contours, sizeof(CvContour), storage, CV_POLY_APPROX_DP, 0, 1 );

			if (true) {
				CvSeq* tcontours = mContours;
				while( tcontours->h_next )
					tcontours = tcontours->h_next;

				boundingRect = cvBoundingRect(tcontours, 1);
				for( ; tcontours != 0; tcontours = tcontours->h_prev )
				{
					CvRect rect = cvBoundingRect(tcontours, 1);
					if (rect.width * rect.height > boundingRect.width * boundingRect.height) {
						boundingRect = rect;
					}
				}

				cvRectangle(mImgTracking, cvPoint(boundingRect.x, boundingRect.y), cvPoint(boundingRect.x + boundingRect.width, boundingRect.y + boundingRect.height), cvScalar(255,0,0), 10);
				cvDrawContours(mImgTracking, mContours, cvScalar(0, 0, 255), cvScalar(0, 255, 0), 3, 3);

				printf("[BallTracker] bounding > x: %d, y: %d, width: %d, height: %d\n",
						(int)(boundingRect.x + boundingRect.width / 2),
						(int)(boundingRect.y + boundingRect.height / 2),
						boundingRect.width,
						boundingRect.height);
			}

			//			double distance = sqrt(pow(posY - mLastY, 2) + pow(posX - mLastX, 2));
			//			if (distance > MOVE_THRESHOLD) {
			//				double angle = atan2((posY - mLastY), (posX - mLastX));
			//				extractDirection(mImgTracking, angle, cvScalar(0, 0, 255), 50, 10);
			//				mLastAngle = angle;
			////			} else {
			////				extractDirection(mImgTracking, mLastAngle, cvScalar(0, 0, 255), 50, 10);
			//			}

			CvSize frameSize = cvGetSize(imgThresh);
			CvPoint frameCenter = cvPoint(frameSize.width / 2, frameSize.height / 2);

			printf("[BallTracker] frame c_x=%d, c_y=%d\n", frameCenter.x, frameCenter.y);

			CvPoint objectCenter = cvPoint(boundingRect.x + boundingRect.width / 2, boundingRect.y + boundingRect.height / 2);

			printf("[BallTracker] object c_x=%d, c_y=%d\n", objectCenter.x, objectCenter.y);

			double angleOffset = getAngleOffset(objectCenter, frameCenter);

			printf("[BallTracker] angleOffset: %.2f\n", angleOffset);

//			int distSize = getObjectDistSize(boundingRect);

//			printf("[BallTracker] objectSize: %d\n", distSize);

			double distOffset = getDistOffset(boundingRect);

			if (abs(distOffset) > 0.1) {
				double angle = distOffset > 0 ? -PI/2 : PI/2;

				extractDirection(mImgTracking, angle, cvScalar(0, 255, 0), MAX(abs(distOffset) * 100, 15), 10);
			}

			printf("[BallTracker] distOffset: %.2f\n", distOffset);

//			double distance = sqrt(pow(frameCenter.x - objectCenter.x, 2) + pow(frameCenter.y - objectCenter.y, 2));
			double distance = abs(frameCenter.x - objectCenter.x);

			printf("[BallTracker] distance: %.2f\n", distance);

			if (distance > MIN_DISTANCE) {
				double angle = objectCenter.x < frameCenter.x ? PI : 0;
//				double angle = atan2((objectCenter.y - frameCenter.y), (objectCenter.x - frameCenter.x));

				printf("[BallTracker] angle: %.2f\n", angle);

				extractDirection(mImgTracking, angle, cvScalar(0, 0, 255), MAX((int)distance, 15), 10);
			}

			calculateCommand(angleOffset, distOffset);
//			writeCommand(*message);

		} else {
			mRobot->moveStop();
		}

		mLastX = posX;
		mLastY = posY;
	} else {
		mRobot->moveStop();
	}

	free(moments);

	return boundingRect;
}

// neg value to turn right
// pos value to turn left
double BallTrackerModuleExt::getAngleOffset(CvPoint objectCenter, CvPoint frameCenter) {

	return (double)(frameCenter.x - objectCenter.x) / frameCenter.x;

//	// for now, only -1 for right, +1 for left
//	if (objectCenter.x < frameCenter.x) {
//		return +1;
//	} else {
//		return - 1;
//	}

}

int MAX_DIST_SIZE = 30;
int MIN_DIST_SIZE = 130;
int TARGET_DIST_SIZE = 80;

int BallTrackerModuleExt::getObjectDistSize(CvRect object) {

//	printf("objectDistSize=%d\n", object.height);
	return object.height;

//	return TARGET_DIST_SIZE;

}

// neg value to move backward
// pos value to move forward
double BallTrackerModuleExt::getDistOffset(CvRect object) {

	double distOffset;

	int objectDistSize = getObjectDistSize(object);
	if (objectDistSize < TARGET_DIST_SIZE) {
		distOffset = (double)(TARGET_DIST_SIZE - objectDistSize) / (double)(TARGET_DIST_SIZE - MAX_DIST_SIZE);
	} else {
		distOffset = (double)(TARGET_DIST_SIZE - objectDistSize) / (double)(MIN_DIST_SIZE - TARGET_DIST_SIZE);
	}

	return distOffset;
}

template <typename T> int sgn(T val) {
    return (T(0) < val) - (val < T(0));
}

#define SPEED_MAX 15
#define RADIUS_MAX 30
#define RADIUS_MIN 12

#define ANGLE_THRESHOLD 0.2
#define DIST_THRESHOLD 0.2

std::string BallTrackerModuleExt::calculateCommand(double angleOffset, double distOffset) {

	double speed, radius;

	speed = fabs(distOffset) * SPEED_MAX;
	radius = angleOffset * RADIUS_MAX;
//	radius = fmax(radius, RADIUS_MIN);

	std::string* jsonString;
	if (fabs(angleOffset) > ANGLE_THRESHOLD) {
		if (distOffset > DIST_THRESHOLD) {
			return mRobot->moveForward(speed, sgn(radius) * 90);
		} else if (distOffset < -DIST_THRESHOLD) {
			return mRobot->moveBackward(speed, sgn(radius) * 90);
		} else {
			if (angleOffset < 0) {
				return mRobot->rotateClockwise(fabs(radius));
			} else if (angleOffset > 0) {
				return mRobot->rotateCounterClockwise(fabs(radius));
			} else {
				// not possible to reach here
				return mRobot->moveStop();
			}
		}
	} else {
		if (distOffset > DIST_THRESHOLD) {
			return mRobot->moveForward(speed);
		} else if (distOffset < -DIST_THRESHOLD) {
			return mRobot->moveBackward(speed);
		} else {
			return mRobot->moveStop();
		}
	}

}

IplImage* BallTrackerModuleExt::decodeFrame(std::vector<int>* readVec) {

	// -- Read the image --
	std::cout << "[BallTracker] Received new image..." << std::endl;
	long_seq::const_iterator it = readVec->begin();
	int dataType = *it++;
	if (dataType != 0)
		return NULL;
	int nArrays = *it++;
	int nDims = *it++;
	if (nDims != 3) {
		std::cerr << "nDims=" << nDims << ", should be 3" << std::endl;
		readVec->clear();
		return NULL;
	}
	int height = *it++;
	int width = *it++;
	int channels = *it++;
	if (channels != 3) {
		std::cerr << "channels=" << channels << ", should be 3" << std::endl;
		readVec->clear();
		return NULL;
	}
	if (readVec->size() < 6+height*width*channels) {
		std::cerr << "read.size=" << readVec->size() << ", should be " << 6+height*width*channels << std::endl;
		readVec->clear();
		return NULL;
	}

	cv::Mat frame(height, width, CV_8UC3); // 3 channel 8bit integer
	//mFrame.create(height, width, CV_8UC3); // 3 channel 8bit integer
	cv::Mat_<cv::Vec3b>::iterator itFrame = frame.begin<cv::Vec3b>(), itFrameEnd = frame.end<cv::Vec3b>();
	for (; itFrame != itFrameEnd; ++itFrame) {
		(*itFrame)[2] = *it++;
		(*itFrame)[1] = *it++;
		(*itFrame)[0] = *it++;
	}

	return new IplImage(frame);

}

IplImage* BallTrackerModuleExt::prepareFrame(IplImage* frame) {

	//	CvSize size = cvGetSize(frame);
	//	printf("[BallTracker] size: w=%d, h=%d\n", size.width, size.height);

	if (!mTracking) {
		//create a blank image and assigned to 'imgTracking' which has the same size of original video
		mImgTracking=cvCreateImage(cvGetSize(frame),IPL_DEPTH_8U, 3);
		cvSetZero(mImgTracking); //covert the image, 'imgTracking' to black
		mTracking = true;
	}

	cvSmooth(frame, frame, CV_GAUSSIAN,3,3); //smooth the original image using Gaussian kernel

	IplImage* imgHSV = cvCreateImage(cvGetSize(frame), IPL_DEPTH_8U, 3);
	cvCvtColor(frame, imgHSV, CV_BGR2HSV); //Change the color format from BGR to HSV
	IplImage* imgThresh = getThresholdedImage(imgHSV);

	cvSmooth(imgThresh, imgThresh, CV_GAUSSIAN,3,3); //smooth the binary image using Gaussian kernel

	//Clean up used images
	cvReleaseImage(&imgHSV);

	return imgThresh;
}

//! Replace with your own code
void BallTrackerModuleExt::Tick() {

	//	std::cout << "[BallTracker] tick" << std::endl;
	std::vector<int>* readVec;
	readVec = readVideo(false);

	if (readVec != NULL && !readVec->empty()) {

		long startTime = get_cur_1ms();
		long endTime;

		IplImage* frame = decodeFrame(readVec);

		if (!frame) {
			std::cerr << "failed to decode frame" << std::endl;
			return;
		}

		frame = cvCloneImage(frame);

		IplImage* imgThresh = prepareFrame(frame);

		//track the possition of the ball
		trackObject(imgThresh);

		if (mDisplay) {
			// Add the tracking image and the frame
			cvAdd(frame, mImgTracking, frame);

			cvShowImage("Ball", imgThresh);
			cvShowImage("Video", frame);
			//			Mat mtx(frame);
			//			imshow("Video", mtx);
		}

		cvReleaseImage(&imgThresh);
		cvReleaseImage(&frame);

		readVec->clear();

		endTime = get_cur_1ms();

		std::cout << "[BallTracker] decoded in " << get_duration(startTime, endTime) << " ms" << std::endl;

		//	} else {
		//		std::cout << "[BallTracker] nothing read..." << std::endl;
	}

	if (mDisplay) {
		//Wait 10mS
		int c = cvWaitKey(100);
	} else {
		usleep(10*1000);
	}

//	mRobot->moveStop();
//	usleep(100*1000);

}

//! Replace with your own code
bool BallTrackerModuleExt::Stop() {
	return false;
}

