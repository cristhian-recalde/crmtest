package com.trilogy.app.crm.home;

import java.util.Iterator;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.home.sub.SubscriberProvisionLogHome;
import com.trilogy.app.crm.support.FrameworkSupportHelper;

/**
 * The EcareAdapter is an adapter that is used in conjunction with AdapterHome
 * it gets the exception listner from BAS node and prints it on screen
 * 
 * @author karen lin
 * 
 */
public class EcareAdapter implements Adapter
{

    public Object unAdapt(Context ctx, Object obj)
    {
        
        return obj;

    }

    public Object adapt(Context ctx, Object obj)
    {

        Subscriber sub = (Subscriber) obj;
        if (sub.getExceptionList() != null)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "adapt(): Sub "+ sub.getBAN()+" has Exceptions " + sub.getExceptionList() , null).log(ctx);
            }

            final HTMLExceptionListener listener = new HTMLExceptionListener(new MessageMgr(ctx, this));

            for (Iterator it = sub.getExceptionList().iterator(); it.hasNext();)
            {
                listener.thrown((Throwable) it.next());
            }

            final Context subCtx = ctx.createSubContext();
            subCtx.put(HTMLExceptionListener.class, listener);
            
            if ( LogSupport.isDebugEnabled(subCtx))
            {
                new DebugLogMsg(this, "HttpServletResponse is " + FrameworkSupportHelper.get(subCtx).getWriter(subCtx) == null ? "null": "Not NULL", null).log(subCtx);
            }
            SubscriberProvisionLogHome.printCapturedExceptions(subCtx, sub);
        }
        return obj;

    }
}
