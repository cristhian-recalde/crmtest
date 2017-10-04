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

import com.trilogy.app.crm.bean.RefundAdjustmentTypeMapping;
import com.trilogy.app.crm.bean.RefundAdjustmentTypeMappingHome;
import com.trilogy.app.crm.bean.RefundAdjustmentTypeMappingXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

public class RASValidator implements Validator{
	
	
	

	@Override
	public void validate(Context ctx, Object obj) throws IllegalStateException
	{

		RefundAdjustmentTypeMapping radjbean = (RefundAdjustmentTypeMapping)obj;
		if(radjbean!=null)
		{
			CompoundIllegalStateException cise = new CompoundIllegalStateException();
			try
			{
				
				Home home = (Home) ctx.get(RefundAdjustmentTypeMappingHome.class);
				And filter = new And();
				String str = "Entity has been already present please select another account state "+radjbean.getAccountState()+" & reason code " ;
				StringBuilder message = new StringBuilder();
				message.append(str); 
			
				filter.add(new EQ(RefundAdjustmentTypeMappingXInfo.ACCOUNT_STATE,radjbean.getAccountState()));
				//filter.add(new EQ(RefundAdjustmentTypeMappingXInfo.IS_DEFAULT,Boolean.TRUE));
				
			    Collection<RefundAdjustmentTypeMapping> asrCollection = home.select(ctx, filter);
			    if(asrCollection.size()>0)
			    {
			    	for(RefundAdjustmentTypeMapping ramBean : asrCollection)
			    	{
			    		if(!(ramBean.getID()==radjbean.getID()))
			    		{
			    			cise.thrown(new HomeException(message.append(radjbean.getReasonCode()).toString()));;
			    		}else
			    		{
			    			break;
			    		}
			    		
			    	}
			    	
			    	
			    }
			    
			}catch (HomeException e)
			{
				LogSupport.major(ctx, this, "Exception got while retrieving the data from the table Refundadjustmenttypemapping" );
				e.printStackTrace();
			}
		cise.throwAll();
		}
		
	
		
	}

}
