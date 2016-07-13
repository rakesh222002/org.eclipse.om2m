package org.onem2m.home.modules;

import org.onem2m.home.types.ModuleType;
import org.onem2m.sdt.Domain;
import org.onem2m.sdt.Module;
import org.onem2m.sdt.datapoints.BooleanDataPoint;
import org.onem2m.sdt.impl.AccessException;
import org.onem2m.sdt.impl.DataPointException;

public class PushButton extends Module {
	
	private BooleanDataPoint pushed;
	
	public PushButton(final String name, final Domain domain, 
			BooleanDataPoint pressed) {
		super(name, domain, ModuleType.pushButton.getDefinition());

		this.pushed = pressed;
		this.pushed.setWritable(false);
		this.pushed.setDoc("To indicate the press of the button.");
		addDataPoint(this.pushed);
	}

	public boolean isPushed() throws DataPointException, AccessException {
		return pushed.getValue();
	}

}