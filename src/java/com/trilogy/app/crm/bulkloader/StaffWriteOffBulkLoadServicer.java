package com.trilogy.app.crm.bulkloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.StaffWriteOffBulkLoad;
import com.trilogy.app.crm.bean.StaffWriteOffGui;
import com.trilogy.app.crm.bean.StaffWriteOffGuiWebControl;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.transaction.AccountWriteOffProcessor;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.web.util.ImageLink;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class StaffWriteOffBulkLoadServicer implements RequestServicer
{
    public void service(Context ctx, HttpServletRequest req,
            HttpServletResponse res) throws ServletException, IOException
    {
        final Context subCtx = ctx.createSubContext();
        subCtx.put("MODE", OutputWebControl.EDIT_MODE);

        // Get the input from the form.
        final StaffWriteOffGui form = new StaffWriteOffGui();
        webcontrol.fromWeb(subCtx, form, req, "");

        final FormRenderer formRenderer = (FormRenderer) subCtx.get(
                FormRenderer.class, DefaultFormRenderer.instance());

        PrintWriter htmlOut = res.getWriter();
        formRenderer.Form(htmlOut, subCtx);

        htmlOut.print("<table>"); // Begin output to the HTML page

        final ButtonRenderer buttonRenderer = (ButtonRenderer) subCtx.get(
                ButtonRenderer.class, DefaultButtonRenderer.instance());

        if (buttonRenderer.isButton(subCtx, "Run"))
        {
            process(ctx, htmlOut, new File(form.getFullAdjustmentFilePath()), form
                    .getReportFilePath());

        }

        // Print the form and the buttons to the HTML page.
        htmlOut.print("<tr><td>");
        webcontrol.toWeb(subCtx, htmlOut, "", form); // AdjustmentBulkLoad form
        outputHelpLink(subCtx, htmlOut); // Help button
        buttonRenderer.inputButton(htmlOut, subCtx, "Run"); // Run button
        htmlOut.print("</td></tr></table>");
        formRenderer.FormEnd(htmlOut);

    }

    /**
     * Copied from com.redknee.framework.xhome.webcontrol.BeanWebController.
     * This functionality is expected to be provided in a more reusable format
     * in an upcoming implementation of the Framework.
     */
    private void outputHelpLink(final Context context, final PrintWriter out)
    {
        final ImageLink image = new ImageLink(context);
        final MessageMgr mmgr = new MessageMgr(context, this);
        final Link link = new Link(context);

        link.add("mode", "help");
        link.add("border", "hide");
        link.add("menu", "hide");

        final String redirectURL = link.write();

        out.print("<a href=\"" + redirectURL + "\" onclick=\"showHelpMenu('"
                + redirectURL + "'); return false\"><img border=\"0\" "
                + image.getSource(mmgr.get("Button.Help", "Help"))
                + " align=\"right\" alt=\"Help\" /></a>");
    }

    private String getReportFilename()
    {
        final SimpleDateFormat formatter = new SimpleDateFormat("ddMMyy_HHmmss");
        return REPORT_FILE_PREFIX + formatter.format(new Date())
                + REPORT_FILE_SUFFIX;
    }

    private void process(Context ctx, PrintWriter htmlOut, File bulkloadFile,
            String reportPath)
    {
        Collection<StaffWriteOffBulkLoad> writeOffs = null;
        PrintWriter reportOut = null;
        FileOutputStream reportFile = null;

        final String reportFilename = reportPath + File.separator
                + getReportFilename();

        try
        {

            reportFile = new FileOutputStream(new File(reportFilename));
            reportOut = new PrintWriter(reportFile);

        }
        catch (IOException e)
        {
            htmlOut
                    .print("Failed to open the report file, please verify the path");
            return;
        }

        try
        {
            writeOffs = StaffWriteOffBulkLoadSupport.parseFile(ctx,
                    bulkloadFile, reportOut);
            bulkloadFile.delete();
        }
        catch (Exception e)
        {
            outputMessage(
                    htmlOut,
                    "Failed to open the bulk loading file, please verify the file",
                    COLOR_ERROR);
            return;
        }

        for (StaffWriteOffBulkLoad writeOff : writeOffs)
        {
            reportOut.println(writeOff.getOrigString());
            Account account = getAccount(ctx, writeOff);
            if (account != null)
            {
                AccountWriteOffProcessor processor = new AccountWriteOffProcessor(
                        ctx, account, writeOff);
                try
                {
                    processor.process(ctx, reportOut);
                }
                catch (Exception e)
                {
                    reportOut.println("fail to process this entry."
                            + e.getMessage());
                    new MinorLogMsg(this, "fail to process staff write off."
                            + writeOff.getBAN(), null).log(ctx);
                }
            }
            else
            {
                // log.
                reportOut.println("fail to find account " + writeOff.getBAN());
            }
        }

        try
        {
            if (reportOut != null)
            {
                reportOut.flush();
                reportOut.close();
            }
            if (reportFile != null)
            {
                reportFile.close();
            }
        }
        catch (Exception e)
        {
            new MinorLogMsg(this,
                    "fail to close staff write off bulk loading report file", e)
                    .log(ctx);
        }

        outputMessage(htmlOut, "Staff write off bulk load file "
                + bulkloadFile.getName() + " is done, please check report file"
                + reportFilename + " for details", COLOR_NORMAL);
    }

    private Account getAccount(Context ctx, StaffWriteOffBulkLoad writeOff)
    {
        if (writeOff.getBAN() == null)
        {
            return null;
        }

        try
        {
            return AccountSupport.getAccount(ctx, writeOff.getBAN().trim());
        }
        catch (HomeException e)
        {
            // do nothing here.
        }

        return null;
    }

    private void outputMessage(PrintWriter htmlOut, String msg, String color)
    {
        htmlOut.println("<tr><td align=\"center\"><b style=\"color:" + color
                + ";\">");
        htmlOut.println(msg);
        htmlOut.println("</b></td></tr>");

    }

    private final WebControl webcontrol = new StaffWriteOffGuiWebControl();
    private static final String REPORT_FILE_PREFIX = "StaffWriteOffBulkLoad.";
    private static final String REPORT_FILE_SUFFIX = ".txt";
    private static final String COLOR_NORMAL = "green";
    private static final String COLOR_ERROR = "red";

}
