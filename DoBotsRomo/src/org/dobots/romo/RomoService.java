package org.dobots.romo;

import org.dobots.aimrobotlibrary.AimRobotService;

import robots.remote.RobotServiceBinder;
import robots.romo.ctrl.Romo;

public class RomoService extends AimRobotService {

	private static final String MODULE_NAME = "RomoModule";
	
	@Override
	public String getModuleName() {
		return MODULE_NAME;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mRobot = new RobotServiceBinder(new Romo());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mRobot.destroy();
	}

}
