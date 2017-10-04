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
package com.trilogy.app.crm.dunning.visitor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.core.locale.CurrencyXInfo;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.holder.LongHolder;
import com.trilogy.framework.xhome.holder.ObjectHolder;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.AbstractJDBCXDB;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.framework.xlog.log.SeverityEnum;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AgedDebt;
import com.trilogy.app.crm.bean.AgedDebtXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.bean.CreditCategoryXInfo;
import com.trilogy.app.crm.bean.DunningConfigurationEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.core.CreditCategory;
import com.trilogy.app.crm.dunning.DunningConstants;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.dunning.DunningReportRecord;
import com.trilogy.app.crm.dunning.DunningReportRecordAgedDebt;
import com.trilogy.app.crm.dunning.DunningReportRecordStatusEnum;
import com.trilogy.app.crm.home.TransactionRedirectionHome;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.state.InOneOfStatesPredicate;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.PaymentPlanSupport;
import com.trilogy.app.crm.support.PaymentPlanSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.app.crm.troubleticket.bean.Dispute;
import com.trilogy.app.crm.troubleticket.bean.DisputeHome;
import com.trilogy.app.crm.troubleticket.bean.DisputeStateEnum;
import com.trilogy.app.crm.troubleticket.bean.DisputeXInfo;


/**
 * Abstract class responsible to process accounts during dunning.
 *
 * @author Marcio Marques
 * @since 9.0
 */
