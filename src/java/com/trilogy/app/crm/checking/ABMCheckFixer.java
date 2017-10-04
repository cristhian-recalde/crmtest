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
package com.trilogy.app.crm.checking;

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.product.bundle.manager.provision.profile.error.ErrorCode;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberInvoice;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.calculation.support.CalculationServiceSupport;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.Parameters;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.util.SubscriberProcessingInterruptionException;


/**
 * @author lxia
 * @author skushwaha
 */
public class ABMCheckFixer extends AbstractIntegrityValidation
{

    /**
     * {@inheritDoc}
     */
    @Override
    public void printResults()
    {
        print("TOTAL: " + totalCount_);
        if (isRepairEnabled())
        {
            print("error:" + errorCount_);
            print("fixed: " + changeCount_);
            print("failed: " + failCount_);
            print("no voice Service or service stopped:" + noServiceCount_);
            print("No need fix:" + noChangeCount_);
        }
        else
        {
            print("Error: " + errorCount_);
            print("no Service or service stopped: " + noServiceCount_);
            //print("No Error: " + noChangeCount_); 
        }
        if (notExistOnEcpCount_ > 0)
        {
            print("Subscriber not exist on ECP: " + notExistOnEcpCount_);
        }
        if (ghostCount_ > 0)
        {
            print("Ghost profile on ECP: " + ghostCount_);

        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void process(final Context ctx, final Subscriber sub) throws SubscriberProcessingInterruptionException
    {
        Home acctHome = (Home) ctx.get(AccountHome.class);

        SubscriberProfileProvisionClient bmClient = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);

        ++totalCount_;

        boolean ownsMsisdn = true;
        try
        {
            ownsMsisdn = SubscriberSupport.ownsMSISDN(ctx, sub);
        }
        catch (HomeException e)
        {
            ownsMsisdn = false;
        }

        if (sub.getState() == SubscriberStateEnum.PENDING || sub.getState() == SubscriberStateEnum.INACTIVE)
        {
            print("Subscriber " + sub.getMSISDN() + "no longer owns MSISDN" + sub.getMSISDN());
            return;
        }

        if (ownsMsisdn)
        {
            // get account
            Account account = null;
            if (sub != null && !SubscriberStateEnum.INACTIVE.equals(sub.getState()))
            {
                try
                {
                    account = (Account) acctHome.find(ctx, sub.getBAN());
                }
                catch (final HomeException e)
                {
                    print(" Account " + sub.getBAN() + "not found");
                    return;
                }
            }

            if (account != null)
            {
				if (account.isPooled(ctx)
				    && !sub.getMSISDN().equals(account.getPoolMSISDN()))
                {
                    return;
                }
            }

			if (account != null)
            {
                boolean result = false;
                Parameters bmSub = null;
                try
                {
                    bmSub = bmClient.querySubscriptionProfile(ctx, sub);
                }
                catch (Exception e)
                {
                    print("Error occurred in process() while retreiving Sub profile " + sub.getId() + " "
                            + sub.getMSISDN() + ": " + e.getMessage());
                }

                if (bmSub != null)
                {
                    if (sub.getSubscriberType() == SubscriberTypeEnum.PREPAID)
                    {
						result =
						    prepaidSubCompareWithABM(ctx, sub, bmSub, account);
                    }
                    else
                    {
						result = subCompareWithABM(ctx, sub, bmSub, account);
                    }
                    if (!result)
                    {
                        ++errorCount_;
                    }
                    else
                    {
                        ++noChangeCount_;
                    }
                }
                else
                {
                    print("Subscriber " + sub.getMSISDN() + " doesn't exist on BM subscriber state is "
                            + sub.getState().getDescription() + " account state is "
                            + account.getState().getDescription());
                    ++notExistOnEcpCount_;
                }
                if (!result)
                {
                    if (isRepairEnabled() && sub.getSubscriberType() == SubscriberTypeEnum.PREPAID)
                    {
						if (prepaidSubSynchWithABM(ctx, sub, bmSub, account,
						    bmClient))
                        {
                            ++changeCount_;
                        }
                        else
                        {
                            ++failCount_;
                        }
                    }
                    else if (isRepairEnabled())
                    {
						if (subSynchWithABM(ctx, sub, bmSub, account, bmClient))
                        {
                            ++changeCount_;
                        }
                        else
                        {
                            ++failCount_;
                        }
                    }
                }

            }
            else
            {
                Parameters bmSub = null;
                try
                {
                    bmSub = bmClient.querySubscriptionProfile(ctx, sub);
                }
                catch (Exception e)
                {
                    print("Error occurred in process() while retreiving Sub profile " + sub.getId() + " "
                            + sub.getMSISDN() + ": " + e.getMessage());
                }
                if (bmSub != null)
                {
                    ++ghostCount_;
                    print("Subscriber " + sub.getMSISDN() + " is a ghost profile on BM subscriber state is "
                            + sub.getState().getDescription() + " account state is "
                            + account.getState().getDescription());
                }
                else
                {
                    if (account == null)
                    {
                        account = new Account();
                        account.setState(AccountStateEnum.INACTIVE);
                    }

                    print("Subscriber " + sub.getMSISDN() + " doesn't exist on BM subscriber state is "
                            + sub.getState().getDescription() + " account state is "
                            + account.getState().getDescription());
                }
            }

        }
        else
        {
            print("Subscriber " + sub.getMSISDN() + "no longer owns MSISDN" + sub.getMSISDN());
            // ignore if the subscriber no longer own this msisdn
        }
    }


    // prepaid subscriber comparison 
    public boolean prepaidSubCompareWithABM(Context ctx, Subscriber crmSub, Parameters bmSub,
 Account account)
    {
        String default_msg = "subscriber MSISDN = " + crmSub.getMSISDN();
        String msg = default_msg;
        boolean ret = true;
        if (crmSub.getSubscriberType().equals(SubscriberTypeEnum.PREPAID))
        {
            print("Subscriber " + crmSub.getMSISDN() + " is prepaid subscriber");
            final Date crmDateNoTime = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(crmSub.getExpiryDate());
            final Date bmDateNoTime = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(bmSub.getExpiryDate());
            if (crmSub.getExpiryDate() == null
                    && bmSub.getExpiryDate().getTime() > 0
                    || crmDateNoTime.compareTo(bmDateNoTime) != 0)
            {
                msg = msg.concat(" Expiry Date (CRM)"
                        + crmDateNoTime
                        + " Expiry Date (BM) "
                        + bmDateNoTime);
                ret = false;
            }
        }
        print(msg);
        return ret;
    }


	public boolean subCompareWithABM(Context ctx, Subscriber crmSub,
	    Parameters bmSub, Account account)
    {
        String default_msg = "subscriber MSISDN = " + crmSub.getMSISDN();
        String msg = default_msg;
        boolean ret = true;

        long balance = 0;

		if (account.isPooled(ctx))
        {
            balance = getAccountBalance(ctx, crmSub, account);
        }
        else
        {
            balance = getSubscriberBalance(ctx, crmSub, account);
        }

        if (!account.getCurrency().equals(bmSub.getCurrency()))
        {
            msg = msg.concat(" currency (CRM) " + account.getCurrency() + " (BM) " + bmSub.getCurrency());
            ret = false;
        }
        if (balance != bmSub.getBalance())
        {
            msg = msg.concat(" balance (CRM) " + balance + "  (BM) " + bmSub.getBalance());
            ret = false;
        }

        if (crmSub.getCreditLimit(ctx) != bmSub.getCreditLimit())
        {
            msg = msg.concat(" Creditlimit (CRM) " + crmSub.getCreditLimit(ctx) + "  (BM) " + bmSub.getCreditLimit());
            ret = false;
        }

        if (!crmSub.getPoolID(ctx).equals(bmSub.getPoolGroupID()))
        {
            msg = msg.concat(" Group Pool ID (CRM) " + crmSub.getPoolID(ctx) + "  (BM) " + bmSub.getPoolGroupID());
            ret = false;
        }

        if (!bmSub.getExpiryDate().equals(crmSub.getExpiryDate()))
        {
            msg = msg.concat(" Expiry Date (CRM) " + crmSub.getExpiryDate() + "  (BM) " + bmSub.getExpiryDate());
            ret = false;
        }

        if (msg.equals(default_msg))
        {
            msg = msg.concat(" BM profile is OK");
        }
        print(msg);
        return ret;
    }


    // prepaid subscriber Sync
    public boolean prepaidSubSynchWithABM(Context ctx, Subscriber crmSub, Parameters bmSub,
 Account account,
	    SubscriberProfileProvisionClient bmClient)
    {
        Home crmHome = (Home) ctx.get(SubscriberHome.class);
        if (crmHome == null)
        {
            print(" CRM Home not in context");
            return false;
        }
        try
        {
            crmSub.setExpiryDate(CalendarSupportHelper.get(ctx).convertDateWithNoTimeOfDayToTimeZone(bmSub.getExpiryDate(), crmSub.getTimeZone(ctx)));
            crmHome.store(ctx, crmSub);
        }
        catch (Exception e)
        {
            print("Fail to update the subscriber expiry date on CRM for prepaid subscriber " + crmSub.getMSISDN());
            return false;
        }
        return true;
    }


    public boolean subSynchWithABM(Context ctx, Subscriber crmSub, Parameters bmToUpdate, Account account,
	    SubscriberProfileProvisionClient bmClient)
    {
        long balance = 0;
		if (account.isPooled(ctx))
        {
            balance = getAccountBalance(ctx, crmSub, account);
        }
        else
        {
            balance = getSubscriberBalance(ctx, crmSub, account);
        }

        int result = 0;
        //long time = crmSub.getExpiryDate() == null?0:crmSub.getExpiryDate().getTime();

        try
        {
            bmClient.updateBalance(ctx, crmSub, balance);
            bmClient.updatePooledGroupID(ctx, crmSub, crmSub.getPoolID(ctx), false);
            bmClient.updateSubscriptionQuotaLimit(ctx, crmSub, crmSub.getQuotaLimit());
        }
        catch (Exception e)
        {
            return false;
        }
        if (result != ErrorCode.SUCCESS)
        {
            print("ABM synchronization failed. " + result);
            return false;
        }

        return true;
    }


    public long getSubscriberBalance(Context ctx, Subscriber crmSub, Account account)
    {
        ctx = ctx.createSubContext();

        String sessionKey = CalculationServiceSupport.createNewSession(ctx);
        try
        {
            long balanceForward = 0;

            CalculationService service = (CalculationService) ctx.get(CalculationService.class);
            SubscriberInvoice previousSubInvoice = null;
            try
            {
                previousSubInvoice = service.getMostRecentSubscriberInvoice(ctx, crmSub.getId(), new Date());
            }
            catch (CalculationServiceException e)
            {
                new MinorLogMsg(this, "Exception while fetching Subscriber Invoice for subscriber", e);
            }

            if (previousSubInvoice != null)
            {
                // Note, we must remove the tax amount from the total because
                // ABM does not acknowledge the tax amounts.
                balanceForward = previousSubInvoice.getTotalAmount() - previousSubInvoice.getTaxAmount();
            }
            
            long balance = 0;        
            try
            {
                long paymentReceived = service.getSubscriberPaymentsReceived(ctx, crmSub.getId(),
                        CalendarSupportHelper.get(ctx).getRunningDate(ctx));
                long subTotal = service.getSubTotal(ctx, crmSub.getId(), CalendarSupportHelper.get(ctx).getRunningDate(ctx));
                // Note: UPS uses the opposite sign for the balance, so we negate here.
                balance = -(int) (balanceForward + paymentReceived + subTotal);
            }
            catch (CalculationServiceException e)
            {
                new MinorLogMsg(this, "Exception while fetching payment for subscriber", e);
            }
            return balance;
        }
        finally
        {
            CalculationServiceSupport.endSession(ctx, sessionKey);
        }
	}
	
	
	public long getAccountBalance( Context ctx, Subscriber crmSub, Account account)
	{
        ctx = ctx.createSubContext();

        String sessionKey = CalculationServiceSupport.createNewSession(ctx);
        try
        {
            CalculationService service = (CalculationService) ctx.get(CalculationService.class);
            Invoice previousAcctInvoice = null;
            try
            {
                previousAcctInvoice = service.getMostRecentInvoice(ctx, crmSub.getBAN());
            }
            catch (CalculationServiceException e)
            {
                new MinorLogMsg(this, "Exception while fetching Subscriber Invoice for subscriber", e);
            }

            long balanceForward =0; 
            
            if (previousAcctInvoice != null)
            {
                // Note, we must remove the tax amount from the total because
                // UPS does not acknowledge the tax amounts.
                balanceForward =
                    previousAcctInvoice.getTotalAmount()
                    - previousAcctInvoice.getTaxAmount();
            }

            long balance = 0;
            try
            {
                long paymentReceived = service.getAccountPaymentsReceived(ctx, crmSub.getBAN(), CalendarSupportHelper.get(ctx).getRunningDate(ctx));
                long acctTotal = service.getAccountTotalBalance(ctx, crmSub.getBAN(), CalendarSupportHelper.get(ctx).getRunningDate(ctx));
                // Note: UPS uses the opposite sign for the balance, so we negate here.
                balance = -(int) (balanceForward + paymentReceived + acctTotal);
            }
            catch (CalculationServiceException e)
            {
                new MinorLogMsg(this, "Exception while fetching payment for subscriber", e);
            }
            
            return balance;
        }
        finally
        {
            CalculationServiceSupport.endSession(ctx, sessionKey);
        }
    }

    private int totalCount_ = 0;
    private int notExistOnEcpCount_ = 0;
    private int changeCount_ = 0;
    private int errorCount_ = 0;
    private int noChangeCount_ = 0;
    private int ghostCount_ = 0;
    private int failCount_ = 0;
    private final int noServiceCount_ = 0;

}
