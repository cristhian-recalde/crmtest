package com.trilogy.app.crm.dunning;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAgedDebt;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.util.CrmCommonUtil;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


public class SubscriberDunningLevelForecasting implements Forecastable
{

    @Override
    public DunningLevel calculateForecastedLevel(Context context, AbstractBean bean,
            List<? extends AbstractBean> subAgedDebtRecords, Currency currency, boolean paymentsConsidered,
            Date runningDate, DunningPolicy policy)
    {

        Subscriber subscriber = (Subscriber)bean;
        List<SubscriberAgedDebt> subscriberAgedDebts = (List<SubscriberAgedDebt>)subAgedDebtRecords;
        SubscriberAgedDebt currentSubAgedDebt = null;
        DunningLevel nextLevel = null;
        DunningLevel matchedLevel = DunningLevel.LEVEL_0;
        long currentDebtAmount = 0;
        long subPaymentFromLastInvoice = (subscriber != null ? subscriber.getPaymentSinceLastInvoice(context) : 0) ;
        
                
        List<DunningLevel> levels = policy.getAllLevels(context);
        //Collections.reverse(levels);
        Collections.reverse(subscriberAgedDebts);
        int tempDunningWaiverDay=0;
        if(context.has(DunningConstants.DUNNING_IS_OTG_APPLIED) &&context.getBoolean(DunningConstants.DUNNING_IS_OTG_APPLIED))
        {
        		try {
					tempDunningWaiverDay=DunningProcessHelper.calculateTemporaryDunningWaiverDays(context,subscriber.getAccount(context));
				} catch (HomeException e) {
					LogSupport.major(context, "Failed to calculate temporary waiver end date", e);
				}
			
        }
        
        
        
        for (SubscriberAgedDebt subAgedDebt : subscriberAgedDebts)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Processing SubscriberAgedDebt for Subscriber :");
                sb.append(subscriber.getMsisdn());
                sb.append(", SubscriberAgedDebt : ");
                sb.append(subAgedDebt);
                sb.append(" Running Date : ");
                sb.append(CoreERLogger.formatERDateDayOnly(runningDate));
                LogSupport.debug(context, this, sb.toString());
            }
            boolean isDebtOutstanding = false;
            
