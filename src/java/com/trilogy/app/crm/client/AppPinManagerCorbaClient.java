/*
 * AppPinManagerClient.java
 * 
 * Author : danny.ng@redknee.com Date : Mar 10, 2006 deepak.mishra@redknee.com : Aug 20,
 * 2008
 * 
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
import org.omg.CORBA.BooleanHolder;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnXInfo;
import com.trilogy.app.crm.bean.PinProvisioningStatusEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.PinManagerSupport;
import com.trilogy.app.pin.manager.ErrorCode;
import com.trilogy.app.pin.manager.PinManagerService;
import com.trilogy.app.pin.manager.param.Parameter;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.protocol.smpp.smppv34.tlv.MsMsgWaitFacilities;

import static com.redknee.app.crm.client.CorbaClientTrapIdDef.PIN_MANAGER_SVC_DOWN;
import static com.redknee.app.crm.client.CorbaClientTrapIdDef.PIN_MANAGER_SVC_UP;

/**
 * Corba Client for services of Pin Manager
 * 
 * @author danny.ng@redknee.com
 * @author deepak.mishra@redknee.com
 * @created Mar 10, 2006
 * 
 * Support clustered corba client
 * Refactored reusable elements to an abstract class AbstractCrmClient<T>
 * @author rchen
 * @since June 26, 2009 
 */
public class AppPinManagerCorbaClient extends AbstractCrmClient<PinManagerService> implements AppPinManagerClient
{

    private static final String SERVICE_NAME = "AppPinManagerClient";
    private static final String SERVICE_DESCRIPTION = "CORBA client for PIN Manager services";


    /**
     * 
     * @param ctx
     */
    public AppPinManagerCorbaClient(Context ctx)
    {
        super(ctx, SERVICE_NAME, SERVICE_DESCRIPTION, PinManagerService.class, PIN_MANAGER_SVC_DOWN, PIN_MANAGER_SVC_UP);
    }


