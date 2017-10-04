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
import com.trilogy.app.crm.bean.Note;
import com.trilogy.app.crm.transfer.GroupPrivacyEnum;
import com.trilogy.app.crm.transfer.TransferContract;
import com.trilogy.app.crm.transfer.TransferContractXInfo;
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
 * A Border for the AccountTransferContract screen which pre-sets the owner Id
 * to that of the Account BAN which was searched for.
 * 
 */
public class AccountTransferContractNewBorder
    implements Border
{
   
   /** Key into Session Context for remembering previous account. **/
   public final Object ACCT_BAN = "AccountTransferContractNewBorder.BAN";
   public final Object ACCT_SPID = "AccountTransferContractNewBorder.SPID";
   
   
   public AccountTransferContractNewBorder()
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
         WebAgents.getSession(ctx).put(ACCT_SPID, acct.getSpid());
      }
      
      if ( ctx.has(ACCT_BAN) )
      {
         XBeans.putBeanFactory(ctx, TransferContract.class, new ContextFactory()
         {
            public Object create(Context ctx)
            {
               TransferContract contract = new TransferContract();
               contract.setPrivacy(GroupPrivacyEnum.PRIVATE);
               contract.setOwnerID((String) ctx.get(ACCT_BAN));   
               contract.setSpid(ctx.getInt(ACCT_SPID));               
               
               return contract;
            }
         });
         
         AbstractWebControl.setMode(ctx, TransferContractXInfo.PRIVACY, ViewModeEnum.READ_ONLY);
         AbstractWebControl.setMode(ctx, TransferContractXInfo.OWNER_ID, ViewModeEnum.READ_ONLY);
      }

      delegate.service(ctx, req, res);
   }
   
}

