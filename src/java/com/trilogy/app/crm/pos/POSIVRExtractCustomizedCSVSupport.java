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
 * @author Angie Li
 */
public class POSIVRExtractCustomizedCSVSupport extends POSIVRExtractCSVSupport 
{
	private final static POSIVRExtractCustomizedCSVSupport instance__ = new POSIVRExtractCustomizedCSVSupport();
	
	public static POSIVRExtractCustomizedCSVSupport getInstance()
	{
		return instance__;
	}
	
	public POSIVRExtractCustomizedCSVSupport()
	{
		
	}
    
	@Override
	public StringBuffer append(StringBuffer buf, char delimiter, Object obj)
	{
		POSIVRExtract bean = (POSIVRExtract) obj;
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
		appendString(buf, bean.getBan());
		appendString(buf, bean.getMsisdn());
		appendString(buf, bean.getBalance());
		appendString(buf, bean.getDateOfExtraction());
		appendString(buf, bean.getInvoiceDueDate());
		appendString(buf, bean.getInvoiceAmount());
		appendString(buf, bean.getAdjustment());
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
