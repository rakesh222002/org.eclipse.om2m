/*******************************************************************************
 * Copyright (c) 2016- 2017 SENSINOV (www.sensinov.com)
 * 41 Rue de la découverte 31676 Labège - France 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *******************************************************************************/
package org.eclipse.om2m.adn.tests;

public class TestConfig {
	
	public final static String originator="admin:admin";
	public final static String cseProtocol="http";
	
	public final static String cseIp = "127.0.0.1";
	public final static int csePort = 8080;
	
	public final static String cseId = "in-cse";
	public final static String cseName = "in-name";
	
	public final static String remoteCseIp = "127.0.0.1";
	public final static int remoteCSEPort = 8282;
	
	public final static String remoteCseId = "mn-cse";
	public final static String remoteCseName = "mn-name";
	
	public final static String csePoa = cseProtocol+"://"+cseIp+":"+csePort;
	public final static String remoteCsePoa = cseProtocol+"://"+remoteCseIp+":"+remoteCSEPort; 
}
