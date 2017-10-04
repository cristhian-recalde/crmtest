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
package com.trilogy.app.crm.validator;

import javax.mail.internet.InternetAddress;

import com.trilogy.app.crm.exception.RethrowExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;


/**
 * A generic secondary email addressess validator
 *
 * @author bdhavalshankh
 * @since 9.4.1
 */
public class SecondaryEmailAddressesValidator implements Validator
{
    public SecondaryEmailAddressesValidator(PropertyInfo primaryEmailProperty, PropertyInfo secondaryEmailProperty)
    {
    	secondaryProperty_ = secondaryEmailProperty;
    	primaryProperty_ = primaryEmailProperty;
    	
        if (!String.class.isAssignableFrom(secondaryProperty_.getType()))
        {
            throw new IllegalArgumentException("Secondary Email Addresses property must be of type '" + String.class.getName() + "'.");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        RethrowExceptionListener el = new RethrowExceptionListener();
        InternetAddress emailAddress =null;
        try
        {
        	String primaryAddress = (String) primaryProperty_.get(obj);
            String secondaryAddresses = (String) secondaryProperty_.get(obj);
           
            if (primaryAddress != null && primaryAddress.length() > 0 )
            {
            	if (secondaryAddresses != null && secondaryAddresses.length() > 0)
                {
            		InternetAddress.parse(secondaryAddresses);
            		InternetAddress[] secondaryEmailAddresses = InternetAddress.parse(secondaryAddresses);
                
            		for (InternetAddress internetAddress : secondaryEmailAddresses) {
                		emailAddress = internetAddress;
                		internetAddress.validate();
    				}
                }
            }
            else
            {
            	if (secondaryAddresses != null && secondaryAddresses.length() > 0)
            	{
            		el.thrown(new IllegalPropertyArgumentException(secondaryProperty_, "Cannot have Secondary Email Addresses without Primary Email Address. "));
            	}
            }
        }
        catch (javax.mail.internet.AddressException e)
        {
            el.thrown(new IllegalPropertyArgumentException(secondaryProperty_, "Invalid Secondary E-Mail Address: "+emailAddress));
        }
        
        el.throwAllAsCompoundException();
    }

    protected PropertyInfo primaryProperty_ = null;
    protected PropertyInfo secondaryProperty_ = null;
}
