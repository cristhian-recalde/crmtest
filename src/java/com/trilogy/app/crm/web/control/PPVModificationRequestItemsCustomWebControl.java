/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 *
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.PPVModificationRequest;
import com.trilogy.app.crm.bean.PPVModificationRequestItems;
import com.trilogy.app.crm.bean.PPVModificationRequestItemsWebControl;
import com.trilogy.app.crm.bean.ServiceFee2XInfo;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServicePackageFeeXInfo;
import com.trilogy.app.crm.bean.ServicePackageXInfo;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bundle.BundleFeeXInfo;
import com.trilogy.app.crm.bundle.BundleProfileHome;
import com.trilogy.app.crm.bundle.BundleProfileXInfo;
import com.trilogy.app.crm.bundle.BundleSegmentEnum;
import com.trilogy.app.crm.support.WebControlSupport;
import com.trilogy.app.crm.support.WebControlSupportHelper;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.LRUSelectCachingHome;
import com.trilogy.framework.xhome.web.renderer.DefaultDetailRenderer;
import com.trilogy.framework.xhome.web.renderer.DetailRenderer;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 *
 * Custom web control for PPV Modification Request Items
 *
 * @author Marcio Marques
 * @since 9.2
 *
 */
public class PPVModificationRequestItemsCustomWebControl extends PPVModificationRequestItemsWebControl
{
	  public static final WebControl serviceFees_wc = new PPVModificationServiceFee2TableWebControl(false);

	  public static final WebControl newServiceFees_wc = new PPVModificationServiceFee2TableWebControl(true);

	  public static final WebControl bundleFees_wc = new PPVModificationBundleFeeTableWebControl(false);

	  public static final WebControl newBundleFees_wc = new PPVModificationBundleFeeTableWebControl(true);

	  public WebControl getServiceFeesWebControl() { return serviceFees_wc; }
	  
	  public WebControl getNewServiceFeesWebControl() { return newServiceFees_wc; }

	  public WebControl getBundleFeesWebControl() { return bundleFees_wc; }
	  
	  public WebControl getNewBundleFeesWebControl() { return newBundleFees_wc; }

	  public PPVModificationRequestItemsCustomWebControl()
    {
        super();
    }

    /**
     * @see com.redknee.app.crm.bean.ServicePackageWebControl#toWeb(com.redknee.framework.xhome.context.Context, java.io.PrintWriter, java.lang.String, java.lang.Object)
     */
    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        ctx=ctx.createSubContext();
        ctx.put(PPVModificationRequestItems.class, obj);
        
//        out.println("<script type = \"text/javascript\" src = \"jquery-1.6.4.js\"></script>");
        WebControlSupport webControlSupport = WebControlSupportHelper.get(ctx);
        webControlSupport.hideProperties(ctx, new PropertyInfo[]
                                                               {
                ServicePackageXInfo.ID,
                ServicePackageXInfo.TYPE,
                ServicePackageXInfo.NAME,
                ServicePackageXInfo.SPID,
                ServicePackageXInfo.CHARGING_LEVEL,
                ServicePackageXInfo.CHARGING_MODE,
                ServicePackageXInfo.RECURRING_RECHARGE,
                ServicePackageXInfo.ADJUSTMENT_GLCODE,
                ServicePackageXInfo.ADJUSTMENT_TYPE_DESCRIPTION,
                ServicePackageXInfo.ADJUSTMENT_INVOICE_DESCRIPTION,
                ServicePackageXInfo.TAX_AUTHORITY,
                ServicePackageXInfo.TOTAL_CHARGE,
                BundleFeeXInfo.START_DATE,
                BundleFeeXInfo.END_DATE,
                BundleFeeXInfo.PAYMENT_NUM,
                BundleFeeXInfo.NEXT_RECURRING_CHARGE_DATE,
                ServiceFee2XInfo.CLTC_DISABLED,
                ServicePackageFeeXInfo.CLTC_DISABLED
                                                               }
        );
// Javascript embedded into BundleFee and ServiceFee web-control control the auto-selection and need them to be fields.
//        webControlSupport.setPropertiesReadOnly(ctx, new PropertyInfo[]
//                {
//				BundleFeeXInfo.SERVICE_PERIOD,
//				ServiceFee2XInfo.SERVICE_PERIOD
//                }
//        );

        ctx.put("ACTIONS",false);

        ctx.put(DetailRenderer.class, new DefaultDetailRenderer(ctx)
            {
               @Override
            public void Table(final Context ctx, PrintWriter out, String title)
               {
                  TREnd(ctx, out);
               }

               @Override
            public void TableEnd(final Context ctx, PrintWriter out, String footer)
               {
                  TR(ctx, out,"");
               }
            });

        Context subCtx = filterAndCacheBundles(ctx);
        subCtx = filterAndCacheServices(subCtx);
        super.toWeb(subCtx, out, name, obj);
    }

    private Context filterAndCacheServices(Context ctx)
    {
        Context subCtx = ctx;
        PPVModificationRequest request = (PPVModificationRequest) ctx.get(BEAN);
        PricePlan pp = request.getPricePlan(ctx);
        if (pp!=null)
        {
            subCtx = ctx.createSubContext();
            Home home = (Home) ctx.get(ServiceHome.class);
            And filter = new And();
            filter.add(new EQ(ServiceXInfo.SPID, pp.getSpid()));
            home = home.where(subCtx, filter);
            subCtx.put(ServiceHome.class, new LRUSelectCachingHome(subCtx, ServiceHome.class.getName(),home)) ;
        }
        return subCtx;
    }

    private Context filterAndCacheBundles(Context ctx)
    {
        Context subCtx = ctx;
        PPVModificationRequest request = (PPVModificationRequest) ctx.get(BEAN);
        PricePlan pp = request.getPricePlan(ctx);
        if (pp!=null)
        {
            subCtx = ctx.createSubContext();
            Home home = (Home) ctx.get(BundleProfileHome.class);
            Or filter = new Or();
            filter.add(new EQ(BundleProfileXInfo.SEGMENT, BundleSegmentEnum.HYBRID));
            if (SubscriberTypeEnum.PREPAID.equals(pp.getPricePlanType()))
            {
                filter.add(new EQ(BundleProfileXInfo.SEGMENT, BundleSegmentEnum.PREPAID));
            }
            else
            {
                filter.add(new EQ(BundleProfileXInfo.SEGMENT, BundleSegmentEnum.POSTPAID));
            }
            And and = new And();
            and.add(filter);
            and.add(new EQ(BundleProfileXInfo.SPID, pp.getSpid()));
            
            home = home.where(subCtx, and);
            subCtx.put(BundleProfileHome.class, new LRUSelectCachingHome(subCtx, BundleProfileHome.class.getName(),home)) ;
        }
        return subCtx;
    }


    
	@Override
	public void fromWeb(Context ctx, Object obj, ServletRequest req, String name)
    {
		super.fromWeb(ctx, obj, req, name);
	}

}
