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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.sat;

import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.beans.Validator;

/**
 * 
 * @author Aaron Gourley
 * @since 
 *
 */
public class SATValidatorFactory
{    
    private static CompoundValidator instance_ = null;
    
    private SATValidatorFactory()
    {}
    
    public static Validator createSATValidator()
    {
        if( instance_ == null )
        {
            instance_ = new CompoundValidator();
            instance_.add(UniqueSATNameForSameSpidValidator.instance());
            instance_.add(SATPricePlanValidator.instance());
        }
        return instance_;
    }
}
