package com.trilogy.app.crm.web.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Transaction;


public class ReceiptViewerServicer extends InvoiceServerRemoteRequestServicer
{
    public ReceiptViewerServicer(Context ctx, String cmd)
    {
        super(ctx, cmd, cmd);
    }
    
    public ReceiptViewerServicer(Context ctx, String localCmd, String remoteCmd)
    {
        super(ctx, localCmd, remoteCmd);
    }

    @Override
    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        String url = getURL(ctx, req);

        try
        {
            // There is some amount of buffering in the response,
            // and that is not commited in the case of serving the PDFs
            res.reset();

            StringBuilder buf = new StringBuilder(url);
            buf.append("&");
            buf.append(getRemoteCmd().replaceAll(" ", "%20"));
            buf.append("&menu=hide");
            buf.append("&border=hide");
            buf.append("&header=hide");
            buf.append("&username=");
            buf.append(getUsername());
            buf.append("&password=");
            buf.append(getPassword(ctx));
            buf.append("&_uri=");
            buf.append(WebAgents.getRequestURI(ctx).replaceAll(getRemoteCmd(), getLocalCmd()));
            buf.append("&.");

            // Authenticate the url as the user can provide a false get/post
            URLConnection conn = null;

            long pageBytes = 0;
            int data = -1;

            try
            {
                conn = new URL(buf.toString()).openConnection();
            }
            catch(MalformedURLException e)
            {
                throw new IOException("MalformedURL ["+buf.toString()+"]");
            }

            final boolean differentTransactionInContext = isDifferentTransactionInContext(ctx);

            if (conn != null && !differentTransactionInContext)
            {
                String type = conn.getContentType();

                if (type == null || type.indexOf("text/html") == -1)
                {
                    res.setContentType("application/pdf");
                    res.addHeader ("Pragma", "no-cache");
                }
                ServletOutputStream os = res.getOutputStream();
                BufferedInputStream bis = null;

                try
                {
                  bis = new BufferedInputStream(conn.getInputStream());
                  while ((data = bis.read()) >= 0)
                  {
                    os.write(data);
                                pageBytes++;
                  }
                }
                finally
                {
                    try
                    {
                        if (bis != null)
                        {
                            bis.close();
                        }
                    }
                    catch(IOException e)
                    {
                    }
                }
            }

            if (conn == null || differentTransactionInContext || pageBytes < 100)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    if (conn == null)
                    {
                        new DebugLogMsg(this, "Error generating receipt.", null)
                        .log(ctx);
                    }
                    else if(differentTransactionInContext)
                    {
                        new DebugLogMsg(this, "Error generating receipt. Transaction in the Context does not match the " +
                                "transaction whose receipt was requested.  Reload the transaction and try again.", null)
                        .log(ctx);
                    }
                    else
                    {
                        new DebugLogMsg(this, "Error generating receipt. The receipt file is corrupted.", null)
                        .log(ctx);
                    }
                }
                MessageMgr mmgr = new MessageMgr(ctx, this);

                res.reset();
                res.setContentType("text/html");

                PrintWriter out = res.getWriter();
                out.print("<html><body>");
                out.print(mmgr.get("Receipt not available.", "<font color=\"red\"><b>Receipt not available. Please see the log for details.</b></font>"));
                out.println("<body></html>");
            }

             sendUMLogMsg(ctx, url, true, null);
        }
        catch(IOException e)
        {
            MessageMgr mmgr = new MessageMgr(ctx, this);

            res.reset();
            res.setContentType("text/html");

            PrintWriter out = res.getWriter();
            out.print("<html><body>");
            out.print(mmgr.get("ERROR: Unable to retrieve receipt.", "<font color=\"red\"><b>ERROR: Unable to retrieve receipt.</b></font>"));

            out.println("<!--");
            e.printStackTrace(out);
            out.println("\n-->");

            out.println("</body></html>");

            sendUMLogMsg(ctx, url, false, e);
            throw e;
        }
    }


    /**
     *
     */
    private boolean isDifferentTransactionInContext(final Context context)
    {
        final String transactionId = WebAgents.getParameter(context, ".transactionId");
        final Transaction transaction = (Transaction)Session.getSession(context).get(Transaction.class);

        return (transaction != null && !String.valueOf(transaction.getReceiptNum()).equals(transactionId));
    }
}
