package org.dobots.cameramodule;

import java.util.List;

import org.dobots.cameramodule.CameraService.CameraBinder;
import org.dobots.utilities.Utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class CameraModule extends Activity {

	private static final int WATCHDOG_INTERVAL = 500;
	
	private static final String TAG = "CameraModule";
	
	private boolean mBound = false;
	
    private Handler mUiHandler = new Handler();
    
	private CameraService mCamera = null;

	private ToggleButton btnStartStop;
	private TextView txtMessageStatus;

	private EditText edtFrameRate;
	private Spinner spPreviewSizes;
	
	private double mFrameRate;

	private Size mPreviewSize;
	private List<Size> mSupportedPreviewSizes;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = new Intent(this, CameraService.class);
		this.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		
		setLayout();
		
		mUiHandler.postDelayed(mWatchdog, WATCHDOG_INTERVAL);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}
	
	class CameraSize {
		
		Size mSize;
		
		public CameraSize(Size size) {
			mSize = size;
		}

		public String toString() {
			return String.format("%d x %d", mSize.width, mSize.height);
		}
		
		public Size getSize() {
			return mSize;
		}
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.i(TAG, "disconnected from service...");
			mBound = false;
		}

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.i(TAG, "connected to service... " + service.toString());
			
			mBound = true;
			CameraBinder binder = (CameraBinder) service;
			mCamera = binder.getCamera();
			
			mFrameRate = mCamera.getFrameRate();
			edtFrameRate.setText(String.valueOf(mFrameRate));
			
			mPreviewSize = mCamera.getPreviewSize();
			
			mSupportedPreviewSizes = mCamera.getSupportedPreviewSizes();
			CameraSize[] list = new CameraSize[mSupportedPreviewSizes.size()];
			
			int selection = -1;
			for (int i = 0; i < mSupportedPreviewSizes.size(); ++i) {
				Size size = mSupportedPreviewSizes.get(i);
				list[i] = new CameraSize(size);
				if (mPreviewSize.equals(size)) {
					selection = i;
				}
			}
			
			ArrayAdapter<CameraSize> adapter = new ArrayAdapter<CameraSize>(CameraModule.this, android.R.layout.simple_spinner_dropdown_item, list);
			spPreviewSizes.setAdapter(adapter);
			Utils.setSpinnerSelectionWithoutCallingListener(spPreviewSizes, selection);
		}
	};

	private void setLayout() {
		setContentView(R.layout.main);
		
		btnStartStop = (ToggleButton) findViewById(R.id.btnStartStop);
		btnStartStop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!btnStartStop.isChecked()) {
					stopService();
				} else {
					startService();
				}
			}
		});
		
		txtMessageStatus = (TextView) findViewById(R.id.txtModuleStatus);
		
		edtFrameRate = (EditText) findViewById(R.id.edtFrameRate);
		edtFrameRate.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (!hasFocus) {
					double frameRate = Double.valueOf(edtFrameRate.getText().toString());
					if (mBound && frameRate != mFrameRate) {
						mFrameRate = frameRate;
						mCamera.setFrameRate(frameRate);
					}

					InputMethodManager imm = (InputMethodManager) view.getContext()
				            .getSystemService(Context.INPUT_METHOD_SERVICE);
				    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
				}
			}
		});
		
		spPreviewSizes = (Spinner) findViewById(R.id.spPreviewSizes);
		spPreviewSizes.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				CameraSize cameraSize = (CameraSize)parent.getItemAtPosition(position);
				Size size = cameraSize.getSize();
				if (mBound && size != mPreviewSize) {
					mCamera.setPreviewSize(size.width, size.height);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	private void startService() {
		Intent intent = new Intent(this, CameraService.class);
		intent.putExtra("id", 0); // Default id
		
		// first start to the service ...
		startService(intent);
		Log.i(TAG, "Starting: " + intent.toString());
		
		// ... then bind to it. don't start the service by binding to it
		// otherwise the service will be stopped when the the module unbinds.
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

    private void stopService() {
    	// first unbind from the service, otherwise the service won't get stopped ...
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}

		// ... then stop the service
		Intent intent = new Intent(this, CameraService.class);
		stopService(intent);
		
		Log.i(TAG, "Stopping service: " + intent.toString());
	}

    private boolean isServiceRunning(Class serviceClass) {
		Log.d(TAG, "Checking if service is running...");
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
    
    private Runnable mWatchdog = new Runnable() {
		
		@Override
		public void run() {
			if (isServiceRunning(CameraService.class)){
				txtMessageStatus.setText("Module running");
				btnStartStop.setChecked(true);
			} else {
				txtMessageStatus.setText("Module stopped");
				btnStartStop.setChecked(false);
			}
			mUiHandler.postDelayed(this, WATCHDOG_INTERVAL);
		}
	};
}
