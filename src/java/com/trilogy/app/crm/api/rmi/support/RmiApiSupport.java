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
package com.trilogy.app.crm.api.rmi.support;


import java.security.Principal;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;

import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.auth.bean.GroupHome;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.auth.bean.UserHome;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.context.ThreadLocalContextFactory;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.format.LogMsgFormat;

import com.trilogy.app.crm.api.ApiSupport;
import com.trilogy.app.crm.api.Constants;
import com.trilogy.app.crm.api.rmi.log.ApiLogMsgFormat;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.BillingMessagePreferenceEnum;
import com.trilogy.app.crm.bean.BillingOptionEnum;
import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.NotificationMethodEnum;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.PricePlanStateEnum;
import com.trilogy.app.crm.bean.QuotaTypeEnum;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.bundle.DurationTypeEnum;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.DefaultConfigChangeRequestSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.CRMException;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.NotificationPreferenceType;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PaidType;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PaidTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PoolLimitStrategy;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SystemType;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SystemTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_0.types.TechnologyType;
import com.trilogy.util.crmapi.wsdl.v2_1.exception.ExceptionCode;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.AccountState;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.BillingMessagePreference;
import com.trilogy.util.crmapi.wsdl.v2_1.types.cardpackage.CardPackage;
import com.trilogy.util.crmapi.wsdl.v2_1.types.cardpackage.CardPackageState;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.BillingOptionType;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionState;
import com.trilogy.util.crmapi.wsdl.v2_2.types.serviceandbundle.RecurrenceTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.GroupHierarchyType;
import com.trilogy.util.crmapi.wsdl.v3_0.types.mobilenumber.MobileNumberState;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.RecurrenceScheme;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServiceBalanceLimit;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServicePeriod;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServicePeriodEnum;


/**
 * For common methods used in RMI API.
 *
 * @author victor.stratan@redknee.com
 */
public class RmiApiSupport
{

    /**
     * Context key for SPID in API.
     */
    public static final String API_SPID = "API_SPID";

    /**
     * Creates a new <code>RmiApiSupport</code> instance. This method is made protected to
     * prevent instantiation of utility class.
     */
    protected RmiApiSupport()
    {
        // empty
    }


    /**
     * Authenticates the user.
     *
     * @param ctx
     *            The operating context.
     * @param header
     *            Header of the API request. Contains user and password to be
     *            authenticated.
     * @param caller
     *            Name of the calling method.
     * @param permissions
     *            Possible permissions required by the calling method. If no permission
     *            is required, pass in <code>null</code> or empty array. This is treated
     *            as an OR, not an AND so that we can continue to support deprecated 
     *            permissions for methods that are renamed or moved to a different module.
     * @return Returns whether the user is successfully authenticated.
     * @throws CRMException
     *             Thrown if the user fails authentication, or does not have the required
     *             permission.
     */
    public static boolean authenticateUser(final Context ctx, final CRMRequestHeader header, final String caller,
        final String... permissions) throws CRMExceptionFault
    {
        ContextLocator.setThreadContext(ctx);
        RmiApiErrorHandlingSupport.validateMandatoryObject(header, "header");
        RmiApiErrorHandlingSupport.validateMandatoryObject(header.getUsername(), "header.username");
        RmiApiErrorHandlingSupport.validateMandatoryObject(header.getPassword(), "header.password");
        
        final boolean login = ApiSupport.authenticateUser(ctx, header.getUsername(), header.getPassword());
        if (!login)
        {
            final String msg = "User cannot be Authenticated";
            RmiApiErrorHandlingSupport.generalException(ctx, null, msg, ExceptionCode.AUTHENTICATION_EXCEPTION, caller);
        }

        boolean authorized = ApiSupport.authorizeUser(ctx, new SimplePermission("api.rmi"));
        ctx.put(DefaultConfigChangeRequestSupport.API_SOURCE_USERNAME, header.getUsername());
        if (!authorized)
        {
            final String msg = "User is not Authorized to Call API";
            RmiApiErrorHandlingSupport.generalException(ctx, null, msg, ExceptionCode.AUTHORIZATION_EXCEPTION, caller);
        }

        if (header.getAgentID() != null && (!header.getAgentID().isEmpty()))
        {
            authorized = ApiSupport.authorizeUser(ctx, new SimplePermission("api.agent." + header.getAgentID()));
            if (!authorized)
            {
                final String msg = "User is not Authorized to act as user [" + header.getAgentID() + "]";
                RmiApiErrorHandlingSupport.generalException(ctx, null, msg, ExceptionCode.AUTHORIZATION_EXCEPTION,
                        caller);
            }
            setAgentAsPrincipal(ctx, header, caller);
        }
        
        /*
         * [Cindy] 2008-02-15: Per-method permission support.
         */
        if (permissions != null && permissions.length > 0)
        {
            for (String permission : permissions)
            {
                authorized = ApiSupport.authorizeUser(ctx, new SimplePermission(permission));
                if (authorized)
                {
                    break;
                }
            }
            if (!authorized)
            {
                final StringBuilder sb = new StringBuilder();
                sb.append("User does not have Sufficient Permission to Call API Method");
                if (caller != null)
                {
                    sb.append(' ');
                    sb.append(caller);
                }
                RmiApiErrorHandlingSupport.generalException(ctx, null, sb.toString(),
                        ExceptionCode.AUTHORIZATION_EXCEPTION, caller);
            }
        }
        ctx.put(CRMRequestHeader.class, header);
        ctx.put(Constants.METHOD_NAME_CTX_KEY, caller);
        ctx.put(LogMsgFormat.class, new ThreadLocalContextFactory(ctx, new ContextFactory()
        {

            @Override
            public Object create(Context ctx1)
            {
                return new ApiLogMsgFormat(ctx);
            }
        }));
        return authorized;
    }


