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
package com.trilogy.app.crm.home.validator;

import java.util.Collection;
import java.util.Map;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.AlcatelSSCProperty;
import com.trilogy.app.crm.bean.KeyConfiguration;
import com.trilogy.app.crm.bean.KeyValueFeatureEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.calculator.AlcatelSSCUserInputValueCalculator;
import com.trilogy.app.crm.calculator.ValueCalculator;
import com.trilogy.app.crm.calculator.ValueCalculatorProxy;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.subscriber.AlcatelSSCSubscriberExtension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.KeyValueSupportHelper;


/**
 * Validates the restriction that if an Alcatel SSC service is selected then an Alcatel SSC extension is required.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class AlcatelSSCSubscriptionValidator implements Validator
{
    private static AlcatelSSCSubscriptionValidator instance_ = null;
    public static AlcatelSSCSubscriptionValidator instance()
    {
        if (instance_ == null)
        {
            instance_ = new AlcatelSSCSubscriptionValidator();
        }
        return instance_;
    }
    
    protected AlcatelSSCSubscriptionValidator()
    {
    }

    /**
     * {@inheritDoc}
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        
        if (obj instanceof Subscriber)
        {
            Subscriber sub = (Subscriber) obj;
            try
            {
                if (sub.hasIntentToProvisionServiceOfType(ctx, ServiceTypeEnum.ALCATEL_SSC))
                {
                    Collection<KeyConfiguration> mandatoryKeys = KeyValueSupportHelper.get(ctx).getConfiguredKeys(
                            ctx, 
                            false, 
                            KeyValueFeatureEnum.ALCATEL_SSC_SUBSCRIPTION);
                    if (mandatoryKeys != null && mandatoryKeys.size() > 0)
                    {
                        AlcatelSSCSubscriberExtension alcatelExt = AlcatelSSCSubscriberExtension.getAlcatelSSCSubscriberExtension(ctx, sub.getId());
                        if (alcatelExt != null)
                        {
                            StringBuilder missingKeys = new StringBuilder();
                            for (KeyConfiguration mandatoryKey : mandatoryKeys)
                            {
                                if (mandatoryKey == null)
                                {
                                    continue;
                                }

                                ValueCalculator calc = mandatoryKey.getValueCalculator();
                                if (calc instanceof ValueCalculatorProxy)
                                {
                                    calc = ((ValueCalculatorProxy)calc).findDecorator(AlcatelSSCUserInputValueCalculator.class);
                                }
                                if (!(calc instanceof AlcatelSSCUserInputValueCalculator))
                                {
                                    // This is a computed key, so user input not required.
                                    continue;
                                }

                                Map<String, AlcatelSSCProperty> pairs = alcatelExt.getKeyValuePairs();
                                if (pairs == null
                                        || !pairs.containsKey(mandatoryKey.getKey()))
                                {
                                    missingKeys.append(mandatoryKey.getKey());
                                    missingKeys.append(", ");
                                }
                            }

                            if (missingKeys.length() > 0)
                            {
                                missingKeys.delete(missingKeys.lastIndexOf(","), missingKeys.length()-1);
                                cise.thrown(new IllegalPropertyArgumentException(
                                        SubscriberXInfo.SUB_EXTENSIONS, 
                                        ExtensionSupportHelper.get(ctx).getExtensionName(ctx, AlcatelSSCSubscriberExtension.class)
                                        + " subscription extension is missing the following mandatory keys: " + missingKeys));
                            }
                        }
                        else
                        {
                            cise.thrown(new IllegalPropertyArgumentException(
                                    SubscriberXInfo.SUB_EXTENSIONS, 
                                    ExtensionSupportHelper.get(ctx).getExtensionName(ctx, AlcatelSSCSubscriberExtension.class)
                                    + " subscription extension required when " + ServiceTypeEnum.ALCATEL_SSC
                                    + " service selected in the price plan."));
                        }   
                    }
                }
            }
            catch (HomeException e)
            {
            }
        }
        
        cise.throwAll();
    }

}
