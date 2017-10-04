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
package com.trilogy.app.crm.api.rmi;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.language.MessageMgrAware;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.util.crmapi.wsdl.v2_0.exception.CRMException;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.ValidationExceptionEntry;

/**
 * Used to collect Exceptions from a CompoundIllegalStateException.
 * Extracts Exceptions from CompoundIllegalStateException and separates them into
 * Validation Errors and All Other Errors.
 * Errors can be retrieved as two separate lists:
 *  +getValidationExceptionEntries()
 *  +getGeneralExceptionEntries()
 *
 * DEBUG:
 * Install the CRMApiDebugConfig key into the Context to turn on Debugging (Stack 
 * Traces will be printed in to the CRMException.Message or ValidationException.Explanation.
 * 
 * 
 * @author victor.stratan@redknee.com
 */
public class ExceptionListenerBridge extends ContextAwareSupport implements ExceptionListener
{
	// Validation Exception Entries
    private List<ValidationExceptionEntry> validationEntries_;
    // General Exception Entries
    private List<CRMException> compoundEntries_;
    private int validationCursor_ = 0;
    private int compoundCursor_ = 0;
    private boolean logStack_ = false;

    public ExceptionListenerBridge(final Context ctx, final int count)
    {
        setContext(ctx);
        this.validationEntries_ = new ArrayList<ValidationExceptionEntry>();
        this.compoundEntries_ = new ArrayList<CRMException>();
        // See class description on how to use this.
        this.logStack_ = (ctx.get(CRMApiDebugConfig.class) != null) ? true : false;
    }

    public void thrown(final Throwable t)
    {
        if (t != null)
        {
            new MinorLogMsg(this, t.getMessage(), t).log(getContext());
        }
        
        /* IllegalPropertyArgumentException maps to ValidationExceptionEntry
         * while 
         * IllegalStateException maps to CRMException
         */
        if (t instanceof IllegalPropertyArgumentException)
        {
            IllegalPropertyArgumentException e = (IllegalPropertyArgumentException)t;
            ValidationExceptionEntry validation= new ValidationExceptionEntry();
            String name = e.getPropertyName();
            if (e.getPropertyInfo() != null)
            {
            	name = e.getPropertyInfo().toString();
            }
            validation.setName(name);

            String msg = null;
            MessageMgrAware mmgrAware = (MessageMgrAware)XBeans.getInstanceOf(getContext(), e, MessageMgrAware.class);
            if (mmgrAware != null)
            {
                msg = mmgrAware.toString(getContext(), new MessageMgr(getContext(), this));
            }
            else
            {
                msg = e.getMessageText();
            }
            if (logStack_)
            {
                msg += "\n" + getStackTraceString(e);
            }
            validation.setExplanation(msg);
            validationEntries_.add(validation);
            validationCursor_++;
        }
        else
        {
            CRMException crmException = new CRMException();
            String msg = t.getMessage();
            if (logStack_)
            {
                msg += "\n" + getStackTraceString(t);
            }
            crmException.setMessage(msg);
            compoundEntries_.add(crmException);
            compoundCursor_++;
        }
    }

    /**
     * Returns the Validation Exception Entries
     * @return
     */
    public ValidationExceptionEntry[] getValidationExceptionEntries()
    {
    	/* The list returned must not contain any null entries,
    	 * unless it is to indicate an empty list.
    	 * Check the last entry if the list is larger than one. */
        ValidationExceptionEntry[] entries = new ValidationExceptionEntry[validationEntries_.size()];
        return validationEntries_.toArray(entries);
    }
    
    /**
     * Returns the General Exceptions Entries 
     * @return
     */
    public CRMException[] getGeneralExceptionEntries()
    {

        CRMException[] newEntryList = new CRMException[compoundEntries_.size()];
        return compoundEntries_.toArray(newEntryList);
    }

    public static String getStackTraceString(final Throwable t)
    {
        final StringWriter stringWriter = new StringWriter();
        t.printStackTrace(new PrintWriter(stringWriter));

        return stringWriter.toString();
    }
}
