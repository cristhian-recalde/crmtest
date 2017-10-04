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
package com.trilogy.app.crm.transfer;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.TfaRmiConfig;
import com.trilogy.app.crm.bean.core.SubscriptionType;

/**
 * @author gary.anderson@redknee.com
 */
public class TransferType
    extends AbstractTransferType
{
    private static final long serialVersionUID = 1L;


    public SubscriptionType getContributorType(final Context context)
    {
        return getSubscriptionType(context, getContributorTypeID());
    }


    public SubscriptionType getRecipientType(final Context context)
    {
        return getSubscriptionType(context, getRecipientTypeID());
    }


    private SubscriptionType getSubscriptionType(final Context context, final long typeID)
    {
        SubscriptionType typeBean = null;

        final TfaRmiConfig config = (TfaRmiConfig)context.get(TfaRmiConfig.class);
        if (typeID == config.getOperatorSubscriptionTypeID())
        {
            typeBean = new SubscriptionType();
            typeBean.setId(typeID);
            typeBean.setName("Operator");
            typeBean.setDescription("Operator");
        }
        else
        {
            typeBean = SubscriptionType.getSubscriptionType(context, typeID);
        }

        return typeBean;
    }

}
