package org.dobots.piratedotty;

import org.dobots.aimrobotlibrary.AimRobotService;
import org.dobots.lib.comm.msg.RoboCommands.BaseCommand;
import org.dobots.lib.comm.msg.RoboCommands.ControlCommand;

import robots.piratedotty.ctrl.PirateDotty;
import robots.remote.RobotServiceBinder;
import android.content.Intent;

public class PirateDottyService extends AimRobotService {

	private static final String MODULE_NAME = "PirateDottyModule";
	
	@Override
	public void onCreate() {
		super.onCreate();
		PirateDotty oPirateDotty = new PirateDotty();
		oPirateDotty.startCamera(this);
		setRobot(new RobotServiceBinder(oPirateDotty));
	}

	@Override
	public String getModuleName() {
		return MODULE_NAME;
	}
	
	@Override
	protected void handleData(BaseCommand cmd) {

		if ((cmd instanceof ControlCommand) && ((ControlCommand)cmd).mCommand.equals("setConnection")) {
			setConnection((String)((ControlCommand)cmd).getParameter(0));
		} else {
			super.handleData(cmd);
		}
	}
	
	private void setConnection(String address) {
		Intent intent = new Intent(this, PirateDottyBTConnectorModule.class);
		intent.putExtra("address", address);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
}
