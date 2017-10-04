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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.CoreCrmLicenseConstants;
import com.trilogy.app.crm.amsisdn.AdditionalMsisdnAuxiliaryServiceSupport;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bulkloader.generic.BulkloadConstants;
import com.trilogy.app.crm.client.EcpVpnClientException;
import com.trilogy.app.crm.client.VpnClientException;
import com.trilogy.app.crm.home.sub.StateChangeAuxiliaryServiceSupport;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.vpn.SubscriberAuxiliaryVpnServiceSupport;


/**
 * 
 *
 * @author sanjay.pagar
 * @since 
 */
public class SubscriberProvisionableServicesCreatingHome extends HomeProxy
{
	
	 public SubscriberProvisionableServicesCreatingHome(final Context ctx, final Home delegate)
	    {
	        super(ctx, delegate);
	    }
	 
	 /**
	     * {@inheritDoc}
	     */
	    @Override
	    public Object create(final Context ctx, final Object obj) throws HomeException
	    {
	    	if (ctx.get(BulkloadConstants.GENERIC_BEAN_BULKLOAD_CSV_COMMAND) != null)
	        {
		        final SubscriberServices association = (SubscriberServices) obj;
	
		        final Service service = association.getService(ctx);
		        
		        ctx.put(Service.class, service);
		        ctx.put(com.redknee.app.crm.bean.Service.class, service);
		        
		        final Subscriber subscriber = SubscriberSupport.getSubscriber(ctx, association.getSubscriberId());
	
		        if (shouldProvision(ctx, association, subscriber, service))
		        {
		            provisionSubscriberService(ctx, association, service, subscriber);
		        }
	
		        final Object createdObject = super.create(ctx, obj);
	
		        return createdObject;
	        }
	    	else
	    	{
	    		return super.create(ctx, obj);
	    	}
	    }

	   
	    private void provisionSubscriberService(final Context ctx, final SubscriberServices association,
	        final Service service, final Subscriber subscriber) throws HomeException
	    {
	        try
	        {
	            provisionHLR(ctx, association, service, subscriber);
	        }
	        catch (final Exception e)
	        {
	            
	            new InfoLogMsg(this, "Failed to provision HLR for subscriptoinId=" + subscriber.getBAN() + " [" + e + "]", e).log(ctx);
	            throw new HomeException(e.getMessage(), e);
	        }	            
	    }

	    private void provisionHLR(final Context ctx, final SubscriberServices association,
	        final Service service, final Subscriber subscriber) throws HomeException
	    {
	        if (LogSupport.isDebugEnabled(ctx))
	        {
	            new DebugLogMsg(this, "Attempting to send HLR provisioning command for  service " , null).log(ctx);
	        }
	        
	              
	        final Map<ExternalAppEnum, ProvisionAgentException> resultCodes = new HashMap<ExternalAppEnum, ProvisionAgentException>();
	        
	        ArrayList<SubscriberServices> subServicesList = new ArrayList<SubscriberServices>(); 
	        subServicesList.add(association);
	        
	        SubscriberServicesSupport.provisionSubscriberServices(ctx, null, subServicesList , subscriber, resultCodes, true);
	       	        
	    }


	    /**
	     * Whether this association should be provisioned.
	     *
	     * 
	     */
	    private boolean shouldProvision(final Context ctx, final SubscriberServices association,
	        final Subscriber subscriber, final Service service)
	    {
	        boolean provision = true;
	        if (service == null || subscriber == null)
	        {
	            provision = false;
	        }
	        
	        return provision;
	    }




	   
}

