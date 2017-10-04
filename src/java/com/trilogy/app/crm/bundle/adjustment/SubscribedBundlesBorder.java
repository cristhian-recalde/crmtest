package com.trilogy.app.crm.bundle.adjustment;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bundle.BundleProfileHome;
import com.trilogy.app.crm.bundle.BundleProfileTransientHome;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.support.BundleSupportHelper;


/**
 * Filter the BundleProfileApiHome to only bundles that a subscriber
 * subscribed to
 *
 * @author Candy Wong, Kevin Greer
 */
public class SubscribedBundlesBorder
   implements Border
{

   public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate)
      throws ServletException, IOException
   {
      Context    subCtx        = ctx.createSubContext();
      Subscriber sub           = (Subscriber)subCtx.get(Subscriber.class);
      Set        bundleIds     = new HashSet();
      Home       transientHome = new BundleProfileTransientHome(ctx);

      if ( sub != null )
      {
            Map bundles = SubscriberBundleSupport.getSubscribedBundlesWithPointsBundles(ctx, sub);

            for ( Iterator iter = bundles.values().iterator() ; iter.hasNext() ; )
            {
               // bundleIds.add(Long.valueOf(((SubscriberBundle)iter.next()).getId()));
               try
               {
                   BundleFee fee = (BundleFee)iter.next();
                   transientHome.create(BundleSupportHelper.get(ctx).getBundleProfile(ctx, fee.getId()));
               }
               catch (Throwable t)
               {
               }
            }
      }

      // filter the BundleProfileApiHome to bundles that a subscriber subscribed to
      // bundleHome = bundleHome.where(subCtx, new In(BundleProfileApiXInfo.BUNDLE_ID, bundleIds));


      // save the home to the subCtx
      subCtx.put(BundleProfileHome.class, transientHome);

      try
      {
         if ( transientHome.selectAll().size() == 0 )
         {
            WebAgents.getWriter(ctx).println("The current subscriber doesn't have any bundles to adjust.</br>");

            return;
         }
      }
      catch (HomeException e)
      {
         // Can't happen on TransientHome
      }

      delegate.service(subCtx, req, res);
   }

}
