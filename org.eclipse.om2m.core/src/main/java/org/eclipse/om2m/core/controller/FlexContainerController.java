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
package org.eclipse.om2m.core.controller;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.commons.constants.Constants;
import org.eclipse.om2m.commons.constants.MimeMediaType;
import org.eclipse.om2m.commons.constants.Operation;
import org.eclipse.om2m.commons.constants.ResourceStatus;
import org.eclipse.om2m.commons.constants.ResourceType;
import org.eclipse.om2m.commons.constants.ResponseStatusCode;
import org.eclipse.om2m.commons.constants.ShortName;
import org.eclipse.om2m.commons.entities.AccessControlPolicyEntity;
import org.eclipse.om2m.commons.entities.AeEntity;
import org.eclipse.om2m.commons.entities.CSEBaseEntity;
import org.eclipse.om2m.commons.entities.ContainerEntity;
import org.eclipse.om2m.commons.entities.FlexContainerEntity;
import org.eclipse.om2m.commons.entities.RemoteCSEEntity;
import org.eclipse.om2m.commons.entities.ResourceEntity;
import org.eclipse.om2m.commons.entities.SubscriptionEntity;
import org.eclipse.om2m.commons.exceptions.BadRequestException;
import org.eclipse.om2m.commons.exceptions.ConflictException;
import org.eclipse.om2m.commons.exceptions.NotPermittedAttrException;
import org.eclipse.om2m.commons.exceptions.Om2mException;
import org.eclipse.om2m.commons.exceptions.ResourceNotFoundException;
import org.eclipse.om2m.commons.resource.CustomAttribute;
import org.eclipse.om2m.commons.resource.FlexContainer;
import org.eclipse.om2m.commons.resource.RequestPrimitive;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;
import org.eclipse.om2m.commons.utils.UriUtil;
import org.eclipse.om2m.commons.utils.Util.DateUtil;
import org.eclipse.om2m.core.datamapper.DataMapperSelector;
import org.eclipse.om2m.core.entitymapper.EntityMapperFactory;
import org.eclipse.om2m.core.flexcontainer.FlexContainerSelector;
import org.eclipse.om2m.core.notifier.Notifier;
import org.eclipse.om2m.core.router.Patterns;
import org.eclipse.om2m.core.urimapper.UriMapper;
import org.eclipse.om2m.core.util.ControllerUtil;
import org.eclipse.om2m.flexcontainer.service.FlexContainerService;
import org.eclipse.om2m.persistence.service.DAO;
import org.xml.sax.SAXException;

/**
 * Controller for the Container Resource
 *
 */
public class FlexContainerController extends Controller {

	/** Logger */
	private static Log LOGGER = LogFactory.getLog(FlexContainerController.class);

