/*******************************************************************************
 * Copyright (c) 2014, 2016 Orange.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *******************************************************************************/
package org.eclipse.om2m.sdt.home.modules;

import java.util.Map;

import org.eclipse.om2m.sdt.DataPoint;
import org.eclipse.om2m.sdt.Domain;
import org.eclipse.om2m.sdt.Module;
import org.eclipse.om2m.sdt.datapoints.IntegerDataPoint;
import org.eclipse.om2m.sdt.exceptions.AccessException;
import org.eclipse.om2m.sdt.exceptions.DataPointException;
import org.eclipse.om2m.sdt.home.types.DatapointType;
import org.eclipse.om2m.sdt.home.types.ModuleType;

public class ColourSaturation extends Module {
	
	private IntegerDataPoint colourSat;

	public ColourSaturation(final String name, final Domain domain, IntegerDataPoint colourSat) {
		super(name, domain, ModuleType.colourSaturation);
		setExtends(domain.getName(), "ColourSaturation");
		
		if ((colourSat == null) ||
				! colourSat.getShortName().equals(DatapointType.colourSaturation.getShortName())) {
			domain.removeModule(getName());
			throw new IllegalArgumentException("Wrong colourSaturation datapoint: " + colourSat);
		}
		this.colourSat = colourSat;
		addDataPoint(this.colourSat);
	}

	public ColourSaturation(final String name, final Domain domain, Map<String, DataPoint> dps) {
		this(name, domain, (IntegerDataPoint) dps.get(DatapointType.colourSaturation.getShortName()));
	}

	public int getColourSat() throws DataPointException, AccessException {
		return colourSat.getValue();
	}
	
	public void setColourSat(int value) throws DataPointException, AccessException {
		colourSat.setValue(value);
	}
	
}
