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
package com.trilogy.app.crm.web.action;



import com.trilogy.app.crm.bean.ConvergedAccountSubscriberXInfo;
import com.trilogy.app.crm.bean.SearchTypeEnum;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.web.action.EditAction;

/**
 * Provides 'Go TO Account' action when search type='Subscriber'
 * @author Sheetal Thakur
 * 
 */
public class ConvergedAccountGoToAction extends ConvergedGoToAction 
{

	public ConvergedAccountGoToAction()
	{
		super();
	}
	//To be enabled only when search type='Subscriber'
	public boolean isEnabled(Context ctx, Object bean)
    {
        return new EQ(ConvergedAccountSubscriberXInfo.TYPE, SearchTypeEnum.Both).f(ctx, bean); 
    }
}
