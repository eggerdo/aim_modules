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

//#include <highgui.h>

using namespace cv;
using namespace rur;

#define PI 3.1416
#define MOVE_THRESHOLD 5

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
	mLowerV	= 120;

	mUpperH	= 130;
	mUpperS	= 256;
	mUpperV	= 256;

	mArea = 1000;

	//	capture =0;
	//	capture = cvCaptureFromCAM(0);
	//	if(!capture){
	//		printf("[BallTracker] Capture failure\n");
	//		return;
	//	}

	//	IplImage* frame=0;
	//	frame = cvQueryFrame(capture);
	//	if(!frame) return;

//	if (mDisplay) {
//		//		namedWindow("Video");
//		//		namedWindow("Ball");
//		cvNamedWindow("Video");
//		cvNamedWindow("Ball");
//	}

	setwindowSettings();
}

//! Replace with your own code
BallTrackerModuleExt::~BallTrackerModuleExt() {

	if (mDisplay) {
		cvDestroyAllWindows() ;
	}

	cvReleaseImage(&mImgTracking);
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
void BallTrackerModuleExt::setwindowSettings() {
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

			double distance = sqrt(pow(posY - mLastY, 2) + pow(posX - mLastX, 2));
			if (distance > MOVE_THRESHOLD) {
				double angle = atan2((posY - mLastY), (posX - mLastX));
				extractDirection(mImgTracking, angle, cvScalar(0, 0, 255), 50, 10);
				mLastAngle = angle;
				//			} else {
				//				extractDirection(mImgTracking, mLastAngle, cvScalar(0, 0, 255), 50, 10);
			}

		}

		mLastX = posX;
		mLastY = posY;
	}

	free(moments);

	return boundingRect;
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

		//	} else {
		//		std::cout << "[BallTracker] nothing read..." << std::endl;
	}

	if (mDisplay) {
		//Wait 10mS
		int c = cvWaitKey(100);
	} else {
		usleep(10*1000);
	}

}

//! Replace with your own code
bool BallTrackerModuleExt::Stop() {
	return false;
}

