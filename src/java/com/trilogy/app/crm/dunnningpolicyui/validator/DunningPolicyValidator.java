/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.dunnningpolicyui.validator;

import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.dunning.DunningPolicy;
import com.trilogy.app.crm.dunning.DunningPolicyXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @since 10.2
 * @author shyamrag.charuvil@redknee.com
 */
public class DunningPolicyValidator implements Validator{

	private DunningPolicyValidator(){}
	private static DunningPolicyValidator instance_ = null;
	
	public static Validator instance()
	{
		if(instance_ == null)
		{
			instance_ = new DunningPolicyValidator();
		}
		return instance_;
	}
	
	@Override
	public void validate(Context ctx, Object obj)
			throws IllegalStateException 
	{
		DunningPolicy dunningPolicy = (DunningPolicy) obj;
		
		validateDunningPolicy(ctx,dunningPolicy);
	}

	private void validateDunningPolicy(Context ctx, DunningPolicy dunningPolicy) throws IllegalStateException 
	{
		int spid = dunningPolicy.getSpid();
		String name = dunningPolicy.getName();
		long dunningId = dunningPolicy.getDunningPolicyId();

		List<DunningPolicy> dunningPolicyList = null;
		try 
		{
			dunningPolicyList = (List<DunningPolicy>) HomeSupportHelper.get(ctx).getBeans(ctx, DunningPolicy.class, new EQ(DunningPolicyXInfo.SPID,spid));
			if(dunningPolicyList == null || dunningPolicyList.isEmpty())
			{
				throw new HomeException("No Dunning Policy found for this SPID");
			}
		} 
		catch (HomeException e) 
		{
			String cause = "Unable to retrieve dunning Policy for spid="+spid;
			StringBuilder sb = new StringBuilder();
			sb.append(cause);
			sb.append(": ");
			sb.append(e.getMessage());
			LogSupport.major(ctx, this, sb.toString(), e);
		}
		
		Iterator<DunningPolicy> dunningPolicyIterator = dunningPolicyList.iterator();
		while(dunningPolicyIterator.hasNext())
		{
			DunningPolicy dunnPolicy = dunningPolicyIterator.next();
			if(dunningId != dunnPolicy.getDunningPolicyId() && name.equals(dunnPolicy.getName()))
			{
				throw new IllegalStateException("The name="+name+" is already used for the same SPID="+spid);
			}
		}		
	}
}

