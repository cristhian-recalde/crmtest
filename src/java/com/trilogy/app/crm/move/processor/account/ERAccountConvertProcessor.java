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

import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.ConvertAccountGroupTypeRequest;
import com.trilogy.app.crm.move.support.AccountMoveValidationSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;


/**
 * This processor is responsible for pegging any relevent ER's for account convert
 * business logic.
 * 
 * It only performs validation required to perform its duty.  No business use case
 * validation is performed here.
 *
 * @author suyash.gaidhani@redknee.com
 * @since 9.5.1
 */
public class ERAccountConvertProcessor<CAGTR extends ConvertAccountGroupTypeRequest> extends MoveProcessorProxy<CAGTR>
{
    
    public static final String SUCCESS_ONLINE_STATUS_MESSAGE = "Account Convertion Online Process Successful. ";
    public static final String FAILURE_ONLINE_STATUS_MESSAGE = "Account Convertion Online Process Failed. ";
    public static final String SUCCESS_OFFLINE_STATUS_MESSAGE = "Account Convertion Offline Process Successful. ";
    public static final String FAILURE_OFFLINE_STATUS_MESSAGE = "Account Convertion Offline Process Failed. ";
    
    public static final String ONLINE = "Online";
    public static final String OFFLINE = "Offline";
    
    public static final String PASSED = "0";
    public static final String FAILED = "1";
    
    
    public ERAccountConvertProcessor(MoveProcessor<CAGTR> delegate)
    {
        super(delegate);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        
        CAGTR request = this.getRequest();

        AccountMoveValidationSupport.validateOldAccountExists(ctx, request, cise);
        AccountMoveValidationSupport.validateNewAccountExists(ctx, request, cise);
        
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx);
    
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void move(Context ctx) throws MoveException
    {
        CAGTR request = this.getRequest();
        try
        {
            request.clearStatusMessage(ctx);
            super.move(ctx);
            try
            {
                if(request.getRetainOriginalAccount())
                {
                    List<String> lstStatusMsg = request.getStatusMessages(ctx);
                    String statusMessage = ""; 
                    if(lstStatusMsg.size() > 0)
                    {
                        for (String string : lstStatusMsg)
                        {
                            statusMessage = statusMessage + string;
                        }
                    }
                    
                    if(!request.getMigrateOnly())
                    {
                        logAccountConvertEventER(
                                ctx, 
                                request, ONLINE, PASSED, SUCCESS_ONLINE_STATUS_MESSAGE+ statusMessage);
                    }
                    else
                    {
                        
                        if(request.getWarnings(ctx).size() > 0)
                        {
                        	logAccountConvertEventER(
                                    ctx, 
                                    request,OFFLINE, FAILED, FAILURE_OFFLINE_STATUS_MESSAGE +  statusMessage);
                        }
                        else
                        {
                            
                            logAccountConvertEventER(
                                    ctx, 
                                    request,OFFLINE, PASSED, SUCCESS_OFFLINE_STATUS_MESSAGE +  statusMessage);
                        }
                    }
                }
            }
            catch (HomeException e)
            {
                throw new MoveException(request, "Error occurred creating 'Account conversion event' ER 1162.", e);
            }
            
        }
        catch (MoveException e) 
        {
            try
            {
                if(request.getRetainOriginalAccount())
                {
                    List<String> lstStatusMsg = request.getStatusMessages(ctx);
                    String statusMessage = ""; 
                    if(lstStatusMsg.size() > 0)
                    {
                        for (String string : lstStatusMsg)
                        {
                            statusMessage = statusMessage + string;
                        }
                    }
                    
                    if(!request.getMigrateOnly())
                    {
                        logAccountConvertEventER(
                                ctx, 
                                request, ONLINE , FAILED, FAILURE_ONLINE_STATUS_MESSAGE +statusMessage);
                    }
                    else
                    {

                    	logAccountConvertEventER(
                    			ctx, 
                    			request,OFFLINE, FAILED, FAILURE_OFFLINE_STATUS_MESSAGE +statusMessage);
                        
                      }
                    }
            }
            catch (HomeException e1)
            {
                throw new MoveException(request, "Error occurred creating 'Account conversion event' ER 1162.", e1);
            }
            throw e;
        }
}

    /**
     * Logs the "Subscriber Move Event" Event Record.
     *
     * @param context
     *            The operating context.
     * @param request 
     *           The request of account conversion
     * @param statusMessage
     *          Message to add in the ER          
     */
    private void logAccountConvertEventER(
            final Context ctx, 
            final CAGTR request, String processId, String statusCode, String statusMsg) throws HomeException
    {
        final Account oldAccount = request.getOldAccount(ctx);
        final Account newAccount = request.getNewAccount(ctx);
        
        ERLogger.logIndividualToGroupAccountConvertER(ctx, oldAccount, newAccount, processId, statusCode, statusMsg, this.getClass());
    }
}
