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
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.transaction;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.core.Transaction;

/**
 * @author ali
 *
 * Redirects the transaction to the appropriate Transaction Processor
 * or continues to pass the transaction through the pipeline
 */
public interface TransactionProcessor {

    /**
     * This method contains the meat for creating transaction.
     *
     * @param obj The transaction object.
     *
     * @return Object The transaction.
     */
	public Transaction createTransaction(Context ctx, Transaction trans) throws HomeException;
    
}
