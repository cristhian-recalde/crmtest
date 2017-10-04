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

import com.trilogy.app.crm.support.PointOfSaleConfigurationSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;

/**
 * CVS Support doesn't use the delimiter.
 * 
 * @author Angie Li
 */
public class CashierCustomizedCSVSupport extends CashierCSVSupport 
{

	private final static CashierCustomizedCSVSupport instance__ = new CashierCustomizedCSVSupport();
	

	
	
	public static CashierCustomizedCSVSupport getInstance()
	{
		return instance__;
	}
	
	
	public CashierCustomizedCSVSupport()
	{
		
	}
	
	@Override
	public StringBuffer append(StringBuffer buf, char delimiter, Object obj)
	{
		Cashier bean = (Cashier) obj;
		boolean spidAsFirst = false;
		boolean allowedSpidForPinting = true;
		
		Context ctx = ContextLocator.locate();
		
		if (ctx!=null)
		{
            PointOfSaleConfiguration config = PointOfSaleConfigurationSupport.getPOSConfig(ctx);
            spidAsFirst = config.isExportSpidAsFirstField();
            allowedSpidForPinting = config.isAllowedSpidInPOSFile();
		}
		
		if (allowedSpidForPinting && spidAsFirst)
		{
	        appendString(buf, bean.getSpid());
		}
		appendString(buf, bean.getName());
		appendString(buf, bean.getMsisdn());
		appendString(buf, bean.getBan());
		appendString(buf, bean.getBalance());
		appendString(buf, bean.getAddress());
		appendString(buf, bean.getDateOfExtraction());
		appendString(buf, bean.getChIdn());
		appendString(buf, bean.getCurrDate());
		appendString(buf, bean.getLastPaid());
		appendString(buf, bean.getLastDate());
		appendString(buf, bean.getArrears());
        if (allowedSpidForPinting && !spidAsFirst)
        {
            appendString(buf, bean.getSpid());
        }
        
		return buf;
	}
	
	@Override
	public StringBuffer appendString(StringBuffer buf, String str)
	{
	    return POSReportSupport.appendStringWithoutQuotes(buf, str);
	}
	
	
}
