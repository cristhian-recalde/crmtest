/*
 * Created on Apr 1, 2003
 * 
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of REDKNEE.com.
 * ("Confidential Information"). You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement you entered
 * into with REDKNEE.com.
 * 
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * REDKNEE.COM SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DCDRIVATIVES.
 */
package com.trilogy.app.crm.bas.promotion.home;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.config.CallDetailConfig;


/**
 * add ReportCmd in the home
 * 
 * @author kwong
 * 
 */
public class CallDetailSummaryAdaptedHome extends HomeProxy
{

    public CallDetailSummaryAdaptedHome(Context ctx, Home delegate)
    {
        super(delegate);
        setContext(ctx);
    }


    @Override
    public Object cmd(Context ctx, Object arg) throws HomeException
    {
        if (arg instanceof ReportCmd)
        {
            ReportCmd cmd = (ReportCmd) arg;
            final List list = new ArrayList();
         
            CallDetailConfig config = (CallDetailConfig) ctx.get(CallDetailConfig.class);
            String tableName = config.getTableName();
            if (tableName == null || tableName.isEmpty())
            {
                tableName = "CallDetail";
            }
            if (tableName.length() > 0)
            {
                String sql = "Select subscriberid, chargedmsisdn, ban, spid, calltype,"
                        + " billingcategory, sum(duration) as \"duration\", sum(usedMinutes) as \"usedMinutes\","
                        + " sum(datausage) as \"datausage\", count(*) as \"count\" ,sum(charge) as \"charge\" "
                        + "from " + tableName + " " + cmd.getWhere()
                        + " group by subscriberid, chargedmsisdn, ban, spid, calltype, billingcategory"
                        + " order by chargedmsisdn";
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "cmd - sql: " + sql, null).log(ctx);
                }
                XDB xdb = (XDB) ctx.get(XDB.class);
                xdb.forEach(ctx, new Visitor()
                {

                    public void visit(Context _ctx, Object obj) throws AgentException, AbortVisitException
                    {
                        list.add(obj);
                    }
                }, sql);
            }
            return list;
        }
        return super.cmd(ctx, arg);
    }
}
