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
package com.trilogy.app.crm.transaction;

import com.trilogy.app.crm.bean.CurrencyPrecision;
import com.trilogy.framework.xhome.context.Context;

/**
 * This Class was added when porting the new Account Level Payment Splitting Logic from 
 * CRM 7.3
 * @since 7.3, ported to 8.2, Sept 21, 2009.
 * 
 * 
 * @author Larry Xia
 * @author Angie Li
 *
 */
public class AbstractPaymentDistribution {


    public long getOutStandingOwing() {
        return outStandingOwing;
    }
    public void setOutStandingOwing(long outStandingOwing) {
        this.outStandingOwing = outStandingOwing;
    }

    public long getPaymentForOutStandOwing() {
        return paymentForOutStandOwing;
    }
    public void setPaymentForOutStandOwing(long paymentForOutStandOwing) {
        this.paymentForOutStandOwing = paymentForOutStandOwing;
    }
    public long getOverPayment() {
        return overPayment;
    }
    public void setOverPayment(long overPayment) {
        this.overPayment = overPayment;
    }
    
    protected long applyDisplayPrecision(Context ctx, long value)
    {
    	long returnVal = value;
    	CurrencyPrecision cp = (CurrencyPrecision)ctx.get(CurrencyPrecision.class);
    	if(cp!= null)
    	{
    		int divFactor = cp.getStoragePrecision() - cp.getDisplayPrecision();
    		if(divFactor > 0)
    		{
    			returnVal = Math.round (value / (Math.pow(10, divFactor)));
    			returnVal = returnVal * (long) (Math.pow(10, divFactor));
    		}
    	}
    	return returnVal;
    }
    
    
    
    /**
     * @return 
     */
    public long getOutStandingTaxOwing()
    {
        return outStandingTaxOwing;
    }
    
    /**
     * @param outStandingTaxOwing 
     */
    public void setOutStandingTaxOwing(long outStandingTaxOwing)
    {
        this.outStandingTaxOwing = outStandingTaxOwing;
    }
    
   
   
    /* 
     * These totals do not include payment plan
     */
    protected long outStandingOwing=0;
	protected long outStandingTaxOwing=0;  

    protected long paymentForOutStandOwing=0;
    protected long overPayment=0; 

    /* Payment plan totals will be handled by a special implementation of AbstractPaymentDistribution.
     * Payment Plan has outStandingOwing and paymentForOutStandOwing just like the regular
     * Subscription balance. 
     */ 

}
