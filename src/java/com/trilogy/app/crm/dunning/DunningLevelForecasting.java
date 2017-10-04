package com.trilogy.app.crm.dunning;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AgedDebt;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.troubleticket.bean.Dispute;
import com.trilogy.app.crm.troubleticket.bean.DisputeHome;
import com.trilogy.app.crm.troubleticket.bean.DisputeStateEnum;
import com.trilogy.app.crm.troubleticket.bean.DisputeXInfo;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.holder.LongHolder;
import com.trilogy.framework.xhome.holder.ObjectHolder;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import AccountFinancialInfoDunningResult;

public class DunningLevelForecasting implements Forecastable, ReportForecastable
{

    @SuppressWarnings("unchecked")
    @Override
    public DunningLevel calculateForecastedLevel(Context context, AbstractBean bean,
            List<? extends AbstractBean> ageDebtRecords, Currency currency, boolean paymentsConsidered, Date runningDate,
            DunningPolicy policy)
    {
        Account account = (Account) bean;
        List<AgedDebt> ageDebts = (List<AgedDebt>) ageDebtRecords;
        AgedDebt currentAgedDebt = null;
        DunningLevel nextLevel = null;
        DunningLevel matchedLevel = DunningLevel.LEVEL_0;
        long currentDebtAmount = 0;
        int tempDunningWaiverDay=0;
        
        if(context.has(DunningConstants.DUNNING_IS_OTG_APPLIED) && context.getBoolean(DunningConstants.DUNNING_IS_OTG_APPLIED))
        {
        	tempDunningWaiverDay=DunningProcessHelper.calculateTemporaryDunningWaiverDays(context,account);
        }
        List<DunningLevel> levels = policy.getAllLevels(context);
        
        Collections.reverse(ageDebts);
        
        for (AgedDebt agedDebt : ageDebts)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Processing AgedDebt for Account :");
                sb.append(account.getBAN());
                sb.append(", agedDebt : ");
                sb.append(agedDebt);
                sb.append(" Running Date : ");
                sb.append(CoreERLogger.formatERDateDayOnly(runningDate));
                LogSupport.debug(context, this, sb.toString());
            }
            boolean isDebtOutstanding = false;
            for (int i = levels.size(); i > 0; i--)
            {
            	int tempGracePeriodDay=0;
                nextLevel = policy.getLevelAt(context, i - 1);
                
                if(nextLevel.getId()== 1)
                {
                	tempGracePeriodDay = account.getDunningGracePeriod();
                	tempGracePeriodDay = tempGracePeriodDay+nextLevel.getGraceDays();
                }else
                {
                	tempGracePeriodDay = nextLevel.getGraceDays();
                }
                
                if(context.has(DunningConstants.DUNNING_IS_OTG_APPLIED) && context.getBoolean(DunningConstants.DUNNING_IS_OTG_APPLIED))
                {
                	tempGracePeriodDay+=tempDunningWaiverDay;
                	
                	if (LogSupport.isDebugEnabled(context))
                    {
           			 StringBuilder sb = new StringBuilder();
           	         sb.append("OTG of ");
           	         sb.append(tempDunningWaiverDay);
           	         sb.append(" is added to ");
           	         sb.append("Level :");
           	         sb.append(nextLevel + " grace days");
           	         sb.append(" for Account " +account.getBAN());
           	         LogSupport.debug(context, this, sb.toString());
           	     }
                }
                Date levelGracePeriodDate = DunningProcessHelper.getMaxDueDate(context, runningDate,tempGracePeriodDay);
                LogSupport.debug(context, this, "levelGracedatePeriod for account "+account.getBAN()+" is "+levelGracePeriodDate);
                
                if (currentAgedDebt == null && (agedDebt.getDueDate().compareTo(levelGracePeriodDate)) <= 0)
                {
                    currentAgedDebt = agedDebt;
                    
                    try {
						currentDebtAmount = getCurrentAmountOwing(context, account, currentAgedDebt, paymentsConsidered, runningDate);
					} catch (Throwable e) {
	    				if (LogSupport.isDebugEnabled(context)) {
	    					LogSupport.debug(context, this, e);
	    				}
					}
                    isDebtOutstanding = isDebtOutstanding(context, account, currentAgedDebt, currentDebtAmount,
                            currency, policy);
                    break;
                }
            }
            if (isDebtOutstanding)
            {
            	matchedLevel = nextLevel;
            	break;            	
            }
            else
            {
            	currentAgedDebt = null;
                currentDebtAmount = 0;
            }
                
        }
        Collections.reverse(ageDebts);
       
        
        return matchedLevel;
    }
	
	@SuppressWarnings("unchecked")
	@Override
	public DunningLevel calculateForecastedLevel(Context context, AbstractBean bean,
	        List<? extends AbstractBean> agedDebtRecords, Currency currency,
	        List<? extends AbstractBean> dunningReportRecordAgedDebt, boolean current, LongHolder dunningAmount,
	        ObjectHolder dunnedAgedDebt, Date runningDate, DunningPolicy policy)
	{
	    
	    Account account = (Account)bean;
	    List<AgedDebt> agedDebts = (List<AgedDebt>)agedDebtRecords;
	    List<DunningReportRecordAgedDebt> reportRecordAgedDebt = (List<DunningReportRecordAgedDebt>)dunningReportRecordAgedDebt;
	    Collections.reverse(agedDebts);
	    AgedDebt currentAgedDebt = null;
	    DunningLevel nextLevel = null;
	    DunningLevel matchedLevel = DunningLevel.LEVEL_0;
	    long currentDebtAmount = 0;
	    List<DunningLevel> levels = policy.getAllLevels(context);
	   // Collections.reverse(levels);
	    int tempDunningWaiverDay=0;
	    if(context.has(DunningConstants.DUNNING_IS_OTG_APPLIED) && context.getBoolean(DunningConstants.DUNNING_IS_OTG_APPLIED))
        {
        	tempDunningWaiverDay=DunningProcessHelper.calculateTemporaryDunningWaiverDays(context,account);
        }
	    
	    boolean ptpExpired = false;
        if (account.getState().equals(AccountStateEnum.PROMISE_TO_PAY))
        {
            Date ptpDate = account.getPromiseToPayDate();
            ptpExpired = runningDate.compareTo(ptpDate) == 0
                    || (CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(ptpDate).compareTo(ptpDate) != 0 && CalendarSupportHelper
                            .get(context).getNumberOfDaysBetween(ptpDate, runningDate) == 1);
        }
	    
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
	             sb.append(CoreERLogger.formatERDateDayOnly(runningDate));
	             LogSupport.debug(context, this, sb.toString());
	         }
	    	 boolean isDebtOutstanding = false;
	
	        //populateRecordAgedDebt(context, agedDebt, recordAgedDebt, agedDebtDates, current);
	    	for(int i=levels.size();i>0;i--)
	    	{
	    		nextLevel = policy.getLevelAt(context, i-1);
	    		int tempGracePeriodDay=0;
	    		if(nextLevel.getId()== 1)
                {
                	tempGracePeriodDay = account.getDunningGracePeriod();
                	tempGracePeriodDay = tempGracePeriodDay+nextLevel.getGraceDays();
                }else
                {
                	tempGracePeriodDay = nextLevel.getGraceDays();
                }
	    		
	    		if(context.has(DunningConstants.DUNNING_IS_OTG_APPLIED) && context.getBoolean(DunningConstants.DUNNING_IS_OTG_APPLIED))
                {
                	tempGracePeriodDay+=tempDunningWaiverDay;
                	
                	if (LogSupport.isDebugEnabled(context))
                    {
           			 StringBuilder sb = new StringBuilder();
           	         sb.append("OTG of");
           	         sb.append(tempDunningWaiverDay);
           	         sb.append("' is added to ");
           	         sb.append("Level :");
           	         sb.append(nextLevel + "grace days");
           	         sb.append("for Account " +account.getBAN());
           	         LogSupport.debug(context, this, sb.toString());
           	     }
                }
	    		
	    		Date levelGracePeriodDate = DunningProcessHelper.getMaxDueDate(context,runningDate,tempGracePeriodDay);
	    		LogSupport.debug(context, this, "levelGracedatePeriod for account "+account.getBAN()+" is "+levelGracePeriodDate);
	    		
	    		if(currentAgedDebt == null && ((agedDebt.getDueDate().compareTo(levelGracePeriodDate))==0 || 
	    				(ptpExpired && (agedDebt.getDueDate().compareTo(levelGracePeriodDate))<=0)))
	    		{
	    			currentAgedDebt = agedDebt;
	    			try{
	    				currentDebtAmount = getCurrentAmountOwing(context, account, currentAgedDebt, current, runningDate);
	    			} catch (Throwable e) {
	    				if (LogSupport.isDebugEnabled(context))
	    				{
	    					LogSupport.debug(context, this, e);
	    				}
					}
	    			isDebtOutstanding = isDebtOutstanding(context, account, currentAgedDebt, currentDebtAmount,currency,policy);
	    			break;
	    		}else if(agedDebt.getDueDate().before(levelGracePeriodDate))
	    		{
	    			break;
	    		}
	    	}
	    	
	    	if (isDebtOutstanding)
            {
	    		matchedLevel = nextLevel;
	    		break;            	
            }
            else
            {
            	currentAgedDebt = null;
                currentDebtAmount = 0;
            }
	    	
	    }
	    
	    dunningAmount.setValue(currentDebtAmount);
	    dunnedAgedDebt.setObj(currentAgedDebt);
	    	
	    Collections.reverse(agedDebts);
	   // Collections.reverse(levels);
	
	    return matchedLevel;
	}
	 
	private long getCurrentAmountOwing(final Context ctx, final Account account, 
			final AgedDebt agedDebt, final boolean current, Date runningDate) throws Throwable
    {
        long result;
        
        try {
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
        		result = getAmountOwingWithDisputeAmount(ctx, account, result, crmSpid, runningDate);
        	}
        } 
		catch (Throwable t) 
		{
			throw t;
		}
		
        return result;
    }

	private long getAmountOwingWithDisputeAmount(final Context ctx, final Account account, long result, CRMSpid crmSpid, Date runningDate)
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

					if(runningDate.before(resolutionBufferDate)) {
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

    public boolean isDebtOutstanding(final Context context, final Account account, final AgedDebt agedDebt,
            final long amountOwing, final Currency currency, DunningPolicy policy)
    {
        boolean result = false;
        if (agedDebt != null && amountOwing > 0)
        {
            final double policyOwingThreshold = calculateAgedDebtOwingThreshold(context, agedDebt, currency,
                    policy);
            result = isDebtOutstanding(context, account, agedDebt, amountOwing, policyOwingThreshold,
                    currency);
        }
        return result;
    }


    private double calculateAgedDebtOwingThreshold(final Context ctx, final AgedDebt agedDebt, final Currency currency,
            DunningPolicy policy)
    {
        double threshold;
        int thresholdPercentage;
        if(policy.getDunningConfig()==DunningConfigTypeEnum.SPID)
        {
        	CRMSpid crmSpid = (CRMSpid)ctx.get(CRMSpid.class);
			if(null == crmSpid){
        		try {
					crmSpid = SpidSupport.getCRMSpid(ctx, policy.getSpid()); 
				} catch (HomeException e) {
				}
        	}
        	threshold = crmSpid.getMinimumOwingThreshold();
        	thresholdPercentage =crmSpid.getThreshold();
        }else
        {
        	threshold = policy.getMinimumOwingThreshold();
        	thresholdPercentage = policy.getThreshold();
        }
        
        threshold = Math.max(threshold,
                calculateMaxAmountOwedBasedOnMinPercentOwingThreshold(agedDebt.getAccumulatedTotalAmount(), thresholdPercentage));
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


    private long calculateMaxAmountOwedBasedOnMinPercentOwingThreshold(final long totalAmount, int thresholdPercentage)
    {
        final double threshold = thresholdPercentage / 100.0;
        return Math.round(totalAmount * threshold);
    }


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
        
        AccountFinancialInfoDunningResult isDebtOutsResult = new AccountFinancialInfoDunningResult();
        isDebtOutsResult.setAgedDebt(debt);
        isDebtOutsResult.setDebtOutstanding(result);
        context.put("DEBT_OUTSTANDING_"+account.getBAN(),isDebtOutsResult);
        
        return result;
    }
    
}
