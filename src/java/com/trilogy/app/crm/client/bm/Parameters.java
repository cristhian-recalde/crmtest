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
package com.trilogy.app.crm.client.bm;

import static com.redknee.product.bundle.manager.provision.v5_0.profile.param.ParameterID.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.trilogy.product.bundle.manager.provision.common.param.Parameter;
import com.trilogy.product.bundle.manager.provision.common.param.ParameterValue;
import com.trilogy.product.bundle.manager.provision.common.param.ParameterValueType;

/**
 * Provides a parameter reader and builder to make higher-level client code
 * easier to read and write, more type safe, etc. The reader methods follow
 * standard JavaBeans conventions for the "get" methods. The builder methods use
 * a fluent style of interface, allowing parameter specifications to be chained
 * together.
 * <p>
 * <strong>Example Reader Usage</strong>
 *
 * <pre>
 * final Parameters values = querySubscriberProfile(ctx, subscription);
 *
 * final long balance = values.balance();
 * </pre>
 *
 * <strong>Example Builder Usage</strong>
 *
 * <pre>
 * final Parameter[] parameters = new Parameters().spid(5).msisdn(&quot;9056252272&quot;).end();
 * </pre>
 *
 * @author gary.anderson@redknee.com
 */
public class Parameters
{
    /**
     * The value used to specify that expiry date is unspecified.
     */
    public static final long NO_DATE = -1L;

    
    /**
     * Creates a new Parameters.
     */
    public Parameters()
    {
        parameters_ = new HashMap<Short, Parameter>();
    }


    /**
     * Creates a new Parameters with some initial values.
     *
     * @param initialValues The initial values of the parameters.
     */
    Parameters(final Parameter[] initialValues)
    {
        this();

        for (final Parameter parameter : initialValues)
        {
            parameters_.put(parameter.parameterID, parameter);
        }
    }

    short FIELD_LEVEL_ALLOWMULTISUB = 1000;
    public Parameters allowMultiSub(final boolean allowMultiSubWithASubscriptionType)
    {
        parameters_.put(FIELD_LEVEL_ALLOWMULTISUB,
             parameter(FIELD_LEVEL_ALLOWMULTISUB, allowMultiSubWithASubscriptionType));
        return this;
    }

    /**
     * Specifies the activation extension.
     *
     * @param activationExtension The activation extension.
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters activationExtension(final int activationExtension)
    {
        parameters_.put(FIELD_LEVEL_ACTIVATIONEXTENSION,
            parameter(FIELD_LEVEL_ACTIVATIONEXTENSION, activationExtension));
        return this;
    }


    /**
     * Gets the specified activation extension.
     *
     * @return The activation extension.
     */
    public int getActivationExtension()
    {
        final Parameter parameter = getParameter(FIELD_LEVEL_ACTIVATIONEXTENSION, "No activation extension specified.");
        return parameter.value.intValue();
    }


    /**
     * Specifies the balance.
     *
     * @param balance The balance.
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters balance(final long balance)
    {
        parameters_.put(FIELD_LEVEL_BALANCE, parameter(FIELD_LEVEL_BALANCE, balance));
        return this;
    }


    /**
     * Gets the specified balance.
     *
     * @return The specified balance.
     */
    public long getBalance()
    {
        final Parameter parameter = getParameter(FIELD_LEVEL_BALANCE, "No balance specified.");
        return parameter.value.longValue();
    }


    /**
     * Specifies the bill cycle day.
     *
     * @param day Day of the month [1-28].
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters billCycleDay(final int day)
    {
        parameters_.put(IN_BILL_CYCLE_DATE, parameter(IN_BILL_CYCLE_DATE, day));
        return this;
    }


    /**
     * Gets the specified bill cycle day.
     *
     * @return The day of the month [1-28].
     */
    public int getBillCycleDay()
    {
        final Parameter parameter = getParameter(IN_BILL_CYCLE_DATE, "No bill cycle day specified.");
        return parameter.value.intValue();
    }


