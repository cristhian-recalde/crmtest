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
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.ClosedUserGroupTemplate;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;


/**
 * @author ltse
 * 
 * ER logger for ClosedUserGroupTemplate
 * 
 * Failing CUG creation can happen in one of two places: when update/store/delete the CUG template
 * (ClosedUserGroupTemplateServiceHome) or while create/remove the Auxiliary Service for it
 * (ClosedUserGroupTemplateAuxiliaryServiceHome). Each of these failures has a
 * different error code for the ER. Logging the failure ER is done in the afore
 * mentioned classes while catching exceptions. This method only logs a successful CUG
 * creation (CUG+Auxiliary Service creation success).
 */
public class ClosedUserGroupTemplateERLogHome extends HomeProxy
{

    public ClosedUserGroupTemplateERLogHome(Context ctx, final Home delegate) throws HomeException
    {
        super(delegate);
    }


    public Object create(Context ctx, Object obj) throws HomeException
    {
        final ClosedUserGroupTemplate createdCug = (ClosedUserGroupTemplate) super.create(ctx, obj);
        CallingGroupERLogMsg.generateCUGTemplateCreationER(
                createdCug,                
                CallingGroupERLogMsg.SUCCESS_RESULT_CODE, 
                ctx);
        return createdCug;
    }
    
    
   
    public void remove(Context ctx, Object obj) throws HomeException
    {
        super.remove(ctx, obj);
     
        CallingGroupERLogMsg.generateCUGTemplateDeletionER((ClosedUserGroupTemplate) obj, 
                CallingGroupERLogMsg.SUCCESS_RESULT_CODE, ctx);
    }
    
    
   
    public Object store(Context ctx, Object obj) throws HomeException
    {
        ClosedUserGroupTemplate oldCugTemplate = null;
        final ClosedUserGroupTemplate cugTemplate = (ClosedUserGroupTemplate) obj;
        
        try
        {
            oldCugTemplate = ClosedUserGroupSupport.getCugTemplate(ctx, cugTemplate.getID(), cugTemplate.getSpid());
        }
        catch (Exception e)
        {
            LogSupport.minor(ctx, this, "Exception while looking up for CUG template : " + cugTemplate.getID(), e);
        }
        obj = super.store(ctx, obj);

        CallingGroupERLogMsg.generateCUGTemplateModificationER(oldCugTemplate, cugTemplate,
                CallingGroupERLogMsg.SUCCESS_RESULT_CODE, ctx);

        return obj;
    }
}
