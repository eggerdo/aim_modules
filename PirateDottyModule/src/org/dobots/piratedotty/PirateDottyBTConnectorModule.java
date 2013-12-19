package org.dobots.piratedotty;

import robots.gui.comm.IRobotConnection;
import robots.gui.comm.bluetooth.BluetoothConnection;
import robots.gui.comm.bluetooth.BluetoothConnectionHelper.BTEnableCallback;
import robots.piratedotty.ctrl.IPirateDotty;
import robots.piratedotty.ctrl.PirateDottyTypes;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

/**
 * This Module uses the ArduinoModule as the base type for it's functionality,
 * but it's sole purpose is to get the bluetooth device from the bluetooth adapter
 * based on the address. with the obtained device, a bluetooth connection is
 * created and assigned to the robot. after that the module is closed again
 * 
 * be sure to use android:theme="@android:style/Theme.NoDisplay" in the Manifest
 * for this Activity to skip showing the UI, since we only need the connecting
 * functionality
 * 
 * @author dominik
 *
 */
public class PirateDottyBTConnectorModule extends PirateDottyModule {
	
	String mAddress;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mAddress = getIntent().getStringExtra("address");
		getIntent().removeExtra("address");
		
		super.onCreate(savedInstanceState);
	}
	
	public void onRobotCtrlReady() {
		
		m_oBTHelper.initBluetooth(new BTEnableCallback() {
			
			@Override
			public void onEnabled() {
				BluetoothDevice device = m_oBTHelper.getRemoteDevice(mAddress);
				IRobotConnection connection = new BluetoothConnection(device, PirateDottyTypes.PIRATEDOTTY_UUID);

				((IPirateDotty)getRobot()).setConnection(connection);
				finish();
			}
		});
	}
	
}
