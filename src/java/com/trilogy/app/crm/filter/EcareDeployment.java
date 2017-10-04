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
package com.trilogy.app.crm.filter;

import java.sql.SQLException;

import com.trilogy.app.crm.support.DeploymentTypeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.False;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;


/**
 * Provides a predicate for determining the deployment type of the application.
 * This is useful, for example, in configuring menus visibility based on
 * deployment type.
 *
 * @author gary.anderson@redknee.com
 */
public final
class EcareDeployment
extends SimpleDeepClone implements Predicate, XStatement
{
    /**
     * {@inheritDoc}
     */
    public boolean f(final Context context, final Object obj)
    {
        return DeploymentTypeSupportHelper.get(context).isEcare(context);
    }

    @Override
    public String createStatement(Context ctx)
    {
        return ((False)False.instance()).createStatement(ctx);
    }

    @Override
    public void set(Context ctx, XPreparedStatement ps) throws SQLException
    {
        ((False)False.instance()).set(ctx, ps);
    }

} // class
