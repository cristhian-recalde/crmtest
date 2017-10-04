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
package com.trilogy.app.crm.client.bm;

import static com.redknee.app.crm.client.CorbaClientTrapIdDef.BALANCE_MANAGEMENT_SVC_DOWN;
import static com.redknee.app.crm.client.CorbaClientTrapIdDef.BALANCE_MANAGEMENT_SVC_UP;

import org.omg.CORBA.LongHolder;

import com.trilogy.app.crm.client.AbstractCrmClient;
import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.app.crm.support.UserSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.BCDChangeRequestReturnParam;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.PropertyReturn;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.SubscriberProfile;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.SubscriberProfileProvision;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.SubscriberProfileProvisionOperations;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.SubscriberRemovalReturnParam;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.SubscriberReturnParam;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.SubscriptionDetailReturnParam;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.SubscriptionProfile;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.SubscriptionReturnParam;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.SubscriptionsForSubscriberReturnParam;
import com.trilogy.product.bundle.manager.provision.common.param.Parameter;

/**
 * Provides access to the CORBA client for the SubscriberProfileProvision
 * interface.
 *
 * @author gary.anderson@redknee.com
 * 
 * Support clustered corba client
 * Refactored reusable elements to an abstract class AbstractCrmClient<T>
 * @author rchen
 * @since June 29, 2009 
 */
class SubscriberProfileProvisionCorbaClient extends AbstractCrmClient<SubscriberProfileProvision>
{
    /**
     * Creates a new CORBA client.
     *
     * @param fallbackContext The fallback Context to use when a locally
     * provided context is unavailable.
     */
    public SubscriberProfileProvisionCorbaClient(final Context fallbackContext)
    {
        super(fallbackContext, CONNECTION_PROPERTIES_KEY, "CORBA client for BM SubscriberProfileProvision API", 
                SubscriberProfileProvision.class, BALANCE_MANAGEMENT_SVC_DOWN, BALANCE_MANAGEMENT_SVC_UP);
    }

    // The public methods that follow are all contexualized versions of those in
    // the SubscriberProfileProvision API.
    //
    // TODO -- this proxying should be generated, and should be used
    // consistently across all CORBA connections.

    /**
     * @param context The operating context.
     * @param subscriberProfile The subscriber account's profile.
     * @param inParamSet Additional parameters.
     * @return Collection of results, including a return code.
     * @throws SubscriberProfileProvisionCorbaException Thrown if exceptions are
     * encountered at the CORBA level.
     */
    public SubscriberReturnParam createSubscriberProfile(final Context context,
        final SubscriberProfile subscriberProfile, final Parameter[] inParamSet)
        throws SubscriberProfileProvisionCorbaException
    {
        final String methodName = "createSubscriberProfile";

        final SubscriberProfileProvisionOperations service = getProvisionClient(context);

        final SubscriberReturnParam results;
        if (service != null)
        {
            try
            {
                results = service.createSubscriberProfile(subscriberProfile, inParamSet);
            }
            catch (final Throwable t)
            {
                throw new SubscriberProfileProvisionCorbaException(methodName + FAILED_MESSAGE_SUFFIX, t);
            }
        }
        else
        {
            throw new SubscriberProfileProvisionCorbaException(methodName + NO_CONNECTION_MESSAGE_SUFFIX);
        }

        return results;
    }


    /**
     * wrapper method of getClient to convert RemoteServiceException to SubscriberProfileProvisionCorbaException
     * @param ctx
     * @return
     * @throws SubscriberProfileProvisionCorbaException
     */
    private SubscriberProfileProvisionOperations getProvisionClient(Context ctx) throws SubscriberProfileProvisionCorbaException
    {
        try
        {
            return getClient(ctx);
        }
        catch (RemoteServiceException e)
        {
            throw new SubscriberProfileProvisionCorbaException("Failed to fetch Subscriber Profile Provision client.", e);
        }
    }

