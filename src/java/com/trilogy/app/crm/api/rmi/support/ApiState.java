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
package com.trilogy.app.crm.api.rmi.support;

/**
 * The purpose is to capture a transient global state for the API. This state will capture
 * (probable) updates pertaining to each (all) the options in an API-request.  
 * 
 * @author sbanerjee
 * 
 */
public class ApiState
{
    private long calculatedSubscriberBalanceRemaining;

    /**
     * @return the calculatedSubscriberBalanceRemaining
     */
    public long getCalculatedSubscriberBalanceRemaining()
    {
        return calculatedSubscriberBalanceRemaining;
    }

    /**
     * @param calculatedSubscriberBalanceRemaining the calculatedSubscriberBalanceRemaining to set
     */
    public void setCalculatedSubscriberBalanceRemaining(
            long calculatedSubscriberBalanceRemaining)
    {
        this.calculatedSubscriberBalanceRemaining = calculatedSubscriberBalanceRemaining;
    }
    
    /**
     * 
     * @param addBy
     */
    public void addCalculatedSubscriberBalanceRemainingBy(
            long addBy)
    {
        this.calculatedSubscriberBalanceRemaining += addBy;
    }
    
    public void reduceCalculatedSubscriberBalanceRemainingBy(
            long reduceBy)
    {
        this.calculatedSubscriberBalanceRemaining -= reduceBy;
    }
}
