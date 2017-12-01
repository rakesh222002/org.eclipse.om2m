/*******************************************************************************
 * Copyright (c) 2014, 2016 Orange.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.om2m.core.controller;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.commons.constants.Constants;
import org.eclipse.om2m.commons.constants.MgmtDefinitionTypes;
import org.eclipse.om2m.commons.constants.MimeMediaType;
import org.eclipse.om2m.commons.constants.ResourceStatus;
import org.eclipse.om2m.commons.constants.ResourceType;
import org.eclipse.om2m.commons.constants.ResponseStatusCode;
import org.eclipse.om2m.commons.constants.ShortName;
import org.eclipse.om2m.commons.entities.AccessControlPolicyEntity;
import org.eclipse.om2m.commons.entities.DynamicAuthorizationConsultationEntity;
import org.eclipse.om2m.commons.entities.MgmtObjAnncEntity;
import org.eclipse.om2m.commons.entities.NodeEntity;
import org.eclipse.om2m.commons.entities.ResourceEntity;
import org.eclipse.om2m.commons.entities.SubscriptionEntity;
import org.eclipse.om2m.commons.exceptions.BadRequestException;
import org.eclipse.om2m.commons.exceptions.ConflictException;
import org.eclipse.om2m.commons.exceptions.NotImplementedException;
import org.eclipse.om2m.commons.exceptions.ResourceNotFoundException;
import org.eclipse.om2m.commons.resource.AnnouncedMgmtResource;
import org.eclipse.om2m.commons.resource.RequestPrimitive;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;
import org.eclipse.om2m.commons.utils.Util.DateUtil;
import org.eclipse.om2m.core.datamapper.DataMapperSelector;
import org.eclipse.om2m.core.entitymapper.EntityMapperFactory;
import org.eclipse.om2m.core.notifier.Notifier;
import org.eclipse.om2m.core.router.Patterns;
import org.eclipse.om2m.core.urimapper.UriMapper;
import org.eclipse.om2m.core.util.ControllerUtil;
import org.eclipse.om2m.persistence.service.DAO;

/**
 * Controller for the MgmtObj Resource
 *
 */
public class MgmtObjAnncController extends Controller {

	/** Logger */
	private static Log LOGGER = LogFactory.getLog(MgmtObjAnncController.class);

	/**
	 * Create the resource in the system according to the representation
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponsePrimitive doCreate(RequestPrimitive request) {
		/*
		 * MgmtObj creation procedure
		 * 
		 * @resourceName NP resourceType NP resourceID NP parentID NP
		 * accessControlPolicyIDs O creationTime NP expirationTime O
		 * lastModifiedTime NP labels O announceTo O announcedAttribute O
		 * 
		 * creator O maxNrOfInstances O maxByteSize O maxInstanceAge O
		 * currentNrOfInstances NP currentByteSize NP locationID O ontologyRef O
		 * 
		 */
		
		String contentFormat = System.getProperty("org.eclipse.om2m.registration.contentFormat", MimeMediaType.XML);
		Patterns patterns = new Patterns();

		ResponsePrimitive response = new ResponsePrimitive(request);

		// get the dao of the parent
		DAO<ResourceEntity> nodeDao = (DAO<ResourceEntity>) patterns.getDAO(request.getTo(), dbs);
		if (nodeDao == null) {
			throw new ResourceNotFoundException("Cannot find parent resource");
		}

		// get the parent entity
		ResourceEntity parentEntity = (ResourceEntity) nodeDao.find(transaction, request.getTo());
		// check the parent existence
		if (parentEntity == null) {
			throw new ResourceNotFoundException("Cannot find parent resource");
		}

		// lock parent
		transaction.lock(parentEntity);

		if (parentEntity.getResourceType().intValue() == (ResourceType.NODE_ANNC)) {
			throw new NotImplementedException("Parent is Node Annc, not implemented yet.");
		}

		// parent is Node
		NodeEntity nodeEntity = (NodeEntity) parentEntity;

		// get lists to change in the method corresponding to specific object
		List<AccessControlPolicyEntity> acpsToCheck = nodeEntity.getAccessControlPolicies();
//		List<DynamicAuthorizationConsultationEntity> dacsToCheck = null;
		List<SubscriptionEntity> subscriptions = nodeEntity.getSubscriptions();

		// check access control policy of the originator
		checkPermissions(request, nodeEntity, acpsToCheck);
		
