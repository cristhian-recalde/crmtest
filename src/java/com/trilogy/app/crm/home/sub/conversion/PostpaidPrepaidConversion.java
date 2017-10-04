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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.amsisdn.AdditionalMsisdnAuxiliaryServiceSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.UpdateReasonEnum;
import com.trilogy.app.crm.bundle.SubscriberBucket;
import com.trilogy.app.crm.bundle.UnitTypeEnum;
import com.trilogy.app.crm.bundle.exception.BundleManagerException;
import com.trilogy.app.crm.bundle.service.CRMSubscriberBucketProfile;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.PaymentPlanSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * Validates and executes the postpaid to prepaid conversion.
 *
 * @author arturo.medina@redknee.com
 */
public class PostpaidPrepaidConversion extends AbstractSubscriberConversion
{

    /**
     * Create a new instance of <code>PostpaidPrepaidConversion</code>.
     */
    public PostpaidPrepaidConversion()
    {
        super();
        this.from_ = SubscriberTypeEnum.POSTPAID;
        this.to_ = SubscriberTypeEnum.PREPAID;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Subscriber executeConvertion(final Context ctx, final Subscriber prevSubscriber,
        final Subscriber currentSubscriber, final Home delegate) throws HomeException
    {
        /*
         * On this method we are not going to need the delegate home because we need to
         * update the old subscriber to deactive (store) and we are going to create a new
         * one and we need the whole home pipeline.
         */
        final Home subsHome = (Home) ctx.get(SubscriberHome.class);
        Subscriber newSub = currentSubscriber;

        CRMSubscriberBucketProfile service = (CRMSubscriberBucketProfile)ctx.get(CRMSubscriberBucketProfile.class);

        if (subsHome != null)
        {

            Collection buckets = new ArrayList();
            try 
            {
                buckets = service.getBuckets(ctx, prevSubscriber.getMSISDN(), (int) newSub.getSubscriptionType()).selectAll();
            Iterator it = buckets.iterator();
            while (it.hasNext())
            {
                    SubscriberBucket bucket =  (SubscriberBucket) it.next();
                if (bucket.getUnitType() != UnitTypeEnum.POINTS)
                {
                    it.remove();
                }
            }
            }
            catch (BundleManagerException e)
            {
                HomeException ex = new HomeException("Conversion failed due to postpaid subscriber not being able to remove buckets: " + e.getMessage(), e);
                throw ex;
            }

            /*
             * Store conversion in context to refund old postpaid sub's services
             */
            ctx.put(Common.POSTPAID_PREPAID_CONVERSION_SUBCRIBER, currentSubscriber);
            setAuxServices(ctx, prevSubscriber);
            prevSubscriber.setState(SubscriberStateEnum.INACTIVE);
            Subscriber deactivatedSub = null;
            try
            {
                deactivatedSub = (Subscriber) subsHome.store(ctx, prevSubscriber);
            }
            catch (final Exception e)
            {
                final HomeException ex = new HomeException(
                    "Conversion failed due to Postpaid subscriber not being deactivated properly");
                ex.initCause(e);
                throw ex;
            }

            if (deactivatedSub == null || deactivatedSub.getState() != SubscriberStateEnum.INACTIVE)
            {
                final HomeException ex = new HomeException(
                    "Conversion failed due to Postpaid subscriber not being deactivated properly");
                throw ex;
            }

            convertMSISDNToPrepaid(ctx, prevSubscriber);

            /*
             * Resetting the deposit and credit limit since for Prepaid are zero and the
             * Deposit release has been made
             */
            newSub.setDeposit(0);
            newSub.setCreditLimit(0);
            newSub.setStartDate(new Date());

            /*
             * Remove the postpaid -> prepaid sub so the new sub doesn't know about
             * conversion so not to interfere with create We reset their services so that
             * the service HLR commands are sent.
             */
            ctx.put(Common.POSTPAID_PREPAID_CONVERSION_SUBCRIBER, null);
            currentSubscriber.setUpdateReason(UpdateReasonEnum.CONVERSION);
            
            /*
             * Remove the bundles from the subscriber, since all the old bundles it had
             * were postpaid bundles, and not prepaid bundles. The bundles on BM should
             * have been removed as part of the postpaid deactivation part of the
             * conversion
             */
            currentSubscriber.resetBundles();

            /*
             * remove reference to look up old sub thats in current context that was put
             * into the context as part of the store operation in converting a sub
             */
            final Context subCtx = ctx.createSubContext();
            subCtx.put(Lookup.OLDSUBSCRIBER, null);
            subCtx.put(Lookup.POSTPAID_PREPAID_CONVERSION, Boolean.TRUE);

            final CRMSpid sp = SpidSupport.getCRMSpid(ctx, currentSubscriber.getSpid());
            if (sp.isAllowToSpecifySubscriberId())
            {
                final String id = (String) ctx.get(Common.CONVERSION_SUBCRIBER_ID);
                if (id != null)
                {
                    currentSubscriber.setId(id);
                }
                else
                {
                    currentSubscriber.setId(SubscriberSupport.NULLID);
                }
            }
            
            newSub = (Subscriber) subsHome.create(subCtx, currentSubscriber);

            /*
             * All the subscriber-auxiliary services of the previous subscriber should be
             * moved to current subscriber, we need a direct connection to
             * SubscriberAuxliaryServcice home (We don't need any side effects given by
             * the pipeline)
             */
            try
            {
                updateSubAuxSvcAssociations(ctx, prevSubscriber, newSub);
            }
            catch (final Exception e)
            {
                final String msg = "Error occured while trnasferring the subscriberauxiliary servcies to "
                    + "new subscriber after conversion Subscriber-ID:" + prevSubscriber.getId() + " to "
                    + newSub.getId() + "\nRoot cause:" + e.getMessage();
                new MajorLogMsg(this, msg, e).log(ctx);
                throw new HomeException(msg);
            }

            Iterator it = buckets.iterator();
            while (it.hasNext())
            {
                SubscriberBucket bucket = (SubscriberBucket) it.next();
                try
                {
                    service.increaseBalanceLimit(ctx, bucket.getMsisdn(), (int) newSub.getSubscriptionType(), bucket.getSpid(),
                        bucket.getBundleId(), bucket.getRegularBal().getPersonalBalance());
                }
                catch (final Exception e)
                {
                    new MajorLogMsg(this, "Error occured while getting auxiliary services for subscriber:"
                        + newSub.getId(), e).log(ctx);
                }
            }

            newSub.setState(SubscriberStateEnum.ACTIVE);
            newSub = (Subscriber) subsHome.store(subCtx, newSub);

        }

        return newSub;
    }


    /**
     * Converts all the MSISDNs of the subscriber to prepaid.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being converted.
     * @throws HomeException
     *             Thrown if there are problems converting the MSISDN.
     */
    private void convertMSISDNToPrepaid(final Context ctx, final Subscriber sub) throws HomeException
    {
        MsisdnSupport.setMsisdnType(ctx, sub.getMSISDN(), SubscriberTypeEnum.PREPAID);
        AdditionalMsisdnAuxiliaryServiceSupport.convertMsisdnToPrepaid(ctx, sub);
    }


    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        if (obj instanceof Subscriber)
        {
            final Subscriber sub = (Subscriber) obj;
            final Subscriber newSub = (Subscriber) ctx.get(Common.POSTPAID_PREPAID_CONVERSION_SUBCRIBER);

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

            if (sub.getState() != SubscriberStateEnum.PENDING || sub.getState() != SubscriberStateEnum.INACTIVE)
            {
                if (acct.getState() != AccountStateEnum.INACTIVE && acct.getSystemType() != SubscriberTypeEnum.HYBRID)
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
                throw new IllegalStateException("System error: conversion Sub not found in context.");
            }

            /*
             * TT5122928672: In the case where the subscriber is the last postpaid sub in
             * the account on Payment Plan, the move shall be prevented and an error
             * message displayed.
             */
            final Account parentAccount = getResponsibleParentAccount(ctx, acct);
            if (PaymentPlanSupportHelper.get(ctx).isEnabled(ctx)
                && PaymentPlanSupportHelper.get(ctx).isValidPaymentPlan(ctx, parentAccount.getPaymentPlan()))
            {
                if (!hasMoreThanOnePostpaidSubscriber(ctx, parentAccount))
                {
                    final String msg = "Cannot perform the subscriber conversion from Postpaid to Prepaid, "
                        + " since this subscriber is the last postpaid subscriber in an account enrolled"
                        + " in Payment Plan. The root account enrolled in Payment Plan is # " + parentAccount.getBAN()
                        + ".";
                    throw new IllegalStateException(msg);
                }
            }
        }
    }


