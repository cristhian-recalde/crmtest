/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bean;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.EntryLogSupport;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.PPMLogMsg;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.numbermgn.PackageProcessingException;
import com.trilogy.app.crm.numbermgn.PackageProcessor;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;


/**
 * Load the Packages from the files provided for Bulk Load
 * 
 * @author simar.singh@redknee.com
 */
public class PackageDealerBulkUpdateTask extends AbstractPackageDealerBulkUpdateTask
        implements ContextAgent, PackageProcessor
{

    public PackageDealerBulkUpdateTask()
    {
        super();
    }
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    /**
     * Name of the Bulk Load File.
     */
    private static final char DELIMITER = ',';
    private static final String ERR_EXT = ".err";
    /**
     * Error Codes.
     */
    private static final int PARSE_ERROR = 1;
    private static final int DB_ERROR = 2;
    private static final int UNKNOWN_ERROR = 3;
    private static final int WRONG_TECHNOLOGY = 4;
    private static final int LICENSE_NOT_AVAILABLE = 5;
    private static final int WRONG_DEALER_CODE = 8;

    @Override
    public void execute(Context ctx) throws AgentException
    {
        String bulkLoadFile = getFileLocation();
        EntryLogSupport log = new EntryLogSupport(ctx, this.getClass().getName());
        PrintWriter out;
        try
        {
            out = new PrintWriter(new BufferedWriter(new FileWriter(bulkLoadFile + ERR_EXT)));
            generateErrorHeader(out);
        }
        catch (IOException ioe)
        {
            // TODO Auto-generated catch block
            log.minor("Could not create Error Log File for [" + bulkLoadFile + "]", ioe);
            out = null;
        }
        try
        {
            final Home importHome = new PackageDealerUpdateImportCSVHome(ctx, bulkLoadFile);
            final LinkedList<PackageDealerUpdateImport> imports = new LinkedList<PackageDealerUpdateImport>(
                    importHome.selectAll(ctx));
            int count = 0;
            final int total = imports.size();
            while (!imports.isEmpty())
            {
                count++;
                final PackageDealerUpdateImport importedPackage = imports.remove();
                final int packagetype = getTechnology().getIndex();
                final LicenseMgr lMgr = (LicenseMgr) ctx.get(LicenseMgr.class);
                boolean licenseNotAvailable = false;
                try
                {
                    switch (packagetype)
                    {
                    // GSM
                    case TechnologyEnum.GSM_INDEX: // Check if Technology specific
                        // license is
                        // enabled
                        if (lMgr != null && !lMgr.isLicensed(ctx, LicenseConstants.GSM_LICENSE_KEY))
                        {
                            licenseNotAvailable = true;
                        }
                        else
                        {
                            GSMPackage card = HomeSupportHelper.get(ctx).findBean(ctx, GSMPackage.class, importedPackage.getPackId());
                            card.setDealer(importedPackage.getDealerCode());
                            processPackage(ctx, card);
                        }
                        break;
                    case TechnologyEnum.CDMA_INDEX:
                    case TechnologyEnum.TDMA_INDEX: // Check if Technology specific
                        // license is
                        // enabled
                        if (lMgr != null && !lMgr.isLicensed(ctx, LicenseConstants.TDMA_CDMA_LICENSE_KEY))
                        {
                            licenseNotAvailable = true;
                        }
                        else
                        {
                            
                            And and = new And();           
                            and.add(new EQ(TDMAPackageXInfo.PACK_ID, importedPackage.getPackId()));
                            and.add(new EQ(TDMAPackageXInfo.SPID, importedPackage.getSpid()));
                    	
                            TDMAPackage card = HomeSupportHelper.get(ctx).findBean(ctx, TDMAPackage.class, and);
                            card.setDealer(importedPackage.getDealerCode());
                            processPackage(ctx, card);
                        }
                        break;
                    case TechnologyEnum.VSAT_PSTN_INDEX: // Check if Technology
                        // specific
                        // license is enabled
                        if (lMgr != null && !lMgr.isLicensed(ctx, LicenseConstants.VSAT_PSTN_LICENSE_KEY))
                        {
                            licenseNotAvailable = true;
                        }
                        else
                        {
                            VSATPackage card = HomeSupportHelper.get(ctx).findBean(ctx, VSATPackage.class, importedPackage.getPackId());
                            card.setDealer(importedPackage.getDealerCode());
                            processPackage(ctx, card);
                        }
                        break;
                    default:
                        writeError(out, importedPackage, WRONG_TECHNOLOGY);
                        log.info(bulkLoadFile + ":" + count + " Invalid Technology", null);
                        break;
                    }
                    if (licenseNotAvailable)
                    {
                        writeError(out, importedPackage, LICENSE_NOT_AVAILABLE);
                        log.info(bulkLoadFile + ":" + count + "LICENSE_NOT_AVAILABLE ", null);
                    }
                }
                catch (IllegalArgumentException iae)
                {
                    writeError(out, importedPackage, PARSE_ERROR);
                    new InfoLogMsg(this, bulkLoadFile + ":" + count, iae);
                }
                catch (HomeException he)
                {
                    writeError(out, importedPackage, DB_ERROR);
                    new InfoLogMsg(this, bulkLoadFile + ":" + count, he);
                }
                catch (PackageProcessingException pe)
                {
                    final Throwable cause = pe.getCause();
                    if (null == cause)
                    {
                        writeError(out, importedPackage, WRONG_DEALER_CODE);
                    }
                    else if (cause instanceof HomeException)
                    {
                        writeError(out, importedPackage, DB_ERROR);
                    }
                    else
                    {
                        writeError(out, importedPackage, UNKNOWN_ERROR);
                    }
                    new InfoLogMsg(this, bulkLoadFile + ":" + count, pe);
                }
                catch (Throwable t)
                {
                    writeError(out, importedPackage, UNKNOWN_ERROR);
                    new InfoLogMsg(this, bulkLoadFile + ":" + count, t);
                }
                reportProgress(ctx, count, total);
            }
        }
        catch (Throwable t)
        {
            log.info("Error when running Package bulk loader for file " + bulkLoadFile, t);
        }
        finally
        {
            closeWriter(out);
        }
    }


    @Override
    public Object processPackage(Context context, GSMPackage card) throws PackageProcessingException
    {
        final String dealerCode = card.getDealer();
        final int spid = card.getSpid();
        try
        {
            if (isDealerCodeExists(context, dealerCode, spid))
            {
                return HomeSupportHelper.get(context).storeBean(context, card);
            }
            else
            {
                throw new PackageProcessingException("Dealer-Code [" + dealerCode
                        + "] is not avaialable for card's SPID [" + spid + "] ");
            }
        }
        catch (HomeException e)
        {
            // TODO Auto-generated catch block
            throw new PackageProcessingException(e.getMessage(), e);
        }
    }


    @Override
    public Object processPackage(Context context, TDMAPackage card) throws PackageProcessingException
    {
        final String dealerCode = card.getDealer();
        final int spid = card.getSpid();
        try
        {
            if (isDealerCodeExists(context, dealerCode, spid))
            {
                return HomeSupportHelper.get(context).storeBean(context, card);
            }
            else
            {
                throw new PackageProcessingException("Dealer-Code [" + dealerCode
                        + "] is not avaialable for card's SPID [" + spid + "] ");
            }
        }
        catch (HomeException e)
        {
            throw new PackageProcessingException(e.getMessage(), e);
        }
    }


    @Override
    public Object processPackage(Context context, VSATPackage card) throws PackageProcessingException
    {
        final String dealerCode = card.getDealer();
        final int spid = card.getSpid();
        try
        {
            if (isDealerCodeExists(context, dealerCode, spid))
            {
                return HomeSupportHelper.get(context).storeBean(context, card);
            }
            else
            {
                throw new PackageProcessingException("Dealer-Code [" + dealerCode
                        + "] is not avaialable for card's SPID [" + spid + "] ");
            }
        }
        catch (HomeException e)
        {
            throw new PackageProcessingException(e.getMessage(), e);
        }
    }


    private void reportProgress(Context ctx, long count, long total)
    {
        PPMLogMsg ppmLogMsg = (PPMLogMsg) ctx.get(PPMLogMsg.class);
        if (null != ppmLogMsg)
        {
            ppmLogMsg.progress(ctx, count, total);
        }
    }


    /**
     * Prints Error Header.
     */
    private void generateErrorHeader(PrintWriter out)
    {
        if (null != out)
        {
            out.println("*************");
            out.println("*  Errors   *");
            out.println("*************");
        }
    }


    private void closeWriter(PrintWriter out)
    {
        if (out != null)
        {
            try
            {
                out.close();
            }
            catch (Exception e)
            {
                // ignore
            }
        }
    }


    private void writeError(PrintWriter out, final PackageDealerUpdateImport importedPackage, int errorCode)
    {
        if (out != null)
        {
            final StringBuffer buffer = PackageDealerUpdateImportCSVSupport.instance().append(new StringBuffer(),
                    DELIMITER, importedPackage);
            buffer.append(DELIMITER);
            buffer.append(errorCode);
            out.println(buffer.toString());
        }
    }


    private boolean isDealerCodeExists(Context ctx, String dealerCode, int spid) throws HomeException
    {
        Home dealerCodeHome = ((Home) ctx.get(DealerCodeHome.class));
        
        And and = new And();
        and.add(new EQ(DealerCodeXInfo.CODE, dealerCode));
        and.add(new EQ(DealerCodeXInfo.SPID, Integer.valueOf(spid)));
        
        DealerCode dealerCodeBean = (DealerCode) dealerCodeHome.find(ctx, and);
        return dealerCodeBean != null;
    }
}
