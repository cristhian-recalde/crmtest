/*
 * Created on Jun 21, 2005
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

import java.io.*;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.csv.CSVIllegalArgumentException;

public class CSVErrorFileExceptionListener implements ExceptionListener
{
	protected PrintWriter errWriter;
	protected PrintWriter logWriter;

	protected int numberOfErrors=0;
	
	public CSVErrorFileExceptionListener(PrintWriter errWriter,PrintWriter logWriter)
	{
		setErrWriter(errWriter);
		setLogWriter(logWriter);
	}
	
	public void thrown(Throwable t)
	{
		if(getErrWriter()!=null)
		{
			if(t instanceof CSVIllegalArgumentException)
			{
				getErrWriter().println(((CSVIllegalArgumentException)t).getLine());
			}
			else
			{
				t.printStackTrace(getErrWriter());
			}
		}
		
		if(getLogWriter()!=null)
		{
			t.printStackTrace(getLogWriter());
		}
		
		numberOfErrors++;
	}

	public void setErrWriter(PrintWriter writer)
	{
		this.errWriter=writer;
	}
	
	public PrintWriter getErrWriter()
	{
		return errWriter;
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
	 * @return Returns the numberOfErrors.
	 */
	public int getNumberOfErrors()
	{
		return numberOfErrors;
	}

	/**
	 * @param numberOfErrors The numberOfErrors to set.
	 */
	public void setNumberOfErrors(int numberOfErrors)
	{
		this.numberOfErrors = numberOfErrors;
	}
	
	
}
