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


import java.io.InputStream;
import java.io.PrintStream;

import com.trilogy.framework.xhome.context.Context;

/**
 * @author ali
 */
public abstract class SubscriberProvisioningCommand {

	private PrintStream out;
    private PrintStream err;
    private InputStream in;

    public SubscriberProvisioningCommand()
    {
        out = System.out;
        in = System.in;
        err = System.err;
    }

    public SubscriberProvisioningCommand(PrintStream sysOut, PrintStream sysErr, InputStream sysIn)
    {
        setOut(sysOut);
        setErr(sysErr);
        setIn(sysIn);
    }
    
    //  data and accessor methods

    protected PrintStream getOut()
    {
        return out;
    }

    protected PrintStream getErr()
    {
        return err;
    }

    protected InputStream getIn()
    {
        return in;
    }
	
	public abstract void start(Context ctx, String args);	
	
	private Object parseArguments(String args){
        return null;
	}
	
    /**
     * @param stream
     */
    public void setErr(PrintStream stream)
    {
        err = stream;
    }

    /**
     * @param stream
     */
    public void setIn(InputStream stream)
    {
        in = stream;
    }

    /**
     * @param stream
     */
    public void setOut(PrintStream stream)
    {
        out = stream;
    }
}
