/*
 * Copyright (c) 1999-2003, REDKNEE. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of REDKNEE.
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with REDKNEE.
 *
 * REDKNEE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT. REDKNEE SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR
 * ITS DERIVATIVES.
 */
// INSPECTED: 10/07/2003 GEA
package com.trilogy.app.crm.poller;

/**
 * Base keys for some objects that are put in the context. It also contains the
 * indexes in the ER array for the fields we are interested in.
 *
 * @author psperneac
 */
public interface Constants
{
   /** prefix for all poller names */
   public static final String POLLER = "poller_";
   /** prefix for all poller thread names */
   public static final String POLLER_THREAD = "poller_thread_";
   /** prefix for all poller heartbeat agent names */
   public static final String POLLERAGENT = "poller_heartbeat_agent_";
   /** prefix for all poller heartbeat task names */
   public static final String POLLERTASK = "poller_heartbeat_task_";
   /** description string for all poller agents */
   public static final String POLLERDESC = " Poller Heartbeat Client ";

/* The SMSB Er indices are configurable now.[7 Oct'06]
 * ////////////////////////////
   // SMSB
   // index 0 is the ER class

   *//** ER field index. service provider id of subscriber. *//*
   public static final int SMSB_SPID_INDEX = 2;
   *//** ER field index. the msisdn of the subscriber (the one that pays) *//*
   public static final int SMSB_CHARGEDMSISDN_INDEX = 3;
   *//**
    * ER field index. originating MSCID that originator was on or 0 if
    * SMPPoroginated message.
    *//*
   public static final int SMSB_ORIGMSCID_INDEX = 4;
   *//**
    * ER field index. the other party msisdn or a combination of the source sme
    * and source account billing.
    *//*
   public static final int SMSB_OTHERMSISDN_INDEX = 5;
   *//**
    * ER field index. charged rate in sub-local currency to subscriber. If
    * negative implies a credit.
    *//*
   public static final int SMSB_RATE_INDEX = 9;

   *//**
    * ER field index. charged party
    *//*
   public static final int SMSB_CHARGED_PARTY = 10;

   *//** ER field index. The rule# that was applied to determine the rate. *//*
   public static final int SMSB_RATINGRULE_INDEX = 12;
   *//**
    * ER field index. Internal transaction ID that can be used to match to
    * resultant transactions and used as a reference in the corresponding OIS
    * transaction.
    *//*
   //public static final int SMSB_TRANSACTIONID_INDEX = 13;
   *//**
    * ER field index.  The remaining balance of the charged MSISDN, IF the
    * MSISDN was charged. Otherwise null. If the message was blocked, the
    * original balance of the subscriber.
    *//*
   public static final int SMSB_BALANCE_INDEX = 15;
   *//**
    * ER field index. The rate plan of of the subscriber who was charged with
    * this transaction. Not valid in Ensure Delivery mode.
    *//*
   public static final int SMSB_RATEPLANID_INDEX = 16;

    *//**
     * The messag is bundle/bucket charged and decremented from the bundle/bucket. Set to True i the message matches
     * a bucket/bundle rule and the SMS count increses.
     *//*
    //public static final int SMSB_BUCKET_DECREMENT = 18;

   public static final int SMSB_ORIG_SVC_GRADE = 19;
   public static final int SMSB_TERM_SVC_GRADE = 20;
   public static final int SMSB_SEQUENCE_NUM = 21;
   *//** ER field index. The local date of the subscriber in his/her timezone. *//*
   public static final int SMSB_LOCAL_SUBSCRIBER_DATE = 23;
   *//** ER field index. The local time of the subscriber in his/her timezone. *//*
   public static final int SMSB_LOCAL_SUBSCRIBER_TIME = 24;

   public static final int SMSB_BAN = 26;

   //public static final int SMSB_ORIG_LARGE_ACCT_ID=35;
   //Manda - Modified the index as per the new ER modified fields
   public static final int SMSB_ORIG_LARGE_ACCT_ID= 34;
   //public static final int SMSB_DEST_LARGE_ACCT_ID=36;
   //Manda - Modified the index as per the new ER modified fields
   public static final int SMSB_DEST_LARGE_ACCT_ID=35;

   //ER Field Index for VPN Call Type
   public static final int SMSB_VPN_CALLED = 42;
   //public static final int SMSB_VPN_SESSION_ID = 43;
   //public static final int SMSB_VPN_BAN = 43;
   //public static final int SMSB_VPN_TRANS_DEST_ADDR = 44;
   //public static final int SMSB_VPN_TRANS_DEST_TON = 45;
   //public static final int SMSB_VPN_TRANS_DEST_NPI = 46;

   public static final int SMSB_VPN_CALL_TYPE = 47;
   public static final int SMSB_VPN_DISCOUNT = 48;
   public static final int SMSB_VPN_BILLINGDN = 49;
   public static final String SMSB_VPN_CALLED_VALID = "1";


   public static final int SMSB_BM_ACTION_ID=44;
 */

