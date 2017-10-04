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
package com.trilogy.app.crm.ondemand;

import javax.servlet.http.HttpServletRequest;

import com.trilogy.framework.xhome.context.Context;


/**
 * Provides utility methods for on-demand support.
 *
 * @author gary.anderson@redknee.com
 */
public
class OnDemandSupport
{
    /**
     * Gets the global request object.  We don't want any 'domain' support.
     *
     * @param context The operating context.
     * @return The request object without any domain support.
     */
    public static
    HttpServletRequest getRequest(final Context context)
    {
        Context localContext = context;
        Context parentContext = (Context)localContext.get("..");
        
        while (parentContext != null && parentContext.get(HttpServletRequest.class) != null)
        {
            localContext = parentContext;
            parentContext = (Context)localContext.get("..");
        }
        
        return (HttpServletRequest)localContext.get(HttpServletRequest.class);
    }
    

    /**
     * Determines if one of the given properties is set as the calculation key.
     *
     * @param context The operating context.
     * @param properties The properties of interest.
     *
     * @return True if the calculation key is set to one of the given
     * properties; false otherwise.
     */
    public static
    boolean isCalculateKeySet(final Context context, final String[] properties)
    {
        return isCalculateKeySet(getRequest(context), properties);
    }

    
    /**
     * Determines if one of the given properties is set as the calculation key.
     *
     * @param request The request to search for the calculation key.
     * @param properties The properties of interest.
     *
     * @return True if the calculation key is set to one of the given
     * properties; false otherwise.
     */
    public static
    boolean isCalculateKeySet(final HttpServletRequest request, final String[] properties)
    {
        final String calculateProperty = request.getParameter(OnDemandWebControl.KEY);

        if (calculateProperty == null || "".equals(calculateProperty.trim()))
        {
            return false;
        }
        
        boolean found = false;
        for (int n = 0; n < properties.length && !found; ++n)
        {
            final String property = properties[n];
            found = property.equals(calculateProperty);
        }

        return found;
    }
    
    

} // class
