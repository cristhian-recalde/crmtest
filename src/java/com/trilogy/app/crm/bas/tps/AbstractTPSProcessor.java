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
package com.trilogy.app.crm.bas.tps;

import java.io.File;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import com.trilogy.app.crm.dunning.DunningProcess;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * @author lxia
 */
public abstract class AbstractTPSProcessor implements ContextAware, TPSProcessor, TPSPipeConstant {

    public Context getContext()
    {
        return ctx_;
    }

    public void setContext(Context context)
    {
        ctx_ = context;
    }
    

	public synchronized String getCountString(long count)
	{
	   return COUNT_FORMAT.format(count);  
	}
	
	protected  final String COUNT_FORMAT_PATTERN = "0000";
	protected  final DecimalFormat COUNT_FORMAT = new DecimalFormat(COUNT_FORMAT_PATTERN);
	protected  File tpsFile_;
	protected  PrintStream out = null; 
	protected  String errFilename; 

	Context ctx_;
}
