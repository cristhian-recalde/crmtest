package com.trilogy.app.crm.subscriber.charge;

public interface SubscriberChargingAccumulator
extends CrmChargingAccumulator
{

     
    public void addChargedService(long id); 
    public void addChargedPackage(long id); 
    public void addChargedBundle(long id); 
    public void addChargedAuxiliaryService(long id); 
     
    public void addRefundService(long id); 
    public void addRefundPackage(long id); 
    public void addRefundBundle(long id); 
    public void addRefundAuxiliaryService(long id); 
    
}
