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
package com.trilogy.app.crm.subscriber.provision.ecp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.AbstractClassAwareHome;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.service.rating.RatePlanInfo;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.ecp.rateplan.ECPRatePlan;
import com.trilogy.app.crm.client.EcpRatePlanClient;
import com.trilogy.app.crm.config.AppEcpClientConfig;


/**
 * A read-only Home adaptation of ECP rate plan queries.
 *
 * @author yassir.pakran@redknee.com
 */
public class EcpRatePlanCorbaHome extends AbstractClassAwareHome
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>EcpRatePlanCorbaHome</code>.
     *
     * @param ctx
     *            The operating context.
     */
    public EcpRatePlanCorbaHome(final Context ctx)
    {
        super(ctx, ECPRatePlan.class);
    }


    /**
     * {@inheritDoc}
     */
    public Object create(final Context ctx, final Object obj)
    {
        throw new UnsupportedOperationException("NOP");
    }


    /**
     * {@inheritDoc}
     */
    public Object store(final Context ctx, final Object obj)
    {
        throw new UnsupportedOperationException("NOP");
    }


    /**
     * {@inheritDoc}
     */
    public Object find(final Context ctx, final Object obj) throws HomeException
    {
        final Collection col = select(ctx, True.instance());
        final Integer key = (Integer) obj;
        for (final Iterator it = col.iterator(); it.hasNext();)
        {
            final ECPRatePlan plan = (ECPRatePlan) it.next();
            if (plan.getRatePlanId() == key.intValue())
            {
                return plan;
            }
        }

        throw new HomeException("Rate Plan id not found");
    }


    /**
     * {@inheritDoc}
     */
    public Collection select(final Context ctx, final Object obj) throws HomeException
    {
        final EcpRatePlanClient appEcpClient = (EcpRatePlanClient) ctx.get(EcpRatePlanClient.class);
        final List ratePlans = new ArrayList();
        try
        {
            // User user = (User) ctx.get(Principal.class);
            final Context parentContext = (Context) ctx.get("..");
            final PricePlan pPlan = (PricePlan) parentContext.get(AbstractWebControl.BEAN);
            final RatePlanInfo[] plans = appEcpClient.getRatePlans(pPlan.getSpid());

            /*
             * Get the Rate plan matching reg exp from the config We ignore all rate plans
             * with ID NOT matching the reg exp
             */
            final AppEcpClientConfig config = (AppEcpClientConfig) ctx.get(AppEcpClientConfig.class);

            if (config == null)
            {
                throw new HomeException("Could not get the ECP Client Configuration from context.");
            }

            final Pattern p = Pattern.compile(config.getRatePlanMatchExp());

            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Matching against reg exp " + config.getRatePlanMatchExp(), null).log(ctx);
            }

            for (final RatePlanInfo plan : plans)
            {

                final Matcher m = p.matcher(plan.ratePlanId);
                if (m.matches())
                {
                    /*
                     * Fetch the numeric ID from the string
                     */
                    final Pattern numericP = Pattern.compile("[\\d]+");
                    final Matcher numericM = numericP.matcher(plan.ratePlanId);
                    if (numericM.find())
                    {
                        /*
                         * Get the string that matched the numeric pattern
                         */
                        final String ratePlan = numericM.group();
                        final int rPlan = Integer.parseInt(ratePlan);

                        final ECPRatePlan ep = new ECPRatePlan();
                        ep.setRatePlanId(rPlan);
                        ep.setSpid(plan.spId);
                        ep.setDescription(plan.desc);

                        ratePlans.add(ep);
                    }
                }

            }
        }
        catch (final PatternSyntaxException e)
        {
            throw new HomeException("ECP Rate Plan ID Matching Regular Expression is malformed." + e.getMessage(), e);
        }
        catch (final Exception e)
        {
            throw new HomeException("Unable to retrieve rating plans from ECP error : " + e.getMessage(), e);
        }

        return ratePlans;
    }


    /**
     * {@inheritDoc}
     */
    public void remove(final Context ctx, final Object obj)
    {
        throw new UnsupportedOperationException("NOP");
    }


    /**
     * {@inheritDoc}
     */
    public void removeAll(final Context ctx, final Object obj)
    {
        throw new UnsupportedOperationException("NOP");
    }


    /**
     * {@inheritDoc}
     */
    public Visitor forEach(final Context ctx, final Visitor visitor, final Object obj)
    {
        throw new UnsupportedOperationException("NOP");
    }

}
