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

import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.app.crm.home.sub.SubscriberNoteSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Provides utilities for Card package . Introduced during Dual-mode CDMA/LTE devices feature
 * 
 * @author bdhavalshankh
 */
public final class SwapPackageSupport
{

    private static final String MODULE = SwapPackageSupport.class.getName();
    private static String oldSecondaryPackIdForNotes = "[]";
    
    /**
     * This method will validate the package swap for both - primary as well as secondary packages.
     * @param ctx
     * @param primaryPackage Primary package to validate
     * @param secondaryPackage - Secondary package to validate. Value can be null.
     * @param sub - Subscriber for which the package swap is being performed
     * @throws CompoundIllegalStateException
     * 
     */
    public static void validatePackagesForSwap(Context ctx, TDMAPackage existingPrimaryPackage, TDMAPackage primaryPackage, TDMAPackage secondaryPackage, Subscriber sub, String secPackId) 
            throws CompoundIllegalStateException
    {
        String existingSerialNumber = null;
        if(existingPrimaryPackage != null && existingPrimaryPackage.getSerialNo() != null)
        {
            existingSerialNumber = existingPrimaryPackage.getSerialNo();
        }
        
        CompoundIllegalStateException exceptions = new CompoundIllegalStateException();
        if(primaryPackage == null)
        {
            exceptions.thrown(new IllegalStateException("Primary package Not found"));
            exceptions.throwAll();
            
        }
        
        if(primaryPackage != null)
        {
            String esn = primaryPackage.getESN();
            if(esn == null || esn.trim().equals(""))
            {
                exceptions.thrown(new IllegalStateException("ESN for Primary package can not be empty"));
                exceptions.throwAll();
            }
        }
        
        if(secPackId != null && !secPackId.trim().equals(""))
        {
            if(secondaryPackage == null)
            {
                exceptions.thrown(new IllegalStateException("Secondary package with pack id : " +secPackId
                        +" does not exist in system "));
                exceptions.throwAll();
            }
        }
        
            if(existingSerialNumber != null && secPackId != null && 
                    !existingSerialNumber.equals(secPackId))//This means secondary pack is being swapped
            {
                if(secondaryPackage.getState().getIndex() !=  PackageStateEnum.AVAILABLE_INDEX)
                {
                    exceptions.thrown(new IllegalStateException("Secondary package with pack id : " +secPackId
                            +" is not in AVAILABLE state."));
                    exceptions.throwAll();
                }
            }
       
    }
    
    
    /**
     * This method will merge the packages supplied as parameters.
     * By merging, we mean- if secondary package is supplied and all the validations are success,
     * then copy the packId field of secondaryPackage to serial number field of Primary package
     * @param ctx Context
     * @param sub Subscriber which is being created
     * @param newPrimaryPackage - New Primary Package 
     * @param newSecondaryPackage - New Secondary Package . This can be null.
     * @param existingPrimaryPackage - Existing PrimaryPackage attached to the subscriber
     * @param msid - MSID passed by external application
     * @throws CompoundIllegalStateException
     * @throws HomeException
     */
    public static void mergeTDMAPackagesOnCreateSubscription(Context ctx, Subscriber sub, TDMAPackage newPrimaryPackage, 
            TDMAPackage newSecondaryPackage, TDMAPackage existingPrimaryPackage, String msid, String secPackId) 
                    throws CompoundIllegalStateException, HomeException
    {
        //Validate packages first
        try
        {
            validatePackagesForSwap(ctx, existingPrimaryPackage, newPrimaryPackage, newSecondaryPackage, sub, secPackId);
        }
        catch (CompoundIllegalStateException e) {
            throw e;
        }
        
        if(existingPrimaryPackage == null && newSecondaryPackage != null)
        {
            try
            {
                if(msid != null && !msid.trim().equals(""))
                {
                    newPrimaryPackage.setExternalMSID(msid);
                }
                updateIMSIinCPS(ctx, sub, newSecondaryPackage);
                attachPackage(ctx, sub, newPrimaryPackage, newSecondaryPackage, true);
            }
            catch (HomeException e)
            {
                LogSupport.major(ctx, MODULE, "INTERNAL ERROR:"+ e.getMessage());
                throw new CompoundIllegalStateException("Error occured for attaching secondary package" +
                        "with ID "+newSecondaryPackage.getPackId()+" and primary package with ID: "+newPrimaryPackage.getPackId()+" error" +
                                "message : "+e.getMessage());
            }
            
        }
        
    }
    
    /**
     * 
     * @param ctx Context
     * @param sub - Subscriber being updated
     * @param newPrimaryPackage - New Primary Package 
     * @param newSecondaryPackage - New Secondary Package . This can be null.
     * @param existingPrimaryPackage - Existing PrimaryPackage attached to the subscriber
     * @throws CompoundIllegalStateException 
     * @throws HomeException
     */
    
