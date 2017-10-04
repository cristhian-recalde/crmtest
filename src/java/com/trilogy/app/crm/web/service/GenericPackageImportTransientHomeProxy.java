/*This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.service;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.GenericPackageImport;
import com.trilogy.app.crm.bean.GenericPackageImportTransientHome;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.bean.VSATPackage;
import com.trilogy.app.crm.numbermgn.PackageProcessingException;
import com.trilogy.app.crm.numbermgn.PackageProcessor;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * @author dmishra
 * This class has been made to Implemented Package Processor interface to enforce compile time inclusion of all processable package types.
 *
 */
public class GenericPackageImportTransientHomeProxy extends GenericPackageImportTransientHome implements PackageProcessor
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public GenericPackageImportTransientHomeProxy(Context ctx)
    {
        super(ctx);
    }


    public Object create(Context ctx, Object bean) throws HomeException
    {
        try
        {
            GenericPackageImport importedPackage = (GenericPackageImport) bean;
            String nextPkgIdentifier = null;
            if (importedPackage.getPackId().equals(""))
            {
                int packagetype = importedPackage.getTechnology() != null ? Integer.parseInt(importedPackage
                        .getTechnology()) : -1;
                int spidInt = Integer.parseInt(importedPackage.getSpid());
                if (packagetype == TechnologyEnum.GSM_INDEX)
                {
                    nextPkgIdentifier = String.valueOf(processPackage(ctx, gsmPackage(spidInt)));
                }
                else if (packagetype == TechnologyEnum.TDMA_INDEX || packagetype == TechnologyEnum.CDMA_INDEX)
                {
                    nextPkgIdentifier = String.valueOf(processPackage(ctx, tdmaPackage(spidInt)));
                }
                else if (packagetype == TechnologyEnum.VSAT_PSTN_INDEX)
                {
                    nextPkgIdentifier = String.valueOf(processPackage(ctx, vsatPackage(spidInt)));
                }
                Home spHome = (Home) getContext().get(CRMSpidHome.class);
                CRMSpid spid = (CRMSpid) spHome.find(getContext(), spidInt);
                if (nextPkgIdentifier != null && spid != null)
                {
                    importedPackage.setPackId(spid.getPackagePrefix() + nextPkgIdentifier);
                }
                else
                {
                    new MajorLogMsg(this, "Identifier Sequence not created for Package ID.Entry Failed :  "
                            + importedPackage, null);
                    throw new IllegalArgumentException();
                }
            }
            return super.create(ctx, importedPackage);
        }
        catch (Throwable t)
        {
            if (t instanceof HomeException)
            {
                throw (HomeException) t;
            }
            else
            {
                throw new HomeException(t.getMessage(), t);
            }
        }
    }


    /**
     * Generates Default Package Identifier for the package type
     */
    @Override
    public Object processPackage(Context ctx, GSMPackage gsmPackage) throws PackageProcessingException
    {
        try
        {
            return Long.toString(IdentifierSequenceSupportHelper.get(getContext()).getNextIdentifier(getContext(),
                    IdentifierEnum.PACKAGE_ID_GSM, gsmPackage.getSpid(), null));
        }
        catch (Throwable e)
        {
            throw new PackageProcessingException("Could not generate Default Pacakge Identifier. Error ["
                    + e.getMessage() + "]", e);
        }
    }


    /**
     * Generates Default Package Identifier for the package type
     */
    @Override
    public Object processPackage(Context ctx, TDMAPackage tdmaPackage) throws PackageProcessingException
    {
        try
        {
            return Long.toString(IdentifierSequenceSupportHelper.get(getContext()).getNextIdentifier(getContext(),
                    IdentifierEnum.PACKAGE_ID_TDMA, tdmaPackage.getSpid(), null));
        }
        catch (Throwable e)
        {
            throw new PackageProcessingException("Could not generate Default Pacakge Identifier. Error ["
                    + e.getMessage() + "]", e);
        }
    }


    /**
     * Generates Default Package Identifier for the package type
     */
    @Override
    public Object processPackage(Context ctx, VSATPackage vsatPackage) throws PackageProcessingException
    {
        try
        {
            return Long.toString(IdentifierSequenceSupportHelper.get(getContext()).getNextIdentifier(getContext(),
                    IdentifierEnum.PACKAGE_ID_VSAT, vsatPackage.getSpid(), null));
        }
        catch (Throwable e)
        {
            throw new PackageProcessingException("Could not generate Default Pacakge Identifier. Error ["
                    + e.getMessage() + "]", e);
        }
    }
    
    private GSMPackage gsmPackage(int spid)
    {
        GSMPackage gsmPackage = new GSMPackage();
        gsmPackage.setSpid(spid);
        return gsmPackage;
    }
    
    private TDMAPackage tdmaPackage(int spid)
    {
        TDMAPackage tdmaPackage = new TDMAPackage();
        tdmaPackage.setSpid(spid);
        return tdmaPackage;
    }
    
    private VSATPackage vsatPackage(int spid)
    {
        VSATPackage vsatPackage = new VSATPackage();
        vsatPackage.setSpid(spid);
        return vsatPackage;
    }
}
