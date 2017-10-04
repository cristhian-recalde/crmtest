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
package com.trilogy.app.crm.api.rmi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistory;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryXInfo;
import com.trilogy.app.crm.provision.CommonProvisionAgentBase;
import com.trilogy.app.crm.provision.service.param.CommandID;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionStateEnum;


/**
 * Adapts Subscriber object to API objects.
 *
 * @author victor.stratan@redknee.com
 */
public class SubscriberToApiAdapter implements Adapter
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * {@inheritDoc}
     */
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptSubscriberToReference(ctx, (Subscriber) obj, new SubscriptionReference());
    }


    /**
     * {@inheritDoc}
     */
    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    /**
     * Adapts a CRM subscriber into an API subscriber reference.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            CRM Subscriber to be adapted.
     * @return The adapted API Subscriber reference.
     */
    public static SubscriptionReference adaptSubscriberToReference(final Context ctx, final Subscriber sub)
    {
        return adaptSubscriberToReference(ctx, sub, new SubscriptionReference());
    }


    /**
     * Adapts a CRM subscriber into an API subscriber reference.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            CRM Subscriber to be adapted.
     * @param subscriptionRef
     *            The subscriber reference to be adapted to.
     * @return The adapted API Subscriber reference.
     */
    public static SubscriptionReference adaptSubscriberToReference(final Context ctx, final Subscriber sub,
        final SubscriptionReference subscriptionRef)
    {
        subscriptionRef.setIdentifier(sub.getId());

        subscriptionRef.setAccountID(sub.getBAN());

        subscriptionRef.setSpid(Integer.valueOf(sub.getSpid()));

        subscriptionRef.setState(SubscriptionStateEnum.valueOf(sub.getStateWithExpired().getIndex()));

        subscriptionRef.setMobileNumber(sub.getMSISDN());
        
        subscriptionRef.setSubscriptionType(Long.valueOf(sub.getSubscriptionType()).intValue());

        try
        {
            final And condition = new And();
            condition.add(new EQ(MsisdnMgmtHistoryXInfo.TERMINAL_ID, sub.getMSISDN()));
            condition.add(new EQ(MsisdnMgmtHistoryXInfo.SUBSCRIBER_ID, sub.getId()));
            condition.add(new EQ(MsisdnMgmtHistoryXInfo.SUBSCRIPTION_TYPE, sub.getSubscriptionType()));
            
            final Collection<MsisdnMgmtHistory> collection = HomeSupportHelper.get(ctx).getBeans(ctx, MsisdnMgmtHistory.class, condition);

            long time = 0;
            MsisdnMgmtHistory history = null;
            for (final MsisdnMgmtHistory item : collection)
            {
                if (item.getTimestamp().getTime() > time)
                {
                    time = item.getTimestamp().getTime();
                    history = item;
                }
            }

            if (history != null)
            {
                subscriptionRef.setMobileNumberOwnership(CalendarSupportHelper.get(ctx).dateToCalendar(history.getTimestamp()));
            }
            
            Context appCtx = (Context) ctx.get("app");
            int hlrExceptionCode =  appCtx.getInt(CommonProvisionAgentBase.SPG_PROVISIONING_EXAPTION_CODE);
            String hlrException = (String) appCtx.get(CommonProvisionAgentBase.SPG_PROVISIONING_EXAPTION);
            appCtx.remove(CommonProvisionAgentBase.SPG_PROVISIONING_EXAPTION_CODE);
            appCtx.remove(CommonProvisionAgentBase.SPG_PROVISIONING_EXAPTION);
            if(hlrException != null || hlrExceptionCode > 0)
            {
            	GenericParameter[] outGenericParameters = {};
       		 	List<GenericParameter> genericParamList = new ArrayList<GenericParameter>();
       		 	genericParamList.add(RmiApiSupport.createGenericParameter(CommonProvisionAgentBase.SPG_PROVISIONING_EXAPTION_CODE,hlrExceptionCode));
       		 	genericParamList.add(RmiApiSupport.createGenericParameter(CommonProvisionAgentBase.SPG_PROVISIONING_EXAPTION,hlrException));
                outGenericParameters = genericParamList.toArray(new GenericParameter[genericParamList.size()]);
                subscriptionRef.setParameters(outGenericParameters);
            }
            
        }
        catch (final Exception e)
        {
            LogSupport.minor(ctx, SubscriberToApiAdapter.class, "Unable to get MSISDN date", e);
        }

        return subscriptionRef;
    }
}