    /**
     * Specifies the birthday.
     *
     * @param date birthday date.
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters birthDay(final Date date)
    {
        final long dateAsLong;

        if (date != null)
        {
            dateAsLong = date.getTime();
        }
        else
        {
            dateAsLong = NO_DATE;
        }

        parameters_.put(SUB_FIELD_LEVEL_BIRTHDATE, parameter(SUB_FIELD_LEVEL_BIRTHDATE, dateAsLong));
        return this;
    }


    /**
     * Gets the specified birth day Date. Null if birthday is not specified.
     *
     * @return birthday Date or null.
     */
    public Date getBirthDay()
    {
        final Parameter parameter = getParameter(SUB_FIELD_LEVEL_BIRTHDATE, "No birthday specified.");

        final long dateAsLong = parameter.value.longValue();
        if (dateAsLong == NO_DATE)
        {
            return null;
        }

        final Date birthDay = new Date(dateAsLong);

        return birthDay;
    }


    /**
     * Specifies the credit limit.
     *
     * @param creditLimit The credit limit.
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters creditLimit(final long creditLimit)
    {
        parameters_.put(FIELD_LEVEL_CREDITLIMIT, parameter(FIELD_LEVEL_CREDITLIMIT, creditLimit));
        return this;
    }


    /**
     * Gets the specified credit limit.
     *
     * @return The credit limit.
     */
    public long getCreditLimit()
    {
        final Parameter parameter = getParameter(FIELD_LEVEL_CREDITLIMIT, "No credit limit specified.");
        return parameter.value.longValue();
    }


    /**
     * Specifies the currency.
     *
     * @param currency The currency.
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters currency(final String currency)
    {
        parameters_.put(FIELD_LEVEL_CURRENCY, parameter(FIELD_LEVEL_CURRENCY, currency));
        return this;
    }


    /**
     * Gets the specified currency.
     *
     * @return The currency.
     */
    public String getCurrency()
    {
        final Parameter parameter = getParameter(FIELD_LEVEL_CURRENCY, "No currency specified.");
        return parameter.value.stringValue();
    }


    /**
     * Specifies whether or not notification is enabled.
     *
     * @param enabled True if notification is enabled; false otherwise.
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters enableNotification(final boolean enabled)
    {
        parameters_.put(FIELD_LEVEL_ENABLENOTIFICATION, parameter(FIELD_LEVEL_ENABLENOTIFICATION, enabled));
        return this;
    }


    /**
     * Indicates whether or not notification is enabled.
     *
     * @return True if notification is enabled; false otherwise.
     */
    public boolean getEnableNotification()
    {
        final Parameter parameter = getParameter(FIELD_LEVEL_ENABLENOTIFICATION, "Enable notification not specified.");
        return parameter.value.booleanValue();
    }


    /**
     * Specifies the expiration date.
     *
     * @param date The expiration date.
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters expiryDate(final Date date)
    {
        final long milliseconds;

        if (date != null)
        {
            milliseconds = date.getTime();
        }
        else
        {
            milliseconds = NO_DATE;
        }

        parameters_.put(FIELD_LEVEL_EXPIRYDATE, parameter(FIELD_LEVEL_EXPIRYDATE, milliseconds));
        return this;

    }
    
    
    /**
     * Specifies the activation date.
     *
     * @param date The activation date.
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters creationDate(final Date date)
    {
        final long milliseconds;

        if (date != null)
        {
            milliseconds = date.getTime();
        }
        else
        {
            milliseconds = NO_DATE;
        }

        parameters_.put(SUB_FIELD_LEVEL_CREATIONDATE, parameter(SUB_FIELD_LEVEL_CREATIONDATE, milliseconds));
        return this;

    }

    /**
     * Specifies the activation date.
     *
     * @param date The activation date.
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters activationDate(final Date date)
    {
        final long milliseconds;

        if (date != null)
        {
            milliseconds = date.getTime();
        }
        else
        {
            milliseconds = NO_DATE;
        }

        parameters_.put(SUP_FIELD_LEVEL_ACTIVATIONDATE, parameter(SUP_FIELD_LEVEL_ACTIVATIONDATE, milliseconds));
        return this;

    }

    /**
     * Gets the specified expiry date.
     * 
     * @return The expiry date if set; null if specified but unset.
     */
    public Date getExpiryDate()
    {
        // NOTE: I would have preferred to specialize on NO_DATE to
        // return null, to be explicit that there is no expiry date, but I found
        // several references in the existing code that assume this to be
        // non-null. I wouldn't be able to make this change without many changes
        // and lots of testing. For now, we'll always return a valid Date
        // object.

        final Parameter parameter = getParameter(FIELD_LEVEL_EXPIRYDATE, "No expiry date specified.");

        final long milliseconds = parameter.value.longValue();
        final Date expiryDate = new Date(milliseconds);

        return expiryDate;
    }

    
    /**
     * Gets the specified creation date.
     * 
     * @return The creation date if set; null if specified but unset.
     */
    public Date getCreationDate()
    {

        final Parameter parameter = getParameter(SUB_FIELD_LEVEL_CREATIONDATE, "No creation date specified.");

        final long milliseconds = parameter.value.longValue();
        final Date creationDate = new Date(milliseconds);

        return creationDate;
    }

