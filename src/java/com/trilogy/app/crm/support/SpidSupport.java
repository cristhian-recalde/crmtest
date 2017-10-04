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
package com.trilogy.app.crm.support;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XResultSet;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.BillCycleXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.bean.SpidIdentificationGroups;
import com.trilogy.app.crm.bean.SpidIdentificationGroupsHome;
import com.trilogy.app.crm.bean.SpidIdentificationGroupsXInfo;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberTypeRecurringChargeEnum;
import com.trilogy.app.crm.bean.TaxAuthority;
import com.trilogy.app.crm.bean.TaxAuthorityXInfo;
import com.trilogy.app.crm.bean.WeekDayEnum;


/**
 * Provides utility methods for dealing with SPIDs.
 *
 * @author gary.anderson@redknee.com
 */
public final class SpidSupport
{

    /**
     * Prevents instantiation of this utility class.
     */
    private SpidSupport()
    {
        // Empty
    }


    /**
     * Gets a list of all the subscriber identifiers associated with the given SPID.
     *
     * @param context
     *            The operating context.
     * @param spid
     *            The SPID.
     * @return A collection of subscriber identifiers. The collection may be empty, but
     *         will always be non-null.
     * @exception HomeException
     *                Thrown if there are any problems accessing Home data in the context.
     */
    public static Collection<String> getSubscriberIdentifiers(final Context context, final CRMSpid spid)
        throws HomeException
    {
        return getSubscriberIdentifiers(context, spid.getId());
    }


    /**
     * Gets a list of all the subscriber identifiers associated with the given SPID.
     *
     * @param context
     *            The operating context.
     * @param spid
     *            The identifier of the SPID.
     * @return A collection of subscriber identifiers. The collection may be empty, but
     *         will always be non-null.
     * @exception HomeException
     *                Thrown if there are any problems accessing Home data in the context.
     */
    public static Collection<String> getSubscriberIdentifiers(final Context context, final int spid)
        throws HomeException
    {
        final Collection<String> subscriberIdentifiers = new ArrayList<String>();

        final XDB xdb = (XDB) context.get(XDB.class);

        final XStatement sql = new XStatement()
        {

            private static final long serialVersionUID = 1L;


            public String createStatement(final Context ctx)
            {
                return " select ID from Subscriber where SPID = ? ";
            }


            public void set(final Context ctx, final XPreparedStatement ps) throws SQLException
            {
                ps.setInt(spid);
            }
        };

        xdb.forEach(context, new Visitor()
        {

            private static final long serialVersionUID = 1L;


            public void visit(final Context ctx, final Object obj) throws AgentException, AbortVisitException
            {
                try
                {
                    subscriberIdentifiers.add(((XResultSet) obj).getString(1));
                }
                catch (final SQLException e)
                {
                    throw new AgentException(e);
                }
            }
        }, sql);

        return subscriberIdentifiers;
    }


    /**
     * Gets the first TaxAuthority for this service provider.
     *
     * @param ctx
     *            The operating context.
     * @param spid
     *            The service provider.
     * @return The first tax authority for this SPID. If multiple tax authorities exist
     *         for this service provider, the result is indeterministic.
     * @throws HomeException
     *             Thrown if there are problems retrieving the tax authority.
     */
    public static TaxAuthority getDefTaxAuthority(final Context ctx, final int spid) throws HomeException
    {
        final TaxAuthority ta = HomeSupportHelper.get(ctx).findBean(ctx, TaxAuthority.class, new EQ(TaxAuthorityXInfo.SPID, spid));
        return ta;

    }


    /**
     * Returns the first bill cycle found for this service provider.
     *
     * @param ctx
     *            The operating context.
     * @param spid
     *            The service provider.
     * @return THe first bill cycle found for this service provider. If multiple bill
     *         cycles exist for this service provider, the result is indeterministic.
     * @throws HomeException
     *             Thrown if there are problems retrieving the bill cycle.
     */
    public static BillCycle getDefBillingCycle(final Context ctx, final int spid) throws HomeException
    {
        final BillCycle bc = HomeSupportHelper.get(ctx).findBean(ctx, BillCycle.class, new EQ(BillCycleXInfo.SPID, spid));
        return bc;
    }


