package com.trilogy.app.crm.bundle.category;

import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.BundleCategory;
import com.trilogy.app.crm.support.BundleSupportHelper;

/**
 * Ensure BundleCategory cannot be deleted if it is used by
 * BundleProfile
 */
public class NoBundleRemoveHome
   extends HomeProxy
{
   public NoBundleRemoveHome(Context ctx, Home delegate)
   {
      super(ctx, delegate);
   }

   @Override
public void remove(final Context ctx, final Object obj) throws HomeException
   {
       final BundleCategory cat = (BundleCategory) obj;
       
       Collection<BundleProfile> collection = null;
       
       try
       {
           collection = BundleSupportHelper.get(ctx).getBundleByCategoryId(ctx, cat.getCategoryId());
       }
       catch (Exception e)
       {
           throw new HomeException("Error verifying if Bundle Category can be removed: " + e.getMessage());
       }
       
       if (collection==null || collection.size()==0)
       {
           super.remove(ctx, obj);
       }
       else
       {
           throw new HomeException("Cannot remove Bundle Category " + cat.getCategoryId() + " because it is used by Bundle Profiles");
       }
   }
}
