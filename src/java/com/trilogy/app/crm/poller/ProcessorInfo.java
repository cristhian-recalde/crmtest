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
package com.trilogy.app.crm.poller;


/**
 * @author vcheng
 */
public class ProcessorInfo
{
    public ProcessorInfo(long date, String erid, char[] record, int startIndex)
    {
        date_ = date;
        erid_ = erid;
        record_ = record;
        startIndex_ = startIndex;
    }
    
    
    public long getDate()
    {
        return date_;
    }
    
    
    public String getErid()
    {
        return erid_;
    }
    
    
    public char[] getRecord()
    {
        return record_;
    }
    
    
    public int getStartIndex()
    {
        return startIndex_;
    }
    
    
    private long date_;
    private String erid_;
    private char[] record_;
    private int startIndex_; 
}
