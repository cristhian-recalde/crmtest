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

/**
 * This is a utility method for sending notification to customer or operator
 * Customer Email & SMS notification is send
 * Operator Email notification is send
 *
 * @author Manish.Negi@redknee.com
 */

package com.trilogy.app.crm.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.trilogy.app.crm.bean.KeyValueFeatureEnum;
import com.trilogy.app.crm.bean.NotificationMethodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.notification.template.NotificationTemplateGroupHome;
import com.trilogy.app.crm.notification.liaison.NotificationLiaison;
import com.trilogy.app.crm.notification.template.NotificationTemplate;
import com.trilogy.app.crm.support.NotificationSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.holder.StringHolder;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.notification.RecipientInfo;
import com.trilogy.app.crm.bean.*;
import com.trilogy.framework.xhome.home.*;
import com.trilogy.app.crm.notification.template.*;
import com.trilogy.app.crm.notification.*;
import com.trilogy.framework.xlog.log.LogSupport;

public class InboundNotificationSupport {

	private static final String MODULE = InboundNotificationSupport.class.getName();
	private static final KeyValueFeatureEnum[] features = { KeyValueFeatureEnum.INBOUND_PAYMENT_FILE_PROCESSING };

	public static void sendCustomerNotification(Context ctx, Subscriber sub, String templateGroupName)
			throws HomeException {

		long accountCategoryId = -1;
		String language = "";
		String emailAddress = null;
		String smsNumber = null;
		int spId = -1;
		NotificationMethodEnum preferredMethod = null;

		try {

			LogSupport.info(ctx, MODULE,
					"sendCustomerNotification:: Sending inbound notification for the subcriber with templateGroupName [ "
							+ templateGroupName + " ]");

			NotificationTypeEnum type = NotificationTypeEnum.CUSTOMER_PAYMENT_PENALTY_INBOUND_NOTIFICATION;
			NotificationLiaison notificationLiaison = getNotificationLiaison(ctx, type);
			RecipientInfo destination = new RecipientInfo();
			if (sub != null) {
				preferredMethod = NotificationMethodEnum.BOTH;
				accountCategoryId = sub.getAccount(ctx).getType();
				language = sub.getAccount(ctx).getLanguage();
				spId = sub.getSpid();

				// Setting the email address for the email notification
				emailAddress = sub.getAccount(ctx).getEmailID();
				if (emailAddress != null && emailAddress.trim().length() > 0)
					{
					EmailAddresses addresses = new EmailAddresses();
					addresses.setTo(Arrays.asList(new StringHolder[] { new StringHolder(emailAddress) }));
					destination.setEmailTo(addresses);
					LogSupport.info(ctx, MODULE,
							"sendCustomerNotification:: Email destination set [ " + destination.getEmailTo() + " ]");
				}else{
					LogSupport.info(ctx, MODULE,
							"sendCustomerNotification:: Email address not configured for the account [" + sub.getAccount(ctx) + " ]");
				}
				// Setting MSISDN number for sms notification
				smsNumber = sub.getMsisdn();
				if (smsNumber != null && smsNumber.trim().length() > 0) {
					destination.setSmsTo(smsNumber);
					LogSupport.info(ctx, MODULE,
							"sendCustomerNotification:: SMS destination set [ " + destination.getSmsTo() + " ]");
				}else{
					LogSupport.info(ctx, MODULE,
							"sendCustomerNotification:: SMS destination not configured for the subscription");
				}
			}

			List<Class<? extends NotificationTemplate>> preferredTypes = NotificationSupportHelper.get(ctx)
					.getPreferredNotificationTypes(preferredMethod);

			// Fetching the templated using the templateGroupId
			Home nGroupHome = (Home) ctx.get(NotificationTemplateGroupHome.class);
			NotificationTemplateGroupID notificationBeanId = new NotificationTemplateGroupID(templateGroupName, spId);
			NotificationTemplateGroup notificationGrpBean = (NotificationTemplateGroup) nGroupHome
					.find(notificationBeanId);
			List<NotificationTemplateHolder> templates = notificationGrpBean.getTemplates();

			// Fetching the templates configured for the group depending on the
			// preferredMethod
			// NotificationMethodEnum.BOTH = EmailNotificationTemplate and
			// SmsNotificationTemplate
			Collection<NotificationTemplate> result = new ArrayList<NotificationTemplate>();
			result = fetchTemplates(ctx, templates, preferredTypes.toArray(new Class[] {}));

			// Sending email and sms notification to the customer
			LogSupport.info(ctx, MODULE, "sendCustomerNotification:: Sending inbound notification to the customer");
			sendNotifications(ctx, result, destination, notificationLiaison);
			LogSupport.info(ctx, MODULE, "sendCustomerNotification:: The inbound notification sent");

		} catch (HomeException e) {
			throw new HomeException("Exception while sending the inbound notification to the customer.");
		}

	}

