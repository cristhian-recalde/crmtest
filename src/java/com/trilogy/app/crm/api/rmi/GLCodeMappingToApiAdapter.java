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

import com.trilogy.app.crm.bean.GLCodeMapping;
import com.trilogy.util.crmapi.wsdl.v2_1.types.transaction.GLCodeReference;
import com.trilogy.util.crmapi.wsdl.v2_1.types.transaction.GLCodeStateEnum;

/**
 * Adapts GLCodeMapping object to API objects.
 *
 * @author victor.stratan@redknee.com
 */
public class GLCodeMappingToApiAdapter implements Adapter
{
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptGLCodeToReference((GLCodeMapping) obj);
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

    public static GLCodeReference adaptGLCodeToReference(final GLCodeMapping glCode)
    {
        final GLCodeReference reference = new GLCodeReference();
        reference.setSpid(glCode.getSpid());
        reference.setGlCode(glCode.getGlCode());
        reference.setDebitGLCode(glCode.getDebitGlCode());
        reference.setCreditGLCode(glCode.getCreditGlCode());
        reference.setDescription(glCode.getDescription());
        reference.setGlAccountNumber(glCode.getGlAccountNumber());
        reference.setGlProductCode(glCode.getGlProductCode());
        reference.setState(GLCodeStateEnum.valueOf(glCode.getState()));

        return reference;
    }
}
