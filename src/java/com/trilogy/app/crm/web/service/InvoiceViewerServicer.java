package com.trilogy.app.crm.web.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;

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

import com.trilogy.app.crm.bean.Account;

public class InvoiceViewerServicer extends InvoiceServerRemoteRequestServicer
{
    public InvoiceViewerServicer(Context ctx, String cmd)
    {
        super(ctx, cmd, cmd);
    }
    
    public InvoiceViewerServicer(Context ctx, String localCmd, String remoteCmd)
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

            final boolean differentAccountIsInContext = isDifferentAccountInContext(ctx);

            if (conn != null && !differentAccountIsInContext)
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

            if (conn == null || differentAccountIsInContext || pageBytes < 100)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    if (conn == null)
                    {
                        new DebugLogMsg(this, "Error generating preview. Possibly, the file doesn't exist " +
                                "in the specified location, or the user does not have access to this file.", null)
                        .log(ctx);
                    }
                    else if(differentAccountIsInContext)
                    {
                        new DebugLogMsg(this, "Error generating preview. Account in the Context does not match the " +
                                "Account whose Invoice was requested.  Reload the Account and try again.", null)
                        .log(ctx);
                    }
                    else
                    {
                        new DebugLogMsg(this, "Error generating preview. The Invoice file is corrupted.", null)
                        .log(ctx);
                    }
                }
                MessageMgr mmgr = new MessageMgr(ctx, this);

                res.reset();
                res.setContentType("text/html");

                PrintWriter out = res.getWriter();
                out.print("<html><body>");
                out.print(mmgr.get("Invoice not available.", "<font color=\"red\"><b>Invoice not available. Please see the log for details.</b></font>"));
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
            out.print(mmgr.get("ERROR: Unable to retrieve invoice.", "<font color=\"red\"><b>ERROR: Unable to retrieve invoice.</b></font>"));

            out.println("<!--");
            e.printStackTrace(out);
            out.println("\n-->");

            out.println("</body></html>");

            sendUMLogMsg(ctx, url, false, e);
            throw e;
        }
    }
    
    @Override
    public String getArgs(HttpServletRequest req)
    {
       if ( ! proxyParms_ )
       {
          return "";
       }

       return InvoiceViewerServicer.buildArgsIgnoringLoginCredentials(req);
    }


    public static String buildArgsIgnoringLoginCredentials(HttpServletRequest req)
    {
       return InvoiceViewerServicer.buildArgsIgnoringLoginCredentials(req, '&');
    }

    public static String buildArgsIgnoringLoginCredentials(HttpServletRequest req, char seperator)
    {
       StringBuffer args = new StringBuffer();

       for ( Iterator i = req.getParameterMap().keySet().iterator() ; i.hasNext() ; )
       {
          String key = (String) i.next();

          if ( "cmd".equals(key) || "username".equals(key) || "password".equals(key))
          {
             continue;
          }

          args.append(seperator);
          args.append(key);
          args.append('=');
          args.append(URLEncoder.encode(req.getParameter(key)));

          seperator = '&';
       }

       return args.toString();
    }
       


    /**
     *
     */
    private boolean isDifferentAccountInContext(final Context context)
    {
        final String accountIdentifier = WebAgents.getParameter(context, ".accountIdentifier");
        final Account account = (Account)Session.getSession(context).get(Account.class);

        return (account != null && !account.getBAN().equals(accountIdentifier));
    }
}
