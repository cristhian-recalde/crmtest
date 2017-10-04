package com.trilogy.app.crm.inboundfile.validators;

import java.util.Collection;

import com.trilogy.app.crm.bean.FeeAndPenalty;
import com.trilogy.app.crm.bean.FeeAndPenaltyHome;
import com.trilogy.app.crm.bean.FeeAndPenaltyXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;

/**
 * In this validator we are going to do duplicate check
 * if duplicate reason code found for the respective serviceprovider
 * then we are going to throw an error message
 * 
 * @author skambab
 *
 */
public class FeeAndPenaltyStoreValidator implements Validator{

	@Override
	public void validate(Context ctx, Object obj)throws IllegalStateException
	{
		CompoundIllegalStateException cise = new CompoundIllegalStateException();
		if((obj != null)&&(obj instanceof FeeAndPenalty))
		{
			try
			{			
				FeeAndPenalty bean = (FeeAndPenalty)obj;
				Home home = (Home) ctx.get(FeeAndPenaltyHome.class);
				String msg = "Reaosn Code "+bean.getReasonCode()+"is already present with for the service provider "+bean.getSpid()+" please provide different reasoncode";
				And filter = new And();
				filter.add(new EQ(FeeAndPenaltyXInfo.SPID,bean.getSpid()));
				filter.add(new EQ(FeeAndPenaltyXInfo.REASON_CODE, bean.getReasonCode()));				
				FeeAndPenalty dbBean = (FeeAndPenalty) home.find(ctx, filter);
				
				if(((bean != null)&&(dbBean != null))&&((bean.getReasonCode().compareTo(dbBean.getReasonCode())==0)&&(bean.getID()!= dbBean.getID())))
				{
					cise.thrown(new HomeException(msg));
				}
			} catch (HomeException e)
			{
				e.printStackTrace();
			}
		}
		cise.throwAll();
	}

}
