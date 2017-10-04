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
package com.trilogy.app.crm.home.account;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.RegistrationStatusEnum;
import com.trilogy.app.crm.config.AccountRequiredField;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.web.border.AccountIdentificationValidator;


/**
 * Validator that checks configurable required values (mandatory & registration) for Account creation/update.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.6
 */
public class AccountRequiredFieldValidator implements Validator
{
    public AccountRequiredFieldValidator()
    {
        this(true, true, true);
    }
    
    public AccountRequiredFieldValidator(boolean validateMandatory, boolean validateRegistration, boolean warnOnRegistrationErrors)
    {
        validateMandatory_ = validateMandatory;
        validateRegistration_ = validateRegistration;
        warnOnRegistrationErrors_ = warnOnRegistrationErrors;
    }
    
    /**
     * {@inheritDoc}
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        CompoundIllegalStateException failures = new CompoundIllegalStateException();
        
        if (obj instanceof Account)
        {
            Account account = (Account) obj;
            Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
            
            Map<String, AccountRequiredField> registrationFields = new HashMap<String, AccountRequiredField>();
            if (account.getRegistrationStatus() != RegistrationStatusEnum.NOT_APPLICABLE_INDEX
                    && isValidateRegistration(ctx, account))
            {
                registrationFields = account.getRegistrationFields(ctx);
            }
            Map<String, AccountRequiredField> mandatoryFields = new HashMap<String, AccountRequiredField>();
            if (isValidateMandatory(ctx))
            {
                mandatoryFields = account.getMandatoryFields(ctx);
            }

            List<PropertyInfo> properties = AccountXInfo.instance().getProperties(ctx);
            if (properties != null)
            {
                for (PropertyInfo prop : properties)
                {
                    if (prop != null)
                    {
                        AccountRequiredField[] fields = new AccountRequiredField[]
                                                                                 {
                            registrationFields.get(prop.getName()),
                            mandatoryFields.get(prop.getName())
                                                                                 };
                        for (int i=0; i<2; i++)
                        {
                            AccountRequiredField field = fields[i];
                            if (!isFieldRequiredForAccount(ctx, field, account))
                            {
                                continue;
                            }
                            
                            boolean isRegistrationCheck = (i == 0);

                            // There are some cases where we don't want validation errors
                            // to prevent a bean update from taking place.
                            boolean treatFailuresAsWarnings = false;
                            if (isRegistrationCheck
                                    && account.getRegistrationStatus() != RegistrationStatusEnum.REGISTERED_INDEX)
                            {
                                // Case 1: This is a registration check and it is a case where
                                //         we expect to warn on registration error(s).
                                treatFailuresAsWarnings = isWarnOnRegistrationErrors(ctx, account);
                            }
                            else if (oldAccount != null)
                            {
                                // Case 2: The old account's value was also applicable to
                                //         this required value and was also invalid.
                                //         It is likely that such failures are caused by
                                //         required value config change after account
                                //         creation.
                                if (field.getPredicate() == null
                                        || field.getPredicate().f(ctx, oldAccount))
                                {
                                    treatFailuresAsWarnings = !isPropertyValueValid(
                                            ctx, 
                                            oldAccount, prop,
                                            isRegistrationCheck);
                                }
                            }

                            ExceptionListener warnings = (ExceptionListener) ctx.get(HTMLExceptionListener.class);
                            if (warnings == null)
                            {
                                warnings = (ExceptionListener) ctx.get(ExceptionListener.class);
                            }
                            
                            validateRequiredFieldValue(ctx, 
                                    account, prop, 
                                    isRegistrationCheck, treatFailuresAsWarnings,
                                    failures, warnings);
                        }
                    }
                }
            }
        }
        
        failures.throwAll();
    }

    
    public boolean isValidateMandatory(Context ctx)
    {
        return validateMandatory_;
    }
    
    
    public boolean isValidateRegistration(Context ctx, Object obj)
    {
        LicenseMgr lMgr = (LicenseMgr) ctx.get(LicenseMgr.class);
        boolean result = false;
        if (lMgr.isLicensed(ctx, LicenseConstants.ACCOUNT_REGISTRATION))
        {
            result = validateRegistration_;
            if (!result && obj instanceof Account)
            {
                // Must validate registration errors if a non-registered -> registered state change is underway
                result = isRegistrationInProgress(ctx, (Account) obj);
            }
        }
        return result;
    }

    
    public boolean isWarnOnRegistrationErrors(Context ctx, Object obj)
    {
        boolean result = warnOnRegistrationErrors_;
        if (result && obj instanceof Account)
        {
            // Must not warn on validation errors if a non-registered -> registered state change is underway
            result = !isRegistrationInProgress(ctx, (Account) obj);
        }
        return result;
    }

    
    protected boolean isFieldRequiredForAccount(Context ctx, AccountRequiredField field, Account account)
    {
        if (field == null || account == null)
        {
            return false;
        }
        
        PropertyInfo prop = field.getPropertyInfo();
        if (field.getPredicate() != null
                && !field.getPredicate().f(ctx, account))
        {
            // Ignore this required value if the account is not applicable to it.
            return false;
        }

        if (field.isDefaultValueValid())
        {
            Object defaultValue = prop.getDefault();
            Object currentValue = prop.get(account);
            if (SafetyUtil.safeEquals(defaultValue, currentValue))
            {
                // Ignore default values that are configured to be valid.  This
                // is common for enums or other integer data types.
                return false;
            }
        }
        
        return true;
    }

    
    protected boolean isRegistrationInProgress(Context ctx, Account account)
    {
        boolean result = false;
        
        if (account != null
                && account.getRegistrationStatus() == RegistrationStatusEnum.REGISTERED_INDEX)
        {
            Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
            if (oldAccount == null
                    || oldAccount.getRegistrationStatus() != RegistrationStatusEnum.REGISTERED_INDEX)
            {
                result = true;
            }
        }
        
        return result;
    }

    protected boolean isPropertyValueValid(Context ctx, 
            Account account, PropertyInfo prop,
            boolean isRegistrationCheck)
    {
        CompoundIllegalStateException localCise = new CompoundIllegalStateException();
        try
        {
            validateRequiredFieldValue(ctx, 
                    account, prop, 
                    isRegistrationCheck, false,
                    localCise, localCise);
            if (localCise.getSize() > 0)
            {
                return false;
            }
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    protected void validateRequiredFieldValue(Context ctx, 
            Account account, PropertyInfo prop,
            boolean isRegistrationCheck, boolean treatFailureAsWarning,
            CompoundIllegalStateException failures, ExceptionListener warnings)
    {
        ExceptionListener handler = failures;
        if (treatFailureAsWarning)
        {
            handler = warnings;
        }
        
        try
        {
            Object defaultValue = prop.getDefault();
            Object currentValue = prop.get(account);

            if (currentValue == null
                    || (currentValue instanceof String 
                            && (((String)currentValue).trim().length() == 0))
                            || SafetyUtil.safeEquals(defaultValue, currentValue))
            {
                submitMissingValueError(
                        ctx,
                        prop, 
                        isRegistrationCheck,  
                        handler);
            }
            else if (AccountXInfo.IDENTIFICATION_GROUP_LIST.equals(prop))
            {
            		 Context idCtx = ctx.createSubContext();
            		 idCtx.put(AccountIdentificationValidator.FORCE_VALIDATION_CTX_KEY, isRegistrationCheck);
            		 AccountIdentificationValidator.instance().validate(idCtx, account);
            	
            }
            else if (AccountXInfo.SECURITY_QUESTIONS_AND_ANSWERS.equals(prop))
            {
                CRMSpid sp = HomeSupportHelper.get(ctx).findBean(ctx, CRMSpid.class, account.getSpid());
                if (sp != null)
                {
                	if(sp.getSkipSecurityAndIdentityCheck() && !account.isResponsible()) 
                		 	return;
                	
                    int minNumSecurityQuestions = sp.getMinNumSecurityQuestions();
                    if (minNumSecurityQuestions > 0)
                    {
                        List l = account.getSecurityQuestionsAndAnswers();
                        if(l == null
                                || l.size() < minNumSecurityQuestions)
                        {
                            submitNewValidationError(
                                    ctx,
                                    prop, 
                                    new IllegalPropertyArgumentException(AccountXInfo.SECURITY_QUESTIONS_AND_ANSWERS, 
                                            "At least " + minNumSecurityQuestions + " security questions must be entered."), 
                                    isRegistrationCheck, 
                                    handler);
                        }
                    }
                }
                else
                {
                    submitNewValidationError(
                            ctx,
                            prop, 
                            new IllegalPropertyArgumentException(AccountXInfo.SPID, "SPID " + account.getSpid() + " not found."),
                            isRegistrationCheck, 
                            handler);
                }
            }
        }
        catch (Exception e)
        {
            // Catch all errors and forward exceptions to appropriate listener
            submitNewValidationError(
                    ctx,
                    prop, 
                    e, 
                    isRegistrationCheck, 
                    handler);
        }
    }

    protected void submitMissingValueError(Context ctx, PropertyInfo prop,
            boolean isRegistrationFailure, ExceptionListener handler)
    {
        submitNewValidationError(ctx, prop, null, isRegistrationFailure, handler);
    }

    protected void submitNewValidationError(Context ctx, PropertyInfo prop, Exception newFailure,
            boolean isRegistrationFailure, ExceptionListener handler)
    {
        int numFailures = 1;
        if (newFailure instanceof CompoundIllegalStateException)
        {
            numFailures = ((CompoundIllegalStateException) newFailure).getSize();
        }
        if (numFailures > 0)
        {
            StringBuilder msg = new StringBuilder("Value required");
            if (isRegistrationFailure)
            {
                msg.append(" for registration");
            }
            
            if (handler != null)
            {
                msg.append(".");
                if (newFailure != null)
                {
                    msg.append("  ");
                    msg.append(numFailures);
                    msg.append(" failure(s) follow.");
                }
                handler.thrown(new IllegalPropertyArgumentException(prop, msg.toString()));
                if (newFailure != null)
                {
                    handler.thrown(newFailure);
                }
            }
        }
    }

    protected boolean validateRegistration_;
    protected boolean validateMandatory_;
    protected boolean warnOnRegistrationErrors_;
}
