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
package com.trilogy.app.crm.web.control;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.util.snippet.context.ContextUtils;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.DiscountGradeHome;
import com.trilogy.app.crm.bean.DiscountGradeKeyWebControl;
import com.trilogy.app.crm.bean.DiscountGradeXInfo;

/**
 * @author vikash.kumar@redknee.com
 *
 * Customized Web Control for DiscountGrade to filter out home based on SPID.
 */
public class CustomDiscountGradeKeyWebControl extends DiscountGradeKeyWebControl {
     
    public CustomDiscountGradeKeyWebControl()
    {
        super();
    }

    public CustomDiscountGradeKeyWebControl(boolean autoPreview)
    {
        super(autoPreview);
    }

    public CustomDiscountGradeKeyWebControl(int listSize)
    {
        super(listSize);
    }

    public CustomDiscountGradeKeyWebControl(int listSize, boolean autoPreview)
    {
        super(listSize, autoPreview);
    }

    public CustomDiscountGradeKeyWebControl(int listSize, boolean autoPreview, boolean isOptional)
    {
        super(listSize, autoPreview, isOptional);
    }

    public CustomDiscountGradeKeyWebControl(int listSize, boolean autoPreview, boolean isOptional, boolean allowCustom)
    {
        super(listSize, autoPreview, isOptional, allowCustom);
    }

    public CustomDiscountGradeKeyWebControl(int listSize, boolean autoPreview, Object optionalValue)
    {
        super(listSize, autoPreview, optionalValue);
    }

    public CustomDiscountGradeKeyWebControl(int listSize, boolean autoPreview, Object optionalValue, boolean allowCustom)
    {
        super(listSize, autoPreview, optionalValue, allowCustom);
    }
    
    public Home getHome(Context ctx) {
		final Home originalHome = (Home) ctx.get(DiscountGradeHome.class);
		Account account = ContextUtils.getBeanInContextByType(ctx, AbstractWebControl.BEAN, Account.class);
		return originalHome.where(ctx, new EQ(DiscountGradeXInfo.SPID, Integer.valueOf(account.getSpid())));
	}

}
