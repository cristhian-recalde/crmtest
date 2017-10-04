/*
 * CreditCardNumberValidator.java
 * 
 * Author : danny.ng@redknee.com Date : Mar 17, 2006
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

import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CreditCardEntry;
import com.trilogy.app.crm.bean.CreditCardType;
import com.trilogy.app.crm.bean.CreditCardTypeHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.TransactionMethod;
import com.trilogy.app.crm.creditcard.CreditCardEntryAware;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.util.cipher.Encrypted;

/**
 * Credit Card Validator.  Performs Luhn validation on
 * the subscriber's credit card if their credit
 * card type requires Luhn validation.<BR>
 * <BR>
 * See <a href="http://en.wikipedia.org/wiki/Luhn_algorithm">Luhn Algorithm</a>
 * for more details on the Luhn algorithm.
 * 
 * @author danny.ng@redknee.com
 * @created Mar 17, 2006
 */
public class CreditCardNumberValidator implements Validator
{

    public CreditCardNumberValidator()
    {
        super();
    }


    public static CreditCardNumberValidator instance()
    {
        return instance_;
    }
    
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        if (obj instanceof Account)
        {
            Account account = (Account) obj;
            
            TransactionMethod method = null;
            if (account.isResponsible())
            {
                try
                {
                    method = HomeSupportHelper.get(ctx).findBean(ctx, TransactionMethod.class, account.getPaymentMethodType());
                }
                catch (final HomeException e)
                {
                    new MinorLogMsg(this, "Error retrieving payment method type " + account.getPaymentMethodType() 
                            + " for account " + account.getBAN() + ".  Skipping card number validation...", e).log(ctx);
                }
            }

            if (method != null)
            {
                if (method.isCardTypeUsed()
                        && method.isIdentifierUsed())
                {
                    String cardNumber = account.getCreditCardNumber();
                    if (cardNumber != null 
                            && !cardNumber.startsWith(Encrypted.ENCRYPTED_MASK_PREFIX))
                    {
                        CreditCardEntry tempEntry = new CreditCardEntry();
                        tempEntry.setCardTypeId(account.getPMethodCardTypeId());
                        tempEntry.setCardNumber(cardNumber);
                        if (method.isDateUsed())
                        {
                            tempEntry.setExpiryDate(account.getExpiryDate());
                        }
                        cardCheck(ctx, tempEntry);
                    }
                }
            }
        }
        
        if (obj instanceof CreditCardEntryAware)
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

            
            if(entry.getCardNumber()!=null && !entry.getCardNumber().startsWith(Encrypted.ENCRYPTED_MASK_PREFIX) )
            {
                cardCheck(ctx, entry);
            }
        }
    }

    
    /**
     * Retruns true if the credit card <code>cardNumber</code> passes
     * Luhn vaildation.<BR>
     * <BR>
     * This method graciously taken from 
     * <a href="http://www.merriampark.com/anatomycc.htm#Source">
     * Anatomy of Credit Card Numbers</a> which was free for use
     * with no restrictions or guarantees.
     * 
     * @param cardNumber
     * @return True if <code>cardNumber</code> passes Luhn validation, false otherwise
     */
    public boolean luhnCheck(String cardNumber)
    {
        int sum = 0;
        int digit = 0;
        int addend = 0;
        boolean timesTwo = false;
        
        checkNumeric(cardNumber);
        
        for (int i = cardNumber.length() - 1; i >= 0; i--)
        {
            digit = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (timesTwo)
            {
                addend = digit * 2;
                if (addend > 9)
                {
                    addend -= 9;
                }
            }
            else
            {
                addend = digit;
            }
            sum += addend;
            timesTwo = !timesTwo;
        }
        int modulus = sum % 10;
        return modulus == 0;
    }
    
    
    private void checkNumeric(String cardNumber)
    {
        Pattern p = Pattern.compile("^(\\d)*$");
        Matcher m = p.matcher(cardNumber);
        boolean matched = m.matches();
        if (!matched)
        {
            throw new IllegalStateException("Invalid Card Number");
        }
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
        
        Pattern p = Pattern.compile(cardType.getNumberregex());
        Matcher m = p.matcher(entry.getCardNumber());
        if (!m.matches())
        {
            throw new IllegalStateException("Card number did not match number format of the selected card type.");
        }
        
        if (cardType.isLuhnValidation() && !luhnCheck(entry.getCardNumber()))
        {
            throw new IllegalStateException("Card number did not pass Luhn validation.  Please verify the card number was entered correctly.");
        }
    }
    
    private final static CreditCardNumberValidator instance_ = new CreditCardNumberValidator();
}
