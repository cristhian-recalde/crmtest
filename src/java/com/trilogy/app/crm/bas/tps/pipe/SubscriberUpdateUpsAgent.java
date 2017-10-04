/*
 *  UpdateUpsAgent.java
 *
 *  Author : Larry Xia
 *  Date   : Oct 24, 2003
 *
 *  Copyright (c) Redknee, 2003
 *  - all rights reserved
 */
package com.trilogy.app.crm.bas.tps.pipe;

import java.util.Date;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.product.s2100.ErrorCode;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.TPSConfig;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.app.crm.home.UpsFailException;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;

/**
 * Update Subscriber on UPS
 *
 * @author Larry Xia
  *
 */

public class SubscriberUpdateUpsAgent
   extends PipelineAgent
{

   public SubscriberUpdateUpsAgent(ContextAgent delegate)
   {
      super(delegate);
   }

   /**
	*
	* @param ctx
	*           A context
	* @exception AgentException
	*               thrown if one of the services fails to initialize
	*/

   @Override
public void execute(Context ctx)
      throws AgentException
   {
		if (LogSupport.isDebugEnabled(ctx))
		{
			new DebugLogMsg(this, "Ups updating", null).log(ctx);
		}
		Transaction trans = (Transaction) ctx.get(Transaction.class);
		TPSConfig config = (TPSConfig) ctx.get(TPSConfig.class); 
		boolean isPaymentReversal = false;
        try
        {
            isPaymentReversal = CoreTransactionSupportHelper.get(ctx).isPaymentReversal(ctx, trans);
        }
        catch (HomeException e1)
        {
            LogSupport.minor(ctx, this, "Exception occured while resolving adjustment type " +
                    "category for transaction with receipt number"+trans.getReceiptNum());
        }
        
		if ( trans.getAmount() != 0  && config.getOCG()){
			creditOCG(ctx);

		} else {
			resetUPS(ctx); 
		}	

    }
    
   private void creditOCG(Context ctx)
   throws AgentException
   {
		int ret = TPSPipeConstant.RESULT_CODE_SUCCESS; 
		int ocgRet = ErrorCode.NO_ERROR; 
		Subscriber subs = ( Subscriber) ctx.get(Subscriber.class); 
		Transaction trans = (Transaction) ctx.get(Transaction.class);
		final long amount = (trans.getAmount() - trans.getTaxPaid())* -1;
	   
	   try 
		{
		   // MAALP : RFF Group Pooled Account (added groupMsisdn to the call)
		   final Account account =
			   SubscriberSupport.lookupAccount(ctx, subs);

			if (account.isPooled(ctx))
            {
                // Replace subscriber with group leader
                subs = SubscriberSupport.lookupSubscriberForMSISDN(ctx, AccountSupport.getAccount(ctx, trans.getBAN()).getPoolMSISDN(), trans
                        .getSubscriptionTypeId(), new Date());
            }
		   
		   if (needsUpsUpdate(subs)) {	
			   ret = TPSPipeConstant.RESULT_CODE_SUCCESS;
			        
			   ocgRet = CoreTransactionSupportHelper.get(ctx).forwardTransactionToOcg(ctx, trans, subs.getMSISDN(),subs.getSubscriberType(), account.getCurrency(), false, amount, this); 
			
			   if (LogSupport.isDebugEnabled(ctx))
			   {
				   new DebugLogMsg(this, "Payment credit to OCG " + amount + ": return code" + ocgRet, null).log(ctx);
			   }
			
			   if (ocgRet == ErrorCode.NO_ERROR )
            {
                ret = TPSPipeConstant.RESULT_CODE_SUCCESS;
            }
            else
            {
                ret = TPSPipeConstant.RESULT_CODE_UPS_FAILS;
            }
		   }
		}	catch ( Exception e )
		{
			ocgRet = ErrorCode.UNKNOWN_ERROR; 
			ret = TPSPipeConstant.RESULT_CODE_UPS_FAILS;
			ctx.put(Exception.class, e); 
				new EntryLogMsg(10535, this, "","", null, e).log(ctx);
		} finally 
		{

			ERLogger.createSubAdjustmentEr(
					ctx,
					subs.getSpid(),
					trans.getAcctNum(),
					subs.getId(),
					trans.getAmount(),
					trans.getCSRInput(),
					String.valueOf(trans.getAdjustmentType()),
					trans.getGLCode(),
					ocgRet, 
					trans.getAgent(),  // Larry: added for TT 403303537. 
					(trans.getLocationCode().length() > 0) ? trans.getLocationCode() : "none"
					);
			
			if ( ret == TPSPipeConstant.RESULT_CODE_SUCCESS ) {
				pass(ctx, this, "update UPS successfully");
			} else {
				if(ocgRet != ErrorCode.UNKNOWN_ERROR)
				{
					ctx.put(UpsFailException.class, new UpsFailException("OCG credit failed",ocgRet));
				}
				fail(ctx, this, "Unable update UPS:" + ret, null, TPSPipeConstant.RESULT_CODE_UPS_FAILS, ocgRet); 
			}

		}	   
	   
   }

   private void resetUPS(final Context ctx) 
   throws AgentException
   {
	   
		Transaction trans = (Transaction)ctx.get(Transaction.class);

		try 
		{
			// query again. again, again. luckly, it is total cached. 
			AdjustmentType type = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, trans.getAdjustmentType()); 

			if ( !type.isChargeToOcg())
			{
				new DebugLogMsg(this, "skip charge to ABM by config" + type.getCode(), null).log(ctx);
				pass(ctx, this, "bypass UPS updating successfully");
				return; 
			}
		} catch (HomeException e )
		{
			// unlike to happen. 
			throw new AgentException("fail to get adjustment Type " + trans.getAdjustmentType()); 
		}

		Subscriber subs = ( Subscriber) ctx.get(Subscriber.class);
   		int ret = TPSPipeConstant.RESULT_CODE_SUCCESS;
		int upsRet = ErrorCode.NO_ERROR;
		boolean needsUpsUpdate = true;
		       
		Long balance = (Long) ctx.get(TPSPipeConstant.TPS_PIPE_SUBSCRIBER_BALANCE);
		Long accBalance = (Long) ctx.get(TPSPipeConstant.TPS_PIPE_ACCOUNT_BALANCE);

	   	try
		{
			// MAALP : RFF Group Pooled Account (added groupMsisdn to the call)
	        final Account account =
	            SubscriberSupport.lookupAccount(ctx, subs);

	        if (account.isPooled(ctx))
            {
                balance = accBalance;
                // Replace subscriber with group leader
                subs = SubscriberSupport.lookupSubscriberForMSISDN(ctx, AccountSupport.getAccount(ctx, trans.getBAN()).getPoolMSISDN(), trans
                        .getSubscriptionTypeId(), new Date());
            }
	        
	   		needsUpsUpdate = needsUpsUpdate(subs);
	   		if (needsUpsUpdate)
	   		{
	 				ret = TPSPipeConstant.RESULT_CODE_SUCCESS;
	
					// negate the balance before applying to ups
					int bal = balance.intValue() * -1;
	
					final SubscriberProfileProvisionClient client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
					try
                    {
                        client.updateBalance(ctx, subs, bal);
                        
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                        	new DebugLogMsg(this, "BM updated with the balance " + balance, null).log(ctx);
                        }

                        ret = TPSPipeConstant.RESULT_CODE_SUCCESS;
                    }
                    catch (final SubscriberProfileProvisionException exception)
                    {
                        upsRet = exception.getErrorCode();
                        ret = TPSPipeConstant.RESULT_CODE_UPS_FAILS;
                        
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            new DebugLogMsg(this, "BM updated with the balance " + balance + ": return code" + upsRet, null).log(ctx);
                        }
                    }
	   		}
		}	catch ( Exception e )
		{
				upsRet = ErrorCode.UNKNOWN_ERROR;
				ret = TPSPipeConstant.RESULT_CODE_UPS_FAILS;
				ctx.put(Exception.class, e);
 				new EntryLogMsg(10535, this, "","", null, e).log(ctx);
		} finally
		{
			
			if (needsUpsUpdate)
			{//for deactivated sub, we do not reset balance on ups
				ERLogger.genBalanceResetER(ctx, subs, balance.intValue(), upsRet, ret, trans.getAmount());
			}
			
			if ( ret == TPSPipeConstant.RESULT_CODE_SUCCESS ) {
 				pass(ctx, this, "update UPS successfully, bypassing sub=" + needsUpsUpdate);
			} else {
				//
				if(upsRet != ErrorCode.UNKNOWN_ERROR)
				{
					ctx.put(UpsFailException.class, new UpsFailException("UPS Call failed",upsRet));
				}
 				fail(ctx, this, "Unable update UPS:" + ret, null, TPSPipeConstant.RESULT_CODE_UPS_FAILS, upsRet);
 			}
 		}

    }

protected boolean needsUpsUpdate(Subscriber sub) 
{
	boolean needsUpsUpdate = true;
	/*
	    * Mike confirmed that we are supposed to block all the transactions going to 
	    * ABM if the subscriber is in deactivated state.
	    * 
	    */       
	if(sub.getState() == SubscriberStateEnum.INACTIVE)
	    {
	    	needsUpsUpdate = false;
	    }
	return needsUpsUpdate;
}

 }


