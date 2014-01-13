package org.dobots.cameramodule;

import java.util.HashMap;
import java.util.List;

import org.dobots.aim.AimProtocol;
import org.dobots.aim.AimService;
import org.dobots.comm.msg.RoboCommands;
import org.dobots.comm.msg.RoboCommands.BaseCommand;
import org.dobots.comm.msg.RoboCommands.CameraCommand;
import org.dobots.comm.msg.RoboCommands.ControlCommand;
import org.dobots.utilities.ThreadMessenger;
import org.dobots.utilities.camera.CameraPreview;
import org.dobots.utilities.camera.CameraPreview.CameraPreviewCallback;
import org.dobots.utilities.log.AndroidLogger;
import org.dobots.utilities.log.Logger;
import org.dobots.zmq.video.IRawVideoListener;
import org.dobots.zmq.video.VideoThrottle;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera.Size;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class CameraService extends AimService implements IRawVideoListener {

	private static final String TAG = "CameraService";
	private static final String MODULE_NAME = "CameraModule";
	
	private static final String PREFS_PREVIEWWIDTH = "previewWidth";
	private static final String PREFS_PREVIEWHEIGHT = "previewHeight";
	private static final String PREFS_FRAMERATE = "frameRate";
	private static final String PREFS_AUTOEXPOSURE = "autoExposure";
	
	private static final int DEF_PREVIEWWIDTH = 320;
	private static final int DEF_PREVIEWHEIGHT = 240;
	private static final float DEF_FRAMERATE = 20F;
	private static final boolean DEF_AUTOEXPOSURE = true;

	private CameraPreview mCameraPreview;
	private VideoThrottle mVideoThrottle;

	// settings, stored as shared preferences
	private int mWidth;
	private int mHeight;
	private float mFrameRate;
	private boolean mAutoExposureEnabled;
	
	@Override
	public String getModuleName() {
		return MODULE_NAME;
	}

	@Override
	public String getTag() {
		return TAG;
	}

	// a ThreadMessenger is a thread with a messenger. messages are
	// handled by that thread
	// a normal Messenger uses the thread that creates the messenger
	// to handle messages (which in this case would be the main (UI)
	// thread
	private ThreadMessenger mPortCmdInReceiver = new ThreadMessenger("PortCmdInMessenger") {

		@Override
		public boolean handleIncomingMessage(Message msg) {
			switch (msg.what) {
			case AimProtocol.MSG_PORT_DATA:
				// do we need to check datatype to make sure it is string?
				String data = msg.getData().getString("data");
				BaseCommand cmd = RoboCommands.decodeCommand(data);

				if (cmd instanceof ControlCommand) {
					ControlCommand control = (ControlCommand)cmd;
					if (control.mCommand.equals("setFrameRate")) {
						setFrameRate((Double)control.getParameter(0));
					} else if (control.mCommand.equals("setSize")) {
						setPreviewSize((Integer)control.getParameter(0), (Integer)control.getParameter(1));
					} else if (control.mCommand.equals("setAutoExposure")) {
						setAutoExposure((Boolean)control.getParameter(0));
					}
				} else if (cmd instanceof CameraCommand) {
					CameraCommand camera = (CameraCommand)cmd;
					switch(camera.eType) {
					case TOGGLE:
						mCameraPreview.toggleCamera();
						break;
					case ON:
						mCameraPreview.startCamera();
						break;
					case OFF:
						mCameraPreview.stopCamera();
						break;
					default:
						break;
					}
				}
				break;
			default:
				return false;
			}
			return true;
		}
	};

	public void defineInMessenger(HashMap<String, Messenger> list) {
		list.put("cmd", mPortCmdInReceiver.getMessenger());
	}

	public void defineOutMessenger(HashMap<String, Messenger> list) {
		list.put("video_raw", null);
		list.put("video_base64", null);
		//		list.put("video_yuv", null);
	}

	private IBinder mBinder = new CameraBinder();
	public class CameraBinder extends Binder {

		public CameraService getCamera() {
			return CameraService.this;
		}

	}

	public void setFrameRate(double rate) {
		Log.d(TAG, String.format("setFrameRate(%d)", rate));
		
		mVideoThrottle.setFrameRate(rate);

		mFrameRate = (float) rate;
		adjustSettings();
	}

	public double getFrameRate() {
		return mVideoThrottle.getFrameRate();
	}

	public void setPreviewSize(int width, int height) {
		Log.d(TAG, String.format("setPreviewSize(%d, %d)", width, height));
		
		mCameraPreview.setPreviewSize(width, height);

		mWidth = width;
		mHeight = height;
		adjustSettings();
	}

	public Size getPreviewSize() {
		return mCameraPreview.getPreviewSize();
	}

	public List<Size> getSupportedPreviewSizes() {
		return mCameraPreview.getSupportedPreviewSizes();
	}

	public void setAutoExposure(boolean enable) {
		Log.d(TAG, String.format("setAutoExposure(%b)", enable));
		
		mCameraPreview.setAutoExposure(enable);
		
		mAutoExposureEnabled = enable;
		adjustSettings();
	}
	
	public boolean isAutoExposureSupported() {
		return mCameraPreview.isAutoExposureLockSupported();
	}
	
	public boolean isAutoExposureEnabled() {
		return mCameraPreview.isAutoExposureEnabled();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Logger.setLogger(new AndroidLogger());
		
		readSettings();

		mCameraPreview = CameraPreview.createCameraWithoutSurface(this);
		mCameraPreview.setPreviewSize(mWidth, mHeight);
		mCameraPreview.setAutoExposure(mAutoExposureEnabled);

		mVideoThrottle = new VideoThrottle("videoThrottle");
		mVideoThrottle.setRawVideoListener(this);
		mVideoThrottle.setFrameRate(mFrameRate);

		mCameraPreview.setFrameListener(new CameraPreviewCallback() {

			@Override
			public void onFrame(byte[] rgb, int width, int height, int rotation) {
				mVideoThrottle.onFrame(rgb, rotation);
			}
		});
		//		mCameraPreview.setYuvListener(new CameraPreviewCallback() {
		//			@Override
		//			public void onFrame(byte[] rgb, int width, int height, int rotation, int id) {
		//				mVideoThrottle.onFrame(rgb, rotation, id);
		//			}
		//		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mCameraPreview.destroy();
		mVideoThrottle.stopThread();
		mPortCmdInReceiver.destroy();
	}

	@Override
	public void onFrame(byte[] rgb, int rotation) {
		
		if (getOutMessenger("video_raw") != null) {
			Bundle data = setVideoData(rgb);
			sendData(getOutMessenger("video_raw"), data);
		}

		if (getOutMessenger("video_base64") != null) {
			String base64 = android.util.Base64.encodeToString(rgb, android.util.Base64.NO_WRAP);
			sendData(getOutMessenger("video_base64"), base64);
		}

		//		if (getOutMessenger("yuv") != null) {
		//			Bundle data = setVideoData(rgb, rotation);
		//			sendData(getOutMessenger("bmp"), data);
		//		}
	}

	private Bundle setVideoData(byte[] rgb) {

		// calculate length
		int length = 1; // 1 field for the number of structures
		length += 2; // 2 fields per structure, 1 for number of dim, 1 for length of dim
		length += rgb.length; // number of rgb elements

		// create aim array
		int[] aimArray = new int[length];

		// fill array
		aimArray[0] = 1; // number of 'structures'

		int index = 1;

		// rgb
		aimArray[index++] = 1; // number of dimensions of 'structure'
		aimArray[index++] = rgb.length; // length of the dimension
		for (int j = 0; j < rgb.length; ++j) {
			aimArray[index++] = rgb[j];
		}

		Bundle bundle = new Bundle();
		bundle.putInt("datatype", AimProtocol.DATATYPE_INT_ARRAY);
		bundle.putIntArray("data", aimArray);
		return bundle;
	}


	private void readSettings() {
		SharedPreferences prefs = getSharedPreferences("cameraSettings", Context.MODE_PRIVATE);
		mWidth = prefs.getInt(PREFS_PREVIEWWIDTH, DEF_PREVIEWWIDTH);
		mHeight = prefs.getInt(PREFS_PREVIEWHEIGHT, DEF_PREVIEWHEIGHT);
		mFrameRate = prefs.getFloat(PREFS_FRAMERATE, DEF_FRAMERATE);
		mAutoExposureEnabled = prefs.getBoolean(PREFS_AUTOEXPOSURE, DEF_AUTOEXPOSURE);
	}

	private void adjustSettings() {

		SharedPreferences prefs = getSharedPreferences("cameraSettings", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.putInt(PREFS_PREVIEWWIDTH, mWidth);
		editor.putInt(PREFS_PREVIEWHEIGHT, mHeight);
		editor.putFloat(PREFS_FRAMERATE, mFrameRate);
		editor.putBoolean(PREFS_AUTOEXPOSURE, mAutoExposureEnabled);
		editor.commit();
		
	}
}
