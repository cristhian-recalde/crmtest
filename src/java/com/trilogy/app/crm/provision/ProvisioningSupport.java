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
package com.trilogy.app.crm.provision;

import java.io.IOException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Provides support for provisioning of services.
 *
 * @author gary.anderson@redknee.com
 */
public final
class ProvisioningSupport
{
    /**
     *  Java package in which to look for Handler classes.
     **/
    public static final String SERVICE_PACKAGE = "com.redknee.app.crm.provision";
    public static final String SUSPEND_SERVICE_PACKAGE = "com.redknee.app.crm.provision.agent.suspend";
    public static final String RESUME_SERVICE_PACKAGE = "com.redknee.app.crm.provision.agent.resume";



    /**
     * Default constructor dissallows instantiation of this support class.
     */
    private ProvisioningSupport()
    {
        // Empty
    }


    /**
     * Creates the context agent for the service represented by the given
     * handler.
     *
     * @param context The operating context.
     * @param handler The name of the service handler (e.g., "Voice", "SMS", etc.).
     * @param agentType The type of the service handler ("Provision" |
     * "Unprovision" | Suspension).
     * @param className full class name
     *
     * @return The ContextAgent that handles the provisioning type described by
     * the given information.
     */
    public static
    ContextAgent createAgent(
        final Context context,
        final String handler,
        final String agentType)
    {
        String packageName = SERVICE_PACKAGE;
        if ( agentType.equalsIgnoreCase("Suspend"))
        {
            packageName = SUSPEND_SERVICE_PACKAGE;
        }

        return createAgent(context,handler,agentType,packageName);
    }
    
    
    /**
     * Creates the context agent for the service represented by the given
     * handler.
     *
     * @param context The operating context.
     * @param handler The name of the service handler (e.g., "Voice", "SMS", etc.).
     * @param agentType The type of the service handler ("Provision" |
     * "Unprovision"| Suspension).
     *
     * @return The ContextAgent that handles the provisioning type described by
     * the given information.
     */
    public static
    ContextAgent createAgent(
        final Context context,
        final String handler,
        final String agentType,
        final String packageName)
    {
        // TODO - 2004-09-30 - This should be rewritten.  It was moved here from
        // the home.sub.ProvisioningHome class.
        try
        {
            final String className = packageName + "." + handler + agentType + "Agent";
            ContextAgent agent =
                (ContextAgent)XBeans.instantiate(
                        className, context);

            // Try specific Package
            if (agent == null)
            {
                agent = (ContextAgent)XBeans.instantiate(handler + agentType + "Agent", context);
            }

            return new SubscriberServiceCLTCAgentProxy(agent);
        }
        catch (ClassNotFoundException ioEx)
        {
            if(LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(ProvisioningSupport.class.getName(),ioEx.getMessage(),ioEx).log(context);
            }
            // throw new AgentException(ioEx.getMessage());
        }

        return null;
    }
    
    
    
    
    /**
     *  Provision the specified Subscriber for the specified Service. *
     *
     *@param  sub                 Description of Parameter
     * @param  service             Description of Parameter
     * @param  referencedSub, the reason we need to pass it, is to sync provisionedService
     * @exception  AgentException  Description of Exception
     *@since
     */
    public static void provisionService(Context context, Subscriber sub, Service service, Subscriber referencedSub) throws AgentException
    {
        if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(
                LOG_MODULE,
                "provision the following service for subscriber "
                + sub.getMSISDN()
                + ": "
                + service.toString(),
                null).log(
                        context);
        }
        executeServiceProvisionHandler("Provision", sub, service, SERVICE_PACKAGE, context);
        
