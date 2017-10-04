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
import com.trilogy.framework.xhome.home.NullHome;

/**
 * This class doesn't perform any actions.
 * For the HomeValidator interface, this class is the equivalent to NullHome for the Home interface.
 * @author Angie Li
 *
 */
public class NullValidatorHome extends NullHome implements HomeValidator 
{
    public static HomeValidator instance()
    {
        return instance_;
    }

    public NullValidatorHome()
    {
        super(null);
    }

    public NullValidatorHome(Context ctx)
    {
        super(ctx);
    }

    public void resolveCreateConflict(Context ctx, Object obj,
            ExternalProvisioningException e) throws HomeException,
            HomeInternalException 
    {
        // Do nothing
    }

    public void validateCreate(Context ctx, Object obj,
            ExternalProvisioningException el) throws IllegalStateException 
    {
        // Do nothing
    }

    public void resolveRemoveConflict(Context ctx, Object obj,
            ExternalProvisioningException e) throws HomeException,
            HomeInternalException 
    {
        // Do nothing
    }

    public void validateRemove(Context ctx, Object obj,
            ExternalProvisioningException el) throws IllegalStateException 
    {
        // Do nothing
    }
    

    public void resolveStoreConflict(Context ctx, Object obj,
            ExternalProvisioningException el) throws HomeException,
            HomeInternalException 
    {
        // Do nothing
    }

    public void validateStore(Context ctx, Object obj,
            ExternalProvisioningException el) throws IllegalStateException 
    {
        // Do nothing
    }
    
    protected static final HomeValidator instance_ = new NullValidatorHome();

}
