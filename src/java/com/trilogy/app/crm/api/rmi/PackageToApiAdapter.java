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
package com.trilogy.app.crm.api.rmi;

import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.bean.VSATPackage;
import com.trilogy.app.crm.numbermgn.GenericPackage;
import com.trilogy.app.crm.numbermgn.PackageProcessingException;
import com.trilogy.app.crm.numbermgn.PackageProcessor;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.crmapi.wsdl.v2_0.types.TechnologyTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.cardpackage.CardPackage;
import com.trilogy.util.crmapi.wsdl.v2_1.types.cardpackage.CardPackageReference;
import com.trilogy.util.crmapi.wsdl.v2_1.types.cardpackage.CardPackageStateEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;

/**
 * Adapts GSMPackage and TDMAPackage objects to API objects.
 *
 * @author victor.stratan@redknee.com
 */
public class PackageToApiAdapter implements Adapter, PackageProcessor
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        final GenericPackage cardPackage = (GenericPackage) obj;
        final Context subCtx = ctx.createSubContext();
        try
        {
            return cardPackage.delegatePackageProcessing(subCtx, this);
        }
        catch (PackageProcessingException e)
        {
            LogSupport.minor(ctx, this, "Adapt Package Exception", e);
        }

        return null;
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see com.redknee.app.crm.numbermgn.PackageProcessor#processPackage(com.redknee.framework.xhome.context.Context, com.redknee.app.crm.bean.VSATPackage)
     */
    @Override
    public Object processPackage(final Context ctx, final GSMPackage card) throws PackageProcessingException
    {
        final CardPackageReference reference = adaptPackageToReference(card);
        return reference;
    }
    
    /*
     * (non-Javadoc)
     * @see com.redknee.app.crm.numbermgn.PackageProcessor#processPackage(com.redknee.framework.xhome.context.Context, com.redknee.app.crm.bean.VSATPackage)
     */
    @Override
    public Object processPackage(final Context ctx, final TDMAPackage card) throws PackageProcessingException
    {
        final CardPackageReference reference = adaptPackageToReference(card);
        return reference;
    }

    /*
     * (non-Javadoc)
     * @see com.redknee.app.crm.numbermgn.PackageProcessor#processPackage(com.redknee.framework.xhome.context.Context, com.redknee.app.crm.bean.VSATPackage)
     */
    @Override
    public Object processPackage(final Context ctx, final VSATPackage card) throws PackageProcessingException
    {
        final CardPackageReference reference = adaptPackageToReference(card);
        return reference;
    }
    
    public static CardPackage adaptPackageToCardPackage(final GSMPackage cardPackage)
    {
        final CardPackage card = new CardPackage();
        adaptPackageToReference(cardPackage, card);
        card.setDealer(cardPackage.getDealer());
        card.setSerialNumber(cardPackage.getSerialNo());
        card.setLastModified(CalendarSupportHelper.get().dateToCalendar(cardPackage.getLastModified()));
        card.setImsi(cardPackage.getIMSI());
//        card.setMin(cardPackage.getMin());
//        card.setEsn(cardPackage.getEsn());

        return card;
    }

    public static CardPackageReference adaptPackageToReference(final GSMPackage cardPackage)
    {
        final CardPackageReference reference = new CardPackageReference();
        adaptPackageToReference(cardPackage, reference);

        return reference;
    }

    public static CardPackageReference adaptPackageToReference(final GSMPackage cardPackage,
            final CardPackageReference reference)
    {
        reference.setIdentifier(cardPackage.getPackId());
        reference.setSpid(cardPackage.getSpid());
        reference.setPackageGroupID(cardPackage.getPackageGroup());
        reference.setTechnology(TechnologyTypeEnum.valueOf(cardPackage.getTechnology().getIndex()));
        reference.setState(CardPackageStateEnum.valueOf(cardPackage.getState().getIndex()));

        return reference;
    }

    public static CardPackage adaptPackageToCardPackage(final TDMAPackage cardPackage)
    {
        final CardPackage card = new CardPackage();
        adaptPackageToReference(cardPackage, card);
        card.setDealer(cardPackage.getDealer());
        card.setSerialNumber(cardPackage.getSerialNo());
        card.setLastModified(CalendarSupportHelper.get().dateToCalendar(cardPackage.getLastModified()));
//        card.setImsi(cardPackage.getImsi());
        card.setMin(cardPackage.getMin());
        card.setEsn(cardPackage.getESN());

        return card;
    }

    public static CardPackageReference adaptPackageToReference(final TDMAPackage cardPackage)
    {
        final CardPackageReference reference = new CardPackageReference();
        adaptPackageToReference(cardPackage, reference);

        return reference;
    }

    public static CardPackageReference adaptPackageToReference(final TDMAPackage cardPackage,
            final CardPackageReference reference)
    {
        reference.setIdentifier(cardPackage.getPackId());
        reference.setSpid(cardPackage.getSpid());
        reference.setPackageGroupID(cardPackage.getPackageGroup());
        reference.setTechnology(TechnologyTypeEnum.valueOf(cardPackage.getTechnology().getIndex()));
        reference.setState(CardPackageStateEnum.valueOf(cardPackage.getState().getIndex()));

        return reference;
    }
    
    public static CardPackageReference adaptPackageToReference(final VSATPackage cardPackage)
    {
        final CardPackageReference reference = new CardPackageReference();
        adaptPackageToReference(cardPackage, reference);
        return reference;
    }

    public static CardPackageReference adaptPackageToReference(final VSATPackage cardPackage,
            final CardPackageReference reference)
    {
        reference.setIdentifier(cardPackage.getPackId());
        reference.setSpid(cardPackage.getSpid());
        reference.setPackageGroupID(cardPackage.getPackageGroup());
        reference.setTechnology(TechnologyTypeEnum.valueOf(cardPackage.getTechnology().getIndex()));
        reference.setState(CardPackageStateEnum.valueOf(cardPackage.getState().getIndex()));
        return reference;
    }

    public static GSMPackage adaptCardPackageToGSMPackage(final Context ctx, final CardPackage cardPackage) throws CRMExceptionFault
    {
        GSMPackage card;
        try
        {
            card = (GSMPackage) XBeans.instantiate(GSMPackage.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(PackageToApiAdapter.class, "Error instantiating new GSM package.  Using default constructor.", e).log(ctx);
            card = new GSMPackage();
        }
        
        card.setPackId(cardPackage.getIdentifier());
        if (cardPackage.getSpid() != null)
        {
            card.setSpid(cardPackage.getSpid());   
        }
        card.setPackageGroup(cardPackage.getPackageGroupID());
        card.setTechnology(RmiApiSupport.convertApiTechnology2Crm(cardPackage.getTechnology()));
        card.setState(RmiApiSupport.convertApiCardPackageState2Crm(cardPackage.getState()));
        card.setDealer(cardPackage.getDealer());
        card.setSerialNo(cardPackage.getSerialNumber());
        card.setLastModified(CalendarSupportHelper.get().calendarToDate(cardPackage.getLastModified()));
        card.setIMSI(cardPackage.getImsi());
        // card.setMin(cardPackage.getMin());
        // card.setEsn(cardPackage.getEsn());

        return card;
    }

    public static TDMAPackage adaptCardPackageToTDMAPackage(final Context ctx, final CardPackage cardPackage) throws CRMExceptionFault
    {
        TDMAPackage card;
        try
        {
            card = (TDMAPackage) XBeans.instantiate(TDMAPackage.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(PackageToApiAdapter.class, "Error instantiating new TDMA package.  Using default constructor.", e).log(ctx);
            card = new TDMAPackage();
        }
        
        card.setPackId(cardPackage.getIdentifier());
        if (cardPackage.getSpid() != null)
        {
            card.setSpid(cardPackage.getSpid());
        }
        card.setPackageGroup(cardPackage.getPackageGroupID());
        card.setTechnology(RmiApiSupport.convertApiTechnology2Crm(cardPackage.getTechnology()));
        card.setState(RmiApiSupport.convertApiCardPackageState2Crm(cardPackage.getState()));
        card.setDealer(cardPackage.getDealer());
        card.setSerialNo(cardPackage.getSerialNumber());
        card.setLastModified(CalendarSupportHelper.get().calendarToDate(cardPackage.getLastModified()));
        // card.setIMSI(cardPackage.getImsi());
        card.setMin(cardPackage.getMin());
        card.setESN(cardPackage.getEsn());

        return card;
    }
    

    public static VSATPackage adaptCardPackageToVSATPackage(final Context ctx, final CardPackage cardPackage) throws CRMExceptionFault
    {
        /*
         * TO DO: Add VSAT Package specific details to CRM-API
         */
        VSATPackage card;
        try
        {
            card = (VSATPackage) XBeans.instantiate(VSATPackage.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(PackageToApiAdapter.class, "Error instantiating new VSAT package.  Using default constructor.", e).log(ctx);
            card = new VSATPackage();
        }
        
        card.setPackId(cardPackage.getIdentifier());
        if (cardPackage.getSpid() != null)
        {
            card.setSpid(cardPackage.getSpid());
        }
        card.setPackageGroup(cardPackage.getPackageGroupID());
        card.setTechnology(RmiApiSupport.convertApiTechnology2Crm(cardPackage.getTechnology()));
        card.setState(RmiApiSupport.convertApiCardPackageState2Crm(cardPackage.getState()));
        card.setDealer(cardPackage.getDealer());
        card.setLastModified(CalendarSupportHelper.get().calendarToDate(cardPackage.getLastModified()));
        return card;
    }

}