		// check if content is present
		if (request.getContent() == null) {
			throw new BadRequestException("A content is requiered for MgmtObj creation");
		}
		LOGGER.info("contentType: " + request.getRequestContentType());
		LOGGER.info("content: " + request.getContent());
		LOGGER.info("parent node: " + nodeEntity);
		// get the object from the representation
		AnnouncedMgmtResource mgmtObj = null;
		try {
			String payload = null;
			if (request.getRequestContentType().equals(MimeMediaType.OBJ)) {
				mgmtObj = (AnnouncedMgmtResource) request.getContent();
				// need to create the payload in order to validate it
				payload = DataMapperSelector.getDataMapperList().get(contentFormat).objToString(mgmtObj);
			} else {
				mgmtObj = (AnnouncedMgmtResource) DataMapperSelector.getDataMapperList()
						.get(request.getRequestContentType()).stringToObj((String) request.getContent());
				if (request.getRequestContentType().equals(contentFormat)) {
					payload = (String) request.getContent();
				} else {
					// need to create the payload in order to validate it
					payload = DataMapperSelector.getDataMapperList().get(contentFormat).objToString(mgmtObj);
				}
			}
			LOGGER.info("payload: " + payload);

			// validate XML payload
//			if (contentFormat.equals(MimeMediaType.XML)) {
//				FlexContainerXMLValidator.validateXMLPayload(payload, flexContainer.getContainerDefinition());
//			}
		} catch (ClassCastException e) {
			e.printStackTrace();
			LOGGER.info("ClassCastException: Incorrect resource type in object conversion.", e);
			throw new BadRequestException("Incorrect resource representation in content", e);
		}

		if (mgmtObj == null) {
			throw new BadRequestException("Error in provided content");
		}
		else LOGGER.info("MgmtObj: " + mgmtObj.getClass() +  ": " + mgmtObj);
		
		BigInteger mgmtDef = mgmtObj.getMgmtDefinition();

		// creating the corresponding entity
		MgmtObjAnncEntity mgmtObjEntity = MgmtObjAnncEntity.create(mgmtDef);

		ControllerUtil.CreateUtil.fillEntityFromGenericResource(mgmtObj, mgmtObjEntity);
		
		mgmtObjEntity.fillFrom(mgmtObj);

		String generatedId = generateId("", "");
		// set name if present and without any conflict
		if (mgmtObj.getName() != null) {
			if (! patterns.checkResourceName(mgmtObj.getName())) {
				throw new BadRequestException("Name provided is incorrect. Must be:" + patterns.ID_STRING);
			}
			mgmtObjEntity.setName(mgmtObj.getName());
		} else {
			mgmtObjEntity.setName(MgmtDefinitionTypes.getShortName(mgmtDef) + "_" + generatedId);
		}
		mgmtObjEntity.setResourceID("/" + Constants.CSE_ID + "/" + ShortName.MGO 
				+ Constants.PREFIX_SEPERATOR + generatedId);
		mgmtObjEntity.setHierarchicalURI(parentEntity.getHierarchicalURI() + "/" + mgmtObjEntity.getName());
		mgmtObjEntity.setParentID(parentEntity.getResourceID());
		mgmtObjEntity.setResourceType(ResourceType.MGMT_OBJ);

		// accessControlPolicyIDs O
		if (! mgmtObj.getAccessControlPolicyIDs().isEmpty()) {
			mgmtObjEntity.setAccessControlPolicies(
					ControllerUtil.buildAcpEntityList(mgmtObj.getAccessControlPolicyIDs(), transaction));
		} else {
			mgmtObjEntity.getAccessControlPolicies().addAll(acpsToCheck);
		}

		// dynamicAuthorizationConsultationIDs O
		if (! mgmtObj.getDynamicAuthorizationConsultationIDs().isEmpty()) {
			mgmtObjEntity.setDynamicAuthorizationConsultations(
					ControllerUtil.buildDacEntityList(mgmtObj.getDynamicAuthorizationConsultationIDs(), transaction));
		}

		if (! UriMapper.addNewUri(mgmtObjEntity.getHierarchicalURI(), mgmtObjEntity.getResourceID(),
				ResourceType.MGMT_OBJ)) {
			throw new ConflictException("Name already present in the parent collection.");
		}

		// create the mgmtObj in the DB
		LOGGER.info("persist " + mgmtObjEntity + " ");
		dbs.getDAOFactory().getMgmtObjAnncDAO().create(transaction, mgmtObjEntity);
		// retrieve the managed object from DB
		MgmtObjAnncEntity mgmtObjFromDB = dbs.getDAOFactory().getMgmtObjAnncDAO().find(transaction,
				mgmtObjEntity.getResourceID());
		