	/**
	 * Create the resource in the system according to the representation
	 */
	@Override
	public ResponsePrimitive doCreate(RequestPrimitive request) {
		/*
		 * Container creation procedure
		 * 
		 * @resourceName NP resourceType NP resourceID NP parentID NP
		 * accessControlPolicyIDs O creationTime NP expirationTime O
		 * lastModifiedTime NP labels O announceTo O announcedAttribute O
		 * 
		 * creator O maxNrOfInstances O maxByteSize O maxInstanceAge O
		 * currentNrOfInstances NP currentByteSize NP locationID O ontologyRef O
		 * 
		 */
		ResponsePrimitive response = new ResponsePrimitive(request);

		// get the dao of the parent
		DAO<ResourceEntity> dao = (DAO<ResourceEntity>) Patterns.getDAO(request.getTargetId(), dbs);
		if (dao == null) {
			throw new ResourceNotFoundException("Cannot find parent resource");
		}

		// get the parent entity
		ResourceEntity parentEntity = (ResourceEntity) dao.find(transaction, request.getTargetId());
		// check the parent existence
		if (parentEntity == null) {
			throw new ResourceNotFoundException("Cannot find parent resource");
		}

		// get lists to change in the method corresponding to specific object
		List<AccessControlPolicyEntity> acpsToCheck = null;
		List<FlexContainerEntity> childFlexContainers = null;
		List<SubscriptionEntity> subscriptions = null;

		// different cases
		// case parent is CSEBase
		if (parentEntity.getResourceType().intValue() == (ResourceType.CSE_BASE)) {
			CSEBaseEntity cseB = (CSEBaseEntity) parentEntity;
			acpsToCheck = cseB.getAccessControlPolicies();
			childFlexContainers = cseB.getChildFlexContainers();
			subscriptions = cseB.getSubscriptions();
		}
		// case parent is AE
		if (parentEntity.getResourceType().intValue() == (ResourceType.AE)) {
			AeEntity ae = (AeEntity) parentEntity;
			acpsToCheck = ae.getAccessControlPolicies();
			childFlexContainers = ae.getChildFlexContainers();
			subscriptions = ae.getSubscriptions();
		}
		// case parent is a FlexContainer
		if (parentEntity.getResourceType().intValue() == (ResourceType.FLEXCONTAINER)) {
			FlexContainerEntity parentFlexContainer = (FlexContainerEntity) parentEntity;
			acpsToCheck = parentFlexContainer.getAccessControlPolicies();
			childFlexContainers = parentFlexContainer.getChildFlexContainers();
			subscriptions = parentFlexContainer.getSubscriptions();
		}
		// case parent is a Container
		if (parentEntity.getResourceType().intValue() == (ResourceType.CONTAINER)) {
			ContainerEntity parentContainer = (ContainerEntity) parentEntity;
			acpsToCheck = parentContainer.getAccessControlPolicies();
			childFlexContainers = parentContainer.getChildFlexContainers();
			subscriptions = parentContainer.getSubscriptions();
		}

		// case parent is a RemoteCSE
		if (parentEntity.getResourceType().intValue() == ResourceType.REMOTE_CSE) {
			RemoteCSEEntity csr = (RemoteCSEEntity) parentEntity;
			acpsToCheck = csr.getAccessControlPolicies();
			childFlexContainers = csr.getChildFcnt();
			subscriptions = csr.getSubscriptions();
		}

		// check access control policy of the originator
		checkACP(acpsToCheck, request.getFrom(), Operation.CREATE);

		// check if content is present
		if (request.getContent() == null) {
			throw new BadRequestException("A content is requiered for FlexContainer creation");
		}
		// get the object from the representation
		FlexContainer flexContainer = null;
		try {

			String xmlPayload = null;
			if (request.getRequestContentType().equals(MimeMediaType.OBJ)) {
				flexContainer = (FlexContainer) request.getContent();

				// need to create the XML payload in order to validate it
				xmlPayload = DataMapperSelector.getDataMapperList().get(MimeMediaType.XML).objToString(flexContainer);

			} else {
				flexContainer = (FlexContainer) DataMapperSelector.getDataMapperList()
						.get(request.getRequestContentType()).stringToObj((String) request.getContent());

				xmlPayload = (String) request.getContent();
			}

			// validate XML payload
			validateXMLPayload(xmlPayload, flexContainer.getContainerDefinition());

		} catch (ClassCastException e) {
			e.printStackTrace();
			LOGGER.debug("ClassCastException: Incorrect resource type in object conversion.", e);
			throw new BadRequestException("Incorrect resource representation in content", e);
		}

		if (flexContainer == null) {
			throw new BadRequestException("Error in provided content");
		}

		// creating the corresponding entity
		FlexContainerEntity flexContainerEntity = new FlexContainerEntity();
		// check attributes
		// @resourceName NP
		// Resource Type NP
		// resourceID NP
		// parentID NP
		// lastModifiedTime NP
		// creationTime NP
		// expiration time O
		// labels O
		// announceTo O
		// announcedAttribute O

		ControllerUtil.CreateUtil.fillEntityFromAnnounceableResource(flexContainer, flexContainerEntity);

		// creator O
		if (flexContainer.getCreator() != null) {
			flexContainerEntity.setCreator(flexContainer.getCreator());
		}

		// containerDefinition != null
		if ((flexContainer.getContainerDefinition() == null) || (flexContainer.getContainerDefinition().isEmpty())) {
			// the containerDefinition MUST be provided
			throw new NotPermittedAttrException("containerDefinition attribute must be provided.");
		} else {
			flexContainerEntity.setContainerDefinition(flexContainer.getContainerDefinition());
		}

		// containerDefinition exists...
		// TODO

		String generatedId = generateId("", "");
		// set name if present and without any conflict
		if (request.getName() != null) {
			if (!Patterns.checkResourceName(request.getName())) {
				throw new BadRequestException("Name provided is incorrect. Must be:" + Patterns.ID_STRING);
			}
			flexContainerEntity.setName(request.getName());
		} else {
			flexContainerEntity.setName(ShortName.FCNT + "_" + generatedId);
		}
		flexContainerEntity.setResourceID(
				"/" + Constants.CSE_ID + "/" + ShortName.FCNT + Constants.PREFIX_SEPERATOR + generatedId);
		flexContainerEntity.setHierarchicalURI(parentEntity.getHierarchicalURI() + "/" + flexContainerEntity.getName());
		flexContainerEntity.setParentID(parentEntity.getResourceID());
		flexContainerEntity.setResourceType(ResourceType.FLEXCONTAINER);

		// accessControlPolicyIDs O
		if (!flexContainer.getAccessControlPolicyIDs().isEmpty()) {
			flexContainerEntity.setAccessControlPolicies(
					ControllerUtil.buildAcpEntityList(flexContainer.getAccessControlPolicyIDs(), transaction));
		} else {
			flexContainerEntity.getAccessControlPolicies().addAll(acpsToCheck);
		}

		if (!UriMapper.addNewUri(flexContainerEntity.getHierarchicalURI(), flexContainerEntity.getResourceID(),
				ResourceType.FLEXCONTAINER)) {
			throw new ConflictException("Name already present in the parent collection.");
		}

		// ontologyRef O
		if (flexContainer.getOntologyRef() != null) {
			flexContainerEntity.setOntologyRef(flexContainer.getOntologyRef());
		}

		// custom attributes
		LOGGER.debug("nb of customAttributes: "
				+ (flexContainer.getCustomAttributes() != null ? flexContainer.getCustomAttributes().size() : "0"));
		for (CustomAttribute ca : flexContainer.getCustomAttributes()) {

			flexContainerEntity.createOrUpdateCustomAttribute(ca.getCustomAttributeName(), ca.getCustomAttributeType(),
					ca.getCustomAttributeValue());
		}

		// create the FlexContainer in the DB
		dbs.getDAOFactory().getFlexContainerDAO().create(transaction, flexContainerEntity);
		// retrieve the managed object from DB
		FlexContainerEntity flexContainerFromDB = dbs.getDAOFactory().getFlexContainerDAO().find(transaction,
				flexContainerEntity.getResourceID());

		// add the container to the parentEntity child list
		childFlexContainers.add(flexContainerFromDB);
		dao.update(transaction, parentEntity);
		// commit the transaction
		transaction.commit();

		Notifier.notify(subscriptions, flexContainerFromDB, ResourceStatus.CHILD_CREATED);

		// create the response
		response.setResponseStatusCode(ResponseStatusCode.CREATED);
		// set the location of the resource
		setLocationAndCreationContent(request, response, flexContainerFromDB);
		return response;
	}

