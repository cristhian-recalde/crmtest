/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.subscriber.provision;

/**
 * @author jchen
 * 
 * (I want to rip my hair out!)  This is a very bad implementation!  It will need to be rewritten and affects Service charging.
 * @author angie.li
 */
public class ProvisionResultCode 
{
	int adjustMinutes;
	long adjustAmount;
	int resultPricePlan;
    int resultStateChange;
    int resultCreditLimit;
    int resultServices;
    int resultEcp;
    int resultSmsb;
    int resultIpc;
    int resultHlr;
    int resultUps;
    int resultCrm;
    int resultLast;
    int resultCharge;
    int resultPricePlanChange;
    int resultAlcatel;
    int resultBlackberry;
    int resultWimax;
    
    
	/**
	 * @return Returns the adjustAmt.
	 */
	public long getAdjustAmount() {
		return adjustAmount;
	}
	/**
	 * @param adjustAmt The adjustAmt to set.
	 */
	public long addAdjustAoumt(long adjustAmt) {
		this.adjustAmount += adjustAmt;
		return this.adjustAmount;
	}
	/**
	 * @return Returns the adjustMin.
	 */
	public int getAdjustMinutes() {
		return adjustMinutes;
	}
	/**
	 * @param adjustMin The adjustMin to set.
	 */
	public int addAdjustMinutes(int adjustMin) 
	{
		this.adjustMinutes += adjustMin;
		return this.adjustMinutes;
	}
	/**
	 * @return Returns the resultCreditLimit.
	 */
	public int getResultCreditLimit() {
		return resultCreditLimit;
	}
	/**
	 * @param resultCreditLimit The resultCreditLimit to set.
	 */
	public void setResultCreditLimit(int code) 
	{
		setResultLast(code);
		this.resultCreditLimit = code;
	}
	/**
	 * @return Returns the resultCrm.
	 */
	public int getResultCrm() {
		return resultCrm;
	}
	/**
	 * @param resultCrm The resultCrm to set.
	 */
	public void setResultCrm(int code) {
		setResultLast(code);
		this.resultCrm = code;
	}
	/**
	 * @return Returns the resultEcp.
	 */
	public int getResultEcp() {
		return resultEcp;
	}
	/**
	 * @param resultEcp The resultEcp to set.
	 */
	public void setResultEcp(int code) {
		setResultLast(code);
		this.resultEcp = code;
	}
	/**
	 * @return Returns the resultHlr.
	 */
	public int getResultHlr() {
		return resultHlr;
	}
	/**
	 * @param resultHlr The resultHlr to set.
	 */
	public void setResultHlr(int code) {
		setResultLast(code);
		this.resultHlr = code;
	}
	/**
	 * @return Returns the resultLast.
	 */
	public int getResultLast() {
		return resultLast;
	}
	/**
	 * @param resultLast The resultLast to set.
	 */
	public void setResultLast(int resultLast) 
	{
		if (resultLast != 0)
			this.resultLast = resultLast;
	}
	/**
	 * @return Returns the resultPricePlan.
	 */
	public int getResultPricePlan() {
		return resultPricePlan;
	}
	/**
	 * @param resultPricePlan The resultPricePlan to set.
	 */
	public void setResultPricePlan(int code) {
		setResultLast(code);
		this.resultPricePlan = code;
	}
	/**
	 * @return Returns the resultServices.
	 */
	public int getResultServices() {
		return resultServices;
	}
	/**
	 * @param resultServices The resultServices to set.
	 */
	public void setResultServices(int code) {
		setResultLast(code);
		this.resultServices = code;
	}
	/**
	 * @return Returns the resultSmsb.
	 */
	public int getResultSmsb() {
		return resultSmsb;
	}
	/**
	 * @param resultSmsb The resultSmsb to set.
	 */
	public void setResultSmsb(int code) {
		setResultLast(code);
		this.resultSmsb = code;
	}
	/**
	 * @return Returns the resultStateChange.
	 */
	public int getResultStateChange() {
		return resultStateChange;
	}
	/**
	 * @param resultStateChange The resultStateChange to set.
	 */
	public void setResultStateChange(int code) {
		setResultLast(code);
		this.resultStateChange = code;
	}
	/**
	 * @return Returns the resultUps.
	 */
	public int getResultUps() {
		return resultUps;
	}
	/**
	 * @param resultUps The resultUps to set.
	 */
	public void setResultUps(int code) {
		setResultLast(code);
		this.resultUps = code;
	}
	/**
	 * @return Returns the resultCharge.
	 */
	public int getResultCharge() {
		return resultCharge;
	}
	/**
	 * @param resultCharge The resultCharge to set.
	 */
	public void setResultCharge(int code) {
		setResultLast(code);
		this.resultCharge = code;
	}
	/**
	 * @return Returns the resultPricePlanChange.
	 */
	public int getResultPricePlanChange() {
		return resultPricePlanChange;
	}
	/**
	 * @param resultPricePlanChange The resultPricePlanChange to set.
	 */
	public void setResultPricePlanChange(int code) {
		setResultLast(code);
		this.resultPricePlanChange = code;
	}

