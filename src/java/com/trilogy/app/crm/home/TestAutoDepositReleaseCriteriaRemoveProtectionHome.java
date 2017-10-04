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

package com.trilogy.app.crm.home;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.AutoDepositReleaseConfigurationEnum;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteria;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteriaHome;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteriaTransientHome;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CRMSpidTransientHome;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.CreditCategoryHome;
import com.trilogy.app.crm.bean.CreditCategoryTransientHome;
import com.trilogy.app.crm.bean.DunningConfigurationEnum;
import com.trilogy.app.crm.bean.ReleaseScheduleConfigurationEnum;
import com.trilogy.app.crm.exception.RemoveException;
import com.trilogy.app.crm.home.pipelineFactory.AutoDepositReleaseCriteriaHomePipelineFactory;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

/**
 * Unit test for AutoDepositReleaseCriteriaRemoveProtectionHome.
 *
 * @author cindy.wong@redknee.com
 */
public class TestAutoDepositReleaseCriteriaRemoveProtectionHome extends ContextAwareTestCase
{

    /**
     *
     */
    private static final String CRITERIA_SHOULD_BE_REMOVED = "Criteria should be removed";

    /**
     *
     */
    private static final String CRITERIA_SHOULD_NOT_BE_REMOVED = "Criteria should not be removed";

