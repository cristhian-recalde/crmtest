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

package com.trilogy.app.crm.deposit;

import java.util.Calendar;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeStateEnum;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteria;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.DepositSupport;


/**
 * Visitor to subscriber home for auto deposit release.
 *
 * @author cindy.wong@redknee.com
 */
public class DefaultSubscriberReleaseVisitor extends SubscriberReleaseVisitor
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -104362907880681180L;


    /**
     * Creates a new subscriber release visitor.
     */
    public DefaultSubscriberReleaseVisitor()
    {
        setSpidCriteria(null);
        setActiveDate(Calendar.getInstance());
        setServiceProvider(-1);
        setTransactionCreator(null);
    }


    /**
     * Creates a new subscriber release visitor, initialized with criteria and active
     * date.
     *
     * @param context
     *            The operating context.
     * @param criteria
     *            The deposit release criteria used by this visitor.
     * @param spid
     *            The service provider being visited.
     * @param creator
     *            Strategy used to create deposit release transaction.
     * @param activeDate
     *            The date to act upon.
     */
    public DefaultSubscriberReleaseVisitor(final Context context, final AutoDepositReleaseCriteria criteria,
        final int spid, final DepositReleaseTransactionCreator creator, final Calendar activeDate)
    {
        initalize(context, criteria, spid, creator, activeDate);
    }


    /**
     * {@inheritDoc}
     */
    public final void visit(final Context context, final Object object) throws AgentException
    {
        final Subscriber subscriber = (Subscriber) object;
        if (!isInitialized())
        {
            final AbortVisitException exception = new AbortVisitException("Visitor has not been initialized");
            new DebugLogMsg(this, exception.getMessage(), exception).log(context);
            throw exception;
        }

        try
        {
            // step 1: determines if this subscriber is eligible
            final CRMSpid serviceProvider = getServiceProvider(context);

            // subscriber is eligible
            if (validate(context, subscriber, serviceProvider, getSpidCriteria(), getActiveDate()))
            {
                incrementNumVisits();
                final AutoDepositReleaseCriteria criteria = getCriteria(context, subscriber.getBAN(),
                    getSpidCriteria());

                // step 2: determines the amount to be released
                new OMLogMsg(Common.OM_MODULE, Common.OM_AUTO_DEPOSIT_RELEASE_ATTEMPT).log(context);
                final ReleaseCalculation calculation = DepositSupport.getReleaseCalculation(context, serviceProvider);
                long amountReleased = calculation.calculate(context, criteria, subscriber);
                if (amountReleased < 0)
                {
                    new OMLogMsg(Common.OM_MODULE, Common.OM_AUTO_DEPOSIT_RELEASE_FAILURE).log(context);
                    throw new AgentException("Deposit release amount cannot be less than 0 (subscriber="
                        + subscriber.getId() + ",amount=" + amountReleased + ")");
                }

                if (amountReleased > subscriber.getDeposit(context))
                {
                    amountReleased = subscriber.getDeposit(context);
                }

                // step 3: creates transaction
                AdjustmentType adjustmentType;
                try
                {
                    adjustmentType = AdjustmentTypeSupportHelper.get(context).getAdjustmentTypeForRead(context,
                            criteria.getDepositReleaseAdjustmentType());
                }
                catch (final HomeException exception)
                {
                    new OMLogMsg(Common.OM_MODULE, Common.OM_AUTO_DEPOSIT_RELEASE_FAILURE).log(context);
                    throw new AgentException(CANNOT_FIND_ADJUSTMENT_TYPE + criteria.getDepositReleaseAdjustmentType()
                        + OF_CRITERIA + criteria.getIdentifier(), exception);
                }

                if (adjustmentType == null)
                {
                    throw new AgentException(CANNOT_FIND_ADJUSTMENT_TYPE + criteria.getDepositReleaseAdjustmentType()
                        + OF_CRITERIA + criteria.getIdentifier());
                }
                else if (adjustmentType.getState() != AdjustmentTypeStateEnum.ACTIVE)
                {
                    throw new AgentException("Adjustment type " + adjustmentType.getCode() + " is not active");
                }

                try
                {
                    getTransactionCreator().createTransaction(context, subscriber, amountReleased, adjustmentType,
                        subscriber.getNextDepositReleaseDate());
                }
                catch (final HomeException exception)
                {
                    new OMLogMsg(Common.OM_MODULE, Common.OM_AUTO_DEPOSIT_RELEASE_FAILURE).log(context);
                    throw new AgentException("Cannot create deposit release transaction for subscriber "
                        + subscriber.getId(), exception);
                }
                new OMLogMsg(Common.OM_MODULE, Common.OM_AUTO_DEPOSIT_RELEASE_SUCCESS).log(context);
            }
        }
        catch (final Exception e)
        {
            new DebugLogMsg(this, "Exception occurred in " + this.getClass().getSimpleName()
                + ".visit() for subscriber " + subscriber.getId() + ": " + e.getMessage(), e).log(context);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final SubscriberReleaseVisitor prototype()
    {
        final DefaultSubscriberReleaseVisitor visitor = new DefaultSubscriberReleaseVisitor();
        visitor.setSpidCriteria(getSpidCriteria());
        visitor.setActiveDate(getActiveDate());
        visitor.setServiceProvider(getServiceProvider());
        visitor.setTransactionCreator(getTransactionCreator());
        return visitor;
    }
}
