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
package com.trilogy.app.crm.api.rmi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.AbstractSubscriber;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SuspendedEntitySupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.ProvisioningStateTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.serviceandbundle.PricePlanOptionTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanOption;


/**
 * Adapts PricePlan object to API objects.
 * 
 * @author victor.stratan@redknee.com
 */
public class PricePlanBundleToPricePlanOptionAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return AuxiliaryBundleToPricePlanOptionAdapter.convertBundleToPricePlanOption(ctx, (BundleFee) obj, new HashMap(), PricePlanOptionTypeEnum.BUNDLE.getValue());
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    /**
     * Returns a list of Bundles that exist in given Subscriber's PricePlanVersion. Set's 'isSelected' field
     * to true if Bundle is associated with Subscriber. It does NOT include Auxiliary Bundles.
     *  
     * @param ctx
     * @param sub       Subscriber
     * @param pp        PricePlan
     * @param ppv       PricePlanVersion
     * @return
     * @throws HomeException
     */
    public static PricePlanOption[] getSubscriberBundlesConvertedPricePlanOption(Context ctx, final Subscriber sub,
            final PricePlan pp, final PricePlanVersion ppv) throws HomeException
    {
        final Map<Long, BundleFee> map;
        if (sub.getPricePlan() != AbstractSubscriber.DEFAULT_PRICEPLAN)
        {
            map = SubscriberBundleSupport.getPricePlanBundles(ctx, pp, ppv);
        }
        else
        {
            map = new HashMap<Long, BundleFee>();
        }
        Map<Long, BundleFee> selectedBundles = sub.getBundles();

        List<PricePlanOption> options = new ArrayList<PricePlanOption>();

        for (Iterator<Map.Entry<Long, BundleFee>> i = map.entrySet().iterator(); i.hasNext();)
        {
            final Map.Entry<Long, BundleFee> entry = i.next();
            final BundleFee fee = entry.getValue();
            PricePlanOption option = AuxiliaryBundleToPricePlanOptionAdapter.convertBundleToPricePlanOption(ctx, fee, selectedBundles,PricePlanOptionTypeEnum.BUNDLE.getValue());
            if (sub != null)
            {
                boolean isSuspended = false;
                try
                {
                    isSuspended = SuspendedEntitySupport.isSuspendedEntity(ctx, 
                            sub.getId(), 
                            fee.getId(), 
                            SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, 
                            BundleFee.class);
                }
                catch (HomeException e)
                {
                    new MinorLogMsg(AuxiliaryBundleToPricePlanOptionAdapter.class, 
                            "Error looking up suspended state of bundle " + fee.getId()
                            + " for subscription " + sub.getId() + ".  Assuming not suspended...", e).log(ctx);
                }
                
                if (isSuspended)
                {
                    option.setProvisioningState(ProvisioningStateTypeEnum.SUSPENDED.getValue());
                }
            }
            
            options.add(option);
        }
        return options.toArray(new PricePlanOption[]{});
    }


    public static PricePlanOption[] getBundlesConvertedPricePlanOption(Context ctx, Collection<BundleFee> feeList)
            throws HomeException
    {
        PricePlanOption[] options = new PricePlanOption[feeList.size()];
        Iterator<BundleFee> iter = feeList.iterator();
        int i = 0;
        while (iter.hasNext())
        {
            BundleFee fee = iter.next();
            PricePlanOption planOption = AuxiliaryBundleToPricePlanOptionAdapter.convertBundleToPricePlanOption(ctx, fee, new HashMap(), PricePlanOptionTypeEnum.BUNDLE.getValue());
            options[i] = planOption;
            i++;
        }
        return options;
    }


}
