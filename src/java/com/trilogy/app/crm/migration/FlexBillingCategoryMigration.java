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
package com.trilogy.app.crm.migration;

import java.util.Collection;
import java.util.Iterator;

import com.trilogy.framework.xhome.beans.Function;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;

import com.trilogy.app.crm.bean.BillingOptionMapping;
import com.trilogy.app.crm.bean.BillingOptionMappingHome;
import com.trilogy.app.crm.bean.DestinationZone;
import com.trilogy.app.crm.bean.DestinationZoneHome;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.IdentifierSequence;
import com.trilogy.app.crm.bean.MsisdnPrefix;
import com.trilogy.app.crm.bean.MsisdnZonePrefix;
import com.trilogy.app.crm.bean.MsisdnZonePrefixHome;
import com.trilogy.app.crm.bean.ZoneInfo;
import com.trilogy.app.crm.bean.ZonePrefix;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;


/**
 * Provides the code for auto-migrating data from the old DestinationZone home to the new
 * BillingOptionMapping home and MsisdnZonePrefix home. Once this migration has taken
 * place on all existing deployments, this class and the DestinationZone data structures
 * can be removed from the code base. This is a fail-fast implementation of the migration.
 * Should a problem be encountered on the migration of any DestinationZone, then the
 * migration will abort and no further DestinationZones will be processed.
 *
 * @author gary.anderson@redknee.com
 */
public class FlexBillingCategoryMigration implements ContextAgent
{

    /**
     * Creates a new FlexBillingCategoryMigration for migration DestinationZone data into
     * the BillingOptionMapping home and MsisdnZonePrefix home.
     */
    public FlexBillingCategoryMigration()
    {
        this.migrationFailure_ = null;
    }


    /**
     * {@inheritDoc}
     */
    public void execute(final Context context) throws AgentException
    {
        if (isMigrationNeeded(context))
        {
            performMigration(context);
        }
    }


    /**
     * Gets the migration failure, if one was detected.
     *
     * @return The migration failure, if one was detected; null otherwise.
     */
    private Throwable getMigrationFailure()
    {
        return this.migrationFailure_;
    }


    /**
     * Indicates whether or not the migration detected a failure.
     *
     * @return True if the migration detected a failure; false otherwise.
     */
    private boolean isMigrationFailure()
    {
        return this.migrationFailure_ != null;
    }


    /**
     * Indicates whether or not the migration is needed.
     *
     * @param context
     *            The operating context.
     * @return True if a migration is needed; false otherwise.
     * @exception AgentException
     *                Thrown if the context fails to provide the information necessary for
     *                determining whether or not we need to migrate.
     */
    private boolean isMigrationNeeded(final Context context) throws AgentException
    {
        /*
         * If no sequence identifier has been created for the new BillingOptionMapping
         * table, then we assume the migration has not yet taken place.
         */
        final IdentifierSequence sequence;
        try
        {
            sequence = IdentifierSequenceSupportHelper.get(context).getIdentifierSequence(context, IdentifierEnum.BILLING_OPTION_RULE_ID);
        }
        catch (final HomeException exception)
        {
            throw new AgentException("Failed to determine if migration was necessary.", exception);
        }

        return sequence == null;
    }


    /**
     * Performs the work of migrating data from the old data structures to the new data
     * structures.
     *
     * @param context
     *            The operating context.
     * @exception AgentException
     *                Thrown if there are problems encountered during the migration.
     */
    private void performMigration(final Context context) throws AgentException
    {
        Home home = null;
        try
        {
            home = (Home) context.get(DestinationZoneHome.class);

            if (home == null)
            {
                throw new HomeException("The DestinationZoneHome does not exist in the context.");
            }

            MigrationVisitor visitor = new MigrationVisitor();

            visitor = (MigrationVisitor) home.forEach(context, visitor);
            setMigrationFailure(visitor.getMigrationFailure());
        }
        catch (final HomeException exception)
        {
            throw new AgentException("Failed to get DestinationZones for migration.", exception);
        }

        if (isMigrationFailure())
        {
            throw new AgentException("Migration failure detected.", getMigrationFailure());
        }
    }


    /**
     * Sets a detected migration failure.
     *
     * @param throwable
     *            A detected migration failure.
     */
    void setMigrationFailure(final Throwable throwable)
    {
        this.migrationFailure_ = throwable;
    }

    /**
     * The migration failure, if one was detected.
     */
    private Throwable migrationFailure_;

} // class


/**
 * Visitor for migrating a given DestinationZone to the BillingOptionMapping home and
 * MsisdnZonePrefix home.
 *
 * @author candy.wong@redknee.com
 */
