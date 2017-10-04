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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.transfer.TransferContractHome;
import com.trilogy.app.crm.transfer.TransferContractXInfo;
import com.trilogy.app.crm.transfer.contract.tfa.TransferContractSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;

/**
 * @author ling.tang@redknee.com
 *
 */
public class AccountTransferContractSearchBorder implements Border
{

    public AccountTransferContractSearchBorder(Context ctx)
    {
        
    }
    
    /**
     * {@inheritDoc}
     */
    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate)
            throws ServletException, IOException
    {
            Context subCtx  = ctx.createSubContext();
            Context session = Session.getSession(ctx);
            Account account = (Account) session.get(Account.class);

            if ( account != null )
            {
                final Set<String> allAccountIds = new HashSet<String>();
                allAccountIds.add(account.getBAN());
                // for public agreements, uncomment following line...
                // allAccountIds.add("");
                subCtx.put(TransferContractSupport.TRANSFER_CONTRACT_OWNER_ID, allAccountIds);
            }
            
            delegate.service(subCtx, req, res);
    }

}
