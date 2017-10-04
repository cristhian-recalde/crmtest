package com.trilogy.app.crm.hlr.bulkload;

public class InvalidInputException 
extends Exception 
{
	public InvalidInputException(String s, String rawline)
	{
		super(s); 
		this.rawLine = rawline; 
	}
	
	public String getRawLine()
	{
		return rawLine; 
	}
	
	String rawLine; 
}
