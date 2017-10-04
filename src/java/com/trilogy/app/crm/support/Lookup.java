/*
 * Created on May 14, 2005
 *
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

import com.trilogy.app.crm.extension.subscriber.FixedStopPricePlanSubExtension;

/**
 * Class containing well-known keys for objects that are to be found in the context.
 *
 * @author psperneac
 *
 */
public interface Lookup
{
	/** Key under which you can find the subscriber object during a subscriber provisioning */
	public static final String OLDSUBSCRIBER="SubscriberProvisioning.OLDSUBSCRIBER";
    public static final String OLD_FROZEN_SUBSCRIBER="SubscriberProvisioning.OLDFROZNESUBSCRIBER";
    public static final String NEW_SUBSCRIBER="SubscriberProvisioning.NEWSUBSCRIBER";

	/** Key under which you can find the subscriber clone object during a subscriber provisioning */
	public static final String TEMPLATESUBSCRIBER="SubscriberProvisioning.TEMPLATESUBSCRIBER";

	/** Key under which you can find the account object during a subscriber provisiioning*/
	public static final String ACCOUNT="SubscriberProvisioning.ACCOUNT";

	public final static String CONTEXT_KEY_ACCOUNT = "com.redknee.app.crm.home.sub.account.";
	public final static String CONTEXT_KEY_SUBSCRIBER = "com.redknee.app.crm.home.sub.";

	/** timestamp for cleaning the subscriber services cache home. */
	public static final String SUBSCRIBERSERVICESTIMESTAMP = "SUBSCRIBERSERVICESTIMESTAMP";

    /** Key under which you can find a flag whether ECP provision requires to update or not during a subscriber provisioning */
    public final static String CONTEXT_KEY_ECP_UPDATE_FLAG = "com.redknee.app.crm.subscriber.provision.ecp.CONTEXT_KEY_ECP_UPDATE_FLAG";
    /** Key under which you can find the ECP profile object during a subscriber provisioning */
    public final static String ECPPROFILE_NEWSTATE = "SubscriberProvisioning.ECPPROFILE_STATE";
    public final static String ECPPROFILE_NEWCOS = "SubscriberProvisioning.ECPPROFILE_COS";
    public final static String ECPPROFILE_NEWVOICEPLAN = "SubscriberProvisioning.ECPPROFILE_VOICEPLAN";
    public final static String ECPPROFILE_GROUPMSISDN= "SubscriberProvisioning.ECPPROFILE_GROUPMSISDN";
    public final static String ECPPROFILE_PACKAGE= "SubscriberProvisioning.ECPPROFILE_PACKAGE";

    /** Key under which you can find the amount remaining on the Payment Plan*/
    public final static String PAYMENTPLAN_BALANCE = "PaymentPlan.BALANCE";
    public static final String OLDACCOUNTPAYMETHOD="AccountPaymentMethod.OLDACCOUNTPAYMETHOD";
    /** Context key for the old additional MSISDN. */
    String OLD_AMSISDN = "SubscriberAuxiliaryService.OLD_AMSISDN";

    /** Context key for the new additional MSISDN. */
    String NEW_AMSISDN = "SubscriberAuxiliaryService.NEW_AMSISDN";

    /** Context key for the old subscriber auxiliary service association. */
    String OLD_SUBSCRIBER_AUXILIARY_SERVICE = "SubscriberAuxiliaryService.OLD_ASSOCIATION";

    /** Context key for postpaid->prepaid conversion. */
    String POSTPAID_PREPAID_CONVERSION = "PostpaidPrepaidConversion";
    
    String PRICEPLAN_SWITCH_COUNTER_INCREMENT = "PricePlanSwitchCounterIncrement";
    
    /** The variable which specifies that its a price plan change request from DCRM*/
    String PRICEPLAN_CHANGE_REQUEST_FROM_DCRM = "PricePlanChangeRequestFromDCRM";

    /** Context key for an old Multi-SIM extension. */
    public String OLD_MULTISIM_SUB_EXTENSION = "MultiSimSubExtension.OLD_VALUE";

    /** Context key for an old FixedStopPricePlan extension. */
    public String OLD_FIXED_STOP_PRICEPLAN_SUB_EXTENSION = "FixedStopPricePlanSubExtension.OLD_VALUE";
    
    /** Context key for an old RESOURCE Object. */
    public static final String OLD_RESOURCE_OBJECT = "OLD_RESOURCE_OBJECT";
    /** Context key for an old SubscriberResources object. */
    public static final String OLD_SUBSCRIBER_RESOURCE = "OLD_SUBSCRIBER_RESOURCE";
    /** Context key for is-swap_request. */
    public static final String IS_SWAP_REQUEST = "IS_SWAP_REQUEST";
    
    /** Context key for ServiceDates */
    public static final String  SERVICE_START_DATE = "ServiceStartDate";
    public static final String  SERVICE_END_DATE = "ServiceEndDate";
    public static final String  BILL_START_DATE = "BillStartDate";
    public static final String  BILL_END_DATE = "BillEndDate";
    public static final String	DEACTIVATION_DATE="DeactivationDate";
    
    /** Context key for ResourceDates */
    public static final String  RESOURCE_BILL_START_DATE = "ResourceBillStartDate";
    public static final String  RESOURCE_BILL_END_DATE = "ResourceBillEndDate";
    
    /** Context key for ReplaceDevice */
    public static final String REPLACE_DEVICE="ReplaceDevice";
    /** Context key for OldServiceDates */
    public static final String  OLD_SERVICE_START_DATE = "OldServiceStartDate";
    public static final String  OLD_SERVICE_END_DATE = "OldServiceEndDate";
    public static final String  OLD_BILL_START_DATE = "OldBillStartDate";
    public static final String  OLD_BILL_END_DATE = "OldBillEndDate";
    public static final String  SPID = "Spid";
    
    /** Context Key for PayerId  **/
    public static final String  PAYER_ID = "PayerId";

    public static final String  PROV_SALE_PAYER = "CANCELPROVSALES";
    
    public static final String  MONTHLY_PRO_SALE = "InMemoryMAP";
    
    public static final String TRIGGER_POINT="trigger";
    
    public static final String SERIVCE_QUANTITY = "ServiceQuantity";
    
    public static final String CLASSIFICATION_OF_PAYMENT = "ClassificationOfPayment";
    
    public static final String MULTIDENOMINATION_KEY = "MultiDenominationKey";
    
    public static final String CUSTOM_INVOICE_DESC = "CustomizedInvoiceDescription";
	public static final String CALL_FORWARDING_NUMBER = "CallForwardingNumber";
    
	//Service ReplaceID Dates
	 public static final String SERVICE_REPLACE_START_DATE = "ServiceReplaceIdStartDate";
	 public static final String SERVICE_REPLACE_END_DATE = "ServiceReplaceIdEndDate";
	 public static final String SERVICE_REPLACE_BILLING_START_DATE = "ServiceReplaceIdBillingStartDate";
	 public static final String SERVICE_REPLACE_BILLING_END_DATE = "ServiceReplaceIdBillingEndDate";
	 
	 public static final String PATH = "path";
	 //Sprint 81 : BSS-6282 : CreateSubscriptionV2 API Enhancement : validFor
	 public static final String VALID_FOR = "validFor";
	 public static final String FUTURE_DATED_PRICEPLAN_CHANGE = "FUTURE_DATED_PRICEPLAN_CHANGE";
}