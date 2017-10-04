/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.dunnningpolicyui.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.dunning.AccountLevelActionConfig;
import com.trilogy.app.crm.dunning.DunningLevel;
import com.trilogy.app.crm.dunning.SubscriptionLevelActionConfig;
import com.trilogy.app.crm.dunning.SubscriptionType;
import com.trilogy.app.crm.dunning.action.AccountDunningAction;
import com.trilogy.app.crm.dunning.action.AccountStateChange;
import com.trilogy.app.crm.dunning.action.DebtCollectionAgencyAccountAction;
import com.trilogy.app.crm.dunning.action.NotifyTempGroupAccountAction;
import com.trilogy.app.crm.dunning.action.NotifyTempGroupSubAction;
import com.trilogy.app.crm.dunning.action.SubscriberDunningAction;
import com.trilogy.app.crm.dunning.action.SubscriberStateChange;
import com.trilogy.app.crm.notification.template.CSVNotificationTemplate;
import com.trilogy.app.crm.notification.template.EmailNotificationTemplate;
import com.trilogy.app.crm.notification.template.NotificationTemplate;
import com.trilogy.app.crm.notification.template.SmsNotificationTemplate;
//import com.trilogy.app.crm.subscriber.customattribute.validator.DataTypeValidator;
import com.trilogy.app.crm.support.LevelChangeNotificationSupport;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * @since 10.2
 * @author shyamrag.charuvil@redknee.com
 */
public class DunningLevelValidator implements Validator{

	private DunningLevelValidator(){}
	private static DunningLevelValidator instance_ = null;
	
	public static Validator instance()
	{
		if(instance_ == null)
		{
			instance_ = new DunningLevelValidator();
		}
		return instance_;
	}
	
	@Override
	public void validate(Context ctx, Object obj)
			throws IllegalStateException {

		DunningLevel dunningLevel = (DunningLevel) obj;
		
		validateDunningLevel(ctx,dunningLevel);
	}

	private void validateDunningLevel(Context ctx,DunningLevel dunningLevel) throws IllegalStateException {
		
		validateAccountLevel(ctx,dunningLevel);
		
		validateSubscriptionLevel(ctx,dunningLevel);		
	}

	private void validateSubscriptionLevel(Context ctx,DunningLevel dunningLevel) throws IllegalStateException 
	{
		List<SubscriptionType> listOfSubscriptionLevels = dunningLevel.getDunningSubscriberLevelConfig();
		Iterator<SubscriptionType> itr = listOfSubscriptionLevels.iterator();
		Boolean bSubscriberDunningActionAlreadyAdded = false;
		
		HashMap<SubscriptionType,Boolean> listOfSubscriptionType = new HashMap<SubscriptionType,Boolean>();
		while(itr.hasNext()) 
		{
			SubscriptionType subscriptionType = itr.next();
			if(listOfSubscriptionType.containsKey(subscriptionType))
			{
				throw new IllegalStateException("Suscription Type ="+subscriptionType.getSubscriptionType() +" should be configured only once.");
			}
			else
			{
				listOfSubscriptionType.put(subscriptionType, true);
			}
			
			List<SubscriptionLevelActionConfig> listOfSubscriptionLevelActionConfig = subscriptionType.getDunningSubscriberLevelConfig();
			Iterator<SubscriptionLevelActionConfig> itrForSubscriptionType = listOfSubscriptionLevelActionConfig.iterator();
			
			bSubscriberDunningActionAlreadyAdded = false;
			while(itrForSubscriptionType.hasNext())
			{
				SubscriptionLevelActionConfig subscriptionLevelActionConfig = itrForSubscriptionType.next();
				SubscriberDunningAction subscriberDunningAction = subscriptionLevelActionConfig.getDunningAction();
				
				if(!(subscriptionLevelActionConfig.getStepDown() || subscriptionLevelActionConfig.getStepUp()))
				{
					throw new IllegalStateException("One of the selected action at subscription level does not have the Step-up or step-down flag checked.");
				}
				
				if(subscriberDunningAction instanceof SubscriberStateChange)
				{		
					if(bSubscriberDunningActionAlreadyAdded)
					{
						throw new IllegalStateException("Multiple Configuration of Subscription state change is not allowed at single level.");
					}
					bSubscriberDunningActionAlreadyAdded = true;
				}				
				else if(subscriberDunningAction instanceof NotifyTempGroupSubAction)
				{		
					NotifyTempGroupSubAction notifyTempGroupSubAction = (NotifyTempGroupSubAction)subscriberDunningAction;

					long recordID = notifyTempGroupSubAction.getNotifyTempGroupSub();
					try
					{
						Collection<NotificationTemplate> listOfTemplates = LevelChangeNotificationSupport.getTemplates(ctx,recordID);	
						//checkForInvalidNotifyTempAction(listOfTemplates );
					}
					catch(HomeException e)
					{
						throw new IllegalStateException(e + "for Subscription Level.");
					}
				}	
			}
		}	
	}