    public static void mergeTDMAPackages(Context ctx, Subscriber sub, TDMAPackage newPrimaryPackage, 
            TDMAPackage newSecondaryPackage, TDMAPackage existingPrimaryPackage, String secondaryPackId) throws CompoundIllegalStateException
    {
        //Validate packages first
        validatePackagesForSwap(ctx, existingPrimaryPackage, newPrimaryPackage, newSecondaryPackage, sub, secondaryPackId);
        
        String existingSerialNo = existingPrimaryPackage.getSerialNo();
        String existingPrimaryPackId = existingPrimaryPackage.getPackId();
        String newSecondaryPackId = newSecondaryPackage.getPackId();
        String newPrimaryPackId = newPrimaryPackage.getPackId();
        
        
        
        //For updateSubscriptionCardPackage
        if(existingPrimaryPackage != null)
        {
            String oldSecondaryPack = newPrimaryPackage.getSerialNo();
            if(newPrimaryPackage.ID().equals(existingPrimaryPackage.ID())) //Primary package is same
            {
                if(!oldSecondaryPack.equals(newSecondaryPackId))//This means secondary pack is being swapped
                {
                    TDMAPackage oldSecPack = null;
                    try
                    {
                        if(oldSecondaryPack != null && !oldSecondaryPack.trim().equals(""))
                        {
                            oldSecPack = DefaultPackageSupport.instance().getTDMAPackage
                                (ctx, oldSecondaryPack, sub.getSpid());
                        }
                    }
                    catch (HomeException e1)
                    {   
                        throw new CompoundIllegalStateException("Error occured to get old secondary pack " +
                                "with id : "+newPrimaryPackage.getSerialNo());
                    }
                   
                    if(oldSecPack != null)
                    {
                        oldSecPack.setState(PackageStateEnum.HELD);
                        try
                        {
                            HomeSupportHelper.get(ctx).storeBean(ctx,oldSecPack);
                        }
                        catch (HomeException e)
                        {
                            throw new CompoundIllegalStateException("Error occured for updating old Secondary package" +
                                    " with packId :"+oldSecPack.getPackId()+" Error: "+e.getMessage());
                        }
                    }
                    try
                    {
                    	updateIMSIinCPS(ctx, sub, newSecondaryPackage);
                    	attachPackage(ctx, sub, newPrimaryPackage, newSecondaryPackage, true);
                    }
                    catch (HomeException e)
                    {
                        throw new CompoundIllegalStateException("Error occured for attaching secondary package" +
                                "with ID "+newSecondaryPackId+" and primary package with ID: "+newPrimaryPackId+" error" +
                                        "message : "+e.getMessage());
                    }
                }
            }
            
            else if(!newPrimaryPackage.ID().equals(existingPrimaryPackage.ID()))//Primary package is being swapped
            {
                    if(existingSerialNo.equals(newSecondaryPackId))//This means secondary pack is same
                    {
                        try
                        {
                            detachPackage(ctx, existingPrimaryPackage);
                        }
                        catch (HomeException e)
                        {
                            throw new CompoundIllegalStateException("Error occured while detaching Primary package " +
                                    " with packId "+existingPrimaryPackId+" Error :"+e.getMessage());
                        }
                        try
                        {
                        	updateIMSIinCPS(ctx, sub, newSecondaryPackage); //TODO - confirm if this is required.
                        	attachPackage(ctx, sub, newPrimaryPackage, newSecondaryPackage, false);
                        }
                        catch (HomeException e)
                        {
                            throw new CompoundIllegalStateException("Error occured for attaching secondary package" +
                                    "with ID "+newSecondaryPackId+" and primary package with ID: "
                                    +newPrimaryPackId+" error message : "+e.getMessage());
                        }
                    }
                    else if(!existingSerialNo.equals(newSecondaryPackId))//This means secondary pack is also being swapped
                    {
                        oldSecondaryPackIdForNotes = existingSerialNo;
                        try
                        {
                            detachPackage(ctx, existingPrimaryPackage);
                        }
                        catch (HomeException e)
                        {
                            throw new CompoundIllegalStateException("Error occured while detaching Primary package " +
                                    " with packId "+existingPrimaryPackId+" Error :"+e.getMessage());
                        }
                        try
                        {
                        	updateIMSIinCPS(ctx, sub, newSecondaryPackage);
                        	attachPackage(ctx, sub, newPrimaryPackage, newSecondaryPackage, true);
                        }
                        catch (HomeException e)
                        {
                            throw new CompoundIllegalStateException("Error occured for attaching secondary package" +
                                    "with ID "+newSecondaryPackId+" and primary package with ID: "+newPrimaryPackId+" error" +
                                            "message : "+e.getMessage());
                        }
                    }
            }
        }
        
        LogSupport.info(ctx, MODULE, "Packages merged successfully");
    }
    
