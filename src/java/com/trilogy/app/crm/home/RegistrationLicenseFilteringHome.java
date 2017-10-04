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

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.WhereHome;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.config.AccountRequiredFieldConfig;
import com.trilogy.app.crm.config.AccountRequiredFieldConfigXInfo;


/**
 * Home that filters out registration specific entities if the feature is disabled.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.6
 */
public class RegistrationLicenseFilteringHome extends WhereHome
{
    public RegistrationLicenseFilteringHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getWhere(Context ctx)
    {
        Object where = super.getWhere(ctx);                
        
        LicenseMgr lMgr = (LicenseMgr) ctx.get(LicenseMgr.class);
        if (lMgr == null || !lMgr.isLicensed(ctx, LicenseConstants.ACCOUNT_REGISTRATION))
        {
            try
            {
                Class beanClass = getClass(ctx);
                if (beanClass != null)
                {
                    if (AccountRequiredFieldConfig.class.getName().equals(beanClass.getName()))
                    {
                        Object tempWhere = new EQ(AccountRequiredFieldConfigXInfo.REGISTRATION_ONLY, false);
                        if (where != null)
                        {
                            where = new And().add(tempWhere).add(where);
                        }
                        else
                        {
                            where = tempWhere;
                        }
                    }
                    else if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, beanClass.getName() + " is not supported for registration entry filtering.", null).log(ctx);
                    }
                }
                else
                {
                    new MajorLogMsg(this, "Unable to determine bean class for home.  Unable to filter out registration entries.", null).log(ctx);
                }
            }
            catch (HomeException e)
            {
                new MajorLogMsg(this, "Error determining bean class for home.  Unable to filter out registration entries.", e).log(ctx);
            }
        }
        
        if (where == null)
        {
            where = True.instance();
        }
        return where;
    }

}
