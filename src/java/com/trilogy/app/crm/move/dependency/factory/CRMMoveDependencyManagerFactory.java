/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.move.dependency.factory;

import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.move.MoveDependencyManager;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.dependency.AccountExtensionMoveDependencyManager;
import com.trilogy.app.crm.move.dependency.AccountMoveDependencyManager;
import com.trilogy.app.crm.move.dependency.ConvertAccountBillingTypeDependencyManager;
import com.trilogy.app.crm.move.dependency.ConvertAccountGroupTypeDependancyManager;
import com.trilogy.app.crm.move.dependency.ConvertSubscriptionBillingTypeDependencyManager;
import com.trilogy.app.crm.move.dependency.EmptyMoveDependencyManager;
import com.trilogy.app.crm.move.dependency.SubscriptionExtensionMoveDependencyManager;
import com.trilogy.app.crm.move.dependency.SubscriptionMoveDependencyManager;
import com.trilogy.app.crm.move.request.AccountExtensionMoveRequest;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequest;
import com.trilogy.app.crm.move.request.ConvertAccountGroupTypeRequest;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequest;
import com.trilogy.app.crm.move.request.SubscriptionExtensionMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.PMLogMsg;


/**
 * This is the main version of the dependency manager factory for CRM.
 * 
 * It uses thread-level caching that automatically cleans up after itself
 * when nobody is holding a reference to the dependency manager instance.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class CRMMoveDependencyManagerFactory extends DefaultMoveDependencyManagerFactory
{    
    private static MoveDependencyManagerFactory CRMInstance_ = null;
    public static MoveDependencyManagerFactory instance()
    {
        if (CRMInstance_ == null)
        {
            CRMInstance_ = new CRMMoveDependencyManagerFactory();
        }
        return CRMInstance_;
    }
    
    protected CRMMoveDependencyManagerFactory()
    {
    }

    @Override
    public MoveDependencyManager getRequestSpecificInstance(Context ctx, MoveRequest request)
    {
        MoveDependencyManager dependencyManager = null;

        if (request instanceof ConvertAccountBillingTypeRequest)
        {
            PMLogMsg pm = new PMLogMsg(CRMMoveDependencyManagerFactory.class.getName(), "ConvertAccountBillingTypeDependencyManagerCreation");
            dependencyManager = new ConvertAccountBillingTypeDependencyManager(ctx, (ConvertAccountBillingTypeRequest)request);
            pm.log(ctx);
        }
        else if (request instanceof ConvertAccountGroupTypeRequest)
        {
        	ConvertAccountGroupTypeRequest convRequest = (ConvertAccountGroupTypeRequest) request;
        	PMLogMsg pm = new PMLogMsg(CRMMoveDependencyManagerFactory.class.getName(), "ConvertAccountGroupTypeDependencyManagerCreation");
        	dependencyManager = new ConvertAccountGroupTypeDependancyManager<ConvertAccountGroupTypeRequest>(ctx, (ConvertAccountGroupTypeRequest)request);	
        	pm.log(ctx);
        }
        else if (request instanceof AccountMoveRequest)
        {
            PMLogMsg pm = new PMLogMsg(CRMMoveDependencyManagerFactory.class.getName(), "AccountMoveDependencyManagerCreation");
            dependencyManager = new AccountMoveDependencyManager(ctx, (AccountMoveRequest)request);
            pm.log(ctx);
        }
        else if (request instanceof AccountExtensionMoveRequest)
        {
            PMLogMsg pm = new PMLogMsg(CRMMoveDependencyManagerFactory.class.getName(), "AccountExtensionMoveDependencyManagerCreation");
            dependencyManager = new AccountExtensionMoveDependencyManager(ctx, (AccountExtensionMoveRequest)request);
            pm.log(ctx);
        }
        else if (request instanceof ConvertSubscriptionBillingTypeRequest)
        {
            PMLogMsg pm = new PMLogMsg(CRMMoveDependencyManagerFactory.class.getName(), "ConvertSubscriptionBillingTypeDependencyManagerCreation");
            dependencyManager = new ConvertSubscriptionBillingTypeDependencyManager(ctx, (ConvertSubscriptionBillingTypeRequest)request);
            pm.log(ctx);
        }
        else if (request instanceof SubscriptionMoveRequest)
        {
            PMLogMsg pm = new PMLogMsg(CRMMoveDependencyManagerFactory.class.getName(), "SubscriptionMoveDependencyManagerCreation");
            dependencyManager = new SubscriptionMoveDependencyManager(ctx, (SubscriptionMoveRequest)request);
            pm.log(ctx);
        }
        else if (request instanceof SubscriptionExtensionMoveRequest)
        {
            PMLogMsg pm = new PMLogMsg(CRMMoveDependencyManagerFactory.class.getName(), "SubscriptionExtensionMoveDependencyManagerCreation");
            dependencyManager = new SubscriptionExtensionMoveDependencyManager(ctx, (SubscriptionExtensionMoveRequest)request);
            pm.log(ctx);
        }
        else
        {
            PMLogMsg pm = new PMLogMsg(CRMMoveDependencyManagerFactory.class.getName(), "DefaultMoveDependencyManagerCreation");
            dependencyManager = super.getRequestSpecificInstance(ctx, request);
            pm.log(ctx);
        }
        
        return dependencyManager;
    }
}