    /**
     * Gets the specified activation date.
     * 
     * @return The activation date if set; null if specified but unset.
     */
    public Date getActivationDate()
    {

        final Parameter parameter = getParameter(SUP_FIELD_LEVEL_ACTIVATIONDATE, "No activation date specified.");

        final long milliseconds = parameter.value.longValue();
        final Date activationDate = new Date(milliseconds);

        return activationDate;
    }

    /**
     * Specifies the group quota.
     *
     * @param groupQuota The group quota.
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters groupQuota(final long groupQuota)
    {
        parameters_.put(FIELD_LEVEL_GROUPQUOTA, parameter(FIELD_LEVEL_GROUPQUOTA, groupQuota));
        return this;
    }


    /**
     * Gets the specified group quota.
     *
     * @return The group quota.
     */
    public long getGroupQuota()
    {
        final Parameter parameter = getParameter(FIELD_LEVEL_GROUPQUOTA, "No group quota specified.");
        return parameter.value.longValue();
    }

    public long getVoiceUsage()
    {
    	final Parameter parameter = parameters_.get(EVENT_TYPE_FIELD_LEVEL_VOICEUSAGE);
    	return parameter == null? -1: parameter.value.longValue();
    }
    public long getDataUsage()
    {
    	final Parameter parameter = parameters_.get(EVENT_TYPE_FIELD_LEVEL_DATAUSAGE);
    	return parameter == null? -1: parameter.value.longValue();
    }
    public long getMessageUsage()
    {
    	final Parameter parameter = parameters_.get(EVENT_TYPE_FIELD_LEVEL_MESSAGEUSAGE);
    	return parameter == null? -1: parameter.value.longValue();
    }
    public long getVoiceCap()
    {
    	final Parameter parameter = parameters_.get(EVENT_TYPE_FIELD_LEVEL_VOICEUSAGE_CAP);
    	return parameter == null? -1: parameter.value.longValue();
    }
    public long getDataCap()
    {
    	final Parameter parameter = parameters_.get(EVENT_TYPE_FIELD_LEVEL_DATAUSAGE_CAP);
    	return parameter == null? -1: parameter.value.longValue();
    }
    public long getMessageCap()
    {
    	final Parameter parameter = parameters_.get(EVENT_TYPE_FIELD_LEVEL_MESSAGEUSAGE_CAP);
    	return parameter == null? -1: parameter.value.longValue();
    }
    /**
     * Specifies the group usage.
     *
     * @param groupUsage The group usage.
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters groupUsage(final long groupUsage)
    {
        parameters_.put(FIELD_LEVEL_GROUPUSAGE, parameter(FIELD_LEVEL_GROUPUSAGE, groupUsage));
        return this;
    }


    /**
     * Gets the specified group usage.
     *
     * @return The group usage.
     */
    public long getGroupUsage()
    {
        final Parameter parameter = getParameter(FIELD_LEVEL_GROUPUSAGE, "No group usage specified.");
        return parameter.value.longValue();
    }


    /**
     * Specifies the mobile number.
     *
     * @param msisdn The mobile number.
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters msisdn(final String msisdn)
    {
        parameters_.put(FIELD_LEVEL_MSISDN, parameter(FIELD_LEVEL_MSISDN, msisdn));
        return this;
    }


    /**
     * Gets the mobile number specified.
     *
     * @return The mobile number.
     */
    public String getMsisdn()
    {
        final Parameter parameter = getParameter(FIELD_LEVEL_MSISDN, "No mobile number specified.");
        return parameter.value.stringValue();
    }


    /**
     * Specifies the overdraft balance limit.
     *
     * @param overdraftBalanceLimit The overdraft balance limit.
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters overdraftBalanceLimit(final long overdraftBalanceLimit)
    {
        parameters_.put(FIELD_LEVEL_OVERDRAFTBALANCELIMIT, parameter(FIELD_LEVEL_OVERDRAFTBALANCELIMIT,
            overdraftBalanceLimit));
        return this;
    }


    /**
     * Gets the specified overdraft balance limit.
     *
     * @return The overdraft balance limit.
     */
    public long getOverdraftBalanceLimit()
    {
        final Parameter parameter =
            getParameter(FIELD_LEVEL_OVERDRAFTBALANCELIMIT, "No overdraft balance limit specified.");
        return parameter.value.longValue();
    }