    private static void setAgentAsPrincipal(Context ctx, final CRMRequestHeader header, final String caller)
            throws CRMExceptionFault
    {
        try
        {
            Home userHome = (Home) ctx.get(UserHome.class);
            User user = (User) userHome.find(header.getAgentID());
            if (user == null)
            {
                String errorMsg = "Invalid agentId [" + header + "]";
                RmiApiErrorHandlingSupport.generalException(ctx, null, errorMsg, ExceptionCode.AUTHORIZATION_EXCEPTION,
                        caller);
            }
            if (!user.getActivated())
            {
                String errorMsg = "Your account [" + header.getAgentID() + "] has been deactivated.";
                RmiApiErrorHandlingSupport.generalException(ctx, null, errorMsg, ExceptionCode.AUTHORIZATION_EXCEPTION,
                        caller);
            }
            Home groupHome = (Home) ctx.get(GroupHome.class);
            CRMGroup group = (CRMGroup) groupHome.find(user.getGroup());
            if (group == null || !group.isEnabled())
            {
                String errorMsg = "Your account [" + header.getAgentID() + "] group has been deactivated.";
                RmiApiErrorHandlingSupport.generalException(ctx, null, errorMsg, ExceptionCode.AUTHORIZATION_EXCEPTION,
                        caller);
            }
            if (user.getStartDate().after(new Date())
                    || (user.getEndDate() != null && user.getEndDate().before(new Date())))
            {
                String errorMsg = "Your account [" + header.getAgentID() + "]is not active at this time.";
                RmiApiErrorHandlingSupport.generalException(ctx, null, errorMsg, ExceptionCode.AUTHORIZATION_EXCEPTION,
                        caller);
            }
            ctx.put(Principal.class, user);
            ctx.put(com.redknee.framework.xhome.auth.bean.Group.class, group);
        }
        catch (HomeException homeEx)
        {
            RmiApiErrorHandlingSupport.generalException(ctx, homeEx, "Unable to set user",
                    ExceptionCode.AUTHORIZATION_EXCEPTION, caller);
        }
    }
    
    public static User retrieveUser(final Context ctx, final CRMRequestHeader header, final String caller)
        throws CRMExceptionFault
    {
        try
        {
            final Home userHome = (Home) ctx.get(UserHome.class);
            final User user = (User) userHome.find(ctx, header.getUsername());

            return user;
        }
        catch (final Exception e)
        {
            final String msg = "Fail to retrieve user " + header.getUsername();
            RmiApiErrorHandlingSupport.generalException(ctx, e, msg, ExceptionCode.GENERAL_EXCEPTION, caller);
        }
        return null;
    }

    
    public static int getDefaultSubscriptionType(final Context ctx, final Integer spidObj, final Object caller)
            throws CRMExceptionFault, HomeException
    {
        final int spid;
        if (spidObj != null)
        {
            spid = spidObj;
        }
        else
        {
            User user = (User) ctx.get(Principal.class);
            spid = user.getSpid();
        }
        return getSubscriptionType(ctx, spid, caller);
    }

    
    public static int getSubscriptionType(final Context ctx, final int spid, final Object caller)
            throws CRMExceptionFault, HomeException
    {
        final CRMSpid crmSpid = getCrmServiceProvider(ctx, spid, caller);
        
        int subscriptionType = Long.valueOf(crmSpid.getDefaultSubscriptionType()).intValue();
        if (subscriptionType < 0)
        {
            subscriptionType = Long.valueOf(SubscriptionType.getINSubscriptionType(ctx).getId()).intValue();
        }
        
        return subscriptionType;
    }
    
    
    public static boolean isSortAscending(Boolean isAscending)
    {
        return isAscending == null || isAscending;
    }

