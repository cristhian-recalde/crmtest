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
package com.trilogy.app.crm.bundle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.URCSGenericParameterHolder;
import com.trilogy.app.crm.bundle.exception.BundleManagerException;
import com.trilogy.app.crm.bundle.service.CRMSubscriberBucketProfile;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.pipe.PipelineAgent;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.product.bundle.manager.provision.common.param.Parameter;


/**
 * @author psperneac
 * Makes the call to BM bucket increase/decrease
 */
public class BundleCallAgent extends PipelineAgent
{
	
    /**
     * @param delegate
     */
    public BundleCallAgent(ContextAgent delegate)
    {
        super(delegate);
    }

    /**
     * @param ctx A context
     * @throws AgentException thrown if one of the services fails to initialize
     */
    public void execute(Context ctx) throws AgentException
    {
        BundleAdjustment form = (BundleAdjustment) ctx.get(BundleAdjustment.class);
        Subscriber sub = (Subscriber) ctx.get(Subscriber.class);

        CRMSubscriberBucketProfile bucketService = (CRMSubscriberBucketProfile) ctx.get(CRMSubscriberBucketProfile.class);

        ExceptionListener exceptions = (ExceptionListener) ctx.get(ExceptionListener.class);

        if (sub != null && form != null)
        {
            String msisdn = sub.getMSISDN();
            int spid = sub.getSpid();
            int subscriptionType = (int) sub.getSubscriptionType();

            for (Iterator i = form.getItems().iterator(); i.hasNext();)
            {
                BundleAdjustmentItem item = (BundleAdjustmentItem) i.next();

                int result = -1;
                URCSGenericParameterHolder paramHolder = (URCSGenericParameterHolder) ctx.get(URCSGenericParameterHolder.class);
                Parameter[] inParamSet = getInputParameterSetFromContextSafe(ctx,paramHolder);
                Map<Short, Parameter> outputParameterMap = getOutputParameterMapFromContextSafe(ctx,paramHolder);
                
                try
                {
                    if (item.getType() == BundleAdjustmentTypeEnum.DECREMENT)
                    {
                        result = bucketService.decreaseBalanceLimit(ctx, msisdn, spid, subscriptionType, item.getBundleProfile(), item.getAmount(), inParamSet, outputParameterMap);
                    }
                    else if (item.getType() == BundleAdjustmentTypeEnum.INCREMENT)
                    {
                        result = bucketService.increaseBalanceLimit(ctx, msisdn, spid, subscriptionType, item.getBundleProfile(), item.getAmount(), inParamSet, outputParameterMap);
                    }
                    else
                    {
                        AgentException ex = new AgentException("Wrong adjustment type" + item.getType());
                        exceptions.thrown(ex);
                        throw ex;
                    }
                }
                catch (BundleManagerException e)
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, e.getMessage(), e).log(ctx);
                    }
                    ctx.put(BundleManagerPipelineConstants.BM_RESULT_CODE, Integer.valueOf(e.getErrorCode()));
                    AgentException ex = new AgentException(e.getMessage(), e);
                    exceptions.thrown(ex);
                    throw ex;
                }
                catch (AgentException e)
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, e.getMessage(), e).log(ctx);
                    }
                    AgentException ex = new AgentException("Unable to adjust the bucket amount for " + msisdn + " : " + e.getMessage(), e);
                    exceptions.thrown(ex);
                    throw ex;
                }

                // TODO: replace with success result code
                if (result != 0)
                {
                    AgentException ex = new AgentException("Unable to adjust the bucket amount for " + msisdn + " : " + result);
                    exceptions.thrown(ex);
                    throw ex;
                }
            }

            ctx.put(BundleManagerPipelineConstants.BM_RESULT_CODE, BundleManagerPipelineConstants.SUCCESS_RC);
            this.pass(ctx, this, "BundleManager Call successful");
        }
        else
        {
            AgentException ex = new AgentException("System error: No parameters or no subscriber set in the context");
            exceptions.thrown(ex);
            throw ex;
        }
    }

	private Map<Short, Parameter> getOutputParameterMapFromContextSafe(
			Context ctx, URCSGenericParameterHolder paramHolder) 
	{
		if(paramHolder == null)
		{
			return new HashMap<Short, Parameter>();
		}
		else
		{
			return paramHolder.getOutputParameterMap();
		}
	}

	private Parameter[] getInputParameterSetFromContextSafe(Context ctx, URCSGenericParameterHolder paramHolder) {
		
		Parameter[] inputParam = null;
		if(paramHolder != null )
		{
			inputParam = paramHolder.getInputParameterArray();
		}
		else
		{
			inputParam = new Parameter[0];
		}
    
		return inputParam;
	}
}