    /**
     * Checks if this service provider needs pre-expiry SMS message.
     *
     * @param ctx
     *            The operating context.
     * @param spid
     *            The service provider.
     * @return Returns <code>true</code> if pre-expiry SMS message should be sent for
     *         subscribers belonging to this service provider, <code>false</code>
     *         otherwise.
     * @throws HomeException
     *             Thrown if there are problems retrieving the service provider.
     */
    public static boolean needsPreExpiryMsg(final Context ctx, final int spid) throws HomeException
    {
        final CRMSpid cfg = SpidSupport.getCRMSpid(ctx, spid);
        boolean needs = false;
        if (cfg != null)
        {
            final int preExpiryDays = cfg.getPreExpirySmsDays();
            if (preExpiryDays > 0)
            {
                needs = true;
            }
        }
        return needs;
    }


    /**
     * Returns the service provider with the provided SPID.
     *
     * @param ctx
     *            The operating context.
     * @param spid
     *            Service provider ID.
     * @return The (CRM) service provider with the provided SPID.
     * @throws HomeException
     *             Thrown if there are problems retrieving the service provider.
     */
    public static CRMSpid getCRMSpid(final Context ctx, final int spid) throws HomeException
    {
        return HomeSupportHelper.get(ctx).findBean(ctx, CRMSpid.class, spid);

    }


    /**
     * Returns the default tax authority of a service provider.
     *
     * @param ctx
     *            The operating context.
     * @param spid
     *            Service provider ID.
     * @return The ID of the default tax authority of the service provider.
     * @throws HomeException
     *             Thrown if there are problems retrieving the service provider.
     */
    public static int getDefaultTaxAuthority(final Context ctx, final int spid) throws HomeException
    {
        final CRMSpid sp = getCRMSpid(ctx, spid);
        if (sp != null)
        {
            return sp.getTaxAuthority();
        }
        throw new HomeException("Can not find service provider " + spid);
    }


    /**
     * Returns the default roaming tax authority of a service provider.
     *
     * @param ctx
     *            The operating context.
     * @param spid
     *            Service provider ID.
     * @return The ID of the default roaming tax authority of the service provider.
     * @throws HomeException
     *             Thrown if there are problems retrieving the service provider.
     */
    public static int getDefaultRoamingTaxAuthority(final Context ctx, final int spid) throws HomeException
    {
        final CRMSpid sp = getCRMSpid(ctx, spid);
        if (sp != null)
        {
            return sp.getRoamingTaxAuthority();
        }
        throw new HomeException("Can not find service provider " + spid);
    }


    /**
     * Determines whether deposit releases should be automatically converted into payments
     * towards subscribers' outstanding balances.
     *
     * @param ctx
     *            The operating context.
     * @param spid
     *            Service provider ID.
     * @return Returns <code>true</code> if a deposit release should be converted into a
     *         payment, <code>false</code> otherwise.
     * @throws HomeException
     *             Thrown if there are problems retrieving the service provider.
     */
    public static boolean isAutoConvertingDepositToPaymentOnRelease(final Context ctx, final int spid)
        throws HomeException
    {
        final CRMSpid sp = getCRMSpid(ctx, spid);
        if (sp != null)
        {
            return sp.isConvertToPaymentOnDepositRelease();
        }

        return false;
    }


