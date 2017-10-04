/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.api.rmi.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.trilogy.app.crm.api.ApiSupport;
import com.trilogy.app.crm.api.Constants;
import com.trilogy.app.crm.api.rmi.ClosedUserGroupTemplateToApiAdapter;
import com.trilogy.app.crm.api.rmi.ClosedUserGroupTemplateToApiReferenceAdapter;
import com.trilogy.app.crm.api.rmi.ClosedUserGroupToApiAdapter;
import com.trilogy.app.crm.api.rmi.ClosedUserGroupToApiReferenceAdapter;
import com.trilogy.app.crm.api.rmi.GenericParameterParser;
import com.trilogy.app.crm.api.rmi.PersonalListPlanToApiAdapter;
import com.trilogy.app.crm.api.rmi.PersonalListPlanToApiReferenceAdapter;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.api.rmi.support.SubscribersApiSupport;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.bean.ClosedSub;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.ClosedUserGroupHome;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplateHome;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplateXInfo;
import com.trilogy.app.crm.bean.ClosedUserGroupXInfo;
import com.trilogy.app.crm.bean.GLCodeMapping;
import com.trilogy.app.crm.bean.GLCodeMappingXInfo;
import com.trilogy.app.crm.bean.PersonalListPlanHome;
import com.trilogy.app.crm.bean.PersonalListPlanXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.TaxAuthority;
import com.trilogy.app.crm.bean.TaxAuthorityXInfo;
import com.trilogy.app.crm.ff.FFEcareException;
import com.trilogy.app.crm.ff.PersonalListPlanSupport;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.ComparableComparator;
import com.trilogy.framework.xhome.beans.ReverseComparator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.CRMException;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SuccessCode;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SuccessCodeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.callinggroup.ShortCodeEntry;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CallingGroupServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroupModificationResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroupReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroupTemplate;
import com.trilogy.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroupTemplateModificationResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroupTemplateReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.callinggroup.PersonalListPlan;
import com.trilogy.util.crmapi.wsdl.v3_0.types.callinggroup.PersonalListPlanModificationResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.callinggroup.PersonalListPlanReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference;
import com.trilogy.app.crm.api.queryexecutor.GenericParametersAdapter;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceXInfo;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bundle.BundleAuxiliaryService;
import com.trilogy.app.crm.bundle.BundleAuxiliaryServiceHome;
import com.trilogy.app.crm.bundle.BundleAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.app.crm.bundle.InvalidBundleApiException;


/**
 * Implementation of CallingGroups interface.
 *
 * @author victor.stratan@redknee.com
 */
