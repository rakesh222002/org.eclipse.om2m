package org.onem2m.home.modules;

import org.onem2m.home.types.LiquidLevel;
import org.onem2m.home.types.ModuleType;
import org.onem2m.sdt.Domain;
import org.onem2m.sdt.Module;
import org.onem2m.sdt.impl.AccessException;
import org.onem2m.sdt.impl.DataPointException;

public class WaterLevel extends Module {
	
	private LiquidLevel liquidLevel;

	public WaterLevel(final String name, final Domain domain, LiquidLevel dp) {
		super(name, domain, ModuleType.waterLevel.getDefinition());

		liquidLevel = dp;
		liquidLevel.setDoc("The desired level of water.");
		addDataPoint(liquidLevel);
	}
	
	public int getLiquidLevel() throws DataPointException, AccessException {
		return liquidLevel.getValue();
	}

	public void setLiquidLevel(int v) throws DataPointException, AccessException {
		liquidLevel.setValue(v);
	}

}
