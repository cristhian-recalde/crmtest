package com.trilogy.app.crm.client.ipcg;

public class IpcgSubProvException extends Exception 
{

	int tag;
	String reason;
	public IpcgSubProvException() 
	{
		super();
	}

	public IpcgSubProvException(String message,int tag) 
	{
		super(message);
		reason=message;
		this.tag=tag;
	}

	public IpcgSubProvException(Throwable cause) 
	{
		super(cause);		
	}

	public IpcgSubProvException(String message, Throwable cause) 
	{
		super(message, cause);
		reason=message;
	}

	public IpcgSubProvException(String string) {
		super(string);
		reason=string;
	}

	public String getReason() {
		return reason;
	}

	public int getTag() {
		return tag;
	}
	
	public int getResult() {
		return tag;
	}

}
