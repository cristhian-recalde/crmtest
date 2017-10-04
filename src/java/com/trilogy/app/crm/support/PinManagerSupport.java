/*
 * PinManagerSupport.java
 * 
 * Author : danny.ng@redknee.com Date : Mar 13, 2006 deepak.mishra@redknee.com Date : Aug
 * 20, 2008 This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily available.
 * Additionally, source code is, by its very nature, confidential information and
 * inextricably contains trade secrets and other information proprietary, valuable and
 * sensitive to Redknee, no unauthorised use, disclosure, manipulation or otherwise is
 * permitted, and may only be used in accordance with the terms of the licence agreement
 * entered into with Redknee Inc. and/or its subsidiaries.
 * 
 * Copyright  Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.support;

import org.omg.CORBA.BooleanHolder;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AcquiredMsisdnPINManagement;
import com.trilogy.app.crm.bean.AcquiredMsisdnPINManagementHome;
import com.trilogy.app.crm.bean.AcquiredMsisdnPINManagementXInfo;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.PinProvisioningStatusEnum;
import com.trilogy.app.crm.client.AppPinManagerClient;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.pin.manager.ErrorCode;
import com.trilogy.framework.auth.cipher.Cipher;
import com.trilogy.framework.auth.spi.UserAndGroupAuthSPI;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;


/**
 * Provides Support methods/data for PIN Manager Interaction.
 * @author dmishra 
 */
public class PinManagerSupport
{


    public PinManagerSupport()
    {
        super();
    }


    /**
     * @param ctx
     * @param msisdn :
     *            MSISDN that would be treated as Id for Pin Profile.
     * @param erReference :
     *            ER Reference for tracking at PIN Manager Side
     * @return returns : The result code of Pin Generation operation , '0' is success.
     * @throws HomeException
     */
    public static short generatePin(Context ctx, String msisdn, Account account, String erReference) throws HomeException
    {
    	if (LogSupport.isDebugEnabled(ctx))
    	{
    		LogSupport.debug(ctx, PinManagerSupport.class, "About to provision PIN on PIN Manager for MSISDN : " + msisdn);
    	}
        final PMLogMsg logMsg = new PMLogMsg(LOGGING_MODULE, "generatePin");
        short result;
        try
        {
            AppPinManagerClient client = getClient(ctx);
            result = client.generatePin(ctx, msisdn,account, erReference);
            if (result != 0)
            {
                throw new ProvisioningHomeException("Failed to generate PIN", result, Common.OM_PM_ERROR);
            }
            if (LogSupport.isDebugEnabled(ctx))
        	{
            	LogSupport.debug(ctx, PinManagerSupport.class, "Successfully provisioned PIN to PIN Manager!");
        	}
        }
        finally
        {
            logMsg.log(ctx);
        }
        
        return result;
    }

    /**
	 * @param ctx
	 * @param msisdn :
	 *            The MSISDN for which to generate PIN for.
	 * @param spid :
	 *            SPID of the MSISDN.           
	 * @param erReference :
	 *            Reference to be used to correlate the request at PIN Manager Side.
	 * @return returns : The result code of Pin Generation operation , '0' is success.
	 * 
	 */
    public static short generatePin(Context ctx, String msisdn, int spid, String erReference) throws HomeException
    {
    	if (LogSupport.isDebugEnabled(ctx))
    	{
    		LogSupport.debug(ctx, PinManagerSupport.class, "About to provision PIN on PIN Manager for MSISDN : " + msisdn);
    	}
        final PMLogMsg logMsg = new PMLogMsg(LOGGING_MODULE, "generatePin");
        short result;
        try
        {
            AppPinManagerClient client = getClient(ctx);
            result = client.generatePin(ctx, msisdn, spid, erReference);
            if (result != 0)
            {
                throw new ProvisioningHomeException("Failed to generate PIN", result, Common.OM_PM_ERROR);
            }
            if (LogSupport.isDebugEnabled(ctx))
        	{
            	LogSupport.debug(ctx, PinManagerSupport.class, "Successfully provisioned PIN to PIN Manager!");
        	}
        }
        finally
        {
            logMsg.log(ctx);
        }
        
        return result;
    }

