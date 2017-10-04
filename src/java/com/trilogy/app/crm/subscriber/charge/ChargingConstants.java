package com.trilogy.app.crm.subscriber.charge;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.bean.SubscriberStateEnum;

public interface ChargingConstants {
    
    // used in subscriber provisioning charger to identify which change triggered 
    // charger to act. 
    public static final int PROVISIONING_CHARGE_ACTION_TYPE_NOT_APPLICABLE = 0; 
    public static final int PROVISIONING_CHARGE_ACTION_TYPE_CREATE = 1; 
    public static final int PROVISIONING_CHARGE_ACTION_TYPE_SIMPLE = 2; 
    public static final int PROVISIONING_CHARGE_ACTION_TYPE_PPV_CHANGE = 3; 
    public static final int PROVISIONING_CHARGE_ACTION_TYPE_STATE_CHANGE =4; 
    public static final int PROVISIONING_CHARGE_ACTION_TYPE_MULTI = 5; 
    public static final int PROVISIONING_CHARGE_ACTION_TYPE_MOVE = 6; 

    // used by support class to calculation amount, and handler to decide 
    // what to do 
    public static final int ACTION_PROVISIONING_CHARGE = 1;
    public static final int ACTION_RECURRING_CHARGE =100;
    public static final int ACTION_UNSUSPENDING_CHARGE =200;
    public static final int ACTION_BUNDLE_OVERUSAGE_CHARGE = 300; 
    public static final int ACTION_PROVISIONING_REFUND = 1000;
    public static final int ACTION_UNPROVISIONING_REFUND = 1100;
    public static final int ACTION_UNPROVISIONING_CHARGE = 400;
   
    public static final Map  ACTION_TYPE_NAMES =  ActionTypes.getMap();
    
    final class ActionTypes {
        private static Map getMap() {
            final HashMap map = new HashMap();
            map.put(new Integer(ACTION_PROVISIONING_CHARGE),   "charge for provisioning");
            map.put(new Integer(ACTION_PROVISIONING_REFUND),   "refund for provisioning");
            map.put(new Integer(ACTION_RECURRING_CHARGE),   "recurring charge");
            map.put(new Integer(ACTION_UNSUSPENDING_CHARGE),   "charge for unsuspending");
            map.put(new Integer(ACTION_BUNDLE_OVERUSAGE_CHARGE),   "charge for bundle over usage");
            map.put(new Integer(ACTION_UNPROVISIONING_CHARGE),   "charge for pay in arrear serv at un prov");
            return map;
        }
    }    
     
    /*
     * the result code for single transation 
     */
    public static final int TRANSACTION_SUCCESS = 0; 
    public final static int TRANSACTION_VALIDATION_SUCCESS =10;
    
    public static final int TRANSACTION_FAIL_UNKNOWN =1000; 
    public static final int TRANSACTION_FAIL_OCG=1010; 
    public static final int TRANSACTION_FAIL_DATA_ERROR = 1020;
    
    public static final int TRANSACTION_SKIPPED_SUSPEND =2000;
    public static final int TRANSACTION_SKIPPED_UNSUPPORTED_TYPE=2010; 
    public final static int TRANSACTION_SKIPPED_DUPLICATE_CHARGE =2020;
    public final static int TRANSACTION_SKIPPED_DUPLICATE_REFUND =2030;
    public final static int TRANSACTION_SKIPPED_NO_CHARGE_IN_BILLING_CYCLE =2040;
    public static final int TRANSACTION_SKIPPED_ONE_TIME=2050;
    public static final int TRANSACTION_SKIPPED_IN_PACKAGE=2060;
    public static final int TRANSACTION_SKIPPED_OVERUSAGE=2070; 
    public static final int TRANSACTION_SKIPPED_NO_REFUND=2080;
    public static final int TRANSACTION_SKIPPED_MULTISIM_PER_SIM=2090;
    public static final int TRANSACTION_SKIPPED_MULTISIM_PER_SERVICE=2091;
    // not transaction for external msisdn for normal cug
    public static final int TRANSACTION_SKIPPED_CUG_EXTERNAL_MSISDN=2080;

    public static final int TRANSACTION_REDIRECT_ONE_TIME=5000;  
    public static final int TRANSACTION_REDIRECT_PCUG=5010; 
    public static final int TRANSACTION_REDIRECT_VPN_GROUP=5020; 
    
    public static final Map  TRANSACTION_RETURN_NAMES =  TransactionReturnTypes.getMap();
    