	/**
	 * Validate XML payload based on the ContainerDefinition value. This method
	 * loads the XSD file related to the ContainerDefinition value and validates
	 * the XML payload using this XSD file.
	 * 
	 * @param request
	 *            request contains the XML payload
	 * @param flexContainer
	 *            unmarshalled flexContainer object
	 * @throws BadRequestException
	 *             in case of the validation failed.
	 */
	private void validateXMLPayload(String xmlPayload, String containerDefinition) throws BadRequestException {
		// validate the XML payload
		// need to perform a post validation due to the fact we need
		// to know the value of the containerDefinition.
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		URL url = FlexContainer.class.getClassLoader().getResource("xsd/" + containerDefinition.toLowerCase() + ".xsd");

		if (url != null) {
			// XSD schema found!
			try {
				Schema schema = schemaFactory.newSchema(url);
				Validator validator = schema.newValidator();
				StreamSource ss = new StreamSource(new StringReader(xmlPayload));
				validator.validate(ss);
			} catch (SAXException e) {
				throw new BadRequestException("Unable to validate XML payload: " + e.getMessage());
			} catch (IOException e) {
				throw new BadRequestException("Unable to validate XML payload: " + e.getMessage());
			} catch (IllegalArgumentException e) {
				throw new BadRequestException("Unable to validate XML payload: " + e.getMessage());
			} catch (NullPointerException e) {
				throw new BadRequestException("Unable to validate XML payload: " + e.getMessage());
			}

		} else {
			// XSD not found
			throw new BadRequestException("No XSD schema found for " + containerDefinition);
		}
	}

