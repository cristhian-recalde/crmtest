package com.trilogy.app.crm.hlr.bulkload;

public interface SimcardInput 
{
	public String mapToCommand(final String cmd);
	public String getRawline(); 
	public void setRawline(String s); 
	public void parse() throws InvalidInputException; 
	
}
