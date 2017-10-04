package com.trilogy.app.crm.home;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.Subscriber;

/**
 * adapt the Exception and set the listner
 * @author klin
 *
 */
public class BasAdapter implements Adapter {
   
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public Object adapt(Context ctx, Object obj){

        Subscriber sub = (Subscriber)obj;
        if ( sub.getExceptionListener() != null )
        {           
            
            ArrayList exList = new ArrayList(sub.getExceptionListener().getExceptions());
            sub.setExceptionList(exList);
            if ( exList.size() > 0)
            {
                sub.setLastExp((Exception)exList.get(0));
            }
            
            if ( LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Sub "+sub.getBAN()+" has exception list "+ sub.getExceptionList() , null).log(ctx);
            }
            
        }
                        
        return obj;
        
        
    }
    
    public Object unAdapt(Context ctx, Object obj){

        return obj;
    }

}
