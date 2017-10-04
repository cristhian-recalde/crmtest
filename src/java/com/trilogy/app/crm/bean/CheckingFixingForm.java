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
package com.trilogy.app.crm.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;


/**
 * Provides detailed JavaBean implementation for the CheckingFixingForm model.
 * Primarily, this involves providing the ability to get a list of subscriber
 * references (identifiers, accounts, etc). from the form.
 *
 * @author gary.anderson@redknee.com
 */
public
class CheckingFixingForm
    extends AbstractCheckingFixingForm
{
    /**
     * Gets the list of account identifiers from the form.
     *
     * @return A collection of account identifiers specified in the form.  The
     * collection may be empty but is always non-null.
     */
    public Collection getAccountIdentifiers()
    {
        final String textArea = getAccountIdentifiersTextAreaString();

        final Collection identifiers = tokenize(textArea);

        return identifiers;
    }


    /**
     * Gets the list of subscriber identifiers from the form.
     *
     * @return A collection of subscriber identifiers specified in the form.
     * The collection may be empty but is always non-null.
     */
    public Collection getSubscriberIdentifiers()
    {
        final String textArea = getSubscriberIdentifiersTextAreaString();

        final Collection identifiers = tokenize(textArea);

        return identifiers;
    }


    /**
     * Gets the list of MSISDNs from the form.
     *
     * @return A collection of MSISDNS specified in the form.  The collection
     * may be empty but is always non-null.
     */
    public Collection getMsisdns()
    {
        final String textArea = getMsisdnsTextAreaString();

        final Collection identifiers = tokenize(textArea);

        return identifiers;
    }


    /**
     * Tokenizes a newline separated text and returns the tokens in a
     * collection.  The return list trims leading and trailing spaces from each
     * token, and ignoes empty tokens.
     *
     * @param text A newline separated text.
     * @return A collection of String tokens.  The list may be empty but will
     * never be null.
     */
    private Collection tokenize(final String text)
    {
        final StringTokenizer tokenizer = new StringTokenizer(text, "\n");

        final Collection identifiers = new ArrayList(tokenizer.countTokens());

        while (tokenizer.hasMoreTokens())
        {
            final String identifer = tokenizer.nextToken().trim();

            if (identifer.length() != 0)
            {
                identifiers.add(identifer);
            }
        }

        return identifiers;
    }


} // class