    /**
     * @param context The operating context.
     * @param subscriberProfile The subscription profile.
     * @param inParamSet Additional parameters.
     * @return Collection of results, including a return code.
     * @throws SubscriberProfileProvisionCorbaException Thrown if exceptions are
     * encountered at the CORBA level.
     */
    public SubscriptionReturnParam createSubscriptionProfile(final Context context,
        final SubscriptionProfile subscriberProfile, final Parameter[] inParamSet)
        throws SubscriberProfileProvisionCorbaException
    {
        final String methodName = "createSubscriptionProfile";

        final SubscriberProfileProvisionOperations service = getProvisionClient(context);

        final SubscriptionReturnParam results;
        if (service != null)
        {
            try
            {
                results = service.createSubscriptionProfile(subscriberProfile, inParamSet);
            }
            catch (final Throwable t)
            {
                throw new SubscriberProfileProvisionCorbaException(methodName + FAILED_MESSAGE_SUFFIX, t);
            }
        }
        else
        {
            throw new SubscriberProfileProvisionCorbaException(methodName + NO_CONNECTION_MESSAGE_SUFFIX);
        }

        return results;
    }


    /**
     * @param context The operating context.
     * @param msisdn The mobile number.
     * @param subscriptionType The type of subscription.
     * @param amount The amount of decrease.
     * @param erRefernce A reference for the resulting ERs.
     * @param creditLimit The resulting credit limit.
     * @return The result code.
     * @throws SubscriberProfileProvisionCorbaException Thrown if exceptions are
     * encountered at the CORBA level.
     */
    public short decCreditLimit(final Context context, final String msisdn, final int subscriptionType,
        final long amount, final String erRefernce, final LongHolder creditLimit)
        throws SubscriberProfileProvisionCorbaException
    {
        final String methodName = "decCreditLimit";

        final SubscriberProfileProvisionOperations service = getProvisionClient(context);

        final short results;
        if (service != null)
        {
            try
            {
                results = service.decCreditLimit(msisdn, subscriptionType, amount, erRefernce, creditLimit);
            }
            catch (final Throwable exception)
            {
                throw new SubscriberProfileProvisionCorbaException(methodName + FAILED_MESSAGE_SUFFIX, exception);
            }
        }
        else
        {
            throw new SubscriberProfileProvisionCorbaException(methodName + NO_CONNECTION_MESSAGE_SUFFIX);
        }

        return results;
    }


    /**
     * @param context The operating context.
     * @param msisdn The mobile number.
     * @param subscriptionType The type of subscription (i.e., the type of balance).
     * @param amount The amount of the decrease.
     * @param erRefernce A reference for the resulting ERs.
     * @param groupQuota The resulting quota.
     * @return The result code.
     * @throws SubscriberProfileProvisionCorbaException Thrown if exceptions are
     * encountered at the CORBA level.
     */
    public short decGroupQuota(final Context context, final String msisdn, final int subscriptionType,
        final long amount, final String erRefernce, final LongHolder groupQuota)
        throws SubscriberProfileProvisionCorbaException
    {
        final String methodName = "decGroupQuota";

        final SubscriberProfileProvisionOperations service = getProvisionClient(context);

        final short results;
        if (service != null)
        {
            try
            {
                results = service.decGroupQuota(msisdn, subscriptionType, amount, erRefernce, groupQuota);
            }
            catch (final Throwable exception)
            {
                throw new SubscriberProfileProvisionCorbaException(methodName + FAILED_MESSAGE_SUFFIX, exception);
            }
        }
        else
        {
            throw new SubscriberProfileProvisionCorbaException(methodName + NO_CONNECTION_MESSAGE_SUFFIX);
        }

        return results;
    }


    /**
     * @param context The operating context.
     * @param msisdn The mobile number.
     * @param subscriptionType The type of subscription (i.e., the type of balance).
     * @param inParamSet Additional parameters passed to BM (service specific).
     * @return Collection of results, including a return code.
     * @throws SubscriberProfileProvisionCorbaException Thrown if exceptions are
     * encountered at the CORBA level.
     */
    public SubscriptionDetailReturnParam getAllSubscriptionDetails(final Context context, final String msisdn,
        final int subscriptionType, final Parameter[] inParamSet)
        throws SubscriberProfileProvisionCorbaException
    {
        final String methodName = "getAllSubscriptionDetails";

        final SubscriberProfileProvisionOperations service = getProvisionClient(context);

        final SubscriptionDetailReturnParam results;
        if (service != null)
        {
            try
            {
                results = service.getAllSubscriptionDetails(msisdn, subscriptionType, inParamSet);
            }
            catch (final Throwable exception)
            {
                throw new SubscriberProfileProvisionCorbaException(methodName + FAILED_MESSAGE_SUFFIX, exception);
            }
        }
        else
        {
            throw new SubscriberProfileProvisionCorbaException(methodName + NO_CONNECTION_MESSAGE_SUFFIX);
        }

        return results;
    }


