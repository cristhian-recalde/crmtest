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

import com.trilogy.app.crm.bean.CrmVmPlan;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.exception.RemoveException;

/**
 * Prevents Voicemail Plan from being deleted when it is in use. A Voicemail Plan is "in use" if it is
 * used by a Voicemail service.
 *
 * @author victor.stratan@redknee.com
 */
public class CrmVmPlanRemoveProtectionHome extends HomeProxy
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 101L;

    /**
     * Creates a new instance of CrmVmPlanRemoveProtectionHome.
     *
     * @param delegate The delegate of this decorator.
     */
    public CrmVmPlanRemoveProtectionHome(final Home delegate)
    {
        super(delegate);
    }

    /**
     * Creates a new instance of CrmVmPlanRemoveProtectionHome.
     *
     * @param context The operating context.
     * @param delegate The delegate of this decorator.
     */
    public CrmVmPlanRemoveProtectionHome(final Context context, final Home delegate)
    {
        super(context, delegate);
    }

    /**
     * Prevents the voicemail plan from being deleted if it is currently used by a service. Throws
     * {@link RemoveException} if the voicemail plan is currently in use.
     *
     * @param context The operating context.
     * @param object The object being removed.
     * @throws HomeException Thrown if there are problems preventing the removal of this criteria.
     * @see HomeProxy#remove(Context, Object)
     */
    @Override
    public void remove(final Context context, final Object object) throws HomeException
    {
        final CrmVmPlan vmPlan = (CrmVmPlan) object;

        if (isVmPlanUsedByServices(context, vmPlan))
        {
            final String msg = "Voicemail Plan " + vmPlan.getId() + " is still in use";
            final RemoveException exception = new RemoveException(msg);
            new DebugLogMsg(this, msg, exception).log(context);
            throw exception;
        }

        super.remove(context, object);
    }

    /**
     * Determines if the vmPlan is currently used by any service provider.
     *
     * @param context The operating context.
     * @param vmPlan The voicemail plan in question.
     * @return Whether the voicemail plan is currently used by any service provider.
     * @throws HomeException Thrown by home.
     */
    protected boolean isVmPlanUsedByServices(final Context context, final CrmVmPlan vmPlan)
        throws HomeException
    {
        final And and = new And();
        and.add(new EQ(ServiceXInfo.TYPE, ServiceTypeEnum.VOICEMAIL));
        // Voice mail plan ID has String data type in Service and long in CrmVmPlan
        and.add(new EQ(ServiceXInfo.VM_PLAN_ID, String.valueOf(vmPlan.getId())));
        final Home serviceHome = ((Home) context.get(ServiceHome.class)).where(context, and);
        final CountingVisitor visitor = new CountingVisitor();
        serviceHome.forEach(context, visitor);
        return (visitor.getCount() > 0L);
    }
}
