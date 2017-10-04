package com.trilogy.app.crm.filter;

import java.sql.SQLException;

import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;


/**
 * @author psperneac
 * @since May 1, 2005 10:43:15 PM
 */
public class BillCycleByDescriptionWild implements Predicate, XStatement
{

    protected String description;


    public BillCycleByDescriptionWild(String description)
    {
        setDescription(description);
    }


    public String getDescription()
    {
        return description;
    }


    public void setDescription(String description)
    {
        this.description = description;
    }


    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        return SafetyUtil.safeContains(((BillCycle) obj).getDescription(), getDescription(), true);
    }


    public String createStatement(Context ctx)
    {
        return "description like %" + getDescription() + "%";
    }


    /**
     * Set a PreparedStatement with the supplied Object.
     */
    public void set(Context ctx, XPreparedStatement ps) throws SQLException
    {
    }
}
