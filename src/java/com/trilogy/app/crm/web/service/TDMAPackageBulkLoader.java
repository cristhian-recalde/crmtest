/**
    PackageBulkLoader

    @Author : Lanny Tse
    Date   : Oct 20, 2003

    Copyright (c) Redknee, 2002
        - all rights reserved
 **/

package com.trilogy.app.crm.web.service;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.TDMAPackageImport;
import com.trilogy.app.crm.bean.TDMAPackageImportCSVHome;
import com.trilogy.app.crm.bean.TDMAPackageImportCSVSupport;
import com.trilogy.app.crm.bulkloader.TDMAPackageImportToGenericPackageImportAdapter;
import com.trilogy.app.crm.technology.TechnologyEnum;

/**
 * PackageBulkLoader - parse the file and load the Package into the Package
 * table
 */
public class TDMAPackageBulkLoader extends GenericPackageBulkLoader
{

	private TDMAPackageImportToGenericPackageImportAdapter adapter_ = null;

	private final TechnologyEnum tech_;
	public static final char DELIMITER = ',';
	public static final String ERR_EXT = ".err";

	public TDMAPackageBulkLoader(Context ctx, String fileName, int spid,
	    String packageGroup, TechnologyEnum tech, String batchId)
	{
		super(ctx, fileName, batchId);
		adapter_ =
		    new TDMAPackageImportToGenericPackageImportAdapter(spid,
		        packageGroup, tech);
		tech_ = tech;
	}

	@Override
	protected Home createImportHome(Context ctx) throws HomeException
	{
		return new TDMAPackageImportCSVHome(ctx, getBulkLoadFile());
	}

	@Override
	protected int getPackageTechnology(Object pkg)
	{
		return tech_.getIndex();
	}

	@Override
	protected void createTDMACDMAPackage(Context ctx, Object obj,
	    TechnologyEnum tech, int count)
	{
		Object obj2 = obj;
		if (obj instanceof TDMAPackageImport)
		{
			obj2 = adapter_.adapt(ctx, obj);
		}
		super.createTDMACDMAPackage(ctx, obj2, tech, count);
	}

}
