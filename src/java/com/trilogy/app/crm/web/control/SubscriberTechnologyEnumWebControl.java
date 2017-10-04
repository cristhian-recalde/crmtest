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
package com.trilogy.app.crm.web.control;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.filter.TechnologyTypeEnumPredicate;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.EnumWebControl;

/**
 * Enum web control that filters the elements based on license and subscription type
 * @author arturo.medina@redknee.com
 *
 */
public class SubscriberTechnologyEnumWebControl extends EnumWebControl
{

    /**
     * Default constructor, sets the autopreview to false 
     */
    public SubscriberTechnologyEnumWebControl()
    {
        this(true);
    }
    
    /**
     * SEts the autoPreview and the Predicate
     * @param autoPreview
     */
    public SubscriberTechnologyEnumWebControl(boolean autoPreview)
    {
        super(TechnologyEnum.COLLECTION, autoPreview);
        setPredicate(new TechnologyTypeEnumPredicate());
    }
    
    public Object fromWeb(Context ctx, ServletRequest req, String name)
    {
        Object result = super.fromWeb(ctx, req, name);
        // Ensure that web control will always return a default value if the enum is not empty.
        if (result == null && getEnumCollection(ctx).size()>0)
        {
            return getEnumCollection(ctx).get((short) 0);
        }
        return result;
    }

}