    /**
     * Determines whether deposit releases should be affect the subscriber's credit limit.
     *
     * @param context
     *            The operating context.
     * @param spid
     *            Service provider ID.
     * @return Returns <code>true</code> if a subscriber's credit limit should be
     *         updated when deposit is released, <code>false</code> otherwise.
     * @throws HomeException
     *             Thrown if there are problems retrieving the service provider.
     */
    public static boolean isCreditLimitUpdatedOnDepositRelease(final Context context, final int spid)
        throws HomeException
    {
        final CRMSpid serviceProvider = getCRMSpid(context, spid);
        if (serviceProvider != null)
        {
            return serviceProvider.isChangeCreditLimitOnDepositRelease();
        }

        return false;
    }
    
    
    public static boolean isAllowDunningProcessSuspendedAccount(final Context context, final int spid)
    {
    	try
		{
	         final CRMSpid serviceProvider = getCRMSpid(context, spid);
	         if (serviceProvider != null)
	         {
	             return serviceProvider.isDunningProcessSuspendedAccount();
	         }
		}
         catch (HomeException e)
 		{
 			new DebugLogMsg(SpidSupport.class.getName(), "Encountered a HomeException while checking the suspended account eligible for dunning or not[id=" + spid + "]", e).log(context);
 		}

         return false;
     }

    /**
     * Returns whether recurring charges are prebilled.
     *
     * @param ctx
     *            The operating context.
     * @param spid
     *            Service provider identifier.
     * @return Whether recurring charges are prebilled.
     * @throws HomeException
     *             Thrown if there are problems looking up the service provider.
     */
    public static boolean isPrebilled(final Context ctx, final int spid) throws HomeException
    {
        final CRMSpid serviceProvider = SpidSupport.getCRMSpid(ctx, spid);
        return serviceProvider.isPrebilledRecurringChargeEnabled();
    }


    /**
     * Get Recurring Charge Applicable subscriber type from configuration.
     *
     * @param ctx
     *            The operating context.
     * @param id
     *            Service provider identifier.
     * @return Type of subscribers eligible for recurring charges.
     * @throws HomeException
     *             Thrown if there are problems looking up the service provider.
     */
    public static SubscriberTypeEnum getRecurringChargeApplicableSub(final Context ctx, final int id)
        throws HomeException
    {
        final CRMSpid spid = SpidSupport.getCRMSpid(ctx, id);
        final SubscriberTypeRecurringChargeEnum optionalEnum = spid.getRecurringChargeSubType();
        return getSubscriberTypeEnum(optionalEnum);
    }
    
    /**
     * Get MultiDay Recurring Charge Applicable subscriber type from configuration.
     *
     * @param ctx
     *            The operating context.
     * @param id
     *            Service provider identifier.
     * @return Type of subscribers eligible for recurring charges.
     * @throws HomeException
     *             Thrown if there are problems looking up the service provider.
     */
    public static SubscriberTypeEnum getMultiDayRecurringChargeApplicableSub(final Context ctx, final int id)
        throws HomeException
    {
        final CRMSpid spid = SpidSupport.getCRMSpid(ctx, id);
        final SubscriberTypeRecurringChargeEnum optionalEnum = spid.getMultiDayRecurringChargeSubType();
        return getSubscriberTypeEnum(optionalEnum);
    }


    /**
     * Translate SubscriberTypeRecurringChargeEnum to SubscriberTypeEnum, note that
     * SubscriberTypeRecurringChargeEnum.OPTIONAL will return NULL.
     *
     * @param optionalEnum
     *            Subscriber type recurring charge enum.
     * @return SubscriberTypeEnum.
     */
    public static SubscriberTypeEnum getSubscriberTypeEnum(final SubscriberTypeRecurringChargeEnum optionalEnum)
    {

        SubscriberTypeEnum result = null;
        if (optionalEnum != null)
        {
            if (optionalEnum.equals(SubscriberTypeRecurringChargeEnum.PREPAID))
            {
                result = SubscriberTypeEnum.PREPAID;
            }
            else if (optionalEnum.equals(SubscriberTypeRecurringChargeEnum.POSTPAID))
            {
                result = SubscriberTypeEnum.POSTPAID;
            }
        }
        return result;
    }
    