   ///////////////////////////
   // URS
   // index 0 is the ER class

   /** ER field index. Service provider ID. */
   public static final int URS_SPID_INDEX = 2;
   /** ER field index. Charged MSISDN in E164 format. */
   public static final int URS_CHARGEDMSISDN_INDEX = 4;
   /** ER field index. Originating MSC ID */
   public static final int URS_ORIGINATING_MSC_ID_INDEX = 5;

   /**
    * ER field index. 0=call initiation event, 1=balance decrement event,
    * 2=call end event.
    */
   public static final int URS_EVENTTYPE_INDEX = 7;
   /** ER field index. the rate plan id used to determine the charge. */
   public static final int URS_RATEPLAN_INDEX = 8;
   /** ER field index. the rule # that was applied to determine this charge. */
   public static final int URS_RATINGRULE_INDEX = 57;
   /** ER field index. Charged flat rate in sub-local currency to subscriber. */
   public static final int URS_FLATRATE_INDEX = 10;
   /**
    * ER field index. Charged variable rate in sub-local currency to
    * subscriber.
    */
   public static final int URS_VARIABLERATE_INDEX = 11;
   /** ER field index. Charged variable rate unit. (Seconds, Minutes, KB). */
   public static final int URS_VARIABLERATEUNIT_INDEX = 12;
   /**
    * ER field index. Who the call/message was charged to: O for originator, T
    * for terminating.
    */
   public static final int URS_CHARGEDPARTY_INDEX = 14;
   /** ER field index. TP Session ID of call. */
   public static final int URS_CALLSESSIONID_INDEX = 15;
   /** ER field index.  The remaining balance of the charged MSISDN if the
    * MSISDN was charged. Otherwise null. If the message was blocked, the
    * original balance of the subscriber. */
   public static final int URS_BALANCE_INDEX = 17;
   /**
    * ER field index. The zone ID under which the location falls for the call
    * origination.
    */
   public static final int URS_ORIGLOCZONED_INDEX = 18;
   /** ER field index. The CDPN for the call attempt. */
   public static final int URS_DESTNUMBER_INDEX = 21;
   /** ER field index. The redirected address of the call */
   public static final int URS_REDIRECTED_ADDRESS_INDEX = 23;
   /** ER field index. The MSC generated call reference id. */
   public static final int URS_CALL_REFERENCE_ID = 25;
   /** ER field index. The duration as passed in by the application. */
   public static final int URS_DURATION_INDEX = 26;
   /** ER field index. The cost of the call to this point. */
   public static final int URS_RUNNINGCALLCOST_INDEX = 27;
   // DZ Added following var definition
   /** ER field index. The zone ID under which the location falls for the call
    * origination
    */
   public static final int URS_ORIGLOCZONEDESC_INDEX = 28;
   /**
    * ER field index. If bucket rating was applied, this is the Bucket Rate ID
    * used to determine the charge.
    */
   public static final int URS_BUCKETRATEID_INDEX = 31;
   /**
    * ER field index. The account bucket rating balance (total call time in the
    * month). This information is provided to URS by the payment application.
    */
   public static final int URS_BUCKETRATINGBALANCE_INDEX = 33; //??
   /**
    * ER field index. This value is passed to the URS service by the
    * application (e.g. Enhanced Call Payment application) when the call is
    * disconnected. It is provided in the "Call End Event" ER-501 generated.
    * The following is a list of applicable Disconnect Reason values:
    * <ol>
    * <li>0 - Originating Party Disconnects</li>
    * <li>1 - Terminating Party Disconnects</li>
    * <li>3 - Call Control Error</li>
    * <li>4 - Interval Timer</li>
    * </ol>
    */
   public static final int URS_DISCONNECTREASON_INDEX = 34;
   /**
    * ER field index. This code may be used for accounting purposes by the
    * Service Provider billing system.
    */
   public static final int URS_GLCODE_INDEX = 35;
   /** ER field index. The CGPN digits. */
   public static final int URS_ORIGMSISDN_INDEX = 36;
   /**
    * ER field index. Time of call adjusted according to the timezone of the
    * subscriber's location. This time value is formatted as HH:MM:SS in a 24
    * hour clock.
    */
   public static final int URS_TIME_OF_CALL = 37;
   /** ER field index */
   public static final int URS_DATE_OF_CALL = 38;

