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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.CRMSpid;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class CRMTaxAuthoritySupport extends DefaultTaxAuthoritySupport
{
    protected static TaxAuthoritySupport CRM_instance_ = null;
    public static TaxAuthoritySupport instance()
    {
        if (CRM_instance_ == null)
        {
            CRM_instance_ = new CRMTaxAuthoritySupport();
        }
        return CRM_instance_;
    }

    protected CRMTaxAuthoritySupport()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTEICEnabled(final Context ctx, final int spid)
    {
        boolean enabled = false;
        try
        {
            final CRMSpid sp = SpidSupport.getCRMSpid(ctx, spid);
            if (sp != null)
            {
                enabled = sp.isEnableTEIC();
            }
        }
        catch (final HomeException he)
        {
            LogSupport.minor(ctx, DefaultTaxAuthoritySupport.class,
                    "Error encountered while finding the service provider with id " + spid, he);
        }
        return enabled;
    }
}
