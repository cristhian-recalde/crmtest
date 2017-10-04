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

package com.trilogy.app.crm.home.account;

import java.util.Collection;

import com.trilogy.app.crm.bean.AccountStateChangeReason;
import com.trilogy.app.crm.bean.AccountStateChangeReasonHome;
import com.trilogy.app.crm.bean.AccountStateChangeReasonXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;

/**
 * 
 * @author skambab
 *
 */
public class AccountStateChangeReasonFieldsValidator implements Validator{

	@Override
	public void validate(Context ctx, Object obj) throws IllegalStateException
	{
		
		AccountStateChangeReason ascrBean= (AccountStateChangeReason)obj;
		CompoundIllegalStateException cise = new CompoundIllegalStateException();
		ctx.put(HomeOperationEnum.class, HomeOperationEnum.CREATE);
		if(ascrBean!=null)
		{
			Home home = (Home) ctx.get(AccountStateChangeReasonHome.class);
			String message = "Reason code "+ascrBean.getReasonCode()+" has been created, please provide any other reason code";
			And filter = new And();
			filter.add(new EQ(AccountStateChangeReasonXInfo.SPID,ascrBean.getSpid()));
			filter.add(new EQ(AccountStateChangeReasonXInfo.REASON_CODE,ascrBean.getReasonCode()));
			
			try
			{
				Collection<AccountStateChangeReason> col = home.select(ctx, filter);
				if(col.size()>0)
				{
					cise.thrown(new HomeException(message));
				}
			} catch (HomeException e)
			{
				
				e.printStackTrace();
			}
		}
		
		cise.throwAll();
		
	}

}
