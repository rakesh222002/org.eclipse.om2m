/*******************************************************************************
 * Copyright (c) 2013-2016 LAAS-CNRS (www.laas.fr)
 * 7 Colonel Roche 31077 Toulouse - France
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 *     Thierry Monteil : Project manager, technical co-manager
 *     Mahdi Ben Alaya : Technical co-manager
 *     Samir Medjiah : Technical co-manager
 *     Khalil Drira : Strategy expert
 *     Guillaume Garzone : Developer
 *     François Aïssaoui : Developer
 *
 * New contributors :
 *******************************************************************************/
package org.eclipse.om2m.core.entitymapper;

import java.math.BigInteger;

import org.eclipse.om2m.commons.constants.ResourceType;
import org.eclipse.om2m.commons.constants.ResultContent;
import org.eclipse.om2m.commons.entities.AccessControlPolicyEntity;
import org.eclipse.om2m.commons.entities.ContainerEntity;
import org.eclipse.om2m.commons.entities.ContentInstanceEntity;
import org.eclipse.om2m.commons.entities.CustomAttributeEntity;
import org.eclipse.om2m.commons.entities.FlexContainerEntity;
import org.eclipse.om2m.commons.entities.SubscriptionEntity;
import org.eclipse.om2m.commons.resource.ChildResourceRef;
import org.eclipse.om2m.commons.resource.Container;
import org.eclipse.om2m.commons.resource.ContentInstance;
import org.eclipse.om2m.commons.resource.CustomAttribute;
import org.eclipse.om2m.commons.resource.FlexContainer;
import org.eclipse.om2m.commons.resource.Subscription;

public class FlexContainerMapper extends EntityMapper<FlexContainerEntity, FlexContainer>{

	@Override
	protected FlexContainer createResource() {
		return new FlexContainer();
	}

	@Override
	protected void mapAttributes(FlexContainerEntity entity, FlexContainer resource) {
		
		resource.setCreator(entity.getCreator());
		resource.setOntologyRef(entity.getOntologyRef());
		resource.setStateTag(entity.getStateTag());
		resource.setExpirationTime(entity.getExpirationTime());
		resource.setContainerDefinition(entity.getContainerDefinition());
		if (!entity.getAnnouncedAttribute().isEmpty()) {			
			resource.getAnnouncedAttribute().addAll(entity.getAnnouncedAttribute());
		}
		if (!entity.getAnnounceTo().isEmpty()) {			
			resource.getAnnounceTo().addAll(entity.getAnnounceTo());
		}
		// setting acpIds
		for (AccessControlPolicyEntity acp : entity.getAccessControlPolicies()) {
			resource.getAccessControlPolicyIDs().add(acp.getResourceID());
		}
		
		for(CustomAttributeEntity cae : entity.getCustomAttributes()) {
			CustomAttribute ca = new CustomAttribute();
			ca.setCustomAttributeName(cae.getCustomAttributeName());
			ca.setCustomAttributeType(cae.getCustomAttributeType());
			ca.setCustomAttributeValue(cae.getCustomAttributeValue());
			resource.getCustomAttributes().add(ca);
		}
	}

	@Override
	protected void mapChildResourceRef(FlexContainerEntity entity,
			FlexContainer resource) {

		// add child ref FlexContainer
		for (FlexContainerEntity fcnt : entity.getChildFlexContainers()) {
			ChildResourceRef child = new ChildResourceRef();
			child.setResourceName(fcnt.getName());
			child.setType(ResourceType.FLEXCONTAINER);
			child.setValue(fcnt.getResourceID());
			resource.getChildResource().add(child);	
		}

		// add child ref subscription
		for (SubscriptionEntity sub : entity.getSubscriptions()){
			ChildResourceRef child = new ChildResourceRef();
			child.setResourceName(sub.getName());
			child.setType(ResourceType.SUBSCRIPTION);
			child.setValue(sub.getResourceID());
			resource.getChildResource().add(child);
		}
		
		
		// add child ref with containers
		for (ContainerEntity childCont : entity.getChildContainers()) {
			ChildResourceRef child = new ChildResourceRef();
			child.setResourceName(childCont.getName());
			child.setType(ResourceType.CONTAINER);
			child.setValue(childCont.getResourceID());
			resource.getChildResource().add(child);
		}
	}

	@Override
	protected void mapChildResources(FlexContainerEntity entity, FlexContainer resource) {
		// add child ref flexContainer
		for (FlexContainerEntity cin : entity.getChildFlexContainers()) {
			FlexContainer flexContainerRes = new FlexContainerMapper().mapEntityToResource(cin, ResultContent.ATTRIBUTES);
			resource.getFlexContainerOrContainerOrSubscription().add(flexContainerRes);
		}

		// add child ref subscription
		for (SubscriptionEntity sub : entity.getSubscriptions()){
			Subscription subRes = new SubscriptionMapper().mapEntityToResource(sub, ResultContent.ATTRIBUTES);
			resource.getFlexContainerOrContainerOrSubscription().add(subRes);
		}
		
		
		// add child ref with containers
		for (ContainerEntity childCont : entity.getChildContainers()) {
			Container cnt = new ContainerMapper().mapEntityToResource(childCont, ResultContent.ATTRIBUTES);
			resource.getFlexContainerOrContainerOrSubscription().add(cnt);
		}
	}

	
}