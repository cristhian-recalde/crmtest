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
package com.trilogy.app.crm.numbermgn;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;

import com.trilogy.app.crm.agent.Install;
import com.trilogy.app.crm.bean.GSMPackageXDBHome;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnGroup;
import com.trilogy.app.crm.bean.MsisdnGroupHome;
import com.trilogy.app.crm.bean.MsisdnGroupTransientHome;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.MsisdnXDBHome;
import com.trilogy.app.crm.bean.PackageGroup;
import com.trilogy.app.crm.bean.PackageGroupHome;
import com.trilogy.app.crm.bean.PackageGroupXDBHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.TDMAPackageXDBHome;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;



/**
 * @author jchen
 */
public class NumberMgnSupportTest extends ContextAwareTestCase {

    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public NumberMgnSupportTest(final String name)
    {
        super(name);
    }


    /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by standard JUnit tools (i.e., those that do not provide a
     * context).
     *
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }


    /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by the Redknee Xtest code, which provides the application's
     * operating context.
     *
     * @param context The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(NumberMgnSupportTest.class);

        return suite;
    }
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp()
    {
        super.setUp();
        
        backupNumberHomes(getContext());
        
        try
        {
            preparePackagesTest(getContext());
            prepareMsisdnsTest(getContext());
        }
        catch (Exception e)
        {
            assertTrue(false);
        }
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown(){
        super.tearDown();
        
        restoreNumberHomes(getContext());
    }
    
    
    /**
     * In order to let prepared home to take effect, we need to install these homes
     * to install.java level, so all request services will still work.
     */
    public Context getContext()
    {
        Context ctx = super.getContext();
        return ctx;
    }
    
    
    
    
    /*********************************************************
     * 
     *Test routines.
     *
     */
    
    
    Home backupMsisdnHome_ = null;
    Home backupMsisdnGroupHome_ = null;
    Home backupPackageHome_ = null;
    Home backupPackageGroupHome_ = null;
    
    void backupNumberHomes(Context ctx)
    {
        backupMsisdnHome_ = (Home)ctx.get(MsisdnHome.class);
        backupMsisdnGroupHome_ = (Home)ctx.get(MsisdnGroupHome.class);
//        backupPackageHome_ = (Home)ctx.get(PackageHome.class);
        backupPackageGroupHome_ = (Home)ctx.get(PackageGroupHome.class);
    }
    
    void restoreNumberHomes(Context ctx)
    {
        if (backupMsisdnHome_ != null)
            ctx.put(MsisdnHome.class, backupMsisdnHome_);
        if (backupMsisdnGroupHome_ != null)
            ctx.put(MsisdnGroupHome.class, backupMsisdnGroupHome_);
        if (backupPackageHome_ != null)
//            ctx.put(PackageHome.class, backupPackageHome_);
        if (backupPackageGroupHome_ != null)
            ctx.put(PackageGroupHome.class, backupPackageGroupHome_);
    }
    
    
    
    Home createMsisdnHome(Context ctx) throws HomeException
    {
        Home home =  new MsisdnXDBHome(ctx, "Msisdn_test");
        ctx.put(MsisdnHome.class, home);
        home.removeAll(ctx);
        return home;
        
    }
    Home createMsisdnGroupHome(Context ctx) throws HomeException
    {
        Home home =   new MsisdnGroupTransientHome(ctx);
        ctx.put(MsisdnGroupHome.class, home);
        home.removeAll(ctx);
        return home;
    }
    Home createGSMPackageHome(Context ctx) throws HomeException
    {
        Home home =   new GSMPackageXDBHome(ctx, "GSMPackage_test");
//        ctx.put(PackageHome.class, home);
        home.removeAll(ctx);
        return home;
    }    
    Home createTDMAPackageHome(Context ctx) throws HomeException
    {
        Home home =   new TDMAPackageXDBHome(ctx, "TDMAPackage_test");
//        ctx.put(PackageHome.class, home);
        home.removeAll(ctx);
        return home;
    }    
    Home createPackageGroupHome(Context ctx) throws HomeException
    {
        Home home =   new PackageGroupXDBHome(ctx, "PackagGroup_test");
        ctx.put(PackageGroupHome.class, home);
        home.removeAll(ctx);
        return home;
    }
    


    
    
    /***********************
     * 
     * 
     * Praparing test data in temp tables.
     */
    
    
    
    String[] groupMsisdnPrefix = new String[] {"41611100", "41622200", "41633300", "416444000", "416555000"};
    
    ArrayList msisdnGroupList = null;
    void prepareMsisdnGroupData(Context ctx) throws HomeException
    {
        msisdnGroupList = new ArrayList();
        Home msisdnGroupHome = (Home)ctx.get(MsisdnGroupHome.class);
        for (int i = 0; i < TOTAL_GROUP_NUMBER; i++)
        {
            MsisdnGroup mg = new MsisdnGroup();
            mg.setName("msisdn group" + i);
            mg.setId(i);
            msisdnGroupHome.create(ctx,mg);
        }
        
    }
    
    
    ArrayList msisdnList = null;
    Home msisdnHome = null;
    
    void prepareMsisdnData(Context ctx) throws HomeException
    {
        msisdnList = new ArrayList();
        Home localMsisdnHome = (Home)ctx.get(MsisdnHome.class);
        
        
        for (int group = 0; group < TOTAL_GROUP_NUMBER; group++)
        {
            for (int j = 0; j < RECORD_CNT_PER_GROUP; j ++)
            {
	            Msisdn ms = new Msisdn();
	            ms.setMsisdn(groupMsisdnPrefix[group] + j);
	            ms.setGroup(group);
	            ms.setSpid(getSpid(group));
	            	           
	            ms = (Msisdn) localMsisdnHome.create(ctx,ms);
	            msisdnList.add(ms);
            }
        }   
        
    }
    
    ArrayList packageGroupList = null;
    Home packageGroupHome = null;         
    public void preparePackageGroupData(Context ctx) throws HomeException
    {
        packageGroupList = new ArrayList();
        packageGroupHome = (Home)ctx.get(PackageGroupHome.class);      
        
            for (int group = 0; group < TOTAL_GROUP_NUMBER; group++)
            {
		        PackageGroup pg = new PackageGroup();
		        pg.setName("package group" + group);		        
		        pg.setSpid( getSpid(group));
		        	       
		        pg = (PackageGroup)packageGroupHome.create(ctx,pg);
		        packageGroupList.add(pg);	        
            }
    }
    
    
    
    ArrayList packageList = null;
    Home packageHome = null;
    String[] groupPackageIdPrefix = new String[] {"99911100", "99922200", "99933300", "999444000", "999555000"};
    
    String[] groupPackageImsiPrefix = new String[] {"88811100", "88822200", "88833300", "888444000", "888555000"};
    
    void preparePackageData(Context ctx) throws HomeException
    {
/*        packageList = new ArrayList();
        Home packageHome1 = (Home)ctx.get(PackageHome.class);
        
         
        for (int group = 0; group < TOTAL_GROUP_NUMBER; group++)
        {
            for (int j = 0; j < RECORD_CNT_PER_GROUP; j ++)
            {
                com.redknee.app.crm.bean.Package ms = new com.redknee.app.crm.bean.Package();
	            ms.setPackId(groupPackageIdPrefix[group] + j);
	            ms.setIMSI(groupPackageImsiPrefix[group] + j);
	            ms.setSpid(getSpid(group));
	            
	            ms.setPackageGroup(((PackageGroup)packageGroupList.get(group)).getName());	            
	           
	            ms = (com.redknee.app.crm.bean.Package) packageHome1.create(ctx,ms);
	            packageList.add(ms); 
            }
        }*/
    }
    
    public void preparePackagesTest(Context ctx) throws HomeException 
    {
        createGSMPackageHome(ctx);
        createTDMAPackageHome(ctx);
        createPackageGroupHome(ctx);
        
        preparePackageGroupData(ctx);
        preparePackageData(ctx);
        
    }
    
    public void prepareMsisdnsTest(Context ctx)  throws HomeException
    {
        createMsisdnHome(ctx);
        createMsisdnGroupHome(ctx);
        
        prepareMsisdnData(ctx);
        prepareMsisdnGroupData(ctx);
    }
    
    
    void assertMsisdnEqual(Msisdn ms0, Object ms1)
    {
        assertTrue(ms0.getMsisdn().equals(((Msisdn)ms1).getMsisdn()));
    }
    
    /**
     * Spid is not tested
     * @throws Exception
     */
    public void testFirstAvailMsisdn() throws Exception
    {
        prepareMsisdnsTest(getContext());
        
        
        
        Msisdn firstMsisdn;
        
        firstMsisdn = NumberMgnSupport.getFirstAvailMsisdn(getContext(), 0, 0, SubscriberTypeEnum.PREPAID_INDEX);        
        System.out.println(firstMsisdn);
        System.out.println(msisdnList.get(0));
        assertMsisdnEqual(firstMsisdn, msisdnList.get(0));
        System.out.println(firstMsisdn);
        
        
        //Total_group_number - 1 is in spid 1
        firstMsisdn = NumberMgnSupport.getFirstAvailMsisdn(getContext(), 0, TOTAL_GROUP_NUMBER - 1, SubscriberTypeEnum.PREPAID_INDEX);
        System.out.println(firstMsisdn);
        assertNull(firstMsisdn);
        
        //spid 3 is not exist
        firstMsisdn = NumberMgnSupport.getFirstAvailMsisdn(getContext(), 3, 0, SubscriberTypeEnum.PREPAID_INDEX);
        System.out.println(firstMsisdn);
        assertNull(firstMsisdn);
        
        firstMsisdn = NumberMgnSupport.getFirstAvailMsisdn(getContext(), 0, 1, SubscriberTypeEnum.PREPAID_INDEX);
        System.out.println(firstMsisdn);
        assertMsisdnEqual(firstMsisdn, msisdnList.get(RECORD_CNT_PER_GROUP));
        
        
        //now check msisdn state cases
        makeMsisdnInUse((Msisdn)msisdnList.get(0));
        
        Msisdn msisdn1 = (Msisdn)msisdnList.get(1);
        firstMsisdn =  NumberMgnSupport.getFirstAvailMsisdn(getContext(), 0, 0, SubscriberTypeEnum.PREPAID_INDEX);
        System.out.println(firstMsisdn);
        assertMsisdnEqual(firstMsisdn, msisdn1);
    }
    
   
    private void makeMsisdnInUse(Msisdn msisdn0) throws HomeException, HomeInternalException 
    {
        msisdn0.setState(MsisdnStateEnum.IN_USE);
        Home home = (Home)getContext().get(MsisdnHome.class);
        home.store(getContext(),msisdn0);
    }
    
    private void makePackageInUse(Object pk) throws HomeException, HomeInternalException 
    {
    }

    public void testGetAvailMsisdnCnt() throws Exception
    {
        prepareMsisdnsTest(getContext());
        
        
        long cnt;
        cnt = NumberMgnSupport.getAvailMsisdnCnt(getContext(), 0, 0, SubscriberTypeEnum.PREPAID_INDEX, null);
        assertTrue(cnt == RECORD_CNT_PER_GROUP);
        
        cnt = NumberMgnSupport.getAvailMsisdnCnt(getContext(), 0, 0, SubscriberTypeEnum.PREPAID_INDEX, ((Msisdn)msisdnList.get(0)).getMsisdn());
        assertTrue(cnt == (RECORD_CNT_PER_GROUP));
        
        //check adjust startmsisdn to the second one
        cnt = NumberMgnSupport.getAvailMsisdnCnt(getContext(), 0, 0, SubscriberTypeEnum.PREPAID_INDEX, ((Msisdn)msisdnList.get(1)).getMsisdn());
        assertTrue(cnt == (RECORD_CNT_PER_GROUP - 1));
        
        //exceed group bourdary
        cnt = NumberMgnSupport.getAvailMsisdnCnt(getContext(), 0, 0, SubscriberTypeEnum.PREPAID_INDEX, ((Msisdn)msisdnList.get( RECORD_CNT_PER_GROUP + 1)).getMsisdn());
        assertTrue(cnt == 0);
        
        //group not exising
        cnt = NumberMgnSupport.getAvailMsisdnCnt(getContext(), 0, TOTAL_GROUP_NUMBER, SubscriberTypeEnum.PREPAID_INDEX, 
        	((Msisdn)msisdnList.get( RECORD_CNT_PER_GROUP + 1)).getMsisdn());
        assertTrue(cnt == 0);
        
//      //spid not exising
        cnt = NumberMgnSupport.getAvailMsisdnCnt(getContext(), 3, 0, SubscriberTypeEnum.PREPAID_INDEX, ((Msisdn)msisdnList.get( RECORD_CNT_PER_GROUP + 1)).getMsisdn());
        assertTrue(cnt == 0);
        
        //
//      now check msisdn state cases
        makeMsisdnInUse((Msisdn)msisdnList.get(1));
        cnt = NumberMgnSupport.getAvailMsisdnCnt(getContext(), 0, 0, SubscriberTypeEnum.PREPAID_INDEX, null);
        assertTrue(cnt == (RECORD_CNT_PER_GROUP - 1));
        
        cnt = NumberMgnSupport.getAvailMsisdnCnt(getContext(), 0, 0, SubscriberTypeEnum.PREPAID_INDEX, ((Msisdn)msisdnList.get(0)).getMsisdn());
        assertTrue(cnt == (RECORD_CNT_PER_GROUP - 1));
        
    }
    
    
    public void testGetAvailPackageCnt() throws Exception
    {
        preparePackagesTest(getContext());
        
        /*
        int cnt;
        cnt = NumberMgnSupport.getAvailPackageCnt(getContext(), 0, ((PackageGroup)packageGroupList.get(0)).getName());
        assertTrue(cnt == RECORD_CNT_PER_GROUP);
        
        
        //group not exising
        cnt = NumberMgnSupport.getAvailPackageCnt(getContext(), 0, "group no exist");
        assertTrue(cnt == 0);
        
        
//      //spid not exising
        cnt = NumberMgnSupport.getAvailPackageCnt(getContext(), 3, ((PackageGroup)packageGroupList.get(0)).getName());
        assertTrue(cnt == 0);
        
        //
//      now check msisdn state cases
        makePackageInUse(packageList.get(1));
        cnt = NumberMgnSupport.getAvailPackageCnt(getContext(), 0, ((PackageGroup)packageGroupList.get(0)).getName());
        assertTrue(cnt == (RECORD_CNT_PER_GROUP - 1));
        
        makePackageInUse(packageList.get(3));
        cnt = NumberMgnSupport.getAvailPackageCnt(getContext(), 0, ((PackageGroup)packageGroupList.get(0)).getName());
        assertTrue(cnt == (RECORD_CNT_PER_GROUP - 2));
        */
    }
    
    
    /**
     * Call this funciton in install.java to do full test.
     * TODO, put it in xtest
     * @param ctx
     */
    public static void testSubBulkCreate(Context ctx) 
    {
     try
       {
 	      NumberMgnSupportTest testCase = new NumberMgnSupportTest("test code");
 	      testCase.setContext(ctx);
 	      
 	      testCase.testFirstAvailMsisdn();
 	      testCase.testGetAvailMsisdnCnt();
 	      testCase.testGetAvailPackageCnt();
 	      
       }
       catch(Throwable e)
       {
           System.out.println("test .... exception " + e);
           e.printStackTrace();
       }
    }
    
    public final static int TOTAL_GROUP_NUMBER = 5;  //total groups number created for msisdnGroup and package group
    public final static int RECORD_CNT_PER_GROUP = 10; //total number of records created in each msisdn goupr or package group
    public final static int FIRST_GROUP_ARRAY_INDEX_FOR_SPID_1 = 3;
    
    //spid number is dertermined by group index, 
    int getSpid(int groupCnt)
    {
        return groupCnt >= FIRST_GROUP_ARRAY_INDEX_FOR_SPID_1 ? 1 : 0;
    }
    
    public final static int FIRST_NUM_ARRAY_INDEX_FOR_SPID_1 = FIRST_GROUP_ARRAY_INDEX_FOR_SPID_1 *  RECORD_CNT_PER_GROUP;
    
}
