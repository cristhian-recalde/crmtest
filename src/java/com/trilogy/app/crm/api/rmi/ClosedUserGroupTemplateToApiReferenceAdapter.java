/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.api.rmi;

import com.trilogy.app.crm.bean.ClosedUserGroupTemplate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroupTemplateReference;


/**
 * Adapts ClosedUserGroupTemplate object to API objects.
 * 
 * @author bhupendra.pandey@redknee.com
 */
public class ClosedUserGroupTemplateToApiReferenceAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptClosedUserGroupTemplateToReference(ctx, (ClosedUserGroupTemplate) obj);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static ClosedUserGroupTemplateReference adaptClosedUserGroupTemplateToReference(final Context ctx,
            final ClosedUserGroupTemplate crmCugTemplate) throws HomeException
    {
        final ClosedUserGroupTemplateReference apiCugTemplateRef = new ClosedUserGroupTemplateReference();
        adaptClosedUserGroupTemplateToReference(ctx, crmCugTemplate, apiCugTemplateRef);
        return apiCugTemplateRef;
    }


    public static ClosedUserGroupTemplateReference adaptClosedUserGroupTemplateToReference(final Context ctx,
            final ClosedUserGroupTemplate crmCugTemplate, ClosedUserGroupTemplateReference apiCugTemplateRef)
            throws HomeException
    {
        apiCugTemplateRef.setIdentifier(crmCugTemplate.getID());
        apiCugTemplateRef.setSpid(crmCugTemplate.getSpid());
        apiCugTemplateRef.setName(crmCugTemplate.getName());
        apiCugTemplateRef.setAuxiliaryServiceID(crmCugTemplate.getAuxiliaryService(ctx));
        return apiCugTemplateRef;
    }
}
