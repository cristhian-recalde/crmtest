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
package com.trilogy.app.crm.dunning.action;

import java.util.Collection;
import java.util.Date;

import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountSuspensionReasonEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriptionSuspensionReason;
import com.trilogy.app.crm.bean.SubscriptionSuspensionReasonHome;
import com.trilogy.app.crm.bean.SubscriptionSuspensionReasonXInfo;
import com.trilogy.app.crm.dunning.DunningConstants;
import com.trilogy.app.crm.home.TransactionRedirectionHome;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.EQIC;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @since 10.2
 * @author shyamrag.charuvil@redknee.com
 * @sunil.khalane@redknee.com
 * @nitin.kumar@redknee.com
 */
public class SubscriberStateChange extends AbstractSubscriberStateChange{

	private static final long serialVersionUID = 1L;
	String subSusReasonCode = null;
	public void execute(Context context) throws AgentException
	{	
		LogSupport.info(context, this, "In SubscriberStateChange:execute() for Subscriber Level action "
				+ "of Dunning Policy Id= "+getAccount().getDunningPolicyId());

		SubscriberStateEnum newState = getNewState();
		Subscriber sub = getSubscriber();		

		boolean isStateSuspendedToActivate = false;

		if(sub == null)
		{
			throw new AgentException("Subscriber object is null.");
		}

		SubscriberStateEnum prevState = sub.getState();
		String prevSuspensionReason = sub.getSuspensionReason();
		boolean isPrevStateChangeDueToDunning = false;
		
		
		if(sub.getState() == newState)
		{
			LogSupport.info(context, this, "The new state and current state for the Subscriber ="+sub.getId()+" is same. Not updating the state.");
			return;
		}
		else
		{			
			//Sprint#7 : If subscriber is going to ACTIVE state from SUSPENDED state, set the resumed date
			//capturing the current and previous states before setting in the new state.
			if(sub.getState().equals(SubscriberStateEnum.SUSPENDED)&&
					newState.equals(SubscriberStateEnum.ACTIVE))
			{
				isStateSuspendedToActivate = true;
				
			}

			sub.setState(newState);
		}

		try
		{
			//If subscriber state is suspension, set the reason and the suspended date.
			if(sub.getState().equals(SubscriberStateEnum.SUSPENDED) || prevState.equals(SubscriberStateEnum.SUSPENDED))
			{				
				//Suspension Reason for unpaid thru dunning 
			
				int spid = sub.getSpid();

				And where = new And();
				where.add(new EQ(SubscriptionSuspensionReasonXInfo.SPID, spid));
				where.add(new EQIC(SubscriptionSuspensionReasonXInfo.NAME, DunningConstants.DUNNING_SUSPENSION_REASON_UNPAID));
				// TODO - Confirm with shyamrag.charuvil@redknee.com - why not ORDER BY in above query?

				final Collection<SubscriptionSuspensionReason> subSuspensionReasonColl
				= HomeSupportHelper.get(context).getBeans(context, SubscriptionSuspensionReason.class, where);

				if (subSuspensionReasonColl != null && !subSuspensionReasonColl.isEmpty())
				{
					for (SubscriptionSuspensionReason subSusReason : subSuspensionReasonColl) 
					{
						subSusReasonCode = subSusReason.getReasoncode();
						if (subSusReasonCode == prevSuspensionReason){
							isPrevStateChangeDueToDunning = true;

							if (LogSupport.isDebugEnabled(context))
							{
								LogSupport.debug(context, this, "SubscriberStateChange: "+ subSusReason);
							}
						}
					}
				}
				else
				{
					LogSupport.minor(context, this, "The mapping for the reason code Unpaid" +
							" does not exists for SPID["+ sub.getSpid() + "]");
				}
				
				if(!(prevState == SubscriberStateEnum.SUSPENDED))
				{
					sub.setSuspensionReason(subSusReasonCode);
					sub.setSuspensionDate(new Date()); 
				}else{
					
					if((prevState==SubscriberStateEnum.SUSPENDED) && !(prevSuspensionReason==subSusReasonCode)){
					sub.setSuspensionReason(prevSuspensionReason);
					sub.setSuspensionDate(new Date());
						
							LogSupport.debug(context, this, "Suspension reason is set other then UNPAID "+ prevSuspensionReason);
						
					}
				}
			}

			if(isStateSuspendedToActivate)
			{
				String subReason=null;
				sub.setSuspensionReason(subReason);
				sub.setResumedDate(new Date());				
			}
			
			if (skipSuspensionReasonChange(prevState, isPrevStateChangeDueToDunning))
			{
				if (LogSupport.isDebugEnabled(context))
					LogSupport.debug(context, this, "Skipping suspension reason change.");
				return;
			}
			
			if(prevState==SubscriberStateEnum.SUSPENDED ){
				if(subSusReasonCode != sub.getSuspensionReason()){
					LogSupport.info(context, this, "Subscriber state can not be updated due to not matching the suspension reason as Unpaid");
				      return;
				}
			}
			
			HomeSupportHelper.get(context).storeBean(context, sub);
			LogSupport.info(context, this, "Subscriber state successfully updated.");
		} 
		catch (HomeException e) 
		{
			LogSupport.major(context, this, "Error occured in updating Susbcriber state.",e);
		} 		
	}

	
	/**
	 * A --> D --> S | (null) --> (null) --> Unpaid
	 * A --> S       | (null) --> Manual
	 * 
	 * SuspensionReason change is allowed only when cause of suspension is dunning.
	 * In other words, if previous state is SUSPENDED and associated reason is MANUAL, then DON'T change suspension reason.
	 * Just skip it! That's what method's name is!
	 * 
	 * @param prevState
	 * @param isPrevStateChangeDueToDunning
	 * @return
	 */
	private boolean skipSuspensionReasonChange(final SubscriberStateEnum prevState, final boolean isPrevStateChangeDueToDunning)
	{
		return (prevState == SubscriberStateEnum.SUSPENDED && isPrevStateChangeDueToDunning == false);
	}
}
