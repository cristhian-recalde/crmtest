/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.poller.agent;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberCltcErCommandEnum;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.home.sub.SubscriberHomeFactory;
import com.trilogy.app.crm.poller.FilterOutException;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.event.ABMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.util.SimpleLocks;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;


/**
 * @author vcheng
 */
public class ABMLowBalanceAgent implements ContextAgent
{

    public ABMLowBalanceAgent(CRMProcessor processor)
    {
        super();
        processor_ = processor;
    }
    
    public void execute(Context ctx) throws AgentException
    {
        List params = new ArrayList();
        ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);
        
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute()");
        
        try {
        	CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',', info.getErid(), this);
        } catch (FilterOutException e){
        	return;
        }

        String  subId = null; 
        final SimpleLocks locker = (SimpleLocks) ctx.get(SubscriberHomeFactory.SUBCRIBER_LOCKER);

        try
       	{
 	        if (LogSupport.isDebugEnabled(ctx))
   	        {
   	           new DebugLogMsg(this, ABMProcessor.getDebugParams(params), null).log(ctx);
   	        }
   	        
   	        // MSISDN from the ER.
   	        final String msisdnStr = CRMProcessorSupport.getField(params, INDEX_MSISDN);
   	        String msisdn = "";
   	        try
   	        {
   	            msisdn = CRMProcessorSupport.getMsisdn(msisdnStr);
   	        }
   	        catch (final ParseException exception)
   	        {
   	            throw new HomeException(
   	                "Could not parse Msisdn \"" + msisdnStr + "\".",
   	                exception);
   	        }
   	        
   	        // Subscriber with the MSISDN
   	        Subscriber subscriber = null;
    	     
   	        try
   	        {
   	       	    subId =  SubscriberSupport.lookupSubscriberIdForMSISDN(ctx, msisdn, new Date(info.getDate()));

            	if (locker != null && subId != null )
            	{
            		locker.lock(subId); 
            	}

   	            subscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn, new Date(info.getDate()));
   	        }
   	        catch (final HomeException exception)
   	        {
   	            throw new HomeException(
   	                "Failed to look-up subscriber for MSISDN \"" + msisdnStr + "\".",
   	                exception);
   	        }
   	        
            ABMUnifiedBillingAgent.createSubscriberCltc(ctx, params);
   	        
            /*if (subscriber.getSubscriberType().equals(SubscriberTypeEnum.PREPAID))
   	        {
   	        	processPrepaidSubscriberLowBalance(ctx, params, subscriber, info.getDate(), info.getErid(), info.getRecord(), info.getStartIndex());
   	        }
   	        else
   	        {
   	        	ABMUnifiedBillingAgent.createPostpaidSubscriberCltc(ctx, params);
   	        }*/
   	        	
   	    }
       	catch (final Throwable t)
       	{
            new MinorLogMsg(this, "Failed to process ABM ER because of Exception " + t.getMessage(), t).log(ctx);
            processor_.saveErrorRecord(ctx, info.getRecord());
        }
       	finally 
       	{
       		if (locker != null && subId !=null  )
       		{
       			locker.unlock(subId); 
       		}
            pmLogMsg.log(ctx);
            CRMProcessor.playNice(ctx, CRMProcessor.MEDIUM_ER_THROTTLING);
        }
    }


    private void processPrepaidSubscriberLowBalance(Context ctx, List params, Subscriber subscriber, long date,
            String erid, char[] record, int startIndex) throws NumberFormatException, IndexOutOfBoundsException,
            HomeException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "processPrepaidSubscriberLowBalance()");
        
        try
        {
            final short command =
                Short.parseShort((String) params.get(INDEX_COMMAND));
            
            // Whether it is up and cross the threshold.
            boolean isUpCrossThreshold = false;
            if (SubscriberCltcErCommandEnum.get(command) ==
                SubscriberCltcErCommandEnum.PROVISION)
            {
                isUpCrossThreshold = true;
            }
            
            boolean doUpdate = false;
            if (subscriber.getSubscriberType().equals(SubscriberTypeEnum.PREPAID))
            {
	            // Do things for the following states.
	            if (subscriber.getState().equals(SubscriberStateEnum.ACTIVE) ||
	                subscriber.getState().equals(SubscriberStateEnum.EXPIRED) ||
	                subscriber.getState().equals(SubscriberStateEnum.LOCKED) ||
	                subscriber.getState().equals(SubscriberStateEnum.SUSPENDED))
	            {
	                subscriber.setAboveCreditLimit(isUpCrossThreshold);
	                             
	                // Activate subscriber if it is up cross the threshold.
	                if (isUpCrossThreshold)
	                {
	                    // Do state change only if the subscriber is Suspended.
	                    if (subscriber.getState().equals(SubscriberStateEnum.SUSPENDED))
	                    {
	                        subscriber.setState(SubscriberStateEnum.ACTIVE);	                        
	                    }
	                }
	                else  // Suspend subscriber if it is down cross the threshold.
	                {
	                    // Do state change only if the subscriber is Active.
	                    if (subscriber.getState().equals(SubscriberStateEnum.ACTIVE))
	                    {
	                        subscriber.setState(SubscriberStateEnum.SUSPENDED);	                        
	                    }
	                }	
	                doUpdate = true;
	            }
            }

            if (doUpdate)
            {
                try
                {
                    Home home = (Home) ctx.get(SubscriberHome.class);
                    home.store(ctx,subscriber);
                }
                catch (final HomeException exception)
                {
                    throw new HomeException(
                        "Failed to update subscriber \"" + subscriber.getId() + "\".",
                        exception);                	
                }
            }
        }
        finally
        {
        
        	pmLogMsg.log(ctx);

        }
    }
    
    private CRMProcessor processor_ = null;

    protected static final String PM_MODULE = ABMLowBalanceAgent.class.getName();
    protected static final int INDEX_MSISDN = 3;
    protected static final int INDEX_COMMAND = 8;
}