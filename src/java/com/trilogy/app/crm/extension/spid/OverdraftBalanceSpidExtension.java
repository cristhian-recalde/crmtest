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
package com.trilogy.app.crm.extension.spid;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.framework.xhome.context.Context;

/**
 * Overdraft Balance spid extension.
 * @author Marcio Marques
 * @since 9.1.1
 *
 */public class OverdraftBalanceSpidExtension extends AbstractOverdraftBalanceSpidExtension
{

    @Override
    public boolean isLicensed(Context ctx)
    {
        return LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.OVERDRAFT_BALANCE_LICENSE);
    }
}
