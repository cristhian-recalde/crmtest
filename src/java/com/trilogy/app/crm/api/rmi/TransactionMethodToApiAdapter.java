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

import com.trilogy.app.crm.bean.TransactionMethod;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v3_0.types.transaction.TransactionMethodReference;


/**
 * Adapts TransactionMethod object to API objects.
 *
 * @author victor.stratan@redknee.com
 */
public class TransactionMethodToApiAdapter implements Adapter
{
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptTransactionMethodToReference((TransactionMethod) obj);
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

    public static TransactionMethodReference adaptTransactionMethodToReference(final TransactionMethod method)
    {
        final TransactionMethodReference reference = new TransactionMethodReference();
        reference.setIdentifier(method.getIdentifier());
        reference.setName(method.getName());
        reference.setDescription(method.getDescription());
        reference.setIdentifierUsed(method.getIdentifierUsed());
        reference.setDateUsed(method.getDateUsed());
        reference.setBankTransitUsed(method.getBankTransitUsed());
        reference.setBankAccountUsed(method.getBankAccountUsed());
        reference.setNameUsed(method.getNameUsed());
        reference.setCreditCardTypeUsed(method.getCardTypeUsed());
        reference.setBankProfileUsed(method.getBankCodeUsed()); 
        reference.setEnabled(method.getShownToAgent());

        return reference;
    }
}
