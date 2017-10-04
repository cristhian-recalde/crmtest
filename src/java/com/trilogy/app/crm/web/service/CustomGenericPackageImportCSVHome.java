/*
 * This code is a protected work and subject to domestic and international copyright

 * law(s). A complete listing of authors of this work is readily available. Additionally,

 * source code is, by its very nature, confidential information and inextricably contains

 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No

 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be

 * used in accordance with the terms of the license agreement entered into with Redknee

 * Inc. and/or its subsidiaries.

 * 

 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.

 */
package com.trilogy.app.crm.web.service;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.app.crm.bean.GenericPackageImportCSVSupport;
import com.trilogy.framework.xhome.csv.GenericCSVHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;


/**
 * Simple Persistence using Comma-Seperated-Value (CSV) flat files.
 */
public class CustomGenericPackageImportCSVHome extends GenericCSVHome implements Home
{

    /**
     * @param ctx 
     * @param filename
     *            name of Bulk Load File
     * @throws HomeException 
     */
    public CustomGenericPackageImportCSVHome(final Context ctx, final String filename) throws HomeException
    {
        this(ctx, filename, com.redknee.framework.xhome.csv.Constants.DEFAULT_SEPERATOR);
    }


    /**
     * @param ctx
     * @param filename
     *            name of Bulk Load File
     * @param seperator
     *            File seperator used in Bulk LOad File
     * @throws HomeException
     */
    public CustomGenericPackageImportCSVHome(final Context ctx, final String filename, final char seperator)
            throws HomeException
    {
        super(ctx, new GenericPackageImportTransientHomeProxy(ctx), GenericPackageImportCSVSupport.instance(),
                filename, seperator);
    }
}
