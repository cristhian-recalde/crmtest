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
package com.trilogy.app.crm.client.urcs;

import org.omg.CORBA.SystemException;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.client.AbstractCrmClient;
import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.app.urcs.promotion.Counter;
import com.trilogy.app.urcs.promotion.CounterDelta;
import com.trilogy.app.urcs.promotion.CounterProfile;
import com.trilogy.app.urcs.promotion.Promotion;
import com.trilogy.app.urcs.promotion.PromotionErrorCode;
import com.trilogy.app.urcs.promotion.PromotionManagement;
import com.trilogy.app.urcs.promotion.PromotionSetHolder;
import com.trilogy.util.snippet.log.Logger;

/**
 * Wrapper class to implement the ExternalService interface to provide status and logic to ease access to methods.
 *
 * @author victor.stratan@redknee.com
 * 
 * Refactored reusable elements to an abstract class AbstractCrmClient<T>
 * @author rchen
 * @since June 24, 2009 
 */
public class PromotionManagementClientImpl extends AbstractCrmClient<PromotionManagement> implements PromotionManagementClient
{
    private static final Class<PromotionManagement> SERVICE_TYPE = PromotionManagement.class;

    private static final String FAILED_MESSAGE_PREFIX = "CORBA comunication failure during ";
    private static final String FAILED_MESSAGE_SUFFIX = " for subscription ";
    public static final String URCS_SERVICE_NAME = "PromotionManagementClient";
    public static final String URCS_SERVICE_DESCRIPTION = "CORBA client for Promotion Management services";

    public PromotionManagementClientImpl(final Context ctx)
    {
        super(ctx, URCS_SERVICE_NAME, URCS_SERVICE_DESCRIPTION, SERVICE_TYPE);
    }

    public Promotion[] listPromotions(final Context ctx, final int spid) throws RemoteServiceException
    {
        final String methodName = "listPromotions";

        final PromotionManagement client = getClient(ctx);
        final PromotionSetHolder holder = new PromotionSetHolder();
        final int resultCode;

        try
        {
            resultCode = client.listPrivatePromotions(spid, holder);
        }
        catch (final Throwable exception)
        {
            throw new RemoteServiceException(PromotionErrorCode.CONNECTION_ERROR,
                    FAILED_MESSAGE_PREFIX + methodName + FAILED_MESSAGE_SUFFIX, exception);
        }

        if (resultCode != PromotionErrorCode.SUCCESSFUL)
        {
            final String msg = "Failure [rc:" + resultCode + "] while listing promotions on URCS.";
            if (Logger.isInfoEnabled(ctx))
            {
                Logger.info(ctx, this, msg);
            }
            throw new RemoteServiceException(resultCode, msg);
        }

        return holder.value;
    }

    public void createPromotion(final Context ctx, final Promotion promotion)
    {
        throw new UnsupportedOperationException();
    }

    public void updatePromotion(final Context ctx, final Promotion promotion)
    {
        throw new UnsupportedOperationException();
    }

    public Promotion retrievePromotion(final Context ctx, final long promotionId)
    {
        throw new UnsupportedOperationException();
    }

    public void deletePromotion(final Context ctx, final long promotionId)
    {
        throw new UnsupportedOperationException();
    }

    public void createCounterProfile(final Context ctx, final CounterProfile counterProfile)
    {
        throw new UnsupportedOperationException();
    }

    public void updateCounterProfile(final Context ctx, final CounterProfile counterProfile)
    {
        throw new UnsupportedOperationException();
    }

    public CounterProfile retrieveCounterProfile(final Context ctx, final long profileId)
    {
        throw new UnsupportedOperationException();
    }

    public void deleteCounterProfile(final Context ctx, final long profileId)
    {
        throw new UnsupportedOperationException();
    }

    public void createCounter(final Context ctx, final String msisdn, final long subscriptionType, final long profileId,
            final Counter counter)
    {
        throw new UnsupportedOperationException();
    }

    public void upateCounter(final Context ctx, final Counter counter)
    {
        throw new UnsupportedOperationException();
    }

    public Counter[] updateCounters(final Context ctx, final CounterDelta[] deltas)
    {
        throw new UnsupportedOperationException();
    }

    public int retrieveCounter(final Context ctx, final String msisdn, final long subscriptionType)
    {
        throw new UnsupportedOperationException();
    }

    public int retrieveCounter2(final Context ctx, final long counterId)
    {
        throw new UnsupportedOperationException();
    }

    public void deleteCounter(final Context ctx, final String msisdn, final long subscriptionType)
    {
        throw new UnsupportedOperationException();
    }

    public void deleteCounter2(final Context ctx, final long counterId)
    {
        throw new UnsupportedOperationException();
    }

}