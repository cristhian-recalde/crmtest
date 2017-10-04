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

package com.trilogy.app.crm.web.control;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.AutoDepositReleaseConfigurationEnum;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CRMSpidXInfo;

/**
 * A proxy web control which filters service providers base on whether Auto Deposit Release is enabled.
 *
 * @author cindy.wong@redknee.com
 */
public class CRMSpidFilterAutoDepositReleaseProxyWebControl extends ProxyWebControl
{
    /**
     * Whether to include service providers with Auto Deposit Release enabled (<code>TRUE</code>) or disabled
     * (<code>FALSE</code>).
     */
    private final AutoDepositReleaseConfigurationEnum useAutoDepositRelease_;

    /**
     * Creates a new proxy web control which only lists SPIDs with Auto Deposit Release enabled.
     */
    public CRMSpidFilterAutoDepositReleaseProxyWebControl()
    {
        super();
        useAutoDepositRelease_ = AutoDepositReleaseConfigurationEnum.YES;
    }

    /**
     * Creates a new proxy web control which only lists SPIDs with Auto Deposit Release enabled or disabled, base on the
     * value of <code>useAutoDepositRelease</code>.
     *
     * @param useAutoDepositRelease
     *            If set to <code>TRUE</code>, only include service providers with Auto Deposit Release enabled;
     *            otherwise only include those with Auto Deposit Release disabled (<code>FALSE</code>)
     */
    public CRMSpidFilterAutoDepositReleaseProxyWebControl(
        final AutoDepositReleaseConfigurationEnum useAutoDepositRelease)
    {
        super();
        useAutoDepositRelease_ = useAutoDepositRelease;
    }

    /**
     * Creates a new proxy web control which only lists SPIDs with Auto Deposit Release enabled.
     *
     * @param delegate
     *            The delegate web control responsible for actual rendering
     */
    public CRMSpidFilterAutoDepositReleaseProxyWebControl(final WebControl delegate)
    {
        super(delegate);
        useAutoDepositRelease_ = AutoDepositReleaseConfigurationEnum.YES;
    }

    /**
     * Creates a new proxy web control which only lists SPIDs with Auto Deposit Release enabled or disabled, base on the
     * value of <code>useAutoDepositRelease</code>.
     *
     * @param delegate
     *            The delegate web control responsible for actual rendering
     * @param useAutoDepositRelease
     *            If set to <code>TRUE</code>, only include service providers with Auto Deposit Release enabled;
     *            otherwise only include those with Auto Deposit Release disabled (<code>FALSE</code>)
     */
    public CRMSpidFilterAutoDepositReleaseProxyWebControl(final WebControl delegate,
        final AutoDepositReleaseConfigurationEnum useAutoDepositRelease)
    {
        super(delegate);
        useAutoDepositRelease_ = useAutoDepositRelease;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context wrapContext(final Context context)
    {
        final Context subContext = context.createSubContext();
        subContext.put(CRMSpidHome.class, ((Home) context.get(CRMSpidHome.class)).where(context, new EQ(
            CRMSpidXInfo.USE_AUTO_DEPOSIT_RELEASE, useAutoDepositRelease_)));
        return subContext;
    }
}
