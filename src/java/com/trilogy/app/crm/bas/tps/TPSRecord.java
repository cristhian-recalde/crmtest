package com.trilogy.app.crm.bas.tps;

import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;

public class TPSRecord 
extends AbstractTPSRecord
{
	
    public Account getAccount() {
		return account;
	}
	public void setAccount(Account account) {
		this.account = account;
	}
	public Subscriber getSubscriber() {
		return subscriber;
	}
	public void setSubscriber(Subscriber subscriber) {
		this.subscriber = subscriber;
	}
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	public Throwable getExceptionCaught() {
		return exceptionCaught;
	}
	public void setExceptionCaught(Throwable exceptionCaught) {
		this.exceptionCaught = exceptionCaught;
	}
	public String getSubscriberList() {
		return subscriberList;
	}
	public void setSubscriberList(String subscriberList) {
		this.subscriberList = subscriberList;
	}


	public Account getResponsibleAcct() {
		return responsibleAcct;
	}
	public void setResponsibleAcct(Account responsibleAcct) {
		this.responsibleAcct = responsibleAcct;
	}

	public PrefixMapping getPrefixMapping() {
		return prefixMapping;
	}
	public void setPrefixMapping(PrefixMapping prefixMapping) {
		this.prefixMapping = prefixMapping;
	} 
	public AdjustmentType getAdjType() {
		return adjType;
	}
	public void setAdjType(AdjustmentType adjType) {
		this.adjType = adjType;
	}
	
    public String getMsisdn()
    {
    	if (prefixMapping != null )
    	{
    		return prefixMapping.getPrefix() + this.getTelephoneNum(); 
    	}
    	
    	return this.getTelephoneNum(); 
    }
	
    
    
    
	public String getTpsFileName() {
		return tpsFileName;
	}
	public void setTpsFileName(String tpsFileName) {
		this.tpsFileName = tpsFileName;
	}




	public String getLastError() {
		return lastError;
	}
	public void setLastError(String lastError) {
		this.lastError = lastError;
	}
	public String getRawline() {
		return rawline;
	}
	public void setRawline(String rawline) {
		this.rawline = rawline;
	}


	public boolean getVoidFlag() {
		return voidFlag;
	}
	public void setVoidFlag(boolean voidFlag) {
		this.voidFlag = voidFlag;
	}
	public Date getExportDate() {
		return ExportDate;
	}
	public void setExportDate(Date exportDate) {
		ExportDate = exportDate;
	}
	public String getExtra() {
		return Extra;
	}
	public void setExtra(String extra) {
		Extra = extra;
	}



	public boolean isAccountLevel() {
		return accountLevel;
	}
	public void setAccountLevel(boolean accountLevel) {
		this.accountLevel = accountLevel;
	}




	Account account=null; 
	Subscriber subscriber = null; 
	int result = TPSPipeConstant.RESULT_CODE_SUCCESS; 
	Throwable exceptionCaught =null; 
	String subscriberList = "";
	Account responsibleAcct = null;
	PrefixMapping prefixMapping = null;
	AdjustmentType adjType = null; 
	String tpsFileName=""; 
	String lastError=""; 
	String rawline=""; 
	boolean voidFlag =false;
    Date ExportDate = null; 
    String Extra = ""; 
    boolean accountLevel = false; 
	
}
