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

import com.trilogy.app.crm.bean.CallType;
import com.trilogy.util.crmapi.wsdl.v2_1.types.calldetail.CallTypeReference;

/**
 * Adapts CallType object to API objects.
 *
 * @author victor.stratan@redknee.com
 */
public class CallTypeToApiAdapter implements Adapter
{
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptCallTypeToReference((CallType) obj);
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

    public static com.redknee.util.crmapi.wsdl.v2_1.types.calldetail.CallType adaptCallTypeToApi(
            final CallType callType)
    {
        final com.redknee.util.crmapi.wsdl.v2_1.types.calldetail.CallType type;
        type = new com.redknee.util.crmapi.wsdl.v2_1.types.calldetail.CallType();
        adaptCallTypeToReference(callType, type);
        type.setInvoiceDescription(callType.getInvoiceDesc());

        return type;
    }

    public static CallTypeReference adaptCallTypeToReference(final CallType callType)
    {
        final CallTypeReference reference = new CallTypeReference();
        adaptCallTypeToReference(callType, reference);

        return reference;
    }

    public static CallTypeReference adaptCallTypeToReference(final CallType callType,
            final CallTypeReference reference)
    {
        reference.setIdentifier(Long.valueOf(callType.getId()));
        reference.setSpid(callType.getSpid());
        reference.setGlCode(callType.getGLCode());

        return reference;
    }
}
