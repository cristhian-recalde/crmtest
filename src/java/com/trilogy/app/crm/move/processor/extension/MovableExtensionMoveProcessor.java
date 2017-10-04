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
package com.trilogy.app.crm.move.processor.extension;

import java.util.Collection;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.LazyLoadBean;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionAware;
import com.trilogy.app.crm.extension.ExtensionInstallationException;
import com.trilogy.app.crm.extension.MovableExtension;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.MoveWarningException;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.AccountExtensionMoveRequest;
import com.trilogy.app.crm.move.request.AccountExtensionMoveRequestXInfo;
import com.trilogy.app.crm.move.request.SubscriptionExtensionMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionExtensionMoveRequestXInfo;
import com.trilogy.app.crm.support.ExtensionSupportHelper;


/**
 * This processor is responsible for executing the move method for any MovableExtension
 * instances in the request.
 * 
 * It only performs validation required to perform its duty.  No business use case
 * validation is performed here.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class MovableExtensionMoveProcessor<MR extends MoveRequest> extends MoveProcessorProxy<MR>
{
    public MovableExtensionMoveProcessor(MoveProcessor<MR> delegate)
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

        MR request = this.getRequest();

        Object parentBean = null;
        if (request instanceof AccountExtensionMoveRequest)
        {
            AccountExtensionMoveRequest aemr = (AccountExtensionMoveRequest) request;
            
            parentBean = aemr.getOldAccount(moveCtx);
        }
        else if (request instanceof SubscriptionExtensionMoveRequest)
        {
            SubscriptionExtensionMoveRequest semr = (SubscriptionExtensionMoveRequest) request;
            
            parentBean = semr.getOldSubscription(moveCtx);
        }
        
        if (parentBean != null)
        {
            ExtensionSupportHelper.get(moveCtx).setParentBean(moveCtx, parentBean);
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
        
        MR request = this.getRequest();
        
        if (request instanceof ExtensionAware)
        {
            Collection<Extension> extensions = ((ExtensionAware)request).getExtensions();
            if (extensions != null)
            {
                for (Extension extension : extensions)
                {
                    if (extension instanceof MovableExtension)
                    { 
                        if (request instanceof AccountExtensionMoveRequest)
                        {
                            AccountExtensionMoveRequest aemr = (AccountExtensionMoveRequest) request;
                            Account newAccount = aemr.getNewAccount(ctx);
                            if (newAccount == null)
                            {
                                cise.thrown(new IllegalPropertyArgumentException(
                                        AccountExtensionMoveRequestXInfo.NEW_BAN, 
                                        "New account (BAN=" + aemr.getNewBAN() + ") does not exist."));
                            }
                            break;   
                        }
                        else if (request instanceof SubscriptionExtensionMoveRequest)
                        {
                            SubscriptionExtensionMoveRequest semr = (SubscriptionExtensionMoveRequest) request;
                            Subscriber newSub = semr.getNewSubscription(ctx);
                            if (newSub == null)
                            {
                                cise.thrown(new IllegalPropertyArgumentException(
                                        SubscriptionExtensionMoveRequestXInfo.NEW_SUBSCRIPTION_ID, 
                                        "New subscription (ID=" + semr.getNewSubscriptionId() + ") does not exist."));
                            }
                            break;   
                        }
                    }
                }
            }   
        }
        
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx);
    }


    /**
     * @{inheritDoc}
     */
    @Override
    public void move(Context ctx) throws MoveException
    {
        MR request = this.getRequest();

        if (request instanceof ExtensionAware)
        {
            Collection<Extension> extensions = ((ExtensionAware)request).getExtensions();
            if (extensions != null)
            {
                for (Extension extension : extensions)
                {
                    if (extension instanceof MovableExtension)
                    {
                        if (extension instanceof LazyLoadBean)
                        {
                            ((LazyLoadBean) extension).lazyLoadAllProperties(ctx);
                        }

                        MovableExtension movableExtension = (MovableExtension) extension;
                        
                        final Object newBean;
                        final Object oldId;
                        final Object newId;
                        if (request instanceof AccountExtensionMoveRequest)
                        {
                            AccountExtensionMoveRequest aemr = (AccountExtensionMoveRequest)request;
                            newBean = aemr.getNewAccount(ctx);
                            oldId = aemr.getExistingBAN();
                            newId = aemr.getNewBAN();
                        }
                        else if (request instanceof SubscriptionExtensionMoveRequest)
                        {
                            SubscriptionExtensionMoveRequest semr = (SubscriptionExtensionMoveRequest)request;
                            newBean = semr.getNewSubscription(ctx);
                            oldId = semr.getOldSubscriptionId();
                            newId = semr.getNewSubscriptionId();
                        }
                        else
                        {
                            newBean = null;
                            oldId = null;
                            newId = null;
                        }
                        
                        if (newBean != null)
                        {
                            try
                            {
                                movableExtension.move(ctx, newBean);
                            }
                            catch (ExtensionInstallationException e)
                            {
                                new MajorLogMsg(this, e.getMessage(), e).log(ctx);
                                request.reportWarning(ctx, 
                                        new MoveWarningException(
                                                request, 
                                                "Error occurred moving " + extension.getName(ctx)
                                                + " extension's features from " + oldId
                                                + " to " + newId + ".", e));
                            }
                        }
                    }
                }
            }
        }
        
        super.move(ctx);
    }

}
