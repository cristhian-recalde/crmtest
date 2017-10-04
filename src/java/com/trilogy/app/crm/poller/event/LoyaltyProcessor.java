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
package com.trilogy.app.crm.poller.event;

import java.util.Iterator;
import java.util.List;

/**
 * @author dzajac
 */
public abstract class LoyaltyProcessor extends CRMProcessor{
    /**
     * Creates a new LoyaltyProcessor.
     */
    public LoyaltyProcessor()
    {
        super();
    }
    
    
    /**
     * This method formats the given list of parameters into a string
     * and returns the string for debugging purpose.
     *
     * @param _params The given list of paramters.
     * 
     * @return String The returning parameter list in String format.
     */
    public static String getDebugParams(List _params)
    {
        Iterator iParams = _params.iterator();
        int index = 0;

        StringBuilder buf = new StringBuilder();
        while (iParams.hasNext())
        {
            buf.append(index);
            buf.append("[");
            buf.append(CRMProcessorSupport.getField(_params, index));
            buf.append("] ");

            iParams.next();
            index++;
        }

        return buf.toString();
    }
    
    
}
