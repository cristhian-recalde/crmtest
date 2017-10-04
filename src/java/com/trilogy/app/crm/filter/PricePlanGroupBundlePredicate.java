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
package com.trilogy.app.crm.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.support.PricePlanSupport;


/**
 *  
 */
public class PricePlanGroupBundlePredicate implements Predicate
{

    public PricePlanGroupBundlePredicate()
    {
    }


    public PricePlanGroupBundlePredicate(Object obj)
    {
        if (obj != null)
        {
            bundleIds_ = new ArrayList((Collection) obj);
        }
    }


    public boolean f(Context ctx, Object obj)
    {
        // no bad bundle IDs to verify against
        if (bundleIds_ == null)
        {
            new MinorLogMsg(
                    this,
                    "Unable to match group bundle ids to price plan bundle ids because the group bundle ids is unspecified.",
                    null).log(ctx);
            return true;
        }
        // check to see if price plan's latest version has bundles that are invalid
        PricePlan pp = (PricePlan) obj;
        Map bundles = new HashMap();
        try
        {
            bundles = PricePlanSupport.getBundleIds(ctx, pp);
        }
        catch (Exception e)
        {
            new DebugLogMsg(PricePlanGroupBundlePredicate.class, e.getMessage(), e).log(ctx);
            return true;
        }
        Iterator iter = bundles.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry entry = (Map.Entry) iter.next();
            final Long id = (Long) entry.getKey();
            BundleFee fee = (BundleFee) entry.getValue();
            if (fee.getServicePreference() == ServicePreferenceEnum.MANDATORY)
            {
                if (bundleIds_.contains(id))
                {
                    return false;
                }
            }
        }
        return true;
    }

    /*
     * List of bad bundle IDs (inverse of group owner bundles)
     */
    private List bundleIds_ = null;
}
