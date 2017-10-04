package com.trilogy.app.crm.dunning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.dunning.action.DunningAction;

public class DunningActionHolder 
{
	public void addAccountDunningActions(List<DunningAction> actions)
	{
		accountActions_.addAll(actions);
	}
	
	public void addSubscriberDunningAction(String subscriberId,List<DunningAction> subscriberAction)
	{
		subscriberActions_.put(subscriberId, subscriberAction);
	}
	
	public List<DunningAction> getAccountDunningActions()
	{
		return accountActions_;
	}
	
	public List<DunningAction> getSubscriberActionForType(long subscriptionType)
	{
		return subscriberActions_.get(subscriptionType);
	}
	
	public Map<String,List<DunningAction>> getSubscriberActions()
	{
		return subscriberActions_;
	}
	private List<DunningAction> accountActions_ = new ArrayList<DunningAction>();
	private Map<String,List<DunningAction>> subscriberActions_ = new HashMap<String,List<DunningAction>>();
}
