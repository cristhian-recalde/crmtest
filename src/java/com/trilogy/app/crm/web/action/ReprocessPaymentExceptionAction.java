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

import com.trilogy.app.crm.bean.payment.PaymentException;
import com.trilogy.app.crm.bean.payment.PaymentFailureTypeEnum;
import com.trilogy.app.crm.web.agent.ReprocessPaymentExceptionWebAgent;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.util.Link;

/**
 * Action that will be used to trigger the Payment Exception
 * record processing.
 *
 * @ author Angie Li
 *
 */ 
public class ReprocessPaymentExceptionAction extends SimpleWebAction 
{
    public ReprocessPaymentExceptionAction()
       {
          super("reprocess", "Reprocess");
       }


       public ReprocessPaymentExceptionAction(Permission permission)
       {
          this();
          setPermission(permission);
       }
       
       /**
        * Output the link on the Table View
        */
       public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
       {
          link.add(ReprocessPaymentExceptionWebAgent.ACTION, ReprocessPaymentExceptionWebAgent.DETAILS);
          link.add(ReprocessPaymentExceptionWebAgent.RECORD_ID, String.valueOf(((PaymentException)bean).getId()));

          out.print("<a href=\"");
          link.write(out);
          out.print("\" >");
          if (!((PaymentException)bean).getType().equals(PaymentFailureTypeEnum.PREPAID))
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
       public void writeLinkDetail(Context ctx, PrintWriter out, Object bean, Link link)
       {
          link = modifyLink(ctx, bean, link);
          link.add(ReprocessPaymentExceptionWebAgent.ACTION, ReprocessPaymentExceptionWebAgent.REPROCESS);
          link.add(ReprocessPaymentExceptionWebAgent.RECORD_ID, String.valueOf(((PaymentException)bean).getId()));
          MessageMgr mmgr = new MessageMgr(ctx, this);
          ButtonRenderer br = (ButtonRenderer)ctx.get(ButtonRenderer.class, DefaultButtonRenderer.instance());

          link.writeLink(out, mmgr.get(WEB_ACTION + getKey() + DETAIL_LABEL, br.getButton(ctx, getKey(), mmgr.get(WEB_ACTION + getKey() + LABEL, getLabel()))));
       }
       
       protected final String beanName_ = "Payment Exception";
       private final String WEB_ACTION = "WebAction.";
       private final String DETAIL_LABEL = ".DetailLabel";
       private final String LABEL = ".Label";
       
}
