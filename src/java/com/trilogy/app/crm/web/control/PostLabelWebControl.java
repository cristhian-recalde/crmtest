/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.TextFieldWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xhome.language.MessageMgr;


/**
 * Provides a WebControl that does the same work as the "postLabel" tag in the
 * model.  This is useful should the post-label need to be inside another web
 * control.
 
 * @author gary.anderson@redknee.com
 */
public
class PostLabelWebControl
    extends ProxyWebControl
{
    /**
     * Creates a new PostLabelWebControl for the given delegate.
     *
     * @param delegate The WebControl to which we delegate.
     * @param key The key used to look the post-label up in the MessageMgr.
     * @param text The default text to use for the post-label should it not be
     * in the MessageMgr.
     */
    public PostLabelWebControl(final WebControl delegate, final String key, final String text)
    {
        super(delegate);
        key_ = key;
        text_ = text;
    }
    

    // INHERIT
    public void toWeb(
        final Context ctx,
        final PrintWriter out,
        final String name,
        final Object obj)
    {
        super.toWeb(ctx, out, name, obj);

        final MessageMgr messages = new MessageMgr(ctx, this);
        final String postLabel = messages.get(key_, text_);
        
        out.print("&nbsp;<b><font color=\"#003366\">");
        out.print(postLabel);
        out.print("</font></b>");
    }


    /**
     * The key used to look the post-label up in the MessageMgr.
     */
    private final String key_;

    /**
     * The default text to use for the post-label should it not be in the
     * MessageMgr.
     */
    private final String text_;
    
    
} // class
