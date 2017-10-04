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
package com.trilogy.app.crm.move.processor.subscription;

import com.trilogy.app.crm.agent.BeanInstall;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.config.AccountRequiredField;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.ReverseActivationMoveRequest;
import com.trilogy.app.crm.move.request.ReverseActivationMoveRequestXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.beans.xi.XInfo;
import com.trilogy.framework.xhome.beans.xi.XInfoSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * This is responsible for performing any setup required for Reverse Activation
 * 
 * It does not implement any account move business logic, modify the request,
 * or modify the accounts involved.
 *
 * @author Mangaraj Sahoo
 * @since 9.2
 */
public class ReverseActivationMoveProcessor<RAMR extends ReverseActivationMoveRequest> extends MoveProcessorProxy<RAMR>
{
    
    public ReverseActivationMoveProcessor(MoveProcessor<RAMR> delegate)
    {
        super(delegate);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public Context setUp(Context ctx) throws MoveException
    {
        Context moveCtx = super.setUp(ctx);

        RAMR request = this.getRequest();
        
        String newBAN = request.getNewBAN();
        if (newBAN == null || "".equals(newBAN.trim()))
        {
            Subscriber subscriber = request.getOriginalSubscription(moveCtx);
            try
            {
                Account newAccount = this.createNewSubscriberAccount(moveCtx, subscriber);
                request.setNewBAN(newAccount.getBAN());
            }
            catch (Exception e)
            {
                throw new MoveException(request, "Error setting new BAN for the move request.", e);
            }
        }

        return moveCtx;
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        String newBAN = this.getRequest().getNewBAN();
        if (newBAN == null || "".equals(newBAN.trim()))
        {
            cise.thrown(new IllegalPropertyArgumentException(ReverseActivationMoveRequestXInfo.NEW_BAN,
                    "Cannot execute the reverse activation move request."));
        }
        
        cise.throwAll();

        super.validate(ctx);
    }
    
    
    private Account createNewSubscriberAccount(Context ctx, Subscriber subscriber) throws HomeException
    {
        Account oldAccount = subscriber.getAccount(ctx);
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "oldAccount -> " + oldAccount);
        }
        
        Account template = (Account) ctx.get(BeanInstall.BULK_ACCOUNT_TEMPLATE);
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, BeanInstall.BULK_ACCOUNT_TEMPLATE + " -> " + template);
        }
        
        Account newAccount = null;
        try
        {
            newAccount = (Account) template.deepClone();
            newAccount.setSpid(oldAccount.getSpid());
        }
        catch (Throwable e)
        {
            LogSupport.major(ctx, this, "Account deepClone() failed: " + e.getMessage(), e);
            throw new IllegalStateException("Account deepClone() failed. ", e);
        }
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "newAccount [cloned] -> " + newAccount);
        }
        
        XInfo srcXInfo = (XInfo) XBeans.getInstanceOf(ctx, oldAccount, XInfo.class);
        XInfo dstXInfo = (XInfo) XBeans.getInstanceOf(ctx, newAccount, XInfo.class);
        
        for(AccountRequiredField field : oldAccount.getMandatoryFields(ctx).values())
        {
            PropertyInfo srcPi = XInfoSupport.getPropertyInfo(ctx, srcXInfo, field.getPropertyName());
            PropertyInfo dstPi = XInfoSupport.getPropertyInfo(ctx, dstXInfo, field.getPropertyName());
            
            if (dstPi.getType().isAssignableFrom(srcPi.getType()))
            {
                dstPi.set(newAccount, srcPi.get(oldAccount));
            }
            else
            {
                dstPi.set(newAccount, dstPi.fromString(srcPi.toString(oldAccount)));
            }
        }
        
        newAccount = HomeSupportHelper.get(ctx).createBean(ctx, newAccount);
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "newAccount [created] -> " + newAccount);
        }
        
        return newAccount;
    }
}
