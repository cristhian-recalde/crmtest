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
package com.trilogy.app.crm.api.rmi.support;

import java.util.Collection;
import java.util.List;

import com.trilogy.app.crm.api.rmi.ExceptionListenerBridge;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Package;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.language.MessageMgrAware;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.SeverityEnum;
import com.trilogy.framework.xlog.log.SeverityLogMsg;
import com.trilogy.product.s2100.ErrorCode;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.CRMException;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.CompoundException;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.DataCreationException;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.DataDeletionException;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.DataStoreException;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.ValidationException;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.ValidationExceptionEntry;
import com.trilogy.util.crmapi.wsdl.v2_1.exception.ExceptionCode;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.AccountState;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.AccountStateEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionState;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionStateEnum;
import com.trilogy.util.crmapi.wsdl.v2_2.types.account.AccountProfileCreationException;
import com.trilogy.util.crmapi.wsdl.v2_2.types.cardpackage.CardPackageCreationException;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.SubscriptionProfileCreationException;
import com.trilogy.util.crmapi.wsdl.v2_2.types.transaction.TransactionCreationException;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.exception.CRMExceptionFactory;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountStateTransitionException;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionStateTransitionException;
import com.trilogy.framework.xhome.beans.AbstractIllegalPropertyArgumentException;

/** 
 * These are all of the Error Reporting Utilities for CRM API.
 * @author ali
 *
 */
public class RmiApiErrorHandlingSupport 
{
    public static com.redknee.util.crmapi.wsdl.v2_3.api.CRMExceptionFault adaptFault(com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault e)
    {
        com.redknee.util.crmapi.wsdl.v2_0.exception.CRMException crmException = null;
        final com.redknee.util.crmapi.wsdl.v2_0.exception.CRMExceptionE faultMessage = e.getFaultMessage();
        if (faultMessage != null)
        {
            crmException = faultMessage.getCRMException();
        }
        return com.redknee.util.crmapi.wsdl.v2_3.exception.CRMExceptionFactory.create(crmException, e);
    }

    public static com.redknee.util.crmapi.wsdl.v2_2.api.CRMExceptionFault adaptFault(com.redknee.util.crmapi.wsdl.v2_3.api.CRMExceptionFault e)
    {
        com.redknee.util.crmapi.wsdl.v2_0.exception.CRMException crmException = null;
        final com.redknee.util.crmapi.wsdl.v2_0.exception.CRMExceptionE faultMessage = e.getFaultMessage();
        if (faultMessage != null)
        {
            crmException = faultMessage.getCRMException();
        }
        return com.redknee.util.crmapi.wsdl.v2_2.exception.CRMExceptionFactory.create(crmException, e);
    }
    
    public static com.redknee.util.crmapi.wsdl.v2_1.api.CRMExceptionFault adaptFault(com.redknee.util.crmapi.wsdl.v2_2.api.CRMExceptionFault e)
    {
        com.redknee.util.crmapi.wsdl.v2_0.exception.CRMException crmException = null;
        final com.redknee.util.crmapi.wsdl.v2_0.exception.CRMExceptionE faultMessage = e.getFaultMessage();
        if (faultMessage != null)
        {
            crmException = faultMessage.getCRMException();
            if (crmException instanceof AccountProfileCreationException)
            {
                AccountProfileCreationException v22Exception = (AccountProfileCreationException) crmException;
                DataCreationException legacyException = new DataCreationException();
                legacyException.setMessage(v22Exception.getMessage());
                legacyException.setProfileCreated(v22Exception.getProfileCreated());
                crmException = legacyException;
            }
            else if (crmException instanceof SubscriptionProfileCreationException)
            {
                SubscriptionProfileCreationException v22Exception = (SubscriptionProfileCreationException) crmException;
                DataCreationException legacyException = new DataCreationException();
                legacyException.setMessage(v22Exception.getMessage());
                legacyException.setProfileCreated(v22Exception.getProfileCreated());
                crmException = legacyException;
            }
            else if (crmException instanceof TransactionCreationException)
            {
                TransactionCreationException v22Exception = (TransactionCreationException) crmException;
                DataCreationException legacyException = new DataCreationException();
                legacyException.setMessage(v22Exception.getMessage());
                legacyException.setProfileCreated(v22Exception.getProfileCreated());
                crmException = legacyException;
            }
            else if (crmException instanceof CardPackageCreationException)
            {
                CardPackageCreationException v22Exception = (CardPackageCreationException) crmException;
                DataCreationException legacyException = new DataCreationException();
                legacyException.setMessage(v22Exception.getMessage());
                legacyException.setProfileCreated(v22Exception.getProfileCreated());
                crmException = legacyException;
            }
        }
        
        return com.redknee.util.crmapi.wsdl.v2_1.exception.CRMExceptionFactory.create(crmException, e);
    }
    
