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
package com.trilogy.app.crm.pos;

/**
 * Customized Exception type for Point of Sale Processors
 * 
 * @author Angie Li
 *
 */
public class POSProcessorException extends Exception 
{

    public POSProcessorException(String processor, String s)
    {
        super(s);
        processor_ = processor;
    }
    
    public POSProcessorException(String processor, Throwable t)
    {
        super(t);
        processor_ = processor;
    }
    
    public POSProcessorException(String processor, String s, Throwable t)
    {
        super(s, t);
        processor_ = processor;
    }
    
    public String getProcessor()
    {
        return processor_;
    }
    
    private String processor_; 
}