    /**
     * @param context The operating context.
     * @param subscriberId The subscriber identifier.
     * @param inParamSet Additional parameters passed to BM (service specific).
     * @return Collection of results, including a return code.
     * @throws SubscriberProfileProvisionCorbaException Thrown if exceptions are
     * encountered at the CORBA level.
     */
    public SubscriptionsForSubscriberReturnParam getAllSubscriptionsForSubscriber(final Context context,
        final String subscriberId, final Parameter[] inParamSet)
        throws SubscriberProfileProvisionCorbaException
    {
        final String methodName = "getAllSubscriptionsForSubscriber";

        final SubscriberProfileProvisionOperations service = getProvisionClient(context);

        final SubscriptionsForSubscriberReturnParam results;
        if (service != null)
        {
            try
            {
                results = service.getAllSubscriptionsForSubscriber(UserSupport.getSpid(context), subscriberId, inParamSet);
            }
            catch (final Throwable exception)
            {
                throw new SubscriberProfileProvisionCorbaException(methodName + FAILED_MESSAGE_SUFFIX, exception);
            }
        }
        else
        {
            throw new SubscriberProfileProvisionCorbaException(methodName + NO_CONNECTION_MESSAGE_SUFFIX);
        }

        return results;
    }


    /**
     * @param context The operating context.
     * @return Collection of properties.
     * @throws SubscriberProfileProvisionCorbaException Thrown if exceptions are
     * encountered at the CORBA level.
     */
    public PropertyReturn getProperties(final Context context)
        throws SubscriberProfileProvisionCorbaException
    {
        final String methodName = "getProperties";

        final SubscriberProfileProvisionOperations service = getProvisionClient(context);

        final PropertyReturn results;
        if (service != null)
        {
            try
            {
                results = service.getProperties();
            }
            catch (final Throwable exception)
            {
                throw new SubscriberProfileProvisionCorbaException(methodName + FAILED_MESSAGE_SUFFIX, exception);
            }
        }
        else
        {
            throw new SubscriberProfileProvisionCorbaException(methodName + NO_CONNECTION_MESSAGE_SUFFIX);
        }

        return results;
    }


    /**
     * @param context The operating context.
     * @param subscriberId The subscriber identifier.
     * @param inParamSet Additional parameters passed to BM (service specific).
     * @return Collection of results, including a return code.
     * @throws SubscriberProfileProvisionCorbaException Thrown if exceptions are
     * encountered at the CORBA level.
     */
    public SubscriberReturnParam getSubscriberProfile(final Context context, final String subscriberId,
        final Parameter[] inParamSet)
        throws SubscriberProfileProvisionCorbaException
    {
        final String methodName = "getSubscriberProfile";

        final SubscriberProfileProvisionOperations service = getProvisionClient(context);

        final SubscriberReturnParam results;
        if (service != null)
        {
            try
            {
                results = service.getSubscriberProfile(UserSupport.getSpid(context), subscriberId, inParamSet);
            }
            catch (final Throwable exception)
            {
                throw new SubscriberProfileProvisionCorbaException(methodName + FAILED_MESSAGE_SUFFIX, exception);
            }
        }
        else
        {
            throw new SubscriberProfileProvisionCorbaException(methodName + NO_CONNECTION_MESSAGE_SUFFIX);
        }

        return results;
    }


