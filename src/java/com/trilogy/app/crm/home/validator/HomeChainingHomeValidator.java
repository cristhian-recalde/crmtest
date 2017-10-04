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
 * This HomeValidator doesn't have a delegate HomeValidator.
 * Instead it delegates to the Primary Pipeline's Home methods. 
 * 
 * This is one of ways that Home Validators can be stored into a Home pipeline.
 * See HomeSequenceHomeValidator for the alternative installation method.
 * In this implementation, a call to the Home create()/store()/remove() actions will
 * be executed as follows:
 *   a) HomeValidator's validateCreate()
 *   b) HomeValidator's resolveCreate()
 *   c) HomeValidator's create().  At the last HomeValidator's the delegate gate will be the 
 *      primary pipeline's Home create() call.
 * Similarly for the other Home methods.
 * 
 * @author angie.li@redknee.com
 *
 */
public class HomeChainingHomeValidator extends AbstractValidatorHome 
{

    public HomeChainingHomeValidator(Context context, Home primaryPipe)
    {
        super(context, NullValidatorHome.instance());
        primaryPipeline_ = primaryPipe;
    }
    
    public Object create(Context context, Object obj)
        throws HomeException, HomeInternalException
    {
        /* After all the HomeValidators have performed their create methods, 
         * continue with the rest of the normal pipeline. */
        return primaryPipeline_.create(context, obj);
    }
    
    public Object store(Context context, Object obj)
        throws HomeException, HomeInternalException
    {
        /* After all the HomeValidators have performed their store methods, 
         * continue with the rest of the normal pipeline. */
        return primaryPipeline_.store(context, obj);
    }
    
    public void remove(Context context, Object obj)
        throws HomeException, HomeInternalException
    {
        /* After all the HomeValidators have performed their remove methods, 
         * continue with the rest of the normal pipeline. */
        primaryPipeline_.remove(context, obj);
    }
    
    // the Primary Pipeline which the Home Validator pipeline is validating.
    private Home primaryPipeline_ = null;
}
