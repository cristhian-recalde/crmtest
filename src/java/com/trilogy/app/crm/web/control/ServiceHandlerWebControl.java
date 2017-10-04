/*
 *  ServiceHandlerWebControl
 *
 *  Author : Kevin Greer
 *  Date   : Feb 3, 2004
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
 
package com.trilogy.app.crm.web.control;

import com.trilogy.app.crm.provision.ProvisioningSupport;
import com.trilogy.framework.xhome.context.*;
import com.trilogy.framework.xhome.webcontrol.*;
import java.io.PrintWriter;


/**
 *  A custom WebControl for the handler field in the Service Bean.
 *
 *  Decorates the regular TextFieldWebControl with colour coded:
 *     Provision / Unprovision
 *  labels to the right.  If an agent exists for the agentType then
 *  it is green, otherwise red.  This lets people know if they've entered
 *  an incorrect Service handler.
 *
 *  @author Kevin Greer
 **/
public class ServiceHandlerWebControl
    extends TextFieldWebControl
{
   
    public ServiceHandlerWebControl()
    {
       super(30);
    }

   
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
       super.toWeb(ctx, out, name, obj);
       
       String handler = (String) obj;
       
       outputLabel(ctx, out, handler, "Provision");
       out.print(" / ");
       outputLabel(ctx, out, handler, "Unprovision");
    }
    
    
    protected void outputLabel(Context ctx, PrintWriter out, String handler, String agentType)
    {
       out.print(" <font color=\"");
       out.print(ProvisioningSupport.createAgent(ctx, handler, agentType) == null ?
          "red" : 
          "green");
       out.print("\">");
       out.print(agentType);
       out.print("</font>");
    }
   
}