    /**
     * @param context The operating context.
     * @param msisdn The mobile number.
     * @param subscriptionType The type of subscription (i.e., the type of balance).
     * @param inParamSet Additional parameters passed to BM (service specific).
     * @return Collection of results, including a return code.
     * @throws SubscriberProfileProvisionCorbaException Thrown if exceptions are
     * encountered at the CORBA level.
     */
    public SubscriptionReturnParam getSubscriptionProfile(final Context context, final String msisdn,
        final int subscriptionType, final Parameter[] inParamSet)
        throws SubscriberProfileProvisionCorbaException
    {
        final String methodName = "getSubscriptionProfile";

        final SubscriberProfileProvisionOperations service = getProvisionClient(context);

        final SubscriptionReturnParam results;
        if (service != null)
        {
            try
            {
                results = service.getSubscriptionProfile(msisdn, subscriptionType, inParamSet);
            }
            catch (final Throwable exception)
            {
                throw new SubscriberProfileProvisionCorbaException(methodName + FAILED_MESSAGE_SUFFIX, exception);
            }
        }
        else
        {
            throw new SubscriberProfileProvisionCorbaException(methodName + NO_CONNECTION_MESSAGE_SUFFIX);
        }

        return results;
    }


    /**
     * @param context The operating context.
     * @param msisdn The mobile number.
     * @param subscriptionType The type of subscription (i.e., the type of balance).
     * @param amount The amount of the increase.
     * @param erRefernce A reference for the resulting ERs.
     * @param creditLimit The resulting credit limit.
     * @return The result code.
     * @throws SubscriberProfileProvisionCorbaException Thrown if exceptions are
     * encountered at the CORBA level.
     */
    public short incCreditLimit(final Context context, final String msisdn, final int subscriptionType,
        final long amount, final String erRefernce, final LongHolder creditLimit)
        throws SubscriberProfileProvisionCorbaException
    {
        final String methodName = "incCreditLimit";

        final SubscriberProfileProvisionOperations service = getProvisionClient(context);

        final short results;
        if (service != null)
        {
            try
            {
                results = service.incCreditLimit(msisdn, subscriptionType, amount, erRefernce, creditLimit);
            }
            catch (final Throwable exception)
            {
                throw new SubscriberProfileProvisionCorbaException(methodName + FAILED_MESSAGE_SUFFIX, exception);
            }
        }
        else
        {
            throw new SubscriberProfileProvisionCorbaException(methodName + NO_CONNECTION_MESSAGE_SUFFIX);
        }

        return results;
    }


    /**
     * @param context The operating context.
     * @param msisdn The mobile number.
     * @param subscriptionType The type of subscription (i.e., the type of balance).
     * @param amount The amount of the increase.
     * @param erRefernce A reference for the resulting ERs.
     * @param groupQuota The resulting quota.
     * @return The result code.
     * @throws SubscriberProfileProvisionCorbaException Thrown if exceptions are
     * encountered at the CORBA level.
     */
    public short incGroupQuota(final Context context, final String msisdn, final int subscriptionType,
        final long amount, final String erRefernce, final LongHolder groupQuota)
        throws SubscriberProfileProvisionCorbaException
    {
        final String methodName = "incGroupQuota";

        final SubscriberProfileProvisionOperations service = getProvisionClient(context);

        final short results;
        if (service != null)
        {
            try
            {
                results = service.incGroupQuota(msisdn, subscriptionType, amount, erRefernce, groupQuota);
            }
            catch (final Throwable exception)
            {
                throw new SubscriberProfileProvisionCorbaException(methodName + FAILED_MESSAGE_SUFFIX, exception);
            }
        }
        else
        {
            throw new SubscriberProfileProvisionCorbaException(methodName + NO_CONNECTION_MESSAGE_SUFFIX);
        }

        return results;
    }