class MigrationVisitor implements Visitor
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * {@inheritDoc}
     */
    public void visit(final Context ctx, final Object obj) throws AgentException, AbortVisitException
    {
        final DestinationZone zone = (DestinationZone) obj;

        try
        {
            migrate(ctx, zone);
        }
        catch (final Throwable exception)
        {
            setMigrationFailure(exception);
            throw new AbortVisitException("Migration failure");
        }
    }


    /**
     * Sets a detected migration failure.
     *
     * @param throwable
     *            A detected migration failure.
     */
    public void setMigrationFailure(final Throwable throwable)
    {
        this.migrationFailure_ = throwable;
    }


    /**
     * Gets the migration failure, if one was detected.
     *
     * @return The migration failure, if one was detected; null otherwise.
     */
    public Throwable getMigrationFailure()
    {
        return this.migrationFailure_;
    }


    /**
     * Migrates a given DestinationZone to the BillingOptionMapping home and
     * MsisdnZonePrefix home.
     *
     * @param context
     *            The operating context.
     * @param zone
     *            The DestinationZone to migrate.
     * @exception HomeException
     *                Thrown if there are problems accessing Home data in the context.
     */
    protected void migrate(final Context context, final DestinationZone zone) throws HomeException
    {
        final MsisdnZonePrefix prefixes = createPrefixSet(context, zone);
        createRules(context, prefixes, zone);
    }


    /**
     * Migrates a given DestinationZone to the BillingOptionMapping home.
     *
     * @param context
     *            The operating context.
     * @param prefixes
     *            The MsisdnZonePrefix to be used for the rules.
     * @param zone
     *            The DestinationZone to migrate.
     * @exception HomeException
     *                Thrown if there are problems accessing Home data in the context.
     */
    private void createRules(final Context context, final MsisdnZonePrefix prefixes, final DestinationZone zone)
        throws HomeException

    {
        final Function copyRule = new ZoneInfoToBillingOptionMappingFunction(prefixes);

        final Collection mappings = CollectionSupportHelper.get(context).process(context, zone.getServiceProviders().values(), copyRule);

        final Home home = (Home) context.get(BillingOptionMappingHome.class);

        final Iterator mappingIterator = mappings.iterator();
        while (mappingIterator.hasNext())
        {
            home.create(context, mappingIterator.next());
        }
    }


    /**
     * Migrates a given DestinationZone to the MsisdnZonePrefix home.
     *
     * @param context
     *            The operating context.
     * @param zone
     *            The DestinationZone to migrate.
     * @return The newly created MsisdnZonePrefix.
     * @exception HomeException
     *                Thrown if there are problems accessing Home data in the context.
     */
    private MsisdnZonePrefix createPrefixSet(final Context context, final DestinationZone zone) throws HomeException

    {
        final MsisdnZonePrefix prefixSet = new MsisdnZonePrefix();
        prefixSet.setDescription(zone.getName());

        final Function copyPrefix = new ZonePrefixToMsisdnPrefixFunction();

        CollectionSupportHelper.get(context).process(context, zone.getPrefixes(), copyPrefix, prefixSet.getPrefixes(context));

        final Home home = (Home) context.get(MsisdnZonePrefixHome.class);
        return (MsisdnZonePrefix) home.create(context, prefixSet);
    }

    /**
     * The migration failure, if one was detected.
     */
    private Throwable migrationFailure_;
}


/**
 * Adapts a {@link ZoneInfo} into {@link BillingOptionMapping}.
 *
 * @author cindy.wong@redknee.com
 */
class ZoneInfoToBillingOptionMappingFunction implements Function
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * MSISDN zone prefixes.
     */
    private final MsisdnZonePrefix prefixes_;


    /**
     * Create a new instance of <code>ZoneInfoToBillingOptionMappingFunction</code>.
     *
     * @param prefixes
     *            MSISDN zone prefixes.
     */
    public ZoneInfoToBillingOptionMappingFunction(final MsisdnZonePrefix prefixes)
    {
        this.prefixes_ = prefixes;
    }


    /**
     * {@inheritDoc}
     */
    public Object f(final Context ctx, final Object object)
    {
        final ZoneInfo info = (ZoneInfo) object;

        final BillingOptionMapping mapping = new BillingOptionMapping();
        mapping.setSpid(info.getSpid());
        mapping.setZoneIdentifier(this.prefixes_.getIdentifier());
        mapping.setBillingCategory(info.getBillingCategory());
        mapping.setTaxAuthority(info.getTaxAuthority1());
        /*
         * [Cindy] 2007-11-13: Roaming tax authority is removed from BillingOptionMapping.
         */
        mapping.setTaxAuthority2(info.getTaxAuthority2());

        return mapping;
    }
}


/**
 * Adapts a {@link ZonePrefix} into {@link MsisdnPrefix}.
 *
 * @author cindy.wong@redknee.com
 */
class ZonePrefixToMsisdnPrefixFunction implements Function
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * {@inheritDoc}
     */
    public Object f(final Context ctx, final Object object)
    {
        final ZonePrefix zonePrefix = (ZonePrefix) object;

        final MsisdnPrefix msisdnPrefix = new MsisdnPrefix();
        msisdnPrefix.setPrefix(zonePrefix.getPrefix());
        msisdnPrefix.setDescription(zonePrefix.getDescription());

        return msisdnPrefix;
    }
}
