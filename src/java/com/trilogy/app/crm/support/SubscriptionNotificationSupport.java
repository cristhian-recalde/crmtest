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
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.trilogy.app.crm.agent.CronConstant;
import com.trilogy.app.crm.bas.recharge.ApplyRecurRecharge;
import com.trilogy.app.crm.bas.recharge.RechargeConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.KeyValueFeatureEnum;
import com.trilogy.app.crm.bean.NotificationMethodEnum;
import com.trilogy.app.crm.bean.NotificationMethodProperty;
import com.trilogy.app.crm.bean.RecurringRecharge;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.app.crm.calculator.TransferDisputeSubscriberValueCalculator;
import com.trilogy.app.crm.delivery.email.RepeatingTemplateTypeEnum;
import com.trilogy.app.crm.extension.spid.NotificationMethodSpidExtension;
import com.trilogy.app.crm.io.OutputStreamFactory;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.notification.EmailAddresses;
import com.trilogy.app.crm.notification.LoggingNotificationResultCallback;
import com.trilogy.app.crm.notification.NotificationResultCallback;
import com.trilogy.app.crm.notification.NotificationTypeEnum;
import com.trilogy.app.crm.notification.RecipientInfo;
import com.trilogy.app.crm.notification.SubscriberNoteNotificationResultCallback;
import com.trilogy.app.crm.notification.TFAParticipantEnum;
import com.trilogy.app.crm.notification.liaison.NotificationLiaison;
import com.trilogy.app.crm.notification.liaison.ScheduledTaskNotificationLiaison;
import com.trilogy.app.crm.notification.template.NotificationTemplate;
import com.trilogy.app.crm.notification.template.StateChangeNotificationTemplate;
import com.trilogy.app.crm.notification.template.TFAEmailNotificationTemplate;
import com.trilogy.app.crm.notification.template.TFASMSNotificationTemplate;
import com.trilogy.app.crm.state.StateAware;
import com.trilogy.app.crm.transfer.TransferDispute;

import com.trilogy.framework.core.locale.Currency;
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
public class SubscriptionNotificationSupport
{
    private static final String MODULE = SubscriptionNotificationSupport.class.getName();
    private static final int RESULT_SUCCESS  = 0; 
    private static final int RESULT_FAILURE  = 1;
    private SubscriptionNotificationSupport()
    {
        // empty
    }
    
    public static void sendNotifications(Context ctx, 
            String emailAddress, String smsNumber, OutputStreamFactory out, 
            NotificationTypeEnum type, 
            Collection<NotificationTemplate> templates,
            KeyValueFeatureEnum... features)
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

        if (smsNumber != null && smsNumber.trim().length() > 0)
        {
            destination.setSmsTo(smsNumber);
        }
        
        if (emailAddress != null && emailAddress.trim().length() > 0)
        {
            EmailAddresses addresses = new EmailAddresses();
            addresses.setTo(Arrays.asList(new StringHolder[]{new StringHolder(emailAddress)}));
            destination.setEmailTo(addresses);
        }
        
