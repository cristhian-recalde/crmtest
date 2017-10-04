package com.trilogy.app.crm.home.generic.unitTest;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.xhome.context.Context;

public class AutoUnadaptAdapterHomeTestCase
extends ContextAwareTestCase
{
	
	public AutoUnadaptAdapterHomeTestCase(String name)
	{
		super(name); 
	}
	

    /**
     * {@inheritDoc}
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(AutoUnadaptAdapterHomeTestCase.class);

        return suite;
    }
    
// remove this package
}
