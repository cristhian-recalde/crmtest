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
import java.util.Collection;
import java.util.LinkedList;

import com.trilogy.app.crm.numbermgn.PackageProcessingException;
import com.trilogy.app.crm.numbermgn.PackageProcessor;
import com.trilogy.app.crm.resource.ResourceDevice;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.BeanNotFoundHomeException;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.EntryLogSupport;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.PPMLogMsg;


/**
 * Load the Packages from the files provided for Bulk Load
 * 
 * @author simar.singh@redknee.com
 */
public class ResourceDealerBulkUpdateTask extends AbstractResourceDealerBulkUpdateTask
        implements ContextAgent, PackageProcessor
{

    public ResourceDealerBulkUpdateTask()
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
    private static final int RECORD_NOT_FOUND = 7;
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
            final LinkedList<ResourceDealerUpdateImport> imports;
            {
                final Home importHome = new ResourceDealerUpdateImportCSVHome(ctx, bulkLoadFile);
                imports = new LinkedList<ResourceDealerUpdateImport>(
                        (Collection<ResourceDealerUpdateImport>) importHome.selectAll(ctx));
            }
            int count = 0;
            final int total = imports.size();
            while (!imports.isEmpty())
            {
                count++;
                final ResourceDealerUpdateImport importedResource = imports.remove();
                try
                {
                    final ResourceDevice resourceDevice = HomeSupportHelper.get(ctx).findBean(ctx, ResourceDevice.class,
                            importedResource.getResourceID());
                    if (null != resourceDevice)
                    {
                        final String dealerCode = importedResource.getDealerCode();
                        final int spid = resourceDevice.getSpid();
                        if (isDealerCodeExists(ctx, dealerCode, resourceDevice.getSpid()))
                        {
                            resourceDevice.setDealerCode(dealerCode);
                            HomeSupportHelper.get(ctx).storeBean(ctx, resourceDevice);
                        }
                        else
                        {
                            writeError(out, importedResource, WRONG_DEALER_CODE);
                            new InfoLogMsg(this, "Dealer-Code [" + dealerCode + "] is not avaialable for card's SPID ["
                                    + spid + "] + :" + bulkLoadFile + ":" + count, null).log(ctx);
                        }
                    }
                    else
                    {
                        writeError(out, importedResource, RECORD_NOT_FOUND);
                        new InfoLogMsg(this, "Could not find resource device bean : " + bulkLoadFile + " : " + count,
                                null).log(ctx);
                        throw new BeanNotFoundHomeException();
                    }
                }
                catch (IllegalArgumentException iae)
                {
                    writeError(out, importedResource, PARSE_ERROR);
                    new InfoLogMsg(this, bulkLoadFile + ":" + count, iae).log(ctx);
                }
                catch (HomeException he)
                {
                    writeError(out, importedResource, DB_ERROR);
                    new InfoLogMsg(this, bulkLoadFile + ":" + count, he).log(ctx);
                }
                catch (Throwable t)
                {
                    writeError(out, importedResource, UNKNOWN_ERROR);
                    new InfoLogMsg(this, bulkLoadFile + ":" + count, t).log(ctx);
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


    private void writeError(PrintWriter out, final ResourceDealerUpdateImport importedPackage, int errorCode)
    {
        if (out != null)
        {
            final StringBuffer buffer = ResourceDealerUpdateImportCSVSupport.instance().append(new StringBuffer(),
                    DELIMITER, importedPackage);
            buffer.append(DELIMITER);
            buffer.append(errorCode);
            out.println(buffer.toString());
        }
    }


    private boolean isDealerCodeExists(Context ctx, String dealerCode, int spid) throws HomeException
    {
        Home dealerCodeHome = (Home) ctx.get(DealerCodeHome.class);
        
        And and = new And();
        and.add(new EQ(DealerCodeXInfo.CODE, dealerCode));
        and.add(new EQ(DealerCodeXInfo.SPID, Integer.valueOf(spid)));
        
        DealerCode dealerCodeBean = (DealerCode) dealerCodeHome.find(ctx, and);
        return dealerCodeBean != null;
    }
}
