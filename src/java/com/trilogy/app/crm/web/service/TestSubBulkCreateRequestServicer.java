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
package com.trilogy.app.crm.web.service;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

import com.trilogy.app.crm.bean.*;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import junit.framework.TestCase;

/**
 * @author jchen
 */
public class TestSubBulkCreateRequestServicer extends TestCase
{
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	//    public static void backupMsisdnHome(Context ctx) throws HomeException
	//    {
	//        Home jdbcHome = new MsisdnXDBHome(ctx);
	//        Home csvHome = new MsisdnCSVHome("\\opt\\msisdn.csv"); 
	//        backupTable(ctx, jdbcHome, csvHome);
	//    }
	//    public static void backupPackageHome(Context ctx) throws HomeException
	//    {
	//        //package home
	//        Home jdbcHome = new PackageXDBHome(ctx);
	//        Home csvHome = new PackageCSVHome("\\opt\\package.csv");                
	//        backupTable(ctx, jdbcHome, csvHome);
	//    }
	//    public static void backupPackageGroupHome(Context ctx) throws HomeException
	//    {
	//        //package home
	//        Home jdbcHome = new PackageGroupXDBHome(ctx);
	//        Home csvHome = new PackageCSVHome("\\opt\\packageGroup.csv");                
	//        backupTable(ctx, jdbcHome, csvHome);
	//    }
	//    
	//    public static void backupTable(Context ctx, Home jdbcHome, Home csvHome) throws HomeException
	//    {
	//        Collection records = jdbcHome.selectAll();
	//        Iterator it = records.iterator();
	//        int size = 0;
	//        while (it.hasNext())
	//        {
	//            csvHome.create(it.next());
	//            System.out.println("Record backup  " + size);            
	//            size++;
	//        }
	//    }
	//    

	//    ArrayList msisdnGroupList = null;
	//    Home msisdnGroupHome = null;         
	//    public void prepareMsisdnGroupData(Context ctx) throws HomeException
	//    {
	//        msisdnGroupList = new ArrayList();
	//        msisdnGroupHome = new MsisdnGroupTransientHome();        
	//        
	//            for (int i = 0; i < 5; i ++)
	//            {
	//		        MsisdnGroup mg = new MsisdnGroup();
	//		        mg.setId(i);
	//		        mg.setName("md" + i);
	//		        	       
	//		        mg = (MsisdnGroup)msisdnGroupHome.create(mg);
	//		        msisdnGroupList.add(mg);	        
	//            }        
	//            ctx.put(MsisdnGroupHome.class, msisdnGroupHome);
	//    }
	//    

}