    /**
     * Header must be authenticated. This method does not do authentication. Does not
     * return null. If account not found Exception is thrown.
     * 
     * @param ctx
     *            the operating context
     * @param billCycleID
     *            id of the bill cycle to retrieve
     * @return BillCycle object
     * @throws CRMExceptionFault
     *             any exception is caught and wrapped in a CRMException
     */
    public static com.redknee.app.crm.bean.BillCycle getCrmBillCycle(final Context ctx, final long billCycleID, final Object caller)
            throws com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault
    {
        com.redknee.app.crm.bean.BillCycle billCycle = null;
        try
        {
            billCycle = HomeSupportHelper.get(ctx).findBean(ctx, com.redknee.app.crm.bean.BillCycle.class,
                    Integer.valueOf((int) billCycleID));
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve BillCycle " + billCycleID;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, caller);
        }
        if (billCycle == null)
        {
            final String identifier = "BillCycle " + billCycleID;
            RmiApiErrorHandlingSupport.identificationException(ctx, identifier, caller);
        }
        return billCycle;
    }

    public static CRMSpid getCrmServiceProvider(final Context ctx, final int spid,
            final Object caller) throws CRMExceptionFault
    {
        CRMSpid sp = null;
        try
        {
            sp = SpidSupport.getCRMSpid(ctx, spid);
        }
        catch (Exception e)
        {
            final String msg = "Unable to retrieve Service Provider " + spid;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, caller);
        }

        if (sp == null)
        {
            final String msg = "Invalid Service Provider " + spid;
            RmiApiErrorHandlingSupport.generalException(ctx, null, msg, ExceptionCode.INVALID_SPID, caller);
        }

