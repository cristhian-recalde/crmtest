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
package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.transfer.GroupPrivacyEnum;
import com.trilogy.app.crm.transfer.TransferAgreement;
import com.trilogy.app.crm.transfer.TransferAgreementXInfo;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;


/**
 * A Border for the AccountTransferAgreement screen which pre-sets the owner Id
 * to that of the Account BAN which was searched for.
 * 
 */
public class AccountTransferAgreementNewBorder
    implements Border
{
   
   /** Key into Session Context for remembering previous account. **/
   public final Object ACCT_BAN = "AccountTransferAgreementNewBorder.BAN";
   
   
   public AccountTransferAgreementNewBorder()
   {
   }
   
   
   public void service(
            Context             ctx,
      final HttpServletRequest  req,
            HttpServletResponse res,
            RequestServicer     delegate)
      throws ServletException, IOException
   {
      ctx = ctx.createSubContext();

      Context session = Session.getSession(ctx);
      
      Account acct = (Account) session.get(Account.class);      
      
      if ( acct != null )
      {
         WebAgents.getSession(ctx).put(ACCT_BAN, acct.getBAN());
      }
      
      if ( ctx.has(ACCT_BAN) )
      {
         XBeans.putBeanFactory(ctx, TransferAgreement.class, new ContextFactory()
         {
            public Object create(Context ctx)
            {
               TransferAgreement agreement = new TransferAgreement();
               agreement.setPrivacy(GroupPrivacyEnum.PRIVATE);
               agreement.setOwnerID((String) ctx.get(ACCT_BAN));               
               
               return agreement;
            }
         });
         
         AbstractWebControl.setMode(ctx, TransferAgreementXInfo.PRIVACY, ViewModeEnum.READ_ONLY);
         AbstractWebControl.setMode(ctx, TransferAgreementXInfo.OWNER_ID, ViewModeEnum.READ_ONLY);
      }

      delegate.service(ctx, req, res);
   }
   
}

