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

package com.trilogy.app.crm.client;

import java.util.Map;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.EcpStateMap;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceBase;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateConversion;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.config.AppEcpClientConfig;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SuspendedEntitySupport;


/**
 * Support class for ECP provisioning client.
 *
 * @author joe.chen@redknee.com
 */
public class AppEcpClientSupport
{

    /**
     * Creates a new <code>AppEcpClientSupport</code> instance. This method is made
     * private to prevent instantiation of utility class.
     */
    private AppEcpClientSupport()
    {
        // empty
    }


    /**
     * Returns the ECP state of the subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param newSub
     *            The subscriber whose state is being determined.
     * @return -1, unknown
     */
    public static int mapToEcpState(final Context ctx, final Subscriber newSub)
    {
        SubscriberStateEnum state = newSub.getState();
        if (state == SubscriberStateEnum.ACTIVE || state == SubscriberStateEnum.EXPIRED)
        {
            boolean suspended = false;
            try
            {
                final Object svc = ServiceSupport.findObjectContainingServiceType(ctx, newSub,
                    ServiceBase.SERVICE_HANDLER_VOICE);
                if (svc != null)
                {
                    if (svc instanceof Service)
                    {
                        suspended = newSub.isServiceSuspended(ctx, ((Service)svc).getID());
                    }
                    else
                    {
                        // Part of a Service Pack, check if the Pack is suspended
                        suspended = SuspendedEntitySupport.isObjectSuspended(ctx, newSub.getId(), svc);
                    }
                }
            }
            catch (final HomeException e)
            {
                LogSupport.debug(ctx, AppEcpClientSupport.class.getName(),
                    "Cannot determine if Voice service is suspended.", e);
            }
            if (suspended)
            {
                state = SubscriberStateEnum.LOCKED;
            }
        }

        int result = -1;

        if (newSub.isPrepaid())
        {
            result = mapPrepaidEcpState(ctx, state);
        }
        else
        {
            result = mapPostpaidEcpState(ctx, state);
        }
        return result;

    }


    /**
     * Returns the equivalent ECP state for the prepaid subscriber state.
     *
     * @param ctx
     *            The operating context.
     * @param enumState
     *            Prepaid subscriber state.
     * @return The equivalent ECP state.
     */
    public static int mapPrepaidEcpState(final Context ctx, final SubscriberStateEnum enumState)
    {
        int ecpState = AppEcpClient.ACTIVE;

        switch (enumState.getIndex())
        {
            case SubscriberStateEnum.INACTIVE_INDEX:
                ecpState = AppEcpClient.DEACTIVATED;
                break;
            case SubscriberStateEnum.LOCKED_INDEX:
                ecpState = AppEcpClient.SUSPENDED;
                break;
        }

        return ecpState;
    }


