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
package com.trilogy.app.crm.provision;

import java.io.IOException;
import java.util.Date;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.dunning.DunningProcess;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.provision.xgen.BASServiceException;
import com.trilogy.app.crm.provision.xgen.BASServiceInternalException;
import com.trilogy.app.crm.provision.xgen.BASServiceProxy;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.util.snippet.log.Logger;


/**
 * Server Implementation.
 *
 * @author paul.spernea@redknee.com
 */
public class BASServiceServer extends BASServiceProxy implements ContextAware
{

    /**
     * Constructor taking the context directly.
     *
     * @param context
     *            Context
     */
    public BASServiceServer(final Context context)
    {
        setContext(context);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Context getContext()
    {
        return this.context_;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setContext(final Context context) throws IllegalArgumentException
    {
        this.context_ = context;
    }


    /**
     * Is used to apply a payment, transaction or adjustment to a specified account
     * number.
     *
     * @param ctx
     *            Context.
     * @param acctNum
     *            Account number.
     * @param msisdn
     *            SUbscriber MSISDN.
     * @param adjustmentType
     *            Adjustment type.
     * @param amount
     *            Amount of the adjustment.
     * @param transDate
     *            Transaction date.
     * @param csrInput
     *            Additional CSR import.
     * @throws BASServiceException
     *             Thrown if there are problems with creating the adjustment.
     * @throws BASServiceInternalException
     *             Thrown if there are irrecoverable with creating the adjustment.
     */
    @Override
    public void acctAdjust(final Context ctx, final String acctNum, final String msisdn, final int adjustmentType,
        final long amount, final Date transDate, final String csrInput) throws BASServiceException,
        BASServiceInternalException
    {
        new DebugLogMsg(this, "acctAdjust" + "," + acctNum + "," + msisdn + "," + adjustmentType + "," + amount + ","
            + transDate + "," + csrInput, null).log(getContext());

        final Home home = (Home) getContext().get(TransactionHome.class);
        if (home == null)
        {
            throw new BASServiceInternalException("Cannot find TransactionHome in Context");
        }

        final Transaction trans;
        
        try
        {
            trans = (Transaction) XBeans.instantiate(Transaction.class, ctx);
        }
        catch (Exception exception)
        {
            throw new BASServiceInternalException("Fail to instantiate transaction bean", exception);
        }

        try
        {
            final Context subCtx = ctx.createSubContext();

            final String correctedBAN = validateAccountByMsisdn(acctNum, msisdn, transDate);
            final Account account = HomeSupportHelper.get(ctx).findBean(ctx, Account.class, trans.getBAN());

            trans.setBAN(correctedBAN);
            trans.setResponsibleBAN(account.getResponsibleBAN());
            trans.setMSISDN(msisdn);
            trans.setAdjustmentType(adjustmentType);
            trans.setAmount(amount);
            trans.setTransDate(transDate);
            trans.setCSRInput(csrInput);
            trans.setPaymentAgency("default");
            trans.setLocationCode("");

            // assume IN subscription type
            SubscriptionType subscriptionType = SubscriptionType.getINSubscriptionType(ctx);
            if(null == subscriptionType)
            {
                throw new HomeException("An IN subscription type is not defined on the system.");
            }
            trans.setSubscriptionTypeId(subscriptionType.getId());

            home.create(subCtx, trans);

        }
        catch (final HomeInternalException e)
        {
            throw new BASServiceInternalException(e);
        }
        catch (final HomeException e)
        {
            throw new BASServiceException(e);
        }
    }


    /**
     * starts the dunning process for the specified account number.
     *
     * @param ctx
     *            Operating context.
     * @param acctNum
     *            Account number.
     * @param billingDate
     *            Billing date.
     * @exception BASServiceException
     *                Thrown if there are problems dunning the account.
     * @exception BASServiceInternalException
     *                Thrown if there are irrecoverable problems dunning the account.
     */
    @Override
    public void invokeDunning(final Context ctx, final String acctNum, final Date billingDate)
        throws BASServiceException, BASServiceInternalException
    {
        new DebugLogMsg(this, "invokeDunning" + "," + acctNum + "," + billingDate, null).log(getContext());

        try
        {
            final DunningProcess dunningProcess = (DunningProcess) ctx.get(DunningProcess.class);
            dunningProcess.processAccount(ctx, billingDate, acctNum);
        }
        catch (final DunningProcessException e)
        {
            new MinorLogMsg(this, e.getMessage(), e).log(getContext());
            throw new BASServiceException(e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int acctAdjustWithSvcFee(final String msisdn, final int amountAdjustType, final String csrInputAmt,
        final long amount, final int svcAdjustType, final String csrInputSvcFee, final long serviceFee,
        final Date transDate) throws BASServiceException, BASServiceInternalException
    {

        new DebugLogMsg(this, "acctAdjustWithSvcFee" + "," + amountAdjustType + "," + csrInputAmt + "," + amount + ","
            + svcAdjustType + "," + csrInputSvcFee + "," + serviceFee + "," + transDate, null).log(getContext());

        final int expiryDaysExt = 0;
        return acctAdjustEx(msisdn, amountAdjustType, csrInputAmt, amount, svcAdjustType, csrInputSvcFee, serviceFee,
            transDate, expiryDaysExt, 0, null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int acctAdjustEx(final String msisdn, final int amountAdjustType, final String csrInputAmt,
        final long amount, final int svcAdjustType, final String csrInputSvcFee, final long svcFee,
        final Date transDate, final int expiryDaysExt, final long resrv1, final String resrv2)
        throws BASServiceException, BASServiceInternalException
    {
        if (Logger.isDebugEnabled())
        {
            new DebugLogMsg(this, "acctAdjustEx" + "," + amountAdjustType + "," + csrInputAmt + "," + amount + ","
                    + svcAdjustType + "," + csrInputSvcFee + "," + svcFee + "," + transDate + "," + expiryDaysExt + ","
                    + resrv1 + "," + resrv2, null).log(getContext());
        }

        if (resrv1 != 0 || resrv2 != null && resrv2.length() > 0)
        {
            throw new IllegalArgumentException("Currently not supported");
        }

        Subscriber sub = null;
        AdjustmentType amountType = null;
        AdjustmentType svcType = null;
        try
        {
            sub = SubscriberSupport.lookupSubscriberForMSISDN(getContext(), msisdn);
            amountType = AdjustmentTypeSupportHelper.get(getContext()).getAdjustmentType(getContext(), amountAdjustType);
            if (svcAdjustType != 0)
            {
                svcType = AdjustmentTypeSupportHelper.get(getContext()).getAdjustmentType(getContext(), svcAdjustType);
            }
        }
        catch (final HomeException e)
        {
            throw new BASServiceException("fail to make adjustment for " + msisdn + " " + e.getMessage());
        }

        if (sub != null && amountType != null)
        {
            try
            {
                final Home home = (Home) getContext().get(TransactionHome.class);

                final Transaction amountTrans = createTransaction(sub, amount, amountType, csrInputAmt, expiryDaysExt);
                amountTrans.setTransDate(transDate);
                home.store(amountTrans);

                if (svcType != null)
                {
                    try
                    {
                        final Transaction svcTrans = TransactionSupport.createTransaction(getContext()
                            .createSubContext(), sub, svcFee, svcType, false, false, csrInputSvcFee);
                        svcTrans.setTransDate(transDate);
                        home.store(svcTrans);

                    }
                    catch (final HomeException e)
                    {
                        home.remove(amountTrans);
                        throw e;
                    }
                }

            }
            catch (final HomeException e)
            {
                throw new BASServiceException("Fail to apply adjustment to " + msisdn + " " + e.getMessage());
            }
            catch (final Exception ge)
            {
                throw new BASServiceException("Fail to apply adjustment to " + msisdn + " " + ge.getMessage());
            }
        }
        return 0;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public long accumulatePaymentPlanRunningBalance(final Context ctx, final String acctnum, final Date startDate,
            final Date endDate) throws BASServiceException, BASServiceInternalException
    {
        long realTimeAmount = 0;
        try
        {
            CalculationService service = (CalculationService) ctx.get(CalculationService.class);
            realTimeAmount = service.getAccountPaymentPlanUnchargedLoanRemainder(ctx, acctnum, startDate, endDate);
        }
        catch (Exception e)
        {
            LogSupport.minor(getContext(), this, "Failed to fetch PaymentPlanUnchargedLoanRemainder for account.", e);
        }
        return realTimeAmount;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public long accumulatePaymentPlanUnpaidAdjustments(final Context context, final String ban, final Date startDate,
        final Date endDate, final Date invoiceEndDate) throws BASServiceException, BASServiceInternalException
    {
        long result = 0;
        /*
        try
        {
            Account account = HomeSupportHelper.get(context).findBean(context, Account.class, ban);
            CalculationService service = (CalculationService) context.get(CalculationService.class);
            if (endDate.equals(invoiceEndDate))
            {
                result = service.getAccountUnpaidPaymentPlanLoanAdjustments(context,
                        ban, startDate, invoiceEndDate);
            }
            else
            {
                final long charges = service.getAccountPaymentPlanAdjustments(context,
                        ban, startDate, endDate);
                final long payments = service.getAccountPaymentPlanAllocations(context,
                        ban, startDate, endDate)
                        + service.getAccountPaymentPlanPayments(context, ban,
                                startDate, endDate);
                result = charges + payments;
            }
            result = service.getAccountPaymentPlanCharges(context, ban, startDate, endDate);
        }
        catch (Exception e)
        {
            LogSupport.minor(getContext(), this, "Failed to fetch PaymentPlanAdjustments for account.", e);
        }
        */
        
        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public long accumulatePaymentPlanCharges(final Context ctx, final String acctnum, final Date startDate,
        final Date endDate) throws BASServiceException, BASServiceInternalException
    {
        long amount = 0;
        try
        {
            CalculationService service = (CalculationService) ctx.get(CalculationService.class);
            amount = service.getAccountPaymentPlanCharges(ctx, acctnum, startDate, endDate);
        }
        catch (Exception e)
        {
            LogSupport.minor(getContext(), this, "Failed to fetch PaymentPlanAdjustments for account.", e);
        }
        return amount;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public long accumulatePaymentPlanPayments(final Context ctx, final String acctnum, final Date startDate,
        final Date endDate) throws BASServiceException, BASServiceInternalException
    {
        long amount = 0;
        /*
        try
        {
            CalculationService service = (CalculationService) ctx.get(CalculationService.class);
            amount = service.getAccountPaymentPlanAdjustments(ctx, acctnum, startDate,
                    endDate);
        }
        catch (Exception e)
        {
            LogSupport.minor(getContext(), this, "Failed to fetch PaymentPlanAdjustments for account.", e);
        }
        */
        return amount;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public long accumulateAmountDue(final Context ctx, final String acctnum, final Date startDate, final Date endDate)
        throws BASServiceException, BASServiceInternalException
    {        
        long amountDue = 0;
        CalculationService service = (CalculationService) ctx.get(CalculationService.class);
        /*
        try
        {
            long paymentsRecvd = service.getAccountPaymentsReceived(ctx, acctnum, startDate, endDate);
            long totalBalance = service.getAccountTotalBalance(ctx, acctnum, startDate, endDate);
            amountDue = (paymentsRecvd + totalBalance);
        }
        catch (Exception e)
        {
            throw new BASServiceException("Failed to get due amount for " + acctnum + " " + e.getMessage());
        }
        */
        return amountDue;
    }


    /**
     * Creates transaction.
     *
     * @param sub
     *            Subscriber.
     * @param amount
     *            Amount of the transaction.
     * @param adjType
     *            Adjustment type.
     * @param csrInput
     *            CSR input.
     * @param expiryDaysExt
     *            Number of days the expiry date is extended.
     * @return The created transaction.
     * @throws HomeException
     *             Thrown if there are problems creating the transaction.
     */
    protected Transaction createTransaction(final Subscriber sub, final long amount, final AdjustmentType adjType,
        final String csrInput, final long expiryDaysExt) throws HomeException
    {
        final boolean limitExemption = false;
        final boolean prorated = false;
        final String csrIdentifier = CoreTransactionSupportHelper.get(getContext()).getCsrIdentifier(getContext());
        final Date billingDate = new Date();
        final Date receivingDate = new Date();
        final long newBalance = 0;
        final Context subCtx = getContext().createSubContext();

        return TransactionSupport.createTransaction(subCtx, sub, amount, newBalance, adjType, prorated, limitExemption,
            csrIdentifier, billingDate, receivingDate, csrInput, (int) expiryDaysExt);

    }


    /**
     * TT5112227094 , if loyalty passes empty account, for some reason, the is some
     * directly Account.class in context, causing transaction creation failed.
     *
     * @param acc
     *            Account number.
     * @param msisdn
     *            MSISDN.
     * @param transDate
     *            Transaction date.
     * @return Account BAN.
     * @throws HomeException
     *             Thrown if the MSISDN does not belong to this account.
     */
    protected String validateAccountByMsisdn(final String acc, final String msisdn, final Date transDate)
        throws HomeException
    {
        String ban = acc;

        final Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDNLimited(getContext(), msisdn, transDate);
        if (sub != null)
        {
            ban = sub.getBAN();
        }

        if (acc != null)
        {
            final String trimmed = acc.trim();

            if (trimmed.length() > 0 && !trimmed.equals(ban))
            {
                final StringBuilder sb = new StringBuilder();
                sb.append("Account does not match with msisdn, acc=");
                sb.append(acc);
                sb.append("current acc= ");
                sb.append(ban);
                sb.append(", msisdn=");
                sb.append(msisdn);
                sb.append(", date=");
                sb.append(transDate);
                if (sub != null)
                {
                    sb.append(", sub=");
                    sb.append(sub.getId());
                }
                throw new HomeException(sb.toString());
            }
        }
        return ban;
    }    

    /**
     * container context.
     */
    private Context context_;
}