   /**
    * ER field index.  If special charging feature is applied to this call,
    * this parameter indicates its type:
    * 0 - Not Applicable
    * 1 - Personal List Plan Rate Plan
    * 2 - Personal List Plan % discount
    * 3 - Closed User Group Rate Plan
    * 4 - Closed User Group % discount
    */
   public static final int URS_APPLIED_SPECIAL_CHARGE_INDICATOR = 39;
   /**
    * ER field index.  If applicable, this parameter identifies the Personal List Plan
    * or Close User Group applied to the call.  If PLP or CUG special rating was not
    * applied to the call then this value is defaulted to zero (0).
    */
   public static final int URS_PLP_CUG_ID = 40;
   /**
    * ER field index.  The percentage discount (0-100) applied to the call rates.
    */
   public static final int URS_APPLIED_PERCENTAGE_DISCOUNT = 41;
   /**
    * ER field index.  It indicates whether or not the presentation of the CGPA is
    * restricted (e.g if it can be shown in billing statements):
    * 0 - Unrestricted (default)
    * 1 - Restricted
    */
   public static final int URS_CGPA_PRESENTATION_RESTRICTED = 42;
   /**
    * ER field index.  It indicates the Subscriber's payment account type:
    * 0 - postpaid
    * 1 - prepaid
    */
   public static final int URS_ACCOUNT_TYPE = 43;
   /**
    * ER field index.  String that identifies the billing option for this call.
    * Information is extracted from the matching rate rule.
    */
   public static final int URS_BILLING_OPTION = 44;

   /**
    * The bucket amount used for each debit interval. The bucket amount is in seconds.
    */
   public static final int URS_BUCKET_COUNTER = 45;

   /**
    * Type of teleservice that applies to the rating rule:
    * 0 - All
    * 1 - Fax
    * 2 - Data
    * 3 - Voice
    * 4 - Sms
    */
   public static final int URS_TELESERVICE_TYPE = 46;

   /**
    * ER Field index. Identifies the Timeband type for this call
    * O - Off Peak
    * I - Independent
    * P - Peak
    * S - Special
    */
   public static final int URS_TIMEBAND_TYPE = 47;

   /**
    * ER Field index. Identifies the Timeband type for this call
    * O - Off Peak
    * I - Independent
    * P - Peak
    * S - Special
    */
   public static final int URS_DAY_CATEGORY = 48;

   /**
    * The charged amounts for the billing event separated according to the rate component. It is '|' delimited list
    * with each entry in the format: <component>=<amount>.
    */
   public static final int URS_LIST_OF_AMOUNTS = 49;

   /**
    * The list of discounts "VPN" or "FF" or "HZ". It is '|' delimited list
    * with each entry in the format: <component>=<amount>.
    */
   public static final int URS_LIST_OF_DISCOUNTS = 50;

   /**
    * The rerating indicator to determine if this is a rerated ER501.
    */
   public static final int URS_RERATE_FLAG = 51;
   
   /**
    * OCG Result code
    */
   public static final int URS_OCG_RESULT_CODE = 52;
   
   /**
     * The running billable call duration in seconds to this point. If the event type is
     * Call Ended Event (2), this represent the total billable call duration. Billalble
     * Call Duration represents Call Duration that rounded to the nearest billing
     * interval. It is equals or larger than Call Duration.
     */
    public static final int URS_BILLABLE_CALL_DURATION = 53;
       