    /**
     * Returns the equivalent ECP state for the postpaid subscriber state.
     *
     * @param ctx
     *            The operating context.
     * @param enumState
     *            Postpaid subscriber state.
     * @return The equivalent ECP state.
     */
    public static int mapPostpaidEcpState(final Context ctx, final SubscriberStateEnum enumState)
    {
        // no change
        int ecpState = -1;

        final EcpStateMap ecpMap = (EcpStateMap) ctx.get(EcpStateMap.class);
        final Map states = ecpMap.getStates();
        final SubscriberStateConversion stateConv = (SubscriberStateConversion) states.get(enumState);
        if (stateConv != null)
        {
            ecpState = stateConv.getNewStateIndex();
        }

        if (ecpState == -1)
        {
            switch (enumState.getIndex())
            {
                case SubscriberStateEnum.PENDING_INDEX:
                    ecpState = AppEcpClient.ACTIVE;
                    break;
                case SubscriberStateEnum.ACTIVE_INDEX:
                    ecpState = AppEcpClient.ACTIVE;
                    break;
                case SubscriberStateEnum.INACTIVE_INDEX:
                    // service should be removed
                    ecpState = AppEcpClient.INACTIVE;
                    break;
                case SubscriberStateEnum.SUSPENDED_INDEX:
                    ecpState = AppEcpClient.SUSPENDED;
                    break;
                case SubscriberStateEnum.LOCKED_INDEX:
                    ecpState = AppEcpClient.BARRED;
                    break;
                case SubscriberStateEnum.EXPIRED_INDEX:
                    ecpState = AppEcpClient.EXPIRED;
                    break;
                case SubscriberStateEnum.NON_PAYMENT_WARN_INDEX:
                    ecpState = AppEcpClient.DUNNED_WARNING;
                    break;
                case SubscriberStateEnum.NON_PAYMENT_SUSPENDED_INDEX:
                    ecpState = AppEcpClient.DUNNED_SUSPENDED;
                    break;
                case SubscriberStateEnum.PROMISE_TO_PAY_INDEX:
                    ecpState = AppEcpClient.ACTIVE;
                    break;
                case SubscriberStateEnum.IN_ARREARS_INDEX:
                    ecpState = AppEcpClient.DUNNED_SUSPENDED;
                    break;
                case SubscriberStateEnum.IN_COLLECTION_INDEX:
                    ecpState = AppEcpClient.DUNNED_SUSPENDED;
                    break;
                case SubscriberStateEnum.DORMANT_INDEX:
                    // service should be removed
                    ecpState = AppEcpClient.INACTIVE;
                    break;
            }
        }
        return ecpState;
    }


    /**
     * Get the Class of Service From ECP Client Config.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The given subscriber.
     * @return Class of service for the subscriber.
     * @throws HomeException
     *             Thrown if there are problems determining the class of service of the
     *             subscriber.
     */
    public static int getClassOfService(final Context ctx, final Subscriber subscriber) throws HomeException
    {
        AppEcpClientConfig config = null;
        config = (AppEcpClientConfig) ctx.get(AppEcpClientConfig.class);
        if (config == null)
        {
            throw new HomeException("System error: AppEcpClientConfig not found in context");
        }

        final Account account = subscriber.getAccount(ctx);

        // Fetch subscriber class of service
        return config.getClassOfService(ctx, account.getSpid(), account.getType(), subscriber.getSubscriberType());

    }


    /**
     * Update the subscriber profile in ECP for the given subscriber.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The given subscriber.
     * @return ECP result code.
     */
    public static short updateECPSubscriberState(final Context context, final Subscriber subscriber)
    {
        final int ecpState = mapToEcpState(context, subscriber);
        short result = 0;
        if (ecpState != -1)
        {
            final AppEcpClient ecpClient = (AppEcpClient) context.get(AppEcpClient.class);

            // we assume that ECP is always there.
            result = (short) ecpClient.updateSubscriberState(subscriber.getMSISDN(), ecpState);
        }
        return result;
    }


    /**
     * Update the voice rate plan of the subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param newSub
     *            The subscriber being updated.
     * @return ECP result code.
     * @throws HomeException
     *             Thrown if there areproblem updating the voice rate plan.
     * @deprecated As of CRM 8.2 AppECPClient.updateRatePlan(MSISDN, Rate Plan) is no longer supported.  Rate Plan is 
     *                 identified by a String in URCS. 
     */
     /*public static int updateVoiceRatePlan(final Context ctx, final Subscriber newSub) throws HomeException*/


    /**
     * Checks if subscriber just added into ECP during this pipeline traversal.
     *
     * @param ctx
     *            The operating context.
     * @param newSub
     *            The subscriber being verified.
     * @return Whether the subscriber has just been added into ECP during this pipeline
     *         traversal.
     * @throws HomeException
     *             Thrown if there are problems determining the result.
     */
    public static boolean hasServiceJustProvisioned(final Context ctx, final Subscriber newSub) throws HomeException
    {
        boolean newly = false;
        if (newSub != null)
        {
            final Service obj = ServiceSupport.findSubscriberNewlyProvisionedServiceType(ctx, newSub,
                ServiceBase.SERVICE_HANDLER_VOICE);
            if (obj != null)
            {
                newly = true;
            }
        }
        return newly;
    }