    /**
     * Throws CRMException to handle returning an error due to limit be higher than configured value.
     * @param context TODO
     * @param input
     * @param max_size
     * @throws CRMException
     */
    public static void validateLimitInput(Context context, final Integer input, final int max_size)
        throws CRMExceptionFault
    {
        if ( input.intValue() <= 0 )
        {
            simpleValidation("limit", "The input limit value is smaller than or equal to 0");
        }
        if ( input.intValue() > max_size )
        {
            final String msg = "The input limit is " + input + ", but the CRM Service Provider defined maximum allowed limit size is " + max_size;
            generalException(context, null, msg, ExceptionCode.MAXIMUM_LIMIT_EXCEEDED, "validateLimitInput");
        }
    }

	public static Long validateLongPageKey(Context context, final String pageKey) throws CRMExceptionFault
	{
	    Long result = null;
	    if (pageKey != null && pageKey.length() > 0)
	    {
	        try
	        {
	            result = Long.parseLong(pageKey);
	        }
	        catch (NumberFormatException e)
	        {
	            final String msg = "Invalid pageKey \"" + pageKey + "\". Please only provide pageKeys returned by API.";
	            generalException(context, e, msg, ExceptionCode.INVALID_PAGEKEY, "validateLongPageKey");
	        }
	    }
	    return result;
	}
	
	public static boolean validateAysnNotesPushSupported(Context context, final int spid) throws CRMExceptionFault
	{
		final CRMSpid spidObj = RmiApiSupport.getCrmServiceProvider(context, spid, RmiApiErrorHandlingSupport.class);

    	if(spidObj== null)
    	{
	            final String msg = "Invalid spid \"" + spid + "\". Please provide valid spid.";
	            generalException(context, null, msg, ExceptionCode.INVALID_SPID, "validateAysnNotesPushSupported");
	            //Code will not continue further because the above method throws exception.
	    }
    	
    	if(spidObj.getAutopushNotesToDcrm())
    	{
	            final String msg = "Notes are automatically pushed by the system during creation for spid \"" + spid + "\". Asynchronous Notes Push is not supported.";
	            generalException(context, null, msg, ExceptionCode.UNKNOWN_EXCEPTION, "validateAysnNotesPushSupported");
	            //Code will not continue further because the above method throws exception.
	    }

    	return true;
	}

	/**
	 * Throws CRMException to handle returning an error due to empty Mandatory fields.
	 * @param value
	 * @param label
	 * @throws CRMException
	 */
	public static void validateMandatoryObject(final Object value, final String label) throws CRMExceptionFault
	{
	    if (value == null)
	    {
	        final String msg = label + " is null. " + label + " is Mandatory and cannot be null";
	        RmiApiErrorHandlingSupport.simpleValidation(label, msg);
	    }
	}

	/*
	 * 
	 */
    public static void inSufficientFundValidation(final String name, final String explanation) throws CRMExceptionFault
    {
        final ValidationException exception = new ValidationException();
        exception.setCode(ExceptionCode.INSUFFICIENT_BALANCE);
        exception.setMessage(explanation);
        final ValidationExceptionEntry[] entries = new ValidationExceptionEntry[1];
        entries[0] = new ValidationExceptionEntry();
        exception.setEntries(entries);
        entries[0].setName(name);
        entries[0].setExplanation(explanation);
        throw CRMExceptionFactory.create(exception);
    }
	
	/**
	 * Throws Validation Exception with one entry containing the given information
	 * @param name
	 * @param explanation
	 * @throws ValidationException
	 */
	public static void simpleValidation(final String name, final String explanation) throws CRMExceptionFault
	{   
	    final ValidationException exception = new ValidationException();
	    exception.setCode(ExceptionCode.VALIDATION_EXCEPTION);
	    exception.setMessage(explanation);
	    
	    final ValidationExceptionEntry[] entries = new ValidationExceptionEntry[1];
	    entries[0] = new ValidationExceptionEntry();
	    exception.setEntries(entries);
	
	    entries[0].setName(name);
	    entries[0].setExplanation(explanation);
	
        throw CRMExceptionFactory.create(exception);
	}
	
