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
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

/**
 * @author chandrachud.ingale
 * @since  9.6
 */
public class BlacklistWhitelistTemplateStoreValidator implements Validator
{
    private static final BlacklistWhitelistTemplateStoreValidator instance = new BlacklistWhitelistTemplateStoreValidator();


    private BlacklistWhitelistTemplateStoreValidator()
    {}


    public static BlacklistWhitelistTemplateStoreValidator getInstance()
    {
        return instance;
    }


    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        throw new IllegalStateException("Cannot modify Blacklist/Whitelist template.");
    }
}
