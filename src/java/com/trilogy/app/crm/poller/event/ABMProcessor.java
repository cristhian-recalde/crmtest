/*
 * Copyright (c) 1999-2003, REDKNEE. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of REDKNEE.
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with REDKNEE.
 * 
 * REDKNEE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT. REDKNEE SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR
 * ITS DERIVATIVES.
 */
package com.trilogy.app.crm.poller.event;

import java.util.Iterator;
import java.util.List;

/**
 * This class implements some basic functions aided in processing the ERs
 * parsed from the ABM ER files.
 *
 * @author jimmy.ng@redknee.com
 */
public abstract class ABMProcessor extends CRMProcessor
{
    /**
     * Creates a new ABMProcessor.
     */
    public ABMProcessor()
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
