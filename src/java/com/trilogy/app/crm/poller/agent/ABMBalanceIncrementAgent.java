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
package com.trilogy.app.crm.poller.agent;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.home.sub.SubscriberHomeFactory;
import com.trilogy.app.crm.poller.FilterOutException;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.event.ABMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.subscriber.state.PoolHandlingSubscriberStateMutator;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SubscriptionNotificationSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.util.SimpleLocks;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.util.snippet.log.Logger;

/**
 * Agent that polls the ER442
 *
 * @author crm.team@redknee.com
 */
public class ABMBalanceIncrementAgent implements ContextAgent
{
    public ABMBalanceIncrementAgent(CRMProcessor processor)
    {
        super();
        processor_ = processor;
    }

    public void execute(Context ctx) throws AgentException
    {
        List params = new ArrayList();
        ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);

        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute()");

        final SimpleLocks locker = (SimpleLocks) ctx.get(SubscriberHomeFactory.SUBCRIBER_LOCKER);
        String subId = null; 

        try
        {
            try
            {
                CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',', info.getErid(),
                        this);
            }
            catch (FilterOutException e)
            {
                return;
            }
            if (Logger.isDebugEnabled())
            {
                Logger.debug(ctx, this, ABMProcessor.getDebugParams(params));
            }

            if (params.size() < REQUIRED_LENGTH)
            {
                throw new HomeException("The number of ER fields, " + params.size()
                        + ", is less than the minimum required, " + REQUIRED_LENGTH);
            }

            final String transactionTypeStr = CRMProcessorSupport.getField(params, INDEX_TRANSACTION_TYPE);
            final int transactionType;
            if (transactionTypeStr.length() == 0)
            {
                throw new HomeException("TransactionType missing!");
            }
            else
            {
                try
                {
                    transactionType = Integer.parseInt(transactionTypeStr);
                }
                catch(NumberFormatException e)
                {
                    final String formattedMsg = MessageFormat.format(
                            "Could not parse TransactionType \"{0}\".",
                            new Object[] {transactionTypeStr});

                    throw new HomeException(formattedMsg, e);
                }
            }

            if (transactionType == ACTIVATION_TRANSACTION)
            {
                if (Logger.isInfoEnabled())
                {
                    Logger.info(ctx, this, "Skip ER442 for Activation event.");
                }
                return;
            }

            final String msisdnStr = CRMProcessorSupport.getField(params, INDEX_MSISDN);
            String msisdn = "";

            try
            {
                msisdn = CRMProcessorSupport.getMsisdn(msisdnStr);
            }
            catch (ParseException e)
            {
                final String formattedMsg = MessageFormat.format(
                        "Could not parse Msisdn \"{0}\".",
                        new Object[] {msisdnStr});

                throw new HomeException(formattedMsg, e);
            }

  
            Subscriber subscriber = null;
            
            
            try
            {
         	    subId =  SubscriberSupport.lookupSubscriberIdForMSISDN(ctx, msisdn, new Date(info.getDate()));

            	if (locker != null && subId != null )
            	{
            		locker.lock(subId); 
            	}
                subscriber = SubscriberSupport.lookupSubscriberForSubId(ctx, subId); 

            }
            catch (HomeException e)
            {
                final String formattedMsg = MessageFormat.format(
                        "Failed to look-up subscriber for MSISDN \"{0}\".",
                        new Object[] {msisdnStr});
                throw new HomeException(formattedMsg);
            }
          

            if (subscriber != null && subscriber.isPostpaid())
            {
                if (Logger.isDebugEnabled())
                {
                    Logger.debug(ctx, this, "Skip ER442 for postpaid subscriber.");
                }
                return;
            }

            // retrieve finalExpiryDate
            final String finalExpiryDateStr = CRMProcessorSupport.getField(params, INDEX_FINAL_EXPIRYDATE);
            final Date finalExpiryDate;