        return sp;
    }    

    public static SubscriberTypeEnum convertApiPaidType2CrmSubscriberType(final PaidType paidType)
        throws CRMExceptionFault
    {   
        if (paidType == null)
        {
            RmiApiErrorHandlingSupport.simpleValidation("paidType", "Unsupported " + PaidType.class.getName() + " "
                    + paidType);
        }

        SubscriberTypeEnum result = null;
        
        long paidTypeValue = paidType.getValue();
        if (paidTypeValue == PaidTypeEnum.POSTPAID.getValue().getValue())
        {
            result = SubscriberTypeEnum.POSTPAID;
        }
        else if (paidTypeValue == PaidTypeEnum.PREPAID.getValue().getValue())
        {
            result = SubscriberTypeEnum.PREPAID;
        }
        else if (paidTypeValue == PaidTypeEnum.UNSPECIFIED.getValue().getValue())
        {
            result = SubscriberTypeEnum.HYBRID;
        }
        else
        {
            RmiApiErrorHandlingSupport.simpleValidation("paidType", "Unsupported " + PaidType.class.getName() + " "
                    + paidType);
        }

        return result;
    }


    public static PaidType convertCrmSubscriberPaidType2Api(final SubscriberTypeEnum type)
    {
        final PaidType result;
        if (type == SubscriberTypeEnum.POSTPAID)
        {
            result = PaidTypeEnum.POSTPAID.getValue();
        }
        else if (type == SubscriberTypeEnum.PREPAID)
        {
            result = PaidTypeEnum.PREPAID.getValue();
        }
        else
        {
            result = PaidTypeEnum.UNSPECIFIED.getValue();
        }

        return result;
    }


    public static SystemType convertCrmSubscriberPaidType2ApiSystemType(final SubscriberTypeEnum type)
    {
        final SystemType result;
        if (type == SubscriberTypeEnum.POSTPAID)
        {
            result = SystemTypeEnum.POSTPAID.getValue();
        }
        else if (type == SubscriberTypeEnum.PREPAID)
        {
            result = SystemTypeEnum.PREPAID.getValue();
        }
        else
        {
            result = SystemTypeEnum.CONVERGE.getValue();
        }

        return result;
    }

	public static GroupTypeEnum convertApiGroupHierarchyType2CrmGroupType(
	    final GroupHierarchyType groupHierarchyType) throws CRMExceptionFault
	{
		if (groupHierarchyType == null)
		{
			RmiApiErrorHandlingSupport.simpleValidation("groupHierarchyType",
			    "Unsupported " + GroupHierarchyType.class.getName() + " "
			        + groupHierarchyType);
		}

		GroupTypeEnum result = null;
		long groupTypeValue = groupHierarchyType.getValue();
		if (groupTypeValue == GroupTypeEnum.GROUP_INDEX)
		{
			result = GroupTypeEnum.GROUP;
		}
		else if (groupTypeValue == GroupTypeEnum.GROUP_POOLED_INDEX)
		{
			result = GroupTypeEnum.GROUP_POOLED;
		}
		else if (groupTypeValue == GroupTypeEnum.SUBSCRIBER_INDEX)
		{
			result = GroupTypeEnum.SUBSCRIBER;
		}
		else
		{
			RmiApiErrorHandlingSupport.simpleValidation("groupHierarchyType",
			    "Unsupported " + GroupHierarchyType.class.getName() + " "
			        + groupHierarchyType);
		}

		return result;
	}

    public static SubscriberTypeEnum convertApiSystemType2CrmSystemType(final SystemType systemType) throws CRMExceptionFault
    {
        if (systemType == null)
        {
            RmiApiErrorHandlingSupport.simpleValidation("systemType", "Unsupported " + SystemType.class.getName() + " "
                    + systemType);
        }
        
        SubscriberTypeEnum result = null;
        
        long systemTypeValue = systemType.getValue();
        if (systemTypeValue == SystemTypeEnum.POSTPAID.getValue().getValue())
        {
            result = SubscriberTypeEnum.POSTPAID;
        }
        else if (systemTypeValue == SystemTypeEnum.PREPAID.getValue().getValue())
        {
            result = SubscriberTypeEnum.PREPAID;
        }
        else if (systemTypeValue == SystemTypeEnum.CONVERGE.getValue().getValue())
        {
            result = SubscriberTypeEnum.HYBRID;
        }
        else
        {
            RmiApiErrorHandlingSupport.simpleValidation("systemType", "Unsupported " + SystemType.class.getName() + " "
                    + systemType);
        }

        return result;
    }


    public static TechnologyEnum convertApiTechnology2Crm(final TechnologyType techType) throws CRMExceptionFault
    {
        TechnologyEnum result = null;
        try
        {
            result = TechnologyEnum.get((short) techType.getValue());
        }
        catch (final Exception e)
        {
            RmiApiErrorHandlingSupport.simpleValidation("technology", "Unsupported " + TechnologyType.class.getName()
                    + " " + techType);
        }

        return result;
    }


    public static SubscriberStateEnum convertApiSubscriberState2Crm(final SubscriptionState state) throws CRMExceptionFault
    {
        SubscriberStateEnum result = null;
        try
        {
            result = SubscriberStateEnum.get((short) state.getValue());
        }
        catch (final Exception e)
        {
            RmiApiErrorHandlingSupport.simpleValidation("state", "Unsupported " + SubscriptionState.class.getName()
                    + " " + state);
        }

        return result;
    }
    
    public static SubscriberStateEnum[] convertApiSubscriberState2Crm(final SubscriptionState[] states) throws CRMExceptionFault
    {
        SubscriberStateEnum[] result = null;
        if (states!=null)
        {
            result = new SubscriberStateEnum[states.length];

            for (int i=0; i<states.length; i++)
            {
                result[i] = convertApiSubscriberState2Crm(states[i]);
            }
        }
        return result;
    }



    public static SubscriptionState convertCrmSubscriberState2Api(final SubscriberStateEnum state) throws CRMExceptionFault
    {
        SubscriptionState result = null;
        try
        {
            result = com.redknee.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionStateEnum.valueOf(state.getIndex());;
        }
        catch (final Exception e)
        {
            RmiApiErrorHandlingSupport.simpleValidation("state", "Unsupported " + SubscriptionState.class.getName()
                    + " " + state);
        }

        return result;
    }

    public static MsisdnStateEnum convertApiMsisdnState2Crm(final MobileNumberState stateType) throws CRMExceptionFault
    {
        MsisdnStateEnum result = null;
        try
        {
            result = MsisdnStateEnum.get((short) stateType.getValue());
        }
        catch (final Exception e)
        {
            RmiApiErrorHandlingSupport.simpleValidation("state", "Unsupported " + MobileNumberState.class.getName()
                    + " " + stateType);
        }

        return result;
    }


    public static PackageStateEnum convertApiCardPackageState2Crm(final CardPackageState state) throws CRMExceptionFault
    {
        PackageStateEnum result = null;
        try
        {
            result = PackageStateEnum.get((short) state.getValue());
        }
        catch (final Exception e)
        {
            RmiApiErrorHandlingSupport.simpleValidation("state", "Unsupported " + CardPackageState.class.getName()
                    + " " + state);
        }

        return result;
    }


    public static AccountStateEnum convertApiAccountState2Crm(final AccountState state) throws CRMExceptionFault
    {
        AccountStateEnum result = null;
        try
        {
            result = AccountStateEnum.get((short) state.getValue());
        }
        catch (final Exception e)
        {
            RmiApiErrorHandlingSupport.simpleValidation("state", "Unsupported " + AccountState.class.getName()
                    + " " + state);
        }

        return result;
    }
    
    public static AccountState convertCrmAccountState2Api(final AccountStateEnum state) throws CRMExceptionFault
    {
        AccountState result = null;
        try
        {
            result = com.redknee.util.crmapi.wsdl.v2_1.types.account.AccountStateEnum.valueOf(state.getIndex());
        }
        catch (final Exception e)
        {
            RmiApiErrorHandlingSupport.simpleValidation("state", "Unsupported " + AccountState.class.getName()
                    + " " + state);
        }

        return result;
    }

    public static AccountStateEnum[] convertApiAccountState2Crm(final AccountState[] states) throws CRMExceptionFault
    {
        AccountStateEnum[] result = null;
        if (states!=null)
        {
            result = new AccountStateEnum[states.length];

            for (int i=0; i<states.length; i++)
            {
                result[i] = convertApiAccountState2Crm(states[i]);
            }
        }
        return result;
    }
    
    
    public static GroupTypeEnum[] convertApiGroupHierarchyType2CrmGroupType(final GroupHierarchyType[] groupHierarchyType) throws CRMExceptionFault
    {
        GroupTypeEnum[] result = null;
        if (groupHierarchyType!=null)
        {
            result = new GroupTypeEnum[groupHierarchyType.length];

            for (int i=0; i<groupHierarchyType.length; i++)
            {
                result[i] = convertApiGroupHierarchyType2CrmGroupType(groupHierarchyType[i]);
            }
        }
        return result;
    }
    

    public static BillingMessagePreferenceEnum convertApiBillMsgPref2Crm(final BillingMessagePreference state) throws CRMExceptionFault
    {
        BillingMessagePreferenceEnum result = null;
        try
        {
            result = BillingMessagePreferenceEnum.get((short) state.getValue());
        }
        catch (final Exception e)
        {
            RmiApiErrorHandlingSupport.simpleValidation("MutableAccountBilling.billingMsgPreference", "Unsupported "
                    + BillingMessagePreference.class.getName() + " " + state);
        }

        return result;
    }


    public static BillingOptionEnum convertApiBillingOption2Crm(final BillingOptionType billingOptionType)
        throws CRMExceptionFault
    {
        BillingOptionEnum result = null;
        try
        {
            result = BillingOptionEnum.get((short) billingOptionType.getValue());
        }
        catch (final Exception e)
        {
            RmiApiErrorHandlingSupport.simpleValidation("MutableSubscriberBilling.billingOption", "Unsupported "
                    + BillingOptionType.class.getName() + " " + billingOptionType);
        }

        return result;
    }


    public static QuotaTypeEnum convertApiPoolLimitStrategy2Crm(final PoolLimitStrategy poolLimitStrategy)
        throws CRMExceptionFault
    {
        QuotaTypeEnum result = null;
        try
        {
            result = QuotaTypeEnum.get((short) poolLimitStrategy.getValue());
        }
        catch (final Exception e)
        {
            RmiApiErrorHandlingSupport.simpleValidation("MutableSubscriberProfile.poolLimitStrategy", "Unsupported "
                    + PoolLimitStrategy.class.getName() + " " + poolLimitStrategy);
        }

        return result;
    }


    public static NotificationMethodEnum convertApiNotificationPreference2Crm(final NotificationPreferenceType pref)
        throws CRMExceptionFault
    {
        NotificationMethodEnum result = null;
        try
        {
            result = NotificationMethodEnum.get((short) pref.getValue());
        }
        catch (final Exception e)
        {
            RmiApiErrorHandlingSupport.simpleValidation("MutableSubscriberProfile.notificationPreference", "Unsupported "
                    + NotificationPreferenceType.class.getName() + " " + pref);
        }

        return result;
    }
    
    
    public static long convertCrmPricePlanState2Api(final PricePlanStateEnum crmPricePlanStateEnum)
    {
    	short crmPricePlanStaeIndex = crmPricePlanStateEnum.getIndex();
    	switch(crmPricePlanStaeIndex)
    	{
    		case PricePlanStateEnum.ACTIVE_INDEX:
    			return com.redknee.util.crmapi.wsdl.v3_0.types.PricePlanStateEnum.ACTIVE.getValue().getValue();
    		
    		case PricePlanStateEnum.GRANDFATHERED_INDEX:
    			return com.redknee.util.crmapi.wsdl.v3_0.types.PricePlanStateEnum.GRANDFATHERED.getValue().getValue();
    		
    		case PricePlanStateEnum.PENDING_ACTIAVTION_INDEX:
    			return com.redknee.util.crmapi.wsdl.v3_0.types.PricePlanStateEnum.PENDING_ACTIVATION.getValue().getValue();
    			
    		case PricePlanStateEnum.INACTIVE_INDEX:
    			return com.redknee.util.crmapi.wsdl.v3_0.types.PricePlanStateEnum.INACTIVE.getValue().getValue();
    	}
    	return 0;
    }



    public static Home getCrmHome(final Context ctx, final Object homeKey, final Object caller) throws CRMExceptionFault
    {
        final Home home = (Home) ctx.get(homeKey);
        if (home == null)
        {
            final String msg = "No Home for key " + homeKey + " found in context.";
            RmiApiErrorHandlingSupport.generalException(ctx, null, msg, caller);
        }
        return home;
    }


    public static void setOptional(final Object obj, final PropertyInfo propertyInfo, final Object value)
    {
        if (value instanceof Calendar
                && Date.class.getName().equals(propertyInfo.getType().getName()))
        {
            propertyInfo.set(obj, CalendarSupportHelper.get().calendarToDate((Calendar)value));
        }
        else if (value != null)
        {
            propertyInfo.set(obj, value);
        }
    }
    

    public static  <T extends AbstractBean>  void validateExistanceOfBeanForKey(Context ctx, Class<T> beanType, Object where) throws CRMExceptionFault
    {
        try
        {
            Object result = HomeSupportHelper.get(ctx).findBean(ctx, beanType, where);
            if (result == null)
            {
                final String msg = "Unable to find " + (beanType != null ? beanType.getName() : null) + " for " + where.toString();
                RmiApiErrorHandlingSupport.generalException(ctx, null, msg, RmiApiSupport.class);
            }
        }
        catch (HomeException homeEx)
        {
            final String msg = "Unable to find " + (beanType != null ? beanType.getName() : null) + " for " + where.toString();
            RmiApiErrorHandlingSupport.generalException(ctx, homeEx, msg, RmiApiSupport.class);
        }
        catch (Exception ex)
        {
            final String msg = "Unable to find " + (beanType != null ? beanType.getName() : null) + " for " + where.toString();
            RmiApiErrorHandlingSupport.generalException(ctx, ex, msg, RmiApiSupport.class);
        }
    }


    public static String cardPackageToString(final CardPackage card)
    {
        final String value = " Card Package with dealer=" + card.getDealer() + " serialNumber="
            + card.getSerialNumber() + " imsi=" + card.getImsi() + " min=" + card.getMin() + " esn=" + card.getEsn();
        return value;

    }
    
    public static Object getGenericParameterValue(String name, GenericParameter[] parameters)
    {
        if(parameters != null)
        {
            for(int i = 0; i < parameters.length; i++)
            {
                if(parameters[i].getName().equals(name))
                {
                    return parameters[i].getValue();
                }
            }            
        }        
        return null;
    }
    
    public static GenericParameter createGenericParameter(String name, Object value)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(name);
        parameter.setValue(value);
        return parameter;
    }


    public static ServiceBalanceLimit getApiServiceBalanceLimitFromCrmServiceBalanceLimit(
            com.redknee.app.crm.bundle.ServiceBalanceLimit crmServiceBalLimit)
    {
        ServiceBalanceLimit apiServiceBalanceLimit = new ServiceBalanceLimit();
        apiServiceBalanceLimit.setApplicationID(crmServiceBalLimit.getApplicationId());
        apiServiceBalanceLimit.setInitialBalanceLimit(crmServiceBalLimit.getInitialBalanceLimit());
        return apiServiceBalanceLimit;
    }
    
    public static com.redknee.app.crm.bundle.ServiceBalanceLimit getCrmServiceBalanceLimitFromApiServiceBalanceLimit(
            ServiceBalanceLimit apiServiceBalanceLimit)
    {
        com.redknee.app.crm.bundle.ServiceBalanceLimit crmServiceBalanceLimit = new com.redknee.app.crm.bundle.ServiceBalanceLimit();
        crmServiceBalanceLimit.setApplicationId(apiServiceBalanceLimit.getApplicationID());
        crmServiceBalanceLimit.setInitialBalanceLimit(apiServiceBalanceLimit.getInitialBalanceLimit());
        return crmServiceBalanceLimit;
    }
    
    
    
    public static ServicePeriod convertCrmServicePeriodEnum2ApiServicePeriodType(com.redknee.app.crm.bean.ServicePeriodEnum en)
    {
        switch(en.getIndex())
        {
        case com.redknee.app.crm.bean.ServicePeriodEnum.DAILY_INDEX:
        case com.redknee.app.crm.bean.ServicePeriodEnum.MULTIDAY_INDEX:
            return ServicePeriodEnum.DAILY.getValue();
            
        case com.redknee.app.crm.bean.ServicePeriodEnum.MONTHLY_INDEX:
        case com.redknee.app.crm.bean.ServicePeriodEnum.MULTIMONTHLY_INDEX:
            return ServicePeriodEnum.MONTHLY.getValue();
            
        default: 
            return ServicePeriodEnum.valueOf(en.getIndex());
        }
    }
    
    
    /**
     * This method converts API ServicePeriod To CRM ServicePeriodEnum 
     * 
     * [API ServicePeriod.DAILY & validity > 1]  => CRM ServicePeriodEnum.MULTIDAY
     * [API ServicePeriod.MONTHLY & validity > 1]  => CRM ServicePeriodEnum.MULTIMONTHLY
     * 
     * @param period - API Service Period (Unit Type)
     * @param validity - API Service Period (Duration)
     * @return
     */
    public static com.redknee.app.crm.bean.ServicePeriodEnum convertApiServicePeriodType2CrmServicePeriodEnum(ServicePeriod period, long validity)
    {
        if (ServicePeriodEnum.DAILY.getValue().getValue() == period.getValue())
        {
            return (validity > 1) ? com.redknee.app.crm.bean.ServicePeriodEnum.MULTIDAY : com.redknee.app.crm.bean.ServicePeriodEnum.DAILY;
        }
        else if (ServicePeriodEnum.MONTHLY.getValue().getValue() == period.getValue())
        {
            return (validity > 1) ? com.redknee.app.crm.bean.ServicePeriodEnum.MULTIMONTHLY : com.redknee.app.crm.bean.ServicePeriodEnum.MONTHLY;
        }
        else if (ServicePeriodEnum.WEEKLY.getValue().getValue() == period.getValue())
        {
            return com.redknee.app.crm.bean.ServicePeriodEnum.WEEKLY;
        }
        else if (ServicePeriodEnum.ANNUAL.getValue().getValue() == period.getValue())
        {
            return com.redknee.app.crm.bean.ServicePeriodEnum.ANNUAL;
        }
        else
        {
            throw new IllegalArgumentException("Unsupported ServicePeriod: " + period.getValue());
        }
    }
    
    
    @Deprecated
    public static ServicePeriod convertCrmServicePeriodEnumIndex2ApiServicePeriodType(final short index)
    {
    
        switch(index)
        {
        case com.redknee.app.crm.bean.ServicePeriodEnum.DAILY_INDEX:
            return ServicePeriodEnum.DAILY.getValue();
        case com.redknee.app.crm.bean.ServicePeriodEnum.MULTIMONTHLY_INDEX:
            return ServicePeriodEnum.MULTIMONTHLY.getValue();
        default: 
            return ServicePeriodEnum.valueOf(index);
        }
    
    }
    
    @Deprecated
    public static ServicePeriod convertCrmDurationEnumIndexIndex2ApiPeriod(final int index)
    {
        switch(index)
        {
        case DurationTypeEnum.DAY_INDEX:
            return ServicePeriodEnum.DAILY.getValue();
        case DurationTypeEnum.MONTH_INDEX:
            return ServicePeriodEnum.MONTHLY.getValue();
        default: 
            return ServicePeriodEnum.valueOf(index);
        }
    }
    
    @Deprecated
    public static com.redknee.app.crm.bean.ServicePeriodEnum convertApiServicePeriodToCrmServicePeriodEnum(ServicePeriod period)
    {
        return com.redknee.app.crm.bean.ServicePeriodEnum.get(convertApiServicePeriodToCrmServicePeriodEnumIndex(period));
    }
    
    @Deprecated
    public static short convertApiServicePeriodToCrmServicePeriodEnumIndex(ServicePeriod period)
    {
        long value = period.getValue();
        if(ServicePeriodEnum.DAILY.getValue().getValue() == value)
        {
            return com.redknee.app.crm.bean.ServicePeriodEnum.DAILY_INDEX; 
        }
        if(ServicePeriodEnum.MULTIMONTHLY.getValue().getValue() == value)
        {
            return com.redknee.app.crm.bean.ServicePeriodEnum.MULTIMONTHLY_INDEX;
        }
        return (short) value;
    }
    
    
    public static RecurrenceScheme getRecurrenceScheme(final BundleProfile bundle)
    {
        RecurrenceScheme scheme = new RecurrenceScheme();
        if (bundle.getChargingRecurrenceScheme().equals(com.redknee.app.crm.bean.ServicePeriodEnum.ONE_TIME))
        {
            scheme.setRecurrenceType(RecurrenceTypeEnum.ONE_TIME.getValue());
            
            if (bundle.getRecurrenceScheme().equals(com.redknee.app.crm.bundle.RecurrenceTypeEnum.ONE_OFF_FIXED_DATE_RANGE_INDEX))
            {
                scheme.setEndDate(bundle.getEndDate());
                scheme.setStartDate(bundle.getStartDate());
            }
            else if (bundle.getRecurrenceScheme().equals(com.redknee.app.crm.bundle.RecurrenceTypeEnum.ONE_OFF_FIXED_INTERVAL))
            {
                if (bundle.getInterval() == DurationTypeEnum.MONTH_INDEX)
                {
                    scheme.setPeriodUnitType(ServicePeriodEnum.MONTHLY.getValue());
                }
                else
                {
                    scheme.setPeriodUnitType(ServicePeriodEnum.DAILY.getValue());
                }
                scheme.setPeriod(Long.valueOf(bundle.getValidity()));
            }
        }
        else
        {
            scheme.setRecurrenceType(RecurrenceTypeEnum.RECURRING.getValue());
            if (bundle.getRecurrenceScheme().equals(com.redknee.app.crm.bundle.RecurrenceTypeEnum.RECUR_CYCLE_FIXED_DATETIME))
            {
                scheme.setPeriodUnitType(ServicePeriodEnum.MONTHLY.getValue());
                scheme.setPeriod(Long.valueOf(1));
            }
            else
            {
                scheme.setPeriodUnitType(convertCrmServicePeriodEnum2ApiServicePeriodType(bundle.getChargingRecurrenceScheme()));
                scheme.setPeriod(Long.valueOf(bundle.getRecurringStartValidity()));
            }
        }
        
        return scheme;
    }


    /**
     * @param crmBean
     * @throws CRMExceptionFault
     */
    public static void validateRepurchaseability(
            com.redknee.app.crm.bean.ui.BundleProfile crmBean)
            throws CRMExceptionFault
    {
        if(crmBean.getRepurchasable() && !crmBean.getChargingRecurrenceScheme().equals(com.redknee.app.crm.bean.ServicePeriodEnum.ONE_TIME))
        {
            final String msg = MessageFormat.format("Bundle is not One-Time. Only One-Time bundles can be marked 'Repurchaseable'.",
                        new Object[]{Long.valueOf(crmBean.getBundleId())});
            RmiApiErrorHandlingSupport.simpleValidation("bundle", msg);
        }
        
        if(crmBean.getExpiryExtensionOnRepurchase()>0 && !crmBean.getRepurchasable())
        {
            final String msg = MessageFormat.format(
                "Bundle is not marked for 'Repurchaseable'. Only One-Time 'Repurchaseable' bundles can have positive repurchase-expiry-extension.",
                    new Object[]{Long.valueOf(crmBean.getBundleId())});
            RmiApiErrorHandlingSupport.simpleValidation("bundle", msg);
        }
        
        if(crmBean.getExpiryExtensionOnRepurchase() < 0)
        {
            final String msg = MessageFormat.format(
                    "Repurchase-expiry-extension value can not be negative.",
                        new Object[]{Long.valueOf(crmBean.getBundleId())});
            RmiApiErrorHandlingSupport.simpleValidation("bundle", msg);
        }
    }
}
