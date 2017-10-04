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
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * ValidatorHome that logs all errors found. A Debugging tool.
 * @author Angie Li
 */
public class LoggingValidatorHome extends AbstractValidatorHome 
{

    public LoggingValidatorHome(Context context, HomeValidator delegate)
    {
        super(context, delegate);
    }

    /**
     * Log all sources of error of resolveCreateConflict.
     */
    public void resolveCreateConflict(Context ctx, Object obj,
            ExternalProvisioningException el) 
        throws HomeException, HomeInternalException 
    {
        LogSupport.info(ctx, this, 
                "Called resolveCreateConflict. " + el.printSourceOfError());
        super.resolveCreateConflict(ctx, obj, el);
    }

    /**
     * Log all exception messages validateCreate.
     */
    public void validateCreate(Context ctx, Object obj,
            ExternalProvisioningException el) throws IllegalStateException 
    {
        LogSupport.info(ctx, this, 
                "Called validateCreate. " + el.printExceptionsMessages());
        super.validateCreate(ctx, obj, el);
    }
    
    /**
     * Log all sources of error of resolveRemoveConflict. 
     */
    public void resolveRemoveConflict(Context ctx, Object obj,
            ExternalProvisioningException el) 
        throws HomeException, HomeInternalException 
    {
        LogSupport.info(ctx, this, 
                "Called resolveRemoveConflict. " + el.printSourceOfError());
        super.resolveRemoveConflict(ctx, obj, el);
    }

    /**
     * Log all exception messages validateRemove.
     */
    public void validateRemove(Context ctx, Object obj,
            ExternalProvisioningException el) throws IllegalStateException 
    {
        LogSupport.info(ctx, this, 
                "Called validateRemove. " + el.printExceptionsMessages());
        super.validateRemove(ctx, obj, el);
    }

    /**
     * Log all sources of error of resolveStoreConflict
     */
    public void resolveStoreConflict(Context ctx, Object obj,
            ExternalProvisioningException el) 
        throws HomeException, HomeInternalException 
    {
        LogSupport.info(ctx, this, 
                "Called resolveStoreConflict. " + el.printSourceOfError());
        super.resolveStoreConflict(ctx, obj, el);
    }

    /**
     * Log all exception messages of validateStore.
     */
    public void validateStore(Context ctx, Object obj,
            ExternalProvisioningException el) throws IllegalStateException 
    {
        LogSupport.info(ctx, this, 
                "Called validateStore. " + el.printExceptionsMessages());
        super.validateStore(ctx, obj, el);
    }
}