		LOGGER.info("Created entity: " + mgmtObjFromDB);
		
//		nodeEntity.addMgmtObj(mgmtObjFromDB); TODO
		nodeDao.update(transaction, nodeEntity);

		// update link with mgmtObjEntity - DacEntity
		for (DynamicAuthorizationConsultationEntity dace : mgmtObjFromDB.getDynamicAuthorizationConsultations()) {
			DynamicAuthorizationConsultationEntity daceFromDB = dbs.getDAOFactory().getDynamicAuthorizationDAO().find(transaction, dace.getResourceID());
//			daceFromDB.addMgmtObj(mgmtObjFromDB); TODO
			dbs.getDAOFactory().getDynamicAuthorizationDAO().update(transaction, daceFromDB);
		}

		// commit the transaction & release lock
		transaction.commit();

		Notifier.notify(subscriptions, mgmtObjFromDB, ResourceStatus.CHILD_CREATED);

		// create the response
		response.setResponseStatusCode(ResponseStatusCode.CREATED);
		// set the location of the resource
		setLocationAndCreationContent(request, response, mgmtObjFromDB);
		return response;
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
		MgmtObjAnncEntity mgmtObjEntity = dbs.getDAOFactory().getMgmtObjAnncDAO().find(transaction,
				request.getTo());
		if (mgmtObjEntity == null) {
			throw new ResourceNotFoundException("Resource not found");
		}

		// if resource exists, check authorization
		// retrieve
		List<AccessControlPolicyEntity> acpList = mgmtObjEntity.getAccessControlPolicies();
//		checkACP(acpList, request.getFrom(), request.getOperation());
		checkPermissions(request, mgmtObjEntity, mgmtObjEntity.getAccessControlPolicies());

		// Mapping the entity with the exchange resource
		AnnouncedMgmtResource mgmtObj = (AnnouncedMgmtResource) 
			EntityMapperFactory.getMapperForMgmtObjAnnc()
				.mapEntityToResource(mgmtObjEntity, request);