	/**
	 * Add to the given List a ValidationExceptionEntry with the given parameters.
	 * @param validations
	 * @param field
	 * @param explanation
	 */
	public static void addSimpleValidationEntry(final List<ValidationExceptionEntry> validations, final String field, final String explanation)
	{
		final ValidationExceptionEntry entry = new ValidationExceptionEntry();
        entry.setName(field);
        entry.setExplanation(explanation);
        validations.add(entry);
	}

	/**
	 * Throws CRMException to handle the given Exception (with non-specific origins)
	 * @param ctx
	 * @param e
	 * @param msg
	 * @param caller
	 * @throws CRMException
	 */
	public static void generalException(final Context ctx, final Exception e, final String msg, final Object caller)
	    throws CRMExceptionFault
	{
		generalException(ctx, e, msg, ExceptionCode.GENERAL_EXCEPTION, caller);
	}
    
	public static void generalException(final Context ctx, final Exception e, final String msg, 
			final long code, final Object caller)
	    throws CRMExceptionFault
	{
		if (e != null)
	    {
            log(ctx, SeverityEnum.MINOR, caller, msg, e);
	    }
		
	    final CRMException exception = new CRMException();
	    exception.setCode(code);
	    if (e != null)
	    {
	        exception.setMessage(msg + " (Caused by: " + e.getMessage() + ")");
	    }
	    else
	    {
	        exception.setMessage(msg);
	    }
	    throw CRMExceptionFactory.create(exception);
	}


    public static void accountStateTransitionException(final Context ctx, final Exception e, final String msg,
            final AccountState oldState, final AccountState requestedState, final AccountState currentState,
            final Object caller) throws CRMExceptionFault
    {
        if (e != null)
        {
            log(ctx, SeverityEnum.MINOR, caller, msg, e);
        }
        
        final AccountStateTransitionException exception = new AccountStateTransitionException();
        exception.setCode(ExceptionCode.ACCOUNT_STATE_TRANSITION_EXCEPTION);
        exception.setCurrentState(currentState);
        exception.setOldState(oldState);
        exception.setRequestedState(requestedState);
        if (e != null)
        {
            exception.setMessage(msg + " (Caused by: " + e.getMessage() + ")");
        }
        else
        {
            exception.setMessage(msg);
        }
        throw CRMExceptionFactory.create(exception);
    }

    public static void subscriptionStateTransitionException(final Context ctx, final Exception e, final String msg,
            final SubscriptionState oldState, final SubscriptionState requestedState, final SubscriptionState currentState,
            final Object caller) throws CRMExceptionFault
    {
        if (e != null)
        {
            log(ctx, SeverityEnum.MINOR, caller, msg, e);
        }
        
        final SubscriptionStateTransitionException exception = new SubscriptionStateTransitionException();
        exception.setCode(ExceptionCode.SUBSCRIPTION_STATE_TRANSITION_EXCEPTION);
        exception.setCurrentState(currentState);
        exception.setOldState(oldState);
        exception.setRequestedState(requestedState);
        if (e != null)
        {
            exception.setMessage(msg + " (Caused by: " + e.getMessage() + ")");
        }
        else
        {
            exception.setMessage(msg);
        }
        throw CRMExceptionFactory.create(exception);
    }

    /**
	 * Throws CRMException to handle an Exception due to errors during Search process.
	 * @param ctx
	 * @param identifier
	 * @param caller
	 * @throws CRMException
	 */
	public static void identificationException(final Context ctx, final String identifier, final Object caller)
	    throws CRMExceptionFault
	{
		String msg = identifier + " does not exist in the system.";
	    generalException(ctx, null, msg, ExceptionCode.INVALID_IDENTIFICATION, caller);
	}
	
	   
    /**
     * Throws CRMException to handle errors in making adjustments on Payment Gateway
     * @param ctx
     * @param rejected
     * @param msg
     * @param caller
     * @throws CRMExceptionFault
     */
    public static void ocgTransactionException(final Context ctx, Exception e,  int ocgResultCode , final String msg, final Object caller) throws CRMExceptionFault
    {
        if(ErrorCode.NO_ERROR != ocgResultCode)
        {
            generalException(ctx, e, msg,(ErrorCode.TRANSACTION_TIMEDOUT == ocgResultCode)? ExceptionCode.PAYMENT_CHARGE_UNCONFIRMED : ExceptionCode.PAYMENT_CHARGE_FAILED, caller);
        }
    }

