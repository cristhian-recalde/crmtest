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
package com.trilogy.app.crm.subscriber.provision.smsb;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.client.exception.SMSBReturnCodeMsgMapping;
import com.trilogy.app.crm.client.smsb.AppSmsbClientSupport;
import com.trilogy.app.crm.support.EnumStateSupportHelper;

/**
 * ECP and voice agent is somehow overlapping, we need more clarification
 * for these two parts, since add/remove are performed in voiceProvisionAgent
 * But for states update is done from ecp.
 * The new implementation is:
 * This Agents only perfrom other parameters update if ECP is not newly provisioned.
 * That also means that VoiceAgent needs to update for all parameters if provision happens.
 *
 * @author ali
 */
public class SubscriberSmsbUpdateHome extends SubscriberSmsbServiceParameterUpdateHome
{

    /**
     * @param ctx
     * @param delegate
     */
    public SubscriberSmsbUpdateHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    @Override
    protected String getOperationMsg()
    {
        return "SMSB Subscriber Update";
    }

    /**
     * @param ctx
     * @param newSub
     * @throws ProvisioningHomeException
     */
    @Override
    protected void updateServiceParameter(Context ctx, Subscriber oldSub, Subscriber newSub)
            throws HomeException, ProvisioningHomeException
    {
        updateSmsb(ctx, oldSub, newSub);
    }

    /**
     * Used to compare only those parameters being modified between oldSub and newSub
     *
     * @param ctx
     * @param oldSub
     * @param newSub
     */
    @Override
    protected boolean parameterEquals(Context ctx, Subscriber oldSub, Subscriber newSub)
            throws HomeException
    {
        boolean stateChanged = false;

        //Checks if the sub state has changed
        if ( ! EnumStateSupportHelper.get(ctx).stateEquals(oldSub, newSub) ) 
        {
            /*
             * For prepaid sub, we only need to update smsb if sub state changed to 
             * Barred/Locked or Deactivated
             * OR sub state goes to Active  from either Barred/Locked or Available state
             */
             stateChanged = true;
             if ( newSub.getSubscriberType() == SubscriberTypeEnum.PREPAID ) 
             {
                 int [] prepaidLeavingStates = {
                     SubscriberStateEnum.AVAILABLE_INDEX,
                     SubscriberStateEnum.LOCKED_INDEX
                     };

                 if ( (! EnumStateSupportHelper.get(ctx).stateEquals(newSub,SubscriberStateEnum.LOCKED )) && 
                    (! EnumStateSupportHelper.get(ctx).stateEquals(newSub,SubscriberStateEnum.INACTIVE )) && 
                    (! EnumStateSupportHelper.get(ctx).isTransition(oldSub,newSub,prepaidLeavingStates, SubscriberStateEnum.ACTIVE_INDEX)))
                {
                    stateChanged = false;
                }
            }
        }

        // Comparing only parameters being modified: Group Msisdn, State, PackageID
        /*
         * As of CRM 8.2, Rate Plan information is in Price Plan and it is ignored when
         * pushed to the subscriber. So we simply stop updating the rate plan field on the
         * URCS subscriber profile
         */
        boolean groupMsisdnAndPackageEqual = SafetyUtil.safeEquals(newSub.getGroupMSISDN(ctx), oldSub.getGroupMSISDN(ctx))
                        && SafetyUtil.safeEquals(oldSub.getPackageId(), newSub.getPackageId());
        boolean expiring = EnumStateSupportHelper.get(ctx).isEnteringState(oldSub, newSub, SubscriberStateEnum.EXPIRED);
        return groupMsisdnAndPackageEqual && (!stateChanged || expiring);
    }

    /**
     * Update the subscriber profile in SMSB for the given subscriber.
     *
     * @param context The operating context.
     * @param newSub  The given subscriber.
     */
    private void updateSmsb(
            final Context context,
            final Subscriber oldSub,
            final Subscriber newSub)
            throws ProvisioningHomeException, HomeException
    {
        int result = 0;

        Short billCycleDay = null;
        try
        {
            Account account = newSub.getAccount(context);
            if (account != null)
            {
                billCycleDay = Integer.valueOf(account.getBillCycleDay(context)).shortValue();
            }
        }
        catch (HomeException e)
        {
            new MinorLogMsg(AppSmsbClientSupport.class, "Error retrieving bill cycle day for account " + newSub.getBAN(), e).log(context);
        }

        if (LogSupport.isDebugEnabled(context))
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("updating ");
            sb.append(newSub.getMSISDN());
            sb.append(" groupMsisdn:");
            sb.append(newSub.getGroupMSISDN(context));
            sb.append(" imsi:");
            sb.append(newSub.getIMSI());
            if (billCycleDay != null)
            {
                sb.append(" billcycleday:");
                sb.append(billCycleDay);
            }

            new DebugLogMsg(this, sb.toString(), null).log(context);
        }
        
        result = AppSmsbClientSupport.updateSubscriberProfile(context, newSub, billCycleDay);

        if (result != 0)
        {
            throw new ProvisioningHomeException(
                    "Failed to update subscription profile with MSISDN " + newSub.getMsisdn() + " on URCS SMS: " + SMSBReturnCodeMsgMapping.getMessage(result),
                    3007, Common.OM_SMSB_ERROR);
        }
    }

}
