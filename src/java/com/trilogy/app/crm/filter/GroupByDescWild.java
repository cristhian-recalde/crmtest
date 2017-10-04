package com.trilogy.app.crm.filter;

import java.sql.SQLException;

import com.trilogy.framework.xhome.auth.bean.Group;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;


/**
 * @author psperneac
 * @since May 4, 2005 12:53:24 AM
 */
public class GroupByDescWild implements Predicate, XStatement
{

    protected String description;


    public GroupByDescWild(String desc)
    {
        setDescription(desc);
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
        return SafetyUtil.safeContains(((Group) obj).getDesc(), getDescription(), true);
    }


    public String createStatement(Context ctx)
    {
        return "desc like %" + getDescription() + "%"; // To change body of implemented
                                                       // methods use File | Settings |
                                                       // File Templates.
    }


    /**
     * Set a PreparedStatement with the supplied Object.
     */
    public void set(Context ctx, XPreparedStatement ps) throws SQLException
    {
    }
}
