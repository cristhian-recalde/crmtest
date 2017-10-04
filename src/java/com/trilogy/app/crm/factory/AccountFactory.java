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

package com.trilogy.app.crm.factory;

import java.util.List;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.Spid;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.util.snippet.log.Logger;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.agent.BeanInstall;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCreationTemplate;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.support.LicensingSupportHelper;

/**
 * Factory for creating Accounts.  Sets the currency, context and spid.
 */
public class AccountFactory implements ContextFactory
{

    public AccountFactory()
    {
    }

    @Override
    public Object create(final Context ctx)
    {
        try
        {
            final Account template = (Account) ctx.get(AccountCreationTemplate.class);
            final Account account = (Account) template.deepClone();

            final Context subCtx = ctx.createSubContext();

            // hard to track error if the template has the spid on 0
            if (account.getSpid() == 0)
            {
                account.setSpid(1);
            }

            /*
             * Making sure any default IDs and security questions are set in the bean, but in a deep-cloned list.
             */
            account.unsetLazyLoadCollections();
            if (template.isAccountIdentificationLoaded())
            {
            	account.setAccountIdentificationLoaded(template.getAccountIdentificationLoaded());
            	account.setIdentificationGroupList((List) XBeans.deepClone(template.getIdentificationGroupList()));
            }
            if (template.isSecurityQuestionAndAnswerLoaded())
            {
				account.setSecurityQuestionAndAnswerLoaded(template
				    .isSecurityQuestionAndAnswerLoaded());
				account.setSecurityQuestionsAndAnswers((List) XBeans
				    .deepClone(template.getSecurityQuestionsAndAnswers()));
            }
            
            // This is so that the Subscriber factory
            // can take certain values (like SPID) from
            // the account if required.
            subCtx.put(Account.class, account);

            // We do this so that some subscriber values can be specified
            // in account template without loosing the behaviour of the
            // SubscriberFactory. KGR
            try
            {
                if (account.getSubscriber() == null)
                {
                    account.setSubscriber((Subscriber) XBeans.instantiate(Subscriber.class, subCtx));
                }
                else
                {
                    SubscriberFactory.initSubscriber(subCtx, account.getSubscriber());
                }
            }
            catch (Throwable t)
            {
                if (Logger.isDebugEnabled())
                {
                    Logger.debug(ctx, this, "Error in Account initialization", t);
                }
            }

            final Currency currency = (Currency) subCtx.get(Currency.class,Currency.DEFAULT);
            if (currency != null)
            {
                account.setCurrency(currency.getCode());
            }
            
            account.setContext(subCtx);
            // account.setPromiseToPayDate(n);

            final Spid spid = (Spid) subCtx.get(Spid.class);
            if (spid != null && spid.getId() > 0)
            {
                account.setSpid(spid.getId());
            }

            if (LicensingSupportHelper.get(subCtx).isLicensed(subCtx, LicenseConstants.POSTPAID_LICENSE_KEY))
            {
                account.setSystemType(SubscriberTypeEnum.POSTPAID);
            }
            else if (LicensingSupportHelper.get(subCtx).isLicensed(subCtx, LicenseConstants.PREPAID_LICENSE_KEY))
            {
                account.setSystemType(SubscriberTypeEnum.PREPAID);
            }
            
            setDetailsFromSpid(ctx, account);

            return account;
        }
        catch (CloneNotSupportedException e)
        {
            Logger.crit(ctx, this, "System error: Unable to clone Account", e);

            return null;
        }

    }

    /**
     * Take some initial values from CRMSpid and set into Subscriber.
     */
    public static void setDetailsFromSpid(final Context ctx, final Account account)
    {
        try
        {
            final Home home = (Home) ctx.get(CRMSpidHome.class);
            final CRMSpid spid = (CRMSpid) home.find(ctx, Integer.valueOf(account.getSpid()));

            if (spid != null)
            {
                if (spid.getBillCycle()!= CRMSpid.DEFAULT_BILLCYCLE)
                {
                    account.setBillCycleID(spid.getBillCycle());
                }
                
                if (spid.getTaxAuthority()!= CRMSpid.DEFAULT_TAXAUTHORITY)
                {
                    account.setTaxAuthority(spid.getTaxAuthority());
                }

                if (spid.getDealer()!= CRMSpid.DEFAULT_DEALER)
                {
                    account.setDealerCode(spid.getDealer());
                }
                
                if (spid.getDefaultCreditCategory()!= CRMSpid.DEFAULT_DEFAULTCREDITCATEGORY)
                {
                    account.setCreditCategory(spid.getDefaultCreditCategory());
                }

                if (spid.getCurrency()!= CRMSpid.DEFAULT_CURRENCY)
                {
                    account.setCurrency(spid.getCurrency());
                }
            }
            else
            {
                new MinorLogMsg(SubscriberFactory.class,
                        "Unable to determine default parameters. No SPID found with identifier " + account.getSpid(),
                        null).log(ctx);
            }
        }
        catch (HomeException exception)
        {
            new MinorLogMsg(SubscriberFactory.class, "Unable to determine default billing option.", exception).log(ctx);
        }
    }
}


