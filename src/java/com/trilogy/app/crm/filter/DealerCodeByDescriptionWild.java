/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.filter;

import java.sql.SQLException;

import com.trilogy.app.crm.bean.DealerCode;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;


/**
 * @author psperneac
 * @since May 2, 2005 12:29:11 AM
 */
public class DealerCodeByDescriptionWild implements Predicate, XStatement
{

    protected String description;


    public DealerCodeByDescriptionWild(String desc)
    {
        setDescription(desc);
    }


    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        return SafetyUtil.safeContains(((DealerCode) obj).getDesc(), getDescription(), true);
    }


    public String createStatement(Context ctx)

    {
        return "desc like %" + getDescription() + "%";
    }


    /**
     * Set a PreparedStatement with the supplied Object.
     */
    public void set(Context ctx, XPreparedStatement ps) throws SQLException
    {
    }


    public String getDescription()
    {
        return description;
    }


    public void setDescription(String description)
    {
        this.description = description;
    }
}
