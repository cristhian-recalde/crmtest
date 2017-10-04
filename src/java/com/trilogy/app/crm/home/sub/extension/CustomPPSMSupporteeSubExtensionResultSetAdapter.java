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
package com.trilogy.app.crm.home.sub.extension;

import java.sql.SQLException;

import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtension;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtensionResultSetAdapter;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.xdb.AbstractResultSetAdapter;
import com.trilogy.framework.xhome.xdb.ResultSetAdapter;
import com.trilogy.framework.xhome.xdb.XResultSet;

/**
 * A custom PPSM Supportee result set adapter which will retrieve the MSISDN from the table (even though
 * it's a transient field), for we use a view to retrieve the data.
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class CustomPPSMSupporteeSubExtensionResultSetAdapter extends AbstractResultSetAdapter
{
    private ResultSetAdapter delegate_;
    private final static ResultSetAdapter instance__ = new CustomPPSMSupporteeSubExtensionResultSetAdapter(PPSMSupporteeSubExtensionResultSetAdapter.instance());
    
    private CustomPPSMSupporteeSubExtensionResultSetAdapter(ResultSetAdapter delegate)
    {
        delegate_ = delegate;
    }
    
    public static ResultSetAdapter instance()
    {
       return instance__;
    }

    
    public Object f(Context ctx, XResultSet rs)
        throws SQLException
    {
       PPSMSupporteeSubExtension bean = new PPSMSupporteeSubExtension();
       bean = (PPSMSupporteeSubExtension) delegate_.f(ctx, rs);

       try { bean.setMSISDN(rs.getString("MSISDN")); } catch (Throwable ex) { logBeanSetException(ctx, ex); }

       return bean;
    }}
