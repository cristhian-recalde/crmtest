package com.trilogy.app.crm.web.border;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;

import com.trilogy.app.crm.web.service.PrepaidCallingCardRequestServicer;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.webcontrol.WebControl;


public class PrepaidCallingCardCallDetailSearchBorder extends CallDetailSearchBorder
{
    public PrepaidCallingCardCallDetailSearchBorder(final Context context)
    {
        super(context);
    }

    public PrepaidCallingCardCallDetailSearchBorder(final Context context, final WebControl webControl)
    {
        super(context, webControl);
    }

    public void outputForm(Context ctx, PrintWriter out, FormRenderer frend, ButtonRenderer brend, Object bean, HttpServletRequest req)
    {
        // If a search is being performed, show the search table. Otherwise, hide the table and show a button.
        if (brend.isButton(ctx, PrepaidCallingCardRequestServicer.SEARCH_BUTTON_NAME))    
        {
            out.println(
                    "<table><tr><td colspan=\"2\"><table border=\"0\" id=\"foldinglist-search\""
                            +"><tr><td>");
        }
        else
        {
            writeShowTableScript(out);
            out.println("<table border=\"0\" id=\"foldinglist-morebutton\"><tr><td>");
            brend.inputButton(out, ctx, this.getClass(), "Search More Call Details", true, "javascript:showTable('foldinglist-search', 'foldinglist-morebutton');");
            out.println("</td></tr></table>");
            out.println(
                    "<table><tr><td colspan=\"2\"><table border=\"0\" id=\"foldinglist-search\""
                            + " style=\"display:none\" "
                            +"><tr><td>");
        }
        super.outputForm(ctx, out, frend, brend, bean, req);
        out.println("</td></tr></table></td></tr></table>");
    }    
    
    private void writeShowTableScript(PrintWriter out)
    {
        out.println("<script type=\"text/javascript\">");
        out.println("function showTable(table, buttonTable)");
        out.println("{");
        out.println("    document.getElementById(table).style.display = 'block';");
        out.println("    document.getElementById(buttonTable).style.display = 'none';");
        out.println("}");
        out.println("</script>");
    }
}
