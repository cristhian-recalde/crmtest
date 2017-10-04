/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.calldetail;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallTypeEnum;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.poller.Constants;
import com.trilogy.app.crm.support.CallDetailSupportHelper;

/**
 * This class sets the supportedSubscriberId field for call details records.
 * @author Marcio Marques
 *
 */
public class CallDetailSupportedSubscriberIdentifierSettingHome extends HomeProxy
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new SubscriberIdentifierSettingHome.
     * 
     * @param delegate
     *            The home to delegate to.
     */
    public CallDetailSupportedSubscriberIdentifierSettingHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        CallDetail callDetail = (CallDetail) obj;
        if ((CallTypeEnum.ORIG.equals(callDetail.getCallType()) || 
                (CallTypeEnum.SMS.equals(callDetail.getCallType()) && callDetail.getChargedParty()!=null && Constants.ER_MO_SMS.equals(callDetail.getChargedParty())) ||
                (CallTypeEnum.ROAMING_SMS.equals(callDetail.getCallType()) && callDetail.getChargedParty()!=null && Constants.ER_MO_SMS.equals(callDetail.getChargedParty())) ||
                CallTypeEnum.ROAMING_MO.equals(callDetail.getCallType())
                ) && !callDetail.getChargedMSISDN().equals(callDetail.getOrigMSISDN()))
        {
            final Msisdn msisdn = lookupMsisdn(ctx, callDetail.getOrigMSISDN());
            callDetail.setSupportedSubscriberID(msisdn.getSubscriberID(ctx, callDetail.getTranDate()));
        }
        else if ((CallTypeEnum.TERM.equals(callDetail.getCallType()) || 
                  (CallTypeEnum.SMS.equals(callDetail.getCallType()) && callDetail.getChargedParty()!=null && Constants.ER_MT_SMS.equals(callDetail.getChargedParty())) ||
                  (CallTypeEnum.ROAMING_SMS.equals(callDetail.getCallType()) && callDetail.getChargedParty()!=null && Constants.ER_MT_SMS.equals(callDetail.getChargedParty())) ||
                  CallTypeEnum.ROAMING_MT.equals(callDetail.getCallType())
                 ) && !callDetail.getChargedMSISDN().equals(callDetail.getDestMSISDN()))
        {
            final Msisdn msisdn = lookupMsisdn(ctx, callDetail.getOrigMSISDN());
            callDetail.setSupportedSubscriberID(msisdn.getSubscriberID(ctx, callDetail.getTranDate()));
        }
        CallDetailSupportHelper.get(ctx).debugMsg(CallDetailSupportedSubscriberIdentifierSettingHome.class, callDetail, " Setting supported subscriber id on the call detail", ctx);
        return getDelegate().create(ctx, obj);
    }

    /**
     * Looks-up the Msisdn for the given mobile number.
     *
     * @param ctx the operating context
     * @param msisdn The mobile number.
     * @return The Msisdn.
     *
     * @throws HomeException Thrown if there is a problem looking up the
     * given mobile number.
     */
    private Msisdn lookupMsisdn(final Context ctx, final String msisdn)
        throws HomeException
    {
        final Home home = (Home)ctx.get(MsisdnHome.class);
        if (home == null)
        {
            throw new HomeException(
                "Could not find MsisdnHome in context.");
        }

        final Msisdn msisdnObject = (Msisdn)home.find(ctx, msisdn);
        if (msisdnObject == null)
        {
            throw new HomeException(
                "Could not find Msisdn for \"" + msisdn + "\".");
        }

        return msisdnObject;
    }
}
