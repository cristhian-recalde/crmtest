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
package com.trilogy.app.crm.subscriber.provision;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;



import com.trilogy.app.crm.aptilo.AptiloSupport;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.EVDOStateEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.WIMAXStateEnum;
import com.trilogy.app.crm.bean.service.EventSuccessEnum;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistory;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistoryHome;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistoryXInfo;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;

import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.state.ResumeWimaxServiceUpdateAgent;
import com.trilogy.app.crm.state.SuspendWimaxServiceUpdateAgent;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SpidSupport;

import com.trilogy.app.crm.provision.WimaxProvisionAgent;
import com.trilogy.app.crm.provision.WimaxUnprovisionAgent;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Limit;
import com.trilogy.framework.xhome.elang.OrderBy;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.OrderByHome;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.logger.LoggerSupport;
import com.trilogy.service.aptilo.ServiceAptiloException;
import com.trilogy.service.aptilo.model.StatusEnum;
import com.trilogy.util.snippet.log.Logger;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.subscriber.charge.CrmCharger;
import com.trilogy.app.crm.subscriber.charge.SubscriberServiceCharger;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.support.PackageSupportHelper;
import com.trilogy.app.crm.bean.service.ServiceProvisionActionEnum;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.util.SubscriberServicesUtil;

/**
 * Home responsible for provisioning Package swap for Aptilo
 * and handling dunning state actions.
 * 
 * @author anuradha.malvadkar@redknee.com 9.7.2
 *
 */