        if (out != null)
        {
            destination.setPostToGenerator(out);
        }
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("sendNotifications - destination: ");
            sb.append(destination);
            sb.append(", templates: ");
            sb.append(templates);
            LogSupport.debug(ctx, MODULE, sb.toString());
        }
        
        if (templates != null)
        {
            NotificationLiaison notificationLiaison = NotificationSupportHelper.get(ctx).getLiaisonForNotificationType(ctx, type);
            if (notificationLiaison != null)
            {
                for (NotificationTemplate template : templates)
                {
                    Context sCtx = ctx.createSubContext();
                    
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
                LogSupport.info(ctx, MODULE, "The notificationLiaison not found for type: "+type);
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
    public static NotificationMethodEnum getNotificationMethod(Context ctx, Subscriber sub)
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
                Collection spidExtensions = Collections.emptyList();
                if (ctx.get(RechargeConstants.CACHED_SPID_EXTENSIONS)!=null && 
                		!((Collection)ctx.get(RechargeConstants.CACHED_SPID_EXTENSIONS)).isEmpty()) 
                {
                	spidExtensions = (Collection)ctx.get(RechargeConstants.CACHED_SPID_EXTENSIONS);
                }
                else 
                {
                	spidExtensions = spid.getExtensions();
                }
                
                for (Object obj : spidExtensions)
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
                new MinorLogMsg(SubscriptionNotificationSupport.class,
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
    public static void sendStateTransitionNotification(Context ctx,
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
            if (lookupSub != null)
            {
                preferredMethod = getNotificationMethod(subCtx, lookupSub);
                smsNumber = lookupSub.getMsisdn();
                
                Account account = lookupSub.getAccount(subCtx);
                if (account != null)
                {
                    subCtx.put(Account.class, account);
                    
                    accountCategoryId = account.getType();
                    //language = account.getLanguage();
                    language = lookupSub.getBillingLanguage();
                    emailAddress = account.getEmailID();
                }
            }
            
            Collection<NotificationTemplate> templates = getStateChangeNotificationTemplates(
                    subCtx, 
                    accountCategoryId, language, 
                    oldSub, newSub, preferredMethod);

            subCtx.put(Subscriber.class, newSub);
            subCtx.put(Lookup.OLDSUBSCRIBER, oldSub);

            NotificationTypeEnum notificationType = NotificationTypeEnum.STATE_CHANGE;
            if (newSub != null)
            {
                ScheduledTaskNotificationLiaison.setCleanupKey(subCtx, newSub.getId() + notificationType);
            }
            sendNotifications(subCtx, 
                    emailAddress, smsNumber, null,
                    notificationType,
                    templates,
                    KeyValueFeatureEnum.STATE_CHANGE_EMAIL);
            ERLogger.createSubscriberStateChangeNotifiactionEr( subCtx, oldSub, newSub,preferredMethod.getDescription(), RESULT_SUCCESS);
        }
        catch (HomeException exception)
        {
            new MinorLogMsg(SubscriptionNotificationSupport.class,
                    "Error sending state transition notification of subscriber "
                            + newSub.getId(), exception).log(ctx);
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
                NotificationTypeEnum.STATE_CHANGE, 
                preferredTypes.toArray(new Class[]{}));
        
        if (templates != null)
        {
            Iterator<NotificationTemplate> iter = templates.iterator();
            while (iter.hasNext())
            {
                NotificationTemplate template = iter.next();
                if (template instanceof StateChangeNotificationTemplate)
                {
                    StateChangeNotificationTemplate stateTmpl = (StateChangeNotificationTemplate) template;
                    
                    EnumStateSupport enumStateSupport = EnumStateSupportHelper.get(ctx);

                    // Don't send notifications if state did not change.
                    if (enumStateSupport.stateEquals(oldStateOwner, newStateOwner))
                    {
                        iter.remove();
                        continue;
                    }
                    
                    if (stateTmpl.getPreviousState() == -1
                            && stateTmpl.getNewState() == -1)
                    {
                        // any state <-> any state = valid
                        continue;
                    }
                    
                    // Check the "From Any State" conditions 
                    if (stateTmpl.getPreviousState() == -1)
                    {
                    	if (oldStateOwner == null)
                        {
                            if (!enumStateSupport.stateEquals(newStateOwner, stateTmpl.getNewState()))
                            {
                            	 // This is a new object creation not satisfying criteria
                                iter.remove();
                                continue;
                            }
                        }
                        else if (newStateOwner != null
                                    && !enumStateSupport.isEnteringState(oldStateOwner, newStateOwner, stateTmpl.getNewState()))
                        {
                        	// State change to a state not satisfying criteria
                            iter.remove();
                            continue;
                        }
                    }

                    // Check the "To Any State" conditions
                    if (stateTmpl.getNewState() == -1)
                    {
                        if (newStateOwner == null)
                        {
                            if (!enumStateSupport.stateEquals(oldStateOwner, stateTmpl.getPreviousState()))
                            {
                                // This is an object deletion not satisfying criteria
                                iter.remove();
                                continue;
                            }
                        }
                        else if (oldStateOwner != null
                                && !enumStateSupport.isLeavingState(oldStateOwner, newStateOwner, stateTmpl.getPreviousState()))
                        {
                        	// State change from a state not satisfying criteria
                            iter.remove(); 
                            continue;
                        }
                    }

                    // Check the "State To State" conditions
                    if (!enumStateSupport.isTransition(oldStateOwner, newStateOwner, stateTmpl.getPreviousState(), stateTmpl.getNewState()) &&  stateTmpl.getPreviousState() != -1)
                    {                    	
                        // Remove state change templates that are not applicable to this state change                    	
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
    public static void sendTransferDisputeNotification(Context ctx,
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
                new MinorLogMsg(SubscriptionNotificationSupport.class,
                        "Fail to look up recipient subscriber of transfer dispute "
                                + dispute.getID(), exception).log(ctx);
            }

            if (recpSub != null)
            {
                sendTransferDisputeNotification(ctx, dispute, recpSub);
            }
        }
    }

    /**
     * Sends transfer dispute email.
     * 
     * @param ctx
     *            The operating context.
     * @param dispute
     *            The transfer dispute.
     * @param subscriber
     *            The recipient subscriber.
     */
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
                   // language = account.getLanguage();
                    language = sub.getBillingLanguage();
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
            new MinorLogMsg(SubscriptionNotificationSupport.class,
                    "Error sending transfer dispute notification email for dispute ID "
                            + dispute.getID(), exception).log(ctx);
        }
    }

    /**
     * Sends voucher topup notification.
     * Modified for TT#12090730043
     * 
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The recipient subscriber.
     * @param transaction
     *            The transaction of the recharge.
     */
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
                            SubscriptionNotificationSupport.class, "Error sending voucher topup notification, Subscriber not found.", null).log(ctx);
                }
                return;
            }
            
            CRMSpid spid = SpidSupport.getCRMSpid(ctx, sub.getSpid());

            if (!spid.isEnableVoucherTopupNotification())
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(
                            SubscriptionNotificationSupport.class,
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
                //language = account.getLanguage();
                language = sub.getBillingLanguage();
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
            
            ERLogger.createVoucherNotifiactionEr(subCtx, sub.getMsisdn(), String.valueOf(transaction.getAmount()), null, sub, preferredMethod.getDescription(),NotificationTypeEnum.VOUCHER_TOPUP.getDescription(),RESULT_SUCCESS );

        }
        catch (HomeException exception)
        {
            new MinorLogMsg(SubscriptionNotificationSupport.class,
                    "Error sending voucher topup notification for subscription ID "
                            + sub.getId() + " of amount "
                            + Math.abs(transaction.getAmount()), exception).log(ctx);
        }
    }
    
    /**
    * @param ctx
    *            The operating context.
    * @param subscriber
    *            The recipient subscriber.
    */
   public static void sendScheduledPPChangeNotification(Context ctx, Subscriber sub, int resultCode, String resultDescription)
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
                           SubscriptionNotificationSupport.class, "Error sending scheduled price plan change notification, Subscriber not found.", null).log(ctx);
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
               //language = account.getLanguage();
               language = sub.getBillingLanguage();
               emailAddress = account.getEmailID();
           }
       
           
           List<Class<? extends NotificationTemplate>> preferredNotificationTypes = NotificationSupportHelper.get(ctx).getPreferredNotificationTypes(preferredMethod);
           
           Collection<NotificationTemplate> templates = NotificationSupportHelper.get(subCtx).getTemplates(
                   subCtx, 
                   accountCategoryId, language, 
                   NotificationTypeEnum.SCHEDULED_PRICEPLAN_CHANGE,
                   preferredNotificationTypes.toArray(new Class[]{}));
           
           if(resultCode == CronConstant.SCHEDULED_PP_CHANGE_SUCCESS)
           {
               subCtx.put("PP_CHANGE_STATUS", " Price Plan changed Successfully ");
           }
           if(resultCode == CronConstant.SCHEDULED_PP_CHANGE_FAILED)
           {
               subCtx.put("PP_CHANGE_STATUS", " Price Plan change failed : " + resultDescription + ".");
           }
           
           subCtx.put(Subscriber.class, sub);
           
           NotificationTypeEnum notificationType = NotificationTypeEnum.SCHEDULED_PRICEPLAN_CHANGE;
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
           new MinorLogMsg(SubscriptionNotificationSupport.class,
                   "Error sending scheduled price plan change notification for subscription ID "
                           + sub.getId(), exception).log(ctx);
       }
   }

    
    public static void sendTFANotification(Context ctx, Subscriber sub, Transaction transaction)
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
                            SubscriptionNotificationSupport.class, "Error sending TFA notification, Subscriber not found.", null).log(ctx);
                }
                return;
            }
            
            CRMSpid spid = SpidSupport.getCRMSpid(ctx, sub.getSpid());

            if (!spid.isEnablePackageNotification())
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(
                            SubscriptionNotificationSupport.class,
                            "TFA notification is not enabled for SPID = " + spid.getSpid(), null).log(ctx);
                }
                return;
            }
            String currency = spid.getCurrency();
            String transAmount = CurrencyPrecisionSupportHelper.get(ctx).formatDisplayCurrencyValue(ctx, currency, Math.abs(transaction.getAmount()));
            String transNewBalance = CurrencyPrecisionSupportHelper.get(ctx).formatDisplayCurrencyValue(ctx, currency, Math.abs(transaction.getBalance()));
            
            preferredMethod = getNotificationMethod(subCtx, sub);
            smsNumber = sub.getMsisdn();
            
            Account account = sub.getAccount(subCtx);
            if (account != null)
            {
                subCtx.put(Account.class, account);
                
                accountCategoryId = account.getType();
                language = sub.getBillingLanguage();
                emailAddress = account.getEmailID();
            }
        
            AdjustmentType adjustmentType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,transaction.getAdjustmentType());
            
            List<Class<? extends NotificationTemplate>> preferredNotificationTypes = NotificationSupportHelper.get(ctx).getPreferredNotificationTypes(preferredMethod);
            
            Collection<NotificationTemplate> templates = NotificationSupportHelper.get(subCtx).getTemplates(
                    subCtx, 
                    accountCategoryId, language, 
                    NotificationTypeEnum.TFA_NOTIFICATION,
                    preferredNotificationTypes.toArray(new Class[]{}));
            subCtx.put(Subscriber.class, sub);
            TFAParticipantEnum participant = TFAParticipantEnum.BOTH;
            if (templates != null)
            {
                  Iterator<NotificationTemplate> iter = templates.iterator();
                  while (iter.hasNext())
                  {
                      NotificationTemplate template = iter.next();
                      Collection<NotificationTemplate> templates_ = new ArrayList<NotificationTemplate>();
                      templates_.add(template);
                      if (template instanceof TFAEmailNotificationTemplate )
                      {
                          TFAEmailNotificationTemplate emailTemplate = (TFAEmailNotificationTemplate) template;
                          participant = emailTemplate.getNotifyTo();
                      }
                      if (template instanceof TFASMSNotificationTemplate )
                      {
                          TFASMSNotificationTemplate smsTemplate = (TFASMSNotificationTemplate) template;
                          participant = smsTemplate.getNotifyTo();
                      }
                      subCtx.put("TRANSACTION_AMOUNT", transAmount);
                      subCtx.put("NEW_BALANCE", transNewBalance);
                      
                      NotificationTypeEnum notificationType = NotificationTypeEnum.TFA_NOTIFICATION;
                      if (sub != null)
                      {
                          ScheduledTaskNotificationLiaison.setCleanupKey(subCtx, sub.getId() + notificationType);
                      }
                      if (AdjustmentTypeActionEnum.CREDIT.equals(adjustmentType.getAction()) && !TFAParticipantEnum.CONTRIBUTOR.equals(participant))
                      {
                          subCtx.put("BALANCE_STATUS", AdjustmentTypeActionEnum.CREDIT.getDescription());
                          sendNotifications(subCtx, emailAddress, smsNumber, null, notificationType,templates_, new KeyValueFeatureEnum[]{});
                          ERLogger.createTFANotifiactionEr(subCtx, sub, String.valueOf(transaction.getAmount()),AdjustmentTypeActionEnum.CREDIT.getDescription(),preferredMethod.getDescription(),NotificationTypeEnum.TFA_NOTIFICATION.getDescription(),RESULT_SUCCESS );
                  	    
                      }
                      else if (AdjustmentTypeActionEnum.DEBIT.equals(adjustmentType.getAction())&& !TFAParticipantEnum.RECEIVER.equals(participant))
                      {
                          subCtx.put("BALANCE_STATUS", AdjustmentTypeActionEnum.DEBIT.getDescription());
                          sendNotifications(subCtx, emailAddress, smsNumber, null, notificationType,templates_, new KeyValueFeatureEnum[]{});
                  		  ERLogger.createTFANotifiactionEr(subCtx, sub, String.valueOf(transaction.getAmount()),AdjustmentTypeActionEnum.DEBIT.getDescription(),preferredMethod.getDescription(),NotificationTypeEnum.TFA_NOTIFICATION.getDescription(),RESULT_SUCCESS );
                      }
                  }
            }
        }
        catch (HomeException exception)
        {
            new MinorLogMsg(SubscriptionNotificationSupport.class,
                    "Error sending voucher topup notification for subscription ID "
                            + sub.getId() + " of amount "
                            + transaction.getAmount(), exception).log(ctx);
        }
    }
    
    
    public static void sendExternalTransactionNotification(Context ctx, Subscriber sub, long amount)
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
                            SubscriptionNotificationSupport.class, "Error sending External Transaction Notification, Subscriber not found.", null).log(ctx);
                }
                return;
            }
            
            CRMSpid spid = SpidSupport.getCRMSpid(ctx, sub.getSpid());

            if (!spid.isEnablePackageNotification())
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(
                            SubscriptionNotificationSupport.class,
                            "External Transaction Notification is not enabled for SPID = " + spid.getSpid(), null).log(ctx);
                }
                return;
            }
            String currency = spid.getCurrency();
            String transAmount = CurrencyPrecisionSupportHelper.get(ctx).formatDisplayCurrencyValue(ctx,
                    currency, Math.abs(amount));
            
            preferredMethod = getNotificationMethod(subCtx, sub);
            smsNumber = sub.getMsisdn();
            
            Account account = sub.getAccount(subCtx);
            if (account != null)
            {
                subCtx.put(Account.class, account);
                
                accountCategoryId = account.getType();
                //language = account.getLanguage();
                language = sub.getBillingLanguage();
                emailAddress = account.getEmailID();
            }
            
            List<Class<? extends NotificationTemplate>> preferredNotificationTypes = NotificationSupportHelper.get(ctx).getPreferredNotificationTypes(preferredMethod);
            
            Collection<NotificationTemplate> templates = NotificationSupportHelper.get(subCtx).getTemplates(
                    subCtx, 
                    accountCategoryId, language, 
                    NotificationTypeEnum.EXT_NOTIFICATION,
                    preferredNotificationTypes.toArray(new Class[]{}));
            
            subCtx.put("TRANSACTION_AMOUNT", transAmount);
            subCtx.put(Subscriber.class, sub);

            NotificationTypeEnum notificationType = NotificationTypeEnum.EXT_NOTIFICATION;
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
            new MinorLogMsg(SubscriptionNotificationSupport.class,
                    "Error sending External Transaction notification for subscription ID "
                            + sub.getId() + " of amount "
                            + amount, exception).log(ctx);
        }
    }
    
    
    /**
     * Sends Service Expiry notification.
     * 
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The recipient subscriber.
     * @param SubscriberServices
     *            The SubscriberServices of the subService.
     * @param SubscriberAuxiliaryService
     *            The SubscriberAuxiliaryService of the subAuxService.
     * @param fee
     *            Service/Auxiliary Service Fees.
     */
    public static void sendRecurrenceServiceNotification(Context ctx, Subscriber sub, Service service, AuxiliaryService auxService, Date endDate, String fee)
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
                            SubscriptionNotificationSupport.class, "Error sending Service Expiry notification, Subscriber not found.", null).log(ctx);
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
                language = sub.getBillingLanguage();
                emailAddress = account.getEmailID();
            }
        
            
            List<Class<? extends NotificationTemplate>> preferredNotificationTypes = NotificationSupportHelper.get(ctx).getPreferredNotificationTypes(preferredMethod);
            
            Collection<NotificationTemplate> templates = NotificationSupportHelper.get(subCtx).getTemplates(
                    subCtx, 
                    accountCategoryId, language, 
                    NotificationTypeEnum.SERVICE_RECURRENCE_NOTIFICATION,
                    preferredNotificationTypes.toArray(new Class[]{}));
            
            if(service != null)
            {
                subCtx.put(Service.class, service);
                subCtx.put("SERVICE_MESSAGE", " Service "+ service.getName());
                
            }
            if(auxService != null)
            {
                subCtx.put(AuxiliaryService.class, auxService);
                subCtx.put("SERVICE_MESSAGE", " Auxiliary service "+ auxService.getName());
            }
            subCtx.put("SUB_SERVICE_RECURRENCE_DATETIME", endDate);
            subCtx.put("SERVICE_FEES", fee);
            subCtx.put(Subscriber.class, sub);
            

            NotificationTypeEnum notificationType = NotificationTypeEnum.SERVICE_RECURRENCE_NOTIFICATION;
            if (sub != null)
            {
                ScheduledTaskNotificationLiaison.setCleanupKey(subCtx, sub.getId() + notificationType);
            }
            sendNotifications(subCtx, 
                    emailAddress, smsNumber, null,
                    notificationType,
                    templates,
                    new KeyValueFeatureEnum[]{});
            ERLogger.createPreExpiryPreRecurrenceEr(subCtx, fee, endDate, sub, service, auxService,preferredMethod.getDescription(),NotificationTypeEnum.SERVICE_RECURRENCE_NOTIFICATION.getDescription(),RESULT_SUCCESS);
        }
        catch (HomeException exception)
        {
            new MinorLogMsg(SubscriptionNotificationSupport.class,
                    "Error sending Service Recurrence notification for subscription ID "
                            + sub.getId() + " of amount: "+ fee , exception).log(ctx);
        }
    }
    
    /**
     * Sends Service Expiry notification.
     * 
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The recipient subscriber.
     * @param SubscriberServices
     *            The SubscriberServices of the subService.
     * @param SubscriberAuxiliaryService
     *            The SubscriberAuxiliaryService of the subAuxService.
     */
    public static void sendServiceExpiryNotification(Context ctx, Subscriber sub, Service service, AuxiliaryService auxService, Date endDate)
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
                            SubscriptionNotificationSupport.class, "Error sending Service Expiry notification, Subscriber not found.", null).log(ctx);
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
                language = sub.getBillingLanguage();
                emailAddress = account.getEmailID();
            }
        
            
            List<Class<? extends NotificationTemplate>> preferredNotificationTypes = NotificationSupportHelper.get(ctx).getPreferredNotificationTypes(preferredMethod);
            
            Collection<NotificationTemplate> templates = NotificationSupportHelper.get(subCtx).getTemplates(
                    subCtx, 
                    accountCategoryId, language, 
                    NotificationTypeEnum.SERVICE_EXPIRY_NOTIFICATION,
                    preferredNotificationTypes.toArray(new Class[]{}));
            
            if(service != null)
            {
                subCtx.put(Service.class, service);
                subCtx.put("SERVICE_MESSAGE", " service "+ service.getName());
            }
            if(auxService != null)
            {
                subCtx.put(AuxiliaryService.class, auxService);
                subCtx.put("SERVICE_MESSAGE", " Auxiliary service "+ auxService.getName());
            }
            subCtx.put("SUB_SERVICE_EXPIRY_DATETIME", endDate);
            subCtx.put(Subscriber.class, sub);
            

            NotificationTypeEnum notificationType = NotificationTypeEnum.SERVICE_EXPIRY_NOTIFICATION;
            if (sub != null)
            {
                ScheduledTaskNotificationLiaison.setCleanupKey(subCtx, sub.getId() + notificationType);
            }
            sendNotifications(subCtx, 
                    emailAddress, smsNumber, null,
                    notificationType,
                    templates,
                    new KeyValueFeatureEnum[]{});
            ERLogger.createPreExpiryPreRecurrenceEr(subCtx, null , endDate, sub, service, auxService,preferredMethod.getDescription(),NotificationTypeEnum.SERVICE_EXPIRY_NOTIFICATION.getDescription(),RESULT_SUCCESS);
        }
        catch (HomeException exception)
        {
            new MinorLogMsg(SubscriptionNotificationSupport.class,
                    "Error sending Service Expiry notification for subscription ID "
                            + sub.getId() + " of amount " , exception).log(ctx);
        }
    }
    
    /**
     * Sends expiry date extension notification Email.
     * 
     * @param ctx
     *            Operating context.
     * @param subscriber
     *            Subscriber.
     * @throws HomeException
     *             Thrown if there are problems sending the message.
     */
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
                   // language = account.getLanguage();
                    language = sub.getBillingLanguage();
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
            new MinorLogMsg(SubscriptionNotificationSupport.class,
                    "Error sending expiry date extension notification email for subscription ID "
                            + sub.getId(), exception).log(ctx);
        }
    }
    
    
    /**
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
     */
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
                   // language = account.getLanguage();
                    language = sub.getBillingLanguage();
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
            new MinorLogMsg(SubscriptionNotificationSupport.class,
                    "Error sending service suspension notification email of subscriber "
                            + sub.getId(), exception).log(ctx);
        }
    }    

    /**
     * Sends an Insufficient Balance Notification to a subscription. Please check for {@link CRMSpid#getRecChrgPrepdInsufBalNotifDaysBefore()}
     * for configuration.
     * 
     * @param ctx
     * @param sub
     * @param items
     * @param billingDate
     * @param chargingCycle
     * @param recurringRecharge
     */
    public static void sendPreWarnInsufficientBalanceNotification(final Context ctx, final Subscriber sub, final Map<Class,List<Object>> items, Date billingDate, 
    		ChargingCycleEnum chargingCycle , RecurringRecharge recurringRecharge)
    {
    	sendNotification(ctx, sub, items, billingDate, chargingCycle, recurringRecharge, 
    			NotificationTypeEnum.PREPAID_RECURRING_RECHARGE_PREWARNING_INSUFFICIENT_BAL, KeyValueFeatureEnum.RECURRING_RECHARGE_PREWARN_EMAIL_INSUFFICIENT_BALANCE);
    }
    
    /**
     * 
     * Sends a Recurring Recharge Notification.
     * 
     * @param ctx
     * @param sub
     * @param items
     * @param billingDate
     * @param chargingCycle
     * @param recurringRecharge
     */
    public static void sendRecurringRechargeNotification(final Context ctx, final Subscriber sub, final Map<Class,List<Object>> items, Date billingDate, 
    		ChargingCycleEnum chargingCycle , RecurringRecharge recurringRecharge)
    {
    	sendNotification(ctx, sub, items, billingDate, chargingCycle, recurringRecharge, NotificationTypeEnum.PREPAID_RECHARGE, KeyValueFeatureEnum.PREPAID_RECHARGE_EMAIL);
    }
    
    /**
     * 
     * Sends a Recurring Recharge Failure Notification.
     * 
     * @param ctx
     * @param sub
     * @param items
     * @param billingDate
     * @param chargingCycle
     * @param recurringRecharge
     */
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
                   // language = account.getLanguage();
                    language = sub.getBillingLanguage();
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
            new MinorLogMsg(SubscriptionNotificationSupport.class,
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
    			LogSupport.minor(ctx, SubscriptionNotificationSupport.class.getName(), "Could not set service name");
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
    			LogSupport.minor(ctx, SubscriptionNotificationSupport.class.getName(), "Could not associate service name from Map.");
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

    /**
     * Sends pre-expiry notification.
     * 
     * @param ctx
     *            Operating context.
     * @param subscriber
     *            Subscriber.
     * @throws HomeException
     *             Thrown if there are problems sending the message.
     */
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
                    //language = account.getLanguage();
                    language = sub.getBillingLanguage();
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
            new MinorLogMsg(SubscriptionNotificationSupport.class,
                    "Error sending expiry date extension notification email for subscription ID "
                            + (sub != null ? sub.getId() : null), exception).log(ctx);
        }
    }
    
    public static void removePendingNotifications(Context ctx, String subId, NotificationTypeEnum type)
    {
        NotificationSupportHelper.get(ctx).removePendingNotifications(ctx, subId + type);
    }

    /**
     * Send the priceplan option removal notification .
     * 
     * @param ctx
     *            The operating context.
     * @param sub
     *            Subscriber to be notified.
     * @param service
     *            Service removed.
     * @param bundle
     *            Bundle removed.
     */
    public static void sendPricePlanOptionRemovalNotification(final Context ctx,
            final Subscriber sub,final ServiceFee2 serviceFee,
            final BundleFee bundleFee)
    {
        Context subCtx = ctx.createSubContext();
        long accountCategoryId = -1;
        String language = "";
        NotificationMethodEnum preferredMethod = null;
        String emailAddress = null;
        String smsNumber = null;
        if (sub == null)
        {
            LogSupport.info(subCtx, MODULE, "No subscriber is provided as input. Returning... ");
            return;
        }
        try
        {
            preferredMethod = getNotificationMethod(subCtx, sub);
            smsNumber = sub.getMsisdn();
            Account account = sub.getAccount(subCtx);
            if (account == null)
            {
                LogSupport.info(subCtx, MODULE, "No Account found for subscriber BAN :" + sub.getBAN()
                        + ". Returning... ");
                return;
            }
            subCtx.put(Account.class, account);
            accountCategoryId = account.getType();
            language = sub.getBillingLanguage();
            emailAddress = account.getEmailID();
            List<Class<? extends NotificationTemplate>> preferredNotificationTypes = NotificationSupportHelper.get(ctx)
                    .getPreferredNotificationTypes(preferredMethod);
            Collection<NotificationTemplate> templates = NotificationSupportHelper.get(subCtx).getTemplates(subCtx,
                    accountCategoryId, language, NotificationTypeEnum.PRICEPLAN_OPTION_REMOVAL_NOTIFICATION,
                    preferredNotificationTypes.toArray(new Class[]{}));
            
            subCtx.put(Subscriber.class, sub);
            subCtx.put(com.redknee.app.crm.bean.ServiceFee2.class, serviceFee);
            subCtx.put(com.redknee.app.crm.bundle.BundleFee.class, bundleFee);
            try
            {
                if(bundleFee != null)
                {
                    subCtx.put(BundleProfile.class, bundleFee.getBundleProfile(subCtx));
                }
                if(serviceFee != null)
                {
                    subCtx.put(Service.class, serviceFee.getService(subCtx));
                }
            }
            catch (Exception e)
            {
                LogSupport.minor(subCtx, MODULE, "Error while fetching BundleProfile/Service");
            }
            NotificationTypeEnum notificationType = NotificationTypeEnum.PRICEPLAN_OPTION_REMOVAL_NOTIFICATION;
            ScheduledTaskNotificationLiaison.setCleanupKey(subCtx, sub.getId() + notificationType);
            sendNotifications(subCtx, emailAddress, smsNumber, null, notificationType, templates,
                    KeyValueFeatureEnum.PRICEPLAN_OPTION_REMOVAL_NOTIFICATION);
        }
        catch (HomeException exception)
        {
            LogSupport.minor(ctx, SubscriptionNotificationSupport.class,
                    "Error sending priceplan option removal notification of subscriber " + sub.getId());
        }
    }    

    public static void sendServiceStateChangeNotification(Context ctx,Subscriber oldSub,Subscriber newSub,Service service, AuxiliaryService auxService, NotificationTypeEnum notiType, ServiceStateEnum state){

        try
        {
        	CRMSpid spid = null;
        	if(newSub != null)
        	{
        		spid = SpidSupport.getCRMSpid(ctx, newSub.getSpid());
        	}
        	else if(oldSub != null)
        	{
        		spid = SpidSupport.getCRMSpid(ctx, oldSub.getSpid());
        	}
            
            if (!spid.isEnablePackageNotification())
            {
                return;
            }
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
            if (lookupSub != null)
            {
                preferredMethod = getNotificationMethod(subCtx, lookupSub);
                smsNumber = lookupSub.getMsisdn();
                
                Account account = lookupSub.getAccount(subCtx);
                if (account != null)
                {
                    subCtx.put(Account.class, account);
                    
                    accountCategoryId = account.getType();
                    //language = account.getLanguage();
                    language = lookupSub.getBillingLanguage();
                    emailAddress = account.getEmailID();
                }
            }
            
            Collection<NotificationTemplate> templates = getServiceStateChangeNotificationTemplates(
                    subCtx, 
                    accountCategoryId, language, 
                    oldSub, newSub, preferredMethod,notiType);

            subCtx.put(Subscriber.class, newSub);
            subCtx.put(Lookup.OLDSUBSCRIBER, oldSub);
            if(service != null)
            {
                subCtx.put(Service.class, service);
                subCtx.put("SERVICE_MESSAGE", " Service "+ service.getName());
              
            }
            if(auxService != null)
            {
                subCtx.put(AuxiliaryService.class, auxService);
                subCtx.put("SERVICE_MESSAGE", " Auxiliary service "+ auxService.getName());
            }
            subCtx.put("STATUS", state.getDescription(subCtx));
            NotificationTypeEnum notificationType = NotificationTypeEnum.STATE_CHANGE;
            if (newSub != null)
            {
                ScheduledTaskNotificationLiaison.setCleanupKey(subCtx, newSub.getId() + notificationType);
            }
            sendNotifications(subCtx, 
                    emailAddress, smsNumber, null,
                    notificationType,
                    templates,
                    KeyValueFeatureEnum.STATE_CHANGE_EMAIL);
            ERLogger.createServiceStateNotifiactionEr(subCtx, lookupSub, service, auxService,preferredMethod.getDescription(),RESULT_SUCCESS, state);
        }
        catch (HomeException exception)
        {
            new MinorLogMsg(SubscriptionNotificationSupport.class,
                    "Error sending state transition notification of subscriber "
                            + newSub.getId(), exception).log(ctx);
        }
    
    }
    
    private static Collection<NotificationTemplate> getServiceStateChangeNotificationTemplates(Context ctx, 
            long accountCategoryId, String language, 
            StateAware oldStateOwner, StateAware newStateOwner, 
            NotificationMethodEnum preferredMethod, NotificationTypeEnum type) throws HomeException
    {
        List<Class<? extends NotificationTemplate>> preferredTypes = NotificationSupportHelper.get(ctx).getPreferredNotificationTypes(preferredMethod);
        
        Collection<NotificationTemplate> templates = NotificationSupportHelper.get(ctx).getTemplates(ctx, 
                accountCategoryId, language, 
                type, 
                preferredTypes.toArray(new Class[]{}));
        
        if (templates != null)
        {
            Iterator<NotificationTemplate> iter = templates.iterator();
            while (iter.hasNext())
            {
                NotificationTemplate template = iter.next();
                if (template instanceof StateChangeNotificationTemplate)
                {
                    StateChangeNotificationTemplate stateTmpl = (StateChangeNotificationTemplate) template;
                    
                    EnumStateSupport enumStateSupport = EnumStateSupportHelper.get(ctx);

                    // Don't send notifications if state did not change.
                    if (enumStateSupport.stateEquals(oldStateOwner, newStateOwner))
                    {
                        iter.remove();
                        continue;
                    }
                    
                    if (stateTmpl.getPreviousState() == -1
                            && stateTmpl.getNewState() == -1)
                    {
                        // any state <-> any state = valid
                        continue;
                    }
                    
                    // Check the "From Any State" conditions 
                    if (stateTmpl.getPreviousState() == -1)
                    {
                    	if (oldStateOwner == null)
                        {
                            if (!enumStateSupport.stateEquals(newStateOwner, stateTmpl.getNewState()))
                            {
                            	 // This is a new object creation not satisfying criteria
                                iter.remove();
                                continue;
                            }
                        }
                        else if (newStateOwner != null
                                    && !enumStateSupport.isEnteringState(oldStateOwner, newStateOwner, stateTmpl.getNewState()))
                        {
                        	// State change to a state not satisfying criteria
                            iter.remove();
                            continue;
                        }
                    }

                    // Check the "To Any State" conditions
                    if (stateTmpl.getNewState() == -1)
                    {
                        if (newStateOwner == null)
                        {
                            if (!enumStateSupport.stateEquals(oldStateOwner, stateTmpl.getPreviousState()))
                            {
                                // This is an object deletion not satisfying criteria
                                iter.remove();
                                continue;
                            }
                        }
                        else if (oldStateOwner != null
                                && !enumStateSupport.isLeavingState(oldStateOwner, newStateOwner, stateTmpl.getPreviousState()))
                        {
                        	// State change from a state not satisfying criteria
                            iter.remove(); 
                            continue;
                        }
                    }

                    // Check the "State To State" conditions
                    if (!enumStateSupport.isTransition(oldStateOwner, newStateOwner, stateTmpl.getPreviousState(), stateTmpl.getNewState()) &&  stateTmpl.getPreviousState() != -1)
                    {                    	
                        // Remove state change templates that are not applicable to this state change                    	
                        iter.remove(); 
                    	continue;
                    }
                }
            }
        }
        
        return templates;
    }
    
    /**
     * Sends Subscriber PayGo mode Enabled notification.
     * 
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The recipient subscriber.
     */
    public static void sendPayGoModeEnabledNotification(Context ctx, Subscriber sub)
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
                            SubscriptionNotificationSupport.class, "Error sending PayGo Enabled notification, Subscriber not found.", null).log(ctx);
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
                language = sub.getBillingLanguage();
                emailAddress = account.getEmailID();
            }
        
            
            List<Class<? extends NotificationTemplate>> preferredNotificationTypes = NotificationSupportHelper.get(ctx).getPreferredNotificationTypes(preferredMethod);
            
            Collection<NotificationTemplate> templates = NotificationSupportHelper.get(subCtx).getTemplates(
                    subCtx, 
                    accountCategoryId, language, 
                    NotificationTypeEnum.PAYGO_MODE_ENABLED,
                    preferredNotificationTypes.toArray(new Class[]{}));
            
            subCtx.put(Subscriber.class, sub);
            

            NotificationTypeEnum notificationType = NotificationTypeEnum.PAYGO_MODE_ENABLED;
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
            new MinorLogMsg(SubscriptionNotificationSupport.class,
                    "Error sending PayGo Mode Enabled notification for subscription ID "
                            + sub.getId(), exception).log(ctx);
        }
    }
    
    /**
     * Sends Subscriber PayGo mode Enabled notification.
     * 
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The recipient subscriber.
     */
    public static void sendPayGoModeDisabledNotification(Context ctx, Subscriber sub)
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
                            SubscriptionNotificationSupport.class, "Error sending PayGo Disabled notification, Subscriber not found.", null).log(ctx);
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
                language = sub.getBillingLanguage();
                emailAddress = account.getEmailID();
            }
        
            
            List<Class<? extends NotificationTemplate>> preferredNotificationTypes = NotificationSupportHelper.get(ctx).getPreferredNotificationTypes(preferredMethod);
            
            Collection<NotificationTemplate> templates = NotificationSupportHelper.get(subCtx).getTemplates(
                    subCtx, 
                    accountCategoryId, language, 
                    NotificationTypeEnum.PAYGO_MODE_DISABLED,
                    preferredNotificationTypes.toArray(new Class[]{}));
            
            subCtx.put(Subscriber.class, sub);
            

            NotificationTypeEnum notificationType = NotificationTypeEnum.PAYGO_MODE_DISABLED;
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
            new MinorLogMsg(SubscriptionNotificationSupport.class,
                    "Error sending PayGo Mode Disabled notification for subscription ID "
                            + sub.getId(), exception).log(ctx);
        }
    }
    /**
     * Send the Voucher failure notification .
     * 
     * @param ctx
     *            The operating context.
     * @param sub
     *            Subscriber to be notified.
     */
    public static void sendVoucherTopupFailureNotification(Context ctx, Subscriber sub)
    {
        try
        {
            Context subCtx = ctx.createSubContext();

            long accountType = -1;
            String language = "";
            NotificationMethodEnum preferredMethod = null;
            String emailAddress = null;
            String smsNumber = null;
            
            if (sub == null)
            {
            	new MinorLogMsg(
            			SubscriptionNotificationSupport.class, "Error sending voucher topup notification, Subscriber not found.", null).log(ctx);
                return;
            }
            
            CRMSpid spid = SpidSupport.getCRMSpid(ctx, sub.getSpid());

            if (!spid.isEnableVoucherTopupNotification())
            {
                new MinorLogMsg(
                        SubscriptionNotificationSupport.class,
                        "Voucher topup notification is not enabled for SPID = " + spid.getSpid(), null).log(ctx);
                return;
            }
            
            preferredMethod = getNotificationMethod(subCtx, sub);
            smsNumber = sub.getMsisdn();
            
            Account account = sub.getAccount(subCtx);
            if (account != null)
            {
                subCtx.put(Account.class, account);
                
                accountType = account.getType();
                //language = account.getLanguage();
                language = sub.getBillingLanguage();
                emailAddress = account.getEmailID();
            }
        
            
            List<Class<? extends NotificationTemplate>> preferredNotificationTypes = NotificationSupportHelper.get(ctx).getPreferredNotificationTypes(preferredMethod);
            
            Collection<NotificationTemplate> templates = NotificationSupportHelper.get(subCtx).getTemplates(
                    subCtx, 
                    accountType, language, 
                    NotificationTypeEnum.VOUCHER_TOPUP_FAILURE,
                    preferredNotificationTypes.toArray(new Class[]{}));
            
            subCtx.put(Subscriber.class, sub);

            if (sub != null)
            {
                ScheduledTaskNotificationLiaison.setCleanupKey(subCtx, sub.getId() + NotificationTypeEnum.VOUCHER_TOPUP_FAILURE);
            }
            sendNotifications(subCtx, 
                    emailAddress, smsNumber, null,
                    NotificationTypeEnum.VOUCHER_TOPUP_FAILURE,
                    templates,
                    new KeyValueFeatureEnum[]{});
            //Sending dummy call with dummy data. will change once voucher failure notification implementation is done.
           // ERLogger.createVoucherNotifiactionEr(subCtx, sub.getMsisdn(), null, null, sub,  RESULT_SUCCESS);
        }
        catch (HomeException exception)
        {
            new MinorLogMsg(SubscriptionNotificationSupport.class,
                    "Error sending voucher topup notification for subscription ID "
                            + sub.getId() ).log(ctx);
        }
    }

}