    /**
     * Description Associated to the rate rule.
     */
    public static final int URS_RATE_RULE_DESCRIPTION = 54;
 
    /**
     * Subscription type
     */
    public static final int URS_SUBSCRIPTION_TYPE = 55;

    /**
     * BAN (SubscriberID on URCS) associated to the subscription.
     */
    public static final int URS_BAN_ID = 56;
    
    /**
     * The Rate Rule Identifier that was applied to determine the rate. The rate rule can
     * be looked by from URS Rate Table using this ID.
     */
    public static final int URS_RATE_RULE_ID = 57;
    
    /**
     * The Rate Rule Identifier that was applied to determine the rate. The rate rule can
     * be looked by from URS Rate Table using this ID.
     */
    public static final int URS_CHARGING_COMPONENTS = 58;
    
   ///////////////////////////
   // IPCG
   // index 0 is the ER class

   /** ER field index */
   public static final int IPCGW_MSISDN = 3;

   /** ER field index */
   //public static final int IPCGW_SCP_ID_VERSION_PRE35 = 14;
   //public static final int IPCGW_SCP_ID_VERSION_35 = 11;
   public static final int IPCGW_SCP_ID = 11;

   /** ER field index */
   //public static final int IPCGW_TRANSACTION_DATE_PRE35 = 15;
   //public static final int IPCGW_TRANSACTION_DATE_35 = 14;
   public static final int IPCGW_TRANSACTION_DATE = 14;

   /** ER field index */
   //public static final int IPCGW_TRANSACTION_TIME_PRE35 = 16;
   //public static final int IPCGW_TRANSACTION_TIME_35 = 15;
   public static final int IPCGW_TRANSACTION_TIME = 15;

   /** ER field index */
   //public static final int IPCGW_PROTOCOL_HTTP_PRE35 = 17;
   //public static final int IPCGW_PROTOCOL_HTTP_35 = 16;
   public static final int IPCGW_PROTOCOL_HTTP = 16;

   /** ER field index offset value */
   public static final int IPCGW_OFFSET_CHARGE = 3;

   /** ER field index offset value */
   public static final int IPCGW_OFFSET_VOLUME_DOWN = 4;

   /** ER field index offset value */
   public static final int IPCGW_OFFSET_VOLUME_UP = 5;

   /** ER field index offset value */
   public static final int IPCGW_OFFSET_EVENT_COUNTER = 6;

   /** ER field index offset value */
   public static final int IPCGW_OFFSET_URL_INFO = 8;

   ///////////////////////////
   // BMEvent 1308
   // index 0 is the ER Date

   /** ER field index */
   public static final int BM_BUCKET_END_DATE = 0;
   /** ER field index */
   public static final int BM_SPID = 2;
   /** ER field index */
   public static final int BM_MSISDN = 4;
   /** ER field index */
   public static final int BM_BUNDLE_ID = 5;
   /** ER field index */
   public static final int BM_BUCKET_ID = 6;
   /** ER field index */
   public static final int BM_CATEGORY_ID = 7;
   /** ER field index */
   public static final int BM_PAST_FCT_AWARDED = 8;
   /** ER field index */
   public static final int BM_PAST_FCT_USED = 9;
   /** ER field index */
   public static final int BM_PAST_ROLLOVER_FCT_AWARDED = 10;
   /** ER field index */
   public static final int BM_PAST_ROLLOVER_FCT_USED = 11;
   /** ER field index */
   public static final int BM_PAST_GROUP_FCT_USED = 12;
   /** ER field index */
   public static final int BM_ROLLOVER_FCT = 13;
   /** ER field index */
   public static final int BM_PAST_GROUP_ROLLOVER_FCT_USED = 14;
   /** ER field index */
   public static final int BM_FCT_AWARDED = 15;
   /** ER field index */
   public static final int BM_PAST_ROLLOVER_EXPIRED = 16;
   /** ER field index */
   public static final int BM_EVENT_REASON = 17;
   /** ER field index */
   public static final int BM_BUCKET_START_DATE = 18;
   /** ER field index */
   public static final int BM_RESULT_CODE = 19;
   /** ER result code error code */
   public static final int BM_SQL_ERROR = 202;
   /** ER result code error code */
   public static final int BM_UNKNOWN_ERROR = 104;
   /** BM rollover event reason code */
   public static final int BM_RESERVED = 0;
   public static final int BM_ROLLOVER_EVENT = 1;
   public static final int BM_EXPIRY_EVENT = 2;
   public static final int BM_SUBSCRIBER_BUCKET_REMOVAL_EVENT = 3;
   public static final int BM_PURGE_EVENT = 4;





