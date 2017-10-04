/* 
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries. 
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.state;

import java.util.Arrays;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.blackberry.BlackberrySupport;
import com.trilogy.app.crm.blackberry.error.ERErrorHandler;
import com.trilogy.app.crm.blackberry.error.ErrorHandler;
import com.trilogy.app.crm.hlr.CrmHlrServicePipelineImpl;
import com.trilogy.app.crm.hlr.HlrSupport;
import com.trilogy.app.crm.provision.CommonProvisionAgentBase;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.service.blackberry.IServiceBlackberry;
import com.trilogy.service.blackberry.ServiceBlackberryException;
import com.trilogy.service.blackberry.ServiceBlackberryFactory;
import com.trilogy.service.blackberry.model.ResultEnum;


/**
 * Supports all the shared methods to execute an external service change
 * @author arturo.medina@redknee.com
 *
 */
public abstract class AbstractBlackberryServiceUpdateAgent implements ServiceStateUpdateAgent
{
    /**
     * initializes the Error handler
     */
    public AbstractBlackberryServiceUpdateAgent()
    {
        handler_ = new ERErrorHandler();
    }

    /**
     * {@inheritDoc}
     */
    public void update(Context ctx, Subscriber subscriber, Service service) throws HomeException
    {
        long[] services = BlackberrySupport.getBlackberryServicesIdsForService(ctx, service.getID());
        try
        {
            //First call the HLR command for BB service suspension
            //callHlr(ctx, subscriber.getHlrId(), getHLRCommand(subscriber, service), subscriber);
            callHlr(ctx, service, subscriber);
            IServiceBlackberry bbService = ServiceBlackberryFactory.getServiceBlackberry(ctx, subscriber.getSpid());
            final CompoundIllegalStateException compound = new CompoundIllegalStateException();
            
            if(bbService == null) 
            {
            	compound.thrown(
                        new IllegalPropertyArgumentException(
                            SubscriberXInfo.INTENT_TO_PROVISION_SERVICES, "Missing Blackberry provision configuration for SPID - " + subscriber.getSpid()));
            	compound.throwAll();
            }
            
            if(bbService != null) 
            {
            	callBlackberryService(ctx, bbService, service, subscriber);
            }
        }
        catch (ServiceBlackberryException e)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Error calling the Blackberry service for IMSI " +
                        subscriber.getIMSI() + 
                        " and services " + 
                        Arrays.toString(services),
                        e);
            }

            //ignore the error on BB PRV, just log an ER
            handler_.handleError(ctx, subscriber, service, e.getResultStatus(), e.getErrorCode(), e.getDescription());
        }
        catch (AgentException e)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Error calling the HLR command for IMSI " +
                        subscriber.getIMSI() + 
                        " and services " + 
                        Arrays.toString(services),e);
            }
            handler_.handleError(ctx, subscriber, service, ResultEnum.RIM_PROVISION_FAILURE, "", "HLR Exception");
            throw new HomeException(e.getMessage(),e);
        }

    }

    /**
     * Calls the Blackberry HLR commands for suspension and reactivation(old)(Function call is from BSS >> (SPG>>HLR))
     * @param ctx
     * @param hlrId
     * @param hlrCmds
     * @param subscriber
     * @throws AgentException
     */
    /*private void callHlr(Context ctx, short hlrId, String hlrCmds, Subscriber subscriber) throws AgentException
    {
        final CrmHlrServicePipelineImpl hlrService = CrmHlrServicePipelineImpl.instance();
        try
        {
        	String request = CommonProvisionAgentBase.replaceHLRCommand(ctx,  hlrCmds, subscriber, null, null);
            hlrService.process(ctx, hlrId, request);

        }
        catch (Exception e)
        {
            throw new AgentException(e);
        }
    }*/
    /**
     * Calls the Blackberry HLR commands for suspension and reactivation(new)(Function call is from BSS >> (SPG>>ESB))
     * @param ctx
     * @param service
     * @param subscriber
     * @throws AgentException
     */
    protected abstract void callHlr(Context ctx, Service service, Subscriber subscriber) throws AgentException;

    /**
     * gets the appropriate HLR command from the service 
     * @param service
     * @return
     */
    protected abstract String getHLRCommand(Subscriber subscriber, Service service);

    
    /**
     * Calls the appropriate service from the BB service
     * @param ctx 
     * @param bbService
     * @param service
     * @param subscriber 
     */
    protected abstract void callBlackberryService(Context ctx,
            IServiceBlackberry bbService,
            Service service,
            Subscriber subscriber) throws ServiceBlackberryException;

    private ErrorHandler handler_;
}
