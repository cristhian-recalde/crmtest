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

package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.bean.GSMPackageHome;
import com.trilogy.app.crm.bean.GeneralConfigSupport;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.TDMAPackageHome;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.numbermgn.GenericPackage;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.PackageSupport;
import com.trilogy.app.crm.support.PackageSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.bean.TDMAPackageXInfo;

/**
 * Static methods for claiming and releasing Msisdn's and Package's.
 *
 * @author paul.sperneac@redknee.com
 */
public final class Claim
{

    /**
     * Creates a new <code>Claim</code> instance. This method is made private to prevent
     * instantiation of utility class.
     */
    private Claim()
    {
        // empty
    }


    /**
     * Mark the MSISDN as being in use.
     *
     * @param ctx
     *            The operating context.
     * @param msisdn
     *            MSISDN being claimed.
     * @param subscriberID
     *            Subscriber claiming it.
     * @param el
     *            Exception listener. If any exception(s) needs to be thrown by the
     *            function, they will be added to this listener.
     * @deprecated [Cindy] 2007-10-09: This function is not currently called by any other
     *             functions.
     */
    @Deprecated
    public static void claimMsisdn(final Context ctx, final Msisdn msisdn, final String subscriberID,
        final HTMLExceptionListener el)
    {
        try
        {
            final Home msisdnHome = (Home) ctx.get(MsisdnHome.class);

            if (msisdnHome == null)
            {
                throw new HomeException("System Error: MsisdnHome does not exist in context");
            }

            if (msisdn != null)
            {
                msisdn.claim(ctx, subscriberID);
                msisdnHome.store(ctx, msisdn);
            }
        }
        catch (final Exception e)
        {
            new MinorLogMsg(Claim.class.getName(), "failed to mark msisdn as in use: " + msisdn, e).log(ctx);
            if (el != null)
            {
                final HomeException homeEx = new HomeException("provisioning error: failed to mark mobile number "
                    + msisdn + " as in use", e);
                el.thrown(homeEx);
            }
        }

    }


    /**
     * Mark the Package as in use.
     *
     * @param ctx
     *            The operating context.
     * @param pack
     *            Package to be claimed.
     * @param technology
     *            Technology to claim.
     * @throws HomeException
     *             Thrown if there are problems claiming the package.
     * @deprecated [Cindy] 2007-10-09: This function is not called by any functions other
     *             than {@link #claimPackage(Context, Subscriber)}, which, in turn, is
     *             not called by any other functions.
     */
    @Deprecated
    public static void claimPackage(final Context ctx, final GenericPackage pack, final TechnologyEnum technology)
        throws HomeException
    {
        if (pack != null)
        {
            pack.setState(PackageStateEnum.IN_USE);
            storePackageBasedOnTechnology(ctx, pack, technology);
        }
    }


    /**
     * Claims the package of the subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The subscriber claiming the package.
     * @throws HomeException
     *             Thrown if there are problems looking up the package.
     * @deprecated [Cindy] 2007-10-09: This function is not currently called by any other
     *             functions.
     */
    @Deprecated
    public static void claimPackage(final Context ctx, final Subscriber subscriber) throws HomeException
    {
        final GenericPackage pack = PackageSupportHelper.get(ctx).getPackage(ctx, subscriber.getTechnology(), subscriber
            .getPackageId(), subscriber.getSpid());
        if (pack == null)
        {
            throw new HomeException("Cannot find package " + subscriber.getPackageId() + " of subscriber "
                + subscriber.getId());
        }
        if (SafetyUtil.safeEquals(pack.getState(), PackageStateEnum.AVAILABLE))
        {
            claimPackage(ctx, pack, subscriber.getTechnology());
        }
        else
        {
            throw new HomeException("Cannot claim package " + pack.getPackId() + " for subscriber "
                + subscriber.getId() + ": Package not available");
        }
    }


    /**
     * Releases the package. The package will go into HELD state.
     *
     * @param ctx
     *            The operating context.
     * @param packageId
     *            Package ID to be released.
     * @param technology
     *            Technology of the package.
     * @throws HomeException
     *             Thrown if there are problems releasing the package.
     */
    public static void releasePackage(final Context ctx, final String packageId, final TechnologyEnum technology, int spid)
        throws HomeException
    {
        final GenericPackage pack = PackageSupportHelper.get(ctx).getPackage(ctx, technology, packageId, spid);
        if (pack == null)
        {
            throw new HomeException("Cannot find package " + packageId);
        }
        pack.setState(PackageStateEnum.HELD);
        storePackageBasedOnTechnology(ctx, pack, technology);
    }


