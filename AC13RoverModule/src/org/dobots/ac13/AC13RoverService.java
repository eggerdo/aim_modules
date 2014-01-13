package org.dobots.ac13;


import org.dobots.aimrobotlibrary.AimRobotService;

import robots.remote.RobotServiceBinder;
import robots.rover.ac13.ctrl.AC13Rover;
import robots.rover.rover2.ctrl.Rover2;

public class AC13RoverService extends AimRobotService {

	private static final String MODULE_NAME = "Ac13RoverModule";
	
	@Override
	public void onCreate() {
		super.onCreate();
		setRobot(new RobotServiceBinder(new AC13Rover()));
	}

	@Override
	public String getModuleName() {
		return MODULE_NAME;
	}
	
}