	private void validateAccountLevel(Context ctx, DunningLevel dunningLevel) throws IllegalStateException 
	{
		ArrayList<AccountDunningAction> accountLevelConfigs = new ArrayList<AccountDunningAction>();
		Boolean isAccountStateInCollection = false;
		
		List<AccountLevelActionConfig> listOfAccountLevels = dunningLevel.getDunningAccountLevelConfig();
		Iterator<AccountLevelActionConfig> itr = listOfAccountLevels.iterator();
		Boolean bAccountDunningActionAlreadyAdded = false;
		
		while(itr.hasNext()) {
			AccountLevelActionConfig accountLevelConfig = itr.next();
			AccountDunningAction accountDunningAction = accountLevelConfig.getDunningAction();
			
			if(!(accountLevelConfig.getStepDown() || accountLevelConfig.getStepUp()))
			{
				throw new IllegalStateException("One of the selected action at Account level does not have the Step-up or step-down flag checked.");
			}
			
			if(accountDunningAction instanceof AccountStateChange)
			{		
				if(bAccountDunningActionAlreadyAdded)
				{
					throw new IllegalStateException("Multiple Configuration of Account state change is not allowed at single level");
				}
				if(((AccountStateChange) accountDunningAction).getNewState() == AccountStateEnum.IN_COLLECTION)
				{
					isAccountStateInCollection = true;
				}
				bAccountDunningActionAlreadyAdded = true;
			}	
			else if(accountDunningAction instanceof NotifyTempGroupAccountAction)
			{
				NotifyTempGroupAccountAction notifyTempGroupAccountAction = (NotifyTempGroupAccountAction)accountDunningAction;
				        
		        long recordID = notifyTempGroupAccountAction.getNotifyTempGroupAcc();
				try
				{
					Collection<NotificationTemplate> listOfTemplates = LevelChangeNotificationSupport.getTemplates(ctx,recordID);
					//checkForInvalidNotifyTempAction(listOfTemplates );
				}
				catch(HomeException e)
				{
					throw new IllegalStateException(e + "for Account Level.");
				}
			}
			
			accountLevelConfigs.add(accountDunningAction);
		}		
		
		for(AccountDunningAction accountDunningAction : accountLevelConfigs)
		{
			if(accountDunningAction instanceof DebtCollectionAgencyAccountAction)
			{
				if(!isAccountStateInCollection)
				{
					throw new IllegalStateException("Account State change should be in '"+AccountStateEnum.IN_COLLECTION.toString()
							+"' state to configure Debt Collection Agency");
				}
			}
		}
		
	}

	private void checkForInvalidNotifyTempAction(
			Collection<NotificationTemplate> listOfTemplates) throws HomeException {
		
		for (NotificationTemplate template : listOfTemplates)
		{
			if(!(template instanceof EmailNotificationTemplate || template instanceof SmsNotificationTemplate || template instanceof CSVNotificationTemplate))
			{
				throw new IllegalStateException("Only SMS, EMAIL & CSV Template is allowed to configure in the selected Template Group ");
			}
		}	
	}

}
