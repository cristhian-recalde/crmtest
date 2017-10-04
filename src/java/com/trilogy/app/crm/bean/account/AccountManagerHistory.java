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
package com.trilogy.app.crm.bean.account;

import com.trilogy.framework.xhome.beans.SafetyUtil;



/**
 * Concrete class for Account Manager History
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class AccountManagerHistory extends AbstractAccountManagerHistory
{

    public long getIdentifier()
    {
        return getId();
    }
    
    public void setIdentifier(long ID) throws IllegalArgumentException
    {
        setId(ID);
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(Object obj)
    {
        if (obj == null)
        {
           return -1;    
        }
        
        AccountManagerHistory other = (AccountManagerHistory) obj;
        
        if (!SafetyUtil.safeEquals(this.getHistoryDate(), other.getHistoryDate()))
        {
            return SafetyUtil.safeCompare(other.getHistoryDate(), this.getHistoryDate());   
        }
        else
        {
            return SafetyUtil.safeCompare(other.getIdentifier(), this.getIdentifier());  
        }
    }
}
