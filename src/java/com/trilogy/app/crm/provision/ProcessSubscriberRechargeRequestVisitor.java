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
package com.trilogy.app.crm.provision;

import java.util.List;

import com.trilogy.app.crm.bas.recharge.PostpaidRetryRecurRechargeVisitor;
import com.trilogy.app.crm.bean.RechargeRequestStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberRechargeRequest;
import com.trilogy.app.crm.bean.SubscriberRechargeRequestXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 *  
 *
 * @author bdhavalshankh
 * @since 9.9
 */
public class ProcessSubscriberRechargeRequestVisitor extends PostpaidRetryRecurRechargeVisitor implements Visitor,
		ContextAgent{

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public ProcessSubscriberRechargeRequestVisitor() {
	}

	/**
	 * {@inheritDoc}
	 */
	public void execute(Context ctx) throws AgentException 
	{
		Subscriber subscriber = (Subscriber) ctx.get(Subscriber.class);
		visit(ctx, subscriber);
	}

	@Override
	public void visit(Context ctx, Object object) throws AgentException, AbortVisitException
	{
	    Subscriber subscriber = (Subscriber) object;
	    if(LogSupport.isDebugEnabled(ctx))
	    {
	        LogSupport.debug(ctx, this, "Recharge Processing STARTED for subscriber ID: "+subscriber.getId());
	    }
	    try
	    {
	        And and = new And();
	        and.add(new EQ(SubscriberRechargeRequestXInfo.SUBSCRIBERID, subscriber.getId()));
	        and.add(new EQ(SubscriberRechargeRequestXInfo.STATE, RechargeRequestStateEnum.INITIATED));
	        List<SubscriberRechargeRequest> requests = (List<SubscriberRechargeRequest>) HomeSupportHelper.get(ctx).
	                getBeans(ctx, SubscriberRechargeRequest.class, and);
	       
	        if(requests != null
	                && requests.size() > 0)
	        {
	            for (SubscriberRechargeRequest req : requests)
                {
                    req.setState(RechargeRequestStateEnum.PROCESSING);
                    req = HomeSupportHelper.get(ctx).storeBean(ctx, req);
                }
	           
	           super.visit(ctx, subscriber);
	          
	           if(LogSupport.isDebugEnabled(ctx))
	           {
	               LogSupport.debug(ctx, this, "Recharged successfuly for subscriber id : "+subscriber.getId()+"" +
	           		" Going to mark request state to COMPLETED");
	           }
	           
	           for (SubscriberRechargeRequest req : requests)
               {
                   req.setState(RechargeRequestStateEnum.COMPLETED);
                   HomeSupportHelper.get(ctx).storeBean(ctx, req);
               }
	        }
	    }
	    catch (AgentException e) 
	    {
           LogSupport.minor(ctx, this, "AgentException occurred while recharging for subscriber with id : "+subscriber.getId(), e);
        }
	    catch (Exception e) 
	    {
	        LogSupport.minor(ctx, this, "Exception occurred while recharging for subscriber with id : "+subscriber.getId(), e);
        }
	}
}