    /**
     * @param ctx
     * @param msisdn :
     *            MSISDN that would be treated as Id for Pin Profile.
     * @param erReference :
     *            ER Reference for tracking at PIN Manager Side
     * @return returns : The result code of Pin PIN Reset operation , '0' is success.
     * @throws HomeException
     */
    public static short resetPin(Context ctx, String msisdn, String erReference) throws HomeException
    {
    	if (LogSupport.isDebugEnabled(ctx))
    	{
    		LogSupport.debug(ctx, PinManagerSupport.class, "About to reset PIN on PIN Manager for MSISDN : " + msisdn);
    	}
        final PMLogMsg logMsg = new PMLogMsg(LOGGING_MODULE, "resetPin");
        short result;
        try
        {
            AppPinManagerClient client = getClient(ctx);
            result = client.resetPin(ctx, msisdn, erReference);
            if (result != 0)
            {
                throw new ProvisioningHomeException("Failed to reset PIN", result, Common.OM_PM_ERROR);
            }
            if (LogSupport.isDebugEnabled(ctx))
        	{
            	LogSupport.debug(ctx, PinManagerSupport.class, "Successfully reset PIN on PIN Manager!");
        	}
        }
        finally
        {
            logMsg.log(ctx);
        }
        
        return result;
    }


    /**
     * @param ctx
     * @param msisdn :
     *            MSISDN that would be treated as Id for Pin Profile.
     * @param erReference :
     *            ER Reference for tracking at PIN Manager Side
     * @return returns : The result code of Pin Delete operation , '0' is success.
     * @throws HomeException
     */
    public static short deletePin(Context ctx, String msisdn, String erReference) throws HomeException
    {
    	if (LogSupport.isDebugEnabled(ctx))
    	{
    		LogSupport.debug(ctx, PinManagerSupport.class, "About to deprovision PIN on PIN Manager for MSISDN : " + msisdn);
    	}
        final PMLogMsg logMsg = new PMLogMsg(LOGGING_MODULE, "deletePin");
        short result;
        try
        {
            AppPinManagerClient client = getClient(ctx);
            result = client.deletePin(ctx, msisdn, erReference);
            if (result != 0)
            {
                throw new ProvisioningHomeException("Failed to delete PIN", result, Common.OM_PM_ERROR);
            }
            if (LogSupport.isDebugEnabled(ctx))
        	{
            	LogSupport.debug(ctx, PinManagerSupport.class, "Successfully deprovisioned PIN from PIN Manager!");
        	}
        }
        finally
        {
            logMsg.log(ctx);
        }
        return result;
    }


    /**
     * This method would be used when the same subscription is assigned to another MSISDN.
     * 
     * @param ctx
     * @param oldMsisdn :
     *            MSISDN with which profile is present at PIN Manager.
     * @param newMsisdn :
     *            MSISDN with which profile's MISIDN to be replaced at PIN Manager.
     * @param erReference
     * @return
     * @throws HomeException
     */
    public static int changeMsisdn(Context ctx,Account account, String oldMsisdn, String newMsisdn, String erReference)
            throws HomeException
    {
    	if (LogSupport.isDebugEnabled(ctx))
    	{
    		LogSupport.debug(ctx, PinManagerSupport.class, "About to change MSISDN on PIN Manager for Old MSISDN : " + oldMsisdn + ", New MSISDN : " + newMsisdn);
    	}
        final PMLogMsg logMsg = new PMLogMsg(LOGGING_MODULE, "changeMsisdn");
        int result;
        try
        {
            AppPinManagerClient pmClient = getClient(ctx);
            result = pmClient.changeMsisdn(ctx, account, oldMsisdn, newMsisdn, erReference);
            if (result != 0)
            {
                throw new ProvisioningHomeException("Failed to Change Msisdn, Old MSISDN : " + oldMsisdn + ", New MSISDN : "
                        + newMsisdn, result, Common.OM_PM_ERROR);
            }
            if (LogSupport.isDebugEnabled(ctx))
        	{
            	LogSupport.debug(ctx, PinManagerSupport.class, "Successfully changed MSISDN on PIN Manager!");
        	}
        }
        finally
        {
            logMsg.log(ctx);
        }
        return result;
    }

