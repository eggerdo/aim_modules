#
# @file binding.gyp
# @brief This file provides the configuration and building options for node-gyp
#
# This file is created at "DoBots". It is open-source software and part of "AIM". 
# This software is published under the . license (.).
#
# Copyright © 2014 Dominik Egger <dominik@almende.org>
#
# @author                    Dominik Egger
# @date                      Jan 28, 2014
# @organisation              DoBots
# @project                   AIM
#
{
	"targets": [
		{
			"target_name": "BallTrackerModule",
			
			"include_dirs": [
				"../../inc",
				"../../aim-core/inc"
				
			],
			
			"dependencies":[
			],
			
			"cflags": [
			],
			
			"libraries": [
			],
			
			"ldflags": [
				"-pthread",
			],
			
			"sources":[
				"../../aim-core/src/BallTrackerModule.cpp",
				"BallTrackerModuleNode.cc",
				"../../src/BallTrackerModuleExt.cpp"
			],
		}
	]
}
