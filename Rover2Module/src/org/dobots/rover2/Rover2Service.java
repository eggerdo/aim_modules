package org.dobots.rover2;

import org.dobots.aimrobotlibrary.AimRobotService;

import robots.remote.RobotServiceBinder;
import robots.rover.rover2.ctrl.Rover2;

public class Rover2Service extends AimRobotService {

	private static final String MODULE_NAME = "Rover2Module";
	
	@Override
	public void onCreate() {
		super.onCreate();
		setRobot(new RobotServiceBinder(new Rover2()));
	}

	@Override
	public String getModuleName() {
		return MODULE_NAME;
	}
	
}
