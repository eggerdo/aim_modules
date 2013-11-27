package org.dobots.cameramodule;

import java.util.HashMap;

import org.dobots.aim.AimProtocol;
import org.dobots.aim.AimService;
import org.dobots.communication.msg.RoboCommands;
import org.dobots.communication.msg.RoboCommands.BaseCommand;
import org.dobots.communication.msg.RoboCommands.CameraCommand;
import org.dobots.communication.msg.RoboCommands.ControlCommand;
import org.dobots.communication.video.IRawVideoListener;
import org.dobots.communication.video.VideoThrottle;
import org.dobots.utilities.CameraPreview;
import org.dobots.utilities.CameraPreview.CameraPreviewCallback;
import org.dobots.utilities.ThreadMessenger;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;

public class CameraService extends AimService implements SurfaceHolder.Callback, IRawVideoListener {

	private static final String TAG = "CameraService";
	private static final String MODULE_NAME = "CameraModule";
	
	private CameraPreview mCameraPreview;
	private WindowManager mWindowManager;
	private VideoThrottle mVideoThrottle;
	
	@Override
	public String getModuleName() {
		// TODO Auto-generated method stub
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
						mVideoThrottle.setFrameRate((Double)control.getParameter(0));
					} else if (control.mCommand.equals("setSize")) {
						mCameraPreview.setPreviewSize((Integer)control.getParameter(0), (Integer)control.getParameter(1));
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
	private LayoutParams params;
	
	public void defineInMessenger(HashMap<String, Messenger> list) {
		list.put("cmd", mPortCmdInReceiver.getMessenger());
	}

	public void defineOutMessenger(HashMap<String, Messenger> list) {
		list.put("video", null);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		// On some Android devices, Camera Preview is only available if
		// a valid SurfaceView is provided. Valid meaning not a dummy SurfaceView.
		// to get a valid SurfaceView in a Service which doesn't have a layout we
		// create a dummy SurfaceView and assign it to the window manager. the window
		// manager then creates the underlying surface so that the camera preview can
		// be obtained.
		mCameraPreview = new CameraPreview(getApplicationContext());
		mCameraPreview.setPreviewSize(320, 240);
		
		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		
		// we assign dummy values as width and height to the surfaceview. we actually
		// want it to be invisible, but we cannot assign 0 as width and hight or the
		// surface would not be created. once the surface is created we can then make
		// it invisible
		params = new WindowManager.LayoutParams(1, 1,
		            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
		            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
		            PixelFormat.TRANSLUCENT);    
		
		// register callback to be notified once the surface is created
		mCameraPreview.getHolder().addCallback(this);
	    
		// add the view to the window manager to let it create the surface
		mWindowManager.addView(mCameraPreview, params);
		
		mVideoThrottle = new VideoThrottle("videoThrottle");
		mVideoThrottle.setRawVideoListener(this);
		mVideoThrottle.setFrameRate(1.0);
		
		mCameraPreview.setFrameListener(new CameraPreviewCallback() {
			
			@Override
			public void onFrame(byte[] rgb, int width, int height, int rotation) {
				mVideoThrottle.onFrame(rgb, rotation);
			}
		});
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		// we have to make the camera preview visible again otherwise the
		// surface doesn't get destroyed (and the camera continues running)
		LayoutParams params = mCameraPreview.getLayoutParams();
		params.height = 1;
		params.width = 1;
		mWindowManager.updateViewLayout(mCameraPreview, params);
		
		// remove the view
		mWindowManager.removeView(mCameraPreview);
		
		mPortCmdInReceiver.destroy();
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d(getTag(), "surfaceChanged");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(getTag(), "surfaceCreated");
		
		// once the surface for the camera preview is created, we make it disappear 
		// by setting the width and height to 0
		LayoutParams params = mCameraPreview.getLayoutParams();
		params.height = 0;
		params.width = 0;
		mWindowManager.updateViewLayout(mCameraPreview, params);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(getTag(), "surfaceDestroyed");
	}

	@Override
	public void onFrame(byte[] rgb, int rotation) {
		if (getOutMessenger("video") != null) {
			String base64 = android.util.Base64.encodeToString(rgb, android.util.Base64.NO_WRAP);
			
			JSONObject json = new JSONObject();
			try {
				json.put("base64", base64);
				json.put("rotation", rotation);

				mAimConnectionHelper.sendData(getOutMessenger("video"), json.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
