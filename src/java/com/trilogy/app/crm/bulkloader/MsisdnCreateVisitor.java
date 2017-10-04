/*
 * Created on Apr 15, 2005
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bulkloader;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.HomeVisitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

public class MsisdnCreateVisitor extends HomeVisitor
{
	protected PrintWriter logWriter;
	
	protected Msisdn template;
	
	protected int processed=0;
	
	protected int success=0;
	
	public MsisdnCreateVisitor(Home home, Msisdn template, PrintWriter logPrintWr)
	{
		super(home);
	}

	/**
	 * @see com.redknee.framework.xhome.visitor.VisitorProxy#visit(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
	{
		BulkLoadMsisdn bm=(BulkLoadMsisdn) obj;
		
		Msisdn m=null;
		if(getTemplate()!=null)
		{
			try
			{
				m=(Msisdn) getTemplate().clone();
			}
			catch (CloneNotSupportedException e)
			{
				if(LogSupport.isDebugEnabled(ctx))
				{
					new DebugLogMsg(this,e.getMessage(),e).log(ctx);
				}
				
				m=new Msisdn();
			}
		}
		else
		{
			if(LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(this,"Template is null. Cannot clone. Creating new Msisdn.",null).log(ctx);
			}
			
			m=new Msisdn();
		}
		
		m.setSpid(bm.getSpid());
		m.setMsisdn(bm.getMsisdn());
		m.setSubscriberType(bm.getSubscriberType());
		m.setGroup(bm.getGroup());
		m.setState(bm.getState());
		
		try
		{
			processed++;
			
			getHome().create(ctx,m);
			
			success++;
		}
		catch (HomeException e)
		{
			if(LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(this,e.getMessage(),e).log(ctx);
			}
		}
	}

	/**
	 * @return Returns the logWriter.
	 */
	public PrintWriter getLogWriter()
	{
		return logWriter;
	}


	/**
	 * @param logWriter The logWriter to set.
	 */
	public void setLogWriter(PrintWriter logWriter)
	{
		this.logWriter = logWriter;
	}


	/**
	 * @return Returns the processed.
	 */
	public int getProcessed()
	{
		return processed;
	}


	/**
	 * @param processed The processed to set.
	 */
	public void setProcessed(int processed)
	{
		this.processed = processed;
	}


	/**
	 * @return Returns the success.
	 */
	public int getSuccess()
	{
		return success;
	}


	/**
	 * @param success The success to set.
	 */
	public void setSuccess(int success)
	{
		this.success = success;
	}


	/**
	 * @return Returns the template.
	 */
	public Msisdn getTemplate()
	{
		return template;
	}


	/**
	 * @param template The template to set.
	 */
	public void setTemplate(Msisdn template)
	{
		this.template = template;
	}
	
	
}
