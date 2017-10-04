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
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.bean.VSATPackage;
import com.trilogy.app.crm.client.aaa.AAAClient;
import com.trilogy.app.crm.client.aaa.AAAClientException;
import com.trilogy.app.crm.client.aaa.AAAClientFactory;
import com.trilogy.app.crm.numbermgn.GenericPackage;
import com.trilogy.app.crm.numbermgn.PackageProcessingException;
import com.trilogy.app.crm.numbermgn.PackageProcessor;
import com.trilogy.app.crm.technology.TechnologyEnum;


/**
 * Provides a process to update AAA when the service login information
 * assocuated with CDMA packages is updated.  This decorator should notify the
 * AAA client of a change in card package when the card is CDMA, in-use, and has
 * had an update to any of the service login or service password fields.
 *
 * @author gary.anderson@redknee.com
 */
public
class AAAPackageServiceLoginUpdateHome
    extends HomeProxy
    implements PackageProcessor
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new AAAPackageServiceLoginUpdateHome which delegates to the
     * given Home.
     *
     * @param delegate The Home to which this proxy delegates.
     */
    public AAAPackageServiceLoginUpdateHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    public Object store(final Context context, final Object bean)
        throws HomeException
    {
        final GenericPackage card = (GenericPackage)super.store(context, bean);

        try
        {
            // Double-dispatch to the PackageProcessor methods.
            card.delegatePackageProcessing(context, this);
        }
        catch (final PackageProcessingException exception)
        {
            throw new HomeException("Failure while processing service login update.", exception);
        }

        return card;
    }


    /**
     * {@inheritDoc}
     */
    public Object processPackage(final Context context, final GSMPackage card)
        throws PackageProcessingException
    {
        // GSM AAA is currently unsupported.  This method is needed for
        // PackageProcessor double-dispatch.
        return null;
    }


    /**
     * {@inheritDoc}
     */
    public Object processPackage(final Context context, final TDMAPackage card)
        throws PackageProcessingException
    {
        // Only CDMA is currently supported.  We only need to update AAA for
        // IN_USE cards.
        if (TechnologyEnum.CDMA != card.getTechnology()
            || PackageStateEnum.IN_USE != card.getState())
        {
            return null;
        }

        final TDMAPackage oldCard = OldTDMAPackageLookupHome.getOldTDMAPackage(context);

        if (!hasServiceDataChanged(context, oldCard, card))
        {
            return null;
        }

        final AAAClient client = AAAClientFactory.locateClient(context);

        if (client == null)
        {
            throw new PackageProcessingException("Failed to locate the AAA client.");
        }

        try
        {
            client.updateProfile(context, oldCard, card);
        }
        catch (final AAAClientException exception)
        {
            throw new PackageProcessingException("Failure while using the AAA client.", exception);
        }
        return null;
    }

    
    @Override
    public Object processPackage(Context context, VSATPackage card) throws PackageProcessingException
    {
        // VSAT AAA is currently unsupported.  This method is needed for
        // PackageProcessor double-dispatch.
        return null;
    }


    /**
     * Determines whether or not any of the service login information in the
     * card has been updated.
     *
     * @param context The operating context.
     * @param oldCard The old version of the TDMAPackage.
     * @param newCard The new version of the TDMAPackage.
     *
     * @return True if the service login data has changed; false otherwise.
     */
    boolean hasServiceDataChanged(
        final Context context,
        final TDMAPackage oldCard,
        final TDMAPackage newCard)
    {
        return
            !SafetyUtil.safeEquals(oldCard.getServiceLogin1(), newCard.getServiceLogin1())
            || !SafetyUtil.safeEquals(oldCard.getServicePassword1(), newCard.getServicePassword1())
            || !SafetyUtil.safeEquals(oldCard.getServiceLogin2(), newCard.getServiceLogin2())
            || !SafetyUtil.safeEquals(oldCard.getServicePassword2(), newCard.getServicePassword2())
            || !SafetyUtil.safeEquals(oldCard.getCallbackID(), newCard.getCallbackID())
            || !SafetyUtil.safeEquals(oldCard.getRadiusProfileName(), newCard.getRadiusProfileName()
           );
    }

} // class
