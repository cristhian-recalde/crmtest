/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.validator;

import com.trilogy.app.crm.dunning.LevelInfo;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

/**
 * @author Sapan Modi
 * @since 10.1.8
 *
 */
public class LevelInfoIdValidator implements Validator
{

    /**
     * {@inheritDoc}
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        if (obj instanceof LevelInfo)
        {
        	if(((LevelInfo)obj).getId() == 0)
        	{
        		throw new IllegalStateException("Please specify Level ID other than '0'"); 
        	}
        }
    }
    
 }