    /**
     * Specifies the pool group ID.
     *
     * @param groupId The identifier of the pool group leader.
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters poolGroupID(final String groupId)
    {
        parameters_.put(FIELD_LEVEL_GROUPID, parameter(FIELD_LEVEL_GROUPID, groupId));
        return this;
    }


    /**
     * Gets the specified pool group ID.
     *
     * @return The pool group ID.
     */
    public String getPoolGroupID()
    {
        final Parameter parameter = getParameter(FIELD_LEVEL_GROUPID, "No pool group ID specified.");
        return parameter.value.stringValue();
    }


    /**
     * Specifies the pool group ID.
     *
     * @param groupOwner The identifier of the pool group leader.
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters poolGroupOwner(final String groupOwner)
    {
        parameters_.put(FIELD_LEVEL_GROUPOWNER, parameter(FIELD_LEVEL_GROUPOWNER, groupOwner));
        return this;
    }


    /**
     * Gets the specified pool group ID.
     *
     * @return The pool group ID.
     */
    public String getPoolGroupOwner()
    {
        final Parameter parameter = getParameter(FIELD_LEVEL_GROUPOWNER, "No pool group owner MSISDN specified.");
        return parameter.value.stringValue();
    }


    /**
     * Specifies the price plan ID.
     *
     * @param pricePlan The identifier of the price plan.
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters pricePlan(final long pricePlan)
    {
        parameters_.put(SUP_FIELD_LEVEL_PRICEPLAN, parameter(SUP_FIELD_LEVEL_PRICEPLAN, pricePlan));
        return this;
    }


    /**
     * Gets the specified price plan ID.
     *
     * @return The price plan ID.
     */
    public long getPricePlan()
    {
        final Parameter parameter = getParameter(SUP_FIELD_LEVEL_PRICEPLAN, "No price plan specified.");
        return parameter.value.longValue();
    }


    /**
     * Specifies the service provider ID.
     *
     * @param spid The service provider ID.
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters spid(final int spid)
    {
        parameters_.put(SPID, parameter(SPID, spid));
        return this;
    }


    /**
     * Gets the specified service provider ID.
     *
     * @return The service provider ID.
     */
    public int getSpid()
    {
        final Parameter parameter = getParameter(SPID, "No service provider ID specified.");
        return parameter.value.intValue();
    }


    /**
     * Specifies the state.
     *
     * @param state The state.
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters state(final int state)
    {
        parameters_.put(FIELD_LEVEL_STATE, parameter(FIELD_LEVEL_STATE, state));
        return this;
    }


    /**
     * Gets the specified state.
     *
     * @return The state.
     */
    public int getState()
    {
        final Parameter parameter = getParameter(FIELD_LEVEL_STATE, "No state specified.");
        return parameter.value.intValue();
    }


    /**
     * Specifies the subscriber account profile ID.
     *
     * @param subscriberID The subscriber account profile ID.
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters subscriberID(final String subscriberID)
    {
        parameters_.put(FIELD_LEVEL_SUBSCRIBERID, parameter(FIELD_LEVEL_SUBSCRIBERID, subscriberID));
        return this;
    }


    /**
     * Gets the specified subscriber account profile ID.
     *
     * @return The subscriber account profile ID.
     */
    public String getSubscriberID()
    {
        final Parameter parameter =
            getParameter(FIELD_LEVEL_SUBSCRIBERID, "No subscriber account profile ID specified.");
        return parameter.value.stringValue();
    }


    /**
     * Specifies the timezone offset.
     *
     * @param timezoneOffset The timezone offset.
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters timezoneOffset(final String timezoneOffset)
    {
        parameters_.put(FIELD_LEVEL_TZOFFSET, parameter(FIELD_LEVEL_TZOFFSET, timezoneOffset));
        return this;
    }


    /**
     * Gets the specified timezone offset.
     *
     * @return The timezone offset.
     */
    public String getTimezoneOffset()
    {
        final Parameter parameter = getParameter(FIELD_LEVEL_TZOFFSET, "No timezone offset specified.");
        return parameter.value.stringValue();
    }


