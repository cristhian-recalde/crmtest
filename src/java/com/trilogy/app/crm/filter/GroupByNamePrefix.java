package com.trilogy.app.crm.filter;

import java.sql.SQLException;

import com.trilogy.framework.xhome.auth.bean.Group;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;

/**
 * @author psperneac
 * @since May 4, 2005 12:43:04 AM
 */
public class GroupByNamePrefix implements Predicate,XStatement
{
    protected String name;

    public GroupByNamePrefix(String name)
    {
        setName(name);
    }

    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        return ((Group)obj).getName().toUpperCase().startsWith(getName().toUpperCase());
    }
 

    /**
     * Set a PreparedStatement with the supplied Object.
     */
    public void set(Context ctx, XPreparedStatement ps) throws SQLException
    {
    }


    public String createStatement(Context ctx)
    {
        return "UPPER( SUBSTR ( name , 0, " + getName().length() + " )) = '" + getName().toUpperCase() + "'"; // To
                                                                                                              // change
                                                                                                              // body
                                                                                                              // of
                                                                                                              // implemented
                                                                                                              // methods
                                                                                                              // use
                                                                                                              // File
                                                                                                              // |
                                                                                                              // Settings
                                                                                                              // |
                                                                                                              // File
                                                                                                              // Templates.
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
