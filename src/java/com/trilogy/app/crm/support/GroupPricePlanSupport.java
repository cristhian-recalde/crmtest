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

package com.trilogy.app.crm.support;

import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;

import com.trilogy.app.crm.extension.account.AccountExtensionXInfo;
import com.trilogy.app.crm.extension.account.GroupPricePlanExtension;


/**
 * Support class for group price plan extension.
 *
 * @author cindy.wong@redknee.com
 * @since 2008-09-19
 */
public final class GroupPricePlanSupport
{

    /**
     * Creates a new <code>GroupPricePlanSupport</code> instance. This method is made
     * private to prevent instantiation of utility class.
     */
    private GroupPricePlanSupport()
    {
        // empty
    }


    /**
     * Returns the group price plan extension of the account, if exists.
     *
     * @param context
     *            The operating context.
     * @param ban
     *            Account to look up group price plan extension for.
     * @return Group price plan extension of the account.
     */
    public static GroupPricePlanExtension getGroupPricePlanExtension(final Context context, final String ban)
    {
        final List<GroupPricePlanExtension> extensions = ExtensionSupportHelper.get(context).getExtensions(context,
            GroupPricePlanExtension.class, new EQ(AccountExtensionXInfo.BAN, ban));
        GroupPricePlanExtension extension = null;
        if (extensions != null && !extensions.isEmpty())
        {
            extension = extensions.get(0);
        }
        return extension;
    }
}
