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

package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.ChargingTemplate;
import com.trilogy.app.crm.bean.ui.DiscountRule;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * Sets ID and identifier for this Discount Rule Template using an
 * IdentifierSequence. 
 * 
 * @author ishan.batra
 * @since  9.9
 */
public class DiscountRuleIDSettingHome extends HomeProxy 
{
	private static final long startValue = 100000;

	public DiscountRuleIDSettingHome(Context ctx, Home delegate)
	{
		super(ctx, delegate);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
	HomeInternalException 
	{

		DiscountRule discountRule = (DiscountRule)obj;

		if(discountRule.getIdentifier() == 0)
		{
			IdentifierSequenceSupportHelper.get(ctx).ensureSequenceExists(ctx, IdentifierEnum.DISCOUNT_RULE_ID,
					startValue, Long.MAX_VALUE);
			
			long ruleId = IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(
					ctx,
					IdentifierEnum.DISCOUNT_RULE_ID,
					null);
			
			discountRule.setIdentifier(ruleId);
		}

		if(LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, this, "Discount Rule ID set to: " + discountRule.getIdentifier());
		}
		return super.create(ctx, obj);
	}


}