public abstract class AbstractDunningAccountProcessor implements Visitor
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new AbstractDunningAccountVisitor visitor.
     *
     * @param runningDate
     */
    public AbstractDunningAccountProcessor(Date runningDate)
    {
        runningDate_ = runningDate;
    }


    /**
     * Returns the process name.
     *
     * @return
     */
    abstract public String getProcessName();


    /**
     * Logic executed when account is dunning exempt.
     *
     * @param context
     * @param account
     * @throws DunningProcessException
     */
    protected void executeOnDunningExempt(final Context context, final Account account,
            final Account responsibleAccount) throws DunningProcessException
    {
        // No action required by default.  Override to add behaviour.
    }


    /**
     * Action executed when the forecasted state requires dunning.
     *
     * @param context
     * @param account
     * @param dunningReportRecord
     * @throws DunningProcessException
     */
    protected void executeOnActionRequired(final Context context, final Account account,
            final Account responsibleAccount, final DunningReportRecord dunningReportRecord)
            throws DunningProcessException
    {
        // No action required by default.  Override to add behaviour.
    }

    /**
     * Action executed when the forecasted state doesn't require dunning.
     *
     * @param context
     * @param account
     * @param dunningReportRecord
     * @throws DunningProcessException
     */
    protected void executeOnActionNotRequired(final Context context, final Account account,
            final Account responsibleAccount, final DunningReportRecord dunningReportRecord)
            throws DunningProcessException
    {
        // No action required by default.  Override to add behaviour.
    }

    /**
     * Action executed when a DunningProcessException is found.
     *
     * @param context
     * @param account
     * @param e
     */
    protected void executeOnDunningProcessException(final Context context, final Account account,
            final DunningProcessException e)
    {
        // No action required by default.  Override to add behaviour.
    }


    /**
     * Retrieves the responsible account.
     *
     * @param context
     * @param account
     * @return
     * @throws IllegalStateException
     */
    protected Account getResponsibleAccout(final Context context, final Account account) throws IllegalStateException
    {
        try
        {
            return account.getResponsibleParentAccount(context);
        }
        catch (final HomeException exception)
        {
            String cause = "Unable to retrieve responsible parent account";
            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(" for account '");
            sb.append(account.getBAN());
            sb.append("': ");
            sb.append(exception.getMessage());
            LogSupport.minor(context, this, sb.toString(), exception);
            throw new IllegalStateException(sb.toString(), exception);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
    {
        try
        {
            process(ctx, obj);
        }
        catch (DunningProcessException e)
        {
            throw new AgentException(e);
        }
    }

    protected boolean processOnlyExactDate()
    {
        return false;
    }


    /**
     * {@inheritDoc}
     */
    protected DunningReportRecord process(Context context, Object obj) throws DunningProcessException
    {
        final Account account = (Account) obj;
        String processName = getProcessName();
        boolean notificationProcess = processName.equals("Pre-Dunning Notification Processing");
        final Account responsibleAccount = getResponsibleAccout(context, account);
        
        if (LogSupport.isDebugEnabled(context))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Executing ");
            sb.append(processName);
            sb.append(" for account '");
            sb.append(responsibleAccount.getBAN());
            sb.append("'");
            if (!responsibleAccount.getBAN().equals(account.getBAN()))
            {
                sb.append(", which is the responsible account for account '");
                sb.append(account.getBAN());
                sb.append("'");
            }
            LogSupport.debug(context, this, sb.toString());
        }
        StringBuilder accountDetails = new StringBuilder();
        accountDetails.append("Account BAN = '");
        accountDetails.append(responsibleAccount.getBAN());
        accountDetails.append("'");
        String details = accountDetails.toString();
        final PMLogMsg pmLogMsg = new PMLogMsg(getProcessName(), "Account dunning logic", details);
        final PMLogMsg pmLogMsgError = new PMLogMsg(getProcessName(), "Account dunning logic - Failure", details);
        final PMLogMsg pmLogMsgExempt = new PMLogMsg(getProcessName(), "Account dunning logic - Exempt accounts",
                details);
        final Context subCtx = context.createSubContext();
        subCtx.put(DunningConstants.CONTEXT_KEY_IS_IN_DUNNING, true);
        //Set this to default fetch size.
        subCtx.put(AbstractJDBCXDB.FETCH_SIZE_KEY,AbstractJDBCXDB.FETCH_SIZE);

        DunningReportRecord result = null;
        try
        {
            updateAccountPTPTightened(subCtx, responsibleAccount);
            boolean creditCategoryDunningExempt = isCreditCategoryDunningExempt(context, responsibleAccount);

			if (!creditCategoryDunningExempt
			    && !isAccountTemporaryDunningExempt(subCtx, responsibleAccount))
            {
				if(LogSupport.isDebugEnabled(subCtx))
				    {
				        LogSupport.debug(subCtx, this, "Creating dunning report record for account : " + responsibleAccount + ", Running date : " + CoreERLogger.formatERDateDayOnly(runningDate_));
				}
                DunningReportRecord dunningReportRecord = generateReportRecord(subCtx, responsibleAccount,
                        creditCategoryDunningExempt);
                if (dunningReportRecord != null
                       /* && (notificationProcess || (!notificationProcess && !dunningReportRecord.getForecastedLevel().equals(responsibleAccount.getState())))*/)
                {
                    executeOnActionRequiredLoggingPM(subCtx, account, responsibleAccount, dunningReportRecord);
                    result = dunningReportRecord;
                }
                else
                {
                    executeOnActionNotRequiredLoggingPM(subCtx, account, responsibleAccount, dunningReportRecord);
                }
                pmLogMsg.log(subCtx);
            }
            else if(AccountStateEnum.PROMISE_TO_PAY.equals(account.getState())
                    && runningDate_.before(account.getPromiseToPayDate()))
            {
            	DunningReportRecord record = generateReportRecord(subCtx, responsibleAccount, creditCategoryDunningExempt);

            	if(AccountStateEnum.IN_ARREARS.equals(record.getForecastedLevel()))
            	{
            		updateAccountLastStateChangeDate(subCtx, responsibleAccount, record);

                    final Home accountHome = (Home) subCtx.get(AccountHome.class);
                    if (accountHome == null)
                    {
                        throw new DunningProcessException("Account home not found in context. Installation failed.");
                    }
                    try
                    {
                    	account.setLastInvoiceDunnedDueDate(record.getDunnedAgedDebt()!=null ? record.getDunnedAgedDebt().getDueDate() : null);
                        accountHome.store(subCtx, responsibleAccount);
                    }
                    catch (final HomeException e)
                    {
                        String cause = "Unable to update account lastStateChangeDate";
                        StringBuilder sb = new StringBuilder();
                        sb.append(cause);
                        sb.append(" for account '");
                        sb.append(responsibleAccount.getBAN());
                        sb.append("': ");
                        sb.append(e.getMessage());
                        LogSupport.minor(subCtx, this, sb.toString(), e);
                        throw new DunningProcessException(cause, e);
                    }
            	}



                executeOnDunningExemptLoggingPM(subCtx, account, responsibleAccount);
                pmLogMsgExempt.log(subCtx);
            }
			else
			{
                executeOnDunningExemptLoggingPM(subCtx, account, responsibleAccount);
                pmLogMsgExempt.log(subCtx);
			}
        }
        catch (DunningProcessException e)
        {
            pmLogMsgError.log(subCtx);
            executeOnDunningProcessException(subCtx, account, e);
            StringBuilder sb = new StringBuilder();
            sb.append(processName);
            sb.append(" failed for account '");
            sb.append(responsibleAccount.getBAN());
            sb.append("' -> ");
            sb.append(e.getMessage());
            LogSupport.minor(subCtx, this, sb.toString(), e);
            throw new DunningProcessException(sb.toString(), e);
        }

        return result;
    }

    /**
     * Logic executed when account is dunning exempt.
     *
     * @param context
     * @param account
     * @throws DunningProcessException
     */    private void executeOnDunningExemptLoggingPM(final Context context, final Account account,
            final Account responsibleAccount) throws DunningProcessException
    {
         StringBuilder accountDetails = new StringBuilder();
         accountDetails.append("Account BAN = '");
         accountDetails.append(responsibleAccount.getBAN());
         accountDetails.append("'");
         String details = accountDetails.toString();
         final PMLogMsg pmLogMsg = new PMLogMsg(getProcessName(), "Account dunning logic - dunning exempt execution", details);

         try
         {
             executeOnDunningExempt(context, account, responsibleAccount);
         }
         finally
         {
             pmLogMsg.log(context);
         }
    }


    /**
     * Action executed when the forecasted state requires dunning.
     *
     * @param context
     * @param account
     * @param dunningReportRecord
     * @throws DunningProcessException
     */
    private void executeOnActionRequiredLoggingPM(final Context context, final Account account,
            final Account responsibleAccount, final DunningReportRecord dunningReportRecord)
            throws DunningProcessException
    {
        StringBuilder accountDetails = new StringBuilder();
        accountDetails.append("Account BAN = '");
        accountDetails.append(responsibleAccount.getBAN());
        accountDetails.append("'");
        String details = accountDetails.toString();
        final PMLogMsg pmLogMsg = new PMLogMsg(getProcessName(), "Account dunning logic - action required execution", details);

        try
        {
            executeOnActionRequired(context, account, responsibleAccount, dunningReportRecord);
        }
        finally
        {
            pmLogMsg.log(context);
        }
    }

    /**
     * Action executed when the forecasted state doesn't require dunning.
     *
     * @param context
     * @param account
     * @param dunningReportRecord
     * @throws DunningProcessException
     */
    private void executeOnActionNotRequiredLoggingPM(final Context context, final Account account,
            final Account responsibleAccount, final DunningReportRecord dunningReportRecord)
            throws DunningProcessException
    {
        StringBuilder accountDetails = new StringBuilder();
        accountDetails.append("Account BAN = '");
        accountDetails.append(responsibleAccount.getBAN());
        accountDetails.append("'");
        String details = accountDetails.toString();
        final PMLogMsg pmLogMsg = new PMLogMsg(getProcessName(), "Account dunning logic - action not required execution", details);

        try
        {
            executeOnActionNotRequired(context, account, responsibleAccount, dunningReportRecord);
        }
        finally
        {
            pmLogMsg.log(context);
        }
    }

    private void updateAccountLastStateChangeDate(Context ctx, Account account, DunningReportRecord dunningReportRecord) throws DunningProcessException
    {
		final CreditCategory creditCategory = retrieveCreditCategory(ctx, account);
        final CRMSpid spid = retrieveSpid(ctx, account);
        int graceDaysInArrears = getGraceDaysInArrears(spid, creditCategory);

        Date dueDate = dunningReportRecord.getDunnedAgedDebt().getDueDate();

		if(LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, this, "Dunning Aged debt due date for ban:" + account.getBAN() + " is " + dueDate);
		}

        Calendar inArrearsCalendar = Calendar.getInstance();
        inArrearsCalendar.setTime(dueDate);
        inArrearsCalendar.add(Calendar.DAY_OF_YEAR, graceDaysInArrears);

		final Date inArrearsDate = inArrearsCalendar.getTime();//getMaxDueDate(ctx, spid, creditCategory, AccountStateEnum.IN_ARREARS);

		account.setLastStateChangeDate(inArrearsDate);

		if(LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, this, "Updated account state change date for account "+account.getBAN()+" to In-Arrears due date "+inArrearsDate);
		}
    }

    /**
     * Modify the account state
     *
     * @param ctx
     * @param state
     * @param account
     * @param dunningReportRecord TODO
     * @throws DunningProcessException
     */
    protected void modifyAccountState(final Context ctx, final AccountStateEnum state, final Account account, DunningReportRecord dunningReportRecord)
            throws DunningProcessException
    {
        if (account.getState().equals(state))
        {
            return;
        }
        if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Modifying state for account '");
            sb.append(account.getBAN());
            sb.append("' to '");
            sb.append(state.getDescription());
            sb.append("'");
            LogSupport.info(ctx, this, sb.toString());
        }

        {
        	if(state == AccountStateEnum.IN_COLLECTION ||	state == AccountStateEnum.PROMISE_TO_PAY
        			||	state == AccountStateEnum.NON_PAYMENT_WARN ||	state == AccountStateEnum.NON_PAYMENT_SUSPENDED
        			||  state == AccountStateEnum.IN_ARREARS
        		)
        	{

        		updateAccountLastStateChangeDate(ctx, account, dunningReportRecord);
        	}
        }

        account.setState(state);
        /*
         * [Cindy Wong] 2008-04-11: Don't reset payment plan if the account is leaving
         * dunning.
         */
        if (PaymentPlanSupportHelper.get(ctx).isEnabled(ctx) && !SafetyUtil.safeEquals(state, AccountStateEnum.ACTIVE))
        {
            account.setPaymentPlan(PaymentPlanSupport.INVALID_PAYMENT_PLAN_ID);
        }
        final Home accountHome = (Home) ctx.get(AccountHome.class);
        if (accountHome == null)
        {
            throw new DunningProcessException("Account home not found in context. Installation failed.");
        }
        
        
        try
        {
        	/**
        	 * Storing the dueDate of invoice which is dunned. This will be read during paymentOverdue task to send overdue reminder.
        	 * Please refer PaymentOverdueNoticeLifeCycleAgent for Payment Overdue Reminder 
        	 */
        	account.setLastInvoiceDunnedDueDate(dunningReportRecord.getDunnedAgedDebt()!=null ? dunningReportRecord.getDunnedAgedDebt().getDueDate() : null);
            accountHome.store(ctx, account);
        }
        catch (final HomeException e)
        {
            String cause = "Unable to update account state";
            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(" for account '");
            sb.append(account.getBAN());
            sb.append("': ");
            sb.append(e.getMessage());
            LogSupport.minor(ctx, this, sb.toString(), e);
            throw new DunningProcessException(cause, e);
        }
    }


    /**
     * Updates the Account PTPTightened field
     *
     * @param context
     * @param account
     */
    private void updateAccountPTPTightened(final Context context, final Account account)
    {
        /* setting PTPTermsTightened to false and storing it */
        if (account.getPtpTermsTightened())
        {
            account.setPtpTermsTightened(false);
            final Home home = (Home) context.get(AccountHome.class);
            try
            {
                home.store(context, account);
            }
            catch (final Exception exception)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Unable to update account PTPTermsTightened for account '");
                sb.append(account.getBAN());
                sb.append("': ");
                sb.append(exception.getMessage());
                LogSupport.minor(context, this, sb.toString(), exception);
            }
        }
    }


    /**
     * Indicates whether or not the current account is exempt entirely from the dunning
     * process.
     *
     * @param ctx
     *            The current context.
     * @return True if the current account is exempt from the dunning process; false
     *         otherwise.
     */
    private boolean isCreditCategoryDunningExempt(final Context ctx, final Account account)
    {
        boolean result = false;
        CreditCategory category;
        try
        {
            category = HomeSupportHelper.get(ctx).findBean(ctx, CreditCategory.class,
                    new EQ(CreditCategoryXInfo.CODE, Integer.valueOf(account.getCreditCategory())));
        }
        catch (HomeException e)
        {
            category = null;
        }
        if (category != null)
        {
            result = category.isDunningExempt();
        }
        else
        {
            result = false;
            StringBuilder sb = new StringBuilder();
            sb.append("Could not find credit category '");
            sb.append(account.getCreditCategory());
            sb.append("' for account '");
            sb.append(account.getBAN());
            sb.append("'. Assuming that exemption status is false.");
            LogSupport.minor(ctx, this, sb.toString(), null);
        }
        if (result == true)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Account '");
                sb.append(account.getBAN());
                sb.append("' is dunning exempt: Credit category is dunning exempt.'");
                LogSupport.debug(ctx, this, sb.toString(), null);
            }
        }
        return result;
    }


    /**
     * Verifies if an account is dunning exempt
     *
     * @param context
     * @param account
     * @return
     * @throws DunningProcessException
     */
    private boolean isAccountTemporaryDunningExempt(final Context context, final Account account)
            throws DunningProcessException
    {
        boolean result = false;
        if (AccountStateEnum.IN_COLLECTION.equals(account.getState()))
        {
            if (LogSupport.isDebugEnabled(context))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Account '");
                sb.append(account.getBAN());
                sb.append("' is dunning exempt: '");
                sb.append(AccountStateEnum.IN_COLLECTION.getDescription());
                sb.append("' state.");
                LogSupport.debug(context, this, sb.toString(), null);
            }
            result = true;
        }
        else if (AccountStateEnum.PROMISE_TO_PAY.equals(account.getState())
                && runningDate_.before(account.getPromiseToPayDate()))
        {
            if (LogSupport.isDebugEnabled(context))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Account '");
                sb.append(account.getBAN());
                sb.append("' is dunning exempt: '");
                sb.append(AccountStateEnum.PROMISE_TO_PAY.getDescription());
                sb.append("' state with expiry date by ");
                sb.append(formatPromiseToPayDate(account));
                sb.append(".");
                LogSupport.debug(context, this, sb.toString(), null);
            }
            result = true;
        }
        else
        {
            final Collection<Subscriber> subscribers = getDunnableSubscribers(context, account);
            final Collection<Account> subAccounts = getDunnableNonResponsibleSubAccounts(context, account);
            if ((subscribers == null || subscribers.size() == 0) && (subAccounts == null || subAccounts.size() == 0))
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Account '");
                    sb.append(account.getBAN());
                    sb
                            .append("' is dunning exempt: No dunnable subscriptions or non-responsible sub-accounts to be processed.");
                    LogSupport.debug(context, this, sb.toString(), null);
                }
                // ignored
                result = true;
            }
        }
        if (result)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Account '");
            sb.append(account.getBAN());
            sb.append("' is dunning exempt.");
            LogSupport.info(context, this, sb.toString());
        }
        return result;
    }


    /**
     * Retrieve all subscribers from account that are dunnable.
     *
     * @param ctx
     * @param account
     * @return
     * @throws DunningProcessException
     */
    public static Collection<Subscriber> getDunnableSubscribers(final Context ctx, final Account account)
            throws DunningProcessException
    {
        Collection<Subscriber> subscribers = null;
        try
        {
            subscribers = account.getImmediateChildrenSubscribers(ctx);
            final And filter = new And();
            filter.add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.POSTPAID));
            filter.add(new InOneOfStatesPredicate<SubscriberStateEnum>(SubscriberStateEnum.IN_ARREARS,
                    SubscriberStateEnum.NON_PAYMENT_WARN, SubscriberStateEnum.IN_COLLECTION,
                    SubscriberStateEnum.PROMISE_TO_PAY, SubscriberStateEnum.NON_PAYMENT_SUSPENDED,
                    SubscriberStateEnum.ACTIVE,SubscriberStateEnum.SUSPENDED));
            subscribers = CollectionSupportHelper.get(ctx).findAll(ctx, subscribers, filter);
        }
        catch (final HomeException e)
        {
            String cause = "Unable to retrieve subscriptions";
            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(" for account '");
            sb.append(account.getBAN());
            sb.append("': ");
            sb.append(e.getMessage());
            LogSupport.minor(ctx, AbstractDunningAccountProcessor.class, sb.toString(), e);
            throw new DunningProcessException(cause, e);
        }
        return subscribers;
    }


    /**
     * Retrieves all non-responsible sub-accounts that are dunnable.
     *
     * @param ctx
     * @param account
     * @return
     * @throws DunningProcessException
     */
    private Collection<Account> getDunnableNonResponsibleSubAccounts(final Context ctx, final Account account)
            throws DunningProcessException
    {
        Collection<Account> accounts = null;
        try
        {
            accounts = account.getImmediateNonResponsibleChildrenAccounts(ctx);
            And filter = new And();
            filter.add(new Not(new EQ(AccountXInfo.SYSTEM_TYPE, SubscriberTypeEnum.PREPAID)));
            filter.add(new InOneOfStatesPredicate<AccountStateEnum>(AccountStateEnum.IN_ARREARS,
                    AccountStateEnum.NON_PAYMENT_WARN, AccountStateEnum.IN_COLLECTION, AccountStateEnum.PROMISE_TO_PAY,
                    AccountStateEnum.NON_PAYMENT_SUSPENDED, AccountStateEnum.ACTIVE));
            accounts = CollectionSupportHelper.get(ctx).findAll(ctx, accounts, filter);
        }
        catch (final HomeException e)
        {
            String cause = "Unable to retrieve non-responsible sub-accounts";
            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(" for account '");
            sb.append(account.getBAN());
            sb.append("': ");
            sb.append(e.getMessage());
            LogSupport.minor(ctx, this, sb.toString(), e);
            throw new DunningProcessException(cause, e);
        }
        return accounts;
    }


    /**
     * Gets a formatted version of the promise-to-pay date. If no date is set, then "[No
     * Promise-to-Pay Date Set]" is returned.
     *
     * @param account
     *            Account from which to get the promise-to-pay date.
     * @return A formatted version of the promise-to-pay date.
     */
    private String formatPromiseToPayDate(final Account account)
    {
        String promiseToPayDate;
        if (account.getPromiseToPayDate() == null)
        {
            promiseToPayDate = "[No Promise-to-Pay Date Set]";
        }
        else
        {
            promiseToPayDate = CoreERLogger.formatERDateDayOnly(account.getPromiseToPayDate());
        }
        return promiseToPayDate;
    }


    /**
     * Retrieves the SPID for an account.
     *
     * @param context
     * @param account
     * @return
     */
    protected CRMSpid retrieveSpid(final Context context, final Account account) throws DunningProcessException
    {
        CRMSpid spid = (CRMSpid) context.get(CRMSpid.class);

        if (spid==null || spid.getSpid()!=account.getSpid())
        {
            try
            {
                spid = HomeSupportHelper.get(context).findBean(context, CRMSpid.class,
                        new EQ(CRMSpidXInfo.ID, Integer.valueOf(account.getSpid())));
            }
            catch (final HomeException exception)
            {
                StringBuilder cause = new StringBuilder();
                cause.append("Unable to retrieve SPID '");
                cause.append(account.getSpid());
                cause.append("'");
                StringBuilder sb = new StringBuilder();
                sb.append(cause);
                sb.append(" for account '");
                sb.append(account.getBAN());
                sb.append("': ");
                sb.append(exception.getMessage());
                LogSupport.minor(context, this, sb.toString(), exception);
                throw new DunningProcessException(cause.toString(), exception);
            }
        }

        if (spid == null)
        {
            StringBuilder cause = new StringBuilder();
            cause.append("SPID '");
            cause.append(account.getSpid());
            cause.append("' not found");
            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(" for account '");
            sb.append(account.getBAN());
            sb.append("'");
            LogSupport.minor(context, this, sb.toString());
            throw new DunningProcessException(cause.toString());
        }
        return spid;
    }


    /**
     * Retrieves the credit category for an account.
     *
     * @param context
     * @param account
     * @return
     */
    protected CreditCategory retrieveCreditCategory(final Context context, final Account account)
            throws DunningProcessException
    {
        CreditCategory creditCategory = null;
        try
        {
            creditCategory = HomeSupportHelper.get(context).findBean(context, CreditCategory.class,
                    new EQ(CreditCategoryXInfo.CODE, Integer.valueOf(account.getCreditCategory())));
        }
        catch (final Exception exception)
        {
            StringBuilder cause = new StringBuilder();
            cause.append("Unable to retrieve credit category '");
            cause.append(account.getCreditCategory());
            cause.append("'");
            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(" for account '");
            sb.append(account.getBAN());
            sb.append("': ");
            sb.append(exception.getMessage());
            LogSupport.minor(context, this, sb.toString(), exception);
            throw new DunningProcessException(cause.toString(), exception);
        }
        if (creditCategory == null)
        {
            StringBuilder cause = new StringBuilder();
            cause.append("Credit category '");
            cause.append(account.getCreditCategory());
            cause.append("' not found");
            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(" for account '");
            sb.append(account.getBAN());
            sb.append("'");
            LogSupport.minor(context, this, sb.toString());
            throw new DunningProcessException(cause.toString());
        }
        return creditCategory;
    }


    /**
     * Retrieves the currency for an account.
     *
     * @param context
     * @param account
     * @return
     */
    private Currency retrieveCurrency(final Context context, final Account account) throws DunningProcessException
    {
        Currency currency = (Currency) context.get(Currency.class);
        if (currency==null || !currency.getCode().equals(account.getCurrency()))
        {
            try
            {
                currency = HomeSupportHelper.get(context).findBean(context, Currency.class,
                        new EQ(CurrencyXInfo.CODE, account.getCurrency()));
            }
            catch (final Exception exception)
            {
                StringBuilder cause = new StringBuilder();
                cause.append("Unable to retrieve currency '");
                cause.append(account.getCurrency());
                cause.append("'");
                StringBuilder sb = new StringBuilder();
                sb.append(cause);
                sb.append(" for account '");
                sb.append(account.getBAN());
                sb.append("': ");
                sb.append(exception.getMessage());
                LogSupport.minor(context, this, sb.toString(), exception);
                throw new DunningProcessException(cause.toString(), exception);
            }
        }

        if (currency == null)
        {
            StringBuilder cause = new StringBuilder();
            cause.append("Currency '");
            cause.append(account.getCurrency());
            cause.append("' not found");
            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(" for account '");
            sb.append(account.getBAN());
            sb.append("'");
            LogSupport.minor(context, this, sb.toString());
            throw new DunningProcessException(cause.toString());
        }
        return currency;
    }


    protected Date getMaxDueDate(final Context ctx, final CRMSpid spid, final CreditCategory creditCategory, final AccountStateEnum state)
    {
        int graceDays = 0;

        switch (state.getIndex())
        {
            case AccountStateEnum.NON_PAYMENT_WARN_INDEX:
                graceDays = getGraceDaysWarning(spid, creditCategory);
                break;
            case AccountStateEnum.NON_PAYMENT_SUSPENDED_INDEX:
                graceDays = getGraceDaysDunning(spid, creditCategory);
                break;
            case AccountStateEnum.IN_ARREARS_INDEX:
                graceDays = getGraceDaysInArrears(spid, creditCategory);
                break;
        }

        Date reportDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(runningDate_);

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(reportDate);

        calendar.add(Calendar.DAY_OF_YEAR, -graceDays);
        final Date maxDueDate = calendar.getTime();

        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Maximum Due Date for moving to '");
            sb.append(state);
            sb.append("' state: '");
            sb.append(CoreERLogger.formatERDateDayOnly(maxDueDate));
            sb.append("'");
            LogSupport.debug(ctx, this, sb.toString());
        }

        return maxDueDate;
    }


    private int getGraceDaysWarning(final CRMSpid spid, final CreditCategory creditCategory)
    {
        int result = creditCategory.getGraceDaysWarning();

        if (creditCategory.getDunningConfiguration() == DunningConfigurationEnum.SERVICE_PROVIDER)
        {
            result = spid.getGraceDayWarning();
        }

        return result;
    }

    private int getGraceDaysDunning(final CRMSpid spid, final CreditCategory creditCategory)
    {
        int result = creditCategory.getGraceDaysDunning();

        if (creditCategory.getDunningConfiguration() == DunningConfigurationEnum.SERVICE_PROVIDER)
        {
            result = spid.getGraceDayDunning();
        }

        return result;
    }

    private int getGraceDaysInArrears(final CRMSpid spid, final CreditCategory creditCategory)
    {
        int result = creditCategory.getGraceDaysInArrears();

        if (creditCategory.getDunningConfiguration() == DunningConfigurationEnum.SERVICE_PROVIDER)
        {
            result = spid.getGraceDayInArrears();
        }

        return result;
    }

    /**
     * Determines the Owing Threshold
     *
     * @param invoice
     *            The invoice to check.
     * @return max( MOT, Invoice.TotalAmount * Threshold / 100 )
     */
    private double calculateAgedDebtOwingThreshold(final Context ctx, final AgedDebt agedDebt, final CRMSpid spid,
            final CreditCategory creditCategory, final Currency currency)
    {
        double threshold;
        if (creditCategory.getDunningConfiguration() == DunningConfigurationEnum.SERVICE_PROVIDER)
        {
            threshold = spid.getMinimumOwingThreshold();
        }
        else
        {
            threshold = creditCategory.getMinimumOwingThreshold();
        }
        threshold = Math.max(threshold, calculateMaxAmountOwedBasedOnMinPercentOwingThreshold(agedDebt.getAccumulatedTotalAmount(), spid,
                creditCategory));
        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Calculating invoice owing treshold. BAN = '");
            sb.append(agedDebt.getBAN());
            sb.append("', Invoice date = '");
            sb.append(agedDebt.getDebtDate());
            sb.append("', Owing treshold = '");
            sb.append(currency.formatValue(Math.round(threshold)));
            sb.append("'.");
            LogSupport.debug(ctx, this, sb.toString(), null);
        }
        return threshold;
    }


    /**
     * Calculates and returns the max owing amount for the given invoice.
     *
     * @param invoice
     *            The invoice for which to calculate the minimum payment due.
     * @return The max owing amount.
     */
    private long calculateMaxAmountOwedBasedOnMinPercentOwingThreshold(final long totalAmount, final CRMSpid spid,
            final CreditCategory creditCategory)
    {
        final double minThreshold;
        if (creditCategory.getDunningConfiguration() == DunningConfigurationEnum.SERVICE_PROVIDER)
        {
            minThreshold = spid.getThreshold();
        }
        else
        {
            minThreshold = creditCategory.getMinimumPaymentThreshold();
        }
        final double threshold = minThreshold / 100.0;
        return Math.round(totalAmount * threshold);
    }

    /**
     * Verifies if invoice is paid.
     *
     * @param context
     * @param account
     * @param invoice
     * @param amountOwing
     * @param owingThreshold
     * @param currency
     * @return
     */
    private boolean isDebtOutstanding(final Context context, final Account account, final AgedDebt debt,
            final long amountOwing, final double owingThreshold, final Currency currency)
    {
        boolean result = false;
        if (amountOwing > owingThreshold)
        {
            result = true;
            if (LogSupport.isDebugEnabled(context))
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Debt not fully paid. BAN = '");
                    sb.append(debt.getBAN());
                    sb.append("', Debt date = '");
                    sb.append(debt.getDebtDate());
                    sb.append("', Amount owing = '");
                    sb.append(currency.formatValue(amountOwing));
                    sb.append("', Minimum Owing Threshold = '");
                    sb.append(currency.formatValue(Math.round(owingThreshold)));
                    sb.append("'.");
                    LogSupport.debug(context, this, sb.toString(), null);
                }
            }
        }
        return result;
    }

    private List<AgedDebt> getAgedDebts(final Context context, final Account account, final CRMSpid spid, final CreditCategory creditCategory)
    {
        Date oldestAgedDebtToLook = getOldestAgedDebtToLook(context, account,spid, creditCategory);

        return account.getInvoicedAgedDebt(context, oldestAgedDebtToLook, true);
    }


	private Date getOldestAgedDebtToLook(final Context context,
			final Account account, final CRMSpid spid,
			final CreditCategory creditCategory) {
		final Date inArrerasDueDate = getMaxDueDate(context, spid, creditCategory, AccountStateEnum.IN_ARREARS);
        int agedDebtBreakdown = SystemSupport.getDunningReportAgedDebtBreakdown(context, spid.getSpid());
        Date agedDebtBreakdownDate = getAgedDebtDate(context, agedDebtBreakdown);
        Date oldestAgedDebtToLook;

        try
        {
        	if (LogSupport.isDebugEnabled(context))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Fetching AgedDebts for Account :");
                sb.append(account.getBAN());
                sb.append(" with DueDate lesser than : ");
                sb.append(CoreERLogger.formatERDateDayOnly(inArrerasDueDate));
                sb.append("(inArrearsDate), Running Date : ");
                sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
                LogSupport.debug(context, this, sb.toString());
            }

        	And predicate = new And();
            predicate.add(new EQ(AgedDebtXInfo.BAN, account.getBAN()));
            predicate.add(new LTE(AgedDebtXInfo.DUE_DATE, inArrerasDueDate));

            Collection<AgedDebt> agedDebts = HomeSupportHelper.get(context).getBeans(context, AgedDebt.class,
                    predicate, 1, false, AgedDebtXInfo.DEBT_DATE);
            if (agedDebts.size() > 0)
            {
            	oldestAgedDebtToLook = agedDebts.iterator().next().getDueDate();
            	 if (LogSupport.isDebugEnabled(context))
                 {
                     StringBuilder sb = new StringBuilder();
                     sb.append("AgedDebts found for Account :");
                     sb.append(account.getBAN());
                     sb.append(" with DueDate lesser than : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(inArrerasDueDate));
                     sb.append(", Setting oldestAgedDebtToLook : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(oldestAgedDebtToLook));
                     sb.append(", Running Date : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
                     LogSupport.debug(context, this, sb.toString());
                 }
            }
            else
            {
            	oldestAgedDebtToLook = inArrerasDueDate;
            	 if (LogSupport.isDebugEnabled(context))
                 {
                     StringBuilder sb = new StringBuilder();
                     sb.append("No AgedDebts found for Account :");
                     sb.append(account.getBAN());
                     sb.append(" with DueDate lesser than : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(inArrerasDueDate));
                     sb.append(", Setting oldestAgedDebtToLook : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(oldestAgedDebtToLook));
                     sb.append(", Running Date : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
                     LogSupport.debug(context, this, sb.toString());
                 }
            }

            if (agedDebtBreakdownDate.before(oldestAgedDebtToLook))
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Account :");
                    sb.append(account.getBAN());
                    sb.append(", agedDebtBreakdownDate : ");
                    sb.append(CoreERLogger.formatERDateDayOnly(agedDebtBreakdownDate));
                    sb.append(" is before oldestAgedDebtToLook : ");
                    sb.append(CoreERLogger.formatERDateDayOnly(oldestAgedDebtToLook));
                    sb.append(", Running Date : ");
                    sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
                    LogSupport.debug(context, this, sb.toString());
                }
                oldestAgedDebtToLook = agedDebtBreakdownDate;
            }
        }
        catch (HomeException e)
        {
            oldestAgedDebtToLook = new Date(0);
        }

        if (LogSupport.isDebugEnabled(context))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Fetching AgedDebts for Account :");
            sb.append(account.getBAN());
            sb.append(" with DueDate greater than : ");
            sb.append(CoreERLogger.formatERDateDayOnly(oldestAgedDebtToLook));
            sb.append(", Running Date : ");
            sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
            LogSupport.debug(context, this, sb.toString());
        }
		return oldestAgedDebtToLook;
	}
	
	Date getOldestAgedDebtToLook(final Context context, final CRMSpid spid,	final CreditCategory creditCategory) {
		final Date inArrerasDueDate = getMaxDueDate(context, spid, creditCategory, AccountStateEnum.IN_ARREARS);
        int agedDebtBreakdown = SystemSupport.getDunningReportAgedDebtBreakdown(context, spid.getSpid());
        Date agedDebtBreakdownDate = getAgedDebtDate(context, agedDebtBreakdown);
        Date oldestAgedDebtToLook;

        try
        {
        	if (LogSupport.isDebugEnabled(context))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Fetching AgedDebts for Account :");
                sb.append(" with DueDate lesser than : ");
                sb.append(CoreERLogger.formatERDateDayOnly(inArrerasDueDate));
                sb.append("(inArrearsDate), Running Date : ");
                sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
                LogSupport.debug(context, this, sb.toString());
            }

        	And predicate = new And();
            predicate.add(new LTE(AgedDebtXInfo.DUE_DATE, inArrerasDueDate));

            Collection<AgedDebt> agedDebts = HomeSupportHelper.get(context).getBeans(context, AgedDebt.class,
                    predicate, 1, false, AgedDebtXInfo.DEBT_DATE);
            if (agedDebts.size() > 0)
            {
            	oldestAgedDebtToLook = agedDebts.iterator().next().getDueDate();
            	 if (LogSupport.isDebugEnabled(context))
                 {
                     StringBuilder sb = new StringBuilder();
                     sb.append("AgedDebts found for Account :");
                     sb.append(" with DueDate lesser than : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(inArrerasDueDate));
                     sb.append(", Setting oldestAgedDebtToLook : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(oldestAgedDebtToLook));
                     sb.append(", Running Date : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
                     LogSupport.debug(context, this, sb.toString());
                 }
            }
            else
            {
            	oldestAgedDebtToLook = inArrerasDueDate;
            	 if (LogSupport.isDebugEnabled(context))
                 {
                     StringBuilder sb = new StringBuilder();
                     sb.append("No AgedDebts found for Account :");
                     sb.append(" with DueDate lesser than : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(inArrerasDueDate));
                     sb.append(", Setting oldestAgedDebtToLook : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(oldestAgedDebtToLook));
                     sb.append(", Running Date : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
                     LogSupport.debug(context, this, sb.toString());
                 }
            }

            if (agedDebtBreakdownDate.before(oldestAgedDebtToLook))
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Account :");
                    sb.append(", agedDebtBreakdownDate : ");
                    sb.append(CoreERLogger.formatERDateDayOnly(agedDebtBreakdownDate));
                    sb.append(" is before oldestAgedDebtToLook : ");
                    sb.append(CoreERLogger.formatERDateDayOnly(oldestAgedDebtToLook));
                    sb.append(", Running Date : ");
                    sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
                    LogSupport.debug(context, this, sb.toString());
                }
                oldestAgedDebtToLook = agedDebtBreakdownDate;
            }
        }
        catch (HomeException e)
        {
            oldestAgedDebtToLook = new Date(0);
        }

        if (LogSupport.isDebugEnabled(context))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Fetching AgedDebts for Account :");
            sb.append(" with DueDate greater than : ");
            sb.append(CoreERLogger.formatERDateDayOnly(oldestAgedDebtToLook));
            sb.append(", Running Date : ");
            sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
            LogSupport.debug(context, this, sb.toString());
        }
		return oldestAgedDebtToLook;
	}
	

    private List<DunningReportRecordAgedDebt> createDunningReportRecordAgedDebtList(final Context context, final int spid)
    {
        int agedDebtBreakdown = SystemSupport.getDunningReportAgedDebtBreakdown(context, spid);
        List<DunningReportRecordAgedDebt> agedDebts = new ArrayList<DunningReportRecordAgedDebt>();
        for (int i=0;i<agedDebtBreakdown;i++)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(i==0?"0":String.valueOf((i*30)+1));
            sb.append("-");
            sb.append(String.valueOf((i+1)*30));
            DunningReportRecordAgedDebt agedDebt = new DunningReportRecordAgedDebt();
            agedDebt.setPeriod(sb.toString());
            agedDebt.setValue(0);
            agedDebts.add(agedDebt);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf((agedDebtBreakdown*30)+1));
        sb.append("+");
        DunningReportRecordAgedDebt agedDebt = new DunningReportRecordAgedDebt();
        agedDebt.setPeriod(sb.toString());
        agedDebt.setValue(0);
        agedDebts.add(agedDebt);

        return agedDebts;
    }

    private List<Date> createAgedDebtDatesList(final Context context, final int spid)
    {
        int agedDebtBreakdown = SystemSupport.getDunningReportAgedDebtBreakdown(context, spid);
        List<Date> agedDebtDate = new ArrayList<Date>();
        for (int i=0;i<agedDebtBreakdown;i++)
        {
            agedDebtDate.add(getAgedDebtDate(context, i+1));
        }
        return agedDebtDate;
    }

    /**
     * Generates a dunning report record for an account.
     *
     * @param context
     * @param account
     * @return
     * @throws DunningProcessException
     */
    private DunningReportRecord generateReportRecord(final Context context, final Account account,
            final boolean dunningExempt) throws DunningProcessException
    {

        StringBuilder accountDetails = new StringBuilder();
        accountDetails.append("Account BAN = '");
        accountDetails.append(account.getBAN());
        accountDetails.append("'");
        String details = accountDetails.toString();
        final PMLogMsg pmLogMsg = new PMLogMsg(getProcessName(), "Account dunning logic calculation", details);


        try
        {
            DunningReportRecord record = new DunningReportRecord();
            record.setBAN(account.getBAN());
            //record.setCurrentState(account.getState());
            //For TTITSC-4196
            //record.setCreditCategoryId(account.getCreditCategory());
            record.setAccountType(account.getType());
            record.setReportDate(CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(runningDate_));
            record.setStatus(DunningReportRecordStatusEnum.PENDING_INDEX);
            record.setSpid(account.getSpid());
            record.setLastInvoiceDate(null);
            record.setAgedDebt(createDunningReportRecordAgedDebtList(context, account.getSpid()));

            if (dunningExempt)
            {
            	 if(LogSupport.isDebugEnabled(context))
                 {
                     LogSupport.debug(context, this, "Account : " + account.getBAN() + " is exempted from dunning. Keeping account in Active state. Running Date : " + CoreERLogger.formatERDateDayOnly(runningDate_));
                 }
                AgedDebt lastAgedDebt = account.getLastInvoicedAgedDebt(context);
                if (lastAgedDebt!=null)
                {
                    record.setLastInvoiceDate(lastAgedDebt.getDebtDate());
                }

                record.setDunningAmount(0);
                //record.setForecastedState(calculateExpectedForecastedState(context, account.getState(), AccountStateEnum.ACTIVE));
            }
            else
            {
                final CreditCategory creditCategory = retrieveCreditCategory(context, account);
                final CRMSpid spid = retrieveSpid(context, account);
                final Currency currency = retrieveCurrency(context, account);

                List<AgedDebt> invoicedAgedDebts = getAgedDebts(context, account, spid, creditCategory);

                if (invoicedAgedDebts.size()>0)
                {
                	Date lastInvoiceDebtDate = invoicedAgedDebts.iterator().next().getDebtDate();
                    record.setLastInvoiceDate(lastInvoiceDebtDate);
                    if (LogSupport.isDebugEnabled(context))
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Got InvoicedAgedDebts for Account :");
                        sb.append(account.getBAN());
                        sb.append(", lastInvoiceDebtDate : ");
                        sb.append(CoreERLogger.formatERDateDayOnly(lastInvoiceDebtDate));
                        sb.append(" Running Date : ");
                        sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
                        LogSupport.debug(context, this, sb.toString());
                    }
                }

                LongHolder dunningAmount = new LongHolder(0);
                ObjectHolder dunnedAgedDebt = new ObjectHolder(0,null);
                AccountStateEnum forecastedState = calculateForecastedState(context, account, invoicedAgedDebts, creditCategory, spid, currency, record.getAgedDebt(), false, dunningAmount, dunnedAgedDebt);

                if (forecastedState == null)
                {
                	 if (LogSupport.isDebugEnabled(context))
                     {
                         StringBuilder sb = new StringBuilder();
                         sb.append("Account :");
                         sb.append(account.getBAN());
                         sb.append(", ForecastedState is null");
                         sb.append(" Running Date : ");
                         sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
                         LogSupport.debug(context, this, sb.toString());
                     }
                    record = null;
                }
                else
                {
                    if (!forecastedState.equals(AccountStateEnum.ACTIVE))
                    {
                    	if (LogSupport.isDebugEnabled(context))
                        {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Account :");
                            sb.append(account.getBAN());
                            sb.append(", ForecastedState is Not Active. Calculating Forecasted date again.");
                            sb.append(" Running Date : ");
                            sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
                            LogSupport.debug(context, this, sb.toString());
                        }

                        List<AgedDebt> agedDebts = account.getAgedDebt(context, invoicedAgedDebts);

                        // Cleaning the record's aged debt values. and recalculating the next state
                        for (DunningReportRecordAgedDebt agedDebt : (List<DunningReportRecordAgedDebt>) record.getAgedDebt())
                        {
                            agedDebt.setValue(0);
                        }

                        // TT#13031133022 - check flag in context to see if dunning process is initiated by Transaction pipeline
                        // If yes, set it to true as the debt is cleared at this stage
                        if (context.has(TransactionRedirectionHome.IS_DEBT_CLEARED_BY_TRANSACTION))
                        {
                        	context.put(TransactionRedirectionHome.IS_DEBT_CLEARED_BY_TRANSACTION,Boolean.TRUE);
                        }

                        forecastedState = calculateForecastedState(context, account, agedDebts, creditCategory, spid, currency, record.getAgedDebt(), true, dunningAmount, dunnedAgedDebt);
                    }

                    if (forecastedState == null)
                    {
                    	  if (LogSupport.isDebugEnabled(context))
                          {
                              StringBuilder sb = new StringBuilder();
                              sb.append("Account :");
                              sb.append(account.getBAN());
                              sb.append(", Recalculated ForecastedState is null");
                              sb.append(" Running Date : ");
                              sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
                              LogSupport.debug(context, this, sb.toString());
                          }
                        record = null;
                    }
                    else
                    {
                       // record.setForecastedState(calculateExpectedForecastedState(context, account.getState(),forecastedState));
                        record.setDunningAmount(dunningAmount.getValue());
                        record.setDunnedAgedDebt((AgedDebt) dunnedAgedDebt.getObj());
                    }
                }
            }

            return record;
        }
        finally
        {
            pmLogMsg.log(context);
        }

    }

	private long getCurrentAmountOwing(final Context ctx, final Account account, final AgedDebt agedDebt,
			final boolean current) throws DunningProcessException
    {
        long result;

		try 
		{
			if (current) 
			{
				result = agedDebt.getCurrentAccumulatedDebt();
			} 
			else 
			{
				result = agedDebt.getAccumulatedDebt();
			}

			CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, account.getSpid());
			if (!crmSpid.isAdjustBalanceOnDisputeCreation()) 
			{
				result = getAmountOwingWithDisputeAmount(ctx, account, result, crmSpid);
			}
		} 
		catch (Throwable t) 
		{
			throw new DunningProcessException(
					"Unable to process customer disputes for account [" + account.getBAN() + "].");
		}
        return result;
    }


    private long getAmountOwingWithDisputeAmount(final Context ctx, final Account account, long result, CRMSpid crmSpid)
            throws DunningProcessException
    {
		try
		{
			Set<String> subscribers = new HashSet<String>();
			for (Subscriber subscriber : account.getSubscribers(ctx))
			{
				subscribers.add(subscriber.getId());
			}
			And filter = new And();
			filter.add(new In(DisputeXInfo.SUBSCRIBER_ID, subscribers));

			//ABankar: Commenting this filter to get resolved dispute as well, 
			//to check and avoid dunning for buffer days.

			//filter.add(new NEQ(DisputeXInfo.STATE, DisputeStateEnum.RESOLVED));  

			Home home = (Home) ctx.get(DisputeHome.class);
			Collection<Dispute> disputes = home.select(ctx, filter);
			if (LogSupport.isDebugEnabled(ctx))
			{
				StringBuilder sb = new StringBuilder();
				sb.append("The amount outstanding before adjusting disputed amount [");
				sb.append(result);
				sb.append("].");
				LogSupport.debug(ctx, this, sb.toString());
			}
			
			boolean adjustResolvedDispute = false;
			for (Dispute dispute : disputes)
			{
				//For resolved disputes, there should be no immediate dunning as soon as dispute is resolved.
				//Adding buffer days to resolution date will give provision to allow customers to pay within buffer days.
				//Adjusting resolved amount from total amount, for such resolved disputes, would allow to keep it within threshold.

				adjustResolvedDispute = false;

				if(dispute.getState() == DisputeStateEnum.RESOLVED) {
					Date resolutionDate = dispute.getResolutionDate();
					int disputeBufferDays = crmSpid.getDisputeBufferDays();

					Calendar calendar = Calendar.getInstance();
					calendar.setTime(resolutionDate);
					calendar.add(Calendar.DAY_OF_YEAR, + disputeBufferDays);

					Date resolutionBufferDate = calendar.getTime();

					if(runningDate_.before(resolutionBufferDate)) {
						adjustResolvedDispute = true;
					}
				}

				if(dispute.getState() == DisputeStateEnum.ACTIVE || adjustResolvedDispute) {

					if (dispute.getDisputedAmountAdjustmentType() == (int) AdjustmentTypeEnum.CustomerDispute.getIndex())
					{
						result = result - dispute.getDisputedAmount();
					}
					else if (dispute.getDisputedAmountAdjustmentType() == (int) AdjustmentTypeEnum.CustomerDisputeDebit.getIndex())
					{
						result = result + dispute.getDisputedAmount();
					}

					if (LogSupport.isDebugEnabled(ctx))
					{
						StringBuilder sb = new StringBuilder();
						if(adjustResolvedDispute) {
							sb.append("The resolved dispute with dispute amount: ");
						}
						else {
							sb.append("The unresolved dispute amount: ");
						}
						sb.append(dispute.getDisputedAmount());
						sb.append(" and adjustment type: ");
						sb.append(dispute.getDisputedAmountAdjustmentType());
						sb.append(". The amount outstanding after adjusting disputed amount [");
						sb.append(result);
						sb.append("].");
						LogSupport.debug(ctx, this, sb.toString());
					}
				}
			}
		}
		catch (Throwable t)
		{
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, this, t);
			}
			
			throw new DunningProcessException("Unable to process customer disputes for account [" 
					+ account.getBAN() + "].");
		}
		return result;
	}
    
    private boolean isDebtOutstanding(final Context context, final Account account, final AgedDebt agedDebt,
            final long amountOwing, final CRMSpid spid, final CreditCategory creditCategory,
            final Currency currency)
    {
        boolean result = false;
        if (agedDebt!=null && amountOwing>0)
        {
            final double inArrearsInvoiceOwingThreshold = calculateAgedDebtOwingThreshold(context,
                    agedDebt, spid, creditCategory, currency);
            result = isDebtOutstanding(context, account, agedDebt, amountOwing,
                    inArrearsInvoiceOwingThreshold, currency);
        }
        return result;
    }

    private Date getAgedDebtDate(final Context context, final int month)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getRunningDate());
        calendar.add(Calendar.MONTH, -month);
        return calendar.getTime();
    }

    private void populateRecordAgedDebt(final Context context, final AgedDebt agedDebt, final List<DunningReportRecordAgedDebt> recordAgedDebts, final List<Date> agedDebtDates, final boolean current)
    {
        long debt = agedDebt.getDebt();
        long accumulatedDebt = agedDebt.getAccumulatedDebt();

        if (current)
        {
            debt = agedDebt.getCurrentDebt();
            accumulatedDebt = agedDebt.getCurrentAccumulatedDebt();
        }

        Iterator<DunningReportRecordAgedDebt> recordAgedDebtsIterator = recordAgedDebts.iterator();
        Iterator<Date> agedDebtDatesIterator = agedDebtDates.iterator();
        DunningReportRecordAgedDebt lastRecordAgedDebt = recordAgedDebts.get(recordAgedDebts.size()-1);
        boolean processed = false;

        while (agedDebtDatesIterator.hasNext() && recordAgedDebtsIterator.hasNext())
        {
            DunningReportRecordAgedDebt recordAgedDebt = recordAgedDebtsIterator.next();
            Date cutDate = agedDebtDatesIterator.next();
            if (agedDebt.getDebtDate().after(cutDate))
            {
                processed = true;
                recordAgedDebt.setValue(recordAgedDebt.getValue() + debt);
                lastRecordAgedDebt.setValue(accumulatedDebt - debt);
                if (LogSupport.isDebugEnabled(context))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Account :");
                    sb.append(agedDebt.getBAN());
                    sb.append(", agedDebt date : ");
                    sb.append(CoreERLogger.formatERDateDayOnly(agedDebt.getDebtDate()));
                    sb.append(" is after cutDate : ");
                    sb.append(CoreERLogger.formatERDateDayOnly(cutDate));
                    sb.append(". Set RecordAgedDebt : ");
                    sb.append(recordAgedDebt);
                    sb.append(" Running Date : ");
                    sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
                    LogSupport.debug(context, this, sb.toString());
                }
                break;
            }
            else
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Ignoring. Account :");
                    sb.append(agedDebt.getBAN());
                    sb.append(", agedDebt date : ");
                    sb.append(CoreERLogger.formatERDateDayOnly(agedDebt.getDebtDate()));
                    sb.append(" is before cutDate : ");
                    sb.append(CoreERLogger.formatERDateDayOnly(cutDate));
                    sb.append(". Set RecordAgedDebt : ");
                    sb.append(recordAgedDebt);
                    sb.append(" Running Date : ");
                    sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
                    LogSupport.debug(context, this, sb.toString());
                }
            }
        }

        if (!processed)
        {
            lastRecordAgedDebt.setValue(accumulatedDebt);
        }

        if (LogSupport.isDebugEnabled(context))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Populated last record Aged debt. Account :");
            sb.append(agedDebt.getBAN());
            sb.append(", lastRecordAgedDebt : ");
            sb.append(lastRecordAgedDebt);
            sb.append(" Running Date : ");
            sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
            LogSupport.debug(context, this, sb.toString());
        }
    }

    private AccountStateEnum calculateForecastedState(final Context context, final Account account,
            final List<AgedDebt> agedDebts, final CreditCategory creditCategory, final CRMSpid spid,
            final Currency currency, final List<DunningReportRecordAgedDebt> recordAgedDebt,
            final boolean current, final LongHolder dunningAmount, final ObjectHolder dunnedAgedDebt) throws DunningProcessException
    {
    	AccountStateEnum result = null; // TT# 13012527003
        dunningAmount.setValue(0);

        String processName = getProcessName();
        boolean dunningNotification = processName.equals("Pre-Dunning Notification Processing");
        boolean dunningReportGen = processName.equals("Dunning Report Generation");
        boolean reportGen = dunningReportGen || dunningNotification;

        boolean ptpExpired = false;
        if (dunningReportGen && account.getState().equals(AccountStateEnum.PROMISE_TO_PAY))
        {
            Date ptpDate = account.getPromiseToPayDate();
            ptpExpired = runningDate_.compareTo(ptpDate) == 0
                    || (CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(ptpDate).compareTo(ptpDate) != 0 && CalendarSupportHelper
                            .get(context).getNumberOfDaysBetween(ptpDate, runningDate_) == 1);
        }


        final Date nonPaymentWarnDate = getMaxDueDate(context, spid, creditCategory, AccountStateEnum.NON_PAYMENT_WARN);
        final Date nonPaymentSuspendedDate = getMaxDueDate(context, spid, creditCategory, AccountStateEnum.NON_PAYMENT_SUSPENDED);
        final Date inArrearsDate = getMaxDueDate(context, spid, creditCategory, AccountStateEnum.IN_ARREARS);

        if (LogSupport.isDebugEnabled(context))
        {
            LogSupport.debug(context, this, "calculateForecastedState::account::" + account.getBAN() + " ,Report Date:"
                    + this.getRunningDate() + " ,nonPaymentWarnDate:" + nonPaymentWarnDate
                    + ",nonPaymentSuspendedDate:" + nonPaymentSuspendedDate + ",inArrearsDate:" + inArrearsDate
                    + " reportGen : " + reportGen + ", ptpExpired : " + ptpExpired);
        }

        boolean nonPaymentWarn = false;
        boolean nonPaymentSuspended = false;
        boolean inArrears = false;

        long nonPaymentWarnCurrentAmountOwing = 0;
        long nonPaymentSuspendedCurrentAmountOwing = 0;
        long inArrearsCurrentAmountOwing = 0;

        AgedDebt nonPaymentWarnAgedDebt = null;
        AgedDebt nonPaymentSuspendedAgedDebt = null;
        AgedDebt inArrearsAgedDebt = null;

        List<Date> agedDebtDates = createAgedDebtDatesList(context, spid.getSpid());

        for (AgedDebt agedDebt : agedDebts)
        {
        	 if (LogSupport.isDebugEnabled(context))
             {
                 StringBuilder sb = new StringBuilder();
                 sb.append("Processing AgedDebt for Account :");
                 sb.append(account.getBAN());
                 sb.append(", agedDebt : ");
                 sb.append(agedDebt);
                 sb.append(" Running Date : ");
                 sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
                 LogSupport.debug(context, this, sb.toString());
             }

            populateRecordAgedDebt(context, agedDebt, recordAgedDebt, agedDebtDates, current);

            if (inArrearsAgedDebt==null && (( reportGen && agedDebt.getDueDate().compareTo(inArrearsDate)==0) || ((!reportGen || ptpExpired) && agedDebt.getDueDate().compareTo(inArrearsDate)<=0)))
            {
                inArrearsAgedDebt = agedDebt;
                inArrearsCurrentAmountOwing=getCurrentAmountOwing(context, account, inArrearsAgedDebt, current);

                if (LogSupport.isDebugEnabled(context))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(AccountStateEnum.IN_ARREARS.getDescription());
                    sb.append(" aged debt due date: '");
                    sb.append(CoreERLogger.formatERDateDayOnly(inArrearsAgedDebt.getDueDate()));
                    sb.append("'");
                    sb.append(" inArrearsDate : ");
                    sb.append(CoreERLogger.formatERDateDayOnly(inArrearsDate));
                    sb.append(" inArrearsCurrentAmountOwing : ");
                    sb.append(inArrearsCurrentAmountOwing);
                    LogSupport.debug(context, this, sb.toString());
                }
            }
            else if (nonPaymentSuspendedAgedDebt==null && ((reportGen && agedDebt.getDueDate().compareTo(nonPaymentSuspendedDate)==0) || ((!reportGen || ptpExpired) && agedDebt.getDueDate().compareTo(nonPaymentSuspendedDate)<=0)))
            {
                nonPaymentSuspendedAgedDebt = agedDebt;
                nonPaymentSuspendedCurrentAmountOwing=getCurrentAmountOwing(context, account, nonPaymentSuspendedAgedDebt, current);

                if (LogSupport.isDebugEnabled(context))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(AccountStateEnum.NON_PAYMENT_SUSPENDED.getDescription());
                    sb.append(" aged debt due date: '");
                    sb.append(CoreERLogger.formatERDateDayOnly(nonPaymentSuspendedAgedDebt.getDueDate()));
                    sb.append("'");
                    sb.append(" nonPaymentSuspendedDate : ");
                    sb.append(CoreERLogger.formatERDateDayOnly(nonPaymentSuspendedDate));
                    sb.append(" nonPaymentSuspendedCurrentAmountOwing : ");
                    sb.append(nonPaymentSuspendedCurrentAmountOwing);
                    LogSupport.debug(context, this, sb.toString());
                }
            }
            else if (nonPaymentWarnAgedDebt == null && ((reportGen && agedDebt.getDueDate().compareTo(nonPaymentWarnDate)==0) || ((!reportGen || ptpExpired) && agedDebt.getDueDate().compareTo(nonPaymentWarnDate)<=0)))
            {
                nonPaymentWarnAgedDebt = agedDebt;
                nonPaymentWarnCurrentAmountOwing = getCurrentAmountOwing(context, account, nonPaymentWarnAgedDebt, current);

                if (LogSupport.isDebugEnabled(context))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(AccountStateEnum.NON_PAYMENT_WARN.getDescription());
                    sb.append(" aged debt due date: '");
                    sb.append(CoreERLogger.formatERDateDayOnly(nonPaymentWarnAgedDebt.getDueDate()));
                    sb.append("'");
                    sb.append(" nonPaymentWarnDate : ");
                    sb.append(CoreERLogger.formatERDateDayOnly(nonPaymentWarnDate));
                    sb.append(" nonPaymentWarnCurrentAmountOwing : ");
                    sb.append(nonPaymentWarnCurrentAmountOwing);
                    LogSupport.debug(context, this, sb.toString());
                }
            }else{
            	//If any of the above conditions is not satisfied and account is not dunning exempted, forecasted state should be same as current state. Forecasted state cannot be active.
            	if(!isCreditCategoryDunningExempt(context, account))
            		result = account.getState();
            }
        }

        inArrears = isDebtOutstanding(context, account, inArrearsAgedDebt, inArrearsCurrentAmountOwing, spid, creditCategory,
                currency);

        nonPaymentSuspended = !inArrears
                && isDebtOutstanding(context, account, nonPaymentSuspendedAgedDebt, nonPaymentSuspendedCurrentAmountOwing,
                        spid, creditCategory, currency);

        nonPaymentWarn = !inArrears
                && !nonPaymentSuspended
                && isDebtOutstanding(context, account, nonPaymentWarnAgedDebt, nonPaymentWarnCurrentAmountOwing, spid,
                        creditCategory, currency);

        if (inArrears)
        {
            if (this.processOnlyExactDate() && inArrearsAgedDebt.getDueDate().before(CalendarSupportHelper.get(context).getDayBefore(inArrearsDate)))
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(AccountStateEnum.IN_ARREARS.getDescription());
                    sb.append(" due date '");
                    sb.append(CoreERLogger.formatERDateDayOnly(inArrearsAgedDebt.getDueDate()));
                    sb.append("' before '");
                    sb.append(CoreERLogger.formatERDateDayOnly(inArrearsDate));
                    sb.append("' date. Not processing record.");
                    LogSupport.debug(context, this, sb.toString());
                }

                result = null;
            }
            else
            {
                result = AccountStateEnum.IN_ARREARS;
                dunningAmount.setValue(inArrearsCurrentAmountOwing);
                dunnedAgedDebt.setObj(inArrearsAgedDebt);

                if (LogSupport.isDebugEnabled(context))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(result.getDescription());
                    sb.append(" due date '");
                    sb.append(CoreERLogger.formatERDateDayOnly(inArrearsAgedDebt.getDueDate()));
                    sb.append("' inArrearsDate '");
                    sb.append(CoreERLogger.formatERDateDayOnly(inArrearsDate));
                    sb.append("' date. dunnedAgedDebt : ");
                    sb.append(dunnedAgedDebt.getObj());
                    sb.append(" . Marked Account in : " + result.getDescription());
                    LogSupport.debug(context, this, sb.toString());
                }

            }
        }
        else if (nonPaymentSuspended)
        {
            if (this.processOnlyExactDate() && nonPaymentSuspendedAgedDebt.getDueDate().before(CalendarSupportHelper.get(context).getDayBefore(nonPaymentSuspendedDate)))
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(AccountStateEnum.NON_PAYMENT_SUSPENDED.getDescription());
                    sb.append(" due date '");
                    sb.append(CoreERLogger.formatERDateDayOnly(nonPaymentSuspendedAgedDebt.getDueDate()));
                    sb.append("' before '");
                    sb.append(CoreERLogger.formatERDateDayOnly(nonPaymentSuspendedDate));
                    sb.append("' date. Not processing record.");
                    LogSupport.debug(context, this, sb.toString());
                }

                result = null;
            }
            else
            {
                result = AccountStateEnum.NON_PAYMENT_SUSPENDED;
                dunningAmount.setValue(nonPaymentSuspendedCurrentAmountOwing);
                dunnedAgedDebt.setObj(nonPaymentSuspendedAgedDebt);

                if (LogSupport.isDebugEnabled(context))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(result.getDescription());
                    sb.append(" due date '");
                    sb.append(CoreERLogger.formatERDateDayOnly(nonPaymentSuspendedAgedDebt.getDueDate()));
                    sb.append("' nonPaymentSuspendedDate '");
                    sb.append(CoreERLogger.formatERDateDayOnly(nonPaymentSuspendedDate));
                    sb.append("' date. dunnedAgedDebt : ");
                    sb.append(dunnedAgedDebt.getObj());
                    sb.append(" . Marked Account in : " + result.getDescription());
                    LogSupport.debug(context, this, sb.toString());
            }
        }
        }
        else if (nonPaymentWarn)
        {
            if (this.processOnlyExactDate() && nonPaymentWarnAgedDebt.getDueDate().before(CalendarSupportHelper.get(context).getDayBefore(nonPaymentWarnDate)))
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(AccountStateEnum.NON_PAYMENT_WARN.getDescription());
                    sb.append(" due date '");
                    sb.append(CoreERLogger.formatERDateDayOnly(nonPaymentWarnAgedDebt.getDueDate()));
                    sb.append("' before '");
                    sb.append(CoreERLogger.formatERDateDayOnly(nonPaymentWarnDate));
                    sb.append("' date. Not processing record.");
                    LogSupport.debug(context, this, sb.toString());
                }

                result = null;
            }
            else
            {
                result = AccountStateEnum.NON_PAYMENT_WARN;
                dunningAmount.setValue(nonPaymentWarnCurrentAmountOwing);
                dunnedAgedDebt.setObj(nonPaymentWarnAgedDebt);

                if (LogSupport.isDebugEnabled(context))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(result.getDescription());
                    sb.append(" due date '");
                    sb.append(CoreERLogger.formatERDateDayOnly(nonPaymentWarnAgedDebt.getDueDate()));
                    sb.append("' nonPaymentWarnDate '");
                    sb.append(CoreERLogger.formatERDateDayOnly(nonPaymentWarnDate));
                    sb.append("' date. dunnedAgedDebt : ");
                    sb.append(dunnedAgedDebt.getObj());
                    sb.append(" . Marked Account in : " + result.getDescription());
                    LogSupport.debug(context, this, sb.toString());
                }
            }
        }
        // TT#13031133022 - if forecasted state is none of the above dunning state
        // & when payment is done against to clear the debt, next state of account is ACTIVE

        else if (context.getBoolean(TransactionRedirectionHome.IS_DEBT_CLEARED_BY_TRANSACTION))
        {
        	result = AccountStateEnum.ACTIVE;

        	if (LogSupport.isDebugEnabled(context))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Account :");
                sb.append(account.getBAN());
                sb.append(" state changed to ");
                sb.append(": "+result.getDescription());
                LogSupport.debug(context, this, sb.toString());
            }
        }
        else
        {
            if (LogSupport.isDebugEnabled(context))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Account :");
                sb.append(account.getBAN());
                sb.append(". No dunning state found.");
                sb.append(" Running Date : ");
                sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
                LogSupport.debug(context, this, sb.toString());
            }

            //TT#13012527003
            if(AccountStateEnum.PROMISE_TO_PAY.equals(account.getState()))
            {
            	result = AccountStateEnum.ACTIVE;
            }
            else
            {
            	result = null;
            }
        }

        return result;
    }



    /**
     * Calculate the expected forecasted state based on the current state and the
     * forecasted state.
     *
     * @param current
     * @param forecasted
     * @return
     */
    private AccountStateEnum calculateExpectedForecastedState(Context context, AccountStateEnum current,
            AccountStateEnum forecasted)
    {
        AccountStateEnum result;
        switch (current.getIndex())
        {
        case AccountStateEnum.IN_COLLECTION_INDEX:
            result = current;
            break;
        case AccountStateEnum.IN_ARREARS_INDEX:
            if (forecasted.equals(AccountStateEnum.ACTIVE))
            {
                result = forecasted;
            }
            else
            {
                result = current;
            }
            break;
        case AccountStateEnum.NON_PAYMENT_SUSPENDED_INDEX:
            if (forecasted.equals(AccountStateEnum.ACTIVE) || forecasted.equals(AccountStateEnum.IN_ARREARS))
            {
                result = forecasted;
            }
            else
            {
                result = current;
            }
            break;
        case AccountStateEnum.NON_PAYMENT_WARN_INDEX:
            if (forecasted.equals(AccountStateEnum.ACTIVE) || forecasted.equals(AccountStateEnum.IN_ARREARS)
                    || forecasted.equals(AccountStateEnum.NON_PAYMENT_SUSPENDED))
            {
                result = forecasted;
            }
            else
            {
                result = current;
            }
            break;
        default:
            result = forecasted;
        }
        if (LogSupport.isDebugEnabled(context))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Calculating forecasted state. currentState = '");
            sb.append(current.getDescription());
            sb.append("', forecastedState = '");
            sb.append(forecasted.getDescription());
            sb.append("', calculatedState = '");
            sb.append(result.getDescription());
            sb.append("'");
            LogSupport.debug(context, this, sb.toString());
        }
        return result;
    }


    public Date getRunningDate()
    {
        return runningDate_;
    }

    private final Date runningDate_;
}