    /**
     * Get account system types to filter on, based on applicable system type
     * 
     * @param type
     * @return
     */
    public static final Set<SubscriberTypeEnum> getSystemTypeToCharge(SubscriberTypeEnum type)
    {
        Set<SubscriberTypeEnum> systemTypes = new HashSet<SubscriberTypeEnum>();
        systemTypes.add(SubscriberTypeEnum.HYBRID);
        
        if (SubscriberTypeEnum.PREPAID.equals(type))
        {
            systemTypes.add(SubscriberTypeEnum.PREPAID);
        }
        else if (SubscriberTypeEnum.POSTPAID.equals(type))
        {
            systemTypes.add(SubscriberTypeEnum.POSTPAID);
        }
        else
        {
            systemTypes.add(SubscriberTypeEnum.PREPAID);
            systemTypes.add(SubscriberTypeEnum.POSTPAID);
        }
        return systemTypes;
    }

    public static boolean isGroupPooledMsisdnGroup(Context ctx, int spid, int msisdnPoolId)
    {
    	try
		{
			CRMSpid spidConfig = getCRMSpid(ctx, spid);
			if( spidConfig != null)
			{
				return (spidConfig.getGroupPooledMSISDNGroup() == msisdnPoolId);
			}
		}
		catch (HomeException e)
		{
			new DebugLogMsg(SpidSupport.class.getName(), "Encountered a HomeException while trying to retrieve CRMSpid for [id=" + spid + "]", e).log(ctx);
		}
		return false;
    }
    
    public static int getGroupPooledMsisdnGroup(Context ctx, int spid)
    {
        try
        {
            final CRMSpid spidConfig = getCRMSpid(ctx, spid);
            if (spidConfig != null)
            {
                return spidConfig.getGroupPooledMSISDNGroup();
            }
        }
        catch (HomeException e)
        {
            new MinorLogMsg(SpidSupport.class.getName(), "Encountered a HomeException while trying to retrieve CRMSpid for [id=" + spid + "]", e).log(ctx);
        }
        return CRMSpid.DEFAULT_GROUPPOOLEDMSISDNGROUP;
    }

    public static long getPooledSubscriptionLevel(final Context ctx, final int spid)
    {
        try
        {
            final CRMSpid spidConfig = getCRMSpid(ctx, spid);
            if (spidConfig != null)
            {
                return spidConfig.getPoolSubscriptionLevel();
            }
        }
        catch (HomeException e)
        {
            new MinorLogMsg(SpidSupport.class.getName(), "Encountered a HomeException while trying to retrieve CRMSpid for [id=" + spid + "]", e).log(ctx);
        }
        return CRMSpid.DEFAULT_POOLSUBSCRIPTIONLEVEL;
    }
    
    public static SpidIdentificationGroups getSpidIdentificationGroups(Context ctx, int spid) throws HomeException
    {
        Home home = (Home) ctx.get(SpidIdentificationGroupsHome.class);
        return (SpidIdentificationGroups) home.find(new EQ(SpidIdentificationGroupsXInfo.SPID, Integer.valueOf(spid)));
    }


    // Ported from 7.4
    public static Collection<CRMSpid> selectByRecurDayOfWeek(Context ctx, WeekDayEnum nameOfDay) throws HomeException
    {
        final Home spidHome = (Home) ctx.get(CRMSpidHome.class);
        if (spidHome == null)
        {
            throw new HomeException("Failed to locate CRMSpidHome in the context.");
        }
    
        final EQ condition = new EQ(CRMSpidXInfo.WEEKLY_RECUR_CHARGING_DAY, nameOfDay);
        return spidHome.select(ctx, condition);
    }
    
    public static boolean isEnforceAuxServiceRecharge(final Context ctx, final int spid)
            throws HomeException
    {
        final CRMSpid sp = getCRMSpid(ctx, spid);
        if (sp != null)
        {
            return sp.isEnforceAuxServiceRecharge();
        }

        return false;
    }
    
    public static Collection<CRMSpid> getAllSpid(Context ctx) throws HomeException
    {
        final Home spidHome = (Home) ctx.get(CRMSpidHome.class);
        if (spidHome == null)
        {
            throw new HomeException("Failed to locate CRMSpidHome in the context.");
        }
        return spidHome.selectAll();
    }
    
} // class
