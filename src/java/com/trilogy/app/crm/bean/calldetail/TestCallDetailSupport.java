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
package com.trilogy.app.crm.bean.calldetail;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.support.CallDetailSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.core.factory.FacetInstall;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.beans.DefaultFacetMgr;
import com.trilogy.framework.xhome.beans.DefaultFactoryFacetMgr;
import com.trilogy.framework.xhome.beans.FacetMgr;
import com.trilogy.framework.xhome.beans.ParentClassFacetMgr;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.XMLContext;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xdb.Install;
import com.trilogy.framework.xhome.xdb.XDBConfiguration;
import com.trilogy.framework.xhome.xdb.XDBMgr;
import com.trilogy.framework.xhome.xdb.XDBSupport;
import com.trilogy.framework.xhome.xdb.oracle.OracleDriverManager;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.framework.xlog.log.SeverityEnum;


/**
 * A suite of test cases for CallDetailSupport.
 *
 * @author Nick Landry
 */
public class TestCallDetailSupport
    extends ContextAwareTestCase
{
    public TestCallDetailSupport(final String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }

    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestCallDetailSupport.class);

        return suite;
    }


    @Override
    protected void setUp()
    {
        super.setUp();

        getContext().put(FacetMgr.class, 
                         new DefaultFactoryFacetMgr(
                                 new ParentClassFacetMgr(
                                         new DefaultFacetMgr())));
        try
        {
            new FacetInstall().execute(getContext());
        }
        catch (AgentException e)
        {
        }
        
        LogSupport.setSeverityThreshold(getContext(), SeverityEnum.DEBUG);
        
        
        XDBSupport.putXDBAlias(getContext(), "default");
        CoreSupport.setXMLContext(getContext(), 
                                  new XMLContext(CoreSupport.getFile(getContext(), 
                                                 "Config.xml")));
        try
        {
            new Install().execute(getContext());
        }
        catch (AgentException e)
        {
            LogSupport.crit(getContext(), 
                    this, 
                    "Failed to initialize XDB in subcontext",
                    e);
        }
        
        XDBMgr xdbMgr = (XDBMgr) getContext().get(com.redknee.framework.xhome.xdb.XDBMgr.class);
        Map<String, XDBConfiguration> config = new HashMap<String, XDBConfiguration>();
        
        XDBConfiguration defaultConfig = new XDBConfiguration();
        
        OracleDriverManager xdbDriver = new OracleDriverManager();
        xdbDriver.setHostname("kang");
        xdbDriver.setDatabaseName("RKDB");
        xdbDriver.setPassword("ds");
        xdbDriver.setUsername("ds");
        defaultConfig.setName("default");
        defaultConfig.setDriver(xdbDriver);
        
        config.put("default", defaultConfig);
        
        xdbMgr.setConfigurations(config);
        
        // yield for a couple of seconds to make sure XDB is launched
        try
        {
            Thread.sleep(5000);
        } 
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        
        //getContext().put(CallDetailHome.class, 
        //                 new CallDetailTransientHome(getContext()));
        getContext().put(CallDetailHome.class, 
                         new CallDetailXDBHome(getContext()));
        
        Home cdrHome = (Home)getContext().get(CallDetailHome.class);

        try
        {
            cdrHome.removeAll();
        }
        catch (UnsupportedOperationException e)
        {
            e.printStackTrace();
            assertTrue(false);
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            assertTrue(false);
        }
        
        
        today_ = Calendar.getInstance();
        
        Calendar today = (Calendar) today_.clone();
        
        Date tran = today.getTime();
        Date posted = today.getTime();
        
        addCallDetail("1234", tran, posted);
    }

    @Override
    protected void tearDown()
    {
        if (getContext() != null)
        {
            
        }
        
        super.tearDown();
    }
    
    // This isn't testing CallDetailSupport, rather it is testing Framework's
    // support of nesting 'And's
    public void _testNestedAndBug()
    {
        Home cdrHome = (Home)getContext().get(CallDetailHome.class);
        
        
        And whereA = new And().add(new EQ(CallDetailXInfo.SUBSCRIBER_ID, 
                                          "1234"));
        
        And whereB = new And().add(new EQ(CallDetailXInfo.BAN, "1234"));
        
        And where = new And().add(whereA)
                             .add(whereB);
                
        try
        {
            cdrHome.select(getContext(), where);
        } 
        catch (UnsupportedOperationException e)
        {
            e.printStackTrace();
            assertTrue(false);
        } 
        catch (HomeException e)
        {
            e.printStackTrace();
            assertTrue(false);
        }
    }
    
    public void _testPerformance()
    {        
        long avg = 0;
        for (int i = 0 ; i < 100; i++)
        {
            PMLogMsg pm = new PMLogMsg(this.getClass().getName(), "getCallDetailsForSubscriberIDHome");
            testSubscriberIDHomeMatch();
            pm.log(getContext());
            LogSupport.info(getContext(), this, "Duration: " + pm.duration());
            avg += pm.duration();
        }   
        LogSupport.info(getContext(), this, "Avg Duration: " + (float)avg / 100.0);
    }
    /*
    public void testFlush()
    {
        
    }
     */
    public void testSubscriberIDHomeMatch()
    {
        Calendar today = (Calendar)today_.clone();
                
        today.add(Calendar.DAY_OF_MONTH, -5);
        Date start = today.getTime();
        
        today.add(Calendar.DAY_OF_MONTH, 10);
        Date end =  today.getTime();
        
        today.add(Calendar.YEAR, -1);
        Date posted = today.getTime();
        
        Home filteredHome = CallDetailSupportHelper.get(getContext()).getCallDetailsForSubscriberIDHome(
                                                            getContext(), 
                                                            "1234", 
                                                            start, 
                                                            end, 
                                                            posted);
        
        checkResult(filteredHome, 1);
    }

    public void testSubscriberIDHomeNoMatch()
    {
        Calendar today = (Calendar)today_.clone();
        
        today.add(Calendar.DAY_OF_MONTH, -5);
        Date start = today.getTime();
        
        today.add(Calendar.DAY_OF_MONTH, 10);
        Date end =  today.getTime();
        
        today.add(Calendar.YEAR, -1);
        Date posted = today.getTime();
        
        Home filteredHome = CallDetailSupportHelper.get(getContext()).getCallDetailsForSubscriberIDHome(
                                                            getContext(), 
                                                            "5555", 
                                                            start, 
                                                            end, 
                                                            posted);
        
        checkResult(filteredHome, 0);
    }
    
    public void testBanMatch()
    {
        Calendar today = (Calendar)today_.clone();
                
        today.add(Calendar.DAY_OF_MONTH, -5);
        Date start = today.getTime();
        
        today.add(Calendar.DAY_OF_MONTH, 10);
        Date end =  today.getTime();
        
        today.add(Calendar.YEAR, -1);
        Date posted = today.getTime();
        
        Collection<CallDetail> c = CallDetailSupportHelper.get(getContext()).getCallDetailsForAccount(getContext(), 
                                                                        "1234", 
                                                                        start, 
                                                                        end, 
                                                                        posted);
                                                                    
        assertTrue(c.size() == 1);
    }
    
    public void testBanNoMatch()
    {
        Calendar today = (Calendar)today_.clone();
                
        today.add(Calendar.DAY_OF_MONTH, -5);
        Date start = today.getTime();
        
        today.add(Calendar.DAY_OF_MONTH, 10);
        Date end =  today.getTime();
        
        today.add(Calendar.YEAR, -1);
        Date posted = today.getTime();
        
        Collection<CallDetail> c = CallDetailSupportHelper.get(getContext()).getCallDetailsForAccount(getContext(), 
                                                                        "1111", 
                                                                        start, 
                                                                        end, 
                                                                        posted);
                                                                    
        assertTrue(c.size() == 0);
    }
    
    
    
    public void testCallDetail_ByDate_Success()
    {
        Calendar today = (Calendar)today_.clone();
        
        today.add(Calendar.DAY_OF_MONTH, -5);
        Date start = today.getTime();
        
        today.add(Calendar.DAY_OF_MONTH, 10);
        Date end =  today.getTime();
        
        today.add(Calendar.YEAR, -1);
        Date lastPosted = today.getTime();
        
        Home filteredHome = CallDetailSupportHelper.get(getContext()).getCallDetailsForSubscriberIDHome(
                                                            getContext(), 
                                                            "1234", 
                                                            start, 
                                                            end, 
                                                            lastPosted);
        
        checkResult(filteredHome, 1);
    }
    
    // PostedDate range check will fail
    // POSTED == now < start (false)
    public void testCallDetail_ByDate_FalseStart()
    {
        Calendar today = (Calendar) today_.clone();
        
        today.add(Calendar.DAY_OF_MONTH, 5);
        Date start = today.getTime();
        today.add(Calendar.DAY_OF_MONTH, 10);
        Date end =  today.getTime();
        
        today.add(Calendar.YEAR, -1);
        Date lastInvoice = today.getTime();
        
        Home filteredHome = CallDetailSupportHelper.get(getContext()).getCallDetailsForSubscriberIDHome(
                                                            getContext(), 
                                                            "1234", 
                                                            start, 
                                                            end, 
                                                            lastInvoice);
        
        checkResult(filteredHome, 0);
    }
    
    // PostedDate range will be successful since it is inclusive
    public void testCallDetail_ByDate_StartBoundary()
    {
        Calendar today = (Calendar) today_.clone();
        
        Date start = today.getTime();
        today.add(Calendar.DAY_OF_MONTH, 10);
        Date end =  today.getTime();
        
        today.add(Calendar.YEAR, -1);
        Date lastPosted = today.getTime();
        
        Home filteredHome = CallDetailSupportHelper.get(getContext()).getCallDetailsForSubscriberIDHome(
                                                            getContext(), 
                                                            "1234", 
                                                            start, 
                                                            end, 
                                                            lastPosted);
        
        checkResult(filteredHome, 1);
    }
    
    // PostedDate range will be successful since it is inclusive (remember: End
    // in the PostedDate range check is actually just new Date().
    public void testCallDetail_ByDate_EndBoundary()
    {
        Calendar today = (Calendar) today_.clone();

        today.add(Calendar.DAY_OF_MONTH, -10);
        Date start = today.getTime();
        
        today.add(Calendar.DAY_OF_MONTH, 20);
        Date end =  today.getTime();
        
        today.add(Calendar.YEAR, -1);
        Date lastPosted = today.getTime();
        
        addCallDetail("1", new Date(), new Date());
        
        Home filteredHome = CallDetailSupportHelper.get(getContext()).getCallDetailsForSubscriberIDHome(
                                                            getContext(), 
                                                            "1", 
                                                            start, 
                                                            end, 
                                                            lastPosted);
        
        checkResult(filteredHome, 1);
    }
    
    // PostedDate range will fail on the end date (since I'm posting this into
    // the future).
    public void testCallDetail_ByDate_FailEnd()
    {
        Calendar today = (Calendar) today_.clone();

        today.add(Calendar.DAY_OF_MONTH, -10);
        Date start = today.getTime();
        
        today.add(Calendar.DAY_OF_MONTH, 20);
        Date end =  today.getTime();
        
        today.add(Calendar.YEAR, -1);
        Date lastPosted = today.getTime();
        
        
        // Post it in the future
        today.add(Calendar.YEAR, 2);
        addCallDetail("1", new Date(), today.getTime());
        
        Home filteredHome = CallDetailSupportHelper.get(getContext()).getCallDetailsForSubscriberIDHome(
                                                            getContext(), 
                                                            "1", 
                                                            start, 
                                                            end, 
                                                            lastPosted);
        
        checkResult(filteredHome, 0);
    }
    
    // TRAN == now < start < end (false)
    // start <= POSTED == now (true)
    public void testCallDetail_InvoiceA_FailStart()
    {
        Calendar today = (Calendar)today_.clone();
        
        today.add(Calendar.MILLISECOND, 1);
        Date start = today.getTime();
        
        today.add(Calendar.DAY_OF_MONTH, 10);
        Date end =  today.getTime();
        
        today.add(Calendar.DAY_OF_MONTH, -1);
        Date lastInvoice = today.getTime();
        
        Home filteredHome = CallDetailSupportHelper.get(getContext()).getCallDetailsForSubscriberIDHome(
                                                            getContext(), 
                                                            "1234", 
                                                            start, 
                                                            end, 
                                                            lastInvoice);
        
        checkResult(filteredHome, 0);
    }
    
    // trandate == start
    public void testCallDetail_InvoiceA_StartBoundary()
    {
        Calendar today = (Calendar)today_.clone();
        
        Date start = today.getTime();
        
        today.add(Calendar.DAY_OF_MONTH, 10);
        Date end =  today.getTime();
        
        today.add(Calendar.YEAR, -1);
        Date posted = today.getTime();
        
        Home filteredHome = CallDetailSupportHelper.get(getContext()).getCallDetailsForSubscriberIDHome(
                                                            getContext(), 
                                                            "1234", 
                                                            start, 
                                                            end, 
                                                            posted);
        
        checkResult(filteredHome, 1);
    }
    
    // trandate > end
    public void testCallDetail_InvoiceA_FailEnd()
    {
        Calendar today = (Calendar)today_.clone();
        
        Date end =  today.getTime();
        
        today.add(Calendar.DAY_OF_MONTH, -10);
        Date start = today.getTime();
                
        today.add(Calendar.YEAR, -1);
        Date posted = today.getTime();
        
        Home filteredHome = CallDetailSupportHelper.get(getContext()).getCallDetailsForSubscriberIDHome(
                                                            getContext(), 
                                                            "1234", 
                                                            start, 
                                                            end, 
                                                            posted);
        
        checkResult(filteredHome, 0);
    }
    
    // trandate == (end - 1)
    public void testCallDetail_InvoiceA_EndBoundary()
    {
        Calendar today = (Calendar)today_.clone();
        
        today.add(Calendar.MILLISECOND, 1);
        Date end =  today.getTime();
        
        today.add(Calendar.DAY_OF_MONTH, -10);
        Date start = today.getTime();
        
        today.add(Calendar.YEAR, -1);
        Date posted = today.getTime();
        
        Home filteredHome = CallDetailSupportHelper.get(getContext()).getCallDetailsForSubscriberIDHome(
                                                            getContext(), 
                                                            "1234", 
                                                            start, 
                                                            end, 
                                                            posted);
        
        checkResult(filteredHome, 1);
    }
  
    // TRANDATE < start <= POSTED <= now (true)
    // lastInvoice > POSTED (false)
    public void testCallDetail_InvoiceB_FailPosted()
    {
        Calendar today = (Calendar)today_.clone();
        
        Date posted = today.getTime();
        Date start = today.getTime();
        
        today.add(Calendar.DAY_OF_MONTH, -1);
        Date tran = today.getTime();
        
        addCallDetail("1", tran, posted);
                        
        today.add(Calendar.DAY_OF_MONTH, 5);
        Date lastInvoice = today.getTime();
        
        today.add(Calendar.DAY_OF_MONTH, 20);
        Date end =  today.getTime();
        
        Home filteredHome = CallDetailSupportHelper.get(getContext()).getCallDetailsForSubscriberIDHome(
                                                            getContext(), 
                                                            "1", 
                                                            start, 
                                                            end, 
                                                            lastInvoice);
        
        checkResult(filteredHome, 0);
    }

    // TRANDATE < start <= POSTED <= now (true)
    // lastInvoice == POSTED (true)
    public void testCallDetail_InvoiceB_PostedBoundary()
    {
        Calendar today = (Calendar)today_.clone();
        
        Date posted = today.getTime();
        Date start = today.getTime();
        Date lastInvoice = today.getTime();
        
        today.add(Calendar.DAY_OF_MONTH, -1);
        Date tran = today.getTime();
        
        addCallDetail("1", tran, posted);
                        
        today.add(Calendar.DAY_OF_MONTH, 20);
        Date end =  today.getTime();
        
        Home filteredHome = CallDetailSupportHelper.get(getContext()).getCallDetailsForSubscriberIDHome(
                                                            getContext(), 
                                                            "1", 
                                                            start, 
                                                            end, 
                                                            lastInvoice);
        
        checkResult(filteredHome, 1);
    }
    
    // start <= POSTED <= now (true)
    // lastInvoice <= POSTED (true)
    // TRAN >= start (false)
    // TRAN == end (false, to force InvoiceB)
    public void testCallDetail_InvoiceB_FailStart()
    {
        Calendar today = (Calendar)today_.clone();
        
        Date posted = today.getTime();
        Date lastInvoice = today.getTime();
        
        today.add(Calendar.DAY_OF_MONTH, -1);
        Date tran = today.getTime();
        Date end =  today.getTime();
        Date start = today.getTime();
        
        addCallDetail("1", tran, posted);
        
        
        Home filteredHome = CallDetailSupportHelper.get(getContext()).getCallDetailsForSubscriberIDHome(
                                                            getContext(), 
                                                            "1", 
                                                            start, 
                                                            end, 
                                                            lastInvoice);
        
        checkResult(filteredHome, 0);
    }
    
    // start <= POSTED <= now (true)
    // lastInvoice <= POSTED (true)
    // TRAN < start (true)
    // TRAN == end (false, to force InvoiceB)
    public void testCallDetail_InvoiceB_StartBoundary()
    {
        Calendar today = (Calendar)today_.clone();
        
        Date posted = today.getTime();
        Date lastInvoice = today.getTime();
        
        today.add(Calendar.DAY_OF_MONTH, -1);
        Date start = today.getTime();
        
        today.add(Calendar.DAY_OF_MONTH, -1);
        Date tran = today.getTime();
        Date end =  today.getTime();
        
        
        addCallDetail("1", tran, posted);
        
        
        Home filteredHome = CallDetailSupportHelper.get(getContext()).getCallDetailsForSubscriberIDHome(
                                                            getContext(), 
                                                            "1", 
                                                            start, 
                                                            end, 
                                                            lastInvoice);
        
        checkResult(filteredHome, 1);
    }
    
    public void testCallDetailsInRangeForSubscriberID_AllMatch()
    {
        Collection<CallDetail> results;
        
        Calendar today = (Calendar)today_.clone();
        
        today.add(Calendar.DAY_OF_MONTH, -10);
        Date start = today.getTime();
        today.add(Calendar.DAY_OF_MONTH, 20);
        Date end = today.getTime();
        
        results = CallDetailSupportHelper.get(getContext()).getCallDetailsInRangeForSubscriberID(
                                        getContext(), 
                                        "1234", 
                                        start, 
                                        end);
        
        assertTrue(results.size() == 1);
    }
    
    public void testCallDetailsInRangeForSubscriberID_AllMatch_Boundary()
    {
        Collection<CallDetail> results;
        
        Calendar today = (Calendar)today_.clone();
        
        // Inclusive, so this should still work.
        Date start = today.getTime();
        Date end = today.getTime();
        
        results = CallDetailSupportHelper.get(getContext()).getCallDetailsInRangeForSubscriberID(
                                        getContext(), 
                                        "1234", 
                                        start, 
                                        end);
        
        assertTrue(results.size() == 1);
    }
    
    public void testCallDetailsInRangeForSubscriberID_SubID_NoMatch()
    {
        Collection<CallDetail> results;
        
        Calendar today = (Calendar)today_.clone();
        
        today.add(Calendar.DAY_OF_MONTH, -10);
        Date start = today.getTime();
        today.add(Calendar.DAY_OF_MONTH, 20);
        Date end = today.getTime();
        
        results = CallDetailSupportHelper.get(getContext()).getCallDetailsInRangeForSubscriberID(
                                        getContext(), 
                                        "5", 
                                        start, 
                                        end);
        
        assertTrue(results.size() == 0);
    }
    
    public void testCallDetailsInRangeForSubscriberID_Start_NoMatch()
    {
        Collection<CallDetail> results;
        
        Calendar today = (Calendar)today_.clone();
        
        today.add(Calendar.MILLISECOND, 1);
        Date start = today.getTime();
        today.add(Calendar.DAY_OF_MONTH, 10);
        Date end = today.getTime();
        
        results = CallDetailSupportHelper.get(getContext()).getCallDetailsInRangeForSubscriberID(
                                        getContext(), 
                                        "1234", 
                                        start, 
                                        end);
        
        assertTrue(results.size() == 0);
    }
    
    public void testCallDetailsInRangeForSubscriberID_End_NoMatch()
    {
        Collection<CallDetail> results;
        
        Calendar today = (Calendar)today_.clone();
        
        today.add(Calendar.MILLISECOND, -1);
        Date end = today.getTime();
        today.add(Calendar.DAY_OF_MONTH, -110);
        Date start = today.getTime();
        
        results = CallDetailSupportHelper.get(getContext()).getCallDetailsInRangeForSubscriberID(
                                        getContext(), 
                                        "1234", 
                                        start, 
                                        end);
        
        assertTrue(results.size() == 0);
    }
    
    public void testCallDetailsByBillingCategory_AllMatch()
    {        
        Calendar today = (Calendar)today_.clone();
        
        today.add(Calendar.DAY_OF_MONTH, -10);
        Date start = today.getTime();
        today.add(Calendar.DAY_OF_MONTH, 20);
        Date end = today.getTime();
        
        today.add(Calendar.MONTH, -3);
        Date previousInvoiceDate = today.getTime();
        
        addCallDetailBilling("1", 
                             "2",
                             BillingCategoryEnum.DOMESTIC_INDEX,
                             true,
                             CallTypeEnum.ORIG,
                             SubscriberTypeEnum.POSTPAID);
        
        Home results = CallDetailSupportHelper.get(getContext()).getCallDetailsByBillingCategory(
                                        getContext(), 
                                        "1", 
                                        start, 
                                        end, 
                                        previousInvoiceDate, 
                                        BillingCategoryEnum.DOMESTIC, 
                                        true, 
                                        "2");
        
        checkResult(results, 1);
    }
    
    public void testCallDetailsByBillingCategory_VPN_NoMatch()
    {        
        Calendar today = (Calendar)today_.clone();
        
        today.add(Calendar.DAY_OF_MONTH, -10);
        Date start = today.getTime();
        today.add(Calendar.DAY_OF_MONTH, 20);
        Date end = today.getTime();
        
        today.add(Calendar.MONTH, -3);
        Date previousInvoiceDate = today.getTime();
        
        addCallDetailBilling("1", 
                             "2",
                             BillingCategoryEnum.DOMESTIC_INDEX,
                             false,
                             CallTypeEnum.ORIG,
                             SubscriberTypeEnum.POSTPAID);
        
        Home results = CallDetailSupportHelper.get(getContext()).getCallDetailsByBillingCategory(
                                        getContext(), 
                                        "1", 
                                        start, 
                                        end, 
                                        previousInvoiceDate, 
                                        BillingCategoryEnum.DOMESTIC, 
                                        true, 
                                        "2");
        
        checkResult(results, 0);
    }
    
    public void testCallDetailsByBillingCategory_SubType_NoMatch()
    {        
        Calendar today = (Calendar)today_.clone();
        
        today.add(Calendar.DAY_OF_MONTH, -10);
        Date start = today.getTime();
        today.add(Calendar.DAY_OF_MONTH, 20);
        Date end = today.getTime();
        
        today.add(Calendar.MONTH, -3);
        Date previousInvoiceDate = today.getTime();
        
        addCallDetailBilling("1", 
                             "2",
                             BillingCategoryEnum.DOMESTIC_INDEX,
                             true,
                             CallTypeEnum.ORIG,
                             SubscriberTypeEnum.PREPAID);
        
        Home results = CallDetailSupportHelper.get(getContext()).getCallDetailsByBillingCategory(
                                        getContext(), 
                                        "1", 
                                        start, 
                                        end, 
                                        previousInvoiceDate, 
                                        BillingCategoryEnum.DOMESTIC, 
                                        true, 
                                        "2");
        
        checkResult(results, 0);
    }
    
    public void testCallDetailsByBillingCategory_DomesticCallType_NoMatch()
    {        
        Calendar today = (Calendar)today_.clone();
        
        today.add(Calendar.DAY_OF_MONTH, -10);
        Date start = today.getTime();
        today.add(Calendar.DAY_OF_MONTH, 20);
        Date end = today.getTime();
        
        today.add(Calendar.MONTH, -3);
        Date previousInvoiceDate = today.getTime();
        
        addCallDetailBilling("1", 
                             "2",
                             BillingCategoryEnum.DOMESTIC_INDEX,
                             true,
                             CallTypeEnum.TERM,
                             SubscriberTypeEnum.POSTPAID);
        
        Home results = CallDetailSupportHelper.get(getContext()).getCallDetailsByBillingCategory(
                                        getContext(), 
                                        "1", 
                                        start, 
                                        end, 
                                        previousInvoiceDate, 
                                        BillingCategoryEnum.DOMESTIC, 
                                        true, 
                                        "2");
        
        checkResult(results, 0);
    }
    
    public void testCallDetailsByBillingCategory_CallType_MatchTerm()
    {        
        Calendar today = (Calendar)today_.clone();
        
        today.add(Calendar.DAY_OF_MONTH, -10);
        Date start = today.getTime();
        today.add(Calendar.DAY_OF_MONTH, 20);
        Date end = today.getTime();
        
        today.add(Calendar.MONTH, -3);
        Date previousInvoiceDate = today.getTime();
        
        addCallDetailBilling("1", 
                             "2",
                             BillingCategoryEnum.DATA_INDEX,
                             true,
                             CallTypeEnum.TERM,
                             SubscriberTypeEnum.POSTPAID);
        
        Home results = CallDetailSupportHelper.get(getContext()).getCallDetailsByBillingCategory(
                                        getContext(), 
                                        "1", 
                                        start, 
                                        end, 
                                        previousInvoiceDate, 
                                        BillingCategoryEnum.DATA, 
                                        true, 
                                        "2");
        
        checkResult(results, 1);
    }
    
    public void testCallDetailsByBillingCategory_CallTypeDrop_NoMatch()
    {        
        Calendar today = (Calendar)today_.clone();
        
        today.add(Calendar.DAY_OF_MONTH, -10);
        Date start = today.getTime();
        today.add(Calendar.DAY_OF_MONTH, 20);
        Date end = today.getTime();
        
        today.add(Calendar.MONTH, -3);
        Date previousInvoiceDate = today.getTime();
        
        addCallDetailBilling("1", 
                             "2",
                             BillingCategoryEnum.DATA_INDEX,
                             true,
                             CallTypeEnum.DROPPED_CALL,
                             SubscriberTypeEnum.POSTPAID);
        
        Home results = CallDetailSupportHelper.get(getContext()).getCallDetailsByBillingCategory(
                                        getContext(), 
                                        "1", 
                                        start, 
                                        end, 
                                        previousInvoiceDate, 
                                        BillingCategoryEnum.DATA, 
                                        true, 
                                        "2");
        
        checkResult(results, 0);
    }
    
    public void testGetCallDetail_ID_Match()
    {
        assertTrue(CallDetailSupportHelper.get(getContext()).getCallDetail(getContext(), "1") != null);
    }
    
    public void testGetCallDetail_ID_NoMatch()
    {
        assertTrue(CallDetailSupportHelper.get(getContext()).getCallDetail(getContext(), "100") == null);
    }
        
    private void addCallDetail(String subId, Date tranDate, Date postedDate)
    {
        CallDetail cdr = new CallDetail();
        
        cdr.setId(++idx_);
        cdr.setSubscriberID(subId);
        cdr.setBAN(subId);
        cdr.setTranDate(tranDate);
        cdr.setPostedDate(postedDate);
        
        Home cdrHome = (Home)getContext().get(CallDetailHome.class);
        
        try
        {
            cdrHome.create(getContext(), cdr);
        } 
        catch (HomeException e)
        {
            assertTrue(false);
        }
    }
    
    private void addCallDetailBilling(
                                String subId, 
                                String ban,
                                short billingCat,
                                boolean isVpn,
                                CallTypeEnum callType,
                                SubscriberTypeEnum subType)
    {
        CallDetail cdr = new CallDetail();
        
        cdr.setId(++idx_);
        cdr.setSubscriberID(subId);
        cdr.setBAN(ban);
        cdr.setTranDate(today_.getTime());
        cdr.setPostedDate(today_.getTime());
        cdr.setBillingCategory(billingCat);
        cdr.setCallType(callType);
        cdr.setSubscriberType(subType);
        
        if (isVpn)
        {
            cdr.setVpn_Discount_On(1);
            cdr.setVpn_BAN(ban);
        }
        else
        {
            cdr.setVpn_Discount_On(0);
        }
        
        Home cdrHome = (Home)getContext().get(CallDetailHome.class);
        
        try
        {
            cdrHome.create(getContext(), cdr);
        } 
        catch (HomeException e)
        {
            assertTrue(false);
        }
    }
    
    
    private void checkResult(Home whereHome, int expectedSize)
    {
        try
        {
            assertTrue(whereHome.selectAll().size() == expectedSize);
        } 
        catch (UnsupportedOperationException e)
        {
            e.printStackTrace();
            assertTrue(false);
        } 
        catch (HomeException e)
        {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    private int idx_ = 0;
    private Calendar today_;
}
