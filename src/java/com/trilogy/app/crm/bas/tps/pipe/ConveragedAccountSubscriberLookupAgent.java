package com.trilogy.app.crm.bas.tps.pipe;

import java.util.Collection;
import java.util.Date;

import com.trilogy.app.crm.bas.tps.InvalidTPSRecordException;
import com.trilogy.app.crm.bas.tps.PrefixMapping;
import com.trilogy.app.crm.bas.tps.PrefixMappingHome;
import com.trilogy.app.crm.bas.tps.TPSRecord;
import com.trilogy.app.crm.bas.tps.TPSSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.TPSConfig;
import com.trilogy.app.crm.bean.TpsAccountIdentificationModeEnum;
import com.trilogy.app.crm.bean.payment.PaymentException;
import com.trilogy.app.crm.bean.payment.PaymentFailureTypeEnum;
import com.trilogy.app.crm.bean.payment.TotalOutstandingSubscriberVisitor;
import com.trilogy.app.crm.home.calldetail.SubscriberNotFoundHomeException;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.numbermgn.HistoryEventSupport;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistory;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryXInfo;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;
import com.trilogy.framework.core.cron.agent.CronContextAgentException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.snippet.log.Logger;

/**
 * 
 * @author lxia
 *
 */
public class ConveragedAccountSubscriberLookupAgent 
extends PipelineAgent
{
    
    /**
     * Constructor that get the delegate agent to run to.
     * @param delegate the next agent to run
     */
   public ConveragedAccountSubscriberLookupAgent(ContextAgent delegate)
   {
      super(delegate);
   }

   
   /** 
    * ap, tb,  tm, pay to
    * 1    1    1   a      account level payment higher priority, there is ban and msisdn in TPS
    * 1    1    0   a	   account level payment higher priority, there is ban not msisdn in tps
    * 1    0    1   s      account level payment higher priority, there is msisdn but no ban in tps
    * 1    0    0   x      account level payment higher priority, there is no ban nor msisdn in tps
    * 0    1    1   s      subscriber level payment higher priority, there is ban and msisdn in tps
    * 0    1    0   a      subscriber level payment higher priority, there is ban no msisdn in tps
    * 0    0    1   s      subscriber level payment higher priority, there is msisdn not ban in tps
    * 0    0    0   x      subscriber level payment higher priority, there is no ban nor msisdn in tps
    *          
    * @exception AgentException
    *               thrown if one of the services fails to initialize
    */

    public void execute(Context ctx)
       throws AgentException
    {
      	TPSConfig config = (TPSConfig) ctx.get(TPSConfig.class); 
      	TPSRecord tps = (TPSRecord) ctx.get(TPSRecord.class); 
       	
      	if ((tps.getTelephoneNum()== null || tps.getTelephoneNum().length() < 1) && 
      			(tps.getAccountNum() == null || tps.getAccountNum().length() < 1))
      	{
      		// case 0,0,0 and 1,0,0
      		tps.setResult( TPSPipeConstant.INVALID_TPS_ENTRY_NO_BAN_NO_MSISDN); 
      		handleResult(ctx, config, tps); 
      	}
      	
      	if (tps.getAccountNum() != null && tps.getAccountNum().length() > 0 &&      			
      			( config.isAccountHigh() || ( !config.isAccountHigh() && 
      	     			(tps.getTelephoneNum() == null || tps.getTelephoneNum().length()<1) ))	)	
      	{
      		 // case 1,1,0 and 1,1,1, 0,1,0
      			tps.setAccountLevel(true); 
      			getAccount(ctx, tps); 
      			handleResult(ctx, config, tps); 
      			
      			return;       			
       	} 

      	
     	// case 1,0,1 and 0, 1,1 and 0,0,1
		getSubscriber(ctx, tps); 
		handleResult(ctx, config, tps);

    } 

    
    public void  getAccount(final Context ctx, final TPSRecord tps)
    {

    	try 
    	{
    		TPSConfig config = null;
    		Account account = null;
    		account = AccountSupport.getAccount(ctx, tps.getAccountNum());
    		if (account != null)
    		{
    			
    			//for direct mode or who are the responsible accounts by own
    			tps.setAccount(account);
    			
    			config=(TPSConfig) ctx.get(TPSConfig.class);
    			if(config==null)
    			{
    				throw new CronContextAgentException("Cannot find TPS configuration bean in context.");
    			}

    			// Get the Identification mode from TPS config
    			TpsAccountIdentificationModeEnum mode = config.getTpsAccountIdentificationMode();

    			Logger.debug(ctx, ConveragedAccountSubscriberLookupAgent.class, "TPS processing mode is ["+mode.getDescription()+"]");

    			if (mode == TpsAccountIdentificationModeEnum.FINDRESPONSIBLE)
    			{
    				if(!account.isResponsible())
    				{
    					Logger.debug(ctx, ConveragedAccountSubscriberLookupAgent.class, "Searching for responsible parent account of ["+ tps.getAccountNum() +"]" );
    					Account responsibleAccount = account.getResponsibleParentAccount(ctx);
    					tps.setAccount(responsibleAccount);
    				}
    			}
    			else if (mode == TpsAccountIdentificationModeEnum.ONLYRESPONSIBLE )
    			{
    				if(!account.isResponsible())
    				{
    					tps.setResult(TPSPipeConstant.FAIL_TO_CREATE_TRANSACTION);
    					Logger.minor(ctx, ConveragedAccountSubscriberLookupAgent.class, " The account number [" + tps.getAccountNum() + "] is not associated to a responsible account.");
    					throw new InvalidTPSRecordException(" The account number [" + tps.getAccountNum() + "] is not associated to a responsible account.");
    				}
    			}
    			Logger.debug(ctx, ConveragedAccountSubscriberLookupAgent.class, "Returning account ["+ tps.getAccount() +"]" );
    		}
    		else 
    		{
    			Logger.minor(ctx, ConveragedAccountSubscriberLookupAgent.class, "Invalid Account Number ["+ tps.getAccountNum() + "].");
    			tps.setResult( TPSPipeConstant.FAIL_TO_FIND_ACCOUNT); 
    		}
    	} catch (Throwable e)
    	{
    		tps.setResult(TPSPipeConstant.FAIL_TO_FIND_ACCOUNT);
    		tps.setExceptionCaught(e); 
    	}  	 
    }
   
   
    public void  getSubscriber(final Context ctx, final TPSRecord tps)
    {
    	PrefixMapping mapping = null; 
    
    	if ( tps.getTpsInitial() != null && tps.getTpsInitial().length() > 0)
    	{	// only apply to IBIS, not HB. 
    		try 
    		{
    		
    			Home home = (Home) ctx.get(PrefixMappingHome.class);
    			mapping = (PrefixMapping) home.find(ctx, tps.getTpsInitial());
    			ctx.put(PrefixMapping.class, mapping); 
    			tps.setPrefixMapping(mapping); 
    			
    		} catch(Exception e)
    		{
    			tps.setResult(TPSPipeConstant.FAIL_TO_FIND_MSISDN_PREFIX); 
    			return; 
    		}
    	}
    	
    	try
    	{
    	    Subscriber subscriber = TPSSupport.getSubscriber(ctx, tps.getMsisdn(), tps.getAccountNum(), tps.getPaymentDate());
    	    tps.setSubscriber(subscriber);
    	}
    	catch (SubscriberNotFoundHomeException exception)
    	{
            tps.setResult(TPSPipeConstant.FAIL_TO_FIND_SUB);
    	}
    	catch (DuplicateMSISDNException exception)
    	{
            tps.setSubscriberList(exception.getSubscribersList()); 
            tps.setResult(TPSPipeConstant.MULTIPLE_SUBCRIBER_EXCEPTION); 
    	    tps.setExceptionCaught(exception);
    	}
    	catch (Throwable t)
    	{
            tps.setExceptionCaught(t); 
            tps.setResult( TPSPipeConstant.FAIL_TO_FIND_SUB); 
    	}
    }
    
    
    
    private void setSubscriber(Context ctx, TPSRecord tps, Subscriber sub)
    throws Throwable
    {
    	tps.setSubscriber(sub);
    	tps.setAccount(sub.getAccount(ctx));
    	tps.setResponsibleAcct(sub.getResponsibleParentAccount(ctx)); 
    	 
    }
    
    
    private void handleResult(Context ctx, TPSConfig config,  TPSRecord tps)
    {
     	String message = ""; 
    	
    	if (tps.getExceptionCaught() != null )
    	{
    		message = tps.getExceptionCaught().getMessage(); 
    	}
    	
    	try 
    	{
    		switch (tps.getResult())
    		{
    		case TPSPipeConstant.RESULT_CODE_SUCCESS:
    			try 
    			{
					ctx.put(Account.class, tps.getAccount());
    				if (tps.isAccountLevel())
    				{
    					pass(ctx,this, "accont found " + tps.getAccount().getBAN());
    				}else 
    				{
    					ctx.put(Subscriber.class, tps.getSubscriber());
     					pass(ctx, this, "Suscriber found:" + tps.getSubscriber().getId() );
    				}
    			} catch ( Throwable t)
    			{
    				// it is unlikely to happen, just in case. 
    				new MajorLogMsg(this, "exception thrown by tranaction pipeline after TPS account/subs searching. ", t).log(ctx); 
    			}
    			break; 
    		case  TPSPipeConstant.FAIL_TO_FIND_ACCOUNT: 
       			message = "Can not find Account:" + tps.getAccountNum(); 
    		    if (tps.getExceptionCaught() != null)
    		    {
    		    	message = message.concat(" due to : " + tps.getExceptionCaught().getMessage()); 
    		    }    			
   			
    			ERLogger.genInvalidTPSSubscriberER(ctx,TPSPipeConstant.FAIL_TO_FIND_ACCOUNT);
    			new EntryLogMsg(10532, this, "","", null, null).log(ctx);
    			TPSSupport.createOrUpdatePaymentExceptionRecord(ctx, tps,PaymentFailureTypeEnum.ACCOUNT , tps.getTelephoneNum(), tps.getAdjType().getCode()); 

    			fail(ctx, this, message, tps.getExceptionCaught(), TPSPipeConstant.FAIL_TO_FIND_ACCOUNT);
    			break; 
    		case TPSPipeConstant.MULTIPLE_SUBCRIBER_EXCEPTION:
    			message = "There are multiple postpaid subscribers have owing"; 
    		    if (tps.getExceptionCaught() != null)
    		    {
    		    	message = message.concat(" due to : " + tps.getExceptionCaught().getMessage()); 
    		    }    			
    			ERLogger.genInvalidTPSSubscriberER(ctx,TPSPipeConstant.MULTIPLE_SUBCRIBER_EXCEPTION);
 	 			new EntryLogMsg(10532, this, ""," multiple subscriber with same msisdn, excluded.", null, null).log(ctx);
  				PaymentException paymentException = TPSSupport.createOrUpdatePaymentExceptionRecord(ctx, tps, PaymentFailureTypeEnum.MULTISUB , tps.getMsisdn(), tps.getAdjType().getCode()); 
 				ERLogger.writeMultipleSubscribersInHistoryER(ctx, tps.getSubscriberList(), paymentException ); 
 				fail(ctx, this,message, tps.getExceptionCaught(),TPSPipeConstant.MULTIPLE_SUBCRIBER_EXCEPTION);
 				break; 
    		case TPSPipeConstant.FAIL_TO_FIND_SUB:
    		default:
    			message = "fail to find postpaid subscriber that matches the ban and msisdn in TPS"; 
    		    if (tps.getExceptionCaught() != null)
    		    {
    		    	message = message.concat(" due to : " + tps.getExceptionCaught().getMessage()); 
    		    }
    			
				ERLogger.genInvalidTPSSubscriberER(ctx,	TPSPipeConstant.FAIL_TO_FIND_SUB);                   
 				new EntryLogMsg(10532, this, ""," Postpaid Subscriber not found. " + tps.getMsisdn(), null, null).log(ctx);
  				TPSSupport.createOrUpdatePaymentExceptionRecord(ctx, tps, PaymentFailureTypeEnum.SUBSCRIBER , tps.getMsisdn(), tps.getAdjType().getCode()); 
 			    fail(ctx, this, message, tps.getExceptionCaught(),	TPSPipeConstant.FAIL_TO_FIND_SUB);

    			
    		}
    		
    	} catch (Exception e)
    	{
    		new MajorLogMsg(this, "Exception caught during loging error message", e).log(ctx); 
    	}
    }
    
    


 

}