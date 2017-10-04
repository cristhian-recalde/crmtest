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
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.FreeCallTime;
import com.trilogy.app.crm.bean.FreeCallTimeHome;
import com.trilogy.app.crm.bean.FreeCallTimeIdentitySupport;
import com.trilogy.app.crm.log.FreeCallTimeTemplateCreationEventRecord;
import com.trilogy.app.crm.log.FreeCallTimeTemplateDeletionEventRecord;
import com.trilogy.app.crm.log.FreeCallTimeTemplateModificationEventRecord;


/**
 * Generates operational measurements (OMs) and event records (ERs) for the
 * FreeCallTime Home.
 *
 * @author gary.anderson@redknee.com
 */
public
class FreeCallTimeLoggingHome
    extends HomeProxy
{
    /**
     * Creates a new FreeCallTimeLoggingHome proxy.
     *
     * @param context The operating context.
     * @param delegate The Home to which we delegate.
     */
    public FreeCallTimeLoggingHome(
        final Context context,
        final Home delegate)
    {
        super(context, delegate);
    }

    /**
     * {@inheritDoc}
     */
    public Object create(Context ctx,final Object obj)
        throws HomeException
    {
        Common.OM_FREE_CALL_TIME_TEMPLATE_CREATION.attempt(ctx);

        FreeCallTime result = null;
        boolean success = false;

        try
        {
            result = (FreeCallTime)super.create(ctx,obj);
            success = true;
        }
        catch (final HomeException exception)
        {
            new MinorLogMsg(this,exception.getMessage(),exception).log(ctx);

            throw exception;
        }
        finally
        {
            if (success)
            {
                Common.OM_FREE_CALL_TIME_TEMPLATE_CREATION.success(ctx);
                new FreeCallTimeTemplateCreationEventRecord(result).generate(ctx);
            }
            else
            {
                Common.OM_FREE_CALL_TIME_TEMPLATE_CREATION.failure(ctx);
            }
        }

        return result;
    }


    /**
     * {@inheritDoc}
     */
    public Object store(Context ctx,final Object obj)
        throws HomeException
    {
        Common.OM_FREE_CALL_TIME_TEMPLATE_MODIFICATION.attempt(ctx);

        final FreeCallTime newTemplate = (FreeCallTime)obj;
        FreeCallTime oldTemplate = null;
        boolean success = false;

        Object ret=null;
        
        try
        {
            oldTemplate = getExistingTemplate(ctx,newTemplate);

            ret=super.store(ctx,newTemplate);
            success = true;
        }
        catch (final HomeException exception)
        {
            new MinorLogMsg(
                this,
                exception.getMessage(),
                exception).log(ctx);

            throw exception;
        }
        finally
        {
            if (success)
            {
                Common.OM_FREE_CALL_TIME_TEMPLATE_MODIFICATION.success(ctx);
                new FreeCallTimeTemplateModificationEventRecord(
                    oldTemplate,
                    newTemplate).generate(ctx);
            }
            else
            {
                Common.OM_FREE_CALL_TIME_TEMPLATE_MODIFICATION.failure(ctx);
            }
        }
        
        return ret;
    }


    /**
     * {@inheritDoc}
     */
    public void remove(Context ctx, final Object obj)
        throws HomeException
    {
        Common.OM_FREE_CALL_TIME_TEMPLATE_DELETION.attempt(ctx);

        final FreeCallTime template = (FreeCallTime)obj;
        boolean success = false;

        try
        {
            super.remove(ctx,template);
            success = true;
        }
        catch (final HomeException exception)
        {
            new MinorLogMsg(
                this,
                exception.getMessage(),
                exception).log(ctx);

            throw exception;
        }
        finally
        {
            if (success)
            {
                Common.OM_FREE_CALL_TIME_TEMPLATE_DELETION.success(ctx);
                new FreeCallTimeTemplateDeletionEventRecord(template).generate(ctx);
            }
            else
            {
                Common.OM_FREE_CALL_TIME_TEMPLATE_DELETION.failure(ctx);
            }
        }
    }


    /**
     * Gets the existing template with the identity of the given modified
     * template.
     *
     * @param modifiedTemplate The modified template for which to get the
     * existing version.
     *
     * @return The existing template.
     *
     * @exception HomeException Thrown if there is a problem accessin the Home
     * information in the context.
     */
    private FreeCallTime getExistingTemplate(Context ctx,final FreeCallTime modifiedTemplate)
        throws HomeException
    {
        final Home home = (Home)ctx.get(FreeCallTimeHome.class);

        final Object key = FreeCallTimeIdentitySupport.instance().ID(modifiedTemplate);

        final FreeCallTime existingTemplate = (FreeCallTime)home.find(ctx,key);

        return existingTemplate;
    }
} // class
