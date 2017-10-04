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
package com.trilogy.app.crm.subscriber.provision.blackberry;

import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.GSMPackageXInfo;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.blackberry.BlackberrySupport;
import com.trilogy.app.crm.blackberry.error.RIMBlackBerryErrorCodes;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionResultCode;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.service.blackberry.IServiceBlackberry;
import com.trilogy.service.blackberry.ServiceBlackberryException;
import com.trilogy.service.blackberry.ServiceBlackberryFactory;
import com.trilogy.service.blackberry.model.Attributes;
import com.trilogy.service.blackberry.model.ResultEnum;

/**
 * Home responsible for provisioning MSISDN and Package swap for Blackberry.
 * @author marcio.marques@redknee.com
 *
 */
public class SubscriberBlackberryMsisdnAndPackageUpdateHome extends HomeProxy
{
    private static final String PROVISIONING_ERROR_MESSAGE = "Provisioning result " + BlackberrySupport.BLACKBERRY_PROVISION_ERRORCODE;
    
    /**
     * @param ctx
     * @param delegate
     */
    public SubscriberBlackberryMsisdnAndPackageUpdateHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }    
    
    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#create(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    public Object create(Context ctx, Object obj) throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[create].....");
    	return super.create(ctx, obj);
    }

    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */    
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	final Subscriber newSub = (Subscriber) obj;
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

        // Needs to swap only if subscriber has blackberry service, and was activated at RIM.
        if (newSub != null && oldSub!=null && subscriberHasActivatedBlackberryService(ctx, newSub))
        {
            // Update subscriber package and MSISDN if necessary
            if (!SafetyUtil.safeEquals(newSub.getPackageId(), oldSub.getPackageId()))
            {
                updateSubscriberIMSI(ctx, newSub, oldSub);
            } 
            
            /* The subscriber MSISDN should be updated at the RIM system in this point. However, 
             * whenever there is an MSISDN change, all the services are unprovisioned an  
             * reprovisioned (SubscriberProvisionServicesHome). Therefore, the user is reactivated  
             * at the RIM System through the Blackberry Provisioning Agent with the new MSISDN, 
             * and there is no need to update the MSISDN again.
             * Nov 19, 2008: Code was deleted through maintenance.  To implement handling of 
             * Msisdn Swap for BB Services check AppCrm revision 14074.
             */
        }
        
        return super.store(ctx, newSub);
    }
    
    /**
     * Verifies whether or not subscriber has a blackberry service provisioned, activated at RIM.
     * @param ctx Context object.
     * @param sub Subscriber object.
     * @return Value indicating whether or not subscriber has a blackberry service provisioned and activated at RIM.
     * @throws HomeException
     */
    private boolean subscriberHasActivatedBlackberryService(Context ctx, Subscriber subscriber) throws HomeException
    {
        return BlackberrySupport.subscriberHasBeenActivated(subscriber) && BlackberrySupport.subscriberHasBlackberryService(ctx, subscriber.getId());
    }
    
    
    
    /**
     * Updates the subscriber IMSI.
     * @param ctx Context object.
     * @param newSub New subscriber object.
     * @param oldSub Old subscriber object.
     */
    private void updateSubscriberIMSI(Context ctx, Subscriber newSub, Subscriber oldSub) throws HomeException
    {
        try
        {
            Service service = BlackberrySupport.getSubscriberBlackberryService(ctx, newSub.getId());
            
            // Modifying subscriber IMSI
            updateIMSI(ctx, service, newSub, oldSub);
            
            if (BlackberrySupport.subscriberHasSuspendedBlackberryService(ctx, newSub))
            {
                processSuspendedServices(ctx, service, newSub, oldSub);
            }
            
            /* TT 8111900059: In order to avoid HLR failures due to "The operation failed since 
             * the IMSI is under SIM replacement", we stop Sending the Service's Provision HLR Command. 
             * (Once IMSI on HLR is PENDING Swap state, all commands to update the HLR profile will 
             * fail with this error.) 
             * TODO: We should explore how to restore the implementation of this method found in 
             * AppCrm revision 14130.  I will be deleting all unused code to decrease code maintenance. */
            /*
            else  
            {  
                provisionBlackberryServiceonHLR(ctx, service, newSub);  
            }  
            */

            
        } 
        catch (ServiceBlackberryException e)
        {
            // Add provision exception
            String message = PROVISIONING_ERROR_MESSAGE + ": BlackBerry services for IMSI " + newSub.getIMSI() + "  not activated (" + retrieveBlackberryExceptionMessage(e) + ")";
            addProvisionException(ctx, message, newSub, oldSub, e);
        } 
        catch (HomeException e)
        {
            LogSupport.debug(ctx, this, "Error retrieving Blackberry service for subscription " + newSub.getId() + ": " + e.getMessage(), e);
            
            // Add provision exception
            String message = PROVISIONING_ERROR_MESSAGE + ": BlackBerry services for IMSI " + newSub.getIMSI() +  " not activated (Error retrieving service: " + e.getMessage() + ")";
            addProvisionException(ctx, message, newSub, oldSub, e);
        }

    }
    
    /**
     * Updates IMSI. 
     * @param ctx Context.
     * @param newSub Subscriber with new IMSI.
     * @param oldSub Subscriber with old IMSI.
     * @throws ServiceBlackberryException
     */
    private void updateIMSI(Context ctx, Service service, Subscriber newSub, Subscriber oldSub) throws ServiceBlackberryException, HomeException
    {
        LogSupport.info(ctx, this, "Swapping subscriber IMSI on RIM Provisioning System: " + 
                " SubscriberId=" + newSub.getId() + 
                ", IMSI=" + newSub.getIMSI() + 
                ", oldBillingId=" + oldSub.getIMSI());

        try
        {
            // Modifying subscriber IMSI
            Attributes attr =  BlackberrySupport.getBlackberryAttributesBasicAttributes(ctx, newSub);

            
            try
            {
                GSMPackage pkg = HomeSupportHelper.get(ctx).findBean(ctx, GSMPackage.class,
                        new EQ(GSMPackageXInfo.IMSI, oldSub.getIMSI()));
                if (pkg != null)
                {
                	if(!pkg.getSerialNo().equals(GSMPackage.DEFAULT_SERIALNO))
                	{
                		attr.setOldBillingId(pkg.getSerialNo());
                	}
                	else	// add old IMSI TT#TCBSUP-1072 issue
                	{
                		attr.setOldBillingId(oldSub.getIMSI());
                	}
                }
                else
                {
                    LogSupport.minor(ctx, this, "Update Blackberry Service. Unable to obtain SIM Package ["
                            + oldSub.getIMSI() + " ] for deactivateAttributes " + oldSub.getId());
                    throw new HomeException(" Unable to find SIM card package");
                }
            }
            catch (HomeException homeEx)
            {
                LogSupport.minor(ctx, this, "Update IMSI Blackberry Service. Unable to obtain SIM Package ["
                        + oldSub.getIMSI() + " ] for sub " + oldSub.getId());
                throw new HomeException(" Unable to find SIM card package");
            }
            

            modifyBlackberryService(ctx, attr, newSub);
            
        } 
        catch (ServiceBlackberryException swapException)
        {
            // Log ER
            BlackberrySupport.getErrorHandler().handleError(ctx, newSub, null, swapException.getResultStatus(), swapException.getErrorCode(), swapException.getDescription());            

            // If new IMSI is deactivated, deactivate old IMSI and activate the new one. Otherwise, throw the exception.
            if (swapException.getResultStatus().equals(ResultEnum.RIM_PROVISION_FAILURE) && 
                SafetyUtil.safeEquals(swapException.getErrorCode(), String.valueOf(RIMBlackBerryErrorCodes.NEW_BILLING_DEACTIVATED)))
            {
                swapToDeactivatedIMSI(ctx, service, newSub, oldSub, swapException);
            } 
            else
            {
                throw swapException;
            }
        }
    }
        
    /**
     * Swap IMSI to a deactivated IMSI
     * @param ctx Context.
     * @param newSub New subscriber.
     * @param oldSub Old subscriber.
     * @param thrownException Exception thrown if error occurs when trying to activate.
     * @throws ServiceBlackberryException
     */
    private void swapToDeactivatedIMSI(Context ctx, Service service, Subscriber newSub, Subscriber oldSub, ServiceBlackberryException thrownException) throws ServiceBlackberryException
    {
        LogSupport.info(ctx, this, "RIM Provisioning System (Blackberry) Modify command failed due to the new IMSI being deactivated." +
                " Trying to deactivate oldBillingId and activate new IMSI: " +
                " SubscriberId=" + newSub.getId() + 
                ", IMSI=" + newSub.getIMSI() + 
                ", oldBillingId=" + oldSub.getIMSI() + 
                ", ServiceId=" + service.getID() + " ('" + service.getName() + "')");

        IServiceBlackberry serviceBlackberryOldSub = ServiceBlackberryFactory.getServiceBlackberry(ctx, oldSub.getSpid());
        IServiceBlackberry serviceBlackberryNewSub = ServiceBlackberryFactory.getServiceBlackberry(ctx, newSub.getSpid());

        /*
         * TT 8111900059: In order to avoid HLR failures due to "The operation
         * failed since the IMSI is under SIM replacement", we stop using
         * RemoveBlackberryServiceUpdateAgent and BlackberryProvisionAgend and
         * deactive and active the provile on the RIM Provisioning System
         * directly. (Once IMSI on HLR is PENDING Swap state, all commands to
         * update the HLR profile will fail with this error.)
         */
        
        final CompoundIllegalStateException compound = new CompoundIllegalStateException();
        if(serviceBlackberryOldSub == null) 
        {
        	compound.thrown(
                    new IllegalPropertyArgumentException(
                        SubscriberXInfo.INTENT_TO_PROVISION_SERVICES, "Missing Blackberry provision configuration for SPID - " + oldSub.getSpid()));
        	compound.throwAll();
        }
        
        if(serviceBlackberryOldSub != null)
        {
        	 // Unprovisioning blackberry services for old IMSI.
            deactivateIMSI(ctx, service, oldSub, serviceBlackberryOldSub);
        }
       
        if(serviceBlackberryNewSub == null) 
        {
        	compound.thrown(
                    new IllegalPropertyArgumentException(
                        SubscriberXInfo.INTENT_TO_PROVISION_SERVICES, "Missing Blackberry provision configuration for SPID - " + newSub.getSpid()));
        	compound.throwAll();
        }
        
        if(serviceBlackberryNewSub != null)
        {
        	// Reprovisioning blackberry services for new IMSI.
            activateIMSI(ctx, service, newSub, serviceBlackberryNewSub, thrownException);
        }
    }
        
        
    /**
     * Process suspended blackberry services.
     * @param ctx Context.
     * @param newSub New subscriber.
     * @param oldSub Old subscriber.
     */
    private void processSuspendedServices(Context ctx, Service service, Subscriber newSub, Subscriber oldSub)
    {
        LogSupport.info(ctx, this, "New IMSI successfully activated. Trying to suspending the services on RIM Provisioning System: " + 
                " SubscriberId=" + newSub.getId() + 
                ", IMSI=" + newSub.getIMSI() + 
                ", oldBillingId=" + oldSub.getIMSI() + 
                ", ServiceId=" + service.getID() + " ('" + service.getName() + "')");

        try
        {
            LogSupport.debug(ctx, this, "Calling Blackberry Service Suspend command for IMSI " + newSub.getIMSI() +
                    " and service '" + service.getName() + "' (ServiceId = " + service.getID() + ").");
            
            /* TT 8111900059: In order to avoid HLR failures due to "The operation failed since 
             * the IMSI is under SIM replacement", we stop using SuspendBlackberryServiceUpdateAgent
             * and modify the profile on the RIM Provisioning System directly. (Once IMSI on HLR 
             * is PENDING Swap state, all commands to update the HLR profile will fail with this error.) 
             * TODO: We should explore how to restore the implementation of this method found in 
             * AppCrm revision 14130.  I will be deleting all unused code to decrease code maintenance. */
            Attributes attributes = BlackberrySupport.getBlackberryAttributesBasicAttributes(ctx, newSub);
            IServiceBlackberry serviceBlackberry = ServiceBlackberryFactory.getServiceBlackberry(ctx, newSub.getSpid());
            serviceBlackberry.suspend(ctx, newSub.getSpid(), BlackberrySupport.getBlackberryServicesIdsForService(ctx, service.getID()), attributes); 
        } 
        catch (ServiceBlackberryException e)
        {
            // Log ER
            BlackberrySupport.getErrorHandler().handleError(ctx, newSub, service, e.getResultStatus(), e.getErrorCode(), e.getDescription());            

            LogSupport.debug(ctx, this, "Error calling Blackberry Service Suspend command for IMSI "
                    + newSub.getIMSI() + ": " + retrieveBlackberryExceptionMessage(e), e);

            // Add provision exception
            String message = PROVISIONING_ERROR_MESSAGE + ": BlackBerry services for IMSI " + newSub.getIMSI() +  " not suspended (" + retrieveBlackberryExceptionMessage(e) + ")";
            addProvisionException(ctx, message, newSub, oldSub, e);
        }
        catch(Exception ex)
        {
            LogSupport.debug(ctx, this, "Error calling Blackberry Service Suspend command for IMSI "
                    + newSub.getIMSI() , ex);
            String message = PROVISIONING_ERROR_MESSAGE + ": BlackBerry services for IMSI " + newSub.getIMSI() ;

            addProvisionException(ctx, message, newSub, oldSub, ex);
    
        }            
        
    }

    
    /**
     * Activate IMSI for Blackberry services.
     * 
     * @param ctx Context object.
     * @param service Blackberry service.
     * @param newSub Subscriber with the new IMSI.
     * @param serviceBlackberry ServiceBlackberry object.
     * @param thrownException Exception thrown if error occurs when trying to
     *        activate.
     * @throws ServiceBlackberryException
     */
    private void activateIMSI(Context ctx, Service service, Subscriber newSub, IServiceBlackberry serviceBlackberry,
            ServiceBlackberryException thrownException) throws ServiceBlackberryException
    {
        try
        {
            LogSupport.debug(ctx, this, "Calling Blackberry Service Activate command for IMSI " + newSub.getIMSI()
                    + " and service '" + service.getName() + "' (ServiceId = " + service.getID() + ").");

            Attributes activateAttributes = new Attributes(newSub.getIMSI());
            serviceBlackberry.activate(ctx, newSub.getSpid(), BlackberrySupport.getBlackberryServicesIdsForService(ctx, service.getID()), activateAttributes);
        }
        catch (ServiceBlackberryException e)
        {
            // Log ER
            BlackberrySupport.getErrorHandler().handleError(ctx, newSub, service, e.getResultStatus(),
                    e.getErrorCode(), e.getDescription());

            LogSupport.debug(ctx, this, "Error calling Blackberry Service Activate command for IMSI "
                    + newSub.getIMSI() + ": " + retrieveBlackberryExceptionMessage(e), e);

            throw thrownException;
        }
    }
    
    
    /**
     * Deactivate IMSI for Blackberry services.
     * 
     * @param ctx Context object.
     * @param service Blackberry service.
     * @param oldSub Subscriber with the new IMSI.
     * @param serviceBlackberry ServiceBlackberry object.
     * @throws ServiceBlackberryException
     */
    private void deactivateIMSI(Context ctx, Service service, Subscriber oldSub, IServiceBlackberry serviceBlackberry)
    {
        try
        {
            LogSupport.debug(ctx, this, "Calling Blackberry Service Deactivate command for IMSI " + oldSub.getIMSI()
                    + " and service '" + service.getName() + "' (ServiceId = " + service.getID() + ").");

            Attributes deactivateAttributes = BlackberrySupport.getBlackberryAttributesBasicAttributes(ctx, oldSub);
            serviceBlackberry.deactivate(ctx, oldSub.getSpid(), BlackberrySupport.getBlackberryServicesIdsForService(ctx, service.getID()), deactivateAttributes);
        }        
        catch (ServiceBlackberryException e)
        {
            // Log ER
            BlackberrySupport.getErrorHandler().handleError(ctx, oldSub, service, e.getResultStatus(),
                    e.getErrorCode(), e.getDescription());

            LogSupport.debug(ctx, this, "Error calling Blackberry Service Deactivate command for IMSI "
                    + oldSub.getIMSI() + ": " + retrieveBlackberryExceptionMessage(e), e);
        }
        catch (Exception e)
        {
            LogSupport.minor(ctx, this, "Error calling Blackberry Service Deactivate command for IMSI "
                    + oldSub.getIMSI() , e);
        }
    }
    
    /**
     * Modifies a blackberry service.
     * @param ctx Context object.
     * @param attributes Attributes map.
     * @throws ServiceBlackberryException
     */
    private void modifyBlackberryService(Context ctx, Attributes attributes, Subscriber newSub) throws ServiceBlackberryException
    {
        String parameters = "IMSI = " + attributes.getImsi();
        
        if (attributes.getOldBillingId()!=null)
        {
            parameters +=", oldBillingId = " + attributes.getOldBillingId();
        }

        if (attributes.getMsisdn()!=null)
        {
            parameters +=", MSISDN = " + attributes.getMsisdn();
        }
        
        LogSupport.debug(ctx, this, "Calling Blackberry Service Modify command for " + parameters);
        
        try
        {
            IServiceBlackberry serviceBlackberry = ServiceBlackberryFactory.getServiceBlackberry(ctx, newSub.getSpid());
            serviceBlackberry.modify(ctx, newSub.getSpid(), new long[] {}, attributes);
        }
        catch (ServiceBlackberryException e)
        {
            LogSupport.debug(ctx, this, "Error calling Blackberry Service Modify command for " + parameters + ": " + 
                    retrieveBlackberryExceptionMessage(e), e);

            throw e;
        }
    }
 

    /**
     * Retrieves the message that should be displayed for the ServiceBlackberryException.
     * @param exception Thrown exception.
     * @return Message to be displayed.
     */
    private String retrieveBlackberryExceptionMessage(ServiceBlackberryException exception)
    {
        if (exception.getResultStatus().equals(ResultEnum.COMM_FAILURE))
        {
            return "Communication failure";
        } 
        else if (exception.getResultStatus().equals(ResultEnum.RIM_COMM_FAILURE) || 
            exception.getResultStatus().equals(ResultEnum.RIM_PROVISION_FAILURE))
        {
            return exception.getDescription();
        }
        else if (exception.getMessage()!=null)
        {
            return exception.getMessage();
        }
        else
        {
            return "Unknown error";
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
        SubscriberProvisionResultCode.setProvisionBlackberryErrorCode(ctx, BlackberrySupport.BLACKBERRY_PROVISION_ERRORCODE);

        ProvisioningHomeException provisionException = new ProvisioningHomeException(message, BlackberrySupport.BLACKBERRY_PROVISION_ERRORCODE);
        provisionException.initCause(e);
        SubscriberProvisionResultCode.addException(ctx, message, provisionException, oldSub, newSub);

        new MajorLogMsg(this, message, e).log(ctx);        
    }
}
