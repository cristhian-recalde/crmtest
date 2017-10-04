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

import com.trilogy.app.crm.bean.PersonalListPlan;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;


/**
 * Adapts PersonalListPlanReference object to API objects.
 * 
 * @author bhupendra.pandey@redknee.com
 */
public class PersonalListPlanToApiReferenceAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptPersonalListPlanToApiReference(ctx, (PersonalListPlan) obj);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.PersonalListPlanReference adaptPersonalListPlanToApiReference(
            final Context ctx, final PersonalListPlan crmPlp) throws HomeException
    {
        final com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.PersonalListPlanReference apiPlpRef = new com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.PersonalListPlanReference();
        adaptPersonalListPlanToApiReference(ctx, crmPlp, apiPlpRef);
        return apiPlpRef;
    }


    public static com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.PersonalListPlanReference adaptPersonalListPlanToApiReference(
            final Context ctx, final PersonalListPlan crmPlp,
            com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.PersonalListPlanReference apiPlpRef)
            throws HomeException
    {
        apiPlpRef.setAuxiliaryServiceID(crmPlp.getAuxiliaryService(ctx));
        apiPlpRef.setIdentifier(crmPlp.getID());
        apiPlpRef.setName(crmPlp.getName());
        apiPlpRef.setSpid(crmPlp.getSpid());
        return apiPlpRef;
    }
}
