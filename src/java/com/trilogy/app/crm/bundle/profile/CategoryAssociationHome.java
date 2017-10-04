package com.trilogy.app.crm.bundle.profile;

import java.util.Iterator;
import java.util.Map;

import com.trilogy.app.crm.bean.core.BundleCategoryAssociation;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.BMBundleCategoryAssociation;
import com.trilogy.app.crm.bundle.BMBundleCategoryAssociationHome;
import com.trilogy.app.crm.bundle.BMBundleCategoryAssociationXInfo;
import com.trilogy.app.crm.bundle.UnitTypeEnum;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;


public class CategoryAssociationHome extends HomeProxy
{
    public CategoryAssociationHome(Context ctx, Home delegate)
    {
       super(ctx, delegate);
    }

    public Object create(Context ctx, Object bean)
     throws HomeException
     {
        BundleProfile bundle = (BundleProfile)super.create(ctx, bean);
    
        Home assocHome = (Home)ctx.get(BMBundleCategoryAssociationHome.class);
    
        Iterator<Map.Entry<?, BundleCategoryAssociation>> iter = bundle.getBundleCategoryIds().entrySet().iterator();
        while (iter.hasNext())
        {
            BundleCategoryAssociation association = iter.next().getValue();
            BMBundleCategoryAssociation assoc = null;
            try
            {
               assoc = (BMBundleCategoryAssociation)XBeans.instantiate(BMBundleCategoryAssociation.class, ctx);
            }
            catch (Throwable t)
            {
               assoc = new BMBundleCategoryAssociation();
            }
            try
            {
                assoc.setBundleId(bundle.getBundleId());
                assoc.setCategoryId(association.getCategoryId());
                assoc.setUnitType(UnitTypeEnum.get((short) association.getType()));
                assoc.setPerUnit(1);
                assoc.setRate(association.getRate());
          
                assocHome.create(ctx, assoc);
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this,
                        "Fail to get the create association between bundle " + bundle.getBundleId() + " and category " + assoc.getCategoryId() + ": " + e.getMessage(),
                        e).log(ctx);
            }
        }  
        return bundle;
    }

    public void remove(Context ctx, Object obj)
       throws HomeException
    {
       BundleProfile bundle = (BundleProfile) obj;

       super.remove(ctx, obj);

       Home catAssocHome = (Home)ctx.get(BMBundleCategoryAssociationHome.class);

       catAssocHome.removeAll(
             ctx, new EQ(BMBundleCategoryAssociationXInfo.BUNDLE_ID, Long.valueOf(bundle.getBundleId())));
    }
}
