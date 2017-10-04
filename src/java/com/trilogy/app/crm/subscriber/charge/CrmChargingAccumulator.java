package com.trilogy.app.crm.subscriber.charge;

public interface CrmChargingAccumulator 
{

    public void accumulateChargedAmount(double amount); 
    public void accumulateToBeChargedAmount(double amount); 
    public void accumulateFailedChargeAmount(double amount); 
    public void accumulateRefundAmount(double amount); 
    public void accumulateToBeRefundAmount(double amount); 
    public void accumulateFailedRefundAmount(double amount); 

}
