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

package com.trilogy.app.crm.client.smsb;

import java.util.Arrays;
import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceBase;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.config.AppSmsbClientConfig;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SuspendedEntitySupport;
import com.trilogy.app.smsb.dataserver.smsbcorba.subsProfile7;


/**
 * @author joe.chen@redknee.com
 */
public final class AppSmsbClientSupport
{
    /**
     * Prevent wanted instantiation of utility class.
     */
    private AppSmsbClientSupport()
    {
        // EMPTY
    }


    /**
     * SMSB state for Prepaid subscriber should be set to active state Some
     * states should not affect enable/disable in smsb side, so we return null.
     *
     * @param newSub
     * @return Boolean object with the mapped state or null is state cannot be
     * determined
     */
    private static Boolean mapPrepaidSmsbState(final Subscriber newSub)
    {
        return mapSmsbState(
                newSub, 
                Arrays.asList(
                        SubscriberStateEnum.INACTIVE,
                        SubscriberStateEnum.LOCKED,
                        SubscriberStateEnum.PENDING), 
                Arrays.asList(
                        SubscriberStateEnum.ACTIVE,
                        SubscriberStateEnum.SUSPENDED,
                        SubscriberStateEnum.EXPIRED,
                        SubscriberStateEnum.AVAILABLE));
    }


    private static Boolean mapPostPaidSmsbState(final Subscriber newSub)
    {
        // Manda- Sub profile state should be enabled on
            // smsb if sub state is in available state for Buzzard
        return mapSmsbState(
                newSub, 
                Arrays.asList(
                        SubscriberStateEnum.INACTIVE,
                        SubscriberStateEnum.EXPIRED,
                        SubscriberStateEnum.LOCKED,
                        SubscriberStateEnum.SUSPENDED,
                        // SubscriberStateEnum.AVAILABLE,
                        SubscriberStateEnum.PENDING), 
                Arrays.asList(
                        SubscriberStateEnum.ACTIVE,
                        SubscriberStateEnum.PROMISE_TO_PAY,
                        SubscriberStateEnum.AVAILABLE));
    }


    private static Boolean mapSmsbState(final Subscriber newSub, final Collection<? extends Enum> disableStates, final Collection<? extends Enum> enabledStates)
    {

        if (EnumStateSupportHelper.get().isOneOfStates(newSub, disableStates))
        {
            return Boolean.FALSE;
        }

        if (EnumStateSupportHelper.get().isOneOfStates(newSub, enabledStates))
        {
            return Boolean.TRUE;
        }


        return null;

    }


    public static Boolean mapSmsbState(final Context ctx, final Subscriber newSub)
    {
        if (newSub.isPrepaid())
        {

            return mapPrepaidSmsbState(newSub);
        }

        return mapPostPaidSmsbState(newSub);
    }


    public static short updateSmsbSubscriberState(final Context context, final Subscriber subscriber)
    {
        final Boolean smsbState = mapSmsbState(context, subscriber);
        short result = 0;
        if (smsbState != null)
        {
            final AppSmsbClient client = (AppSmsbClient)context.get(AppSmsbClient.class);

            result = (short)client.enableSubscriber(subscriber.getMSISDN(), smsbState.booleanValue());
        }
        return result;
    }


