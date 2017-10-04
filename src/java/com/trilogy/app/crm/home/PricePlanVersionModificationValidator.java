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

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.PPVModificationRequest;
import com.trilogy.app.crm.bean.PPVModificationRequestStateEnum;
import com.trilogy.app.crm.bean.PPVModificationRequestXInfo;
import com.trilogy.app.crm.bean.ServiceFee2;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bundle.BundleFee;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Validates the price plan version modification
 *
 * @author Marcio Marques
 * @since 9.2
 */

public class PricePlanVersionModificationValidator implements Validator
{
    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj)
    {
        final PPVModificationRequest ppv = (PPVModificationRequest) obj;
        
        // Validation not needed when marking request as processed.
        if (ppv.getStatus() != PPVModificationRequestStateEnum.PROCESSED_INDEX)
        {
            PricePlanVersion version = null;
            CompoundIllegalStateException cise = new CompoundIllegalStateException();

            Date today = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(CalendarSupportHelper.get(ctx).getRunningDate(ctx));
            if (ppv.getActivationDate().before(today))
            {
            	cise.thrown(new IllegalPropertyArgumentException(PPVModificationRequestXInfo.ACTIVATION_DATE, "Activation date should be today for immediate activation or in the future"));
            }
            
            try
            {
    	        version = PricePlanSupport.getVersion(ctx, ppv.getPricePlanIdentifier(), ppv.getPricePlanVersion());
            }
            catch (HomeException e)
            {
            	LogSupport.minor(ctx,  this, "Unable to retrieve price plan version " + ppv.getPricePlanVersion() + " for price plan " + ppv.getPricePlanIdentifier() + ": " + e.getMessage(), e);
            }
            
            if (version!=null)
            {
            	validateServiceFees(ctx, ppv, version, cise);
            	validateBundleFees(ctx, ppv, version, cise);
    
            }
            else
            {
            	cise.thrown(new IllegalPropertyArgumentException(PPVModificationRequestXInfo.PRICE_PLAN_VERSION, "Unable to retrieve price plan version"));
            }
            
            cise.throwAll();
        }
    }
    
    private void validateServiceFees(Context ctx, PPVModificationRequest ppv, PricePlanVersion version, CompoundIllegalStateException cise)
    {
        final Map requestServiceFees = ppv.getServicePackageVersion().getServiceFees(ctx);
        final Map<ServiceFee2ID, com.redknee.app.crm.bean.core.ServiceFee2> oldServiceFees = version.getServicePackageVersion(ctx).getServiceFees();

        // Validating existing fees
        for (ServiceFee2ID identifier : (Set<ServiceFee2ID>) (oldServiceFees.keySet()))
        {
        	ServiceFee2 newFee = (ServiceFee2) requestServiceFees.get(identifier);
        	ServiceFee2 oldFee = (ServiceFee2) oldServiceFees.get(identifier);
        	if (newFee==null)
        	{
				cise.thrown(new IllegalPropertyArgumentException(
						PPVModificationRequestXInfo.SERVICE_PACKAGE_VERSION,
						"Service fee "
								+ oldFee.getServiceId()
								+ " cannot be removed during price plan modification request"));
        	}
        	else if (oldFee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY) && !newFee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY))
        	{
				cise.thrown(new IllegalPropertyArgumentException(
						PPVModificationRequestXInfo.SERVICE_PACKAGE_VERSION,
						"Service fee "
								+ oldFee.getServiceId()
								+ " should remain MANDATORY during price plan modification request"));
        	}
        	else if (!oldFee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY) && newFee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY))
        	{
				cise.thrown(new IllegalPropertyArgumentException(
						PPVModificationRequestXInfo.SERVICE_PACKAGE_VERSION,
						"Service fee "
								+ oldFee.getServiceId()
								+ " cannot be made MANDATORY during price plan modification request"));
        	}
        }

        final Map newServiceFees = ppv.getServicePackageVersion().getNewServiceFees();

        // Validating new fees
        for (Long identifier : (Set<Long>) (newServiceFees.keySet()))
        {
        	ServiceFee2 newFee = (ServiceFee2) newServiceFees.get(identifier);
        	ServiceFee2 oldFee = (ServiceFee2) oldServiceFees.get(identifier);
        	if (oldFee==null && newFee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY))
        	{
				cise.thrown(new IllegalPropertyArgumentException(
						PPVModificationRequestXInfo.SERVICE_PACKAGE_VERSION,
						"Service fee "
								+ oldFee.getServiceId()
								+ " cannot be added as MANDATORY during price plan modification request"));
        	}
        	else if (oldFee!=null)
        	{
				cise.thrown(new IllegalPropertyArgumentException(
						PPVModificationRequestXInfo.SERVICE_PACKAGE_VERSION,
						"Service fee "
								+ oldFee.getServiceId()
								+ " is already in the current price plan version"));
        	}

        }
    }

    private void validateBundleFees(Context ctx, PPVModificationRequest ppv, PricePlanVersion version, CompoundIllegalStateException cise)
    {
        final Map requestBundleFees = ppv.getServicePackageVersion().getBundleFees(ctx);
        final Map oldBundleFees = version.getServicePackageVersion(ctx).getBundleFees();

        // Validating existing fees
        for (Long identifier : (Set<Long>) (oldBundleFees.keySet()))
        {
        	BundleFee newFee = (BundleFee) requestBundleFees.get(identifier);
    		BundleFee oldFee = (BundleFee) oldBundleFees.get(identifier);
        	if (newFee==null)
        	{
				cise.thrown(new IllegalPropertyArgumentException(
						PPVModificationRequestXInfo.SERVICE_PACKAGE_VERSION,
						"Bundle fee "
								+ oldFee.getId()
								+ " cannot be removed during price plan modification request"));
        	}
        	else if (oldFee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY) && !newFee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY))
        	{
				cise.thrown(new IllegalPropertyArgumentException(
						PPVModificationRequestXInfo.SERVICE_PACKAGE_VERSION,
						"Bundle fee "
								+ oldFee.getId()
								+ " should remain MANDATORY during price plan modification request"));
        	}
        	else if (!oldFee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY) && newFee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY))
        	{
				cise.thrown(new IllegalPropertyArgumentException(
						PPVModificationRequestXInfo.SERVICE_PACKAGE_VERSION,
						"Bundle fee "
								+ oldFee.getId()
								+ " cannot be made MANDATORY during price plan modification request"));
        	}
        }

        final Map newBundleFees = ppv.getServicePackageVersion().getNewBundleFees();

        // Validating new fees
        for (Long identifier : (Set<Long>) (newBundleFees.keySet()))
        {
        	BundleFee newFee = (BundleFee) newBundleFees.get(identifier);
    		BundleFee oldFee = (BundleFee) oldBundleFees.get(identifier);
        	if (oldFee==null && newFee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY))
        	{
				cise.thrown(new IllegalPropertyArgumentException(
						PPVModificationRequestXInfo.SERVICE_PACKAGE_VERSION,
						"Bundle fee "
								+ oldFee.getId()
								+ " cannot be added as MANDATORY during price plan modification request"));
        	}
        	else if (oldFee!=null)
        	{
				cise.thrown(new IllegalPropertyArgumentException(
						PPVModificationRequestXInfo.SERVICE_PACKAGE_VERSION,
						"Bundle fee "
								+ oldFee.getId()
								+ " is already in the current price plan version"));
        	}
        }
    }
}