    /**
     * Checks if the subscriber has service newly provisioned.
     *
     * @param ctx
     *            The operating context.
     * @param newSub
     *            The subscriber being verified.
     * @return Whether the subscriber any services just added into ECP during this
     *         pipeline traversal.
     * @throws HomeException
     *             Thrown if there are problems determining the result.
     */
    public static boolean hasServiceProvisioned(final Context ctx, final Subscriber newSub) throws HomeException
    {
        boolean newly = false;
        if (newSub != null)
        {
            final Service obj = ServiceSupport.findSubscriberProvisionedServiceType(ctx, newSub,
                ServiceBase.SERVICE_HANDLER_VOICE);
            if (obj != null)
            {
                newly = true;
            }
        }
        return newly;
    }


    /**
     * Updates the group MSISDN of a subscriber.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber being updated.
     * @return result of update operation
     */
    public static int updateGroupMsisdn(final Context context, final Subscriber subscriber)
    {
        // ECP
        final AppEcpClient client = (AppEcpClient) context.get(AppEcpClient.class);
        String groupMSISDN = subscriber.getGroupMSISDN(context);
        if( "".equals(groupMSISDN) )
        {
            // This check is required because as of CRM 7.5, the group MSISDN field in CRM and ABM is blank
            // for non-pooled subscribers.  Previous versions provisioned ECP with group MSISDN = MSISDN.
            groupMSISDN = subscriber.getMSISDN();
        }
        final int result = client.updateGroupAccount(subscriber.getMSISDN(), groupMSISDN);
        return result;
    }


    /**
     * Updates the class of service of a subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The subscriber being updated.
     * @return ECP result code.
     * @throws AgentException
     *             Thrown if there are problems updating the class of service of the
     *             subscriber.
     */
    public static int updateClassOfService(final Context ctx, final Subscriber subscriber) throws AgentException
    {
        final AppEcpClient client = (AppEcpClient) ctx.get(AppEcpClient.class);

        AppEcpClientConfig config = null;
        config = (AppEcpClientConfig) ctx.get(AppEcpClientConfig.class);
        if (config == null)
        {
            throw new AgentException("System error: AppEcpClientConfig not found in context");
        }

        final Account account = (Account) ctx.get(Account.class);

        // Fetch subscriber class of service
        int classOfService = 0;
        try
        {
            classOfService = config.getClassOfService(ctx, account.getSpid(), account.getType(), subscriber
                .getSubscriberType());
        }
        catch (final HomeException e)
        {
            throw new AgentException(e.getMessage());
        }

        final int result = client.updateClassOfService(subscriber.getMSISDN(), classOfService);
        return result;
    }


    /**
     * Enables home zone for a subscriber in ECP.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber whose home zone service is being enabled.
     * @return ECP result code.
     * @throws AgentException
     *             Thrown if there are problems enabling home zone.
     */
    public static int enableHomeZoneInECP(final Context ctx, final Subscriber sub) throws AgentException
    {
        final AppEcpClient client = (AppEcpClient) ctx.get(AppEcpClient.class);
        if (client == null)
        {
            throw new AgentException("System error: AppEcpClient not found in context");
        }
        if (sub == null)
        {
            throw new AgentException("System error: Subscriber for whichhomezone is to be enabled is null");
        }
        return client.enableHomezone(sub.getMSISDN());
    }


    /**
     * Disables home zone for a subscriber in ECP.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber whose home zone service is being disabled.
     * @return ECP result code.
     * @throws AgentException
     *             Thrown if there are problems disabling home zone.
     */
    public static int disableHomeZoneInECP(final Context ctx, final Subscriber sub) throws AgentException
    {
        final AppEcpClient client = (AppEcpClient) ctx.get(AppEcpClient.class);
        if (client == null)
        {
            throw new AgentException("System error: AppEcpClient not found in context");
        }
        if (sub == null)
        {
            throw new AgentException("System error: Subscriber for whichhomezone is to be enabled is null");
        }
        return client.disableHomezone(sub.getMSISDN());
    }

}
