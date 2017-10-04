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

/**
 * @author rchen
 * Ported from 7.7
 */
abstract class Reversible
{
    protected final boolean isDescending_;
    public Reversible(boolean bDescending)
    {
        isDescending_ = bDescending;
    }
    
    protected boolean isDescending()
    {
        return isDescending_;
    }
}
