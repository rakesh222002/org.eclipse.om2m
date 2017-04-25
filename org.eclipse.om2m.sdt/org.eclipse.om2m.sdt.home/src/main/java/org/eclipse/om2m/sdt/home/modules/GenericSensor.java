/*******************************************************************************
 * Copyright (c) 2014, 2016 Orange.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.om2m.sdt.home.modules;

import org.eclipse.om2m.sdt.Domain;
import org.eclipse.om2m.sdt.Module;
import org.eclipse.om2m.sdt.datapoints.BooleanDataPoint;
import org.eclipse.om2m.sdt.exceptions.AccessException;
import org.eclipse.om2m.sdt.exceptions.DataPointException;
import org.eclipse.om2m.sdt.home.types.ModuleType;

public class GenericSensor extends Module {
	
	private BooleanDataPoint value;
	
	public GenericSensor(final String name, final Domain domain, 
			BooleanDataPoint value) {
		this(name, domain, value, ModuleType.genericSensor.getDefinition());
	}
	
	public GenericSensor(final String name, final Domain domain, 
			BooleanDataPoint value, String containerDefinition) {
		super(name, domain, containerDefinition, 
				ModuleType.genericSensor.getLongDefinitionName(),
				ModuleType.genericSensor.getShortDefinitionName());

		this.value = value;
		this.value.setWritable(false);
		this.value.setDoc("True = Sensed, False = Not Sensed");
		this.value.setLongDefinitionType("value");
		this.value.setShortDefinitionType("value");
		addDataPoint(this.value);
	}

	public boolean getValue() throws DataPointException, AccessException {
		return value.getValue();
	}

}
