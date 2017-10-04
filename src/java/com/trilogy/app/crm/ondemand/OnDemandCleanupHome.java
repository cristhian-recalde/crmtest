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

import java.util.Calendar;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * Used to wrap the simple cache used for on-demand values.  This cache
 * cleans-up expired entries, allows the find() method to take a
 * bean as a parameter, and allows the create() method to recreate a value
 * without having to first remove the value.
 *
 * @author gary.anderson@redknee.com
 */
public
class OnDemandCleanupHome
    extends HomeProxy
{
    /**
     * Creates a new OnDemandCleanupHome for thie given simple cache.
     *
     * @param delegate The simple cache to which we delegate.
     */
    public
    OnDemandCleanupHome(final Home delegate)
    {
        super(delegate);
    }
    
    
    // INHERIT
    public
    Object find(Context ctx,final Object key)
        throws HomeException
    {
        OnDemandValue result = null;
        
        if (key instanceof OnDemandValue)
        {
            final OnDemandValue value = (OnDemandValue)key;
            result = (OnDemandValue)find(ctx,value.getKey());
        }
        else
        {
            result = (OnDemandValue)super.find(ctx,key);
        }

        if (result != null && result.isValid())
        {
            final Calendar expiration = Calendar.getInstance();
            expiration.setTime(result.getCalculationDate());
            expiration.add(Calendar.MILLISECOND, result.getTimeout());
            if (expiration.getTime().before(new Date()))
            {
                try
                {
                    remove(ctx,result);
                }
                catch (final Throwable t)
                {
                    // EMPTY
                }

                result = null;
            }
        }

        return result;
    }


    // INHERIT
    public
    Object create(Context ctx,final Object obj)
        throws HomeException, HomeInternalException
    {
        // We want to allow recreation.
        try
        {
            remove(ctx,obj);
        }
        catch (final Throwable throwable)
        {
            // Empty
        }

        return super.create(ctx,obj);
    }
    
    
} // class
