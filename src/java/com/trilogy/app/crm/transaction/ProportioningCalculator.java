/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.transaction;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.Transaction;


/**
 * 
 *
 * @author Aaron Gourley
 * @since 8.0
 */
public interface ProportioningCalculator
{

    /**
     * Perform the calculations
     */
    public void execute(Context ctx, final Transaction trans, CRMSpid spid) throws ProportioningCalculatorException;


    /**
     * Returns the Subscriber that will be assigned the remainder of the transaction amount that
     * cannot be easily proportioned.
     * @return
     */
    public Subscriber getAssignedForRemaining();


    /**
     * Returns the percentage of amount proportioned to paying off current 
     * charges that will be given to each active subscriber in the account. 
     * @return
     */
    public double getRatio();


    /**
     * Returns the amount that will be overpaid/overcharged/overcredited to each subscriber
     * @return
     */
    public long getDelta();


    /**
     * Returns the abosulte value of the amount from the original transaction 
     * @return
     */
    public long getOriginalTransactionAmount();

}