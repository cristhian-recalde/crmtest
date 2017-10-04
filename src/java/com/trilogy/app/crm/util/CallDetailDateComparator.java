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
package com.trilogy.app.crm.util;

import java.util.Comparator;

import com.trilogy.app.crm.bean.calldetail.CallDetail;

/**
 * @author rchen
 * Ported from 7.7
 */
@SuppressWarnings("unchecked")
public final class CallDetailDateComparator extends Reversible implements Comparator
{
    public CallDetailDateComparator(boolean bDesc)
    {
        super(bDesc);
    }
    
    public int compare(Object obj1, Object obj2)
    {
        CallDetail cd1 = (CallDetail)obj1;
        CallDetail cd2 = (CallDetail)obj2;
        
        if (isDescending())
        {
            return cd2.getTranDate().compareTo(cd1.getTranDate());
        }
        else
        {
            return cd1.getTranDate().compareTo(cd2.getTranDate());
        }
    }
}
