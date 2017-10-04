/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.ui.BundleProfile;
import com.trilogy.app.crm.bean.ui.BundleProfileHome;
import com.trilogy.app.crm.bean.ui.BundleProfileKeyWebControl;
import com.trilogy.app.crm.bean.ui.BundleProfileXInfo;
import com.trilogy.app.crm.bundle.FlexTypeEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;


/**
 * @author bpandey
 * 
 *         Customized Web Control for Flex bundle root.
 */
public class CustomizedFlexBundleProfileKeyWebControl extends BundleProfileKeyWebControl
{

    FlexTypeEnum flexType_;


    public CustomizedFlexBundleProfileKeyWebControl(FlexTypeEnum flexType)
    {
        super();
        flexType_ = flexType;
    }


    public CustomizedFlexBundleProfileKeyWebControl(int listSize, boolean autoPreview, Object optionalValue,
            FlexTypeEnum flexType)
    {
        super(listSize, autoPreview, optionalValue);
        flexType_ = flexType;
    }


    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        super.toWeb(wrapContext(ctx), out, name, obj);
    }


    @Override
    public Context wrapContext(final Context ctx)
    {
        BundleProfile bundle = (BundleProfile) ctx.get(AbstractWebControl.BEAN);
        Context subContext = ctx.createSubContext();
        And filter = new And().add(new EQ(BundleProfileXInfo.FLEX, true))
                .add(new EQ(BundleProfileXInfo.FLEX_TYPE, flexType_))
                .add(new EQ(BundleProfileXInfo.SPID, bundle.getSpid()))
                .add(new EQ(BundleProfileXInfo.SEGMENT, bundle.getSegment()));
        Home home = (Home) ctx.get(BundleProfileHome.class);
        subContext.put(BundleProfileHome.class, home.where(subContext, filter));
        return subContext;
    }
}
