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
package com.trilogy.app.crm.socklet;

import com.trilogy.app.crm.audi.AUDILoadLogicProcess;
import com.trilogy.app.crm.bean.audi.AudiLoadSubscriberCSVSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.support.StringSeperator;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.app.crm.bean.audi.AudiLoadSubscriber;

/**
 * @author ali
 */
public class AddSubscriberCommand extends SubscriberProvisioningCommand {

	/* (non-Javadoc)
	 * @see com.redknee.app.crm.socklet.SubscriberProvisioningCommand#service(com.redknee.framework.xhome.context.Context, java.lang.String)
	 */
	public void start(Context ctx, String args) {
		String errMsg = "";
		Object bean = null;
		try
		{
			bean = parseArguments(args);
		}
		catch (Throwable t)
		{
			// An error occured during parsing of this command
			StringSeperator seperator = new StringSeperator(args, ',');
			String msisdn = getMsisdn(seperator);
			if ( msisdn.length() > 0 )
			{
				errMsg = msisdn + "-Failure Parsing Audi Command-" + t.getMessage() + "\n";
			}
			else
			{
				errMsg = "Missing Msisdn -Failure Parsing Audi Command- " + t.getMessage() + " \n\t for Command [A," + args + "]\n";
			}
		}
		
		AUDILoadLogicProcess process = (AUDILoadLogicProcess) ctx.get(AUDILoadLogicProcess.class);
		
		if (bean != null)
		{
			process.add(ctx, bean);
		}
		else
		{
			process.printMessageToLog(ctx, errMsg);
		}

	}
	
	/* (non-Javadoc)
	 * @see com.redknee.app.crm.socklet.SubscriberProvisioningCommand#service(com.redknee.framework.xhome.context.Context, java.lang.String)
	 */
	public Object parseArguments(String args) {
        StringSeperator seperator = new StringSeperator(args, ',');
        Object bean = AudiLoadSubscriberCSVSupport.instance().parse(seperator);
        
        return bean;
	}
	
	private String getMsisdn(StringSeperator seperator)
	{
		//Skip BAN, Spid, FirstName, LastName, SubscriberType
		for (int i = 0; i < 5; i++)
		{
			seperator.next();
		}
		return seperator.next();	// Msisdn
	}

}
