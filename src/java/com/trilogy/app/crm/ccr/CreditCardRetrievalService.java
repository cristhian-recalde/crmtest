/*
 * CreditCardRetrievalService.java
 * 
 * Author : danny.ng@redknee.com Date : Mar 08, 2006
 * 
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.ccr;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.CreditCardInfo;
import com.trilogy.app.crm.bean.CreditCardInfoHome;
import com.trilogy.app.crm.bean.CreditCardInfoXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.ccr.exception.SubscriberNoCCInfoException;
import com.trilogy.app.crm.ccr.exception.SubscriberNotFoundException;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * Credit Card Retrieval Service. Does the actual work of 
 * the retrieval of credit card information.
 * 
 * @author danny.ng@redknee.com
 * @since Mar 08, 2006
 */
public class CreditCardRetrievalService
{
    
    public CreditCardRetrievalService()
    {
        super();
    }
    
    /**
     * Searches up the subscriber's credit card information.  If the subscriber
     * has no credit card information then it checks if the account's
     * "use account credit card info if no subscriber credit card info" flag
     * is true, and it is true, it will look up the account credit card information.
     * If it is false, then it will throw a SubscriberNotFoundException.
     * 
     * If we cannot find a subscriber with the MSISDN, then it will throw
     * a SubscriberNotFoundException
     * 
     * 
     * @param ctx
     * @param msisdn
     */
    public CreditCardInfo retrieveCreditCardInfo(Context ctx, String msisdn)
        throws SubscriberNotFoundException, HomeException
    {
        final Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn);
        if (sub == null)
        {
            throw new SubscriberNotFoundException("No subscriber found with MSISDN: " + msisdn);
        }
        
        final Home infoHome = (Home) ctx.get(CreditCardInfoHome.class); 
        
        CreditCardInfo info = (CreditCardInfo) infoHome.find(ctx, new EQ(CreditCardInfoXInfo.ID,sub.getId()));
        if (info == null)
        {
            Home accountHome = (Home) ctx.get(AccountHome.class);
            Account account = (Account) accountHome.find(ctx, sub.getBAN());

            /*
             * Check the flag that determines if we should use the Account Info
             * if the subscriber info is not there.
             */
            if (account.getCreditCardPayment() && account.getUseIfNoSubCreditInfo())
            {
                info = (CreditCardInfo) infoHome.find(ctx, new EQ(CreditCardInfoXInfo.ID,sub.getBAN()));
            }
            
            if (info == null)
            {
                throw new SubscriberNoCCInfoException("Could not find credit card information for subscriber " + sub.getId());
            }
        }
        return info;
    }
}
