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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceSelection;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.search.AuxiliaryServiceSearch;

/**
 * TODO: Extract an AuxiliaryServiceTypeAware interface and make this more generic.
 * 
 * @author simar.singh@redknee.com
 */
public class AuxServiceTypePredicate implements Predicate
{

    private static final long serialVersionUID = 1L;

    public AuxServiceTypePredicate(AuxiliaryServiceTypeEnum type, AuxiliaryServiceTypeEnum ... types)
    {
        Set<AuxiliaryServiceTypeEnum> setOfTypes = new HashSet<AuxiliaryServiceTypeEnum>(Arrays.asList(types));
        setOfTypes.add(type);
        this.auxServiceType_ = Collections.unmodifiableSet(setOfTypes);
    }
    
    private final Set<AuxiliaryServiceTypeEnum> auxServiceType_;
    
    public boolean f(Context ctx, Object obj) throws AbortVisitException 
    {
        AuxiliaryServiceTypeEnum type = getAuxiliaryServiceType(ctx, obj);
        if (type == null)
        {
            type = getAuxiliaryServiceType(ctx, ctx.get(AbstractWebControl.BEAN));
        }
        return auxServiceType_.contains(type);
    }

    public AuxiliaryServiceTypeEnum getAuxiliaryServiceType(Context ctx, Object bean)
    {
        AuxiliaryServiceTypeEnum type = null;
        if (bean instanceof AuxiliaryServiceSelection)
        {
            type = ((AuxiliaryServiceSelection) bean).getType();
        }
        else if (bean instanceof AuxiliaryService)
        {
            type = ((AuxiliaryService) bean).getType();
        }
        else if (bean instanceof SubscriberAuxiliaryService)
        {
            type = ((SubscriberAuxiliaryService) bean).getType(ctx);
        }
        else if (bean instanceof AuxiliaryServiceSearch)
        {
            type = ((AuxiliaryServiceSearch) bean).getType();
        }
        return type;
    }
}
