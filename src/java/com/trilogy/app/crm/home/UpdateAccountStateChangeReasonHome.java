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

import java.util.Collection;

import com.trilogy.app.crm.bean.AccountReasonCodeMapping;
import com.trilogy.app.crm.bean.AccountStateChangeReason;
import com.trilogy.app.crm.bean.AccountStateChangeReasonHome;
import com.trilogy.app.crm.bean.AccountStateChangeReasonXInfo;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author skambab
 *
 */
public class UpdateAccountStateChangeReasonHome extends HomeProxy{
	
	public UpdateAccountStateChangeReasonHome(Context ctx, Home delegate)
	{
		super(ctx, delegate);
	}
	/*public UpdateAccountStateChangeReasonHome() {
	super();
	}
	*/
	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
	HomeInternalException
	{
		LogSupport.debug(ctx, this,"entered into create method of updateAccountStatechangeReasonHome");
		AccountStateChangeReason ascrBean = (AccountStateChangeReason)obj;
		if((ascrBean!=null)&&(ascrBean.getID()==0))
		{
			ascrBean.setID((int) getNextIdentifier(ctx));
		}
		return super.create(ctx, ascrBean);
	}
	
	@Override
	public Object store(Context ctx, Object obj) throws HomeException,HomeInternalException
	{
		if((obj instanceof AccountStateChangeReason) &&(obj !=null))
		{
			AccountStateChangeReason ascrBean = (AccountStateChangeReason)obj;
			Home home = (Home) ctx.get(AccountStateChangeReasonHome.class);
			And filter = new And();

			filter.add(new EQ(AccountStateChangeReasonXInfo.SPID,ascrBean.getSpid()));
			filter.add(new EQ(AccountStateChangeReasonXInfo.REASON_CODE,ascrBean.getReasonCode()));
			//AccountStateChangeReason ascrDBBean = (AccountStateChangeReason) home.find(ctx, filter);
			Collection<AccountStateChangeReason> col = home.select(ctx, filter);
			if(col.size()>0)
			{
				for(AccountStateChangeReason ar :col)
				{
					if((ar.getReasonCode().equals(ascrBean.getReasonCode()))&&(ar.getID()==ascrBean.getID()))
					{
						super.store(ctx, ascrBean);
						//break;
						
						//return super.store(ctx, ascrBean);
						
					}else
					{
						throw new HomeException("Reason Code already exists please provide new reason code");
					}
				}
			}else
			{
				return super.store(ctx, ascrBean);
			}
			
			
		}
		
		LogSupport.debug(ctx, this,"entered into store method of updateAccountStatechangeReasonHome");
		return super.store(ctx, obj);
	}
	
	 private long getNextIdentifier(Context ctx) throws HomeException
	 {
		 IdentifierSequenceSupportHelper.get((Context)ctx).ensureSequenceExists(ctx, IdentifierEnum.ACCOUNTREASONCODE_ID, 1, Long.MAX_VALUE);
		    
	        return IdentifierSequenceSupportHelper.get((Context)ctx).getNextIdentifier(ctx, IdentifierEnum.ACCOUNTREASONCODE_ID, null);
	 }

}
