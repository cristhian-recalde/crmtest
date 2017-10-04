package com.trilogy.app.crm.transaction;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.app.crm.bas.tps.TPSSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.PayeeEnum;
import com.trilogy.app.crm.bean.StaffWriteOffBulkLoad;
import com.trilogy.app.crm.bean.SystemTransactionMethodsConstants;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.calculation.support.CalculationServiceSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.writeoff.WriteOffSupport;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class AccountWriteOffProcessor extends AccountPaymentDistribution
{
    public AccountWriteOffProcessor(Context ctx, Account acct,
            StaffWriteOffBulkLoad writeOff)
    {
        super(ctx, acct);
        this.writeOff = writeOff;
    }

    public void process(Context ctx, PrintWriter reportOut)
            throws HomeException, CalculationServiceException
    {
        ctx = ctx.createSubContext();

        String sessionKey = CalculationServiceSupport.createNewSession(ctx);
        try
        {
            this.OBOPayableSubscribers = TPSSupport.getPayableSubscribers(ctx,
                    account);

            this.calculateOBO(ctx);
            if (this.getOutStandingOwing() != 0)
            {
                Transaction trans = createTransaction(ctx, reportOut);
                this.setOrignalTransaction(trans);
                this.distributeOBO(ctx, trans.getAmount());
                this.createDistributions(ctx, reportOut);
            }
            else
            {
                reportOut.println("There is 0 owing under account "
                        + account.getBAN());
            }
        }
        finally
        {
            CalculationServiceSupport.endSession(ctx, sessionKey);
        }
    }

    protected void createDistributions(Context ctx, PrintWriter reportOut)
    {
        Home transHome = (Home) ctx.get(TransactionHome.class);

        for (Iterator i = subscriberDistributions.values().iterator(); i
                .hasNext();)
        {
            SubscriberPaymentDistribution subDistribution = (SubscriberPaymentDistribution) i
                    .next();
            if (subDistribution.createTransaction(ctx, this.orignalTransaction,
                    transHome, getCurrency(ctx)))
            {
                this.successAmount += subDistribution
                        .getPaymentForOutStandOwing()
                        + subDistribution.getOverPayment();
                ++this.successCount;
                reportOut.println("Subscriber " + subDistribution.sub.getId()
                        + " got "
                        + subDistribution.getPaymentForOutStandOwing()
                        + " write off");
            }
            else
            {
                this.failedAmount += subDistribution
                        .getPaymentForOutStandOwing()
                        + subDistribution.getOverPayment();
                ++this.failedCount;
                reportOut.println("Failed to write off Subscriber "
                        + subDistribution.sub.getId() + " for "
                        + subDistribution.getPaymentForOutStandOwing());

            }
        }
    }

    public Transaction createTransaction(Context ctx, PrintWriter reportOut)
            throws HomeException
    {
        AdjustmentType adj = getAdjustmentType(ctx, reportOut);
        if (adj == null)
        {
            throw new HomeException("no suitable adjustment type could be found");
        }
        
        final Transaction trans;
        try
        {
            trans = (Transaction) XBeans.instantiate(Transaction.class, ctx);
        }
        catch (Exception exception)
        {
            throw new HomeException("Cannot instantiate transaction bean", exception);
        }

        Account account = HomeSupportHelper.get(ctx).findBean(ctx, Account.class, writeOff.getBAN());

        trans.setBAN(writeOff.getBAN());
        trans.setResponsibleBAN(account.getResponsibleBAN());
        trans.setAmount(this.getOutStandingOwing() * -1);
        trans.setCSRInput(writeOff.getCSRInput());
        trans.setReasonCode(writeOff.getReasonCode());
        trans.setExtTransactionId(writeOff.getExtTransactionId());
        trans.setPayee(PayeeEnum.Subscriber);
        trans.setTransactionMethod(SystemTransactionMethodsConstants.TRANSACTION_METHOD_CASH);
        trans.setReceiveDate(new Date());
        trans.setTransDate(new Date());
        trans.setAdjustmentType(adj.getCode());
        trans.setAction(adj.getAction());

        AdjustmentInfo adjustInfo = (AdjustmentInfo) adj
                .getAdjustmentSpidInfo().get(
                        new Integer(this.account.getSpid()));
        trans.setGLCode(adjustInfo.getGLCode());

        return trans;
    }

    protected AdjustmentType getAdjustmentType(Context ctx, PrintWriter reportOut)
            throws HomeException
    {
        AdjustmentTypeSupport adjustmentTypeSupport = AdjustmentTypeSupportHelper.get(ctx);
        
        AdjustmentType adj = null;

        int adjustTypeId = writeOff.getAdjustmentType();
        try
        {
            adj = adjustmentTypeSupport.getAdjustmentType(ctx, adjustTypeId);
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Error retrieving adjustment type " + adjustTypeId, e).log(ctx);
        }

        if (adj == null)
        {
            reportOut.print("invalid adjustment in bulk load record, ");
            AdjustmentTypeEnum staffWriteOffSysAdjType = AdjustmentTypeEnum.StaffWriteOff;
            adj = adjustmentTypeSupport.getAdjustmentTypeForRead(ctx,
                    adjustmentTypeSupport.getSystemAdjustmentTypeByAdjustmentTypeCode(ctx, 
                            staffWriteOffSysAdjType.getIndex()));
            
            if (adj == null)
            {
                reportOut.println("system adjustment type " + staffWriteOffSysAdjType + " (" + staffWriteOffSysAdjType.getIndex() + ") not found");
            }
            else
            {
                reportOut.println("system adjustment will be used");
            }
        }
        
        return adj;

    }

    final StaffWriteOffBulkLoad writeOff;
}
