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

import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bas.tps.ServiceActivationTemplate;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.SctAuxiliaryBundle;
import com.trilogy.app.crm.bean.SctAuxiliaryBundleHome;
import com.trilogy.app.crm.bean.SctAuxiliaryBundleXInfo;
import com.trilogy.app.crm.bean.SctAuxiliaryService;
import com.trilogy.app.crm.bean.SctAuxiliaryServiceHome;
import com.trilogy.app.crm.bean.SctAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.BundleProfileHome;


/**
 * A new Support class added with new methods to support for SCT operations.
 *
 * @author manda.subramanyam@redknee.com
 */
public final class SubscriberCreateTemplateSupport
{

    /**
     * Creates a new <code>SubscriberCreateTemplateSupport</code> instance. This method
     * is made private to prevent instantiation of utility class.
     */
    private SubscriberCreateTemplateSupport()
    {
        // empty
    }

    /**
     * Gets a specific SctAuxiliaryService associated with the given
     * Service Activation Template.
     *
     * @param associations A Collection of SctAuxiliaryServices.     
     * @param serviceIdentifier The AuxiliaryService id for which to look up
     * @return The specific SctAuxiliaryService associated with the given
     * Aux.Svc identifier.
     */
    public static Collection<SctAuxiliaryService> getSelectedSctAuxiliaryServices(final Collection<SctAuxiliaryService> associations,final long serviceIdentifier)
    {
        Collection<SctAuxiliaryService> entries = new ArrayList<SctAuxiliaryService>();
        for (SctAuxiliaryService association : associations)
        {
            if ( association.getAuxiliaryServiceIdentifier() == serviceIdentifier )
            {
                entries.add(association);
            }
        }
        return entries;
    }

    /**
     * Gets a specific SctAuxiliaryBundle associated with the given
     * Service Activation Template.
     *
     * @param associations A Collection of SctAuxiliaryBundles.
     * @param bundleIdentifier The AuxiliaryBundle id for which to look up
     * @return The specific SctAuxiliaryBundle associated with the given identifier.
     */
    public static Collection<SctAuxiliaryBundle> getSelectedSctAuxiliaryBundles(final Collection<SctAuxiliaryBundle> associations,final long bundleIdentifier)
    {
        Collection<SctAuxiliaryBundle> entries = new ArrayList<SctAuxiliaryBundle>();
        for (SctAuxiliaryBundle association : associations)
        {
            if ( association.getAuxiliaryBundleIdentifier() == bundleIdentifier )
            {
                entries.add(association);
            }
        }
        return entries;
    }
    
    /**
     * Gets the SctAuxiliaryServices associated with the given
     * SCT.
     *
     * @param context
     *            The operating context.
     * @param sct
     *            The service Activation Template for which to look up
     *            SctAuxiliaryServices.
     * @return The SctAuxiliaryServices associated with the given SCT.
     */
    public static Collection<SctAuxiliaryService> getSctAuxiliaryServices(final Context context,
        final ServiceActivationTemplate sct)
    {
        final long identifier = sct.getIdentifier();
        return getSctAuxiliaryServices(context, identifier);
    }


    /**
     * Returns the SctAuxiliaryServices for the given SCT Id.
     *
     * @param ctx
     *            Context object
     * @param identifier
     *            SCT Id
     * @return Collection of SCtAuxiliaryServices.
     */
    public static Collection<SctAuxiliaryService> getSctAuxiliaryServices(final Context ctx, final long identifier)
    {
        final Home home = (Home) ctx.get(SctAuxiliaryServiceHome.class);

        Collection<SctAuxiliaryService> auxiliaryServiceAssociations = null;
        try
        {
            auxiliaryServiceAssociations = home.where(ctx,
                new EQ(SctAuxiliaryServiceXInfo.SCT_IDENTIFIER, Long.valueOf(identifier))).selectAll(ctx);

        }
        catch (final UnsupportedOperationException e)
        {
            new MajorLogMsg(SubscriberCreateTemplateSupport.class,
                "Failed to look-up AuxiliaryService associations for SCT " + identifier, e).log(ctx);
        }
        catch (final HomeException e)
        {
            new MajorLogMsg(SubscriberCreateTemplateSupport.class,
                "Failed to look-up AuxiliaryService associations for SCT " + identifier, e).log(ctx);
        }

        if (auxiliaryServiceAssociations == null)
        {
            auxiliaryServiceAssociations = new ArrayList<SctAuxiliaryService>();
        }
        return auxiliaryServiceAssociations;
    }


    /**
     * Gets the SctAuxiliaryServices associated with the given
     * SCT.
     *
     * @param context The operating context.
     * @param sct  The service Activation Template for which to look up SctAuxiliaryServices.
     * @return The SctAuxiliaryServices associated with the given SCT.
     */
    public static Collection getSctAuxiliaryBundles(final Context context, final ServiceActivationTemplate sct)
    {
        final long identifier = sct.getIdentifier();
        return getSctAuxiliaryBundles(context, identifier);
    }


    /**
     * Returns the SctAuxiliaryServices for the given SCT Id
     * @param ctx Context object
     * @param identifier SCT Id
     * @return Collection of SCtAuxiliaryServices.
     */
    public static Collection getSctAuxiliaryBundles(final Context ctx, final long identifier)
    {
        final Home home = (Home) ctx.get(SctAuxiliaryBundleHome.class);

        Collection auxiliaryBundleAssociations;
        try
        {
            final EQ condition = new EQ(SctAuxiliaryBundleXInfo.SCT_IDENTIFIER, Long.valueOf(identifier));
            auxiliaryBundleAssociations = home.select(ctx, condition);
        }
        catch (UnsupportedOperationException e)
        {
            new MajorLogMsg(SubscriberCreateTemplateSupport.class,
                    "Failed to look-up AuxiliaryBundle associations for SCT " + identifier, e).log(ctx);
            auxiliaryBundleAssociations = new ArrayList();
        }
        catch (HomeException e)
        {
            new MajorLogMsg(SubscriberCreateTemplateSupport.class,
                    "Failed to look-up AuxiliaryBundle associations for SCT " + identifier, e).log(ctx);
            auxiliaryBundleAssociations = new ArrayList();
        }

        return auxiliaryBundleAssociations;
    }

