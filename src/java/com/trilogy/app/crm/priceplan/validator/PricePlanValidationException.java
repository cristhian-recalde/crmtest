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
package com.trilogy.app.crm.priceplan.validator;

import com.trilogy.framework.xhome.home.HomeException;

/**
 * General Price Plan Validation Exception.  Used to validate adherence to 
 * Price Plan Group configuration (Exclusive/Inclusive/Prerequisite rules).
 * @author ali
 *
 */
public class PricePlanValidationException extends HomeException 
{
    public PricePlanValidationException(String s) 
    {
        super(s);

    }
    
    public PricePlanValidationException(String s, Throwable t) 
    {
        super(s, t);
    }
    
    private static final long serialVersionUID = 1L;
}