    /**
     * @param context The operating context.
     * @param subscriberId The subscriber identifier.
     * @param inParamSet Additional parameters passed to BM (service specific).
     * @return Collection of results, including a return code.
     * @throws SubscriberProfileProvisionCorbaException Thrown if exceptions are
     * encountered at the CORBA level.
     */
    public SubscriberRemovalReturnParam removeSubscriberProfile(final Context context, final String subscriberId,
        final Parameter[] inParamSet)
        throws SubscriberProfileProvisionCorbaException
    {
        final String methodName = "removeSubscriberProfile";

        final SubscriberProfileProvisionOperations service = getProvisionClient(context);

        final SubscriberRemovalReturnParam results;
        if (service != null)
        {
            try
            {
                results = service.removeSubscriberProfile(UserSupport.getSpid(context), subscriberId, inParamSet);
            }
            catch (final Throwable exception)
            {
                throw new SubscriberProfileProvisionCorbaException(methodName + FAILED_MESSAGE_SUFFIX, exception);
            }
        }
        else
        {
            throw new SubscriberProfileProvisionCorbaException(methodName + NO_CONNECTION_MESSAGE_SUFFIX);
        }

        return results;
    }


    /**
     * @param context The operating context.
     * @param msisdn The mobile number.
     * @param subscriptionType The type of subscription (i.e., the type of balance).
     * @param inParamSet Additional parameters passed to BM (service specific).
     * @return Collection of results, including a return code.
     * @throws SubscriberProfileProvisionCorbaException Thrown if exceptions are
     * encountered at the CORBA level.
     */
    public SubscriptionReturnParam removeSubscriptionProfile(final Context context, final String msisdn,
        final int subscriptionType, final Parameter[] inParamSet)
        throws SubscriberProfileProvisionCorbaException
    {
        final String methodName = "removeSubscriptionProfile";

        final SubscriberProfileProvisionOperations service = getProvisionClient(context);

        final SubscriptionReturnParam results;
        if (service != null)
        {
            try
            {
                results = service.removeSubscriptionProfile(msisdn, subscriptionType, inParamSet);
            }
            catch (final Throwable exception)
            {
                throw new SubscriberProfileProvisionCorbaException(methodName + FAILED_MESSAGE_SUFFIX, exception);
            }
        }
        else
        {
            throw new SubscriberProfileProvisionCorbaException(methodName + NO_CONNECTION_MESSAGE_SUFFIX);
        }

        return results;
    }


    /**
     * @param context The operating context.
     * @param subscriberId The subscriber identifier.
     * @param inParamSet Additional parameters passed to BM (service specific).
     * @return Collection of results, including a return code.
     * @throws SubscriberProfileProvisionCorbaException Thrown if exceptions are
     * encountered at the CORBA level.
     */
    public SubscriberReturnParam updateSubscriberProfile(final Context context, final String subscriberId,
        final Parameter[] inParamSet)
        throws SubscriberProfileProvisionCorbaException
    {
        final String methodName = "updateSubscriberProfile";

        final SubscriberProfileProvisionOperations service = getProvisionClient(context);

        final SubscriberReturnParam results;
        if (service != null)
        {
            try
            {
                results = service.updateSubscriberProfile(UserSupport.getSpid(context), subscriberId, inParamSet);
            }
            catch (final Throwable exception)
            {
                throw new SubscriberProfileProvisionCorbaException(methodName + FAILED_MESSAGE_SUFFIX, exception);
            }
        }
        else
        {
            throw new SubscriberProfileProvisionCorbaException(methodName + NO_CONNECTION_MESSAGE_SUFFIX);
        }

        return results;
    }


    /**
     * @param context The operating context.
     * @param msisdn The mobile number.
     * @param subscriptionType The type of subscription (i.e., the type of balance).
     * @param inParamSet Additional parameters passed to BM (service specific).
     * @return Collection of results, including a return code.
     * @throws SubscriberProfileProvisionCorbaException Thrown if exceptions are
     * encountered at the CORBA level.
     */
    public SubscriptionReturnParam updateSubscriptionProfile(final Context context, final String msisdn,
        final int subscriptionType, final Parameter[] inParamSet)
        throws SubscriberProfileProvisionCorbaException
    {
        final String methodName = "updateSubscriptionProfile";

        final SubscriberProfileProvisionOperations service = getProvisionClient(context);

        final SubscriptionReturnParam results;
        if (service != null)
        {
            try
            {
                results = service.updateSubscriptionProfile(msisdn, subscriptionType, inParamSet);
            }
            catch (final Throwable exception)
            {
                throw new SubscriberProfileProvisionCorbaException(methodName + FAILED_MESSAGE_SUFFIX, exception);
            }
        }
        else
        {
            throw new SubscriberProfileProvisionCorbaException(methodName + NO_CONNECTION_MESSAGE_SUFFIX);
        }

        return results;
    }


