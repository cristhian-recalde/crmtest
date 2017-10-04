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
package com.trilogy.app.crm.home.sub.conversion;

import java.util.Calendar;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.amsisdn.AdditionalMsisdnAuxiliaryServiceSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;


/**
 * Validate and executes Prepaid->Postpaid conversion. The subscriber pipeline takes care
 * of the conversion, this class would only delegate the storage. BUT it will validate
 * according to the requirements.
 *
 * @author arturo.medina@redknee.com
 */
public class PrepaidPostPaidConversion extends AbstractSubscriberConversion
{

    /**
     * Number of years.
     */
    private static final int NUMBER_OF_YEARS = 25;


    /**
     * Create a new instance of <code>PrepaidPostPaidConversion</code>.
     */
    public PrepaidPostPaidConversion()
    {
        super();
        this.from_ = SubscriberTypeEnum.PREPAID;
        this.to_ = SubscriberTypeEnum.POSTPAID;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Subscriber executeConvertion(final Context ctx, final Subscriber prevSubscriber,
        final Subscriber currentSubscriber, final Home delegate) throws HomeException
    {
        ctx.put(Common.PREPAID_POSTPAID_CONVERSION_SUBCRIBER, currentSubscriber);
        currentSubscriber.setExpiryDate(addExpiryDate());

        try
        {
            /*
             * TT5101025178: (Pre->Postpaid conversion) The expiry date should be updated to
             * 0 in ABM. This signifies that the subscriber does not expire.
             */
            final SubscriberProfileProvisionClient bmClient = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
            bmClient.updateExpiryDate(ctx, currentSubscriber, new Date(0));
        }
        catch (final SubscriberProfileProvisionException exception)
        {
            throw new ProvisioningHomeException("Failed to update expirydate on converson" + currentSubscriber.getId()
                + ",rc=" + exception.getErrorCode(), 0, Common.OM_CRM_PROV_ERROR, exception);
        }

        if (prevSubscriber.getState() == SubscriberStateEnum.SUSPENDED)
        {
            currentSubscriber.setState(SubscriberStateEnum.ACTIVE);
            currentSubscriber.setAboveCreditLimit(true);            
        }
        else if (prevSubscriber.getState() == SubscriberStateEnum.EXPIRED)
        {
            currentSubscriber.setState(SubscriberStateEnum.ACTIVE);
        }
        convertMSISDNToPostpaid(ctx, currentSubscriber);
        final Subscriber retSub = (Subscriber) delegate.store(ctx, currentSubscriber);
        /*
         * This conversion resetting the homezone flag in the ECP profile, hence we need
         * to explicitly set it, this needs to be done after store is called on the
         * subscriber.
         */
        try
        {
            SubscriberAuxiliaryServiceSupport.enableHZIfRequired(ctx, retSub);
        }
        catch (final Exception e)
        {
            // log and ignore
            new MajorLogMsg(this, "Can not enable the homezone flag in ECP profile for subscriber:" + retSub.getId(), e)
                .log(ctx);
        }
        return retSub;
    }


    /**
     * Adds {@link #NUMBER_OF_YEARS} years after today.
     *
     * @return A date {@link #NUMBER_OF_YEARS} years after today.
     */
    private Date addExpiryDate()
    {
        final Calendar cal = Calendar.getInstance();
        final int year = cal.get(Calendar.YEAR);
        cal.set(Calendar.YEAR, year + NUMBER_OF_YEARS);
        return cal.getTime();
    }


    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        if (obj instanceof Subscriber)
        {
            final Subscriber sub = (Subscriber) obj;
            final Subscriber newSub = (Subscriber) ctx.get(Common.PREPAID_POSTPAID_CONVERSION_SUBCRIBER);
            Account acct = null;
            try
            {
                acct = getAccount(ctx, sub);
            }
            catch (final HomeException e)
            {
                LogSupport.major(ctx, this, "Home Exception : ", e);
                throw new IllegalStateException("Home Exception when trying to get the account: " + e.getMessage());
            }

            if (sub.getState() == SubscriberStateEnum.ACTIVE || sub.getState() == SubscriberStateEnum.SUSPENDED
                || sub.getState() == SubscriberStateEnum.EXPIRED)
            {
                if (acct.getState() == AccountStateEnum.ACTIVE && acct.getSystemType() != SubscriberTypeEnum.HYBRID)
                {
                    throw new IllegalStateException("The subscriber's account must be Active and Hybrid");
                }
            }
            else
            {
                throw new IllegalStateException(
                    "The subscriber needs to be within the following states: [Active, Suspended, Expired] ");
            }
            if (newSub != null)
            {
                if (sub.getPricePlan() == newSub.getPricePlan())
                {
                    throw new IllegalStateException("Price Plan needs to change when converting.");
                }
            }
            else
            {
                throw new IllegalStateException("System error: no AccountHome found in context.");
            }
        }
    }


    /**
     * Converts all the MSISDNs owned by this subscriber to postpaid.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being updated.
     * @throws HomeException
     *             Thrown if there are problems converting the MSISDN to postpaid.
     */
    private void convertMSISDNToPostpaid(final Context ctx, final Subscriber sub) throws HomeException
    {
        MsisdnSupport.setMsisdnType(ctx, sub.getMSISDN(), SubscriberTypeEnum.POSTPAID);
        AdditionalMsisdnAuxiliaryServiceSupport.convertMsisdnToPostpaid(ctx, sub);
    }

}
