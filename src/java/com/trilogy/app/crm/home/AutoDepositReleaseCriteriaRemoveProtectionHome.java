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
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.visitor.CountingVisitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.AutoDepositReleaseConfigurationEnum;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteria;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.bean.CreditCategoryHome;
import com.trilogy.app.crm.bean.CreditCategoryXInfo;
import com.trilogy.app.crm.bean.DunningConfigurationEnum;
import com.trilogy.app.crm.exception.RemoveException;

/**
 * <p>
 * Prevents Auto Deposit Release Criteria from being deleted when it is in use. The definition of "in use", as seen by
 * this decorator, is:
 * </p>
 * <ul>
 * <li>It is set as the criteria used by a service provider <em>with <b>Auto Deposit Release</b> enabled</em>; or</li>
 * <li>It is set as the criteria used by a credit category where
 * <em><b>Auto Deposit Release Configuration</b> is set to "custom"</em>.</li>
 * </ul>
 * <p>
 * This home should be placed after {@link AutoDepositReleaseCriteriaCreationHome} in the pipeline to ensure the OM
 * count is correct.
 * </p>
 *
 * @author cindy.wong@redknee.com
 */
public class AutoDepositReleaseCriteriaRemoveProtectionHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 8046272933171226101L;

    /**
     * Creates a new instance of AutoDepositReleaseCriteriaRemoveProtectionHome.
     *
     * @param delegate
     *            The delegate of this decorator.
     */
    public AutoDepositReleaseCriteriaRemoveProtectionHome(final Home delegate)
    {
        super(delegate);
    }

    /**
     * Creates a new instance of AutoDepositReleaseCriteriaRemoveProtectionHome.
     *
     * @param context
     *            The operating context.
     * @param delegate
     *            The delegate of this decorator.
     */
    public AutoDepositReleaseCriteriaRemoveProtectionHome(final Context context, final Home delegate)
    {
        super(context, delegate);
    }

    /**
     * Prevents the criteria from being deleted if it is currently used by a service provider. Throws
     * {@link RemoveException} if the criteria is currently in use.
     *
     * @param context
     *            The operating context.
     * @param object
     *            The object being removed.
     * @throws HomeException
     *             Thrown if there are problems preventing the removal of this criteria.
     * @see com.redknee.framework.xhome.home.HomeProxy#remove(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public void remove(final Context context, final Object object) throws HomeException
    {
        final AutoDepositReleaseCriteria criteria = (AutoDepositReleaseCriteria) object;

        if (isCriteriaUsedByServiceProvider(context, criteria) || isCriteriaUsedByCreditCategory(context, criteria))
        {
            final RemoveException exception = new RemoveException("Criteria is in use");
            new DebugLogMsg(this, "Criteria " + criteria.getIdentifier() + " is still in use", exception).log(context);
            throw exception;
        }

        super.remove(context, object);
    }

    /**
     * Determines if the criteria is currently used by any service provider.
     *
     * @param context
     *            The operating context.
     * @param criteria
     *            The criteria in question.
     * @return Whether the criteria is currently used by any service provider.
     * @throws HomeException
     *             Thrown by home.
     */
    private boolean isCriteriaUsedByServiceProvider(final Context context, final AutoDepositReleaseCriteria criteria)
        throws HomeException
    {
        final And and = new And();
        and.add(new EQ(CRMSpidXInfo.USE_AUTO_DEPOSIT_RELEASE, AutoDepositReleaseConfigurationEnum.YES));
        and.add(new EQ(CRMSpidXInfo.AUTO_DEPOSIT_RELEASE_CRITERIA, Long.valueOf(criteria.getIdentifier())));
        final Home spidHome = ((Home) context.get(CRMSpidHome.class)).where(context, and);
        final CountingVisitor visitor = new CountingVisitor();
        spidHome.forEach(context, visitor);
        return (visitor.getCount() > 0L);
    }

    /**
     * Determines if the criteria is currently used by any credit category.
     *
     * @param context
     *            The operating context.
     * @param criteria
     *            The criteria in question.
     * @return Whether the criteria is currently used by any credit category.
     * @throws HomeException
     *             Thrown by home.
     */
    private boolean isCriteriaUsedByCreditCategory(final Context context, final AutoDepositReleaseCriteria criteria)
        throws HomeException
    {
        final And and = new And();
        and.add(new EQ(CreditCategoryXInfo.AUTO_DEPOSIT_RELEASE_CONFIGURATION, DunningConfigurationEnum.CUSTOM));
        and.add(new EQ(CreditCategoryXInfo.AUTO_DEPOSIT_RELEASE_CRITERIA, Long.valueOf(criteria.getIdentifier())));
        final Home creditCategoryHome = ((Home) context.get(CreditCategoryHome.class)).where(context, and);
        final CountingVisitor visitor = new CountingVisitor();
        creditCategoryHome.forEach(context, visitor);
        return (visitor.getCount() > 0L);
    }
}
