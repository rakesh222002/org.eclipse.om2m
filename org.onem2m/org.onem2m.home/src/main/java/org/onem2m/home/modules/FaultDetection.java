package org.onem2m.home.modules;

import org.onem2m.home.types.ModuleType;
import org.onem2m.sdt.Domain;
import org.onem2m.sdt.Module;
import org.onem2m.sdt.datapoints.BooleanDataPoint;
import org.onem2m.sdt.datapoints.IntegerDataPoint;
import org.onem2m.sdt.datapoints.StringDataPoint;
import org.onem2m.sdt.impl.AccessException;
import org.onem2m.sdt.impl.DataPointException;

public class FaultDetection extends Module {
	
	private BooleanDataPoint status;
	private IntegerDataPoint code;
	private StringDataPoint description;

	public FaultDetection(final String name, final Domain domain, BooleanDataPoint status) {
		super(name, domain, ModuleType.faultDetection.getDefinition());
		
		this.status = status;
		this.status.setWritable(false);
		this.status.setDoc("Status of fault detection");
		addDataPoint(this.status);
	}

	public boolean getStatus() throws DataPointException, AccessException{
		return status.getValue();
	}

	public int getCode() throws DataPointException, AccessException {
		if (code == null)
			throw new DataPointException("Not implemented");
		return code.getValue();
	}

	public void setCode(IntegerDataPoint dp) throws DataPointException {
		this.code = dp;
		this.code.setOptional(true);
		this.code.setWritable(false);
		this.code.setDoc("Code of the fault.");
		addDataPoint(code);
	}

	public String getDescription() throws DataPointException, AccessException {
		if (description == null)
			throw new DataPointException("Not implemented");
		return description.getValue();
	}

	public void setDescription(StringDataPoint dp) {
		this.description = dp;
		this.description.setOptional(true);
		this.description.setWritable(false);
		this.description.setDoc("Message of the fault.");
		addDataPoint(description);
	}

}
