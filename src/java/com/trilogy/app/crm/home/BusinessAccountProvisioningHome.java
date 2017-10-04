/*
 *  BusinessAccountProvisioningHome.java
 *
 *  Author : Gary Anderson
 *  Date   : 2003-11-14
 *
 *  Copyright (c) Redknee, 2003
 *  - all rights reserved
 */
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.AbstractAccount;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategory;

/**
 * Manages AccountType changes affecting business accounts.
 *
 * @author gary.anderson@redknee.com
 */
public class BusinessAccountProvisioningHome
    extends HomeProxy
    implements ContextAware
{
    /**
     * Creates a new BusinessAccountProvisioningHome for the given home.
     *
     * @param context The operating context.
     * @param delegate The Home to which this object delegates.
     */
    public BusinessAccountProvisioningHome(
        final Context context,
        final Home delegate)
    {
        super(delegate);
        setContext(context);
    }

    // INHERIT
    @Override
    public Object store(Context ctx,final Object obj)
        throws HomeException
    {
        final Account newAccount = (Account)obj;
        
        final Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);

        processAccountTypeChange(ctx, oldAccount, newAccount);

        return super.store(ctx,newAccount);
    }


    /**
     * Adds business information for the account.
     *
     * @param account The account that has just been switched to a business type.
     */
    private void addBusinessInformation(final Account account)
    {
        // There is currently nothing that needs to be done.  The UI validator
        // will make sure that the company information is set.
    }
    

    /**
     * Processes the given old-to-new account type changes.
     *
     * @param oldAccount The old state of the account.
     * @param newAccount The new state of the account.
     */
    private void processAccountTypeChange(Context ctx,
        final Account oldAccount,
        final Account newAccount)
    {
		final AccountCategory oldType = oldAccount.getAccountCategory(ctx);
		final AccountCategory newType = newAccount.getAccountCategory(ctx);

        if (oldType.equals(newType))
        {
            return;
        }

		if (oldAccount.isBusiness(ctx) && !newAccount.isBusiness(ctx))
        {
            removeBusinessInformation(newAccount);
        }
		else if (!oldAccount.isBusiness(ctx) && newAccount.isBusiness(ctx))
        {
            addBusinessInformation(newAccount);
        }
    }
    
    
    /**
     * Removes business information for the account.
     *
     * @param account The account that has just been switched to a non-business
     * type.
     */
    private void removeBusinessInformation(final Account account)
    {
        // NOTE - 2003-12-12 - It is expected that this account will be stored
        // after this method is called, so there is no need to do so here.
        account.setCompanyName(AbstractAccount.DEFAULT_COMPANYNAME);
        account.setTradingName(AbstractAccount.DEFAULT_TRADINGNAME);
        account.setRegistrationNumber(AbstractAccount.DEFAULT_REGISTRATIONNUMBER);
        account.setCompanyTel(AbstractAccount.DEFAULT_COMPANYTEL);
        account.setCompanyFax(AbstractAccount.DEFAULT_COMPANYFAX);
        account.setCompanyAddress1(AbstractAccount.DEFAULT_COMPANYADDRESS1);
        account.setCompanyAddress2(AbstractAccount.DEFAULT_COMPANYADDRESS2);
        account.setCompanyCity(AbstractAccount.DEFAULT_COMPANYCITY);
        account.setCompanyProvince(AbstractAccount.DEFAULT_COMPANYPROVINCE);
        account.setCompanyCountry(AbstractAccount.DEFAULT_COMPANYCOUNTRY);
    }
} // class
