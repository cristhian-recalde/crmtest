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
package com.trilogy.app.crm.filter;

import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.KeyConfiguration;
import com.trilogy.app.crm.bean.KeyValueFeatureEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.calculator.UserInputValueCalculator;
import com.trilogy.app.crm.calculator.ValueCalculator;
import com.trilogy.app.crm.calculator.ValueCalculatorProxy;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionAware;
import com.trilogy.app.crm.extension.service.AlcatelSSCServiceExtension;
import com.trilogy.app.crm.extension.spid.AlcatelSSCSpidExtension;
import com.trilogy.app.crm.extension.subscriber.AlcatelSSCSubscriberExtension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;


/**
 * A predicate used to filter the XML provisioning configurable "Keys" based on the type
 * of entity. For eg: if the Entity is of type Service,then,only those keys would be shown
 * up which have been configured with beanClass=Service.
 * 
 * This predicate is designed to work only on entities of type
 * 'KeyConfiguration'.
 * 
 * @author abaid
 * @since Oct 2009
 * 
 */
public class XMLProvisioningKeyPredicate implements Predicate
{

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context
     * .Context, java.lang.Object)
     */
    public boolean f(final Context ctx, final Object obj) throws AbortVisitException
    {
        boolean selected = false;
        final Object parentBean = ExtensionSupportHelper.get(ctx).getParentBean(ctx);
        if (parentBean != null)
        {
            if (obj instanceof KeyConfiguration)
            {
                final KeyConfiguration keyConf = (KeyConfiguration) obj;
                ValueCalculator valueCalculator = keyConf.getValueCalculator();
                if (valueCalculator instanceof ValueCalculatorProxy)
                {
                    valueCalculator = ((ValueCalculatorProxy)valueCalculator).findDecorator(UserInputValueCalculator.class);
                }
                if (valueCalculator instanceof UserInputValueCalculator)
                {
                    Class parentBeanClass = parentBean.getClass();

                    KeyValueFeatureEnum feature = keyConf.getFeature();
                    switch (feature.getIndex())
                    {
                    case KeyValueFeatureEnum.ALCATEL_SSC_SPID_INDEX:
                        selected |= CRMSpid.class.isAssignableFrom(parentBeanClass);
                    case KeyValueFeatureEnum.ALCATEL_SSC_SERVICE_INDEX:
                        selected |= com.redknee.app.crm.bean.ui.Service.class.isAssignableFrom(parentBeanClass);
                        selected |= com.redknee.app.crm.bean.Service.class.isAssignableFrom(parentBeanClass);
                    case KeyValueFeatureEnum.ALCATEL_SSC_SUBSCRIPTION_INDEX:
                        selected |= Subscriber.class.isAssignableFrom(parentBeanClass);
                    }
                }
                
                if (selected
                        && parentBean instanceof ExtensionAware)
                {
                    Collection<Extension> extensions = ((ExtensionAware)parentBean).getExtensions();
                    for (Extension extension : extensions)
                    {
                        if (extension instanceof AlcatelSSCSubscriberExtension)
                        {
                            AlcatelSSCSubscriberExtension alcatelExt = (AlcatelSSCSubscriberExtension) extension;
                            selected = !alcatelExt.getKeyValuePairs().keySet().contains(keyConf.getKey());
                            break;
                        }
                        if (extension instanceof AlcatelSSCServiceExtension)
                        {
                            AlcatelSSCServiceExtension alcatelExt = (AlcatelSSCServiceExtension) extension;
                            selected = !alcatelExt.getKeyValuePairs().keySet().contains(keyConf.getKey());
                            break;
                        }
                        if (extension instanceof AlcatelSSCSpidExtension)
                        {
                            AlcatelSSCSpidExtension alcatelExt = (AlcatelSSCSpidExtension) extension;
                            selected = !alcatelExt.getKeyValuePairs().keySet().contains(keyConf.getKey());
                            break;
                        }
                    }
                }
            }
        }
        return selected;
    }
}