    /**
	 * @param ctx
	 * @param msisdn :
	 *            The MSISDN for which to change the PIN at PIN Manager.
	 * @param oldPinNumber :
	 *            The old PIN number to be validated before the PIN change occurs at PIN Manager.           
	 * @param newPINNumber1 :
	 *            The new PIN number to be associated to the MSISDN at PIN Manager.
	 * @param newPINNumber2 :
	 *            The second new PIN number to be used for confirmation against the first PIN number before the PIN change occurs at PIN Manager.
	 * @param erReference :
	 *            Reference to be used to correlate the request at PIN Manager Side.
	 * @return returns : The result code of Pin Generation operation , '0' is success.
	 * 
	 */
    public static short changePin(Context ctx, String msisdn, String oldNumber, String newPinNumber1,
            String newPinNumber2, String erReference) throws HomeException
    {
    	if (LogSupport.isDebugEnabled(ctx))
    	{
    		LogSupport.debug(ctx, PinManagerSupport.class, "About to change PIN on PIN Manager for MSISDN : " + msisdn);
    	}
        final PMLogMsg logMsg = new PMLogMsg(LOGGING_MODULE, "changePin");
        short result;
        try
        {
            AppPinManagerClient client = getClient(ctx);
            result = client.changePin(ctx, msisdn,oldNumber,newPinNumber1,newPinNumber2,erReference);
            if (result != 0)
            {
                throw new ProvisioningHomeException("Failed to change PIN", result, Common.OM_PM_ERROR);
            }
            if (LogSupport.isDebugEnabled(ctx))
        	{
            	LogSupport.debug(ctx, PinManagerSupport.class, "Successfully changed PIN on PIN Manager!");
        	}
        }
        finally
        {
            logMsg.log(ctx);
        }
        return result;
    }
    
    /**
	 * @param ctx
	 * @param msisdn :
	 *            The MSISDN for which to validate the PIN for.
	 * @param pinNumber :
	 *            The PIN to validate for the MSISDN.
	 * @param erReference :
	 *            Reference to be used to correlate the request at PIN Manager Side.
	 * @return returns : The result code of Pin Generation operation , '0' is success.
	 * 
	 */
    public static short verifyPin(Context ctx, String msisdn, String pinNumber, String erReference) throws HomeException
    {
    	if (LogSupport.isDebugEnabled(ctx))
    	{
    		LogSupport.debug(ctx, PinManagerSupport.class, "About to verify PIN on PIN Manager for MSISDN : " + msisdn);
    	}
        final PMLogMsg logMsg = new PMLogMsg(LOGGING_MODULE, "verifyPin");
        short result;
        try
        {
            AppPinManagerClient client = getClient(ctx);
            result = client.verifyPin(ctx, msisdn, pinNumber, erReference);
            if (result != 0)
            {
                throw new ProvisioningHomeException("Failed to verify PIN", result, Common.OM_PM_ERROR);
            }
            if (LogSupport.isDebugEnabled(ctx))
        	{
            	LogSupport.debug(ctx, PinManagerSupport.class, "Successfully verified PIN on PIN Manager!");
        	}
        }
        finally
        {
            logMsg.log(ctx);
        }
        return result;
    }
    
