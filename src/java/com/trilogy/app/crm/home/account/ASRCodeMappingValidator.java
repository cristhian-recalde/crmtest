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

import com.trilogy.app.crm.bean.AccountReasonCodeMapping;
import com.trilogy.app.crm.bean.AccountReasonCodeMappingHome;
import com.trilogy.app.crm.bean.AccountReasonCodeMappingXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * 
 * @author skambab
 *
 */
public class ASRCodeMappingValidator implements Validator{

	@Override
	public void validate(Context ctx, Object obj)
			throws IllegalStateException
	{
		AccountReasonCodeMapping asrbean = (AccountReasonCodeMapping)obj;
		if(asrbean!=null)
		{
			CompoundIllegalStateException cise = new CompoundIllegalStateException();
			try
			{
				
				Home home = (Home) ctx.get(AccountReasonCodeMappingHome.class);
				And filter = new And();
				String message = "Account state " +asrbean.getAccountState()+ " with reason code "+asrbean.getReasonCode()+" is already present please select another reason code";
				filter.add(new EQ(AccountReasonCodeMappingXInfo.ACCOUNT_STATE,asrbean.getAccountState()));
				filter.add(new EQ(AccountReasonCodeMappingXInfo.REASON_CODE,asrbean.getReasonCode()));
				
			    Collection<AccountReasonCodeMapping> asrCollection = home.select(ctx, filter);
			    if(asrCollection.size()>0)
			    {
			    	
			    	cise.thrown(new HomeException(message));;
			    }
			    
			}catch (HomeException e) {
				
				e.printStackTrace();
			}
		cise.throwAll();
		}
		
	}

}
