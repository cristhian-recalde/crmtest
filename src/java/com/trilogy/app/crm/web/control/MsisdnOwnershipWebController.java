package com.trilogy.app.crm.web.control;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.MsisdnOwnership;
import com.trilogy.app.crm.bean.MsisdnOwnershipXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xhome.webcontrol.WebController;
import com.trilogy.framework.xhome.webcontrol.XTestIgnoreWebControl;

public class MsisdnOwnershipWebController
    extends WebController
{

    public MsisdnOwnershipWebController()
    {
    }

    public MsisdnOwnershipWebController(WebControl table, WebControl control, Home home)
    {
        super(table, control, home);
    }

    public MsisdnOwnershipWebController(IdentitySupport id, WebControl table, WebControl control, Home home)
    {
        super(id, table, control, home);
    }

    public MsisdnOwnershipWebController(WebControl table, WebControl control, Object home)
    {
        super(table, control, home);
    }

    public MsisdnOwnershipWebController(IdentitySupport id, WebControl table, WebControl control, Object home)
    {
        super(id, table, control, home);
    }

    public MsisdnOwnershipWebController(WebControl table, WebControl control, ContextFactory home)
    {
        super(table, control, home);
    }

    public MsisdnOwnershipWebController(IdentitySupport id, WebControl table, WebControl control, ContextFactory home)
    {
        super(id, table, control, home);
    }

    public MsisdnOwnershipWebController(Context ctx, Class beanType)
    {
        super(ctx, beanType);
    }

    public void outputUpdate(Context ctx, PrintWriter out, HttpServletRequest req, HttpServletResponse res, Object key, String query, MessageMgr mmgr, HTMLExceptionListener hel, int mode)
        throws HomeException, ServletException, IOException
    {
        Object bean = getHome(ctx).find(ctx, key);
        if(bean == null)
        {
            out.println(XTestIgnoreWebControl.IGNORE_BEGIN);
            out.println(mmgr.get("NoEntryError", "<b>No entry with key ''{0}'' was found</b>", new Object[] {
                String.valueOf(query)
            }));
            out.println(XTestIgnoreWebControl.IGNORE_END);
        }
        else
        {
            getWebControl().fromWeb(ctx, bean, req, "");
            
            MsisdnOwnership form = (MsisdnOwnership) bean;
            boolean hasChangedMsisdn = (form.getOriginalMsisdn() != null && form.getMsisdn() != null
                                        && !form.getOriginalMsisdn().equals(form.getMsisdn()));
            String confirmUpdate = req.getParameter(confirmFlag);
            boolean hasConfirmedUpdate = confirmUpdate != null && confirmUpdate.equals("Y");
            if (hasChangedMsisdn && !hasConfirmedUpdate)
            {
                ButtonRenderer br = (ButtonRenderer)ctx.get(ButtonRenderer.class, DefaultButtonRenderer.instance());
                Link link = new Link(ctx);
                link.add("CMD", "Update");
                link.add("key", form.getOriginalMsisdn());
                link.add(".language", form.getLanguage());
                link.add(confirmFlag, "Y");
                
                out.println(XTestIgnoreWebControl.IGNORE_BEGIN);
                String msg = "Warning: All the subscriptions attached to the Mobile Number: " + form.getOriginalMsisdn() + 
                    "<br/> will be changed to be available on Mobile Number: " + form.getMsisdn() + 
                    ".<br/>  Click <b>Confirm Update</b> to continue with the update.";
                hel.thrown(new IllegalPropertyArgumentException(MsisdnOwnershipXInfo.MSISDN, msg));
                hel.toWeb(ctx, out, null, null);
                hel.clear();
                
                br.linkButton(out, ctx, "Confirm Update", "Confirm Update", link);
                out.print("&nbsp;");
                out.println(XTestIgnoreWebControl.IGNORE_END);
            }
            if(hel.hasErrors())
            {
                out.println(XTestIgnoreWebControl.IGNORE_BEGIN);
                out.println(mmgr.get("UnableToUpdateEntry", "<font color=\"red\"><b>Unable to Update Entry: ''{0}''</b></font>", new Object[] {
                    String.valueOf(key)
                }));
                out.println(XTestIgnoreWebControl.IGNORE_END);
                outputDetailView(ctx, req, res, hel, mode, key, bean);
            }
            else
            {
                try
                {
                    if (!hasChangedMsisdn || 
                            (hasChangedMsisdn && hasConfirmedUpdate))
                    {
                        bean = getHome(ctx).store(ctx, bean);
                        out.println(XTestIgnoreWebControl.IGNORE_BEGIN);
                        StringBuilder msg = new StringBuilder();
                        msg.append("<center><b>Updated Entry: {0}. </b></center>");
                        if (hasChangedMsisdn)
                        {
                            msg.append("<br/><center>All the subscriptions attached to the Mobile Number: ");
                            msg.append(form.getOriginalMsisdn());
                            msg.append("<br/> have now changed associations to Mobile Number: </center>");
                            msg.append(form.getMsisdn());
                        }
                        out.println(mmgr.get("UpdatedEntry", msg.toString(), new Object[] {
                                String.valueOf(key)
                        }));
                        
                        out.println(XTestIgnoreWebControl.IGNORE_END);
                    }
                }
                catch(HomeException e)
                {
                    if(e.getCause() instanceof CompoundIllegalStateException)
                    {
                        ((CompoundIllegalStateException)e.getCause()).rethrow(hel);
                    }
                    else
                    {
                        outputError(ctx, mmgr, out, e.getMessage());
                    }
                }
                if(key != null)
                {
                    outputDetailView(ctx, req, res, hel, mode, key, bean);
                }
            }
        }
    }
    
    private String confirmFlag = "confirmUpdate";
}
