package com.trilogy.app.crm.web.border.search;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.web.xmenu.service.XMenuService;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;

public class RedirectSearchMenuBorder implements Border 
{

	public RedirectSearchMenuBorder(String servicerKey) 
	{
		super();
		servicerKey_ = servicerKey;
	}

	public void service(Context ctx, HttpServletRequest req,
			HttpServletResponse res, RequestServicer delegate)
			throws ServletException, IOException 
	{
        String key = (String) req.getParameter("key");
        String newCmd = (String) req.getParameter("CMD");
        String preview = (String) req.getParameter("PreviewCMD.x");
        String copy = (String) req.getParameter("CopyCMD.x");
        String save = (String) req.getParameter("SaveCMD.x");
        
        if ((key == null) && 
        		(preview == null || preview.equals("")) && 
        		(newCmd == null || !newCmd.equals("New")) &&
        		(copy == null || copy.equals(""))&&
        		(save == null || save.equals("")))
        {
        	XMenuService srv = (XMenuService) ctx.get(XMenuService.class);
        	RequestServicer advSearch = srv.getServicer(ctx, servicerKey_);
        	advSearch.service(ctx, req, res);
        }
        else
        {
        	delegate.service(ctx, req, res);
        }
	}

	private String servicerKey_;
}