    public static int updateSubscriberProfile(final Context ctx, final Subscriber sub, Short billCycleDay)
    {
        final AppSmsbClient smsbClient = (AppSmsbClient)ctx.get(AppSmsbClient.class);

        // SmsbSubscriberState
        Boolean smsbState = mapSmsbState(ctx, sub);
        // if state is enabled, check that the SMS service is not suspended
        if (Boolean.TRUE.equals(smsbState))
        {
            boolean suspended = false;
            try
            {
                final Object svc =
                    ServiceSupport.findObjectContainingServiceType(ctx, sub, ServiceBase.SERVICE_HANDLER_SMS);
                suspended = isServiceEntitySuspended(ctx, sub, svc);
            }
            catch (final HomeException e)
            {
                LogSupport.debug(ctx, AppSmsbClientSupport.class.getName(),
                    "Cannot determine if SMS service is suspended.", e);
            }
            if (suspended)
            {
                smsbState = Boolean.FALSE;
            }
        }

        String groupMSISDN = sub.getGroupMSISDN(ctx);

        // This check is required because as of CRM 7.5, the group MSISDN
        // field in CRM and ABM is blank
        // for non-pooled subscribers. Previous versions provisioned SMSB
        // with group MSISDN = MSISDN.
        if (!sub.isPooled(ctx))
        {
            groupMSISDN = sub.getMSISDN();
        }
        
        final int result =
            smsbClient.updateSelectedParameters(sub.getMSISDN(), groupMSISDN, sub.getIMSI(), billCycleDay, smsbState);

        return result;
    }


    public static boolean hasServiceProvisioned(final Context ctx, final Subscriber newSub)
        throws HomeException
    {
        boolean newly = false;
        if (newSub != null)
        {
            final Service obj =
                ServiceSupport.findSubscriberProvisionedServiceType(ctx, newSub, ServiceBase.SERVICE_HANDLER_SMS);
            if (obj != null)
            {
                newly = true;
            }
        }
        return newly;
    }


    /**
     * Determines if provisioning just happened in this subscriber pipeline.
     *
     * @param ctx
     * @param newSub
     * @return
     * @throws HomeException
     */
    public static boolean hasServiceJustProvisioned(final Context ctx, final Subscriber newSub)
        throws HomeException
    {
        boolean newly = false;
        if (newSub != null)
        {
            final Service obj =
                ServiceSupport.findSubscriberNewlyProvisionedServiceType(ctx, newSub, ServiceBase.SERVICE_HANDLER_SMS);
            if (obj != null)
            {
                newly = true;
            }
        }
        return newly;
    }


    /**
     * @param context
     * @param subscriber
     * @return result of update operation
     */
    public static int updateGroupMsisdn(final Context context, final Subscriber subscriber)
    {
        final AppSmsbClient client = (AppSmsbClient)context.get(AppSmsbClient.class);
        String groupMSISDN = subscriber.getGroupMSISDN(context);
        if ("".equals(groupMSISDN))
        {
            // This check is required because as of CRM 7.5, the group MSISDN
            // field in CRM and ABM is blank
            // for non-pooled subscribers. Previous versions provisioned SMSB
            // with group MSISDN = MSISDN.
            groupMSISDN = subscriber.getMSISDN();
        }
        final int result = client.updateGroupMsisdn(subscriber.getMSISDN(), groupMSISDN);
        return result;
    }


