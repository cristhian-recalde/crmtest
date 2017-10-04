package com.trilogy.app.crm.clean;

import com.trilogy.framework.xhome.context.AgentException;

/**
 * Exception thrown by the cleanup agents
 */
public class CleanupAgentException
	extends AgentException
{
	/**
	 * name of the SMSB module
	 */
	public static final String SMSB = "AppSmsb";
	/**
	 * name fo the ECP module
	 */
	public static final String ECP = "AppEcp";
	/**
	 * name of the HLR module
	 */
	public static final String HLR = "Hlr";
    /**
    * name of the UPS module
    */
   public static final String UPS = "UPS";
    /**
    * name of the BAS module
    */
   public static final String BAS = "BAS";

	private int resultCode_ = 0;
	private String key_;
	private int srcResultCode_ = 0;

	/**
    * Generic data copying constructor
    * 
	 * @param msg
	 * @param srcResultCode
	 * @param resultCode
	 * @param key
	 */
	public CleanupAgentException(String msg, int srcResultCode, int resultCode, String key)
	{
		super("CleanUp result " + resultCode + ": fail to clean " + key + " due to " + msg + "(" + srcResultCode + ")");
		setResultCode(resultCode);
		setKey(key);
		setSourceResultCode(srcResultCode);
	}
   
	/**
    * returns the operation result code
	 * @return
	 */
	public int getResultCode()
	{
		return resultCode_;
	}

	/**
    * Setter for the operation result code
	 * @param resultCode
	 */
	public void setResultCode(int resultCode)
	{
		resultCode_ = resultCode;
	}

	/**
    * Getter for the key
	 * @return
	 */
	public String getKey()
	{
		return key_;
	}

	/**
    * Setter for the key
	 * @param key
	 */
	public void setKey(String key)
	{
		key_ = key;
	}

	/**
    * Getter for the source result code
	 * @return
	 */
	public int getSourceResultCode()
	{
		return srcResultCode_;
	}

	/**
    * Setter for the source result code
	 * @param srcResultCode
	 */
	public void setSourceResultCode(int srcResultCode)
	{
		srcResultCode_ = srcResultCode;
	}
}
