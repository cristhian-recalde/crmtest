package com.trilogy.app.crm.factory;

import java.io.IOException;
import com.trilogy.app.crm.account.BANAware;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.MsisdnOwnership;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;


public class MsisdnOwnershipFactory implements ContextFactory
{

    public Object create(Context ctx)
    {
        final MsisdnOwnership msisdnOwnership = new MsisdnOwnership();
        
        if(ctx.has(Account.class))
        {
            Account account = (Account)ctx.get(Account.class);
            msisdnOwnership.setBAN(account.getBAN());
            msisdnOwnership.setSubscriberType(account.getSystemType());
            msisdnOwnership.setOriginalSubscriberType(account.getSystemType());
            msisdnOwnership.setSpid(account.getSpid());
        }
        else if (ctx.has(Subscriber.class))
        {
            Subscriber sub = (Subscriber)ctx.get(Subscriber.class);
            msisdnOwnership.setBAN(sub.getBAN());
            msisdnOwnership.setSubscriberType(sub.getSubscriberType());
            msisdnOwnership.setOriginalSubscriberType(sub.getSubscriberType());
            msisdnOwnership.setSpid(sub.getSpid());
        }
        else if(ctx.has(AbstractWebControl.class))
        {
            Object obj = ctx.get(AbstractWebControl.class);
            if( obj instanceof BANAware)
            {
                msisdnOwnership.setBAN(((BANAware)obj).getBAN());
            }
            
            if( obj instanceof SpidAware)
            {
                msisdnOwnership.setSpid(((SpidAware)obj).getSpid());
            }
            
            // TODO handle subscriber type
        }
        
        return msisdnOwnership;
    }
}
