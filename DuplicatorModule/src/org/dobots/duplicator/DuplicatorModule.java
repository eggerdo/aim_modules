package org.dobots.duplicator;

import org.dobots.aim.SimpleAimServiceUI;

import android.os.Bundle;

public class DuplicatorModule extends SimpleAimServiceUI {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, DuplicatorService.class);
	}

}
