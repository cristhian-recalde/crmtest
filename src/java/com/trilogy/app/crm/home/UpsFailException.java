/*
 * Created on Feb 19, 2004
 *
 */
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.home.HomeException;

/**
 * @author dzhang
 *
 */
public class UpsFailException extends HomeException
{
	
	private int errorCode_ = 0;
	
	public UpsFailException( String msg, int errorCode)
	{
		super(msg);
		setErrorCode(errorCode);
	}

	/**
	 * Returns the errorCode.
	 * @return int
	 */
	public int getErrorCode() {
		return errorCode_;
	}

	/**
	 * Sets the errorCode.
	 * @param errorCode The errorCode to set
	 */
	public void setErrorCode(int errorCode) {
		this.errorCode_ = errorCode;
	}

}