	private static void sendNotifications(Context ctx, Collection<NotificationTemplate> result,
			RecipientInfo destination, NotificationLiaison notificationLiaison) {

		for (NotificationTemplate notificationTemplate : result) {
			if (LogSupport.isDebugEnabled(ctx)) {
				StringBuilder sb = new StringBuilder();
				sb.append("sendNotifications::- destination: ");
				sb.append(destination);
				sb.append(", templates: ");
				sb.append(notificationTemplate);
				LogSupport.debug(ctx, MODULE, sb.toString());
			}
			notificationLiaison.sendNotification(ctx, notificationTemplate, destination, features);

		}
		LogSupport.info(ctx, MODULE, "sendNotifications:: sent.......");
	}

	private static Collection<NotificationTemplate> fetchTemplates(Context ctx,
			List<NotificationTemplateHolder> templates, Class<NotificationTemplate>... preferredTypes) {

		Collection<NotificationTemplate> result = new ArrayList<NotificationTemplate>();
		for (NotificationTemplateHolder templateHolder : templates) {

			if (templateHolder == null || !templateHolder.isEnabled()) {
				continue;
			}

			NotificationTemplate template = templateHolder.getTemplate();
			if (template == null) {
				continue;
			}

			for (Class<NotificationTemplate> preference : preferredTypes) {
				if (preference != null && preference.isInstance(template)) {
					result.add(template);
					LogSupport.info(ctx, MODULE,
							"fetchTemplates:: Found the preference template [ " + preference + " ]");
					break;
				}
			}

		}
		LogSupport.info(ctx, MODULE, "fetchTemplates:: No of templateds fetched to process [ " + result.size() + " ]");
		return result;
	}

	private static NotificationLiaison getNotificationLiaison(Context ctx, NotificationTypeEnum type) {

		NotificationLiaison notificationLiaison = NotificationSupportHelper.get(ctx).getLiaisonForNotificationType(ctx,
				type);
		return notificationLiaison;
	}

	public static void sendOperatorNotification(Context ctx, String templateGroupName, int spID, String emailAddress)
			throws HomeException {

		int spId = spID;
		NotificationMethodEnum preferredMethod = null;

		try {

			LogSupport.info(ctx, MODULE,
					"sendOperatorNotification:: Sending operator inbound notification with templateGroupName [ "
							+ templateGroupName + " ]");
			NotificationTypeEnum type = NotificationTypeEnum.OPERATOR_INBOUND_NOTIFICATION;
			NotificationLiaison notificationLiaison = getNotificationLiaison(ctx, type);
			RecipientInfo destination = new RecipientInfo();
			preferredMethod = NotificationMethodEnum.EMAIL;
			if (emailAddress != null && emailAddress.trim().length() > 0) {
				EmailAddresses addresses = new EmailAddresses();
				addresses.setTo(Arrays.asList(new StringHolder[] { new StringHolder(emailAddress) }));
				destination.setEmailTo(addresses);
				LogSupport.info(ctx, MODULE,
						"sendOperatorNotification:: Email destination set [ " + destination.getEmailTo() + " ]");
			}
			List<Class<? extends NotificationTemplate>> preferredTypes = NotificationSupportHelper.get(ctx)
					.getPreferredNotificationTypes(preferredMethod);

			// Fetching the template using the templateGroupId
			Home nGroupHome = (Home) ctx.get(NotificationTemplateGroupHome.class);
			NotificationTemplateGroupID notificationBeanId = new NotificationTemplateGroupID(templateGroupName, spId);
			NotificationTemplateGroup notificationGrpBean = (NotificationTemplateGroup) nGroupHome
					.find(notificationBeanId);
			List<NotificationTemplateHolder> templates = notificationGrpBean.getTemplates();

			// Fetching the templates configured for the group depending on the
			// preferredMethod
			// NotificationMethodEnum.BOTH = EmailNotificationTemplate and
			// SmsNotificationTemplate
			Collection<NotificationTemplate> result = new ArrayList<NotificationTemplate>();
			result = fetchTemplates(ctx, templates, preferredTypes.toArray(new Class[] {}));

			// Sending email notification to the operator
			LogSupport.info(ctx, MODULE, "sendOperatorNotification:: Sending inbound notification to the operator");
			sendNotifications(ctx, result, destination, notificationLiaison);
			LogSupport.info(ctx, MODULE, "sendOperatorNotification:: The inbound notification sent");

		} catch (HomeException e) {
			throw new HomeException("Exception while sending the inbound notification to the Operator.");
		}

	}
}
