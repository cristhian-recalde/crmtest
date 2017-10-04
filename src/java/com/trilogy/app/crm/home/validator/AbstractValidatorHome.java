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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * The methods delegate to their HomeValidator delegate classes.
 * 
 * Abstract class serves the same purpose for HomeValidators as AbstractHome does for the home Pipeline. 
 * @author Angie Li
 *
 */
public abstract class AbstractValidatorHome extends HomeProxy implements HomeValidator 
{
    
    public AbstractValidatorHome(Context context, HomeValidator delegate)
    {
        super(context, delegate);
    }
    
    /**
     * Calls the delegate HomeValidator's resolveCreateConflict(Context, Object, ExternalProvisioningException)
     */
    public void resolveCreateConflict(Context context, Object obj, ExternalProvisioningException el)
        throws HomeException, HomeInternalException 
    {
        getDelegate().resolveCreateConflict(context, obj, el);
    }

    /**
     * Calls the delegate HomeValidator's validateCreate(Context, Object, ExternalProvisioningException).
     * After completing an entire pass of this method through the HomeValidator pipeline, if 
     * there are errors logged, throw them.
     */
    public void validateCreate(Context context, Object obj, ExternalProvisioningException el) 
        throws IllegalStateException 
    {
        getDelegate().validateCreate(context, obj, el);

        //Throw all errors. Once thrown, it will abort the validateCreate pipeline call.
        if (el.getSize() > 0)
        {
            el.throwAll();
        }
    }
    
    /**
     * Calls the delegate HomeValidator's validateRemove(Context, Object, ExternalProvisioning Exception)
     * After completing an entire pass of this method through the HomeValidator pipeline, if 
     * there are errors logged, throw them. 
     */
    public void validateRemove(Context context, Object obj, ExternalProvisioningException el)
    {
        getDelegate().validateRemove(context, obj, el);

        //Throw all errors. Once thrown, it will abort the validateCreate pipeline call.
        if (el.getSize() > 0)
        {
            el.throwAll();
        }
    }
    
    /**
     * Calls the delegate HomeValidator's resolveRemoveConflict(Context, Object, ExternalProvisioningException)
     */
    public void resolveRemoveConflict(Context context, Object obj, ExternalProvisioningException el)
        throws HomeException, HomeInternalException 
    {
        getDelegate().resolveRemoveConflict(context, obj, el);
    }
    
    /**
     * Calls the delegate HomeValidator's validateStore(Context, Object, ExternalProvisioningException)
     * After completing an entire pass of this method through the HomeValidator pipeline, if 
     * there are errors logged, throw them.
     */
    public void validateStore(Context context, Object obj, ExternalProvisioningException el)
    {
        getDelegate().validateStore(context, obj, el);
        
        //Throw all errors. Once thrown, it will abort the validateCreate pipeline call.
        if (el.getSize() > 0)
        {
            el.throwAll();
        }
    }
    
    /**
     * Calls the delegate HomeValidator's resolveStoreConflict(Context, Object, ExternalProvisioningException)
     */
    public void resolveStoreConflict(Context context, Object obj, ExternalProvisioningException el)
        throws HomeException, HomeInternalException
    {
        getDelegate().resolveStoreConflict(context, obj, el);
    }
    
    /**
     * Returns the delegate Home Validator 
     */
    public HomeValidator getDelegate()
    {
        return (HomeValidator) super.getDelegate();
    }
}
