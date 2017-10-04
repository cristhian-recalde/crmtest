package com.trilogy.app.crm.subscriber.charge;


public abstract class AbstractCrmCharger 
implements CrmCharger, CrmChargingAccumulator,
ChargingConstants
{
    

     
    public double getTotalChargedAmount() {
        return totalChargedAmount;
    }


    public double getTotalRefundAmount() {
        return totalRefundAmount;
    }

 

    public double getTotalToBeChargedAmount() {
        return totalToBeChargedAmount;
    }

 

    public double getTotalToBeRefundAmount() {
        return totalToBeRefundAmount;
    }

 

    public double getTotalFailedChargeAmount() {
        return totalFailedChargeAmount;
    }


    public double getTotalFailedRefundAmount() {
        return totalFailedRefundAmount;
    }

    public void accumulateChargedAmount(double amount)
    {
        this.setTotalChargedAmount(this.getTotalChargedAmount() + amount); 
    }
    public void accumulateToBeChargedAmount(double amount)
    {
        this.setTotalToBeChargedAmount(this.getTotalToBeChargedAmount() + amount); 
    }
    public void accumulateFailedChargeAmount(double amount)
    {
        this.setTotalFailedChargeAmount(this.getTotalFailedChargeAmount() + amount); 
    }
    public void accumulateRefundAmount(double amount)
    {
        this.setTotalRefundAmount(this.getTotalRefundAmount() + amount); 
    }
    public void accumulateToBeRefundAmount(double amount)
    {
        this.setTotalToBeRefundAmount(this.getTotalToBeRefundAmount() + amount); 
    }
    public void accumulateFailedRefundAmount(double amount)
    {
        this.setTotalFailedRefundAmount(this.getTotalFailedRefundAmount() + amount);
    }
    

    
    private void setTotalChargedAmount(double totalChargedAmount) {
        this.totalChargedAmount = totalChargedAmount;
    }

    private void setTotalRefundAmount(double totalRefundAmount) {
        this.totalRefundAmount = totalRefundAmount;
    }

    private void setTotalToBeChargedAmount(double totalToBeChargedAmount) {
        this.totalToBeChargedAmount = totalToBeChargedAmount;
    }

    private void setTotalToBeRefundAmount(double totalToBeRefundAmount) {
        this.totalToBeRefundAmount = totalToBeRefundAmount;
    }

    private void setTotalFailedChargeAmount(double totalFailedChargeAmount) {
        this.totalFailedChargeAmount = totalFailedChargeAmount;
    }

    private void setTotalFailedRefundAmount(double totalFailedRefundAmount) {
        this.totalFailedRefundAmount = totalFailedRefundAmount;
    }
    
    
    protected int getResult()
    {
        if ( calculationResult == CALCULATION_SUCCESS && 
                chargeResult == RUNNING_SUCCESS)
        {
            return OPERATION_SUCCESS;
        }
         
        return OPERATION_ERROR;

    }
    
    protected void clearChargedSet()
    {
       this.totalChargedAmount = 0;
       this.totalFailedChargeAmount =0; 
       this.totalToBeChargedAmount =0;
    }
    
    protected void clearRefundSet()
    {
        this.totalRefundAmount = 0;
        this.totalFailedRefundAmount =0; 
        this.totalToBeRefundAmount =0;
    }
    
    public int getChargeResult() {
        return chargeResult;
    }


    public void setChargeResult(int chargeResult) {
        this.chargeResult = chargeResult;
    }


    public int getCalculationResult() {
        return calculationResult;
    }


    public void setCalculationResult(int calculationResult) {
        this.calculationResult = calculationResult;
    }

    private double totalChargedAmount; 
    private double totalRefundAmount;
    private double totalToBeChargedAmount;
    private double totalToBeRefundAmount;
    private double totalFailedChargeAmount;
    private double totalFailedRefundAmount;    

    protected int chargeResult=RUNNING_SUCCESS;
    protected int calculationResult=CALCULATION_SUCCESS;

}