    /**
     * Specifies the subscription level.
     *
     * @param subscriptionLevel The subscription level.
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters subscriptionLevel(final int subscriptionLevel)
    {
        parameters_.put(FIELD_LEVEL_SUBSCRIPTIONLEVEL, parameter(FIELD_LEVEL_SUBSCRIPTIONLEVEL, subscriptionLevel));
        return this;
    }


    /**
     * Gets the specified subscription level.
     *
     * @return The subscription level.
     */
    public int getSubscriptionLevel()
    {
        final Parameter parameter = getParameter(FIELD_LEVEL_SUBSCRIPTIONLEVEL, "No subscription level specified.");
        return parameter.value.intValue();
    }


    /**
     * Specifies the subscription type.
     *
     * @param subscriptionType The subscription type.
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters subscriptionType(final int subscriptionType)
    {
        parameters_.put(FIELD_LEVEL_SUBSCRIPTIONTYPE, parameter(FIELD_LEVEL_SUBSCRIPTIONTYPE, subscriptionType));
        return this;
    }


    /**
     * Gets the specified subscription type.
     *
     * @return The subscription type.
     */
    public int getSubscriptionType()
    {
        final Parameter parameter = getParameter(FIELD_LEVEL_SUBSCRIPTIONTYPE, "No subscription type specified.");
        return parameter.value.intValue();
    }

    
    /**
     * Specifies the monthly spend limit
     *
     * @param monthlyspendLimit monthly spend limit quota
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters monthlySpendLimit(final long  monthlySpendLimit)
    {
        parameters_.put(BAL_FIELD_LEVEL_MONTHLYSPENDLIMIT, parameter(BAL_FIELD_LEVEL_MONTHLYSPENDLIMIT, monthlySpendLimit));
        return this;
    }


    /**
     * Gets the specified monthly spend usage
     *
     * @return monthly spend usage
     */
    public long getMonthlySpendUsage()
    {
        final Parameter parameter = getParameter(BAL_FIELD_LEVEL_MONTHLYSPENDUSAGE, "No monthly spend usage specified.");
        return parameter.value.longValue();
    }
    
    
    /**
     * Specifies the monthly spend usage
     *
     * @param monthlyspendusage 
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters monthlySpendUsage(final long monthlySpendUsage)
    {
        parameters_.put(BAL_FIELD_LEVEL_MONTHLYSPENDUSAGE, parameter(BAL_FIELD_LEVEL_MONTHLYSPENDUSAGE, monthlySpendUsage));
        return this;
    }


    /**
     * Gets the specified monthlyspendlimit
     *
     * @return The monthlyspendlimit
     */
    public long getMonthlySpendLimit()
    {
        final Parameter parameter = getParameter(BAL_FIELD_LEVEL_MONTHLYSPENDLIMIT, "No monthly spend limit specified.");
        return parameter.value.longValue();
    }


    /**
     * Specifies the billing type
     * 
     * @param billingType
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters billingType(final short accountType)
    {
        parameters_.put(SUP_FIELD_LEVEL_BILLINGTYPE, parameter(SUP_FIELD_LEVEL_BILLINGTYPE, accountType));
        return this;
    }


    /**
     * Gets the specified billing
     * 
     * @return The billing
     */
    public short getBillingType()
    {
        final Parameter parameter = getParameter(SUP_FIELD_LEVEL_BILLINGTYPE, "No Billing Type specified.");
        return 0;//parameter.value.shortValue();
    }


