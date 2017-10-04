package com.trilogy.app.crm.bas.recharge;

import java.util.Comparator;

import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


public class SubscriberAuxiliaryServiceRechargeOrder implements Comparator<SubscriberAuxiliaryService>,ContextAware
{
	public SubscriberAuxiliaryServiceRechargeOrder(Context ctx)
	{
		context_ = ctx;
	}
	
	public int compare(final SubscriberAuxiliaryService s1, final SubscriberAuxiliaryService s2)
    {
		int order = 0;
		try{
			if(s1.getAuxiliaryService(getContext()).getCharge() < s2.getAuxiliaryService(getContext()).getCharge())
			{
				order = -1;
			}else if(s1.getAuxiliaryService(getContext()).getCharge() > s2.getAuxiliaryService(getContext()).getCharge())
			{
				order = 1;
			}
		}catch(HomeException e)
		{
			LogSupport.minor(getContext(), this, "Exception while comparing auxiliary service charge",e);
		}
		return order;
    }
	
	public Context getContext()
	{
		return context_;
	}

	public void setContext(Context paramContext)
	{
		context_ = paramContext;
	}
	
	private Context context_ ;
}
