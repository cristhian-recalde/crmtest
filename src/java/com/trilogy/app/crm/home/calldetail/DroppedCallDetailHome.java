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

package com.trilogy.app.crm.home.calldetail;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.omg.CORBA.LongHolder;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.calldetail.BillingCategoryEnum;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallTypeEnum;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.client.AppOcgClient;
import com.trilogy.app.crm.config.CallDetailConfig;
import com.trilogy.app.crm.filter.EitherPredicate;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.CallDetailSupport;
import com.trilogy.app.crm.support.CallDetailSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextSupport;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.util.time.Time;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.product.s2100.ErrorCode;

/**
 * This decorates the CallDetailHome to add reimbursements to the subscriber for dropped
 * calls within a specified time window
 */

public class DroppedCallDetailHome extends HomeProxy implements ContextAware
{
    public static final int DROPPED_CALL_WINDOW = 5 * 60 * 1000;
    public static final int CALL_CONTROL_ERROR = 3;
    public static final int RESULT_SUCCESSFUL = 0;
    public static final int RESULT_GENERAL_ERROR = 9999;
    public static final String BILL_MARKER = "*";

    public static Date lastTimestamp = null;

    public Map<String, CallDetail> droppedCalls_ = Collections.synchronizedMap(
            new LinkedHashMap<String, CallDetail>()
    {
        private static final long serialVersionUID = 1L;

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean removeEldestEntry(final Map.Entry<String, CallDetail> eldest)
        {
            if (LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(this, "Start DroppedCallDetailHome.removeEldestEntry", null).log(getContext());
            }

            final CallDetail call = eldest.getValue();

            final Date transDate = call.getTranDate();

            if ((lastTimestamp.getTime() - transDate.getTime()) > DROPPED_CALL_WINDOW)
            {
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    new DebugLogMsg(this, "Removing this dropped call from dropped calls map: " + call,
                            null).log(getContext());
                }
                return true;
            }

            if (LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(this, "Nothing to remove.\nEnd DroppedCallDetailHome.removeEldestEntry",
                        null).log(getContext());
            }

            return false;
        }
    });

    protected Context context_ = new ContextSupport();


    public DroppedCallDetailHome(final Context ctx, final Home delegate)
    {
        super(delegate);
        setContext(ctx);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Context getContext()
    {
        return context_;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setContext(final Context context)
    {
        context_ = context;
    }


    ////////////////////////////////////////////// Impl Home

    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj)
            throws HomeException
    {
        boolean processedDroppedCall = false;
        final CallDetail call = (CallDetail) getDelegate().create(ctx, obj);
        lastTimestamp = call.getTranDate();

        CallDetailSupport callDetailSupport = CallDetailSupportHelper.get(ctx);
        callDetailSupport.debugMsg(DroppedCallDetailHome.class, call,
                "Checking for match for call under Dropped Calls Map: " + droppedCalls_.toString(), ctx);

        final CallDetail droppedCall = droppedCalls_.get(call.getChargedMSISDN());

        // If the dropped call exists and meets the criteria, refund.
        if ((droppedCall != null) && call.getDestMSISDN().equals(droppedCall.getDestMSISDN()))
        {
            final long timeDifference = calculateDuration(droppedCall, call);

            callDetailSupport.debugMsg(DroppedCallDetailHome.class, call,
                    "Dropped call found with Time difference " + timeDifference, ctx);

            if ((timeDifference >= 0) && (timeDifference <= DROPPED_CALL_WINDOW))
            {
                logOm(Common.OM_DROPPED_CALL_REIMBURSEMENT_ATTEMPT);

                long reimbursementAmount = 0;

                try
                {
                    reimbursementAmount = calculateReimbursement(ctx, droppedCall);
                }
                catch (final HomeException e)
                {
                    new InfoLogMsg(this, "Dropped call reimbursement calculation failed for dropped call: "
                            + droppedCall + "\n" + e, null).log(getContext());
                    handleErAndOm(droppedCall, RESULT_GENERAL_ERROR, Common.OM_DROPPED_CALL_REIMBURSEMENT_FAIL);
                    return call;
                }

                // no need to do anything if the reimbursement amount is less than 1 unit
                if (reimbursementAmount < 1)
                {
                    droppedCalls_.remove(droppedCall.getChargedMSISDN());

                    final StringBuilder msg = new StringBuilder("Reimbursement Amount is : ");
                    msg.append(reimbursementAmount);
                    msg.append(", no processing necessary.  Removed this dropped call from the map: ");
                    msg.append(droppedCall);
                    callDetailSupport.debugMsg(DroppedCallDetailHome.class, call, msg.toString(), ctx);

                    handleErAndOm(droppedCall, RESULT_GENERAL_ERROR, Common.OM_DROPPED_CALL_REIMBURSEMENT_SUCCESS);

                    return call;
                }

                final CallDetail reimbursement = createReimbursment(droppedCall, reimbursementAmount);

                callDetailSupport.debugMsg(DroppedCallDetailHome.class, call, "Dropped call reimbursement: "
                        + reimbursement, ctx);

                boolean rollback = false;

                try
                {
                    final String result = applySubscriberAdjustment(reimbursement, false);

                    if (result.equals(Common.OM_DROPPED_CALL_REIMBURSEMENT_SUCCESS))
                    {
                        rollback = true; //if anything fails after this, we have to rollack the ocg transaction
                        callDetailSupport.debugMsg(DroppedCallDetailHome.class, call,
                                "Create CallDetail for reimbursement: " + reimbursement, ctx);
                        getDelegate().create(ctx, reimbursement);

                        // set the bill marker on the original dropped call and store it
                        droppedCall.setBillMarker(BILL_MARKER);

                        getDelegate().store(ctx, droppedCall);

                        handleErAndOm(reimbursement, RESULT_SUCCESSFUL, result);
                    }
                    else
                    {
                        handleErAndOm(reimbursement, RESULT_GENERAL_ERROR, result);
                    }
                }
                catch (final HomeException e)
                {
                    handleErAndOm(reimbursement, RESULT_GENERAL_ERROR, Common.OM_DROPPED_CALL_REIMBURSEMENT_FAIL);
                    new InfoLogMsg(this, "Dropped call reimbursement failed for call: " + droppedCall,
                            e).log(getContext());

                    if (rollback)
                    {
                        new InfoLogMsg(this, "Rollback OCG transaction.", null).log(getContext());
                        applySubscriberAdjustment(reimbursement, rollback);
                    }
                }

                droppedCalls_.remove(droppedCall.getChargedMSISDN());

                processedDroppedCall = true;
            }
        }

        if (!processedDroppedCall && (isCompenableDroppedCall(ctx, call)))
        {
            callDetailSupport.debugMsg(DroppedCallDetailHome.class, call, "Adding dropped call", ctx);

            droppedCalls_.put(call.getChargedMSISDN(), call);
        }

        return call;
    }


    /**
     * Check dropped call reason to see if it is compensable
     * 
     * @param ctx
     * @param call
     * @return
     */
    public static boolean isCompenableDroppedCall(final Context ctx, final CallDetail call)
    {
        //return CALL_CONTROL_ERROR == call.getDisconnectReason();
        final CallDetailConfig cdc = (CallDetailConfig) ctx.get(CallDetailConfig.class);
        String compensableReason = cdc.getCompensableReason();
        final String codeDelimiter = ",";
        compensableReason = codeDelimiter + compensableReason + codeDelimiter;
        return compensableReason.indexOf(codeDelimiter + call.getDisconnectReason() + codeDelimiter) != -1;
    }


    /**
     * @param droppedCall
     * @param reimbursementAmount
     * @return
     */
    private CallDetail createReimbursment(final CallDetail droppedCall, final long reimbursementAmount)
    {
        // TODO - 2003-12-24 - Use CalLDetail.clone() instead.
        final CallDetail reimbursement = new CallDetail();

        // set up the reimbursement call detail
        reimbursement.setTranDate(droppedCall.getTranDate());
        reimbursement.setPostedDate(new Date());
        reimbursement.setBAN(droppedCall.getBAN());
        reimbursement.setChargedMSISDN(droppedCall.getChargedMSISDN());
        reimbursement.setOrigMSISDN(droppedCall.getOrigMSISDN());
        reimbursement.setDestMSISDN(droppedCall.getDestMSISDN());
        reimbursement.setCallingPartyLocation(droppedCall.getCallingPartyLocation());
        reimbursement.setDuration(droppedCall.getDuration());
        reimbursement.setFlatRate(droppedCall.getFlatRate());
        reimbursement.setVariableRate(droppedCall.getVariableRate());
        reimbursement.setVariableRateUnit(droppedCall.getVariableRateUnit());
        reimbursement.setUsedMinutes(droppedCall.getUsedMinutes());
        reimbursement.setSpid(droppedCall.getSpid());
        reimbursement.setRatePlan(droppedCall.getRatePlan());
        reimbursement.setRatingRule(droppedCall.getRatingRule());
        reimbursement.setCallType(CallTypeEnum.DROPPED_CALL);
        reimbursement.setBillMarker("*");
        reimbursement.setGLCode(droppedCall.getGLCode());
        reimbursement.setBucketRateID(droppedCall.getBucketRateID());
        reimbursement.setBillingCategory(BillingCategoryEnum.DROPPED_CALL_COMPENSATION_INDEX);
        reimbursement.setCharge(-1 * reimbursementAmount);
        reimbursement.setTaxAuthority1(droppedCall.getTaxAuthority1());
        reimbursement.setTaxAuthority2(droppedCall.getTaxAuthority2());
        reimbursement.setSubscriberType(droppedCall.getSubscriberType());
        reimbursement.setDisconnectReason(droppedCall.getDisconnectReason());
        return reimbursement;
    }


    /////////////////////////////////////////////////////

    /**
     * @param droppedCall
     * @return
     */
    private long calculateReimbursement(final Context ctx, final CallDetail droppedCall) throws HomeException
    {
        double centsPerMinuteCost = 0;
        long reimbursement = 0;
        final double totalDebit = droppedCall.getCharge();

        final Time duration = droppedCall.getDuration();
        final int seconds = duration.getHours() * 60 * 60 + duration.getMinutes() * 60 + duration.getSeconds();
        int minSeconds = seconds;

        final StringBuilder introDebugMsg = new StringBuilder(
                "Calculate Reimbursement using dropped call with duration: ");
        introDebugMsg.append("seconds");
        introDebugMsg.append("s , Total Debit: ");
        introDebugMsg.append(totalDebit);
        introDebugMsg.append(", BucketRateId: ");
        introDebugMsg.append(droppedCall.getBucketRateID());

        CallDetailSupportHelper.get(ctx).debugMsg(DroppedCallDetailHome.class, droppedCall, introDebugMsg.toString(), ctx);

        if (seconds > 60)
        {
            minSeconds = 60;
        }

        if (totalDebit == 0 && droppedCall.getBucketRateID() != 0)
        {
            final Home subscriberHome = (Home) getContext().get(SubscriberHome.class);

            final String sql_clause = "msisdn = '" + droppedCall.getChargedMSISDN() + "'";
            final Subscriber sub = (Subscriber) subscriberHome.find(ctx, new EitherPredicate(new EQ(SubscriberXInfo.MSISDN,
                    droppedCall.getChargedMSISDN()), sql_clause));

            if (sub == null)
            {
                throw new HomeException("Calculate Reimbursement Failed. Cannot find Subscriber with MSISDN: "
                        + droppedCall.getChargedMSISDN());
            }

            final PricePlanVersion pricePlanVersion = sub.getRawPricePlanVersion(getContext());

            if (pricePlanVersion == null)
            {
                throw new HomeException(
                        "Calculate Reimbursement Failed. Cannot find Price Plan for Subscriber MSISDN: "
                                + droppedCall.getChargedMSISDN());
            }

            centsPerMinuteCost = pricePlanVersion.getDefaultPerMinuteAirRate();

        }
        else if (seconds != 0)
        {
            final double tempMin = seconds / 60.0;
            centsPerMinuteCost = Math.floor((totalDebit / tempMin) + 0.5);
        }

        final double tempMinSeconds = minSeconds / 60.0;
        reimbursement = (long) Math.floor((tempMinSeconds * centsPerMinuteCost) + 0.5);

        return reimbursement;
    }


    private long calculateDuration(final CallDetail droppedCall, final CallDetail reconnectCall)
    {
        final Time duration = droppedCall.getDuration();
        //long durationMillis = duration.getHours() * 60 + duration.getMinutes() * 60 + duration.getSeconds() * 1000 + duration.getMillis();        
        final long durationMillis = duration.getTime();
        final long droppedTime = droppedCall.getTranDate().getTime() + durationMillis;
        final long reconnectTime = reconnectCall.getTranDate().getTime();

        final long timeDifference = reconnectTime - droppedTime;
        if (timeDifference < 0)
        {
            return 0;
        }

        return timeDifference;
    }


    private void handleErAndOm(final CallDetail call, final int resultCode, final String status)
    {
        logEr(call, resultCode);
        logOm(status);
    }


    private void logEr(final CallDetail call, final int resultCode)
    {
        try
        {
            ERLogger.generateDroppedCallReimbursementER(getContext(), call, resultCode);
        }
        catch (final Exception e)
        {
            new InfoLogMsg(this, "Generation of Dropped Call Reimbursement ER failed: " + e, null).log(getContext());
        }
    }


    private void logOm(final String status)
    {
        try
        {
            new OMLogMsg(Common.OM_MODULE, status).log(getContext());
        }
        catch (final Exception e)
        {
            new InfoLogMsg(this, "Generation of Dropped Call Reimbursement OM " + status + ", failed.",
                    null).log(getContext());
        }
    }


    private String applySubscriberAdjustment(final CallDetail call, final boolean rollback)
    {
        final AppOcgClient client = (AppOcgClient) getContext().get(AppOcgClient.class);
        int result = 0;
        long amount = call.getCharge();
        Account acct = null;
        SubscriberTypeEnum subType = null;
        try
        {
            final Home acctHome = (Home) getContext().get(AccountHome.class);
            if (acctHome == null)
            {
                throw new HomeException("Create failed. Cannot find AccountHome in context.");
            }
            acct = (Account) acctHome.find(getContext(), call.getBAN());
            if (acct == null)
            {
                throw new HomeException("Create failed. Cannot find account with acctnumber=" + call.getBAN());
            }

            final Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDN(getContext(), call.getChargedMSISDN());

            if (sub == null
                    || (!sub.getBAN().equals(acct.getBAN())
                            && sub.getState() != com.redknee.app.crm.bean.SubscriberStateEnum.ACTIVE))
            {
                throw new HomeException("Create failed. Cannot find Subscriber.");
            }
            subType = sub.getSubscriberType();

        }
        catch (final HomeException e)
        {
            new InfoLogMsg(this, "Apply subscriber adjustment failed.  Unable to find account: " + call.getBAN() + "\n"
                    + e, null).log(getContext());
            new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_ADJUST_FAIL).log(getContext());
            return Common.OM_DROPPED_CALL_REIMBURSEMENT_FAIL;
        }

        if (rollback)
        {
            amount = amount * -1;
        }

        SubscriptionType INSubscriptionType = null;
        try
        {
            INSubscriptionType = SubscriptionType.getINSubscriptionType(getContext());
        }
        catch (final Exception e)
        {
            new MajorLogMsg(this, "No IN subscription type defined in system.", null).log(getContext());
            return Common.OM_DROPPED_CALL_REIMBURSEMENT_FAIL;
        }
        if (null == INSubscriptionType)
        {
            new MajorLogMsg(this, "No IN subscription type defined in system.", null).log(getContext());
            return Common.OM_DROPPED_CALL_REIMBURSEMENT_FAIL;
        }

        if (amount > 0)
        {
            final boolean bBalFlag = true; // for insufficient balance, true = fail transaction, false - zero account

            final String erReference = "AppCrm-" + call.getId();

            result = client.requestDebit(call.getChargedMSISDN(), subType, amount, acct.getCurrency(), bBalFlag,
                    erReference, INSubscriptionType.getId(), new LongHolder());

            if (result != ErrorCode.NO_ERROR)
            {
                new InfoLogMsg(this, "Request debit failed.  Result code: " + result, null).log(getContext());
                new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_ADJUST_FAIL).log(getContext());
                return Common.OM_DROPPED_CALL_REIMBURSEMENT_FAIL;
            }
        }
        else if (amount < 0)
        {
            final String erReference = "AppCrm-" + call.getId();
            final boolean bExtendBalance = false;
            final short nNumDaysExtendBalance = 0;

            result = client.requestCredit(call.getChargedMSISDN(), subType, -amount, acct.getCurrency(),
                    bExtendBalance, nNumDaysExtendBalance, erReference, INSubscriptionType.getId(), null,
                    new LongHolder());

            if (result != ErrorCode.NO_ERROR)
            {
                new InfoLogMsg(this, "Request credit failed.  Result code: " + result, null).log(getContext());
                new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_ADJUST_FAIL).log(getContext());
                return Common.OM_DROPPED_CALL_REIMBURSEMENT_FAIL;
            }
        }

        return Common.OM_DROPPED_CALL_REIMBURSEMENT_SUCCESS;
    }
    
    
    
    
}
