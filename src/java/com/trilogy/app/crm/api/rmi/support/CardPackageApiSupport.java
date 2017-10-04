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
package com.trilogy.app.crm.api.rmi.support;

import java.rmi.RemoteException;

import com.trilogy.app.crm.api.ApiSupport;
import com.trilogy.app.crm.api.rmi.PackageToApiAdapter;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.bean.DealerCode;
import com.trilogy.app.crm.bean.DealerCodeXInfo;
import com.trilogy.app.crm.bean.GSMPackageHome;
import com.trilogy.app.crm.bean.Package;
import com.trilogy.app.crm.bean.TDMAPackageHome;
import com.trilogy.app.crm.numbermgn.GenericPackage;
import com.trilogy.app.crm.numbermgn.PackageHomeRetriever;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.util.crmapi.wsdl.v2_0.types.TechnologyType;
import com.trilogy.util.crmapi.wsdl.v2_0.types.TechnologyTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.cardpackage.CardPackage;
import com.trilogy.util.crmapi.wsdl.v2_1.types.cardpackage.CardPackageStateEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;





/**
 * Support methods for CardPackage objects in API implementation.
 *
 * @author Marcio Marques
 * @since 9.3
 */
public final class CardPackageApiSupport
{

    /**
     * Creates a new <code>SubscribersApiSupport</code> instance. This method is made
     * private to prevent instantiation of utility class.
     */
    private CardPackageApiSupport()
    {
        // empty
    }

    /**
     * Does not return null. If create failed throwing an exception.
     * 
     * @param ctx
     *            the operating context
     * @param card
     *            card package to create
     * @param caller
     *            object that requested the call
     * @return created CRM card package object
     * @throws RemoteException
     *             if exception in CRM or create failed
     */
    public static GenericPackage createCrmCardPackage(final Context ctx, final CardPackage card, final Object caller)
            throws com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault
    {
        GenericPackage result = null;
        Package cardPackage = null;
        try
        {
            Home home = null;
            RmiApiSupport.validateExistanceOfBeanForKey(ctx, CRMSpid.class, new EQ(CRMSpidXInfo.ID, card.getSpid()));
            RmiApiSupport.validateExistanceOfBeanForKey(ctx, DealerCode.class,
                    new EQ(DealerCodeXInfo.CODE, card.getDealer()));
            final TechnologyType technology = card.getTechnology();
            final long technologyValue;
            if (technology == null)
            {
                technologyValue = -1;
            }
            else
            {
                technologyValue = technology.getValue();
            }
            if (technologyValue == TechnologyTypeEnum.GSM.getValue().getValue())
            {
                home = RmiApiSupport.getCrmHome(ctx, GSMPackageHome.class, caller);
                cardPackage = PackageToApiAdapter.adaptCardPackageToGSMPackage(ctx, card);
            }
            else if (technologyValue == TechnologyTypeEnum.TDMA.getValue().getValue()
                    || technologyValue == TechnologyTypeEnum.CDMA.getValue().getValue())
            {
                home = RmiApiSupport.getCrmHome(ctx, TDMAPackageHome.class, caller);
                cardPackage = PackageToApiAdapter.adaptCardPackageToTDMAPackage(ctx, card);
            }
            else
            {
                RmiApiErrorHandlingSupport.simpleValidation("Unsuported Technology type",
                        "Only GSM, TDMA and CDMA Technologies are supported");
            }
            
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            result = (GenericPackage) home.create(ctx, cardPackage);
            if (result == null)
            {
                final String msg = "Failed to create Card Package " + card.getIdentifier();
                RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, null, msg, false, cardPackage.getClass(), card.getIdentifier(), caller);
            }
        }
        catch (final com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            final String msg = "Unable to create Card Package " + card.getIdentifier();
            Class<? extends Package> pkgType = null;
            if (cardPackage != null)
            {
                pkgType = cardPackage.getClass();
            }
            RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, e, msg, false, pkgType, card.getIdentifier(), caller);
        }
        return result;
    }

    /**
     * Removes a card package.
     * 
     * @param ctx
     *            The operating context.
     * @param card
     *            The card package to be removed.
     * @param caller
     *            Caller of this function; used for logging purposes.
     * @throws CRMExceptionFault
     *             Thrown if there are problems removing the card package.
     */
    public static void removeCrmCardPackage(final Context ctx, final GenericPackage card, final Object caller)
            throws com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault
    {
        try
        {
            final Home home = (Home) card.delegatePackageProcessing(ctx, PackageHomeRetriever.getInstance());
            home.remove(ctx, card);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to remove Card Package " + card.getPackId();
            RmiApiErrorHandlingSupport.handleDeleteExceptions(ctx, e, msg, caller);
        }
    }

    /**
     * Fill in the default value of a new prepaid card package.
     * 
     * @param cardPackage
     *            Card package.
     * @param spid
     *            Service provider identifier.
     * @param technology
     *            Technology of the package.
     */
    public static void prepareCardPackage(final CardPackage cardPackage, final int spid,
            final TechnologyType technology)
    {
        cardPackage.setSpid(spid);
        cardPackage.setState(CardPackageStateEnum.AVAILABLE.getValue());
        cardPackage.setTechnology(technology);
    }


}