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
package com.trilogy.app.crm.home;


import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistory;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryXInfo;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CallDetailSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * Adds support for setting the subscriber identifier of beans based on the
 * values of other properties.  The subsrciber identifier will only be set, if it
 * not already set, at creation time (no change are made during updates).
 * Currently supported beans:
 *
 *   <li>CallDetail - based on chargedMSISDN.</li>
 *   <li>Transaction - based on MSISDN.</li>
 *
 * @author gary.anderson@redknee.com
 */
public class SubscriberIdentifierSettingHome
    extends HomeProxy
{

    /**
     * Creates a new SubscriberIdentifierSettingHome.
     *
     * @param delegate The home to delegate to.
     */
    public SubscriberIdentifierSettingHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj)
        throws HomeException
    {
        if (obj instanceof CallDetail)
        {
            setCallDetailSubscriberID(ctx, (CallDetail)obj);
        }
        else if (obj instanceof Transaction)
        {
            setTransactionSubscriberID(ctx, (Transaction)obj);
        }

        return super.create(ctx, obj);
    }


    /**
     * Looks-up the Msisdn for the given mobile number.
     *
     * @param ctx the operating context
     * @param msisdn The mobile number.
     * @return The Msisdn.
     *
     * @throws HomeException Thrown if there is a problem looking up the
     * given mobile number.
     */
    private Msisdn lookupMsisdn(final Context ctx, final String msisdn)
        throws HomeException
    {
        final Home home = (Home)ctx.get(MsisdnHome.class);
        if (home == null)
        {
            throw new HomeException(
                "Could not find MsisdnHome in context.");
        }

        final Msisdn msisdnObject = (Msisdn)home.find(ctx, msisdn);
        if (msisdnObject == null)
        {
            throw new HomeException(
                "Could not find Msisdn for \"" + msisdn + "\".");
        }

        return msisdnObject;
    }


    /**
     * Sets the subscriberID of the given CallDetail based on the value of its
     * chargedMSISDN.  Note: this method only changes the value in the bean, not
     * in the home.
     *
     * @param ctx the operating context
     * @param callDetail The CallDetail to update.
     * @throws HomeException Thrown if there is a problem looking up the
     * given mobile number.
     */
    private void setCallDetailSubscriberID(final Context ctx, final CallDetail callDetail)
        throws HomeException
    {
        if (isBlank(callDetail.getSubscriberID()))
        {
            final Msisdn msisdn = lookupMsisdn(ctx, callDetail.getChargedMSISDN());
            callDetail.setSubscriberID(msisdn.getSubscriberID(ctx, callDetail.getTranDate()));
        }
        CallDetailSupportHelper.get(ctx).debugMsg(SubscriberIdentifierSettingHome.class, callDetail, " Setting subscriber id on the call detail", ctx);
    }


    /**
     * Sets the subscriberID of the given Transaction based on the value of its
     * MSISDN.  Note: this method only changes the value in the bean, not in the
     * home.
     *
     * @param ctx the operating context
     * @param transaction The Transaction to update.
     * @throws HomeException Thrown if there is a problem looking up the
     * given mobile number.
     */
    private void setTransactionSubscriberID(final Context ctx, final Transaction transaction)
        throws HomeException
    {
        if (!CoreTransactionSupportHelper.get(ctx).isPayment(ctx, transaction) && isBlank(transaction.getSubscriberID()))
        {
            setSubscriberID(ctx, transaction);
        }
        else if (isBlank(transaction.getSubscriberID()))
        {
            /*
             * Checks if it's safe to set the subcriber id by checking if there is
             * only one subcriber on the MSISDN history
             */
            long count = HomeSupportHelper.get(ctx).getBeanCount(ctx,
                    MsisdnMgmtHistory.class,
                    new EQ(MsisdnMgmtHistoryXInfo.TERMINAL_ID, transaction.getMSISDN()));
            
            if (count == 1)
            {
                setSubscriberID(ctx, transaction);
            }
            else
            {
                String uniqueSubscriberId = retrieveSubscriberId(ctx, transaction);
                
                
                if (uniqueSubscriberId!=null)
                {
                    transaction.setSubscriberID(uniqueSubscriberId);
                }
                else
                {
                    LogSupport.debug(ctx, this, "Number of instances in the MSISDN history for MSISDN "
                        + transaction.getMSISDN()
                        + " = "
                        + count);
                }
            }
        }
    }
    
    private String retrieveSubscriberId(Context ctx, Transaction transaction)
    {
        String uniqueSubscriberId = null;
        try
        {
            if (transaction.getBAN()!=null && !transaction.getBAN().isEmpty())
            {
                Account account = (Account) ctx.get(Account.class);
                if (account==null || !account.getBAN().equals(transaction.getBAN()))
                {
                    account = AccountSupport.getAccount(ctx, transaction.getBAN());
                }
                
                if (account!=null)
                {
                    Collection<Subscriber> subscribers = account.getSubscribers(ctx);
                    int subCount = 0;
                    for (Subscriber sub : subscribers)
                    {
                        if (sub.getMSISDN().equals(transaction.getMSISDN()))
                        {
                            subCount++;
                            uniqueSubscriberId = sub.getId();
                        }
                        if (subCount>1)
                        {
                            uniqueSubscriberId = null;
                            break;
                        }
                    }
                }
            }    
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to retrieve account for BAN " + transaction.getBAN() + ": " + e.getMessage(), e);
        }
        return uniqueSubscriberId;
    }

    /**
     * For performance, checks first if the subscriber is in the context and
     * matches the MSISDN otherwise, it looks up at the subscriber id
     * on the MSISDN history.
     * @param ctx the operating context
     * @param transaction the transaction to set the subscriber id
     * @throws HomeException if sonething is wrong on the lookup
     */
    private void setSubscriberID(final Context ctx, final Transaction transaction)
        throws HomeException
    {
        final Msisdn msisdn = lookupMsisdn(ctx, transaction.getMSISDN());
        final Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
        if (sub == null)
        {
            transaction.setSubscriberID(msisdn.getSubscriberID(ctx, transaction.getTransDate()));
        }
        else
        {
            if (sub.getMSISDN().equals(transaction.getMSISDN()))
            {
                transaction.setSubscriberID(sub.getId());
            }
            else
            {
                transaction.setSubscriberID(msisdn.getSubscriberID(ctx, transaction.getTransDate()));
            }
        }
    }

    /**
     * Indicates whether or not the given string is null or blank.
     *
     * @param value The string value to check.
     * @return True if the given value is null or blank; false otherwise.
     */
    boolean isBlank(final String value)
    {
        return (value == null) || (value.trim().length() == 0);
    }

    /**
     * The serial version id
     */
    private static final long serialVersionUID = -3178677215634816397L;

} // class
