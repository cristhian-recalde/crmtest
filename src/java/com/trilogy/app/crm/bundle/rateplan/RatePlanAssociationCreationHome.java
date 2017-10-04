package com.trilogy.app.crm.bundle.rateplan;

import java.util.Iterator;
import java.util.Map;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.core.BundleCategoryAssociation;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.BundleCategory;
import com.trilogy.app.crm.bundle.service.CRMBundleCategory;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * create a RatePlanAssocation upon successful BundleProfileApi creation
 */
public class RatePlanAssociationCreationHome
   extends HomeProxy
{
   public RatePlanAssociationCreationHome(Context ctx, Home delegate)
   {
      super(ctx, delegate);
   }

   public Object create(Context ctx, Object bean)
      throws HomeException
   {
      BundleProfile bundle = (BundleProfile)super.create(ctx, bean);

      Home assocHome = (Home)ctx.get(RatePlanAssociationHome.class);
      CRMBundleCategory catService = (CRMBundleCategory)ctx.get(CRMBundleCategory.class);

      Iterator<Map.Entry<?, BundleCategoryAssociation>> iter = bundle.getBundleCategoryIds().entrySet().iterator();
      while (iter.hasNext())
      {
          BundleCategoryAssociation association = iter.next().getValue();
          RatePlanAssociation assoc = null;
          try
          {
             assoc = (RatePlanAssociation)XBeans.instantiate(RatePlanAssociation.class, ctx);
          }
          catch (Throwable t)
          {
             assoc = new RatePlanAssociation();
          }
    
          BundleCategory category = null;
          
          try
          {
              category = catService.getCategory(ctx, association.getCategoryId()); 
    
              assoc.setSpid(bundle.getSpid());
              assoc.setBundleId(bundle.getBundleId());
              assoc.setCategoryId(association.getCategoryId());
              if (category != null)
              {
                  assoc.setCategoryDesc(category.getName());
              }
        
              assocHome.create(ctx, assoc);
          }
          catch (Exception e)
          {
              new MinorLogMsg(this,
                      "fail to get the category from bundle " + bundle.getBundleId() + " " + e.getMessage(),
                      e).log(ctx);
    
          }
      }


      return bundle;
   }
}
