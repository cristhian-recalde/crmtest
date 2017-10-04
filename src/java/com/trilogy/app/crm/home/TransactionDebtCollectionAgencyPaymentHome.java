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
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.TransactionCollectionAgency;
import com.trilogy.app.crm.bean.TransactionCollectionAgencyHome;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * This home creates entries in the TransactionDebtCollection table when payment is received
 * by account in collection.
 * 
 * @author Marcio Marques
 *
 */
public class TransactionDebtCollectionAgencyPaymentHome extends HomeProxy
{
    private static final long serialVersionUID = 1L;


    public TransactionDebtCollectionAgencyPaymentHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final Transaction transaction = (Transaction) super.create(ctx, (Transaction) obj);

        Account account = (Account) ctx.get(Account.class);
        if (account == null || account.getBAN() == null
            || !SafetyUtil.safeEquals(account.getBAN().trim(), transaction.getBAN().trim()))
        {
            account = HomeSupportHelper.get(ctx).findBean(ctx, Account.class, transaction.getBAN());
        }
        
        if (account.isInCollection() && !account.isPrepaid() && CoreTransactionSupportHelper.get(ctx).isStandardPayment(ctx, transaction))
        {
            CRMSpid spid = SpidSupport.getCRMSpid(ctx, account.getSpid());
            
            if (spid.isEnableDebtCollectionAgencies())
            {
                Account responsableAccount = account.getResponsibleParentAccount(ctx);
                if (responsableAccount.getDebtCollectionAgencyId() > 0)
                {
                    Home home = (Home) ctx.get(TransactionCollectionAgencyHome.class);
                    TransactionCollectionAgency bean;
                    
                    try
                    {
                        bean = (TransactionCollectionAgency) XBeans.instantiate(TransactionCollectionAgency.class, ctx);
                    }
                    catch (Throwable t)
                    {
                        bean = new TransactionCollectionAgency();
                    }
                    
                    bean.setReceiptNum(transaction.getReceiptNum());
                    bean.setAgencyId(responsableAccount.getDebtCollectionAgencyId());
                    
                    home.create(ctx, bean);
                }
            }
        }
        
        return transaction;
    }
}