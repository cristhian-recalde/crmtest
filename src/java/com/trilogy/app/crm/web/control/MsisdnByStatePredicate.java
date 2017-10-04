// INSPECTED: 19/09/03 MLAM

package com.trilogy.app.crm.web.control;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnStateEnum;

public class MsisdnByStatePredicate
	implements Predicate
{
	private MsisdnStateEnum state_;

	public MsisdnByStatePredicate(MsisdnStateEnum state)
	{
		state_ = state;
	}

	public boolean f(Context ctx,Object obj)
	{
		Msisdn msisdn = (Msisdn)obj;

		return (msisdn.getState().equals(state_));
	}
}