    /* (non-Javadoc)
	 * @see com.redknee.app.crm.client.AppPinManagerClient#generatePin(com.redknee.framework.xhome.context.Context, java.lang.String, java.lang.String)
	 */
    public short generatePin(Context ctx, String msisdn, Account account, String erReference)
    {
    	if (account != null)
    	{
    		return generatePin(ctx, msisdn, account.getBAN(), account.getSpid(), erReference);	
    	}
    	else
    	{
    		String errorMessage = "PIN provisionoing for MSISDN : [" + msisdn + "] failed, Account cannot be null";
    		LogSupport.minor(ctx,this, errorMessage);
    		return -1;
    	}
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
    @Override
    public short generatePin(Context ctx, String msisdn, int spid, String erReference)
    {
    	String accountId = null;
    	try 
    	{
    		accountId = PinManagerSupport.getSubscriberAccountOfMsisdn(ctx,msisdn);
    		return generatePin(ctx, msisdn, accountId, spid, erReference);
    	} 
    	catch (HomeException e) 
    	{
    		String errorMessage = "PIN provisionoing for MSISDN : [" + msisdn + "] failed because of Error : ["
                    + e.getMessage() + "]";
    		LogSupport.minor(ctx,this, errorMessage);
    		return -1;
    	}        
    }

    public short generatePin(Context ctx, String msisdn, String accountId, int spid, String erReference)
    {
    	short result = -1;
        try
        {
            PinManagerService pinManagerService = getService();
            if (pinManagerService == null)
            {
            	LogSupport.minor(ctx, this, "Cannot get Pin Manager service.");
                return -1;
            }
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Calling PIN Manager generatePin for msisdn [" + msisdn + "]");
            }
            final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "generatePin");
            result = pinManagerService.generatePin(msisdn, spid, erReference);
            pmLogMsg.log(ctx);
        }
        catch (Throwable t)
        {
            String errorMessage = "PIN provisionoing for MSISDN [" + msisdn + "] failed because of Error ["
                    + t.getMessage() + "]";
            if (LogSupport.isDebugEnabled(ctx))
            {
            	new DebugLogMsg(this, errorMessage, t).log(ctx);
            }
            new MinorLogMsg(this, errorMessage, null).log(ctx);
        }
        finally
        {
            try
            {
                if (result == ErrorCode.SUCCESS)
                {
                    // PinManagerSupport.updatePinProvisoningStatus(ctx,msisdn,PinProvisioningStatusEnum.PROVISIONED);
                    PinManagerSupport.createPinProvisoningRecord(ctx, accountId, msisdn,
                            PinProvisioningStatusEnum.PROVISIONED);
                }
                else if (result == ErrorCode.ENTRY_ALREADY_EXISTED)
                {
                    // If profile already exists on PIN Manager, set the status to
                    // PROVISIONED on CRM so that
                    // the Reset and Delete links become visible on the screen
                    PinManagerSupport.updatePinProvisoningStatus(ctx, msisdn, PinProvisioningStatusEnum.PROVISIONED);
                }
                else
                {
                    PinManagerSupport.createPinProvisoningRecord(ctx, accountId, msisdn,
                            PinProvisioningStatusEnum.UNPROVISIONED);
                }
            }
            catch (Throwable t)
            {
                // do not rethrow. Pin Manger operation must not fail end to end because
                // the local record error
                String errorMessage = "Could not provision local pin gernation record for for Account ["
                        + accountId + "] MSISDN [" + msisdn + "] because of Error [" + t.getMessage() + "]";
                new MinorLogMsg(this, errorMessage, null).log(ctx);
                if (LogSupport.isDebugEnabled(ctx))
                {
                	new DebugLogMsg(this, errorMessage, t).log(ctx);
                }
            }
        }
        return result;
    }
    
    /* (non-Javadoc)
	 * @see com.redknee.app.crm.client.AppPinManagerClient#resetPin(com.redknee.framework.xhome.context.Context, java.lang.String, java.lang.String)
	 */
    public short resetPin(Context ctx, String msisdn, String erReference)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "resetPin()");
        short result = -1;
        try
        {
            PinManagerService pinManagerService = getService();
            if (pinManagerService == null)
            {
            	LogSupport.minor(ctx, this, "Cannot get Pin Manager service.");
                return -1;
            }
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Calling PIN Manager resetPin for msisdn [" + msisdn + "]");
            }
            result = pinManagerService.resetPin(msisdn, erReference);
        }
        catch (Throwable t)
        {
            String errorMessage = "PIN resetting for MSISDN [" + msisdn + "] failed because of Error ["
                    + t.getMessage() + "]";
            if (LogSupport.isDebugEnabled(ctx))
            {
            	new DebugLogMsg(this, errorMessage, t).log(ctx);
            }
            new MinorLogMsg(this, errorMessage, null).log(ctx);
        }
        finally
        {
            pmLogMsg.log(ctx);
        }
        return result;
    }


    /* (non-Javadoc)
	 * @see com.redknee.app.crm.client.AppPinManagerClient#deletePin(com.redknee.framework.xhome.context.Context, java.lang.String, java.lang.String)
	 */
    public short deletePin(Context ctx, String msisdn,String erReference)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "deletePin");
        short result = -1;
        try
        {
            PinManagerService pinManagerService = getService();
            if (pinManagerService == null)
            {
            	LogSupport.minor(ctx, this, "Cannot get Pin Manager service.");
                return -1;
            }
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Calling PIN Manager deletePin for msisdn [" + msisdn + "]");
            }
            result = pinManagerService.deletePin(msisdn, erReference);
        }
        catch (Throwable t)
        {
            String errorMessage = "PIN deletion for MSISDN [" + msisdn + "] failed because of Error [" + t.getMessage()
                    + "]";
            if (LogSupport.isDebugEnabled(ctx))
            {
            	new DebugLogMsg(this, errorMessage, t).log(ctx);
            }
            new MinorLogMsg(this, errorMessage, null).log(ctx);
        }
        finally
        {
            pmLogMsg.log(ctx);
            try
            {
                if (result == ErrorCode.SUCCESS)
                {
                    PinManagerSupport.deletePinProvisoningRecord(ctx, msisdn);
                }
                else if (result == ErrorCode.ENTRY_NOT_FOUND)
                {
                	if (LogSupport.isDebugEnabled(ctx))
                    {
                		LogSupport.debug(ctx, this, "Since profile is not present in PIN Manager nothing needs to be done.");
                    }
                    PinManagerSupport.deletePinProvisoningRecord(ctx, msisdn);
                }
                // don't delete the record.
            }
            catch (Throwable t)
            {
                // do not rethrow. Pin Manger operation must not fail end to end because
                // the local record error
                String errorMessage = "Could not remove local pin gernation record for MSISDN [" + msisdn
                        + "] because of Error [" + t.getMessage() + "]";
                new MinorLogMsg(this, errorMessage, null).log(ctx);
                if (LogSupport.isDebugEnabled(ctx))
                {
                	new DebugLogMsg(this, errorMessage, t).log(ctx);
                }
            }
        }
        return result;
    }


    /* (non-Javadoc)
	 * @see com.redknee.app.crm.client.AppPinManagerClient#changeMsisdn(com.redknee.framework.xhome.context.Context, java.lang.String, java.lang.String, java.lang.String)
	 */
    public int changeMsisdn(Context ctx, Account account , String oldMsisdn, String newMsisdn, String erReference)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "changeMsisdn()");
        int result = -1;
        try
        {
            PinManagerService pinManagerService = getService();
            if (pinManagerService == null)
            {
            	LogSupport.minor(ctx, this, "Cannot get Pin Manager service.");
                return -1;
            }
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Calling PIN Manager changeMsisdn for oldMsisdn [" + oldMsisdn
                        + "]" + "newMsisdn [" + newMsisdn + "]");
            }
            result = pinManagerService.changeMsisdn(oldMsisdn, newMsisdn, erReference);
        }
        catch (Throwable t)
        {
            String errorMessage = "PIN change-msidn for MSISDN [" + oldMsisdn + "] to MSISDN [" + newMsisdn + "] failed because of Error ["
                    + t.getMessage() + "]";
            if (LogSupport.isDebugEnabled(ctx))
            {
            	new DebugLogMsg(this, errorMessage, t).log(ctx);
            }
            new MinorLogMsg(this, errorMessage, null).log(ctx);
        }
        finally
        {
            try
            {
                if (result == ErrorCode.SUCCESS)
                {
                    // PinManagerSupport.updatePinProvisoningStatus(ctx,msisdn,PinProvisioningStatusEnum.PROVISIONED);
                    PinManagerSupport.createPinProvisoningRecord(ctx, account.getBAN(), newMsisdn,
                            PinProvisioningStatusEnum.PROVISIONED);
                }
                else if (result == ErrorCode.ENTRY_ALREADY_EXISTED)
                {
                    // If profile already exists on PIN Manager, set the status to
                    // PROVISIONED on CRM so that
                    // the Reset and Delete links become visible on the screen
                    PinManagerSupport.updatePinProvisoningStatus(ctx, newMsisdn, PinProvisioningStatusEnum.PROVISIONED);
                }
                else
                {
                    PinManagerSupport.createPinProvisoningRecord(ctx, account.getBAN(), newMsisdn,
                            PinProvisioningStatusEnum.UNPROVISIONED);
                }
                PinManagerSupport.deletePinProvisoningRecord(ctx, oldMsisdn);
            }
            catch (Throwable t)
            {
                // do not rethrow. Pin Manger operation must not fail end to end because
                // the local record error
                String errorMessage = "Could not change local pin gernation record for Account [" + account.getBAN()
                        + "] MSISDN [" + newMsisdn + "] because of Error [" + t.getMessage() + "]";
                new MinorLogMsg(this, errorMessage, null).log(ctx);
                if (LogSupport.isDebugEnabled(ctx))
                {
                	new DebugLogMsg(this, errorMessage, t).log(ctx);
                }
            }
        }
        return result;
    }

    @Override
    public short queryAuthenticatedFlag( String msisdn, int subscriptionType, String erReference, BooleanHolder status)
            throws HomeException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "queryAuthenticatedFlag");
        short resultCodeFromPinManager = 0;
        try
        {
            PinManagerService pinManagerService = getService();
            if (pinManagerService == null)
            {
            	LogSupport.minor(getContext(), this, "Cannot get Pin Manager service.");
                return -1;
            }
            if (LogSupport.isDebugEnabled(getContext()))
            {
                LogSupport.debug(getContext(), this, "Calling PIN Manager queryAuthenticatedFlag for msisdn [" + msisdn + "]");
            }
            status.value =false;
            Parameter[] param = new Parameter[0];
            resultCodeFromPinManager = pinManagerService.queryAuthenticatedFlag(msisdn,subscriptionType, param, erReference, status);
            
            if (resultCodeFromPinManager  == ErrorCode.ENTRY_NOT_FOUND)
            {
                LogSupport.info(getContext(), this, "Since profile is not present in PIN Manager nothing needs to be done.");
            }
            else if ( resultCodeFromPinManager != ErrorCode.SUCCESS)
            {
                LogSupport.info(getContext(), this, "Unable to query the msisdn [ " + msisdn + " ] on the Pin manager.");                
            }
        }
        catch (Throwable t)
        {
            resultCodeFromPinManager = COMMUNICATION_FAILURE;
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
        return resultCodeFromPinManager;
    }


    /**
     * 
     */
    public short setAuthenticatedFlag(Context ctx, String msisdn, int subscriptionType, boolean authenicated,
            String erReference) throws HomeException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "setAuthenticatedFlag");
        short result = -1;
        try
        {
            PinManagerService pinManagerService = getService();
            if (pinManagerService == null)
            {
            	LogSupport.minor(ctx, this, "Cannot get Pin Manager service.");
                return -1;
            }
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Calling PIN Manager setAuthenticatedFlag for msisdn [" + msisdn + "]");
            }
            Parameter[] param = new Parameter[0];
            result = pinManagerService.setAuthenticatedFlag(msisdn, subscriptionType, param, authenicated, erReference);
            if (result == ErrorCode.ENTRY_NOT_FOUND)
            {
                LogSupport.info(ctx, this, "Since profile is not present in PIN Manager nothing needs to be done.");
            }
            else if ( result != ErrorCode.SUCCESS)
            {
                LogSupport.info(ctx, this, "Unable to setAuthenticationFlag the msisdn [ " + msisdn + " ] on the Pin manager.");                
            }
        }
        catch (Throwable t)
        {
            result = COMMUNICATION_FAILURE;
        }
        finally
        {
            pmLogMsg.log(ctx);
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
    @Override
    public short changePin(Context ctx, String msisdn, String oldPinNumber, String newPinNumber1, String newPinNumber2,
            String erReference)
    {
    	final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "changePin()");
        short result = -1;
        try
        {
            PinManagerService pinManagerService = getService();
            if (pinManagerService == null)
            {
            	LogSupport.minor(ctx, this, "Cannot get Pin Manager service.");
                return -1;
            }
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Calling PIN Manager changePin for MSISDN : [" + msisdn + "]");
            }
            result = pinManagerService.changePin(msisdn, oldPinNumber, newPinNumber1, newPinNumber2, erReference);
        }
        catch (Throwable t)
        {
            String errorMessage = "Change PIN for MSISDN : [" + msisdn + "] failed because of Error : ["
                    + t.getMessage() + "]";
            LogSupport.minor(ctx,this, errorMessage);
        }
        finally
        {
            pmLogMsg.log(ctx);
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
    @Override
    public short verifyPin(Context ctx, String msisdn, String pinNumber, String erReference)
    {
    	final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "verifyPin()");
        short result = -1;
        try
        {
            PinManagerService pinManagerService = getService();
            if (pinManagerService == null)
            {
            	LogSupport.minor(ctx, this, "Cannot get Pin Manager service.");
                return -1;
            }
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Calling PIN Manager verifyPin for MSISDN : [" + msisdn + "]");
            }
            result = pinManagerService.verifyPin(msisdn, pinNumber, erReference);
        }
        catch (Throwable t)
        {
            String errorMessage = "Verify PIN for MSISDN : [" + msisdn + "] failed because of Error : ["
                    + t.getMessage() + "]";
            LogSupport.minor(ctx,this, errorMessage);
        }
        finally
        {
            pmLogMsg.log(ctx);
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
    @Override
    public short setPin(Context ctx, String msisdn, Integer spid, String newPinNumber, String erReference)
    {
    	final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "setPin()");
        short result = -1;
        try
        {
            PinManagerService pinManagerService = getService();
            if (pinManagerService == null)
            {
            	LogSupport.minor(ctx, this, "Cannot get Pin Manager service.");
                return -1;
            }
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Calling PIN Manager setPin for MSISDN : [" + msisdn + "]");
            }

            Parameter[] param = new Parameter[0];
            result = pinManagerService.setPin(msisdn, spid, newPinNumber, param, erReference);
        }
        catch (Throwable t)
        {
            String errorMessage = "Set PIN for MSISDN : [" + msisdn + "] failed because of Error : ["
                    + t.getMessage() + "]";
            LogSupport.minor(ctx,this, errorMessage);
        }
        finally
        {
            pmLogMsg.log(ctx);
            String accountId = null;
            try
            {
            	accountId = PinManagerSupport.getSubscriberAccountOfMsisdn(ctx,msisdn);
                if (result == ErrorCode.SUCCESS)
                {
                    PinManagerSupport.createPinProvisoningRecord(ctx, accountId, msisdn,
                            PinProvisioningStatusEnum.PROVISIONED);
                }
                else
                {
                    PinManagerSupport.createPinProvisoningRecord(ctx, accountId, msisdn,
                            PinProvisioningStatusEnum.UNPROVISIONED);
                }
            }
            catch (Throwable t)
            {
                // do not rethrow. Pin Manger operation must not fail end to end because
                // the local record error
                String errorMessage = "Could not provision local pin set record for Account : ["
                        + accountId + "], MSISDN : [" + msisdn + "], because of Error : [" + t.getMessage() + "]";
                LogSupport.minor(ctx,this, errorMessage);
            }
            
        }
        return result;
    }
    
    private static final String PM_MODULE = AppPinManagerClient.class.getName();

}
