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

import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.KeyConfiguration;
import com.trilogy.app.crm.bean.KeyValueFeatureEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;


/**
 * A value calculator that returns a user-input value.  Note that this calculator
 * needs an instance of com.redknee.app.crm.bean.KeyConfiguration in the context
 * to run properly.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class AlcatelSSCUserInputValueCalculator extends UserInputValueCalculator
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Object> getDependentContextKeys(Context ctx)
    {
        Collection dependencies = super.getDependentContextKeys(ctx);
        
        KeyConfiguration keyConf = (KeyConfiguration) ctx.get(KeyConfiguration.class);
        if (keyConf != null)
        {
            switch (keyConf.getFeature().getIndex())
            {
            case KeyValueFeatureEnum.ALCATEL_SSC_SUBSCRIPTION_INDEX:
                dependencies.add(Subscriber.class);
            case KeyValueFeatureEnum.ALCATEL_SSC_SERVICE_INDEX:
                dependencies.add(Service.class);
            case KeyValueFeatureEnum.ALCATEL_SSC_SPID_INDEX:
                dependencies.add(CRMSpid.class);
            }
        }
        
        return dependencies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class getBeanClass(Context ctx, KeyConfiguration keyConf)
    {
        switch (keyConf.getFeature().getIndex())
        {
        case KeyValueFeatureEnum.ALCATEL_SSC_SUBSCRIPTION_INDEX:
        case KeyValueFeatureEnum.ALCATEL_SSC_SERVICE_INDEX:
        case KeyValueFeatureEnum.ALCATEL_SSC_SPID_INDEX:
            Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
            if (sub != null)
            {
                return Subscriber.class;
            }
        }
        
        switch (keyConf.getFeature().getIndex())
        {
        case KeyValueFeatureEnum.ALCATEL_SSC_SERVICE_INDEX:
        case KeyValueFeatureEnum.ALCATEL_SSC_SPID_INDEX:
            Service svc = (Service) ctx.get(Service.class);
            if (svc != null)
            {
                return Service.class;
            }
        }
        
        switch (keyConf.getFeature().getIndex())
        {
        case KeyValueFeatureEnum.ALCATEL_SSC_SPID_INDEX:
            CRMSpid spidBean = (CRMSpid) ctx.get(CRMSpid.class);
            if (spidBean != null)
            {
                return CRMSpid.class;
            }
        }
        
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getBeanId(Context ctx, KeyConfiguration keyConf)
    {
        switch (keyConf.getFeature().getIndex())
        {
        case KeyValueFeatureEnum.ALCATEL_SSC_SUBSCRIPTION_INDEX:
        case KeyValueFeatureEnum.ALCATEL_SSC_SERVICE_INDEX:
        case KeyValueFeatureEnum.ALCATEL_SSC_SPID_INDEX:
            Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
            if (sub != null)
            {
                return sub.getId();
            }
        }
        
        switch (keyConf.getFeature().getIndex())
        {
        case KeyValueFeatureEnum.ALCATEL_SSC_SERVICE_INDEX:
        case KeyValueFeatureEnum.ALCATEL_SSC_SPID_INDEX:
            Service svc = (Service) ctx.get(Service.class);
            if (svc != null)
            {
                return String.valueOf(svc.getID());
            }
        }
        
        switch (keyConf.getFeature().getIndex())
        {
        case KeyValueFeatureEnum.ALCATEL_SSC_SPID_INDEX:
            CRMSpid spidBean = (CRMSpid) ctx.get(CRMSpid.class);
            if (spidBean != null)
            {
                return String.valueOf(spidBean.getId());
            }
        }
        
        return null;
    }
}
