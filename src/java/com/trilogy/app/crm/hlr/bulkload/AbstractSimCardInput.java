package com.trilogy.app.crm.hlr.bulkload;

public abstract class AbstractSimCardInput 
implements SimcardInput
{

	public String getImsi() {
		return imsi;
	}
	public void setImsi(final String imsi) {
		this.imsi = imsi;
	}
	
	public String getRawline()
	{
		return this.rawline;
	}
	public void setRawline(String s)
	{
		this.rawline = s;
	}
	
	String imsi="";
	String rawline  = "";
}