    /**
     * Updates the subscriber auxiliary services to new subscriber ID.
     *
     * @param ctx
     *            The operating context.
     * @param oldSub
     *            The old subscriber.
     * @param newSub
     *            The new subscriber.
     * @throws HomeException
     *             Thrown if there are problems updating the subscriber auxiliary service
     *             associations.
     */
    private void updateSubAuxSvcAssociations(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
        throws HomeException
    {
        final String oldId = oldSub.getId();
        final String newId = newSub.getId();
        SubscriberAuxiliaryServiceSupport.moveSubscriber(ctx, oldId, newId);
    }


    /**
     * Sets the auxiliary services of the subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being updated.
     */
    private void setAuxServices(final Context ctx, final Subscriber sub)
    {
        List currentSubAuxSvcs = null;
        List futureSubAuxSvcs = null;
        try
        {
            currentSubAuxSvcs = new ArrayList(SubscriberAuxiliaryServiceSupport.getActiveSubscriberAuxiliaryServices(ctx, sub,
                new Date()));
        }
        catch (final HomeException he)
        {
            new MajorLogMsg(this, "Error occured while getting current auxiliary services for subscriber:"
                + sub.getId(), he).log(ctx);
        }
        try
        {
            futureSubAuxSvcs = new ArrayList(SubscriberAuxiliaryServiceSupport.getSubscriberFutureProvisionAuxSvcs(ctx, sub,
                new Date()));
        }
        catch (final HomeException he)
        {
            new MajorLogMsg(this, "Error occured while getting current auxiliary services for subscriber:"
                + sub.getId(), he).log(ctx);
        }

        if (currentSubAuxSvcs == null)
        {
            currentSubAuxSvcs = new ArrayList();
        }
        if (futureSubAuxSvcs == null)
        {
            futureSubAuxSvcs = new ArrayList();
        }

        sub.setAuxiliaryServices(currentSubAuxSvcs);
        sub.setFutureAuxiliaryServices(futureSubAuxSvcs);
    }




    /**
     * Returns the first responsible parent account of the the given account. If this
     * account is the root account or responsible itself, then the given account is
     * returned.
     *
     * @param ctx
     *            The operating context.
     * @param account
     *            The account to look up.
     * @return The first responsible parent account of the given account.
     * @throws IllegalStateException
     *             Thrown if there are any problems retrieving the account hierarchy.
     */
    private Account getResponsibleParentAccount(final Context ctx, final Account account) throws IllegalStateException
    {
        Account parentAccount = null;
        try
        {
            parentAccount = account.getResponsibleParentAccount(ctx);
        }
        catch (final HomeException he)
        {
            final IllegalStateException e = new IllegalStateException(
                "Error occurred while retrieving account heirarchy.");
            e.initCause(he);
            throw e;
        }

        return parentAccount;
    }


    /**
     * Returns TRUE if there is more than 1 active postpaid subscriber in the account
     * hierarchy. <p/> Since Conversion is invoked through the GUI, we have the challenge
     * that the SubscriberHome in the context given is already filtered according to the
     * BAN of the subscriber being Stored. A Subscriber Home thus filtered is of no use to
     * us, since we want to look for postpaid subscribers in all of the non-responsible
     * sub-accounts of the given account. <p/> To solve this problem, a service
     * (AccountHierarchyService) was installed to do the check for us.
     *
     * @param ctx
     *            The operating context.
     * @param parentAccount
     *            Parent account.
     * @return TRUE if there is more than 1 active postpaid subscriber in the account
     *         hierarchy
     */
    private boolean hasMoreThanOnePostpaidSubscriber(final Context ctx, final Account parentAccount)
    {
        return AccountSupport.hasMoreThanOnePostpaidSubscriber(ctx, parentAccount);
    }
}
