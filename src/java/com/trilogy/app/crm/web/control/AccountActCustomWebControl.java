package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.extension.account.GroupPricePlanExtension;
import com.trilogy.app.crm.support.AccountSupport;


/**
 * Class customized to apply ACT values to Account.
 * 
 * @author marcio.marques@redknee.com
 */
public class AccountActCustomWebControl extends ProxyWebControl
{
    public AccountActCustomWebControl()
    {
        this((WebControl)XBeans.getInstanceOf(ContextLocator.locate(), Account.class, WebControl.class));
    }

    public AccountActCustomWebControl(WebControl delegate)
    {
        setDelegate(delegate);
    }
    
    /**
     * Apply ACT values to the Account profile, and write values to web.
     * @param ctx Context.
     * @param out Output print writer.
     * @param name Object name.
     * @param obj Account object.
     */
    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj) 
    {   
        Account account = (Account) obj;

        if (account.getActId() != account.getLastActId() && account.getActId() != -1)
        {
            applyAccountCreationTemplate(ctx, account, account.getActId());
            account.setLastActId(account.getActId());
        }
        else if( account.getLastActId() != -1 )
        {
            account.setLastActId(account.getActId());
        }
        
        Context subCtx = ctx.createSubContext();
        subCtx.put(GroupPricePlanExtension.SYSTEM_TYPE, account.getSystemType());
        super.toWeb(subCtx, out, name, obj);
    }
    
    @Override
    public Object fromWeb(Context ctx, ServletRequest req, String name)
    {
        Object obj = super.fromWeb(ctx, req, name);

        Account account = (Account) obj;
        if( account.getActId() != -1 )
        {
            applyMandatoryAccountCreationTemplate(ctx, account, account.getActId());   
        }
        
        return obj;
    }
    
    /**
     * Apply values in ACT to Account.
     * @param ctx Context
     * @param account Account
     * @param actId Account creation template identifier
     */
    private void applyAccountCreationTemplate(Context ctx, Account account, int actId)
    {
        try
        {
            AccountSupport.applyAccountCreationTemplate(ctx, account, actId);
        }
        catch(Exception e)
        {
            new MajorLogMsg(
                    this,
                    "Failed to apply account creation template.",
                    e).log(ctx);
        }
    }

    /**
     * Apply mandatory values in ACT to Account.
     * @param ctx Context
     * @param account Account
     * @param actId Account creation template identifier
     */
    private void applyMandatoryAccountCreationTemplate(Context ctx, Account account, int actId)
    {
        try
        {
            AccountSupport.applyAccountCreationTemplate(ctx, account, actId, true);
        }
        catch(Exception e)
        {
            new MajorLogMsg(
                    this,
                    "Failed to apply account creation template.",
                    e).log(ctx);
        }
    }
}
