/*
 * CreditCardEncryptionAdapterHome.java
 * 
 * Author : danny.ng@redknee.com Date : Mar 31, 2006
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
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.CreditCardInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.auth.cipher.SimpleCipher;


/**
 * Encrypts and Decrypts credit card information as its is
 * stored or fetched from the database 
 * 
 * @author danny.ng@redknee.com
 * @created Mar 31, 2006
 */
public class CreditCardEncryptionAdapterHome extends AdapterHome implements Adapter
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public CreditCardEncryptionAdapterHome(Context ctx, Home home)
    {
        super(ctx, home, CreditCardEncryptionAdapterHome.instance());
    }
    
    
    public CreditCardEncryptionAdapterHome()
    {
    }

    
    public static CreditCardEncryptionAdapterHome instance()
    {
        return instance_;
    }
    
    /**
     * Decrypts the card number and CVV
     * 
     * Used by find and select methods.
     */
    public Object adapt(Context ctx, Object obj) throws HomeException
    {
        CreditCardInfo creditCardInfo = (CreditCardInfo) obj;
        
        creditCardInfo.setCardNumber(cipher.decode(creditCardInfo.getCardNumber()));
        creditCardInfo.setCvv(cipher.decode(creditCardInfo.getCvv()));
        
        return creditCardInfo;
    }


    /**
     * Encrypts the card number and CVV
     * 
     * Used by create and store methods.
     */
    public Object unAdapt(Context ctx, Object obj) throws HomeException
    {
        CreditCardInfo creditCardInfo = (CreditCardInfo) obj;
        
        creditCardInfo.setCardNumber(cipher.encode(creditCardInfo.getCardNumber()));
        creditCardInfo.setCvv(cipher.encode(creditCardInfo.getCvv()));
        
        return creditCardInfo;
    }
    
    public final static CreditCardEncryptionAdapterHome instance_ = new CreditCardEncryptionAdapterHome();
    
    public final static SimpleCipher cipher = SimpleCipher.instance();
}