        if (sub != null)
        {
            sub.serviceProvisioned(context, service);
        }
        if (referencedSub != null)
        {
            referencedSub.serviceProvisioned(context, service);
        }
    }

    /**
     *  Unprovision the specified Subscriber for the specified Service. *
     *
     *@param  sub                 Description of Parameter
     * @param  service             Description of Parameter
     * @param  referencedSub, the reason we need to pass it, is to sync provisionedService
     * @exception  AgentException  Description of Exception
     *@since
     */
    public static void unprovisionService(Context context, Subscriber sub, Service service, Subscriber referencedSub) throws AgentException
    {
        if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(
                LOG_MODULE,
                "unprovision the following service for subscriber "
                + sub.getMSISDN()
                + ": "
                + service.toString(),
                null).log(
                        context);
        }
        try
        {
            executeServiceProvisionHandler("Unprovision", sub, service, SERVICE_PACKAGE, context);
        }
        catch(SkipProvisioningException e)
        {
            //Do not continue to throw this as a bad exception
            //log the exception and continue normally.
            LogSupport.info(context, e.getSource(), e.getMessage());
        }
        
        if (sub != null)
        {
            sub.serviceUnProvisioned(context, service);
        }
        if (referencedSub != null)
        {
            referencedSub.serviceUnProvisioned(context, service);
        }
    }
    
    /**
     *  Suspend the specified Subscriber for the specified Service. *
     *
     *@param  sub                 Description of Parameter
     * @param  service             Description of Parameter
     * @param  referencedSub, the reason we need to pass it, is to sync provisionedService
     * @exception  AgentException  Description of Exception
     *@since
     */
    public static void suspendService(Context context, Subscriber sub, Service service, Subscriber referencedSub) throws AgentException
    {
        if (!exemptFromProvisioningHere(service))
        {
            if (LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(
                        LOG_MODULE,
                        "Suspend the following service for subscriber "
                        + sub.getMSISDN()
                        + ": "
                        + service.toString(),
                        null).log(
                                context);
            }
            final String agentType = "Suspend";
            final String packageName = SUSPEND_SERVICE_PACKAGE ;
            executeServiceProvisionHandler(agentType, sub, service, packageName, context);
            if (sub != null)
            {
                sub.serviceUnProvisioned(context, service);
            }
            if (referencedSub != null)
            {
                referencedSub.serviceUnProvisioned(context, service);
            }
        }
        else
        {
            if (LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, ProvisioningSupport.class, "This service is not suspended by this agent.");
            }
        }
    }
    
    
    /**
     *  Resume the specified Subscriber for the specified Service. *
     *
     *@param  sub                 Description of Parameter
     * @param  service             Description of Parameter
     * @param  referencedSub, the reason we need to pass it, is to sync provisionedService
     * @exception  AgentException  Description of Exception
     *@since
     */
    public static void resumeService(Context context, Subscriber sub, Service service, Subscriber referencedSub) throws AgentException
    {
        if (!exemptFromProvisioningHere(service))
        {
            if (LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(
                        LOG_MODULE,
                        "Resume the following service for subscriber "
                        + sub.getMSISDN()
                        + ": "
                        + service.toString(),
                        null).log(
                                context);
            }

            final String agentType = "Resume";
            final String packageName = RESUME_SERVICE_PACKAGE ;
            executeServiceProvisionHandler(agentType, sub, service,  packageName, context);

            if (sub != null)
            {
                sub.serviceProvisioned(context, service);
            }
            if (referencedSub != null)
            {
                referencedSub.serviceProvisioned(context, service);
            }
        }
        else
        {
            if (LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, ProvisioningSupport.class, "This service is not resumed by this agent.");
            }
        }
    }
    
    /**
     * We will slowly move away from this way to handle Suspend and Resume.
     * See the SubscriberStateChangeUpdateHome.
     * @param service
     * @return
     */
    private static boolean exemptFromProvisioningHere(Service service) 
    {
        //All Alcatel suspend and resume provisioning will be done using SuspendAlcatelServiceUpdateAgent and ResumeAlcatelServiceUpdateAgent 
        boolean condition = service.getType().equals(ServiceTypeEnum.ALCATEL_SSC);
        return condition;
    }


    public static void executeServiceProvisionHandler(
        String           agentType,
        Subscriber       sub,
        Service          service,
        String           packageName,
        Context context)
        throws AgentException
    {
        // look up the Account associated with the subcriber and store it in the subcontext
        Account account;

        try
        {
            account = (Account) context.get(Account.class);
            
            if(account==null)
            {
                account = AccountSupport.getAccount(context, sub.getBAN());
            }

            if (account == null)
            {
                throw new AgentException(
                    "Invalid BAN. Account " + sub.getBAN() + " does not exist");
            }
        }
        catch (HomeException e)
        {
            throw new AgentException("Invalid BAN. Account " + sub.getBAN() + " does not exist");
        }

        Context ctx = context.createSubContext();

        ctx.put(Subscriber.class, sub);
        ctx.put(Service.class, service);
        ctx.put(Account.class, account);

        // Look in Default Package
        ContextAgent agent = null;
        String handler = null;

        if (service == null)
        {
            handler = "Generic";
        }
        else
        {
            handler = service.getHandler();
        }

        agent = ProvisioningSupport.createAgent(ctx, handler, agentType, packageName);
        
        if(agent == null && ServiceTypeEnum.SERVICE_PROVISIONING_GATEWAY.equals(service.getType()) )
        {
        	LogSupport.info(ctx, ProvisioningSupport.class.getName(), "Using Generic Handler for SPG service type");
        	handler="Generic";
        	agent = ProvisioningSupport.createAgent(ctx, handler, agentType, packageName);
        }

        if (agent != null)
        {
            agent.execute(ctx);
        }
    }

    public static final  String LOG_MODULE = "ProvisioningSupport";

} // class
