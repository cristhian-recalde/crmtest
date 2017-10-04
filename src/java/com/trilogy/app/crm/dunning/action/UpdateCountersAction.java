package com.trilogy.app.crm.dunning.action;


import java.util.List;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.app.crm.core.ruleengine.actions.*;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.app.crm.counter.*;
import com.trilogy.framework.xlog.log.LogSupport;


public class UpdateCountersAction extends AbstractUpdateCountersAction{
	public void execute(Context context) throws AgentException
	{
		
		Account account = getAccount();
		String ban = account.getBAN();
		try{
		      List subList = (List) account.getAllSubscribers(context);
		      List<CounterUpdate> updates = getCounterUpdates();
		     
		       if (LogSupport.isDebugEnabled(context)){
			             LogSupport.debug(context,this, " BAN is:: " + account.getBAN());
		       }
		
		     for(CounterUpdate update : updates){
			    	CounterUpdater.updateCounter(context, update, ban, subList);
			   }
			
		}
		catch(Exception e){
		        // actionOutputImpl.setResultCode(ActionOutputIfc.RESULT_CODE_FAIL);
		        	LogSupport.debug(context, this, "Failed to execute Update Counter Action for Account:" + ban );
	    }
	}
}