    /**
	 * @param ctx
	 * @param msisdn :
	 *            The MSISDN for which to set a PIN.
	 * @param spid :
	 *            The SPID of the MSISDN.
	 * @param newPINNumber :
	 *            The new PIN Number to be associated to the MSISDN at PIN Manager Side.
	 * @param erReference :
	 *            Reference to be used to correlate the request at PIN Manager Side.
	 * @return returns : The result code of Pin Generation operation , '0' is success.
	 * 
	 */
    public static short setPin(Context ctx, String msisdn, Integer spid, String newPinNumber, String erReference) throws HomeException
    {
    	if (LogSupport.isDebugEnabled(ctx))
    	{
    		LogSupport.debug(ctx, PinManagerSupport.class, "About to provision PIN on PIN Manager for MSISDN : " + msisdn);
    	}
        final PMLogMsg logMsg = new PMLogMsg(LOGGING_MODULE, "setPin");
        short result;
        try
        {
            AppPinManagerClient client = getClient(ctx);
            result = client.setPin(ctx, msisdn, spid, newPinNumber, erReference);
            if (result != 0)
            {
                throw new ProvisioningHomeException("Failed to set PIN", result, Common.OM_PM_ERROR);
            }
            if (LogSupport.isDebugEnabled(ctx))
        	{
            	LogSupport.debug(ctx, PinManagerSupport.class, "Successfully provisioned PIN on PIN Manager!");
        	}
        }
        finally
        {
            logMsg.log(ctx);
        }
        return result;
    }
    
    /**
     * @param ctx
     * @return AppPinManagerClient is returned from context.
     * @throws HomeException
     */
    public static AppPinManagerClient getClient(Context ctx) throws HomeException
    {
        AppPinManagerClient pmClient = (AppPinManagerClient) ctx.get(AppPinManagerClient.class);
        if (pmClient == null)
        {
            throw new HomeException("Could not fetch PIN Manager client from context.");
        }
        return pmClient;
    }


    /**
     * @param ctx
     * @param userId :
     *            Login of the user performing the operation.
     * @param password :
     *            Password of the user performing the operation.
     * @return : PinManagerService is returned after authentication by the PIN
     *         Manager.PinManagerService is used for accessing the supported methods.
     * @throws HomeException
     */
//    public static PinManagerService login(Context ctx, String userId, String password) throws HomeException
//    {
//        PinManagerService pinManagerService = null;
//        final PMLogMsg logMsg = new PMLogMsg(LOGGING_MODULE, "login");
//        try
//        {
//            AppPinManagerClient appPinManagerClient = getClient(ctx);
//            appPinManagerClient.login(userId, password);
//        }
//        finally
//        {
//            logMsg.log(ctx);
//        }
//        return pinManagerService;
//    }

    /**
     * This method queries the status of the Subscription Authentication flag.
     * @param ctx
     * @param msisdn :
     *            MSISDN that would be treated as Id for Pin Profile.
     * @param subscriptionType:
     *          subscriptionType of the msisdn
     * @param erReference :
     *            ER Reference for tracking at PIN Manager Side
     * @param status:
     *          Current status of the Authentication field on pin manager
     *  
     * @return if the execution was successful returns 0, otherwise some errorCode
     *          
     * @throws HomeException
     */
    public static short queryAuthenticatedFlag(Context ctx, String msisdn, int subscriptionType, String erReference, BooleanHolder status)
            throws HomeException
    {
    	if (LogSupport.isDebugEnabled(ctx))
    	{
    		LogSupport.debug(ctx, PinManagerSupport.class,"About to query authenication status from PIN Manager for MSISDN : " + msisdn);
    	}
        final PMLogMsg logMsg = new PMLogMsg(LOGGING_MODULE, "queryAuthenticatedFlag");
        short result;
        try
        {
            AppPinManagerClient client = getClient(ctx);
            result = client.queryAuthenticatedFlag(msisdn, subscriptionType, erReference, status);
            if (LogSupport.isDebugEnabled(ctx))
        	{
            	LogSupport.debug(ctx, PinManagerSupport.class, "Successfully query Authenicationfrom PIN Manager!");
        	}
        }
        finally
        {
            logMsg.log(ctx);
        }
        return result;
    }

