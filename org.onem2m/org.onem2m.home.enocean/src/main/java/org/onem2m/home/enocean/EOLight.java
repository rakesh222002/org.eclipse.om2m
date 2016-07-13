package org.onem2m.home.enocean;

import java.util.List;

import org.onem2m.home.actions.Toggle;
import org.onem2m.home.devices.Light;
import org.onem2m.home.driver.Utils;
import org.onem2m.home.enocean.Activator.EnOceanSDTDevice;
import org.onem2m.home.modules.BinarySwitch;
import org.onem2m.home.modules.Colour;
import org.onem2m.home.modules.ColourSaturation;
import org.onem2m.home.modules.FaultDetection;
import org.onem2m.home.modules.RunMode;
import org.onem2m.sdt.Domain;
import org.onem2m.sdt.datapoints.ArrayDataPoint;
import org.onem2m.sdt.datapoints.BooleanDataPoint;
import org.onem2m.sdt.datapoints.IntegerDataPoint;
import org.onem2m.sdt.impl.ActionException;
import org.onem2m.sdt.impl.DataPointException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.enocean.EnOceanDevice;
import org.osgi.service.enocean.EnOceanMessage;

public class EOLight extends Light implements EnOceanSDTDevice {

	private EnOceanDevice eoDevice;
	private Domain domain;
	private List<ServiceRegistration> registrations;
	private BundleContext context;

	public EOLight(EnOceanDevice device, Domain domain, BundleContext context) {
		super(Integer.toString(device.getChipId()), "0x" + Integer.toHexString(device.getChipId()), domain);
		setDeviceManufacturer("0x" + Integer.toHexString(device.getManufacturer()));
		this.domain = domain;
		this.eoDevice = device;
		this.context = context;

		try {
			addBinarySwitch();
		} catch (Exception e) {
			Activator.logger.warning("Error addBinarySwitch", e);
		}
		try {
			addFaultDetection();
		} catch (Exception e) {
			Activator.logger.warning("Error addFaultDetection", e);
		}
		try {
			addRunMode();
		} catch (Exception e) {
			Activator.logger.warning("Error addRunMode", e);
		}
		try {
			addColour();
		} catch (Exception e) {
			Activator.logger.warning("Error addColour", e);
		}
		try {
			addColourSaturation();
		} catch (Exception e) {
			Activator.logger.warning("Error addColourSaturation", e);
		}
	}

	@Override
	public void handleEvent(EnOceanMessage data) {
		// TODO Auto-generated method stub
	}

	@Override
	public void register() {
		registrations = Utils.register(this, context);
	}

	@Override
	public void unregister() {
		for (ServiceRegistration reg : registrations) {
			reg.unregister();
		}
	}

	private void addBinarySwitch() {
		BinarySwitch binarySwitch = new BinarySwitch("BinarySwitch_" + eoDevice.getChipId(), 
			domain,
			new BooleanDataPoint("powerState") {
				@Override
				public Boolean doGetValue() throws DataPointException {
					return false;
				}
		});
		binarySwitch.setToggle(new Toggle("toggle_" + eoDevice.getChipId()) {
			@Override
			protected void doToggle() throws ActionException {
			}
		});
		addModule(binarySwitch);
	}

	private void addFaultDetection() {
		FaultDetection faultDetection = new FaultDetection("FaultDetection_" + eoDevice.getChipId(), 
			domain,
			new BooleanDataPoint("status") {
				@Override
				public Boolean doGetValue() throws DataPointException {
					return false;
				}
		});
		addModule(faultDetection);
	}

	private void addRunMode() {
		RunMode runMode = new RunMode("RunMode_" + eoDevice.getChipId(), domain, 
			new ArrayDataPoint<String>("operationMode") {
				@Override
				public void doSetValue(List<String> values) throws DataPointException {
					throw new DataPointException("Not implemented");
				}
				@Override
				public List<String> doGetValue() throws DataPointException {
					return null;
				}
			}, 
			new ArrayDataPoint<String>("supportedModes") {
				@Override
				public void doSetValue(List<String> value) throws DataPointException {
					throw new DataPointException("Not implemented");
				}
				@Override
				public List<String> doGetValue() throws DataPointException {
					return null;
				}
			});
		addModule(runMode);
	}

	private void addColour() {
		Colour colour = new Colour("colour_" + eoDevice.getChipId(), domain, 
			new IntegerDataPoint("red") {
				@Override
				public void doSetValue(Integer value) throws DataPointException {
				}
				@Override
				public Integer doGetValue() throws DataPointException {
					return null;
				}
			}, 
			new IntegerDataPoint("green") {
				@Override
				public void doSetValue(Integer value) throws DataPointException {
				}
				@Override
				public Integer doGetValue() throws DataPointException {
					return null;
				}
			}, 
			new IntegerDataPoint("blue") {
				@Override
				public void doSetValue(Integer value) throws DataPointException {
				}
				@Override
				public Integer doGetValue() throws DataPointException {
					return null;
				}
			});
		addModule(colour);
	}

	private void addColourSaturation() {
		ColourSaturation colourSaturation = new ColourSaturation("colourSaturation_" + eoDevice.getChipId(), 
			domain,
			new IntegerDataPoint("colourSaturation") {
				@Override
				public void doSetValue(Integer value) throws DataPointException {
				}
				@Override
				public Integer doGetValue() throws DataPointException {
					return null;
				}
			});
		addModule(colourSaturation);
	}

	@Override
	public Integer getChipId() {
		return eoDevice.getChipId();
	}

}
