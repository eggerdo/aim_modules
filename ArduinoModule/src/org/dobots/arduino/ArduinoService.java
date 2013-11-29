package org.dobots.arduino;

import org.dobots.aimrobotlibrary.AimRobotService;
import org.dobots.lib.comm.msg.RoboCommands.BaseCommand;
import org.dobots.lib.comm.msg.RoboCommands.ControlCommand;
import org.dobots.utilities.BaseActivity;

import robots.RobotType;
import robots.arduino.ctrl.Arduino;
import robots.arduino.ctrl.ArduinoTypes;
import robots.arduino.gui.ArduinoBluetooth;
import robots.gui.BluetoothRobot;
import robots.gui.BluetoothRobot.BTDeviceCallback;
import robots.remote.RobotServiceBinder;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;


public class ArduinoService extends AimRobotService {
	
	private static final String MODULE_NAME = "ArduinoModule";

	Arduino mArduino;
	
	@Override
	public String getModuleName() {
		// TODO Auto-generated method stub
		return MODULE_NAME;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mArduino = new Arduino();
		setRobot(new RobotServiceBinder(mArduino));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getRobot().destroy();
	}

	@Override
	protected void handleData(BaseCommand cmd) {

		if ((cmd instanceof ControlCommand) && ((ControlCommand)cmd).mCommand.equals("setConnection")) {
			setConnection((String)((ControlCommand)cmd).getParameter(0));
		} else {
			super.handleData(cmd);
		}
	}
	
	private void setConnection(String address) {
		Intent intent = new Intent(this, ArduinoBTConnectorModule.class);
		intent.putExtra("address", address);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
}