    /**
     * Releases the MSISDN. The MSISDN will go into HELD state.
     *
     * @param ctx
     *            The operating context.
     * @param msisdn
     *            The MSISDN to be released.
     * @throws HomeException
     *             Thrown if there are problems releasing the MSISDN.
     */
    public static void releaseMsisdn(final Context ctx, final Msisdn msisdn) throws HomeException
    {
        final Home msisdnHome = (Home) ctx.get(MsisdnHome.class);

        if (msisdnHome == null)
        {
            throw new HomeException("System Error: MsisdnHome does not exist in context");
        }

        if (msisdn != null)
        {
            msisdn.release();
            msisdnHome.store(ctx, msisdn);
        }
    }


    /**
     * Retrieves the MSISDN object.
     *
     * @param ctx
     *            The operating context.
     * @param msisdn
     *            The MSISDN to be looked up.
     * @return The MSISDN object with the provided MSISDN, or <code>null</code> if none
     *         exists.
     * @throws HomeException
     *             Thrown if there are problems looking up the MSISDN.
     * @deprecated Use {@link MsisdnSupport#getMsisdn(Context, String)} instead.
     */
    @Deprecated
    public static Msisdn getMsisdn(final Context ctx, final String msisdn) throws HomeException
    {
        final Home msisdnHome = (Home) ctx.get(MsisdnHome.class);

        if (msisdnHome == null)
        {
            throw new HomeException("System Error: MsisdnHome does not exist in context");
        }

        Msisdn lookup = null;
        // fax/data msisdn is optional so might be empty, in such case, ignore them
        if (msisdn == null || msisdn.length() == 0)
        {
            return null;
        }

        try
        {
            lookup = (Msisdn) msisdnHome.find(ctx, msisdn);
            // if (lookup.getState().equals(MsisdnStateEnum.IN_USE))
            // {
            // TODO: "AVAILABLE" is possible ?

            return lookup;
            // }
        }
        catch (final HomeException e)
        {
            lookup = null;
            new MinorLogMsg(Claim.class, "failed to find Msisdn [" + msisdn + "]", e).log(ctx);
            throw e;
        }

        // return lookup;
    }


    /**
     * Releases the package. The package will go into HELD state.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber releasing the package.
     * @param resultMSISDNData
     *            NOT USED - The results?
     * @param lastResult
     *            NOT USED - Previous results?
     * @param el
     *            Exception listener. Any exception(s) thrown in this function will be
     *            added to this listener.
     * @param technology
     *            Technology of the package to be released.
     * @deprecated [Cindy] 2007-10-09: This function is not currently called by any other
     *             functions.
     */
    @Deprecated
    public static void releasePackage(final Context ctx, final Subscriber sub, final ProcessResult resultMSISDNData,
        final ProcessResult lastResult, final HTMLExceptionListener el, final TechnologyEnum technology)
    {
        GenericPackage pack = null;
        try
        {
            pack = PackageSupportHelper.get(ctx).getPackage(ctx, sub.getTechnology(), sub.getPackageId(), sub.getSpid());
        }
        catch (final Exception e)
        {
            new MinorLogMsg(Claim.class, "failed to release Package for sub with MSISDN [" + sub.getMSISDN() + "]", e)
                .log(ctx);
            el.thrown(new HomeException("failed to release Package for sub with MSISDN [" + sub.getMSISDN() + "]"));
        }

        if (pack != null)
        {

            pack.setState(PackageStateEnum.HELD);
            try
            {
                storePackageBasedOnTechnology(ctx, pack, technology);
            }
            catch (final Exception e)
            {
                new MinorLogMsg(Claim.class, "failed to release Package for sub with MSISDN [" + sub.getMSISDN() + "]",
                    e).log(ctx);
                el.thrown(new HomeException("failed to release Package for sub with MSISDN [" + sub.getMSISDN() + "]"));
            }
        }

    }


