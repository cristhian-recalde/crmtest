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
package com.trilogy.app.crm.move.dependency;

import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.request.ConvertAccountGroupTypeRequest;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;

/**
 * When an individual account is being converted to group and we want to retain the original account
 * as child of the newly created Account, this class should be used otherwise the behaviour should be same 
 * as it's parent's behaviour.
 * 
 * The data migration process is taken separately, as it needs to be handled in asynchronous manner.
 * A different threadpool will take care of this part. 
 *
 * @author suyash.gaidhani@redknee.com
 * @since 9.5.1
 */
public class ConvertAccountGroupTypeDependancyManager<CAGTR extends ConvertAccountGroupTypeRequest> extends AccountMoveDependencyManager<CAGTR>
{	
    public ConvertAccountGroupTypeDependancyManager(Context ctx, CAGTR srcRequest)
    {
        super(ctx, srcRequest);
    }
    
    @Override
    protected Collection<? extends MoveRequest> getDependencyRequests(Context ctx, CAGTR request) throws MoveException
    {
        Collection<MoveRequest> dependencies = new ArrayList<MoveRequest>();
        
        if(request.getGroupType().getIndex() == GroupTypeEnum.GROUP_INDEX && request.getRetainOriginalAccount() == true)
        {
        	if(request.getMigrateOnly() == false)
        	{
        		CAGTR clonedRequest = null;
                try
                {
                    new DebugLogMsg(this, "Cloning ConvertAccountGroupTypeRequest in order to initiate offline data migration process...", null).log(ctx);
                    clonedRequest = (CAGTR) request.deepClone();
                    clonedRequest.setMigrateOnly(true);
                    dependencies.add(clonedRequest);
                    
                }
                catch (final CloneNotSupportedException exception)
                {
                    throw new MoveException(request, "Unable to clone " + request.getClass() + ". ", exception);
                }
        	}
        	else
        	{
        		//No need to add further dependancies.
        	}
        }
        else
        {
        	dependencies.addAll(super.getDependencyRequests(ctx, request));
        }
        
        
        return dependencies;
    }
}
