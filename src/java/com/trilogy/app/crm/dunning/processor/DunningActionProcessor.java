package com.trilogy.app.crm.dunning.processor;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.dunning.DunningActionHolder;
import com.trilogy.app.crm.dunning.DunningLevel;
import com.trilogy.app.crm.dunning.action.DunningAction;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

public class DunningActionProcessor 
{
	public void executeAllActions(Context context,DunningActionHolder actionHolder , DunningLevel nextLevel ,
			Map<String,DunningLevel> subscribersNextLevel,Account account,Date runningDate)
	{
		List<DunningAction> accountActions = actionHolder.getAccountDunningActions();
		Map<String,List<DunningAction>> subscriberActions = actionHolder.getSubscriberActions();
		executeAccountActions(context,accountActions,nextLevel,account,runningDate);
		executeSubscriberActions(context,subscriberActions,subscribersNextLevel,account,runningDate);
	}
	
	private void executeAccountActions(Context context,List<DunningAction> actions, DunningLevel nextLevel,
			Account account,Date runningDate) 
	{
		executeActions(context,actions,nextLevel,account,null,runningDate);
	}
	

    private void executeSubscriberActions(Context context, Map<String, List<DunningAction>> subscriberActions,
    		Map<String,DunningLevel> subscribersNextLevel, Account account, Date runningDate)
    {
        try
	{
			Collection<Subscriber> subscribers = account.getSubscribers(context);
			for(Subscriber subscriber : subscribers)
			{
				List<DunningAction> actions = subscriberActions.get(subscriber.getId());
				if(actions != null)
					executeActions(context,actions,subscribersNextLevel.get(subscriber.getId()),account,subscriber,runningDate);
			}
        }
        catch (HomeException he)
		{
			// log exception for sub action failure
			
		}
	}
	

    public void executeActions(Context context, List<DunningAction> actions, DunningLevel nextLevel, Account account,
            Subscriber subscriber, Date runningDate)
	{
		for(DunningAction action : actions)
		{
			action.populateActionWithDunningData(account, subscriber, nextLevel, runningDate);
            try
            {
				action.execute(context);
            }
            catch (AgentException e)
			{
				//log exception for action
			}
		}
	}
}