    /**
     * This method sets the status of the Subscription Authentication flag.
     * @param ctx
     * @param msisdn :
     *            MSISDN that would be treated as Id for Pin Profile.
     * @param subscriptionType:
     *          subscriptionType of the msisdn
     * @param authenticated:
     *          Set to true to enable the Authentication, otherwise, false.
     * @param erReference :
     *            ER Reference for tracking at PIN Manager Side
     *  
     * @return if the execution was successful returns 0, otherwise some errorCode
     * @throws HomeException
     */
    public static short setAuthenticatedFlag(Context ctx, String msisdn, int subscriptionType, boolean authenicated,
            String erReference) throws HomeException
    {
    	if (LogSupport.isDebugEnabled(ctx))
    	{
    		LogSupport.debug(ctx, PinManagerSupport.class, "About to set Authenication flag on PIN Manager for MSISDN : " + msisdn);
    	}
        final PMLogMsg logMsg = new PMLogMsg(LOGGING_MODULE, "setAuthenticatedFlag");
        short result;
        try
        {
            AppPinManagerClient client = getClient(ctx);
            result = client.setAuthenticatedFlag(ctx, msisdn, subscriptionType, authenicated, erReference);
            if (result != 0)
            {
                throw new ProvisioningHomeException("Failed to authenicate PIN", result, Common.OM_PM_ERROR);
            }
            if (LogSupport.isDebugEnabled(ctx))
        	{
            	LogSupport.debug(ctx, PinManagerSupport.class, "Successfully authenicated in the PIN Manager!");
        	}
        }
        finally
        {
            logMsg.log(ctx);
        }
        return result;
    }
    /**
     * @param ctx
     * @return : returns logged in User.
     * @throws HomeException
     */
    static public User getUser(final Context ctx) throws HomeException
    {
        User principal = (User) ctx.get(java.security.Principal.class, new User());
        if (principal == null)
        {
            throw new HomeException("Not able to get Valid User");
        }
        return principal;
    }


    /**
     * @param ctx
     * @return : returns loginId of the logged in user
     * @throws HomeException
     */
    static public String getLoginId(final Context ctx) throws HomeException
    {
        return getUser(ctx).getId();
    }


    /**
     * @param ctx
     * @return : returns decrypted password of the logged in user.
     * @throws HomeException
     */
    static public String getPassword(final Context ctx) throws HomeException
    {
        User principal = getUser(ctx);
        UserAndGroupAuthSPI authSPI = (UserAndGroupAuthSPI) ctx.get(UserAndGroupAuthSPI.class);
        Cipher cipher = authSPI.getCipher(ctx, principal);
        return cipher.decode(principal.getPassword());
    }


    /**
     * @param ctx
     * @param resultCode :
     *            return code of operation.
     * @return : returns message corresponding to return code.
     */
    static public String pinManagerResultToMessageMapping(Context ctx, short resultCode)
    {
        final MessageMgr mmgr = new MessageMgr(ctx, PinManagerSupport.class);
        String message = "";
        switch (resultCode)
        {
        case ErrorCode.SUCCESS:
            message = mmgr.get("PinManagerSupport.ErrorCode.Success", SUCCESS_MSG);
            break;
        case ErrorCode.INTERNAL_ERROR:
            message = mmgr.get("PinManagerSupport.ErrorCode.InternalError", INTERNAL_ERROR_MSG);
            break;
        case ErrorCode.INVALID_PARAMATER:
            message = mmgr.get("PinManagerSupport.ErrorCode.InvalidParams", INVALID_PARAMATER_MSG);
            break;
        case ErrorCode.UNAUTHORIZED_ACCESS:
            message = mmgr.get("PinManagerSupport.ErrorCode.UnauthorizedAccess", UNAUTHORIZED_ACCESS_MSG);
            break;
        case ErrorCode.ENTRY_ALREADY_EXISTED:
            message = mmgr.get("PinManagerSupport.ErrorCode.DuplicateEntry", ENTRY_ALREADY_EXISTED_MSG);
            break;
        case ErrorCode.ENTRY_NOT_FOUND:
            message = mmgr.get("PinManagerSupport.ErrorCode.MissingEntry", ENTRY_NOT_FOUND_MSG);
            break;
        case ErrorCode.SPID_NOT_FOUND:
            message = mmgr.get("PinManagerSupport.ErrorCode.InvalidSpid", SPID_NOT_FOUND_MSG);
            break;
        case ErrorCode.INVALID_PIN:
            message = mmgr.get("PinManagerSupport.ErrorCode.InvalidPin", INVALID_PIN_MSG);
            break;
        /* NEW_PIN_MISMATCH and INVALID_NEW_PIN should never come to CRM */
        case ErrorCode.NEW_PIN_MISMATCH:
            message = mmgr.get("PinManagerSupport.ErrorCode.NewPinMismatch", NEW_PIN_MISMATCH_MSG);
            break;
        case ErrorCode.INVALID_NEW_PIN:
            message = mmgr.get("PinManagerSupport.ErrorCode.InvalidNewPin", INVALID_NEW_PIN_MSG);
            break;
        case ErrorCode.PIN_LOCKED:
            message = mmgr.get("PinManagerSupport.ErrorCode.PinLocked", PIN_LOCKED_MSG);
            break;
        case ErrorCode.SEND_SMS_ERROR:
            message = mmgr.get("PinManagerSupport.ErrorCode.SendSmsError", SEND_SMS_ERROR_MSG);
            break;
        default:
            message = mmgr.get("PinManagerSupport.ErrorCode.UnknownError", UNKNOWN_MSG);
        }
        return message;
    }


