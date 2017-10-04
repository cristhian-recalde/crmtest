package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.home.InvoicePredictionHome;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xlog.log.*;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Border for use on Invoice screen to decorate InvoiceHome to add phantom future invoice entries.
 *
 * @author Gary Anderson, Kevin Greer
 **/
public class InvoicePredictionBorder
   implements Border
{
   
   public InvoicePredictionBorder(Context ctx)
   {
   }


   public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate)
      throws ServletException, IOException
   {
      Context subCtx = ctx.createSubContext();
      subCtx.setName("InvoicePrediction");
      Context session = Session.getSession(ctx);
      Account account = (Account) session.get(Account.class);

      if ( account != null )
      {
         subCtx.put(
            InvoiceHome.class,
            new InvoicePredictionHome(
               ctx,
               account.getBAN(),
               (Home)subCtx.get(InvoiceHome.class)));
      }
      
      delegate.service(subCtx, req, res);
   }

}