    /**
     * Specifies the language
     * 
     * @param lang
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters billingLanguage(final String lang)
    {
        parameters_.put(SUP_FIELD_LEVEL_LANGUAGE, parameter(SUP_FIELD_LEVEL_LANGUAGE, lang));
        return this;
    }


    /**
     * Gets the specified billing language
     * 
     * @return The billing
     */
    public String getBillingLanguage()
    {
        final Parameter parameter = getParameter(SUP_FIELD_LEVEL_LANGUAGE, "No language specified.");
        return parameter.value.stringValue();
    }

    
    /**
     * Gets the specified monthly spend usage
     *
     * @return monthly spend usage
     */
    public boolean getDualBalanceStatus()
    {
        final Parameter parameter = getParameter(SUP_FIELD_LEVEL_DUAL_BALANCE, "No dual balance specified.");
        return parameter.value.booleanValue();
    }
    
    
    /**
     * Specifies the monthly spend usage
     *
     * @param monthlyspendusage 
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters dualBalanceStatus(final boolean monthlySpendUsage)
    {
        parameters_.put(SUP_FIELD_LEVEL_DUAL_BALANCE, parameter(SUP_FIELD_LEVEL_DUAL_BALANCE, monthlySpendUsage));
        return this;
    }
    
    /**
     * Gets the Notification Preference
     *
     * @return Notification Preference
     */
    public short getNotificationPreference()
    {
        final Parameter parameter = getParameter(SUP_FIELD_LEVEL_NOTIFICATION_PREFERNCE, "No notification preference specified.");
        return parameter.value.shortValue();
    }
    
    
    /**
     * Specifies the Notification Preference
     *
     * @param notificationPreference 
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters notificationPreference(final String notificationPreference)
    {
        parameters_.put(SUP_FIELD_LEVEL_NOTIFICATION_PREFERNCE, parameter(SUP_FIELD_LEVEL_NOTIFICATION_PREFERNCE, notificationPreference));
        return this;
    }

    /**
     * Gets the email
     *
     * @return Email
     */
    public String getEmail()
    {
        final Parameter parameter = getParameter(SUP_FIELD_LEVEL_EMAIL, "No email specified.");
        return parameter.value.stringValue();
    }
    
    
    /**
     * Specifies the Email
     *
     * @param email 
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters email(final String email)
    {
        parameters_.put(SUP_FIELD_LEVEL_EMAIL, parameter(SUP_FIELD_LEVEL_EMAIL, email));
        return this;
    }

    /**
     * Gets the PPSM Supporter
     *
     * @return PPSM Supporter
     */
    public String getPpsmSupporter()
    {
        final Parameter parameter = getParameter(SUP_FIELD_LEVEL_PPSM_SUPPORTER, "No PPSM supporter specified.");
        return parameter.value.stringValue();
    }
    
    
    /**
     * Specifies the PPSM Supporter
     *
     * @param ppsmSupporter 
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters ppsmSupporter(final String ppsmSupporter)
    {
        parameters_.put(SUP_FIELD_LEVEL_PPSM_SUPPORTER, parameter(SUP_FIELD_LEVEL_PPSM_SUPPORTER, ppsmSupporter));
        return this;
    }

    /**
     * Gets the PPSM Screening Template
     *
     * @return PPSM Screening Template
     */
    public long getPpsmScreeningTemplate()
    {
        final Parameter parameter = getParameter(SUP_FIELD_LEVEL_PPSM_SCREENING_TEMPLATE, "No PPSM screening template specified.");
        return parameter.value.longValue();
    }
    
    
    /**
     * Specifies the PPSM Screening Template
     *
     * @param ppsmScreeningTemplate 
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters ppsmScreeningTemplate(final long ppsmScreeningTemplate)
    {
        parameters_.put(SUP_FIELD_LEVEL_PPSM_SCREENING_TEMPLATE, parameter(SUP_FIELD_LEVEL_PPSM_SCREENING_TEMPLATE, ppsmScreeningTemplate));
        return this;
    }

    /**
     * Gets the Blocking Template
     *
     * @return Blocking Template
     */
    public long getBlockingTemplate()
    {
        final Parameter parameter = getParameter(SUP_FIELD_LEVEL_BLOCKING_TEMPLATE_ID, "No Blocking template specified.");
        return parameter.value.longValue();
    }
    
    /**
     * Specifies the Blocking Template
     *
     * @param blockingTemplate 
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters blockingTemplate(final long blockingTemplate)
    {
        parameters_.put(SUP_FIELD_LEVEL_BLOCKING_TEMPLATE_ID, parameter(SUP_FIELD_LEVEL_BLOCKING_TEMPLATE_ID, blockingTemplate));
        return this;
    }
    
    
    
    /**
     * Specifies the IMSI
     * 
     * @param IMSI
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters IMSI(final String IMSI)
    {
        parameters_.put(SUP_FIELD_LEVEL_IMSI, parameter(SUP_FIELD_LEVEL_IMSI, IMSI));
        return this;
    }


    /**
     * Gets the IMSI
     * 
     * @return The IMSI
     */
    public String getIMSI()
    {
        final Parameter parameter = getParameter(SUP_FIELD_LEVEL_IMSI, "No IMSI specified.");
        return parameter.value.stringValue();
    }
	
