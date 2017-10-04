/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bulkloader;


import java.io.IOException;

import com.trilogy.app.crm.audi.AUDIDeleteLogicProcess;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.audi.AudiDeleteSubscriber;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * @author amedina
 *
 * Process any subscriber deactivation on any delta changes
 */
public class AUDIDeleteProcess extends AbstractAUDIProcess implements AUDIDeleteLogicProcess 
{
	public AUDIDeleteProcess(Context ctx)
	{
		try 
		{
			setSubscriberWriter(getSubscriberWriter(ctx));
		}
		catch (IOException e) 
		{
			LogSupport.major(ctx,this,"AUDIDeleteProcess is not installed properly. IO Exception encountered : Cannot set the log file", e);
		}
	}

	/* (non-Javadoc)
	 * @see com.redknee.app.crm.audi.AUDIDeleteLogicProcess#delete(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public void delete(Context ctx, Object csvObject)
	{
		final PMLogMsg createSubscriberPM = new PMLogMsg(PM_MODULE, "Delete Subscriber");
		Subscriber subscriber = null;
		
		if (csvObject instanceof AudiDeleteSubscriber)
		{
			AudiDeleteSubscriber update = (AudiDeleteSubscriber) csvObject;
			try 
			{
				subscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, update.getMSISDN());
				if (subscriber != null)
				{
					Home home = (Home) ctx.get(SubscriberHome.class);
					
					if (home != null)
					{
						subscriber.setState(SubscriberStateEnum.INACTIVE);
						home.store(ctx, subscriber);
					}
					else
					{
						printMessage(ctx, subscriber.getMSISDN(), "No Home in context");
					}
				}
				else
				{
					printMessage(ctx, update.getMSISDN(), "Subscriber doesn't exists");
				}
			}
			catch (Throwable t)
			{
				printMessage(ctx, update.getMSISDN(), t.getMessage());
				t.printStackTrace(getSubscriberWriter());
			}
			finally
			{
				createSubscriberPM.log(ctx);
				getSubscriberWriter().flush();
			}
			
		}
	}

	public static final String	PM_MODULE	= AUDIDeleteProcess.class.getName();
	
	/* 
	 * Used to access the Error Log from the socklet
	 * @param msg is a well formed string error message
	 */
	public void printMessageToLog(Context ctx, String msg)
	{
		printMessage(ctx, msg);
		getSubscriberWriter().flush();
	}

}
