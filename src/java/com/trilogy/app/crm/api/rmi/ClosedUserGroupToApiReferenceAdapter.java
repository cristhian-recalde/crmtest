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

import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplate;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
// import com.trilogy.util.crmapi.wsdl.v2_1.types.callinggroup.ClosedUserGroupReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroupReference;

/**
 * Adapts ClosedUserGroup object to API objects.
 *
 * @author victor.stratan@redknee.com
 */
public class ClosedUserGroupToApiReferenceAdapter implements Adapter
{
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptClosedUserGroupToReference(ctx, (ClosedUserGroup) obj);
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static ClosedUserGroupReference adaptClosedUserGroupToReference(final Context ctx,
            final ClosedUserGroup crmCug) throws HomeException
    {
        final ClosedUserGroupReference apiCugRef = new ClosedUserGroupReference();
        adaptClosedUserGroupToReference(ctx, crmCug, apiCugRef);
        return apiCugRef;
    }
    

    public static ClosedUserGroupReference adaptClosedUserGroupToReference(final Context ctx,
            final ClosedUserGroup crmCug, ClosedUserGroupReference apiCugRef) throws HomeException
    {
        apiCugRef.setIdentifier(crmCug.getID());
        apiCugRef.setSpid(crmCug.getSpid());
        apiCugRef.setName(crmCug.getName());
        ClosedUserGroupTemplate cugTemplate = ClosedUserGroupSupport.getCugTemplate(ctx, crmCug.getCugTemplateID(), crmCug.getSpid());        
        apiCugRef.setAuxiliaryServiceID(cugTemplate.getAuxiliaryService(ctx));
        apiCugRef.setCugTemplateID(crmCug.getCugTemplateID());
        return apiCugRef;
    }
}