   ///////////////////////////
   // IPCG
   // Call Type Indices

   public static final int CALLTYPE_DOWNLOAD_INDEX = 0;
   public static final int CALLTYPE_WEB_INDEX      = 1;
   public static final int CALLTYPE_WAP_INDEX      = 2;
   public static final int CALLTYPE_MMS_INDEX      = 3;
   public static final int CALLTYPE_SDR_INDEX      = 4;

   // SMSB
   public static final int ER_PREPAID =1;
   public static final int ER_POSTPAID = 2;
   public static final String ER_MO_SMS = "O";
   public static final String ER_MT_SMS = "T";


   /*
    * MCommerce
    */
   public static final int MCOMMERCE_MSISDN_INDEX = 3;
   public static final int MCOMMERCE_RECHARGE_VALUE_INDEX = 8;
   public static final int MCOMMERCE_FINAL_BALANCE_INDEX = 12;

   /*
    * OCG 375
    */
   public static final int OCG375_MSISDN_INDEX = 3;
   public static final int OCG375_TRANS_DATE_INDEX = 4;
   public static final int OCG375_ADJ_TYPE_INDEX = 5;
   public static final int OCG375_ADJ_DESC_INDEX = 6;
   public static final int OCG375_CHARGING_AMT_INDEX = 7;
   public static final int OCG375_EXTERNAL_TRANS_ID_INDEX = 8;
   public static final int OCG375_NEWBALANCE_AMT_INDEX = 10;
   public static final int OCG375_EXPIRY_EXTENSION_INDEX = 11;
	public static final int OCG375_SUBSCRIPTION_TYPE_INDEX = 12;
	public static final int OCG375_AGENT_INDEX = 13;
	public static final int OCG375_TRANSACTION_METHOD_INDEX = 14;
	public static final int OCG375_PAYMENT_DETAILS_INDEX = 15;
	public static final int OCG375_BAN_INDEX = 16;
	public static final int OCG375_SUBSCRIPTION_ID_INDEX = 17;
	public static final int OCG375_PAYMENT_AGENCY_INDEX = 18;
	public static final int OCG375_LOCATION_CODE_INDEX = 19;

   /*
    * FnF_SELFCARE ER1900 fields Indexes
    */
   public static final int FnF_SELFCARE_ER1900_SPID_INDEX = 2;
   public static final int FnF_SELFCARE_ER1900_OPERATIONCODE_INDEX = 3;
   public static final int FnF_SELFCARE_ER1900_RESULTCODE_INDEX = 4;
   public static final int FnF_SELFCARE_ER1900_ORIGDN_INDEX = 5;
   public static final int FnF_SELFCARE_ER1900_CUGOWNERDN_INDEX = 6;
   public static final int FnF_SELFCARE_ER1900_TARGETSUBDN_INDEX = 7;
   public static final int FnF_SELFCARE_ER1900_CUGTEMPLATEID_INDEX = 8;
   public static final int FnF_SELFCARE_ER1900_CUGID_INDEX = 9;
   public static final int FnF_SELFCARE_ER1900_CUGNAME_INDEX = 10;
   public static final int FnF_SELFCARE_ER1900_OCGRESERVEDID_INDEX = 11;
   public static final int FnF_SELFCARE_ER1900_OCGCHARGEDAMOUNT_INDEX = 12;
   public static final int FnF_SELFCARE_ER1900_OCGRESULTCODE_INDEX = 13;
   public static final int FnF_SELFCARE_ER1900_FnFAPIRESULTCODE_INDEX = 14;
   public static final int FnF_SELFCARE_ER1900_PLPID_INDEX = 16;
   public static final int FnF_SELFCARE_ER1900_PLPNAME_INDEX = 17;
   public static final int FnF_SELFCARE_ER1900_SERVICETYPE_INDEX = 18;
   public static final int FnF_SELFCARE_ER1900_CRMSERVICEFEE_INDEX = 20;
   public static final int FnF_SELFCARE_ER1900_CRMTAXAUTHORITY_INDEX = 21;
   public static final int FnF_SELFCARE_ER1900_CRMGLCODE_INDEX = 22;
   public static final int FnF_SELFCARE_ER1900_SMARTSUSPENSION_INDEX = 23;
   public static final int FnF_SELFCARE_ER1900_FULLorPRORATE_INDEX = 24;