	/**
	 * Throws CRMException to containing the list of Validation entries given
	 * @param validations
	 * @throws ValidationException
	 */
	public static void compoundValidation(final List<ValidationExceptionEntry> validations) throws CRMExceptionFault
	{
	    final int count = validations.size();
	    if (count == 0)
	    {
	        return;
	    }
	
	    final ValidationExceptionEntry[] entries;
	    entries = validations.toArray(new ValidationExceptionEntry[count]);
	
	    final ValidationException exception = new ValidationException();
	    exception.setCode(ExceptionCode.VALIDATION_EXCEPTION);
	    exception.setEntries(entries);
	
	    throw CRMExceptionFactory.create(exception);
	}

	/**
	 * Throws CRMException to deal with the given CompoundIllegalStateExceptions 
	 * @param ctx
	 * @param compound
	 * @param operationSuccess TODO
	 * @throws ValidationException
	 */
	public static void compoundValidation(
			final Context ctx, 
			final CompoundIllegalStateException compound, 
			final HomeOperationEnum operation,
			final boolean operationSuccess, 
			Class<? extends AbstractBean> objectType, Object objectRef,
			final Object caller)
	    throws CRMExceptionFault
	{
		/* Since CompoundIllegalStateException can contain a mix of IllegalPropertyArgumentException,
		 * IllegalStateException, and any Throwable known in existence, we have to create an error
		 * hierarchy.  CRM will throw Validation errors over all other errors.
		 * Only IllegalProperyArgumentExceptions map to ValidationException (bean) since they have 
		 * information about the erring field and why it is wrong.  All other Exceptions lack this 
		 * information and so will be generalized in CRMExceptions.  It would not make sense to 
		 * throw all the errors we know about when we have a mix of both Validation errors and 
		 * Processing errors, when a fix of the input parameters may fix the processing errors.
		 */
	    final ExceptionListenerBridge el = new ExceptionListenerBridge(ctx, compound.getSize());
	    compound.rethrow(el);
	    
	    final ValidationExceptionEntry[] validationErrors = el.getValidationExceptionEntries();
	    final CRMException[] generalExceptionEntries = el.getGeneralExceptionEntries();
	    
	    if (validationErrors != null && validationErrors.length > 0)
	    {
	    	ValidationException exception = new ValidationException();
	    	exception.setMessage(validationErrors[0].getExplanation());
	    	exception.setCode(ExceptionCode.VALIDATION_EXCEPTION);
	    	exception.setEntries(validationErrors);
	    	throw CRMExceptionFactory.create(exception);
	    }
	    else if (generalExceptionEntries != null && generalExceptionEntries.length > 0)
	    {
	        if (generalExceptionEntries.length == 1)
	        {
                CRMException details = generalExceptionEntries[0];
                //Throw the single CRM Exception depending on operation
                if (operation == HomeOperationEnum.STORE)
                {
                    RmiApiErrorHandlingSupport.storeException(ctx, null, details.getMessage(), operationSuccess, caller);
                }
                else if (operation == HomeOperationEnum.CREATE)
                {
                    RmiApiErrorHandlingSupport.creationException(ctx, null, details.getMessage(), operationSuccess, objectType, objectRef, caller);
                }
                else if (operation == HomeOperationEnum.REMOVE)
                {
                    RmiApiErrorHandlingSupport.deletionException(ctx, null, details.getMessage(), operationSuccess, caller);
                }
                else
                {
                    // General Exception
                    throw CRMExceptionFactory.create(generalExceptionEntries[0]);
                }
	        }
	        else
	        {
                // Multiple errors
                CompoundException exception = new CompoundException();
                exception.setMessage(generalExceptionEntries[0].getMessage());
                exception.setCompoundEntries(generalExceptionEntries);
                throw CRMExceptionFactory.create(exception);
            }
	    }
	    else
	    {
	        // This should never happen, but if it does (i.e. we get an empty CompoundIllegalStateException)
	        // then we have to still log it and return something to the caller because the request was not processed
	        // successfully.  If we do see this log message, find the source and fix it.
	    	String msg = compound.getMessage();
	        new MinorLogMsg(caller, "Unspecified validation exception: " + msg, compound).log(ctx);
	        simpleValidation("Unknown", msg);
	    }
	}

