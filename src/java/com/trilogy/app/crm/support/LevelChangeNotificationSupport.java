/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.KeyValueFeatureEnum;
import com.trilogy.app.crm.dunning.support.DunningNotificationSupport;
import com.trilogy.app.crm.io.FileOutputStreamFactory;
import com.trilogy.app.crm.notification.EmailAddresses;
import com.trilogy.app.crm.notification.NotificationTypeEnum;
import com.trilogy.app.crm.notification.RecipientInfo;
import com.trilogy.app.crm.notification.generator.MessageGenerationException;
import com.trilogy.app.crm.notification.generator.SimpleJasperMessageGenerator;
import com.trilogy.app.crm.notification.liaison.NotificationLiaison;
import com.trilogy.app.crm.notification.template.BinaryNotificationTemplate;
import com.trilogy.app.crm.notification.template.CSVNotificationTemplate;
import com.trilogy.app.crm.notification.template.EmailNotificationTemplate;
import com.trilogy.app.crm.notification.template.JasperNotificationTemplate;
import com.trilogy.app.crm.notification.template.NotificationTemplate;
import com.trilogy.app.crm.notification.template.NotificationTemplateGroup;
import com.trilogy.app.crm.notification.template.NotificationTemplateGroupID;
import com.trilogy.app.crm.notification.template.NotificationTemplateHolder;
import com.trilogy.app.crm.notification.template.SmsNotificationTemplate;
import com.trilogy.app.crm.notification.template.TemplateGroupGlobalRecord;
import com.trilogy.app.crm.notification.template.TemplateGroupGlobalRecordXInfo;
import com.trilogy.app.crm.paymentmethod.exception.PaymentMethodProcessorException;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.holder.StringHolder;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;

public class LevelChangeNotificationSupport
{
    private static final String MODULE = LevelChangeNotificationSupport.class.getName();

    private LevelChangeNotificationSupport()
    {
    }
    
	public static Collection<NotificationTemplate> getTemplates(Context ctx,
			long recordID, String language,
			List<Class<? extends NotificationTemplate>> preferredTypes)
			throws HomeException 
    {
		NotificationTemplateGroupID notificationTemplateGroupID = getNotificationTemplateGroupID(
				ctx, recordID);
		if(null == notificationTemplateGroupID)
		{
			return null;
		}
		Collection<NotificationTemplate> listOfTemplates = null;

		listOfTemplates = getTemplates(ctx,
				notificationTemplateGroupID, language,
				preferredTypes.toArray(new Class[] {}));

		return listOfTemplates;
	}

	 private static Collection<NotificationTemplate> getTemplates(
            Context ctx, 
            NotificationTemplateGroupID notificationTemplateGroupID, 
            String language, 
            Class<NotificationTemplate>... preferredTypes) throws HomeException
    {
        Collection<NotificationTemplate> result = new ArrayList<NotificationTemplate>();

        NotificationTemplateGroup group = HomeSupportHelper.get(ctx).findBean(ctx, 
                NotificationTemplateGroup.class, 
                notificationTemplateGroupID);
        if (group == null)
        {
            return result;
        }

        List<NotificationTemplateHolder> templates = group.getTemplates();
        if (templates == null)
        {
            return result;
        }
        
        for (NotificationTemplateHolder templateHolder : templates)
        {
            if (templateHolder == null || !templateHolder.isEnabled())
            {
                continue;
            }

            if (!NotificationTemplateHolder.DEFAULT_LANGUAGE.equals(templateHolder.getLanguage())
                    && !SafetyUtil.safeEquals(language, templateHolder.getLanguage()))
            {
                continue;
            }

            NotificationTemplate template = templateHolder.getTemplate();
            if (template == null)
            {
                continue;
            } 
            
            if(template instanceof CSVNotificationTemplate)
            {
            	 result.add(template);
            	 continue;
            }

            if (templateHolder.isMandatory()
                    || preferredTypes == null
                    || preferredTypes.length == 0)
            {
                result.add(template);
            }
            else
            {              
                for (Class<NotificationTemplate> preference : preferredTypes)
                {
                    if (preference != null && preference.isInstance(template))
                    {
                        result.add(template);
                        break;
                    }
                }
            }
        }       
        return result;
    }
    
	public static Collection<NotificationTemplate> getTemplates(Context ctx, long recordID) throws HomeException 
	{		
		Collection<NotificationTemplate> result = new ArrayList<NotificationTemplate>();
		NotificationTemplateGroupID notificationTemplateGroupID = getNotificationTemplateGroupID(ctx,recordID);
		
		if(notificationTemplateGroupID == null) return result;
		NotificationTemplateGroup group = HomeSupportHelper.get(ctx).findBean(ctx, 
                NotificationTemplateGroup.class, 
                notificationTemplateGroupID);
		
		if (group == null) {
			return result;
		}

		List<NotificationTemplateHolder> templates = group.getTemplates();
		if (templates == null) {
			return result;
		}

		for (NotificationTemplateHolder templateHolder : templates) 
		{
			if (templateHolder == null || !templateHolder.isEnabled()) {
				continue;
			}

			NotificationTemplate template = templateHolder.getTemplate();
			if (template == null) {
				continue;
			}

			result.add(template);
		}
		return result;
	}
	
