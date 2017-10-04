package com.trilogy.app.crm.filter;
import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.entity.ByClassPredicate;
import com.trilogy.framework.xhome.entity.EntityInfoHome;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;



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

/**
 * Removes all normal predicates from the delegate's view of the EntityInfoHome
 *
 * @author aaron.gourley@redknee.com
 * @since 8.6
 */
public class AccountRequiredFieldPredicateFilteringWebControl extends ProxyWebControl
{
    public AccountRequiredFieldPredicateFilteringWebControl(WebControl delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        Context sCtx = ctx.createSubContext();
        
        Or filter = new Or();
        filter.add(new ByClassPredicate(AccountRequiredFieldPredicate.class));
        filter.add(new Not(new ByClassPredicate(Predicate.class)));
        
        Home entityHome = (Home) ctx.get(EntityInfoHome.class);
        entityHome = entityHome.where(ctx, filter);
        
        sCtx.put(EntityInfoHome.class, entityHome);
        
        super.toWeb(sCtx, out, name, obj);
    }
}
