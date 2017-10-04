package com.trilogy.app.crm.web.border;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.XTestIgnoreWebControl;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.calldetail.PrepaidCallingCardWebControl;
import com.trilogy.app.crm.bean.core.PrepaidCallingCard;
import com.trilogy.app.crm.bean.search.PrepaidCallingCardRequest;
import com.trilogy.app.crm.bean.search.PrepaidCallingCardRequestWebControl;
import com.trilogy.app.crm.web.service.PrepaidCallingCardRequestServicer;


public class PrepaidCallingCardCallDetailBorder implements Border
{
    
    public PrepaidCallingCardCallDetailBorder()
    {
        webControl_ = new PrepaidCallingCardWebControl();
    }

    public PrepaidCallingCardCallDetailBorder(boolean unique)
    {
        webControl_ = new PrepaidCallingCardWebControl();
    }

    public void service(
            final Context context,
            final HttpServletRequest request,
            final HttpServletResponse response,
            final RequestServicer delegate)
        throws ServletException, IOException
    {
        final PrintWriter out = response.getWriter();
        PrepaidCallingCard pcc = (PrepaidCallingCard) context.get(PrepaidCallingCard.class);
        
        outputSearch(context, out, pcc);

        if (pcc!=null)
        {
            outputForm(context, out, pcc, request); 
        }
        else
        {
            String msg = "Prepaid Calling Card not found in the context.";
            new MajorLogMsg(this.getClass().getName(), msg, null);
        }

        delegate.service(context, request, response);
    }
    /** Output the Search Form **/
    public void outputForm(Context ctx, PrintWriter out, Object bean, HttpServletRequest req)
    {
       Context subCtx = ctx.createSubContext();
       out.println("<center><table><tbody><tr><td colspan=\"3\">");
       webControl_.toWeb(subCtx, out, WebAgents.rewriteName(ctx, ".border"), bean);
       out.println("<td></tr></tbody></table></center>");
          
    }
    
    private void outputSearch(Context context, PrintWriter out, Object bean)
    {
        PrepaidCallingCard pcc = (PrepaidCallingCard) bean;
        Context subCtx = context.createSubContext();
        String warningMsg = "";

        out.print("\n<form action=\"");
        out.print(WebAgents.getRequestURI(subCtx));
        out.println("\" method=\"post\" onSubmit=\"return checkSubmit()\">");
        PrepaidCallingCardRequest form = new PrepaidCallingCardRequest();
        
        if (bean!=null)
        {
            form.setSerial(pcc.getSerial());
            if (pcc.getPricePlan().endsWith(PrepaidCallingCardRequestServicer.UNKNOWN_PRICE_PLAN_DESCRIPTION))
            {
                warningMsg = "Warning: Calling card type not configured on CRM.";
            }
        }
        
        out.println(XTestIgnoreWebControl.IGNORE_BEGIN);
        out.println("<input type=\"hidden\" name=\"cmd\" value=\"appCRMPrepaidCallingCard\"/>");
        out.println(XTestIgnoreWebControl.IGNORE_END);
        
        final ButtonRenderer buttonRenderer = (ButtonRenderer) subCtx.get(ButtonRenderer.class,
                DefaultButtonRenderer.instance());

        PrepaidCallingCardRequestServicer.displaySearchTable(subCtx, out, buttonRenderer, form, new PrepaidCallingCardRequestWebControl(), false, warningMsg);

        out.println("\n</form>");
    }

    private final PrepaidCallingCardWebControl webControl_;
    
}