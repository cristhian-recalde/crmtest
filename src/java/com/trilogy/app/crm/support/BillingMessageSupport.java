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
package com.trilogy.app.crm.support;

import java.text.MessageFormat;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.BillCycleBillingMessage;
import com.trilogy.app.crm.bean.BillCycleBillingMessageID;
import com.trilogy.app.crm.bean.BillingMessage;
import com.trilogy.app.crm.bean.BillingMessagePreferenceEnum;
import com.trilogy.app.crm.bean.CreditCategoryBillingMessage;
import com.trilogy.app.crm.bean.CreditCategoryBillingMessageID;
import com.trilogy.app.crm.bean.PricePlanBillingMessage;
import com.trilogy.app.crm.bean.PricePlanBillingMessageID;
import com.trilogy.app.crm.bean.SpidBillingMessage;
import com.trilogy.app.crm.bean.SpidBillingMessageID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.billing.message.BillingMessageHomePipelineFactory;
import com.trilogy.app.crm.support.messages.MessageConfigurationSupport;
import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * This class has general methods to retrieve a Billing Message from a given bean.
 * @author jke
 * 
 * This is not the support class for the bean "com.redknee.app.crm.bean.BillingMessage".  
 * That class is called MultiLangBillingMessageSupport.
 * 
 * Since CRM 8.2, the Billing Message feature has been changed to support multiple languages.
 * TODO: the Account Type biling message still doesn't support multiple languages.
 * @author angie.li@redknee.com
 */
public class BillingMessageSupport
{
    final private static String billingMsg = "The billing message will be \"{0}\", taken from {1} billing message.";

    public static String getUIBillingMessage(final Context context, final Account account)
    {
        return getBillingMessage(context, account, true);
    }

    public static String getBillingMessage(final Context context, final Account account)
    {
        return getBillingMessage(context, account, false);
    }

    private static String getBillingMessage(final Context context, final Account account, boolean formatted)
    {
        try
        {
            String message;

            message = getAccountBillingMessage(context, account, formatted);

            if (message != null)
                return  message;
            /* 
            if (account.isIndividual(context))
            {
                message = getPricePlanBillingMessage(context, account, formatted);
                 if (message != null && message.trim().length() > 0)
                    return message;
            }
            message = getAccountTypeBillingMessage(context, account, formatted);
            if (message != null && message.trim().length() > 0)
                return message;
            message = getCreditCategoryBillingMessage(context, account, formatted);
            if (message != null && message.trim().length() > 0)
                return message;
            message = getBillingCycleBillingMessage(context, account, formatted);
             if (message != null && message.trim().length() > 0)
                return message;
            message = getSpidBillingMessage(context, account, formatted);
            if (message != null && message.trim().length() > 0){
                return message;
            }
             */
        }
        catch (Exception ex)
        {
            new MinorLogMsg("BillingMessageSupport", "Fail to get billing message - account: " + account.getBAN(), ex).log(context);
        }
        return "";
    }


    public static String formatMessage(String message, String value0, String value1)
    {
        String formattedMessage = MessageFormat.format(message, new Object[]{value0, value1});
        return formattedMessage;
    }