    /**
     * Looks up a package.
     *
     * @param ctx
     *            The operating context.
     * @param packageId
     *            The ID of the package to look up.
     * @param technology
     *            The technology to look up.
     * @return The package with the specified ID, <code>null</code> if none exists.
     * @throws HomeException
     *             Thrown if there are problems looking up the package.
     * @deprecated Use {@link PackageSupport#getPackage(Context, TechnologyEnum, String)}
     *             instead.
     */
    @Deprecated
    public static GenericPackage lookupPackage(final Context ctx, final Object packageId,final Object spid ,
        final TechnologyEnum technology) throws HomeException
    {
        Home home = null;

        if (TechnologyEnum.GSM == technology)
        {
            home = (Home) ctx.get(GSMPackageHome.class);
        }
        else if (TechnologyEnum.TDMA == technology || TechnologyEnum.CDMA == technology)
        {
            home = (Home) ctx.get(TDMAPackageHome.class);
        }

        try
        {
        	And and = new And();
            and.add(new EQ(TDMAPackageXInfo.SPID, spid));
            and.add(new EQ(TDMAPackageXInfo.PACK_ID,packageId));
        	
            return (GenericPackage) home.find(ctx, and);
            //return (GenericPackage) home.find(ctx, packageId);
        }
        catch (final Exception e)
        {
            new MinorLogMsg(Claim.class, "invalid package " + packageId, e).log(ctx);
            throw new ClaimHomeException("Provisioning Error 3005: Invalid Package " + packageId + " ["
                + e.getMessage() + "]", e);
        }
    }


    /**
     * Validates the package specified in the subscriber is of the correct type and is
     * available.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being validated.
     * @throws HomeException
     *             Thrown if there are problems looking up the package for validation.
     */
    public static void validatePackageTypeAndAvailable(final Context ctx, final Subscriber sub) throws HomeException
    {
        validatePackageType(ctx, sub, sub.getPackageId());
        validatePackageAvailable(ctx, sub);
    }


    /**
     * Determines whether the subscriber owns the provided MSISDN.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being verified.
     * @param md
     *            The MSISDN being verified.
     * @return Returns <code>true</code> if the subscriber owns the provided MSISDN,
     *         <code>false</code> otherwise.
     * @throws HomeException
     *             Thrown if there are problems looking up the subscriber MSISDN.
     * @deprecated [Cindy] 2007-10-09: This function is not currently called by any other
     *             functions.
     */
    @Deprecated
    public static boolean subscriberOwnsMSISDN(final Context ctx, final Subscriber sub, final Msisdn md)
        throws HomeException
    {
        /*
         * [Cindy] 2007-10-09: Rewritten the original query using ELang. The logic,
         * however, still seems flawed: Even if the subscriber has the MSISDN specified,
         * it still doesn't mean the MSISDN belongs to the subscriber -- MSISDN home still
         * needs to be checked.
         */
        final Home home = (Home) ctx.get(SubscriberHome.class);
        final And and = new And();
        and.add(new EQ(SubscriberXInfo.MSISDN, md.getMsisdn()));
        and.add(new EQ(SubscriberXInfo.ID, sub.getId()));
        return home.find(ctx, and) != null;
    }


    /**
     * Determines whether the subscriber owns the provided card package.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being verified.
     * @param packageId
     *            The ID of the package being verified.
     * @return Returns <code>true</code> if the subscriber owns the provided package,
     *         <code>false</code> otherwise.
     * @throws HomeException
     *             Thrown if there are problems looking up the subscriber or package.
     */
    public static boolean subscriberOwnsPackage(final Context ctx, final Subscriber sub, final String packageId)
        throws HomeException
    {
        /*
         * [Cindy] 2007-10-09: Rewritten to use ELang. However, it is still not 100% safe
         * from a data integrity standpoint, since multiple subscribers might have the
         * same package ID.
         */
        final Home home = (Home) ctx.get(SubscriberHome.class);
        final And and = new And();
        and.add(new EQ(SubscriberXInfo.ID, sub.getId()));
        and.add(new EQ(SubscriberXInfo.PACKAGE_ID, packageId));

        return home.find(ctx, and) != null;
    }


