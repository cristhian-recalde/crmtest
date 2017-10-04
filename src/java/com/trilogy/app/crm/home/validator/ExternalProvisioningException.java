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
package com.trilogy.app.crm.home.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;

/**
 * Exception used to record all the validation errors while provisioning to external applications.
 * @author Angie Li
 *
 */
public class ExternalProvisioningException extends CompoundIllegalStateException 
{
    public ExternalProvisioningException()
    {
        super();
        sourcesOfError_ = new ArrayList(); 
    }
    
    public void thrown(Throwable t, String source)
    {
        super.thrown(t);
        sourcesOfError_.add(source);
    }
    
    /** 
     * Throw this ExternalProvisioningException containing the collection of 
     * any errors thrown and their sources. 
     */
    public void throwAll()
        throws ExternalProvisioningException
    {
        throw this;
    }
    
    /**
     * Returns true if the given Class name is contained in the list of sources of error.
     * @param source
     * @return
     */
    public boolean isSourceOfError(String source)
    {
        return sourcesOfError_.contains(source);
    }
    
    /**
     * Returns a String with all the sources of error recorded in this Exception. 
     * If there are no exceptions, then an empty String is returned.
     * @return
     */
    public String printSourceOfError()
    {
        StringBuilder msg = new StringBuilder();
        if (sourcesOfError_.size() > 0 )
        {
            msg = new StringBuilder("The following are the sources of error: ");
            for (Iterator<String> i = sourcesOfError_.iterator(); i.hasNext(); )
            {
                msg.append(i.next());
                if (i.hasNext())
                {
                    msg.append(", ");
                }
            }
        }
        return msg.toString();
    }
    
    /**
     * Return a String of Exception Messages recorded in this Exception, separated by line breaks.
     * If there are no exceptions, then an empty String is returned.
     * @return
     */
    public String printExceptionsMessages()
    {
        StringBuilder msg = new StringBuilder();
        if (exceptions_.size() > 0)
        {
            msg = new StringBuilder("The following are the exceptions that occurred during validation: ");
            for (Iterator i = exceptions_.iterator(); i.hasNext(); )
            {
                msg.append("\n ");
                msg.append(((Throwable)i.next()).getMessage());
            }
        }
        return msg.toString();
    }
    
    private List<String> sourcesOfError_;
}
