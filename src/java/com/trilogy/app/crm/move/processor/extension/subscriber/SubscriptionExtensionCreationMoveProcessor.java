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
package com.trilogy.app.crm.move.processor.extension.subscriber;

import java.util.Collection;

import com.trilogy.app.crm.bean.LazyLoadBean;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.MovableExtension;
import com.trilogy.app.crm.extension.SubscriberTypeDependentExtension;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtension;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.MoveWarningException;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.SubscriptionExtensionMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionExtensionMoveRequestXInfo;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;


/**
 * This processor is responsible for setting the 'move in progress' flag
 * during the setup to prevent the ExtensionInstallationHome from executing
 * the install logic.  During the move it is responsible for creating a
 * copy of the subscription extension referencing the new sub's ID via Home
 * operations.  It does not permanently modify the subscriber extensions in
 * the request.
 * 
 * It only performs validation required to perform its duty.  No business use case
 * validation is performed here.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class SubscriptionExtensionCreationMoveProcessor<SEMR extends SubscriptionExtensionMoveRequest> extends MoveProcessorProxy<SEMR>
{
    public SubscriptionExtensionCreationMoveProcessor(MoveProcessor<SEMR> delegate)
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
        
        moveCtx.put(MovableExtension.MOVE_IN_PROGRESS_CTX_KEY, true);
        
        return moveCtx;
    }


    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        
        SEMR request = this.getRequest();
        
        Subscriber oldSub = request.getOriginalSubscription(ctx);
        if (oldSub == null)
        {
            cise.thrown(new IllegalPropertyArgumentException(
                    SubscriptionExtensionMoveRequestXInfo.OLD_SUBSCRIPTION_ID, 
                    "Subscription (ID=" + request.getOldSubscriptionId() + ") does not exist."));
        }
        else
        {
            Collection<Extension> extensions = request.getExtensions();
            if (extensions != null)
            {
                for (Extension ext : extensions)
                {
                    if (ext == null)
                    {
                        cise.thrown(new IllegalPropertyArgumentException(
                                SubscriptionExtensionMoveRequestXInfo.SUBSCRIPTION_EXTENSIONS, 
                                "Extension list has a null extension in it."));
                    }
                    else if (!(ext instanceof SubscriberExtension))
                    {
                        cise.thrown(new IllegalPropertyArgumentException(
                                SubscriptionExtensionMoveRequestXInfo.SUBSCRIPTION_EXTENSIONS, 
                                "Extension list has an invalid extension type in it."));
                    }
                    else
                    {
                        SubscriberExtension extension = (SubscriberExtension) ext;
                        if(!SafetyUtil.safeEquals(extension.getSubId(), request.getOldSubscriptionId()))
                        {
                            cise.thrown(new IllegalPropertyArgumentException(
                                    SubscriptionExtensionMoveRequestXInfo.SUBSCRIPTION_EXTENSIONS, 
                                    "Subscription (ID=" + request.getOldSubscriptionId() + ") does not match extension (ID=" + extension.getSubId() + ")."));   
                        }
                    }
                }
            }
        }
        
        Subscriber newSub = request.getNewSubscription(ctx);
        if (newSub == null)
        {
            cise.thrown(new IllegalPropertyArgumentException(
                    SubscriptionExtensionMoveRequestXInfo.NEW_SUBSCRIPTION_ID, 
                    "New subscription (ID=" + request.getNewSubscriptionId() + ") does not exist."));
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
        SEMR request = this.getRequest();

        Subscriber oldSubscription = request.getOriginalSubscription(ctx);
        Subscriber newSubscription = request.getNewSubscription(ctx);
        
        Collection<Extension> extensions = request.getExtensions();
        if (extensions != null)
        {
            for (Extension ext : extensions)
            {
                if (ext instanceof SubscriberExtension)
                {
                    SubscriberExtension extension = (SubscriberExtension) ext;
                    
                    if (extension instanceof SubscriberTypeDependentExtension)
                    {
                        if (!((SubscriberTypeDependentExtension) extension).isValidForSubscriberType(newSubscription.getSubscriberType()))
                        {
                            // When moving converting, do not copy subscriber type dependent extensions.
                            continue;
                        }
                    }
                    
                    if (extension instanceof LazyLoadBean)
                    {
                        ((LazyLoadBean) extension).lazyLoadAllProperties(ctx);
                    }
                    
                    // Set the new ID/BAN/SPID before creating the extension for the new subscription.
                    extension.setSubId(newSubscription.getId());
                    extension.setBAN(newSubscription.getBAN());
                    extension.setSpid(newSubscription.getSpid());
                    try
                    {
                        Home extensionHome = ExtensionSupportHelper.get(ctx).getExtensionHome(ctx, extension);
                        if (extensionHome != null)
                        {
                            extensionHome.create(ctx, extension);
                        }
                        else
                        {
                            request.reportWarning(ctx, new MoveWarningException(request, "Unable to move unsupported extension of type: " + (extension != null ? extension.getClass().getName() : null)));
                        }
                    }
                    catch (HomeException e)
                    {
                        throw new MoveException(request, "Error occurred creating " + extension.getName(ctx) + " for subscription " + request.getNewSubscriptionId(), e);
                    }
                    finally
                    {
                        // Reset the extension ID/BAN/SPID
                        extension.setSubId(oldSubscription.getId());
                        extension.setBAN(oldSubscription.getBAN());
                        extension.setSpid(oldSubscription.getSpid());   
                    }
                }
            }
        }
        
        super.move(ctx);
    }

}
