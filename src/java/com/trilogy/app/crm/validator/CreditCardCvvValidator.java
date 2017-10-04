/*
 * CreditCardCvvValidator.java
 * 
 * Author : danny.ng@redknee.com Date : Mar 28, 2006
 * 
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CreditCardEntry;
import com.trilogy.app.crm.bean.CreditCardType;
import com.trilogy.app.crm.bean.CreditCardTypeHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.creditcard.CreditCardEntryAware;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;

/**
 * Credit Card CVV Validator
 * Validates the Entry's CVV against the regex
 * stored in the Credit Card Type
 * 
 * @author danny.ng@redknee.com
 * @created Mar 28, 2006
 */
public class CreditCardCvvValidator implements Validator
{

    public CreditCardCvvValidator()
    {
        super();
    }


    public static CreditCardCvvValidator instance()
    {
        return instance_;
    }
    
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        CreditCardEntryAware bean = (CreditCardEntryAware) obj;
        
        if (!bean.isCreditCardPayment())
        {
            return;
        }
        CreditCardEntry entry = bean.getCreditCardInfo();
        if (entry == null)
        {
            if (obj instanceof Subscriber)
            {
                Subscriber sub = (Subscriber) obj;
                throw new IllegalStateException("Subscriber " + sub.getId() + " has no credit card entry");                
            }
            else if (obj instanceof Account)
            {
                Account acct = (Account) obj;
                throw new IllegalStateException("Account " + acct.getBAN() + " has no credit card entry");                
            }
        }

        
 /*       if (entry.isCvvChanged())
        {
            cardCheck(ctx, entry);
        }
 */  
    }

    
    private void cardCheck(Context ctx, CreditCardEntry entry)
    {
        Home cardTypeHome = (Home) ctx.get(CreditCardTypeHome.class);
        CreditCardType cardType = null; 
        
        try
        {
            cardType = (CreditCardType) cardTypeHome.find(ctx, Integer.valueOf(entry.getCardTypeId()));
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Encoutered error looking up credity card type " + entry.getCardTypeId());
        }
        
        if (cardType == null)
        {
            throw new IllegalStateException("Could not find card type " + entry.getCardTypeId() + ".");
        }
        
        Pattern p = Pattern.compile(cardType.getCvvregex());
        Matcher m = p.matcher(entry.getCvv());
        if (!m.matches())
        {
            throw new IllegalStateException("Card CVV did not match CVV format of the selected card type.");
        }
    }
    
    private final static CreditCardCvvValidator instance_ = new CreditCardCvvValidator();
}