    /**
     * @param ctx
     * @param msisdn
     * @param status
     * @throws HomeException
     */
    static public void updatePinProvisoningStatus(Context ctx, String msisdn, PinProvisioningStatusEnum status)
            throws HomeException
    {
        Home home = (Home) ctx.get(AcquiredMsisdnPINManagementHome.class);
        AcquiredMsisdnPINManagement acqMsisdn = new AcquiredMsisdnPINManagement();
        acqMsisdn.setMsisdn(msisdn);
        acqMsisdn = (AcquiredMsisdnPINManagement) home.find(ctx, acqMsisdn);
        if (acqMsisdn != null)
        {
            acqMsisdn.setState(status);
            home.store(ctx,acqMsisdn);
        }
        else
        {
            throw new HomeException("Profile Missing in AcquiredMsisdnPINManagement for msisdn [" + msisdn + "]");
        }
    }


    /**
     * Creates record at CRM on provisioning on PIN Manager to keep track of Provisoning
     * status.
     * 
     * @param ctx
     * @param subAcountId
     * @param msisdn
     * @param status
     * @throws HomeException
     */
    static public void createPinProvisoningRecord(Context ctx, String subAcountId, String msisdn,
            PinProvisioningStatusEnum status) throws HomeException
    {
        Home home = (Home) ctx.get(AcquiredMsisdnPINManagementHome.class);
        if (home != null)
        {
            AcquiredMsisdnPINManagement acqMsisdn = getPinProvisoningRecord(ctx, msisdn);
            if (acqMsisdn == null)
            {
                acqMsisdn = new AcquiredMsisdnPINManagement();
                acqMsisdn.setMsisdn(msisdn);
                acqMsisdn.setState(status);
                acqMsisdn.setIdIdentifier(subAcountId);
                home.create(ctx, acqMsisdn);
            }
            else
            {
                acqMsisdn.setMsisdn(msisdn);
                acqMsisdn.setState(status);
                acqMsisdn.setIdIdentifier(subAcountId);
                home.store(ctx, acqMsisdn);
            }
        }
        else
        {
            throw new HomeException("Creation of profile in AcquiredMsisdnPINManagement  for msisdn [" + msisdn
                    + "] failed.Home not found in context !!");
        }
    }


    /**
     * Determines if MSISDN is provisioned on PIN Manager relying on
     * ACQUIREDMSISDNPINMANAGEMENT table at CRM
     * 
     * @param ctx
     * @param msisdn
     * @return
     * @throws HomeException
     */
    public static PinProvisioningStatusEnum getStateOnPinManager(final Context ctx, final String msisdn)
            throws HomeException
    {        
        AcquiredMsisdnPINManagement acqMsisdn = getPinProvisoningRecord(ctx,msisdn);
        if (acqMsisdn != null)
        {
            return acqMsisdn.getState();
        }
        else
        {
            return PinProvisioningStatusEnum.UNPROVISIONED;
        }
    }


