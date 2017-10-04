/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.AbstractAdjustmentType;
import com.trilogy.app.crm.bean.MsisdnGroup;
import com.trilogy.app.crm.numbermgn.NumberMgnSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * @author lxia
 */
public class MsisdnGroupValidatingHome extends HomeProxy
{

    public MsisdnGroupValidatingHome(Context ctx, Home _delegate)
    {
        super(_delegate);
        setContext(ctx);
    }


    public Object create(Context ctx, Object obj) throws HomeException
    {
        MsisdnGroup mg = (MsisdnGroup) obj;
        /*
         * prevent customer create a msisnd group with id 999 Otherwise, the MSISDN Web
         * Control won't show up on subscriber creation
         */
        if (mg.getId() == RESERVED_GROUP_ID_CUSTOM)
        {
            throw new HomeException("Group ID is reserved for system");
        }
        validate(ctx, obj);
        return getDelegate().create(ctx, obj);
    }


    public Object store(Context ctx, Object obj) throws HomeException
    {
        MsisdnGroup mg = (MsisdnGroup) obj;
        validate(ctx, obj);
        return getDelegate().store(ctx, obj);
    }


    public void remove(Context ctx, Object obj) throws HomeException
    {
        MsisdnGroup mg = (MsisdnGroup) obj;
        if (NumberMgnSupport.isInUsed(ctx, mg.getId()))
        {
            throw new HomeException("Can not remove this group, there are MSISDNs associated with this group");
        }
        getDelegate().remove(ctx, obj);
    }


    private void validate(final Context ctx, Object obj) throws HomeException
    {
        MsisdnGroup group = (MsisdnGroup) obj;
        if (group.getFee() > 0 && (group.getAdjustmentId() == MsisdnGroup.DEFAULT_ADJUSTMENTID))
        {
            throw new HomeException("Invalid adjustment type!  Please select a valid adjustment type.");
        }
    }

    static final public int RESERVED_GROUP_ID_CUSTOM = 999;
}
