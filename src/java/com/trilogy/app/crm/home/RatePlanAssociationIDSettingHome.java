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

package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bundle.rateplan.RatePlanAssociation;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * Sets ID and identifier for this Rate Plan Association using an
 * IdentifierSequence. 
 * 
 * @author bdhavalshankh
 *
 */
public class RatePlanAssociationIDSettingHome extends HomeProxy 
{
    private static final long startValue = 100000;
    
    public RatePlanAssociationIDSettingHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    @Override
    public Object create(Context ctx, Object obj) throws HomeException,
            HomeInternalException 
    {
        
        RatePlanAssociation rpa = (RatePlanAssociation)obj;
        
        IdentifierSequenceSupportHelper.get(ctx).ensureSequenceExists(ctx, IdentifierEnum.RATE_PLAN_ASSOCIATION_ID,
                startValue, Long.MAX_VALUE);
        
        long rpaID = IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(
                ctx,
                IdentifierEnum.RATE_PLAN_ASSOCIATION_ID,
                null);
        
        if(rpa.getId() == 0)
        {
            rpa.setId(rpaID);
        }
        
        LogSupport.info(ctx, this, "Rate Plan Association ID set to: " + rpa.getId());
        
        return super.create(ctx, obj);
    }

    
}
