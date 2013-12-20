package org.dobots.throttle;

import org.dobots.aim.SimpleAimServiceUI;
import org.dobots.throttle.ThrottleService.ThrottleBinder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class ThrottleModule extends SimpleAimServiceUI {
	
	private static final String TAG = "ThrottleModule";

	private boolean mBound;

	private ThrottleService mThrottle;

	private EditText edtRate;

	private double mRate;

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
			ThrottleBinder binder = (ThrottleBinder) service;
			mThrottle = binder.getThrottle();
			
			mRate = mThrottle.getRate();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, ThrottleService.class);

		Intent intent = new Intent(this, ThrottleService.class);
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

	@Override
	protected void setLayout(int layoutResID) {
		super.setLayout(layoutResID);
		
		edtRate = (EditText) findViewById(R.id.edtRate);
		edtRate.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (!hasFocus) {
					double rate = Double.valueOf(edtRate.getText().toString());
					if (mBound && rate != mRate) {
						mRate = rate;
						mThrottle.setRate(rate);
					}

					InputMethodManager imm = (InputMethodManager) view.getContext()
				            .getSystemService(Context.INPUT_METHOD_SERVICE);
				    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
				}
			}
		});
	}
	
}
