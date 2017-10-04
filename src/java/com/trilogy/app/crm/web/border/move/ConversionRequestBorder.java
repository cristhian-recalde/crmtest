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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.web.border.move;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.web.service.MoveRequestServicer;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;


/**
 * Abstract class providing common functionality to move request borders.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class ConversionRequestBorder implements Border
{
    public static final String MSG_KEY_Conversion_BUTTON_NAME = "ConversionRequest.ButtonName";

    public void service(Context context, HttpServletRequest httpservletrequest,
            HttpServletResponse httpservletresponse, RequestServicer requestservicer) throws ServletException,
            IOException
    {
        Context subCtx = context.createSubContext();
        MessageMgr manager = new MessageMgr(context, this);

        String moveButtonText = manager.get(MSG_KEY_Conversion_BUTTON_NAME, "Convert");

        context.put(MoveRequestServicer.MSG_KEY_BUTTON_NAME_CTX_KEY,MSG_KEY_Conversion_BUTTON_NAME);

        requestservicer.service(subCtx,httpservletrequest,httpservletresponse);
    
    }
}
