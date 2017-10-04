package com.trilogy.app.crm.web.control;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.search.CallDetailSearch;
import com.trilogy.app.crm.bean.search.CallDetailSearchWebControl;


public class PrepaidCallingCardCallDetailSearchWebControl extends CallDetailSearchWebControl
{
    @Override
    public Object fromWeb(Context ctx, ServletRequest req, String name)
    {
      Object obj = null;

      try
      {
        obj = XBeans.instantiate(CallDetailSearch.class, ctx);
        CallDetailSearch cds = (CallDetailSearch) obj;
        cds.setLimit(RECENT_PCC_CALLDETAIL_DEFAULT_LIMIT);
        cds.setStartDate(null);
        cds.setEndDate(null);
      }
      catch (Exception e)
      {
        if(LogSupport.isDebugEnabled(ctx))
        {
          new DebugLogMsg(this,e.getMessage(),e).log(ctx);
        }

      }

      fromWeb(ctx, obj, req, name);

      return obj;
    }
    
    private static int RECENT_PCC_CALLDETAIL_DEFAULT_LIMIT = 5;

}