   /*
    * FnF_SELFCARE CUG 1900 operation Code Constant
    *
    */
   public static final int FnF_SELFCARE_OPCODE_RESERVED = 0;
   public static final int FnF_SELFCARE_OPCODE_CREATEOWNER_CUG = 1;
   public static final int FnF_SELFCARE_OPCODE_ADDSUBSCRIBERTOCUG = 2;
   public static final int FnF_SELFCARE_OPCODE_REMOVESUBSCRIBERFROMCUG = 3;
   public static final int FnF_SELFCARE_OPCODE_ATTACHPLP = 4;
   public static final int FnF_SELFCARE_OPCODE_DETACHPLP = 5;
   public static final int FnF_SELFCARE_OPCODE_REMOVECUG = 6;
   public static final int FnF_SELFCARE_OPCODE_CREATEPLPTEMPLATE = 7;
   public static final int FnF_SELFCARE_OPCODE_REMOVEPLPTEMPLATE = 8;

   /*
    * FnF_SELFCARE CUG 1900 ResultCode Description
    *
    */
   public static final int FnF_SELFCARE_RESULTCODE_SUCCESS = 0;
   public static final int FnF_SELFCARE_RESULTCODE_CUGALREADYEXIST = 200;
   public static final int FnF_SELFCARE_RESULTCODE_SUBNOTFOUND = 201;
   public static final int FnF_SELFCARE_RESULTCODE_INVALIDCUGTEMPLATEID = 202;
   public static final int FnF_SELFCARE_RESULTCODE_CREATECUGFAILED = 203;
   public static final int FnF_SELFCARE_RESULTCODE_CUGDATANOTFOUND = 204;
   public static final int FnF_SELFCARE_RESULTCODE_CUGSIZEEXCEEDED = 205;
   public static final int FnF_SELFCARE_RESULTCODE_ADDSUBSCRIBERFAILED = 206;
   public static final int FnF_SELFCARE_RESULTCODE_NOTENOUGHBALANCE = 207;
   public static final int FnF_SELFCARE_RESULTCODE_REMOVESUBFAILED = 208;
   public static final int FnF_SELFCARE_RESULTCODE_BADPARAMS = 209;
   public static final int FnF_SELFCARE_RESULTCODE_INTERNALERROR = 210;
   public static final int FnF_SELFCARE_RESULTCODE_INVALIDDATERANGE = 211;
   public static final int FnF_SELFCARE_RESULTCODE_PLPINUSE = 212;
   public static final int FnF_SELFCARE_RESULTCODE_PLPNOUPDATE = 213;

   /*
    * PPSM Screening Template Removal ER7018
    */
   public static final int PPSM_SCREENING_TEMPLATE_REMOVAL_SPID_INDEX = 2;
   public static final int PPSM_SCREENING_TEMPLATE_REMOVAL_TEMPLATE_ID_INDEX = 3;
   public static final int PPSM_SCREENING_TEMPLATE_REMOVAL_TEMPLATE_NAME_INDEX = 4;
   public static final int PPSM_SCREENING_TEMPLATE_REMOVAL_REFERENCE_ID_INDEX = 5;

   public static final int ER_DATE_START_POS = 0;
   public static final int ER_DATE_LENGTH = 10;
   
   /*
    * Bundle Depletion ER1338
    */
   public static final int BUNDLE_DEPLETE_SPID = 2;
   public static final int BUNDLE_DEPLETE_BAN = 3;
   public static final int BUNDLE_DEPLETE_MSISDN = 4;
   public static final int BUNDLE_DEPLETE_BUCKET_ID = 5;
   public static final int BUNDLE_DEPLETE_BUNDLE_ID = 6;

}