		response.setContent(mgmtObj);

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
	private ResponsePrimitive processInternalNotifyOrUpdate(RequestPrimitive request, 
			boolean isInternalNotify) {
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
		String contentFormat = System.getProperty("org.eclipse.om2m.registration.contentFormat", MimeMediaType.XML);

		ResponsePrimitive response = new ResponsePrimitive(request);

		// retrieve the resource from database
		MgmtObjAnncEntity mgmtObjEntity = dbs.getDAOFactory().getMgmtObjAnncDAO().find(transaction,
				request.getTo());

		// lock current object
		transaction.lock(mgmtObjEntity);

		if (mgmtObjEntity == null) {
			throw new ResourceNotFoundException("Resource not found");
		}
		// check ACP
//		checkACP(mgmtObjEntity.getAccessControlPolicies(), request.getFrom(), Operation.UPDATE);
		if (! isInternalNotify) {
			checkPermissions(request, mgmtObjEntity, mgmtObjEntity.getAccessControlPolicies());
		}

		AnnouncedMgmtResource modifiedMgmtObj = (AnnouncedMgmtResource) 
			EntityMapperFactory.getMapperForMgmtObjAnnc()
				.mapEntityToResource(mgmtObjEntity, request);
		// check if content is present
		if (request.getContent() != null) {
			// create the java object from the resource representation
			// get the object from the representation
			AnnouncedMgmtResource mgmtObj = null;
			try {
				String payload = null;
				if (request.getRequestContentType().equals(MimeMediaType.OBJ)) {
					mgmtObj = (AnnouncedMgmtResource) request.getContent();
					// need to create the payload in order to validate it
					payload = DataMapperSelector.getDataMapperList().get(contentFormat)
							.objToString(mgmtObj);

				} else {
					mgmtObj = (AnnouncedMgmtResource) DataMapperSelector.getDataMapperList()
							.get(request.getRequestContentType()).stringToObj((String) request.getContent());

					if (request.getRequestContentType().equals(contentFormat)) {
						payload = (String) request.getContent();
					} else {
						// need to create the XML payload in order to validate
						// it
						payload = DataMapperSelector.getDataMapperList().get(contentFormat)
								.objToString(mgmtObj);
					}
				}

//				// validate XML payload
//				if (contentFormat.equals(MimeMediaType.XML)) {
//					FlexContainerXMLValidator.validateXMLPayload(payload, mgmtObj.getContainerDefinition());
//				}

			} catch (ClassCastException e) {
				throw new BadRequestException("Incorrect resource representation in content", e);
			}
			if (mgmtObj == null) {
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
			if (! mgmtObj.getAccessControlPolicyIDs().isEmpty()) {
				mgmtObjEntity.getAccessControlPolicies().clear();
				mgmtObjEntity.setAccessControlPolicies(
						ControllerUtil.buildAcpEntityList(mgmtObj.getAccessControlPolicyIDs(), transaction));
				modifiedMgmtObj.getAccessControlPolicyIDs().addAll(mgmtObj.getAccessControlPolicyIDs());
			}
			
			// dynamicAuthorizationConsultationIDs O
			if (! mgmtObj.getDynamicAuthorizationConsultationIDs().isEmpty()) {
				mgmtObjEntity.setDynamicAuthorizationConsultations(
						ControllerUtil.buildDacEntityList(mgmtObj.getDynamicAuthorizationConsultationIDs(), transaction));
				
				// update link with mgmtObjEntity - DacEntity
				for(DynamicAuthorizationConsultationEntity dace : mgmtObjEntity.getDynamicAuthorizationConsultations()) {
					DynamicAuthorizationConsultationEntity daceFromDB = dbs.getDAOFactory().getDynamicAuthorizationDAO().find(transaction, dace.getResourceID());
//					if (mgmtObj instanceof DeviceInfo) TODO
//						daceFromDB.getLinkedDeviceInfoEntities().add((DeviceInfoEntity) mgmtObjEntity);
//					else if (mgmtObj instanceof AreaNwkDeviceInfo)
//						daceFromDB.getLinkedAreaNwkDeviceInfoEntities().add((AreaNwkDeviceInfoEntity) mgmtObjEntity);
//					else if (mgmtObj instanceof AreaNwkInfo)
//						daceFromDB.getLinkedAreaNwkInfoEntities().add((AreaNwkInfoEntity) mgmtObjEntity);
					dbs.getDAOFactory().getDynamicAuthorizationDAO().update(transaction, daceFromDB);
				}
			}
			
			// labels O
			if (! mgmtObj.getLabels().isEmpty()) {
				mgmtObjEntity.setLabelsEntitiesFromSring(mgmtObj.getLabels());
				modifiedMgmtObj.getLabels().addAll(mgmtObj.getLabels());
			}
			// expirationTime O
			if (mgmtObj.getExpirationTime() != null) {
				mgmtObjEntity.setExpirationTime(mgmtObj.getExpirationTime());
				modifiedMgmtObj.setExpirationTime(mgmtObj.getExpirationTime());
			}

			// mgmtDefinition
			if ((mgmtObj.getMgmtDefinition() != null)
					&& ! mgmtObjEntity.getMgmtDefinition().equals(mgmtObj.getMgmtDefinition())) {
				throw new BadRequestException("unable to change the mgmtDefinition value");
			}
		}

		mgmtObjEntity.setLastModifiedTime(DateUtil.now());
		modifiedMgmtObj.setLastModifiedTime(mgmtObjEntity.getLastModifiedTime());

		response.setContent(modifiedMgmtObj);
		// update the resource in the database
		dbs.getDAOFactory().getMgmtObjAnncDAO().update(transaction, mgmtObjEntity);

		// commit and release lock
		transaction.commit();

		Notifier.notify(mgmtObjEntity.getSubscriptions(), mgmtObjEntity, modifiedMgmtObj,
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
		MgmtObjAnncEntity mgmtObjEntity = dbs.getDAOFactory().getMgmtObjAnncDAO().find(transaction,
				request.getTo());
		if (mgmtObjEntity == null) {
			throw new ResourceNotFoundException("Resource not found");
		}

		// lock entity
		transaction.lock(mgmtObjEntity);
		
		// check access control policies
		checkPermissions(request, mgmtObjEntity, mgmtObjEntity.getAccessControlPolicies());

		UriMapper.deleteUri(mgmtObjEntity.getHierarchicalURI());
		Notifier.notifyDeletion(mgmtObjEntity.getSubscriptions(), mgmtObjEntity);

		// delete the resource in the database
		dbs.getDAOFactory().getMgmtObjAnncDAO().delete(transaction, mgmtObjEntity);
		// commit the transaction & release lock
		transaction.commit();

		// return the response
		response.setResponseStatusCode(ResponseStatusCode.DELETED);
		return response;
	}

}
