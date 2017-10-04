/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bas.recharge;

import java.util.Collection;
import java.util.Date;

import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.contract.SubscriptionContract;
import com.trilogy.app.crm.contract.SubscriptionContractSupport;
import com.trilogy.app.crm.contract.SubscriptionContractTerm;
import com.trilogy.app.crm.contract.SubscriptionContractTermXInfo;
import com.trilogy.app.crm.home.sub.SubscriberNoteSupport;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.RecurringRechargeSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Generates recurring recredit for subscription contract
 * 
 * @author kumaran.sivasubramaniam@redknee
 */
public class CreditContractPrepaymentVisitor extends AbstractRechargeItemVisitor
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    public CreditContractPrepaymentVisitor(final Date billingDate, final String agentName,
            final ChargingCycleEnum chargingPeriod, final Subscriber subscriber, final Date startDate,
            final Date endDate, final double rate, final boolean preBilling, final boolean proRated,
            final int billCycleDay)
    {
        super(billingDate, agentName, chargingPeriod, startDate, endDate, rate, preBilling, proRated, false);
        setSub(subscriber);
        setSuspendOnFailure(false);
        billCycleDay_ = billCycleDay;
    }


    public CreditContractPrepaymentVisitor(final Context context, final Date billingDate, final String agentName,
            final ChargingCycleEnum chargingPeriod, final Subscriber subscriber, final double rate,
            final boolean preBilling, final boolean proRated, final int billCycleDay) throws HomeException
    {
        super(context, billingDate, agentName, chargingPeriod, subscriber.getAccount(context).getBillCycleDay(context),
                subscriber.getSpid(), rate, preBilling, proRated, false);
        this.setSub(subscriber);
        this.setSuspendOnFailure(false);
        billCycleDay_ = billCycleDay;
    }


    /**
     * {@inheritDoc}
     */
    public void visit(final Context ctx, final Object obj)
    {
        int result = RECHARGE_SUCCESS;
        SubscriptionContract contract = (SubscriptionContract) obj;
        long currentCharge = 0;
        try
        {
            if (contract != null && !SubscriptionContractSupport.isDummyContract(ctx, contract.getContractId()))
            {
                SubscriptionContractTerm term = HomeSupportHelper.get(ctx).findBean(ctx,
                        SubscriptionContractTerm.class,
                        new EQ(SubscriptionContractTermXInfo.ID, contract.getContractId()));
                Date lastDayOfBillCycle = BillCycleSupport.getDateOfBillCycleLastDay(billCycleDay_,
                        this.getBillingDate());
                Date prePaymentEndDate = contract.getPrePaymentEndDate();
                Date startDate = contract.getContractStartDate();
                // Make sure you are still in teh contract period
                if (prePaymentEndDate != null && prePaymentEndDate.after(this.getBillingDate()))
                {
                    if (lastDayOfBillCycle.after(prePaymentEndDate))
                    {
                        currentCharge = contract.getBalancePaymentAmount();
                        contract.setBalancePaymentAmount(0);
                    }
                    else
                    {
                        long monthlyCredit = SubscriptionContractSupport.getMonthlyCreditAmount(
                                term.getPrepaymentAmount(), term.getPrePaymentLength());
                        currentCharge = monthlyCredit;
                        contract.setBalancePaymentAmount(contract.getBalancePaymentAmount() - monthlyCredit);
                    }
                    this.setChargeable(this.isChargeable(ctx, term, -currentCharge));
                    if (this.isChargeable())
                    {
                        handleMonthlyCreditTransaction(ctx, term, -currentCharge);
                    }
                    else
                    {
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, this, "Contract " + term.getName()
                                    + "'s Monthly Credit is not creditable for sub " + this.getSub().getId());
                        }
                    };
                    HomeSupportHelper.get(ctx).storeBean(ctx, contract);
                }
                Date bonusEndDate = contract.getBonusEndDate();
                if (bonusEndDate.before(lastDayOfBillCycle))
                {
                    SubscriberNoteSupport
                            .createSubscriptionContractExpireWarningNote(ctx, this, getSub(), bonusEndDate,term.getName());
                }
            }
        }
        catch (HomeException homeEx)
        {
            new MinorLogMsg(this, "Unable to find contract " + contract.getContractId(), homeEx).log(ctx);
            result = RECHARGE_FAIL_XHOME;
        }
        finally
        {
            this.accumulateForOne(result == RECHARGE_SUCCESS, currentCharge);
        }
    }


    public boolean isChargeable(final Context parentCtx, SubscriptionContractTerm term, long amount)
            throws HomeException
    {
        Context context = parentCtx.createSubContext();
        context.put(Subscriber.class, getSub());
        boolean ret = true;
        
        // TT#13011421012
        if( SpidSupport.getCRMSpid(parentCtx, term.getSpid()).isIgnoreChargeForZeroMontlyContractCredit()
        		&& amount == 0)
        {
        	return Boolean.FALSE;
        }
        
        //
        if (!isChargeable(context, term, ChargedItemTypeEnum.PREPAYMENTCREDIT,
                (int) term.getContractAdjustmentTypeId(), amount, ServicePeriodEnum.MONTHLY))
        {
            if (LogSupport.isDebugEnabled(context))
            {
                // sb.append("it is not chargeable (it was charged for the current billing cycle or on a future date).");
                LogSupport.debug(context, this, "Already credited");
            }
            ret = false;
        }
        if (isProRated())
        {
            setItemRate(getRate());
        }
        /*
         * log the error condition.
         */
        if (!ret && LogSupport.isDebugEnabled(context))
        {
        }
        return ret;
    }


    public boolean handleMonthlyCreditTransaction(final Context context, final SubscriptionContractTerm term,
            final long amount)
    {
        Transaction trans = null;
        int result = RECHARGE_SUCCESS;
        int ocgResult = OCG_RESULT_SUCCESS;
        Context ctx = context.createSubContext();
        ctx.put(RecurringRechargeSupport.RECURRING_RECHARGE_CHARGED_ITEM, term);
        try
        {
            try
            {
                trans = this.createTransaction(ctx, (int) term.getContractAdjustmentTypeId(), amount);            
                trans.setServiceRevenueRecognizedDate(trans.getServiceEndDate());    
                
                trans = CoreTransactionSupportHelper.get(ctx).createTransaction(ctx, trans, true);
                SubscriberSubscriptionHistorySupport.addChargingHistory(ctx, term, getSub(),
                        HistoryEventTypeEnum.CHARGE, ChargedItemTypeEnum.PREPAYMENTCREDIT, trans.getAmount(), amount,
                        trans, getBillingDate());
            }
            catch (final OcgTransactionException e)
            {
                LogSupport.minor(ctx, this,
                        "Couldn't credit subscriber " + getSub().getId() + " for contract " + term.getId(), e);
                handleFailure(ctx, trans, result, ocgResult, "OCG Failure", term.getId(),
                        ChargedItemTypeEnum.PREPAYMENTCREDIT);
            }
        }
        catch (final HomeException e)
        {
            LogSupport.minor(ctx, this,
                    "Couldn't credit subscriber " + getSub().getId() + " for contract " + term.getId(), e);
            result = RECHARGE_FAIL_XHOME;
            handleFailure(ctx, trans, result, ocgResult, "Xhome Exception:" + e.getMessage(), term.getId(),
                    ChargedItemTypeEnum.PREPAYMENTCREDIT);
        }
        catch (final Throwable t)
        {
            LogSupport.minor(ctx, this,
                    "Couldn't credit subscriber " + getSub().getId() + " for contract " + term.getId(), t);
            result = RECHARGE_FAIL_UNKNOWN;
            handleFailure(ctx, trans, result, ocgResult, "Failure Unknown:" + t.getMessage(), term.getId(),
                    ChargedItemTypeEnum.PREPAYMENTCREDIT);
        }
        finally
        {
            createER(ctx, trans, String.valueOf(term.getId()), result, ocgResult);
            createOM(ctx, getAgentName(), result == RECHARGE_SUCCESS);
            this.accumulateForOne(result == RECHARGE_SUCCESS, amount);
        }
        return result == RECHARGE_SUCCESS;
    }


    protected synchronized void accumulateForOne(final boolean success, final long amount)
    {
        if (success)
        {
            this.incrementServicesCountSuccess();
            this.incrementChargeAmountSuccess(amount);
        }
        else
        {
            this.incrementServicesCountFailed();
            this.incrementChargeAmountFailed(amount);
        }
        this.incrementServicesCount();
        this.incrementChargeAmount(amount);
    }

    private int billCycleDay_;
}
