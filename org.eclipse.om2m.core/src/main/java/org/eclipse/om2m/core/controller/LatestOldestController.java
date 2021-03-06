/*******************************************************************************
 * Copyright (c) 2013-2020 LAAS-CNRS (www.laas.fr)
 * 7 Colonel Roche 31077 Toulouse - France
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
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

import java.math.BigInteger;
import java.util.List;

import org.eclipse.om2m.commons.constants.ResponseStatusCode;
import org.eclipse.om2m.commons.entities.AccessControlPolicyEntity;
import org.eclipse.om2m.commons.entities.ContainerEntity;
import org.eclipse.om2m.commons.entities.ContentInstanceEntity;
import org.eclipse.om2m.commons.entities.ResourceEntity;
import org.eclipse.om2m.commons.exceptions.OperationNotAllowed;
import org.eclipse.om2m.commons.exceptions.ResourceNotFoundException;
import org.eclipse.om2m.commons.resource.ContentInstance;
import org.eclipse.om2m.commons.resource.RequestPrimitive;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;
import org.eclipse.om2m.core.entitymapper.EntityMapperFactory;
import org.eclipse.om2m.core.notifier.Notifier;
import org.eclipse.om2m.core.router.Patterns;
import org.eclipse.om2m.core.urimapper.UriMapper;
import org.eclipse.om2m.persistence.service.DAO;

/**
 * Controller for latest/oldest virtual resources
 *
 */
public class LatestOldestController extends Controller{

	public enum SortingPolicy{
		LATEST,
		OLDEST
	}

	private SortingPolicy policy;

	public LatestOldestController(SortingPolicy policy) {
		this.policy = policy;
	}

	@Override
	public ResponsePrimitive doCreate(RequestPrimitive request) {
		throw new OperationNotAllowed("Create on " + policy.toString() +" is not allowed");
	}

	@Override
	public ResponsePrimitive doRetrieve(RequestPrimitive request) {
		// Creating the response primitive
		ResponsePrimitive response = new ResponsePrimitive(request);

		// Check existence of the resource
		ContainerEntity containerEntity = dbs.getDAOFactory().getContainerDAO()
				.find(transaction, request.getTo());
		if (containerEntity == null) {
			throw new ResourceNotFoundException("Resource not found");
		}

		// if resource exists, check authorization
		// retrieve 
		List<AccessControlPolicyEntity> acpList = containerEntity.getAccessControlPolicies();
		checkACP(acpList, request.getFrom(), request.getOperation());

		ContentInstanceEntity cinEntity = null;
		List<ContentInstanceEntity> instances = containerEntity.getChildContentInstances();
		if (instances.isEmpty()) {
			throw new ResourceNotFoundException("Resource not found");
		}
		switch(this.policy) {
		case LATEST:
			cinEntity = instances.get(instances.size()-1);
			break;
		case OLDEST:
			cinEntity = dbs.getDAOFactory().getOldestDAO().find(transaction, request.getTo());
			break;
		default:
			break;
		}

		// mapping the entity with the exchange resource
		ContentInstance cin = EntityMapperFactory.getContentInstanceMapper()
				.mapEntityToResource(cinEntity, request);		
		response.setContent(cin);
		response.setResponseStatusCode(ResponseStatusCode.OK);
		return response;
	}

	@Override
	public ResponsePrimitive doUpdate(RequestPrimitive request) {
		throw new OperationNotAllowed("Update on " + policy + " is not allowed");
	}

	@Override
	public ResponsePrimitive doDelete(RequestPrimitive request) {
		// Creating the response primitive
		ResponsePrimitive response = new ResponsePrimitive(request);
		Patterns patterns = new Patterns();


		// Check existence of the resource
		ContainerEntity containerEntity = dbs.getDAOFactory().getContainerDAO().
				find(transaction, request.getTo());
		if (containerEntity == null) {
			throw new ResourceNotFoundException();
		}

		// if resource exists, check authorization
		// retrieve 
		List<AccessControlPolicyEntity> acpList = containerEntity.getAccessControlPolicies();
		checkACP(acpList, request.getFrom(), request.getOperation());

		List<ContentInstanceEntity> instances = containerEntity.getChildContentInstances();
		if (instances.isEmpty()) {
			throw new ResourceNotFoundException();
		}
		ContentInstanceEntity cinEntity = null;
		switch(this.policy) {
		case LATEST:
			cinEntity = instances.get(instances.size()-1);
			break;
		case OLDEST:
			cinEntity = instances.get(0);
			break;
		default:
			break;
		}
		UriMapper.deleteUri(cinEntity.getHierarchicalURI());
		DAO<?> dao = (DAO<?>) patterns.getDAO(cinEntity.getParentID(), dbs);
		ResourceEntity parentEntity = (ResourceEntity)dao.find(transaction, cinEntity.getParentID());

		ContainerEntity container = (ContainerEntity) parentEntity;
		int nbI = container.getCurrentNrOfInstances().intValue();
		container.setCurrentNrOfInstances(BigInteger.valueOf(nbI-1));

		dbs.getDAOFactory().getContainerDAO().update(transaction, container);

		Notifier.notifyDeletion(null, cinEntity);
		
		dbs.getDAOFactory().getContentInstanceDAO().delete(transaction, cinEntity);
		transaction.commit();		
		
		response.setResponseStatusCode(ResponseStatusCode.DELETED);
		return response;
	}

}
