package com.trilogy.app.crm.web.border;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;

public class RemoteApplicationHideFieldsBorder
   implements Border
{
	public void service(Context ctx, HttpServletRequest req,
			HttpServletResponse res, RequestServicer delegate)
			throws ServletException, IOException 
	{
      Context subCtx = ctx.createSubContext();
      subCtx.put("RemoteApplication.key.mode", ViewModeEnum.NONE);
      subCtx.put("RemoteApplication.startTime.mode", ViewModeEnum.NONE);
      subCtx.put("RemoteApplication.uptime.mode", ViewModeEnum.NONE);
      subCtx.put("RemoteApplication.lastHeartbeat.mode", ViewModeEnum.NONE);
      subCtx.put("RemoteApplication.persistent.mode", ViewModeEnum.NONE);
      subCtx.put("RemoteApplication.rmiServices.mode", ViewModeEnum.NONE);

      delegate.service(subCtx, req, res);
   }
}