    public static String getSpidBillingMessage(final Context context, final Account account, boolean formatted) throws HomeException
    {
        MessageConfigurationSupport<SpidBillingMessage, SpidBillingMessageID> support = 
            (MessageConfigurationSupport<SpidBillingMessage, SpidBillingMessageID>) context.get(SpidBillingMessage.class.getName() + BillingMessageHomePipelineFactory.BILLING_MESSAGE_CONFIGURATION_SUPPORT_SUFIX);
        SpidBillingMessageID messageID = support.getMessageID(account.getSpid(), account.getLanguage());
        
        SpidBillingMessage bean = new SpidBillingMessage();
        if (messageID!=null)
        {
            messageID.set(bean);
        }
        bean.setIdentifier(Integer.valueOf(account.getSpid()).longValue());
        messageID.get(bean);
        BillingMessage config = support.getMessageConfiguration(context, messageID);
        
        String billingMessage = "";
        if (config != null)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(BillingMessageSupport.class, 
                        "Spid's Billing Message is configured for the Account's language. Use this message for Account=" 
                        + account.getBAN(), null);
            }
            billingMessage = config.getMessage();
        }

        if(formatted && billingMessage.trim().length() > 0)
            return formatMessage(billingMsg, billingMessage, "Spid " + account.getSpid());
        else 
            return billingMessage;

    }


    public static String getBillingCycleBillingMessage(final Context context, final Account account, boolean formatted)
    throws HomeException
    {
        MessageConfigurationSupport<BillCycleBillingMessage, BillCycleBillingMessageID> support = 
            (MessageConfigurationSupport<BillCycleBillingMessage, BillCycleBillingMessageID>) context.get(BillCycleBillingMessage.class.getName() + BillingMessageHomePipelineFactory.BILLING_MESSAGE_CONFIGURATION_SUPPORT_SUFIX);
        BillCycleBillingMessageID messageID = support.getMessageID(account.getSpid(), account.getLanguage());

        BillCycleBillingMessage bean = new BillCycleBillingMessage();
        if (messageID!=null)
        {
            messageID.set(bean);
        }
        bean.setIdentifier(Integer.valueOf(account.getBillCycleID()).longValue());
        messageID.get(bean);        

        BillingMessage config = support.getMessageConfiguration(context, messageID);

        String billingMessage = "";
        if (config != null)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(BillingMessageSupport.class, 
                        "Bill Cycle's Billing Message is configured for the Account's language. Use this message for Account=" 
                        + account.getBAN(), null);
            }
            billingMessage = config.getMessage();
        }

        if(formatted && billingMessage.trim().length() > 0)
            return formatMessage(billingMsg, billingMessage, "Bill Cycle");
        else 
            return billingMessage;
    }


    /**
     * Gets the Billing Message used by the given Credit Category.
     * 
     * @param context:
     *            The operating context.
     * @param id:
     *            The identifier(code) of the Credit Category
     * @return The Billing Message.
     * @exception HomeException
     *                Thrown if there is a problem accessing Home data in the context.
     */
    public static String getCreditCategoryBillingMessage(final Context context, final Account account, boolean formatted)
    throws HomeException
    {
        MessageConfigurationSupport<CreditCategoryBillingMessage, CreditCategoryBillingMessageID> support = 
            (MessageConfigurationSupport<CreditCategoryBillingMessage, CreditCategoryBillingMessageID>) context.get(CreditCategoryBillingMessage.class.getName() + BillingMessageHomePipelineFactory.BILLING_MESSAGE_CONFIGURATION_SUPPORT_SUFIX);
        CreditCategoryBillingMessageID messageID = support.getMessageID(account.getSpid(), account.getLanguage());
        
        CreditCategoryBillingMessage bean = new CreditCategoryBillingMessage();
        if (messageID!=null)
        {
            messageID.set(bean);
        }
        bean.setIdentifier(Integer.valueOf(account.getCreditCategory()).longValue());
        messageID.get(bean);

        BillingMessage config = support.getMessageConfiguration(context, messageID);
        String billingMessage = "";
        if (config != null)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(BillingMessageSupport.class, 
                        "Credit Category's Billing Message is configured for the Account's language. Using this message for Account=" 
                        + account.getBAN(), null);
            }
            billingMessage = config.getMessage();
        }

        if(formatted && billingMessage.trim().length() > 0)
            return formatMessage(billingMsg, billingMessage, "Credit Category");
        else
            return billingMessage;

    }


    public static String getAccountTypeBillingMessage(final Context context, final Account account, boolean formatted)
    throws HomeException
    {
		AccountCategory accType = account.getAccountCategory(context);
//        final BillingMessagePreferenceEnum preference = accType.getBillingMsgPreference();
//        if (preference.equals(BillingMessagePreferenceEnum.NONE))
//        return "";
//        else if (preference.equals(BillingMessagePreferenceEnum.DEFAULT))
//        return null;
//        else
//        {
        if(formatted && accType.getBillingMessage().trim().length() > 0)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(BillingMessageSupport.class, 
                        "Account Type's Billing Message is configured for the Account's language. Using this message for Account=" 
                        + account.getBAN(), null);
            }
            return formatMessage(billingMsg, accType.getBillingMessage(), "Account Type");
        }
        else 
        {
            return accType.getBillingMessage();
        }
