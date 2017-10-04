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
package com.trilogy.app.crm.calculator;

import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.provision.gateway.SPGCustomParameterValueProvider;
import com.trilogy.app.crm.provision.gateway.SPGParameter;
import com.trilogy.app.crm.provision.gateway.ServiceProvisioningGatewaySupport;
import com.trilogy.util.snippet.log.Logger;


/**
 * A bridge between SPGCustomParameterValueProvider and ValueCalculator.  This will make it possible
 * to one day migrate out all implementations of SPGCustomParameterValueProvider, replacing them with
 * more flexible value calculators instead.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.8/9.0
 */
public class SPGCustomValueProviderValueCalculatorBridge extends AbstractSPGCustomValueProviderValueCalculatorBridge
        implements
            SPGCustomParameterValueProvider
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection getDependentContextKeys(Context ctx)
    {
        Collection result = new ArrayList();
        result.add(Subscriber.class);
        result.add(SPGParameter.class);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Object getValueAdvanced(Context ctx)
    {
        Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
        SPGParameter param = (SPGParameter) ctx.get(SPGParameter.class);
        return provideValue(ctx, param, sub);
    }


    /**
     * {@inheritDoc}
     */
    public Object provideValue(Context ctx, SPGParameter spgParameter, Subscriber subscriber)
    {
        SPGCustomParameterValueProvider delegate = getDelegate(ctx);
        if (delegate == null)
        {
            return null;
        }
        
        return delegate.provideValue(ctx, spgParameter, subscriber);
    }


    public SPGCustomParameterValueProvider getDelegate(Context ctx)
    {
        if (delegate_ == null)
        {
            try
            {
                Class clazz = Class.forName(getDelegateClass());
                delegate_ = (SPGCustomParameterValueProvider) clazz.newInstance();
            }
            catch (Exception e)
            {
                Logger.minor(ctx, ServiceProvisioningGatewaySupport.class,
                        "Error while creating custom value provider [" + getDelegateClass() + "] " + e.getMessage(), e);
            }
        }
        return delegate_;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean transientEquals(Object o)
    {
        return equals(o);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean persistentEquals(Object o)
    {
        return equals(o);
    }

    protected SPGCustomParameterValueProvider delegate_ = null;
}