    final class TransactionReturnTypes {
        private static Map getMap() {
            final HashMap map = new HashMap();
            map.put(new Integer(TRANSACTION_SUCCESS),  "Success" );
            map.put(new Integer(TRANSACTION_FAIL_UNKNOWN),  "Unknown error" );
            map.put(new Integer(TRANSACTION_FAIL_OCG), "OCG error" );
            map.put(new Integer(TRANSACTION_FAIL_DATA_ERROR),  "Data error" );
            map.put(new Integer(TRANSACTION_SKIPPED_SUSPEND), "Skipped due to suspension" );
            map.put(new Integer(TRANSACTION_SKIPPED_UNSUPPORTED_TYPE), "Skipped unknown type"  );
            map.put(new Integer(TRANSACTION_VALIDATION_SUCCESS),  "Validation success" );
            map.put(new Integer(TRANSACTION_SKIPPED_DUPLICATE_CHARGE), "Skipped for duplicate charge" );
            map.put(new Integer(TRANSACTION_SKIPPED_DUPLICATE_REFUND), "Skipped for duplicate refund"  );
            map.put(new Integer(TRANSACTION_SKIPPED_NO_CHARGE_IN_BILLING_CYCLE), "Skipped refund for no charge in billing cycle " );
            map.put(new Integer(TRANSACTION_REDIRECT_ONE_TIME),  "Skipped due to one time charge" );
            map.put(new Integer(TRANSACTION_SKIPPED_IN_PACKAGE), "Skipped due to include in pacakge"  );
            map.put(new Integer(TRANSACTION_SKIPPED_OVERUSAGE), "Skipped for overusage" );
            map.put(new Integer(TRANSACTION_REDIRECT_PCUG), "PCUG charge" );
            map.put(new Integer(TRANSACTION_REDIRECT_VPN_GROUP), "VPN group charge" );
            map.put(Integer.valueOf(TRANSACTION_SKIPPED_ONE_TIME), "Skipped due to one time chargeable entity");
            
            return map;
        }
    }    
    
    
    public static final int FULL_CHARGE = 1; 
    public static final boolean IS_ACTIVATION_MOVE = false; 
    
    
    // The chargable item type is mainly for handlers. 
    public static final int CHARGABLE_ITEM_SERVICE = 0; 
    public static final int CHARGABLE_ITEM_BUNDLE = 1; 
    public static final int CHARGABLE_ITEM_AUX_SERVICE = 2;  
    public static final int CHARGABLE_ITEM_PACKAGE = 3;

    
    /**
     *  this is the code for charge transation stage
     */
    public static final int RUNNING_SUCCESS = 0; 
    public static final int RUNNING_CONTINUE_SUSPEND = 100;
    public static final int RUNNING_ERROR_STOP = 200;
 
    
    /**
     * this result code for calculation stage
     */
    public static final int CALCULATION_SUCCESS = 0; 
    public static final int CALCULATION_WITH_ERROR = 1; 
    public static final int CALCULATION_SKIPPED = 2; 
    public static final int CALCULATION_PREDATED_DUNNING = 3; 
    
    
    /**
     * this section is the result code returned in chargeAndrefund, recurringCharge method
     */
    public static final int OPERATION_SUCCESS = 0; 
    public static final int OPERATION_ERROR = 1; 
    public static final int OPERATION_NOT_SUPPORT = 2; 
    
    
    /**
     * this section is the result code of handler, the caller could need the result 
     * in order to decide what to do next. such as continue or stop the whole process. 
     * For example, if one service charge fail it could trigger suspension of all the 
     */
    public static final int ERROR_HANDLE_RESULT_SUCCESS = 0; 
    public static final int ERROR_HANDLE_RESULT_FAIL_SUSPEND = 1;
    public static final int ERROR_HANDLE_RESULT_FAIL_FIND_PACK = 2; 
    
    
    public final static String[] CHARGABLE_ITEM_NAMES = {"Service", "Bundle", "Auxiliary Service", "Package"};  
    
    
    public final static int CHARGER_TYPE_POST_PROVISION_SUBSCRIBER =0;
    public final static int CHARGER_TYPE_PRE_PROVISION_SUBSCRIBER =1;
    public final static int CHARGER_TYPE_RECURRING_CHARGE_SUBSCRIBER =2;
    public final static int CHARGER_TYPE_SUBSCRIBER_AUX_SERVICER =3;
    public final static int CHARGER_TYPE_CUG =4;
    public final static int CHARGER_TYPE_UNSUSPENDING_SUBSCRIBER =5;
    public final static int CHARGER_TYPE_MOVE_SUBSCRIBER =6;
    public final static int CHARGER_TYPE_DUMMY =7;
    public final static int CHARGER_TYPE_PRIVATE_CUG =8;
    public final static int CHARGER_TYPE_SUBSCRIBER_SERVICER =9;
    public final static int CHARGER_TYPE_CHANGE_BILL_CYCLE =10;

    public final static int SUBSCRIBER_STATE_ACTION_NO_ACTION=0; 
    public final static int SUBSCRIBER_STATE_ACTION_CHARGE_ALL=1; 
    public final static int SUBSCRIBER_STATE_ACTION_CHARGE_EXCLUDE_SMART_SUSPENSION=2; 
    public final static int SUBSCRIBER_STATE_ACTION_REFUND_ALL=3; 
    public final static int SUBSCRIBER_STATE_ACTION_REFUND_INCLUDE_SMART_SUSPENSION=4; 
    public final static int SUBSCRIBER_STATE_ACTION_REFUND_EXCLUDE_SMART_SUSPENSION=5; 
    
    
   public final static  int[] POSTPAID_RECURRING_CHARGE_SUBCRIBER_STATES= new int[] 
   {
	   SubscriberStateEnum.ACTIVE_INDEX,  
	   SubscriberStateEnum.NON_PAYMENT_WARN_INDEX,
	   SubscriberStateEnum.NON_PAYMENT_SUSPENDED_INDEX,
	   SubscriberStateEnum.PROMISE_TO_PAY_INDEX,
   };

    
   public final static short CHARGING_CYCLE_WEEKLY =0; 
   public final static short CHARGING_CYCLE_MONTHLY=1;
   public final static short CHARGING_CYCLE_ONETIME= 2;
   public final static short CHARGING_CYCLE_ANNUAL=3; 
   public final static short CHARGING_CYCLE_MULTIMONTHLY=4;
   public final static short CHARGING_CYCLE_DAILY=5;
   public final static short CHARGING_CYCLE_CONFIGURABLE=6; 
   public final static short CHARGING_CYCLE_MULTIDAY=7; 
   
   public final static String IS_RECURRING_RECHARGE = "IS_RECURRING_RECHARGE";
   
   public static Boolean IS_REFUND_TRANSACTION = false;
   
}