    public static void detachPackage(Context ctx, TDMAPackage primaryPackage) throws HomeException
    {
        primaryPackage.setState(PackageStateEnum.HELD);
        HomeSupportHelper.get(ctx).storeBean(ctx,primaryPackage);
    }
    
    public static void attachPackage(Context ctx, Subscriber sub, TDMAPackage primaryPackage,
            TDMAPackage secondaryPackage, boolean updateSecPack) throws  HomeException
    {
        final StringBuilder noteBuff = new StringBuilder();
        
        String serialNo = secondaryPackage.getPackId();
        String oldSerialNo = oldSecondaryPackIdForNotes;
        
        primaryPackage.setSerialNo(serialNo);
        
        HomeSupportHelper.get(ctx).storeBean(ctx, primaryPackage);
        
        LogSupport.info(ctx, MODULE, "Primary Package with Id:"+primaryPackage.getPackId()+"updated successfully with serial number : "+serialNo);
        
        if(updateSecPack)
        {
            secondaryPackage.setState(PackageStateEnum.IN_USE);
            HomeSupportHelper.get(ctx).storeBean(ctx,secondaryPackage);
            LogSupport.info(ctx, MODULE, "State of Secondary Package with Id:"+secondaryPackage.getPackId()+"updated successfully to IN_USE");
            

            noteBuff.append("Subscriber updating succeeded\n");
            noteBuff.append("Subscriber UICC identifier : old value"+oldSerialNo+"->"+primaryPackage.getSerialNo());
            SubscriberNoteSupport.createSubscriberNote(ctx, SwapPackageSupport.class.getName(), SubscriberNoteSupport.getCsrAgent(ctx, null),
                    sub.getId(), SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.SUBUPDATE, noteBuff);
        }
        
    }
    
    /**
     * For "LTE Removable" package merge...
     * (1) 2 Packages are created.
     * (2) Package with ESN is considered as a primary package. It has got a dummy IMSI, as IMSI is mandatory in BSS.
     * (3) Package with SERIAL# is considered as a secondary package. It has got a real IMSI, to be used as an identifier.
     * (4) Real IMSI needs to be set in CPS. (This was not happening, refer http://jira01.bln1.bf.nsn-intra.net/jira/browse/USACLD-432).
     * 
     * This method takes care of setting correct IMSI in CPS.
     * 
     * On successful execution..
     * Real IMSI will be updated in CPS.
     * BSS will continue to have dummy IMSI, to keep consistency in IMSIHistMgmt.
     * BSS has a logic to retrieve real IMIS from MSISDN, thus BSS will remain unaffected with IMSI value.
     * 
     * @param ctx
     * @param sub
     * @param newSecondaryPackage
     * @throws HomeException 
     * 
     * @author kashyap.deshpande
     * @since 9_9_tcb FixForBugId: USACLD-432
     */
    private static void updateIMSIinCPS(final Context ctx, final Subscriber sub, TDMAPackage newSecondaryPackage) throws HomeException
    {
    	if (sub == null)
    	{
    		LogSupport.minor(ctx, MODULE, "Subscriber not found, updateIMSIinCPS cannot be executed.");
    		return;
    	}
    	if (newSecondaryPackage == null)
    	{
    		LogSupport.minor(ctx, MODULE, "newSecondaryPackage not found, updateIMSIinCPS cannot be executed.");
    		return;
    	}

    	final String dummyIMSI = sub.getIMSI();
    	final String realIMSI = newSecondaryPackage.getMin();
    	if (LogSupport.isDebugEnabled(ctx))
    	{
    		LogSupport.debug(ctx, MODULE, "SubscriberIMSI is [" + dummyIMSI + "] and SecondaryPackageIMSI is [" + realIMSI + "]");
    	}
    	
    	try
    	{
    		// updateIMSI() method below doesn't use 3rd (IMSI) parameter, instead it uses IMSI value on subscriber.
    		// Hence setting required IMSI on subscriber.
        	sub.setIMSI(realIMSI);
    		
    		final SubscriberProfileProvisionClient client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
    		client.updateIMSI(ctx, sub, null);
    	}
    	catch (final HomeException e)
    	{
    		LogSupport.major(ctx, MODULE, "HomeException occurred while performing "
    				+ "IMSI [" + realIMSI +"] update on CPS for subscriber [" + sub + "]", e);
    		throw e;
    	}
    	catch (final SubscriberProfileProvisionException e)
    	{
    		LogSupport.major(ctx, MODULE, "SubscriberProfileProvisionException occurred while performing "
    				+ "IMSI [" + realIMSI +"] update on CPS for subscriber [" + sub + "]", e);
    		throw new HomeException(e);
    		
    	}
    	
		LogSupport.info(ctx, MODULE, "IMSI [" + realIMSI + "] successfully updated in CPS, for subscriber [" + sub + "]");
    }
}
