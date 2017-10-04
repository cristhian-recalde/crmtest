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

package com.trilogy.app.crm.home.calldetail;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallTypeEnum;
import com.trilogy.app.crm.support.CallDetailSupportHelper;
import com.trilogy.app.crm.support.DestinationZoneSupportHelper;


/**
 * Set BillingOptionMapping related fields on CallDetails when they're stored.
 *
 * @author kevin.greer@redknee.com
 * @author paul.sperneac@redknee.com
 */
public class CallDetailZoneHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Constructor. Keeps the context and the delegate and uses them to fill in the zone
     * info and redirect the calls afterwards
     *
     * @param ctx
     *            the context
     * @param delegate
     *            the next home in the chain
     */
    public CallDetailZoneHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * Creates the call detail record in the call detail home after it fills some zone
     * dependent info in the CallDetail object.
     *
     * @param ctx
     *            The operating context.
     * @param obj
     *            the call detail record
     * @return the record that was created
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final CallDetail cdr = (CallDetail) obj;

        setZoneValues(ctx, cdr);

        return getDelegate().create(ctx, obj);
    }


    /**
     * Modify the supplied CDR so that several of its fields are populated from
     * information taken from the PrefixService.
     *
     * @param ctx
     *            The operating context.
     * @param cdr
     *            Call detail record.
     */
    public static void setZoneValues(final Context ctx, final CallDetail cdr)
    {
        /*
         * TODO: Should try to reimplement the code without having to rely on the Billing
         * Category.
         */

        /*
         * if (cdr.getBillingCategory() == BillingCategoryEnum.DATA) { return; }
         */

        String dest = "";
        if (cdr.getCallType() == CallTypeEnum.SMS && cdr.getDestMSISDN() != null)
        {
            if (cdr.getDestMSISDN().length() < 10)
            {
                dest = DestinationZoneSupportHelper.get(ctx).getShortCodeDestinationZoneDescription(ctx, cdr.getDestMSISDN(), true);
            }
            else
            {
                dest = DestinationZoneSupportHelper.get(ctx).getShortCodeDestinationZoneDescription(ctx, cdr.getDestMSISDN(), false);
            }
        }
        else
        {
            dest = DestinationZoneSupportHelper.get(ctx).getDestinationZoneDescription(ctx, cdr.getDestMSISDN());
        }
        final String orig = DestinationZoneSupportHelper.get(ctx).getDestinationZoneDescription(ctx, cdr.getOrigMSISDN());

        cdr.setDestinationPartyLocation(dest);
        cdr.setDestPrefixDesc(dest);

        cdr.setOrigPrefixDesc(orig);

        CallDetailSupportHelper.get(ctx).debugMsg(CallDetailZoneHome.class, cdr, "Setting destination location : " + dest + ", origPrefixDesc:" + orig, ctx);
    
    }
}
