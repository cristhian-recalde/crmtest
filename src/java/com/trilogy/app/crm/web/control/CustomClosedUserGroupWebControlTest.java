package com.trilogy.app.crm.web.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

public class CustomClosedUserGroupWebControlTest extends ContextAwareTestCase
{
	public CustomClosedUserGroupWebControlTest(final String name)
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

        final TestSuite suite = new TestSuite(CustomClosedUserGroupWebControlTest.class);

        return suite;
    }


    // INHERIT
    public void setUp()
    {
        super.setUp();
        
        // Set up the Test Subscriber.
    }


    // INHERIT
    public void tearDown()
    {
        // Tear down the Test Subscriber.
        
        super.tearDown();
    }
    
    public void testGetEligibleSubsFromCollection() throws Exception
    {
    	Collection subs = new ArrayList();
    
    	long id = 1;
    	String activeSub = ""; 
    	
    	{
    		Subscriber sas = new Subscriber();
    		activeSub = "" + id++; 
    		sas.setId(activeSub);
    		sas.setState(SubscriberStateEnum.ACTIVE);
    		subs.add(sas);
    		
    	}
    	
    	{
    		Subscriber sas = new Subscriber();
    		sas.setId("" + id++);
    		sas.setState(SubscriberStateEnum.PENDING);
    		subs.add(sas);
    	}
    	
    	{
    		Subscriber sas = new Subscriber();
    		sas.setId("" + id++);
    		sas.setState(SubscriberStateEnum.MOVED);
    		subs.add(sas);
    	}
    	
    	{
    		Subscriber sas = new Subscriber();
    		sas.setId("" + id++);
    		sas.setState(SubscriberStateEnum.INACTIVE);
    		subs.add(sas);
    	}
    	
    	CustomClosedUserGroupWebControl wc = new CustomClosedUserGroupWebControl();
    	List cl = (List)wc.getEligibleSubsFromCollection(getContext(), subs);
    	assertTrue(cl.size() == 1);
    	assertTrue(((Subscriber)cl.get(0)).getId().equals(activeSub));
    	
    }


}
