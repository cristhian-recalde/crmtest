package com.trilogy.app.crm.ff;

public class FFEcareException 
extends Exception 
{

	public FFEcareException(String arg0, int resultCode, Throwable arg1) {
		super(arg0, arg1);
		resultCode_ = resultCode;
	}

	public FFEcareException(String arg0, int resultCode) {
		super(arg0);
        resultCode_ = resultCode;
	}

	public FFEcareException(Throwable arg0, int resultCode) {
		super(arg0);
        resultCode_ = resultCode;
	}
	
	public int getResultCode()
	{
	    return resultCode_;
	}
	
	private int resultCode_;

}
