package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.HashSet;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.account.AccountExtension;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.web.border.AccountCopyBorder;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;


public class AccountCopyWebControl extends ProxyWebControl
{
    public AccountCopyWebControl(WebControl delegate)
    {
        setDelegate(delegate);
    }
    
    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj) 
    {   
        Account account = (Account) obj;
        if (ctx.getBoolean(AccountCopyBorder.ACCOUNT_COPY_PERFORMED, Boolean.FALSE))
        {
        	// Removing the values from discount class holder.
        	account.setDiscountsClassHolder(new HashSet());
            ctx.remove(AccountCopyBorder.ACCOUNT_COPY_PERFORMED);
            for (Extension extension : account.getExtensions())
            {
                ((AccountExtension) extension).setBAN("");
                
                if (extension instanceof PoolExtension)
                {
                    PoolExtension pool = (PoolExtension) extension;
                    pool.setPoolMsisdnByForce("");
                }
            }
        }
        getDelegate().toWeb(ctx, out, name, obj);
    }

}
