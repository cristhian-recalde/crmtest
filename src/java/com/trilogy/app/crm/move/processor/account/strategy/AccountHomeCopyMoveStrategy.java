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
package com.trilogy.app.crm.move.processor.account.strategy;

import java.util.Collection;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.extension.MovableExtension;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.home.account.AccountHomePipelineFactory;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.ConvertAccountGroupTypeRequest;
import com.trilogy.app.crm.move.support.AccountMoveValidationSupport;
import com.trilogy.app.crm.move.support.MoveProcessorSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;


/**
 * This move strategy is responsible for creating the new account and updating
 * the old account via Home operations.  It does not modify the old or new accounts.
 * 
 * It only performs validation required to perform its duty.  No business use case
 * validation is performed here.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class AccountHomeCopyMoveStrategy<AMR extends AccountMoveRequest> implements CopyMoveStrategy<AMR>
{
	boolean storeSubscriberOnIndividualToGroupConversion_;

    /**
	 * 
	 */
	public AccountHomeCopyMoveStrategy() 
	{
	}

	/**
	 * @param storeSubscriberOnIndividualToGroupConversion
	 */
	public AccountHomeCopyMoveStrategy(
			boolean storeSubscriberOnIndividualToGroupConversion) 
	{
		super();
		this.storeSubscriberOnIndividualToGroupConversion_ = storeSubscriberOnIndividualToGroupConversion;
	}

	/**
     * @{inheritDoc}
     */
    public void initialize(Context ctx, AMR request)
    {
    }
    
    /**
     * @{inheritDoc}
     */
    public void validate(Context ctx, AMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        if (!ctx.has(MoveConstants.CUSTOM_ACCOUNT_HOME_CTX_KEY))
        {
            cise.thrown(new IllegalStateException(
                "Custom account home not installed in context."));
        }

        AccountMoveValidationSupport.validateOldAccountExists(ctx, request, cise);

        AccountMoveValidationSupport.validateNewAccountExists(ctx, request, cise);
        
        cise.throwAll();
    }


    /**
     * @{inheritDoc}
     */
    public void createNewEntity(Context ctx, AMR request) throws MoveException
    {
        Account newAccount = request.getNewAccount(ctx);
        Account oldAccount = request.getOldAccount(ctx);
        Context subCtx = ctx.createSubContext();
        subCtx.put(HTMLExceptionListener.class, null);
        boolean pooledConversion = false;
        
        try
        {
            Home accountHome = (Home) subCtx.get(MoveConstants.CUSTOM_ACCOUNT_HOME_CTX_KEY);
            if (ConvertAccountGroupTypeRequest.class.isAssignableFrom(request.getClass()))
            {
                ConvertAccountGroupTypeRequest convertRequest = (ConvertAccountGroupTypeRequest) request;
                if (convertRequest.getGroupType().equals(GroupTypeEnum.GROUP_POOLED))
                {
                    pooledConversion = true;
                }
            }
            
            if (pooledConversion)
            {
                accountHome =  AccountHomePipelineFactory.instance().decoratePoolConversionMoveHome(
                        (Home) ctx.get(Common.ACCOUNT_CACHED_HOME), ctx, ctx);
                subCtx.put(MovableExtension.MOVE_IN_PROGRESS_CTX_KEY, false);
            }
            
            newAccount.setOldBAN(oldAccount.getBAN());
            if (ConvertAccountGroupTypeRequest.class.isAssignableFrom(request.getClass()))
            {
                newAccount.setCreateAccountReason(MoveConstants.CONVERT_REASON);
            }
            else
            {
                newAccount.setCreateAccountReason("MOVE");
            }
            
            if(!oldAccount.getIdentificationGroupList().isEmpty())
            {
                newAccount.setIdentificationGroupList(oldAccount.getIdentificationGroupList());
            }
            
            if(!oldAccount.getSecurityQuestionsAndAnswers().isEmpty())
            {
                newAccount.setSecurityQuestionsAndAnswers(oldAccount.getSecurityQuestionsAndAnswers());
            }
            
            if(!oldAccount.getDiscountsClassHolder().isEmpty())
            {
                newAccount.setDiscountsClassHolder(oldAccount.getDiscountsClassHolder());
            }
            
            new DebugLogMsg(this, "Creating new account in account home (required to move account " + request.getExistingBAN() + ").  If successful, new BAN and new account values will be set in request object.", null).log(subCtx);
            newAccount = (Account) accountHome.create(subCtx, newAccount);

            if (newAccount != null)
            {
                request.setNewBAN(newAccount);
                if (pooledConversion)
                {
                    ((PoolExtension) ((ConvertAccountGroupTypeRequest) request).getExtensions().iterator().next())
                            .setBAN(newAccount.getBAN());
                }
                new InfoLogMsg(this, "New account (BAN=" + request.getNewBAN() + ") created in account home successfully.", null).log(subCtx);   
            }
        }
        catch (HomeException he)
        {
            throw new MoveException(request, "Error occurred while creating account (required to move account "
                    + request.getExistingBAN() + ")", he);
        }
        finally
        {
            MoveProcessorSupport.copyHTMLExceptionListenerExceptions(subCtx, ctx);
        }
    }

    /**
     * @{inheritDoc}
     */
    public void removeOldEntity(Context ctx, AMR request) throws MoveException
    {
        Account oldAccount = request.getOldAccount(ctx);
        Context subCtx = ctx.createSubContext();
        subCtx.put(HTMLExceptionListener.class, null);
        try
        {
            Home accountHome = (Home) ctx.get(MoveConstants.CUSTOM_ACCOUNT_HOME_CTX_KEY);

            new DebugLogMsg(this, "Updating old account (BAN=" + request.getExistingBAN() + ") in account home.", null).log(subCtx);
            subCtx.put(HTMLExceptionListener.class, null);
            oldAccount = (Account) accountHome.store(subCtx, oldAccount);
            
            request.setExistingBAN(oldAccount);
            if (oldAccount != null)
            {
                new InfoLogMsg(this, "Old account (BAN=" + request.getExistingBAN() + ") updated in account home successfully.", null).log(subCtx);
            }
            
            if(storeSubscriberOnIndividualToGroupConversion_)
            {
            	updateSubscriberForInBanCallingService(subCtx, oldAccount, request);    
            }
        }
        catch (HomeException he)
        {
            throw new MoveException(request, "Error occurred while updating account with BAN "
                    + request.getExistingBAN(), he);
        }
        finally
        {
            MoveProcessorSupport.copyHTMLExceptionListenerExceptions(subCtx, ctx);
        }
    }
    
  /**
     * TT#13042937082
     * On Individual to Group account conversion the mandatory in-ban calling service remains in 'failed to provision' state
     * Updating the subscriber after conversion to group so that service gets provisioned
     * @param ctx
     * @param oldAccount
     * @param request
     * @throws MoveException
     */
    private void updateSubscriberForInBanCallingService(Context ctx, Account oldAccount, AMR request) throws MoveException
    {
    	long inBanCallingServiceId = -1;
        Subscriber oldSubscriber = SubscriberSupport.getSubscriberIndividualAccount(ctx, oldAccount.getBAN());
   
        Collection<SubscriberServices> subServices = SubscriberServicesSupport.getProvisionedOrProvisionedWithErrorsSubscriberServices(ctx, oldSubscriber.getId());
		
		for(SubscriberServices subService : subServices)
		{
			if(subService.getService().getEnumType().equals(ServiceTypeEnum.CALLING_GROUP) 
					&& subService.getProvisionedState().equals(ServiceStateEnum.PROVISIONEDWITHERRORS))
			{
				inBanCallingServiceId = subService.getServiceId();
				break;
 			}
		}
		
		if(inBanCallingServiceId != -1)
		{
			Home subscriberHome = (Home) ctx.get(SubscriberHome.class);
			try 
			{
				subscriberHome.store(ctx, oldSubscriber);
				new InfoLogMsg(this, "Subscriber [ID=" + oldSubscriber.getId() + "] updated in subscriber home successfully for the In-BAN calling service "+inBanCallingServiceId+".", null).log(ctx);
			} 
			catch (HomeException e) 
			{
				throw new MoveException(request, "Updation of Subscriber ["+oldSubscriber.getId()+"] failed .", e.getCause());
			}
		}
    }
}
