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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.KeyValueFeatureEnum;
import com.trilogy.app.crm.bean.NotificationMethodEnum;
import com.trilogy.app.crm.bean.NotificationMethodProperty;
import com.trilogy.app.crm.bean.PricePlanChangeTypeEnum;
import com.trilogy.app.crm.bean.RecurringRecharge;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.calculator.TransferDisputeSubscriberValueCalculator;
import com.trilogy.app.crm.delivery.email.RepeatingTemplateTypeEnum;
import com.trilogy.app.crm.extension.spid.NotificationMethodSpidExtension;
import com.trilogy.app.crm.io.OutputStreamFactory;
import com.trilogy.app.crm.notification.EmailAddresses;
import com.trilogy.app.crm.notification.LoggingNotificationResultCallback;
import com.trilogy.app.crm.notification.NotificationResultCallback;
import com.trilogy.app.crm.notification.NotificationTypeEnum;
import com.trilogy.app.crm.notification.RecipientInfo;
import com.trilogy.app.crm.notification.SubscriberNoteNotificationResultCallback;
import com.trilogy.app.crm.notification.liaison.NotificationLiaison;
import com.trilogy.app.crm.notification.liaison.ScheduledTaskNotificationLiaison;
import com.trilogy.app.crm.notification.template.NotificationTemplate;
import com.trilogy.app.crm.notification.template.PricePlanChangeSmsNotificationTemplate;
import com.trilogy.app.crm.notification.template.StateChangeNotificationTemplate;
import com.trilogy.app.crm.state.StateAware;
import com.trilogy.app.crm.transfer.TransferDispute;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.holder.StringHolder;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * @author aaron.gourley@redknee.com
 * @since 8.8/9.0
 */
public class PricePlanNotificationSupport
{
    private static final String MODULE = PricePlanNotificationSupport.class.getName();
    private static final String OLD_PRICE_PLAN="OLD_PRICE_PLAN";
    private static final String NEW_PRICE_PLAN="NEW_PRICE_PLAN";
	private static final String BAN = "BAN";
    
    private PricePlanNotificationSupport()
    {
        // empty
    }
    
    private static void sendNotifications(Context ctx, 
            String emailAddress, String smsNumber, OutputStreamFactory out, 
            NotificationTypeEnum type, 
            Collection<NotificationTemplate> templates,
            KeyValueFeatureEnum... features)
    {
    	Context subCtx = ctx.createSubContext();
    	//PricePlan primaryPricePan= PricePlanSupport.getPlan(ctx, primaryPricePlan);
        RecipientInfo destination = null;
        try
        {
            destination = (RecipientInfo) XBeans.instantiate(RecipientInfo.class, subCtx);
        }
        catch (Exception e)
        {
            destination = new RecipientInfo();
        }

        if (smsNumber != null && smsNumber.trim().length() > 0)
        {
            destination.setSmsTo(smsNumber);
        }
        
        if (emailAddress != null && emailAddress.trim().length() > 0)
        {
            EmailAddresses addresses = new EmailAddresses();
            addresses.setTo(Arrays.asList(new StringHolder[]{new StringHolder(emailAddress)}));
            //destination.setEmailTo(addresses);
        }
        
        if (out != null)
        {
            destination.setPostToGenerator(out);
        }
        
        if (LogSupport.isDebugEnabled(subCtx))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("sendNotifications - destination: ");
            sb.append(destination);
            sb.append(", templates: ");
            sb.append(templates);
            LogSupport.debug(subCtx, MODULE, sb.toString());
        }
        
