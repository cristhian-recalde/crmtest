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

import java.util.Date;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.AbstractPricePlan;
import com.trilogy.app.crm.bean.PricePlanStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.exception.RethrowExceptionListener;
import com.trilogy.app.crm.support.AuthSupport;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.PricePlanSupport;

/**
 * Provides bean-level validation for the Subscriber's price plan.
 *
 * @author danny.ng@redknee.com
 * @author aaron.gourley@redknee.com
 */
public final class SubscriptionPricePlanValidator implements Validator
{
    public SubscriptionPricePlanValidator()
    {
        
    }
    
    public SubscriptionPricePlanValidator(PropertyInfo property)
    {
        if (Subscriber.class.isAssignableFrom(property.getBeanClass())
                && Number.class.isAssignableFrom(property.getType()))
        {
            property_  = property;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj)
    {
        final Subscriber subscriber = (Subscriber) obj;

        // this should not be null for store(), SubscriberPipeLineContextPrepareHome should set this
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

        final long defaultPlanID = ((Number) property_.getDefault()).longValue();
        final long planID = ((Number) property_.get(subscriber)).longValue();
        long oldPlanID = defaultPlanID;
        if (oldSub != null)
        {
            oldPlanID = ((Number) property_.get(oldSub)).longValue();
        }

        final RethrowExceptionListener exceptions = new RethrowExceptionListener();
        
        if (HomeOperationEnum.CREATE.equals(ctx.get(HomeOperationEnum.class))
                || planID != oldPlanID)
        {
            // Price Plan change is underway (including creation, where change is from null)
            if (planID != defaultPlanID)
            {
                validatePricePlanSwitch(ctx, subscriber, planID, exceptions);
                
                if (isSecondaryPricePlanCheck())
                {
                    // Secondary Price Plan specific validation
                    if (planID == subscriber.getPricePlan())
                    {
                        final IllegalPropertyArgumentException ex = new IllegalPropertyArgumentException(
                                property_,
                                "Secondary price plan should be different from the primary price plan.");
                        exceptions.thrown(ex);
                    }

                    validateStartDate(ctx, subscriber, exceptions);
                }
                
                if ((oldSub == null || planID != oldPlanID)
                        && EnumStateSupportHelper.get(ctx).isOneOfStates(
                                subscriber, 
                                SubscriberStateEnum.INACTIVE,
                                SubscriberStateEnum.LOCKED))
                {
                    final Exception ex = new IllegalPropertyArgumentException(
                            property_,
                            "Unable to change price plan for subscription in " + subscriber.getState() + " state.");
                    exceptions.thrown(ex);
                }
            }
            else if (!isSecondaryPricePlanCheck())
            {
                final Exception ex = new IllegalPropertyArgumentException(
                        property_,
                        "Selection required for Price Plan.");
                exceptions.thrown(ex);
            }
        }
        else if (oldSub != null
                && isSecondaryPricePlanCheck()
                && !SafetyUtil.safeEquals(subscriber.getSecondaryPricePlanStartDate(), oldSub.getSecondaryPricePlanStartDate()))
        {
            validateStartDate(ctx, subscriber, exceptions);
        }
        
        exceptions.throwAllAsCompoundException();
    }

    /**
     * Validates that the secondary price plan has a start date
     *
     * @param ctx the operating context
     * @param subscriber The subscriber to validate.
     * @throws IllegalStateException Thrown if the subscriber's secondary price plan does not have a start date.
     */
    private void validateStartDate(final Context ctx, final Subscriber subscriber,
            RethrowExceptionListener exceptions)
    {
        final Date secondaryPricePlanStartDate = subscriber.getSecondaryPricePlanStartDate();

        // Check start date is populated if secondary price plan is selected
        if (secondaryPricePlanStartDate == null)
        {
            final IllegalPropertyArgumentException ex = new IllegalPropertyArgumentException(
                    property_,
                    "Missing secondary price plan start date.");
            exceptions.thrown(ex);

            // the actual date values are verified in SubscriberDatesValidator
        }
    }

    protected void validatePricePlanSwitch(Context ctx, Subscriber sub, long pricePlanID, ExceptionListener exceptions)
    {
        PricePlan plan = null;
        try
        {
            plan = PricePlanSupport.getPlan(ctx, pricePlanID);
        }
        catch (final HomeException e)
        {
            final Exception ex = new IllegalPropertyArgumentException(property_, "Error while retreiving Price Plan "
                + pricePlanID);
            ex.initCause(e);
            exceptions.thrown(ex);
        }

        if (plan == null)
        {
            final Exception ex = new IllegalPropertyArgumentException(property_, "Price Plan " + pricePlanID
                + " not found!");
            exceptions.thrown(ex);
        }
        else
        {
            if (!AuthSupport.hasPermission(ctx, Common.PRICE_PLAN_RESTRICTION_OVERRIDE_PERMISSION)
                    && plan.isRestrictionViolation(ctx, sub))
            {
                boolean fail = true;
                
                StringBuilder restrictionType = new StringBuilder();
                if (plan.isApplyContractDurationCriteria())
                {
                    fail = !AuthSupport.hasPermission(ctx, Common.PRICE_PLAN_RESTRICTION_OVERRIDE_CONTRACT_PERMISSION);
                    if (fail)
                    {
                        restrictionType.append("Contract Duration");
                    }
                }

                if (fail)
                {
                    final Exception ex = new IllegalPropertyArgumentException(property_, "Subscriber is not authorized to use Price Plan [" + pricePlanID + " - "
                            + plan.getName() + "].  " + restrictionType + " Price Plan restrictions are not satisfied.");
                    exceptions.thrown(ex);
                }
            }
            
            final com.redknee.app.crm.bean.PricePlanVersion ppv = plan.getVersions();
            if (ppv == null || plan.getCurrentVersion() == AbstractPricePlan.DEFAULT_CURRENTVERSION)
            {
                final Exception ex = new IllegalPropertyArgumentException(property_, "Price Plan [" + pricePlanID + " - "
                    + plan.getName() + "] does not have an active Price Plan version.");
                exceptions.thrown(ex);
            }

            if (!plan.isEnabled() && plan.getState() != PricePlanStateEnum.GRANDFATHERED)
            {
                final Exception ex = new IllegalPropertyArgumentException(property_, "Price Plan [" + pricePlanID + " - "
                    + plan.getName() + "] is " + PricePlanSupport.getStateDescription(ctx, plan));
                exceptions.thrown(ex);
            }

            if (plan.getSpid() != sub.getSpid())
            {
                final Exception ex = new IllegalPropertyArgumentException(property_, "Price Plan [" + pricePlanID + " - "
                    + plan.getName() + "] is on a different Service Provider than the subscription.");
                exceptions.thrown(ex);
            }

            if (!SafetyUtil.safeEquals(plan.getTechnology(), sub.getTechnology()))
            {
                final Exception ex = new IllegalPropertyArgumentException(property_, "Price Plan [" + pricePlanID + " - "
                    + plan.getName() + "] is on a different Technology than the subscription.");
                exceptions.thrown(ex);
            }

            if (!SafetyUtil.safeEquals(plan.getPricePlanType(), sub.getSubscriberType()))
            {
                final Exception ex = new IllegalPropertyArgumentException(property_, "Price Plan [" + pricePlanID + " - "
                    + plan.getName() + "] is of a different paid type than the subscription.");
                exceptions.thrown(ex);
            }

            if (plan.getSubscriptionType() != sub.getSubscriptionType())
            {
                final Exception ex = new IllegalPropertyArgumentException(property_, "Price Plan [" + pricePlanID + " - "
                    + plan.getName() + "] is of different subscription type then the subscription.");
                exceptions.thrown(ex);
            }
        }
    }

    protected boolean isSecondaryPricePlanCheck()
    {
        return property_ == SubscriberXInfo.SECONDARY_PRICE_PLAN;
    }

    protected PropertyInfo property_ = SubscriberXInfo.PRICE_PLAN;
} // class