	/**
	 * Throws CRMException to handle the given Exception occurring during the Profile Creation process.
	 * @param ctx
	 * @param e
	 * @param msg
	 * @param profileCreated
	 * @param caller
	 * @throws CRMException
	 */
	private static void reviewCauseForCreationException(final Context ctx, final Exception e, final String msg,
	        final boolean profileCreated, final Class<? extends AbstractBean> objectType, final Object objectRef, final Object caller) throws CRMExceptionFault
	{
	    Throwable cause = null;
	    if (e != null)
	    {
	        cause = e.getCause();
	    }
	
	    if (e != null && e instanceof CompoundIllegalStateException)
	    {
	    	compoundValidation(ctx, (CompoundIllegalStateException) e, HomeOperationEnum.CREATE, profileCreated, objectType, objectRef, caller);
	    }
	    else if (cause != null && cause instanceof CompoundIllegalStateException)
	    {
	    	compoundValidation(ctx, (CompoundIllegalStateException) cause, HomeOperationEnum.CREATE, profileCreated, objectType, objectRef, caller);
	    }
	    else
	    {
	        RmiApiErrorHandlingSupport.creationException(ctx, e, msg, profileCreated, objectType, objectRef, caller);
	    }
	}

	/**
	 * Throws the DataCreationException using the given details.
	 * @param ctx
	 * @param e
	 * @param msg
	 * @param wasProfileCreated TRUE if the profile is persistent in the system.
	 * @param caller
	 * @throws CRMException
	 */
	private static void creationException(final Context ctx, final Exception e, final String msg,
	        boolean wasProfileCreated, final Class<? extends AbstractBean> objectType, final Object objectRef, final Object caller) 
		throws CRMExceptionFault
	{
        if (e != null)
        {
            log(ctx, SeverityEnum.MINOR, caller, msg, e);
        }
        
        final DataCreationException exception; 
        if (objectType != null 
                && Account.class.isAssignableFrom(objectType))
        {
            AccountProfileCreationException apce = new AccountProfileCreationException();
            exception = apce;
            
            Account account = (Account) lookupBean(ctx, objectType, objectRef);
            if (account != null)
            {
                wasProfileCreated = true;
                apce.setIdentifier(account.getBAN());
                apce.setState(AccountStateEnum.valueOf(account.getState().getIndex()));
            }
        }
        else if (objectType != null 
                && Subscriber.class.isAssignableFrom(objectType))
        {
            SubscriptionProfileCreationException spce = new SubscriptionProfileCreationException();
            exception = spce;

            Subscriber sub = (Subscriber) lookupBean(ctx, objectType, objectRef);
            if (sub != null)
            {
                wasProfileCreated = true;
                spce.setIdentifier(sub.getId());
                spce.setState(SubscriptionStateEnum.valueOf(sub.getState().getIndex()));
            }
        }
        else if (objectType != null 
                && Package.class.isAssignableFrom(objectType))
        {
            CardPackageCreationException cpce = new CardPackageCreationException();
            exception = cpce;

            Package pkg = (Package) lookupBean(ctx, objectType, objectRef);
            if (pkg != null)
            {
                wasProfileCreated = true;
                cpce.setIdentifier(pkg.getPackId());
            }
        }
        else if (objectType != null 
                && Transaction.class.isAssignableFrom(objectType))
        {
            TransactionCreationException tce = new TransactionCreationException();
            exception = tce;

            Transaction trans = (Transaction) lookupBean(ctx, objectType, objectRef);
            if (trans != null)
            {
                wasProfileCreated = true;
                tce.setIdentifier(String.valueOf(trans.getReceiptNum()));
            }
        }
        else
        {
            exception = new DataCreationException();
        }
        
	    exception.setCode(ExceptionCode.DATA_CREATION_EXCEPTION);
	    exception.setProfileCreated(wasProfileCreated);
	    
	    if (e != null)
	    {
	    	if(e instanceof OcgTransactionException)
	    	{
	    		OcgTransactionException ocge = (OcgTransactionException) e;
	    		if(ocge.getErrorCode() == ErrorCode.NOT_ENOUGH_BAL)
	    		{
	    			exception.setCode(ExceptionCode.INSUFFICIENT_BALANCE);
	    		}else if(ocge.getErrorCode() == ErrorCode.TRANSACTION_TIMEDOUT)
	    		{
	    			exception.setCode(ExceptionCode.TRANSACTION_TIMED_OUT);
	    		}
	    	}
	    	
	        exception.setMessage(msg + " " + e.getMessage());
	    }
	    else
	    {
	        exception.setMessage(msg);
	    }
	    throw CRMExceptionFactory.create(exception);
	}

