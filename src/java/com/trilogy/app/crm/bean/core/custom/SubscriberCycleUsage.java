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
package com.trilogy.app.crm.bean.core.custom;

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.service.ChargingCycleHandler;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ChargingCycleSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * Stores the information provided about the subscriber's usage statistics
 * during previous billing cycle periods.  The particular billing cycle is
 * identified using the cycleStart property.
 *
 * @author gary.anderson@redknee.com
 */
public
class SubscriberCycleUsage extends com.redknee.app.crm.bean.SubscriberCycleUsage
{
	  public static final long serialVersionUID = -233697482996424607L;

    /**
     * Initializes the identification information of this usage: the subscriber
     * identifier, the account identifier, the SPID, and the associated dates.
     *
     * @param context The operating context.
     * @param msisdn The subscriber's voice number.
     * @param referenceDate The reference date of the collected data.
     *
     * @exception HomeException Thrown if there are problems accessing Home data
     * in the context.
     */
    public void initializeIdentification(
        final Context context,
        final String msisdn,
        final Date referenceDate)
        throws HomeException
    {
        final Subscriber subscriber =
            SubscriberSupport.lookupActiveSubscriberForMSISDN(context, msisdn);

        setSpid(subscriber.getSpid());
        setAccountIdentifier(subscriber.getBAN());
        setSubscriberIdentifier(subscriber.getId());

        final BillCycle cycle = BillCycleSupport.getBillCycleForBan(context, subscriber.getBAN());
        setReference(referenceDate);
        ChargingCycleHandler handler = ChargingCycleSupportHelper.get(context).getHandler(ChargingCycleEnum.MONTHLY);
        Date startDate = handler.calculateCycleStartDate(context, referenceDate, cycle.getDayOfMonth(), cycle.getSpid());
        
        setCycleStart(CalendarSupportHelper.get(context).findDateMonthsAfter(-1, startDate));
    }

    public String getMsisdn(Context ctx)
       throws HomeException
    {
       Subscriber sub = (Subscriber) ((Home)ctx.get(SubscriberHome.class)).find(ctx, getSubscriberIdentifier());
       return sub.getMSISDN();
    }

} // class
