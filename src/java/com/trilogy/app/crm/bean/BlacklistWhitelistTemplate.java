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
package com.trilogy.app.crm.bean;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.ff.FFClosedUserGroupSupport;
import com.trilogy.app.crm.support.ServiceSupport;

public class BlacklistWhitelistTemplate extends AbstractBlacklistWhitelistTemplate implements LazyLoadBean
{

    private static final long serialVersionUID = -6784057683256271061L;

    public boolean lazyLoad(Context ctx, PropertyInfo property)
    {
        if (property != null)
        {
            if (property.getBeanClass().isAssignableFrom(this.getClass()))
            {
                PMLogMsg pm = new PMLogMsg(LazyLoadBean.class.getName(), this.getClass().getSimpleName() + ".lazyLoad("
                        + property.getName() + ")");
                lazyLoadAllProperties(ctx);
                pm.log(ctx);
            }
        }
        return false;
    }

    public boolean lazyLoadAllProperties(Context ctx)
    {
        boolean result = false;
        if (getIdentifier() > 0)
        {
            try
            {
                CallingGroupTypeEnum callingGroupTypeEnum = null;
                if(getType().getIndex() == BlacklistWhitelistTypeEnum.BLACKLIST_INDEX)
                {
                    callingGroupTypeEnum = CallingGroupTypeEnum.BL;
                }
                else
                {
                    callingGroupTypeEnum = CallingGroupTypeEnum.WL;
                }
                final com.redknee.app.crm.bean.core.Service service = ServiceSupport
                        .getServiceForBlacklistWhitelistTemplate(ctx, getIdentifier(),
                                callingGroupTypeEnum);
                if (service != null)
                {
                    super.setServiceReferencedIn(service.getIdentifier());
                    result = true;
                }
            }
            catch (Exception e)
            {
                LogSupport.major(ctx, FFClosedUserGroupSupport.class,
                        "Unable to retrieve Service for Blacklist Whitelist Template : " + getIdentifier(), e);
            }
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public long getServiceReferencedIn()
    {
        return getServiceReferencedIn(getContext());
    }
    
    /**
     * @param context
     * @return
     */
    private long getServiceReferencedIn(Context context)
    {
        if (context!=null && super.getServiceReferencedIn()==-1)
        {
            lazyLoadAllProperties(context);
        }
        return super.getServiceReferencedIn();
    }
}