    protected static Object lookupBean(final Context ctx, final Class<? extends AbstractBean> objectType, final Object objectRef)
    {
        Object result = null;
        
        IdentitySupport idSupport = (IdentitySupport) XBeans.getInstanceOf(ctx, objectType, IdentitySupport.class);
        if (idSupport.isKey(objectRef))
        {
            try
            {
                result = HomeSupportHelper.get(ctx).findBean(ctx, objectType, objectRef);
            }
            catch (HomeException he)
            {
                // NOP
            }
        }
        return result;
    }

	/**
	 * Throws CRMException to handle the given Exception occurring during the Profile Update(Store) process.
	 * Reviews the cause of the given Exception to ascertain the true cause and most appropriate
	 * CRMException to throw.
	 * Calling this method denotes the Store process was not saved to the CRM.
	 * @param ctx
	 * @param e Expected exceptions are IllegalStateException and HomeExceptions (that may decorate CompoundIllegalStateException)
	 * @param msg
	 * @param wasProfileStored TODO
	 * @param caller
	 * @throws CRMException
	 */
	private static void reviewCauseForStoreException(final Context ctx, final Exception e, final String msg, boolean wasProfileStored, final Object caller)
	    throws CRMExceptionFault
	{
	    Throwable cause = null;
	    if (e != null)
	    {
	        cause = e.getCause();
	    }
	
	    if (e != null && e instanceof CompoundIllegalStateException)
	    {
	    	// Using this to catch possible unchecked IllegalStateExceptions
	    	compoundValidation(ctx, (CompoundIllegalStateException) e, HomeOperationEnum.STORE, wasProfileStored, null, null, caller);
	    }
	    else if (cause != null && cause instanceof CompoundIllegalStateException)
	    {
	    	// Throw Exception cause's details
	    	compoundValidation(ctx, (CompoundIllegalStateException) cause, HomeOperationEnum.STORE, wasProfileStored, null, null, caller);
	    }
	    else
	    {
            // Throw Original exception details
	    	RmiApiErrorHandlingSupport.storeException(ctx, e, msg, wasProfileStored, caller);
	    }
	}

	/**
	 * Throws DataStoreException with the given details
	 * @param ctx
	 * @param e
	 * @param msg
	 * @param wasProfileStored TRUE indicates the update is persistent in the system. 
	 * @param caller
	 * @throws DataStoreException
	 */
	private static void storeException(final Context ctx, final Exception e, final String msg, 
			final boolean wasProfileStored, final Object caller)
		throws CRMExceptionFault
	{
        if (e != null)
        {
            log(ctx, SeverityEnum.MINOR, caller, msg, e);
        }
	    final DataStoreException exception = new DataStoreException();
	    exception.setCode(ExceptionCode.DATASTORE_EXCEPTION);
	    exception.setProfileStored(wasProfileStored);
	    if (e != null)
	    {
	        exception.setMessage(msg + " " + e.getMessage());
	    }
	    else
	    {
	        exception.setMessage(msg);
	    }
	    throw CRMExceptionFactory.create(exception);
	}

	/**
	 * Throws CRMException to handle the given Exception occurring during the Profile Deletion process.
	 * Calling this method denotes that the Profile was not deleted from CRM.
	 * @param ctx
	 * @param e
	 * @param msg
	 * @param wasProfileDeleted TRUE indicates the Delete was persistent and the profile no longer resides in the system.
	 * 				FALSE indicates the profile persists in the system. 
     * @param caller
	 * @throws CRMException
	 */
	private static void reviewCauseForDeletionException(final Context ctx, final Exception e, final String msg, 
			final boolean wasProfileDeleted, final Object caller)
	    throws CRMExceptionFault
	{
	    Throwable cause = null;
	    if (e != null)
		{
	    	cause = e.getCause();
		}
	    
	    if (e != null && e instanceof CompoundIllegalStateException)
	    {
	    	compoundValidation(ctx, (CompoundIllegalStateException) e, HomeOperationEnum.REMOVE, wasProfileDeleted, null, null, caller);
	    }
	    else if (cause != null && cause instanceof CompoundIllegalStateException)
	    {
	    	compoundValidation(ctx, (CompoundIllegalStateException) cause, HomeOperationEnum.REMOVE, wasProfileDeleted, null, null, caller);
	    }
	    else
	    {
	        RmiApiErrorHandlingSupport.deletionException(ctx, e, msg, wasProfileDeleted, caller);
	    }
	}

