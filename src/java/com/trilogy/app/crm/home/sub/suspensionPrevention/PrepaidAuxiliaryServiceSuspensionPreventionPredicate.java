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
package com.trilogy.app.crm.home.sub.suspensionPrevention;

import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.util.TypedPredicate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

/**
 * @author sbanerjee
 *
 */
public class PrepaidAuxiliaryServiceSuspensionPreventionPredicate implements
        TypedPredicate<SubscriberAuxiliaryService>
{
    public PrepaidAuxiliaryServiceSuspensionPreventionPredicate()
    {
        
    }

    @Override
    public boolean f(Context ctx, SubscriberAuxiliaryService subAuxService) throws AbortVisitException
    {
        try
        {
            return subAuxService.getAuxiliaryService(ctx).getRestrictProvisioning();
        } 
        catch (Exception e)
        {
            // Nothing to be done; will return false;
        }
        
        return false;
    }
}