        if (templates != null)
        {
            NotificationLiaison notificationLiaison = NotificationSupportHelper.get(subCtx).getLiaisonForNotificationType(subCtx, type);
            if (notificationLiaison != null)
            {
                for (NotificationTemplate template : templates)
                {
                    Context sCtx = subCtx.createSubContext();
                    
                    NotificationResultCallback callback = null;
                    
                    Subscriber sub = (Subscriber) sCtx.get(Subscriber.class, sCtx.get(Lookup.OLDSUBSCRIBER));
                    if (sub != null)
                    {
                        SubscriberNoteNotificationResultCallback noteCallback = null;
                        try
                        {
                            noteCallback = (SubscriberNoteNotificationResultCallback) XBeans.instantiate(SubscriberNoteNotificationResultCallback.class, sCtx);
                        }
                        catch (Exception e)
                        {
                            noteCallback = new SubscriberNoteNotificationResultCallback();
                        }
                        callback = noteCallback;
                        noteCallback.setSubId(sub.getId());
                        noteCallback.setEmailAddress(emailAddress);
                        noteCallback.setSmsNumber(smsNumber);
                        noteCallback.setOutFactory(out);
                        noteCallback.setNotificationTypeIndex(type.getIndex());
                        noteCallback.setTemplate(template);
                    }
                    
                    callback = new LoggingNotificationResultCallback(callback);
                    
                    sCtx.put(NotificationResultCallback.class, callback);
                    
                    List<KeyValueFeatureEnum> featureList = new ArrayList<KeyValueFeatureEnum>(Arrays.asList(features));
                    featureList.add(KeyValueFeatureEnum.GENERIC);
                    
                    notificationLiaison.sendNotification(sCtx, template, destination, featureList.toArray(new KeyValueFeatureEnum[]{}));
                }
            }
            else
            {
                LogSupport.info(subCtx, MODULE, "The notificationLiaison not found for type: "+type);
            }
        }
    }
    
    /**
     * Retrieves the notification method of the subscriber.
     * 
     * @param ctx
     *            Operating context.
     * @param sub
     *            Subscriber.
     * @return The notification method of the subscriber.
     */
    private static NotificationMethodEnum getNotificationMethod(Context ctx, Subscriber sub)
    {
        if (sub.getNotificationMethod() != NotificationMethodEnum.DEFAULT_INDEX)
        {
            return NotificationMethodEnum.get((short) sub.getNotificationMethod());
        }
        else
        {
            try
            {
                CRMSpid spid = SpidSupport.getCRMSpid(ctx, sub.getSpid());
                for (Object obj : spid.getExtensions())
                {
                    if (obj instanceof NotificationMethodSpidExtension)
                    {
                        NotificationMethodSpidExtension ext = (NotificationMethodSpidExtension) obj;
                        Long key = Long.valueOf(sub.getSubscriptionType());
                        if (ext.getNotificationMethods().containsKey(key))
                        {
                            return NotificationMethodEnum
                                    .get((short) ((NotificationMethodProperty) ext
                                            .getNotificationMethods().get(key))
                                            .getDefaultMethod());
                        }
                        return NotificationMethodEnum.get((short) ext.getDefaultMethod());
                    }
                }
            }
            catch (HomeException exception)
            {
                new MinorLogMsg(PricePlanNotificationSupport.class,
                        "Cannot retrieve SPID", exception).log(ctx);
            }
            return NotificationMethodEnum.SMS;
        }
    }

    /**
     * Sends the state transition notification.
     * 
     * @param ctx
     *            The operating context.
     * @param oldSub
     *            Old subscriber.
     * @param newSub
     *            New subscriber.
     */
    public static void sendSecondarytoPrimaryNotification(Context ctx,
            Subscriber oldSub, Subscriber newSub)
    {
        try
        {
            Context subCtx = ctx.createSubContext();
            
            long accountCategoryId = -1;
            String language = "";
            NotificationMethodEnum preferredMethod = null;
            String emailAddress = null;
            String smsNumber = null;
            
            Subscriber lookupSub = newSub;
            if (lookupSub == null)
            {
                lookupSub = oldSub;
            }
            
            PricePlan newPlan = PricePlanSupport.getPlan(ctx, newSub.getPricePlan());
            PricePlan oldPlan = PricePlanSupport.getPlan(ctx, oldSub.getPricePlan());
            
            if(oldPlan.getSendswitchnotification()){
            	
            	if (lookupSub != null)
            	{
            		// preferredMethod = getNotificationMethod(subCtx, lookupSub);
            		preferredMethod=NotificationMethodEnum.SMS;
            		smsNumber = lookupSub.getMsisdn();


            		Account account = lookupSub.getAccount(subCtx);
            		if (account != null)
            		{
            			subCtx.put(Account.class, account);

            			accountCategoryId = account.getType();
            			language = account.getLanguage();
            			//emailAddress = account.getEmailID();
            		}
            	}

            	Collection<NotificationTemplate> templates = getStateChangeNotificationTemplates(
            			subCtx, 
            			accountCategoryId, language, 
            			oldSub, newSub, preferredMethod);

            	subCtx.put(Subscriber.class, newSub);
            	subCtx.put(Lookup.OLDSUBSCRIBER, oldSub);

            	subCtx.put(PricePlanNotificationSupport.OLD_PRICE_PLAN, oldPlan);
            	subCtx.put(PricePlanNotificationSupport.NEW_PRICE_PLAN, newPlan);
            	subCtx.put(PricePlanNotificationSupport.BAN, lookupSub.getBAN());

            	NotificationTypeEnum notificationType = NotificationTypeEnum.PRICEPLAN_CHANGE_NOTIFICATION;
            	if (newSub != null)
            	{
            		ScheduledTaskNotificationLiaison.setCleanupKey(subCtx, newSub.getId() + notificationType);
            	}
            	sendNotifications(subCtx, 
            			emailAddress, smsNumber, null,
            			notificationType,
            			templates,
            			KeyValueFeatureEnum.PRICE_PLAN_CHANGE_NOTIFICATION);
            }
        }
        catch (HomeException exception)
        {
            new MinorLogMsg(PricePlanNotificationSupport.class,
                    "Error sending notification change request for primary/secondary switch"
                            + newSub.getId() +oldSub.getId(), exception).log(ctx);
        }
    }

    private static Collection<NotificationTemplate> getStateChangeNotificationTemplates(Context ctx, 
            long accountCategoryId, String language, 
            StateAware oldStateOwner, StateAware newStateOwner, 
            NotificationMethodEnum preferredMethod) throws HomeException
    {
        List<Class<? extends NotificationTemplate>> preferredTypes = NotificationSupportHelper.get(ctx).getPreferredNotificationTypes(preferredMethod);
        
        Collection<NotificationTemplate> templates = NotificationSupportHelper.get(ctx).getTemplates(ctx, 
                accountCategoryId, language, 
                NotificationTypeEnum.PRICEPLAN_CHANGE_NOTIFICATION, 
                preferredTypes.toArray(new Class[]{}));
        
        if (templates != null)
        {
            Iterator<NotificationTemplate> iter = templates.iterator();
            while (iter.hasNext())
            {
                NotificationTemplate template = iter.next();
                if (template instanceof PricePlanChangeSmsNotificationTemplate)
                {
                	PricePlanChangeSmsNotificationTemplate stateTmpl = (PricePlanChangeSmsNotificationTemplate) template;
                    
                	if (!(stateTmpl.getPricePlanChangeType()==PricePlanChangeTypeEnum.PRIMARY_SECONDARY_SWITCH_INDEX)){
                		iter.remove();
                		continue;
                	}
                }
            }
        }
		return templates;
    }
        
                 

    /**
     * Sends transfer dispute notification.
     * 
     * @param ctx
     *            The operating context.
     * @param dispute
     *            The transfer dispute.
     */
    /*public static void sendTransferDisputeNotification(Context ctx,
            TransferDispute dispute)
    {
        if (!TransferSupport.OPERATOR_ID.equals(dispute.getRecpSubId()))
        {
            Subscriber recpSub = null;
            try
            {
                recpSub = SubscriberSupport.getSubscriber(ctx, dispute
                        .getRecpSubId());
            }
            catch (HomeException exception)
            {
                new MinorLogMsg(PricePlanNotificationSupport.class,
                        "Fail to look up recipient subscriber of transfer dispute "
                                + dispute.getID(), exception).log(ctx);
            }

            if (recpSub != null)
            {
                sendTransferDisputeNotification(ctx, dispute, recpSub);
            }
        }
    }

    *//**
     * Sends transfer dispute email.
     * 
     * @param ctx
     *            The operating context.
     * @param dispute
     *            The transfer dispute.
     * @param subscriber
     *            The recipient subscriber.
     *//*
    private static void sendTransferDisputeNotification(Context ctx,
            TransferDispute dispute, Subscriber sub)
    {
        try
        {
            Context subCtx = ctx.createSubContext();

            long accountCategoryId = -1;
            String language = "";
            NotificationMethodEnum preferredMethod = null;
            String emailAddress = null;
            String smsNumber = null;
            
            if (sub != null)
            {
                preferredMethod = getNotificationMethod(subCtx, sub);
                smsNumber = sub.getMsisdn();
                
                Account account = sub.getAccount(subCtx);
                if (account != null)
                {
                    subCtx.put(Account.class, account);
                    
                    accountCategoryId = account.getType();
                    language = account.getLanguage();
                    emailAddress = account.getEmailID();
                }
            }
            
            List<Class<? extends NotificationTemplate>> preferredNotificationTypes = NotificationSupportHelper.get(ctx).getPreferredNotificationTypes(preferredMethod);
            
            Collection<NotificationTemplate> templates = NotificationSupportHelper.get(subCtx).getTemplates(
                    subCtx, 
                    accountCategoryId, language, 
                    NotificationTypeEnum.TRANSFER_DISPUTE, 
                    preferredNotificationTypes.toArray(new Class[]{}));
            
            subCtx.put(Subscriber.class, sub);
            subCtx
                    .put(
                            TransferDisputeSubscriberValueCalculator.RECIPIENT_SUBSCRIBER_KEY,
                            sub);
            subCtx
                    .put(
                            TransferDisputeSubscriberValueCalculator.CONTRIBUTOR_SUBSCRIBER_KEY,
                            sub);
            
            Subscriber contribSub = SubscriberSupport.getSubscriber(subCtx, dispute.getContSubId());
            subCtx.put(TransferDispute.class, dispute);

            NotificationTypeEnum notificationType = NotificationTypeEnum.TRANSFER_DISPUTE;
            if (sub != null)
            {
                ScheduledTaskNotificationLiaison.setCleanupKey(subCtx, sub.getId() + notificationType);
            }
            sendNotifications(subCtx, 
                    emailAddress, smsNumber, null,
                    notificationType,
                    templates,
                    KeyValueFeatureEnum.TRANSFER_DISPUTE_EMAIL);
        }
        catch (HomeException exception)
        {
            new MinorLogMsg(PricePlanNotificationSupport.class,
                    "Error sending transfer dispute notification email for dispute ID "
                            + dispute.getID(), exception).log(ctx);
        }
    }

    *//**
     * Sends voucher topup notification.
     * Modified for TT#12090730043
     * 
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The recipient subscriber.
     * @param transaction
     *            The transaction of the recharge.
     *//*
    public static void sendVoucherTopupNotification(Context ctx, Subscriber sub, Transaction transaction)
    {
        try
        {
            Context subCtx = ctx.createSubContext();

            long accountCategoryId = -1;
            String language = "";
            NotificationMethodEnum preferredMethod = null;
            String emailAddress = null;
            String smsNumber = null;
            
            if (sub == null)
            {
            	if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(
                            PricePlanNotificationSupport.class, "Error sending voucher topup notification, Subscriber not found.", null).log(ctx);
                }
                return;
            }
            
            CRMSpid spid = SpidSupport.getCRMSpid(ctx, sub.getSpid());

            if (!spid.isEnableVoucherTopupNotification())
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(
                            PricePlanNotificationSupport.class,
                            "Voucher topup notification is not enabled for SPID = " + spid.getSpid(), null).log(ctx);
                }
                return;
            }
            
            preferredMethod = getNotificationMethod(subCtx, sub);
            smsNumber = sub.getMsisdn();
            
            Account account = sub.getAccount(subCtx);
            if (account != null)
            {
                subCtx.put(Account.class, account);
                
                accountCategoryId = account.getType();
                language = account.getLanguage();
                emailAddress = account.getEmailID();
            }
        
            
            List<Class<? extends NotificationTemplate>> preferredNotificationTypes = NotificationSupportHelper.get(ctx).getPreferredNotificationTypes(preferredMethod);
            
            Collection<NotificationTemplate> templates = NotificationSupportHelper.get(subCtx).getTemplates(
                    subCtx, 
                    accountCategoryId, language, 
                    NotificationTypeEnum.VOUCHER_TOPUP,
                    preferredNotificationTypes.toArray(new Class[]{}));
            
            subCtx.put(Transaction.class, transaction);
            subCtx.put(Subscriber.class, sub);

            NotificationTypeEnum notificationType = NotificationTypeEnum.VOUCHER_TOPUP;
            if (sub != null)
            {
                ScheduledTaskNotificationLiaison.setCleanupKey(subCtx, sub.getId() + notificationType);
            }
            sendNotifications(subCtx, 
                    emailAddress, smsNumber, null,
                    notificationType,
                    templates,
                    new KeyValueFeatureEnum[]{});
        }
        catch (HomeException exception)
        {
            new MinorLogMsg(PricePlanNotificationSupport.class,
                    "Error sending voucher topup notification for subscription ID "
                            + sub.getId() + " of amount "
                            + Math.abs(transaction.getAmount()), exception).log(ctx);
        }
    }

    *//**
     * Sends expiry date extension notification Email.
     * 
     * @param ctx
     *            Operating context.
     * @param subscriber
     *            Subscriber.
     * @throws HomeException
     *             Thrown if there are problems sending the message.
     *//*
    public static void sendExpiryDateExtensionNotification(
            final Context ctx, final Subscriber sub)
            throws HomeException
    {
        try
        {
            Context subCtx = ctx.createSubContext();

            long accountCategoryId = -1;
            String language = "";
            NotificationMethodEnum preferredMethod = null;
            String emailAddress = null;
            String smsNumber = null;
            
            if (sub != null)
            {
                preferredMethod = getNotificationMethod(subCtx, sub);
                smsNumber = sub.getMsisdn();
                
                Account account = sub.getAccount(subCtx);
                if (account != null)
                {
                    subCtx.put(Account.class, account);
                    
                    accountCategoryId = account.getType();
                    language = account.getLanguage();
                    emailAddress = account.getEmailID();
                }
            }
            
            List<Class<? extends NotificationTemplate>> preferredNotificationTypes = NotificationSupportHelper.get(ctx).getPreferredNotificationTypes(preferredMethod);
            
            Collection<NotificationTemplate> templates = NotificationSupportHelper.get(subCtx).getTemplates(
                    subCtx, 
                    accountCategoryId, language, 
                    NotificationTypeEnum.EXPIRY_EXTENSION, 
                    preferredNotificationTypes.toArray(new Class[]{}));
            
            subCtx.put(Subscriber.class, sub);

            NotificationTypeEnum notificationType = NotificationTypeEnum.EXPIRY_EXTENSION;
            if (sub != null)
            {
                ScheduledTaskNotificationLiaison.setCleanupKey(subCtx, sub.getId() + notificationType);
            }
            sendNotifications(subCtx, 
                    emailAddress, smsNumber, null,
                    notificationType,
                    templates,
                    KeyValueFeatureEnum.EXPIRY_EXTENSION_EMAIL);
        }
        catch (HomeException exception)
        {
            new MinorLogMsg(PricePlanNotificationSupport.class,
                    "Error sending expiry date extension notification email for subscription ID "
                            + sub.getId(), exception).log(ctx);
        }
    }
    
    
    *//**
     * Send the suspension/unsuspension notification email.
     * 
     * @param ctx
     *            The operating context.
     * @param sub
     *            Subscriber to be notified.
     * @param packages
     *            Packages to be suspended/unsuspended.
     * @param services
     *            Services to be suspended/unsuspended.
     * @param bundles
     *            Bundles to be suspended/unsuspended.
     * @param auxServices
     *            Auxiliary services to be suspended/unsuspended.
     * @param suspend
     *            Whether this is suspension (true) or unsuspension (false).
     *//*
    public static void sendSuspendNotification(final Context ctx,
            final Subscriber sub, final Map packages, final Map services,
            final Map bundles,
            final Map<Long, Map<Long, SubscriberAuxiliaryService>> auxServices,
            boolean suspend)
    {
        try
        {
            Context subCtx = ctx.createSubContext();

            long accountCategoryId = -1;
            String language = "";
            NotificationMethodEnum preferredMethod = null;
            String emailAddress = null;
            String smsNumber = null;
            
            if (sub != null)
            {
                preferredMethod = getNotificationMethod(subCtx, sub);
                smsNumber = sub.getMsisdn();
                
                Account account = sub.getAccount(subCtx);
                if (account != null)
                {
                    subCtx.put(Account.class, account);
                    
                    accountCategoryId = account.getType();
                    language = account.getLanguage();
                    emailAddress = account.getEmailID();
                }
            }
            
            List<Class<? extends NotificationTemplate>> preferredNotificationTypes = NotificationSupportHelper.get(ctx).getPreferredNotificationTypes(preferredMethod);
            
            Collection<NotificationTemplate> templates = NotificationSupportHelper.get(subCtx).getTemplates(
                    subCtx, 
                    accountCategoryId, language, 
                    suspend ? NotificationTypeEnum.SERVICE_SUSPENSION
                            : NotificationTypeEnum.SERVICE_UNSUSPENSION, 
                    preferredNotificationTypes.toArray(new Class[]{}));
            
            subCtx.put(Subscriber.class, sub);
            subCtx.put(RepeatingTemplateTypeEnum.SERVICE, associateServiceNameForServiceFee2(subCtx, services));
            subCtx.put(RepeatingTemplateTypeEnum.PACKAGE, packages);
            subCtx.put(RepeatingTemplateTypeEnum.BUNDLE, bundles);
            subCtx.put(RepeatingTemplateTypeEnum.AUXILIARY_SERVICE, auxServices);

            NotificationTypeEnum notificationType = suspend ? 
                    NotificationTypeEnum.SERVICE_SUSPENSION : 
                        NotificationTypeEnum.SERVICE_UNSUSPENSION;
            if (sub != null)
            {
                ScheduledTaskNotificationLiaison.setCleanupKey(subCtx, sub.getId() + notificationType);
            }
            sendNotifications(subCtx, 
                    emailAddress, smsNumber, null,
                    notificationType,
                    templates,
                    suspend ? 
                            KeyValueFeatureEnum.SERVICE_SUSPENSION_EMAIL : 
                                KeyValueFeatureEnum.SERVICE_UNSUSPENSION_EMAIL);
        }
        catch (HomeException exception)
        {
            new MinorLogMsg(PricePlanNotificationSupport.class,
                    "Error sending service suspension notification email of subscriber "
                            + sub.getId(), exception).log(ctx);
        }
    }    

    *//**
     * Sends an Insufficient Balance Notification to a subscription. Please check for {@link CRMSpid#getRecChrgPrepdInsufBalNotifDaysBefore()}
     * for configuration.
     * 
     * @param ctx
     * @param sub
     * @param items
     * @param billingDate
     * @param chargingCycle
     * @param recurringRecharge
     *//*
    public static void sendPreWarnInsufficientBalanceNotification(final Context ctx, final Subscriber sub, final Map<Class,List<Object>> items, Date billingDate, 
    		ChargingCycleEnum chargingCycle , RecurringRecharge recurringRecharge)
    {
    	sendNotification(ctx, sub, items, billingDate, chargingCycle, recurringRecharge, 
    			NotificationTypeEnum.PREPAID_RECURRING_RECHARGE_PREWARNING_INSUFFICIENT_BAL, KeyValueFeatureEnum.RECURRING_RECHARGE_PREWARN_EMAIL_INSUFFICIENT_BALANCE);
    }
    
    *//**
     * 
     * Sends a Recurring Recharge Notification.
     * 
     * @param ctx
     * @param sub
     * @param items
     * @param billingDate
     * @param chargingCycle
     * @param recurringRecharge
     *//*
    public static void sendRecurringRechargeNotification(final Context ctx, final Subscriber sub, final Map<Class,List<Object>> items, Date billingDate, 
    		ChargingCycleEnum chargingCycle , RecurringRecharge recurringRecharge)
    {
    	sendNotification(ctx, sub, items, billingDate, chargingCycle, recurringRecharge, NotificationTypeEnum.PREPAID_RECHARGE, KeyValueFeatureEnum.PREPAID_RECHARGE_EMAIL);
    }
    
    *//**
     * 
     * Sends a Recurring Recharge Failure Notification.
     * 
     * @param ctx
     * @param sub
     * @param items
     * @param billingDate
     * @param chargingCycle
     * @param recurringRecharge
     *//*
    public static void sendRecurringRechargeFailureNotification(final Context ctx, final Subscriber sub, final Map<Class,List<Object>> items, Date billingDate, 
    		ChargingCycleEnum chargingCycle , RecurringRecharge recurringRecharge)
    {
    	sendNotification(ctx, sub, items, billingDate, chargingCycle, recurringRecharge, NotificationTypeEnum.PREPAID_RECHARGE_FAILURE, KeyValueFeatureEnum.PREPAID_RECHARGE_EMAIL);
    }    

    private static void sendNotification(final Context ctx, final Subscriber sub, final Map<Class,List<Object>> items, 
    		Date billingDate, ChargingCycleEnum chargingCycle, RecurringRecharge recurringRecharge,
    		NotificationTypeEnum notificationType , KeyValueFeatureEnum keyValue)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("BEGIN - sendNotification for Subscriber: ");
            sb.append( sub!=null ? sub.getId() : "null");
            sb.append(", billingDate: ");
            sb.append(billingDate);
            sb.append(", items: ");
            sb.append(items);
            sb.append(", chargingCycle: ");
            sb.append(chargingCycle);
            sb.append(", notificationType: ");
            sb.append(notificationType);
            sb.append(", recurringRecharge: ");
            sb.append(recurringRecharge);
            sb.append(", keyValue: ");
            sb.append(keyValue);
            LogSupport.debug(ctx, MODULE, sb.toString());
        }
        
        try
        {
            Context subCtx = ctx.createSubContext();
            
            long accountCategoryId = -1;
            String language = "";
            NotificationMethodEnum preferredMethod = null;
            String emailAddress = null;
            String smsNumber = null;
            
            if (sub != null)
            {
                preferredMethod = getNotificationMethod(subCtx, sub);
                smsNumber = sub.getMsisdn();
                
                Account account = sub.getAccount(subCtx);
                if (account != null)
                {
                    subCtx.put(Account.class, account);
                    
                    accountCategoryId = account.getType();
                    language = account.getLanguage();
                    emailAddress = account.getEmailID();
                }
            }
            
            List<Class<? extends NotificationTemplate>> preferredNotificationTypes = NotificationSupportHelper.get(ctx).getPreferredNotificationTypes(preferredMethod);
            
            Collection<NotificationTemplate> templates = NotificationSupportHelper.get(subCtx).getTemplates(
                    subCtx, 
                    accountCategoryId, language, 
                    notificationType, 
                    preferredNotificationTypes.toArray(new Class[]{}));
            
            subCtx.put(Subscriber.class, sub);
            subCtx.put(RecurringRecharge.class, recurringRecharge);
            subCtx.put(ChargingCycleEnum.class, chargingCycle);
            subCtx.put(Date.class, billingDate);
            subCtx.put(RepeatingTemplateTypeEnum.SERVICE, associateServiceNameForServiceFee2(subCtx , items.get(ServiceFee2.class)) );
            subCtx.put(RepeatingTemplateTypeEnum.PACKAGE, items.get(ServicePackageFee.class));
            subCtx.put(RepeatingTemplateTypeEnum.BUNDLE, items.get(BundleFee.class));
            subCtx.put(RepeatingTemplateTypeEnum.AUXILIARY_SERVICE, items.get(AuxiliaryService.class));

            if (sub != null)
            {
                ScheduledTaskNotificationLiaison.setCleanupKey(subCtx, sub.getId() + notificationType);
            }
            sendNotifications(subCtx, 
                    emailAddress, smsNumber, null,
                    notificationType,
                    templates,
                    keyValue);
        }
        catch (Throwable t)
        {
            new MinorLogMsg(PricePlanNotificationSupport.class,
                    "Error sending pre warn notification email of subscriber "
                            + sub.getId(), t).log(ctx);
        }
        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("END - sendNotification for Subscriber: ");
            sb.append(sub!=null ? sub.getId() : "null");
            sb.append(", billingDate: ");
            sb.append(billingDate);
            sb.append(", chargingCycle: ");
            sb.append(chargingCycle);
            LogSupport.debug(ctx, MODULE, sb.toString());
        }
    }
    
    private static List associateServiceNameForServiceFee2(Context ctx, final List services)
    {	
    	for (Object serviceObj : services)
    	{
    		try
    		{
    			ServiceFee2 service = (ServiceFee2)serviceObj;
    			service.setServiceName(service.getService(ctx).getName());
    		}
    		catch (Throwable e)
    		{
    			LogSupport.minor(ctx, PricePlanNotificationSupport.class.getName(), "Could not set service name");
    		}
    	}
    	
    	return services;
    }
    
    private static Map associateServiceNameForServiceFee2(Context ctx, final Map services)
    {	
    	Iterator serviceIterator = services.entrySet().iterator();
    	
    	while(serviceIterator.hasNext())
    	{
    		Entry entry = (Entry)serviceIterator.next();
    		
    		try
    		{
    			ServiceFee2 fee = (ServiceFee2)entry.getValue();
    			fee.setServiceName(fee.getService(ctx).getName());
    		}
    		catch(Throwable e)
    		{
    			LogSupport.minor(ctx, PricePlanNotificationSupport.class.getName(), "Could not associate service name from Map.");
    		}
    	}
    	
    	return services;
    }
    
    public static void sendPreWarnNotification(final Context ctx,
            final Subscriber sub, final Map<Class,List<Object>> items, Date billingDate, ChargingCycleEnum chargingCycle, RecurringRecharge recurringRecharge)
    {
    	sendNotification(ctx, sub, items, billingDate, chargingCycle, recurringRecharge,
    			NotificationTypeEnum.PREPAID_RECURRING_RECHARGE_PREWARNING, KeyValueFeatureEnum.RECURRING_RECHARGE_PREWARN_EMAIL);
    }

    *//**
     * Sends pre-expiry notification.
     * 
     * @param ctx
     *            Operating context.
     * @param subscriber
     *            Subscriber.
     * @throws HomeException
     *             Thrown if there are problems sending the message.
     *//*
    public static void sendPreExpiryNotification(final Context ctx, final Subscriber sub)
    {
        try
        {
            Context subCtx = ctx.createSubContext();

            long accountCategoryId = -1;
            String language = "";
            NotificationMethodEnum preferredMethod = null;
            String emailAddress = null;
            String smsNumber = null;
            
            if (sub != null)
            {
                preferredMethod = getNotificationMethod(subCtx, sub);
                smsNumber = sub.getMsisdn();
                
                Account account = sub.getAccount(subCtx);
                if (account != null)
                {
                    subCtx.put(Account.class, account);
                    
                    accountCategoryId = account.getType();
                    language = account.getLanguage();
                    emailAddress = account.getEmailID();
                }
            }
            
            List<Class<? extends NotificationTemplate>> preferredNotificationTypes = NotificationSupportHelper.get(ctx).getPreferredNotificationTypes(preferredMethod);
            
            Collection<NotificationTemplate> templates = NotificationSupportHelper.get(subCtx).getTemplates(
                    subCtx, 
                    accountCategoryId, language, 
                    NotificationTypeEnum.PRE_EXPIRY, 
                    preferredNotificationTypes.toArray(new Class[]{}));
            
            subCtx.put(Subscriber.class, sub);

            NotificationTypeEnum notificationType = NotificationTypeEnum.PRE_EXPIRY;
            if (sub != null)
            {
                ScheduledTaskNotificationLiaison.setCleanupKey(subCtx, sub.getId() + notificationType);
            }
            sendNotifications(subCtx, 
                    emailAddress, smsNumber, null,
                    notificationType,
                    templates,
                    KeyValueFeatureEnum.PRE_EXPIRY_EMAIL);
        }
        catch (HomeException exception)
        {
            new MinorLogMsg(PricePlanNotificationSupport.class,
                    "Error sending expiry date extension notification email for subscription ID "
                            + (sub != null ? sub.getId() : null), exception).log(ctx);
        }
    }
    
    public static void removePendingNotifications(Context ctx, String subId, NotificationTypeEnum type)
    {
        NotificationSupportHelper.get(ctx).removePendingNotifications(ctx, subId + type);
    }*/
}

