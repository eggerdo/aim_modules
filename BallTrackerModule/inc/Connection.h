/*
 * Connection.h
 *
 *  Created on: Feb 11, 2014
 *      Author: dominik
 */

#ifndef CONNECTION_H_
#define CONNECTION_H_

#include <string>

class Connection {
public:
	virtual bool send(std::string message) = 0;
};

#endif /* CONNECTION_H_ */
