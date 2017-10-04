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
package com.trilogy.app.crm.external.ecp;

import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AbstractHome;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.osa.ecp.provision.SubsProfile;

import com.trilogy.app.crm.client.AppEcpClient;
import com.trilogy.app.crm.client.exception.ECPReturnCodeMsgMapping;


/**
 * Adapts subscribers from ECP into {@link EcpSubscriber} bean.
 *
 * @author paul.sperneac@redknee.com
 * @since Aug 1, 2007
 */
public class EcpServiceHome extends AbstractHome implements Adapter
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>EcpServiceHome</code>.
     *
     * @param ctx
     *            The operating context.
     */
    public EcpServiceHome(final Context ctx)
    {
        super(ctx);
    }


    /**
     * {@inheritDoc}
     */
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        if (obj == null || getService(ctx) == null)
        {
            return null;
        }

        final SubsProfile profile = (SubsProfile) adapt(ctx, obj);

        final int ret = getService(ctx).addSubscriber(profile);
        if (ret != 0)
        {
            throw new HomeException("Unable to create URCS voice profile with MSISDN " + profile.msisdn + ": " + ECPReturnCodeMsgMapping.getMessage(ret));
        }

        return obj;
    }


    /**
     * {@inheritDoc}
     */
    public Object store(final Context ctx, final Object obj)
    {
        if (obj == null || getService(ctx) == null)
        {
            return null;
        }

        final SubsProfile profile = (SubsProfile) adapt(ctx, obj);

        getService(ctx).updateSubscriber(profile);

        return obj;
    }


    /**
     * {@inheritDoc}
     */
    public Object find(final Context ctx, final Object obj) throws HomeException
    {
        if (obj == null || getService(ctx) == null)
        {
            return null;
        }

        String msisdn = "";
        if (obj instanceof String)
        {
            msisdn = (String) obj;
        }
        else if (obj instanceof EcpSubscriber)
        {
            msisdn = ((EcpSubscriber) obj).getMsisdn();
        }
        try
        {
            final SubsProfile profile = getService(ctx).getSubsProfile(msisdn);
            return unAdapt(ctx, profile);
        }
        catch (final IllegalStateException exception)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                final StringBuilder sb = new StringBuilder();
                sb.append(exception.getClass().getSimpleName());
                sb.append(" caught in ");
                sb.append("EcpServiceHome.find(): ");
                if (exception.getMessage() != null)
                {
                    sb.append(exception.getMessage());
                }
                LogSupport.debug(ctx, this, sb.toString(), exception);
            }
            throw new HomeException("Cannot connect to ECP when attempting to find subscriber", exception);
        }
    }


    /**
     * {@inheritDoc}
     */
    public void remove(final Context ctx, final Object obj)
    {
        if (obj == null)
        {
            return;
        }

        if (getService(ctx) == null)
        {
            return;
        }

        getService(ctx).deleteSubscriber(((EcpSubscriber) obj).getMsisdn());
    }


    /**
     * Retrieves the ECP provisioning client.
     *
     * @param ctx
     *            The operating context.
     * @return The ECP provisioning client.
     */
    private AppEcpClient getService(final Context ctx)
    {
        return (AppEcpClient) ctx.get(AppEcpClient.class);
    }


    /**
     * {@inheritDoc}
     */
    public Collection select(final Context ctx, final Object obj)
    {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Home where(final Context ctx, final Object where)
    {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     */
    public void removeAll(final Context ctx, final Object where)
    {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     */
    public Visitor forEach(final Context ctx, final Visitor visitor, final Object where)
    {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void drop(final Context ctx)
    {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     */
    public Object adapt(final Context ctx, final Object obj)
    {
        if (obj == null)
        {
            return null;
        }

        final EcpSubscriber sub = (EcpSubscriber) obj;
        final SubsProfile profile = new SubsProfile();

        profile.classOfService = sub.getClassOfService();
        profile.currencyType = sub.getCurrency();
        profile.billingNumber = sub.getBillingNumber();
        profile.imsi = sub.getImsi();
        profile.language = sub.getLanguage();
        profile.msisdn = sub.getMsisdn();
        profile.ratePlan = sub.getPricePlan();
        profile.timeRegionID = sub.getRegionTimeId();
        profile.spid = sub.getSpid();
        profile.state = sub.getState().getIndex();

        return profile;
    }


    /**
     * {@inheritDoc}
     */
    public Object unAdapt(final Context ctx, final Object obj)
    {
        if (obj == null)
        {
            return null;
        }

        final SubsProfile profile = (SubsProfile) obj;
        final EcpSubscriber sub = new EcpSubscriber();

        sub.setClassOfService(profile.classOfService);
        sub.setCurrency(profile.currencyType);
        sub.setBillingNumber(profile.billingNumber);
        sub.setImsi(profile.imsi);
        sub.setLanguage(profile.language);
        sub.setMsisdn(profile.msisdn);
        sub.setPricePlan(profile.ratePlan);
        sub.setRegionTimeId(profile.timeRegionID);
        sub.setSpid(profile.spid);
        sub.setState(EcpSubscriberStateEnum.get((short) profile.state));

        return sub;
    }
}
