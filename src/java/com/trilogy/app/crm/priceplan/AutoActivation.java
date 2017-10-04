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
package com.trilogy.app.crm.priceplan;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.IsNull;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionXInfo;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.provision.UpdatedPricePlanHandler;
import com.trilogy.app.crm.support.PricePlanSupport;

/**
 * Performs the work of looking through existing PricePlanVersions to find those that should be activated.
 * 
 * @author gary.anderson@redknee.com
 * @author cindy.wong@redknee.com
 */
public class AutoActivation
{
    /**
     * Creates a new AutoActivation for the given date.
     * 
     * @param context
     *            The operating context.
     * @param date
     *            The date of activation for auto-activating price plan versions.
     */
    public AutoActivation(final Context context, final Date date)
    {
        context_ = context;
        activationDate_ = date;
    }

    /**
     * Processes each of the activations.
     */
    public void processAllActivations()
    {

        Home versionHome = (Home) getContext().get(PricePlanVersionHome.class);

        final Map versionMap = new TreeMap();

        try
        {
            // [Cindy Wong] Change to elang predicate
            final And and = new And();
            and.add(new IsNull(PricePlanVersionXInfo.ACTIVATION));
            and.add(new LTE(PricePlanVersionXInfo.ACTIVATE_DATE, getActivationDate()));
            versionHome.where(getContext(), and).forEach(getContext(), new Visitor()
            {

                @Override
                public void visit(final Context ctx, final Object obj) throws AgentException, AbortVisitException
                {
                    PricePlanVersion version = (PricePlanVersion) obj;

                    Long key = Long.valueOf(version.getId());
                    PricePlanVersion selectedVersion = (PricePlanVersion) versionMap.get(key);

                    if (selectedVersion == null)
                    {
                        versionMap.put(key, version);
                    }
                    else
                    {
						    /**
						     * This is a case where a price plan has more than
						     * one version that has not been activated for
						     * some system error. We are only interested in the
						     * most recent version so we will discard the
						     * others
						     */
                        if (selectedVersion.getVersion() < version.getVersion())
                        {
                            versionMap.put(key, version);
                        }
                    }

                }
            });
        }
        catch (Exception e)
        {
            LogSupport.major(getContext(), this, "Error processing price plan activations", e);
        }

        for (Iterator it = versionMap.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry entry = (Map.Entry) it.next();
            Long key = (Long) entry.getKey();
            PricePlanVersion version = (PricePlanVersion) entry.getValue();

            activate(key.longValue(), version);
        }

    }

    /**
     * Activate the most recent of the given list of PricePlanVersions.
     * 
     * @param planIndentifier
     *            The identifier of the plan to which the versions belong.
     * @param version
     *            A list of PricePlanVersion, all for the same PricePlan, that need to be activated.
     */
    protected void activate(final long planIndentifier, final PricePlanVersion version)
    {
        try
        {
            final PricePlan plan = PricePlanSupport.getPlan(getContext(), planIndentifier);
            
            if(null != plan)
            {
            	
                if (plan.getCurrentVersion() < version.getVersion())
                {
                	com.redknee.framework.xhome.msp.MSP.setBeanSpid(getContext(), plan.getSpid());
                    activate(plan, version);
                }
                else
                {
                    LogSupport.major(getContext(), this, "There are one or more price plans versions that were skiped "
                            + "because of a more recent version for price plan id : " + plan.getId());
                }
            } else
            {
                LogSupport.major(getContext(), this, "There are one or more price plans versions that were skiped "
                        + "because price plan entry either does not exist or is in invalid stae : " + plan.getId());
            }
            
        }
        catch (final HomeException exception)
        {
            new MajorLogMsg(this, "Failed to activate new version of price plan " + planIndentifier, exception)
                .log(getContext());
        }
    }

    /**
     * Activate the given PricePlanVersion.
     * 
     * @param plan
     *            The plan to which the version belongs.
     * @param version
     *            The version to make the current, active version.
     * @exception HomeException
     *                Thrown if there are problems accessing Home data in the context.
     */
    protected void activate(final PricePlan plan, final PricePlanVersion version) throws HomeException
    {
        final UpdatedPricePlanHandler handler = new UpdatedPricePlanHandler(getContext(), plan, version);

        handler.createRequests();
        handler.updatePricePlan();
    }

    /**
     * Gets the date of activation for auto-activating price plan versions.
     * 
     * @return The date of activation for auto-activating price plan versions.
     */
    protected Date getActivationDate()
    {
        return activationDate_;
    }

    /**
     * Gets the operating context.
     * 
     * @return The operating context.
     */
    protected Context getContext()
    {
        return context_;
    }

    /**
     * The operating context.
     */
    private final Context context_;

    /**
     * The date of activation for auto-activating price plan versions.
     */
    private final Date activationDate_;

} // class
