package com.trilogy.app.crm.filter;

import java.sql.SQLException;

import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.app.crm.bean.DealerCodeAware;

/**
 * @author psperneac
 * @since Apr 29, 2005 12:05:04 PM
 */
public class ByDealerCode implements Predicate,XStatement
{
    protected String fieldName="dealerCode";
    protected String dealerCode;

    public ByDealerCode(String code)
    {
        setDealerCode(code);
    }

    public ByDealerCode(String fieldName,String code)
    {
        setFieldName(fieldName);
        setDealerCode(code);
    }

    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        return obj!=null && ((DealerCodeAware)obj).getDealerCode().equals(getDealerCode());
    }


    /**
     * Set a PreparedStatement with the supplied Object.
     */
    public void set(Context ctx, XPreparedStatement ps) throws SQLException
    {
    }


    public String createStatement(Context ctx)

    {
        return fieldName + " = '" + getDealerCode() + "'";
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getDealerCode()
    {
        return dealerCode;
    }

    public void setDealerCode(String dealerCode)
    {
        this.dealerCode = dealerCode;
    }
}
