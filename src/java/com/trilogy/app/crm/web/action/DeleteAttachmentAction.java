package com.trilogy.app.crm.web.action;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.account.AccountAttachment;
import com.trilogy.app.crm.bean.account.AccountAttachmentHome;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgents;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


public class DeleteAttachmentAction extends AbstractConfirmingAction
{

    /**
     * author: simar singh@redknee.com
     * delete the attachment and clean dependecies
     */
    private static final long serialVersionUID = 1L;


    public DeleteAttachmentAction()
    {
        // super("applyDisputeFee", "Apply Dispute Fee");
        super("deleteAttachment", "Remove");
    }


    public void execute(Context ctx) throws AgentException
    {
        PrintWriter out = WebAgents.getWriter(ctx);
        String stringKey = WebAgents.getParameter(ctx, "key");
        Home home = (Home) ctx.get(AccountAttachmentHome.class);
        try
        {
            AccountAttachment accAttach = (AccountAttachment) home.find(ctx, stringKey);
            Account account = AccountSupport.getAccount(ctx, accAttach.getBAN());
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Received request to mark as profile photo with [key=" + stringKey
                        + "]. For BAN: " + accAttach.getBAN(), null).log(ctx);
            }
            if (accAttach == null)
            {
                printError(out, "Unable to find specified attachment with [key=" + stringKey + "]");
            }
            else if (account == null)
            {
                printError(out, "Unable to find account with [BAN=" + accAttach.getBAN() + "]");
            }
            else
            {
                try
                {
                    if(stringKey.equals(account.getProfileAttachmentKey()))
                    {
                        account.setProfileAttachmentKey("");
                        ((Home) (ctx.get(AccountHome.class))).store(ctx, account);
                    }
                    
                    Account sessionAccount = (Account) Session.getSession(ctx).get(Account.class);
                    if (sessionAccount != null && sessionAccount.getBAN() == account.getBAN())
                    {
                        sessionAccount.setProfileAttachmentKey("");
                    }
                    accAttach.setProfilePhoto(false);
                    ((Home) (ctx.get(AccountAttachmentHome.class))).remove(ctx, accAttach);
                    printMessage(out, "Succcessfully removed attachment for BAN: " + accAttach.getBAN());
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "Succcessfully changed the profile photo for BAN: " + accAttach.getBAN(),
                                null).log(ctx);
                    }
                }
                catch (HomeException e)
                {
                    new InfoLogMsg(this,
                            "Encountered a HomeException while trying to set profile photo with attachment [key="
                                    + stringKey + "].  The change will fail.", e).log(ctx);
                    printError(out, "Unable to set profile photo for Account BAN " + accAttach.getBAN() + " with [key="
                            + stringKey + "] due to HomeException [message=" + e.getMessage() + "]");
                }
            }
        }
        catch (HomeException e)
        {
            new InfoLogMsg(this, "Encountered a HomeException while trying to retrieve and set Attachments with [key="
                    + stringKey + "].  Attempt to Apply Dispute Fee will be failed.", e).log(ctx);
            printError(out, "Unable to retrieve Attachment with [key=" + stringKey
                    + "] due to HomeException [message=" + e.getMessage()
                    + "].  Check Application logs for more details.");
        }
        //ContextAgents.doReturn(ctx);
        // return to summary view. There will be no bean to pop-up :)
        returnToSummaryView(ctx);
    }
    
    public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
    {
        if (bean instanceof AccountAttachment)
        {
            AccountAttachment sub = (AccountAttachment) bean;
            if (isAllowed(ctx, sub))
            {
                link = modifyLink(ctx, bean, link);
                out.print("<a href=\"");
                link.write(out);
                out.print("\" onclick=\"try{return confirm('" + getConfirmationMessage(ctx, sub)
                        + "');}catch(everything){}\">");
                out.print(getLabel());
                out.print("</a>");
            }
        }
    }


    public void writeLinkDetail(Context ctx, PrintWriter out, Object bean, Link link)
    {

        if (bean instanceof AccountAttachment)
        {
            AccountAttachment attachment = (AccountAttachment) bean;
            if (isAllowed(ctx, attachment))
            {
                MessageMgr mmgr = new MessageMgr(ctx, this);
                ButtonRenderer br = (ButtonRenderer) ctx
                        .get(ButtonRenderer.class, DefaultButtonRenderer.instance());
                link = modifyLink(ctx, bean, link);
                out.print("<a href=\"");
                link.write(out);
                out.print("\" onclick=\"try{return confirm('" + getConfirmationMessage(ctx, attachment)
                        + "');}catch(everything){}\">");
                out.print(mmgr.get("WebAction." + getKey() + ".DetailLabel", br.getButton(ctx, getKey(), mmgr.get(
                        "WebAction." + getKey() + ".Label", getLabel()))));
                out.print("</a>");
            }
        }
    
    }


    protected String getConfirmationMessage(Context ctx, AccountAttachment attachment)
    {
        if(attachment.isProfilePhoto())
        {
           return "Are you sure you want to delete the Attachment [" +attachment.getFileName()+ "]?. This a profile profile image for Account:BAN [" +attachment.getBAN() +"]?. Removing it will un-set the image as profile for the account."; 
        }
        return "Proceed with deletion of Attachment [" +attachment.getFileName() +  "] ?";
    }


    protected boolean isAllowed(Context ctx, AccountAttachment attachment)
    {
       return true;
    }
}
