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
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.CUGStateEnum;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.util.crmapi.wsdl.v3_0.types.callinggroups.CUGStateTypeEnum;


/**
 * Adapts ClosedUserGroup object to API objects.
 * 
 * @author bhupendra.pandey@redknee.com
 */
public class ClosedUserGroupToApiAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptClosedUserGroupToApi(ctx, (ClosedUserGroup) obj);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroup adaptClosedUserGroupToApi(
            final Context ctx, final ClosedUserGroup crmCug) throws HomeException
    {        
        final com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroup apiCug = new com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroup();
        ClosedUserGroupToApiReferenceAdapter.adaptClosedUserGroupToReference(ctx, crmCug, apiCug);
        apiCug.setCugState(CUGStateTypeEnum.valueOf(crmCug.getCugState().getIndex()));      
        apiCug.setDeprecated(crmCug.getDeprecated());
        apiCug.setOwnerMSISDN(crmCug.getOwnerMSISDN());        
        apiCug.setSmsNotifyUser(crmCug.getSmsNotifyUser());        
        return apiCug;
    }
    

    public static ClosedUserGroup adaptApiToClosedUserGroup(final Context ctx,
            final com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroup apiCug) throws HomeException
    {
        ClosedUserGroup crmCug = null;
        try
        {
            crmCug = (ClosedUserGroup) XBeans.instantiate(ClosedUserGroup.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(ClosedUserGroupToApiAdapter.class,
                    "Error instantiating new ClosedUserGroup.  Using default constructor.", e).log(ctx);
            crmCug = new ClosedUserGroup();
        }
        adaptApiToClosedUserGroup(ctx, apiCug, crmCug);
        return crmCug;
    }
    
    public static ClosedUserGroup adaptApiToClosedUserGroup(final Context ctx,
            final com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroup apiCug, ClosedUserGroup crmCug) throws HomeException
    {        
        if (apiCug.getCugTemplateID() != null)
        {
            ClosedUserGroupTemplate cugTemplate = ClosedUserGroupSupport.getCugTemplate(ctx, apiCug.getCugTemplateID(), apiCug.getSpid());
            crmCug.setSpid(cugTemplate.getSpid());
          	crmCug.setName(apiCug.getName());
            crmCug.setDeprecated(cugTemplate.getDeprecated());
            crmCug.setCugTemplateID(apiCug.getCugTemplateID());
        }
        if (apiCug.getCugState() != null)
        {
            crmCug.setCugState(CUGStateEnum.get((short) apiCug.getCugState().getValue()));
        }
        if (apiCug.getOwnerMSISDN() != null)
        {
            crmCug.setOwnerMSISDN(apiCug.getOwnerMSISDN());
        }
        if (apiCug.getSmsNotifyUser() != null)
        {
            crmCug.setSmsNotifyUser(apiCug.getSmsNotifyUser());
        }
        return crmCug;
    }
}
