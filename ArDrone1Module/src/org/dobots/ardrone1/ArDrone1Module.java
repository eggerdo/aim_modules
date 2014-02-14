package org.dobots.ardrone1;

import org.dobots.zmq.ZmqHandler;

import robots.RobotType;
import robots.parrot.ardrone1.gui.ArDrone1UI;
import robots.parrot.ardrone2.ctrl.remote.ArDrone2Proxy;
import robots.parrot.ctrl.remote.ParrotProxy;
import android.os.Bundle;
import android.util.Log;

public class ArDrone1Module extends ArDrone1UI  {
	
	private static final String TAG = ArDrone1Module.class.getSimpleName();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		ZmqHandler.initialize(this);
		
		m_eRobot = RobotType.RBT_ARDRONE2;
		// we don't own the robot because it is a service and could have been started
		// before displaying the UI
		m_bOwnsRobot = false;

		// if direct
		ParrotProxy robot = new ParrotProxy(this, ArDrone1Service.class);
		
		// if ipc
		//		RemoteRobotWrapper robot = new Rover2Remote(this, RobotType.RBT_ROVER2, Rover2Service.class);
		//		robot.setHandler(m_oUiHandler);
		
		setRobot(robot);
		
		// zmq handler and robot type have to be assigned before calling the parent's
		// onCreate
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		getRobot().setHandler(null);
		
		// we have to call that here because with OwnsRobot=false the robot
		// is not automatically destroyed by the RobotView
		getRobot().destroy();
		
		Log.i(TAG, "onDestroy");
	}

}
