/*
 * Created on Oct 29, 2004
 *
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.com.
 *
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DCDRIVATIVES.
 */
package com.trilogy.app.crm.cltc;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberCltc;
import com.trilogy.app.crm.bean.SubscriberCltcErStateEnum;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.calculator.CalculatorUtil;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * @author jchen
 *
 * Acting as a proxy home for SubcriberCltcHome, it will update subscriber CLTC status,
 * and provision/unprovision services accordingly.
 */
public class SubCltcServiceProvisionHome 
   extends HomeProxy
{

    public SubCltcServiceProvisionHome(Context ctx, Home delegate)
    {
       super(ctx, delegate);
    }
    
    
    private boolean isSuspension(boolean aboveCreditLimit)
    {
    	return aboveCreditLimit;
    }
    

    /**
     * @see com.redknee.framework.xhome.home.Home#create(java.lang.Object)
     */
    public Object create(Context ctx, Object obj) throws HomeException 
    {
         
        SubscriberCltc subCltc =  (SubscriberCltc)(super.create(ctx,obj));
        
        ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);
        Subscriber subscriber = null;
        try
        {
        	subscriber = SubscriberSupport.lookupActiveSubscriberForMSISDN(ctx, subCltc.getMsisdn());
        }
        catch(HomeException he)
        {
        	if(LogSupport.isDebugEnabled(ctx))
        	{
        		LogSupport.debug(ctx, this, "No active subscription found for MSISDN = "+subCltc.getMsisdn());
        	}
        	
        	subscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, subCltc.getMsisdn(), new Date(info.getDate()));
	        
	        if (subscriber == null)
	        {
	            throw new HomeException("No subscriber found with msisdn " + subCltc.getMsisdn() + " for transaction date " + new Date(info.getDate()) + ". Cannot continue.");
	        }
        }
        
        //proviosn/unprovision services
        try
        {
            subCltc.setSubId(subscriber.getId());
	        subscriber.setAboveCreditLimit(subCltc.getUpCrossThreshold());
	        subscriber.setClctChange(true);
            subscriber.setSubNewBalance(subCltc.getNewBalance());
            subscriber.setSubOldBalance(subCltc.getOldBalance());
            /**
             * Added to get the operation
             */
            subscriber.setCLCTOpertionType(subCltc.getOperation());
	        
            ctx.put(Common.ER_447_SUBSCRIBER_CLCT, subCltc);
            
        	if(subCltc.getBundleServicesChanged() != null && subCltc.getBundleServicesChanged().length() > 0)
        	{
        	
        		StringTokenizer strTokenizer = new StringTokenizer(subCltc.getBundleServicesChanged(), ",");
        		Map<Long, Boolean> serviceTypeExtensionActionFlag = new HashMap<Long, Boolean>();
        		while(strTokenizer.hasMoreTokens())
        		{
        			long serviceId = Long.parseLong(strTokenizer.nextToken());
        			try
                    {
        				boolean actionFlag = CalculatorUtil.getServiceTypeExtensionActionFlag(ctx, serviceId);
               	 		serviceTypeExtensionActionFlag.put(serviceId, actionFlag);
                    }
                    catch (Throwable t)
                    {
                    	LogSupport.minor(ctx, this, "ServiceTypeExtension is not configured in service: "+ serviceId);
                    }
        		}
           	 	ctx.put(Common.SERVICETYPE_EXTENSION_ACTION_FLAG, serviceTypeExtensionActionFlag);
        	}
            
            
            Home subHome = (Home) ctx.get(SubscriberHome.class);

            switch (subCltc.getOperation())
            {
            // Bundle Category depleted
                case SubCltcOperationCode.BUNDLE_BALANCE_DECREASED:
                case SubCltcOperationCode.BUNDLE_BALANCE_DEPLETED:
                case SubCltcOperationCode.BUNDLE_BALANCE_TOPPED:
                case SubCltcOperationCode.BUNDLE_PROVISIONED:
                    subCltc.setServicesChanged(subCltc.getBundleServicesChanged());
                    subHome.store(ctx, subscriber);
                    break;

                default:
                    String bundleService = (subCltc.getBundleServicesChanged().isEmpty() ? "" : ","
                            + subCltc.getBundleServicesChanged());

                    if (!isSuspension(subCltc.getUpCrossThreshold())) // PROVISION
                    {
                        Collection diffServicesBeforeProcessing = ServiceCreditLimitCheckSupport.getDiffServiceIds(ctx,
                                subscriber, this);
                        subCltc.setServicesChanged(ServiceSupport.getServiceIdString(diffServicesBeforeProcessing)
                                + bundleService);
                    }

                    subHome.store(ctx, subscriber);

                    if (isSuspension(subCltc.getUpCrossThreshold())) // UNPROVISION
                    {
                        Collection diffServicesAfterProcessing = ServiceCreditLimitCheckSupport.getDiffServiceIds(ctx,
                                subscriber, this);
                        subCltc.setServicesChanged(ServiceSupport.getServiceIdString(diffServicesAfterProcessing)
                                + bundleService);
                    }
                    break;
            }
	        
	        subCltc.setState(SubscriberCltcErStateEnum.PROCESSED);
            
        }
        catch(Throwable e)
        {
            new MajorLogMsg(
                    this,
                    "Failed to process CLCT ER for msisdn." + subCltc.getMsisdn(),
                    e).log(ctx);
            
            //now mark status to error
            subCltc.setState(SubscriberCltcErStateEnum.PROCESSFAILED);
        }
        finally
        {
            store(ctx,subCltc);
        }
        return subCltc;
    }
    
}
