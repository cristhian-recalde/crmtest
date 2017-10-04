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

package com.trilogy.app.crm.subscriber.state;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountTransientHome;
import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountCategoryHome;
import com.trilogy.app.crm.bean.AccountCategoryTransientHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.state.EnumStateTransitionSupport;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * JUnit test for subscriber state transition validation.
 *
 * @author cindy.wong@redknee.com
 * @since 13-Mar-08
 */
public class SubscriberStateTransitionSupportTest extends ContextAwareTestCase
{

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked
     * by standard JUnit tools (i.e., those that do not provide a context).
     *
     * @return A new suite of Tests for execution
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
     *            The operating context
     * @return A new suite of Tests for execution
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(SubscriberStateTransitionSupportTest.class);

        return suite;
    }


    /**
     * Create a new instance of <code>SubscriberStateTransitionSupportTest</code>.
     *
     * @param name
     *            Unit test name.
     */
    public SubscriberStateTransitionSupportTest(final String name)
    {
        super(name);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp()
    {
        super.setUp();

        // set up system feature configuration bean
        final SysFeatureCfg systemFeatureConfig = new SysFeatureCfg();
        getContext().put(SysFeatureCfg.class, systemFeatureConfig);

        // set up account types
        final AccountCategoryHome accountTypeHome = new AccountCategoryTransientHome(getContext());

        final AccountCategory individualType = new AccountCategory();
        individualType.setIdentifier(1);
        individualType.setName("Individual");

        final AccountCategory nonIndividualType = new AccountCategory();
        nonIndividualType.setIdentifier(2);
        nonIndividualType.setName("Non-Individual");

        try
        {
            accountTypeHome.create(getContext(), individualType);
            accountTypeHome.create(getContext(), nonIndividualType);
        }
        catch (final HomeException exception)
        {
            fail("Cannot create account types");
        }
        getContext().put(AccountCategoryHome.class, accountTypeHome);

        // set up account home
        final AccountHome home = new AccountTransientHome(getContext());

        this.individualResponsibleAccount_ = new Account();
        this.individualResponsibleAccount_.setBAN("123");
        this.individualResponsibleAccount_.setType(individualType.getIdentifier());
        this.individualResponsibleAccount_.setResponsible(true);
        this.individualResponsibleAccount_.setSystemType(SubscriberTypeEnum.HYBRID);

        this.responsibleAccount_ = new Account();
        this.responsibleAccount_.setBAN("456");
        this.responsibleAccount_.setType(nonIndividualType.getIdentifier());
        this.responsibleAccount_.setResponsible(true);
        this.responsibleAccount_.setSystemType(SubscriberTypeEnum.HYBRID);

        this.individualNonResponsibleAccount_ = new Account();
        this.individualNonResponsibleAccount_.setBAN("012");
        this.individualNonResponsibleAccount_.setType(individualType.getIdentifier());
        this.individualNonResponsibleAccount_.setParentBAN(this.responsibleAccount_.getBAN());
        this.individualNonResponsibleAccount_.setResponsible(false);
        this.individualNonResponsibleAccount_.setSystemType(SubscriberTypeEnum.HYBRID);

        this.nonResponsibleAccount_ = new Account();
        this.nonResponsibleAccount_.setBAN("789");
        this.nonResponsibleAccount_.setType(nonIndividualType.getIdentifier());
        this.nonResponsibleAccount_.setParentBAN(this.responsibleAccount_.getBAN());
        this.nonResponsibleAccount_.setResponsible(false);
        this.nonResponsibleAccount_.setSystemType(SubscriberTypeEnum.HYBRID);

        try
        {
            home.create(getContext(), this.individualResponsibleAccount_);
            home.create(getContext(), this.responsibleAccount_);
            home.create(getContext(), this.nonResponsibleAccount_);
            home.create(getContext(), this.individualNonResponsibleAccount_);
        }
        catch (final HomeException exception)
        {
            fail("Cannot create accounts");
        }
        getContext().put(AccountHome.class, home);
    }


    /**
     * Enables true prepaid.
     */
    protected void enableTruePrepaid()
    {
        final SysFeatureCfg config = (SysFeatureCfg) getContext().get(SysFeatureCfg.class);
        getContext().put(SysFeatureCfg.class, config);
    }


    /**
     * Enables hybrid prepaid.
     */
    protected void enableHybridPrepaid()
    {
        final SysFeatureCfg cfg = (SysFeatureCfg) getContext().get(SysFeatureCfg.class);
        cfg.setCreatePrepaidInActiveState(true);
        cfg.setAllowPrepaidToBeCreatedInPending(true);
        cfg.setUnExpirablePrepaid(true);
        getContext().put(SysFeatureCfg.class, cfg);
    }


    /**
     * Test state transition.
     *
     * @param subscriber
     *            Subscriber.
     * @param state
     *            New state.
     * @param expected
     *            Expected result.
     * @param manual
     *            whether this is a manual transition
     */
    protected void testTransition(final Subscriber subscriber, final SubscriberStateEnum state, final boolean expected,
        final boolean manual)
    {
        final StringBuilder sb = new StringBuilder();
        final SubscriberStateEnum startState = subscriber.getState();
        if (manual)
        {
            sb.append("manual ");
        }
        sb.append("state transition from ");
        sb.append(startState.getIndex());
        sb.append(":");
        sb.append(startState.getDescription());
        sb.append(" to ");
        sb.append(state.getIndex());
        sb.append(":");
        sb.append(state.getDescription());
        sb.append(" is");
        if (!expected)
        {
            sb.append(" not");
        }
        sb.append(" allowed for ");
        sb.append(subscriber.getSubscriberType().getDescription());
        sb.append(" subscribers");

        boolean result;
        final EnumStateTransitionSupport support = SubscriberStateTransitionSupport.instance(getContext(), subscriber);
        if (manual)
        {
            result = support.isManualStateTransitionAllowed(getContext(), subscriber, state);
        }
        else
        {
            result = support.isStateTransitionAllowed(getContext(), subscriber, state);
        }
        assertEquals(sb.toString(), expected, result);
    }


    /**
     * Test state transitions.
     *
     * @param subscriber
     *            Subscriber.
     * @param map
     *            Map of start state, end state, and expected status.
     * @param manual
     *            Whether to test manual state transition.
     */
    protected void testTransitions(final Subscriber subscriber,
        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map, final boolean manual)
    {
        final EnumStateTransitionSupport stateTransitionSupport = SubscriberStateTransitionSupport.instance(
            getContext(), subscriber);

        fillInStateTransitionMap(map);

        for (final Map.Entry<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> outerEntry : map.entrySet())
        {
            final SubscriberStateEnum startState = outerEntry.getKey();
            subscriber.setState(startState);
            final Map<SubscriberStateEnum, Boolean> transitionMap = outerEntry.getValue();

            final Collection possibleStates;

            if (manual)
            {
                possibleStates = stateTransitionSupport.getPossibleManualStateCollection(getContext(), subscriber);
            }
            else
            {
                possibleStates = stateTransitionSupport.getPossibleStateCollection(getContext(), subscriber);
            }

            for (final Map.Entry<SubscriberStateEnum, Boolean> entry : transitionMap.entrySet())
            {
                // test using the isStateTransitionAllowed() /
                // isManualStateTransitionAllowed() method
                testTransition(subscriber, entry.getKey(), entry.getValue().booleanValue(), manual);

                final StringBuilder sb = new StringBuilder();
                sb.append(entry.getKey());
                sb.append(" is ");
                if (entry.getValue().booleanValue())
                {
                    sb.append("a valid");
                }
                else
                {
                    sb.append("an invalid");
                }
                sb.append(" possible state to transition from ");
                sb.append(startState);

                // test using getPossibleStateCollection() /
                // getPossibleManualStateCollection() method
                assertEquals(sb.toString(), entry.getValue().booleanValue(), possibleStates.contains(entry.getKey()));
            }
        }
    }


    /**
     * Fill in the missing state transitions -- assume all to be invalid, expect for
     * transitioning from an existing start state to itself.
     *
     * @param map
     *            State transition map.
     */
    private void fillInStateTransitionMap(final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map)
    {
        // other states are invalid
        for (final Map.Entry<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> entry : map.entrySet())
        {
            final SubscriberStateEnum state = entry.getKey();
            final Map<SubscriberStateEnum, Boolean> endStateMap = entry.getValue();
            for (final Iterator it = SubscriberStateEnum.COLLECTION.iterator(); it.hasNext();)
            {
                final SubscriberStateEnum endState = (SubscriberStateEnum) it.next();
                if (!endStateMap.containsKey(endState))
                {
                    endStateMap.put(endState, Boolean.FALSE);
                }
            }
            endStateMap.put(state, Boolean.TRUE);
        }

        for (final Iterator it = SubscriberStateEnum.COLLECTION.iterator(); it.hasNext();)
        {
            final SubscriberStateEnum state = (SubscriberStateEnum) it.next();
            if (!map.containsKey(state))
            {
                final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
                for (final Iterator it2 = SubscriberStateEnum.COLLECTION.iterator(); it2.hasNext();)
                {
                    final SubscriberStateEnum endState = (SubscriberStateEnum) it2.next();
                    endStateMap.put(endState, Boolean.FALSE);
                }
                map.put(state, endStateMap);
            }
        }
    }


    /**
     * Test for allowed manual state transitions for individual responsible true prepaid
     * subscribers.
     */
    public void testIndividualResponsibleTruePrepaidManualStateTransition()
    {
        enableTruePrepaid();

        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber.setId("4000");
        subscriber.setBAN(this.individualResponsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();

        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.AVAILABLE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from available
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.AVAILABLE, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.LOCKED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from barred
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.LOCKED, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from expired
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.EXPIRED, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, true);
    }


    /**
     * Test for allowed manual state transitions for individual non-responsible true
     * prepaid subscribers.
     */
    public void testIndividualNonResponsibleTruePrepaidManualStateTransition()
    {
        enableTruePrepaid();
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber.setId("4001");
        subscriber.setBAN(this.individualNonResponsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();

        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.AVAILABLE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from available
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.AVAILABLE, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.LOCKED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from barred
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.LOCKED, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from expired
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.EXPIRED, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, true);
    }


    /**
     * Test for allowed manual state transitions for non-individual, responsible true
     * prepaid subscribers.
     */
    public void testResponsibleTruePrepaidManualStateTransition()
    {
        enableTruePrepaid();
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber.setId("4002");
        subscriber.setBAN(this.responsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();
        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.AVAILABLE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from available
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.AVAILABLE, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.LOCKED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from barred
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.LOCKED, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from expired
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.EXPIRED, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, true);

    }


    /**
     * Test for allowed manual state transitions for non-individual, non-responsible true
     * prepaid subscribers.
     */
    public void testNonResponsibleTruePrepaidManualStateTransition()
    {
        enableTruePrepaid();
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber.setId("4003");
        subscriber.setBAN(this.nonResponsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();
        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.AVAILABLE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from available
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.AVAILABLE, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.LOCKED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from barred
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.LOCKED, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from expired
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.EXPIRED, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, true);

    }


    /**
     * Test for allowed state transitions for individual responsible hybrid prepaid
     * subscribers.
     */
    public void testIndividualResponsibleHybridPrepaidManualStateTransition()
    {
        enableHybridPrepaid();

        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber.setId("5000");
        subscriber.setBAN(this.individualResponsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();

        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, true);
    }


    /**
     * Test for allowed manual state transitions for individual non-responsible hybrid
     * prepaid subscribers.
     */
    public void testIndividualNonResponsibleHybridPrepaidManualStateTransition()
    {
        enableHybridPrepaid();
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber.setId("5001");
        subscriber.setBAN(this.individualNonResponsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();
        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from closed
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, true);
    }


    /**
     * Test for allowed manual state transitions for non-individual, responsible hybrid
     * prepaid subscribers.
     */
    public void testResponsibleHybridPrepaidManualStateTransition()
    {
        enableHybridPrepaid();
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber.setId("5002");
        subscriber.setBAN(this.responsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();

        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, true);
    }


    /**
     * Test for allowed manual state transitions for non-individual, non-responsible
     * hybrid prepaid subscribers.
     */
    public void testNonResponsibleHybridPrepaidManualStateTransition()
    {
        enableHybridPrepaid();
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber.setId("5003");
        subscriber.setBAN(this.nonResponsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();
        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, true);
    }


    /**
     * Test for allowed manual state transitions for individual responsible postpaid
     * subscribers.
     */
    public void testIndividualResponsiblePostpaidManualStateTransition()
    {
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.POSTPAID);
        subscriber.setId("6000");
        subscriber.setBAN(this.individualResponsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();

        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.PROMISE_TO_PAY, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from warned
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.NON_PAYMENT_WARN, endStateMap);
        }

        // from dunned
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.NON_PAYMENT_SUSPENDED, endStateMap);
        }

        // from in-arrears
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.IN_ARREARS, endStateMap);
        }

        // from in-collection
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.IN_COLLECTION, endStateMap);
        }

        // from promise-to-pay
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.PROMISE_TO_PAY, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, true);
    }


    /**
     * Test for allowed manual state transitions for individual non-responsible postpaid
     * subscribers.
     */
    public void testIndividualNonResponsiblePostpaidManualStateTransition()
    {
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.POSTPAID);
        subscriber.setId("6001");
        subscriber.setBAN(this.individualNonResponsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();

        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from warned
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.NON_PAYMENT_WARN, endStateMap);
        }

        // from dunned
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.NON_PAYMENT_SUSPENDED, endStateMap);
        }

        // from in-arrears
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.IN_ARREARS, endStateMap);
        }

        // from in-collection
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.IN_COLLECTION, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, true);
    }


    /**
     * Test for allowed manual state transitions for non-individual, responsible postpaid
     * subscribers.
     */
    public void testResponsiblePostpaidManualStateTransition()
    {
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.POSTPAID);
        subscriber.setId("6002");
        subscriber.setBAN(this.responsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();

        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from warned
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.NON_PAYMENT_WARN, endStateMap);
        }

        // from dunned
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.NON_PAYMENT_SUSPENDED, endStateMap);
        }

        // from in-arrears
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.IN_ARREARS, endStateMap);
        }

        // from in-collection
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.IN_COLLECTION, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, true);
    }


    /**
     * Test for allowed manual state transitions for non-individual, non-responsible
     * postpaid subscribers.
     */
    public void testNonResponsiblePostpaidManualStateTransition()
    {
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.POSTPAID);
        subscriber.setId("6003");
        subscriber.setBAN(this.nonResponsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();

        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from warned
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.NON_PAYMENT_WARN, endStateMap);
        }

        // from dunned
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.NON_PAYMENT_SUSPENDED, endStateMap);
        }

        // from in-arrears
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.IN_ARREARS, endStateMap);
        }

        // from in-collection
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.IN_COLLECTION, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, true);
    }


    /**
     * Test for allowed state transitions for individual responsible true prepaid
     * subscribers.
     */
    public void testIndividualResponsibleTruePrepaidStateTransition()
    {
        enableTruePrepaid();

        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber.setId("3000");
        subscriber.setBAN(this.individualResponsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();

        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.AVAILABLE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from available
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.LOCKED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.EXPIRED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.AVAILABLE, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.LOCKED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.EXPIRED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from barred
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.LOCKED, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.LOCKED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.EXPIRED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from expired
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.LOCKED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.EXPIRED, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, false);
    }


    /**
     * Test for allowed state transitions for individual non-responsible true prepaid
     * subscribers.
     */
    public void testIndividualNonResponsibleTruePrepaidStateTransition()
    {
        enableTruePrepaid();
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber.setId("3001");
        subscriber.setBAN(this.individualNonResponsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();

        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.AVAILABLE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from available
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.LOCKED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.EXPIRED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.AVAILABLE, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.LOCKED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.EXPIRED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from barred
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.LOCKED, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.LOCKED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.EXPIRED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from expired
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.LOCKED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.EXPIRED, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, false);
    }


    /**
     * Test for allowed state transitions for non-individual, responsible true prepaid
     * subscribers.
     */
    public void testResponsibleTruePrepaidStateTransition()
    {
        enableTruePrepaid();
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber.setId("3002");
        subscriber.setBAN(this.responsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();

        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.AVAILABLE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from available
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.LOCKED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.EXPIRED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.AVAILABLE, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.LOCKED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.EXPIRED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from barred
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.LOCKED, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.LOCKED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.EXPIRED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from expired
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.LOCKED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.EXPIRED, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, false);

    }


    /**
     * Test for allowed state transitions for non-individual, non-responsible true prepaid
     * subscribers.
     */
    public void testNonResponsibleTruePrepaidStateTransition()
    {
        enableTruePrepaid();
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber.setId("3003");
        subscriber.setBAN(this.nonResponsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();
        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.AVAILABLE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from available
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.LOCKED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.EXPIRED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.AVAILABLE, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.LOCKED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.EXPIRED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from barred
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.LOCKED, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.LOCKED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.EXPIRED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from expired
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.LOCKED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.EXPIRED, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, false);

    }


    /**
     * Test for allowed state transitions for individual responsible hybrid prepaid
     * subscribers.
     */
    public void testIndividualResponsibleHybridPrepaidStateTransition()
    {
        enableHybridPrepaid();

        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber.setId("2000");
        subscriber.setBAN(this.individualResponsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();

        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, false);
    }


    /**
     * Test for allowed state transitions for individual non-responsible hybrid prepaid
     * subscribers.
     */
    public void testIndividualNonResponsibleHybridPrepaidStateTransition()
    {
        enableHybridPrepaid();
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber.setId("2001");
        subscriber.setBAN(this.individualNonResponsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();

        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, false);
    }


    /**
     * Test for allowed state transitions for non-individual, responsible hybrid prepaid
     * subscribers.
     */
    public void testResponsibleHybridPrepaidStateTransition()
    {
        enableHybridPrepaid();
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber.setId("2002");
        subscriber.setBAN(this.responsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();

        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, false);
    }


    /**
     * Test for allowed state transitions for non-individual, non-responsible hybrid
     * prepaid subscribers.
     */
    public void testNonResponsibleHybridPrepaidStateTransition()
    {
        enableHybridPrepaid();
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber.setId("2003");
        subscriber.setBAN(this.nonResponsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();

        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, false);
    }


    /**
     * Test for allowed state transitions for individual responsible postpaid subscribers.
     */
    public void testIndividualResponsiblePostpaidStateTransition()
    {
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.POSTPAID);
        subscriber.setId("1000");
        subscriber.setBAN(this.individualResponsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();

        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.IN_ARREARS, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_SUSPENDED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_WARN, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.PROMISE_TO_PAY, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from warned
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.IN_ARREARS, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.IN_COLLECTION, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_SUSPENDED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.PROMISE_TO_PAY, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.NON_PAYMENT_WARN, endStateMap);
        }

        // from dunned
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.IN_ARREARS, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.IN_COLLECTION, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_WARN, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.PROMISE_TO_PAY, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.NON_PAYMENT_SUSPENDED, endStateMap);
        }

        // from in-arrears
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.IN_COLLECTION, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_SUSPENDED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_WARN, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.PROMISE_TO_PAY, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.IN_ARREARS, endStateMap);
        }

        // from in-collection
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.IN_COLLECTION, endStateMap);
        }

        // from promise-to-pay
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.IN_ARREARS, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.IN_COLLECTION, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_SUSPENDED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_WARN, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.PROMISE_TO_PAY, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, false);
    }


    /**
     * Test for allowed state transitions for individual non-responsible postpaid
     * subscribers.
     */
    public void testIndividualNonResponsiblePostpaidStateTransition()
    {
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.POSTPAID);
        subscriber.setId("1001");
        subscriber.setBAN(this.individualNonResponsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();

        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.IN_ARREARS, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_SUSPENDED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_WARN, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from warned
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.IN_ARREARS, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.IN_COLLECTION, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_SUSPENDED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.NON_PAYMENT_WARN, endStateMap);
        }

        // from dunned
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.IN_ARREARS, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.IN_COLLECTION, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_WARN, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.NON_PAYMENT_SUSPENDED, endStateMap);
        }

        // from in-arrears
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.IN_COLLECTION, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_SUSPENDED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_WARN, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.IN_ARREARS, endStateMap);
        }

        // from in-collection
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.IN_COLLECTION, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, false);
    }


    /**
     * Test for allowed state transitions for non-individual, responsible postpaid
     * subscribers.
     */
    public void testResponsiblePostpaidStateTransition()
    {
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.POSTPAID);
        subscriber.setId("1002");
        subscriber.setBAN(this.responsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();

        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.IN_ARREARS, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_SUSPENDED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_WARN, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from warned
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.IN_ARREARS, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.IN_COLLECTION, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_SUSPENDED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.NON_PAYMENT_WARN, endStateMap);
        }

        // from dunned
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.IN_ARREARS, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.IN_COLLECTION, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_WARN, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.NON_PAYMENT_SUSPENDED, endStateMap);
        }

        // from in-arrears
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.IN_COLLECTION, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_SUSPENDED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_WARN, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.IN_ARREARS, endStateMap);
        }

        // from in-collection
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.IN_COLLECTION, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, false);
    }


    /**
     * Test for allowed state transitions for non-individual, non-responsible postpaid
     * subscribers.
     */
    public void testNonResponsiblePostpaidStateTransition()
    {
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.POSTPAID);
        subscriber.setId("1003");
        subscriber.setBAN(this.nonResponsibleAccount_.getBAN());

        final Map<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>> map;
        map = new HashMap<SubscriberStateEnum, Map<SubscriberStateEnum, Boolean>>();

        // from pending
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.PENDING, endStateMap);
        }

        // from active
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.IN_ARREARS, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_SUSPENDED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_WARN, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.SUSPENDED, endStateMap);
        }

        // from warned
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.IN_ARREARS, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.IN_COLLECTION, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_SUSPENDED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.NON_PAYMENT_WARN, endStateMap);
        }

        // from dunned
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.IN_ARREARS, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.IN_COLLECTION, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_WARN, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.NON_PAYMENT_SUSPENDED, endStateMap);
        }

        // from in-arrears
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.IN_COLLECTION, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_SUSPENDED, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.NON_PAYMENT_WARN, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.SUSPENDED, Boolean.TRUE);
            map.put(SubscriberStateEnum.IN_ARREARS, endStateMap);
        }

        // from in-collection
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.IN_COLLECTION, endStateMap);
        }

        // from deactivated
        {
            final Map<SubscriberStateEnum, Boolean> endStateMap = new HashMap<SubscriberStateEnum, Boolean>();
            endStateMap.put(SubscriberStateEnum.INACTIVE, Boolean.TRUE);
            map.put(SubscriberStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(subscriber, map, false);
    }

    /**
     * Individual responsible account.
     */
    private Account individualResponsibleAccount_;

    /**
     * Individual non-responsible account.
     */
    private Account individualNonResponsibleAccount_;

    /**
     * Non-individual, responsible account.
     */
    private Account responsibleAccount_;

    /**
     * Non-individual, non-responsible account.
     */
    private Account nonResponsibleAccount_;
}
