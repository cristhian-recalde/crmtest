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

package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteria;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteriaHome;
import com.trilogy.app.crm.log.ERLogger;

/**
 * Generates operational measurements (OMs) for AutoDepositReleaseCriteriaHome operations.
 *
 * @author cindy.wong@redknee.com
 */
public class AutoDepositReleaseCriteriaCreationHome extends HomeProxy
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 5278804818912806744L;

    /**
     * Creates a new AutoDepositReleaseCriteriaCreationHome proxy.
     *
     * @param delegate
     *            The Home to which we delegate.
     */
    public AutoDepositReleaseCriteriaCreationHome(final Home delegate)
    {
        super(delegate);
    }

    /**
     * Add OM on Auto Deposit Release Criteria creation attempts, successes and failures.
     *
     * @param context
     *            The operating context.
     * @param object
     *            The criteria being created.
     * @return The criteria actually created.
     * @throws HomeException
     *             If there are problems creating the criteria.
     * @see com.redknee.framework.xhome.home.HomeProxy#create(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public Object create(final Context context, final Object object) throws HomeException
    {
        new OMLogMsg(Common.OM_MODULE, Common.OM_AUTO_DEPOSIT_RELEASE_CRITERIA_CREATE_ATTEMPT).log(context);
        final Object result;
        try
        {
            result = super.create(context, object);
            new OMLogMsg(Common.OM_MODULE, Common.OM_AUTO_DEPOSIT_RELEASE_CRITERIA_CREATE_SUCCESS).log(context);
            ERLogger.generateAutoDepositReleaseCriteriaCreateER(context, (AutoDepositReleaseCriteria) result);
        }
        catch (final HomeException exception)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_AUTO_DEPOSIT_RELEASE_CRITERIA_CREATE_FAILURE).log(context);
            throw exception;
        }
        return result;
    }

    /**
     * Add OM on Auto Deposit Release Criteria modification attempts, successes and failures.
     *
     * @param ctx
     *            The operating context.
     * @param obj
     *            The criteria being modified.
     * @return The criteria actually modified.
     * @throws HomeException
     *             If there are problems updating the criteria.
     * @see com.redknee.framework.xhome.home.HomeProxy#store(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        new OMLogMsg(Common.OM_MODULE, Common.OM_AUTO_DEPOSIT_RELEASE_CRITERIA_UPDATE_ATTEMPT).log(ctx);

        Object result = null;
        try
        {
            // get the old version
            final AutoDepositReleaseCriteria newCriteria = (AutoDepositReleaseCriteria) obj;
            final AutoDepositReleaseCriteria oldCriteria = (AutoDepositReleaseCriteria) ((Home) ctx
                .get(AutoDepositReleaseCriteriaHome.class)).find(ctx, Long.valueOf(newCriteria.getIdentifier()));
            result = super.store(ctx, obj);
            new OMLogMsg(Common.OM_MODULE, Common.OM_AUTO_DEPOSIT_RELEASE_CRITERIA_UPDATE_SUCCESS).log(ctx);
            ERLogger.generateAutoDepositReleaseCriteriaUpdateER(ctx, oldCriteria, newCriteria);
        }
        catch (final HomeException exception)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_AUTO_DEPOSIT_RELEASE_CRITERIA_UPDATE_FAILURE).log(ctx);
            throw exception;
        }

        return result;
    }

    /**
     * Add OM on Auto Deposit Release Criteria deletion attempts, successes and failures.
     *
     * @param ctx
     *            The operating context.
     * @param obj
     *            The criteria being deleted.
     * @throws HomeException
     *             If there are problems deleting the criteria.
     * @see com.redknee.framework.xhome.home.HomeProxy#remove(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public void remove(final Context ctx, final Object obj) throws HomeException
    {
        new OMLogMsg(Common.OM_MODULE, Common.OM_AUTO_DEPOSIT_RELEASE_CRITERIA_DELETE_ATTEMPT).log(ctx);

        try
        {
            super.remove(ctx, obj);
            new OMLogMsg(Common.OM_MODULE, Common.OM_AUTO_DEPOSIT_RELEASE_CRITERIA_DELETE_SUCCESS).log(ctx);
            ERLogger.generateAutoDepositReleaseCriteriaDeleteER(ctx, (AutoDepositReleaseCriteria) obj);
        }
        catch (final HomeException exception)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_AUTO_DEPOSIT_RELEASE_CRITERIA_DELETE_FAILURE).log(ctx);
            throw exception;
        }
    }
}
