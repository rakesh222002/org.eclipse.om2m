package org.onem2m.home.modules;

import org.onem2m.home.types.ModuleType;
import org.onem2m.sdt.Domain;
import org.onem2m.sdt.datapoints.BooleanDataPoint;
import org.onem2m.sdt.datapoints.IntegerDataPoint;
import org.onem2m.sdt.impl.AccessException;
import org.onem2m.sdt.impl.DataPointException;

public class MotionSensor extends AbstractAlarmSensor {
	
	private IntegerDataPoint silentTime;
	private IntegerDataPoint sensitivity;
	
	public MotionSensor(final String name, final Domain domain, BooleanDataPoint alarm) {
		super(name, domain, alarm, ModuleType.motionSensor,
				"The detection of the motion occurrence.");
	}

	public void setSilentTime(IntegerDataPoint dp) {
		silentTime = dp;
		silentTime.setOptional(true);
		silentTime.setDoc("The time that the motionSensor restrains from sending an alarm in case continous motions are detected after one alarm is produced. This DataPoint can be used to avoid repeated alarm reports.");
		addDataPoint(silentTime);
	}
	
	public int getSilentTime() throws DataPointException, AccessException {
		if (silentTime == null)
			throw new DataPointException("Not implemented");
		return silentTime.getValue();
	}

	public void setSilentTime(int v) throws DataPointException, AccessException {
		if (silentTime == null)
			throw new DataPointException("Not implemented");
		silentTime.setValue(v);
	}

	public void setSensitivity(IntegerDataPoint dp) {
		sensitivity = dp;
		sensitivity.setOptional(true);
		sensitivity.setDoc("The level of the detection accuracy of the motion sensor. This DataPoint can be used to control the number of the report.");
		addDataPoint(sensitivity);
	}
	
	public int getSensitivity() throws DataPointException, AccessException {
		if (sensitivity == null)
			throw new DataPointException("Not implemented");
		return sensitivity.getValue();
	}

	public void setSensitivity(int v) throws DataPointException, AccessException {
		if (sensitivity == null)
			throw new DataPointException("Not implemented");
		sensitivity.setValue(v);
	}

}
