package com.trilogy.app.crm.dunning.processor;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.dunning.DunningReportRecord;
import com.trilogy.framework.xhome.context.Context;

public interface DunningProcessor {
	void processAccount(Context context,Account account) throws DunningProcessException;
	DunningReportRecord generateReportRecord(Context context,Account account) throws DunningProcessException;
}
