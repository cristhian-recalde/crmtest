package com.trilogy.app.crm.client.ipcg;

public class IpcgRatingProvException extends Exception 
{
	int tag;
	String reason;
	public IpcgRatingProvException() 
	{
		super();
	}
	
	public IpcgRatingProvException(String message,int tag) 
	{
		super(message);
		reason=message;
		this.tag=tag;
	}
	
	public IpcgRatingProvException(String message, int tag,Throwable cause) 
	{
		super(message, cause);
		reason=message;
		this.tag=tag;
	}
	
	public IpcgRatingProvException(Throwable cause) 
	{
		super(cause);		
	}
	
	public IpcgRatingProvException(String message, Throwable cause) 
	{
		super(message, cause);
		reason=message;
	}
	
	public IpcgRatingProvException(String string) {
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

