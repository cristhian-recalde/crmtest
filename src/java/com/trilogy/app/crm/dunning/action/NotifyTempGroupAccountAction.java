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
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.KeyValueFeatureEnum;
import com.trilogy.app.crm.bean.NotificationMethodEnum;
import com.trilogy.app.crm.calculation.support.InvoiceSupport;
import com.trilogy.app.crm.notification.NotificationTypeEnum;
import com.trilogy.app.crm.notification.template.NotificationTemplate;
import com.trilogy.app.crm.support.LevelChangeNotificationSupport;
import com.trilogy.app.crm.support.NotificationSupportHelper;
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
public class NotifyTempGroupAccountAction extends AbstractNotifyTempGroupAccountAction{
	
	public static String MODULE = NotifyTempGroupAccountAction.class.getName();
	
	public void execute(Context ctx) throws AgentException
	{
		new InfoLogMsg(MODULE, "In execute() method of Notification Template for Account Level action "
        		+ "of Dunning Policy Id= "+getAccount().getDunningPolicyId(), null).log(ctx);
		    
        String smsNumber = "";
        String language = "";
        String emailAddress = "";
        
        Account account = getAccount();
        if (account != null)
        {
    		//ITSC-4209 : Adding account in context, as we will require data while creating CSV
        	ctx.put(Account.class, account);
			smsNumber = account.getNotificationNumber();
            language = account.getLanguage();
            emailAddress = account.getEmailID();
            
            if (LogSupport.isDebugEnabled(ctx))
            {
            	LogSupport.debug(ctx, MODULE, "Billing language= "+language+", emailAddress="+emailAddress+" and sms Number="
                		+smsNumber+ " for Account="+account.getBAN() + ".");
            }
            
            Invoice invoice=InvoiceSupport.getMostRecentInvoice(ctx, account.getBAN());
            if(invoice!=null){
            	ctx.put(Invoice.class, invoice);
            	
            	 if (LogSupport.isDebugEnabled(ctx))
                 {
                 	LogSupport.debug(ctx, MODULE, "Billing language= "+language+", dueAmount="+invoice.getTotalAmount()+" and dueDate="
                     		+invoice.getDueDate()+ " for Account="+invoice.getBAN() + ".");
                 }
            } 
        }
        
        long recordID = getNotifyTempGroupAcc();
        if(recordID == OptionalLongWebControl.DEFAULT_VALUE)
        {
        	throw new AgentException("The template id ="+recordID+
        			"is not found for Account level of Dunning Policy ID ="+account.getDunningPolicyId());
        }
        
    	NotificationMethodEnum preferredMethod = NotificationMethodEnum.BOTH;
        if (LogSupport.isDebugEnabled(ctx))
        {
        	LogSupport.debug(ctx, MODULE, "The Preferred Notification Method of Account = "
        			+account.getBAN()+" is "+preferredMethod.toString() + ".");
        }
        
		List<Class<? extends NotificationTemplate>> preferredTypes = NotificationSupportHelper.get(ctx)
				.getPreferredNotificationTypes(preferredMethod);

        Collection<NotificationTemplate> listOfTemplates = null;
		try 
		{
			listOfTemplates = LevelChangeNotificationSupport.getTemplates(ctx,recordID,language,preferredTypes);
			if(listOfTemplates ==  null || listOfTemplates.isEmpty())
			{
				throw new HomeException("No Templates found");
			}
		} 
		catch (HomeException e) 
		{
			throw new AgentException("Error while getting list of templates for ID = "+recordID+":"+e.getMessage());
		}

        NotificationTypeEnum notificationType = NotificationTypeEnum.STATE_CHANGE;    
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
