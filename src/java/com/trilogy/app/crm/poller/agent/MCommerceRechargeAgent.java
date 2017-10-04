/*
 * MCommerceRechargeAgent.java
 * 
 * Author : danny.ng@redknee.com Date : Mar 03, 2006
 * 
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.filter.AdjustmentTypeByName;
import com.trilogy.app.crm.home.pipelineFactory.AdjustmentTypeHomePipelineFactory;
import com.trilogy.app.crm.poller.Constants;
import com.trilogy.app.crm.poller.FilterOutException;
import com.trilogy.app.crm.poller.MCommercePoller;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.poller.event.MCommerceRechargeProcessor;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * Agent that acutally performs operations with the ER.
 * 
 * @author danny.ng@redknee.com
 * @since Mar 03, 2006
 */
public class MCommerceRechargeAgent implements ContextAgent, Constants
{

    public MCommerceRechargeAgent(CRMProcessor processor)
    {
        super();
        processor_ = processor;
    }
    
    public void execute(Context ctx) throws AgentException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "starting to execute", null).log(ctx);
        }
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute()");
        
        List params = new ArrayList();
        ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);
        
        try
        {
        	try {
        		CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',', info.getErid(),this);
           	} catch ( FilterOutException e){
				return; 
			}

            switch (Integer.parseInt(info.getErid()))
            {
                case MCommercePoller.MCOMMERCE_ER_IDENTIFIER:
                {
                    createTransaction(ctx, new Date(info.getDate()), params);
                    break;
                }
                default:
                {
                    // Unknown MCommerce ER -- Ignore.
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "Unknown MCommerce ER -- Ignoring ER" + info.getErid(), null).log(ctx);
                    }
                }
            }
        }
        catch (final Throwable t)
        {
            new MajorLogMsg(this, "Failed to process ER 1251 because of Exception " + t.getMessage(), t).log(ctx);
            processor_.saveErrorRecord(ctx, info.getRecord());
        }
        finally
        {
            pmLogMsg.log(ctx);
        }
    }
    
    
    /**
     * Update subscriber profile and create Transaction Entry per VRA ER.
     *
     * @param transDate Transaction date.
     * @param params1 The parsed ER fields value list.
     */

    public void createTransaction(final Context ctx, final Date transDate, final List params1)
        throws Exception
    {
        String msisdn       = "";
        long   rechargeValue = 0l;
        long   newBalance    = 0l;

        try
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_VRA_ER).log(ctx);

            msisdn       = ((String)params1.get(MCOMMERCE_MSISDN_INDEX)).trim();

            rechargeValue = Long.parseLong((String)params1.get(MCOMMERCE_RECHARGE_VALUE_INDEX));
            newBalance = Long.parseLong((String)params1.get(MCOMMERCE_FINAL_BALANCE_INDEX));

            Subscriber subscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn, transDate);

            if ( subscriber == null )
            {
                new MinorLogMsg(this,"Subscriber with MSISDN [" + msisdn +  "] could not be found. Failed to process MCommerce ER properly.",null).log(ctx);
                return;
            }

            /*
             * Get the adjustment type to use for MCommerce adjustments
             * from the General Configuration
             */
            GeneralConfig config = (GeneralConfig) ctx.get(GeneralConfig.class);
            
            int adjustmentTypeId = config.getMCommerceAdjustmentType();
            
             final Home adjustmentTypeHome = (Home) ctx.get(AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_READ_ONLY_HOME);

            AdjustmentType mCommerceAdjustmentType = null;
            /*
             * No adjustment type is specified so we'll use the default one
             */
            if (adjustmentTypeId == 0)
            {
                mCommerceAdjustmentType = (AdjustmentType)adjustmentTypeHome.find(ctx,
                                new AdjustmentTypeByName(MCOMMERCE_ADJUSTMENTTYPE_NAME));
            }
            else
            {
                mCommerceAdjustmentType = (AdjustmentType)adjustmentTypeHome.find(ctx, Integer.valueOf(adjustmentTypeId));
            }
            
            if ( mCommerceAdjustmentType == null )
            {
                new MinorLogMsg(this, "Cann't find correct AdjustmentType "+MCOMMERCE_ADJUSTMENTTYPE_NAME+" for MCommerce transaction. Failed to process MCommerce ER properly.",null).log(ctx);
                return;
            }

            // create Transaction Entry in Ecare DB. It should not check the limit.
            TransactionSupport.createTransaction(ctx,
                                                 subscriber,
                                                 rechargeValue,
                                                 newBalance,
                                                 mCommerceAdjustmentType,
                                                 true,
                                                 transDate,
                                                 "");
        }
        catch (Exception e)
        {
            throw e;
        }
    }
    
    private CRMProcessor processor_= null;
    
    private static final String MCOMMERCE_ADJUSTMENTTYPE_NAME = "AdjustmentType For MCommerce";
    
    private static final String PM_MODULE = MCommerceRechargeProcessor.class.getName();
}
