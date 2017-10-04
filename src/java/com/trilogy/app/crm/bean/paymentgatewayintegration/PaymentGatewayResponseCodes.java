package com.trilogy.app.crm.bean.paymentgatewayintegration;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.trilogy.product.s2100.ErrorCode;


/**
 * Payment Gateway Integration API response codes.
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public class PaymentGatewayResponseCodes {

	public static final int SUCCESS = 0;
	public static final int NO_SUCH_BAN = 1;
	public static final int NO_SUCH_SUBSCRIPTION = 2;
	public static final int NON_RESPONSIBLE_ACCOUNT = 3;	
	public static final int INVALID_PARAMETER = 4;
	public static final int NO_SUCH_TOKEN_ID = 5;
	public static final int NO_SUCH_TOKEN_VALUE = 6;
	public static final int NO_SUCH_SCHEDULE_ID = 7;
	public static final int NO_SCHDEULE_CREATION_ALLOWED = 8;
		
	/**
	 * applyPaymentCharge result codes 
	 */
	
	public static final int PGW_CHARGE_SUCCEEDED = 0;
	public static final int PGW_CHARGE_FAILED = 33;
	public static final int NO_CHARGE_ATTEMPT_BSS_INTERNAL_ERROR = 34;
	public static final int OCG_INTERNAL_ERROR = 35;  
	public static final int OCG_TIMEOUT = 36;

	
	public static String resolveOcgMessage(short ocgResultCode )
	{
		return resultCodeMessageMap_.get(ocgResultCode);
	}
	private static Map<Short, String> resultCodeMessageMap_ = new HashMap<Short, String>();
	static 
	{
		initOcgMessageMap();
	}
	
	private static void initOcgMessageMap()
	{
		for(Field field : ErrorCode.class.getFields() )
		{
			try
			{
				resultCodeMessageMap_.put(field.getShort(field), field.getName());
			}catch (Exception e) 
			{
				//ignore
			}
			
		}
	}
}
