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

package com.trilogy.app.crm.poller;

import java.util.List;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.calldetail.CallTypeEnum;


/**
 * MCG Advanced Event Rating ER (511) call detail creator.
 *
 * @author cindy.wong@redknee.com
 * @since 24-Jun-08
 */
public class MCGAdvancedEventCallDetailCreator extends AbstractAdvancedEventCallDetailCreator
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDebugParamsString(final List<String> params)
    {
        return params.toString();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected SubscriberTypeEnum getEquivalentSubscriberType(final int scpId)
    {
        // MCG poller currently doesn't support SCP ID.
        return SubscriberTypeEnum.HYBRID;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean processScpId(final Context context, final int scpId)
    {
        // MCG poller currently doesn't support SCP ID.
        return true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected CallTypeEnum getCallType(final String protocol)
    {
        if ("MMS".equals(protocol))
        {
            return CallTypeEnum.MMS;
        }
        return super.getCallType(protocol);
    }

}
