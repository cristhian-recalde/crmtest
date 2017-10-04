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
package com.trilogy.app.crm.priceplan.task;

import java.util.Set;

import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.PPVModificationRequest;
import com.trilogy.app.crm.bean.PPVModificationRequestItems;
import com.trilogy.app.crm.bean.PPVModificationRequestStateEnum;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.ServicePackageVersion;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.framework.xlog.log.SeverityEnum;

/**
 * Visitor responsible to apply price plan version modifications to price plan versions.
 * @author Marcio Marques
 * @since 9.2
 *
 */
public class PricePlanVersionModificationApplierVisitor implements Visitor
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PricePlanVersionModificationApplierVisitor(LifecycleAgentSupport agent)
	{
		agent_ = agent;
	}

	@Override
	public void visit(Context ctx, Object obj) throws AgentException,
			AbortVisitException 
    {
        if (agent_ != null && !LifecycleStateEnum.RUNNING.equals(agent_.getState()))
        {
            String msg = "Lifecycle agent " + agent_.getAgentId() + " no longer running.  Remaining modifications will be processed next time.";
            new InfoLogMsg(this, msg, null).log(ctx);
            throw new AbortVisitException(msg);
        }
        
        PPVModificationRequest request = (PPVModificationRequest) obj;
        
        PMLogMsg logMsg = new PMLogMsg("Price Plan Version Modification Agent", "Visitor", String.valueOf(request.getId()));
        try
        {
    
            boolean success = false;
            
            if (LogSupport.isDebugEnabled(ctx))
            {
    			LogSupport.debug(
    					ctx,
    					this,
    					"Processing Price Plan Modification Request for price plan "
    							+ request.getPricePlanIdentifier()
    							+ " version "
    							+ request.getPricePlanVersion()
    							+ " with activation date "
    							+ CoreERLogger.formatERDateDayOnly(request
    									.getActivationDate()));
            }
            
            try
            {
            	PricePlanVersion version = PricePlanSupport.getVersion(ctx, request.getPricePlanIdentifier(), request.getPricePlanVersion());
            	if (version!=null)
            	{
            		version.setCreditLimit(request.getCreditLimit());
            		version.setDeposit(request.getDeposit());
            		version.setDefaultPerMinuteAirRate(request.getDefaultPerMinuteAirRate());
            		version.setOverusageVoiceRate(request.getOverusageVoiceRate());
            		version.setOverusageSmsRate(request.getOverusageSmsRate());
            		version.setOverusageDataRate(request.getOverusageDataRate());
            		version.setChargeCycle(ChargingCycleEnum.get((short) request.getChargeCycle()));
            		
            		PPVModificationRequestItems requestPackages = request.getServicePackageVersion();
            		ServicePackageVersion versionPackages = version.getServicePackageVersion(ctx);
            		
            		for (ServiceFee2ID key : (Set<ServiceFee2ID>) requestPackages.getServiceFees(ctx).keySet())
            		{
            			ServiceFee2 value = (ServiceFee2) requestPackages.getServiceFees(ctx).get(key);
            			if (versionPackages.getServiceFees(ctx).containsKey(key))
            			{
            			    ((com.redknee.app.crm.bean.ServiceFee2) versionPackages.getServiceFees().get(key)).setFee(value.getFee());
            			    ((com.redknee.app.crm.bean.ServiceFee2) versionPackages.getServiceFees().get(key)).setServicePreference(value.getServicePreference());
            			}
            			else
            			{
            				throw new IllegalStateException("Service fee " + value.getServiceId() + " is not present in current version.");
            			}
            		}
    
                    for (ServiceFee2ID key : (Set<ServiceFee2ID>) requestPackages.getNewServiceFees(ctx).keySet())
            		{
            			ServiceFee2 value = (ServiceFee2) requestPackages.getNewServiceFees(ctx).get(key);
            			if (versionPackages.getServiceFees(ctx).containsKey(key))
            			{
            				throw new IllegalStateException("Service fee " + value.getServiceId() + " is already present in current version.");
            			}
            			else
            			{
            				versionPackages.getServiceFees().put(key, value);
            			}
            		}
    
            		for (Long key : (Set<Long>) requestPackages.getBundleFees(ctx).keySet())
            		{
            			BundleFee value = (BundleFee) requestPackages.getBundleFees(ctx).get(key);
            			if (versionPackages.getBundleFees(ctx).containsKey(key))
            			{
            				((com.redknee.app.crm.bundle.BundleFee) versionPackages.getBundleFees().get(key)).setFee(value.getFee());
            				((com.redknee.app.crm.bundle.BundleFee) versionPackages.getBundleFees().get(key)).setServicePreference(value.getServicePreference());
            			}
            			else
            			{
            				throw new IllegalStateException("Bundle fee " + value.getId() + " is not present in current version.");
            			}
            		}
            		
            		for (Long key : (Set<Long>) requestPackages.getNewBundleFees(ctx).keySet())
            		{
            			BundleFee value = (BundleFee) requestPackages.getNewBundleFees(ctx).get(key);
            			if (versionPackages.getBundleFees(ctx).containsKey(key))
            			{
            				throw new IllegalStateException("Bundle fee " + value.getId() + " is already present in current version.");
            			}
            			else
            			{
            				versionPackages.getBundleFees().put(key, value);
            			}
            		}
            		version.setServicePackageVersion(versionPackages);
            		HomeSupportHelper.get(ctx).storeBean(ctx, version);
            		success = true;
            	}
            	else
            	{
    				LogSupport.minor(ctx, this, "Unable to find price plan "
    						+ request.getPricePlanIdentifier() + " version " + request.getPricePlanVersion());
            	}
            }
            catch (Throwable e)
            {
    			LogSupport.minor(ctx, this, "Unable to modify price plan "
    					+ request.getPricePlanIdentifier() + " version " + request.getPricePlanVersion()
    					+ ": " + e.getMessage(), e);
            }
            
            if (success)
            {
            	try
            	{
            		PPVModificationRequest clonedRequest = (PPVModificationRequest)request.clone();
            		clonedRequest.setStatus(PPVModificationRequestStateEnum.PROCESSED_INDEX);
            		clonedRequest.setActivationDate(CalendarSupportHelper.get(ctx).getRunningDate(ctx));
            		HomeSupportHelper.get(ctx).storeBean(ctx, clonedRequest);
            		
            		if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
            		{
    	        		StringBuilder sb = new StringBuilder();
    	        		sb.append("Price Plan Modification Request for price plan ");
    	        		sb.append(request.getPricePlanIdentifier());
    	        		sb.append(" version ");
    	        		sb.append(request.getPricePlanVersion());
    	        		sb.append(" with activation date ");
    					sb.append(CoreERLogger.formatERDateDayOnly(request
    							.getActivationDate()));
    					sb.append(" successfully processed.");
    					LogSupport.info(ctx, this, sb.toString());
            		}
    				
                }
                catch (HomeException e)
                {
    				LogSupport.minor(
    						ctx,
    						this,
    						"Unable to update price plan version modification request for price plan "
    								+ request.getPricePlanIdentifier() + " version "
    								+ request.getPricePlanVersion() + " to PROCESSED: "
    								+ e.getMessage(), e);
                } catch (CloneNotSupportedException e) {
    				LogSupport.minor(
    						ctx,
    						this,
    						" Could not Clone the original PPVModificationRequest object. Unable to update price plan version modification request for price plan "
    								+ request.getPricePlanIdentifier() + " version "
    								+ request.getPricePlanVersion() + " to PROCESSED: "
    								+ e.getMessage(), e);
				}
            }
        }
        finally
        {
            logMsg.log(ctx);
        }
        
		
	}
	
    private final LifecycleAgentSupport agent_;


}
