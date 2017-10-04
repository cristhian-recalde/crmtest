/*
 * Copyright (c) 2007, REDKNEE.com. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of REDKNEE.com.
 * ("Confidential Information"). You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement you entered
 * into with REDKNEE.com.
 * 
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * REDKNEE.COM SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */package com.trilogy.app.crm.api.generic.entity.interceptor;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.api.ApiSupport;
import com.trilogy.app.crm.api.generic.entity.bean.Interceptor;
import com.trilogy.app.crm.support.HomeSupportHelper;


public class DefaultGenericEntityInterceptor extends AbstractGenericEntityInterceptor implements Interceptor
{

    @Override
    public void preProcess(Context ctx, Object obj)
    {
    }


    @Override
    public void postProcess(Context ctx, Object obj)
    {
    }

    @Override
	public Home getHome(Context ctx, Object obj) {
		Home home  = null;
		if(obj != null)
		{
			try 
			{
				home = HomeSupportHelper.get(ctx).getHome(ctx, obj.getClass());
				return ApiSupport.injectAPIUpdateEntityHomeIntoGenericEntityPipeline(ctx, home, obj.getClass().getName());
			}
			catch (HomeException e) 
			{
				LogSupport.minor(ctx, this, "HomeException encountered while trying to get Home for class :" + obj.getClass(), e);
			}
		}
		return home;
	}
}
