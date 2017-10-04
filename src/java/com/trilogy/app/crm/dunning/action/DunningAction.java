package com.trilogy.app.crm.dunning.action;

import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.dunning.DunningLevel;
import com.trilogy.framework.xhome.context.ContextAgent;

public interface DunningAction extends ContextAgent
{
	void populateActionWithDunningData(Account account , Subscriber subscriber , 
			DunningLevel nextLevel,Date runningDate );
}
