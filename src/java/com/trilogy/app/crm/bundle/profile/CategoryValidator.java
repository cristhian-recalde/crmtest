package com.trilogy.app.crm.bundle.profile;

import java.util.Iterator;
import java.util.Map;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.core.BundleCategoryAssociation;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.BundleCategory;
import com.trilogy.app.crm.bundle.BundleCategoryAssociationXInfo;
import com.trilogy.app.crm.bundle.BundleProfileXInfo;
import com.trilogy.app.crm.bundle.service.CRMBundleCategory;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

/**
 * Ensure the bundle category is a valid ID
 * @author Candy Wong
 */
public class CategoryValidator
   implements Validator
{
    /**
     * @param ctx 
     * @param obj 
     * @throws IllegalStateException 
     */
   public void validate(Context ctx, Object obj)
      throws IllegalStateException
   {
      CompoundIllegalStateException el = new CompoundIllegalStateException();
      BundleProfile bundle = (BundleProfile)obj;
      CRMBundleCategory service = (CRMBundleCategory) ctx.get(CRMBundleCategory.class);

      Iterator<Map.Entry<?, BundleCategoryAssociation>> iter = bundle.getBundleCategoryIds().entrySet().iterator();
      try
      {
          while (iter.hasNext())
          {
              BundleCategoryAssociation association = iter.next().getValue();
              try
              {
                  BundleCategory category = service.getCategory(ctx, association.getCategoryId());
       
                  if (category == null)
                  {
                      el.thrown(new IllegalPropertyArgumentException(BundleCategoryAssociationXInfo.CATEGORY_ID, "Category "
                              + association.getCategoryId() + " does not exist in service provider " + bundle.getSpid()));
                  }
              }
              catch (Exception hEx)
              {
                  el.thrown(new IllegalPropertyArgumentException(BundleCategoryAssociationXInfo.CATEGORY_ID, "Cannot retrieve category "
                          + association.getCategoryId() + " in service provider " + bundle.getSpid()));
              }
         }
          
          if (bundle.getBundleCategoryIds().size()==0)
          {
              el.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.BUNDLE_CATEGORY_IDS, "Bundle must be associated to at least one category"));
          }
          else if (bundle.isCrossService() && !LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.CROSS_UNIT_BUNDLES))
          {
              el.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.BUNDLE_CATEGORY_IDS, "Cross service bundles not supported"));
          }
          else if (bundle.getBundleCategoryIds().size()==1 && bundle.isCrossService())
          {
              el.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.BUNDLE_CATEGORY_IDS, "Cross service bundles must be associated with more than one category"));
          }
      }
      finally
      {
         el.throwAll();
      }
   }
}
