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
package com.trilogy.app.crm.bean.custom;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class Msisdn extends com.redknee.app.crm.bean.core.Msisdn
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * {@inheritDoc}
     */
    @Override
    public long getAuxSvcId()
    {
        return getAuxSvcId(getContext());
    }


    /**
     * Returns the additional MSISDN auxiliary service ID associated with this MSISDN.
     *
     * @param context
     *            The operating context.
     * @return ID of the additional MSISDN auxiliary service associated with this MSISDN.
     */
    public long getAuxSvcId(final Context context)
    {
        long id = super.getAuxSvcId();
        final long associationId = getSubAuxSvcId();
        if (isAMsisdn() && getState() == MsisdnStateEnum.IN_USE)
        {
            SubscriberAuxiliaryService association = null;
            try
            {
                association = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryService(context, associationId);
            }
            catch (final HomeException exception)
            {
                new DebugLogMsg(this, "Exception caught when lookup up subscriberauxiliaryservice", exception)
                    .log(context);
            }
            if (association == null)
            {
                new MinorLogMsg(this, "Cannot find SubscriberAuxiliaryService ID " + associationId, null).log(context);
            }
            else
            {
                id = association.getAuxiliaryServiceIdentifier();
            }
        }
        return id;
    }
}
