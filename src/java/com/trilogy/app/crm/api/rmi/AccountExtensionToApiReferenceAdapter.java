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
package com.trilogy.app.crm.api.rmi;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.extension.account.AccountExtension;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.util.crmapi.wsdl.v2_2.types.account.BaseAccountExtensionReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.account.extensions.PoolAccountExtensionReference;

/**
 * Adapts AccountExtension object to API reference objects.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class AccountExtensionToApiReferenceAdapter implements Adapter
{   
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        AccountExtension extension = (AccountExtension) obj;

        BaseAccountExtensionReference apiExtension = null;
        
        if (extension instanceof PoolExtension)
        {
            PoolExtension poolExtension = (PoolExtension) extension;
            
            PoolAccountExtensionReference poolReference = new PoolAccountExtensionReference();
            poolReference.setGroupMobileNumber(poolExtension.getPoolMSISDN());
            poolReference.setAccountID(extension.getBAN());
            
            apiExtension = poolReference;
        }
        
        return apiExtension;
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }
}
