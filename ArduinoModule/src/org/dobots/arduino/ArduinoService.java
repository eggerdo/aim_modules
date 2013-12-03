package org.dobots.arduino;

import java.util.HashMap;

import org.dobots.aimrobotlibrary.AimRobotService;
import org.dobots.lib.comm.msg.ISensorDataListener;
import org.dobots.lib.comm.msg.RoboCommands.BaseCommand;
import org.dobots.lib.comm.msg.RoboCommands.ControlCommand;
import org.dobots.utilities.BaseActivity;
import org.dobots.zmq.ZmqHandler;
import org.dobots.zmq.sensors.ZmqSensorsReceiver;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

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
import android.os.Messenger;


public class ArduinoService extends AimRobotService implements ISensorDataListener {
	
	private static final String MODULE_NAME = "ArduinoModule";

	Arduino mArduino;

	private ZmqSensorsReceiver m_oSensorsReceiver;
	
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
		
		ZMQ.Socket sensorRecvSocket = ZmqHandler.getInstance().obtainSensorsRecvSocket();
		sensorRecvSocket.subscribe(mArduino.getID().getBytes());
		m_oSensorsReceiver = new ZmqSensorsReceiver(sensorRecvSocket, "ArduinoSensorService");
		m_oSensorsReceiver.setSensorDataListener(this);
		m_oSensorsReceiver.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getRobot().destroy();
		m_oSensorsReceiver.destroy();
	}

	public void defineOutMessenger(HashMap<String, Messenger> list) {
		super.defineOutMessenger(list);
		list.put("sensors", null);
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

	@Override
	public void onSensorData(String data) {
		dataSend(getOutMessenger("sensors"), data);
	}
}
