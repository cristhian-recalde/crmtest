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

import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXInfo;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class AuxiliaryService extends com.redknee.app.crm.bean.core.AuxiliaryService
{
    /**
     * Gets the existing subscriber associations.
     *
     * @param context
     *            The operating context.
     *            
     * @return The set of all subscriber-auxiliary services associated with the provided auxiliary service.
     * 
     * @exception HomeException
     *                Thrown if there are problems accessing Home information
     */
    public Collection<SubscriberAuxiliaryService> getSubscriberAssociations(final Context ctx) throws HomeException
    {
        return HomeSupportHelper.get(ctx).getBeans(
                ctx, 
                SubscriberAuxiliaryService.class, 
                new EQ(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, Long.valueOf(this.getIdentifier())));
    }
    

    /**
     * Determines whether or not there are any subscriber associations for this
     * auxiliary service.
     *
     * @param context
     *            The operating context.
     *            
     * @return True iff there are subscriber associations for this auxiliary service.
     * 
     * @exception HomeException
     *                Thrown if there are problems accessing Home information
     */
    public boolean hasSubscriberAssociations(final Context ctx) throws HomeException
    {
        return HomeSupportHelper.get(ctx).hasBeans(
                ctx, 
                SubscriberAuxiliaryService.class, 
                new EQ(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, Long.valueOf(this.getIdentifier())));
    }
    

    /**
     * Gets the number of existing subscriber associations.
     *
     * @param context
     *            The operating context.
     *            
     * @return The set of all subscriber-auxiliary services associated with the provided auxiliary service.
     * 
     * @exception HomeException
     *                Thrown if there are problems accessing Home information
     */
    public long getNumberOfSubscriberAssociations(final Context ctx) throws HomeException
    {
        return HomeSupportHelper.get(ctx).getBeanCount(
                ctx, 
                SubscriberAuxiliaryService.class, 
                new EQ(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, Long.valueOf(this.getIdentifier())));
    }

    /**
     * Returns the group leader subscriber for this particular group chargeable auxiliary service type.
     * When adding auxiliary service type to group chargeable set, add implementation to get the leader for a sub.
     *
     * @param ctx the operating context
     * @param sub the sub for which to get the leader
     * @return the group leader of this auxiliary service type for the given subscriber
     * @throws HomeException
     */
    public Subscriber getGroupLeaderForCharging(final Context ctx, final Subscriber sub) throws HomeException
    {
        Subscriber result = null;
        if (this.getType() == AuxiliaryServiceTypeEnum.Vpn)
        {
            Account account = sub.getAccount(ctx);
            final Account rootAccount = account.getRootAccount(ctx);
            result = getGroupLeaderForCharging(ctx, rootAccount);
        }

        return result;
    }

    /**
     * Returns the group leader subscriber for this particular group chargeable auxiliary service type.
     * When adding auxiliary service type to group chargeable set, add implementation to get the leader for a sub.
     *
     * @param ctx the operating context
     * @param sub the sub for which to get the leader
     * @return the group leader of this auxiliary service type for the given subscriber
     * @throws HomeException
     */
    public Subscriber getGroupLeaderForCharging(final Context ctx, final Account account) throws HomeException
    {
        Subscriber result = null;
        if (this.getType() == AuxiliaryServiceTypeEnum.Vpn)
        {
            final Account rootAccount = account.getRootAccount(ctx);
            if (rootAccount.getVpnMSISDN() != null && rootAccount.getVpnMSISDN().length() != 0)
            {
                result = SubscriberSupport.lookupSubscriberForMSISDN(ctx, rootAccount.getVpnMSISDN());
            }
        }

        return result;
    }
    
    
    public short getChargingCycleType()
    {
        switch (this.getChargingModeType().getIndex())
        {
        case ServicePeriodEnum.MONTHLY_INDEX:
            return ChargingConstants.CHARGING_CYCLE_MONTHLY;
        case ServicePeriodEnum.ONE_TIME_INDEX:
            return ChargingConstants.CHARGING_CYCLE_ONETIME;
        case ServicePeriodEnum.WEEKLY_INDEX:
            return ChargingConstants.CHARGING_CYCLE_WEEKLY;
        case ServicePeriodEnum.MULTIMONTHLY_INDEX:
            return ChargingConstants.CHARGING_CYCLE_WEEKLY;
        }
        
        return ChargingConstants.CHARGING_CYCLE_CONFIGURABLE; 
    }
}
