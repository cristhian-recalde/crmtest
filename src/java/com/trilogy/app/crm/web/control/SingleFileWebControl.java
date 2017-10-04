package com.trilogy.app.crm.web.control;

/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
import java.io.File;
import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.LinkWebControl;
import com.trilogy.framework.xhome.webcontrol.PrimitiveWebControl;
import com.trilogy.framework.xhome.webcontrol.ReadOnlyWebControl;
import com.trilogy.framework.xhome.webcontrol.TextFieldWebControl;


/**
 * 
 * @author simar.singh@redknee.com This provides download link to a file represented by
 *         the web-object.toString()
 * 
 */
public class SingleFileWebControl extends PrimitiveWebControl
{

    /**
     * Method toWeb provides a link to download the file represented by the web-object
     * 
     * @param ctx
     *            Context
     * @param out
     *            out stream for the webcontrol
     * @param name
     *            Name of the webcontrol on the form
     * @param obj
     *            File to be downloaded
     */
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        obj = (obj == null) ? "" : obj;
        final String fileLocation = String.valueOf(obj);
        File file = new File(fileLocation);
        if (file.exists())
        {
            new LinkWebControl("FileStreamingServicer").toWeb(ctx, out, name, obj);
        }
        else
        {
            final String fileNotFoundMessage = "File [" + fileLocation + " ] not found.";
            final int messageLength = fileNotFoundMessage.length() + 1;
            new ReadOnlyWebControl(new TextFieldWebControl(messageLength, messageLength)).toWeb(ctx, out, name,
                    fileNotFoundMessage);
        }
    }


    @Override
    public Object fromWeb(Context ctx, ServletRequest req, String name) throws NullPointerException
    {
        // TODO Auto-generated method stub
        return name;
    }
}
