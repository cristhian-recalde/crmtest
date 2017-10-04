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
package com.trilogy.app.crm.move.processor.account.strategy;

import java.util.Collection;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountsDiscount;
import com.trilogy.app.crm.bean.AccountsDiscountXInfo;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.support.AccountMoveValidationSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * Copies the new discount class to the new account during move
 *
 * @author Ankit Nagpal
 * @since 9_7_2
 */
public class ServiceLevelDiscountClassCopyMoveStrategy<AMR extends AccountMoveRequest> extends MoveProcessorProxy<AMR>
{
    public ServiceLevelDiscountClassCopyMoveStrategy(MoveProcessor<AMR> delegate)
    {
        super(delegate);
    }
    
    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        AMR request = this.getRequest();
        
        AccountMoveValidationSupport.validateOldAccountExists(ctx, request, cise);
        
        AccountMoveValidationSupport.validateNewAccountExists(ctx, request, cise);
        
        cise.throwAll();

        super.validate(ctx);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void move(Context ctx) throws MoveException
    {
        super.move(ctx);

        AMR request = this.getRequest();
        
        Account newAccount = request.getNewAccount(ctx);
        Account oldAccount = request.getOldAccount(ctx);
        
        And and = new And();
        and.add(new EQ(AccountsDiscountXInfo.BAN, oldAccount.getBAN()));
        and.add(new EQ(AccountsDiscountXInfo.SPID, oldAccount.getSpid()));
        Collection<AccountsDiscount> coll = null;
		try {
			
			Home home = HomeSupportHelper.get(ctx).getHome(ctx, AccountsDiscount.class);
			coll = HomeSupportHelper.get(ctx).getBeans(ctx, AccountsDiscount.class, and);
			
	        for (AccountsDiscount accountsDiscount : coll)
	        {
	        	AccountsDiscount ad = new AccountsDiscount();
	        	ad.setBAN(newAccount.getBAN());
				ad.setDiscountClass(accountsDiscount.getDiscountClass());
				ad.setSpid(newAccount.getSpid());
				home.create(ctx, ad);
				
				if (newAccount.getBAN() != oldAccount.getBAN())
				{
					HomeSupportHelper.get(ctx).removeBean(ctx, accountsDiscount);
				}
	        }
		}
        
         catch (HomeException e) {
        	 new MajorLogMsg(Account.class, "Unable to create/remove discount class for ban " + newAccount.getBAN(), e).log(ctx);
		}
    }
    
}
