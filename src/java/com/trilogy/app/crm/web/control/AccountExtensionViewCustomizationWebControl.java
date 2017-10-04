/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.List;

import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.account.AccountExtensionHolder;
import com.trilogy.app.crm.extension.account.AccountExtensionXInfo;
import com.trilogy.framework.xhome.beans.FacetMgr;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.beans.xi.XInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.DummyMessageMgrSPI;
import com.trilogy.framework.xhome.language.Lang;
import com.trilogy.framework.xhome.language.MessageMgrSPI;
import com.trilogy.framework.xhome.language.MessageMgrSPIProxy;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xhome.webcontrol.WebControl;



/**
 * This web control hides the BAN field of an arbitrary Account extension.  We don't want to see it on the ACT or Account screens.
 *
 * @author Aaron Gourley
 * @since 7.4.16 
 */
public class AccountExtensionViewCustomizationWebControl extends ProxyWebControl
{
    public AccountExtensionViewCustomizationWebControl(WebControl delegate)
    {
        super(delegate);
    }

    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        Context sCtx = ctx.createSubContext();
        
        Extension extension = null;
        if( obj instanceof Extension )
        {
            extension = (Extension)obj;
        }
        else if( obj instanceof AccountExtensionHolder )
        {
            extension = ((AccountExtensionHolder)obj).getExtension();
        } 

        if( extension != null )
        {
            // Clear the title.  The account extension title should be shown by the containing web control.
            final String headerMessageKey = extension.getClass().getSimpleName() + ".Label";
            MessageMgrSPI spi = (MessageMgrSPI)sCtx.get(MessageMgrSPI.class, DummyMessageMgrSPI.instance());
            sCtx.put(MessageMgrSPI.class, new MessageMgrSPIProxy(spi) {
                @Override
                public String get(Context mCtx, String key, Class module, Lang lang, String defaultValue, Object[] args)
                {
                    if( headerMessageKey.equals(key) )
                    {
                        return "";
                    }
                    return super.get(mCtx, key, module, lang, defaultValue, args);
                }
            });
            
            FacetMgr fmgr = (FacetMgr)sCtx.get(FacetMgr.class);
            XInfo xinfo = (XInfo)fmgr.getInstanceOf(sCtx, extension.getClass(), XInfo.class);
            List<PropertyInfo> properties = xinfo.getProperties(sCtx);
            if( properties != null )
            {
                for( PropertyInfo prop : properties )
                {
                    if( AccountExtensionXInfo.BAN.getName().equalsIgnoreCase(prop.getName()) )
                    {
                        // Hide the BAN
                        AbstractWebControl.setMode(sCtx, prop, ViewModeEnum.NONE);
                        break;
                    }
                }
            }
        }

        super.toWeb(sCtx, out, name, obj);
    }

}
