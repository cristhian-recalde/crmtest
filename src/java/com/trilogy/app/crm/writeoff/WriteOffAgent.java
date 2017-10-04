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
package com.trilogy.app.crm.writeoff;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.csv.CSVIterator;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.DeactivatedReasonEnum;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.WriteOffConfig;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.PaymentPlanSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.writeoff.WriteOffErrorReport.ResultCode;


/**
 * 
 * 
 * @author alpesh.champeneri@redknee.com
 */
@SuppressWarnings("unchecked")
public class WriteOffAgent extends ContextAwareSupport implements ContextAgent
{

    public WriteOffAgent(Context ctx, String date, WriteOffConfig config)
    {
        setContext(ctx);
        date_ = date;
        config_ = config;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.
     * xhome.context.Context)
     */
    public void execute(Context ctx) throws AgentException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "WriteOffAgent execute() called.");
        }
        String timestamp = getCurrentTimeStamp();
        String inputDir = config_.getInputDir();
        String inputFileName = inputDir + File.separator + date_;
        String outputDir = config_.getOutputDir() + File.separator;
        String writeOffResultName = outputDir + "WRITE_OFF_" + date_ + timestamp;
        String vatReclaimableName = outputDir + "VAT_RECLAIMABLE_" + date_ + timestamp;
        String writeOffErrorName = outputDir + "ERROR_LOG_" + date_ + timestamp;
        PrintWriter writeOffResult = null, vatReclaimable = null, writeOffError = null;
        try
        {
            writeOffResult = new PrintWriter(writeOffResultName);
            vatReclaimable = new PrintWriter(vatReclaimableName);
            writeOffError = new PrintWriter(writeOffErrorName);
            Collection<WriteOffInput> writeOffs = null;
            try
            {
                writeOffs = parse(ctx, inputFileName);
            }
            catch (Exception e)
            {
                String msg = "Failed to parse input file";
                WriteOffErrorReport.logError(writeOffError, ResultCode.GENERAL_ERROR, msg);
                LogSupport.major(ctx, this, msg, e);
                return;
            }
            for (WriteOffInput writeOff : writeOffs)
            {
                String ban = writeOff.BAN;
                if (ban == null || ban.length() == 0)
                {
                    String msg = "BAN is empty in input write-off record";
                    logError(ctx, writeOffError, writeOff, msg);
                    continue;
                }
                Account account = getAccount(ctx, ban);
                if (account == null)
                {
                    String msg = "Account " + ban + " not found";
                    logError(ctx, writeOffError, writeOff, msg);
                    continue;
                }
                if (account.getSpid() != config_.getSpid())
                {
                    String msg = "Account " + ban + " SPID and WriteOff configuration SPID are not match";
                    logError(ctx, writeOffError, writeOff, msg);
                    continue;
                }
                if (account.getSystemType() == SubscriberTypeEnum.PREPAID)
                {
                    String msg = "Prepaid account cannot be written off";
                    logAccountWriteOffError(ctx, writeOffError, account, msg);
                    continue;
                }
                if (PaymentPlanSupportHelper.get(ctx).isValidPaymentPlan(ctx, account.getPaymentPlan()))
                {
                    String msg = "Account has a valid payment plan";
                    logAccountWriteOffError(ctx, writeOffError, account, msg);
                    continue;
                }
                if (account.getWrittenOff())
                {
                    String msg = "Account has been written off";
                    logAccountWriteOffError(ctx, writeOffError, account, msg);
                    continue;
                }
                if (config_.isToDeactivate())
                {
                    account = deactivateAccount(ctx, account, writeOffError);
                    if (account == null || account.getState() != AccountStateEnum.INACTIVE)
                    {
                        logError(ctx, writeOffError, writeOff, "Account " + ban + " fails to deactivate.");
                        continue;
                    }
                }
                AccountWriteOffProcessor.Result writeOffRet = null;
                try
                {
                    AccountWriteOffProcessor processor = new AccountWriteOffProcessor(ctx, account, writeOff,
                            writeOffError);
                    writeOffRet = processor.process(ctx);
                }
                catch (HomeException e)
                {
                    logAccountWriteOffError(ctx, writeOffError, account, e.getMessage());
                    continue;
                }
                if (writeOffRet != null && writeOffRet.IsSuccess)
                {
                    generateReport(ctx, writeOffRet, account, writeOff.ExternalTransactionId, writeOffResult,
                            vatReclaimable);
                }
            }
        }
        catch (FileNotFoundException e)
        {
            LogSupport.major(ctx, this, "Failed to create the report file, write-off aborts...", e);
        }
        catch (Throwable t)
        {
            LogSupport.major(ctx, this, "Exception encountered during Write-off process, abort...", t);
        }
        finally
        {
            writeOffResult.close();
            vatReclaimable.close();
            writeOffError.close();
        }
    }


    private void generateReport(Context ctx, AccountWriteOffProcessor.Result writeOffRet, Account account, long extNum,
            PrintWriter writeOffResult, PrintWriter vatReclaimable)
    {
        long writeOffAmount = writeOffRet.WriteOffAmount;
        WriteOffResult result = new WriteOffResult(ctx, account.getBAN(), writeOffAmount);
        result.setAccountName(account.getAccountName()).setAccountResponsible(account.getResponsible())
                .setAccountState(account.getState().getIndex()).setBillCycleId(account.getBillCycleID())
                .setCreditTxnRefNum(extNum).setDate(new Date()).setFirstName(account.getFirstName())
                .setLastName(account.getLastName());
        result.print(writeOffResult);
        long taxPaid = writeOffRet.TaxPaid;
        VatReclaimableReport vat = new VatReclaimableReport(ctx, account.getSpid(), account.getBAN(), taxPaid);
        long vatableAmount = writeOffAmount - taxPaid;
        vat.setAccountName(account.getAccountName()).setAccountResponsible(account.getResponsible())
                .setAccountState(account.getState().getIndex())
                .setAccountType(account.getAccountCategory().getIdentifier()).setBillCycleId(account.getBillCycleID())
                .setFirstName(account.getFirstName()).setLastName(account.getLastName())
                .setQualifyingAmount(vatableAmount).setFormattedQualifyingAmount(vatableAmount).setExtTxnNum(extNum);
        vat.print(vatReclaimable);
    }


    private String getCurrentTimeStamp()
    {
        return timeStampFormat.format(new Date());
    }

    private static SimpleDateFormat timeStampFormat = new SimpleDateFormat("HHmmss");


    private void logAccountWriteOffError(Context ctx, PrintWriter writeOffError, Account account, String msg)
    {
        ResultCode rCode = ResultCode.ACCOUNT_WRITE_OFF_FAILURE;
        logError(ctx, writeOffError, account, msg, rCode, null);
    }


    private void logError(Context ctx, PrintWriter writeOffError, Account account, String msg, ResultCode rCode,
            Throwable t)
    {
        WriteOffErrorReport.logError(writeOffError, rCode, msg, account);
        LogSupport.minor(ctx, this, msg + " - BAN=" + account.getBAN(), t);
    }


    private void logError(Context ctx, PrintWriter writeOffError, WriteOffInput writeOff, String msg)
    {
        ResultCode rCode = ResultCode.GENERAL_ERROR;
        WriteOffErrorReport.logError(writeOffError, rCode, msg);
        LogSupport.minor(ctx, this, msg + ". The original input is " + writeOff.OriginalString);
    }


    private Account deactivateAccount(Context ctx, Account account, PrintWriter errLog)
    {
        if (account.getState() == AccountStateEnum.INACTIVE)
            return account;
        try
        {
                Collection subAccounts = AccountSupport.getImmediateChildrenAccounts(ctx, account.getBAN());
                for (Iterator it = subAccounts.iterator(); it.hasNext();)
                {
                    Account subAcc = (Account) it.next();
                    subAcc = deactivateAccount(ctx, subAcc, errLog);
                    if (subAcc.getState() != AccountStateEnum.INACTIVE)
                    {
                        logAccountDeactError(ctx, errLog, subAcc, "Failed to deactivate a reponsible sub-account", null);
                        return null;
                    }
                }
        }
        catch (HomeException e)
        {
            logAccountDeactError(ctx, errLog, account, "Failed to retrieve the resposible sub-accounts", e);
        }
        try
        {
            Collection subs = AccountSupport.getNonResponsibleSubscribers(ctx, account);
            boolean ret = deactivateSubscribers(ctx, account, subs, errLog);
            if (!ret)
                return null;
        }
        catch (HomeException e)
        {
            logAccountDeactError(ctx, errLog, account,
                    "Unable to fetch the non-responsible subscribers under the account", e);
            return null;
        }
        // retrieve account again to refresh references in account obj
        account = getAccount(ctx, account.getBAN());
        if (account == null)
            return account;
        account.setState(AccountStateEnum.INACTIVE);
        Home accHome = (Home) ctx.get(AccountHome.class);
        try
        {
            account.setReason(DeactivatedReasonEnum.WRITE_OFF.getDescription());
            account = (Account) accHome.store(ctx, account);
            return account;
        }
        catch (Exception e)
        {
            logAccountDeactError(ctx, errLog, account,
                    e.getMessage(), e);
            return null;
        }
    }


    private void logAccountDeactError(Context ctx, PrintWriter errLog, Account account, String msg, Throwable t)
    {
        logError(ctx, errLog, account, msg, ResultCode.ACCOUNT_DEACTIVATION_FAILURE, t);
    }


    private boolean deactivateSubscribers(Context ctx, Account account, Collection subs, PrintWriter errLog)
    {
        Subscriber leader = null;
        if(account.getGroupType() != GroupTypeEnum.SUBSCRIBER)
        {
            try
            {
                leader = account.getGroupSubscriber(ctx);
            }
            catch (HomeException e)
            {
                logAccountDeactError(ctx, errLog, account, "Failed to locate group leader.", null);
                return false;
            }
            subs.remove(leader);
        }
        Home home = (Home) ctx.get(SubscriberHome.class);
        for (Iterator it = subs.iterator(); it.hasNext();)
        {
            Subscriber sub = (Subscriber) it.next();
            boolean ret = deactivateSubscriber(ctx, account, errLog, home, sub);
            if (!ret)
                return ret;
        }
        if (leader != null)
            return deactivateSubscriber(ctx, account, errLog, home, leader);
        else
            return true;
    }


    private boolean deactivateSubscriber(Context ctx, Account account, PrintWriter errLog, Home home, Subscriber sub)
    {
        if (sub.getState() == SubscriberStateEnum.INACTIVE)
            return true;
        sub.setState(SubscriberStateEnum.INACTIVE);
        try
        {
            Subscriber ret = (Subscriber) home.store(sub);
            if (ret.getState() != SubscriberStateEnum.INACTIVE)
            {
                logSubDeactError(ctx, errLog, account, sub, null);
                return false;
            }
            return true;
        }
        catch (Exception e)
        {
            logSubDeactError(ctx, errLog, account, sub, e);
            try
            {
                sub = SubscriberSupport.getSubscriber(ctx, sub.getId());
            }
            catch (HomeException e1)
            {
                return false;
            }
            return sub.getState() == SubscriberStateEnum.INACTIVE;
        }
    }


    private void logSubDeactError(Context ctx, PrintWriter errLog, Account account, Subscriber sub, Exception e)
    {
        ResultCode rCode = ResultCode.SUBSCRIBER_DEACTIVATION_FAILURE;
        String msg = "Failed to deactivate subscriber";
        WriteOffErrorReport.logError(errLog, rCode, msg, account, sub);
        LogSupport.minor(ctx, this, msg + ". SubId=" + sub.getId(), e);
    }


    private Account getAccount(Context ctx, String ban)
    {
        Account ret = null;
        try
        {
            ret = AccountSupport.getAccount(ctx, ban);
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Failed to fetch account for BAN: " + ban, e);
        }
        return ret;
    }


    private Collection<WriteOffInput> parse(Context ctx, String fileName) throws HomeException
    {
        Collection<WriteOffInput> inputs = new ArrayList<WriteOffInput>();
        char seperator = com.redknee.framework.xhome.csv.Constants.DEFAULT_SEPERATOR;
        for (Iterator it = new CSVIterator(WriteOffInputCsvSupport.Instance, fileName, seperator); it.hasNext();)
        {
            try
            {
                WriteOffInput writeOff = (WriteOffInput) it.next();
                inputs.add(writeOff);
            }
            catch (Throwable t)
            {
                LogSupport.minor(ctx, this, "Exception detected during parsing input write-off file.", t);
            }
        }
        return inputs;
    }

    private final String date_;
    private final WriteOffConfig config_;
}