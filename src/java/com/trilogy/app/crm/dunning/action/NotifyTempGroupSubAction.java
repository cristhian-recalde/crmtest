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
import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.KeyValueFeatureEnum;
import com.trilogy.app.crm.bean.NotificationMethodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.notification.NotificationTypeEnum;
import com.trilogy.app.crm.notification.liaison.ScheduledTaskNotificationLiaison;
import com.trilogy.app.crm.notification.template.NotificationTemplate;
import com.trilogy.app.crm.support.LevelChangeNotificationSupport;
import com.trilogy.app.crm.support.NotificationSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SubscriptionNotificationSupport;
import com.trilogy.app.crm.support.SubscriptionTypeSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.OptionalLongWebControl;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @since 10.2
 * @author shyamrag.charuvil@redknee.com
 */
public class NotifyTempGroupSubAction extends AbstractNotifyTempGroupSubAction{
	
	private static String MODULE = NotifyTempGroupSubAction.class.getName();
	
	public void execute(Context ctx) throws AgentException
	{
        new InfoLogMsg(MODULE, "In execute() method of Notification Template for Subscriber Level action "
        		+ "of Dunning Policy Id= "+getAccount().getDunningPolicyId(), null).log(ctx);
        
        final Account account = getAccount();
        final Subscriber sub = getSubscriber();
        if(sub == null) 
        {
        	throw new AgentException("Subscriber not found for account = "+account.getBAN());
        }
		//ITSC-4209 : Adding account and subaccount in context, as we will require data while creating CSV
		ctx.put(Account.class, account);
		ctx.put(Subscriber.class, sub);
        String smsNumber = "";
        String language = "";
        String emailAddress = "";
        
        try
        {
        	if(SubscriptionTypeSupport.isLegacySubscriptiontype(ctx,(int)sub.getSubscriptionType()))
        	{
        		smsNumber = sub.getMSISDN();
        		new InfoLogMsg(MODULE, "Sms number="+smsNumber +" is retrieved from Subscriber", null).log(ctx);
        	}
        	else
        	{
        		smsNumber = account.getNotificationNumber();
        		new InfoLogMsg(MODULE, "Sms number="+smsNumber +" is retrieved from Account", null).log(ctx);
        	}
        } 
        catch (HomeException e) 
        { 
        	throw new AgentException("Error in retrieving Susbcription type "+sub.getSubscriptionType());
        }

        language = sub.getBillingLanguage();
        emailAddress = account.getEmailID();
        if (LogSupport.isDebugEnabled(ctx))
        {
        	LogSupport.debug(ctx, MODULE, "Billing language= "+language+" and emailAddress="+emailAddress+" "
        			+ "for Subscriber="+sub.getId() + ".");
        }

        
        long recordID = getNotifyTempGroupSub();
        if(recordID == OptionalLongWebControl.DEFAULT_VALUE)
        {
        	throw new AgentException("The template id ="+recordID+
        			"is not found for Subscription level of Dunning Policy ID ="+account.getDunningPolicyId());
        }
        
    	NotificationMethodEnum preferredMethod = SubscriptionNotificationSupport.getNotificationMethod(ctx, sub);
        if (LogSupport.isDebugEnabled(ctx))
        {           
            LogSupport.debug(ctx, MODULE, "The Preferred Notification Method of Subscriber = "
            		+sub.getId()+" is "+preferredMethod.toString() + ".");
        }
        
		List<Class<? extends NotificationTemplate>> preferredTypes = NotificationSupportHelper.get(ctx)
				.getPreferredNotificationTypes(preferredMethod);

        Collection<NotificationTemplate> listOfTemplates = null;
		try 
		{
			listOfTemplates = LevelChangeNotificationSupport.getTemplates(ctx,recordID,language,preferredTypes);
			if(null == listOfTemplates || listOfTemplates.isEmpty())
			{
				throw new HomeException("No Templates found");
			}
		} 
		catch (HomeException e) 
		{
			throw new AgentException("Error while getting list of templates for ID = "+recordID+":"+e.getMessage());
		}
   
        NotificationTypeEnum notificationType = NotificationTypeEnum.STATE_CHANGE;
        
        ScheduledTaskNotificationLiaison.setCleanupKey(ctx, sub.getId() + notificationType);

        try
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
            	 LogSupport.debug(ctx, MODULE, "Sending Notifications for DunningPolicyId = "+
            	            account.getDunningPolicyId()+" to Sms Number = "+smsNumber+"Email Address="+emailAddress+" with templates"
            	            +listOfTemplates.toString() + ".");
            }
            
			LevelChangeNotificationSupport.sendNotifications(ctx, 
					emailAddress, smsNumber, null,
					notificationType,
					listOfTemplates,
					KeyValueFeatureEnum.STATE_CHANGE_EMAIL);
		} 
        catch (HomeException e) 
        {
			throw new AgentException("Error in Sending Notification for Dunning Policy id "
					+ "="+account.getDunningPolicyId()+":"+e.getMessage());
		}
            
	}
}
