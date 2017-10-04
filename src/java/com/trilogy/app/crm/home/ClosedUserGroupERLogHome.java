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
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;

/**
 * @author ali
 *
 * ER logger for ClosedUserGroup create.
 */
public class ClosedUserGroupERLogHome extends HomeProxy {

	public ClosedUserGroupERLogHome(
    		Context ctx,
            final Home delegate)
            throws HomeException
    {
            super(delegate);
    }

	/* Failing CUG creation can happen in one of two places:  when creating the CUG 
	 * (ClosedUserGroupServiceHome) or while creating the Subscriber Auxiliary Service for it 
	 * (ClosedUserGroupSubscriberAuxiliaryServiceCreationHome).  Each of these failures has a 
	 * different error code for the ER.  Logging the failure ER is done in the 
	 * afore mentioned classes while catching exceptions.  This method only logs a 
	 * successful CUG creation (CUG+Subscriber Auxiliary Service creation success).
	 */
    public Object create(Context ctx, Object obj)
    throws HomeException
	{
        try
	    {
	        new OMLogMsg(Common.OM_MODULE, Common.OM_CUG_CREATION_ATTEMPT).log(ctx);
	    	
	    	final ClosedUserGroup createdCug = (ClosedUserGroup) super.create(ctx,obj);
			
	        new OMLogMsg(Common.OM_MODULE, Common.OM_CUG_CREATION_SUCCESS).log(ctx);
	
			CallingGroupERLogMsg.generateCUGCreationER(createdCug,
					CallingGroupERLogMsg.SUCCESS_RESULT_CODE, ctx);
					
			return createdCug;
        }
        catch(HomeException  he)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_CUG_CREATION_FAIL).log(ctx);
            throw he;

        }
        catch (Exception e)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_CUG_CREATION_FAIL).log(ctx);
            new MajorLogMsg(this, "cug create fail", e).log(ctx);
            throw new HomeException (e.getMessage(), e);
        }	
    }
    
    public Object store(Context ctx, Object obj)
    throws HomeException
    {
        try
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_CUG_MODIFICATION_ATTEMPT).log(ctx);

            ClosedUserGroup oldCug = (ClosedUserGroup) ctx.get(ClosedUserGroupServiceHome.OLD_CUG);
            
            final ClosedUserGroup returnedCug = (ClosedUserGroup) super.store(ctx,obj);
            
            new OMLogMsg(Common.OM_MODULE, Common.OM_CUG_MODIFICATION_SUCCESS).log(ctx);
            
            
            CallingGroupERLogMsg.generateCUGModificationER(oldCug, returnedCug,
                    CallingGroupERLogMsg.SUCCESS_RESULT_CODE, ctx);
            
            return returnedCug;
        }
        catch(HomeException  he)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_CUG_MODIFICATION_FAIL).log(ctx);
            throw he;

        }
        catch (Exception e)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_CUG_MODIFICATION_FAIL).log(ctx);
            new MajorLogMsg(this, "cug store fail", e).log(ctx);
            throw new HomeException (e.getMessage(), e);
        }
    }
    
    
    public void remove(Context ctx, Object obj)
    throws HomeException
    {
        try
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_CUG_DELETION_ATTEMPT).log(ctx);

            super.remove(ctx,obj);
            
            new OMLogMsg(Common.OM_MODULE, Common.OM_CUG_DELETION_SUCCESS).log(ctx);
            
            CallingGroupERLogMsg.generateCUGDeletionER((ClosedUserGroup)obj,
                    CallingGroupERLogMsg.SUCCESS_RESULT_CODE, ctx);
            
        }
        catch(HomeException  he)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_CUG_DELETION_FAIL).log(ctx);
            throw he;

        }
        catch (Exception e)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_CUG_DELETION_FAIL).log(ctx);
            new MajorLogMsg(this, "cug delete fail", e).log(ctx);
            throw new HomeException (e.getMessage(), e);
        }
    }    
	
}
