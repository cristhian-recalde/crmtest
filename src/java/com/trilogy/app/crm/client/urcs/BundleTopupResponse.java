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
package com.trilogy.app.crm.client.urcs;

import java.util.Calendar;

/**
 * @author sbanerjee
 *
 */
public class BundleTopupResponse
{
    int serviceResponseCode = -1;
    long newBalance= 0L;
    Calendar newExpiryDate = null;
    
    public BundleTopupResponse(int serviceResponseCode, long newBalance, Calendar newExpiryDate)
    {
        this.serviceResponseCode = serviceResponseCode;
        this.newBalance = newBalance;
        this.newExpiryDate = newExpiryDate;
    }
    
    /**
     * @return the newExpiryDate
     */
    public Calendar getNewExpiryDate()
    {
        return newExpiryDate;
    }

    /**
     * @param newExpiryDate the newExpiryDate to set
     */
    public void setNewExpiryDate(Calendar newExpiryDate)
    {
        this.newExpiryDate = newExpiryDate;
    }

    /**
     * @return the serviceResponseCode
     */
    public int getServiceResponseCode()
    {
        return serviceResponseCode;
    }
    /**
     * @param serviceResponseCode the serviceResponseCode to set
     */
    public void setServiceResponseCode(int serviceResponseCode)
    {
        this.serviceResponseCode = serviceResponseCode;
    }
    /**
     * @return the newBalance
     */
    public long getNewBalance()
    {
        return newBalance;
    }
    /**
     * @param newBalance the newBalance to set
     */
    public void setNewBalance(long newBalance)
    {
        this.newBalance = newBalance;
    }
    
}
