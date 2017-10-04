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
package com.trilogy.app.crm.move.support;

import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.VpnAuxiliarySubscriber;
import com.trilogy.app.crm.bean.VpnAuxiliarySubscriberHome;
import com.trilogy.app.crm.bean.VpnAuxiliarySubscriberXInfo;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.MoveWarningException;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.AccountMoveRequestXInfo;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequestXInfo;
import com.trilogy.app.crm.subscriber.charge.AbstractSubscriberCharger;
import com.trilogy.app.crm.subscriber.charge.VPNMoveSubscriberCharger;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;


/**
 * This class handles scenarios related to moving subscriptions in VPN accounts.
 *
 * Only validation required to perform its task without exception is performed.
 * Business logic validation must be performed by the caller.
 * 
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class VPNSubscriptionMoveSupport<MR extends MoveRequest>
{
    public VPNSubscriptionMoveSupport(Context ctx, Account oldRootAccount, Account newRootAccount, Subscriber newVPNSubscription, boolean generateCharges)
    {
        init(ctx, oldRootAccount, newRootAccount, newVPNSubscription, generateCharges);
    }
    
    public VPNSubscriptionMoveSupport(Context ctx, MR request, boolean generateCharges)
    {
        Account oldAccount = null;
        Account newParentAccount = null;

        if (request instanceof AccountMoveRequest)
        {
            AccountMoveRequest accountRequest = (AccountMoveRequest) request;
            oldAccount = accountRequest.getOldAccount(ctx);
            newParentAccount = accountRequest.getNewParentAccount(ctx);
        }
        else if (request instanceof SubscriptionMoveRequest)
        {
            SubscriptionMoveRequest subRequest = (SubscriptionMoveRequest) request;
            oldAccount = subRequest.getOldAccount(ctx);
            newParentAccount = subRequest.getNewAccount(ctx);
        }

        Account oldRootAccount = null;
        if (oldAccount != null)
        {
            try
            {
                oldRootAccount = oldAccount.getRootAccount(ctx);
            }
            catch (HomeException e)
            {
                new MajorLogMsg(this, "Error retrieving root account for old account (BAN=" + oldAccount.getBAN() + ").", e).log(ctx);
            }
        }

        Account newRootAccount = null;
        Subscriber newVPNSubscription = null;
        if (newParentAccount != null)
        {
            try
            {
                newRootAccount = newParentAccount.getRootAccount(ctx);
            }
            catch (HomeException e)
            {
                new MajorLogMsg(this, "Error retrieving root account for new account (BAN=" + newParentAccount.getBAN() + ").", e).log(ctx);
            }

            if (newRootAccount != null
                    && newRootAccount.isMom(ctx))
            {
                String groupVpnMsisdn = newRootAccount.getVpnMSISDN();
                if (groupVpnMsisdn != null && groupVpnMsisdn.trim().length() > 0)
                {
                    try
                    {
                        newVPNSubscription  = SubscriberSupport.lookupSubscriberForMSISDN(ctx, groupVpnMsisdn);
                    }
                    catch (HomeException e)
                    {
                        new MajorLogMsg(this, "Error looking up group VPN subscription (MSISDN=" + groupVpnMsisdn + ") for account " + newRootAccount_.getBAN(), e).log(ctx);
                    }
                }
            }
        }

        init(ctx, oldRootAccount, newRootAccount, newVPNSubscription, generateCharges);
    }

    private void init(Context ctx, Account oldRootAccount, Account newRootAccount, Subscriber newVPNSubscription, boolean generateCharges)
    {
        oldRootAccount_ = oldRootAccount;
        newRootAccount_ = newRootAccount;
        newVpnSub_ = newVPNSubscription;
        generateCharges_ = generateCharges;

        if (oldRootAccount_ == null)
        {
            // This is an undefined case.  Assume it is a VPN account change since
            // we can't determine whether or not it is.  This should trigger validation
            // failure due to root account not existing.
            isVPNAccountChange_ = true;
        }
        else
        {
            final boolean sameRootAccount = newRootAccount_ != null
                                                && newRootAccount_.getBAN().equals(oldRootAccount_.getBAN());
            if (!sameRootAccount)
            {
                final boolean isOldRootVPN = oldRootAccount_.isMom(ctx);
                final boolean isNewRootVPN = newRootAccount_ != null && newRootAccount_.isMom(ctx);

                isVPNAccountChange_ = isOldRootVPN || isNewRootVPN;
            }
        }
    }

    public void validate(Context ctx, MR request, ExceptionListener el) throws IllegalStateException
    {
        Account oldAccount = null;
        Account newAccount = null;
        PropertyInfo oldBANProperty = null;
        PropertyInfo newBANProperty = null;

        if (request instanceof AccountMoveRequest)
        {
            AccountMoveRequest accountRequest = (AccountMoveRequest) request;

            oldAccount = AccountMoveValidationSupport.validateOldAccountExists(ctx, accountRequest, el);
            newAccount = AccountMoveValidationSupport.validateNewAccountExists(ctx, accountRequest, el);
            oldBANProperty = AccountMoveRequestXInfo.EXISTING_BAN;
            newBANProperty = AccountMoveRequestXInfo.NEW_BAN;
        }
        else if (request instanceof SubscriptionMoveRequest)
        {
            SubscriptionMoveRequest subRequest = (SubscriptionMoveRequest) request;

            oldAccount = SubscriptionMoveValidationSupport.validateOldAccountExists(ctx, subRequest, el);
            newAccount = SubscriptionMoveValidationSupport.validateNewAccountExists(ctx, subRequest, el);
            oldBANProperty = SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID;
            newBANProperty = SubscriptionMoveRequestXInfo.NEW_BAN;
        }

        if (newAccount != null)
        {
            validateNewAccount(ctx, newAccount, newBANProperty, el);
        }

        if (oldAccount != null)
        {
            validateOldAccount(ctx, oldAccount, oldBANProperty, el);
        }
    }

    private void validateNewAccount(Context ctx, 
            Account newAccount, 
            PropertyInfo newAccountProperty, 
            ExceptionListener cise)
    {
        if (newRootAccount_ == null)
        {
            if (newAccount.getParentBAN() != null
                    && newAccount.getParentBAN().length() > 0)
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        newAccountProperty, 
                        "Root account for new account (BAN=" + newAccount.getBAN() + ") does not exist."));
            }
        }
        else  if (newVpnSub_ == null
                && newRootAccount_.isMom(ctx))
        {
            String groupVpnMsisdn = newRootAccount_.getVpnMSISDN();
            if (groupVpnMsisdn != null && groupVpnMsisdn.trim().length() > 0)
            {
                cise.thrown(new IllegalStateException("Group VPN subscription (MSISDN=" + groupVpnMsisdn + ") for account " + newRootAccount_.getBAN() + " does not exist.", null));
            }
        }
    }

    private void validateOldAccount(Context ctx, 
            Account oldAccount, 
            PropertyInfo oldAccountProperty, 
            ExceptionListener cise)
    {
        if (oldRootAccount_ == null)
        {
            cise.thrown(new IllegalPropertyArgumentException(
                    oldAccountProperty, 
                    "Root account for old account (BAN=" + oldAccount.getBAN() + ") does not exist."));
        }
    }

    public boolean isVPNAccountChange()
    {
        return isVPNAccountChange_;
    }
    
    public void addVPNToSubscription(Context ctx, MR request, Subscriber sub)
    {
        if (newVpnSub_ != null
                && newRootAccount_ != null
                && isVPNAccountChange())
        {
            LogMsg pmSuccess = new PMLogMsg(this.getClass().getName(), "addVPNToSubscription()");
            LogMsg pmFail = new PMLogMsg(this.getClass().getName(), "addVPNToSubscription()-Exception");

            CRMSpid spid = null;
            
            try
            {
                spid = SpidSupport.getCRMSpid(ctx, sub.getSpid());
            }
            catch (HomeException e)
            {
                new DebugLogMsg(this, "Unable to retrieve spid " + sub.getSpid() + " for subscriber " + sub.getId() + ": " + e.getMessage(), e).log(ctx);
            }
            
            try
            {
                // Create new auxiliary service association
                new DebugLogMsg(this, "Adding VPN for subscription " + sub.getId() + "...", null).log(ctx);
                Home vpnSubHome = (Home) ctx.get(VpnAuxiliarySubscriberHome.class);
                vpnSubHome = vpnSubHome.where(ctx, new EQ(VpnAuxiliarySubscriberXInfo.SUBSCRIBER_ID, newVpnSub_.getId()));
                vpnSubHome.forEach(
                        ctx, 
                        new MoveVPNAddVisitor(newRootAccount_, sub));
                new InfoLogMsg(this, "VPN successfully added to subscription " + sub.getId(), null).log(ctx);
                if (generateCharges_ || (sub.isPostpaid()
                        && spid != null && spid.isCarryOverBalanceOnMove()))
                {
                    VPNMoveSubscriberCharger charger = new VPNMoveSubscriberCharger(ctx, sub, sub);
                    ctx.put(Subscriber.class, sub);
                    ctx.put(AbstractSubscriberCharger.class, charger);
                    charger.charge(ctx, null);
                }
                pmSuccess.log(ctx);
            }
            catch (final HomeException e)
            {
                String msg = "Error occurred adding VPN for subscription " + sub.getId();
                new MinorLogMsg(this, msg, e).log(ctx);
                request.reportWarning(ctx, new MoveWarningException(request, msg, e));
                pmFail.log(ctx);
            }
        }
    }

    public void removeVPNFromSubscription(Context ctx, MR request, Subscriber sub)
    {
        if (isVPNAccountChange())
        {
            LogMsg pmSuccess = new PMLogMsg(this.getClass().getName(), "removeVPNFromSubscription()");
            LogMsg pmFail = new PMLogMsg(this.getClass().getName(), "removeVPNFromSubscription()-Exception");

            CRMSpid spid = null;
            
            try
            {
                spid = SpidSupport.getCRMSpid(ctx, sub.getSpid());
            }
            catch (HomeException e)
            {
                new DebugLogMsg(this, "Unable to retrieve spid " + sub.getSpid() + " for subscriber " + sub.getId() + ": " + e.getMessage(), e).log(ctx);
            }
            
            try
            {
                new DebugLogMsg(this, "Removing VPN from subscription " + sub.getId() + "...", null).log(ctx);
                if (generateCharges_ || (sub.isPostpaid()
                        && spid != null && spid.isCarryOverBalanceOnMove()))
                {
                    VPNMoveSubscriberCharger charger = new VPNMoveSubscriberCharger(ctx, sub, sub);
                    ctx.put(Subscriber.class, sub);
                    ctx.put(AbstractSubscriberCharger.class, charger);
                    charger.refund(ctx, null);
                }
                Home vpnAuxHome = (Home) ctx.get(VpnAuxiliarySubscriberHome.class);
                vpnAuxHome = vpnAuxHome.where(ctx, new EQ(VpnAuxiliarySubscriberXInfo.SUBSCRIBER_ID, sub.getId()));
                vpnAuxHome.forEach(
                        ctx, 
                        new MoveVPNDeleteVisitor(oldRootAccount_, sub));
                new InfoLogMsg(this, "VPN successfully removed from subscription " + sub.getId(), null).log(ctx);
                pmSuccess.log(ctx);
            }
            catch (final HomeException e)
            {
                String msg = "Error occurred removing VPN from subscription " + sub.getId();
                new MinorLogMsg(this, msg, e).log(ctx);
                request.reportWarning(ctx, new MoveWarningException(request, msg, e));
                pmFail.log(ctx);
            }
        }
    }

    /**
     * @return 
     */
    public Account getOldRootAccount()
    {
        return oldRootAccount_;
    }


    /**
     * @return 
     */
    public Account getNewRootAccount()
    {
        return newRootAccount_;
    }


    /**
     * @return 
     */
    public Subscriber getNewVPNSubscription()
    {
        return newVpnSub_;
    }



    private Account oldRootAccount_;
    private Account newRootAccount_;
    private Subscriber newVpnSub_;
    private boolean isVPNAccountChange_ = false;
    private boolean generateCharges_ = false;

    /**
     * A visitor which add VPN relationships to the new subscriber.
     *
     * @author cindy.wong@redknee.com
     * @author aaron.gourley@redknee.com
     */
    private static class MoveVPNAddVisitor implements Visitor
    {
        private static final long serialVersionUID = -5127774860790880526L;


        /**
         * Creates a new <code>AddVPNVisitor</code>.
         *
         * @param account
         *            The new account of the subscriber
         * @param subscriber
         *            The subscriber which should have VPN associated with it
         */
        private MoveVPNAddVisitor(final Account vpnAccount, final Subscriber subscriber)
        {
            this.id_ = subscriber.getId();
            this.vpnAccountBan_ = vpnAccount.getBAN();
            this.sub_ = subscriber;
        }


        /**
         * {@inheritDoc}
         */
        public void visit(final Context context, final Object object) throws AgentException, AbortVisitException
        {
            final String subId = this.id_;

            try
            {
                final VpnAuxiliarySubscriber vpnAuxiliarySubscriber = (VpnAuxiliarySubscriber) object;

                SubscriberAuxiliaryService subAuxSvc = HomeSupportHelper.get(context).findBean(
                        context, 
                        SubscriberAuxiliaryService.class, 
                        vpnAuxiliarySubscriber.getSubcriberAuxiliaryId());
                subAuxSvc.setIdentifier(-1);
                subAuxSvc.setStartDate(CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(new Date()));
                subAuxSvc.setSubscriberIdentifier(subId);

                new DebugLogMsg(this,
                        "Adding VPN Auxiliary Service " + subAuxSvc.getAuxiliaryServiceIdentifier()
                        + " to subscriber " + subId + "...", null).log(context);
                subAuxSvc = HomeSupportHelper.get(context).createBean(context, subAuxSvc);
                if (!sub_.getAuxiliaryServices(context).contains(sub_.getAuxiliaryServices(context)))
                {
                    sub_.getAuxiliaryServices(context).add(subAuxSvc);
                }
                new InfoLogMsg(this,
                        "VPN Auxiliary Service " + subAuxSvc.getAuxiliaryServiceIdentifier()
                        + " added to subscriber " + subId, null).log(context);
            }
            catch (final Exception e)
            {
                final String msg = "Failed to add VPN Auxiliary Service to subscriber " + subId;
                new MinorLogMsg(this, msg, e).log(context);
                ExceptionListener el = (ExceptionListener) context.get(ExceptionListener.class);
                if (el != null)
                {
                    el.thrown(new Exception(msg, e));
                }
            }
        }

        /**
         * The root VPN account of the new subscriber.
         */
        private final String vpnAccountBan_;

        /**
         * The subscriber's ID.
         */
        private final String id_;
        
        private final Subscriber sub_;
    }

    /**
     * A visitor which deletes VPN relationships from the old subscriber.
     *
     * @author cindy.wong@redknee.com
     * @author aaron.gourley@redknee.com
     */
    private static class MoveVPNDeleteVisitor implements Visitor
    {
        private static final long serialVersionUID = 9210941797840698060L;


        /**
         * Create a new <code>DeleteVPNVisitor</code>.
         *
         * @param vpnAccount
         *            The root VPN account
         * @param subscriber
         *            The subscriber whose VPN relationships are being deleted
         */
        private MoveVPNDeleteVisitor(final Account vpnAccount, final Subscriber subscriber)
        {
            this.id_ = subscriber.getId();
            this.vpnAccountBan_ = vpnAccount.getBAN();
        }


        /**
         * {@inheritDoc}
         */
        public void visit(final Context context, final Object object)
        {
            final String subId = this.id_;

            final VpnAuxiliarySubscriber vpnSub = (VpnAuxiliarySubscriber) object;
            try
            {
                final SubscriberAuxiliaryService subAuxSvc = HomeSupportHelper.get(context).findBean(
                        context, 
                        SubscriberAuxiliaryService.class, 
                        vpnSub.getSubcriberAuxiliaryId());
                if (subAuxSvc != null)
                {
                    new DebugLogMsg(this,
                            "Remvoing VPN Auxiliary Service " + subAuxSvc.getAuxiliaryServiceIdentifier()
                            + " from subscriber " + subId + "...", null).log(context);
                    HomeSupportHelper.get(context).removeBean(context, subAuxSvc);
                    new InfoLogMsg(this,
                            "VPN Auxiliary Service " + subAuxSvc.getAuxiliaryServiceIdentifier()
                            + " removed from subscriber " + subId, null).log(context);
                }
            }
            catch (final Exception e)
            {
                final String msg = "Failed to remove VPN Auxiliary Service from subscriber " + subId;
                new MinorLogMsg(this, msg, e).log(context);
                ExceptionListener el = (ExceptionListener) context.get(ExceptionListener.class);
                if (el != null)
                {
                    el.thrown(new Exception(msg, e));
                }
            }
        }

        /**
         * The subscriber's ID.
         */
        private final String id_;

        /**
         * The root VPN account's BAN.
         */
        private final String vpnAccountBan_;
    }
}