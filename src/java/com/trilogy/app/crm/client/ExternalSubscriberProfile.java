package com.trilogy.app.crm.client;

public class ExternalSubscriberProfile {
	  
	  public String msisdn = null;
	  public String groupMsisdn = null;
	  public int spid = 0;
	  public String currency = null;
	  public int creditLimit = 0;
	  public int balance = 0;
	  public long expiryDate = 0L;
	  public String tzOffset = null;
	  public int billingID = 0;
	  public int activationExtension = 0;
	  public int state = 0;
	  public long activationDate = 0L;
	  public int groupQuota = 0;
	  public int groupUsage = 0;

	  public ExternalSubscriberProfile ()
	  {
	  } 

	  public ExternalSubscriberProfile (
	          String _msisdn, 
	          String _groupMsisdn, 
	          int _spid, 
	          String _currency, 
	          int _creditLimit, 
	          int _balance, 
	          long _expiryDate, 
	          String _tzOffset, 
	          int _billingID, 
	          int _activationExtension, 
	          int _state,
	          long _activationDate,
	          int _groupQuota,
	          int _groupUsage)
	  {
	    msisdn = _msisdn;
	    groupMsisdn = _groupMsisdn;
	    spid = _spid;
	    currency = _currency;
	    creditLimit = _creditLimit;
	    balance = _balance;
	    expiryDate = _expiryDate;
	    tzOffset = _tzOffset;
	    billingID = _billingID;
	    activationExtension = _activationExtension;
	    state = _state;
	    activationDate = _activationDate;
	    groupQuota = _groupQuota;
        groupUsage = _groupUsage;
	  }
}
