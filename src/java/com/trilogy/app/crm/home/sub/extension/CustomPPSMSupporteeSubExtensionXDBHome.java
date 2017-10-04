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

import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtension;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtensionXDBHome;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtensionXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xdb.ColumnInfo;
import com.trilogy.framework.xhome.xdb.TableInfo;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * A custom PPSM Supportee XDB home which will use the CustomPPSMSupporteeSubExtensionResultSetAdapter
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class CustomPPSMSupporteeSubExtensionXDBHome extends PPSMSupporteeSubExtensionXDBHome
{
    public CustomPPSMSupporteeSubExtensionXDBHome(Context ctx)
    {
     this(ctx, PPSMSupporteeSubExtension.class, PPSMSupporteeSubExtensionXInfo.DEFAULT_TABLE_NAME);
    }
    
    
    public CustomPPSMSupporteeSubExtensionXDBHome(Context ctx, String tableName)
    {
        this(ctx, PPSMSupporteeSubExtension.class, tableName);
    }
    
    
    public CustomPPSMSupporteeSubExtensionXDBHome(Context ctx, Class cls, String tableName)
    {
       super(ctx, cls, tableName);
       setResultSetAdapter(CustomPPSMSupporteeSubExtensionResultSetAdapter.instance());
    }
    
    @Override
    public boolean createTable() throws HomeException
    {
        TableInfo info = getTableInfo();
        try
        {
            Home home = tableInfoHome(getContext());
            TableInfo existing = (TableInfo) home.find(info.getName());
            if (existing == null)
            {
                home.create(info);
                XDB xdb = (XDB) getContext().get(XDB.class);

                xdb.execute(getContext(), "DROP TABLE " + info.getName());
                
                StringBuffer buf = new StringBuffer();
                
                buf.append("CREATE VIEW " + info.getName() + " AS SELECT ");
                
                TableInfo mainTableInfo = (TableInfo) home.find("SUBEXTPPSMSUPPORTEE");

                for ( Iterator i = mainTableInfo.getColumns().iterator() ; i.hasNext() ; )
                {
                   ColumnInfo col = (ColumnInfo) i.next();
                   
                   buf.append("T1." + col.getName());
                   
                   if ( i.hasNext() ) buf.append(", ");
                }
                buf.append(", T2.MSISDN");
                
                buf.append(" FROM SUBEXTPPSMSUPPORTEE T1");
                buf.append(" INNER JOIN SUBSCRIBER T2");
                buf.append(" ON T1.SUBID = T2.ID");
                        
                xdb.execute(getContext(), buf.toString());
            }
            else
            {
                home.store(existing);
            }
        }
        catch (HomeException e)
        {
            if (LogSupport.isDebugEnabled(getContext()))
            {
                new MajorLogMsg(this, e.getMessage(), null).log(getContext());
            }
        }
        return true;
    }    
}
