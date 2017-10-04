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
 */

package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * A Home decorator which set Subscriber Hlr id with the Hlr Id configured at Spid level. 
 * 
 * @author piyush.shirke@redknee.com
 * 
 */
public class SubscriberHLRHome extends HomeProxy {
	
	public SubscriberHLRHome(Home delegate){
		super(delegate);
	}
	
	 @Override
	 public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException{
		
		LogSupport.debug(ctx, this, "SubscriberPipeline[create].....");
		Subscriber sub = (Subscriber) obj;
		CRMSpid spid = SpidSupport.getCRMSpid(ctx, sub.getSpid());
		if(spid != null){
			short hlrId = (short) spid.getDefaultHlrId();
			sub.setHlrId(hlrId);
		}
		return super.create(ctx, sub);
	}
	
}
