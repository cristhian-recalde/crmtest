/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.client.alcatel;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.Identifiable;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.KeyConfiguration;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.calculator.PropertyBasedValueCalculator;
import com.trilogy.app.crm.calculator.ValueCalculator;
import com.trilogy.app.crm.calculator.ValueCalculatorProxy;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.KeyValueSupportHelper;


/**
 * A Class that encapslute Alcatel-SSC System Related function
 * 
 * @author simar.singh@redknee.com
 * 
 */
public class AlcatelFunctions
{

    /**
     * 
     * @author simar.singh@redknee.com An interface to collect Alcatel related Properties
     *         and Subscribers related to an Object/Bean of interest
     */
    public static interface AlcateBeanFunction<BEAN extends AbstractBean>
    {

        public Collection<SubscriberServices> getSubscriberServices(Context ctx, BEAN bean) throws HomeException;


        public Map<KeyConfiguration, PropertyInfo> getKeyValue(Context ctx, BEAN bean);
    }
    /**
     * 
     * @author simar.singh@redknee.com An abstract implentation that holds generic method
     *         to get Alcatel property map for any bean
     * 
     */
    public static abstract class AbsstractAlcatelBeanFunction<ANYBEAN extends AbstractBean>
            implements
                AlcateBeanFunction<ANYBEAN>
    {

        @Override
        public Map<KeyConfiguration, PropertyInfo> getKeyValue(Context ctx, ANYBEAN bean)
        {
            final Collection<KeyConfiguration> alcatelKeys = KeyValueSupportHelper.get(ctx).getAlcatelSSCSubscriptionKeys(ctx, true);
            final Map<KeyConfiguration, PropertyInfo> beanProperyMap = new HashMap<KeyConfiguration, PropertyInfo>();
            for (KeyConfiguration key : alcatelKeys)
            {
                ValueCalculator vCal = key.getValueCalculator();
                if (vCal instanceof ValueCalculatorProxy)
                {
                    vCal = ((ValueCalculatorProxy) vCal).findDecorator(PropertyBasedValueCalculator.class);
                }
                if (vCal instanceof PropertyBasedValueCalculator)
                {
                    final PropertyBasedValueCalculator pCal = (PropertyBasedValueCalculator) vCal;
                    if (bean.getClass().isAssignableFrom(pCal.getProperty().getBeanClass()))
                    {
                        beanProperyMap.put(key, pCal.getProperty());
                    }
                }
            }
            return beanProperyMap;
        }


        protected Collection<SubscriberServices> getAlcatelSubscriberServices(Context ctx, Subscriber sub)
                throws HomeException
        {
            And condition = new And();
            condition.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, sub.getId()));
            condition.add(new In(SubscriberServicesXInfo.SERVICE_ID, sub.getServices(ctx, ServiceTypeEnum.ALCATEL_SSC)));
            return HomeSupportHelper.get(ctx).getBeans(ctx, SubscriberServices.class, condition);
        }


        final void logException(Context ctx, Throwable t)
        {
            new MinorLogMsg(this, t.getMessage(), null).log(ctx);
            new DebugLogMsg(this, "Error: ", t).log(ctx);
            final ExceptionListener exceptListner = (ExceptionListener) ctx.get(ExceptionListener.class);
            if (null != exceptListner)
            {
                exceptListner.thrown(t);
            }
        }
    }
    /**
     * 
     * @author simar.singh@redknee.com Collects the Alcatel proeprties and subscribers
     *         related to a Subscriber
     * 
     */
    public static class AlcatalSubscriberFunction<SUBSCRBER extends Subscriber>
            extends
                AbsstractAlcatelBeanFunction<SUBSCRBER>
    {

        @Override
        public Collection<SubscriberServices> getSubscriberServices(Context ctx, SUBSCRBER sub) throws HomeException
        {
            return getAlcatelSubscriberServices(ctx, sub);
        }
    }
    /**
     * 
     * @author simar.singh@redknee.com Collects all Alcatel subscribers and properties
     *         related to an account
     * 
     */
    public static class AlcatalAccountFunction<ACCOUNT extends Account> extends AbsstractAlcatelBeanFunction<ACCOUNT>
    {

        @Override
        public Collection<SubscriberServices> getSubscriberServices(Context ctx, ACCOUNT account) throws HomeException
        {
            ctx = HomeSupportHelper.get(ctx).getWhereContext(ctx, Subscriber.class, new EQ(SubscriberXInfo.BAN, account.getBAN()));
            final Set<SubscriberServices> subscriberServiceSet = new HashSet<SubscriberServices>();
            ((Home) ctx.get(SubscriberHome.class)).forEach(ctx, new Visitor()
            {

                /**
                 * This visitor collects all immediate Alcatel subscribers under an
                 * Account
                 */
                private static final long serialVersionUID = 1L;


                @Override
                public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                {
                    if (Subscriber.class.isAssignableFrom(obj.getClass()))
                    {
                        Subscriber sub = (Subscriber) obj;
                        try
                        {
                            subscriberServiceSet.addAll(getAlcatelSubscriberServices(ctx, sub));
                        }
                        catch (Throwable t)
                        {
                            logException(ctx, t);
                        }
                    }
                    else
                    {
                        final String errorMessage = "ForEach - Vistor expects ENTITY of TYPE [? extends Account]; it is being fed ENTITY of type ["
                                + obj.getClass().getSimpleName()
                                + "] with ID ["
                                + ((obj instanceof Identifiable)
                                        ? (String.valueOf(((Identifiable) obj).ID()))
                                        : ("unknown")) + "]";
                        logException(ctx, new IllegalStateException(errorMessage));
                    }
                }
            });
            return subscriberServiceSet;
        }
    }
}
