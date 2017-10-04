/*
 *  RatePlanAssociationRemoveHome.java
 *
 *  Author : victor.stratan@redknee.com
 *  Date   : Apr 27, 2006
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

package com.trilogy.app.crm.bundle.profile;

import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.rateplan.RatePlanAssociationHome;
import com.trilogy.app.crm.bundle.rateplan.RatePlanAssociationXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * Delete Price Plan Association if Bundle is deleted
 */
public class RatePlanAssociationRemoveHome
   extends HomeProxy
{
   public RatePlanAssociationRemoveHome(Context ctx, Home delegate)
   {
      super(ctx, delegate);
   }

   public void remove(Context ctx, Object obj)
      throws HomeException
   {
      BundleProfile bundle = (BundleProfile) obj;

      super.remove(ctx, obj);

      Home assocHome = (Home)ctx.get(RatePlanAssociationHome.class);

      assocHome.removeAll(
            ctx, 
            new And()
            .add(new EQ(RatePlanAssociationXInfo.BUNDLE_ID, Long.valueOf(bundle.getBundleId())))
            .add(new EQ(RatePlanAssociationXInfo.SPID, Integer.valueOf(bundle.getSpid()))));
   }
}
