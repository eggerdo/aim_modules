package org.dobots.cameramodule;

import java.util.List;

import org.dobots.aim.SimpleAimServiceUI;
import org.dobots.cameramodule.CameraService.CameraBinder;
import org.dobots.utilities.Utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;

public class CameraModule extends SimpleAimServiceUI {

	private static final String TAG = "CameraModule";
	
	private boolean mBound = false;
	
	private CameraService mCamera = null;

	private EditText edtFrameRate;
	private Spinner spPreviewSizes;
	private CheckBox cbAutoExposure;
	
	private boolean mAutoExposureEnabled;
	private double mFrameRate;
	private Size mPreviewSize;
	private List<Size> mSupportedPreviewSizes;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, CameraService.class, R.layout.main);

		Intent intent = new Intent(this, CameraService.class);
		this.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
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
			
			if (mCamera.isAutoExposureSupported()) {
				mAutoExposureEnabled = mCamera.isAutoExposureEnabled();
				cbAutoExposure.setChecked(mAutoExposureEnabled);
			} else {
				cbAutoExposure.setVisibility(View.GONE);
			}
		}
	};

	@Override
	protected void setLayout(int layoutResID) {
		super.setLayout(layoutResID);
		
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
		
		cbAutoExposure = (CheckBox) findViewById(R.id.cbAutoExposure);
		cbAutoExposure.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (mBound && mAutoExposureEnabled != isChecked) {
					mCamera.setAutoExposure(isChecked);
				}
			}
		});
	}
	
	@Override
	protected void startService() {
		// first start to the service ...
		super.startService();
		
		// ... then bind to it. don't start the service by binding to it
		// otherwise the service will be stopped when the the module unbinds.
		Intent intent = new Intent(this, CameraService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

    protected void stopService() {
    	// first unbind from the service, otherwise the service won't get stopped ...
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}

		super.stopService();
	}
}
