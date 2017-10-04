/*
 *  RemoveRatePlanAssociationHome.java
 *
 *  Author : candy.wong@redknee.com
 *  Date   : Oct 18, 2006
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.bundle.category;

import com.trilogy.app.crm.bundle.rateplan.RatePlanAssociationHome;
import com.trilogy.app.crm.bundle.rateplan.RatePlanAssociationXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.app.crm.bundle.BundleCategory;

/**
 * Delete Price Plan Association if Category is deleted
 */
public class RemoveRatePlanAssociationHome
   extends HomeProxy
{
   public RemoveRatePlanAssociationHome(Context ctx, Home delegate)
   {
      super(ctx, delegate);
   }

   public void remove(Context ctx, Object obj)
      throws HomeException
   {
      BundleCategory category = (BundleCategory)obj;

      super.remove(ctx, obj);

      Home assocHome = (Home)ctx.get(RatePlanAssociationHome.class);

      assocHome.removeAll(
            ctx, 
            new And()
            .add(new EQ(RatePlanAssociationXInfo.CATEGORY_ID, Integer.valueOf(category.getCategoryId())))
            .add(new EQ(RatePlanAssociationXInfo.SPID, Integer.valueOf(category.getSpid()))));
   }
}
