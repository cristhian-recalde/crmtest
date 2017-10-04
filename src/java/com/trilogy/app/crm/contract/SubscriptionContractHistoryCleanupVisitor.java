/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.contract;

import java.util.Date;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.api.Constants;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * @author amoll
 */
public class SubscriptionContractHistoryCleanupVisitor implements Visitor
{

    private static final long serialVersionUID = 1L;


    /**
     * @param ctx
     * @param obj
     * @throws AgentException
     * @throws AbortVisitException
     * @see com.redknee.framework.xhome.visitor.Visitor#visit(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
    {
    	SubscriptionContractHistory contract = (SubscriptionContractHistory) obj;
    	Home home = (Home)ctx.get(SubscriptionContractHistoryHome.class);
        try
        {
        	if(contract.getContractStatus() != null && contract.getContractStatus().equals(Constants.CONTRACT_INACTIVE))
        		home.remove(contract);
        	else if(contract.getContractStatus() != null && contract.getContractStatus().equals(Constants.CONTRACT_ACTIVE)){
        		if(!SubscriptionContractSupport.isDummyContract(ctx, contract.getContractId())){
	        		contract = (SubscriptionContractHistory)contract.clone();
	        		contract.setContractStatus(Constants.CONTRACT_EXPIRED);
	        		contract.setRecordModifyDate(new Date(System.currentTimeMillis()));
	        		home.store(ctx, contract);
        		}
        	}
        }
        catch (Exception exception)
        {
            LogSupport.minor(ctx, this, "Exception caught during subscription contract history removal",
                    exception);
            throw new AgentException(exception);
        }
    }
}
