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
package com.trilogy.app.crm.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.DiscountGrade;
import com.trilogy.app.crm.bean.DiscountGradeHome;
import com.trilogy.app.crm.bean.DiscountGradeXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.session.SessionInfo;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * @author vikash.kumar@redknee.com
 * @since 2016-10-10
 * Purpose: Home responsible for updating userId in DiscountGrade.
 */
public class DiscountGradeUpdateHome extends HomeProxy
{
	
	private static String MODULE = DiscountGradeUpdateHome.class.getName();

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new DiscountGradeUpdateHome object.
     * 
     * @param ctx
     * @param delegate
     */
    public DiscountGradeUpdateHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


	@Override
	public Object create(final Context ctx, final Object obj) throws HomeException {
		
		DiscountGrade discountGrade = (DiscountGrade) obj;
		Home home = (Home) ctx.get(DiscountGradeHome.class);
		
		SessionInfo session = (SessionInfo) ctx.get(SessionInfo.class);        
        if (session != null)
        {
        	discountGrade.setUserName(session.getPrincipal());
        }
        
        if (home != null) {
			try {
				home.store(ctx, discountGrade);
			} catch (HomeInternalException e) {
				LogSupport.info(ctx, MODULE,
						"createRecord : Unable to add entry to DiscountGrade for UserName: " + discountGrade.getUserName());
			} catch (HomeException e) {
				LogSupport.info(ctx, MODULE,
						"createRecord : Got HomeException, Unable to insert to DiscountGrade for UserName: " + discountGrade.getUserName());
			}
        }
        
		return super.create(ctx, obj);	
	}
	
	@Override
	public Object store(Context ctx, final Object obj) throws HomeException {
		if((obj instanceof DiscountGrade)&& (obj !=null))
		{
			Home home = (Home) ctx.get(DiscountGradeHome.class);
			DiscountGrade discountGrade = (DiscountGrade) obj;
			discountGrade.setLastModified(new Date());
		
			And filter = new And();
			filter.add(new EQ(DiscountGradeXInfo.SPID,discountGrade.getSpid()));
			filter.add(new EQ(DiscountGradeXInfo.IS_DEFAULT,Boolean.TRUE));
		
			Collection<DiscountGrade> col = home.select(ctx, filter);
			if(col.size()>0)
			{
				for(DiscountGrade dbBean:col)
				{
					String message = "Default Discount Grade  "+dbBean.getDescription()+" already exists please de-select default.";
					
					if(dbBean.getSpid()==discountGrade.getSpid() && (dbBean.getIsDefault() == discountGrade.getIsDefault()) && !(dbBean.getDiscountGrade().equals(discountGrade.getDiscountGrade())))
					{
						LogSupport.info(ctx, MODULE,
								"storeRecord : Unable to update entry to DiscountGrade for : " + discountGrade.getDescription());
						throw new HomeException(message);	
					}else
					{
						super.store(ctx, discountGrade);
					}				
				}
			
			}else
			{
				return super.store(ctx, discountGrade);
			}
		}
		return super.store(ctx, obj);
	}
}