	private static void deletionException(final Context ctx, final Exception e, final String msg, 
			final boolean wasProfileDeleted, final Object caller)
	    throws CRMExceptionFault
	{
        if (e != null)
        {
            log(ctx, SeverityEnum.MINOR, caller, msg, e);
        }
	    final DataDeletionException exception = new DataDeletionException();
	    exception.setCode(ExceptionCode.DATA_DELETION_EXCEPTION);
	    if(e != null)
	    {
	    	exception.setMessage(msg + " " + e.getMessage());
	    }
	    else
	    {
	    	exception.setMessage(msg);
	    }
	    exception.setProfileDeleted(wasProfileDeleted);
	    throw CRMExceptionFactory.create(exception);
	}

	/**
	 * Delegate the general Error Handling Strategy for Profile Update (Store)
	 * TODO 
	 *   Determine if the Store operation was persisted.  Update the parameters passed in to reviewCauseForStoreException accordingly.
	 * @param context
	 * @param e
	 * @param msg
	 * @param caller
	 * @throws CRMException
	 */
	public static void handleStoreExceptions(Context context, Exception e, String msg, Object caller)
	    throws CRMExceptionFault
	{
	    if (e instanceof IllegalPropertyArgumentException)
	    {
            log(context, SeverityEnum.MINOR, caller, msg, e);
	        IllegalPropertyArgumentException ipae = (IllegalPropertyArgumentException)e;
            String propertyName = ipae.getPropertyName();
            
            final String messageText;
            MessageMgrAware mmgrAware = (MessageMgrAware)XBeans.getInstanceOf(context, ipae, MessageMgrAware.class);
            if (mmgrAware != null)
            {
                messageText = mmgrAware.toString(context, new MessageMgr(context, RmiApiErrorHandlingSupport.class));
            }
            else
            {
                messageText = ipae.getMessageText();
            }
            
            simpleValidation(propertyName, messageText);
	    }
	    else if(e instanceof IllegalArgumentException)
	    {
            log(context, SeverityEnum.MINOR, caller, msg, e);
            IllegalArgumentException iae = (IllegalArgumentException)e;
            AbstractIllegalPropertyArgumentException aipa =(AbstractIllegalPropertyArgumentException)iae;
            
            String propertyName = aipa.getPropertyName();
            final String messageText;
            MessageMgrAware mmgrAware = (MessageMgrAware)XBeans.getInstanceOf(context, iae, MessageMgrAware.class);
            if (mmgrAware != null)
            {
                messageText = mmgrAware.toString(context, new MessageMgr(context, RmiApiErrorHandlingSupport.class));
            }
            else
            {
                messageText = msg;
            }
            
            simpleValidation(propertyName, messageText);
	    }
	    else if (e instanceof IllegalStateException)
	    {
	    	// These exceptions are not validation related
	        reviewCauseForStoreException(context, e, msg, false, caller);
	    }
	    else
	    {
	        // Everything else including CompoundIllegalStateException and HomeException
	        reviewCauseForStoreException(context, e, msg, false, caller);
	    }
	}

	/**
	 * Delegate the general Error Handling Strategy for Profile Deletion.
	 * TODO 
	 *   Determine if the Delete operation was persisted.  Update the parameters passed in to reviewCauseForDeletionException accordingly.
	 * @param context
	 * @param e
	 * @param msg
	 * @param caller
	 * @throws CRMException
	 */
	public static void handleDeleteExceptions(Context context, Exception e, String msg, Object caller)
		throws CRMExceptionFault
	{
		if (e instanceof IllegalPropertyArgumentException)
	    {
            log(context, SeverityEnum.MINOR, caller, msg, e);
            IllegalPropertyArgumentException ipae = (IllegalPropertyArgumentException)e;
            String propertyName = ipae.getPropertyName();
            
            final String messageText;
            MessageMgrAware mmgrAware = (MessageMgrAware)XBeans.getInstanceOf(context, ipae, MessageMgrAware.class);
            if (mmgrAware != null)
            {
                messageText = mmgrAware.toString(context, new MessageMgr(context, RmiApiErrorHandlingSupport.class));
            }
            else
            {
                messageText = ipae.getMessageText();
            }
            
            simpleValidation(propertyName, messageText);
	    }
		else if (e instanceof IllegalStateException)
	    {
	    	// These exceptions are not validation related
	        reviewCauseForDeletionException(context, e, msg, false, caller);
	    }
		else
	    {
	        reviewCauseForDeletionException(context, e, msg, false, caller);
	    }
	}