    /**
     * @return Returns the resultBlackberry.
     */
    public int getResultBlackberry()
    {
        return resultBlackberry;
    }
    
    /**
     * @param resultBlackberry The resultBlackberry to set.
     */
    public void setResultBlackberry(int code)
    {
        setResultLast(code);
        this.resultBlackberry = code;
    }


    
    /**
     * @return Returns the ResultWimax.
     */
    public int getResultWimax()
    {
        return resultWimax;
    }
    
    /**
     * @param ResultWimax The ResultWimax to set.
     */
    public void setResultWimax(int code)
    {
        setResultLast(code);
        this.resultWimax = code;
    }    
    
   public String toString()

   {
      StringBuilder buffer = new StringBuilder(128);
      buffer.append("ProvisionResultCode {");
      buffer.append("adjustMinutes = ");
      buffer.append(adjustMinutes);
      buffer.append(", ");
      buffer.append("adjustAmount = ");
      buffer.append(adjustAmount);
      buffer.append(", ");
      buffer.append("resultPricePlan = ");
      buffer.append(resultPricePlan);
      buffer.append(", ");
      buffer.append("resultStateChange = ");
      buffer.append(resultStateChange);
      buffer.append(", ");
      buffer.append("resultCreditLimit = ");
      buffer.append(resultCreditLimit);
      buffer.append(", ");
      buffer.append("resultServices = ");
      buffer.append(resultServices);
      buffer.append(", ");
      buffer.append("resultEcp = ");
      buffer.append(resultEcp);
      buffer.append(", ");
      buffer.append("resultSmsb = ");
      buffer.append(resultSmsb);
      buffer.append(", ");
      buffer.append("resultIpc = ");
      buffer.append(resultIpc);
      buffer.append(", ");
      buffer.append("resultHlr = ");
      buffer.append(resultHlr);
      buffer.append(", ");
      buffer.append("resultUps = ");
      buffer.append(resultUps);
      buffer.append(", ");
      buffer.append("resultWimax = ");
      buffer.append(resultWimax);
      buffer.append(", ");
      buffer.append("resultCrm = ");
      buffer.append(resultCrm);
      buffer.append(", ");
      buffer.append("resultLast = ");
      buffer.append(resultLast);
      buffer.append(", ");
      buffer.append("resultCharge = ");
      buffer.append(resultCharge);
      buffer.append(", ");
      buffer.append("resultPricePlanChange = ");
      buffer.append(resultPricePlanChange);
      buffer.append(", ");
      buffer.append("resultAlcatel = ");
      buffer.append(resultAlcatel);
      buffer.append(", ");
      buffer.append("resultBlackberry = ");
      buffer.append(resultBlackberry);

      return buffer.toString();
   }
    public int getResultIpc()
    {
        return resultIpc;
    }
    public void setResultIpc(int resultIpc)
    {
        this.resultIpc = resultIpc;
    }
    
    public int getResultAlcatel()
    {
    	return resultAlcatel;
    }
    
    public void setResultAlcatel(int value)
    {
    	resultAlcatel = value;
    }
}
