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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Collection;

import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.filter.EitherPredicate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.*;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Provides a custom WebControl for selection FreeCallTime templates from within
 * a PricePlanVersion.
 *
 * @author gary.anderson@redknee.com
 */
public
class PricePlanVersionFreeCallTimeWebControl
    extends FreeCallTimeKeyWebControl
{
    /**
     * Gets the singleton instance of this webcontrol.
     *
     * @return The singleton instance of this webcontrol.
     */
    public static PricePlanVersionFreeCallTimeWebControl instance()
    {
        return INSTANCE;
    }


    /**
     * Prevents external instantiation of this class.
     */
    protected PricePlanVersionFreeCallTimeWebControl()
    {
        // Empty
    }


    /**
     * {@inheritDoc}
     */
    public void toWeb(
        Context context,
        final PrintWriter out,
        final String name,
        final Object object)
    {
        if (context.getInt("MODE", DISPLAY_MODE) == DISPLAY_MODE)
        {
            super.toWeb(context, out, name, object);
            return;
        }

        context = context.createSubContext();
        context.setName(getClass().getName());

        Home home = (Home)context.get(FreeCallTimeHome.class);
        home = createFilteredHome(context, home);

        final Long identifier = (Long)object;
        if (identifier.longValue() == AbstractFreeCallTime.DEFAULT_IDENTIFIER)
        {
            home = createInitialSelectionHome(context, home);
        }

        context.put(FreeCallTimeHome.class, home);

        super.toWeb(context, out, name, object);
    }


    /**
     * Creates a filterred home that reveals only the FreeCallTime templates for
     * the proper SPID.
     *
     * @param context The operating context.
     * @param delegate The Home to which the filtered home delegates.
     *
     * @return A SPID-filtered Home.
     */
    private Home createFilteredHome(final Context context, final Home delegate)
    {
        final PricePlan parentPlan = getParentPricePlan(context);
        final Home home = new FilteredSelectAllHome(delegate, parentPlan.getSpid());
        return home;
    }


    /**
     * Creates a Home that provides a default FreeCallTime with a name of
     * "-- Choose Template --".  Since this is a default, it can't be saved from
     * the price plan screen because the default identifier is invalid.
     *
     * @param context The operating context.
     * @param delegate The home to which we delegate to look up real FCTs.
     *
     * @return A Home that provides a default, but invalid, FreeCallTime.
     */
    private Home createInitialSelectionHome(final Context context, final Home delegate)
    {
        final Home transientHome = new FreeCallTimeTransientHome(context);
        final FreeCallTime template = new FreeCallTime();
        template.setName("-- Choose Template --");

        try
        {
            transientHome.create(context,template);
        }
        catch (final HomeException exception)
        {
            new MinorLogMsg(
                this,
                "Failed to add invalid selection to initial selection home.",
                exception).log(context);
        }

        final Home home = new OrHome(transientHome, delegate);

        return home;
    }


    /**
     * Gets the parent PricePlan of the bean being rendered.
     *
     * @param context The operating context.
     * @return The prent PricePlan of the bean being rendered.
     */
    private PricePlan getParentPricePlan(final Context context)
    {
        final PricePlan parentPlan =
            (PricePlan)context.get(WebControllerWebControl57.PARENT_CPROPERTY);

        return parentPlan;
    }


    /**
     * The one singleton instance of this class.
     */
    private static final PricePlanVersionFreeCallTimeWebControl INSTANCE =
        new PricePlanVersionFreeCallTimeWebControl();


    /**
     * Provides a home that filters based on SPID when the selectAll() method is
     * called.
     */
    private static
    class FilteredSelectAllHome
        extends HomeProxy
    {
        /**
         * Creates a new FilteredSelectAllHome for the given SPID.
         *
         * @param delegate The home to which we delegate for actual FCTs.
         * @param spid The SPID of the FCTs to show.
         */
        public FilteredSelectAllHome(final Home delegate, final int spid)
        {
            super(delegate);
            predicate_ = createWhereClause(spid);
        }


        /**
         * {@inheritDoc}
         */
        public Collection select(Context ctx,Object what)
            throws HomeException
        {
            return super.select(ctx,predicate_);
        }


        /**
         * Creates the home predicate used to filter based on SPID.
         *
         * @param spid The SPID of the FCTs to show.
         * @return The home predicate used to filter based on SPID.
         */
        private Object createWhereClause(final int spid)
        {
            final Object predicate =
                new EitherPredicate(
                    new SpidAwarePredicate(spid),
                    " spid = " + spid);

            return predicate;
        }


        /**
         * The home predicate used to filter based on SPID.
         */
        private final Object predicate_;

    } // inner-class


    /**
     * Provides a predicate that filters based on spid.
     */
    private static
    class SpidAwarePredicate
        implements Predicate
    {
        /**
         * Creates a new predicate for the given SPID.
         *
         * @param spid The SPID on which to match.
         */
        public SpidAwarePredicate(final int spid)
        {
            spid_ = spid;
        }


        /**
         * {@inheritDoc}
         */
        public boolean f(Context ctx,final Object obj)
        {
            final SpidAware bean = (SpidAware)obj;
            return spid_ == bean.getSpid();
        }


        /**
         * The SPID on which to match.
         */
        private final int spid_;

    } // inner-class

} // class
