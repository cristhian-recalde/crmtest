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
package com.trilogy.app.crm.client.urcs;

import org.omg.CORBA.SystemException;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.AbstractCrmClient;
import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.app.urcs.promotion.IdentificationType;
import com.trilogy.app.urcs.promotion.PromotionErrorCode;
import com.trilogy.app.urcs.promotion.PromotionIDSetHolder;
import com.trilogy.app.urcs.promotion.PromotionProvision;
import com.trilogy.app.urcs.promotion.PromotionStatus;
import com.trilogy.app.urcs.promotion.PromotionStatusHolder;
import com.trilogy.app.urcs.promotion.PromotionStatusSetHolder;
import com.trilogy.app.urcs.promotion.SubscriberIdentity;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.util.snippet.log.Logger;

/**
 * 
 * Refactored reusable elements to an abstract class AbstractCrmClient<T>
 * @author rchen
 * @since June 24, 2009 
 */
public final class PromotionProvisionClientImpl extends AbstractCrmClient<PromotionProvision> implements PromotionProvisionClient
{
    private static final Class<PromotionProvision> CORBA_CLIENT_KEY = PromotionProvision.class;
    private static final String URCS_SERVICE_NAME = "PromotionProvisionClient";
    private static final String URCS_SERVICE_DESC = "CORBA client for Promotion Provision services";
    private static final String FAILED_MESSAGE_PREFIX = "CORBA comunication failure during ";
    private static final String FAILED_MESSAGE_SUFFIX = " for subscription ";
//    private static final int SERVICE_UP_TRAP_ID = 9998;
//    private static final int SERVICE_DOWN_TRAP_ID = 9999;
    
    //private final RemoteServiceStatusImpl<PromotionProvision> _serviceStatus;
    
    public PromotionProvisionClientImpl(final Context ctx)
    {
        //_serviceStatus = new RemoteServiceStatusImpl<PromotionProvision>(ctx, URCS_SERVICE_NAME, URCS_SERVICE_NAME, CORBA_CLIENT_KEY);
        super(ctx, URCS_SERVICE_NAME, URCS_SERVICE_DESC, CORBA_CLIENT_KEY);
    }

    public void setSubscriptionPromotions(final Context ctx, final Subscriber subscription,
            final long[] addOptions, final long[] removeOptions) throws RemoteServiceException
    {
        final String methodName = "setSubscriptionPromotions";

        final PromotionProvision client = getClient(ctx);
        final SubscriberIdentity identity = getSubscriberIdentityFromSubscription(subscription);
        final int resultCode;
        try
        {
            resultCode = client.setSubscriberOptions(identity, addOptions, removeOptions);
        }
        catch (final Throwable exception)
        {
            throw new RemoteServiceException(PromotionErrorCode.CONNECTION_ERROR,
                    FAILED_MESSAGE_PREFIX + methodName + FAILED_MESSAGE_SUFFIX + subscription.getId(), exception);
        }

        if (resultCode != PromotionErrorCode.SUCCESSFUL)
        {
            final String msg = "Failure [rc:" + resultCode
                    + "] during subscription [" + subscription.getId() + "] promotions provisioning";
            if (Logger.isInfoEnabled(ctx))
            {
                Logger.info(ctx, this, msg);
            }
            throw new RemoteServiceException(resultCode, msg);
        }
    }

    public long[] listSubscriptionPromotions(final Context ctx, final Subscriber subscription)
        throws RemoteServiceException
    {
        final String methodName = "listSubscriptionPromotions";

        final PromotionProvision client = getClient(ctx);
        final SubscriberIdentity identity = getSubscriberIdentityFromSubscription(subscription);
        final PromotionIDSetHolder holder = new PromotionIDSetHolder();
        final int resultCode;

        try
        {
            resultCode = client.listAllPromotions(identity, holder);
        }
        catch (final Throwable exception)
        {
            throw new RemoteServiceException(PromotionErrorCode.CONNECTION_ERROR,
                    FAILED_MESSAGE_PREFIX + methodName + FAILED_MESSAGE_SUFFIX + subscription.getId(), exception);
        }

        if (resultCode == PromotionErrorCode.PROMOTION_NOT_FOUND)
        {
            holder.value = new long[0];
        }
        else if (resultCode != PromotionErrorCode.SUCCESSFUL)
        {
            final String msg = "Failure [rc:" + resultCode
                    + "] while listing promotions for subscription " + subscription.getId();
            if (Logger.isInfoEnabled(ctx))
            {
                Logger.info(ctx, this, msg);
            }
            throw new RemoteServiceException(resultCode, msg);
        }

        return holder.value;
    }

    public PromotionStatus[] listSubscriptionPromotionStatus(final Context ctx, final Subscriber subscription)
        throws RemoteServiceException
    {
        final String methodName = "listSubscriptionPromotionStatus";

        final PromotionProvision client = getClient(ctx);
        final SubscriberIdentity identity = getSubscriberIdentityFromSubscription(subscription);
        final PromotionStatusSetHolder holder = new PromotionStatusSetHolder();
        final int resultCode;
        try
        {
            resultCode = client.listAllPromotionStatus(identity, holder);
        }
        catch (final Throwable exception)
        {
            throw new RemoteServiceException(PromotionErrorCode.CONNECTION_ERROR,
                    FAILED_MESSAGE_PREFIX + methodName + FAILED_MESSAGE_SUFFIX + subscription.getId(), exception);
        }

        if (resultCode == PromotionErrorCode.PROMOTION_NOT_FOUND)
        {
            holder.value = new PromotionStatus[0]; 
        }
        else if (resultCode != PromotionErrorCode.SUCCESSFUL)
        {
            final String msg = "Failure [rc:" + resultCode
                    + "] while listing promotions status for subscription " + subscription.getId();
            if (Logger.isInfoEnabled(ctx))
            {
                Logger.info(ctx, this, msg);
            }
            throw new RemoteServiceException(resultCode, msg);
        }

        return holder.value;
    }

    public PromotionStatus getSubscriptionPromotionStatus(final Context ctx, final Subscriber subscription,
            final long promotionId) throws RemoteServiceException
    {
        final String methodName = "getSubscriptionPromotionStatus";

        final PromotionProvision client = getClient(ctx);
        final SubscriberIdentity identity = getSubscriberIdentityFromSubscription(subscription);
        final PromotionStatusHolder holder = new PromotionStatusHolder();
        final int resultCode;
        try
        {
            resultCode = client.getPromotionStatus(identity, promotionId, holder);
        }
        catch (final Throwable exception)
        {
            throw new RemoteServiceException(PromotionErrorCode.CONNECTION_ERROR,
                    FAILED_MESSAGE_PREFIX + methodName + FAILED_MESSAGE_SUFFIX + subscription.getId(), exception);
        }

        if (resultCode != PromotionErrorCode.SUCCESSFUL)
        {
            final String msg = "Failure [rc:" + resultCode
                    + "] during subscription [" + subscription.getId() + "] promotion [" + promotionId + "] status.";
            if (Logger.isInfoEnabled(ctx))
            {
                Logger.info(ctx, this, msg);
            }
            throw new RemoteServiceException(resultCode, msg);
        }

        return holder.value;
    }

    private SubscriberIdentity getSubscriberIdentityFromSubscription(final Subscriber subscription)
    {
        final SubscriberIdentity identity = new SubscriberIdentity();
        identity.idType = IdentificationType.MSISDN;
        identity.identifier = subscription.getMSISDN();
        identity.subscriptionType = (int) subscription.getSubscriptionType();

        return identity;
    }


}
