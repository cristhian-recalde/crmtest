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
package com.trilogy.app.crm.support;


import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextSupport;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.KeyConfiguration;
import com.trilogy.app.crm.bean.KeyValueFeatureEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.ui.ServiceUIContextFactory;
import com.trilogy.app.crm.calculator.AlcatelSSCUserInputValueCalculator;
import com.trilogy.app.crm.calculator.UserInputValueCalculator;
import com.trilogy.app.crm.calculator.ValueCalculator;
import com.trilogy.app.crm.calculator.ValueCalculatorProxy;
import com.trilogy.app.crm.extension.service.AlcatelSSCServiceExtension;
import com.trilogy.app.crm.extension.spid.AlcatelSSCSpidExtension;
import com.trilogy.app.crm.extension.subscriber.AlcatelSSCSubscriberExtension;
import com.trilogy.app.crm.transfer.TransferDispute;


/**
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class CRMKeyValueSupport extends DefaultKeyValueSupport
{
    protected static KeyValueSupport instance_ = null;
    public static KeyValueSupport instance()
    {
        if (instance_ == null)
        {
            instance_ = new CRMKeyValueSupport();
        }
        return instance_;
    }

    protected CRMKeyValueSupport()
    {
    }


    /**
     * This method is protected because it alters the input context (not a sub-context)
     */
    @Override
    protected Map<Class, Collection<PropertyInfo>> getBeanLoaderMapForFeature(
            Context ctx,
            KeyValueFeatureEnum feature)
            {
        Map<Class, Collection<PropertyInfo>> loaderMap = null;
        if (KeyValueFeatureEnum.INVOICE_EMAIL.equals(feature))
        {
            loaderMap = BeanLoaderSupportHelper.get(ctx).getBeanLoaderMap(ctx, Invoice.class);
        }
        else if (KeyValueFeatureEnum.ALCATEL_SSC_SUBSCRIPTION.equals(feature))
        {
            if (!ctx.has(Service.class))
            {
                // Try to load the service into the context because the Alcatel SSC Subscription
                // level feature needs to get try and get defaults from it for SPID & Service
                // level keys.
                Subscriber sub = BeanLoaderSupportHelper.get(ctx).getBean(ctx, Subscriber.class);
                if (sub != null)
                {
                    Set<ServiceFee2ID> alcatelSvcs;
                    try
                    {
                        alcatelSvcs = sub.getIntentToProvisionServices(ctx, ServiceTypeEnum.ALCATEL_SSC);
                        if (alcatelSvcs != null && alcatelSvcs.size() > 0)
                        {
                            // As of CRM 8.2, only one Alcatel SSC service is supported, so assume the first one is the one we want.
                        	ServiceFee2ID serviceFee2ID = alcatelSvcs.iterator().next();
                            final com.redknee.app.crm.bean.core.Service service = ServiceSupport.getService(ctx, serviceFee2ID.getServiceId());
                            ctx.put(com.redknee.app.crm.bean.Service.class, service);
                            ctx.put(com.redknee.app.crm.bean.core.Service.class, service);
                            ctx.put(com.redknee.app.crm.bean.ui.Service.class, new ServiceUIContextFactory(service));
                        }
                    }
                    catch (HomeException e)
                    {
                        new DebugLogMsg(this, "Error occurred retrieving Alcatel SSC service for subscription " + sub.getId() + ". Will proceed without it.", e).log(ctx);
                    }
                }
            }

            loaderMap = BeanLoaderSupportHelper.get(ctx).getBeanLoaderMap(ctx, AlcatelSSCSubscriberExtension.class);
        }
        else if (KeyValueFeatureEnum.ALCATEL_SSC_SERVICE.equals(feature))
        {
            // We don't want values that come from the subscription at this level
            ctx.put(Subscriber.class, null);

            loaderMap = BeanLoaderSupportHelper.get(ctx).getBeanLoaderMap(ctx, AlcatelSSCServiceExtension.class);
        }
        else if (KeyValueFeatureEnum.ALCATEL_SSC_SPID.equals(feature))
        {
            // We don't want values that come from the subscription or the service at this level
            ctx.put(Subscriber.class, null);
            ctx.put(Service.class, null);

            loaderMap = BeanLoaderSupportHelper.get(ctx).getBeanLoaderMap(ctx, AlcatelSSCSpidExtension.class);
        }
        else if (KeyValueFeatureEnum.STATE_CHANGE_EMAIL.equals(feature)
                || KeyValueFeatureEnum.EXPIRY_EXTENSION_EMAIL.equals(feature)
                || KeyValueFeatureEnum.PRE_EXPIRY_EMAIL.equals(feature)
                || KeyValueFeatureEnum.RECURRING_RECHARGE_PREWARN_EMAIL.equals(feature)
                || KeyValueFeatureEnum.SERVICE_SUSPENSION_EMAIL.equals(feature)
                || KeyValueFeatureEnum.SERVICE_UNSUSPENSION_EMAIL.equals(feature))
        {
            loaderMap = BeanLoaderSupportHelper.get(ctx).getBeanLoaderMap(ctx, Subscriber.class);
        }
        else if (KeyValueFeatureEnum.TRANSFER_DISPUTE_EMAIL.equals(feature))
        {
            loaderMap = BeanLoaderSupportHelper.get(ctx).getBeanLoaderMap(ctx, TransferDispute.class);
        }
        else if (KeyValueFeatureEnum.PREPAID_RECHARGE_EMAIL.equals(feature))
        {
            loaderMap = BeanLoaderSupportHelper.get(ctx).getBeanLoaderMap(ctx, Transaction.class);
        }
        return loaderMap;
            }


    @Override
    public String getUserInputValue(Context ctx, KeyValueFeatureEnum feature, KeyConfiguration keyConf)
    {
        switch (feature.getIndex())
        {
        case KeyValueFeatureEnum.ALCATEL_SSC_SUBSCRIPTION_INDEX:
        case KeyValueFeatureEnum.ALCATEL_SSC_SERVICE_INDEX:
        case KeyValueFeatureEnum.ALCATEL_SSC_SPID_INDEX:
            if (keyConf != null)
            {
                final ValueCalculator rootCalc = keyConf.getValueCalculator();
                ValueCalculator calc = rootCalc;
                if (calc instanceof ValueCalculatorProxy)
                {
                    calc = ((ValueCalculatorProxy)calc).findDecorator(UserInputValueCalculator.class);
                }
                if (!(calc instanceof AlcatelSSCUserInputValueCalculator))
                {
                    // For Alcatel SSC features, swap out non-Alcatel SSC value calculator with Alcatel SSC ones
                    final ValueCalculator newUserInputCalculator = (ValueCalculator) XBeans.copy(ctx, 
                            calc, 
                            new AlcatelSSCUserInputValueCalculator());
                    
                    if (rootCalc instanceof ValueCalculatorProxy)
                    {
                        ValueCalculatorProxy lastProxy = (ValueCalculatorProxy) rootCalc;
                        while (lastProxy.getDelegate() instanceof ValueCalculatorProxy)
                        {
                            lastProxy = (ValueCalculatorProxy) ((ValueCalculatorProxy) rootCalc).getDelegate();
                        }
                        if (lastProxy.isFrozen())
                        {
                            try
                            {
                                lastProxy = (ValueCalculatorProxy) lastProxy.clone();
                            }
                            catch (CloneNotSupportedException e)
                            {
                            }
                        }
                        if (!lastProxy.isFrozen())
                        {
                            lastProxy.setDelegate(newUserInputCalculator);
                        }
                    }
                    else
                    {
                        if (keyConf.isFrozen())
                        {
                            try
                            {
                                keyConf = (KeyConfiguration) keyConf.clone();
                            }
                            catch (CloneNotSupportedException e)
                            {
                            }
                        }
                        if (!keyConf.isFrozen())
                        {
                            keyConf.setValueCalculator(newUserInputCalculator);
                        }
                    }
                }
            }
            break;
        default:
            // NOP
        }

        // Execute the calculator
        return super.getUserInputValue(ctx, feature, keyConf);
    }
}
