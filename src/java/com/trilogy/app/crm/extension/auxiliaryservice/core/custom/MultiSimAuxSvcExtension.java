package com.trilogy.app.crm.extension.auxiliaryservice.core.custom;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.extension.ExtendedAssociableExtension;
import com.trilogy.app.crm.extension.ExtensionAssociationException;
import com.trilogy.app.crm.extension.subscriber.MultiSimSubExtension;
import com.trilogy.app.crm.extension.subscriber.MultiSimSubExtensionAuxSvcFlatteningHome;
import com.trilogy.app.crm.extension.subscriber.MultiSimSubExtensionXInfo;
import com.trilogy.app.crm.home.sub.Claim;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.log.MultiSimProvisioningTypeEnum;
import com.trilogy.app.crm.numbermgn.AppendNumberMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.NumberMgmtHistory;
import com.trilogy.app.crm.numbermgn.NumberMgnSupport;
import com.trilogy.app.crm.numbermgn.PackageMgmtHistoryHome;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.PackageSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NullHome;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class MultiSimAuxSvcExtension extends
com.redknee.app.crm.extension.auxiliaryservice.core.MultiSimAuxSvcExtension implements
ExtendedAssociableExtension<SubscriberAuxiliaryService>
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void associate(Context ctx, SubscriberAuxiliaryService association) throws ExtensionAssociationException
    {
        if (association.getSecondaryIdentifier() != SubscriberAuxiliaryService.DEFAULT_SECONDARYIDENTIFIER)
        {
            Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
            String packageId = association.getMultiSimPackage();
            try{
				
				validateAndUpdatePackage(ctx,association,sub);
				association.setProvisionActionState(true);
			}catch(ExtensionAssociationException e)
			{
				association.setProvisionActionState(false);
				throw e;
			} 
         }
    }

    @Override
    public void updateAssociation(Context ctx, SubscriberAuxiliaryService association)
            throws ExtensionAssociationException
    {
    	if (association.getSecondaryIdentifier() != SubscriberAuxiliaryService.DEFAULT_SECONDARYIDENTIFIER)
        {
    		SubscriberAuxiliaryService oldAssociation = (SubscriberAuxiliaryService) ctx.get(Lookup.OLD_SUBSCRIBER_AUXILIARY_SERVICE);
    		final boolean isImsiSwapped = !SafetyUtil.safeEquals(association.getMultiSimImsi(), oldAssociation.getMultiSimImsi());
    		Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
    		if(isImsiSwapped){
    			try{
    				String packageId = association.getMultiSimPackage();
    				validateAndUpdatePackage(ctx,association,sub);
    				association.setProvisionActionState(true);
    			}catch(ExtensionAssociationException e)
    			{
    				association.setProvisionActionState(false);
    				throw e;
    			}
    		}
        }
    }
    
    
    @Override
    public void dissociate(Context ctx, SubscriberAuxiliaryService association) throws ExtensionAssociationException
    {
        if (association.getSecondaryIdentifier() == SubscriberAuxiliaryService.DEFAULT_SECONDARYIDENTIFIER)
        {
            And filter = new And();
            filter.add(new EQ(MultiSimSubExtensionXInfo.SUB_ID, association.getSubscriberIdentifier()));
            filter.add(new EQ(MultiSimSubExtensionXInfo.AUX_SVC_ID, association.getAuxiliaryServiceIdentifier()));

            subscriberExtensions_ = ExtensionSupportHelper.get(ctx).getExtensions(ctx, MultiSimSubExtension.class, filter);

            if (subscriberExtensions_ != null 
                    && !ctx.getBoolean(MultiSimSubExtensionAuxSvcFlatteningHome.EXTENSION_TRIGGERED_REMOVE, Boolean.FALSE))
            {
                for (MultiSimSubExtension extension : subscriberExtensions_)
                {
                    // Remove the extension first so that all of its SIMs can be removed.
                    Context sCtx = ctx.createSubContext();
                    sCtx.put(SERVICE_TRIGGERED_REMOVE, Boolean.TRUE);

                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "Attempting to remove Multi-SIM extension for auxiliary service [" + association.getAuxiliaryServiceIdentifier()
                                + "] and subscription [" + association.getSubscriberIdentifier() + "]...", null).log(ctx);
                    }
                    try
                    {
                        HomeSupportHelper.get(sCtx).removeBean(sCtx, extension);
                        association.setProvisionActionState(true);
                        ERLogger.createMultiSimProvisioningEr(ctx, extension, MultiSimProvisioningTypeEnum.UNPROVISIONED);
                    }
                    catch (HomeException e)
                    {
                        association.setProvisionActionState(true);
                        throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to remove Multi-SIM extension: " + e.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_EXTENSION_REMOVAL, e, false);
                    }
                }
            }
        }
        else
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Marking package " + association.getMultiSimPackage() + " as held for removal of subscription " + association.getSubscriberIdentifier() + "'s additional SIM association...", null).log(ctx);
            }
            
            Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
            try
            {
                PackageSupportHelper.get(ctx).setPackageState(ctx, association.getMultiSimPackage(), sub.getTechnology(), PackageStateEnum.HELD_INDEX, sub.getSpid());
                association.setProvisionActionState(true);
            }
            catch (HomeException e)
            {
                association.setProvisionActionState(true);
                throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to update package '" + association.getMultiSimPackage() + "' to HELD: " + e.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_PACKAGE_UPDATE_HELD, e, false);
            }

        }
    }

    @Override
    public void postExternalBeanCreation(Context ctx, SubscriberAuxiliaryService association, boolean success)
            throws ExtensionAssociationException
    {
        if (success)
        {
            if (association.getSecondaryIdentifier() == SubscriberAuxiliaryService.DEFAULT_SECONDARYIDENTIFIER
                    && AuxiliaryServiceTypeEnum.MultiSIM.equals(association.getType(ctx)))
            {
                AuxiliaryService auxSvc = this.getAuxiliaryService(ctx);

                // No extension technically exists yet.  Make a dummy one for ER logging.
                MultiSimSubExtension extension = null;
                try
                {
                    extension = (MultiSimSubExtension) XBeans.instantiate(MultiSimSubExtension.class, ctx);
                }
                catch (Exception e)
                {
                    extension = new MultiSimSubExtension();
                }
                
                boolean isChargePerSim = isChargePerSim();
                long maxNumSims = getMaxNumSIMs();

                extension.setAuxSvcId(association.getAuxiliaryServiceIdentifier());
                extension.setSubId(association.getSubscriberIdentifier());
                extension.setAuxSvcName(auxSvc.getName());
                extension.setChargePerSim(isChargePerSim);
                extension.setCharge(auxSvc.getCharge());
                extension.setMaxNumSims(maxNumSims);
                extension.setSpid(auxSvc.getSpid());
                
                // Set the SIM list to non-null.  We know there are no SIMs yet so skip the lazy-load.
                extension.setSims(new ArrayList());

                ERLogger.createMultiSimProvisioningEr(ctx, extension, MultiSimProvisioningTypeEnum.PROVISIONED);
            }
        }
        
    }

    @Override
    public void postExternalBeanUpdate(Context ctx, SubscriberAuxiliaryService association, boolean success)
            throws ExtensionAssociationException
    {
        if(success)
        {
        	if (association.getSecondaryIdentifier() != SubscriberAuxiliaryService.DEFAULT_SECONDARYIDENTIFIER)
            {
        		SubscriberAuxiliaryService oldAssociation = (SubscriberAuxiliaryService) ctx.get(Lookup.OLD_SUBSCRIBER_AUXILIARY_SERVICE);
        		final boolean isImsiSwapped = !SafetyUtil.safeEquals(association.getMultiSimImsi(), oldAssociation.getMultiSimImsi());
        		Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
        		if(isImsiSwapped){
        			
                    try
                    {
                    	PackageSupportHelper.get(ctx).setPackageState(ctx, oldAssociation.getMultiSimPackage(), sub.getTechnology(), PackageStateEnum.HELD_INDEX, sub.getSpid());
                    	addSimDetachedHistory(ctx,sub.getId(),oldAssociation.getMultiSimPackage());
                    }
                    catch (HomeException e)
                    {
                        new MinorLogMsg(this, "Unable to change state of old Package to HELD "+oldAssociation.getMultiSimPackage()+ " during Slave sim swap",e).log(ctx);
                    }
                    
                    
        		}
            }
        }
    }

    @Override
    public void postExternalBeanRemoval(Context ctx, SubscriberAuxiliaryService association, boolean success)
            throws ExtensionAssociationException
    {
        if (success && subscriberExtensions_ != null)
        {
            for (MultiSimSubExtension extension : subscriberExtensions_)
            {
                ERLogger.createMultiSimProvisioningEr(ctx, extension, MultiSimProvisioningTypeEnum.UNPROVISIONED);
            }
        }
        if(success && association.getSecondaryIdentifier() != SubscriberAuxiliaryService.DEFAULT_SECONDARYIDENTIFIER)
        {
        	Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
        	addSimDetachedHistory(ctx,sub.getId(),association.getMultiSimPackage());
        }
    }
    
    private void validateAndUpdatePackage(Context ctx,SubscriberAuxiliaryService association,Subscriber sub) throws ExtensionAssociationException
    {
    	boolean updatePackage = true;
    	String packageId = association.getMultiSimPackage();
    	try
        {
            Claim.validatePackageNotInUse(ctx, sub.getSpid(), sub.getTechnology(), packageId);
        }
        catch (HomeException e)
        {
        	new DebugLogMsg(this, "Package" + packageId + " is in use ,checking Imsi history ...", null).log(ctx);
        	try{
        		boolean historyPresent = allowClaimWhenPkgNotInUse(ctx,association,sub);
        		if(historyPresent)
        			updatePackage = false;
        		else
        			throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to validate that package '" + packageId + "' is not in use: " + e.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_PACKAGE_NOT_IN_USE_VALIDATION, e, false);
        	}catch(HomeException he)
        	{
            	throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to validate that package '" + packageId + "' is not in use: " + e.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_PACKAGE_NOT_IN_USE_VALIDATION, e, false);
        	}
        }
    	if(updatePackage)
    	{
	        if (LogSupport.isDebugEnabled(ctx))
	        {
	            new DebugLogMsg(this, "Marking package " + packageId + " as in use for subscription " + sub.getId() + "'s additional SIM association...", null).log(ctx);
	        }
	        
	        try
	        {
	            PackageSupportHelper.get(ctx).setPackageState(ctx, packageId, sub.getTechnology(),PackageStateEnum.IN_USE_INDEX, sub.getSpid());
	        }
	        catch (HomeException e)
	        {
	            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to update package '" + packageId + "' to IN USE: " + e.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_PACKAGE_UPDATE_IN_USE, e, false);
	        }
	        
	        try
	        {
	            AppendNumberMgmtHistoryHome appendNumberMgmtHistory = getAppendNumberMgmtHistory(ctx);
	
	            NumberMgmtHistory history = (NumberMgmtHistory) appendNumberMgmtHistory.appendImsiHistory(
	                    ctx,
	                    packageId,
	                    sub.getId(),
	                    appendNumberMgmtHistory.getHistoryEventSupport(ctx).getSubIdModificationEvent(ctx),
	                    "IMSI assigned to Subscriber for Multi-SIM");
	            
	        }
	        catch (HomeException e)
	        {
	            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to update Imsi History for package '" + packageId + "': " + e.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_IMSI_HISTORY_UPDATE, e, false);
	        }
	    }
    }

    private boolean allowClaimWhenPkgNotInUse(Context ctx,SubscriberAuxiliaryService association,Subscriber sub) throws HomeException
    {
    	boolean allowclaim = false;
    	String packageId = association.getMultiSimPackage();
    	if(!matchesWithPrimaryImsi(sub.getPackageId(),packageId))
    	{
	    	boolean presentInImsiHistory = NumberMgnSupport.checkNumberMgmtHistory(ctx,sub.getId(),packageId);
	    	if(presentInImsiHistory)
	    	{
		    	And associationExistanceFilter = new And();
		        associationExistanceFilter.add(new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER, sub.getId()));
		        associationExistanceFilter.add(new EQ(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER,association.getAuxiliaryServiceIdentifier() ));
		        associationExistanceFilter.add(new EQ(SubscriberAuxiliaryServiceXInfo.MULTI_SIM_PACKAGE, packageId));
		        boolean associationExists = HomeSupportHelper.get(ctx).hasBeans(ctx, SubscriberAuxiliaryService.class, associationExistanceFilter);
		    	
		        allowclaim = presentInImsiHistory && !associationExists;
	    	}
    	}
        return allowclaim;
    }
    
    private boolean matchesWithPrimaryImsi(String primaryImsi,String multisimImsi)
    {
    	return primaryImsi.equalsIgnoreCase(multisimImsi);
    }
    
    private AppendNumberMgmtHistoryHome getAppendNumberMgmtHistory(Context ctx)
    {
        if (appendNumberMgmtHistory_==null)
        {
            appendNumberMgmtHistory_ = new AppendNumberMgmtHistoryHome(ctx, NullHome.instance(), PackageMgmtHistoryHome.class){}; 
        }
        return appendNumberMgmtHistory_;
    }
    
    private void addSimDetachedHistory(Context ctx,String subscriberId,String packageId)
    {
    	try
        {
            AppendNumberMgmtHistoryHome appendNumberMgmtHistory = getAppendNumberMgmtHistory(ctx);

            NumberMgmtHistory history = (NumberMgmtHistory) appendNumberMgmtHistory.appendImsiHistory(
                    ctx,
                    packageId,
                    subscriberId,
                    appendNumberMgmtHistory.getHistoryEventSupport(ctx).getSubIdModificationEvent(ctx),
                    "IMSI detached from Subscriber for Multi-SIM");
            
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Unable to add history for Sim "+packageId+" for subscriber "+subscriberId,e).log(ctx);
        }
    }
    
    private AppendNumberMgmtHistoryHome appendNumberMgmtHistory_ = null;

    private List<MultiSimSubExtension> subscriberExtensions_ = null;

    public static final String SERVICE_TRIGGERED_REMOVE = "SERVICE_TRIGGERED_REMOVE";

}
