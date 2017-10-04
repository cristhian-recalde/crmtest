package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.app.crm.support.UserGroupSupport;
import com.trilogy.app.crm.validator.UserDailyAdjustmentLimitTransactionValidator;
import com.trilogy.framework.xhome.auth.bean.Group;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * 
 * TT#13012321075
 * 
 * Validate If Subscription Initial Balance is going to result in 
 * total User Adjustment being greater than User Daily Adjustment Limit.
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public class SubscriberInitialBalanceDailyAdjustmentLimitValidator implements
		Validator 
{
	private static Validator instance_ = null;

	private SubscriberInitialBalanceDailyAdjustmentLimitValidator() 
	{
	}
	
	public static Validator instance()
	{
		if(instance_ == null)
		{
			instance_ = new SubscriberInitialBalanceDailyAdjustmentLimitValidator();
		}
		
		return instance_;
	}

	@Override
	public void validate(Context ctx, Object obj) throws IllegalStateException 
	{
		
		HomeOperationEnum homeOp = (HomeOperationEnum) ctx.get(HomeOperationEnum.class); 
        if (homeOp != null) 
        { 
            if (homeOp != HomeOperationEnum.CREATE)
            	return;
        }
		if(obj instanceof Subscriber)
		{
			Subscriber subscriber = (Subscriber)obj;
			long initialBalance = subscriber.getInitialBalance();
			CompoundIllegalStateException e = new CompoundIllegalStateException();
			
			try
			{
				long adjustmentLimit = UserGroupSupport.getAdjustmentLimit(ctx);
				CRMGroup group = (CRMGroup) ctx.get(Group.class);
				
				if(initialBalance > adjustmentLimit)
				{
					e.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.INITIAL_BALANCE, "Initial Balance is greater than the allowed adjustment limit" +
							"of :" + adjustmentLimit + " for the CRM User Group: "+ group.getName()));
				}

				long dailyLimit = UserDailyAdjustmentLimitTransactionValidator.getUserTransactionLimit(ctx, SystemSupport.getAgent(ctx), initialBalance);

				if(Math.abs(initialBalance) > dailyLimit)
				{
					e.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.INITIAL_BALANCE, "Initial Balance is greater than the allowed daily adjustment limit" +
							"of :" + dailyLimit + " for the CRM User Group: "+ group.getName()));		
				}
			}
			catch (final HomeException exception)
			{
				new MinorLogMsg(this,e.toString(),null).log(ctx);
			}
			e.throwAll();
		}
	}

}
