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

import com.trilogy.app.crm.bean.AccountCreationTemplate;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SystemTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountCreationTemplateReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.GroupHierarchyTypeEnum;

/**
 * Adapts AccountCreationTemplate object to API objects.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class AccountCreationTemplateToApiAdapter implements Adapter
{
    @Override
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptAccountCreationTemplateToReference(ctx, (AccountCreationTemplate) obj);
    }

    @Override
    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

    public static AccountCreationTemplateReference adaptAccountCreationTemplateToReference(final Context ctx, final AccountCreationTemplate act)
    {
        final AccountCreationTemplateReference reference = new AccountCreationTemplateReference();
        reference.setIdentifier(act.getIdentifier());
        reference.setName(act.getName());
        reference.setSpid(act.getSpid());
        reference.setAccountType(act.getType());
        reference.setSystemType(SystemTypeEnum.valueOf(act.getSystemType().getIndex()));
		reference.setGroupType(GroupHierarchyTypeEnum.valueOf(act
		    .getGroupType().getIndex()));
        
        return reference;
    }
}