public class SubscriberWimaxAptiloUpdateHome extends HomeProxy
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final String PROVISIONING_ERROR_MESSAGE = "Provisioning result " + AptiloSupport.WIMAX_PROVISION_ERRORCODE;
    
    /**
     * @param ctx
     * @param delegate
     */
    public SubscriberWimaxAptiloUpdateHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }    
    

    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */    
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        
        boolean call_provisiong_Flag= false;
        final Subscriber newSub = (Subscriber) obj;
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        Service service = null;

        // Needs to update only if subscriber has Wimax service, and was activated
              
        if(newSub!=null && subscriberHasAptiloService(ctx,newSub,oldSub))
        {            
            if (!SafetyUtil.safeEquals(newSub.getState(), oldSub.getState()))
            {
            	
                handleDunningRelatedStates(ctx, newSub, oldSub);
            }
                    
        }
        if (newSub != null && oldSub!=null && subscriberHasActivatedAptiloService(ctx, newSub, oldSub))
        {
            if (!SafetyUtil.safeEquals(newSub.getPackageId(), oldSub.getPackageId()))
            {
                try{
            
            	service =updateSubscriberOnAptiloUnProvision(ctx, newSub, oldSub);
            	call_provisiong_Flag=true;
                }
            	catch (AgentException e)
                {
            	   
                    String message = PROVISIONING_ERROR_MESSAGE + ": Aptilo services for subscriber " + newSub.getId() + "  not activated ("
                        + retrieveAptiloExceptionMessage(e) + ")";
                    addProvisionException(ctx, message, newSub, oldSub, e);
                    throw new HomeException ("Package Unprovision Failure");
                    } 
                catch (Exception e)
                {
                    new InfoLogMsg(this, "Error retrieve Aptilo service for subscriber " + newSub.getId() + ": " + e.getMessage(), e).log(ctx);
                    
                    
                    String message = PROVISIONING_ERROR_MESSAGE + ": Aptilo services for subscriber " + newSub.getId() +  " not activated (Error retrieving service: " + e.getMessage() + ")";
                    addProvisionException(ctx, message, newSub, oldSub, e);
                    throw new HomeException ("Package Unprovision Failure");
                }
                finally
                {
                    if(newSub!=null)
                    {
                      
                        PackageSupportHelper.get(ctx).setPackageState(ctx, newSub.getPackageId(),newSub.getTechnology(),PackageStateEnum.AVAILABLE_INDEX, newSub.getSpid());
                      
                        if(service ==null)
                        {
                              service = AptiloSupport.getSubscriberAptiloService(ctx, newSub.getId());
                        }
                       
                       
                     updateSubscriberServiceState(ctx, newSub,
                             AptiloSupport.getSubscriberAptiloServiceID(ctx, newSub.getId()), ServiceProvisionActionEnum.PROVISION, true);
                       
                        
                    }
                    else if(oldSub!=null)
                    {
                  
                        PackageSupportHelper.get(ctx).setPackageState(ctx, oldSub.getPackageId(), oldSub.getTechnology(),PackageStateEnum.AVAILABLE_INDEX, oldSub.getSpid());
                        
                    }
                }
                
            } 
        }
        
        Object ret =  super.store(ctx, newSub);
        
        
        if (call_provisiong_Flag )
        {
            
            final Subscriber newSub1 = (Subscriber) obj;
       
            	updateSubscriberOnAptiloProvision(ctx, newSub1,service);
            
        }
        
        
        return ret;
        
    }
    
    private void handleDunningRelatedStates(Context ctx, Subscriber newSub, Subscriber oldSub) throws HomeException
    {
        WIMAXStateEnum state = getDunnedServiceAction(ctx, newSub, oldSub);
        
        if (WIMAXStateEnum.INACTIVE.equals(state))
        {
            suspendWimaxService(ctx, newSub, oldSub);
        }
        else if (WIMAXStateEnum.ACTIVE.equals(state))
        {
        	
            resumeWimaxService(ctx, newSub, oldSub);
        }
    
        
    }


    private WIMAXStateEnum getDunnedServiceAction(Context ctx, Subscriber subscriber, Subscriber oldSub) throws HomeException
    {
        CRMSpid spid = SpidSupport.getCRMSpid(ctx, subscriber.getSpid());
        WIMAXStateEnum state = null;
        
        switch (subscriber.getState().getIndex())
        {
            case SubscriberStateEnum.NON_PAYMENT_WARN_INDEX:
                state = spid.getWIMAXwarningAction();
                new InfoLogMsg(this, "Dunning - Subscriber entring Wanred state - Wimax aptilo service entering warn set to:" 
                        + state.getDescription(), null).log(ctx);
                break;
            case SubscriberStateEnum.NON_PAYMENT_SUSPENDED_INDEX:
                state = spid.getWIMAXdunningAction();
                new InfoLogMsg(this, "Dunning - Subscriber entring Dunned state - Wimax aptilo service entering warn set to:" 
                        + state.getDescription(), null).log(ctx);
                break;
            case SubscriberStateEnum.IN_ARREARS_INDEX:
                state = spid.getWIMAXinArrearsAction();
                new InfoLogMsg(this, "Dunning - Subscriber entring In Arrears state - Wimax aptilo service entering set to:" 
                        + state.getDescription(), null).log(ctx);
                break;
            case SubscriberStateEnum.PROMISE_TO_PAY_INDEX:
                state = WIMAXStateEnum.ACTIVE;
                
                
                new InfoLogMsg(this, "Dunning - Subscriber entring Promise to Pay state - Wimax aptilo service entering set to:" 
                        + state.getDescription(), null).log(ctx);
                Logger.debug(ctx, SubscriberWimaxAptiloUpdateHome.class, "PROMISE_TO_PAY_INDEX" , null);
              	if(LogSupport.isDebugEnabled(ctx))
              	{
              		Logger.debug(ctx, SubscriberWimaxAptiloUpdateHome.class, "PROMISE_TO_PAY_INDEX 2" , null);
              	}
                break;
            
            case SubscriberStateEnum.ACTIVE_INDEX:
            
                if (EnumStateSupportHelper.get(ctx).isOneOfStates(oldSub, new Enum[]
                            {  SubscriberStateEnum.NON_PAYMENT_WARN,
                	        SubscriberStateEnum.NON_PAYMENT_SUSPENDED, 
                	        SubscriberStateEnum.IN_ARREARS,
                	        SubscriberStateEnum.IN_COLLECTION}))
                {
                    state = WIMAXStateEnum.ACTIVE;
                    new InfoLogMsg(this, "Dunning - Subscriber entring Active state - Wimax aptilo service entering set to:" 
                            + state.getDescription(), null).log(ctx);
                }
                break;                
            default:
                state = null;
        }
        
       
        return state;
    }


    private void suspendWimaxService(Context ctx, Subscriber newSub, Subscriber oldSub)
    {
        try
        {
            Service service = AptiloSupport.getSubscriberAptiloService(ctx, newSub.getId());
            if(service!=null)
            {
            
            	new SuspendWimaxServiceUpdateAgent().update(ctx, newSub, service);
            }
        }
        catch (Exception e)
        {
            String message = PROVISIONING_ERROR_MESSAGE + ": Aptilo services for subscriber " + newSub.getId() + "  not resumed ("
            + retrieveAptiloExceptionMessage(e) + ")";
            addProvisionException(ctx, message, newSub, oldSub, e);
        }
        
    }


    private void resumeWimaxService(Context ctx, Subscriber newSub, Subscriber oldSub)
    {
        
        try
        {
            Service service = AptiloSupport.getSubscriberAptiloService(ctx, newSub.getId());
            
            if(service!=null)
            {
            
            	new ResumeWimaxServiceUpdateAgent().update(ctx, newSub, service);
            }
        }
        catch (Exception e)
        {
            String message = PROVISIONING_ERROR_MESSAGE + ": Aptilo services for subscriber " + newSub.getId() + "  not resumed ("
            + retrieveAptiloExceptionMessage(e) + ")";
            addProvisionException(ctx, message, newSub, oldSub, e);
        }
    }


    /**
     * Verifies whether or not subscriber has a Wimax service and provisoined at Aptilo.
     * Service changes are handled by another home.
     * @param ctx Context object.
     * @param oldSub 
     * @param sub Subscriber object.
     * @return Value indicating whether or not subscriber has a Wimax service provisioned and activated at Aptilo.
     * 
     * @throws HomeException
     */
    private boolean subscriberHasActivatedAptiloService(Context ctx, Subscriber newSub, Subscriber oldSub) 
        throws HomeException
    {
        if (AptiloSupport.subscriberHasBeenActivated(newSub) &&
                AptiloSupport.subscriberHasBeenActivated(oldSub))
        {
            Service service = AptiloSupport.getSubscriberAptiloService(ctx, newSub.getId());
          
            if(service!=null)
            {
            	//This is not a new service or price plan switch
            	if (newSub.getProvisionedServices(ctx).contains(service.getID()) &&
                    oldSub.getProvisionedServices(ctx).contains(service.getID()))
            	{
            		return true;
            	}
            }
        }
        return false;
    }
    
    private boolean subscriberHasAptiloService(Context ctx, Subscriber newSub, Subscriber oldSub) 
            throws HomeException
        {
            if (AptiloSupport.subscriberHasBeenActivated(newSub) &&
                    AptiloSupport.subscriberHasBeenActivated(oldSub))
            {
                Service service = AptiloSupport.getSubscriberAptiloService(ctx, newSub.getId());
              
                if(service!=null)
                {
                	//This is not a new service or price plan switch
                	
                		return true;
                	
                }
            }
            return false;
        }
        
    
   
    
    
    /**
     * Updates the subscriber IMSI.
     * @param ctx Context object.
     * @param newSub New subscriber object.
     * @param oldSub Old subscriber object.
     */
    private Service updateSubscriberOnAptiloUnProvision(Context ctx, Subscriber newSub, Subscriber oldSub) throws AgentException,HomeException
    {
        Service oldService = null;
    
           
             oldService = AptiloSupport.getSubscriberAptiloService(ctx, oldSub.getId());
             Service newService = AptiloSupport.getSubscriberAptiloService(ctx, newSub.getId());
            if ( oldService == null)
            {
                //no need to update Aptilo on service addition/removal
                return oldService;
            }
            ctx.put(Subscriber.class, oldSub);
            ctx.put(com.redknee.app.crm.bean.core.Service.class, oldService);
          
            new WimaxUnprovisionAgent().execute(ctx);
          
          
        
        return oldService;

    }
    private void updateSubscriberOnAptiloProvision(Context ctx, Subscriber newSub,Service newService)
    {
        try
        {
  
            ctx.put(Subscriber.class, newSub);
            ctx.put(com.redknee.app.crm.bean.core.Service.class, newService);
            new MinorLogMsg(this, "newSub newService " + newService, null).log(ctx);
            new MinorLogMsg(this, "newSub  " + newSub, null).log(ctx);
            new WimaxProvisionAgent().execute(ctx);
          
           
        } 
        catch (AgentException e)
        {
           
            String message = PROVISIONING_ERROR_MESSAGE + ": Aptilo services for subscriber " + newSub.getId() + "  not activated ("
                + retrieveAptiloExceptionMessage(e) + ")";
           
            new MajorLogMsg(this, message, e).log(ctx);      
        } 
        catch (Exception e)
        {
            new InfoLogMsg(this, "Error retrieve Aptilo service for subscriber " + newSub.getId() + ": " + e.getMessage(), e).log(ctx);
            
            
            String message = PROVISIONING_ERROR_MESSAGE + ": Aptilo services for subscriber " + newSub.getId() +  " not activated (Error retrieving service: " + e.getMessage() + ")";
            new MajorLogMsg(this, message, e).log(ctx);      
        }

    }

    /**
     * Retrieves the message that should be displayed for the ServiceAptiloException.
     * @param exception Thrown exception.
     * @return Message to be displayed.
     */
    private String retrieveAptiloExceptionMessage(Exception e)
    {
        if (e.getCause() instanceof ServiceAptiloException)
        {
            ServiceAptiloException exception = (ServiceAptiloException)e;
            if (StatusEnum.COMM_FAILURE.equals(exception.getResultCode()))
            {
                return "Communication failure: " + exception.getDescription();
            } 
            else if (exception.getResultCode() != null)
            {
                return exception.getResultCode().getDescription() + " - " + String.valueOf(exception.getDescription());
            }
            else if (exception.getDescription() != null)
            {
                return exception.getDescription();
            }
            else
            {
                return "Unknown error";
            }
        }
        else
        {
            return String.valueOf(e.getMessage());
        }
    }
    
    
    /**
     * Adds provisioning exception the the Subscriber Provision Result code, and writes a major log message.
     * @param ctx Context object.
     * @param message Message to be logged.
     * @param newSub New subscriber.
     * @param oldSub Old subscriber.
     * @param e Thrown exception.
     */
    private void addProvisionException(Context ctx, String message, Subscriber newSub, Subscriber oldSub, Throwable e)
    {
        SubscriberProvisionResultCode.setProvisionWimaxErrorCode(ctx, AptiloSupport.WIMAX_PROVISION_ERRORCODE);

        ProvisioningHomeException provisionException = new ProvisioningHomeException(message, AptiloSupport.WIMAX_PROVISION_ERRORCODE);
        provisionException.initCause(e);
        SubscriberProvisionResultCode.addException(ctx, message, provisionException, oldSub, newSub);

        new MajorLogMsg(this, message, e).log(ctx);        
    }
    
    public static void updateSubscriberServiceState(final Context ctx,
            final Subscriber subscriber,
             final long serviceId,   
            final ServiceProvisionActionEnum action,
            final boolean provisionResult) 
    {
        
        
        SubscriberServices service = SubscriberServicesSupport.getSubscriberServiceRecord(ctx,
                subscriber.getId(),
                Long.valueOf(serviceId), SubscriberServicesUtil.DEFAULT_PATH);
     
        
        final ServiceStateEnum state = getServiceState(action, provisionResult);  
        
        if (service != null )
        {    
            service.setProvisionAction( action);
            service.setProvisionedState(state);
            service.setProvisionActionState(provisionResult); 

  
        }     
        try 
        {
            SubscriberServicesSupport.updateSubscriberServiceRecord(ctx, service);
        } catch (Exception e)
        {
            
           
        }
    }
    
    private static ServiceStateEnum getServiceState(final ServiceProvisionActionEnum action,
            final boolean provisionResult)
    {
        switch (action.getIndex())
        {
        case ServiceProvisionActionEnum.PROVISION_INDEX:
        case ServiceProvisionActionEnum.UPDATE_ATTRIBUTES_INDEX:
        case ServiceProvisionActionEnum.RESUME_INDEX:    
            if (provisionResult)
            {
                return ServiceStateEnum.PROVISIONED;
            }else
             {
             return ServiceStateEnum.PROVISIONEDWITHERRORS;   
            }
        case ServiceProvisionActionEnum.UNPROVISION_INDEX:
            if (provisionResult)
            {
                return ServiceStateEnum.UNPROVISIONED;
            }else
             {
             return ServiceStateEnum.UNPROVISIONEDWITHERRORS;   
            }    
        case ServiceProvisionActionEnum.SUSPEND_INDEX:
            if (provisionResult)
            {
                return ServiceStateEnum.SUSPENDED;
            }else
             {
             return ServiceStateEnum.SUSPENDEDWITHERRORS;   
            }   
        }
        
        
        return ServiceStateEnum.PENDING;
    }
  
    
    
    private static final int[] SUBSCRIBER_SUSPENDED_STATES = new int[]
    {
        SubscriberStateEnum.SUSPENDED_INDEX, 
        SubscriberStateEnum.LOCKED_INDEX,
        SubscriberStateEnum.EXPIRED_INDEX,
    };
    
    private static final int[] SUBSCRIBER_DUNNED_STATES = new int[]
    {
        SubscriberStateEnum.NON_PAYMENT_WARN_INDEX,
        SubscriberStateEnum.NON_PAYMENT_SUSPENDED_INDEX, 
        SubscriberStateEnum.IN_ARREARS_INDEX,
        SubscriberStateEnum.IN_COLLECTION_INDEX,
    };
    
}
