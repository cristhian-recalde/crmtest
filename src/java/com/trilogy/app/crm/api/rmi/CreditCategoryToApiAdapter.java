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

import com.trilogy.app.crm.bean.core.CreditCategory;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.CreditCategoryReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.DunningConfigurationTypeEnum;

/**
 * Adapts CreditCategory object to API objects.
 *
 * @author victor.stratan@redknee.com
 */
public class CreditCategoryToApiAdapter implements Adapter
{
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptCreditCategoryToReference((CreditCategory) obj);
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

    public static com.redknee.util.crmapi.wsdl.v2_2.types.generalprovisioning.CreditCategory adaptCreditCategoryToApi(
            final CreditCategory creditCategory)
    {
        final com.redknee.util.crmapi.wsdl.v2_2.types.generalprovisioning.CreditCategory category;
        category = new com.redknee.util.crmapi.wsdl.v2_2.types.generalprovisioning.CreditCategory();
        adaptCreditCategoryToReference(creditCategory, category);
        category.setDunningConfiguration(DunningConfigurationTypeEnum.valueOf(creditCategory.getDunningConfiguration().getIndex()));

        return category;
    }

    public static CreditCategoryReference adaptCreditCategoryToReference(final CreditCategory creditCategory)
    {
        final CreditCategoryReference reference = new CreditCategoryReference();
        adaptCreditCategoryToReference(creditCategory, reference);

        return reference;
    }

    public static CreditCategoryReference adaptCreditCategoryToReference(final CreditCategory creditCategory,
            final CreditCategoryReference reference)
    {
        reference.setIdentifier(Long.valueOf(creditCategory.getCode()));
        reference.setSpid(creditCategory.getSpid());
        reference.setDescription(creditCategory.getDesc());
        reference.setFactor(creditCategory.getFactor());
        reference.setDunningExempt(creditCategory.getDunningExempt());

        return reference;
    }
}