    /**
     * Sets entry to un-provisioned from ACQUIREDMSISDNPINMANAGEMENT table using Msisdn as key.
     * 
     * @param ctx
     * @param msisdn
     * @throws HomeException
     */
    static public void deletePinProvisoningRecord(Context ctx, String msisdn) throws HomeException
    {
        Home home = (Home) ctx.get(AcquiredMsisdnPINManagementHome.class);
        if (home != null)
        {
            AcquiredMsisdnPINManagement acqMsisdn = getPinProvisoningRecord(ctx,msisdn);
            if (acqMsisdn != null)
            {
                acqMsisdn.setState(PinProvisioningStatusEnum.UNPROVISIONED);
                acqMsisdn.setAuthenticated(false);
                home.store(ctx, acqMsisdn);
            }
            else
            {
            	if (LogSupport.isDebugEnabled(ctx))
            	{
            		LogSupport.debug(ctx, PinManagerSupport.class, "The profile is not there for MSISDN [" + msisdn
                        + "] , nothing needs to be done.");
            	}
            }
        }
        else
        {
            throw new HomeException("Deletion of profile in AcquiredMsisdnPINManagement  for msisdn [" + msisdn
                    + "] failed.Home not found in context !!");
        }
    }
    
    
//    /**
//     * Sets entry to un-provisioned from ACQUIREDMSISDNPINMANAGEMENT table using Msisdn as key.
//     * 
//     * @param ctx
//     * @param msisdn
//     * @throws HomeException
//     */
//    static public void changeMsisdnPinProvisoningRecord(Context ctx, String oldMsisdn, String newMsisdn, PinProvisioningStatusEnum status) throws HomeException
//    {
//        Home home = (Home) ctx.get(AcquiredMsisdnPINManagementHome.class);
//        if (home != null)
//        {
//            AcquiredMsisdnPINManagement acqMsisdn = getPinProvisoningRecord(ctx, oldMsisdn);
//            if (acqMsisdn != null)
//            {
//                AcquiredMsisdnPINManagement newAcqMsisdn = new AcquiredMsisdnPINManagement();
//                newAcqMsisdn.setMsisdn(newMsisdn);
//                newAcqMsisdn.setState(status);
//                newAcqMsisdn.setMsisdn(acqMsisdn.getIdIdentifier());
//                home.remove(ctx,newAcqMsisdn);
//                home.create(ctx, newAcqMsisdn);
//                home.remove(ctx, acqMsisdn);
//            }
//            else
//            {
//                LogSupport.debug(ctx, PinManagerSupport.class, "The profile is not there for MSISDN [" + oldMsisdn
//                        + "] , nothing needs to be done.");
//            }
//        }
//        else
//        {
//            throw new HomeException("Deletion of profile in AcquiredMsisdnPINManagement  for msisdn [" + oldMsisdn
//                    + "] failed.Home not found in context !!");
//        }
//    }

    /**
     * Removes from ACQUIREDMSISDNPINMANAGEMENT table using Msisdn as key.
     * 
     * @param ctx
     * @param msisdn
     * @throws HomeException
     */
    static public void removePinProvisoningRecord(Context ctx, String msisdn) throws HomeException
    {
        Home home = (Home) ctx.get(AcquiredMsisdnPINManagementHome.class);
        if (home != null)
        {
            AcquiredMsisdnPINManagement acqMsisdn = getPinProvisoningRecord(ctx, msisdn);
            if (acqMsisdn != null)
            {
                if (acqMsisdn.getState() != PinProvisioningStatusEnum.PROVISIONED)
                {
                    home.remove(ctx, acqMsisdn);
                } else
                {
                	if (LogSupport.isDebugEnabled(ctx))
                	{
                		LogSupport.debug(ctx, PinManagerSupport.class, "The PIN profile for MSISDN [" + msisdn
                            + "] indicates that it is in provisioned state. Hence the record can not be remved");
                	}
                }
            }
            else
            {
            	if (LogSupport.isDebugEnabled(ctx))
            	{
            		LogSupport.debug(ctx, PinManagerSupport.class, "The profile is not there for MSISDN [" + msisdn
                        + "] , nothing needs to be done.");
            	}
            }
        }
        else
        {
            throw new HomeException("Deletion of profile in AcquiredMsisdnPINManagement  for msisdn [" + msisdn
                    + "] failed.Home not found in context !!");
        }
    }
    
