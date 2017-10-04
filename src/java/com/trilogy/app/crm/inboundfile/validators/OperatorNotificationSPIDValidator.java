package com.trilogy.app.crm.inboundfile.validators;

import java.util.Collection;

import com.trilogy.app.crm.bean.OperatorNotification;
import com.trilogy.app.crm.bean.OperatorNotificationHome;
import com.trilogy.app.crm.bean.OperatorNotificationXInfo;
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
public class OperatorNotificationSPIDValidator implements Validator{

	@Override
	public void validate(Context ctx, Object obj)throws IllegalStateException
	{
		CompoundIllegalStateException cise = new CompoundIllegalStateException();
		if((obj != null)&&(obj instanceof OperatorNotification))
		{
			try
			{			
				OperatorNotification bean = (OperatorNotification)obj;
				Home home = (Home) ctx.get(OperatorNotificationHome.class);
				String msg = "For Service Provider "+bean.getSpid()+" already TPS ID "+bean.getTpsID()+" and email has been configured "+bean.getOperatorEmail();
				And filter = new And();
				filter.add(new EQ(OperatorNotificationXInfo.SPID,bean.getSpid()));
				filter.add(new EQ(OperatorNotificationXInfo.TPS_ID, bean.getTpsID()));
				filter.add(new EQ(OperatorNotificationXInfo.OPERATOR_EMAIL,bean.getOperatorEmail()));
				filter.add(new EQ(OperatorNotificationXInfo.NOTIFY_OPERATOR, bean.getNotifyOperator()));
				Collection<OperatorNotification> beanColl = home.select(ctx, filter);
				if(beanColl.size()>0)
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
