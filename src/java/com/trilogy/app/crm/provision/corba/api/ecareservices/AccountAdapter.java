/*
 * Created on April 11, 2006
 * 
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with REDKNEE.com.
 * 
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFCDRED
 * BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR
 * ITS DERIVATIVES.
 */

package com.trilogy.app.crm.provision.corba.api.ecareservices;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.provision.corba.api.ecareservices.acctmgmt.AccountParamID;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.provision.corba.api.ecareservices.acctmgmt.AccountParameter;
import com.trilogy.app.crm.provision.corba.api.ecareservices.error.ServiceError;
import com.trilogy.app.crm.provision.corba.api.ecareservices.exception.AccountNotFoundException;

/**
 * To adapt the account and subscriber to Array of Account Parameters.
 * 
 * @author kso
 *
 */
public class AccountAdapter extends ParamAdapter
{
    /**
     * @param ctx
     * @param acct  Account
     * @param sub   Subscriber
     * @param reqSet Request Parameters
     * @return Array of Account Parameters
     * @throws ServiceError
     */
    public AccountParameter[] adapt(Context ctx, Account acct, Subscriber sub, AccountParamID[] reqSet) throws AccountNotFoundException
    {
        List result = new ArrayList();
        for ( int i = 0; i < reqSet.length && acct != null; i++ )
        {
            if ( AccountParamID.ACCOUNT_ID.equals(reqSet[i]))
            {
                result.add(new AccountParameter(AccountParamID.ACCOUNT_ID,adaptParamValue(acct.getBAN())));  
            }
            else if ( AccountParamID.RESP_ACCOUNT_ID.equals(reqSet[i]))
            {
                String respBAN = acct.getBAN();
                // account is not responsible, it is required to search from its parents.
                if ( !acct.getResponsible() )
                {
                    Account respAcct;
                    try
                    {
                        respAcct = acct.getResponsibleParentAccount(ctx);
                        if ( respAcct != null )
                        {
                            respBAN = respAcct.getBAN();
                        }
                    } 
                    catch (HomeException e)
                    {
                        String msg = "Got an exception "+ e.getMessage()+" while finding the responsible account by MSISDN "+acct.getBAN();
                        throw new AccountNotFoundException(msg);
                    }
                                     
                }
                
                result.add( new AccountParameter(AccountParamID.RESP_ACCOUNT_ID,adaptParamValue(respBAN)));

            }
            else if ( AccountParamID.SUBSCRIBER_ID.equals(reqSet[i]))
            {
                result.add(new AccountParameter(AccountParamID.SUBSCRIBER_ID,adaptParamValue(sub.getBAN())));
            }
        }
        
        if ( LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx,this, result.toString());
        }
        return toArray(result);        
    }

    /**
     * To convert the arraylist to array of AccountParameter
     * @param result
     * @return
     */
    public AccountParameter[] toArray(List result)
    {
        AccountParameter[] list = new AccountParameter[result.size()];
        
        for (int i = 0; i < result.size(); i ++ )
        {
            list[i] = (AccountParameter) result.get(i);
        }
        return list;
    }
   
    
}
