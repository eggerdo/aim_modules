package org.dobots.duplicator;

import java.util.HashMap;

import org.dobots.aim.AimProtocol;
import org.dobots.aim.AimProtocol.AimDataTypeException;
import org.dobots.aim.AimService;
import org.dobots.aim.AimUtils;

import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public class DuplicatorService extends AimService {
	
	private static final String MODULE_NAME = "DuplicatorModule";

	@Override
	public String getModuleName() {
		return MODULE_NAME;
	}
	
	private Messenger inMessenger = new Messenger(new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case AimProtocol.MSG_PORT_DATA:
				String data;
				try {
					data = AimUtils.getStringData(msg);
				
					Messenger messenger;
					
					messenger = getOutMessenger("out1");
					if (messenger != null) { 
						sendData(messenger, data);
					}
	
					messenger = getOutMessenger("out2");
					if (messenger != null) {
						sendData(messenger, data);
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

	@Override
	public void defineInMessenger(HashMap<String, Messenger> list) {
		list.put("in", inMessenger);
	}

	@Override
	public void defineOutMessenger(HashMap<String, Messenger> list) {
		list.put("out1", null);
		list.put("out2", null);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
