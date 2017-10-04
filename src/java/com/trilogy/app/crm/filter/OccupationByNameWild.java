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
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.filter;

import java.sql.SQLException;

import com.trilogy.app.crm.bean.Occupation;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;

/**
 * @author psperneac
 * @since May 1, 2005 11:06:49 PM
 */
public class OccupationByNameWild implements Predicate,XStatement
{
    protected String name;

    public OccupationByNameWild(String name)
    {
        setName(name);
    }

    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        return SafetyUtil.safeContains(((Occupation)obj).getName(),getName(),true);
    }


    public String createStatement(Context ctx)
    {
        return "name like %" + getName() + "%";
    }


    /**
     * Set a PreparedStatement with the supplied Object.
     */
    public void set(Context ctx, XPreparedStatement ps) throws SQLException
    {
    }


    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
