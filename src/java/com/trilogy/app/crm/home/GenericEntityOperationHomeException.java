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
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.home.HomeException;

/**
 * @author sbanerjee
 *
 */
public class GenericEntityOperationHomeException extends HomeException
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Code should ideally be > 0 as defined in ICDs
     */
    private long entityOperationCode = -1;

    /**
     * @return the entityOperationCode
     */
    public final long getEntityOperationCode()
    {
        return this.entityOperationCode;
    }
    
    /**
     * 
     * @param entityOperationCode
     * @param s
     */
    public GenericEntityOperationHomeException(long entityOperationCode, String s)
    {
        super(s);
        this.entityOperationCode = entityOperationCode;
    }

    /**
     * 
     * @param entityOperationCode
     * @param s
     * @param t
     */
    public GenericEntityOperationHomeException(long entityOperationCode, String s, Throwable t)
    {
        super(s, t);
        this.entityOperationCode = entityOperationCode;
    }

    /**
     * 
     * @param entityOperationCode
     * @param t
     */
    public GenericEntityOperationHomeException(long entityOperationCode, Throwable t)
    {
        super(t);
        this.entityOperationCode = entityOperationCode;
    }

}
