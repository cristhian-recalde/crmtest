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
package com.trilogy.app.crm.discount;

import com.trilogy.app.crm.CoreCrmLicenseConstants;
import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Process entities from DiscountsEvent table for all entries which are in Calculation_Pending state.
 * 
 * @author harsh.murumkar
 * @since 10.5
 */
public class DiscountingClassAssignmentLifecycleAgent extends LifecycleAgentScheduledTask
{
    private static final long serialVersionUID = 1L;
    

    /**
     * Creates a DiscountingClassAssignmentLifecycleAgent object.
     * 
     * @param ctx
     * @param agentId
     * @throws AgentException
     */
    public DiscountingClassAssignmentLifecycleAgent(Context ctx, String agentId) throws AgentException
    {
        super(ctx, agentId);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isEnabled(Context ctx)
    {
        return (LicensingSupportHelper.get(ctx).isLicensed(ctx, CoreCrmLicenseConstants.POSTPAID_LICENSE_KEY)
                || LicensingSupportHelper.get(ctx).isLicensed(ctx, CoreCrmLicenseConstants.HYBRID_LICENSE_KEY)) && 
                DunningReport.isDunningReportSupportEnabled(ctx);
    }

    /**
     * {@inheritDoc}
     */
    protected void start(Context ctx) throws LifecycleException
    {
        try
        {
        	Context subContext = ctx.createSubContext();
        	String banParameter = getParameter2(ctx, String.class);
        	subContext.put(AccountConstants.BAN_FOR_DISCOUNT_EVENT_UPDATE, banParameter);
        	
        	DiscountClassAssignmentVisitor visitor = new DiscountClassAssignmentVisitor(this);
            LogSupport.debug(subContext,"In Scheduler of Discount Class Assignement", "Starting Discount Class Assignemnt Process");
            visitor.visit(subContext,null);
            
        }
         catch (AbortVisitException e) {
            // TODO Auto-generated catch block
             final String message = e.getMessage();
             LogSupport.minor(ctx, getClass().getName(), message,e);
        } catch (AgentException e) {
            final String message = e.getMessage();
            LogSupport.minor(ctx, getClass().getName(), message,e);
        }
        
    }
}
