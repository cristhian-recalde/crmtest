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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.framework.xhome.msp.Spid;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.numbermgn.GenericPackage;
import com.trilogy.app.crm.support.PackageSupportHelper;
import com.trilogy.app.crm.technology.TechnologyAware;
import com.trilogy.app.crm.technology.TechnologyEnum;


/**
 * append a link to the item in PackageHome
 */
public class PackageLinkWebControl
   extends PreviewTextFieldWebControl
{
   public PackageLinkWebControl()
   {
   }

   public PackageLinkWebControl(int size)
   {
      super(size);
   }

   @Override
public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
   {
      int mode = ctx.getInt("MODE", DISPLAY_MODE);

      super.toWeb(ctx, out, name, obj);
      
      if (mode == EDIT_MODE || mode == DISPLAY_MODE)
      {
          // append link
          String packageId = (String)obj;
          
          Object bean = ctx.get(AbstractWebControl.BEAN);
          TechnologyAware technologyAware = null;
          if (bean instanceof TechnologyAware)
          {
              technologyAware = (TechnologyAware) bean;
          }
          if (technologyAware != null)
          {
              final TechnologyEnum technology = technologyAware.getTechnology();
              
              Spid spid = MSP.getBeanSpid(ctx);
              
              final GenericPackage card = getPackage(ctx, technology, packageId, spid.getId());
                
              Link link = null;

              if (card != null)
              {
                  // override the domain so that the end of the link is display
                  // at the root domain
                  Context noDomainCtx = ctx.createSubContext();
                  WebAgents.setDomain(noDomainCtx, "");
                  link = new Link(noDomainCtx);
                  // remove possible embellishment caused by WebControllerWebcontrol
                  link.remove("cmd");
                  link.remove("query");
                  link.remove("key");

                  if (technology == TechnologyEnum.GSM)
                  {
                      link.add("cmd", "appCRMPackageMenu");
                      link.add("key", packageId);
                  }
                  else if (technology == TechnologyEnum.TDMA || technology == TechnologyEnum.CDMA)
                  {
                      link.add("cmd", "AppCrmTDMACDMAPackage");
                      link.add("key", packageId+"`"+card.getSpid());
                  }
                  else
                  {
                      new MinorLogMsg(
                          this,
                          "Could not create link for unexpected technology " + technology,
                          null).log(ctx);

                      return;
                  }
                  link.add("mode", "display");
                  link.writeLink(out, "detail");
              }
          }
      }
   }

    /**
     * Gets the package for the given technology and identifier.
     *
     * @param context The operating context.
     * @param technology The technology type of the card.
     * @param identifier The identifier of the card.
     *
     * @return The package for the given technology and identifier if found;
     * null otherwise.
     */
    private GenericPackage getPackage(
        final Context context,
        final TechnologyEnum technology,
        final String identifier, int spid)
    {
        GenericPackage card = null;

		if (technology.isPackageAware())
		{
			try
			{
				card =
				    PackageSupportHelper.get(context).getPackage(context,
				        technology, identifier, spid);
			}
			catch (final HomeException exception)
			{
				new MinorLogMsg(this, "Failed to locate package " + identifier,
				    exception).log(context);
			}
        }
        return card;
    }
    
}
