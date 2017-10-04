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
package com.trilogy.app.crm.dunning.notice;

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AgedDebt;
import com.trilogy.app.crm.bean.CreditEventTypeEnum;
import com.trilogy.app.crm.bean.CreditNotificationEvent;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.dunning.DunningReportRecord;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningAccountProcessor;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.CalendarSupportHelper;


/**
 * Processor responsible to verify if messages should be sent to an account during
 * pre-dunning notification.
 * 
 * @author Marcio Marques
 * @since 9.0
 */
public class DunningNoticeAccountProcessor extends AbstractDunningAccountProcessor
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new DunningProcessingAccountVisitor visitor.
     * 
     * @param report
     */
    public DunningNoticeAccountProcessor(final Context context, final Account account, final CreditNotificationEvent notificationEvent)
    {
        super(CalendarSupportHelper.get(context).findDateDaysAfter(-notificationEvent.getEventDateOffset(), new Date()));
        creditEventType_ = notificationEvent.getEventTypeIndex();
        account_ = account;
        if(LogSupport.isDebugEnabled(context))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Created DunningNoticeAccountProcessor for Account '");
            sb.append(account.getBAN());
            sb.append("' based on its forecasted state on date '");
            sb.append(CoreERLogger.formatERDateDayOnly(getRunningDate()));
            sb.append("', notificationEvent : ");
            sb.append(notificationEvent);            
            LogSupport.debug(context, this, sb.toString());
        }
        try
        {
            if (account_.isResponsible() && !account_.isPrepaid())
            {
                record_ = process(context, account);
            }
        }
        catch (DunningProcessException e)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Unable to calculate whether or not to send dunning notice to account '");
            sb.append(account.getBAN());
            sb.append("' based on its forecasted state on date '");
            sb.append(CoreERLogger.formatERDateDayOnly(getRunningDate()));
            sb.append("'.");
            LogSupport.minor(context, this, sb.toString());
            record_ = null;
        }

    }


    /**
     * Retrieves the process name.
     * 
     * @return
     */
    public static String getVisitorProcessName()
    {
        return "Pre-Dunning Notification Processing";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getProcessName()
    {
        return getVisitorProcessName();
    }
    
    /**
     * Returns whether or not a notice should be sent for the account, based on the
     * CreditNotificationEvent received on this object creation.
     * @param context
     * @param account
     * @return
     */
    public boolean sendNotice(final Context ctx)
    {
        boolean result = false;
        if (account_.isResponsible() && !account_.isPrepaid())
        {
            result = shouldSendNotice();
        }
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuffer sb = new StringBuffer();
            if (result)
            {
                sb.append("Sending dunning notice to account '");
            }
            else
            {
                sb.append("Not sending dunning notice to account '");
            }
            
            sb.append(account_.getBAN());
            sb.append("': ");
            sb.append("Credit Event Type = ");
            sb.append(creditEventType_);
            if (record_!=null)
            {
//                sb.append(", Forecasted transition = '");
//                sb.append(record_.getCurrentState().getDescription());
//                sb.append("' -> '");
//                sb.append(record_.getForecastedLevel().getDescription());
//                sb.append("'");
            }
            else
            {
                sb.append(". No Record created.");
            }
            
            LogSupport.debug(ctx, this, sb.toString());
        }
        
        return result;
    }
    
    public DunningReportRecord getDunningReportRecord()
    {
        return record_;
    }

    public AgedDebt getAgedDebt()
    {
        AgedDebt result = null;
        
        if (record_ != null)
        {
            result = record_.getDunnedAgedDebt();
        }
        
        return result;
    }
    
    
    @Override
    protected boolean processOnlyExactDate()
    {
        return true;
    }
    
    private boolean shouldSendNotice()
    {
        boolean result = false;
		//Forcasted State and Current State can be same in case of generating notices for After configuration. Hence, modified the logic
        if (record_ != null)
        {
            switch (creditEventType_)
            {
//            case CreditEventTypeEnum.DUNNING_WARNING_INDEX:
//                result = record_.getForecastedLevel().equals(AccountStateEnum.NON_PAYMENT_WARN) && 
//                            (record_.getCurrentState().equals(record_.getForecastedLevel()) || record_.getCurrentState().equals(AccountStateEnum.PROMISE_TO_PAY) || 
//                             record_.getCurrentState().equals(AccountStateEnum.ACTIVE));
//                break;
//            case CreditEventTypeEnum.DUNNING_DUNNED_INDEX:
//                result = record_.getForecastedLevel().equals(AccountStateEnum.NON_PAYMENT_SUSPENDED) && 
//                            (record_.getCurrentState().equals(record_.getForecastedLevel()) || record_.getCurrentState().equals(AccountStateEnum.PROMISE_TO_PAY) || 
//                             record_.getCurrentState().equals(AccountStateEnum.ACTIVE) ||
//                             record_.getCurrentState().equals(AccountStateEnum.NON_PAYMENT_WARN));
//                break;
//            case CreditEventTypeEnum.DUNNING_IN_ARREARS_INDEX:
//                result = record_.getForecastedLevel().equals(AccountStateEnum.IN_ARREARS) && 
//                            (record_.getCurrentState().equals(record_.getForecastedLevel()) || record_.getCurrentState().equals(AccountStateEnum.PROMISE_TO_PAY) || 
//                             record_.getCurrentState().equals(AccountStateEnum.ACTIVE) ||
//                             record_.getCurrentState().equals(AccountStateEnum.NON_PAYMENT_WARN) ||
//                             record_.getCurrentState().equals(AccountStateEnum.NON_PAYMENT_SUSPENDED));
//                break;
            }
        }
        
        return result;
    }

    private int creditEventType_;
    
    private Account account_;
    
    private DunningReportRecord record_ = null;

}
