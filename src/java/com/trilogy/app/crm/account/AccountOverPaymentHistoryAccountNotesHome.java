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
package com.trilogy.app.crm.account;


import com.trilogy.app.crm.bean.AccountOverPaymentHistory;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.report.ReportUtilities;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * @author vickhram.sanap
 *
 */
public class AccountOverPaymentHistoryAccountNotesHome extends HomeProxy {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public AccountOverPaymentHistoryAccountNotesHome(Home delegate)
    {
        super(delegate);
    }
	
	public AccountOverPaymentHistoryAccountNotesHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }
	
	/**
	 * Storing AccountOverPaymentHistoryAccountNotes entry is not supported
	 */
	public Object store(Context ctx, Object bean) throws HomeException
	{
		
		throw new UnsupportedOperationException();
		
	}
	
	/**
	 * Removing AccountOverPaymentHistoryAccountNotes entry is not supported
	 */
	public void remove(Context ctx, Object bean) throws HomeException
	{
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Removing All AccountOverPaymentHistoryAccountNotes entries is not supported
	 */
	public void removeAll(Context ctx, Object bean) throws HomeException
	{
		throw new UnsupportedOperationException();
	}
    public Home getDelegate(Context ctx)
    {
        /*
        Uncomment this to help detect StackOverFlowErrors.

        Exception e = new Exception("debug");

        if ( e.getStackTrace().length == 150 ) e.printStackTrace();
        */

        return delegate_;
    }

    //////////////////////////////////////// SPI Impl

    /**
     * @param ctx
     * @param obj
     * @return Object
     * @throws HomeException
     * @throws HomeInternalException
     */
    public Object create(Context ctx, Object obj)
        throws HomeException, HomeInternalException
    {
    	Object returnObj = null;
    	try 
    	{
    		returnObj = getDelegate(ctx).create(ctx, obj);
    		AccountOverPaymentHistory aOPH = (AccountOverPaymentHistory) returnObj;
    		createAccountNote(ctx,aOPH,null,false);

    	}
        catch (HomeException he)
        {
        	createAccountNote(ctx,(AccountOverPaymentHistory) obj,he,true);
        }

        
        return returnObj;
    }
    protected void createAccountNote(Context ctx, AccountOverPaymentHistory cf, Throwable e,boolean failure ) throws HomeException {      
     
        Currency currency = ReportUtilities.getCurrency(ctx, AccountSupport.getAccount(ctx, cf.getBan()).getCurrency());

        StringBuilder msg = null;
        String newOverpaymentBalance = currency.formatValue(cf.getNewOverpaymentBalance());
        String oldOverpaymentBalance = currency.formatValue(cf.getOldOverpaymentBalance());
        String distributedAmount = currency.formatValue(cf.getDistributedAmount());
        
        long id = cf.getId();
        if (failure == false )
        {
            /* Below if statement is for 1st account note generated; Applied Over Payment should be 0 is such case */
            if (cf.getOldOverpaymentBalance()==0L)
                distributedAmount=currency.formatValue(0L);
            
        	msg = new StringBuilder("Applied Over Payment = "+distributedAmount+" from ID = "+id+" : newOverpaymentBalance = "+newOverpaymentBalance+" oldOverpaymentBalance = "+oldOverpaymentBalance);
        }
        else 
        {
        	msg = new StringBuilder("Unable to Apply Over Payment "+oldOverpaymentBalance+" ===> "+newOverpaymentBalance+" of id = "+id);
        }	
        
        //msg.append(amount);
        
        msg.append(" for account '");
        msg.append(cf.getBan());
        msg.append("'");
        
        if (failure == true && e != null)
        {
        	msg.append("failed : ");
        	msg.append(e.getMessage());
        }	

        NoteSupportHelper.get(ctx).addAccountNote(ctx, cf.getBan(), msg.toString(),
    		    SystemNoteTypeEnum.ADJUSTMENT, SystemNoteSubTypeEnum.ACCUPDATE);
 
	}
}
