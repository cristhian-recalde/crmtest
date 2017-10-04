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

/*
 * @author jchen
 * Created on Dec 14, 2005
 */
package com.trilogy.app.crm.support;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.util.StringUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xhome.xenum.AbstractEnum;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * @author jchen A util class allow to work with xdb in tranditional jdbc
 *         preparedStatement style,  not use eLang
 *  
 */
public class SimplePreparedXStatement implements XStatement {

    public static final String SQL_PREPARESTATMENT_PLACEHOLDER = "?";

    public SimplePreparedXStatement(String sqlClause) {
        this(sqlClause, (List) null);
    }

    public SimplePreparedXStatement(String sqlClause, Object[] sqlParameters) {
        sqlClause_ = sqlClause;
        sqlParameters_ = sqlParameters;
    }

    public SimplePreparedXStatement(String sqlClause, List sqlParameters) {
        sqlClause_ = sqlClause;
        if (sqlParameters != null)
            sqlParameters_ = sqlParameters.toArray();
    }

    protected int getDelimiterCount() {
        return StringUtil.getMatchCount(sqlClause_, SQL_PREPARESTATMENT_PLACEHOLDER);
    }
    
    protected int getParameterCount() {
        return getParameterCount(sqlParameters_);
    }

    public static int getParameterCount(Object[] sqlParameters) {
        int count = 0;
        if (sqlParameters != null) {
            count = sqlParameters.length;
        }
        return count;
    }

    public static int getParameterCount(List sqlParameters) {
        int count = 0;
        if (sqlParameters != null) {
            count = getParameterCount(sqlParameters.toArray());
        }
        return count;
    }
    //    public SimplePreparedXStatement add(String sqlClause, List sqlParameters)
    //    {
    //        Object[] newParams = null;
    //        if (sqlParameters != null)
    //            newParams = sqlParameters.toArray();
    //        return add(sqlClause, newParams);
    //    }
    
    //    public SimplePreparedXStatement add(String sqlClause, Object[]
    // sqlParameters)
    //    {
    //        String newSql = sqlClause_;
    //        if (sqlClause != null)
    //            newSql += sqlClause;
    //        
    //        ArrayList newList = new ArrayList();
    //        addArrayToList(newList, sqlParameters_);
    //        addArrayToList(newList, sqlParameters);
    //        
    //        sqlClause_ = newSql;
    //        sqlParameters_ = newList.toArray();
    //        return this;
    //    }

//    private void addArrayToList(List list, Object[] array) {
//        if (array != null) {
//            for (int i = 0; i < array.length; i++) {
//                list.add(array[i]);
//            }
//        }
//    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SimplePreparedXStatement[sql, place holder:");
        sb.append(getDelimiterCount()).append("<");
        sb.append(sqlClause_);
        sb.append(">");
        if (sqlParameters_ != null) {
            sb.append("params size=").append(sqlParameters_.length).append(",array<");
            for (int i = 0; i < sqlParameters_.length; i++) {
                sb.append(sqlParameters_[i]);
                sb.append(",");
            }
            sb.append(">");
        }
        sb.append("]");
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.framework.xhome.xdb.XStatement#createStatement(com.redknee.framework.xhome.context.Context)
     */
    public String createStatement(Context ctx) {
        return sqlClause_;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.framework.xhome.xdb.XStatement#set(com.redknee.framework.xhome.context.Context,
     *      com.redknee.framework.xhome.xdb.XPreparedStatement)
     */
    public void set(Context ctx, XPreparedStatement ps) throws SQLException {
        if (sqlParameters_ == null || sqlParameters_.length == 0)
            return;

        //validating prepared sql clause syntax
        if (getDelimiterCount() != getParameterCount()) {
            SQLException sqlExp = new SQLException(
                    "parameter count mismatch, smst=" + this);
            //sqlExp.printStackTrace();
            throw sqlExp;
        }
        for (int i = 0; i < sqlParameters_.length; i++) {
            Object obj = sqlParameters_[i];
            if (obj == null) {
                String msg = "Null object, don't know how to handle it, sql="
                        + getSqlClause() + "index i=" + i;
                //new MinorLogMsg(this, msg, new Exception(msg)).log(ctx);
                throw new SQLException(msg);
            } else if (obj instanceof Long) {
                ps.setLong(((Long) obj).longValue());
            } else if (obj instanceof Integer) {
                ps.setInt(((Integer) obj).intValue());
            }

            else if (obj instanceof Short) {
                ps.setShort(((Short) obj).shortValue());
            }

            else if (obj instanceof Byte) {
                ps.setByte(((Byte) obj).byteValue());
            } else if (obj instanceof Float) {
                ps.setFloat(((Float) obj).floatValue());
            } else if (obj instanceof Double) {
                ps.setDouble(((Double) obj).doubleValue());
            } else if (obj instanceof String) {
                ps.setString(((String) obj));
            } else if (obj instanceof Date) {
                ps.setDate(((Date) obj));
            } else if (obj instanceof Boolean) {
                ps.setBoolean(((Boolean) obj).booleanValue());
            } else if (obj instanceof AbstractEnum) {
                ps.setInt(((AbstractEnum) obj).getIndex());
            } else //unknow obj type
            {
                //ps.setString(obj.toString());
                String msg = "Unknow type of object, don't know how to handle it, sql="
                        + getSqlClause() + "index i=" + i + ",obj=" + obj;
                throw new SQLException(msg);
            }
        }
    }

    /**
     * @return Returns the sqlClause_.
     */
    public String getSqlClause() {
        return sqlClause_;
    }

    /**
     * @return Returns the sqlParameters_.
     */
    public Object[] getSqlParameters() {
        return sqlParameters_;
    }
    
    protected String sqlClause_;
    protected Object[] sqlParameters_;
}