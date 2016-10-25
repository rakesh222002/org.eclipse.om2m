/*
ModuleClass : WaterSensor



This ModuleClass provides the capabilities to indicates whether  water has been sensed or not and raises an alarm if the triggering  criterion is met.

Created: 2017-07-17 15:25:54
*/

package org.eclipse.om2m.commons.resource.flexcontainerspec;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.om2m.commons.resource.AbstractFlexContainer;


@XmlRootElement(name = WaterSensorFlexContainer.SHORT_NAME, namespace = "http://www.onem2m.org/xml/protocols/homedomain")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = WaterSensorFlexContainer.SHORT_NAME, namespace = "http://www.onem2m.org/xml/protocols/homedomain")
public class WaterSensorFlexContainer extends AbstractFlexContainer {
	
	public static final String LONG_NAME = "waterSensor";
	public static final String SHORT_NAME = "watSr";
	
	public WaterSensorFlexContainer () {
		setContainerDefinition("org.onem2m.home.moduleclass." + LONG_NAME);
		setLongName(LONG_NAME);
		setShortName(SHORT_NAME);
	}
	
	public void finalizeSerialization() {
	}
	
}