    /**
     * Validates that MSISDN is available.
     *
     * @param ctx
     *            The operating context.
     * @param subType
     *            Subscriber type.
     * @param msisdn
     *            MSISDN to be validated.
     * @param msisdnRef
     *            What field of the subscriber the MSISDN is used in (e.g. voice, data,
     *            fax).
     * @param isOptional
     *            Whether the MSISDN is optional.
     * @return The validated MSISDN object.
     * @throws HomeException
     *             Thrown if there are problems looking up the MSISDN.
     */
    public static Msisdn validateMsisdnTypeAndAvailable(final Context ctx, final String ban, final SubscriberTypeEnum subType,
        final String msisdn, final String msisdnRef, final boolean isOptional) throws HomeException
    {
        Msisdn lookup = null;

        if (isOptional)
        {
            if (msisdn == null || msisdn.length() == 0)
            {
                // no validation is necessary
                return lookup;
            }
        }

        lookup = MsisdnSupport.getMsisdn(ctx, msisdn);
        // better check null before use the reference --- DZ
        if (lookup != null && lookup.getSubscriberType() != subType && !GeneralConfigSupport.isAllowedSharedMsisdnAcrossSpids(ctx))
        {
            throw new ClaimHomeException(msisdnRef + " Mobile Number " + msisdn + " has different Subscriber Type: "
                + lookup.getSubscriberType().getDescription());
        }

        if (lookup != null)
        {
            if (MsisdnStateEnum.AVAILABLE == lookup.getState())
            {
                return lookup;
            } else if (ban.equals(lookup.getBAN()) )
            {
                return lookup;
            }
        }

        throw new ClaimHomeException(msisdnRef + " Mobile Number is not available: " + msisdn);
    }


    /**
     * Validates the package is available.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber whose package is being validated.
     * @throws HomeException
     *             Thrown if there are problems looking up the package.
     */
    public static void validatePackageAvailable(final Context context, final Subscriber subscriber)
        throws HomeException
    {
        final GenericPackage pack = PackageSupportHelper.get(context).getPackage(context, subscriber.getTechnology(), subscriber
            .getPackageId(), subscriber.getSpid());

        if (pack == null)
        {
            throw new ClaimHomeException("Provisioning Error 3005: Invalid Package " + subscriber.getPackageId());
        }

        if (!SafetyUtil.safeEquals(pack.getState(), PackageStateEnum.AVAILABLE))
        {
            if (!subscriberOwnsPackage(context, subscriber, pack.getPackId()))
            {
                throw new ClaimHomeException("Provisioning Error 3005: Invalid Package " + subscriber.getPackageId()
                    + ": Package not available");
            }
        }
    }


    /**
     * Validate the Package of the given package id is not in IN_USE state.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The given subscriber.
     * @throws HomeException
     *             Thrown if there are problems looking up the package.
     */
    public static void validatePackageNotInUse(final Context context, final Subscriber sub) throws HomeException
    {
        validatePackageNotInUse(context, sub.getSpid(), sub.getTechnology(), sub.getPackageId());
    }
    
    public static void validatePackageNotInUse(final Context context, final int spid, final TechnologyEnum techType, String packageId) throws HomeException
    {
        validatePackageType(context, spid, techType, packageId);
        final GenericPackage pack = PackageSupportHelper.get(context).getPackage(context, techType, packageId, spid);

        if (pack == null)
        {
            throw new ClaimHomeException("Provisioning Error 3005: Invalid Package " + packageId);
        }
        if (SafetyUtil.safeEquals(pack.getState(), PackageStateEnum.IN_USE))
        {
            throw new ClaimHomeException("Provisioning Error 3005: Invalid Package " + packageId
                + ": Package is currently in use by other subscriber");
        }
    }


    /**
     * Validates the MSISDN has the same type as the subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being validated.
     * @param number
     *            The MSISDN number being validated.
     * @param msisdnRef
     *            What field of the subscriber the MSISDN is used in (e.g. voice, data,
     *            fax).
     * @param optional
     *            Whether this MSISDN is optional.
     * @throws HomeException
     *             Thrown if there are problems looking up the MSISDN.
     */
    public static void validateMsisdnType(final Context ctx, final Subscriber sub, final String number,
        final String msisdnRef, final boolean optional) throws HomeException
    {
        if (optional)
        {
            if (number == null || number.length() == 0)
            {
                return;
            }
        }
        final Msisdn dataMSISDN = MsisdnSupport.getMsisdn(ctx, number);
        if (dataMSISDN == null)
        {
            throw new ClaimHomeException("Mobile Number is not found: " + number);
        }
        else if (!dataMSISDN.getSubscriberType().equals(SubscriberTypeEnum.HYBRID) && !dataMSISDN.getSubscriberType().equals(sub.getSubscriberType()) && !GeneralConfigSupport.isAllowedSharedMsisdnAcrossSpids(ctx))
        {
            throw new ClaimHomeException("Provisioning Error 3004: Invalid Conversion Request for MSISDN " + msisdnRef + " [" + dataMSISDN
                + "] Requesting to change Subscriber Type to [" + sub.getSubscriptionType() + " ] when MSISDN has Subsriber Type [" + dataMSISDN.getSubscriberType().getDescription() + "]");
        }
    }


