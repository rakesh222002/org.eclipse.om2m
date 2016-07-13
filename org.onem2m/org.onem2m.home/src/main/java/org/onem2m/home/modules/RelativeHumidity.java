package org.onem2m.home.modules;

import org.onem2m.home.types.ModuleType;
import org.onem2m.sdt.Domain;
import org.onem2m.sdt.Module;
import org.onem2m.sdt.datapoints.FloatDataPoint;
import org.onem2m.sdt.datapoints.IntegerDataPoint;
import org.onem2m.sdt.impl.AccessException;
import org.onem2m.sdt.impl.DataPointException;

public class RelativeHumidity extends Module {
	
	private FloatDataPoint relativeHumidity;
	private IntegerDataPoint desiredHumidity;
	
	public RelativeHumidity(final String name, final Domain domain, FloatDataPoint dp) {
		super(name, domain, ModuleType.relativeHumidity.getDefinition());

		this.relativeHumidity = dp;
		this.relativeHumidity.setWritable(false);
		this.relativeHumidity.setDoc("The measurement of the relative humidity value; the common unit is percentage.");
		addDataPoint(relativeHumidity);
	}
	
	public float getRelativeHumidity() throws DataPointException, AccessException {
		return relativeHumidity.getValue();
	}

	public void setDesiredHumidity(IntegerDataPoint dp) {
		this.desiredHumidity = dp;
		this.desiredHumidity.setOptional(true);
		this.desiredHumidity.setDoc("Desired value for Humidity.");
		addDataPoint(desiredHumidity);
	}

	public int getDesiredHumidity() throws DataPointException, AccessException {
		if (desiredHumidity == null)
			throw new DataPointException("Not implemented");
		return desiredHumidity.getValue();
	}

	public void setDesiredHumidity(int b) throws DataPointException, AccessException {
		if (desiredHumidity == null)
			throw new DataPointException("Not implemented");
		desiredHumidity.setValue(b);
	}

}
