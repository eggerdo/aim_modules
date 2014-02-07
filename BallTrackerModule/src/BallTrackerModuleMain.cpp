/**
 * @file BallTrackerModuleMain.cpp
 * @brief Entry function for BallTrackerModule
 *
 * This file is created at "DoBots". It is open-source software and part of "AIM". 
 * This software is published under the . license (.).
 *
 * Copyright © 2014 Dominik Egger <dominik@almende.org>
 *
 * @author                   Dominik Egger
 * @date                     Jan 28, 2014
 * @organisation             DoBots
 * @project                  AIM
 */
 
#include <BallTrackerModuleExt.h>

#include <stdlib.h>
#include <iostream>

using namespace rur;
using namespace std;

/**
 * Every module is a separate binary and hence has its own main method. It is recommended
 * to have a version of your code running without any middleware wrappers, so preferably
 * have this file and the BallTrackerModule header and code in a separate "aim" directory.
 */
int main(int argc, char *argv[])  {
	BallTrackerModuleExt *m = new BallTrackerModuleExt();

	if (argc < 2) {
		std::cout << "Use an identifier as argument for this instance" << endl;
		return EXIT_FAILURE;
	}
	std::string identifier = argv[1];
	m->Init(identifier);

	do {
		m->Tick();
	} while (!m->Stop()); 

	delete m;

	return EXIT_SUCCESS;
}