            // expiryDate might be empty string in ER in case subscriber�s Expiry Date is 0
            // (i.e. The balance will never expire.)
            if (finalExpiryDateStr.length() == 0)
            {
                finalExpiryDate = null;
            }
            else
            {
                try
                {
                    finalExpiryDate = CalendarSupportHelper.get(ctx).convertDateWithNoTimeOfDayToTimeZone(
                            CRMProcessorSupport.getDateOnly(finalExpiryDateStr), subscriber.getTimeZone(ctx));
                }
                catch (ParseException e)
                {
                    final String formattedMsg = MessageFormat.format(
                            "Could not parse FinalExpiryDate String \"{0}\".",
                            new Object[] {finalExpiryDateStr});

                    throw new HomeException(formattedMsg, e);
                }
            }

            // We assume that no update is required to the subscriber. If
            // an update is necessary, then this flag should be set to
            // true. Only if this flag is true will Home.store() be called.
            boolean subscriberRequiresUpdate = false;
            final Date adjustedExpiryDate =
                    CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(subscriber.getExpiryDate());

            if ((!subscriber.isPostpaid())
                    && ((subscriber.getExpiryDate() == null && finalExpiryDate != null)
                            || (subscriber.getExpiryDate() != null && finalExpiryDate == null)
                    || (subscriber.getExpiryDate() != null && finalExpiryDate != null
                            && !adjustedExpiryDate.equals(finalExpiryDate))))
            {
            	if(finalExpiryDate.after(subscriber.getExpiryDate()))
            	{	
            		subscriber.setExpiryDate(finalExpiryDate);
            		subscriberRequiresUpdate = true;
            	}	
            }

            Date now = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());

            if ((SubscriberStateEnum.EXPIRED == subscriber.getState()
                        || SubscriberStateEnum.DORMANT == subscriber.getState())
                    && subscriber.getExpiryDate().after(now))
            {
                // a logic needs to be added here to activate all the pool members sharing the state
                subscriber.setState(SubscriberStateEnum.ACTIVE);
                // subscriber.setExpiryTimer(0);
                subscriberRequiresUpdate = true;
            }

            try
            {
                if (subscriberRequiresUpdate)
                {
                    // renew subscirber entry
                    if (subscriber.isPooledGroupLeader(ctx))
                    {
                        new PoolHandlingSubscriberStateMutator(SubscriberStateEnum.ACTIVE).mutate(ctx, subscriber);
                    }
                    ((Home) ctx.get(SubscriberHome.class)).store(ctx, subscriber);
                    
                    // send ExpiryDate SMS to subscriber
                    // Manda - Don't send the sms to postpaid subscriber
                    if (!subscriber.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID))
                    {
                        SubscriptionNotificationSupport.sendExpiryDateExtensionNotification(ctx, subscriber);
                    }
                }
            }
            catch (HomeException e)
            {
                final String formattedMsg = MessageFormat.format(
                        "Encounter Exception in ABMBalanceIncrementProcess when handling subscriber \"{0}\". "
                        + "Exception is: \"{1}\" .", new Object[] {subscriber.getId(), e.getMessage()});

                throw new HomeException(formattedMsg, e);
            }
            catch (Exception e)
            {
                final String formattedMsg = MessageFormat.format(
                        "Encounter Exception in ABMBalanceIncrementProcess when handling subscriber \"{0}\" . "
                        + "Exception is: \"{1}\" .", new Object[] {subscriber.getId(), e.getMessage()});

                throw new HomeException(formattedMsg, e);
            }
            finally 
            {
                

            }
        }
        catch (final Throwable t)
        {
            Logger.minor(ctx, this, "Failed to process ER 442 because of Exception " + t.getMessage(), t);
            processor_.saveErrorRecord(ctx, info.getRecord());
        }
        finally
        {
        	if( locker != null && subId != null )
            {	
            	locker.unlock(subId);
            }	
            pmLogMsg.log(ctx);
            CRMProcessor.playNice(ctx, CRMProcessor.HIGH_ER_THROTTLING);
        }
    }

    private CRMProcessor processor_ = null;

    protected static final int INDEX_MSISDN = 4;
    protected static final int INDEX_TRANSACTION_TYPE = 16;
    protected static final int INDEX_FINAL_EXPIRYDATE = 19;

    protected static final int ACTIVATION_TRANSACTION = 2;

    protected static final int REQUIRED_LENGTH = INDEX_FINAL_EXPIRYDATE + 1;

    public static final String PM_MODULE = ABMBalanceIncrementAgent.class.getName();
}
