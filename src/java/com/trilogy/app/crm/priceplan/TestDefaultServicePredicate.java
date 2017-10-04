/**
 * This JUnit test class is used to test the Default, Optional, Mandatory services and bundle provision
 */
package com.trilogy.app.crm.priceplan;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;

/**
 * @author skushwaha
 * 
 */
public class TestDefaultServicePredicate extends ContextAwareTestCase {

	/**
	 * @param name
	 */
	public TestDefaultServicePredicate(String name) {
		super(name);
	}

	/**
	 * Creates a new suite of Tests for execution. This method is intended to be
	 * invoked by standard JUnit tools (i.e., those that do not provide a
	 * context).
	 * 
	 * @return A new suite of Tests for execution.
	 */
	public static Test suite() {
		return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
	}

	/**
	 * Creates a new suite of Tests for execution. This method is intended to be
	 * invoked by the Redknee Xtest code, which provides the application's
	 * operating context.
	 * 
	 * @param context
	 *            The operating context.
	 * @return A new suite of Tests for execution.
	 */
	public static Test suite(final Context context) {
		setParentContext(context);

		final TestSuite suite = new TestSuite(TestDefaultServicePredicate.class);

		return suite;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setUp() {
		super.setUp();
	}

	/**
	 * {@inheritDoc}
	 */
	public void tearDown() {
		super.tearDown();
	}

	/* Test default services or bundles
	 * Get's price plan and check for all its 
	 * services wheather its a default, mandatory or optional service 
	 */
	public void testServicePreferance() throws Exception {

		PricePlan plan = PricePlanSupport.getPlan(getContext(), 501);
		//assertNull("PricePlan not found.", Long.valueOf(plan.getId()));
		Collection serviceFees = PricePlanSupport.getServiceFees(getContext(),
				plan.getId());
		for (Iterator iter = serviceFees.iterator(); iter.hasNext();) {
			ServiceFee2 fee = (ServiceFee2) iter.next();

			if (fee.getServicePreference() == ServicePreferenceEnum.DEFAULT) {
				assertEquals("SMS SERVICE ID ", 6, fee.getServiceId());
				assertEquals("DEFAULT SERVICES ",
						ServicePreferenceEnum.DEFAULT, fee
								.getServicePreference());
			}
			if (fee.getServicePreference() == ServicePreferenceEnum.MANDATORY) {
				assertEquals("VOICE SERVICE ID ", 5, fee.getServiceId());
				assertEquals("MANDATORY SERVICES ",
						ServicePreferenceEnum.MANDATORY, fee
								.getServicePreference());
			}
			if (fee.getServicePreference() == ServicePreferenceEnum.OPTIONAL) {
				assertEquals("WAP SERVICE ID ", 7, fee.getServiceId());
				assertEquals("OPTIONAL SERVICES ",
						ServicePreferenceEnum.OPTIONAL, fee
								.getServicePreference());
			}
		}
	}

}
