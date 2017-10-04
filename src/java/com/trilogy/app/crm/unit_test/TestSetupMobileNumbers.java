package com.trilogy.app.crm.unit_test;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.GenericTransientHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.GSMPackageHome;
import com.trilogy.app.crm.bean.GSMPackageTransientHome;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnGroup;
import com.trilogy.app.crm.bean.MsisdnGroupHome;
import com.trilogy.app.crm.bean.MsisdnGroupTransientHome;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnTransientHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.TDMAPackageHome;
import com.trilogy.app.crm.bean.TDMAPackageTransientHome;
import com.trilogy.app.crm.numbermgn.HistoryEvent;
import com.trilogy.app.crm.numbermgn.HistoryEventHome;
import com.trilogy.app.crm.numbermgn.HistoryEventSupport;
import com.trilogy.app.crm.numbermgn.HistoryEventTransientHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryTransientHome;
import com.trilogy.app.crm.numbermgn.TestMsisdnManagement;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.unit_test.utils.TransientSequenceIdentifiedSettingHome;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;
import com.trilogy.app.crm.xhome.home.TransientFieldResettingHome;

public class TestSetupMobileNumbers extends ContextAwareTestCase 
{
    public TestSetupMobileNumbers(String name)
    {
        super(name);
    }

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked
     * by standard JUnit tools (i.e., those that do not provide a context).
     * 
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }
    
    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked
     * by the Redknee Xtest code, which provides the application's operating context.
     * 
     * @param context
     *            The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);
        final TestSuite suite = new TestSuite(TestSetupMobileNumbers.class);
        return suite;
    }
    
    @Override
    public void setUp()
    {
        super.setUp();
        setup(getContext());
        
    }
    
    public static void setup(Context context)
    {
        if (context.getBoolean(TestSetupMobileNumbers.class, true))
        {
            TestSetupIdentifierSequence.setup(context);
            
            context.put(MsisdnHome.class, new TransientFieldResettingHome(context, new AdapterHome(
                    context, 
                    new MsisdnTransientHome(context), 
                    new ExtendedBeanAdapter<com.redknee.app.crm.bean.Msisdn, com.redknee.app.crm.bean.core.Msisdn>(
                            com.redknee.app.crm.bean.Msisdn.class, 
                            com.redknee.app.crm.bean.core.Msisdn.class))));
            context.put(MsisdnGroupHome.class, new TransientFieldResettingHome(context, new MsisdnGroupTransientHome(context)));
            context.put(GSMPackageHome.class, new TransientFieldResettingHome(context, new GSMPackageTransientHome(context)));
            context.put(TDMAPackageHome.class, new TransientFieldResettingHome(context, new TDMAPackageTransientHome(context)));

            Home home = new MsisdnMgmtHistoryTransientHome(context);
            //Have to allow overwriting the records.
            ((GenericTransientHome)((MsisdnMgmtHistoryTransientHome)home).getDelegate()).setFreezeEnabled(false);
            home = new AdapterHome(
                    context, 
                    home, 
                    new ExtendedBeanAdapter<com.redknee.app.crm.numbermgn.MsisdnMgmtHistory, com.redknee.app.crm.bean.core.MsisdnMgmtHistory>(
                            com.redknee.app.crm.numbermgn.MsisdnMgmtHistory.class, 
                            com.redknee.app.crm.bean.core.MsisdnMgmtHistory.class));
            home = new TransientFieldResettingHome(context, home);
            try
            {
                home = new TransientSequenceIdentifiedSettingHome(context, home, MSISDN_HISTORY_MGMT);
            }
            catch(HomeException e)
            {
                fail("failed to setup the MsisdnMgmtHistoryTransientHome");
            }
            /* Removing this Home decorator for now since we should isolate the call that needs pause.  See TestSetupAccountHierarchy.pause(Context)
            home = new HomeProxy(context, home)
            {
                public Object create(Context ctx, Object obj)
                throws HomeException, HomeInternalException
                {
                    Object ret = getDelegate().create(ctx, obj);
                    /* I learned this from TestMsisdnManagement.
                     * Since MsisdnMgmtHistory has primary key of timeStamp and MSISDN, we have to buffer the test time so that the time value has
                     * changed enough to avoid such conflicts while testing:
                     *   HomeException: Can't create(MsisdnMgmtHistory(timestamp: Fri Feb 06 16:05:09 EST 2009, terminalId: 333333333)) because entry already exists.  
                     * The unit tests are more consistently persistent if you wait a few milliseconds to before the next query/update to 
                     * MsisdnMgmtHistory.*/
             /*       
                    LogSupport.debug(ctx, this, "Created MsisdnMgmtHistory record: " + ret.toString());
                    
                    try
                    {
                        Thread.sleep(100); // Too quick and the association will not be ready by the time we start the test.
                    }
                    catch (InterruptedException e)
                    {
                        LogSupport.debug(ctx, this, "Encountered an InterruptedException, this could affect the results " +
                                " of the test, but then again it might not.  We don't stop the test and instead hope that " +
                        " this really long log message will lag the unit test enough to make a difference.");
                    }
                    return ret;
                }
            };*/
            context.put(MsisdnMgmtHistoryHome.class, home);

            setupHistoryEventSupport(context);
            //Prevent setup from overwriting when it is called multiple times.
            context.put(TestSetupAccountHierarchy.class, false);
        }
        else
        {
            LogSupport.debug(context, TestSetupMobileNumbers.class.getName(), 
                    "Skipping TestSetupMobileNumbers.setup since it has already been run.");
        }
    }
    
    /*
     * Moved from TestMsisdnManagement.
     */
    public static void setupHistoryEventSupport(Context ctx)
    {
        try
        {
            ctx.put(HistoryEventSupport.class, new HistoryEventSupport(ctx));
            Home home = new TransientFieldResettingHome(ctx, new HistoryEventTransientHome(ctx));
            ctx.put(HistoryEventHome.class, home);
            
            home.create(ctx,new HistoryEvent(HistoryEventSupport.SUBID_MOD, "SubscriberModificationEvent"));
            
        }
        catch (Exception e)
        {
             new MajorLogMsg(TestMsisdnManagement.class, "Setup Error: " + e.getMessage(), e).log(ctx);
        }
    }
    
    public static void setupUnitTestPoolMsisdn(Context ctx)
    {
        //Create a Pooled MSISDN Group
        Home mgHome = (Home) ctx.get(MsisdnGroupHome.class);
        MsisdnGroup group = null;
        try
        {
            group = (MsisdnGroup) mgHome.find(ctx, Integer.valueOf(DEFAULT_POOLED_MSISDN_GROUP));
            if (group == null)
            {
                group = new MsisdnGroup();
                group.setId(DEFAULT_POOLED_MSISDN_GROUP);
                group.setName("Unit Test Pooled Group");
                group.setTechnology(TechnologyEnum.ANY);
                mgHome.create(group);
            }
        }
        catch (HomeException e)
        {
            new DebugLogMsg(TestSetupMobileNumbers.class, "Failed to create Pooled Msisdn Group due to " + e.getMessage(), e).log(ctx);
        }
        
        //Set this Msisdn Group in the SPID as the default  
        if (group != null)
        {
            try
            {
                Home sHome = (Home) ctx.get(CRMSpidHome.class);
                CRMSpid spid  = (CRMSpid) sHome.find(ctx, Integer.valueOf(TestSetupAccountHierarchy.SPID_ID));
                if (spid != null)
                {
                    spid.setGroupPooledMSISDNGroup(DEFAULT_POOLED_MSISDN_GROUP);
                    sHome.store(ctx, spid);
                }
                else
                {
                    throw new HomeException("Default Service Provider is not in the system."); 
                }
            }
            catch (HomeException e)
            {
                new DebugLogMsg(TestSetupMobileNumbers.class, "Failed to update SPID with default Pool Msisdn Group due to " + 
                        e.getMessage(), e).log(ctx);
            }
        }
    }
    
    public static void createPoolMsisdn(Context ctx,
            final String mobileNumber)
    {
        Msisdn msisdn = new Msisdn();
        msisdn.setMsisdn(mobileNumber);
        msisdn.setGroup(DEFAULT_POOLED_MSISDN_GROUP);
        msisdn.setTechnology(TechnologyEnum.ANY);
        msisdn.setSubscriberType(SubscriberTypeEnum.HYBRID);
        msisdn.setSpid(TestSetupAccountHierarchy.SPID_ID);
        
        try
        {
            Home mHome = (Home) ctx.get(MsisdnHome.class);
            mHome.create(msisdn);
        }
        catch (HomeException e)
        {
            new DebugLogMsg(TestSetupMobileNumbers.class, "Failed to create Pool Msisdn due to " + e.getMessage(), e).log(ctx);
        }
    }
    
    //  INHERIT
    @Override
    public void tearDown()  
    {
        //tear down here
        tearDown(getContext());
        
        super.tearDown();
    }
    
    public static void tearDown(Context context)
    {
        try
        {
            ((Home)context.get(MsisdnHome.class)).removeAll();
            ((Home)context.get(GSMPackageHome.class)).removeAll();
            ((Home)context.get(TDMAPackageHome.class)).removeAll();
            ((Home)context.get(MsisdnMgmtHistoryHome.class)).removeAll();
        }
        catch (Exception e)
        {
            //Do nothing. Test is over.
        }
    }
    
    public void testSetup()
    {
        assertNotNull(getContext().get(MsisdnHome.class));
        assertNotNull(getContext().get(MsisdnGroupHome.class));
        assertNotNull(getContext().get(GSMPackageHome.class));
        assertNotNull(getContext().get(TDMAPackageHome.class));
        assertNotNull(getContext().get(MsisdnMgmtHistoryHome.class));
    }
    
    public final static IdentifierEnum MSISDN_HISTORY_MGMT = new IdentifierEnum(99, "MSISDN_HISTORY_MGMT", "Msisdn history mgmt");

    public static int DEFAULT_POOLED_MSISDN_GROUP = 99;
}
