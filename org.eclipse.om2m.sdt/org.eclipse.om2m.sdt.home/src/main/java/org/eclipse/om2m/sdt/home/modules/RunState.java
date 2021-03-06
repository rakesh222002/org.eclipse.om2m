/*******************************************************************************
 * Copyright (c) 2014, 2016 Orange.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *******************************************************************************/
package org.eclipse.om2m.sdt.home.modules;

import java.util.List;
import java.util.Map;

import org.eclipse.om2m.sdt.DataPoint;
import org.eclipse.om2m.sdt.Domain;
import org.eclipse.om2m.sdt.Module;
import org.eclipse.om2m.sdt.datapoints.ArrayDataPoint;
import org.eclipse.om2m.sdt.datapoints.FloatDataPoint;
import org.eclipse.om2m.sdt.exceptions.AccessException;
import org.eclipse.om2m.sdt.exceptions.DataPointException;
import org.eclipse.om2m.sdt.home.types.DatapointType;
import org.eclipse.om2m.sdt.home.types.JobState;
import org.eclipse.om2m.sdt.home.types.MachineState;
import org.eclipse.om2m.sdt.home.types.ModuleType;

public class RunState extends Module {
	
	private JobState currentJobState;
	private ArrayDataPoint<JobState.Values> jobStates;
	private MachineState currentMachineState;
	private ArrayDataPoint<MachineState.Values> machineStates;
	
	private FloatDataPoint progressPercentage;

	public RunState(final String name, final Domain domain,
			JobState jobState, ArrayDataPoint<JobState.Values> jobStates,
			MachineState machineState, ArrayDataPoint<MachineState.Values> machineStates) {
		super(name, domain, ModuleType.runState);
		
		if ((jobState == null) ||
				! jobState.getShortName().equals(DatapointType.currentJobState.getShortName())) {
			domain.removeModule(getName());
			throw new IllegalArgumentException("Wrong currentJobState datapoint: " + jobState);
		}
		this.currentJobState = jobState;
		this.currentJobState.setWritable(false);
		this.currentJobState.setDoc("Currently active job state. The value of this property shall be idle unless the value of currentMachineState property is active");
		addDataPoint(this.currentJobState);
		
		if ((jobStates == null) ||
				! jobStates.getShortName().equals(DatapointType.jobStates.getShortName())) {
			domain.removeModule(getName());
			throw new IllegalArgumentException("Wrong jobStates datapoint: " + jobStates);
		}
		this.jobStates = jobStates;
		this.jobStates.setWritable(false);
		this.jobStates.setDoc("List of possible job states the device supports");
		addDataPoint(this.jobStates);
		
		if ((machineState == null) ||
				! machineState.getShortName().equals(DatapointType.currentMachineState.getShortName())) {
			domain.removeModule(getName());
			throw new IllegalArgumentException("Wrong currentMachineState datapoint: " + machineState);
		}
		this.currentMachineState = machineState;
		this.currentMachineState.setDoc("Currently active machine state");
		addDataPoint(this.currentMachineState);
		
		if ((machineStates == null) ||
				! machineStates.getShortName().equals(DatapointType.machineStates.getShortName())) {
			domain.removeModule(getName());
			throw new IllegalArgumentException("Wrong machineStates datapoint: " + machineStates);
		}
		this.machineStates = machineStates;
		this.machineStates.setWritable(false);
		this.machineStates.setDoc("List of possible machine states the device supports ");
		addDataPoint(this.machineStates);
	}
	
	@SuppressWarnings("unchecked")
	public RunState(final String name, final Domain domain, Map<String, DataPoint> dps) {
		this(name, domain,
			(JobState) dps.get(DatapointType.currentJobState.getShortName()),
			(ArrayDataPoint<JobState.Values>) dps.get(DatapointType.jobStates.getShortName()),
			(MachineState) dps.get(DatapointType.currentMachineState.getShortName()), 
			(ArrayDataPoint<MachineState.Values>) dps.get(DatapointType.machineStates.getShortName()));
		FloatDataPoint progressPercentage = (FloatDataPoint) dps.get(DatapointType.progressPercentage.getShortName());
		if (progressPercentage != null)
			setProgressPercentage(progressPercentage);
	}

	public JobState.Values getJobState() throws DataPointException, AccessException {
		return currentJobState.getValue();
	}

	public void setJobState(JobState.Values v) throws DataPointException, AccessException {
		currentJobState.setValue(v);
	}

	public List<JobState.Values> getJobStates() throws DataPointException, AccessException {
		return jobStates.getValue();
	}

	public void setJobStates(List<JobState.Values> states) throws DataPointException, AccessException {
		jobStates.setValue(states);
	}

	public MachineState.Values getMachineState() throws DataPointException, AccessException {
		return currentMachineState.getValue();
	}

	public void setMachineState(MachineState.Values v) throws DataPointException, AccessException {
		currentMachineState.setValue(v);
	}

	public List<MachineState.Values> getMachineStates() throws DataPointException, AccessException {
		return machineStates.getValue();
	}

	public void setProgressPercentage(FloatDataPoint dp) {
		this.progressPercentage = dp;
		this.progressPercentage.setOptional(true);
		this.progressPercentage.setWritable(false);
		this.progressPercentage.setDoc("Indication of current progress in percentage.");
		addDataPoint(progressPercentage);
	}

	public float getProgressPercentage() throws DataPointException, AccessException {
		if (progressPercentage == null)
			throw new DataPointException("Not implemented");
		return progressPercentage.getValue();
	}

}
