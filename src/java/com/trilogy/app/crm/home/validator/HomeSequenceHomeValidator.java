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
 * This HomeValidator continues to execute the primary pipeline after all the HomeValidators 
 * have performed their Home actions. 
 * 
 * This is one of ways that Home Validators can be stored into a Home pipeline.  
 * See HomeChainingHomeValidator for the alternative installation method.
 * In this implementation, a call to the Home create()/store()/remove() actions will
 * be executed as follows:
 *   a) HomeValidator's validateCreate()
 *   b) HomeValidator's resolveCreate()
 *   c) HomeValidator's create(). HomeValidator's pipeline is completely traversed.
 *   d) Home's create().  Home pipeline continues to traverse.
 * Similarly for the other Home methods.
 *
 * @author angie.li@redknee.com
 *
 */
public class HomeSequenceHomeValidator extends AbstractValidatorHome 
{

    public HomeSequenceHomeValidator(Context context, HomeValidator validatedPipe, Home primaryPipe)
    {
        super(context, validatedPipe);
        primaryPipeline_ = primaryPipe;
    }
    
    /* Execute the continuation of the primary pipeline after all the HomeValidators 
     * have performed their Home actions. */
    public Object create(Context context, Object obj)
        throws HomeException, HomeInternalException
    {
        Object returnObj = super.create(context, obj);
        
        return primaryPipeline_.create(context, returnObj);
    }
    
    /* Execute the continuation of the primary pipeline after all the HomeValidators 
     * have performed their Home actions. */
    public Object store(Context context, Object obj)
        throws HomeException, HomeInternalException
    {
        Object returnObj = super.store(context, obj);
        return primaryPipeline_.store(context, returnObj);
    }
    
    /* Execute the continuation of the primary pipeline after all the HomeValidators 
     * have performed their Home actions. */
    public void remove(Context context, Object obj)
        throws HomeException, HomeInternalException
    {
        super.remove(context, obj);
        primaryPipeline_.remove(context, obj);
    }
    
    // the Primary Pipeline which the Home Validator pipeline is validating.
    private Home primaryPipeline_ = null;
}
