package org.dobots.throttle;

import java.util.HashMap;

import org.dobots.aim.AimProtocol;
import org.dobots.aim.AimProtocol.AimDataTypeException;
import org.dobots.aim.AimService;
import org.dobots.aim.AimUtils;

import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public class ThrottleService extends AimService {

	public static final String MODULE_NAME = "ThrottleModule";

	private long mNextSendTime;
	private double mRate;
	
	@Override
	public String getModuleName() {
		return MODULE_NAME;
	}

	private Messenger mPortInMessenger = new Messenger(new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case AimProtocol.MSG_PORT_DATA:
				String data;
				try {
					data = AimUtils.getStringData(msg);
				
					Messenger messenger = getOutMessenger("out");
					if (messenger != null) { 
						if (mNextSendTime < System.currentTimeMillis()) {
							sendData(messenger, data);
							
							mNextSendTime = System.currentTimeMillis() + (long)(1000.0 / mRate);
						}
					}
				} catch (AimDataTypeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			default:
				super.handleMessage(msg);
			}
		}
		
	});
	
	public void setRate(double rate) {
		if (mRate != rate) {
			mRate = rate;
			mNextSendTime = System.currentTimeMillis();
		}
	}

	public double getRate() {
		return mRate;
	}

	@Override
	public void defineInMessenger(HashMap<String, Messenger> list) {
		list.put("in", mPortInMessenger);
	}

	@Override
	public void defineOutMessenger(HashMap<String, Messenger> list) {
		list.put("out", null);
	}


	private IBinder mBinder = new ThrottleBinder();
	public class ThrottleBinder extends Binder {

		public ThrottleService getThrottle() {
			return ThrottleService.this;
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

}
