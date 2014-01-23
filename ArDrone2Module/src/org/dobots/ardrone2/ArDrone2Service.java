package org.dobots.ardrone2;


import org.dobots.aimrobotlibrary.AimRobotService;

import robots.parrot.ardrone2.ctrl.ArDrone2;
import robots.parrot.ctrl.Parrot;
import robots.remote.RobotServiceBinder;

public class ArDrone2Service extends AimRobotService {

	private static final String MODULE_NAME = "ArDrone2Module";
	
	@Override
	public void onCreate() {
		super.onCreate();
		setRobot(new RobotServiceBinder(new ArDrone2()));
	}

	@Override
	public String getModuleName() {
		return MODULE_NAME;
	}
	
}
