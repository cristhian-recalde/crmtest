package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.popup.PopupWithLayoutImageLink;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.PrimitiveWebControl;

import com.trilogy.app.crm.bean.VoicemailFieldsBean;

/*
 * @author pkulkarni
 */
public class VMResetPwdWebControl extends PrimitiveWebControl
{
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        VoicemailFieldsBean vmBean = (VoicemailFieldsBean) ctx
        .get(AbstractWebControl.BEAN);
        
        Link link = new PopupWithLayoutImageLink(ctx, "Reset Password");
        link.addRaw("pwd", vmBean.getPassword());
        link.remove("key");
        link.addRaw("parentDomain", WebAgents.getDomain(ctx));
        link.addRaw("cmd", "resetVMPassword");
        link.addRaw("CMD", "Reset");
        link.writeLink(out, " Reset Password ");
    }

    public Object fromWeb(Context ctx, ServletRequest req, String name) throws NullPointerException
    {
        return null;
    }
    
}
