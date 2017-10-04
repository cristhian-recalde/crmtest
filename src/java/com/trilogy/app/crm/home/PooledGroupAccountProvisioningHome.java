/*
 *  PooledGroupAccountProvisioningHome.java
 *
 *  Author : Gary Anderson
 *  Date   : 2003-11-14
 *
 *  Copyright (c) Redknee, 2003
 *  - all rights reserved
 */
package com.trilogy.app.crm.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.AppEcpClient;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.Parameters;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.app.crm.client.exception.ECPReturnCodeMsgMapping;
import com.trilogy.app.crm.client.exception.SMSBReturnCodeMsgMapping;
import com.trilogy.app.crm.client.smsb.AppSmsbClient;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * Manages AccountType changes affecting pooled-group accounts.
 *
 * @author gary.anderson@redknee.com
 */
public class PooledGroupAccountProvisioningHome
    extends HomeProxy
{
    /**
     * Creates a new PooledGroupAccountProvisioningHome for the given home.
     *
     * @param context The operating context.
     * @param delegate The Home to which this object delegates.
     */
    public PooledGroupAccountProvisioningHome(
        Context context,
        Home delegate)
    {
        super(context, delegate);
    }


    // INHERIT
    @Override
    public Object store(Context ctx, Object obj)
        throws HomeException
    {
        Account newAccount = (Account) obj;

        final Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);

        if (oldAccount.isPooled(ctx) && !newAccount.isPooled(ctx))
        {
            // Clear also the ownerMSISDN -- it's not used.
            newAccount.setOwnerMSISDN(null);
        }

		boolean accountTypeChange =
		    (oldAccount.getType() != newAccount.getType());

        if (accountTypeChange)
        {
            new OMLogMsg(
                Common.OM_MODULE,
                Common.OM_ACCT_TYPE_CHANGE_ATTEMPT).log(ctx);
        }

        boolean successfulAccountTypeChange = false;

        Object ret = null;

        try
        {
            ret = super.store(ctx, newAccount);
            successfulAccountTypeChange = true;
        }
        finally
        {
            if (accountTypeChange && successfulAccountTypeChange)
            {
                new OMLogMsg(
                    Common.OM_MODULE,
                    Common.OM_ACCT_TYPE_CHANGE_SUCCESS).log(ctx);
            }
            else if (accountTypeChange)
            {
                new OMLogMsg(
                    Common.OM_MODULE,
                    Common.OM_ACCT_TYPE_CHANGE_FAIL).log(ctx);
            }
        }

        return ret;
    }

    /**
     * Updates the given subscriber's group MSISDN in external services (ECP, SMSB).
     *
     * @param ctx
     * @param subscriber
     * @param groupMsisdn
     * @param ex
     */
    private void updateGroupMsisdnInServices(Context ctx, Subscriber subscriber, String groupMsisdn, StringBuilder ex)
    {
        // List of subscribed services
        Set subServices = subscriber.getServices();
        if( subServices != null && subServices.size() > 0 )
        {
            // ECP
            Service voiceService = SubscriberSupport.getService(ctx, subscriber, ServiceTypeEnum.VOICE, this);
            if( voiceService != null && subServices.contains(voiceService.getID()) )
            {
                updateGroupMsisdnInECP(ctx, subscriber, groupMsisdn, ex);
            }

            // SMSB
            Service smsService = SubscriberSupport.getService(ctx, subscriber, ServiceTypeEnum.SMS, this);
            if( smsService != null && subServices.contains(smsService.getID()) )
            {
                updateGroupMsisdnInSMSB(ctx, subscriber, groupMsisdn, ex);
            }
        }
    }

    /**
     * This method returns a collection of subscribers such that the group leader of a
     * pooled account (i.e. MSISDN = Group MSISDN) is last. This is required for
     * provisioning to ABM, since it returns 204 (OPS_NOT_ALLOWED) when attempting to
     * change the Group MSISDN of a group leader that has members.
     *
     * @param ctx FW Context
     * @param subscribers Unsorted list of subscribers.  They should all be in the same account.
     * @return
     */
    private Collection<Subscriber> sortSubscribersByMembersFirst(final Context ctx, final String groupMSISDN,
            final Collection<Subscriber> subscribers)
    {
        if( subscribers.size() <= 1 )
        {
            return subscribers;
        }

        List<Subscriber> returnValue;

        // Sort the accounts subscribers such that group leaders are processed last
        if(subscribers instanceof List)
        {
            returnValue = (List<Subscriber>)subscribers;
        }
        else
        {
            // Collections.sort() takes a list as the first argument
            List<Subscriber> tempSubs = new ArrayList<Subscriber>(subscribers.size());
            for( Subscriber sub : subscribers )
            {
                tempSubs.add(sub);
            }
            returnValue = tempSubs;
        }

        Collections.sort(returnValue, new Comparator<Subscriber>()
        {
            @Override
            public int compare(Subscriber sub1, Subscriber sub2)
            {
                if( SafetyUtil.safeEquals(sub2.getMSISDN(), groupMSISDN) )
                {
                    return -1;
                }
                return 1;
            }
        });

        return returnValue;
    }

    private void updateGroupMsisdnInSMSB(Context ctx, Subscriber subscriber, String groupMsisdn, StringBuilder ex)
    {
        int result = 0;
        String msisdn = subscriber.getMSISDN();
        try
        {
            AppSmsbClient client = getAppSmsbClient(ctx);
            result = client.updateGroupMsisdn(msisdn,groupMsisdn);
        }
        catch (Exception e)
        {
            logException(ctx,
                    "Failure to update subscriber \"" + msisdn
                    + "\" with GroupMSISDN \"" + groupMsisdn + "\".  "
                    + "SMSB communication failure.", e, ex);
        }

        if (result != 0)
        {
            logException(ctx,
                    "Failure to update subscriber \"" + msisdn
                    + "\" with GroupMSISDN \"" + groupMsisdn + "\" on URCS SMS: "
                    + SMSBReturnCodeMsgMapping.getMessage(result), null, ex);
        }
    }


    private void updateGroupMsisdnInECP(Context ctx, Subscriber subscriber, String groupMsisdn, StringBuilder ex)
    {
        int result = 0;
        String msisdn = subscriber.getMSISDN();
        try
        {
            AppEcpClient client = getAppEcpClient(ctx);
            result = client.updateGroupAccount(msisdn,groupMsisdn);
        }
        catch (Exception e)
        {
            logException(ctx,
                    "Failure to update subscriber \"" + msisdn
                    + "\" with GroupMSISDN \"" + groupMsisdn + "\".  "
                    + "ECP communication failure.", e, ex);
        }

        if (result != 0)
        {
            logException(ctx,
                    "Failure to update subscription's URCS Voice profile for MSISDN \"" + msisdn
                    + "\" with GroupMSISDN \"" + groupMsisdn + "\": "
                    + ECPReturnCodeMsgMapping.getMessage(result), null, ex);
        }
    }


    private void logException(Context ctx, String error, Throwable th, StringBuilder ex)
    {
        ex.append("<br>");
        ex.append(error);
        new MinorLogMsg(this, error, th).log(ctx);
    }

    /**
     * Gets the AppEcpClient from the context.
     *
     * @return The AppEcpClient from the context.
     *
     * @exception HomeException Thrown if the AppEcpClient is not found in the context.
     */
    private AppEcpClient getAppEcpClient(Context ctx)
        throws HomeException
    {
        final AppEcpClient client = (AppEcpClient) ctx.get(AppEcpClient.class);

        if (client == null)
        {
            throw new HomeException("No AppEcpClient found in context.");
        }

        return client;
    }


    /**
     * Gets the AppSmsbClient from the context.
     *
     * @return The AppSmsbClient from the context.
     *
     * @exception HomeException Thrown if the AppSmsbClient is not found in the context.
     */
    private AppSmsbClient getAppSmsbClient(Context ctx)
        throws HomeException
    {
        final AppSmsbClient client = (AppSmsbClient) ctx.get(AppSmsbClient.class);

        if (client == null)
        {
            throw new HomeException("No AppSmsbClient found in context.");
        }

        return client;
    }


    /**
     * Gets the SubscriberProfileProvisionClient from the context.
     *
     * @return The SubscriberProfileProvisionClient from the context.
     *
     * @exception HomeException Thrown if the SubscriberProfileProvisionClient is not found in the context.
     */
    private SubscriberProfileProvisionClient getSubscriberProfileProvisionClient(Context ctx)
        throws HomeException
    {
        final SubscriberProfileProvisionClient client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
        if (client == null)
        {
            throw new HomeException("No SubscriberProfileProvisionClient found in context.");
        }

        return client;
    }


    /**
     * Updates the group leader's balance in UPS.
     *
     * @param newGroupMSISDN The msisdn of the group leader.
     * @param oldGroupMSISDN The msisdn of the previous group leader.
     * @param balance The new balance of the group leader.
     * @param account
     *
     * @exception HomeException Thrown if there are problems access data in the
     * context.
     */
    private void updateGroupLeaderInUPS(Context ctx, String newGroupMSISDN, String oldGroupMSISDN, int balance, Subscriber subscription, StringBuilder ex)
        throws HomeException
    {
        // TODO -- This simply won't work.  Updates need to be made to properly support group-pooled features in MM.
        final Subscriber newLeader = null;
        final Subscriber oldLeader = null;
        
        final SubscriberProfileProvisionClient client = getSubscriberProfileProvisionClient(ctx);

        try
        {
            client.updatePooledGroupID(ctx, subscription, newGroupMSISDN, false);
            client.updateBalance(ctx, newLeader, balance);
            
        if( subscription.isPrepaid() )
            {
                // OID 36351 - For prepaid group pooled accounts, we move the old group leader's ABM values to the new group leader
                Parameters oldGroupLeaderMsisdnProfile = client.querySubscriptionProfile(ctx, oldLeader);
                if( oldGroupLeaderMsisdnProfile != null )
                {
                    // The old group MSISDN exists in BM
                    // In this case, there are old BM values to move to the new group leader
                    //client.updateCurrency(ctx, newLeader, oldGroupLeaderMsisdnProfile.getCurrency());
                    client.updateExpiryDate(ctx, newLeader, oldGroupLeaderMsisdnProfile.getExpiryDate());
                    //client.updateState(ctx, newLeader, oldGroupLeaderMsisdnProfile.getState());
                }
            }
        }
        catch (final SubscriberProfileProvisionException exception)
        {
            // TODO - EntryLogMsg?  OM?
            logException(ctx,
                "Failure to update group leader \""
                + newGroupMSISDN
                + "\" BM result: "
                + exception.getErrorCode(), exception, ex);
        }
    }


    /**
     * The operating context.
     */
    protected Context context_ = new ContextSupport();

} // class