    /**
     * Creates a new test case with the given name.
     *
     * @param name
     *            Name of the test case.
     */
    public TestAutoDepositReleaseCriteriaRemoveProtectionHome(final String name)
    {
        super(name);
    }

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked by standard JUnit tools (i.e.,
     * those that do not provide a context).
     *
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked by the Redknee Xtest code,
     * which provides the application's operating context.
     *
     * @param context
     *            The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestAutoDepositReleaseCriteriaRemoveProtectionHome.class);
        return suite;
    }

    /**
     * Sets up the environment for testing.
     *
     * @see com.redknee.app.crm.unit_test.ContextAwareTestCase#setUp()
     */
    @Override
    protected void setUp()
    {
        super.setUp();

        // create AutoDepositReleaseCriteria home.
        Home criteriaHome = new AutoDepositReleaseCriteriaTransientHome(getContext());
        criteriaHome = AutoDepositReleaseCriteriaHomePipelineFactory.instance().addDecorators(getContext(),
            criteriaHome);
        getContext().put(AutoDepositReleaseCriteriaHome.class, criteriaHome);

        // create SPID home
        final Home spidHome = new CRMSpidTransientHome(getContext());
        getContext().put(CRMSpidHome.class, spidHome);

        // create credit category home
        final Home creditCategoryHome = new CreditCategoryTransientHome(getContext());
        getContext().put(CreditCategoryHome.class, creditCategoryHome);
    }

    /**
     * Creates an AutoDepositReleaseCriteria and add it to the home.
     *
     * @param home
     *            Home to add the criteria to.
     * @param id
     *            Identifier of the criteria.
     * @throws HomeException
     *             Thrown by Home.
     */
    private void createCriteria(final Home home, final long id) throws HomeException
    {
        final AutoDepositReleaseCriteria criteria = new AutoDepositReleaseCriteria();
        criteria.setIdentifier(id);
        criteria.setReleaseSchedule(1);
        criteria.setReleaseScheduleConfiguration(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH);
        criteria.setServiceDuration(1);
        try
        {
            home.create(getContext(), criteria);
        }
        catch (HomeException exception)
        {
            home.store(getContext(), criteria);
        }
    }

    /**
     * Test method for {@link com.redknee.app.crm.home.AutoDepositReleaseCriteriaRemoveProtectionHome#remove}.
     *
     * @throws HomeException
     *             Thrown by Home.
     */
    public void testRemoveCriteriaNotInUse() throws HomeException
    {
        // create credit category not using auto deposit release criteria
        int code = 1;
        final int numCategories = 5;
        for (int i = 0; i < numCategories; i++)
        {
            final CreditCategory creditCategory = new CreditCategory();
            creditCategory.setCode(code);
            creditCategory.setAutoDepositReleaseConfiguration(DunningConfigurationEnum.SERVICE_PROVIDER);
            ((Home) getContext().get(CreditCategoryHome.class)).create(creditCategory);
            code++;
        }

        // create SPIDs not using auto deposit release criteria
        int spid = 1;
        final int numSpids = 5;
        for (int i = 0; i < numSpids; i++)
        {
            final CRMSpid serviceProvider = new CRMSpid();
            serviceProvider.setSpid(spid);
            serviceProvider.setId(spid);
            serviceProvider.setUseAutoDepositRelease(AutoDepositReleaseConfigurationEnum.NO);
            ((Home) getContext().get(CRMSpidHome.class)).create(serviceProvider);
            spid++;
        }

        // try removing a criteria not in use
        long criteriaId = 1L;
        {
            createCriteria((Home) getContext().get(AutoDepositReleaseCriteriaHome.class), criteriaId);

            final AutoDepositReleaseCriteria criteria = (AutoDepositReleaseCriteria) ((Home) getContext().get(
                    AutoDepositReleaseCriteriaHome.class)).find(getContext(), Long.valueOf(criteriaId));
            try
            {
                ((Home) getContext().get(AutoDepositReleaseCriteriaHome.class)).remove(getContext(), criteria);
            }
            catch (RemoveException exception)
            {
                fail(CRITERIA_SHOULD_BE_REMOVED);
            }
        }

        // create 5 SPID using the 1st criteria
        for (int i = 0; i < numSpids; i++)
        {
            final CRMSpid serviceProvider = new CRMSpid();
            serviceProvider.setSpid(spid);
            serviceProvider.setId(spid);
            serviceProvider.setUseAutoDepositRelease(AutoDepositReleaseConfigurationEnum.YES);
            serviceProvider.setAutoDepositReleaseCriteria(criteriaId);
            ((Home) getContext().get(CRMSpidHome.class)).create(serviceProvider);
            spid++;
        }

        // try removing the 1st criteria
        {
            final AutoDepositReleaseCriteria criteria = (AutoDepositReleaseCriteria) ((Home) getContext().get(
                    AutoDepositReleaseCriteriaHome.class)).find(getContext(), Long.valueOf(criteriaId));
            try
            {
                ((Home) getContext().get(AutoDepositReleaseCriteriaHome.class)).remove(getContext(), criteria);
                fail(CRITERIA_SHOULD_NOT_BE_REMOVED);
            }
            catch (RemoveException exception)
            {
                // empty
            }
        }

        // create a 2nd criteria and try removing it
        criteriaId++;
        {
            createCriteria((Home) getContext().get(AutoDepositReleaseCriteriaHome.class), criteriaId);

            final AutoDepositReleaseCriteria criteria = (AutoDepositReleaseCriteria) ((Home) getContext().get(
                    AutoDepositReleaseCriteriaHome.class)).find(getContext(), Long.valueOf(criteriaId));
            try
            {
                ((Home) getContext().get(AutoDepositReleaseCriteriaHome.class)).remove(getContext(), criteria);
            }
            catch (RemoveException exception)
            {
                fail(CRITERIA_SHOULD_BE_REMOVED);
            }
        }
    }

    /**
     * Test method for {@link com.redknee.app.crm.home.AutoDepositReleaseCriteriaRemoveProtectionHome#remove}.
     *
     * @throws HomeException
     *             Thrown by Home.
     */
    public void testRemoveCriteriaInUseBySpid() throws HomeException
    {
        final int spid = 1;
        final long criteriaId = 1;

        // create a criteria
        createCriteria((Home) getContext().get(AutoDepositReleaseCriteriaHome.class), criteriaId);

        // create a SPID which uses the criteria
        {
            final CRMSpid serviceProvider = new CRMSpid();
            serviceProvider.setSpid(spid);
            serviceProvider.setId(spid);
            serviceProvider.setUseAutoDepositRelease(AutoDepositReleaseConfigurationEnum.YES);
            serviceProvider.setAutoDepositReleaseCriteria(criteriaId);
            ((Home) getContext().get(CRMSpidHome.class)).create(serviceProvider);
        }

        // try removing criteria
        {
            final AutoDepositReleaseCriteria criteria = (AutoDepositReleaseCriteria) ((Home) getContext().get(
                    AutoDepositReleaseCriteriaHome.class)).find(getContext(), Long.valueOf(criteriaId));
            try
            {
                ((Home) getContext().get(AutoDepositReleaseCriteriaHome.class)).remove(getContext(), criteria);
                fail(CRITERIA_SHOULD_NOT_BE_REMOVED);
            }
            catch (RemoveException exception)
            {
                // empty
            }
        }

        // change the criteria of the spid
        {
            final CRMSpid serviceProvider = (CRMSpid) ((Home) getContext().get(CRMSpidHome.class)).find(getContext(),
                    Integer.valueOf(spid));
            serviceProvider.setAutoDepositReleaseCriteria(0);
            ((Home) getContext().get(CRMSpidHome.class)).store(serviceProvider);
        }

        // try removing criteria again
        {
            final AutoDepositReleaseCriteria criteria = (AutoDepositReleaseCriteria) ((Home) getContext().get(
                    AutoDepositReleaseCriteriaHome.class)).find(getContext(), Long.valueOf(criteriaId));
            try
            {
                ((Home) getContext().get(AutoDepositReleaseCriteriaHome.class)).remove(getContext(), criteria);
            }
            catch (RemoveException exception)
            {
                fail(CRITERIA_SHOULD_BE_REMOVED);
            }
        }

        // create a criteria again
        createCriteria((Home) getContext().get(AutoDepositReleaseCriteriaHome.class), criteriaId);

        // disable auto deposit release
        {
            final CRMSpid serviceProvider = (CRMSpid) ((Home) getContext().get(CRMSpidHome.class)).find(getContext(),
                    Integer.valueOf(spid));
            serviceProvider.setAutoDepositReleaseCriteria(criteriaId);
            serviceProvider.setUseAutoDepositRelease(AutoDepositReleaseConfigurationEnum.NO);
            ((Home) getContext().get(CRMSpidHome.class)).store(serviceProvider);
        }

        // try removing criteria again
        {
            final AutoDepositReleaseCriteria criteria = (AutoDepositReleaseCriteria) ((Home) getContext().get(
                    AutoDepositReleaseCriteriaHome.class)).find(getContext(), Long.valueOf(criteriaId));
            try
            {
                ((Home) getContext().get(AutoDepositReleaseCriteriaHome.class)).remove(getContext(), criteria);
            }
            catch (RemoveException exception)
            {
                fail(CRITERIA_SHOULD_BE_REMOVED);
            }
        }
    }

    /**
     * Test method for {@link com.redknee.app.crm.home.AutoDepositReleaseCriteriaRemoveProtectionHome#remove}.
     *
     * @throws HomeException
     *             Thrown by Home.
     */
    public void testRemoveCriteriaInUseByCreditCategory() throws HomeException
    {
        final int spid = 1;
        final long spidCriteriaId = 1;
        final long creditCategoryCriteriaId = 2;
        final int creditCategoryCode = 1;

        // create SPID criteria
        createCriteria((Home) getContext().get(AutoDepositReleaseCriteriaHome.class), spidCriteriaId);

        // create a SPID which uses the criteria
        {
            final CRMSpid serviceProvider = new CRMSpid();
            serviceProvider.setSpid(spid);
            serviceProvider.setId(spid);
            serviceProvider.setUseAutoDepositRelease(AutoDepositReleaseConfigurationEnum.YES);
            serviceProvider.setAutoDepositReleaseCriteria(spidCriteriaId);
            ((Home) getContext().get(CRMSpidHome.class)).create(serviceProvider);
        }

        // create a credit category criteria
        createCriteria((Home) getContext().get(AutoDepositReleaseCriteriaHome.class), creditCategoryCriteriaId);

        // create a credit category which uses custom criteria
        {
            final CreditCategory creditCategory = new CreditCategory();
            creditCategory.setCode(creditCategoryCode);
            creditCategory.setAutoDepositReleaseConfiguration(DunningConfigurationEnum.CUSTOM);
            creditCategory.setAutoDepositReleaseCriteria(creditCategoryCriteriaId);
            ((Home) getContext().get(CreditCategoryHome.class)).create(creditCategory);
        }

        // try removing credit category criteria
        {
            final AutoDepositReleaseCriteria criteria = (AutoDepositReleaseCriteria) ((Home) getContext().get(
                    AutoDepositReleaseCriteriaHome.class)).find(getContext(), Long.valueOf(creditCategoryCriteriaId));
            try
            {
                ((Home) getContext().get(AutoDepositReleaseCriteriaHome.class)).remove(getContext(), criteria);
                fail(CRITERIA_SHOULD_NOT_BE_REMOVED);
            }
            catch (RemoveException exception)
            {
                // empty
            }
        }

        // change the criteria of the credit category
        {
            final CreditCategory creditCategory = (CreditCategory) ((Home) getContext().get(CreditCategoryHome.class))
                .find(getContext(), Integer.valueOf(creditCategoryCode));
            creditCategory.setAutoDepositReleaseCriteria(0);
            ((Home) getContext().get(CreditCategoryHome.class)).store(creditCategory);
        }

        // try removing criteria
        {
            final AutoDepositReleaseCriteria criteria = (AutoDepositReleaseCriteria) ((Home) getContext().get(
                    AutoDepositReleaseCriteriaHome.class)).find(getContext(), Long.valueOf(creditCategoryCriteriaId));
            try
            {
                ((Home) getContext().get(AutoDepositReleaseCriteriaHome.class)).remove(getContext(), criteria);
            }
            catch (RemoveException exception)
            {
                fail(CRITERIA_SHOULD_BE_REMOVED);
            }
        }

        // create a credit category criteria again
        createCriteria((Home) getContext().get(AutoDepositReleaseCriteriaHome.class), creditCategoryCriteriaId);

        // change the credit category to not use custom criteria
        {
            final CreditCategory creditCategory = (CreditCategory) ((Home) getContext().get(CreditCategoryHome.class))
                .find(getContext(), Integer.valueOf(creditCategoryCode));
            creditCategory.setAutoDepositReleaseConfiguration(DunningConfigurationEnum.SERVICE_PROVIDER);
            creditCategory.setAutoDepositReleaseCriteria(creditCategoryCriteriaId);
            ((Home) getContext().get(CreditCategoryHome.class)).store(creditCategory);
        }

        // try removing criteria
        {
            final AutoDepositReleaseCriteria criteria = (AutoDepositReleaseCriteria) ((Home) getContext().get(
                    AutoDepositReleaseCriteriaHome.class)).find(getContext(), Long.valueOf(creditCategoryCriteriaId));
            try
            {
                ((Home) getContext().get(AutoDepositReleaseCriteriaHome.class)).remove(getContext(), criteria);
            }
            catch (RemoveException exception)
            {
                fail(CRITERIA_SHOULD_BE_REMOVED);
            }
        }
    }
}
