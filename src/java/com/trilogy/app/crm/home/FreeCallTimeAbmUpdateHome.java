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
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.FreeCallTime;
import com.trilogy.app.crm.client.AbmBucketException;
import com.trilogy.app.crm.client.ProductAbmBucketClient;


/**
 * Updates ABM when a new FreeCallTime template is created or removed.
 *
 * @author gary.anderson@redknee.com
 */
public
class FreeCallTimeAbmUpdateHome
    extends HomeProxy
{
    /**
     * Creates a new FreeCallTimeAbmUpdateHome.
     *
     * @param ctx
     * @param delegate The Home to which we delegate.
     */
    public FreeCallTimeAbmUpdateHome(
        final Context ctx, 
        final Home delegate)
    {
        super(ctx, delegate);
    }

    /**
     * {@inheritDoc}
     */
    public Object create(Context ctx,final Object obj)
        throws HomeException
    {
        final FreeCallTime proposedTemplate = (FreeCallTime)obj;
        final FreeCallTime createdTemplate;

        try
        {
            final ProductAbmBucketClient client = getClient(ctx);

            if(client==null)
            {
                throw new HomeException("Failed to create new template. Cannot find ProductAbmBucketClient in context.");
            }
            
            final long identifier = client.createTemplate(proposedTemplate);

            proposedTemplate.setIdentifier(identifier);

            createdTemplate = (FreeCallTime)super.create(ctx,proposedTemplate);
        }
        catch (final AbmBucketException exception)
        {
            throw new HomeException("Failed to create new template.", exception);
        }

        return createdTemplate;
    }


    /**
     * {@inheritDoc}
     */
    public void remove(Context ctx, final Object obj)
        throws HomeException
    {
        final FreeCallTime template = (FreeCallTime)obj;

        try
        {
            final ProductAbmBucketClient client = getClient(ctx);

            client.deleteTemplate(template.getIdentifier());
        }
        catch (final AbmBucketException exception)
        {
            new MinorLogMsg(
                this,
                "Failed to delete ABM bucket " + template.getIdentifier(),
                exception).log(ctx);
        }

        super.remove(ctx,template);
    }


    /**
     * {@inheritDoc}
     */
    public Object store(Context ctx,final Object obj)
        throws HomeException
    {
        final FreeCallTime template = (FreeCallTime)obj;

        final UnsupportedOperationException rootException =
            new UnsupportedOperationException(
                "ABM does not support the modification of FCTTs.");

        throw new HomeException(
            "Failed to delete Free Call Time Template " + template.getIdentifier(),
            rootException);
    }


    /**
     * Gets the client used to communicate with ABM.
     *
     * @return The client used to communicate with ABM.
     */
    private ProductAbmBucketClient getClient(Context ctx)
    {
        return (ProductAbmBucketClient)ctx.get(ProductAbmBucketClient.class);
    }

} // class
