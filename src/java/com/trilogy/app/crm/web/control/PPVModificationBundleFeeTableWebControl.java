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
import java.util.Set;

import com.trilogy.app.crm.bean.PPVModificationRequestItems;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bundle.BundleFee;
import com.trilogy.app.crm.bundle.BundleFeeTableWebControl;
import com.trilogy.app.crm.bundle.BundleFeeXInfo;
import com.trilogy.app.crm.bundle.BundleProfileHome;
import com.trilogy.app.crm.bundle.BundleProfileXInfo;
import com.trilogy.app.crm.support.WebControlSupport;
import com.trilogy.app.crm.support.WebControlSupportHelper;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.web.renderer.DefaultDetailRenderer;
import com.trilogy.framework.xhome.web.renderer.DetailRenderer;
import com.trilogy.framework.xhome.webcontrol.AbstractTableWebControl;
import com.trilogy.framework.xhome.webcontrol.EnumWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xhome.xenum.EnumCollection;

/**
 * Custom web control for PPV Modification Service Fees
 * 
 * @author Marcio Marques
 * @since 9.2
 */
public class PPVModificationBundleFeeTableWebControl extends BundleFeeTableWebControl
{

    public PPVModificationBundleFeeTableWebControl(boolean newBundles)
    {
        super();
        newBundles_ = newBundles;
    }

    private boolean         newBundles_;

    public final WebControl servicePreference_wc = new EnumWebControl(ServicePreferenceEnum.COLLECTION)
                                                 {
                                                     public EnumCollection getEnumCollection(Context ctx)
                                                     {
                                                         Object bean = ctx.get(BEAN);
                                                         EnumCollection enumc = enumc_;
                                                         if (bean != null && bean instanceof BundleFee)
                                                         {
                                                             if (newBundles_
                                                                     || !ServicePreferenceEnum.MANDATORY
                                                                             .equals(((BundleFee) bean)
                                                                                     .getServicePreference()))
                                                             {
                                                                 enumc = new EnumCollection(
                                                                         new com.redknee.framework.xhome.xenum.Enum[] {
                                                                                 ServicePreferenceEnum.DEFAULT, ServicePreferenceEnum.OPTIONAL });
                                                             }
                                                             else if (!newBundles_
                                                                     && ServicePreferenceEnum.MANDATORY
                                                                             .equals(((BundleFee) bean)
                                                                                     .getServicePreference()))
                                                             {
                                                                 enumc = new EnumCollection(
                                                                         new com.redknee.framework.xhome.xenum.Enum[] { ServicePreferenceEnum.MANDATORY });
                                                             }
                                                         }
                                                         return enumc.where(ctx, getPredicate());
                                                     }

                                                 };


    public WebControl getServicePreferenceWebControl()
    {
        return servicePreference_wc;
    }


    /**
     * @see com.redknee.app.crm.bean.ServicePackageWebControl#toWeb(com.redknee.framework.xhome.context.Context,
     *      java.io.PrintWriter, java.lang.String, java.lang.Object)
     */
    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        ctx = ctx.createSubContext();
//        out.println("<script type = \"text/javascript\" src = \"jquery-1.6.4.js\"></script>");
        WebControlSupport webControlSupport = WebControlSupportHelper.get(ctx);

        if (!newBundles_)
        {
            ctx.put(NUM_OF_BLANKS, 0);
            ctx.put(AbstractTableWebControl.HIDE_CHECKBOX, Boolean.TRUE);
            ctx.put(AbstractTableWebControl.DISABLE_NEW, Boolean.TRUE);

            webControlSupport.setPropertiesReadOnly(ctx, new PropertyInfo[] { BundleFeeXInfo.ID,
                    BundleFeeXInfo.PAID_BY_CONTRACT });
        }
        else
        {
            PPVModificationRequestItems items = (PPVModificationRequestItems) ctx
                    .get(PPVModificationRequestItems.class);
            if (items != null)
            {
                Set<Long> ids = items.getBundleFees(ctx).keySet();
                Home bundleProfileHome = (Home) ctx.get(BundleProfileHome.class);
                ctx.put(BundleProfileHome.class,
                        bundleProfileHome.where(ctx, new Not(new In(BundleProfileXInfo.BUNDLE_ID, ids))));
            }
        }

        ctx.put("ACTIONS", false);

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
                TR(ctx, out, "");
            }
        });

        super.toWeb(ctx, out, name, obj);
    }

}