    /**
     * Get's entry from ACQUIREDMSISDNPINMANAGEMENT table using Msisdn as key.
     * 
     * @param ctx
     * @param msisdn
     * @throws HomeException
     */
    static public AcquiredMsisdnPINManagement getPinProvisoningRecord(Context ctx, String msisdn) throws HomeException
    {
        Home home = (Home) ctx.get(AcquiredMsisdnPINManagementHome.class);
        if (home != null)
        {
            return (AcquiredMsisdnPINManagement) home
                    .find(ctx, new EQ(AcquiredMsisdnPINManagementXInfo.MSISDN, msisdn));
        }
        else
        {
            throw new HomeException("Deletion of profile in AcquiredMsisdnPINManagement  for msisdn [" + msisdn
                    + "] failed.Home not found in context !!");
        }
    }
    
    /** Get the subscriber account Id of the msisdn ; have to correct it as of now just keeping for testing
     * @param ctx
     * @param msisdn
     * @return
     * @throws HomeException
     */
    public static String getSubscriberAccountOfMsisdn(final Context ctx, final String msisdn) throws HomeException
    {
        // TODO After MSISDN Manamgement refactoring MSIDN will contain subscriber account
        // Id
        String subAccountId = null;
        Msisdn msisdnRecord = MsisdnSupport.getMsisdn(ctx, msisdn);
        if (msisdnRecord != null)
        {
            subAccountId=msisdnRecord.getBAN();
        }
        return subAccountId;
    }
    
    
    public static  boolean isPINManagerLicenseEnabled(final Context ctx)
    {
        return LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.PIN_MANAGER_LICENSE_KEY);
        
    }
    public static final String SUCCESS_MSG = "Succesful operation.";
    public static final String INTERNAL_ERROR_MSG = "Internal error occured in PIN manager Service, please check logs.";
    public static final String INVALID_PARAMATER_MSG = "Invalid paramater provided for operation.";
    public static final String UNAUTHORIZED_ACCESS_MSG = "Unauthorized operation.";
    public static final String ENTRY_ALREADY_EXISTED_MSG = "Pin profile already present.";
    public static final String ENTRY_NOT_FOUND_MSG = "Pin profile missing.";
    public static final String SPID_NOT_FOUND_MSG = "Invalid Spid provided.";
    public static final String INVALID_PIN_MSG = "Invalid Pin provided.";
    public static final String NEW_PIN_MISMATCH_MSG = "New Pin mismatched.";
    public static final String INVALID_NEW_PIN_MSG = "Invalid New Pin";
    public static final String UNKNOWN_MSG = "Connectivity issue or Uknown Error , please check logs. ";
    public static final String PIN_LOCKED_MSG = "Pin is currently locked due to excessive failed login attempts.";
    public static final String SEND_SMS_ERROR_MSG = "Pin generation failed due to the failed attempt as SMSs the new Pin.";

    public static final String ACQUIRED_MSISDN_PIN_MANAGEMENT_TRANSIENT_HOME = PinManagerSupport.class.getName()
            + "ACQUIRED_MSISDN_PIN_MANAGEMENT_TRANSIENT_HOME";
    /**
     * ER-Reference sent to PIN manager for tracking purpose.
     */
    public static final String ER_REFERENCE = "CRM-PIN-MANAGEMENT";
    private static final String LOGGING_MODULE = PinManagerSupport.class.getName();
}
