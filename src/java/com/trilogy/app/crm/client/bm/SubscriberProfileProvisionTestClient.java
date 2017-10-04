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
package com.trilogy.app.crm.client.bm;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;


/**
 * Provides a testing client that never throws an exception and returns simple
 * data for queries.
 *
 * @author gary.anderson@redknee.com
 */
public class SubscriberProfileProvisionTestClient
    extends SubscriberProfileProvisionClientProxy

{
    /**
     * Creates a new testing client.
     */
    public SubscriberProfileProvisionTestClient()
    {
        this(null);
    }


    /**
     * Creates a new testing client.
     *
     * @param name The name of the test client, used in log messages.
     */
    public SubscriberProfileProvisionTestClient(final String name)
    {
        super(createHandler(name));
    }


    /**
     * Creates the handler used by the proxy.
     *
     * @param name The name of the test client, used in log messages.
     * @return The handler used by the proxy.
     */
    private static SubscriberProfileProvisionClient createHandler(final String name)
    {
        final ClassLoader classLoader = SubscriberProfileProvisionClient.class.getClassLoader();
        final Class<?>[] interfaces = new Class[]
        {
            SubscriberProfileProvisionClient.class,
        };

        final Object handler = Proxy.newProxyInstance(classLoader, interfaces, new Handler(name));

        return (SubscriberProfileProvisionClient)handler;
    }


    /**
     * Handles the SubscriberProfileProvisionClient calls.
     *
     * @author gary.anderson@redknee.com
     */
    private static final class Handler
        implements InvocationHandler
    {
        /**
         * Creates a new Handler.
         *
         * @param name The name of the test client, used in log messages.
         */
        public Handler(final String name)
        {
            if (name == null || name.trim().length() == 0)
            {
                name_ = "SubscriberProfileProvisionTestClient";
            }
            else
            {
                name_ = name;
            }
        }


        /**
         * {@inheritDoc}
         */
        public Object invoke(final Object proxy, final Method method, final Object[] args)
        {
            final Context context = (Context)args[0];

            if (LogSupport.isDebugEnabled(context))
            {
                final StringBuilder builder = new StringBuilder();
                builder.append(name_);
                builder.append('[');
                builder.append(method.getName());
                builder.append('(');

                for (int n = 0; n < args.length; ++n)
                {
                    if (n != 0)
                    {
                        builder.append(", ");
                    }

                    final Object arg = args[n];

                    if (arg instanceof Account)
                    {
                        final Account account = (Account)arg;
                        builder.append("Account[BAN=");
                        builder.append(account.getBAN());
                        builder.append("]");
                        // TODO -- Add additional BM specific fields, e.g.: group ID and owner.
                    }
                    else if (arg instanceof Subscriber)
                    {
                        final Subscriber subscription = (Subscriber)arg;
                        builder.append("Subscriber[ID=");
                        builder.append(subscription.getId());
                        builder.append(",MSISDN=");
                        builder.append(subscription.getMSISDN());
                        builder.append("]");
                        // TODO -- Add additional BM specific fields, e.g.: subscription type and level.
                    }
                    else if (arg == null)
                    {
                        builder.append("null");
                    }
                    else
                    {
                        builder.append(arg.toString());
                    }
                }

                builder.append(")]");

                new DebugLogMsg(this, builder.toString(), null).log(context);
            }

            return null;
        }


        /**
         * The name of the test client, used in log messages.
         */
        private final String name_;
    }
}
