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
package com.trilogy.app.crm.subscriber.provision;

/**
 * @author jchen
 */
public class AdjustmentResult  
{
    private int min_;
    private int amt_;

    public AdjustmentResult(int min, int amt)
    {
        min_ = min;
        amt_ = amt;
    }

    public int getAdjustMin()
    {
        return min_;
    }

    public int getAdjustAmt()
    {
        return amt_;
    }
}
