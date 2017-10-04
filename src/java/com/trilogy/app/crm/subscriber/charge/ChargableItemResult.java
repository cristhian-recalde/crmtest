package com.trilogy.app.crm.subscriber.charge;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.subscriber.charge.customize.TransactionCustomize;
import com.trilogy.app.crm.subscriber.charge.validator.DuplicationValidator;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;

public class ChargableItemResult {

	public ChargableItemResult()
	{

	}
    
	
	public ChargableItemResult(int action, int chargableItemType, 
            Object obj, long id, Subscriber sub)
    {
        this.chargableItemType = chargableItemType; 
        this.chargableObject = obj; 
        this.id = id; 
        this.action = action;
        this.subscriber = sub; 
        this.oldSubscriber = null; 
        this.chargedSubscriber = sub;
 
    }
    
    public ChargableItemResult(int action, int chargableItemType, 
            Object obj, long id, Subscriber sub, Subscriber oldSub)
    {
        this.chargableItemType = chargableItemType; 
        this.chargableObject = obj; 
        this.id = id; 
        this.action = action;
        this.subscriber = sub; 
        this.oldSubscriber = oldSub; 
        this.chargedSubscriber = sub;
 
    }

    public int getChargableItemType() {
        return chargableItemType;
    }
    public void setChargableItemType(int chargableItemType) {
        this.chargableItemType = chargableItemType;
    }
    public Object getChargableObject() {
        return chargableObject;
    }
    public void setChargableObject(Object chargableObject) {
        this.chargableObject = chargableObject;
    }
    public long getId() {
        return id;
    }
    public Subscriber getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
        this.chargedSubscriber = subscriber;
    }

    public Subscriber getOldSubscriber() {
        return oldSubscriber;
    }

    public void setOldSubscriber(Subscriber oldSubscriber) {
        this.oldSubscriber = oldSubscriber;
    }

    public void setId(long id) {
        this.id = id;
    } 
    
    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    } 
    
    public int getChargeResult() {
        return chargeResult;
    }

    public void setChargeResult(int chargeResult) {
        this.chargeResult = chargeResult;
    }

    
    public Object getChargableObjectRef() {
        return chargableObjectRef;
    }

    public void setChargableObjectRef(Object chargableObjectRef) {
        this.chargableObjectRef = chargableObjectRef;
    }

    public Transaction getTrans() {
        return trans;
    }

    public void setTrans(Transaction trans) {
        this.trans = trans;
    } 

    
    public int chargableItemType; 
    public Subscriber subscriber;
    public Subscriber chargedSubscriber;
    public Subscriber oldSubscriber; 
    public Object chargableObject; 
    public int action; 
    public long id; 
    public int chargeResult; 
    public Object chargableObjectRef; 
    public Transaction trans;    
    public Throwable thrownObject; 
    public boolean isActivation = false; 
    public boolean isHistoryCreated = true; 
    public Long itemChargedFee = null;
    public short chargingCycleType; 
    public boolean skipValidation = false;
    
    public void setSkipValidation(boolean skipValidation)
    {
        this.skipValidation = skipValidation;
    }
    
    public boolean getSkipValidation() {
		return skipValidation;
	}
    
    public Long getItemChargedFee() 
    {
		return itemChargedFee;
	}


	public void setItemChargedFee(Long itemChargedFee) 
	{
		this.itemChargedFee = itemChargedFee;
	}


	public int runningState = ChargingConstants.RUNNING_SUCCESS;

	public Throwable getThrownObject() {
		return thrownObject;
	}


	public void setThrownObject(Throwable thrownObject) {
		this.thrownObject = thrownObject;
	}


	public boolean isActivation() {
		return isActivation;
	}


	public void setActivation(boolean isActivation) {
		this.isActivation = isActivation;
	}



	public boolean isHistoryCreated() {
		return isHistoryCreated;
	}


	public void setHistoryCreated(boolean isHistoryCreated) {
		this.isHistoryCreated = isHistoryCreated;
	}


	public short getChargingCycleType() {
		return chargingCycleType;
	}


	public void setChargingCycleType(short chargingCycleType) {
		this.chargingCycleType = chargingCycleType;
	}


	public int getRunningState() {
		return runningState;
	}


	public void setRunningState(int runningState) {
		this.runningState = runningState;
	}
	
	public boolean isOCGError()
	{
	    return (this.thrownObject instanceof OcgTransactionException);
	}
	
	public int getOCGErrorCode()
	{
	    if(this.thrownObject instanceof OcgTransactionException)
	        return ((OcgTransactionException)this.thrownObject).getErrorCode();
	    
	    if(this.thrownObject != null)
	        return -1;
	    
	    return 0;
	}
	
	public String getOCGErrorMessage()
    {
	    if(this.thrownObject instanceof OcgTransactionException)
            return ((OcgTransactionException)this.thrownObject).getMessage();
        
        if(this.thrownObject != null)
            return this.thrownObject.getMessage()!=null ? this.thrownObject.getMessage() : "Unknown Error";
        
        return "No OCG Error";
    }


    public Subscriber getChargedSubscriber()
    {
        return chargedSubscriber;
    }


    public void setChargedSubscriber(Subscriber chargedSubscriber)
    {
        this.chargedSubscriber = chargedSubscriber;
    }	
}