	/**
     * @param groupScreeningTemplateID
     */
    public Parameters setGroupScreeningTemplateld(final long groupScreeningTemplateId)
    {
        parameters_.put((short)546, parameter((short)546, groupScreeningTemplateId));
        return this;
    }
   
    /**
     * @return The GroupScreeningTemplateId value
     */
    public long getGroupScreeningTemplateld()
    {
        final Parameter parameter = getParameter((short)546, "No GroupScreeningTemplateId specified.");
        return parameter.value.longValue();
    }
    
    
    /**
     * Specifies the ESN
     * 
     * @param ESN
     * @return These Parameters, to allow additional values to be specified.
     */
    
    public Parameters ESN(final String ESN)
    {
        parameters_.put(SUP_CHARGING_ID_ESN, parameter(SUP_CHARGING_ID_ESN, ESN));
        return this;
    }

    /**
     * Gets the ESN
     * 
     * @return The ESN
     */
    public String getESN()
    {
        final Parameter parameter = getParameter(SUP_CHARGING_ID_ESN, "No ESN specified.");
        return parameter.value.stringValue();
    }
    
    /**
     * Specifies the MSID
     * 
     * @param MSID
     * @return These Parameters, to allow additional values to be specified.
     */
    
    public Parameters MSID(final String MSID)
    {
        parameters_.put(SUP_CHARGING_ID_MSID, parameter(SUP_CHARGING_ID_MSID, MSID));
        return this;
    }

    /**
     * Gets the MSID
     * 
     * @return The MSID
     */
    public String getMSID()
    {
        final Parameter parameter = getParameter(SUP_CHARGING_ID_MSID, "No IMSI specified.");
        return parameter.value.stringValue();
    }
    
    
    public Parameters setImsi(final String IMSI)
    {
        parameters_.put(SUP_CHARGING_ID_IMSI, parameter(SUP_CHARGING_ID_IMSI, IMSI));
        return this;
    }
    public String getImsi()
    {
        final Parameter parameter = getParameter(SUP_CHARGING_ID_IMSI, "No IMSI specified.");
        return parameter.value.stringValue();
    }
    /**
     * Specifies the Class Of Service
     * 
     * @param classOfService
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters classOfService(final int classOfService)
    {
        parameters_.put(SUP_FIELD_LEVEL_CLASSOFSERVICE, parameter(SUP_FIELD_LEVEL_CLASSOFSERVICE, classOfService));
        return this;
    }


    /**
     * Gets the Class of Service
     * 
     * @return The Class of Service
     */
    public int getClassOfService()
    {
        final Parameter parameter = getParameter(SUP_FIELD_LEVEL_CLASSOFSERVICE, "No IMSI specified.");
        return parameter.value.intValue();
    }


    /**
     * @param balanceThreshold
     */
    public Parameters setBalanceThreshold(final long balanceThreshold)
    {
        parameters_.put((short)619, parameter((short)619, balanceThreshold));
        return this;
    }


    /**
     * @return The balanceThreshold value
     */
    public long getBalanceThreshold()
    {
        final Parameter parameter = getParameter((short)619, "No balanceThreshold specified.");
        return parameter.value.longValue();
    }

    /**
     * Gets the Notification Type
     *
     * @return Notification Type
     */
    public int getNotificationType()
    {
        final Parameter parameter = getParameter(SUP_FIELD_LEVEL_NOTIFICATION_TYPE, NO_NOTIFICATION_TYPE_MSG);
        return parameter.value.intValue();
    }
    
    
    /**
     * Specifies the Notification Type
     *
     * @param notificationType
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters notificationType(final int notificationType)
    {
        parameters_.put(SUP_FIELD_LEVEL_NOTIFICATION_TYPE, parameter(SUP_FIELD_LEVEL_NOTIFICATION_TYPE, notificationType));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (final Map.Entry<Short, Parameter> entry : parameters_.entrySet())
        {
            builder.append(entry.getKey());
            builder.append('=');

            final ParameterValue parameter = entry.getValue().value;
            final ParameterValueType discriminator = parameter.discriminator();

            switch(discriminator.value())
            {
                case ParameterValueType._PARAM_BOOLEAN:
                {
                    builder.append(parameter.booleanValue());
                    break;
                }
                case ParameterValueType._PARAM_BYTE_STREAM:
                {
                    builder.append("STREAM");
                    break;
                }
                case ParameterValueType._PARAM_DOUBLE:
                {
                    builder.append(parameter.doubleValue());
                    break;
                }
                case ParameterValueType._PARAM_FLOAT:
                {
                    builder.append(parameter.floatValue());
                    break;
                }
                case ParameterValueType._PARAM_INT:
                {
                    builder.append(parameter.intValue());
                    break;
                }
                case ParameterValueType._PARAM_LONG:
                {
                    builder.append(parameter.longValue());
                    break;
                }
                case ParameterValueType._PARAM_SHORT:
                {
                    builder.append(parameter.shortValue());
                    break;
                }
                case ParameterValueType._PARAM_STRING:
                {
                    builder.append("\"");
                    builder.append(parameter.stringValue());
                    builder.append("\"");
                    break;
                }
                default:
                {
                    builder.append("Unknown type: ");
                    builder.append(discriminator.value());
                }
            }

            builder.append(',');
        }
        builder.append("]");
        return builder.toString();
    }


    /**
     * Ends a typical Parameters usage, returning the Parameter array. This
     * method is package visible to avoid leaking CORBA/version specific code.
     *
     * @return The array of Parameters specified so far.
     */
    Parameter[] end()
    {
        return parameters_.values().toArray(new Parameter[parameters_.values().size()]);
    }