    /**
     * Validates the card package type.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being validated.
     * @param number
     *            The ID of the package being validated.
     * @throws HomeException
     *             Thrown if there are problems looking up the card package.
     */
    public static void validatePackageType(final Context ctx, final Subscriber sub, final String number)
        throws HomeException
    {
        validatePackageType(ctx, sub.getSpid(), sub.getTechnology(), number);
    }


    private static void validatePackageType(Context ctx, int spid, TechnologyEnum techType, String packageId)
        throws HomeException
    {
        final GenericPackage pack = PackageSupportHelper.get(ctx).getPackage(ctx, techType, packageId, spid);
        if (pack == null)
        {
            throw new ClaimHomeException("Package Number " + packageId + "  is not found for technology: "
                + techType);
        }

        if (pack.getSpid() != spid)
        {
            throw new ClaimHomeException("Provisioning Error 3004: Invalid package [" + pack + "] Subscriber spid ["
                + pack.getSpid() + "]");
        }
    }


    /**
     * Validates the MSISDN is of the right type and it is not currently in use by other
     * subscribers.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being validated.
     * @param number
     *            The MSISDN being validated.
     * @param msisdnRef
     *            What field of the subscriber the MSISDN is used in (e.g. voice, data,
     *            fax).
     * @param optional
     *            Whether the MSISDN is optional.
     * @throws HomeException
     *             Thrown if there are problems looking up the MSISDN.
     */
    public static void validateMsisdnTypeAndNotInUse(final Context ctx, final Subscriber sub, final String number,
        final String msisdnRef, final boolean optional) throws HomeException
    {
        if (optional)
        {
            // this is for empty numbers like fax, data [psperneac]
            if (number == null || number.length() == 0)
            {
                return;
            }
        }
        final Msisdn dataMSISDN = MsisdnSupport.getMsisdn(ctx, number);

        /*
         * MSISDN in state of HELD will be reset to AVAILABLE by cron job after
         * pre-defined period, so here we assume that both AVAIABLE and HELD are valid
         * states for MSISDN.
         */
        if (dataMSISDN != null && dataMSISDN.getSubscriberType() != sub.getSubscriberType() && !GeneralConfigSupport.isAllowedSharedMsisdnAcrossSpids(ctx))
        {
            throw new ClaimHomeException("Provisioning Error 3004: Invalid " + msisdnRef + " [" + dataMSISDN
                + "] Subscriber Type [" + dataMSISDN.getSubscriberType().getDescription() + "]");
        }

        if (dataMSISDN != null && dataMSISDN.getState().equals(MsisdnStateEnum.IN_USE)
            && !SafetyUtil.safeEquals(sub.getId(), dataMSISDN.getSubscriberID()))
        {
            throw new ClaimHomeException("Provisioning Error 3004: Invalid " + msisdnRef + " [" + dataMSISDN
                + "] State [" + dataMSISDN.getState().getDescription() + "]" + " and msisdn does not belong to "
                + sub.getId());
        }
    }


    /**
     * Stores the package.
     *
     * @param ctx
     *            The operating context.
     * @param pkg
     *            The package being stored.
     * @param tech
     *            The technology of the package being stored.
     * @throws HomeException
     *             Thrown if there are problems storing the package.
     */
    private static void storePackageBasedOnTechnology(final Context ctx, final GenericPackage pkg,
        final TechnologyEnum tech) throws HomeException
    {
        if (TechnologyEnum.GSM == tech)
        {
            final Home gsmPkgHome = (Home) ctx.get(GSMPackageHome.class);
            gsmPkgHome.store(ctx, pkg);
        }
        else if (TechnologyEnum.TDMA == tech || TechnologyEnum.CDMA == tech)
        {
            final Home tdmaPkgHome = (Home) ctx.get(TDMAPackageHome.class);
            tdmaPkgHome.store(ctx, pkg);
        }

    }

}
