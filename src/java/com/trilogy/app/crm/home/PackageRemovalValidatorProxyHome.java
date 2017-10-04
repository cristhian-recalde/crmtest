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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.VSATPackage;
import com.trilogy.app.crm.numbermgn.GenericPackage;
import com.trilogy.app.crm.numbermgn.PackageProcessingException;
import com.trilogy.app.crm.numbermgn.PackageProcessor;


/**
 * Verifies if the SIM Package can be removed.
 *
 * @author victor.stratan@redknee.com
 */
public class PackageRemovalValidatorProxyHome
    extends HomeProxy
    implements PackageProcessor
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new PackageRemovalValidatorProxyHome.
     *
     * @param delegate The Home to which we delegate.
     */
    public PackageRemovalValidatorProxyHome(
		final Home delegate)
        throws HomeException
    {
        super(delegate);
    }

    //INHERIT
    public void remove(Context ctx, Object obj)
        throws HomeException,  HomeInternalException
    {
        final GenericPackage card = (GenericPackage)obj;

        // Double-dispatch back to this PackageProcessor.
        try
        {
            card.delegatePackageProcessing(ctx, this);
        }
        catch (final PackageProcessingException exception)
        {
            throw new HomeException(exception);
        }
    }


    /**
     * {@inheritDoc}
     */
    public Object processPackage(final Context context, final GSMPackage card)
        throws PackageProcessingException
    {
        if (card.getState().equals(PackageStateEnum.IN_USE))
        {
            throw new PackageProcessingException("Cannot delete " + debugPackage(card) + " while it is IN USE.");
        }

        try
        {
            getDelegate(context).remove(context, card);
        }
        catch (final HomeException exception)
        {
            throw new PackageProcessingException(exception);
        }
        return null;
    }

    
    /**
     * {@inheritDoc}
     */
    public Object processPackage(final Context context, final TDMAPackage card)
        throws PackageProcessingException
    {
        if (card.getState().equals(PackageStateEnum.IN_USE))
        {
            throw new PackageProcessingException("Cannot delete " + debugPackage(card) + " while it is IN USE.");
        }

        try
        {
            getDelegate(context).remove(context, card);
        }
        catch (final HomeException exception)
        {
            throw new PackageProcessingException(exception);
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object processPackage(Context context, VSATPackage card) throws PackageProcessingException
    {

        if (card.getState().equals(PackageStateEnum.IN_USE))
        {
            throw new PackageProcessingException("Cannot delete " + debugPackage(card) + " while it is IN USE.");
        }

        try
        {
            getDelegate(context).remove(context, card);
        }
        catch (final HomeException exception)
        {
            throw new PackageProcessingException(exception);
        }
        return null;
    
    }

    /**
     * Provides a simple method of creating a debug message for a GSMPackage.
     *
     * @param card A GSMPackage.
     * @return A debug message.
     */
    private String debugPackage(final GSMPackage card)
    {
        return "GSMPackage[ID=" + card.getPackId()
            + ", IMSI=" + card.getIMSI()
            + ", SerialNumber=" + card.getSerialNo() + "]";
    }
    

    /**
     * Provides a simple method of creating a debug message for a TDMAPackage.
     *
     * @param card A TDMAPackage.
     * @return A debug message.
     */
    private String debugPackage(final TDMAPackage card)
    {
        return "TDMAPackage[ID=" + card.getPackId()
            + ", MIN=" + card.getMin()
            + ", ESN=" + card.getESN() + "]";
    }
    
    /**
     * Provides a simple method of creating a debug message for a TDMAPackage.
     *
     * @param card A TDMAPackage.
     * @return A debug message.
     */
    private String debugPackage(final VSATPackage card)
    {
        return "VSATPackage[ID=" + card.getPackId()
            + ", VSAT_ID=" + card.getVsatId()
            + ", PORT=" + card.getPort() + "]"
            + ", CHANNEL=" + card.getChannel() + "]";
    }
    
} // class
