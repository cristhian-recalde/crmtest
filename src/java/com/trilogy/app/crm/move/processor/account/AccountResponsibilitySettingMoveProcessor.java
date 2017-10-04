/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.move.processor.account;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * This processor is responsible for setting responsible flag for the new account.
 * This has been introduced while fixing TT#14020426013.
 * Please refer the ticket and code review link http://floyd:8080/cru/AppCrm-1330 for more details.
 * 
 * @author bdhavalshankh
 * @since 9.7.2
 */
public class AccountResponsibilitySettingMoveProcessor<AMR extends AccountMoveRequest> extends MoveProcessorProxy<AMR>
{
    
    public AccountResponsibilitySettingMoveProcessor(MoveProcessor<AMR> delegate)
    {
        super(delegate);
    }

   
    @Override
    public void move(Context ctx) throws MoveException
    {
        AMR request = this.getRequest();
        try
        {
            Account oldAccount = request.getOriginalAccount(ctx);
            Account oldResponsibleParent = null;
            Account newParentAccount = null;
            
            /*
             * getResponsibleParentAccount throws NPE after framework upgrade. So here I'm handling it in separate try catch block.
             */
            try
            {
                oldResponsibleParent = oldAccount.getResponsibleParentAccount(ctx);
            }
            catch (NullPointerException e)
            {
                LogSupport.info(ctx, this, "Exception occurred while fetching Responsibe Parent" +
                		" Account for account with ban "+oldAccount.getBAN()+" . " +
        				"Old responsible account value remains NULL while setting responsible flag value for new account .");
            }
            
            Account newAccount = request.getNewAccount(ctx);
            if (newAccount != null)
            {
                newParentAccount = request.getNewParentAccount(ctx);
            
                if(oldResponsibleParent != null && newParentAccount == null && 
                        newAccount.isPrepaid()  && oldResponsibleParent.isPooled(ctx))
                {
                    newAccount.setResponsible(true);
                    if(LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "Setting responsible flag as TRUE for the new account being moved");
                    }
                }
                else if(newParentAccount != null && newParentAccount.isPooled(ctx))
                {
                    newAccount.setResponsible(false);
                    if(LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "Setting responsible flag as FALSE for the account being moved");
                    }
                }
                if(newParentAccount != null && !newParentAccount.isPooled(ctx) && newAccount.isPrepaid() )
                {
                	//for prepaid account in group or indivisual always responsible, 
                	//and responsible BAN will be set in account pipeline AccountSetResponsibleBANOnCreateHome.class 
                	//so it require to be null otherwise not set, here we found BAN was parent, thats wrong
                	newAccount.setResponsible(true); 
                	newAccount.setResponsibleBAN(null);
                	
                }
            }
        }
       catch (Exception e)
       {
           LogSupport.minor(ctx, this, "Error occurred while setting responsible flag for new account while account move.", e);
           throw new MoveException(request, "Error occurred while setting responsible flag for new account while account move.", e);
       }
        
       super.move(ctx);
    }
}
