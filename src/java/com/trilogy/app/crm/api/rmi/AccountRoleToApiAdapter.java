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

import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.account.AccountRole;
import com.trilogy.app.crm.bean.account.SubscriptionClassRow;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.AccountRoleReference;

/**
 * Adapts AccountRole object to API objects.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class AccountRoleToApiAdapter implements Adapter
{
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptAccountRoleToReference(ctx, (AccountRole) obj);
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

    public static AccountRoleReference adaptAccountRoleToReference(final Context ctx, final AccountRole role)
    {
        final AccountRoleReference reference = new AccountRoleReference();
        
        reference.setIdentifier(role.getId());
        reference.setName(role.getName());
        
        List<SubscriptionClassRow> crmAllowedClasses = role.getAllowedSubscriptionClass();
        Long[] apiAllowedClasses = new Long[crmAllowedClasses.size()];
        for( int i=0; i<apiAllowedClasses.length; i++)
        {
            apiAllowedClasses[i] = crmAllowedClasses.get(i).getSubscriptionClass();
        }
        
        reference.setAllowedSubscriptionClasses(apiAllowedClasses);
        
        return reference;
    }
}