    public short resetGroupUsage(final Context context, final String msisdn, final int subscriptionType)
            throws SubscriberProfileProvisionCorbaException
    {
        final String methodName = "resetGroupUsage";
        final SubscriberProfileProvisionOperations service = getProvisionClient(context);
        final short results;
        if (service != null)
        {
            try
            {
                results = service.resetGroupUsage(msisdn, subscriptionType);
            }
            catch (final Throwable exception)
            {
                throw new SubscriberProfileProvisionCorbaException(methodName + FAILED_MESSAGE_SUFFIX, exception);
            }
        }
        else
        {
            throw new SubscriberProfileProvisionCorbaException(methodName + NO_CONNECTION_MESSAGE_SUFFIX);
        }
        return results;
    }


    public short resetMonthlySpendUsage(final Context context, final String msisdn, final int subscriptionType)
            throws SubscriberProfileProvisionCorbaException
    {
        final String methodName = "resetMonthlySpendUsage";
        final SubscriberProfileProvisionOperations service = getProvisionClient(context);
        final short results;
        if (service != null)
        {
            try
            {
                results = service.resetMonthlySpendUsage(msisdn, subscriptionType);
            }
            catch (final Throwable exception)
            {
                throw new SubscriberProfileProvisionCorbaException(methodName + FAILED_MESSAGE_SUFFIX, exception);
            }
        }
        else
        {
            throw new SubscriberProfileProvisionCorbaException(methodName + NO_CONNECTION_MESSAGE_SUFFIX);
        }
        return results;
    }
    
    public BCDChangeRequestReturnParam[] addUpdateBcdChangeRequest(final Context context, String[] subscriptionIDs, int newBillCycleDay)
        throws SubscriberProfileProvisionCorbaException
    {
        final String methodName = "addUpdateBcdChangeRequest";
        final SubscriberProfileProvisionOperations service = getProvisionClient(context);
        final BCDChangeRequestReturnParam[] results;
        if (service != null)
        {
            try
            {
                results = service.addUpdateBcdChangeRequest(UserSupport.getSpid(context), subscriptionIDs, newBillCycleDay);
            }
            catch (final Throwable exception)
            {
                throw new SubscriberProfileProvisionCorbaException(methodName + FAILED_MESSAGE_SUFFIX, exception);
            }
        }
        else
        {
            throw new SubscriberProfileProvisionCorbaException(methodName + NO_CONNECTION_MESSAGE_SUFFIX);
        }
        return results;
    }
    
    public BCDChangeRequestReturnParam[] removeBcdChangeRequest(final Context context, String[] subscriptionIDs)
        throws SubscriberProfileProvisionCorbaException
    {
        final String methodName = "removeBcdChangeRequest";
        final SubscriberProfileProvisionOperations service = getProvisionClient(context);
        final BCDChangeRequestReturnParam[] results;
        if (service != null)
        {
            try
            {
                results = service.removeBcdChangeRequest(UserSupport.getSpid(context), subscriptionIDs);
            }
            catch (final Throwable exception)
            {
                throw new SubscriberProfileProvisionCorbaException(methodName + FAILED_MESSAGE_SUFFIX, exception);
            }
        }
        else
        {
            throw new SubscriberProfileProvisionCorbaException(methodName + NO_CONNECTION_MESSAGE_SUFFIX);
        }
        return results;
    }
    
    /**
     * The name of the key used to look-up the connection properties in the
     * CorbaClientProperty Home.
     */
    private static final String CONNECTION_PROPERTIES_KEY = "BMGT.SubscriberProfileProvision";

    /**
     * Suffix for exception message used when no connection is available.
     */
    private static final String NO_CONNECTION_MESSAGE_SUFFIX = ": could not establish a connection to BM.";

    /**
     * Suffix for exception message used then the connection fails.
     */
    private static final String FAILED_MESSAGE_SUFFIX = " failed.";

}
