/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.client;

import org.omg.CORBA.IntHolder;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.homezone.DiscountVO;
import com.trilogy.app.crm.homezone.LocationMappingVO;
import com.trilogy.app.crm.homezone.SubscriberHomezoneVO;
import com.trilogy.app.crm.support.CorbaSupportHelper;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.app.homezone.corba.ErrorCode;
import com.trilogy.app.homezone.corba.HomezoneProvision;
import com.trilogy.app.homezone.corba.HomezoneProvisionHelper;
import com.trilogy.app.homezone.corba.LocationMappingInfo;
import com.trilogy.app.homezone.corba.LocationMappingInfoHolder;
import com.trilogy.app.homezone.corba.SubscriberHomezoneInfo;
import com.trilogy.app.homezone.corba.HomezoneProvisionPackage.SubscriberHomezoneInfoSetHolder;
import com.trilogy.service.corba.CorbaClientProperty;
import com.trilogy.service.corba.CorbaClientPropertyHome;
import com.trilogy.util.corba.ConnectionListener;
import com.trilogy.util.corba.ConnectionUpException;
import com.trilogy.util.corba.CorbaClientException;
import com.trilogy.util.corba.CorbaClientProxy;

/**
 * @author pkulkarni
 * 
 * CORBA client for Homezone provisioning
 *  
 */
