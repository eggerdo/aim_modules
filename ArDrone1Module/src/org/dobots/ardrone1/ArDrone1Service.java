package org.dobots.ardrone1;

import org.dobots.aimrobotlibrary.AimRobotService;

import robots.parrot.ardrone1.ctrl.ArDrone1;
import robots.remote.RobotServiceBinder;

public class ArDrone1Service extends AimRobotService {

	private static final String MODULE_NAME = "ArDrone1Module";
	
	@Override
	public void onCreate() {
		super.onCreate();
		setRobot(new RobotServiceBinder(new ArDrone1()));
	}

	@Override
	public String getModuleName() {
		return MODULE_NAME;
	}
	
}
