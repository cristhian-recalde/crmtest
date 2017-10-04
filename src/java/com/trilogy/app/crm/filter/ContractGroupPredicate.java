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

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.app.crm.bean.ChargingLevelEnum;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.ServicePackage;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.ServicePackageHome;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport; // import
import com.trilogy.app.crm.transfer.ContractGroup;
                                                                // com.redknee.app.crm.bean.PricePlanTypeEnum;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 *  Filters out contract groups based on the list of contract group ids specified
 */
public class ContractGroupPredicate implements Predicate
{

    public ContractGroupPredicate()
    {
    }


    public ContractGroupPredicate(Object obj)
    {
        if (obj != null)
        {
            groupIds_ = new ArrayList((List) obj);
        }
    }


    public boolean f(Context ctx, Object obj)
    {
        // no group IDs to verify against
        if (groupIds_ == null)
        {
            new MinorLogMsg(
                    this,
                    "Unable to filter because the contract group ids is unspecified.",
                    null).log(ctx);
            return true;
        }
        
        ContractGroup group = (ContractGroup) obj;

        if (groupIds_.contains(group.getIdentifier()))
        {
            return true;
        }
 
        return false;
    }

    private List groupIds_ = null;
}
