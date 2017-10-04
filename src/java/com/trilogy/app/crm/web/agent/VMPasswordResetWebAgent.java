package com.trilogy.app.crm.web.agent;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.voicemail.VoiceMailServer;
import com.trilogy.app.crm.voicemail.VoiceMailService;
import com.trilogy.app.crm.voicemail.client.ExternalProvisionResult;
import com.trilogy.app.crm.web.service.VoiceMailPINResetServicer;
import com.trilogy.framework.xhome.beans.DefaultExceptionListener;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.agent.ServletBridge;
import com.trilogy.framework.xhome.web.agent.WebAgent;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/*
 * @author pkulkarni, jhughes
 * 
 */
public class VMPasswordResetWebAgent extends ServletBridge implements WebAgent
{
    // just like a ContextAgent, the execute method must be implemented
    public void execute(Context ctx) throws AgentException
    {
        // retrieve the output stream using convenience method from ServletBridge
        PrintWriter out = getWriter(ctx);
        String password = getParameter(ctx, "pwd");
        String message = "password is null";
        if (password != null)
        {
			MessageMgr mmgr = new MessageMgr(ctx, this);
            Subscriber sub = (Subscriber) ctx.get(Subscriber.class);

            message = resetPIN(ctx, this, mmgr, sub, password);
        }

        out.println("<br/><h3>");
        out.println(message);
        out.println("</h3><br/>");
    }
    
    
    public static String resetPINThroughVoiceMailServer(Context ctx, Object caller, MessageMgr mmgr, Subscriber sub, String password)
    {
    	VoiceMailService vmServer = (VoiceMailService) ctx.get(VoiceMailServer.class);
    	String message = null;
    	if (vmServer != null)
        {
    		ExternalProvisionResult ret = vmServer.resetPassword(ctx, sub, password);
            message = mmgr.get("voicemail.reset.pass", "Voice mail password is reset, the result code is "
                    + ret.getCrmVMResultCode(), new Object[]{SubscriberXInfo.ID.get(sub), password});
        }
    	else
        {
            message = mmgr.get("voicemail.reset.fail", "System error: Voicemail service not found in context",
                    new Object[]{SubscriberXInfo.ID.get(sub), password});
        }
    	return message;
    }
    

    public static String resetPIN(Context ctx, Object caller, MessageMgr mmgr, Subscriber sub, String password)
    {
        
        String message;
        
            try
            {
                DefaultExceptionListener el = new DefaultExceptionListener();
                VoiceMailPINResetServicer.resetVoicemailPIN(ctx, sub.getId(), el);
                message = mmgr.get("voicemail.reset.pass", "Voice mail password is reset"
                        , new Object[]{SubscriberXInfo.ID.get(sub), password});
            }

            catch (Throwable e)
            {
                message = mmgr.get("voicemail.reset.fail", e.getMessage(),
                        new Object[]{SubscriberXInfo.ID.get(sub), password});

                new MajorLogMsg(caller, e.getMessage(), e).log(ctx);
            }
        

        return message;
    }

    private static final long serialVersionUID = 1L;
}
