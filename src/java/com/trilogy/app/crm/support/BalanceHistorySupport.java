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
package com.trilogy.app.crm.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.bean.BalanceHistory;
import com.trilogy.app.crm.bean.BalanceHistoryTypeEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallDetailXInfo;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Support Functions for Subscriber Balance History
 * 
 * @author angie.li@redknee.com
 */
public class BalanceHistorySupport
{
    /**
     * Query for all Transactions and Call Details within the date range (startDate, endDate),
     * convert to a Balance History bean and merge the results.
     *
     * @param ctx the operating context
     * @param subscriberID subscriber to search
     * @param startDate beginning of Date range
     * @param endDate end of Date range
     * @param limit the maximum number of records
     * @return Balance History result collection
     */
    public static Collection<BalanceHistory> mergeSelectBySubscriberID(final Context ctx, final String subscriberID,
            final Date startDate, final Date endDate, final int limit)
    {
        final List<BalanceHistory> result = new ArrayList<BalanceHistory>(limit);
        Collection<Transaction> trans;
        Collection<CallDetail> callDetails;

        //Collect all relevant Transactions 
        try
        {
            final And condition = new And();
            condition.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, subscriberID));
            condition.add(new GTE(TransactionXInfo.RECEIVE_DATE, startDate));
            condition.add(new LTE(TransactionXInfo.RECEIVE_DATE, endDate));

            trans = HomeSupportHelper.get(ctx).getBeans(
                    ctx,
                    Transaction.class,
                    condition,
                    limit,
                    true,
                    TransactionXInfo.RECEIVE_DATE);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(BalanceHistorySupport.class, "Unable to retreive Transactions for Subscription ["
                    + subscriberID + "] Error: " + e.getMessage(), e).log(ctx);
            trans = new ArrayList<Transaction>();
        }

        //Collect all relevant Call Details
        try
        {
            final And condition = new And();
            condition.add(new EQ(CallDetailXInfo.SUBSCRIBER_ID, subscriberID));
            condition.add(new EQ(CallDetailXInfo.VPN__DISCOUNT__ON, 0));
            condition.add(new GTE(CallDetailXInfo.POSTED_DATE, startDate));
            condition.add(new LTE(CallDetailXInfo.POSTED_DATE, endDate));

            callDetails = HomeSupportHelper.get(ctx).getBeans(
                    ctx,
                    CallDetail.class,
                    condition,
                    limit,
                    true,
                    CallDetailXInfo.POSTED_DATE);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(BalanceHistorySupport.class, "Unable to retreive CallDetails for Subscription ["
                    + subscriberID + "] Error: " + e.getMessage(), e).log(ctx);
            callDetails = new ArrayList<CallDetail>();
        }

        final Iterator<CallDetail> cdIt = callDetails.iterator();
        final Iterator<Transaction> trIt = trans.iterator();
        BalanceHistory cdHist = null;
        BalanceHistory trHist = null;
        for (int i = 0; i < limit && (cdIt.hasNext() || trIt.hasNext()); i++)
        {
            if (cdHist == null && cdIt.hasNext())
            {
                cdHist = BalanceHistorySupport.convertCallDetailToHistory(ctx, cdIt.next());
            }

            if (trHist == null && trIt.hasNext())
            {
                trHist = BalanceHistorySupport.convertTransactionToHistory(ctx, trIt.next());
            }

            if (cdHist == null && trHist != null)
            {
                result.add(trHist);
                trHist = null;
            }
            else if (trHist == null || trHist.getSortDate().after(cdHist.getSortDate()))
            {
                result.add(cdHist);
                cdHist = null;
            }
            else
            {
                result.add(trHist);
                trHist = null;
            }
        }

        return result;
    }


    public static BalanceHistory convertTransactionToHistory(final Context ctx, final Transaction trans)
    {
        final BalanceHistory hist = new BalanceHistory();
        try
        {
            /* For Transactions, we will sort by the Receive Date, unless the transaction 
             * was processed through an ER.  The reason for this is that, when generating 
             * transactions in quick succession, the transaction date appears to be the same, 
             * but actually differ by a few hundred milliseconds.  Sorting by transaction date 
             * then produces non-sequential running balances. */
            if (trans.isFromVRAPoller())
            {
                hist.setSortDate(trans.getTransDate());
            }
            else
            {
                hist.setSortDate(trans.getReceiveDate());
            }
            hist.setKeyDate(trans.getTransDate());
            hist.setId((Long.valueOf(trans.getReceiptNum())).toString());
            hist.setSpid(trans.getSpid());
            hist.setCallAdjustmentType(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeNameDescription(ctx,
                    trans.getAdjustmentType()));
            hist.setCallAdjustmentTypeCode(trans.getAdjustmentType());
            hist.setBAN(trans.getAcctNum());
            hist.setSubscriberID(trans.getSubscriberID());
            hist.setCharge(trans.getAmount());
            hist.setBalance(trans.getBalance());
            
        }
        catch (Exception e)
        {
            new MinorLogMsg(BalanceHistorySupport.class, "Unable to addapt CallDetail to BalanceHistory "
                    + e.getMessage(), e).log(ctx);
        }

        return hist;
    }


    public static BalanceHistory convertCallDetailToHistory(final Context ctx, final CallDetail cd)
    {
        final BalanceHistory hist = new BalanceHistory();
        try
        {
            hist.setType(BalanceHistoryTypeEnum.CALLDETAIL);
            
            /* For Call Details, we will sort by the Transaction Date (as in Subscriber Invoices). */
            hist.setSortDate(cd.getTranDate());
            /* To keep it consistent with what is displayed on the Call Details screen we 
             * will display by Transaction Date (as opposed to by Posted Date) */
            hist.setKeyDate(cd.getTranDate()); //Date and time call occurred.
            //hist.setKeyDate(cd.getPostedDate());	//Date and time call was recorded.
            hist.setId((Long.valueOf(cd.getId())).toString());
            hist.setSpid(cd.getSpid());
            hist.setCallAdjustmentType(cd.getCallType() != null ? cd.getCallType().getDescription() : " ");
            hist.setBAN(cd.getBAN());
            hist.setSubscriberID(cd.getSubscriberID());
            hist.setOrigMSISDN(cd.getOrigMSISDN());
            hist.setDestMSISDN(cd.getDestMSISDN());
            hist.setMsisdn(cd.getChargedMSISDN());
            hist.setDestPartyLocation(cd.getDestinationPartyLocation());
            hist.setOrigPartyLocation(cd.getCallingPartyLocation());
            hist.setUsage(cd.getDuration());
            hist.setBundleUsage(cd.getBucketCounter());
            hist.setDataUsage(cd.getDataUsage());
            hist.setBillingCatagory(String.valueOf(cd.getBillingCategory()) );
            hist.setDuration(cd.getDuration());
            hist.setUsageType(cd.getUsageType());
            hist.setCharge(cd.getCharge());
            hist.setBalance(cd.getBalance());
        }
        catch (Exception e)
        {
            new MinorLogMsg(BalanceHistorySupport.class, "Unable to addapt CallDetail to BalanceHistory "
                    + e.getMessage(), e).log(ctx);
        }

        return hist;
    }
}
