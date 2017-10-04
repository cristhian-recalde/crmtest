package com.trilogy.app.crm.dunning;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.AgedDebt;
import com.trilogy.app.crm.bean.AgedDebtXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAgedDebt;
import com.trilogy.app.crm.bean.SubscriberAgedDebtXInfo;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningAccountProcessor;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.state.InOneOfStatesPredicate;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class DunningProcessHelper {
	
	public static Long getOldestDate(Context context,Date runningDate)
	{

		Date oldestAgedDebt = new Date();
		
		try {
			Collection<DunningPolicy> dunningPolicies =  HomeSupportHelper.get(context).getBeans(context,DunningPolicy.class);
			for(DunningPolicy dunningPolicy : dunningPolicies){
				if(!dunningPolicy.isDunningExempt(context)){
					CRMSpid spid = (CRMSpid) context.get(CRMSpid.class);
											
					Date agedDebtDate = getOldestAgedDebtToLook(context, dunningPolicy, runningDate);
					if(agedDebtDate.before(oldestAgedDebt))
						oldestAgedDebt = agedDebtDate;
				}
			}
			
		} catch (HomeInternalException e) {
			new MinorLogMsg(DunningProcessHelper.class.getName(), "Unable to find oldest debt date from DunningPolicy" );
		} catch (HomeException e) {
			new MinorLogMsg(DunningProcessHelper.class.getName(), "Unable to find oldest debt date from DunningPolicy" );
		}
		return oldestAgedDebt.getTime();
	
	}
	
	public static Date getOldestAgedDebtToLook(final Context context, DunningPolicy policy,Date runningDate) {
		final Date lastLevelDueDate = getMaxDueDateForLevel(context, policy,policy.getLastLevelIndex(context),runningDate);
		CRMSpid spid = (CRMSpid) context.get(CRMSpid.class);
        int agedDebtBreakdown = SystemSupport.getDunningReportAgedDebtBreakdown(context, spid.getSpid());
        Date agedDebtBreakdownDate = getAgedDebtDate(context, agedDebtBreakdown,runningDate);
        Date oldestAgedDebtToLook;

        try
        {
        	if (LogSupport.isDebugEnabled(context))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Fetching AgedDebts for Account :");
                sb.append(" with DueDate lesser than : ");
                sb.append(CoreERLogger.formatERDateDayOnly(lastLevelDueDate));
                sb.append("(inArrearsDate), Running Date : ");
                sb.append(CoreERLogger.formatERDateDayOnly(runningDate));
                LogSupport.debug(context, DunningProcessHelper.class.getName(), sb.toString());
            }

        	And predicate = new And();
            predicate.add(new LTE(AgedDebtXInfo.DUE_DATE, lastLevelDueDate));

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
                     sb.append(CoreERLogger.formatERDateDayOnly(lastLevelDueDate));
                     sb.append(", Setting oldestAgedDebtToLook : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(oldestAgedDebtToLook));
                     sb.append(", Running Date : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(runningDate));
                     LogSupport.debug(context, DunningProcessHelper.class.getName(), sb.toString());
                 }
            }
            else
            {
            	oldestAgedDebtToLook = lastLevelDueDate;
            	 if (LogSupport.isDebugEnabled(context))
                 {
                     StringBuilder sb = new StringBuilder();
                     sb.append("No AgedDebts found for Account :");
                     sb.append(" with DueDate lesser than : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(lastLevelDueDate));
                     sb.append(", Setting oldestAgedDebtToLook : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(oldestAgedDebtToLook));
                     sb.append(", Running Date : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(runningDate));
                     LogSupport.debug(context, DunningProcessHelper.class.getName(), sb.toString());
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
                    sb.append(CoreERLogger.formatERDateDayOnly(runningDate));
                    LogSupport.debug(context, DunningProcessHelper.class.getName(), sb.toString());
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
            sb.append(CoreERLogger.formatERDateDayOnly(runningDate));
            LogSupport.debug(context, DunningProcessHelper.class.getName(), sb.toString());
        }
		return oldestAgedDebtToLook;
	}
	
	public static Date getOldestAgedDebtToLook(final Context context,
			final Account account,DunningPolicy policy,Date runningDate ) 
	{
		final Date maxDueDate = getMaxDueDateForLevel(context, policy, policy.getLastLevelIndex(context), runningDate);
        int agedDebtBreakdown = SystemSupport.getDunningReportAgedDebtBreakdown(context, account.getSpid());
        Date agedDebtBreakdownDate = getAgedDebtDate(context, agedDebtBreakdown,runningDate);
        Date oldestAgedDebtToLook;

        try
        {
        	if (LogSupport.isDebugEnabled(context))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Fetching AgedDebts for Account :");
                sb.append(account.getBAN());
                sb.append(" with DueDate lesser than : ");
                sb.append(CoreERLogger.formatERDateDayOnly(maxDueDate));
                sb.append(" Running Date : ");
                sb.append(CoreERLogger.formatERDateDayOnly(runningDate));
                LogSupport.debug(context, DunningProcessHelper.class.getName(), sb.toString());
            }

        	And predicate = new And();
            predicate.add(new EQ(AgedDebtXInfo.BAN, account.getBAN()));
            predicate.add(new LTE(AgedDebtXInfo.DUE_DATE, maxDueDate));
            //ITSC-4209 : changing the fetch order to Ascending, as we are getting 'oldestAgedDebtToLook' as first element in iterator.
            Collection<AgedDebt> agedDebts = HomeSupportHelper.get(context).getBeans(context, AgedDebt.class,
                    predicate, 1, true, AgedDebtXInfo.DEBT_DATE);
            if (agedDebts.size() > 0)
            {
            	oldestAgedDebtToLook = agedDebts.iterator().next().getDueDate();
            	 if (LogSupport.isDebugEnabled(context))
                 {
                     StringBuilder sb = new StringBuilder();
                     sb.append("AgedDebts found for Account :");
                     sb.append(account.getBAN());
                     sb.append(" with DueDate lesser than : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(maxDueDate));
                     sb.append(", Setting oldestAgedDebtToLook : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(oldestAgedDebtToLook));
                     sb.append(", Running Date : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(runningDate));
                     LogSupport.debug(context, DunningProcessHelper.class.getName(), sb.toString());
                 }
            }
            else
            {
            	oldestAgedDebtToLook = maxDueDate;
            	 if (LogSupport.isDebugEnabled(context))
                 {
                     StringBuilder sb = new StringBuilder();
                     sb.append("No AgedDebts found for Account :");
                     sb.append(account.getBAN());
                     sb.append(" with DueDate lesser than : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(maxDueDate));
                     sb.append(", Setting oldestAgedDebtToLook : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(oldestAgedDebtToLook));
                     sb.append(", Running Date : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(runningDate));
                     LogSupport.debug(context, DunningProcessHelper.class.getName(), sb.toString());
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
                    sb.append(CoreERLogger.formatERDateDayOnly(runningDate));
                    LogSupport.debug(context, DunningProcessHelper.class.getName(), sb.toString());
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
            sb.append(CoreERLogger.formatERDateDayOnly(runningDate));
            LogSupport.debug(context, DunningProcessHelper.class.getName(), sb.toString());
        }
		return oldestAgedDebtToLook;
	}
	
	public static Date getOldestSubscriberAgedDebtToLook(final Context context,
            final Subscriber subscriber, DunningPolicy policy, Date runningDate) 
    {
        final Date maxDueDate = getMaxDueDateForLevel(context, policy, policy.getLastLevelIndex(context), runningDate);
        int agedDebtBreakdown = SystemSupport.getDunningReportAgedDebtBreakdown(context, subscriber.getSpid());
        Date agedDebtBreakdownDate = getAgedDebtDate(context, agedDebtBreakdown, runningDate);
        Date oldestAgedDebtToLook;

        try
        {
            if (LogSupport.isDebugEnabled(context))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Fetching SubscriberAgedDebts for Account :");
                sb.append(subscriber.getBAN());
                sb.append(" Subscriber :");
                sb.append(subscriber.getMsisdn());
                sb.append(" with DueDate lesser than : ");
                sb.append(CoreERLogger.formatERDateDayOnly(maxDueDate));
                sb.append(" Running Date : ");
                sb.append(CoreERLogger.formatERDateDayOnly(runningDate));
                LogSupport.debug(context, DunningProcessHelper.class.getName(), sb.toString());
            }

            And predicate = new And();
            predicate.add(new EQ(SubscriberAgedDebtXInfo.SUBSCRIBER_ID, subscriber.getId()));
            predicate.add(new LTE(SubscriberAgedDebtXInfo.DUE_DATE, maxDueDate));

            Collection<SubscriberAgedDebt> subAgedDebt = HomeSupportHelper.get(context).getBeans(context, SubscriberAgedDebt.class,
                    predicate, 1, false, SubscriberAgedDebtXInfo.DEBT_DATE);
            if (subAgedDebt.size() > 0)
            {
                oldestAgedDebtToLook = subAgedDebt.iterator().next().getDueDate();
                 if (LogSupport.isDebugEnabled(context))
                 {
                     StringBuilder sb = new StringBuilder();
                     sb.append("SubscriberAgedDebts found for Account :");
                     sb.append(subscriber.getBAN());
                     sb.append(" Subscriber :");
                     sb.append(subscriber.getMsisdn());
                     sb.append(" with DueDate lesser than : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(maxDueDate));
                     sb.append(", Setting oldestAgedDebtToLook : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(oldestAgedDebtToLook));
                     sb.append(", Running Date : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(runningDate));
                     LogSupport.debug(context, DunningProcessHelper.class.getName(), sb.toString());
                 }
            }
            else
            {
                oldestAgedDebtToLook = maxDueDate;
                 if (LogSupport.isDebugEnabled(context))
                 {
                     StringBuilder sb = new StringBuilder();
                     sb.append("No AgedDebts found for Account :");
                     sb.append(subscriber.getBAN());
                     sb.append(" Subscriber :");
                     sb.append(subscriber.getMsisdn());
                     sb.append(" with DueDate lesser than : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(maxDueDate));
                     sb.append(", Setting oldestAgedDebtToLook : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(oldestAgedDebtToLook));
                     sb.append(", Running Date : ");
                     sb.append(CoreERLogger.formatERDateDayOnly(runningDate));
                     LogSupport.debug(context, DunningProcessHelper.class.getName(), sb.toString());
                 }
            }

            if (agedDebtBreakdownDate.before(oldestAgedDebtToLook))
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Account :");
                    sb.append(subscriber.getBAN());
                    sb.append(", Subscriber :");
                    sb.append(subscriber.getMsisdn());
                    sb.append(", agedDebtBreakdownDate : ");
                    sb.append(CoreERLogger.formatERDateDayOnly(agedDebtBreakdownDate));
                    sb.append(" is before oldestAgedDebtToLook : ");
                    sb.append(CoreERLogger.formatERDateDayOnly(oldestAgedDebtToLook));
                    sb.append(", Running Date : ");
                    sb.append(CoreERLogger.formatERDateDayOnly(runningDate));
                    LogSupport.debug(context, DunningProcessHelper.class.getName(), sb.toString());
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
            sb.append("Fetching SubscriberAgedDebts for Account :");
            sb.append(subscriber.getBAN());
            sb.append(" Subscriber :");
            sb.append(subscriber.getMsisdn());
            sb.append(" with DueDate greater than : ");
            sb.append(CoreERLogger.formatERDateDayOnly(oldestAgedDebtToLook));
            sb.append(", Running Date : ");
            sb.append(CoreERLogger.formatERDateDayOnly(runningDate));
            LogSupport.debug(context, DunningProcessHelper.class.getName(), sb.toString());
        }
        return oldestAgedDebtToLook;
    }
	
	public static Date getMaxDueDateForLevel(final Context ctx, DunningPolicy policy, int index,Date runningDate)
    {
        int graceDays = 0;
        try{
        graceDays = policy.getLevelAt(ctx, index).getGraceDays();
        }catch(Exception e){
			LogSupport.debug(ctx, DunningProcessHelper.class.getName(), "Can not retrive grace days");
		}
        
        Date maxDueDate = getMaxDueDate(ctx,runningDate,graceDays);
        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Maximum Due Date for moving to Level '");
            sb.append(index+1);
            sb.append("' state: '");
            sb.append(CoreERLogger.formatERDateDayOnly(maxDueDate));
            sb.append("'");
            LogSupport.debug(ctx, DunningProcessHelper.class.getName(), sb.toString());
        }
        return maxDueDate;
    }
	
	public static Date getMaxDueDate(Context ctx,Date runningDate,int graceDays)
	{
		Date reportDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(runningDate);
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(reportDate);

        calendar.add(Calendar.DAY_OF_YEAR, -graceDays);
        return calendar.getTime();          
	}
	
	public static Account getResponsibleAccout(final Context context, final Account account) throws IllegalStateException
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
            LogSupport.minor(context, DunningProcessHelper.class.getName(), sb.toString(), exception);
            throw new IllegalStateException(sb.toString(), exception);
        }
    }
	
	public static boolean isAccountTemporaryDunningExempt(final Context context, final Account account,Date runningDate)
            throws DunningProcessException
    {
        boolean result = false;
        
        if (AccountStateEnum.PROMISE_TO_PAY.equals(account.getState())
                && runningDate.before(account.getPromiseToPayDate()))
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
                LogSupport.debug(context, DunningProcessHelper.class.getName(), sb.toString(), null);
            }
            result = true;
        }else if(account.getIsDunningExempted())
        {
        	if (LogSupport.isDebugEnabled(context))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Account '");
                sb.append(account.getBAN());
                sb.append("' is dunning exempt: '");
                sb.append(account.getIsDunningExempted());
                LogSupport.debug(context, DunningProcessHelper.class.getName(), sb.toString(), null);
            }
            result = true;
        }
        else
        {
            final Collection<Subscriber> subscribers = AbstractDunningAccountProcessor.getDunnableSubscribers(context, account);
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
                    LogSupport.debug(context, DunningProcessHelper.class.getName(), sb.toString(), null);
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
            LogSupport.info(context, DunningProcessHelper.class.getName(), sb.toString());
        }
        return result;
    }
	
	private static Collection<Account> getDunnableNonResponsibleSubAccounts(final Context ctx, final Account account)
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
            LogSupport.minor(ctx, DunningProcessHelper.class.getName(), sb.toString(), e);
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
    private static String formatPromiseToPayDate(final Account account)
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
	
	private static Date getAgedDebtDate(final Context context, final int month, Date runningDate)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(runningDate);
        calendar.add(Calendar.MONTH, -month);
        return calendar.getTime();
    }

	public static int calculateTemporaryDunningWaiverDays(Context context,
			Account account)
	{
		Collection<DunningWaiver> list=null;
		int totalExemptedDays=0;
		And and= new And();
		and.add(new EQ(DunningWaiverXInfo.BAN, account.getBAN()));
		Or or =  new Or();
		or.add(new EQ(DunningWaiverXInfo.STATUS, OTGDunningStatusEnum.PENDING_INDEX));
		or.add(new EQ(DunningWaiverXInfo.STATUS, OTGDunningStatusEnum.APPLIED_INDEX));
		and.add(or);
		
		try 
		{
			list=HomeSupportHelper.get(context).getBeans(context, DunningWaiver.class,and);
		}  catch (HomeException e) {
			
			LogSupport.major(context, "Failed to calculate waiver end dat", e);
		}
		
		if(list!=null && !list.isEmpty())
		{
			for(DunningWaiver dw: list)
			{
				totalExemptedDays+=dw.getExemptedDays();
			}
		}
		
		 if (LogSupport.isDebugEnabled(context))
         {
			 StringBuilder sb = new StringBuilder();
	         sb.append("Account '");
	         sb.append(account.getBAN());
	         sb.append("' has total expemted days "+totalExemptedDays );
	         LogSupport.debug(context, DunningProcessHelper.class.getName(), sb.toString());
	     }
		
		return totalExemptedDays;
	}

	public static boolean ignoreSubscriberAgedDebt(Context context, Account account) throws DunningProcessException {
		
		CRMSpid spid = null;
		try 
		{
			spid = SpidSupport.getCRMSpid(context, account.getSpid());
		} 
		catch (HomeException e) 
		{
			throw new DunningProcessException("Unable to find service provider details",e);
		}
		
		return spid.getIgnoreSubscriptionAgedDebtInDunning();
		
	
	}

}
