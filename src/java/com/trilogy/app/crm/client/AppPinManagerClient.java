package com.trilogy.app.crm.client;

import org.omg.CORBA.BooleanHolder;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * Removed login() method
 * @author rchen
 * Since June 26, 2009
 *
 */
public interface AppPinManagerClient
{

    

    
	/**
	 * @param ctx
	 * @param msisdn :
	 *            MSISDN that would be treated as Id for Pin Profile.
	 * @param erReference :
	 *            ER Reference for tracking at PIN Manager Side
	 * @return returns : The result code of Pin Generation operation , '0' is success.
	 * @throws HomeException
	 */
	public short generatePin(Context ctx, String msisdn, Account account,
			String erReference);

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
	public short generatePin(Context ctx, String msisdn, int spid, String erReference);
	
	/**
	 * @param ctx
	 * @param msisdn :
	 *            MSISDN that would be treated as Id for Pin Profile.
	 * @param erReference :
	 *            ER Reference for tracking at PIN Manager Side
	 * @return returns : The result code of Pin PIN Reset operation , '0' is success.
	 * @throws HomeException
	 */
	public short resetPin(Context ctx, String msisdn, 
			String erReference);

	/**
	 * @param ctx
	 * @param msisdn :
	 *            MSISDN that would be treated as Id for Pin Profile.
	 * @param erReference :
	 *            ER Reference for tracking at PIN Manager Side
	 * @return returns : The result code of Pin Delete operation , '0' is success.
	 * @throws HomeException
	 */
	public short deletePin(Context ctx, String msisdn,
			String erReference);

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
	public int changeMsisdn(Context ctx, Account account, String oldMsisdn,
			String newMsisdn, String erReference);

	/**
	 * @param userName :
	 *            Login of the user performing the operation.
	 * @param pwd :
	 *            Password of the user performing the operation.
	 * @return : PinManagerService is returned after authentication by the PIN
	 *         Manager.PinManagerService is used for accessing the supported methods.
	 * @throws HomeException
	 */
//	public PinManagerService login(String userName, String pwd)
//			throws HomeException;
	
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
    public short queryAuthenticatedFlag(String msisdn, int subscriptionType, String erReference, BooleanHolder status)
    throws HomeException;
    
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
    public short setAuthenticatedFlag(Context ctx, String msisdn, int subscriptionType, boolean authenicated, String erReference)
    throws HomeException;

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
    public short changePin(Context ctx, String msisdn, String oldPinNumber, String newPinNumber1, String newPinNumber2,
            String erReference);

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
    public short verifyPin(Context ctx, String msisdn, String pinNumber, String erReference);
    
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
    public short setPin(Context ctx, String msisdn, Integer spid, String newPinNumber, String erReference);
    
}