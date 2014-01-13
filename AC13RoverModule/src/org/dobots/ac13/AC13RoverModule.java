package org.dobots.ac13;

import org.dobots.zmq.ZmqHandler;

import robots.RobotType;
import robots.rover.ac13.ctrl.remote.AC13RoverRemoteBinder;
import robots.rover.ac13.gui.AC13RoverUI;
import android.os.Bundle;
import android.util.Log;

public class AC13RoverModule extends AC13RoverUI  {
	
	private static final String TAG = "Ac13RoverModule";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		ZmqHandler.initialize(this);
		
		m_eRobot = RobotType.RBT_AC13ROVER;
		// we don't own the robot because it is a service and could have been started
		// before displaying the UI
		m_bOwnsRobot = false;

		// if direct
		AC13RoverRemoteBinder robot = new AC13RoverRemoteBinder(this, AC13RoverService.class);
		
		// if ipc
		//		RemoteRobotWrapper robot = new Ac13RoverRemote(this, RobotType.RBT_Ac13Rover, Ac13RoverService.class);
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