    /**
     * Utility method to create a Parameter from an "short" value.
     *
     * @param id The parameter ID. See ParameterID.
     * @param value The value of the parameter.
     * @return The newly created Parameter.
     */
    private Parameter parameter(final short id, final short value)
    {
        final ParameterValue paramValue = new ParameterValue();
        paramValue.shortValue(value);

        return new Parameter(id, paramValue);
    }

    /**
     * Utility method to create a Parameter from an "int" value.
     *
     * @param id The parameter ID. See ParameterID.
     * @param value The value of the parameter.
     * @return The newly created Parameter.
     */
    private Parameter parameter(final short id, final int value)
    {
        final ParameterValue paramValue = new ParameterValue();
        paramValue.intValue(value);

        return new Parameter(id, paramValue);
    }

    /**
     * Utility method to create a Parameter from an "long" value.
     *
     * @param id The parameter ID. See ParameterID.
     * @param value The value of the parameter.
     * @return The newly created Parameter.
     */
    private Parameter parameter(final short id, final long value)
    {
        final ParameterValue paramValue = new ParameterValue();
        paramValue.longValue(value);

        return new Parameter(id, paramValue);
    }


    /**
     * Utility method to create a Parameter from an "boolean" value.
     *
     * @param id The parameter ID. See ParameterID.
     * @param value The value of the parameter.
     * @return The newly created Parameter.
     */
    private Parameter parameter(final short id, final boolean value)
    {
        final ParameterValue paramValue = new ParameterValue();
        paramValue.booleanValue(value);

        return new Parameter(id, paramValue);
    }


    /**
     * Utility method to create a Parameter from an "String" value.
     *
     * @param id The parameter ID. See ParameterID.
     * @param value The value of the parameter.
     * @return The newly created Parameter.
     */
    private Parameter parameter(final short id, final String value)
    {
        final ParameterValue paramValue = new ParameterValue();
        paramValue.stringValue(value);

        return new Parameter(id, paramValue);
    }


    /**
     * Gets the parameter specified for the given ID.
     *
     * @param parameterID The ID of the parameter to query.
     * @param unspecifiedMessage The message included in the exception if a
     * value is not found.
     * @return The parameter with the given ID.
     * @exception IllegalArgumentException Thrown if no such parameter has been
     * specified.
     */
    private Parameter getParameter(final short parameterID, final String unspecifiedMessage)
    {
        final Parameter parameter = parameters_.get(parameterID);
        if (parameter == null)
        {
            throw new IllegalArgumentException(unspecifiedMessage);
        }
        return parameter;
    }


    /**
     * Specifies the monthly spend usage
     *
     * @param monthlyspendusage 
     * @return These Parameters, to allow additional values to be specified.
     */
    public Parameters promotionalSmsOptOut(final boolean status)
    {
        parameters_.put(SUP_FIELD_LEVEL_PROMOTION_SMS_OPTOUT, parameter(SUP_FIELD_LEVEL_PROMOTION_SMS_OPTOUT, status));
        return this;
    }
    
    
    /**
     * Maps ParameterID to a Parameter.
     */
    private final Map<Short, Parameter> parameters_;
    public static final String NO_NOTIFICATION_TYPE_MSG = "No notification type specified.";
}
