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
package com.trilogy.app.crm.client;

import java.util.HashMap;

import org.omg.CORBA.BooleanHolder;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.pin.manager.PinManagerService;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;

/**
 * Originally this was to be a dummy Client that always returned success upon every
 * operation. 
 * 
 * I have implemented a very crude PIN Manager, but it will also return success codes
 * for every operation.  It will rely on the person to implement the test to verify 
 * that the PIN is action has been done correctly.
 *  
 * For example, when testing:
 * 1) retrieve the current PIN, retrieveTestPINValue()
 * 2) perform test actions
 * 3) verify that the retrieved PIN has changed (or not) as desired.
 * 
 * PIN is an Integer.  A Value equivalent to 0 (zero) or less means that the PIN is UNPROVISIONED.
 * A value above zero means the PIN is PROVISIONED.
 * 
 * @author angie.li@redknee.com
 *
 */
public class AppPinManagerTestClient implements AppPinManagerClient,
        RemoteServiceStatus
{
    
    public AppPinManagerTestClient(boolean isConnected)
    {
        init();
        defaultResult = isConnected ? 0 : CONNECTIVITY_ERROR;
    }

    public int changeMsisdn(Context ctx, Account account, String oldMsisdn, String newMsisdn,
            String erReference)
    {
        //Swap PIN from Old MSISDN to new MSISDN
        Integer pin = retrieveTestPINValue(oldMsisdn);
        pinCounter_.put(newMsisdn, pin);
        pinCounter_.put(oldMsisdn, unprovision(pin));
        
        return defaultResult;
    }

    public short deletePin(Context ctx, String msisdn, String erReference)
    {
        Integer pin = retrieveTestPINValue(msisdn);
        pinCounter_.put(msisdn, unprovision(pin));
        
        return defaultResult;
    }

    public short generatePin(Context ctx, String msisdn, Account account, String erReference)
    {
        Integer pin = retrieveTestPINValue(msisdn);
        pinCounter_.put(msisdn, provision(pin));
        
        return defaultResult;
    }

    public short generatePin(Context ctx, String msisdn, int spid, String erReference)
    {
    	return defaultResult;
    }
    
    public short changePin(Context ctx, String msisdn, String oldPinNumber, String newPinNumber1, String newPinNumber2,
            String erReference)
    {
    	return defaultResult;
    }
    
    public short verifyPin(Context ctx, String msisdn, String pinNumber, String erReference)
    {
    	return defaultResult;
    }
    
    public short setPin(Context ctx, String msisdn, Integer spid, String newPinNumber, String erReference)
    {
    	return defaultResult;
    }
    
    public PinManagerService login(String userName, String pwd)
            throws HomeException
    {
        return null;
    }

    public short resetPin(Context ctx, String msisdn, String erReference)
    {
        Integer pin = retrieveTestPINValue(msisdn);
        pinCounter_.put(msisdn, unprovision(pin));
        
        return defaultResult;
    }

    public short setAuthenticatedFlag(Context ctx, String msisdn, int subscriptionType, boolean authenicated,
            String erReference) throws HomeException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public short queryAuthenticatedFlag(String msisdn, int subscriptionType, String erReference, BooleanHolder status)
            throws HomeException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getDescription()
    {
        return "Test stub client for the AppPinManagerClient interface";
    }

    public String getName()
    {
        return "AppPinManagerTestClient";
    }

    public boolean isAlive()
    {
        return true;
    }

    public void init()
    {
        pinCounter_ = new HashMap<String, Integer>();  
    }
    
    /**
     * Returns a non null PIN value stored in our mock Pin Manager
     */
    public Integer retrieveTestPINValue(String msisdn)
    {
        Integer pin = pinCounter_.get(msisdn);
        if (pin == null)
        {
            pin = Integer.valueOf(0);
        }
        return pin;
    }
    
    /**
     * Return the value of the PIN after provisioning.
     * To provision a PIN, make it a positive value (ABS) and increase it's positive value.
     * @param pin
     * @return
     */
    public static Integer provision(Integer pin)
    {
        return Math.abs(pin) + 1;
    }
    
    /**
     * Return the value of the PIN after unprovisioning.
     * To provision a PIN, make it a negative value and increase it's negative value.
     * @param pin
     * @return
     */
    public static Integer unprovision(Integer pin)
    {
        return -(Math.abs(pin) + 1);
    }
    
    
    /**
     * String Key is the MSISDN.
     * Integer is the pin.  A Value equivalent to 0 (zero) or less means that the PIN is UNPROVISIONED.
     * A value above zero means the PIN is PROVISIONED.
     */
    HashMap<String, Integer> pinCounter_;
    
    /**
     * An error code not existing in com.redknee.app.crm.pin.manager.ErrorCode.
     * This should translate to an "Unknown Error" by PinManagerSupporty.
     */
    final short CONNECTIVITY_ERROR = 500;
    final short defaultResult;
    @Override
    public ConnectionStatus[] getConnectionStatus()
    {
        return SystemStatusSupportHelper.get().generateConnectionStatus("", isAlive());
    }

    @Override
    public String getServiceStatus()
    {
        return SystemStatusSupportHelper.get().generateServiceStatusString(isAlive());
    }


}
