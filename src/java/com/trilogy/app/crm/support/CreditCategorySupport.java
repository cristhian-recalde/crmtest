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
package com.trilogy.app.crm.support;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XResultSet;
import com.trilogy.framework.xhome.xdb.XStatement;

import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXDBHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.CreditCategoryHome;
import com.trilogy.app.crm.bean.core.CreditCategory;


/**
 * @author jchen
 */
public class CreditCategorySupport
{

    public static CreditCategory findCreditCategory(Context ctx, int id) throws HomeException
    {
        CreditCategory creditCategory = null;
        Home home = (Home) ctx.get(CreditCategoryHome.class);
        creditCategory = (CreditCategory) home.find(ctx, Integer.valueOf(id));
        return creditCategory;
    }


    public static Collection getAccountIdentifiers(final Context context, final int creditcategory, final int currentNumPTPTransactions)
            throws HomeException
    {
        final Collection bans = new ArrayList();
        XDB xdb = (XDB) context.get(XDB.class);
        final String tableName = MultiDbSupportHelper.get(context).getTableName(context, AccountHome.class, AccountXInfo.DEFAULT_TABLE_NAME);
        XStatement sql = new XStatement()
        {

            public String createStatement(Context ctx)
            {
                return " select BAN from " + tableName + " where CREDITCATEGORY = ? AND CURRENTNUMPTPTRANSITIONS > ?"; // AND STATE <> " + AccountStateEnum.PROMISE_TO_PAY_INDEX;
            }


            public void set(Context ctx, XPreparedStatement ps) throws SQLException
            {
                ps.setInt(creditcategory);
                ps.setInt(currentNumPTPTransactions);
                
            }
        };
        xdb.forEach(context, new Visitor()
        {

            public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
            {
                try
                {
                    bans.add(((XResultSet) obj).getString(1));
                }
                catch (SQLException e)
                {
                    throw new AgentException(e);
                }
            }
        }, sql);
        return bans;
    }
}