/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.client.alcatel;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.trilogy.app.crm.bean.AlcatelSSCConfig;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.service.ServiceProvisionActionEnum;
import com.trilogy.app.crm.client.xmlhttp.Response;
import com.trilogy.app.crm.client.xmlhttp.Transalator;
import com.trilogy.app.crm.client.xmlhttp.XMLTranslationConfiguration;
import com.trilogy.app.crm.client.xmlhttp.XMLTranslationConfigurationHome;
import com.trilogy.app.crm.client.xmlhttp.XMLTranslationConfigurationID;
import com.trilogy.app.crm.client.xmlhttp.XmlParsedXpathQueryDocument;
import com.trilogy.app.crm.hlr.RMIHLRProvisioningExternalService;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.KeyValueSupportHelper;
import com.trilogy.app.crm.util.Objects;
import com.trilogy.app.crm.util.StringUtil;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.interfaces.crm.hlr.CrmHlrResponse;
import com.trilogy.interfaces.crm.hlr.HLRProvisioningException;


/**
 * This interface will be installed for testing. Depending on the license state, the
 * relevant methods will return success (by way of no exception) or an error (by way of an
 * exception.
 * 
 * @author simar.singh@redknee.com
 * 
 */
public class AlcatelSSCProvisioningImpl implements AlcatelProvisioning
{
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void createService(Context ctx, Service service, Subscriber subscriber) throws AlcatelProvisioningException
    {
        LogSupport.debug(ctx, this, "Create Alcatel Account=" + service.getID() + "-" + service.getName()
                + " for subscriber=" + subscriber.getId());
        Transalator transaltor;
        final Objects input = getInputObjects(subscriber, service);
        String requestString;
        try
        {
            transaltor = getTranslator(ctx, service.getXmlProvSvcType(), ServiceProvisionActionEnum.PROVISION,
                    subscriber.getSpid());
            requestString = processRequest(ctx, transaltor.prepareRequest(ctx, input,String.class).getObject(String.class), input);
        }
        catch (Throwable e)
        {
            final String message = "Could not find Translator Service '" + service.getName()
                    + "' while creating subscription " + subscriber.getId() + ": " + e.getMessage();
            logException(ctx, message, e);
            if (e instanceof AlcatelProvisioningException)
            {
                throw (AlcatelProvisioningException) e;
            }
            else
            {
                throw new AlcatelProvisioningException("Error preparing request: " + e.getMessage(), ERROR_UNKNOWN_REQUEST);
            }
        }
        
        try
        {
            String responseString = executeAlcatelSSCOperation(ctx, requestString);
            // custom response
            Objects result = processResult(transaltor.handleResponse(ctx, new Objects().putObject(String.class, responseString), Response.class), Response.class);
            Response response = result.getObject(Response.class);
            if (null != response.getResultCode() && response.getResultCode().length() > 0)
            {
                if (ACT_NAME_NOT_UNIQUE.equals(response.getResultCode()))
                {
                    logException(ctx, "Alcatel-SSC system asserts that account already exists for SUBSCRIBER-ID [ "
                            + subscriber.getId()
                            + "]. Making an attempt resume the existing service; can not create a new one.", null);
                    resumeService(ctx, service, subscriber);
                }
                else
                {
                    throw new AlcatelProvisioningException( 
                            "Error received from external system - AlcatelSSC (Create-Service). Response ["
                                    + response.toString() + "]", ERROR_INVALID_RESPONSE_CODE, response.getResultCode(), response.getResultMessage());
                }
            }
          
        }
        catch (Throwable e)
        {
            final String message = "Could not create service '" + service.getName()
                    + "' for subscription '" + subscriber.getId() + "': " + e.getMessage();
            logException(ctx, message, e);
            generateAlarm(ctx, message, subscriber, service, e);
            if (e instanceof AlcatelProvisioningException)
            {
                throw (AlcatelProvisioningException) e;
            }
            else
            {
                throw new AlcatelProvisioningException(message, ERROR_UNKNOWN_RESPONSE, e);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAccount(Context ctx, Service service, Subscriber subscriber) throws AlcatelProvisioningException
    {
        LogSupport.debug(ctx, this, "Delete Alcatel Account=" + service.getID() + "-" + service.getName()
                + " for subscriber=" + subscriber.getId());
        Transalator transaltor;
        final Objects input = getInputObjects(subscriber, service);
        String requestString;
        try
        {
            transaltor = getTranslator(ctx, service.getXmlProvSvcType(), ServiceProvisionActionEnum.REMOVE, subscriber
                    .getSpid());
            requestString = processRequest(ctx, transaltor.prepareRequest(ctx, input,String.class).getObject(String.class), input);
        }
        catch (Throwable e1)
        {
            final String message = "Could not find Translator Service '" + service.getName()
                    + "' while removing subscription " + subscriber.getId() + ": " + e1.getMessage();
            logException(ctx, message, e1);
            if (e1 instanceof AlcatelProvisioningException)
            {
                throw (AlcatelProvisioningException) e1;
            }
            else
            {
                throw new AlcatelProvisioningException(message, ERROR_UNKNOWN_REQUEST);
            }
        }
        try
        {
            String responseString = executeAlcatelSSCOperation(ctx, requestString);
            Objects result = processResult(transaltor.handleResponse(ctx, new Objects().putObject(String.class, responseString), Response.class),
                    Response.class);
            Response response = result.getObject(Response.class);
            if (null != response.getResultCode() && response.getResultCode().length() > 0)
            {
                throw new AlcatelProvisioningException( 
                        "Error received from external system - Alcatel SSC. Response ["
                                + response.toString() + "]", ERROR_INVALID_RESPONSE_CODE, response.getResultCode(), response.getResultMessage());
            }
            
        }
        catch (Throwable e)
        {
            final String message = "Could not remove service '" + service.getName()
                    + "' for subscription '" + subscriber.getId() + "': " + e.getMessage();
            logException(ctx, message, e);
            generateAlarm(ctx, message, subscriber, service, e);
            if (e instanceof AlcatelProvisioningException)
            {
                throw (AlcatelProvisioningException) e;
            }
            else
            {
                throw new AlcatelProvisioningException(message, ERROR_UNKNOWN_RESPONSE, e);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public AccountUsage queryAccountUsage(Context ctx, Service service, Subscriber subscriber)
            throws AlcatelProvisioningException
    {
        Transalator transaltor;
        final Objects input = getInputObjects(subscriber, service);
        {
        }
        String requestString;
        try
        {
            transaltor = getTranslator(ctx, service.getXmlProvSvcType(), ServiceProvisionActionEnum.USAGE_QUERY,
                    subscriber.getSpid());
            requestString = processRequest(ctx, transaltor.prepareRequest(ctx, input,String.class).getObject(String.class), input);
        }
        catch (Throwable e)
        {
            final String message = "Could not find Translator Service '" + service.getName()
                    + "' while querying usage for subscription " + subscriber.getId() + ": " + e.getMessage();
            logException(ctx, message, e);
            if (e instanceof AlcatelProvisioningException)
            {
                throw (AlcatelProvisioningException) e;
            }
            else
            {
                throw new AlcatelProvisioningException("Error preparing request: " + e.getMessage(), ERROR_UNKNOWN_REQUEST);
            }
        }
        try
        {
            String responseString = executeAlcatelSSCOperation(ctx, requestString);
            Objects result = processResult(transaltor.handleResponse(ctx, new Objects().putObject(String.class, responseString), Response.class),
                    Response.class);
            Response response = result.getObject(Response.class);
            if (null != response.getResultCode() && response.getResultCode().length() > 0)
            {
                throw new AlcatelProvisioningException( 
                        "Error received from external system - Alcatel SSC. Response ["
                                + response.toString() + "]", ERROR_INVALID_RESPONSE_CODE, response.getResultCode(), response.getResultMessage());
            }
            else
            {
                return result.getObject(AccountUsage.class);
            }
        }
        catch (Throwable e)
        {
            final String message = "Could not query usage for service '" + service.getName()
                    + "' for subscription '" + subscriber.getId() + "': " + e.getMessage();
            logException(ctx, message, e);
            if (e instanceof AlcatelProvisioningException)
            {
                throw (AlcatelProvisioningException) e;
            }
            else
            {
                throw new AlcatelProvisioningException(message, ERROR_UNKNOWN_RESPONSE, e);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void removeService(Context ctx, Service service, Subscriber subscriber) throws AlcatelProvisioningException
    {
        LogSupport.debug(ctx, this, "Remove Alcatel Account=" + service.getID() + "-" + service.getName()
                + " for subscriber=" + subscriber.getId());
        Transalator transaltor;
        final Objects input = getInputObjects(subscriber, service);
        String requestString;
        try
        {
            transaltor = getTranslator(ctx, service.getXmlProvSvcType(), ServiceProvisionActionEnum.UNPROVISION,
                    subscriber.getSpid());
            requestString = processRequest(ctx, transaltor.prepareRequest(ctx, input,String.class).getObject(String.class), input);
        }
        catch (Throwable e)
        {
            final String message = "Could not find Translator Service '" + service.getName()
                    + "' while removing subscription " + subscriber.getId() + ": " + e.getMessage();
            logException(ctx, message, e);
            if (e instanceof AlcatelProvisioningException)
            {
                throw (AlcatelProvisioningException) e;
            }
            else
            {
                throw new AlcatelProvisioningException("Error preparing request: " + e.getMessage(), ERROR_UNKNOWN_REQUEST);
            }
        }
        try
        {
            String responseString = executeAlcatelSSCOperation(ctx, requestString);
            Objects result = processResult(transaltor.handleResponse(ctx, new Objects().putObject(String.class, responseString), Response.class),
                    Response.class);
            Response response = result.getObject(Response.class);
            if (null != response.getResultCode() && response.getResultCode().length() > 0)
            {
                throw new AlcatelProvisioningException( 
                        "Error received from external system - Alcatel SSC. Response ["
                                + response.toString() + "]", ERROR_INVALID_RESPONSE_CODE, response.getResultCode(), response.getResultMessage());
            }
        }
        catch (Throwable e)
        {
            final String message = "Could not remove service '" + service.getName()
                    + "' for subscription '" + subscriber.getId() + "': " + e.getMessage();
            logException(ctx, message, e);
            generateAlarm(ctx, message, subscriber, service, e);
            if (e instanceof AlcatelProvisioningException)
            {
                throw (AlcatelProvisioningException) e;
            }
            else
            {
                throw new AlcatelProvisioningException(message, ERROR_UNKNOWN_RESPONSE, e);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void resumeService(Context ctx, Service service, Subscriber subscriber) throws AlcatelProvisioningException
    {
        LogSupport.debug(ctx, this, "Resume Alcatel Account=" + service.getID() + "-" + service.getName()
                + " for subscriber=" + subscriber.getId());
        Transalator transaltor;
        final Objects input = getInputObjects(subscriber, service);
        String requestString;
        try
        {
            transaltor = getTranslator(ctx, service.getXmlProvSvcType(), ServiceProvisionActionEnum.RESUME, subscriber
                    .getSpid());
            requestString = processRequest(ctx, transaltor.prepareRequest(ctx, input,String.class).getObject(String.class), input);
        }
        catch (Throwable e)
        {
            final String message = "Could not find Translator Service '" + service.getName()
                    + "' while resuming subscription " + subscriber.getId() + ": " + e.getMessage();
            logException(ctx, message, e);
            if (e instanceof AlcatelProvisioningException)
            {
                throw (AlcatelProvisioningException) e;
            }
            else
            {
                throw new AlcatelProvisioningException("Error preparing request: " + e.getMessage(), ERROR_UNKNOWN_REQUEST);
            }
        }
        try
        {
            String responseString = executeAlcatelSSCOperation(ctx, requestString);
            // custom response
            Objects result = processResult(transaltor.handleResponse(ctx, new Objects().putObject(String.class, responseString), Response.class),
                    Response.class);
            Response response = result.getObject(Response.class);
            if (null != response.getResultCode() && response.getResultCode().length() > 0)
            {
                throw new AlcatelProvisioningException( 
                        "Error received from external system - Alcatel SSC. Response ["
                                + response.toString() + "]", ERROR_INVALID_RESPONSE_CODE, response.getResultCode(), response.getResultMessage());
            }
        }
        catch (Throwable e)
        {
            final String message = "Could not resume service '" + service.getName()
                    + "' for subscription '" + subscriber.getId() + "': " + e.getMessage();
            logException(ctx, message, e);
            generateAlarm(ctx, message, subscriber, service, e);
            if (e instanceof AlcatelProvisioningException)
            {
                throw (AlcatelProvisioningException) e;
            }
            else
            {
                throw new AlcatelProvisioningException(message, ERROR_UNKNOWN_RESPONSE, e);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void suspendService(Context ctx, Service service, Subscriber subscriber) throws AlcatelProvisioningException
    {
        LogSupport.debug(ctx, this, "Suspend Alcatel Account=" + service.getID() + "-" + service.getName()
                + " for subscriber=" + subscriber.getId());
        Transalator transaltor;
        final Objects input = getInputObjects(subscriber, service);
        String requestString;
        try
        {
            transaltor = getTranslator(ctx, service.getXmlProvSvcType(), ServiceProvisionActionEnum.SUSPEND, subscriber
                    .getSpid());
            requestString = processRequest(ctx, transaltor.prepareRequest(ctx, input,String.class).getObject(String.class), input);
        }
        catch (Throwable e)
        {
            final String message = "Could not find Translator Service '" + service.getName()
                    + "' while suspending subscription " + subscriber.getId() + ": " + e.getMessage();
            logException(ctx, message, e);
            if (e instanceof AlcatelProvisioningException)
            {
                throw (AlcatelProvisioningException) e;
            }
            else
            {
                throw new AlcatelProvisioningException("Error preparing request: " + e.getMessage(), ERROR_UNKNOWN_REQUEST);
            }
        }
        try
        {
            String responseString = executeAlcatelSSCOperation(ctx, requestString);
            // custom response
            Objects result = processResult(transaltor.handleResponse(ctx, new Objects().putObject(String.class, responseString), Response.class),
                    Response.class);
            Response response = result.getObject(Response.class);
            if (null != response.getResultCode() && response.getResultCode().length() > 0)
            {
                throw new AlcatelProvisioningException( 
                        "Error received from external system - Alcatel SSC. Response ["
                                + response.toString() + "]", ERROR_INVALID_RESPONSE_CODE, response.getResultCode(), response.getResultMessage());
            }
        }
        catch (Throwable e)
        {
            final String message = "Could not suspend service '" + service.getName()
                    + "' for subscription '" + subscriber.getId() + "': " + e.getMessage();
            logException(ctx, message, e);
            generateAlarm(ctx, message, subscriber, service, e);
            if (e instanceof AlcatelProvisioningException)
            {
                throw (AlcatelProvisioningException) e;
            }
            else
            {
                throw new AlcatelProvisioningException(message, ERROR_UNKNOWN_RESPONSE, e);
            }
        }
    }


    public void updateAccount(Context ctx, Service service, Subscriber subscriber) throws AlcatelProvisioningException
    {
        LogSupport.debug(ctx, this, "Update Alcatel Account=" + service.getID() + "-" + service.getName()
                + " for subscriber=" + subscriber.getId());
        Transalator transaltor;
        final Objects input = getInputObjects(subscriber, service);
        String requestString;
        try
        {
            transaltor = getTranslator(ctx, service.getXmlProvSvcType(), ServiceProvisionActionEnum.UPDATE_ATTRIBUTES,
                    subscriber.getSpid());
            requestString = processRequest(ctx, transaltor.prepareRequest(ctx, input,String.class).getObject(String.class), input);
        }
        catch (Throwable e)
        {
            final String message = "Could not find Translator Service '" + service.getName()
                    + "' while updating subscription " + subscriber.getId() + ": " + e.getMessage();
            logException(ctx, message, e);
            if (e instanceof AlcatelProvisioningException)
            {
                throw (AlcatelProvisioningException) e;
            }
            else
            {
                throw new AlcatelProvisioningException("Error preparing request: " + e.getMessage(), ERROR_UNKNOWN_REQUEST);
            }
        }
        try
        {
            String responseString = executeAlcatelSSCOperation(ctx, requestString);
            Objects result = processResult(transaltor.handleResponse(ctx, new Objects().putObject(String.class, responseString), Response.class),
                    Response.class);
            Response response = result.getObject(Response.class);
            if (null != response.getResultCode() && response.getResultCode().length() > 0)
            {
                throw new AlcatelProvisioningException( 
                        "Error received from external system - Alcatel SSC. Response ["
                                + response.toString() + "]", ERROR_INVALID_RESPONSE_CODE, response.getResultCode(), response.getResultMessage());
            }
        }
        catch (Throwable e)
        {
            final String message = "Could not update service '" + service.getName()
                    + "' for subscription '" + subscriber.getId() + "': " + e.getMessage();
            logException(ctx, message, e);
            generateAlarm(ctx, message, subscriber, service, e);
            if (e instanceof AlcatelProvisioningException)
            {
                throw (AlcatelProvisioningException) e;
            }
            else
            {
                throw new AlcatelProvisioningException(message, ERROR_UNKNOWN_RESPONSE, e);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void createAccount(Context ctx, Service service, Subscriber subscriber) throws AlcatelProvisioningException
    {
        LogSupport.debug(ctx, this, "Create Alcatel Account=" + service.getID() + "-" + service.getName()
                + " for subscriber=" + subscriber.getId());
        Transalator transaltor;
        final Objects input = getInputObjects(subscriber, service);
        String requestString;
        try
        {
            transaltor = getTranslator(ctx, service.getXmlProvSvcType(), ServiceProvisionActionEnum.CREATE, subscriber
                    .getSpid());
            requestString = processRequest(ctx, transaltor.prepareRequest(ctx, input,String.class).getObject(String.class), input);
        }
        catch (Throwable e)
        {
            final String message = "Could not find Translator Service '" + service.getName()
                    + "' while creating subscription " + subscriber.getId() + ": " + e.getMessage();
            logException(ctx, message, e);
            if (e instanceof AlcatelProvisioningException)
            {
                throw (AlcatelProvisioningException) e;
            }
            else
            {
                throw new AlcatelProvisioningException("Error preparing request: " + e.getMessage(), ERROR_UNKNOWN_REQUEST);
            }
        }
        try
        {
            String responseString = executeAlcatelSSCOperation(ctx, requestString);
            // custom response
            Objects result = processResult(transaltor.handleResponse(ctx, new Objects().putObject(String.class,
                    responseString), Response.class), Response.class);
            Response response = result.getObject(Response.class);
            
            
            if (null != response.getResultCode() && response.getResultCode().length() > 0)
            {
                if (ACT_NAME_NOT_UNIQUE.equals(response.getResultCode()))
                {
                    
                    final String message = "Alcatel account already exists for Subscriber [" + subscriber.getId() + "]";
                    logException(ctx, message, null);
                }
                else
                {
                    throw new AlcatelProvisioningException( 
                            "Error received from external system - Alcatel SSC. Response ["
                                    + response.toString() + "]", ERROR_INVALID_RESPONSE_CODE, response.getResultCode(), response.getResultMessage());
                }
            }
            else
            {
                throw new AlcatelProvisioningException( 
                        "Result-Object Type Unknown - Alcatel-Service. Failure [Got Response of unkonwn TYPE ("
                                + response + ")].", ERROR_INVALID_RESPONSE_OBJECT, null, response.getResultMessage());
            }
            
        }
        catch (Throwable e)
        {
            final String message = "Could not create service '" + service.getName()
                    + "' for subscription '" + subscriber.getId() + "': " + e.getMessage();
            logException(ctx, message, e);
            generateAlarm(ctx, message, subscriber, service, e);
            if (e instanceof AlcatelProvisioningException)
            {
                throw (AlcatelProvisioningException) e;
            }
            else
            {
                throw new AlcatelProvisioningException(message, ERROR_UNKNOWN_RESPONSE, e);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public AccountInfo getAccount(Context ctx, Service service, Subscriber subscriber) throws AlcatelProvisioningException
    {
        LogSupport.debug(ctx, this, "Get Alcatel Account=" + service.getID() + "-" + service.getName()
                + " for subscriber=" + subscriber.getId());
        Transalator transaltor;
        final Objects input = getInputObjects(subscriber, service);
        String requestString;
        try
        {
            transaltor = getTranslator(ctx, service.getXmlProvSvcType(), ServiceProvisionActionEnum.GET, subscriber
                    .getSpid());
            requestString = processRequest(ctx, transaltor.prepareRequest(ctx, input,String.class).getObject(String.class), input);
        }
        catch (Throwable e)
        {
            final String message = "Could not find Translator Service '" + service.getName()
                    + "' while querying subscription " + subscriber.getId() + ": " + e.getMessage();
           logException(ctx, message, e);
           if (e instanceof AlcatelProvisioningException)
           {
               throw (AlcatelProvisioningException) e;
           }
           else
           {
               throw new AlcatelProvisioningException("Error preparing request: " + e.getMessage(), ERROR_UNKNOWN_REQUEST);
           }
        }
        try
        {
            String responseString = executeAlcatelSSCOperation(ctx, requestString);
            Objects result = processResult(transaltor.handleResponse(ctx, new Objects().putObject(String.class, responseString), Response.class,
                    AccountInfo.class), Response.class, AccountInfo.class);
            Response response = result.getObject(Response.class);
            if (null != response.getResultCode() && response.getResultCode().length() > 0)
            {
                throw new AlcatelProvisioningException( 
                        "Error received from external system - Alcatel SSC. Response ["
                                + response.toString() + "]", ERROR_INVALID_RESPONSE_CODE, response.getResultCode(), response.getResultMessage());
            }
            else if (result.hasObject(AccountInfo.class))
            {
                return result.getObject(AccountInfo.class);
            }
            else
            {
                throw new AlcatelProvisioningException( 
                        "Result-Object Type Unknown - Alcatel-Service. Failure [Got Response of unkonwn TYPE ("
                                + response + ")].", ERROR_INVALID_RESPONSE_OBJECT, null, response.getResultMessage());
            }
        }
        catch (Throwable e)
        {
            final String message = "Could not query service '" + service.getName()
                    + "' for subscription '" + subscriber.getId() + "': " + e.getMessage();
            logException(ctx, message, e);
            if (e instanceof AlcatelProvisioningException)
            {
                throw (AlcatelProvisioningException) e;
            }
            else
            {
                throw new AlcatelProvisioningException(message, ERROR_UNKNOWN_RESPONSE, e);
            }
        }
    }


    private Transalator getTranslator(Context ctx, long provisionableServiceType, ServiceProvisionActionEnum action,
            int spid) throws AlcatelProvisioningException
    {
        Home home = (Home) ctx.get(XMLTranslationConfigurationHome.class);
        final XMLTranslationConfigurationID configID = new XMLTranslationConfigurationID(provisionableServiceType,
                action, spid);
        try
        {
            final XMLTranslationConfiguration config = (XMLTranslationConfiguration) home.find(ctx, configID);
            if (null == config)
            {
                throw new AlcatelProvisioningException("Could not find XML Translator with ID[" + configID.toString() + "]", ERROR_TRANSLATOR_NOT_FOUND);
            }

            return config.getTranslator(ctx);
        }
        catch (HomeException e)
        {
            throw new AlcatelProvisioningException(e.getMessage(), ERROR_DATABASE_ERROR);
        }
    }


    private String executeAlcatelSSCOperation(Context ctx, String request) throws AlcatelProvisioningException
    {
        final RMIHLRProvisioningExternalService hlrClient = (RMIHLRProvisioningExternalService) ctx
                .get(RMIHLRProvisioningExternalService.class);
        
        if (hlrClient==null)
        {
            throw new AlcatelProvisioningException(
                    "Alcatel SSC service not available", ExternalAppSupport.NO_CONNECTION);
        }

        try
        {
            CrmHlrResponse task = hlrClient.provision(getHlrId(ctx), request);
    
            if (task.getCrmHlrCode() == 0 )
            {
                    // Success
                    return task.getRawHlrData();
             }
             else
             { // response with error
                    throw new AlcatelProvisioningException(
                            "General failure to provision service to Alcatel SSC (Error code = "
                                    + task.getDriverHlrCode() + ")", ERROR_HLR, String.valueOf(task.getDriverHlrCode()), task.getMessage());
             }
        }
        catch (HLRProvisioningException t)
        {
            throw new AlcatelProvisioningException(
                    "General failure to provision service to Alcatel SSC: " + t.getMessage(), ERROR_HLR, String.valueOf("-1"), t.getMessage());
        }
        
        
    }


    private String getHlrId(Context ctx)
    {
        AlcatelSSCConfig alcatelConfig = (AlcatelSSCConfig) ctx.get(AlcatelSSCConfig.class);
        if (null != alcatelConfig)
        {
            return alcatelConfig.getHlrID();
        }
        return AlcatelSSCConfig.DEFAULT_HLRID;
    }



    /**
     * 
     * @param ctx
     * @param errorMessage
     *            - Error Message; Cannot be null
     * @param t
     *            - Error thrown or null if no error
     */
    protected void logException(Context ctx, final String errorMessage, final Throwable t)
    {
        new DebugLogMsg(this, errorMessage, t).log(ctx);
        new MinorLogMsg(this, errorMessage, null).log(ctx);
        final ExceptionListener el = (ExceptionListener) ctx.get(ExceptionListener.class);
        if (null != el)
        {
            el.thrown(new IllegalStateException(errorMessage, t));
        }
    }


    /**
     * Generates Entry-Log (Alarm) for an un-toward event.
     * 
     * @param ctx
     * @param message
     * @param subscriber
     * @param service
     * @param t
     */
    protected void generateAlarm(Context ctx, String message, Subscriber subscriber, Service service, Throwable t)
    {
        new EntryLogMsg(ALARM_ID, this, ALARM_COMPONENT, message, (new String[]
            {subscriber.getId(), String.valueOf(service.getID())}), t).log(ctx);
    }


    /**
     * @param ctx
     * @param request
     * @param service_
     * @param subscriber_
     * @return
     * @throws HomeException
     */
    private String processRequest(Context ctx, String request, Objects input)
    {
        ctx = ctx.createSubContext();
        ctx.put(Service.class, input.getObject(Service.class));
        ctx.put(Subscriber.class, input.getObject(Subscriber.class));
        KeyValueSupportHelper.get(ctx).getAlcatelSSCSubscriptionKeyValueMap(ctx);
        return StringUtil.replaceAll(request, KeyValueSupportHelper.get(ctx).getAlcatelSSCSubscriptionKeyValueMap(ctx));
    }


    private Objects getInputObjects(Subscriber subscriber, Service service)
    {
        return new Objects().putObject(Subscriber.class, subscriber).putObject(Service.class, service);
    }


    private Objects processResult(Objects result, Class<?> responseClass, Class<?>... resultClasses)
            throws AlcatelProvisioningException
    {
        if (!result.hasObject(responseClass))
        {
            if (!result.hasObject(String.class))
            {
                throw new AlcatelProvisioningException("No redeable text/response received from external system.", ERROR_UNREADABLE_DATA);
            }
            else
            {
                try
                {
                    return generateResultFromString(result.getObject(String.class), responseClass, resultClasses);
                }
                catch (Throwable t)
                {
                    throw new AlcatelProvisioningException("Malformated text/response received from external system.", ERROR_UNREADABLE_DATA, t);
                }
            }
        }
        return result;
    }


    private Objects generateResultFromString(String resultString, Class<?> responseClass, Class<?>... resultClasses)
            throws AgentException, XPathExpressionException, ParserConfigurationException, SAXException, IOException
    {
        final Objects result = new Objects();
        if (!responseClass.isAssignableFrom(Response.class))
        {
            throw new AgentException("Invalid Expectation of Response [" + responseClass.getName() + " ] ");
        }
        else
        {
            final AlcatelResponseParseXml alcatelResponseParseXml = new AlcatelResponseParseXml(resultString);
            result.putObject(Response.class, alcatelResponseParseXml.getResponse());
            if (null != resultClasses && resultClasses.length > 0)
            {
                for (Class<?> resultClass : resultClasses)
                {
                    if (!result.hasObject(resultClass))
                    {
                        if (AccountInfo.class.equals(resultClass))
                        {
                            AccountInfo acctInfo = new AccountInfo();
                            acctInfo.setAccountId(alcatelResponseParseXml.getAccountID());
                            result.putObject(AccountInfo.class, acctInfo);
                        }
                        else if (XmlParsedXpathQueryDocument.class.equals(resultClass))
                        {
                            result.putObject(XmlParsedXpathQueryDocument.class, alcatelResponseParseXml);
                        }
                        else if (AlcatelResponseParseXml.class.equals(resultClass))
                        {
                            result.putObject(AlcatelResponseParseXml.class, alcatelResponseParseXml);
                        }
                        else
                        {
                            throw new AgentException("Invalid Expectation of Result [" + resultClass.getName() + " ] ");
                        }
                    }
                }
            }
        }
        return result;
    }
    
    // errors from Alcatel we handle
    public static final String ACT_NAME_NOT_UNIQUE = "ACT-00001"; // Account name is not
    public static final String ACT_NOT_FOUND = "ACT-00015"; // Account does not exist
    public static final String NO_ERROR = "";
    final public static long ALARM_ID = 13791;
    final public static String ALARM_COMPONENT = "ALCATEL-SSC";
}
