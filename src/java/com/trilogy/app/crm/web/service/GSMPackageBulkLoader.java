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

import com.trilogy.app.crm.bean.GSMPackageImport;
import com.trilogy.app.crm.bean.GSMPackageImportCSVHome;
import com.trilogy.app.crm.bean.GSMPackageImportCSVSupport;
import com.trilogy.app.crm.bulkloader.GSMPackageImportToGenericPackageImportAdapter;
import com.trilogy.app.crm.technology.TechnologyEnum;

/**
 * PackageBulkLoader - parse the file and load the Package into the Package
 * table
 */
public class GSMPackageBulkLoader extends GenericPackageBulkLoader
{

	private GSMPackageImportToGenericPackageImportAdapter adapter_ = null;

	public GSMPackageBulkLoader(Context ctx, String fileName, int spid,
	    String packageGroup, String batchId)
	{
		super(ctx, fileName, batchId);
		adapter_ =
		    new GSMPackageImportToGenericPackageImportAdapter(spid,
		        packageGroup);
	}

	@Override
	protected Home createImportHome(Context ctx) throws HomeException
	{
		return new GSMPackageImportCSVHome(ctx, getBulkLoadFile());
	}

	@Override
	protected int getPackageTechnology(Object pkg)
	{
		return TechnologyEnum.GSM_INDEX;
	}

	@Override
	protected void createGSMPackage(Context ctx, Object obj, int count)
	{
		Object obj2 = obj;
		if (obj instanceof GSMPackageImport)
		{
			obj2 = adapter_.adapt(ctx, obj);
		}
		super.createGSMPackage(ctx, obj2, count);
	}

}