public class CallingGroupsImpl implements CallingGroupServiceSkeletonInterface, ContextAware
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    
    public static final int PARAM_MOBILE_NUMBER = 1;
    public static final int PARAM_SPID = 2;
    public static final String PARAM_MOBILE_NUMBER_NAME = "MemberMSISDN";
    public static final String PARAM_SPID_NAME = "SPID";


    /**
     * Create a new instance of <code>CallingGroupsImpl</code>.
     *
     * @param ctx
     *            The operating context.
     * @throws RemoteException
     *             Thrown by RMI.
     */
    public CallingGroupsImpl(final Context ctx) throws RemoteException
    {
        this.context_ = ctx;
        this.closedUserGroupReferenceAdapter_ = new ClosedUserGroupToApiReferenceAdapter();
        this.closedUserGroupTemplateReferenceAdapter_ = new ClosedUserGroupTemplateToApiReferenceAdapter();
        this.closedUserGroupAdapter_ = new ClosedUserGroupToApiAdapter();
        this.closedUserGroupTemplateAdapter_ = new ClosedUserGroupTemplateToApiAdapter();
        this.personalListPlanAdapter_ = new PersonalListPlanToApiAdapter();
        this.personalListPlanReferenceAdapter_ = new PersonalListPlanToApiReferenceAdapter();
    }


    /**
     * {@inheritDoc}
     */
    public String[] listSubscriptionPersonalListPlanEntries(final CRMRequestHeader header,
            final SubscriptionReference subscriptionRef, final Long listID, Boolean isAscending,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listSubscriptionPersonalListPlanEntries",
                Constants.PERMISSION_CALLINGGROUPS_READ_LISTSUBSCRIPTIONPERSONALLISTPLANENTRIES,
                Constants.PERMISSION_CALLINGGROUPS_READ_LISTSUBSCRIBERPERSONALLISTPLANENTRIES);
        final Subscriber sub = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        Map plpMap = getPLPMap(ctx, sub);
        long newListID = resolvePLPID(ctx, sub, listID, plpMap);
        Set<String> entries = (Set<String>) plpMap.get(Long.valueOf(newListID));
        if (RmiApiSupport.isSortAscending(isAscending))
        {
            entries = new TreeSet<String>(entries);
        }
        else
        {
            Set<String> unsortedEntries = entries;
            entries = new TreeSet<String>(new ReverseComparator(ComparableComparator.instance()));
            entries.addAll(unsortedEntries);
        }
        final String[] result = entries.toArray(new String[]
            {});
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public SuccessCode addSubscriptionPersonalListPlanEntries(final CRMRequestHeader header,
            final SubscriptionReference subscriptionRef, final Long listID, final String[] mobileNumbers,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "addSubscriptionPersonalListPlanEntries",
                Constants.PERMISSION_CALLINGGROUPS_WRITE_ADDSUBSCRIPTIONPERSONALLISTPLANENTRIES,
                Constants.PERMISSION_CALLINGGROUPS_WRITE_ADDSUBSCRIBERPERSONALLISTPLANENTRIES);
        RmiApiErrorHandlingSupport.validateMandatoryObject(mobileNumbers, "mobileNumbers");
        Subscriber sub = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        Map plpMap = getPLPMap(ctx, sub);
        long newListID = resolvePLPID(ctx, sub, listID, plpMap);
        final Set entries = (Set) plpMap.get(Long.valueOf(newListID));
        for (int i = 0; i < mobileNumbers.length; i++)
        {
            entries.add(mobileNumbers[i]);
        }
        try
        {
            PersonalListPlanSupport.updatePersonalListPlanMSISDNs(ctx, sub, newListID, entries);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to add Personal List Plan for Subscription " + subscriptionRef;
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
        }
        return SuccessCodeEnum.SUCCESS.getValue();
    }


    /**
     * {@inheritDoc}
     */
    public SuccessCode removeSubscriptionPersonalListPlanEntries(final CRMRequestHeader header,
            final SubscriptionReference subscriptionRef, final Long listID, final String[] mobileNumbers, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "removeSubscriptionPersonalListPlanEntries",
                Constants.PERMISSION_CALLINGGROUPS_WRITE_REMOVESUBSCRIPTIONPERSONALLISTPLANENTRIES,
                Constants.PERMISSION_CALLINGGROUPS_WRITE_REMOVESUBSCRIBERPERSONALLISTPLANENTRIES);
        RmiApiErrorHandlingSupport.validateMandatoryObject(mobileNumbers, "mobileNumbers");
        Subscriber sub = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        Map plpMap = getPLPMap(ctx, sub);
        long newListID = resolvePLPID(ctx, sub, listID, plpMap);
        final Set entries = (Set) plpMap.get(Long.valueOf(newListID));
        for (int i = 0; i < mobileNumbers.length; i++)
        {
            entries.remove(mobileNumbers[i]);
        }
        try
        {
            PersonalListPlanSupport.updatePersonalListPlanMSISDNs(ctx, sub, newListID, entries);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to remove Personal List Plan for Subscription " + subscriptionRef;
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
        }
        return SuccessCodeEnum.SUCCESS.getValue();
    }


    /**
     * {@inheritDoc}
     */
    public ClosedUserGroupReference[] listClosedUserGroups(final CRMRequestHeader header, final int spid,
            Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listClosedUserGroups",
                Constants.PERMISSION_CALLINGGROUPS_READ_LISTCLOSEDUSERGROUPS);
        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
        ClosedUserGroupReference[] cardPackageGroupReferences = new ClosedUserGroupReference[]
            {};
        try
        {
            final Object condition = new EQ(ClosedUserGroupXInfo.SPID, spid);
            final Collection<ClosedUserGroup> collection = HomeSupportHelper.get(ctx).getBeans(ctx,
                    ClosedUserGroup.class, condition, RmiApiSupport.isSortAscending(isAscending));
            cardPackageGroupReferences = CollectionSupportHelper.get(ctx).adaptCollection(ctx, collection,
                    this.closedUserGroupReferenceAdapter_, cardPackageGroupReferences);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Closed User Groups for Service Provider=" + spid;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return cardPackageGroupReferences;
    }


    /**
     * {@inheritDoc}
     */
    public String[] listClosedUserGroupEntries(final CRMRequestHeader header, final long groupID, Boolean isAscending,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listClosedUserGroupEntries",
                Constants.PERMISSION_CALLINGGROUPS_READ_LISTCLOSEDUSERGROUPENTRIES);
        final com.redknee.app.crm.bean.ClosedUserGroup cug = getCrmClosedUserGroup(ctx, groupID);
        Set<String> entries = cug.getSubscribers().keySet();
        if (RmiApiSupport.isSortAscending(isAscending))
        {
            entries = new TreeSet<String>(entries);
        }
        else
        {
            Set<String> unsortedEntries = entries;
            entries = new TreeSet<String>(new ReverseComparator(ComparableComparator.instance()));
            entries.addAll(unsortedEntries);
        }
        final String[] result = entries.toArray(new String[entries.size()]);
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public SuccessCode addClosedUserGroupEntries(final CRMRequestHeader header, final long groupID,
            final String[] mobileNumbers, GenericParameter[] parameters) throws CRMExceptionFault
            {
            final Context ctx = getContext().createSubContext();
            // handle login. Failed logins throw exceptions
            RmiApiSupport.authenticateUser(ctx, header, "addClosedUserGroupEntries",
                    Constants.PERMISSION_CALLINGGROUPS_WRITE_ADDCLOSEDUSERGROUPENTRIES);

            RmiApiErrorHandlingSupport.validateMandatoryObject(mobileNumbers, "mobileNumbers");

            final com.redknee.app.crm.bean.ClosedUserGroup cug = getCrmClosedUserGroup(ctx, groupID);

            if (mobileNumbers.length == 0)
            {
                // nothing to do
                return SuccessCodeEnum.SUCCESS.getValue();
            }

            Home home = getClosedUserGroupHome(ctx);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            try
            {
                final Map<String, com.redknee.app.crm.bean.ClosedSub> subscribers = cug.getSubscribers();
                for (final String element : mobileNumbers)
                {
                    if (subscribers.get(element) == null)
                    {
                        com.redknee.app.crm.bean.ClosedSub s = new com.redknee.app.crm.bean.ClosedSub();
                        s.setPhoneID(element);
                        subscribers.put(element, s);
                    }
                }

                home.store(ctx, cug);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to update Closed User Group " + groupID;
                RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
            }

            return SuccessCodeEnum.SUCCESS.getValue();

            }


    /**
     * {@inheritDoc}
     */
    public SuccessCode removeClosedUserGroupEntries(final CRMRequestHeader header, final long groupID,
            final String[] mobileNumbers, GenericParameter[] parameters) throws CRMExceptionFault
            {
            final Context ctx = getContext().createSubContext();
            // handle login. Failed logins throw exceptions
            RmiApiSupport.authenticateUser(ctx, header, "removeClosedUserGroupEntries",
                    Constants.PERMISSION_CALLINGGROUPS_WRITE_REMOVECLOSEDUSERGROUPENTRIES);

            RmiApiErrorHandlingSupport.validateMandatoryObject(mobileNumbers, "mobileNumbers");

            final com.redknee.app.crm.bean.ClosedUserGroup cug = getCrmClosedUserGroup(ctx, groupID);

            if (mobileNumbers.length == 0)
            {
                // nothing to do
                return SuccessCodeEnum.SUCCESS.getValue();
            }

            Home home = getClosedUserGroupHome(ctx);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, getClosedUserGroupHome(ctx));
            
            try
            {
                final Set<String> subscribers = cug.getSubscribers().keySet();
                
                boolean isPCUG = cug.getAuxiliaryService(ctx).isPrivateCUG(ctx);
                boolean isRemoval = false;

                if (isPCUG)
                {
                    String cugAccountBan = ClosedUserGroupSupport.getCugAccount(ctx, cug.getID());
                    final Set<String> privateCugMsisdns = new HashSet<String>(cug.getSubscribers().keySet());
                    for (final String element : mobileNumbers)
                    {
                    	Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, element);
                    	if(sub != null)
                    	{
                    	    
                        if (cugAccountBan != null && sub.getRootAccount(ctx).getBAN().equals(cugAccountBan))
                    	    {
                    	        throw new CRMExceptionFault( "Subscription with MSISDN \"" + element
                                            + "\" is a child of CUG Account " + cugAccountBan + ". Cannot delete member from CUG Account Hierarchy.");
                    	    }
                    	}
                        privateCugMsisdns.remove(element);
                    }
                    
                    if (privateCugMsisdns.isEmpty())
                    {
                        isRemoval = true;
                    }
                }
                
                
                if (isPCUG && isRemoval)
                {
                    home.remove(ctx, cug);
                }
                else
                {
                    for (final String element : mobileNumbers)
                    {
                        subscribers.remove(element);
                    }

                    home.store(ctx, cug);
                }
            }
            catch (final Exception e)
            {
                final String msg = "Unable to update Closed User Group " + groupID;
                RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
            }

            return SuccessCodeEnum.SUCCESS.getValue();

            }


    /**
     * {@inheritDoc}
     */
    public SuccessCode addClosedUserGroupShortCodeEntries(CRMRequestHeader header, long groupID,
            ShortCodeEntry[] entries, GenericParameter[] parameters) throws CRMExceptionFault
    {
            final Context ctx = getContext().createSubContext();
            // handle login. Failed logins throw exceptions
            RmiApiSupport.authenticateUser(ctx, header, "addClosedUserGroupShortCodeEntries",
                    Constants.PERMISSION_CALLINGGROUPS_WRITE_ADDCLOSEDUSERGROUPSHORTCODEENTRIES);
            
            final com.redknee.app.crm.bean.ClosedUserGroup cug = getCrmClosedUserGroup(ctx, groupID);
            @SuppressWarnings("unchecked")
            final Map<String, ClosedSub> subscriberMap = cug.getSubscribers();
            try
            {
                for (ShortCodeEntry shortCodeEntry : entries)
                {
                    final String number = shortCodeEntry.getMobileNumber();
                    if (subscriberMap.containsKey(number))
                    {
                        throw new Exception("Entry already exists for number: " + number);
                    }
                    else
                    {
                        final ClosedSub newEntry;
                        {
                            newEntry = new ClosedSub();
                            newEntry.setPhoneID(number);
                            newEntry.setShortCode(shortCodeEntry.getShortCode());
                        }
                        subscriberMap.put(number, newEntry);
                        
                    }
                }
                
                ctx.put(ClosedUserGroupHome.class, ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx,  getClosedUserGroupHome(ctx)));                
                HomeSupportHelper.get(ctx).storeBean(ctx, cug);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to update Closed User Group " + groupID;
                RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
            }
            return SuccessCodeEnum.SUCCESS.getValue();
    }


    /**
     * {@inheritDoc}
     */
    public ShortCodeEntry[] listClosedUserGroupShortCodeEntries(CRMRequestHeader header, long groupID,
            Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
            final Context ctx = getContext().createSubContext();
            // handle login. Failed logins throw exceptions
            RmiApiSupport.authenticateUser(ctx, header, "updateClosedUserGroupShortCodeEntries",
                    Constants.PERMISSION_CALLINGGROUPS_READ_LISTCLOSEDUSERGROUPSHORTCODEENTRIES);
            final com.redknee.app.crm.bean.ClosedUserGroup cug = getCrmClosedUserGroup(ctx, groupID);
            final ArrayList<ShortCodeEntry> shortCodeEntries = new ArrayList<ShortCodeEntry>();
            @SuppressWarnings("unchecked")
            final Map<String, ClosedSub> subscriberMap = cug.getSubscribers();
            try
            {
                for (Map.Entry<String, ClosedSub> entry : subscriberMap.entrySet())
                {
                    final ClosedSub closedSub = entry.getValue();
                    final ShortCodeEntry shortCodeEntry;
                    {
                        shortCodeEntry = new ShortCodeEntry();
                        shortCodeEntry.setMobileNumber(closedSub.getPhoneID());
                        shortCodeEntry.setShortCode(closedSub.getShortCode());
                    }
                    shortCodeEntries.add(shortCodeEntry);
                }
            }
            catch (final Exception e)
            {
                final String msg = "Unable to update Closed User Group " + groupID;
                RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
            }
            return shortCodeEntries.toArray(new ShortCodeEntry[0]);
    }


    /**
     * {@inheritDoc}
     */
    public SuccessCode updateClosedUserGroupShortCodeEntries(CRMRequestHeader header, long groupID,
            ShortCodeEntry[] entries, GenericParameter[] parameters) throws CRMExceptionFault
    {
            final Context ctx = getContext().createSubContext();
            // handle login. Failed logins throw exceptions
            RmiApiSupport.authenticateUser(ctx, header, "updateClosedUserGroupShortCodeEntries",
                    Constants.PERMISSION_CALLINGGROUPS_WRITE_UPDATECLOSEDUSERGROUPSHORTCODEENTRIES);
            final com.redknee.app.crm.bean.ClosedUserGroup cug = getCrmClosedUserGroup(ctx, groupID);
            final Home home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, getClosedUserGroupHome(ctx));
            ctx.put(ClosedUserGroupHome.class, home);
            
            @SuppressWarnings("unchecked")
            final Map<String, ClosedSub> subscriberMap = cug.getSubscribers();
            try
            {
                for (ShortCodeEntry shortCodeEntry : entries)
                {
                    final String number = shortCodeEntry.getMobileNumber();
                    final ClosedSub closedSub = subscriberMap.get(number);
                    if (null != closedSub)
                    {
                        closedSub.setShortCode(shortCodeEntry.getShortCode());
                        subscriberMap.put(number, closedSub);
                    }
                    else
                    {
                        throw new Exception("Entry does not exist for number: " + number);
                    }
                }
                cug.setSubscribers(subscriberMap);
                HomeSupportHelper.get(ctx).storeBean(ctx, cug);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to update Closed User Group " + groupID;
                RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
            }
            return SuccessCodeEnum.SUCCESS.getValue();
    }


    private Map getPLPMap(Context ctx, final Subscriber sub) throws com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault
    {
        Map plpLists = null;

        try 
        {
            plpLists = PersonalListPlanSupport.getPersonalListPlanMSISDNs(ctx, sub.getMSISDN());
        }
        catch (FFEcareException e )
        {
            RmiApiErrorHandlingSupport.generalException(ctx,e, "Fail to get PLP", this);
        }

        for(Iterator it = sub.getPersonalListPlan().iterator(); it.hasNext(); )
        {
            Long plpid = (Long) it.next();
            if (plpLists.get( plpid) == null)
            {
                plpLists.put(plpid, new TreeSet()); 
            }
        }
        return plpLists;
    }



    private long resolvePLPID(Context ctx,  final Subscriber sub, final Long listID, Map plpLists) 
    throws com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault
    {
        long newListID = -1;

        if (listID != null)
        {
            newListID = listID.longValue(); 
            if (!plpLists.containsKey(listID))
            {
                final String msg = "PLP ID " + listID + " for subscriber " + sub.getId();
                RmiApiErrorHandlingSupport.identificationException(ctx, msg, this);
            }
        }
        else if (plpLists.size() == 1)
        {
            newListID = ((Long) plpLists.keySet().iterator().next()).longValue();   
        }
        else if (plpLists.size() > 1)
        {
            final String msg = " Subscriber " + sub.getId() + " has more than one PLP Provisioned (listID required).";
            RmiApiErrorHandlingSupport.generalException(ctx,null, msg, this);
        }
        else
        {
            final String msg = " Subscriber " + sub.getId() + " has no PLP Provisioned";
            RmiApiErrorHandlingSupport.generalException(ctx,null, msg, this);
        }

        return newListID;
    }

    /**
     * Returns the CRM closed user group object.
     *
     * @param ctx
     *            The operating context.
     * @param groupID
     *            Closed user group identifier.
     * @return The identified closed user group.
     * @throws CRMExceptionFault
     *             Thrown if the closed user group does not exist.
     */
    private com.redknee.app.crm.bean.ClosedUserGroup getCrmClosedUserGroup(final Context ctx, final long groupID)
    throws com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault
    {
        com.redknee.app.crm.bean.ClosedUserGroup cug = null;
        try
        {
            cug = HomeSupportHelper.get(ctx).findBean(ctx, com.redknee.app.crm.bean.ClosedUserGroup.class, groupID);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Closed User Group " + groupID;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }

        if (cug == null)
        {
            final String msg = "Closed User Group " + groupID;
            RmiApiErrorHandlingSupport.identificationException(ctx, msg, this);
        }

        return cug;
    }
    

    /**
     * 
     * @param ctx
     * @param groupID
     * @return
     * @throws com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault
     */
    private com.redknee.app.crm.bean.ClosedUserGroupTemplate getCrmClosedUserGroupTemplate(final Context ctx,
            final long groupTemplateID) throws com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault
    {
        com.redknee.app.crm.bean.ClosedUserGroupTemplate cug = null;
        try
        {
            cug = HomeSupportHelper.get(ctx).findBean(ctx, com.redknee.app.crm.bean.ClosedUserGroupTemplate.class,
                    groupTemplateID);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Closed User Group Template" + groupTemplateID;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        if (cug == null)
        {
            final String msg = "Closed User Group Template" + groupTemplateID;
            RmiApiErrorHandlingSupport.identificationException(ctx, msg, this);
        }
        return cug;
    }

    
    /**
     * {@inheritDoc}
     */
    public ClosedUserGroupModificationResult createClosedUserGroup(CRMRequestHeader header,
            com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroup cug, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "createClosedUserGroup",
                Constants.PERMISSION_CALLINGGROUPS_WRITE_CREATECLOSEDUSERGROUP);
        RmiApiErrorHandlingSupport.validateMandatoryObject(cug, "cug");
        RmiApiErrorHandlingSupport.validateMandatoryObject(cug.getCugState(), "cug.cugState");
        RmiApiErrorHandlingSupport.validateMandatoryObject(cug.getCugTemplateID(), "cug.cugTemplateID");
        if (cug.getSpid() != null)
        {
            RmiApiSupport.validateExistanceOfBeanForKey(ctx, CRMSpid.class, new EQ(CRMSpidXInfo.ID, cug.getSpid()));
        }        
        
        ClosedUserGroupModificationResult closedUserGroupModification  = new ClosedUserGroupModificationResult();
        try
        {
            And and = new And();
            and.add(new EQ(ClosedUserGroupTemplateXInfo.ID, cug.getCugTemplateID()));
            if (cug.getSpid() != null)
            {
                and.add(new EQ(ClosedUserGroupTemplateXInfo.SPID, cug.getSpid()));
            }
            com.redknee.app.crm.bean.ClosedUserGroupTemplate template = HomeSupportHelper.get(ctx).findBean(ctx,
                    com.redknee.app.crm.bean.ClosedUserGroupTemplate.class, and);
            if (template == null)
            {
                RmiApiErrorHandlingSupport.identificationException(ctx,
                        "Template does not exist : " + cug.getCugTemplateID() + " spid : " + cug.getSpid(), this);
            }
            if (template.isDeprecated())
            {
                RmiApiErrorHandlingSupport.simpleValidation("cug",
                        "Cug Template is deprecated : " + cug.getCugTemplateID());
            }
            Home home = getClosedUserGroupHome(ctx);    
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, getClosedUserGroupHome(ctx));
            ClosedUserGroup crmClosedUserGroup = (ClosedUserGroup) ClosedUserGroupToApiAdapter.adaptApiToClosedUserGroup(ctx, cug);
            Object obj = home.create(crmClosedUserGroup);
            closedUserGroupModification.setCug((com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroup) closedUserGroupAdapter_.adapt(ctx, obj));
            closedUserGroupModification.setParameters(parameters);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to create Closed User Group";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return closedUserGroupModification;
    }


    /**
     * {@inheritDoc}
     */
    public ClosedUserGroupTemplateModificationResult createClosedUserGroupTemplate(CRMRequestHeader header,
            ClosedUserGroupTemplate cugTemplate, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "createClosedUserGroupTemplate",
                Constants.PERMISSION_CALLINGGROUPS_WRITE_CREATECLOSEDUSERGROUPTEMPLATE);
        RmiApiErrorHandlingSupport.validateMandatoryObject(cugTemplate, "cugTemplate");
        RmiApiErrorHandlingSupport.validateMandatoryObject(cugTemplate.getSpid(), "cugTemplate.spid");
        RmiApiErrorHandlingSupport.validateMandatoryObject(cugTemplate.getActivationFeeType(), "cugTemplate.activationFeeType");
        RmiApiErrorHandlingSupport.validateMandatoryObject(cugTemplate.getCugServiceType(), "cugTemplate.cugServiceType");
        RmiApiErrorHandlingSupport.validateMandatoryObject(cugTemplate.getCugType(), "cugTemplate.cugType");
        RmiApiSupport.validateExistanceOfBeanForKey(ctx, CRMSpid.class, new EQ(CRMSpidXInfo.ID, cugTemplate.getSpid()));
        ClosedUserGroupTemplateModificationResult closedUserGroupTemplateModification  = new ClosedUserGroupTemplateModificationResult();
        try
        {
            Home home = RmiApiSupport.getCrmHome(ctx, ClosedUserGroupTemplateHome.class, this);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            com.redknee.app.crm.bean.ClosedUserGroupTemplate crmClosedUserGroup = (com.redknee.app.crm.bean.ClosedUserGroupTemplate) ClosedUserGroupTemplateToApiAdapter
                    .adaptApiToClosedUserGroupTemplate(ctx, cugTemplate);            
            RmiApiSupport.validateExistanceOfBeanForKey(
                    ctx,
                    GLCodeMapping.class,
                    new And().add(new EQ(GLCodeMappingXInfo.SPID, crmClosedUserGroup.getSpid())).add(
                            new EQ(GLCodeMappingXInfo.GL_CODE, crmClosedUserGroup.getGlCode())));
            RmiApiSupport.validateExistanceOfBeanForKey(
                    ctx,
                    TaxAuthority.class,
                    new And().add(new EQ(TaxAuthorityXInfo.SPID, crmClosedUserGroup.getSpid())).add(
                            new EQ(TaxAuthorityXInfo.TAX_ID, crmClosedUserGroup.getTaxAuthority())));
            Object obj = home.create(crmClosedUserGroup);
            closedUserGroupTemplateModification.setCugTemplate((ClosedUserGroupTemplate) closedUserGroupTemplateAdapter_.adapt(ctx, obj));
            closedUserGroupTemplateModification.setParameters(parameters);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to create Closed User Group Template";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return closedUserGroupTemplateModification;
    }


    /**
     * {@inheritDoc}
     */
    public PersonalListPlanModificationResult createPersonalListPlan(CRMRequestHeader header, PersonalListPlan plp,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "createPersonalListPlan",
                Constants.PERMISSION_CALLINGGROUPS_WRITE_CREATEPERSONALLISTPLAN);
        RmiApiErrorHandlingSupport.validateMandatoryObject(plp, "plp");
        RmiApiErrorHandlingSupport.validateMandatoryObject(plp.getSpid(), "plp.spid");
        RmiApiErrorHandlingSupport.validateMandatoryObject(plp.getPlpServiceType(), "plp.plpServiceType");
        RmiApiErrorHandlingSupport.validateMandatoryObject(plp.getParameters(), "plp.parameters");
        RmiApiSupport.validateExistanceOfBeanForKey(ctx, CRMSpid.class, new EQ(CRMSpidXInfo.ID, plp.getSpid()));        
        PersonalListPlanModificationResult personalListPlanModification  = new PersonalListPlanModificationResult();
        try
        {
            Home home = RmiApiSupport.getCrmHome(ctx, PersonalListPlanHome.class, this);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            com.redknee.app.crm.bean.PersonalListPlan crmPlp = (com.redknee.app.crm.bean.PersonalListPlan) PersonalListPlanToApiAdapter
                    .adaptApiToPersonalListPlan(ctx, plp);
            PersonalListPlanToApiAdapter.adaptGenericParametersToCreatePersonalListPlan(ctx, plp.getParameters(), crmPlp); 
            RmiApiSupport.validateExistanceOfBeanForKey(
                    ctx,
                    GLCodeMapping.class,
                    new And().add(new EQ(GLCodeMappingXInfo.SPID, crmPlp.getSpid())).add(
                            new EQ(GLCodeMappingXInfo.GL_CODE, crmPlp.getAdjustmentGLCode())));
            RmiApiSupport.validateExistanceOfBeanForKey(
                    ctx,
                    TaxAuthority.class,
                    new And().add(new EQ(TaxAuthorityXInfo.SPID, crmPlp.getSpid())).add(
                            new EQ(TaxAuthorityXInfo.TAX_ID, crmPlp.getTaxAuthority())));                       
            Object obj = home.create(crmPlp);
            personalListPlanModification.setPlp((PersonalListPlan) personalListPlanAdapter_.adapt(ctx, obj));
            personalListPlanModification.setParameters(parameters);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to create Personal List Plan";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return personalListPlanModification;
    }


    /**
     * {@inheritDoc}
     */
    public com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroup getClosedUserGroup(
            CRMRequestHeader header, long cugID, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "getClosedUserGroup",
                Constants.PERMISSION_CALLINGGROUPS_READ_GETCLOSEDUSERGROUP);
        com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroup closedUserGroup  = null;
        
        GenericParameterParser parser = null;
        if (parameters!=null)
        {
            parser = new GenericParameterParser(parameters);
        }
        try
        {
            ClosedUserGroup group = getCrmClosedUserGroup(ctx, cugID);
            closedUserGroup = (com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroup) closedUserGroupAdapter_
                    .adapt(ctx, group);


            String mobileNo = "";
            if(parser!=null && parser.containsParam(PARAM_MOBILE_NUMBER_NAME))
        	{      


        		mobileNo = parser.getParameter(PARAM_MOBILE_NUMBER_NAME, String.class);
            }

        	int spid = 0 ;

        	
        	if(parser!=null && parser.containsParam(PARAM_SPID_NAME))
            {
        		spid = parser.getParameter(PARAM_SPID_NAME, Integer.class);
            }
            long id = group.getAuxiliaryService(ctx).getID();
            				
            if (mobileNo != null && mobileNo.trim().length()>0)
            {

            	closedUserGroup = AddBundlesToGenericResponse(ctx, closedUserGroup, mobileNo, id, false);				
            } 
            else
            {
            	closedUserGroup = AddBundlesToGenericResponse(ctx, closedUserGroup, null, id, true);
            }
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Closed User Group";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return closedUserGroup;
    }


    /**
     * {@inheritDoc}
     */
    public ClosedUserGroupTemplate getClosedUserGroupTemplate(CRMRequestHeader header, long cugTemplateID, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "getClosedUserGroupTemplate",
                Constants.PERMISSION_CALLINGGROUPS_READ_GETCLOSEDUSERGROUPTEMPLATE);
        ClosedUserGroupTemplate closedUserGroupTemplate = null;
        try
        {
            com.redknee.app.crm.bean.ClosedUserGroupTemplate group = getCrmClosedUserGroupTemplate(ctx, cugTemplateID);
            closedUserGroupTemplate = (ClosedUserGroupTemplate) closedUserGroupTemplateAdapter_.adapt(ctx, group);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Closed User Group Template";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return closedUserGroupTemplate;
    }


    /**
     * {@inheritDoc}
     */
    public PersonalListPlan getPersonalListPlan(CRMRequestHeader header, long plpID, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "getPersonalListPlan",
                Constants.PERMISSION_CALLINGGROUPS_READ_GETPERSONALLISTPLAN);
        PersonalListPlan personalListPlan  = null;
        try
        {
            com.redknee.app.crm.bean.PersonalListPlan plp =  PersonalListPlanSupport.getPLPByID(ctx, plpID);
            personalListPlan = (PersonalListPlan) personalListPlanAdapter_.adapt(ctx, plp);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve PersonalList Plan";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return personalListPlan;
    }


    /**
     * {@inheritDoc}
     */
    public ClosedUserGroupTemplateReference[] listClosedUserGroupTemplates(CRMRequestHeader header, int spid,
            Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listClosedUserGroupTemplates",
                Constants.PERMISSION_CALLINGGROUPS_READ_LISTCLOSEDUSERGROUPTEMPLATES);
        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
        ClosedUserGroupTemplateReference[] closedUserGroupTemplateReferences  = new ClosedUserGroupTemplateReference[] {};
        try
        {
            // Homesupport helper getbeans method is returning empty list                       
            final Collection collection = getCugTemplateCollection(ctx, spid, isAscending);
            closedUserGroupTemplateReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.closedUserGroupTemplateReferenceAdapter_, 
                    closedUserGroupTemplateReferences);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Closed User Group Template Reference";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return closedUserGroupTemplateReferences;
    }


    /**
     * {@inheritDoc}
     */
    public ClosedUserGroupTemplate[] listDetailedClosedUserGroupTemplates(CRMRequestHeader header, int spid,
            Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listDetailedClosedUserGroupTemplates",
                Constants.PERMISSION_CALLINGGROUPS_READ_LISTDETAILEDCLOSEDUSERGROUPTEMPLATES);
        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
        ClosedUserGroupTemplate[] closedUserGroupTemplates  = new ClosedUserGroupTemplate[] {};
        try
        {
            // Homesupport helper getbeans method is returning empty list                  
            final Collection collection = getCugTemplateCollection(ctx, spid, isAscending);
            closedUserGroupTemplates = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.closedUserGroupTemplateAdapter_, 
                    closedUserGroupTemplates);        
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Closed User Group Templates";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return closedUserGroupTemplates;
    }
    
   
    private Collection getCugTemplateCollection(Context ctx, int spid, Boolean isAscending)
            throws HomeInternalException, UnsupportedOperationException, HomeException, CRMExceptionFault
    {
        Home home = RmiApiSupport.getCrmHome(ctx, ClosedUserGroupTemplateHome.class, this);
        final Object condition = new EQ(ClosedUserGroupTemplateXInfo.SPID, spid);
        Collection collection = home.select(condition);
        if (RmiApiSupport.isSortAscending(isAscending))
        {
            Set<com.redknee.app.crm.bean.ClosedUserGroupTemplate> cugTemplateSet = new TreeSet<com.redknee.app.crm.bean.ClosedUserGroupTemplate>(
                    new Comparator<com.redknee.app.crm.bean.ClosedUserGroupTemplate>()
                    {

                        @Override
                        public int compare(com.redknee.app.crm.bean.ClosedUserGroupTemplate o1,
                                com.redknee.app.crm.bean.ClosedUserGroupTemplate o2)
                        {
                            if (o1.getID() < o2.getID())
                            {
                                return -1;
                            }
                            else if (o1.getID() < o2.getID())
                            {
                                return 1;
                            }
                            return 0;
                        }
                    });
            cugTemplateSet.addAll(collection);
        }
        return collection;
    }

    
    private Collection getPersonalListPlanCollection(Context ctx, int spid, Boolean isAscending)
            throws HomeInternalException, UnsupportedOperationException, HomeException, CRMExceptionFault
    {
        Home home = RmiApiSupport.getCrmHome(ctx, PersonalListPlanHome.class, this);
        final Object condition = new EQ(PersonalListPlanXInfo.SPID, spid);
        Collection collection = home.select(condition);
        List<com.redknee.app.crm.bean.PersonalListPlan> list = new ArrayList<com.redknee.app.crm.bean.PersonalListPlan>(
                collection);
        if (RmiApiSupport.isSortAscending(isAscending))
        {
            Set<com.redknee.app.crm.bean.PersonalListPlan> cugTemplateSet = new TreeSet<com.redknee.app.crm.bean.PersonalListPlan>(
                    new Comparator<com.redknee.app.crm.bean.PersonalListPlan>()
                    {

                        @Override
                        public int compare(com.redknee.app.crm.bean.PersonalListPlan o1,
                                com.redknee.app.crm.bean.PersonalListPlan o2)
                        {
                            if (o1.getID() < o2.getID())
                            {
                                return -1;
                            }
                            else if (o1.getID() > o2.getID())
                            {
                                return 1;
                            }
                            return 0;
                        }
                    });
            cugTemplateSet.addAll(collection);
            collection.clear();
            collection.addAll(cugTemplateSet);
        }
        else
        {
            Set<com.redknee.app.crm.bean.PersonalListPlan> cugTemplateSet = new TreeSet<com.redknee.app.crm.bean.PersonalListPlan>(
                    new Comparator<com.redknee.app.crm.bean.PersonalListPlan>()
                    {

                        @Override
                        public int compare(com.redknee.app.crm.bean.PersonalListPlan o1,
                                com.redknee.app.crm.bean.PersonalListPlan o2)
                        {
                            if (o1.getID() < o2.getID())
                            {
                                return 1;
                            }
                            else if (o1.getID() > o2.getID())
                            {
                                return -1;
                            }
                            return 0;
                        }
                    });
            cugTemplateSet.addAll(collection);            
            collection.clear();
            collection.addAll(cugTemplateSet);
        }
        return collection;
    }
    

    /**
     * {@inheritDoc}
     */
    public com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroup[] listDetailedClosedUserGroups(
            CRMRequestHeader header, int spid, Boolean isAscending, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listDetailedClosedUserGroups",
                Constants.PERMISSION_CALLINGGROUPS_READ_LISTDETAILEDCLOSEDUSERGROUPS);
        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
        com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroup[] closedUserGroups = new com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroup[]
            {};
        try
        {
            final Object condition = new EQ(ClosedUserGroupXInfo.SPID, spid);
            final Collection<ClosedUserGroup> collection = HomeSupportHelper.get(ctx).getBeans(ctx,
                    ClosedUserGroup.class, condition, RmiApiSupport.isSortAscending(isAscending));
            closedUserGroups = CollectionSupportHelper.get(ctx).adaptCollection(ctx, collection,
                    this.closedUserGroupAdapter_, closedUserGroups);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Closed User Groups";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return closedUserGroups;
    }


    /**
     * {@inheritDoc}
     */
    public PersonalListPlan[] listDetailedPersonalListPlans(CRMRequestHeader header, int spid, Boolean isAscending,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listDetailedPersonalListPlans",
                Constants.PERMISSION_CALLINGGROUPS_READ_LISTDETAILEDPERSONALLISTPLANS);
        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
        PersonalListPlan[] personalListPlans = new PersonalListPlan[]
            {};
        try
        {
            // Homesupport helper getbeans method is returning empty list
            final Collection collection = getPersonalListPlanCollection(ctx, spid, isAscending);
            personalListPlans = CollectionSupportHelper.get(ctx).adaptCollection(ctx, collection,
                    this.personalListPlanAdapter_, personalListPlans);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Detailed Personal List Plans";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return personalListPlans;
    }


    /**
     * {@inheritDoc}
     */
    public PersonalListPlanReference[] listPersonalListPlans(CRMRequestHeader header, int spid,
            Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listPersonalListPlans",
                Constants.PERMISSION_CALLINGGROUPS_READ_LISTPERSONALLISTPLANS);
        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
        PersonalListPlanReference[] personalListPlanReferences  = new PersonalListPlanReference[] {};
        try
        {
            // Homesupport helper getbeans method is returning empty list                  
            final Collection collection = getPersonalListPlanCollection(ctx, spid, isAscending);
            personalListPlanReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.personalListPlanReferenceAdapter_, 
                    personalListPlanReferences);        
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Personal List Plans";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return personalListPlanReferences;
    }


    /**
     * {@inheritDoc}
     */
    public ClosedUserGroupModificationResult updateClosedUserGroup(CRMRequestHeader header,
            com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroup cug, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateClosedUserGroup",
                Constants.PERMISSION_CALLINGGROUPS_WRITE_UPDATECLOSEDUSERGROUP);
        RmiApiErrorHandlingSupport.validateMandatoryObject(cug, "cug");
        RmiApiErrorHandlingSupport.validateMandatoryObject(cug.getCugState(), "cug.cugState");
        if (cug.getSpid() != null)
        {
            RmiApiSupport.validateExistanceOfBeanForKey(ctx, CRMSpid.class, new EQ(CRMSpidXInfo.ID, cug.getSpid()));
        }       
        ClosedUserGroupModificationResult closedUserGroupModification  = new ClosedUserGroupModificationResult();
        try
        {
            Home home = getClosedUserGroupHome(ctx);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            ClosedUserGroup findBean = getCrmClosedUserGroup(ctx, cug.getIdentifier());
            if (findBean == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("cug",
                        "Closed User Group does not exist " + cug.getIdentifier() + " .");
            }            
            ClosedUserGroup originalBean = (ClosedUserGroup) findBean.clone();            
            ClosedUserGroup crmClosedUserGroup = (ClosedUserGroup) ClosedUserGroupToApiAdapter.adaptApiToClosedUserGroup(ctx, cug, findBean);
            if (!originalBean.getOwnerMSISDN().equals(crmClosedUserGroup.getOwnerMSISDN()))
            {
                RmiApiErrorHandlingSupport.simpleValidation("cug",
                        "OwnerMsisdn update is not Allowed for CUG Template " + cug.getIdentifier()
                                + " to OwnerMsisdn : " + crmClosedUserGroup.getOwnerMSISDN());
            }
            if(originalBean.getCugTemplateID() != crmClosedUserGroup.getCugTemplateID())
            {
                And and = new And();
                and.add(new EQ(ClosedUserGroupTemplateXInfo.ID, crmClosedUserGroup.getCugTemplateID()));
                and.add(new EQ(ClosedUserGroupTemplateXInfo.SPID, crmClosedUserGroup.getSpid()));
                com.redknee.app.crm.bean.ClosedUserGroupTemplate template = HomeSupportHelper.get(ctx).findBean(ctx,
                        com.redknee.app.crm.bean.ClosedUserGroupTemplate.class, and);
                if (template == null)
                {
                    RmiApiErrorHandlingSupport.identificationException(ctx, "Template does not exist : "
                            + crmClosedUserGroup.getCugTemplateID() + " spid : " + crmClosedUserGroup.getSpid(), this);
                }
                if (template.isDeprecated())
                {
                    RmiApiErrorHandlingSupport.simpleValidation("cug", "Cug Template is deprecated : "
                            + crmClosedUserGroup.getCugTemplateID());
                }
            }
            Object obj = home.store(crmClosedUserGroup);
            closedUserGroupModification.setCug((com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroup) closedUserGroupAdapter_.adapt(ctx, obj));
            closedUserGroupModification.setParameters(parameters);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update Closed User Group";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return closedUserGroupModification;
    }


    /**
     * {@inheritDoc}
     */
    public ClosedUserGroupTemplateModificationResult updateClosedUserGroupTemplate(CRMRequestHeader header,
            ClosedUserGroupTemplate cugTemplate, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateClosedUserGroupTemplate",
                Constants.PERMISSION_CALLINGGROUPS_WRITE_UPDATECLOSEDUSERGROUPTEMPLATE);
        RmiApiErrorHandlingSupport.validateMandatoryObject(cugTemplate, "cugTemplate");
        ClosedUserGroupTemplateModificationResult closedUserGroupTemplateModification  = new ClosedUserGroupTemplateModificationResult();
        try
        {
            Home home = RmiApiSupport.getCrmHome(ctx, ClosedUserGroupTemplateHome.class, this);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            
            com.redknee.app.crm.bean.ClosedUserGroupTemplate findBean = getCrmClosedUserGroupTemplate(ctx,
                    cugTemplate.getIdentifier());
            if (findBean == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("cug", "Closed User Group Template does not exist "
                        + cugTemplate.getIdentifier() + " .");
            }
            if (cugTemplate.getSpid() != null && findBean.getSpid() != cugTemplate.getSpid().intValue())
            {
                RmiApiErrorHandlingSupport.simpleValidation("cug", "Spid update is not Allowed for CUG Template "
                        + cugTemplate.getIdentifier() + " to Spid : " + cugTemplate.getSpid());
            }                       
            com.redknee.app.crm.bean.ClosedUserGroupTemplate originalBean = (com.redknee.app.crm.bean.ClosedUserGroupTemplate) findBean.clone();
            com.redknee.app.crm.bean.ClosedUserGroupTemplate crmClosedUserGroup = (com.redknee.app.crm.bean.ClosedUserGroupTemplate) ClosedUserGroupTemplateToApiAdapter
                    .adaptApiToClosedUserGroupTemplate(ctx, cugTemplate, findBean);
            if (crmClosedUserGroup.getCugType().getIndex() != originalBean.getCugType().getIndex())
            {
                RmiApiErrorHandlingSupport.simpleValidation("cug", "CUG Type update is not Allowed for CUG Template "
                        + cugTemplate.getIdentifier() + " to Type : " + cugTemplate.getCugType());
            }
            Object obj = home.store(crmClosedUserGroup);
            closedUserGroupTemplateModification.setCugTemplate((ClosedUserGroupTemplate) closedUserGroupTemplateAdapter_.adapt(ctx, obj));
            closedUserGroupTemplateModification.setParameters(parameters);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update Closed User Group Template";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return closedUserGroupTemplateModification;
    }


    /**
     * {@inheritDoc}
     */
    public PersonalListPlanModificationResult updatePersonalListPlan(CRMRequestHeader header, PersonalListPlan plp,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updatePersonalListPlan",
                Constants.PERMISSION_CALLINGGROUPS_WRITE_UPDATEPERSONALLISTPLAN);
        RmiApiErrorHandlingSupport.validateMandatoryObject(plp, "plp");
        RmiApiErrorHandlingSupport.validateMandatoryObject(plp.getPlpServiceType(), "plp.plpServiceType");
        PersonalListPlanModificationResult personalListPlanModification  = new PersonalListPlanModificationResult();
        try
        {
            Home home = RmiApiSupport.getCrmHome(ctx, PersonalListPlanHome.class, this);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            com.redknee.app.crm.bean.PersonalListPlan findBean = HomeSupportHelper.get(ctx).findBean(ctx,
                    com.redknee.app.crm.bean.PersonalListPlan.class, plp.getIdentifier());
            if (findBean == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("plp",
                        "Personal List Plan does not exist " + plp.getIdentifier() + " .");
            }
            if (plp.getSpid()!= null && findBean.getSpid() != plp.getSpid().intValue())
            {
                RmiApiErrorHandlingSupport.simpleValidation("plp",
                        "Spid update is not Allowed for plp " + plp.getIdentifier() + " to Spid : " + plp.getSpid());
            }            
            com.redknee.app.crm.bean.PersonalListPlan originalBean = (com.redknee.app.crm.bean.PersonalListPlan) findBean.clone();
            com.redknee.app.crm.bean.PersonalListPlan crmPlp = (com.redknee.app.crm.bean.PersonalListPlan) PersonalListPlanToApiAdapter
                    .adaptApiToPersonalListPlan(ctx, plp, findBean);
            if (crmPlp.getMaxSubscriberCount() != originalBean.getMaxSubscriberCount())
            {
                RmiApiErrorHandlingSupport.simpleValidation("plp", "MaxSubscriberCount update is not Allowed for plp "
                        + plp.getIdentifier() + " to MaxSubscriberCount : " + plp.getMaxSubscriberCount());
            }
            Object obj = home.store(crmPlp);
            personalListPlanModification.setPlp((PersonalListPlan) personalListPlanAdapter_.adapt(ctx, obj));
            personalListPlanModification.setParameters(parameters);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update Personal List Plan";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return personalListPlanModification;
    }

    /**
     * Returns the closed user group home.
     *
     * @param ctx
     *            The operating context.
     * @return Closed user group home.
     * @throws CRMException
     *             Thrown if the home is not found.
     */
    private Home getClosedUserGroupHome(final Context ctx) throws com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault
    {
        return RmiApiSupport.getCrmHome(ctx, ClosedUserGroupHome.class, this);
    }

    private com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroup AddBundlesToGenericResponse(Context ctx, com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroup closeUserGroup, String mobileNo,
			long auxServiceId, boolean allAssociatedBundles) 
	{	
    	StringBuilder auxiliaryBundles = new StringBuilder() ;
		try 
		{
			Home home = (Home) ctx.get(AuxiliaryServiceHome.class);
			Home bundleAuxServiceHome = (Home) ctx
					.get(BundleAuxiliaryServiceHome.class);
			String subID = null;
			if (mobileNo != null && mobileNo.trim() != "")
			{
				subID = SubscriberSupport.lookupSubscriberForMSISDN(ctx,
					mobileNo).getId();
			}			
			com.redknee.app.crm.bean.AuxiliaryService auxService = (AuxiliaryService) home.find(
					ctx,
					new EQ(AuxiliaryServiceXInfo.IDENTIFIER, Long
							.valueOf(auxServiceId)));
			String strBundleIDs = null;
			String auxPrepaidBundleIds = auxService.getPrepaidBundles(); 
			String auxPostpaidBundleIds = auxService.getPostpaidBundles();
			if(auxPrepaidBundleIds != null && !auxPrepaidBundleIds.trim().isEmpty())				
			{
				strBundleIDs = auxPrepaidBundleIds;
			}
			if(auxPostpaidBundleIds != null && !auxPostpaidBundleIds.trim().isEmpty())
			{
				if (strBundleIDs!= null && strBundleIDs.length()>0)
				{
					strBundleIDs += "," + auxPostpaidBundleIds;
				}
				else
				{
					strBundleIDs = auxPostpaidBundleIds;
				}
			}			
			String[] bundleIDs = null;
			if(strBundleIDs != null && !strBundleIDs.trim().isEmpty())
			{				
				bundleIDs = strBundleIDs.split(",");
				int bundleId = 0;
				And and = null;
				BundleAuxiliaryService bean = null;
				BundleProfile bundle = null;
				for (int i = 0; i < bundleIDs.length; i++) 
				{
					bundleId = Integer.parseInt(bundleIDs[i]);
					if ((!allAssociatedBundles) && subID != null) 
					{
						and = new And();
						and.add(new EQ(BundleAuxiliaryServiceXInfo.SUBSCRIBER_ID,
								subID));
						and.add(new EQ(BundleAuxiliaryServiceXInfo.ID, bundleId));
						bean = (BundleAuxiliaryService) bundleAuxServiceHome.find(
								ctx, and);
					}
					try
					{
						bundle = BundleSupportHelper.get(ctx).getBundleProfile(ctx,
								bundleId);
					} 
					catch (InvalidBundleApiException e)
					{
						// TODO: Auto-generated catch block!
					}				
					String bundleName, bundleType; 
					String auxBundleInfo = null;
					if (allAssociatedBundles) 
					{
						// Add data to Generic response Here -BundleName and ID	
						bundleName = bundle.getName();
						bundleType = bundle.getSegment().toString();
						auxBundleInfo = bundleName + ","+ bundleId + "," + bundleType;
					}
					else if (bean != null && bundle.isAuxiliary()) 
					{
						// Add data to Generic response Here -BundleType , Name and ID
	
						bundleName = bundle.getName();
						bundleType = bundle.getSegment().toString();
						auxBundleInfo = bundleName + ","+ bundleId + "," + bundleType;
					}
					if(auxBundleInfo != null)
					{
						if(auxiliaryBundles.length()>0)
						{
							auxiliaryBundles.append("|"+auxBundleInfo);
						}
						else 
						{
							auxiliaryBundles.append(auxBundleInfo);
						}
					}
				}//End of for loop 
				closeUserGroup.addParameters(RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_AUXILIARYBUNDLES, auxiliaryBundles.toString()));
			}
		} catch (HomeInternalException e) {
			// TODO: Auto-generated catch block!
		} catch (HomeException e) {
			// TODO: Auto-generated catch block!
		}
		return closeUserGroup;
	}
 
    protected <Y extends Object> Y getParameter(Context ctx, int paramIdentifier, String paramName, Class<Y> resultClass, Object... parameters) throws CRMExceptionFault
    {
        Y result = null;
        if (isGenericExecution(ctx, parameters))
        {
            GenericParametersAdapter<Y> adapter = new GenericParametersAdapter<Y>(resultClass, paramName);
            try
            {
                result = (Y) adapter.unAdapt(ctx, parameters);
            }
            catch (HomeException e)
            {
                RmiApiErrorHandlingSupport.generalException(ctx, e,
                        "Unable to extract argument '" + paramName + "' from generic parameters: " + e.getMessage(), this);
            }
        }
        else
        {
            result = (Y) parameters[paramIdentifier];
        }
        return result;
    }
    
    public boolean isGenericExecution(Context ctx, Object... parameters)
    {
        return (parameters.length == 2  && parameters[1] instanceof GenericParameter[]);
    }
    

    /**
     * {@inheritDoc}
     */
    public Context getContext()
    {
        return this.context_;
    }


    /**
     * {@inheritDoc}
     */
    public void setContext(final Context ctx)
    {
        this.context_ = ctx;
    }

    /**
     * The operating context.
     */
    private Context context_;

    /**
     * CRM closed user group to API closed user group adapter.
     */
    private final ClosedUserGroupToApiReferenceAdapter closedUserGroupReferenceAdapter_;
    
    private final ClosedUserGroupTemplateToApiReferenceAdapter closedUserGroupTemplateReferenceAdapter_;
    
    private final ClosedUserGroupToApiAdapter closedUserGroupAdapter_;
    
    private final ClosedUserGroupTemplateToApiAdapter closedUserGroupTemplateAdapter_;
    
    private final PersonalListPlanToApiAdapter personalListPlanAdapter_;
    
    private final PersonalListPlanToApiReferenceAdapter personalListPlanReferenceAdapter_;
    
}
