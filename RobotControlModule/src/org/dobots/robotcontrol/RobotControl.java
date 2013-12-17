package org.dobots.robotcontrol;

import java.util.HashMap;

import org.dobots.aim.AimProtocol;
import org.dobots.aim.AimProtocol.AimDataTypeException;
import org.dobots.aim.AimUtils;
import org.dobots.aimrobotlibrary.AimRobotActivity;
import org.dobots.lib.comm.msg.RoboCommands.BaseCommand;
import org.dobots.utilities.Utils;
import org.dobots.utilities.VerticalSeekBar;
import org.dobots.zmq.video.gui.VideoHelper;

import robots.ctrl.control.RemoteControlHelper;
import robots.ctrl.control.RemoteControlSender;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class RobotControl extends AimRobotActivity {

	private static final String TAG = "RobotControl";
	private static final String MODULE_NAME = "RobotControlModule";

	private static final int MENU_CAMERA_CONTROL = 1;
	
	private Messenger mPortVideoInMessenger = new Messenger(new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AimProtocol.MSG_PORT_DATA:
				// handle video data
				try {
					handleVideo(AimUtils.getStringData(msg));
				} catch (AimDataTypeException e) {
					e.printStackTrace();
				}
				break;
			default:
				super.handleMessage(msg);
			}
		}
	});
	
	private LinearLayout m_layVideo;
	
	private VideoHelper mVideoHelper;

	private LinearLayout m_layCameraCtrl;

	private VerticalSeekBar m_sbCamera;

	private Button m_btnToggle;
	private boolean m_bCameraControl = false;
	
	private RemoteControlHelper m_oRemoteCtrl;
	
	@Override
	public String getModuleName() {
		return MODULE_NAME;
	}

	@Override
	public String getTag() {
		return TAG;
	}

	@Override
	public void defineInMessenger(HashMap<String, Messenger> list) {
		list.put("video", mPortVideoInMessenger);
	}

	@Override
	public void defineOutMessenger(HashMap<String, Messenger> list) {
		list.put("cmd", null);
	}

	@Override
	public void onAimStop() {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				finish();
			}
		});
	}
	
	class ControlHelper extends RemoteControlSender {
		
		@Override
		protected void sendCommand(BaseCommand i_oCmd) {
			sendData(getOutMessenger("cmd"), i_oCmd.toJSONString());
		}

		@Override
		public void close() {
		}
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setProperties();
		
		ControlHelper helper = new ControlHelper();
		m_oRemoteCtrl = new RemoteControlHelper(this);
		m_oRemoteCtrl.setCameraControlListener(helper);
		m_oRemoteCtrl.setDriveControlListener(helper);

		mVideoHelper = new VideoHelper(this, m_layVideo);
		mVideoHelper.setVideoTimeout(false);
		
		if (savedInstanceState != null) {
			m_oRemoteCtrl.setJoystickControl(savedInstanceState.getBoolean("joystickCtrl"));
			m_bCameraControl = savedInstanceState.getBoolean("cameraCtrl");
		}

		showCameraControl(m_bCameraControl);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("joystickCtrl", m_oRemoteCtrl.isJoystickControl());
		outState.putBoolean("cameraCtrl", m_bCameraControl);

		super.onSaveInstanceState(outState);
	}
	
	private void setProperties() {
		setContentView(R.layout.main);
		
		m_layVideo = (LinearLayout) findViewById(R.id.layVideo);
		
		m_layCameraCtrl = (LinearLayout) findViewById(R.id.layCameraControl);
		
		m_sbCamera = (VerticalSeekBar) findViewById(R.id.sbCamera);
		m_sbCamera.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar paramSeekBar) {
				m_sbCamera.setNewProgress(50);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar paramSeekBar) {
				m_sbCamera.getProgress();
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (progress < 50) {
					m_oRemoteCtrl.cameraDown();
				} else if (progress > 50) {
					m_oRemoteCtrl.cameraUp();
				} else {
					m_oRemoteCtrl.cameraStop();
				}
			}
		});
		
		m_btnToggle = (Button) findViewById(R.id.btnToggle);
		m_btnToggle.getBackground().setAlpha(99);
		m_btnToggle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View paramView) {
				m_oRemoteCtrl.toggleCamera();
			}
		});
	}
	
	private void handleVideo(String videoFrame) {

		byte[] frame = Base64.decode(videoFrame, Base64.NO_WRAP);
		mVideoHelper.onFrame(frame, -1);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mVideoHelper != null) {
			mVideoHelper.destroy();
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		stopVideo();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		startVideo();
	}

    private void startVideo() {
    	if (mVideoHelper != null) {
			mVideoHelper.onStartVideo(false);
    	}
    }
    
    private void stopVideo() {
		if (mVideoHelper != null) {
			mVideoHelper.onStopVideo();
		}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	menu.add(Menu.NONE, MENU_CAMERA_CONTROL, MENU_CAMERA_CONTROL, "Camera Control ON");
    	
    	return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	Utils.updateOnOffMenuItem(menu.findItem(MENU_CAMERA_CONTROL), m_bCameraControl);
    	
    	return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case MENU_CAMERA_CONTROL:
    		showCameraControl(!m_bCameraControl);
    		return true;
    	}
    	
    	return super.onOptionsItemSelected(item);
    }

	private void showCameraControl(boolean show) {
		m_bCameraControl = show;
		Utils.showView(m_layCameraCtrl, m_bCameraControl);
	}

}
