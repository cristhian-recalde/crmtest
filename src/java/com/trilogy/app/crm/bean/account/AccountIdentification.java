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
package com.trilogy.app.crm.bean.account;

import com.trilogy.app.crm.bean.BlackTypeEnum;
import com.trilogy.app.crm.bean.account.AbstractAccountIdentification;
import com.trilogy.app.crm.blacklist.BlackListSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

public class AccountIdentification
    extends AbstractAccountIdentification
{
    /**
     * Serial version UID.
     */
    public static final long serialVersionUID = 3870492883711976831L;

    public AccountIdentification()
    {
    }

    public void setContext(Context ctx)
    {
        ctx_ = ctx;
    }

    public Context getContext()
    {
        return ctx_;
    }

    public BlackTypeEnum getIsIdListed()
        throws HomeException
    {
        return getIsIdListed(getContext());
    }

    public BlackTypeEnum getIsIdListed(final Context ctx)
        throws HomeException
    {
        return BlackListSupport.getIdListType(ctx, getIdType(), getIdNumber());
    }

    private transient Context ctx_;
}