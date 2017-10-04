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

package com.trilogy.app.crm.home.account;

import com.trilogy.app.crm.bean.account.Contact;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;

/**
 * Constructs the Contact home pipeline.
 */
public class ContactHomeFactory implements PipelineFactory
{
    /**
     * Create a new instance of <code>ContactHomeFactory</code>.
     */
    protected ContactHomeFactory()
    {
        // empty
    }


    /**
     * Returns an instance of <code>ContactHomeFactory</code>.
     *
     * @return An instance_ of <code>ContactHomeFactory</code>.
     */
    public static ContactHomeFactory instance()
    {
        if (instance_ == null)
        {
            instance_ = new ContactHomeFactory();
        }
        return instance_;
    }


    public Home createPipeline(final Context ctx, final Context serverCtx)
    {
        Home home = StorageSupportHelper.get(ctx).createHome(ctx, Contact.class, "XCONTACT");
        home =
            ConfigChangeRequestSupportHelper.get(ctx)
                .registerHomeForConfigSharing(ctx, home, Contact.class);

        return home;
    }

    /**
     * Singleton instance.
     */
    private static ContactHomeFactory instance_;
}
