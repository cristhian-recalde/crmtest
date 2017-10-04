/*
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
package com.trilogy.app.crm.web.action;

import java.io.PrintWriter;
import java.security.Permission;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.util.Link;

import com.trilogy.app.crm.transfer.TransferException;
import com.trilogy.app.crm.transfer.TransferFailureStateEnum;
import com.trilogy.app.crm.web.agent.ReprocessTransferExceptionWebAgent;

/**
 * Action that will be used to trigger the Transfer Exception
 * record processing.
 *
 */ 
public class ReprocessTransferExceptionAction extends SimpleWebAction 
{
    public ReprocessTransferExceptionAction()
       {
          super("reprocess", "Reprocess");
       }


       public ReprocessTransferExceptionAction(Permission permission)
       {
          this();
          setPermission(permission);
       }
       
       /**
        * Output the link on the Table View
        */
       @Override
    public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
       {
          link.add(ReprocessTransferExceptionWebAgent.ACTION, ReprocessTransferExceptionWebAgent.DETAILS);
          link.add(ReprocessTransferExceptionWebAgent.RECORD_ID, String.valueOf(((TransferException)bean).getId()));

          out.print("<a href=\"");
          link.write(out);
          out.print("\" >");
          if (((TransferException)bean).getState().equals(TransferFailureStateEnum.FAILED))
          {
              out.print(getLabel());
          }
          else
          {
              out.print("View");
          }
          out.print("</a>");
       }
       
       /**
        * Output the button link in the Detail View
        */
       @Override
    public void writeLinkDetail(Context ctx, PrintWriter out, Object bean, Link link)
       {
          link = modifyLink(ctx, bean, link);
          link.add(ReprocessTransferExceptionWebAgent.ACTION, ReprocessTransferExceptionWebAgent.REPROCESS);
          link.add(ReprocessTransferExceptionWebAgent.RECORD_ID, String.valueOf(((TransferException)bean).getId()));
          MessageMgr mmgr = new MessageMgr(ctx, this);
          ButtonRenderer br = (ButtonRenderer)ctx.get(ButtonRenderer.class, DefaultButtonRenderer.instance());

          link.writeLink(out, mmgr.get(WEB_ACTION + getKey() + DETAIL_LABEL, br.getButton(ctx, getKey(), mmgr.get(WEB_ACTION + getKey() + LABEL, getLabel()))));
       }

    protected static final String beanName_ = "Transfer Exception";
    private static final String WEB_ACTION = "WebAction.";
    private static final String DETAIL_LABEL = ".DetailLabel";
    private static final String LABEL = ".Label";
}