	/**
	 * Return the container resource with the normalized representation
	 * 
	 * @param request
	 *            primitive routed
	 * @return response primitive
	 */
	@Override
	public ResponsePrimitive doRetrieve(RequestPrimitive request) {
		// Creating the response primitive
		ResponsePrimitive response = new ResponsePrimitive(request);

		// Check existence of the resource
		FlexContainerEntity flexContainerEntity = dbs.getDAOFactory().getFlexContainerDAO().find(transaction,
				request.getTargetId());
		if (flexContainerEntity == null) {
			throw new ResourceNotFoundException("Resource not found");
		}

		// if resource exists, check authorization
		// retrieve
		List<AccessControlPolicyEntity> acpList = flexContainerEntity.getAccessControlPolicies();
		checkACP(acpList, request.getFrom(), request.getOperation());

		// Mapping the entity with the exchange resource
		FlexContainer flexContainerResource = EntityMapperFactory.getFlexContainerMapper()
				.mapEntityToResource(flexContainerEntity, request);

		// check if a FlexContainer service exist
		FlexContainerService fcs = FlexContainerSelector.getFlexContainerService(flexContainerResource.getResourceID());
		if (fcs != null) {
			// retrieve the last values of custom attribute
			for (CustomAttribute ca : flexContainerResource.getCustomAttributes()) {
				ca.setCustomAttributeValue(fcs.getCustomAttributeValue(ca.getCustomAttributeName()));
			}
		}

		response.setContent(flexContainerResource);

		response.setResponseStatusCode(ResponseStatusCode.OK);
		// return the response
		return response;
	}

	/**
	 * Implement the full update method for container entity
	 * 
	 * @param request
	 * @return
	 */
	@Override
	public ResponsePrimitive doUpdate(RequestPrimitive request) {
		return processInternalNotifyOrUpdate(request, false);
	}

	@Override
	public ResponsePrimitive doInternalNotify(RequestPrimitive request) {
		return processInternalNotifyOrUpdate(request, true);
	}

