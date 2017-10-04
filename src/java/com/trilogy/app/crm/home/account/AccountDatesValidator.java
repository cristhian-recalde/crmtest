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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import java.util.TimeZone;
import java.text.SimpleDateFormat;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.MissingRequireValueException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xhome.webcontrol.DateWebControl;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.exception.RethrowExceptionListener;
import com.trilogy.app.crm.home.DateValidator;
import com.trilogy.app.crm.support.AgeValidationSupport;

/**
 * Validate dates in the {@link Account} bean are within range.
 *
 * @author cindy.wong@redknee.com
 */
public class AccountDatesValidator extends DateValidator
{
    /**
     * Create a new instance of <code>AccountDatesValidator</code>.
     */
    protected AccountDatesValidator()
    {
        super();
    }

    /**
     * Returns an instance of <code>AccountDatesValidator</code>.
     *
     * @return An instance of <code>AccountDatesValidator</code>.
     */
    public static AccountDatesValidator instance()
    {
        if (instance == null)
        {
            instance = new AccountDatesValidator();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Context context, final Object object)
    {
        final RethrowExceptionListener el = new RethrowExceptionListener();

        // get the new and old beans
        final Account newAccount = (Account) object;
        Account oldAccount = (Account) context.get(AccountConstants.OLD_ACCOUNT);

        if (oldAccount == null && HomeOperationEnum.STORE.equals(context.get(HomeOperationEnum.class)))
        {
            el.thrown(new IllegalPropertyArgumentException(AccountXInfo.BAN, "Unable to locate Account with BAN "
                    + newAccount.getBAN()));
            el.throwAllAsCompoundException();
        }

        final GeneralConfig config = (GeneralConfig) context.get(GeneralConfig.class);

        if( !newAccount.isPrepaid() )
        {
            try
            {
				validatePrior(context, oldAccount, newAccount,
				    AccountXInfo.CONTRACT_START_DATE, config);
                // protect against really, really off dates.
				validBefore(context, oldAccount, newAccount,
				    AccountXInfo.CONTRACT_START_DATE, Calendar.YEAR, 100);
            }
            catch (IllegalPropertyArgumentException exception)
            {
                el.thrown(exception);
            }

            try
            {
				validatePrior(context, oldAccount, newAccount,
				    AccountXInfo.CONTRACT_END_DATE, config);
                // protect against really, really off dates.
				validBefore(context, oldAccount, newAccount,
				    AccountXInfo.CONTRACT_END_DATE, Calendar.YEAR, 100);
            }
            catch (IllegalPropertyArgumentException exception)
            {
                el.thrown(exception);
            }
        }

        try
        {
			validateDateOfBirth(context, newAccount.getSpid(),
			    newAccount.getSystemType(), oldAccount, newAccount,
			    AccountXInfo.DATE_OF_BIRTH);
        }
        catch (IllegalPropertyArgumentException exception)
        {
            el.thrown(exception);
        }

        try
        {
			validAfter(context, oldAccount, newAccount,
			    AccountXInfo.PROMISE_TO_PAY_DATE, Calendar.DAY_OF_MONTH, 0);
            // TODO: what should be considered a reasonable upper bound for promise-to-pay date?
			validBefore(context, oldAccount, newAccount,
			    AccountXInfo.PROMISE_TO_PAY_DATE, Calendar.YEAR, 1);
        }
        catch (IllegalPropertyArgumentException exception)
        {
            el.thrown(exception);
        }

        el.throwAllAsCompoundException();
    }

    /**
     * Validates a date of birth field of a bean. Only the date part of the date
     * is validated; time is not.
     * @param spid
     *            SPID of the new bean.
     * @param billingType
     *            Billing type of the new bean.
     * @param oldBean
     *            The older version of the bean, <code>null</code> if none
     *            exists.
     * @param newBean
     *            The bean being validated.
     * @param dateOfBirthProperty
     *            The property being validated.
     * 
     * @throws IllegalPropertyArgumentException
     *             Thrown if the date of birth is outside the allowed range.
     */
	public static void validateDateOfBirth(final Context ctx, int spid,
	    SubscriberTypeEnum billingType, final Object oldBean,
        final Object newBean, final PropertyInfo dateOfBirthProperty) throws IllegalPropertyArgumentException
    {
    	/*
    	 * [Cindy Wong] 2011-01-23: Don't validate if DOB hasn't changed; this
    	 * makes sure account updates don't break after SPID extension is
    	 * installed and DOB hasn't been modified.
    	 */
		if (needsValidation(oldBean, newBean, dateOfBirthProperty)

		/*
		 * [Cindy Wong] 2011-04-13: validate age on move.
		 */
		    || isSystemTypeChange(oldBean, newBean))
        {
            final Date date = (Date) dateOfBirthProperty.get(newBean);
            
    		/*
    		 * [Cindy Wong] 2011-01-23: Falls back to validating age using the
    		 * deployment-wide limits in GeneralConfig.
    		 */
    		if (!AgeValidationSupport.validateAge(ctx, spid, billingType, date,
    		    true))
            {
                if (date == null)
                {
                    throw new MissingRequireValueException(dateOfBirthProperty);
                }
                
                SimpleDateFormat df = DateWebControl.instance().getFormatter(ctx);
                final TimeZone tz = (TimeZone) ctx.get(TimeZone.class);
                if (tz != null)
                {
                    df.setTimeZone(tz);
                }
    			throw new IllegalPropertyArgumentException(dateOfBirthProperty,
    			    dateOfBirthProperty.getLabel(ctx) + " \""
    			        + df.format(date.getTime())
    			        + "\" is outside the allowed range");
            }
        }
    }

	private static boolean isSystemTypeChange(Object oldBean, Object newBean)
	{
		boolean result = false;
		if (oldBean != null && newBean != null)
		{
			Account oldAccount = (Account) oldBean;
			Account newAccount = (Account) newBean;
			result =
			    !SafetyUtil.safeEquals(oldAccount.getSystemType(),
			        newAccount.getSystemType());
		}
		return result;
	}

	/**
     * Singleton instance.
     */
    private static AccountDatesValidator instance = new AccountDatesValidator();

}
