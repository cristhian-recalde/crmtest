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

package com.trilogy.app.crm.home.sub;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.bean.AbstractSubscriber;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnEntryTypeEnum;
import com.trilogy.app.crm.bean.MsisdnGroup;
import com.trilogy.app.crm.bean.MsisdnGroupXInfo;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.PortingTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;


/**
 * Validates the MSISDNs of a subscriber.
 *
 * @author cindy.wong@redknee.com
 */
public class SubscriberMsisdnValidator implements Validator
{

    /**
     * Create a new instance of <code>SubscriberMsisdnValidator</code>.
     */
    protected SubscriberMsisdnValidator()
    {
        final Map<PropertyInfo, Boolean> properties = new HashMap<PropertyInfo, Boolean>();
        properties.put(SubscriberXInfo.MSISDN, Boolean.TRUE);
        properties.put(SubscriberXInfo.DATA_MSISDN, Boolean.FALSE);
        properties.put(SubscriberXInfo.FAX_MSISDN, Boolean.FALSE);
        this.properties_ = Collections.unmodifiableMap(properties);
    }


    /**
     * Returns an instance of <code>SubscriberMsisdnValidator</code>.
     *
     * @return An instance of <code>SubscriberMsisdnValidator</code>.
     */
    public static SubscriberMsisdnValidator instance()
    {
        if (instance == null)
        {
            instance = new SubscriberMsisdnValidator();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    public void validate(final Context context, final Object object) throws IllegalStateException
    {
        final HomeOperationEnum operation = (HomeOperationEnum) context.get(HomeOperationEnum.class);
        final Subscriber newSubscriber = (Subscriber) object;
        final CompoundIllegalStateException exceptions = new CompoundIllegalStateException();
        if (HomeOperationEnum.CREATE == operation)
        {
            /*
             * [Cindy]: skip resource validation when doing postpaid->prepaid conversion.
             */
            if (!((Boolean) context.get(Lookup.POSTPAID_PREPAID_CONVERSION, Boolean.FALSE)).booleanValue())
            {
                validateSubscriber(context, null, newSubscriber, exceptions);
            }
        }
        else
        {
            Subscriber oldSubscriber = null;
            try
            {
                oldSubscriber = lookupOldSubscriber(context, newSubscriber.getId());
                validateSubscriber(context, oldSubscriber, newSubscriber, exceptions);
            }
            catch (final HomeException exception)
            {
                exceptions.thrown(exception);
            }

        }
        exceptions.throwAll();
    }


    /**
     * Validates the MSISDNs of this subscriber.
     *
     * @param context
     *            The operating context.
     * @param oldSubscriber
     *            The old subscriber.
     * @param newSubscriber
     *            The new subscriber.
     * @param exceptions
     *            Compound exceptions; exceptions thrown by this method will be added to
     *            it.
     */
    private void validateSubscriber(final Context context, final Subscriber oldSubscriber,
        final Subscriber newSubscriber, final CompoundIllegalStateException exceptions)
    {
        for (final PropertyInfo property : this.properties_.keySet())
        {
            boolean validate = false;
            if (oldSubscriber == null)
            {
                validate = true;
            }
            else
            {
                final String oldMsisdn = getMsisdn(oldSubscriber, property);
                final String newMsisdn = getMsisdn(newSubscriber, property);
                validate = !SafetyUtil.safeEquals(oldMsisdn, newMsisdn);
            }

            if (validate)
            {
                try
                {
                    validateMsisdn(context, newSubscriber, oldSubscriber, property, this.properties_.get(property).booleanValue());
                }
                catch (final IllegalPropertyArgumentException exception)
                {
                    exceptions.thrown(exception);
                }
            }
        }
    }


    /**
     * Looks up the subscriber in database.
     *
     * @param ctx
     *            The operating context.
     * @param subId
     *            The subscriber ID.
     * @return The subscriber with the provided ID, or <code>null</code> if none exists.
     * @throws HomeException
     *             Thrown if there are problems looking up the subscriber.
     */
    protected Subscriber lookupOldSubscriber(final Context ctx, final String subId) throws HomeException
    {
        Subscriber result = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        if (result == null || !result.getId().equals(subId))
        {
            result = SubscriberSupport.lookupSubscriberForSubId(ctx, subId);
        }

        return result;
    }


    /**
     * Validates a MSISDN of the subscriber.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber being validated.
     * @param property
     *            The property of the subscriber to be validated. It should be one of
     *            {@link SubscriberXInfo#MSISDN}, {@link SubscriberXInfo#FAX_MSISDN}, or
     *            {@link SubscriberXInfo#DATA_MSISDN}.
     * @param isMandatory
     *            Whether the MSISDN is mandatory.
     */
    protected static void validateMsisdn(final Context context, final Subscriber subscriber, final Subscriber oldSubscriber,
        final PropertyInfo property, final boolean isMandatory)
    {
        final String msisdnString = getMsisdn(subscriber, property);
        com.redknee.app.crm.bean.core.Msisdn msisdbObject  = null;
        
        final boolean isEmpty = msisdnString == null || msisdnString.trim().length() == 0;

        if (!isMandatory && isEmpty)
        {
            return;
        }

        Msisdn msisdn = null;
        try
        {
            msisdn = MsisdnSupport.getMsisdn(context, msisdnString);
            msisdbObject = MsisdnSupport.getMsisdn(context, msisdnString);
        }
        catch (final HomeException exception)
        {
            throw new IllegalPropertyArgumentException(property, exception);
        }

        boolean error = false;
        final StringBuilder sb = new StringBuilder();
        sb.append("Mobile Number ");
        sb.append(msisdnString);
        if (msisdn == null )
        {
        	if(subscriber.getMsisdnEntryType() != MsisdnEntryTypeEnum.EXTERNAL_INDEX)
        	{
	            sb.append(" does not exist in the system");
	            error = true;
        	}
        }
        else if (SafetyUtil.safeEquals(msisdn.getState(), MsisdnStateEnum.IN_USE)
                && !SafetyUtil.safeEquals(msisdn.getBAN(), subscriber.getBAN()))
            {
            sb.append(" is already used by another account");
            error = true;
        }

        else if(SafetyUtil.safeEquals(msisdn.getState(), MsisdnStateEnum.IN_USE)
        		&& msisdbObject.getSubscriberID(context, subscriber.getSubscriptionType(), null) != null
        		&& !SafetyUtil.safeEquals(msisdbObject.getSubscriberID(context, subscriber.getSubscriptionType(), null),subscriber.getId()))
        {
        	sb.append(" is already associated with another subscriptionId:"+msisdbObject.getSubscriberID(context, subscriber.getSubscriptionType(), null));
            error = true;
        }
        
        else if(SafetyUtil.safeEquals(msisdn.getState(), MsisdnStateEnum.IN_USE)
        		&&  msisdbObject.getOthersubscriptionTypeSubscriberID(context, subscriber.getSubscriptionType(), null) != null
        		&& !SafetyUtil.safeEquals(msisdbObject.getOthersubscriptionTypeSubscriberID(context, subscriber.getSubscriptionType(), null),subscriber.getId()))
        {
        	sb.append(" is already associated with another subscriptionId:"+msisdbObject.getOthersubscriptionTypeSubscriberID(context, subscriber.getSubscriptionType(), null));
            error = true;
        }
        
        else if (SafetyUtil.safeEquals(msisdn.getState(), MsisdnStateEnum.HELD)
            && !SafetyUtil.safeEquals(msisdn.getBAN(), subscriber.getBAN()))
        {

            sb.append(" is currently held by another account");
            error = true;
        }
        else if (!SafetyUtil.safeEquals(msisdn.getSubscriberType(), subscriber.getSubscriberType()) 
    		&& !SafetyUtil.safeEquals(msisdn.getSubscriberType(), SubscriberTypeEnum.HYBRID))
        {
            sb.append(" is only assignable to ");
            sb.append(msisdn.getSubscriberType().getDescription());
            sb.append(" subscribers");
            error = true;
        }
        try
        {
            final HomeOperationEnum operation = (HomeOperationEnum) context.get(HomeOperationEnum.class);
            if (!hasSufficientBalance(context, msisdn, oldSubscriber, subscriber, (HomeOperationEnum.CREATE == operation )))
            {
                sb.append("  group fee exceeds initial/current balance");
                error = true;
            }
        }
        catch (HomeException homeEx)
        {
            sb.append("Unable to find Msisdn group with id =>" + msisdn.getGroup());
            error = true;
        }
        if (error)
        {
            throw new IllegalPropertyArgumentException(property, sb.toString());
        }
    }

    /**
     * This checks if subscriber has enough initial/current balance for the msisdn group fee
     * @param context
     * @param msisdn
     * @param oldSubscriber
     * @param subscriber
     * @param isCreate
     * @return
     * @throws HomeException
     */
    public static boolean hasSufficientBalance(final Context context, Msisdn msisdn,            
            final Subscriber oldSubscriber, final Subscriber subscriber, boolean isCreate) throws HomeException
    {
        boolean result = true;
        
        if (subscriber.isPrepaid() && msisdn!=null)
        {
            MsisdnGroup group = HomeSupportHelper.get(context).findBean(context,
                    com.redknee.app.crm.bean.MsisdnGroup.class, new EQ(MsisdnGroupXInfo.ID, msisdn.getGroup()));
            if (group != null && group.getFee() > 0)
            {
                if ((isCreate && subscriber.getInitialBalance() < group.getFee())
                        || (!isCreate && oldSubscriber.getBalanceRemaining(context) < group.getFee()))
                {
                    result = false;
                }
            }
        }
        return result;
    }

    /**
     * Retrieves a MSISDN of a subscriber.
     *
     * @param subscriber
     *            The subscriber being looked up.
     * @param property
     *            The field containing the MSISDN.
     * @return The MSISDN of the subscriber.
     */
    private static String getMsisdn(final Subscriber subscriber, final PropertyInfo property)
    {
        if (subscriber != null)
        {
            return (String) property.get(subscriber);
        }
        return null;
    }

    /**
     * Singleton instance.
     */
    private static SubscriberMsisdnValidator instance;

    /**
     * The list of properties being validated.
     */
    private final Map<PropertyInfo, Boolean> properties_;
}