            for (int i = levels.size(); i > 0; i--)
            {	
            	int tempGracePeriodDay=0;
                nextLevel = policy.getLevelAt(context, i - 1);
                tempGracePeriodDay=nextLevel.getGraceDays();
                
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
           	         LogSupport.debug(context, this, sb.toString());
           	     }
                }
                
                
                Date levelGracePeriodDate = DunningProcessHelper.getMaxDueDate(context, runningDate,
                		tempGracePeriodDay);
                if (currentSubAgedDebt == null && (subAgedDebt.getDueDate().compareTo(levelGracePeriodDate)) <= 0)
                {
                    currentSubAgedDebt = subAgedDebt;
                    currentDebtAmount = getCurrentAmountOwing(currentSubAgedDebt, paymentsConsidered, subPaymentFromLastInvoice);
                    isDebtOutstanding = isDebtOutstanding(context, subscriber, currentSubAgedDebt, currentDebtAmount,
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
                currentSubAgedDebt = null;
                currentDebtAmount = 0;
            }
        }
        
        Collections.reverse(subscriberAgedDebts);
        //Collections.reverse(levels);

        return matchedLevel;
    }
    
    
    private long getCurrentAmountOwing(final SubscriberAgedDebt subAgedDebt, boolean current, long subPaymentFromLastInvoice)
    {
        final long result;
        if (current)
        {
            result = subAgedDebt.getCurrentAccumulatedDebt() + subPaymentFromLastInvoice;
        }
        else
        {
            result = subAgedDebt.getAccumulatedDebt();
        }
        return result;
    }
    
    private boolean isDebtOutstanding(final Context context, final Subscriber subscriber, final SubscriberAgedDebt subAgedDebt,
            final long amountOwing, final Currency currency, DunningPolicy policy)
    {
        boolean result = false;
        if (subAgedDebt != null && amountOwing > 0)
        {
            final double inArrearsInvoiceOwingThreshold = calculateAgedDebtOwingThreshold(context, subscriber, subAgedDebt, currency,
                    policy);
            result = isDebtOutstanding(context, subscriber, subAgedDebt, amountOwing, inArrearsInvoiceOwingThreshold,
                    currency);
        }
        return result;
    }
    
    private boolean isDebtOutstanding(final Context context, final Subscriber subscriber, final SubscriberAgedDebt debt,
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
                    sb.append("', Subscriber = '");
                    sb.append(subscriber.getMsisdn());
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
    
    
    private double calculateAgedDebtOwingThreshold(final Context ctx, final Subscriber subscriber, final SubscriberAgedDebt subAgedDebt, final Currency currency,
            DunningPolicy policy)
    {
        double threshold = 0.0;
        SubscriptionThresholdConfig config;
        
        
        config = subscriptionBasedThresholdConfig(ctx, subscriber, policy);
        
        //check for SubscriptionThresholdConfig - if configuration exist for particular SubscriptionType then take it forward to calculate OwningAmount
        // if not then take policy based configuration to move further.
        if(config != null)
        {
            threshold = config.getMinimumOwingThreshold();
            threshold = Math.max(threshold, calculateMaxAmountOwedBasedOnMinPercentOwingThreshold(subAgedDebt.getAccumulatedTotalAmount(), config.getThreshold()));
        }else 
        {
            // no configuration then just play with threshold % not with actual value.
            threshold = Math.max(threshold, calculateMaxAmountOwedBasedOnMinPercentOwingThreshold(subAgedDebt.getAccumulatedTotalAmount(), policy.getThreshold()));
        }
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Calculating invoice owing treshold. BAN = '");
            sb.append(subAgedDebt.getBAN());
            sb.append(" Subscriber = '");
            sb.append(subscriber.getMsisdn());
            sb.append(" SubscriptionThresholdConfig = '");
            sb.append(config);
            sb.append("', SubscriberInvoice date = '");
            sb.append(subAgedDebt.getDebtDate());
            sb.append("', Owing treshold = '");
            sb.append(currency.formatValue(Math.round(threshold)));
            sb.append("'.");
            LogSupport.debug(ctx, this, sb.toString(), null);
        }
        return threshold;
    }
    
    private long calculateMaxAmountOwedBasedOnMinPercentOwingThreshold(long totalAmount, double minThreshold)
    {
        final double threshold = minThreshold / 100.0;
        return Math.round(totalAmount * threshold);
    }
    
    private SubscriptionThresholdConfig subscriptionBasedThresholdConfig(final Context ctx, final Subscriber subscriber, final DunningPolicy policy)
    {
        if(policy.getDunningConfig() == DunningConfigTypeEnum.CUSTOM)
        {
            List<SubscriptionThresholdConfig> subscriptionThresholdList = (List<SubscriptionThresholdConfig>)policy.getSubscriptionThresholdDunningConfig();
            for (SubscriptionThresholdConfig subscriptionThresholdConfig : subscriptionThresholdList)
            {
                if(subscriptionThresholdConfig.getSubscriptionType() == subscriber.getSubscriptionType())
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append("DunningConfigTypeEnum.CUSTOM BAN = '");
                        sb.append(subscriber.getBAN());
                        sb.append(" Subscriber = '");
                        sb.append(subscriber.getMsisdn());
                        sb.append("', SubscriptionThresholdConfig = '");
                        sb.append(subscriptionThresholdConfig);
                        sb.append("'.");
                        LogSupport.debug(ctx, this, sb.toString(), null);
                    }
                    return subscriptionThresholdConfig;
                }else
                    continue;
            } 
        }else if(policy.getDunningConfig() == DunningConfigTypeEnum.SPID)
        {
            CRMSpid spid;
            try
            {
                spid = CrmCommonUtil.retrieveSpid(ctx, subscriber.getSpid());
                SubscriptionThresholdConfig subscriptionThresholdConfig = new SubscriptionThresholdConfig(subscriber.getSubscriptionType(), spid.getMinimumOwingThreshold(), spid.getThreshold());
                if (LogSupport.isDebugEnabled(ctx))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("DunningConfigTypeEnum.CUSTOM BAN = '");
                    sb.append(subscriber.getBAN());
                    sb.append(" Subscriber = '");
                    sb.append(subscriber.getMsisdn());
                    sb.append("', SubscriptionThresholdConfig = '");
                    sb.append(subscriptionThresholdConfig);
                    sb.append("'.");
                    LogSupport.debug(ctx, this, sb.toString(), null);
                }
                return subscriptionThresholdConfig;
            }
            catch (HomeException e)
            {
                new DunningProcessException(e.getMessage(), e);
            }
        }
        
        return null;
    }
}
