package com.trilogy.app.crm.support;

import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.Deposit;
import com.trilogy.app.crm.bean.DepositStatusEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

public interface ReleaseDeposit {

	public int releaseDeposit(Context ctx,
			Map parameters, List<DepositStatusEnum> statusList,List<Deposit> depositList) throws DepositReleaseException, HomeException;
	
}
