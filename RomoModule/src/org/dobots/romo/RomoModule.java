package org.dobots.romo;

import org.dobots.zmq.ZmqHandler;

import robots.RobotType;
import robots.romo.gui.RomoRobot;
import robots.rover.rover2.ctrl.remote.Rover2RemoteBinder;
import android.os.Bundle;
import android.util.Log;

public class RomoModule extends RomoRobot {

	private static final String TAG = "RomoModule";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		ZmqHandler.initialize(this);
		
		m_eRobot = RobotType.RBT_ROMO;
		// we don't own the robot because it is a service and could have been started
		// before displaying the UI
		m_bOwnsRobot = false;

		// if direct
		Rover2RemoteBinder robot = new Rover2RemoteBinder(this, RomoService.class);
		
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
		
		// we have to call that here because with OwnsRobot=false the robot
		// is not automatically destroyed by the RobotView
		getRobot().destroy();
		
		Log.i(TAG, "onDestroy");
	}
}
