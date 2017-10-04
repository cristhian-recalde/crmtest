package com.trilogy.app.crm.bas.roamingcharges;

import java.text.SimpleDateFormat;

public class Constants
{	
	public static final String ROAMING_FILENAME="ROAMING_FILENAME";
	public static final String TOTAL_CHARGE="TOTAL_CHARGE";
	public static final String APPLY_TAX_AMOUNT="APPLY_TAX_AMOUNT";
	public static final String BAN="BAN";
	public static final String POSTED_DATE_STRING="POSTED_DATE_STRING";
	public static final String TRANSACTION_DATE_STRING="TRANSACTION_DATE_STRING";
	public static final String TRANSACTION_TIME_STRING="TRANSACTION_TIME_STRING";
	public static final String TRANSACTION_DATE="TRANSACTION_DATE";
	public static final String TRANSACTION_TIME="TRANSACTION_TIME";
	public static final String TRANSACTION_TYPE="TRANSACTION_TYPE";
	public static final String POSTED_DATE="POSTED_DATE";
	public static final String POSTED_TIME="POSTED_TIME";
	public static final String CHARGED_MSISDN="CHARGED_MSISDN";
	public static final String ORIGINATING_MSISDN="ORIGINATING_MSISDN";
	public static final String DESTINATION_MSISDN="DESTINATION_MSISDN";
	public static final String IMSI="IMSI";
	public static final String CALLING_PARTY_LOCATION="CALLING_PARTY_LOCATION";
	public static final String DURATION="DURATION";
	public static final String FLAT_RATE="FLAT_RATE";
	public static final String VARIABLE_RATE="VARIABLE_RATE";
	public static final String VARIABLE_RATE_UNIT="VARIABLE_RATE_UNIT";
	public static final String CHARGE="CHARGE";
	public static final String CURRENCY="CURRENCY";
	public static final String USED_MONTHLY_BUCKET_MINUTES="USED_MONTHLY_BUCKET_MINUTES";
	public static final String SPID="SPID";
	public static final String RATE_PLAN="RATE_PLAN";
	public static final String RATING_RULE="RATING_RULE";
	public static final String MSC_CALL_REFERENCE_ID="MSC_CALL_REFERENCE_ID";
	public static final String DISCONNECT_REASON="DISCONNECT_REASON";
	public static final String BILLING_CATEGORY="BILLING_CATEGORY";
	public static final String GLCODE="GLCODE";
	public static final String ADJUSTMENT_TYPE="ADJUSTMENT_TYPE";
	public static final String PLMN_CODE="PLMN_CODE";
	public static final String TAX_AMOUNT="TAX_AMOUNT";
	
	public static final int CALL_TYPE_MOC=1;
	public static final int CALL_TYPE_MTC=2;
	public static final int CALL_TYPE_SMS=3;
	
	public static final String SERV_CODE_22="22";
	public static final String SERV_CODE_21="21";
	
	public static final String DATE_FORMAT_STRING = "yyyyMMdd";

	public static final String ER_ROAMING_CHARGE_EVENT= "Roaming Charge Event";
	public static final String ER_INVALID_ROAMING_ENTRY_EVENT= "Invalid Roaming Entry Event";
	public static final String ER_INVALID_ROAMING_FILE_EVENT= "Invalid Roaming File Event";
	
	public static final String RESULT_SUCCESS = "0";	
	public static final String RESULT_DUPLICATE_ENTRY = "800";
	public static final String RESULT_GEN_ERR = "9999";
	
	public static final String BILL_CAT_ROAMING_OUT = "01";
	public static final String BILL_CAT_ROAMING_IN = "02";
	public static final String BILL_CAT_ROAMING_SMS = "03";
	public static final String BILL_CAT_ROAMING_TAX = "04";
	
	public static String ADJUSTMENT_TYPE_ROAMING_CHARGES = "Roaming Deposit";
	
	public static final int SUCCESS=0;
	public static final int FAILURE=-1;
	
	public static final String CALL_TYPE="CALL_TYPE";
}
