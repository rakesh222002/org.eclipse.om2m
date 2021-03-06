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
package org.eclipse.om2m.commons.entities;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.eclipse.om2m.commons.constants.DBEntities;
import org.eclipse.om2m.commons.constants.ResourceType;
import org.eclipse.om2m.commons.constants.ShortName;

/**
 * Subscription JPA entity
 *
 */
@Entity(name = DBEntities.SUBSCRIPTION_ENTITY)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class SubscriptionEntity extends RegularResourceEntity {
	
	/** Link to the ACPs */
	@ManyToMany(fetch=FetchType.LAZY)
	@JoinTable(
			name=DBEntities.SUBACP_JOIN,
			joinColumns={@JoinColumn(name=DBEntities.SUB_JOIN_ID, referencedColumnName=ShortName.RESOURCE_ID)},
			inverseJoinColumns={@JoinColumn(name=DBEntities.ACP_JOIN_ID, referencedColumnName=ShortName.RESOURCE_ID)}
			)
	protected List<AccessControlPolicyEntity> accessControlPolicies;
	
	/** Link to the ACPs */
	@ManyToMany(fetch=FetchType.LAZY)
	@JoinTable(
			name=DBEntities.SUB_DAC_JOIN,
			joinColumns={@JoinColumn(name=DBEntities.SUB_JOIN_ID, referencedColumnName=ShortName.RESOURCE_ID)},
			inverseJoinColumns={@JoinColumn(name=DBEntities.DAC_JOINID, referencedColumnName=ShortName.RESOURCE_ID)}
			)
	protected List<DynamicAuthorizationConsultationEntity> dynamicAuthorizationConsultations;
	
	// TODO event notification criteria
	@Column(name = ShortName.EXPIRATION_COUNTER)
	protected BigInteger expirationCounter;
	@Column (name = ShortName.NOTIFICATION_URI, nullable = false)
	protected List<String> notificationURI;
	protected String groupID;
	@Column(name = ShortName.NOTIFICATION_FORWARDING_URI)
	protected String notificationForwardingURI;
	
	// TODO batch notify : embedded
	// TODO rate limit

	protected BigInteger preSubscriptionNotify;
	protected BigInteger pendingNotification;

	protected BigInteger notificationStoragePriority;
	protected Boolean latestNotify;
	protected BigInteger notificationContentType;
	protected String notificationEventCat;
	@Column(name = ShortName.CREATOR)
	protected String creator;
	
	/* 2017 07 25 - BONNARDEL Gregory */
	/* notificationPayloadContentType contains the type of representation */
	/* to be used when sending a notification */
	/* either xml or json */
	protected String notificationPayloadContentType;
	
	/*
	 * 2017 07 25 - BONNARDEL Gregory (Thales)
	 * nbOfFailedNotifications keep track of the number of notifications that 
	 * have not been successfully delivered.
	 */
	@Column(name="nbOfFailedNotifications")
	protected Integer nbOfFailedNotifications;
	
	protected String subscriberURI;
	
	// links to parents
	@ManyToOne(targetEntity = AeEntity.class, fetch = FetchType.LAZY)
	@JoinTable(
			name = DBEntities.AESUB_JOIN,
			inverseJoinColumns = { @JoinColumn(name = DBEntities.AE_JOINID, referencedColumnName = ShortName.RESOURCE_ID) }, 
			joinColumns = { @JoinColumn(name = DBEntities.SUB_JOIN_ID, referencedColumnName = ShortName.RESOURCE_ID) }
			)
	protected AeEntity parentAe;

	@ManyToOne(targetEntity = ContainerEntity.class, fetch = FetchType.LAZY)
	@JoinTable(
			name = DBEntities.CNTSUB_JOIN,
			inverseJoinColumns = { @JoinColumn(name = DBEntities.CNT_JOIN_ID, referencedColumnName = ShortName.RESOURCE_ID) }, 
			joinColumns = { @JoinColumn(name = DBEntities.SUB_JOIN_ID, referencedColumnName = ShortName.RESOURCE_ID) }
			)
	protected ContainerEntity parentCnt;
	
	@ManyToOne(targetEntity=FlexContainerEntity.class, fetch=FetchType.LAZY)
	@JoinTable(
			name=DBEntities.FCNTSUB_JOIN,
			inverseJoinColumns={@JoinColumn(name=DBEntities.FCNT_JOIN_ID, referencedColumnName=ShortName.RESOURCE_ID)},
			joinColumns={ @JoinColumn(name=DBEntities.SUB_JOIN_ID, referencedColumnName = ShortName.RESOURCE_ID) }
			)
	protected FlexContainerEntity parentFlexCnt;

	@ManyToOne(targetEntity = CSEBaseEntity.class, fetch = FetchType.LAZY)
	@JoinTable(
			name = DBEntities.CSBSUB_JOIN,
			inverseJoinColumns = { @JoinColumn(name = DBEntities.CSEB_JOIN_ID, referencedColumnName = ShortName.RESOURCE_ID) }, 
			joinColumns = { @JoinColumn(name = DBEntities.SUB_JOIN_ID, referencedColumnName = ShortName.RESOURCE_ID) }
			)
	protected CSEBaseEntity parentCsb;

	@ManyToOne(targetEntity = GroupEntity.class, fetch = FetchType.LAZY)
	@JoinTable(
			name = DBEntities.GRPSUB_JOIN,
			inverseJoinColumns = { @JoinColumn(name = DBEntities.GRP_JOIN_ID, referencedColumnName = ShortName.RESOURCE_ID) }, 
			joinColumns = { @JoinColumn(name = DBEntities.SUB_JOIN_ID, referencedColumnName = ShortName.RESOURCE_ID) }
			)
	protected GroupEntity parentGrp;

	@ManyToOne(targetEntity = RemoteCSEEntity.class, fetch = FetchType.LAZY)
	@JoinTable(
			name = DBEntities.CSRSUB_JOIN,
			inverseJoinColumns = { @JoinColumn(name = DBEntities.CSR_JOIN_ID, referencedColumnName = ShortName.RESOURCE_ID) }, 
			joinColumns = { @JoinColumn(name = DBEntities.SUB_JOIN_ID, referencedColumnName = ShortName.RESOURCE_ID) }
			)
	protected RemoteCSEEntity parentCsr;

	@ManyToOne(targetEntity = AccessControlPolicyEntity.class, fetch = FetchType.LAZY)
	@JoinTable(
			name = DBEntities.ACPSUB_JOIN,
			inverseJoinColumns = { @JoinColumn(name = DBEntities.ACP_JOIN_ID, referencedColumnName = ShortName.RESOURCE_ID) }, 
			joinColumns = { @JoinColumn(name = DBEntities.SUB_JOIN_ID, referencedColumnName = ShortName.RESOURCE_ID) }
			)
	protected AccessControlPolicyEntity parentAcp;

	@ManyToOne(targetEntity = ScheduleEntity.class, fetch = FetchType.LAZY)
	@JoinTable(
			name = DBEntities.SCHSUB_JOIN,
			inverseJoinColumns = { @JoinColumn(name = DBEntities.SCH_JOIN_ID, referencedColumnName = ShortName.RESOURCE_ID) }, 
			joinColumns = { @JoinColumn(name = DBEntities.SUB_JOIN_ID, referencedColumnName = ShortName.RESOURCE_ID) }
			)
	protected ScheduleEntity parentSch;
	
	@ManyToOne(targetEntity = NodeEntity.class, fetch = FetchType.LAZY)
	@JoinTable(
			name = DBEntities.NOD_SUB_JOIN,
			inverseJoinColumns = { @JoinColumn(name = DBEntities.NOD_JOIN_ID, referencedColumnName = ShortName.RESOURCE_ID) }, 
			joinColumns = { @JoinColumn(name = DBEntities.SUB_JOIN_ID, referencedColumnName = ShortName.RESOURCE_ID) }
			)
	protected ScheduleEntity parentNode;
	
	
	// Database link to Subscriptions
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = AreaNwkInfoEntity.class)
	@JoinTable(
			name = DBEntities.ANI_SUB_JOIN,
			inverseJoinColumns = { @JoinColumn(name = DBEntities.ANI_JOIN_ID, referencedColumnName = ShortName.RESOURCE_ID) }, 
			joinColumns = { @JoinColumn(name = DBEntities.SUB_JOIN_ID, referencedColumnName = ShortName.RESOURCE_ID) }
			)
	protected AreaNwkInfoEntity parentAni;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = AreaNwkDeviceInfoEntity.class)
	@JoinTable(
			name = DBEntities.ANDI_SUB_JOIN,
			inverseJoinColumns = { @JoinColumn(name = DBEntities.ANDI_JOIN_ID, referencedColumnName = ShortName.RESOURCE_ID) }, 
			joinColumns = { @JoinColumn(name = DBEntities.SUB_JOIN_ID, referencedColumnName = ShortName.RESOURCE_ID) }
			)
	protected AreaNwkDeviceInfoEntity parentAndi;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = DeviceInfoEntity.class)
	@JoinTable(
			name = DBEntities.DVI_SUB_JOIN,
			inverseJoinColumns = { @JoinColumn(name = DBEntities.DVI_JOIN_ID, referencedColumnName = ShortName.RESOURCE_ID) }, 
			joinColumns = { @JoinColumn(name = DBEntities.SUB_JOIN_ID, referencedColumnName = ShortName.RESOURCE_ID) }
			)
	protected DeviceInfoEntity parentDvi;
	
	// link to schedule entity
	@OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, targetEntity = ScheduleEntity.class)
	@JoinColumn(name = "sch_id")
	protected ScheduleEntity childSchedule;
	

	/**
	 * @return the expirationCounter
	 */
	public BigInteger getExpirationCounter() {
		return expirationCounter;
	}

	/**
	 * @param expirationCounter the expirationCounter to set
	 */
	public void setExpirationCounter(BigInteger expirationCounter) {
		this.expirationCounter = expirationCounter;
	}

	/**
	 * @return the notificationURI
	 */
	public List<String> getNotificationURI() {
		if(notificationURI == null){
			notificationURI = new ArrayList<>();
		}
		return notificationURI;
	}

	/**
	 * @param notificationURI the notificationURI to set
	 */
	public void setNotificationURI(List<String> notificationURI) {
		this.notificationURI = notificationURI;
	}

	/**
	 * @return the groupID
	 */
	public String getGroupID() {
		return groupID;
	}

	/**
	 * @param groupID the groupID to set
	 */
	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	/**
	 * @return the notificationForwardingURI
	 */
	public String getNotificationForwardingURI() {
		return notificationForwardingURI;
	}

	/**
	 * @param notificationForwardingURI the notificationForwardingURI to set
	 */
	public void setNotificationForwardingURI(String notificationForwardingURI) {
		this.notificationForwardingURI = notificationForwardingURI;
	}

	/**
	 * @return the preSubscriptionNotify
	 */
	public BigInteger getPreSubscriptionNotify() {
		return preSubscriptionNotify;
	}

	/**
	 * @param preSubscriptionNotify the preSubscriptionNotify to set
	 */
	public void setPreSubscriptionNotify(BigInteger preSubscriptionNotify) {
		this.preSubscriptionNotify = preSubscriptionNotify;
	}

	/**
	 * @return the pendingNotification
	 */
	public BigInteger getPendingNotification() {
		return pendingNotification;
	}

	/**
	 * @param pendingNotification the pendingNotification to set
	 */
	public void setPendingNotification(BigInteger pendingNotification) {
		this.pendingNotification = pendingNotification;
	}

	/**
	 * @return the notificationStoragePriority
	 */
	public BigInteger getNotificationStoragePriority() {
		return notificationStoragePriority;
	}

	/**
	 * @param notificationStoragePriority the notificationStoragePriority to set
	 */
	public void setNotificationStoragePriority(
			BigInteger notificationStoragePriority) {
		this.notificationStoragePriority = notificationStoragePriority;
	}

	/**
	 * @return the latestNotify
	 */
	public Boolean getLatestNotify() {
		return latestNotify;
	}

	/**
	 * @param latestNotify the latestNotify to set
	 */
	public void setLatestNotify(Boolean latestNotify) {
		this.latestNotify = latestNotify;
	}

	/**
	 * @return the notificationContentType
	 */
	public BigInteger getNotificationContentType() {
		return notificationContentType;
	}

	/**
	 * @param notificationContentType the notificationContentType to set
	 */
	public void setNotificationContentType(BigInteger notificationContentType) {
		this.notificationContentType = notificationContentType;
	}

	/**
	 * @return the notificationEventCat
	 */
	public String getNotificationEventCat() {
		return notificationEventCat;
	}

	/**
	 * @param notificationEventCat the notificationEventCat to set
	 */
	public void setNotificationEventCat(String notificationEventCat) {
		this.notificationEventCat = notificationEventCat;
	}

	/**
	 * @return the creator
	 */
	public String getCreator() {
		return creator;
	}

	/**
	 * @param creator the creator to set
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}

	/**
	 * @return the subscriberURI
	 */
	public String getSubscriberURI() {
		return subscriberURI;
	}

	/**
	 * @param subscriberURI the subscriberURI to set
	 */
	public void setSubscriberURI(String subscriberURI) {
		this.subscriberURI = subscriberURI;
	}

	/**
	 * @return the parentAe
	 */
	public AeEntity getParentAe() {
		return parentAe;
	}

	/**
	 * @param parentAe the parentAe to set
	 */
	public void setParentAe(AeEntity parentAe) {
		this.parentAe = parentAe;
	}

	/**
	 * @return the parentCnt
	 */
	public ContainerEntity getParentCnt() {
		return parentCnt;
	}

	/**
	 * @param parentCnt the parentCnt to set
	 */
	public void setParentCnt(ContainerEntity parentCnt) {
		this.parentCnt = parentCnt;
	}
	
	/**
	 * @return the parentFleCnt
	 */
	public FlexContainerEntity getParentFlexCnt() {
		return parentFlexCnt;
	}

	/**
	 * @param parentFlexCnt the parentFlexCnt to set
	 */
	public void setParentFlexCnt(FlexContainerEntity parentFlexCnt) {
		this.parentFlexCnt = parentFlexCnt;
	}

	/**
	 * @return the parentCsb
	 */
	public CSEBaseEntity getParentCsb() {
		return parentCsb;
	}

	/**
	 * @param parentCsb the parentCsb to set
	 */
	public void setParentCsb(CSEBaseEntity parentCsb) {
		this.parentCsb = parentCsb;
	}

	/**
	 * @return the parentGrp
	 */
	public GroupEntity getParentGrp() {
		return parentGrp;
	}

	/**
	 * @param parentGrp the parentGrp to set
	 */
	public void setParentGrp(GroupEntity parentGrp) {
		this.parentGrp = parentGrp;
	}

	/**
	 * @return the parentCsr
	 */
	public RemoteCSEEntity getParentCsr() {
		return parentCsr;
	}

	/**
	 * @param parentCsr the parentCsr to set
	 */
	public void setParentCsr(RemoteCSEEntity parentCsr) {
		this.parentCsr = parentCsr;
	}

	/**
	 * @return the parentAcp
	 */
	public AccessControlPolicyEntity getParentAcp() {
		return parentAcp;
	}

	/**
	 * @param parentAcp the parentAcp to set
	 */
	public void setParentAcp(AccessControlPolicyEntity parentAcp) {
		this.parentAcp = parentAcp;
	}

	/**
	 * @return the parentSch
	 */
	public ScheduleEntity getParentSch() {
		return parentSch;
	}

	/**
	 * @param parentSch the parentSch to set
	 */
	public void setParentSch(ScheduleEntity parentSch) {
		this.parentSch = parentSch;
	}
	
	/**
	 * @return the parentAni
	 */
	public AreaNwkInfoEntity getParentAni() {
		return parentAni;
	}

	/**
	 * @param parentAni the parentAni to set
	 */
	public void setParentAni(AreaNwkInfoEntity parentAni) {
		this.parentAni = parentAni;
	}
	
	/**
	 * @return the parentAndi
	 */
	public AreaNwkDeviceInfoEntity getParentAndi() {
		return parentAndi;
	}

	/**
	 * @param parentAndi the parentAndi to set
	 */
	public void setParentAndi(AreaNwkDeviceInfoEntity parentAndi) {
		this.parentAndi = parentAndi;
	}
	
	/**
	 * @return the parentDvi
	 */
	public DeviceInfoEntity getParentDvi() {
		return parentDvi;
	}

	/**
	 * @param parentDvi the parentDvi to set
	 */
	public void setParentDvi(DeviceInfoEntity parentDvi) {
		this.parentDvi = parentDvi;
	}

	/**
	 * @return the linkedSchedule
	 */
	public ScheduleEntity getChildSchedule() {
		return childSchedule;
	}

	/**
	 * @param linkedSchedule the linkedSchedule to set
	 */
	public void setcChildSchedule(ScheduleEntity linkedSchedule) {
		this.childSchedule = linkedSchedule;
	}

	public void setParentEntity(ResourceEntity parentEntity) {
		int rt = parentEntity.getResourceType().intValue();
		
		switch (rt) {
		case ResourceType.ACCESS_CONTROL_POLICY:
			this.parentAcp = (AccessControlPolicyEntity) parentEntity;
			break;
		case ResourceType.AE:
			this.parentAe = (AeEntity) parentEntity;
			break;
		case ResourceType.CONTAINER:
			this.parentCnt = (ContainerEntity) parentEntity;
			break;
		case ResourceType.FLEXCONTAINER:
			this.parentFlexCnt = (FlexContainerEntity) parentEntity;
			break;
		case ResourceType.CSE_BASE:
			this.parentCsb = (CSEBaseEntity) parentEntity;
			break;
		case ResourceType.GROUP:
			this.parentGrp = (GroupEntity) parentEntity;
			break;
		case ResourceType.REMOTE_CSE:
			this.parentCsr = (RemoteCSEEntity) parentEntity;
			break;
		case ResourceType.SCHEDULE:
			this.parentSch = (ScheduleEntity) parentEntity;
			break;
		default:
			break;
		}
		
	}

	@Override
	public List<AccessControlPolicyEntity> getAccessControlPolicies() {
		if (accessControlPolicies == null) {
			accessControlPolicies = new ArrayList<>();
		}
		return accessControlPolicies;
	}

	@Override
	public void setAccessControlPolicies(List<AccessControlPolicyEntity> accessControlPolicies) {
		this.accessControlPolicies = accessControlPolicies;
	}

	@Override
	public List<DynamicAuthorizationConsultationEntity> getDynamicAuthorizationConsultations() {
		if (dynamicAuthorizationConsultations == null) {
			dynamicAuthorizationConsultations = new ArrayList<>();
		}
		return dynamicAuthorizationConsultations;
	}

	@Override
	public void setDynamicAuthorizationConsultations(List<DynamicAuthorizationConsultationEntity> list) {
		this.dynamicAuthorizationConsultations = list;
	}

	/**
	 * @return the notificationPayloadContentType
	 */
	public String getNotificationPayloadContentType() {
		return notificationPayloadContentType;
	}

	/**
	 * @param notificationPayloadContentType the notificationPayloadContentType to set
	 */
	public void setNotificationPayloadContentType(String notificationPayloadContentType) {
		this.notificationPayloadContentType = notificationPayloadContentType;
	}

	/**
	 * @return the nbOfFailedNotifications
	 */
	public Integer getNbOfFailedNotifications() {
		return nbOfFailedNotifications;
	}

	/**
	 * @param nbOfFailedNotifications the nbOfFailedNotifications to set
	 */
	public void setNbOfFailedNotifications(Integer nbOfFailedNotifications) {
		this.nbOfFailedNotifications = nbOfFailedNotifications;
	}
	
	
	
}
