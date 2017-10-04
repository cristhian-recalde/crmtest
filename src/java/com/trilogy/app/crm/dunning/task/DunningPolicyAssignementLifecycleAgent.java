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

package com.trilogy.app.crm.dunning.task;


import java.util.Date;

import com.trilogy.app.crm.CoreCrmLicenseConstants;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.visitor.accountprocessing.AccountDunningPolicyAssignmentVisitor;
import com.trilogy.app.crm.invoice.InvoiceCalculationSupport;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Processes accounts for sunning policy assignment.
 *
 * @author Sapan Modi
 * 
 */

public class DunningPolicyAssignementLifecycleAgent extends LifecycleAgentScheduledTask {

	
	private static final long serialVersionUID = 1L;
	

    /**
     * Creates a DunningPolicyAssignementLifecycleAgent object.
     * 
     * @param ctx
     * @param agentId
     * @throws AgentException
     */
    public DunningPolicyAssignementLifecycleAgent(Context ctx, String agentId) throws AgentException
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
        	ctx.put(InvoiceCalculationSupport.DUNNING_TASK, true);
        	AccountDunningPolicyAssignmentVisitor visitor = new AccountDunningPolicyAssignmentVisitor(this);
        	LogSupport.debug(ctx,"In Scheduler of Dunning Policy Assignement", "Starting Dunning Policy Assignemnt Process");
        	visitor.visit(ctx,null);
            

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
    
    /**
     * The date format used for specifying the "current date" in parameter 1. This format
     * is currently consistent with other CronAgents.
     */
    private static final String DATE_FORMAT_STRING = "yyyyMMdd";
}