//        }
    }


    /**
     * Gets the price-plan billing message only if the account has just one subscriber (subscription)
     * @param context
     * @param account
     * @param formatted
     * @return
     * @throws HomeException
     */
    public static String getPricePlanBillingMessage(final Context context, final Account account, boolean formatted) throws HomeException
    {
        Subscriber sub = account.getSubscriber();
        long pricePlanID = sub.getPricePlan();
        if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(BillingMessageSupport.class, 
                    "This account is individual.  Checking the Price Plan's Billing Message. Account=" 
                    + account.getBAN() + " price plan=" + pricePlanID, null);
        }
        
        MessageConfigurationSupport<PricePlanBillingMessage,PricePlanBillingMessageID> support = 
            (MessageConfigurationSupport<PricePlanBillingMessage, PricePlanBillingMessageID>) context
            .get(PricePlanBillingMessage.class.getName()
                    + BillingMessageHomePipelineFactory.BILLING_MESSAGE_CONFIGURATION_SUPPORT_SUFIX);
        PricePlanBillingMessageID messageID = support.getMessageID(account.getSpid(), account.getLanguage());
        PricePlanBillingMessage bean = new PricePlanBillingMessage();
        if (messageID!=null)
        {
            messageID.set(bean);
        }
        bean.setIdentifier(pricePlanID);
        messageID.get(bean);

        BillingMessage config = support.getMessageConfiguration(context, messageID);

        String billingMessage = "";
        if (config != null)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(BillingMessageSupport.class, 
                        "Price Plan's Billing Message is configured for the Account's language. Use this message for Account=" 
                        + account.getBAN(), null);
            }
            billingMessage = config.getMessage();
        }

        if(formatted && billingMessage.trim().length() > 0)
            return formatMessage(billingMsg, billingMessage, "Price Plan");
        else
            return billingMessage;
    }


    public static String getAccountBillingMessage(final Context context, final Account account, boolean formatted) throws HomeException
    {
        final BillingMessagePreferenceEnum preference = account.getBillingMsgPreference();
        if (preference.equals(BillingMessagePreferenceEnum.NONE))
        {
            if (LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(BillingMessageSupport.class, "This account has selected the No Billing Message option.  Account=" + account.getBAN(), null);
            }
            return "";
        }
        else if (preference.equals(BillingMessagePreferenceEnum.DEFAULT))
        {
            Account paAccount = account.getParentAccount(context);
            if (paAccount != null)
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    new DebugLogMsg(BillingMessageSupport.class, 
                            "This account has a parent account.  Checking the Billing Message option of the parent account of account=" + account.getBAN(), null);
                }
                return getAccountBillingMessage(context, paAccount, formatted);
            }
            else
            {
                String message;
                final LicenseMgr lMgr = (LicenseMgr) context.get(LicenseMgr.class);
                if (account.isIndividual(context))
                {
                    message = getPricePlanBillingMessage(context, account, formatted);
                    if (message != null && message.trim().length() > 0)
                        return message;
                }
                message = getAccountTypeBillingMessage(context, account, formatted);
                if (message != null && message.trim().length() > 0)
                    return message;
                message = getCreditCategoryBillingMessage(context, account, formatted);
                if (message != null && message.trim().length() > 0)
                    return message;
                message = getBillingCycleBillingMessage(context, account, formatted);
                if (message != null && message.trim().length() > 0)
                    return message;
                message = getSpidBillingMessage(context, account, formatted);
                if (message != null && message.trim().length() > 0)
                {
                    return message;
                }
                return "";     
            }
        }
        else
        {
            if (LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(BillingMessageSupport.class, 
                        "This account has chosen a customized Billing Message.  See profile of account=" 
                        + account.getBAN(), null);
            }
            if(formatted)
                return formatMessage(billingMsg, account.getBillingMessage(), "Account " + account.getBAN());
            else
                return account.getBillingMessage();

        }
    }
}