	/**
	 * @param request
	 * @return
	 */
	private ResponsePrimitive processInternalNotifyOrUpdate(RequestPrimitive request, boolean isInternalNotify) {
		/*
		 * Container update procedure
		 * 
		 * @resourceName NP resourceType NP resourceID NP parentID NP
		 * accessControlPolicyIDs O creationTime NP expirationTime O
		 * lastModifiedTime NP labels O announceTo O announcedAttribute O
		 * 
		 * creator NP maxNrOfInstances O maxByteSize O maxInstanceAge O
		 * currentNrOfInstances NP currentByteSize NP locationID O ontologyRef O
		 * 
		 */
		// create the response base
		ResponsePrimitive response = new ResponsePrimitive(request);

		// retrieve the resource from database
		FlexContainerEntity flexContainerEntity = dbs.getDAOFactory().getFlexContainerDAO().find(transaction,
				request.getTargetId());
		if (flexContainerEntity == null) {
			throw new ResourceNotFoundException("Resource not found");
		}
		// check ACP
		checkACP(flexContainerEntity.getAccessControlPolicies(), request.getFrom(), Operation.UPDATE);

		FlexContainer modifiedAttributes = new FlexContainer();
		// check if content is present
		if (request.getContent() != null) {
			// create the java object from the resource representation
			// get the object from the representation
			FlexContainer flexContainer = null;
			try {
				String xmlPayload = null;
				if (request.getRequestContentType().equals(MimeMediaType.OBJ)) {
					flexContainer = (FlexContainer) request.getContent();
					// need to create the XML payload in order to validate it
					xmlPayload = DataMapperSelector.getDataMapperList().get(MimeMediaType.XML)
							.objToString(flexContainer);

				} else {
					flexContainer = (FlexContainer) DataMapperSelector.getDataMapperList()
							.get(request.getRequestContentType()).stringToObj((String) request.getContent());
					xmlPayload = (String) request.getContent();
				}

				// validate XML payload
				validateXMLPayload(xmlPayload, flexContainerEntity.getContainerDefinition());

			} catch (ClassCastException e) {
				throw new BadRequestException("Incorrect resource representation in content", e);
			}
			if (flexContainer == null) {
				throw new BadRequestException("Error in provided content");
			}

			// check attributes, NP attributes are ignored
			// @resourceName NP
			// resourceType NP
			// resourceID NP
			// parentID NP
			// creationTime NP
			// creator NP
			// lastModifiedTime NP
			// currentNrOfInstances NP
			// currentByteSize NP

			
			// labels O
			// accessControlPolicyIDs O
			if (!flexContainer.getAccessControlPolicyIDs().isEmpty()) {
				flexContainerEntity.getAccessControlPolicies().clear();
				flexContainerEntity.setAccessControlPolicies(
						ControllerUtil.buildAcpEntityList(flexContainer.getAccessControlPolicyIDs(), transaction));
				modifiedAttributes.getAccessControlPolicyIDs().addAll(flexContainer.getAccessControlPolicyIDs());
			}
			// labels O
			if (!flexContainer.getLabels().isEmpty()) {
				flexContainerEntity.setLabelsEntitiesFromSring(flexContainer.getLabels());
				modifiedAttributes.getLabels().addAll(flexContainer.getLabels());
			}
			// expirationTime O
			if (flexContainer.getExpirationTime() != null) {
				flexContainerEntity.setExpirationTime(flexContainer.getExpirationTime());
				modifiedAttributes.setExpirationTime(flexContainer.getExpirationTime());
			}
			// announceTo O
			if (!flexContainer.getAnnounceTo().isEmpty()) {
				// TODO Announcement in AE update
				flexContainerEntity.getAnnounceTo().clear();
				flexContainerEntity.getAnnounceTo().addAll(flexContainer.getAnnounceTo());
				modifiedAttributes.getAnnounceTo().addAll(flexContainer.getAnnounceTo());
			}
			// announcedAttribute O
			if (!flexContainer.getAnnouncedAttribute().isEmpty()) {
				flexContainerEntity.getAnnouncedAttribute().clear();
				flexContainerEntity.getAnnouncedAttribute().addAll(flexContainer.getAnnouncedAttribute());
				modifiedAttributes.getAnnouncedAttribute().addAll(flexContainer.getAnnouncedAttribute());
			}
			// ontologyRef O
			if (flexContainer.getOntologyRef() != null) {
				flexContainerEntity.setOntologyRef(flexContainer.getOntologyRef());
				modifiedAttributes.setOntologyRef(flexContainer.getOntologyRef());
			}

			// containerDef
			if ((flexContainer.getContainerDefinition() != null)
					&& (!flexContainerEntity.getContainerDefinition().equals(flexContainer.getContainerDefinition()))) {
				throw new BadRequestException("unable to change the containerDefinition value");
			}

			// here add customAttribute that might be updated
			if (!flexContainer.getCustomAttributes().isEmpty()) {
				for (CustomAttribute ca : flexContainer.getCustomAttributes()) {
					flexContainerEntity.createOrUpdateCustomAttribute(ca.getCustomAttributeName(),
							ca.getCustomAttributeType(), ca.getCustomAttributeValue());
				}
				modifiedAttributes.setCustomAttributes(flexContainer.getCustomAttributes());
			}
			
			

		} else {
			// 	content might be null for FlexContainer representing a SDT action
		}

		flexContainerEntity.setLastModifiedTime(DateUtil.now());
		modifiedAttributes.setLastModifiedTime(flexContainerEntity.getLastModifiedTime());

		// in case of update operation
		if (!isInternalNotify) {
			LOGGER.info("flexContainer.getResourceID=" + flexContainerEntity.getResourceID());
			// check if a FlexContainerService exist
			FlexContainerService fcs = FlexContainerSelector.getFlexContainerService(
					/* request.getTo() */ /*UriUtil.toCseRelativeUri(*/flexContainerEntity.getResourceID()/*)*/);
			if (fcs != null) {
				try {
					fcs.setCustomAttributeValues(modifiedAttributes.getCustomAttributes(), request);
					// at this modifiedAttributes.getCustomAttributes() list contains the new values of CustomAttribute
					
				} catch (Om2mException e) {
					throw e;
				}
			}
		}

		// at this point, we are sure there was no error when setting custom
		// attribute parameter

		response.setContent(modifiedAttributes);
		// update the resource in the database
		dbs.getDAOFactory().getFlexContainerDAO().update(transaction, flexContainerEntity);
		transaction.commit();

		Notifier.notify(flexContainerEntity.getSubscriptions(), flexContainerEntity, modifiedAttributes,
				ResourceStatus.UPDATED);

		// set response status code
		response.setResponseStatusCode(ResponseStatusCode.UPDATED);
		return response;
	}

	/**
	 * Delete the container if access control policies are correct
	 */
	@Override
	public ResponsePrimitive doDelete(RequestPrimitive request) {
		// Generic delete procedure
		ResponsePrimitive response = new ResponsePrimitive(request);

		// retrieve the corresponding resource from database
		FlexContainerEntity flexContainerEntity = dbs.getDAOFactory().getFlexContainerDAO().find(transaction,
				request.getTargetId());
		if (flexContainerEntity == null) {
			throw new ResourceNotFoundException("Resource not found");
		}

		// check access control policies
		checkACP(flexContainerEntity.getAccessControlPolicies(), request.getFrom(), Operation.DELETE);

		UriMapper.deleteUri(flexContainerEntity.getHierarchicalURI());
		Notifier.notifyDeletion(flexContainerEntity.getSubscriptions(), flexContainerEntity);

		// delete the resource in the database
		dbs.getDAOFactory().getFlexContainerDAO().delete(transaction, flexContainerEntity);
		// commit the transaction
		transaction.commit();
		// return the response
		response.setResponseStatusCode(ResponseStatusCode.DELETED);
		return response;
	}

}
