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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.app.crm.bean.payment.PaymentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.SelectWebControl;

/**
 * Customized web control will parse the value of the String field 
 * (comma-separated values) and display the distinct values as a drop-down
 * menu.
 *
 * The fromWeb has not been overridden as it works.
 *
 * @author Angie Li
 */ 
public class PaymentExceptionResolutionSubIDWebControl extends SelectWebControl 
{
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        setCollection(parse((String) obj));
        super.toWeb(ctx, out, name, obj);
    }
    
    private Collection parse(String value)
    {
        value = value.trim();
        ArrayList<String> list = new ArrayList<String>();
        
        //Add optional value, if there is more than one choice
        if (value.indexOf(DELIMITER) > 0)
        {
            list.add(OPTIONAL_VALUE);
        }
        
        while(value.length() > 0 )
        {
            int endChar = value.indexOf(DELIMITER);
            if (endChar > 0)
            {
                // Subscriber ID excluding the comma
                list.add(value.substring(0, endChar));
                // Remainder of the list after the comma
                value = value.substring(endChar + 1);                
            }
            else
            {
                // Add the remainder of the string 
                list.add(value);
                value = "";
            }
        }
        return list;
    }

    public static final String OPTIONAL_VALUE = "-Select One-";
    public static final char DELIMITER = '|'; 
}