    /**
     * Provides sorted map of AuxiliaryService identifiers (Long) to
     * SCT Auxiliary Services for the given Collection of SctAuxiliaryServices.
     * This is useful determining if a given AuxiliaryService is represented in
     * the given set.
     *
     * @param associations
     *            A Collection of SctAuxiliaryServices.
     * @return A unique, sorted map of AuxiliaryService identifiers (Long) to SCT Auxiliary Services.
     */
    public static SortedMap<Long, SctAuxiliaryService> getSelectionMap(final Collection<SctAuxiliaryService> associations)
    {
        final SortedMap<Long, SctAuxiliaryService> selections = new TreeMap<Long, SctAuxiliaryService>();

        if (associations == null)
        {
            return selections;
        }

        for (final SctAuxiliaryService association : associations)
        {
            selections.put(Long.valueOf(association.getAuxiliaryServiceIdentifier()), association);
        }

        return selections;
    }


    /**
     * Gets the SctAuxiliaryService associated with the given SCT id and given
     * auxiliaryservice id.
     *
     * @param context
     *            The operating context.
     * @param sctIdentifier
     *            The identifier of the SCT for which to look up
     * @param auxSvcIdentifier
     *            The identifier of the auxiliary service for which to look up
     *            SctAuxiliaryServices.
     * @return SctAuxiliaryService - The SctAuxiliaryService associated with the given SCT
     *         and given id.
     */
    public static SctAuxiliaryService getSctAuxiliaryServicesBySctIdAndSvcId(final Context context,
        final long sctIdentifier, final long auxSvcIdentifier)
    {
        final Home home = (Home) context.get(SctAuxiliaryServiceHome.class);

        SctAuxiliaryService auxiliaryService = null;

        final And and = new And();
        and.add(new EQ(SctAuxiliaryServiceXInfo.SCT_IDENTIFIER, Long.valueOf(sctIdentifier)));
        and.add(new EQ(SctAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, Long.valueOf(auxSvcIdentifier)));

        try
        {
            auxiliaryService = (SctAuxiliaryService) home.find(context, and);
        }
        catch (final Throwable t)
        {
            new MajorLogMsg(SubscriberCreateTemplateSupport.class,
                "Failed to look-up AuxiliaryService associations for SCT " + sctIdentifier
                    + " and auxiliary service Id " + auxSvcIdentifier, t).log(context);

        }
        return auxiliaryService;
    }


    /**
     * This method returns AuxiliaryService object for the given AuxiliaryService ID.
     *
     * @param ctx
     *            Context object
     * @param auxServId
     *            AuxiliaryService Id
     * @return AuxiliaryService Object
     */
    public static AuxiliaryService getAuxiliaryServiceforAuxId(final Context ctx, final long auxServId)
    {
        final Home home = (Home) ctx.get(AuxiliaryServiceHome.class);
        AuxiliaryService auxServ = null;
        try
        {
            auxServ = (AuxiliaryService) home.find(ctx, Long.valueOf(auxServId));
        }
        catch (final HomeException e)
        {
            new MinorLogMsg(SubscriberCreateTemplateSupport.class,
                "Exception while finding the Auxiliary Service for the given AuxService Id", e).log(ctx);
        }
        return auxServ;
    }

   /**
    * This method returns AuxiliaryService object for the given AuxiliaryService ID
    * @param ctx Context object
    * @param auxBundleId  Auxiliary Bundle Id
    * @return AuxiliaryService Object
    */
   public static BundleProfile getAuxiliaryBundleforAuxId(final Context ctx, final long auxBundleId)
   {
       Home home = (Home) ctx.get(BundleProfileHome.class);
       BundleProfile auxBundle = null;
       try
       {
           auxBundle = (BundleProfile) home.find(ctx, Long.valueOf(auxBundleId));
       }
       catch (HomeException e)
       {
           new MinorLogMsg(SubscriberCreateTemplateSupport.class,"Exception while finding the Auxiliary Bundle for the given AuxBundle Id", e).log(ctx);
       }
       return auxBundle;
   }

   /**
    * Gets the SctAuxiliaryBundle associated with the given auxBundleIdentifier
    *
    * @param context The operating context.
    * @param auxBundleIdentifier The identifier of the auxiliary bundle for which to look up
    * SctAuxiliaryBundles.
    * @return SctAuxiliaryBundle - The SctAuxiliaryBundle associated with the given id.
    */
   public static SctAuxiliaryBundle getSctAuxiliaryBundle(
       final Context context,
       final long auxBundleIdentifier)
   {
       final Home home = (Home)context.get(SctAuxiliaryBundleHome.class);

       SctAuxiliaryBundle auxiliaryBundle = null;
       try
       {
           auxiliaryBundle = (SctAuxiliaryBundle) home.find(context, new EQ(SctAuxiliaryBundleXInfo.AUXILIARY_BUNDLE_IDENTIFIER,Long.valueOf(auxBundleIdentifier)));
       }
       catch (HomeException e)
       {
           new MajorLogMsg(
                   SubscriberCreateTemplateSupport.class,
                   "Failed to look-up SctAuxiliaryBundle for auxiliary Bundle Id " + auxBundleIdentifier, e).log(context);
       }
       return auxiliaryBundle;
   }
}
