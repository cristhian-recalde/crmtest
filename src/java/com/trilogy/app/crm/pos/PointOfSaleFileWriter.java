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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * File Writer class for all Point of Sale
 * @author ali 
 */
public interface PointOfSaleFileWriter 
{
    /**
     * Extracts information and writes to a CSV file.
     * 
     * @param ctx
     * @throws HomeException
     */
    abstract void writeFile(Context ctx) throws HomeException;

    /**
     * Looks up and returns the file name for this extraction preconfigured in the POS Configuration.
     * 
     * @param ctx
     * @return
     */
    abstract String getFileName(Context ctx); 
    
}
