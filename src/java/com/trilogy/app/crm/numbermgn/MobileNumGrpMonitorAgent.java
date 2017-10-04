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
package com.trilogy.app.crm.numbermgn;


import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.MsisdnGroupHome;
import com.trilogy.app.crm.numbermgn.visitor.NumberManagmentMonitorVisitor;

/**
 * This cron agent monitors the mobile number groups, by checking the number of available Msisdns
 * with the minimum number of msisdns allowed for that group
 *
 * @author manda.subramanyam@redknee.com
 *
 */
public class MobileNumGrpMonitorAgent implements ContextAgent 
{

	/**
     * Default Constructor
     */
    public MobileNumGrpMonitorAgent() {}

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    public void execute(Context ctx) throws AgentException
    {
    	if (LogSupport.isDebugEnabled(ctx))
    	{
    		new DebugLogMsg(MobileNumGrpMonitorAgent.class,"Started the execution " +
    				"of Mobile Number Monitor Agent", null).log(ctx);
    	}
    	
        final LicenseMgr manager = (LicenseMgr)ctx.get(LicenseMgr.class);

        if (!manager.isLicensed(ctx, Common.MOBILE_NUMBER_MONITOR))
		{
        	LogSupport.minor(ctx, this, "License " + Common.MOBILE_NUMBER_MONITOR + " doesn't exists or is not enabled");
			return;
	    }

    	Home grpHome = (Home) ctx.get(MsisdnGroupHome.class);

    	String errorMsg = "Failed to get the Msisdn Group Home Collection due to error = ";

    	try
    	{
    		grpHome.forEach(ctx, NumberManagmentMonitorVisitor.instance());
    	}
    	catch (UnsupportedOperationException e)
    	{
    		new MinorLogMsg(this, errorMsg + e.getMessage(), e).log(ctx);
    		return;
    	}
    	catch (HomeException e)
    	{
    		new MinorLogMsg(this, errorMsg + e.getMessage(), e).log(ctx);
    		return;
    	}

    	if (LogSupport.isDebugEnabled(ctx))
    	{
    		new DebugLogMsg(MobileNumGrpMonitorAgent.class,"Finished executing " +
    				"of Mobile Number Monitor Agent", null).log(ctx);
    	}
    }
}
