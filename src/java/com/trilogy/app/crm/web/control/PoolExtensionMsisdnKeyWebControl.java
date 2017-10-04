/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.extension.account.PoolExtensionXInfo;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.facets.java.lang.StringWebControl;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.HiddenWebControl;
import com.trilogy.framework.xhome.webcontrol.PrimitiveWebControl;
import com.trilogy.framework.xhome.webcontrol.ReadOnlyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * WebControl for selecting MSISDN's and From Pool MSISDN Group configured for PooledExtension's spid.
 * 
 * @author simar.singh@redknee.com
 */
public class PoolExtensionMsisdnKeyWebControl extends PrimitiveWebControl
{

    @Override
    public Object fromWeb(Context ctx, ServletRequest req, String name) throws NullPointerException
    {
        return webControl_.fromWeb(ctx, req, name);
    }


    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        PoolExtension poolExtension = (PoolExtension) ctx.get(AbstractWebControl.BEAN);
        String msisdn = "";
        try
        {
            if (ctx.getInt("MODE", DISPLAY_MODE) == CREATE_MODE)
            {
                msisdn = poolExtension.getFreePoolMsisdn(ctx);
            }
            else
            {
                msisdn = poolExtension.findPoolMsisdn(ctx);
            }
        }
        catch (Throwable t)
        {
            new MinorLogMsg(this, "Error in finding Pool MSISDN", t).log(ctx);
            ExceptionListener excl = (ExceptionListener) ctx.get(ExceptionListener.class);
            if (null != excl)
            {
                excl.thrown(new IllegalPropertyArgumentException(PoolExtensionXInfo.POOL_MSISDN, t.getMessage()));
            }
        }
        if(null != msisdn && !msisdn.isEmpty())
        {
            webControl_.toWeb(ctx, out, name, msisdn);
        }
        else
        {
            webControl_.toWeb(ctx, out, name, "Not Available (Ensure Availability from Pool MSISDN Group)");
        }
        //out.println("<input type=\"hidden\" name=\"" + name + "\" value=\""+(msisdn).replaceAll("\"", "&quot;")+"\" />");
        
    }
    
    private WebControl webControl_ = new HiddenWebControl(new ReadOnlyWebControl(new StringWebControl()));
    
//    private String getMsgLoadMorePoolMsisdns(Context ctx, Object... values)
//    {
//        final MessageMgr msgMgr;
//        msgMgr = new MessageMgr(ctx, PoolExtensionMsisdnKeyWebControl.class);
//        return LOAD_MORE_MSISDN_TEXT.get(msgMgr, values);
//    }

    
//    private static final MessageManagerText LOAD_MORE_MSISDN_TEXT = new MessageManagerText(
//            "PoolMsisdn.LOAD_MORE_MSISDNS", "Please load more MSISDNs in MSISDN Group ID [ {0} ]");
}
