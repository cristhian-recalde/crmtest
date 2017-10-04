// INSPECTED: 19/09/03 MLAM

package com.trilogy.app.crm.web.control;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.app.crm.bean.Msisdn;

public class MsisdnByGroupIdPredicate
	implements Predicate
{
	private int groupId_;

	public MsisdnByGroupIdPredicate(int groupId)
	{
		groupId_ = groupId;
	}

	public boolean f(Context ctx,Object obj)
	{
		Msisdn msisdn = (Msisdn)obj;

		return (msisdn.getGroup() == groupId_);
	}
}
