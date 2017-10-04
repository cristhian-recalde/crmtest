package com.trilogy.app.crm.numbermgn;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.pin.manager.SubscriberLangProvOperations;

import com.trilogy.app.crm.client.SubscriberLanguageClient;
import com.trilogy.app.crm.client.SubscriberLanguageTestClient;
import com.trilogy.app.crm.client.TestSubscriberLangProv;
import com.trilogy.app.crm.support.MultiLanguageSupport;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


public class TestMsisdnLanguage extends ContextAwareTestCase
{

    public TestMsisdnLanguage(String name)
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
        final TestSuite suite = new TestSuite(TestMsisdnLanguage.class);
        return suite;
    }
    
    /* (non-Javadoc)
     * @see com.redknee.app.crm.unit_test.ContextAwareTestCase#setUp()
     */
    @Override
    protected void setUp() 
    {
        super.setUp();
        
        setupService();

    }
    
    private void setupService()
    {
        getContext().put(SubscriberLangProvOperations.class, new TestSubscriberLangProv());
		getContext().put(SubscriberLanguageClient.class,
		    new SubscriberLanguageTestClient());
    }
    
    /* (non-Javadoc)
     * @see com.redknee.app.crm.unit_test.ContextAwareTestCase#tearDown()
     */
    @Override
    protected void tearDown() 
    {
        super.tearDown();
    }
    
    public void testSetSubscriberLanguage()
    {
        try
        {
            String lang = MultiLanguageSupport.setSubscriberLanguage(getContext(), SPID, SUB_MSISDN, LANGUAGE);  
            assertEquals(LANGUAGE, lang);
        }
        catch (HomeException e)
        {
            fail("Encountered a HomeException when trying to set language of msisdn.  [message=" + e.getMessage() + "]");        
        }                
    }
    
    public void testGetSubscriberLanguage()
    {
        try
        {
            String lang = MultiLanguageSupport.setSubscriberLanguage(getContext(), SPID, SUB_MSISDN, LANGUAGE);
            
            lang = MultiLanguageSupport.getSubscriberLanguage(getContext(), SPID, SUB_MSISDN);
            assertEquals(LANGUAGE, lang);
        }
        catch (HomeException e)
        {
            fail("Encountered a HomeException when trying to get language of msisdn.  [message=" + e.getMessage() + "]");              
        }
        
       
    }
    
    public void testGetSubscriberLanguageWithDefault()
    {
        try
        {
            String lang = MultiLanguageSupport.setSubscriberLanguage(getContext(), SPID, SUB_MSISDN, LANGUAGE);
            lang = MultiLanguageSupport.getSubscriberLanguageWithDefault(getContext(), SPID, SUB_MSISDN);
            assertEquals(LANGUAGE, lang);
            lang = MultiLanguageSupport.getSubscriberLanguageWithDefault(getContext(), SPID, SUB_MSISDN_INVALID);
            if (getContext().get(SubscriberLangProvOperations.class) instanceof TestSubscriberLangProv)
            {
                // i know what is the default language that wiil be returned
                // it is test service I wrote :)
                assertEquals(TestSubscriberLangProv.DEFAULT_LANGUAGE, lang);
            }
            else
            {
                // i don't know what is the default set to return by service
                // but at least i know it must return something
                assertNotNull(lang);
            }
        }
        catch (HomeException e)
        {
            fail("Encountered a HomeException when trying to get language of msisdn.  [message=" + e.getMessage() + "]");
        }
    }
    
    public void testIsSubscriberLanguageUpdateSupported()
    {
        try
        {
            boolean supported = MultiLanguageSupport.isSubscriberLanguageUpdateSupported(getContext(), SPID);
            
            assertEquals(true, supported);
        }
        catch (HomeException e)
        {
            fail("Encountered a HomeException when trying to determine if subscriber language update is supported.  [message=" + e.getMessage() + "]");              
        }
    }
    private static final int SPID = 1;
    private static String SUB_MSISDN = "9991234567";
    private static String SUB_MSISDN_INVALID  = "4168366322";
    private static String LANGUAGE = "en";
}
