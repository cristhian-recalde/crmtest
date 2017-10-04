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
package com.trilogy.app.crm.writeoff;

/**
 * 
 *
 * @author ray.chen@redknee.com
 */
final class WriteOffInput
{
    public static final int INVALID_NUMBER = -9999;
    
    public int SPID = INVALID_NUMBER;
    public int Type = INVALID_NUMBER;
    public String BAN = null;
    public int BillCycle = INVALID_NUMBER;
    public String LastName = null;
    public String FirstName = null;
    public String AccountName = null;
    public int State = INVALID_NUMBER;
    public long ExternalTransactionId = INVALID_NUMBER;
    
    public String OriginalString = null;
    
    public void setBan(String ban)
    {
        if (ban!=null)
        {
            BAN = ban.trim();
        }
    }
}
