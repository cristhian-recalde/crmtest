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
package com.trilogy.app.crm.extension.usergroup;

import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.bean.KeyValueFeatureEnum;



/**
 * Extension that contains adjustment type code and their respective limit as
 * applicable to a user group
 *
 * @author cindy.wong@redknee.com
 * @since 8.3
 */
public class AdjustmentTypeLimitUserGroupExtension extends AbstractAdjustmentTypeLimitUserGroupExtension implements ContextAware
{

    public AdjustmentTypeLimitUserGroupExtension()
    {
        super();
    }

    public AdjustmentTypeLimitUserGroupExtension(Context ctx)
    {
        super();
        setContext(ctx);
    }

    /**
     * {@inheritDoc}
     */
    public void validate(Context context) throws IllegalStateException
    {
        // TODO: Validate extension contents (i.e. key/value pairs)
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setLimits(Map limits) throws IllegalArgumentException
    {
        assertLimits(limits);
        assertBeanNotFrozen();
        
        PMLogMsg pm = new PMLogMsg("AdjustmentTypeLimit", "UserGroup.initializeMap()");
        try
        {
            //AlcatelSSCSupport.initializeMap(getContext(), KeyValueFeatureEnum.ALCATEL_SSC_SPID, keyValuePairs);
        }
        finally
        {
            if (getContext() != null)
            {
                pm.log(getContext());
            }
        }
        
        super.setLimits(limits);
    }

    /**
     * {@inheritDoc}
     */
    public Context getContext()
    {
        return ctx_;
    }

    /**
     * {@inheritDoc}
     */
    public void setContext(Context ctx)
    {
        ctx_ = ctx;
    }

    protected Context ctx_ = null;
}
