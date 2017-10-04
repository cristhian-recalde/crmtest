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
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;

/**
 *  Interface to pre-validate and resolve possible Home errors.
 *  @author Angie Li
 */
public interface HomeValidator extends Home
{
    /**
     * Validation Performed prior to the "create" action of the Home interface. 
     * @param ctx
     * @param obj
     * @throws HomeException
     * @throws HomeInternalException
     */
    public void validateCreate(Context ctx, Object obj, ExternalProvisioningException el)
        throws IllegalStateException;
    
    /**
     * Validation Performed prior to the "store" action of the Home interface.
     * @param ctx
     * @param obj
     * @param el
     * @throws IllegalStateException
     */
    public void validateStore(Context ctx, Object obj, ExternalProvisioningException el)
        throws IllegalStateException;
    
    /**
     * Validation Performed prior to the "remove" action of the Home interface.
     * @param ctx
     * @param obj
     * @param el
     * @throws IllegalStateException
     */
    public void validateRemove(Context ctx, Object obj, ExternalProvisioningException el)
        throws IllegalStateException;
        
    /**
     * Resolve the Exception thrown by the validateCreate method
     * @param ctx
     * @param obj
     * @throws HomeException
     * @throws HomeInternalException
     */
    public void resolveCreateConflict(Context ctx, Object obj, ExternalProvisioningException el)
        throws HomeException, HomeInternalException;

    /**
     * Resolve the Exception thrown by the validateStore method
     * @param ctx
     * @param obj
     * @param e
     * @throws HomeException
     * @throws HomeInternalException
     */
    public void resolveStoreConflict(Context ctx, Object obj, ExternalProvisioningException el)
        throws HomeException, HomeInternalException;
    
    /**
     * Resolve the Exception thrown by the validateRemove method
     * @param ctx
     * @param obj
     * @param e
     * @throws HomeException
     * @throws HomeInternalException
     */
    public void resolveRemoveConflict(Context ctx, Object obj, ExternalProvisioningException el)
        throws HomeException, HomeInternalException;
    
}
