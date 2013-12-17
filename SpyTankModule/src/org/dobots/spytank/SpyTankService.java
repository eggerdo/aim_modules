package org.dobots.spytank;

import org.dobots.aimrobotlibrary.AimRobotService;

import robots.remote.RobotServiceBinder;
import robots.spytank.ctrl.SpyTank;

public class SpyTankService extends AimRobotService {

	private static final String MODULE_NAME = "SpyTankModule";

	@Override
	public void onCreate() {
		super.onCreate();
		setRobot(new RobotServiceBinder(new SpyTank()));
	}

	@Override
	public String getModuleName() {
		return MODULE_NAME;
	}
	
}
