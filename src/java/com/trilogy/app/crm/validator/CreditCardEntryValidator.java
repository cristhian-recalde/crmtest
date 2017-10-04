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
package com.trilogy.app.crm.validator;

import java.util.regex.Pattern;

import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CreditCardEntry;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.creditcard.CreditCardEntryAware;

/**
 * Credit Card Validator.  Performs validation
 * on all the credit card entry fields.<BR>
 * <BR>
 * All the validation for credit card is done here
 * because this bean appears and disappears depending on a flag
 * in Subscriber and Account.  Unfortunately, when clearing this flag,
 * the validation still happends on the Credit Card Entries
 * causing error messages to be shown, so moving the validation
 * to this class allows us to check the flag and determine
 * if we need to perform the validation.
 *
 * @author danny.ng@redknee.com
 */
public class CreditCardEntryValidator implements Validator
{
    public CreditCardEntryValidator()
    {
        super();
    }

    public static CreditCardEntryValidator instance()
    {
        return INSTANCE;
    }

    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final CreditCardEntryAware bean = (CreditCardEntryAware) obj;

        if (!bean.isCreditCardPayment())
        {
            return;
        }
        final CreditCardEntry entry = bean.getCreditCardInfo();
        if (entry == null)
        {
            if (obj instanceof Subscriber)
            {
                final Subscriber sub = (Subscriber) obj;
                throw new IllegalStateException("Subscriber " + sub.getId() + " has no credit card entry");
            }
            else if (obj instanceof Account)
            {
                final Account acct = (Account) obj;
                throw new IllegalStateException("Account " + acct.getBAN() + " has no credit card entry");
            }
        }

        /*
        * Check the card number
        */
        final CreditCardNumberValidator cardNumberValiator = CreditCardNumberValidator.instance();
        cardNumberValiator.validate(ctx, bean);

        /*
        * Check CVV
        */
        final CreditCardCvvValidator cardCvvValidator = CreditCardCvvValidator.instance();
        cardCvvValidator.validate(ctx, bean);

        /*
        * Check expiry date
        */
        checkExpiryDate(ctx, entry.getExpiryDate());
    }

    private void checkExpiryDate(final Context ctx, final String expiryDate)
    {
        if (!EXPIRYDATE_PATTERN.matcher(String.valueOf(expiryDate)).matches())
        {
            // TODO use IllegalPropertyArgumentException
            throw new IllegalStateException("Credit Card Expiry Date doesn't match pattern: "
                    + EXPIRYDATE_PATTERN.pattern() + "' .");
        }

        final int month = Integer.parseInt(expiryDate.substring(0, 2));
        if (month < 1 || month > 12)
        {
            // TODO use IllegalPropertyArgumentException
            throw new IllegalStateException("Invalid month. Please specify between 01 and 12");
        }
    }

    private static final CreditCardEntryValidator INSTANCE = new CreditCardEntryValidator();

    public static final Pattern EXPIRYDATE_PATTERN = Pattern.compile("^\\d{4}$");
}
