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

import java.sql.*;

import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.report.ReportUtilities;
import com.trilogy.app.crm.support.MultiDbSupportHelper;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XResultSet;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.visitor.AbortVisitException;


/**
 * Provides a check in the remove() method that enforces the rule that in-use
 * FCTTs may not be deleted.  A FCTT is in-use if it is referrenced by either a
 * current or future price plan version.
 *
 * @author gary.anderson@redknee.com
 */
public
class FreeCallTimeInUseCheckingHome
    extends HomeProxy
{
    /**
     * Creates a new FreeCallTimeInUseCheckingHome.
     *
     * @param context The operating context.
     * @param delegate The Home to which we delegate.
     */
    public FreeCallTimeInUseCheckingHome(
        final Context context,
        final Home delegate)
    {
        super(context, delegate);
    }


    /**
     * {@inheritDoc}
     */
    public void remove(Context ctx, final Object obj)
        throws HomeException
    {
        final FreeCallTime template = (FreeCallTime)obj;

        validateUsage(ctx, template);

        super.remove(ctx,template);
    }


    /**
     * Validates that the template may be deleted (not in-use).
     *
     * @param template The FreeCallTimeTemplate to validate.
     *
     * @exception HomeException Thrown if the given template is in use.
     */
    private void validateUsage(Context ctx, final FreeCallTime template)
        throws HomeException
    {
        final String pricePlanVersionTableName = MultiDbSupportHelper.get(ctx).getTableName(ctx, PricePlanVersionHome.class, PricePlanVersionXInfo.DEFAULT_TABLE_NAME);
        final String pricePlanTableName = MultiDbSupportHelper.get(ctx).getTableName(ctx, PricePlanHome.class, PricePlanXInfo.DEFAULT_TABLE_NAME);

        XDB xdb=(XDB) ctx.get(XDB.class);

        XStatement sql=new XStatement(){
            public String createStatement(Context ctx)
            {
                return " select "
                    + pricePlanVersionTableName + "." + AbstractPricePlanVersion.ID_PROPERTY + ", "
                    + pricePlanVersionTableName + "." + AbstractPricePlanVersion.VERSION_PROPERTY
                    + " from "
                    + pricePlanVersionTableName + ", " + pricePlanTableName
                    + " where 1=1 "
                    // + pricePlanVersionTableName + "." + AbstractPricePlanVersion.FREECALLTIMETEMPLATE_PROPERTY + " = ?"
                    // TODO: REPLACE with BUNDLEMANAGER
                    + " and ( "
                    + pricePlanVersionTableName + "." + AbstractPricePlanVersion.ACTIVATION_PROPERTY + " = -1"
                    + " or ( "
                    + pricePlanTableName + "." + PricePlan.ID_PROPERTY + " = " + pricePlanVersionTableName + "." + AbstractPricePlanVersion.ID_PROPERTY
                    + " and "
                    + pricePlanTableName + "." + PricePlan.CURRENTVERSION_PROPERTY + " = " + pricePlanVersionTableName + "." + AbstractPricePlanVersion.VERSION_PROPERTY
                    + " ) ) group by "
                    + pricePlanVersionTableName + "." + AbstractPricePlanVersion.ID_PROPERTY + ", "
                    + pricePlanVersionTableName + "." + AbstractPricePlanVersion.VERSION_PROPERTY;
            }

            public void set(Context ctx, XPreparedStatement ps) throws SQLException
            {
                // TODO: REPLACE with BUNDLEMANAGER
                //ps.setLong(template.getIdentifier());
            }
        };

        final StringBuilder buffer = new StringBuilder();

        xdb.forEach(ctx,new Visitor(){
            public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
            {
                try
                {
                    final int plan = ((XResultSet)obj).getInt(1);
                    final int version = ((XResultSet)obj).getInt(2);

                    if (buffer.length() != 0)
                    {
                        buffer.append(", ");
                    }

                    buffer.append("Plan ").append(plan);
                    buffer.append(" Version ").append(version);
                }
                catch (SQLException e)
                {
                    throw new AgentException(e);
                }
            }
        },sql);

        if (buffer.length() != 0)
        {
            throw new HomeException(
                "Cannot delete free call time template " + template.getIdentifier()
                + " because it is in use: " + buffer.toString() + ".");
        }
    }

} // class
