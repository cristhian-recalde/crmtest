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

import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.bean.KeyValueFeatureEnum;
import com.trilogy.app.crm.support.AlcatelSSCSupportHelper;



/**
 * Extension that contains key/value pairs that can be provisioned to an Alcatel SSC
 * for such broadband services
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class AlcatelSSCSpidExtension extends AbstractAlcatelSSCSpidExtension implements ContextAware
{

    public AlcatelSSCSpidExtension()
    {
        super();
    }

    public AlcatelSSCSpidExtension(Context ctx)
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
    public void setKeyValuePairs(Map keyValuePairs) throws IllegalArgumentException
    {
        assertKeyValuePairs(keyValuePairs);
        assertBeanNotFrozen();
        
        PMLogMsg pm = new PMLogMsg("AlcatelSSC", "SPID.initializeMap()");
        try
        {
            AlcatelSSCSupportHelper.get(getContext()).initializeMap(getContext(), KeyValueFeatureEnum.ALCATEL_SSC_SPID, keyValuePairs);
        }
        finally
        {
            if (getContext() != null)
            {
                pm.log(getContext());
            }
        }
        
        super.setKeyValuePairs(keyValuePairs);
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

    protected transient Context ctx_ = null;
}