public class AppHomezoneClient extends ContextAwareSupport implements
        RemoteServiceStatus, ConnectionListener
{

    private static final String SERVICE_DESCRIPTION = "CORBA client for Homezone services";

    public AppHomezoneClient(Context ctx)
    {
        setContext(ctx);
        init();
    }

    private void init() throws IllegalArgumentException
    {
        Home corbaClientPropertyHome = null;
        corbaClientPropertyHome = (Home) getContext().get(
                CorbaClientPropertyHome.class);
        if (corbaClientPropertyHome == null)
        {
            throw new IllegalArgumentException(
                    "Corba client configuration does not exist");
        }
        try
        {
            homezoneProperty_ = (CorbaClientProperty) corbaClientPropertyHome
                    .find(getContext(), corbaClientPropName_);
            if (homezoneProperty_ == null)
            {
                throw new IllegalArgumentException("Corba client <"
                        + corbaClientPropName_
                        + "> configuration does not exist");
            }
            new InfoLogMsg(this, homezoneProperty_.toString(), null)
                    .log(getContext());
            service_ = null;
            homezoneProvProxy_ = CorbaSupportHelper.get(getContext()).createProxy(getContext(),
                    homezoneProperty_, this);
        }
        catch (Exception e)
        {
            connectionDown();
            if (homezoneProperty_ != null)
            {
                // snmp external link down trap
                throw new IllegalArgumentException(
                        homezoneProperty_.toString()
                                + " Host:"
                                + homezoneProperty_.getNameServiceHost()
                                + "Port:"
                                + String.valueOf(homezoneProperty_
                                        .getNameServicePort()));
            }
            throw new IllegalArgumentException(
                    "Unable to load corba proxy for ProductS5600IpcgSubProvClient");
        }
    }

    /**
     * Gets the object that proviced access service to the CORBA connection.
     * 
     * @return The object that proviced access HomezoneProvision service to the
     *         CORBA connection.
     */
    private synchronized HomezoneProvision getService()
    {
        org.omg.CORBA.Object objServant = null;
        if (service_ != null)
        {
            return service_;
        }
        if (homezoneProvProxy_ == null)
        {
            if (LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(
                        this,
                        "homezoneProvProxy_ is null,trying to create the proxy",
                        null).log(getContext());
            }
            try
            {
                homezoneProvProxy_ = CorbaSupportHelper.get(getContext()).createProxy(getContext(),
                        homezoneProperty_, this);
            }
            catch (CorbaClientException ccEx)
            {
                invalidate();
                return null;
            }
        }
        objServant = homezoneProvProxy_.instance();
        if (LogSupport.isDebugEnabled(getContext()) && objServant == null)
        {
            new DebugLogMsg(this, "objServant is null", null).log(getContext());
        }
        if (objServant != null)
        {
            try
            {
                // attempt to derive HomezoneProvision
                service_ = HomezoneProvisionHelper.narrow(objServant);
                if (LogSupport.isDebugEnabled(getContext()) && service_ == null)
                {
                    new DebugLogMsg(this, "service_ is null after narrowing",
                            null).log(getContext());
                }
                return service_;
            }
            catch (Exception e)
            {
                invalidate();
                return null;
            }
        }
        invalidate();
        return null;
    }

    /**
     * Calls
     * 
     * @see com.redknee.util.corba.CorbaClientProxy#invalidate() and raises
     *      connectionDown alarm
     */
    public synchronized void invalidate()
    {
        connectionDown();
        if (homezoneProvProxy_ != null)
        {
            homezoneProvProxy_.invalidate();
            homezoneProvProxy_ = null;
        }
    }

    /**
     * @see com.redknee.app.crm.client.ExternalService#getDescription()
     */
    public String getDescription()
    {
        return SERVICE_DESCRIPTION;
    }

    /**
     * @see com.redknee.app.crm.client.ExternalService#getName()
     */
    public String getName()
    {
        if (homezoneProperty_ != null)
        {
            return homezoneProperty_.getKey();
        }
        return corbaClientPropName_;
    }

    /**
     * @see com.redknee.app.crm.client.ExternalService#isAlive()
     */
    public boolean isAlive()
    {
        return bConnected;
    }

    /**
     * @see com.redknee.util.corba.ConnectionListener#connectionDown()
     */
    public void connectionDown()
    {
        bConnected = false;
        service_ = null;
        if (homezoneProperty_ != null)
        {
            if (!downTrapSent)
            {
                //send alarm after configuring one
                //new EntryLogMsg(10340, this, "",
                // homezoneProperty_.toString(), new
                // String[]{homezoneProperty_.getNameServiceHost(),
                // String.valueOf(homezoneProperty_.getNameServicePort())},null).log(getContext());
                downTrapSent = true;
                upTrapSent = false;
            }
        }
    }

    /**
     * @see com.redknee.util.corba.ConnectionListener#connectionUp()
     */
    public void connectionUp() throws ConnectionUpException
    {
        bConnected = true;
        if (!upTrapSent)
        {
            //send alarm after configuring one
            // snmp external link up trap
            //new EntryLogMsg(10341, this, "", homezoneProperty_.toString(),
            // new String[]
            // {homezoneProperty_.getNameServiceHost(),
            // String.valueOf(homezoneProperty_.getNameServicePort())},
            // null).log(getContext());
            downTrapSent = false;
            upTrapSent = true;
        }
    }

    /* Homezone provisioning methods */
    /**
     * Provisions subscriber in Homezone application with provided homezones
     * 
     * @param MSISDN
     * @param SubscriberHomezoneInfo[]...contains
     *            all the homezones for the given subscriber
     * @return the resultcode returned by CORBA call
     *  
     */
    public int createSubscriberHomezone(String mainMSISDN,
            SubscriberHomezoneInfo[] subscriberHomezoneInfo)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE,
                "createSubscriberHomezone()");
        int result = ErrorCode.SUCCESS;
        HomezoneProvision service = getService();
        if (service == null)
        {
            new DebugLogMsg(this, "Cannot get SubScriberProvision service.",
                    null).log(getContext());
            return -1;
        }
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(
                    this,
                    "Going to call createSubscriberHomezone, Parameters passed",
                    null).log(getContext());
            new DebugLogMsg(this, "mainMSISDN:" + mainMSISDN, null)
                    .log(getContext());
            for (int i = 0; i < subscriberHomezoneInfo.length; i++)
            {
                new DebugLogMsg(this, "subscriberHomezoneInfo[" + i + "]::"
                        + "msisdn:" + subscriberHomezoneInfo[i].msisdn
                        + ",zoneId:" + subscriberHomezoneInfo[i].zoneId
                        + ",priority:" + subscriberHomezoneInfo[i].priority
                        + ",x:" + subscriberHomezoneInfo[i].x + ",y:"
                        + subscriberHomezoneInfo[i].y + ",r:"
                        + subscriberHomezoneInfo[i].r, null).log(getContext());
            }
        }
        result = service.createSubscriberHomezone(mainMSISDN,
                subscriberHomezoneInfo);
        putResultCodeDebug(result, "createSubscriberHomezone");
        pmLogMsg.log(getContext());
        return result;
    }

    /**
     * Modifies the passed homezone in the Homezone application
     * 
     * @param subscriberHomezone
     *            to be modified
     * 
     * @return the resultcode returned by CORBA call
     *  
     */
    public int modifySubscriberHomezone(
            SubscriberHomezoneInfo subscriberHomezoneInfo)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE,
                "modifySubscriberHomezone()");
        int result = ErrorCode.SUCCESS;
        HomezoneProvision service = getService();
        if (service == null)
        {
            new DebugLogMsg(this, "Cannot get SubScriberProvision service.",
                    null).log(getContext());
            return -1;
        }
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(
                    this,
                    "Going to call modifySubscriberHomezone, Parameters passed, subscriberHomezoneInfo details ::+"
                            + "msisdn:"
                            + subscriberHomezoneInfo.msisdn
                            + ",zoneId:"
                            + subscriberHomezoneInfo.zoneId
                            + ",priority:"
                            + subscriberHomezoneInfo.priority
                            + ",x:"
                            + subscriberHomezoneInfo.x
                            + ",y:"
                            + subscriberHomezoneInfo.y
                            + ",r:"
                            + subscriberHomezoneInfo.r, null).log(getContext());
        }
        result = service.modifySubscriberHomezone(subscriberHomezoneInfo);
        putResultCodeDebug(result, "modifySubscriberHomezone");
        pmLogMsg.log(getContext());
        return result;
    }

    /**
     * Deletes the homezone for the passed MSISDN from Homezone application
     * 
     * @param MSISDN
     *            of the subscriber to be deleted
     * @param the
     *            zoneID
     * 
     * @return the resultcode returned by CORBA call
     *  
     */
    public int deleteSubscriberHomezone(String MSISDN, int zoneId)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE,
                "deleteSubscriberHomezone()");
        int result = ErrorCode.SUCCESS;
        HomezoneProvision service = getService();
        if (service == null)
        {
            new DebugLogMsg(this, "Cannot get SubScriberProvision service.",
                    null).log(getContext());
            return -1;
        }
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(this,
                    "Going to call deleteSubscriberHomezone, Parameters passed,msisdn:"
                            + MSISDN + ",zoneId:" + zoneId, null)
                    .log(getContext());
        }
        result = service.deleteSubscriberHomezone(MSISDN, zoneId);
        putResultCodeDebug(result, "deleteSubscriberHomezone");
        pmLogMsg.log(getContext());
        return result;
    }

    /**
     * Queries to Homezone application for the given MSISDN
     * 
     * @param MSISDN
     *            to be queried
     * @return the SubscriberHomezoneVO which contains SubscriberHomezoneInfo[]
     *         and resultcode returned by CORBA call
     *  
     */
    public SubscriberHomezoneVO querySubscriberHomezone(String mainMSISDN)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE,
                "querySubscriberHomezone()");
        SubscriberHomezoneInfo[] subscriberHomezoneInfos = null;
        int result = ErrorCode.SUCCESS;
        SubscriberHomezoneInfoSetHolder subHomezoneInfoSetHolder = new SubscriberHomezoneInfoSetHolder();
        HomezoneProvision service = getService();
        if (service == null)
        {
            new DebugLogMsg(this, "Cannot get SubScriberProvision service.",
                    null).log(getContext());
            return null;
        }
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(this,
                    "Going to call querySubscriberHomezone, Parameters passed::mainMSISDN:"
                            + mainMSISDN, null).log(getContext());
        }
        result = service.querySubscriberHomezone(mainMSISDN,
                subHomezoneInfoSetHolder);
        String message = putResultCodeDebug(result, "querySubscriberHomezone");
        subscriberHomezoneInfos = subHomezoneInfoSetHolder.value;
        if (subscriberHomezoneInfos != null)
        {
            if (LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(this,
                        "The subscriberHomezoneInfos returned are:", null)
                        .log(getContext());
                for (int i = 0; i < subscriberHomezoneInfos.length; i++)
                {
                    new DebugLogMsg(this, "subscriberHomezoneInfo[" + i + "]::"
                            + "msisdn:" + subscriberHomezoneInfos[i].msisdn
                            + ",zoneId:" + subscriberHomezoneInfos[i].zoneId
                            + ",priority:"
                            + subscriberHomezoneInfos[i].priority + ",x:"
                            + subscriberHomezoneInfos[i].x + ",y:"
                            + subscriberHomezoneInfos[i].y + ",r:"
                            + subscriberHomezoneInfos[i].r, null)
                            .log(getContext());
                }
            }
        }
        pmLogMsg.log(getContext());
        return new SubscriberHomezoneVO(subscriberHomezoneInfos, result,
                message);
    }

    /**
     * Modifies the homezone's radius with the value passed
     * 
     * @param zoneId
     *            of the Homezone to be modified
     * @param newRadius
     *            value
     * 
     * @return the resultcode returned by CORBA call
     *  
     */
    public int modifyHomezoneRadius(int zoneId, int newRadius)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE,
                "modifyHomezoneRadius()");
        int result = ErrorCode.SUCCESS;
        HomezoneProvision service = getService();
        if (service == null)
        {
            new DebugLogMsg(this, "Cannot get SubScriberProvision service.",
                    null).log(getContext());
            return -1;
        }
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(this,
                    "Going to call modifyHomezoneRadius, Parameters passed::zoneId:"
                            + zoneId + "newRadius" + newRadius, null)
                    .log(getContext());
        }
        result = service.modifyHomezoneRadius(zoneId, newRadius);
        putResultCodeDebug(result, "modifyHomezoneRadius");
        pmLogMsg.log(getContext());
        return result;
    }

    /*
     * LocationMapping provisioning- Only queryLocationMapping is implemented
     * now create,modify,delete r not implemeted
     */
    /**
     * Queries to Homezone application for the x and y values for the passed
     * cellId(LocationId)
     * 
     * @param cellId
     *            to be queried
     * @return the LocationMappingVO which holds LocationMappingInfo and the
     *         resultcode returned by CORBA call
     *  
     */
    public LocationMappingVO queryLocationMapping(String cellId)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE,
                "queryLocationMapping()");
        LocationMappingInfo locationMappingInfo = null;
        int result = ErrorCode.SUCCESS;
        LocationMappingInfoHolder locationMappingInfoHolder = new LocationMappingInfoHolder();
        HomezoneProvision service = getService();
        if (service == null)
        {
            new DebugLogMsg(this, "Cannot get SubScriberProvision service.",
                    null).log(getContext());
            return null;
        }
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(this,
                    "Going to call queryLocationMapping, Parameters passed::cellId:"
                            + cellId, null).log(getContext());
        }
        result = service
                .queryLocationMapping(cellId, locationMappingInfoHolder);
        String message = putResultCodeDebug(result, "queryLocationMapping");
        locationMappingInfo = locationMappingInfoHolder.value;
        if (locationMappingInfo != null)
        {
            if (LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(this, "The locationMappingInfo returned is:",
                        null).log(getContext());
                new DebugLogMsg(this,
                        "locationMappingInfo::" + "LocationId:"
                                + locationMappingInfo.locationId + ",x:"
                                + locationMappingInfo.x + ",y:"
                                + locationMappingInfo.y, null)
                        .log(getContext());
            }
        }
        pmLogMsg.log(getContext());
        return new LocationMappingVO(locationMappingInfo, result, message);
    }

    /*
     * HomezoneDiscount provisioning- Not sure about these, whethere these will
     * be used or not, but for discount provisioning any of these methods will
     * be used
     */
    /**
     * Provisions discount in Homezone application for passed zoneId
     * 
     * @param zoneId-the
     *            identifier of the Homezone
     * @param rate-discount
     * @return the resultcode returned by CORBA call
     *  
     */
    public int createHomezoneDiscount(int zoneId, int rate)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE,
                "createHomezoneDiscount()");
        int result = ErrorCode.SUCCESS;
        HomezoneProvision service = getService();
        if (service == null)
        {
            new DebugLogMsg(this, "Cannot get SubScriberProvision service.",
                    null).log(getContext());
            return -1;
        }
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(this,
                    "Going to call createHomezoneDiscount, Parameters passed, zoneId:"
                            + zoneId + ",rate:" + rate, null).log(getContext());
        }
        result = service.createHomezoneDiscount(zoneId, rate);
        putResultCodeDebug(result, "createHomezoneDiscount");
        pmLogMsg.log(getContext());
        return result;
    }

    /**
     * Modifies the discount in Homezone application for passed zoneId
     * 
     * @param zoneId-the
     *            identifier of the Homezone
     * @param rate-
     *            new discount
     * @return the resultcode returned by CORBA call
     *  
     */
    public int modifyHomezoneDiscount(int zoneId, int rate)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE,
                "modifyHomezoneDiscount()");
        int result = ErrorCode.SUCCESS;
        HomezoneProvision service = getService();
        if (service == null)
        {
            new DebugLogMsg(this, "Cannot get SubScriberProvision service.",
                    null).log(getContext());
            return -1;
        }
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(this,
                    "Going to call modifyHomezoneDiscount, Parameters passed, zoneId:"
                            + zoneId + ",rate:" + rate, null).log(getContext());
        }
        result = service.modifyHomezoneDiscount(zoneId, rate);
        putResultCodeDebug(result, "modifyHomezoneDiscount");
        pmLogMsg.log(getContext());
        return result;
    }

    /**
     * Deletes the discount associated withe the homezone for passed zoneId
     * 
     * @param zoneId-the
     *            identifier of the Homezone
     * 
     * @return the resultcode returned by CORBA call
     *  
     */
    public int deleteHomezoneDiscount(int zoneId)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE,
                "deleteHomezoneDiscount()");
        int result = ErrorCode.SUCCESS;
        HomezoneProvision service = getService();
        if (service == null)
        {
            new DebugLogMsg(this, "Cannot get SubScriberProvision service.",
                    null).log(getContext());
            return -1;
        }
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(this,
                    "Going to call deleteHomezoneDiscount, Parameters passed, zoneId:"
                            + zoneId, null).log(getContext());
        }
        result = service.deleteHomezoneDiscount(zoneId);
        putResultCodeDebug(result, "deleteHomezoneDiscount");
        pmLogMsg.log(getContext());
        return result;
    }

    /**
     * Queries the discount associated with the homezone for passed zoneId
     * 
     * @param zoneId-the
     *            identifier of the Homezone
     * 
     * @return the DiscountVO...the rate and the resultcode by corba call
     *  
     */
    public DiscountVO queryHomezoneDiscount(int zoneId)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE,
                "queryHomezoneDiscount()");
        int result = ErrorCode.SUCCESS;
        int rate = 0;
        IntHolder rateHolder = new IntHolder();
        HomezoneProvision service = getService();
        if (service == null)
        {
            new DebugLogMsg(this, "Cannot get SubScriberProvision service.",
                    null).log(getContext());
            return null;
        }
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(this,
                    "Going to call queryHomezoneDiscount, Parameters passed, zoneId:"
                            + zoneId, null).log(getContext());
        }
        result = service.queryHomezoneDiscount(zoneId, rateHolder);
        String message = putResultCodeDebug(result, "queryHomezoneDiscount");
        if (result == ErrorCode.SUCCESS)
        {
            rate = rateHolder.value;
            if (LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(this, "The new discount returned is:" + result,
                        null).log(getContext());
            }
        }
        pmLogMsg.log(getContext());
        return new DiscountVO(rate, result, message);
    }

    /**
     * Puts debug log for the displaying the resulcode description
     * 
     * @param resultcode
     * @param methodName
     *            from which putResultCodeDebug is called
     * @return the message accortding to the resultcode
     */
    public String putResultCodeDebug(int resultcode, String methodName)
    {
        String message = null;
        switch (resultcode)
        {
            case ErrorCode.SUCCESS:
                message = "SUCCESS";
                break;
            case ErrorCode.SQL_ERROR:
                message = "SQL_ERROR";
                break;
            case ErrorCode.INTERNAL_ERROR:
                message = "INTERNAL_ERROR";
                break;
            case ErrorCode.MANDATORY_INFO_MISSING:
                message = "MANDATORY_INFO_MISSING";
                break;
            case ErrorCode.INVALID_PARAMETER:
                message = "INVALID_PARAMETER";
                break;
            case ErrorCode.UPDATE_NOT_ALLOWED:
                message = "UPDATE_NOT_ALLOWED";
                break;
            case ErrorCode.ENTRY_ALREADY_EXISTED:
                message = "ENTRY_ALREADY_EXISTED";
                break;
            case ErrorCode.ENTRY_NOT_FOUND:
                message = "ENTRY_NOT_FOUND";
                break;
            case ErrorCode.INVALID_HOMEZONE_PRIORITY:
                message = "INVALID_HOMEZONE_PRIORITY";
                break;
            case ErrorCode.HOMEZONE_PRIORITY_ALREADY_EXISTED:
                message = "HOMEZONE_PRIORITY_ALREADY_EXISTED";
                break;
            default:
                message = "ERROR_UNKNOWN";
                break;
        }
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(this, "The resultcode returned by method '"
                    + methodName + "' is:" + resultcode + ", which implies:"
                    + message, null).log(getContext());
        }
        return message;
    }

    private boolean downTrapSent = false;

    private boolean upTrapSent = false;

    private String corbaClientPropName_ = "AppHomezoneClient";

    private CorbaClientProxy homezoneProvProxy_;

    private volatile HomezoneProvision service_;

    private CorbaClientProperty homezoneProperty_;

    private boolean bConnected = false;

    private static final String PM_MODULE = AppHomezoneClient.class.getName();

    @Override
    public ConnectionStatus[] getConnectionStatus()
    {
        return SystemStatusSupportHelper.get().generateConnectionStatus(homezoneProperty_, isAlive());
    }

    @Override
    public String getServiceStatus()
    {
        return SystemStatusSupportHelper.get().generateServiceStatusString(isAlive());
    }
}
