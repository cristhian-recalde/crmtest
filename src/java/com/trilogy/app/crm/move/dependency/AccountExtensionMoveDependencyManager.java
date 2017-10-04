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
package com.trilogy.app.crm.move.dependency;

import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.request.AccountExtensionMoveRequest;


/**
 * Given an account extension move request, this class calculates its dependencies.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class AccountExtensionMoveDependencyManager extends AbstractMoveDependencyManager<AccountExtensionMoveRequest>
{
    public AccountExtensionMoveDependencyManager(Context ctx, AccountExtensionMoveRequest srcRequest)
    {
        super(ctx, srcRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<? extends MoveRequest> getDependencyRequests(Context ctx, AccountExtensionMoveRequest request)
            throws MoveException
    {
        // TODO: Implement this if any extensions ever require that other entities are moved
        return NullMoveDependencyManager.instance().getDependencyRequests();
    }
}