    /**
     * This method updates the SMSB Service Grade based on the VPN
     * Service selection.
     *
     * @param ctx Context Object
     * @param sub Subscriber Object
     * @return int value specifying the result of the update operation.
     */
    public static int updateSubscriberProfileForSvcGrade(final Context ctx, final Subscriber sub)
        throws HomeException
    {
        int result = ERROR_RESULT;
        final AppSmsbClient smsbClient = (AppSmsbClient)ctx.get(AppSmsbClient.class);

        if (smsbClient == null)
        {
            new MajorLogMsg(AppSmsbClientSupport.class, "Smsb Client not found in Context, "
                + "Cannot proceed further to update Subscriber profile on SMSB for the Service Grade", null).log(ctx);

            throw new HomeException("Smsb Client not found in Context, "
                + "Cannot proceed further to update Subscriber profile on SMSB for the Service Grade");
        }

        final AppSmsbClientConfig config = (AppSmsbClientConfig)ctx.get(AppSmsbClientConfig.class);

        if (config == null)
        {
            new MajorLogMsg(AppSmsbClientSupport.class, "Smsb Client config not found in Context, "
                + "Cannot proceed further to update Subscriber profile on SMSB for the Service Grade", null).log(ctx);

            throw new HomeException("Smsb Client Config not found in Context, "
                + "Cannot proceed further to update Subscriber profile on SMSB for the Service Grade");
        }

        // SmsbSubscriberState
        Boolean smsbState = mapSmsbState(ctx, sub);

        // if state is enabled, check that the SMS service is not suspended
        if (Boolean.TRUE.equals(smsbState) || Boolean.FALSE.equals(smsbState)
            && sub.getSubscriberType().equals(SubscriberTypeEnum.PREPAID)
            && !sub.getState().equals(SubscriberStateEnum.AVAILABLE))
        {
            boolean suspended = false;
            try
            {
                final Object svc =
                    ServiceSupport.findObjectContainingServiceType(ctx, sub, ServiceBase.SERVICE_HANDLER_SMS);
                suspended = isServiceEntitySuspended(ctx, sub, svc);
            }
            catch (final HomeException e)
            {
                LogSupport.debug(ctx, AppSmsbClientSupport.class.getName(),
                    "Cannot determine if SMS service is suspended.", e);
            }
            if (suspended)
            {
                smsbState = Boolean.FALSE;
            }
        }

        // Check if the subscriber is prepaid, since prepaid subscribers state
        // defaults to Available while creation.
        if (smsbState.equals(Boolean.TRUE) || Boolean.FALSE.equals(smsbState)
            && sub.getSubscriberType().equals(SubscriberTypeEnum.PREPAID)
            && sub.getState().equals(SubscriberStateEnum.AVAILABLE))

        {
            final String subMsisdn = sub.getMSISDN();
            final subsProfile7 smsSubProfile = smsbClient.getSubsProfile(subMsisdn);

            if (smsSubProfile != null)
            {
                // Modify only the Service Grade field
                smsSubProfile.svcGrade =
                    config.getSvcGradeWithVpnCheck(ctx, AccountSupport.getAccountByMsisdn(ctx, subMsisdn), sub);

                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(AppSmsbClientSupport.class.getName(), " Provisioning SMSB with service grade = "
                        + smsSubProfile.svcGrade, null).log(ctx);
                }

                result = smsbClient.updateSubscriber(smsSubProfile);
            }
            else
            {
                // No need to update as the subscriber is not provisioned on
                // smsb
                result = SUCCESS_RESULT;
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(AppSmsbClientSupport.class.getName(), " Not Provisioning SMSB with service grade "
                        + " as the Subscriber " + sub.getId() + " is not provisioned on smsb ", null).log(ctx);
                }
            }
        }
        else
        {
            // SMSB State is suspended, no need for update
            result = SUCCESS_RESULT;
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(AppSmsbClientSupport.class.getName(), " Not Provisioning SMSB with service grade "
                    + " as the Subscriber " + sub.getId() + " is suspended ", null).log(ctx);
            }
        }
        return result;
    }


    /**
     * Manda - This method updates the SMSB Service Grade based on the VPN
     * Service selection, when VPN aux Service is unprovisioned for the
     * subscriber.
     *
     * @param ctx Context Object
     * @param sub Subscriber Object
     * @return int value specifying the result of the update operation.
     */
    public static int updateSubscriberProfileForSvcGradeForUnprovision(final Context ctx, final Subscriber sub)
        throws HomeException
    {
        int result = ERROR_RESULT;
        final AppSmsbClient smsbClient = (AppSmsbClient)ctx.get(AppSmsbClient.class);

        if (smsbClient == null)
        {
            new MajorLogMsg(AppSmsbClientSupport.class, "Smsb Client not found in Context, "
                + "Cannot proceed further to update Subscriber profile on SMSB for the Service Grade", null).log(ctx);

            throw new HomeException("Smsb Client not found in Context, "
                + "Cannot proceed further to update Subscriber profile on SMSB for the Service Grade");
        }

        final AppSmsbClientConfig config = (AppSmsbClientConfig)ctx.get(AppSmsbClientConfig.class);

        if (config == null)
        {
            new MajorLogMsg(AppSmsbClientSupport.class, "Smsb Client config not found in Context, "
                + "Cannot proceed further to update Subscriber profile on SMSB for the Service Grade", null).log(ctx);

            throw new HomeException("Smsb Client Config not found in Context, "
                + "Cannot proceed further to update Subscriber profile on SMSB for the Service Grade");
        }

        // SmsbSubscriberState
        Boolean smsbState = mapSmsbState(ctx, sub);

        // if state is enabled, check that the SMS service is not suspended
        if (Boolean.TRUE.equals(smsbState))
        {
            boolean suspended = false;
            try
            {
                final Object svc =
                    ServiceSupport.findObjectContainingServiceType(ctx, sub, ServiceBase.SERVICE_HANDLER_SMS);
                suspended = isServiceEntitySuspended(ctx, sub, svc);
            }
            catch (final HomeException e)
            {
                LogSupport.debug(ctx, AppSmsbClientSupport.class.getName(),
                    "Cannot determine if SMS service is suspended.", e);
            }
            if (suspended)
            {
                smsbState = Boolean.TRUE;
            }
        }

        final short svcGrade = config.getSvcGrade(sub.getSubscriberType());

        if (smsbState.equals(Boolean.TRUE) || smsbState.equals(Boolean.FALSE)
            && sub.getState().equals(SubscriberStateEnum.SUSPENDED)
            || sub.getState().equals(SubscriberStateEnum.INACTIVE) || sub.getState().equals(SubscriberStateEnum.MOVED)
            || sub.getState().equals(SubscriberStateEnum.LOCKED) || sub.getState().equals(SubscriberStateEnum.EXPIRED)
            || sub.getState().equals(SubscriberStateEnum.IN_ARREARS))

        {
            final subsProfile7 smsSubProfile = smsbClient.getSubsProfile(sub.getMSISDN());

            if (smsSubProfile != null)
            {
                // Modify only the Service Grade field
                // smsSubProfile.svcGrade =
                // config.getSvcGradeWithVpnCheck(ctx,AccountSupport.getAccountByMsisdn(ctx,sub.getMSISDN()),sub,sub.getSubscriberType());
                if (smsSubProfile.svcGrade == svcGrade)
                {
                    return SUCCESS_RESULT;
                }
                smsSubProfile.svcGrade = svcGrade;

                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(AppSmsbClientSupport.class.getName(), " Provisioning SMSB with service grade = "
                        + svcGrade + " for Subscriber = " + sub.getId(), null).log(ctx);
                }

                result = smsbClient.updateSubscriber(smsSubProfile);
            }
            else
            {
                // No need to update as the subscriber is not provisioned on
                // smsb
                result = SUCCESS_RESULT;
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(AppSmsbClientSupport.class.getName(), " Not Provisioning SMSB with service grade "
                        + " as the Subscriber " + sub.getId() + " is not provisioned to smsb ", null).log(ctx);
                }

            }
        }
        else
        {
            // SMSB State is suspended, no need for update
            result = SUCCESS_RESULT;
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(AppSmsbClientSupport.class.getName(), " Not Provisioning SMSB with service grade "
                    + " as the Subscriber " + sub.getId() + " is suspended ", null).log(ctx);
            }
        }
        return result;
    }


    private static boolean isServiceEntitySuspended(final Context ctx, final Subscriber sub, final Object svc)
        throws HomeException
    {
        boolean result = false;
        if (svc != null)
        {
            if (svc instanceof Service)
            {
                result = sub.isServiceSuspended(ctx, ((Service)svc).getID());
            }
            else
            {
                // Part of a Service Pack, check if the Pack is suspended
                result = SuspendedEntitySupport.isObjectSuspended(ctx, sub.getId(), svc);
            }
        }
        return result;
    }


    private static final int ERROR_RESULT = -2;

    private static final int SUCCESS_RESULT = 0;
}
