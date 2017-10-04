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
package com.trilogy.app.crm.pos;

import com.trilogy.app.crm.pos.Conciliation;
import com.trilogy.app.crm.pos.ConciliationCSVSupport;

/**
 * CVS Support doesn't use the delimiter.
 * 
 * @author Angie Li
 */
public class ConciliationCustomizedCSVSupport extends ConciliationCSVSupport 
{
	private final static ConciliationCustomizedCSVSupport instance__ = new ConciliationCustomizedCSVSupport();
	
	public static ConciliationCustomizedCSVSupport getInstance()
	{
		return instance__;
	}
	
	
	public ConciliationCustomizedCSVSupport()
	{
		
	}
	
    @Override
	public StringBuffer append(StringBuffer buf, char delimiter, Object obj)
	{
		Conciliation bean = (Conciliation) obj;
		
		appendString(buf, bean.getPayNum());
		appendString(buf, bean.getTxnDate());
		appendString(buf, bean.getAmount());
		
		return buf;
	}

    
    @Override
    public StringBuffer appendString(StringBuffer buf, String str)
    {
        return POSReportSupport.appendStringWithoutQuotes(buf, str);
    }

}