	public static NotificationTemplateGroupID getNotificationTemplateGroupID(Context ctx, long recordID) throws HomeInternalException, HomeException
	{
		Context whereCtx = HomeSupportHelper.get(ctx).getWhereContext(ctx, 
    			TemplateGroupGlobalRecord.class, 
    			new EQ(TemplateGroupGlobalRecordXInfo.APP_NAME, CoreSupport.getApplication(ctx).getName()));
		
   		TemplateGroupGlobalRecord record = HomeSupportHelper.get(whereCtx).findBean(whereCtx, TemplateGroupGlobalRecord.class, recordID);
   		if(null == record)
   		{
   			return null;
   		}

		NotificationTemplateGroupID notificationTemplateGroupID = new NotificationTemplateGroupID(record.getName(), record.getSpid());
		
		return notificationTemplateGroupID;
	}
	    
	
	public static void sendNotifications(Context ctx, RecipientInfo destination, 
			NotificationTypeEnum type, NotificationTemplate template, KeyValueFeatureEnum... features)
	{
		if (LogSupport.isDebugEnabled(ctx))
		{
			StringBuilder sb = new StringBuilder();
			sb.append("sendNotifications - destination: ");
			sb.append(destination);
			sb.append(", templates: ");
			sb.append(template);
			LogSupport.debug(ctx, MODULE, sb.toString());
		}

		if (template != null)
		{
			NotificationLiaison notificationLiaison = NotificationSupportHelper.get(ctx)
					.getLiaisonForNotificationType(ctx, type);
			if (notificationLiaison != null)
			{                
				List<KeyValueFeatureEnum> featureList = new ArrayList<KeyValueFeatureEnum>(Arrays.asList(features));
				featureList.add(KeyValueFeatureEnum.GENERIC);

				notificationLiaison.sendNotification(ctx, template, destination,
						featureList.toArray(new KeyValueFeatureEnum[] {}));
			}
		}
		else
		{
			LogSupport.info(ctx, MODULE, "The notificationLiaison not found for type: " + type);
		}
	}

	public static void sendNotifications(Context ctx, String emailAddress,
			String smsNumber, Object object,
			NotificationTypeEnum notificationType,
			Collection<NotificationTemplate> listOfTemplates,
			KeyValueFeatureEnum stateChangeEmail) throws HomeException 
	{		
		for (NotificationTemplate template : listOfTemplates)
        {
            if(template instanceof CSVNotificationTemplate)
            {
            	CSVNotificationProcessor csvNotificationProcessor = new CSVNotificationProcessor();
            	try 
            	{
            		csvNotificationProcessor.process(ctx, (CSVNotificationTemplate)template);
            		continue;
            	} 
            	catch (PaymentMethodProcessorException e)
            	{
            		throw new HomeException("Error in processing CSV Template:"+template.toString());
            	}
            }
            else if(template instanceof SmsNotificationTemplate || template instanceof EmailNotificationTemplate || template instanceof BinaryNotificationTemplate)
            {
                RecipientInfo destination = null;
                try
                {
                    destination = (RecipientInfo) XBeans.instantiate(RecipientInfo.class, ctx);
                }
                catch (Exception e)
                {
                    destination = new RecipientInfo();
                }
                
                if(template instanceof SmsNotificationTemplate)
                {
                	destination.setSmsTo(smsNumber);
                }
                else if(template instanceof EmailNotificationTemplate)
                {
                	 EmailAddresses addresses = new EmailAddresses();
                     addresses.setTo(Arrays.asList(new StringHolder[]{new StringHolder(emailAddress)}));
                     destination.setEmailTo(addresses);
                }
                else if (template instanceof BinaryNotificationTemplate)
                {
                    Account acc = (Account)ctx.get(com.redknee.app.crm.bean.Account.class);
                    String pdfFileName = DunningNotificationSupport.getDunningNoticeFilePath(ctx, acc.getSpid(), acc.getAccountCategory().getName(), acc.getBAN());
                    if (pdfFileName != null)
                    {
                        FileOutputStreamFactory fosf = new FileOutputStreamFactory();
                        fosf.setFilename(pdfFileName);                        
                        destination.setPostToGenerator(fosf);
                    }
                    else
                    {
                        throw new HomeException(
                                "The Dunning JasperNotification Processed Failed while processing PDF file. PDF file does not exist for spid : "
                                        + acc.getSpid());
                    }
                }
               else
                {
                	throw new HomeException(template.toString() +" is not supported for Dunning Notification.");
                }
                
                sendNotifications(ctx, destination, notificationType, template,KeyValueFeatureEnum.STATE_CHANGE_EMAIL );
            }
        }		
	}
}
