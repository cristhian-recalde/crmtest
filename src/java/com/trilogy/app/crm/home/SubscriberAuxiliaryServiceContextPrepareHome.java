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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.visitor.RemoveAllVisitor;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * Install old/new versions of bean in context to detect changes.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.8/9.0
 */
public class SubscriberAuxiliaryServiceContextPrepareHome extends HomeProxy
{

    public SubscriberAuxiliaryServiceContextPrepareHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        if (obj instanceof SubscriberAuxiliaryService)
        {
            SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) obj;
            
            ctx = ctx.createSubContext();

            ctx.put(SubscriberAuxiliaryService.class, obj);

            final Subscriber subscriber = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, association);
            ctx.put(Subscriber.class, subscriber);
        }
        
        return super.create(ctx, obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        if (obj instanceof SubscriberAuxiliaryService)
        {
            SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) obj;
            
            final SubscriberAuxiliaryService oldAssociation = (SubscriberAuxiliaryService) this.find(ctx, obj);
            if (oldAssociation == null)
            {
                throw new HomeException("Cannot find existing SubsccriberAuxiliaryService " + association.getIdentifier());
            }

         //   oldAssociation.freeze();
            
            ctx = ctx.createSubContext();

            ctx.put(SubscriberAuxiliaryService.class, obj);

            final Subscriber subscriber = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, association);
            ctx.put(Subscriber.class, subscriber);
            
            ctx.put(Lookup.OLD_SUBSCRIBER_AUXILIARY_SERVICE, oldAssociation);
        }
        
        return super.store(ctx, obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        if (obj instanceof SubscriberAuxiliaryService)
        {
            SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) obj;
            
            final SubscriberAuxiliaryService oldAssociation = (SubscriberAuxiliaryService) this.find(ctx, obj);
            if (oldAssociation == null)
            {
                // Nothing to do.  The association is already removed.
                return;
            }
            
           // oldAssociation.freeze();

            ctx = ctx.createSubContext();

            ctx.put(SubscriberAuxiliaryService.class, obj);

            final Subscriber subscriber = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, association);
            ctx.put(Subscriber.class, subscriber);

            ctx.put(Lookup.OLD_SUBSCRIBER_AUXILIARY_SERVICE, oldAssociation);
        }
        
        super.remove(ctx, obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAll(Context ctx, Object where) throws HomeException, HomeInternalException,
            UnsupportedOperationException
    {
        // This is to ensure that the context prepare home gets invoked for removeAll calls.
        // If it does not, then downstream homes will not work properly
        forEach(ctx, new RemoveAllVisitor(this), where);
    }
    
}
