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
package com.trilogy.app.crm.subscriber.provision.ipc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.LRUCachingHome;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.ipc.IpcRatePlan;
import com.trilogy.app.crm.technology.TechnologyEnum;


/**
 * Provides a Home that acts as a switch, returning either the standard IPCG
 * rate plans, or the CDMA specific rate plans, depending on the type of
 * PricePlan in the context.  This Home is meant specifically for use by the
 * Data rate plan drop-down in the PricePlanVersion web UI.  Adding this switch
 * was the cleanest way to introduce multiple rate plan providers when CDMA
 * specific IPCG was added.
 *
 * @author gary.anderson@redknee.com
 */
public
class IpcRatePlanSwitchHome
    extends HomeProxy
{
    /**
     * Creates a new IpcRatePlanSwitchHome and sets-up the delegates between
     * which Home calls are switched.
     *
     * @param context The operating context.
     */
    public IpcRatePlanSwitchHome(final Context context)
    {
        new InfoLogMsg(this, "Creating IpcRatePlanSwitchHome.", null).log(context);

        corbaHomeMap_ = new HashMap();

        final Iterator technologyIterator = TechnologyEnum.COLLECTION.iterator();

        while (technologyIterator.hasNext())
        {
            final TechnologyEnum technology = (TechnologyEnum)technologyIterator.next();

            Home home = new IpcRatePlanCorbaHome(context, technology);
            home = new LRUCachingHome(context, IpcRatePlan.class, true, home);

            corbaHomeMap_.put(technology, home);
        }

        new InfoLogMsg(this, "Creation of IpcRatePlanSwitchHome completed without exception.", null).log(context);
    }


    /**
     * This is where the switching occurs.  We look for a BEAN in the context,
     * which should be the PricePlanVersion for which the rate plans are needed.
     * We then return the Home specific to the Technology type of that version.
     *
     * {@inheritDoc}
     */
    public Home getDelegate(final Context context)
    {
        debug(context, "Looking up PricePlan.");

        final PricePlan plan = getPricePlan(context);

        debug(context, "Done looking up PricePlan", plan);
        debug(context, "Looking up Home delegate.");

        final Home delegate = (Home)corbaHomeMap_.get(plan.getTechnology());

        debug(context, "Done looking up Home delegate: ", delegate);

        return delegate;
    }


    /**
     * This method attemps to find the PricePlan BEAN in the context by walking
     * up the context hierarchy until one is found.
     *
     * @param context The operating context.
     * @return The first PricePlan BEAN found when searching up the context
     * hierarchy
     */
    private PricePlan getPricePlan(final Context context)
    {
        PricePlan plan = null;

        Context currentContext = context;
        while (currentContext != null)
        {
            final Object bean = currentContext.get(AbstractWebControl.BEAN);
            if (bean instanceof PricePlan)
            {
                plan = (PricePlan)bean;
                break;
            }

            currentContext = (Context)currentContext.get("..");
        }

        return plan;
    }


    /**
     * Provides a convenient method of generating debugging messages.
     *
     * @param context The operating context.
     * @param message The message to write to the log.
     */
    private void debug(final Context context, final String message)
    {
        if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, message, null).log(context);
        }
    }


    /**
     * Provides a convenient method of generating debugging messages.
     *
     * @param context The operating context.
     * @param message The message to write to the log.
     * @param plan The PricePlan for which to include information.
     */
    private void debug(
        final Context context,
        final String message,
        final PricePlan plan)
    {
        if (LogSupport.isDebugEnabled(context))
        {
            final String fullMessage;
            if (plan == null)
            {
                fullMessage = " -- No PricePlan found.";
            }
            else
            {
                fullMessage =
                    message
                    + " -- PricePlan["
                    + plan.getId()
                    + ", Technology: "
                    + plan.getTechnology() + "]";
            }

            debug(context, fullMessage);
        }
    }


    /**
     * Provides a convenient method of generating debugging messages.
     *
     * @param context The operating context.
     * @param message The message to write to the log.
     * @param home The Home for which to include information.
     */
    private void debug(
        final Context context,
        final String message,
        final Home home)
    {
        if (LogSupport.isDebugEnabled(context))
        {
            final String fullMessage = message + " -- Home: " + home;
            debug(context, fullMessage);
        }
    }


    /**
     * The capacity of the caches used on the underlying homes.
     */
    private static final int CACHE_CAPACITY = 100;

    /**
     * The Map of TechnologyEnum to specific CORBA Home.
     */
    private final Map corbaHomeMap_;

} // class
