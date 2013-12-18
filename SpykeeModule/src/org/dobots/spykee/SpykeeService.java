package org.dobots.spykee;

import org.dobots.aimrobotlibrary.AimRobotService;

import robots.remote.RobotServiceBinder;
import robots.spykee.ctrl.Spykee;

public class SpykeeService extends AimRobotService {

	private static final String MODULE_NAME = "SpykeeModule";
	
	@Override
	public void onCreate() {
		super.onCreate();
		setRobot(new RobotServiceBinder(new Spykee()));
	}

	@Override
	public String getModuleName() {
		return MODULE_NAME;
	}
	
}
