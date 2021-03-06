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
package org.eclipse.om2m.persistence.eclipselink.internal.dao;

import org.eclipse.om2m.commons.entities.CSEBaseEntity;
import org.eclipse.om2m.commons.entities.RequestEntity;
import org.eclipse.om2m.persistence.eclipselink.internal.DBTransactionJPAImpl;
import org.eclipse.om2m.persistence.service.DBTransaction;

public class RequestEntityDAO extends AbstractDAO<RequestEntity> {

	@Override
	public RequestEntity find(DBTransaction dbTransaction, Object id) {
		DBTransactionJPAImpl transaction = (DBTransactionJPAImpl) dbTransaction;
		return transaction.getEm().find(RequestEntity.class, id);
	}

	@Override
	public void delete(DBTransaction dbTransaction, RequestEntity resource) {
		DBTransactionJPAImpl transaction = (DBTransactionJPAImpl) dbTransaction;
		transaction.getEm().remove(resource);
		transaction.getEm().getEntityManagerFactory().getCache().evict(CSEBaseEntity.class);
	}

}
