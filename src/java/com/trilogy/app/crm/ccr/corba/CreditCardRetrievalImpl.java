/*
 * CreditCardRetrievalImpl.java
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
package com.trilogy.app.crm.ccr.corba;

import com.trilogy.app.crm.bean.CreditCardInfo;
import com.trilogy.app.crm.bean.CreditCardType;
import com.trilogy.app.crm.bean.CreditCardTypeHome;
import com.trilogy.app.crm.ccr.CreditCardRetrievalPOA;
import com.trilogy.app.crm.ccr.CreditCardRetrievalService;
import com.trilogy.app.crm.ccr.ErrorCode;
import com.trilogy.app.crm.ccr.CreditCardRetrievalPackage.CreditCardInfoHolder;
import com.trilogy.app.crm.ccr.exception.SubscriberNoCCInfoException;
import com.trilogy.app.crm.ccr.exception.SubscriberNotFoundException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Credit Card Retrieval Implementation.  Basically just calls the Credit Card Retrieval
 * Service and adapts results for CORBA use.
 * 
 * @author danny.ng@redknee.com
 * @since Mar 08, 2006
 */
public class CreditCardRetrievalImpl extends CreditCardRetrievalPOA implements ContextAware
{
    public CreditCardRetrievalImpl(Context ctx)
    {
        super();
        setContext(ctx);
        defaultCreditCardInfo = new com.redknee.app.crm.ccr.CreditCardRetrievalPackage.CreditCardInfo();
        defaultCreditCardInfo.cardType = "";
        defaultCreditCardInfo.creditCardNum = "";
        defaultCreditCardInfo.cvv = "";
        defaultCreditCardInfo.expiryDate = "";
        defaultCreditCardInfo.name = "";
    }
    
    
    /**
     * Queries CRM for the credit card info, adapts the results for CORBA
     * and returns the info
     */
    public short retrieveCreditCardInfo(String msisdn, CreditCardInfoHolder holder)
    {
        // Initalize the value to the defaults
        holder.value = defaultCreditCardInfo;
        
        final CreditCardRetrievalService service = new CreditCardRetrievalService();
        
        try
        {
            final CreditCardInfo info = service.retrieveCreditCardInfo(getContext(), msisdn);
         
            com.redknee.app.crm.ccr.CreditCardRetrievalPackage.CreditCardInfo returnInfo 
                = convertCreditCardInfo(getContext(), info);
            
            holder.value = returnInfo;
        }
        catch (SubscriberNotFoundException e1)
        {
            if (LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(this, "Could not find subscriber with msisdn: " + msisdn, e1).log(getContext());
            }
            
            return ErrorCode.ENTRY_NOT_FOUND;
        }
        catch (SubscriberNoCCInfoException e2)
        {
            if (LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(this, "Subscriber with msisdn: " + msisdn + " has no credit card information.", e2).log(getContext());
            }
            
            return ErrorCode.ENTRY_HAS_NO_CCINFO;
        }
        catch (Exception e3)
        {
            new MinorLogMsg(this, "Encounterd an exception searching for credit card informatino for msisdn: " + msisdn, e3).log(getContext());
            return ErrorCode.INTERNAL_ERROR;
        }
        
        return ErrorCode.SUCCESS;
    }

    
    /**
     * Converts the CreditCardInfo object of AppCrm into the CreditCardInfo corba object
     * 
     * @param ctx
     * @param info
     * @return
     * @throws HomeException
     */
    protected com.redknee.app.crm.ccr.CreditCardRetrievalPackage.CreditCardInfo 
        convertCreditCardInfo(Context ctx, CreditCardInfo info) throws HomeException
    {
        com.redknee.app.crm.ccr.CreditCardRetrievalPackage.CreditCardInfo returnInfo 
            = new com.redknee.app.crm.ccr.CreditCardRetrievalPackage.CreditCardInfo();
        
        Home infoHome = (Home) ctx.get(CreditCardTypeHome.class);
        if (infoHome == null)
        {
            throw new HomeException("Could not find the Credit Card Type Home from the context");
        }
        
        CreditCardType cardType = (CreditCardType) infoHome.find(ctx, Integer.valueOf(info.getCardTypeId()));
        if (cardType == null)
        {
            throw new HomeException("Could not find Credit Card Type with id " + info.getCardTypeId());
        }
        
        returnInfo.cardType = cardType.getCardType();
        returnInfo.name = info.getCardName();
        returnInfo.expiryDate = info.getExpiryDate();
        returnInfo.creditCardNum = info.getCardNumber();
        returnInfo.cvv = info.getCvv();
        
        return returnInfo;
    }
    
    
    public Context getContext()
    {
        return ctx_;
    }


    public void setContext(Context ctx)
    {
        ctx_ = ctx;
    }
    
    protected Context ctx_ = null;
    
    private static com.redknee.app.crm.ccr.CreditCardRetrievalPackage.CreditCardInfo defaultCreditCardInfo;
}
