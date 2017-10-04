package com.trilogy.app.crm.dunning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
//import com.trilogy.app.crm.bean.PaymentMethodOperationEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.dunning.action.AccountDunningAction;
import com.trilogy.app.crm.dunning.action.AccountStateChange;
//import com.trilogy.app.crm.dunning.action.DebtCollectionAgencyAccountAction;
import com.trilogy.app.crm.dunning.action.DunningAction;
//import com.trilogy.app.crm.dunning.action.PaymentMethodStatusChange;
//import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.visitor.FunctionVisitor;
import com.trilogy.framework.xhome.visitor.ListBuildingVisitor;
import com.trilogy.framework.xlog.log.LogSupport;

public class DunningLevel extends AbstractDunningLevel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DunningLevel() {}

	public boolean isAfterLevel(int levelSequence)
	{
		if(this.getId() > levelSequence)
		{
			return true;
		}
		return false;
	}
	
	public int compareTo(int levelSequence)
	{
		int currentSequence = (int)this.getId();
		if(currentSequence > levelSequence)
		{
			return 1;
		}else if(currentSequence < levelSequence)
		{
			return -1;
		}else
			return 0;
	}
	
	public List<DunningAction> getAccountStepUpActions()
	{
		List<DunningAction> accountStepUpActions = new ArrayList<DunningAction>();
		
		List<AccountLevelActionConfig> tempAccountStepUpActions= getDunningAccountLevelConfig();
		
		if(tempAccountStepUpActions==null)
		{
			return accountStepUpActions;
		}
		for(AccountLevelActionConfig temp:tempAccountStepUpActions)
		{
		    if(temp.isStepUp())
		    {
		    	accountStepUpActions.add(temp.getDunningAction());
		    }
		}
		
		return accountStepUpActions;
	}
	
	public List<DunningAction> getAccountStepDownActions()
	{
		List<DunningAction> accountStepDownActions = new ArrayList<DunningAction>();
		
		List<AccountLevelActionConfig> tempAccountStepUpActions= getDunningAccountLevelConfig();
		
		if(tempAccountStepUpActions==null)
		{
			return accountStepDownActions;
		}
		for(AccountLevelActionConfig temp:tempAccountStepUpActions)
		{
		    if(temp.isStepDown())
		    {
		    	accountStepDownActions.add(temp.getDunningAction());
		    }
		}
		
		return accountStepDownActions;
	}
	
	public List<DunningAction> getSubscriberStepUpActions(long subscriptionType)
	{
		List<DunningAction> subscriberStepUpActions = new ArrayList<DunningAction>();
		
		List<SubscriptionType> tempSubscriptionType= getDunningSubscriberLevelConfig();
		
		if(tempSubscriptionType==null)
		{
			return subscriberStepUpActions;
		}
		for(SubscriptionType temp:tempSubscriptionType)
		{
		    if(temp.getSubscriptionType() == subscriptionType)
		    {
		    	List<SubscriptionLevelActionConfig> tempSubscriptionLevelActionConfig = temp.getDunningSubscriberLevelConfig();
		    	if(tempSubscriptionLevelActionConfig == null)
		    	{
		    		return subscriberStepUpActions;
		    	}
		        for(SubscriptionLevelActionConfig tempSubcriptionLevel:tempSubscriptionLevelActionConfig)
		        {
		        	
		        	if(tempSubcriptionLevel.isStepUp())
		        	{
		        		subscriberStepUpActions.add(tempSubcriptionLevel.getDunningAction());
		        	}
		        }
		    	
		    }
		}
		
		
		return subscriberStepUpActions;
	}
	
	public List<DunningAction> getSubscriberStepDownActions(long subscriptionType)
	{
		List<DunningAction> subscriberStepDownActions = new ArrayList<DunningAction>();
		
			
		List<SubscriptionType> tempSubscriptionType= getDunningSubscriberLevelConfig();
		
		if(tempSubscriptionType==null)
		{
			return subscriberStepDownActions;
		}
		for(SubscriptionType temp:tempSubscriptionType)
		{
		    if(temp.getSubscriptionType() == subscriptionType)
		    {
		    	List<SubscriptionLevelActionConfig> tempSubscriptionLevelActionConfig = temp.getDunningSubscriberLevelConfig();
		    	if(tempSubscriptionLevelActionConfig == null)
		    	{
		    		return subscriberStepDownActions;
		    	}
		        for(SubscriptionLevelActionConfig tempSubcriptionLevel:tempSubscriptionLevelActionConfig)
		        {
		        	if(tempSubcriptionLevel.isStepDown())
		        	{
		        		subscriberStepDownActions.add(tempSubcriptionLevel.getDunningAction());
		        	}
		        }
		    	
		    }
		}
		
		return subscriberStepDownActions;
	}
	
	public boolean isLevelZero()
	{
		if(this.getId() == 0)
		{
			return true;
		}
		return false;
	}
	
	public static final DunningLevel LEVEL_0 = new DunningLevel(){
	
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public int getId()
		   {
		      return 0;
		   }

		@Override
		public int getGraceDays()
	   {
	      return 0;
	   }
		
		@Override
		public boolean getIsApproved()
		   {
		      return false;
		   }
	      
		@Override
	      public List getDunningAccountLevelConfig()
	      {
	    	  final AccountStateChange accountStateChange = new AccountStateChange();
		      accountStateChange.setNewState(AccountStateEnum.ACTIVE);
		      final AccountDunningAction accountDunningAction = accountStateChange;
		      final AccountLevelActionConfig accountLevelDunningAction = new AccountLevelActionConfig();
		      accountLevelDunningAction.setDunningAction(accountDunningAction);
		      accountLevelDunningAction.setStepDown(true);
		      
		    /*  final PaymentMethodStatusChange paymentMethodStatusChange = new PaymentMethodStatusChange();
		      paymentMethodStatusChange.setNewPaymentState(PaymentMethodOperationEnum.ACTIVE);
		      final AccountDunningAction paymentMethodDunningAction = paymentMethodStatusChange;
		      final AccountLevelActionConfig accountLevelDunningActionForPayment = new AccountLevelActionConfig();
		      accountLevelDunningActionForPayment.setDunningAction(paymentMethodDunningAction);
		      accountLevelDunningActionForPayment.setStepDown(true);
		      
		      final DebtCollectionAgencyAccountAction debtCollectionAgencyAccountAction = new DebtCollectionAgencyAccountAction();
		      debtCollectionAgencyAccountAction.setDebtCollectionAgencyAccAction(0);
		      final AccountDunningAction debtCollectionDunningAction = debtCollectionAgencyAccountAction;
		      final AccountLevelActionConfig accountLevelDunningActionForDebtCollection = new AccountLevelActionConfig();
		      accountLevelDunningActionForDebtCollection.setDunningAction(debtCollectionDunningAction);
		      accountLevelDunningActionForDebtCollection.setStepDown(true);*/
		      
		      final List<AccountLevelActionConfig> accountActions = new ArrayList<AccountLevelActionConfig>();
		      accountActions.add(accountLevelDunningAction);
		      //accountActions.add(accountLevelDunningActionForPayment);
		     // accountActions.add(accountLevelDunningActionForDebtCollection);
		      
		      return accountActions;
	      }
	      
		@Override
	      public List getDunningSubscriberLevelConfig()
	      {
	         return Collections.emptyList();
	      }
		
	};
}