	/**
	 * Delegate the general Error Handling Strategy for Profile Creation.
	 * @param context
	 * @param e
	 * @param msg
	 * @param wasSubscriberCreated
	 * @param caller
	 * @throws CRMException
	 */
	public static void handleCreateExceptions(Context context, Exception e, String msg, boolean wasSubscriberCreated, Class<? extends AbstractBean> objectType, Object objectRef, Object caller)
		throws CRMExceptionFault
	{
		if (e instanceof IllegalPropertyArgumentException)
	    {
            log(context, SeverityEnum.MINOR, caller, msg, e);
            IllegalPropertyArgumentException ipae = (IllegalPropertyArgumentException)e;
	        String propertyName = ipae.getPropertyName();
	        
	        final String messageText;
            MessageMgrAware mmgrAware = (MessageMgrAware)XBeans.getInstanceOf(context, ipae, MessageMgrAware.class);
            if (mmgrAware != null)
            {
                messageText = mmgrAware.toString(context, new MessageMgr(context, RmiApiErrorHandlingSupport.class));
            }
            else
            {
                messageText = ipae.getMessageText();
            }
            
            simpleValidation(propertyName, messageText);
	    }
		else if (e instanceof IllegalStateException)
	    {
	        reviewCauseForCreationException(context, e, msg, wasSubscriberCreated, objectType, objectRef, caller);
	    }
		else
	    {
	        reviewCauseForCreationException(context, e, msg, wasSubscriberCreated, objectType, objectRef, caller);
	    }
	}
	
	/**
	 * Delegate the general Error Handling Strategy for the Profile Query
	 * @param context
	 * @param e
	 * @param msg
	 * @param caller
	 * @throws CRMException
	 */
	public static void handleQueryExceptions(Context context, Exception e, String msg, Object caller)
	throws CRMExceptionFault
	{
		RmiApiErrorHandlingSupport.generalException(context, e, msg, caller);
	}


    public static void log(final Context ctx, final SeverityEnum severity, final Object caller, final String msg, final Exception e)
    {
        if (caller instanceof Class)
        {
            new SeverityLogMsg(severity, ((Class) caller).getName(), msg, e).log(ctx);
        }
        else
        {
            new SeverityLogMsg(severity, caller, msg, e).log(ctx);
        }
    }
    
    public static void assertExtensionUpdateEnabled(Context ctx, Class<? extends Extension> extType, Object caller) throws CRMExceptionFault
    {
        if (!ExtensionSupportHelper.get(ctx).isExtensionLicensed(ctx, extType))
        {
            final String msg = "Extension type not supported: " + extType.getName();
            handleStoreExceptions(ctx, null, msg, caller);
        }
    }
    
    public static void assertExtensionRemoveEnabled(Context ctx, Class<? extends Extension> extType, Object caller) throws CRMExceptionFault
    {
        if (!ExtensionSupportHelper.get(ctx).isExtensionLicensed(ctx, extType))
        {
            final String msg = "Extension type not supported: " + extType.getName();
            handleDeleteExceptions(ctx, null, msg, caller);
        }
    }
    
    public static void validatePricePlanWithBillingType(Context ctx, SubscriberTypeEnum subscriberTypeEnum, Long pricePlanID, final Object caller) throws CRMExceptionFault
    {
    	Collection<PricePlan> pricePlan;
    	And filter = new And();
    	filter.add(new EQ(PricePlanXInfo.PRICE_PLAN_TYPE, subscriberTypeEnum));
    	filter.add(new EQ(PricePlanXInfo.ID, pricePlanID));
    	try
    	{
    		pricePlan = HomeSupportHelper.get(ctx).getBeans(ctx, PricePlan.class, filter);
        	if(pricePlan.isEmpty())
        	{
        		identificationException(ctx, "Price Plan ID "+ Long.toString(pricePlanID), caller);
        	}
    	}
    	catch(HomeException e)
    	{
    		LogSupport.minor(ctx, PricePlan.class,
                    "Error getting Price Plan " + Long.toString(pricePlanID), e);
    		simpleValidation("Price Plan", "Error getting Price Plan " + Long.toString(pricePlanID) + "in CRM");
    	}
    }
}
