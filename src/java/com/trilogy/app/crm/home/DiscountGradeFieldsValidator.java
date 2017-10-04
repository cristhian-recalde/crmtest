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

import com.trilogy.app.crm.bean.DiscountGrade;
import com.trilogy.app.crm.bean.DiscountGradeHome;
import com.trilogy.app.crm.bean.DiscountGradeXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author vikash.kumar@redknee.com
 * @since 2016-10-16
 * Purpose: To make sure in DiscountGrade GUI for a SPID there should be 
 * 			only one default 'Discount Grade'
 *
 */
public class DiscountGradeFieldsValidator implements Validator{
	public static String MODULE = DiscountGradeFieldsValidator.class.getName();

	@Override
	public void validate(Context ctx, Object obj) throws IllegalStateException
	{
		
		DiscountGrade discountGradeBean= (DiscountGrade)obj;
		CompoundIllegalStateException cise = new CompoundIllegalStateException();

		if(discountGradeBean!=null)
		{
			Home home = (Home) ctx.get(DiscountGradeHome.class);
			
			And filter = new And();
			filter.add(new EQ(DiscountGradeXInfo.SPID,discountGradeBean.getSpid()));
			filter.add(new EQ(DiscountGradeXInfo.IS_DEFAULT,Boolean.TRUE));
			
			try
			{
				Collection<DiscountGrade> col = home.select(ctx, filter);
				if(col.size()>0)
				{
					for(DiscountGrade dbBean:col)
					  {
						String message = "Default Discount Grade ( "+ dbBean.getSpid() + ", " +  dbBean.getDescription()+" ) has been created, please create non-default grade.";
						String errMessage = dbBean.getDiscountGrade() + " already exist, please create other Discount Grade";
						
					    if(dbBean.getSpid()== discountGradeBean.getSpid() && (dbBean.getIsDefault() == discountGradeBean.getIsDefault()))
					    {
					    	LogSupport.info(ctx, MODULE,
									"validateRecord : Unable to create entry in DiscountGrade for " + discountGradeBean.getDescription());
							
					    	cise.thrown(new HomeException(message));
					    }
					    if(dbBean.getSpid()== discountGradeBean.getSpid() && (dbBean.getDiscountGrade().equals(discountGradeBean.getDiscountGrade())))
					    {
					    	LogSupport.info(ctx, MODULE,
									"validateRecord : Unable to create entry in DiscountGrade for " + discountGradeBean.getDescription());
							
					    	cise.thrown(new HomeException(errMessage));
					    }
					    else{
					    	LogSupport.info(ctx, MODULE,
									"validateRecord : creating entry in DiscountGrade for " + discountGradeBean.getDescription());	
					    }
					    
					  }
					
				}
			} catch (HomeException e)
			{
				
				e.printStackTrace();
			}
		}
		
		cise.throwAll();
		
	}

}
