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
package com.trilogy.app.crm.priceplan;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.Visitor;

import com.trilogy.app.crm.bean.PricePlanVersionXInfo;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.core.ServicePackage;

/**
 * @author victor.stratan@redknee.com
 *
 */
public class CollectUniqueBundlesVisitor implements Visitor {
    final Map allBundles;
    final HashSet dublicates;
    final CompoundIllegalStateException el;

    public CollectUniqueBundlesVisitor(Map bundles, HashSet dublicates, CompoundIllegalStateException el)
    {
        this.allBundles = new HashMap(bundles);
        this.dublicates = dublicates;
        this.el = el;
    }

    public void visit(Context ctx, Object obj)
    {
        ServicePackage pack = (ServicePackage) obj;
        ServicePackageVersion version = pack.getCurrentVersion(ctx);
        Map packBundles = version.getBundleFees();
        Iterator it = packBundles.keySet().iterator();
        while (it.hasNext())
        {
            Long key = (Long) it.next();
            if (allBundles.containsKey(key))
            {
                el.thrown(new IllegalPropertyArgumentException(
                        PricePlanVersionXInfo.SERVICE_PACKAGE_VERSION,
                        "BundleID " + key + " is duplicate inside package " + pack.getId() + "."));
                if (allBundles.get(key) instanceof Long)
                {
                    el.thrown(new IllegalPropertyArgumentException(
                            PricePlanVersionXInfo.SERVICE_PACKAGE_VERSION,
                            "BundleID " + key + " is duplicate inside package " + allBundles.get(key) + "."));
                    allBundles.put(key, null);
                }
                else
                {
                    dublicates.add(key);
                }
            }
            else
            {
                allBundles.put(key, Long.valueOf(pack.getId()));
            }
        }
    }

    private static final long serialVersionUID = 10L;
}
