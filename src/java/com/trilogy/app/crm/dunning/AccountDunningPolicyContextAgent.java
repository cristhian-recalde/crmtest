/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.dunning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.core.bean.ifc.SubscriberIfc;
import com.trilogy.app.crm.core.ruleengine.BusinessRuleAndActionOutput;
import com.trilogy.app.crm.core.ruleengine.BusinessRuleEngineUtility;
import com.trilogy.app.crm.core.ruleengine.EventTypeEnum;
import com.trilogy.app.crm.core.ruleengine.actions.ifc.ActionOutputIfc;
import com.trilogy.app.crm.core.ruleengine.actions.param.ActionParameter;
import com.trilogy.app.crm.dunning.action.DunningIdUpdateAction;
import com.trilogy.app.crm.dunning.visitor.accountprocessing.AccountDunningPolicyAssignmentVisitor;

/**
 * Assigns the dunning policy to the account.
 *
 * @author sapan.modi@redknee.com
 */
public class AccountDunningPolicyContextAgent implements ContextAgent {
	
	
	public AccountDunningPolicyContextAgent(AccountDunningPolicyAssignmentVisitor accountDunningPolicyAssignmentVisitor)
	{
		 dunningPolicyPredicateVisitor_ = accountDunningPolicyAssignmentVisitor;
	}
	
    public void execute(Context context)
    {
        final Account account = (Account) context.get(DUNNINGPOLICY_ACCOUNT);
        
        if (account!=null)
        {
            try
            {
            	Subscriber subscriber = account.getSubscriber();
            	Collection<Subscriber> subList= account.getSubscribers(context);
            	List<SubscriberIfc> subscriberList = new ArrayList<SubscriberIfc>();
            	for(Subscriber sub:subList)
            	{
            		subscriberList.add(sub);
            	}
            	
            	BusinessRuleAndActionOutput output = BusinessRuleEngineUtility.evaluateRuleExecuteAction(context,EventTypeEnum.DUNNING,
            											account, subscriber, subscriberList, prameterMap_, new ActionParameter[]{});
            	
            	List<ActionOutputIfc> actionOutputList = output.getActionOutputList();
            	for(ActionOutputIfc actionOutput:actionOutputList)
            	{
            		//I don't find a good way to decide in a specific action and actions loose coupled with the caller to rule engine  
            		if(DunningIdUpdateAction.NAME.equals(actionOutput.getActionName()) && actionOutput.getResultCode() == ActionOutputIfc.RESULT_CODE_FAIL)
            	    {
            			addFailedAssignedBAN(account.getBAN());
            		}
            	}
            }
            catch (Exception e)
            {
                new MinorLogMsg(this, "AccountDunningPolicy process failed for account '" + account.getBAN() + "': " +  e.getMessage(), e).log(context);
                addFailedAssignedBAN(account.getBAN());
                
            }
        }
    }
	    
    private synchronized void addFailedAssignedBAN(String BAN)
    {
        this.failedBANs_.add(BAN);
    }
    

    public synchronized List<String> getFailedAssignedBANs()
    {
        return failedBANs_;
    }

	private List<String> failedBANs_ = new ArrayList<String>();
	private AccountDunningPolicyAssignmentVisitor dunningPolicyPredicateVisitor_;
	private static Map<Integer,Object> prameterMap_ = new HashMap<Integer, Object>();//Taking Static Instance as we don't need to pass anything for now
	public static String DUNNINGPOLICY_ACCOUNT = "DunningPolicyAccount";

}
