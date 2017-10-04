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
package com.trilogy.app.crm.home.sub;

import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Collection;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bean.service.SuspendReasonEnum;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.dunning.DunningConstants;
import com.trilogy.app.crm.dunning.DunningProcess;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.home.cmd.CreateSubscriberCmd;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.subscriber.provision.ProvisionResultCode;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionEndHome;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionHlrGatewayHome;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionResultCode;
import com.trilogy.app.crm.support.DefaultConfigChangeRequestSupport;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.EQIC;
import com.trilogy.framework.xhome.home.BeanNotFoundHomeException;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.app.crm.dunning.DunningConstants;
import com.trilogy.app.crm.bean.SubscriptionSuspensionReason;
import com.trilogy.app.crm.bean.SubscriptionSuspensionReasonHome;
import com.trilogy.app.crm.bean.SubscriptionSuspensionReasonXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;

/**
 * This decorator logs CRM subscriber provisioning status.
 *
 * @author joe.chen@redknee.com
 */
public class SubscriberProvisionLogHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>SubscriberProvisionLogHome</code>.
     *
     * @param delegate
     *            Delegate of this home.
     */
    public SubscriberProvisionLogHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException, HomeInternalException
    {
        // We need this in order to avoid one hundred million casts
        Subscriber sub = (Subscriber) obj;
        Context subCtx = ctx.createSubContext();

        if (sub.getSubscriberType() == SubscriberTypeEnum.PREPAID && sub.getState() == SubscriberStateEnum.AVAILABLE)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_CREATE_ATTEMPT).log(ctx);
        }
        else
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_ACTIVATE_ATTEMPT).log(ctx);
        }

        Exception exp = null;
        int lastResult = -1;
        try
        {
            subCtx.put(HTMLExceptionListener.class, new HTMLExceptionListener(new MessageMgr(ctx, Subscriber.class)));
            sub = (Subscriber) super.create(subCtx, obj);
            // Propagating the key added to the context during subscriber creation, since it will be needed by SubscriberHomeNoteHome.
            ctx.put(SubscriberProvisionEndHome.getSubscriberCreatedKey(sub), subCtx.getBoolean(SubscriberProvisionEndHome.getSubscriberCreatedKey(sub)));
        }
        catch (final ProvisioningHomeException phe)
        {
            new OMLogMsg(Common.OM_MODULE, phe.getErrModule()).log(ctx);
            exp = phe;
        }
        catch (final Exception e)
        {
            final Subscriber subscriber = (Subscriber) obj;
            final String msg = "Subscriber creation error, out of sync." + subscriber.getId();
            SubscriberProvisionResultCode.addException(subCtx, msg, e, null, subscriber);
            exp = e;
        }

        lastResult = SubscriberProvisionResultCode.getProvisionLastResultCode(subCtx);
        if (exp != null || lastResult != 0)
        {
            if (sub.getSubscriberType() == SubscriberTypeEnum.PREPAID
                && sub.getState() == SubscriberStateEnum.AVAILABLE)
            {
                new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_CREATE_FAIL).log(ctx);
            }
            else
            {
                new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_ACTIVATE_FAIL).log(ctx);
            }

            // [CW] log the reason for the original failure
            new MinorLogMsg(this, "fail to provision subscriber " + sub.getId() + " with error code "
                + SubscriberProvisionResultCode.getProvisionResultCode(subCtx) + ". Begin errorhandling", exp).log(ctx);

            if (SystemSupport.supportsPrepaidPendingState(ctx) || sub.isPostpaid())
            {
                sub.setState(SubscriberStateEnum.PENDING);
                /*
                 * Adding services to subscriber.
                 */
                for (ServiceFee2ID serviceFee2ID : sub.getServices())
                {
                    SubscriberServices service = SubscriberServicesSupport.getSubscriberServiceRecord(ctx, sub.getId(),
                    		serviceFee2ID.getServiceId(), serviceFee2ID.getPath());
                    if (service == null)
                    {
                        SubscriberServicesSupport.createOrModifySubcriberService(ctx, sub, serviceFee2ID.getServiceId(),
                                ServiceStateEnum.PENDING, SuspendReasonEnum.NONE, null);
                    }
                }
                /*
                 * [CW] TT 5050217772 apparently the row doesn't necessarily exist in the
                 * database. So attempt an update first (Joe's original design) and if
                 * that fails, we'll attempt a create
                 */
                try
                {
                    final CreateSubscriberCmd createCmd = new CreateSubscriberCmd(sub, HomeOperationEnum.STORE);
                    sub = (Subscriber) getDelegate().cmd(ctx, createCmd);
                }
                catch (final HomeException hEx)
                {
                    final CreateSubscriberCmd createCmd = new CreateSubscriberCmd(sub);
                    sub = (Subscriber) getDelegate().cmd(ctx, createCmd);
                }
                
                //Logging subscription note.
                String message = "Subscriber activation failed";
                NoteSupportHelper.get(ctx).addSubscriberNote(ctx, sub.getId(), message, SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.SUBUPDATE);

                logActivationFailureNotes(subCtx, sub);
            }
            else
            {
                try
                {
                    final CreateSubscriberCmd createCmd = new CreateSubscriberCmd(sub, HomeOperationEnum.REMOVE);
                    getDelegate().cmd(ctx, createCmd);
                }
                catch (final BeanNotFoundHomeException hEx)
                {
                }
                catch (Exception ex)
                { // [CW] log the reason for the original failure
                    new MinorLogMsg(this, "fail to remove subscriber " + sub.getId() + " which failed to provision", exp)
                            .log(ctx);
                }
            }
        }
        else
        {
            if (sub.getSubscriberType() == SubscriberTypeEnum.PREPAID
                && sub.getState() == SubscriberStateEnum.AVAILABLE)
            {
                new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_CREATE_SUCCESS).log(ctx);
            }
            else
            {
                new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_ACTIVATE_SUCCESS).log(ctx);
            }
        }

        printCapturedExceptions(subCtx, sub);

        if (exp != null)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "create(): fail to provision subscriber " + sub.getId() + exp, exp).log(ctx);
            }
            throw new HomeException("" + exp, exp);
        }

        return sub;
    }

    private void logActivationFailureNotes(Context ctx, Subscriber sub) throws HomeException
    {
        final HTMLExceptionListener el = SubscriberProvisionResultCode.getExceptionListener(ctx);
        if (el.getExceptions()!=null && el.getExceptions().size()>0)
        {
            for (Throwable t: (List<Throwable>) el.getExceptions())
            {
                NoteSupportHelper.get(ctx).addSubscriberNote(ctx, sub.getId(), t.getMessage(), SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.SUBUPDATE);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException, HomeInternalException
    {
        HomeException exp = null;
        Context subCtx = ctx.createSubContext();

        Subscriber newSub = (Subscriber) obj;
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        final Account localAccount = (Account) ctx.get(Lookup.ACCOUNT);

        // Generate OM
        if (oldSub != null && newSub != null)
        {
        	//manual suspension of subscriber from bss
        	if(newSub.getState().equals(SubscriberStateEnum.SUSPENDED) && newSub.getSuspensionReason().equals(null))
    		{
    			//set suspension date for manual suspension for subscription(TT N0-ITSC-8046)
    			newSub.setSuspensionDate(new Date());
    			
    		}
        	

        	if(oldSub.getState().equals(SubscriberStateEnum.SUSPENDED)&& newSub.getState().equals(SubscriberStateEnum.ACTIVE))
        	{
        		int spid = newSub.getSpid();

				And where = new And();
				where.add(new EQ(SubscriptionSuspensionReasonXInfo.SPID, spid));
				where.add(new EQIC(SubscriptionSuspensionReasonXInfo.NAME, DunningConstants.DUNNING_SUSPENSION_REASON_UNPAID));
				
				final Collection<SubscriptionSuspensionReason> subSuspensionReasonColl
				= HomeSupportHelper.get(ctx).getBeans(ctx, SubscriptionSuspensionReason.class, where);

				if (subSuspensionReasonColl != null && !subSuspensionReasonColl.isEmpty())
				{
					for (SubscriptionSuspensionReason subSusReason : subSuspensionReasonColl) 
					{
						String subSusReasonCode = subSusReason.getReasoncode();
						if(subSusReasonCode != oldSub.getSuspensionReason())
						{
							String subReason=null;
			        		newSub.setSuspensionReason(subReason);
			        		newSub.setResumedDate(new Date());	
						}
					}
				}else
				{
					 if (LogSupport.isDebugEnabled(ctx))
				        {
				            new DebugLogMsg(this, "SubscriberSuspensionReasonMapping is not found", null)
				                .log(ctx);
				        }
				}
						

        	}
        	
            logOMAttempt(ctx, oldSub, newSub);
        }

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Update Subscriber  old:" + oldSub.getId() + "  new:" + newSub.getId(), null)
                .log(ctx);
        }

        logDunningStartAttempt(ctx, localAccount, oldSub, newSub);

        try
        {
            if (!SubscriberSupport.isSamePricePlanVersion(ctx, oldSub, newSub))
            {
                new OMLogMsg(Common.OM_MODULE, Common.OM_PRICE_PLAN_CHANGE).log(ctx);
            }

            subCtx.put(HTMLExceptionListener.class, new HTMLExceptionListener(new MessageMgr(ctx, Subscriber.class)));
            newSub = (Subscriber) super.store(subCtx, obj);
        }
        catch (final ProvisioningHomeException phe)
        {
            new OMLogMsg(Common.OM_MODULE, phe.getErrModule()).log(ctx);

            SubscriberProvisionResultCode.setProvisionLastResultCode(subCtx, phe.getResultCode());
            exp = phe;
        }
        catch (final Exception e)
        {

            /* ali: According to the HLD "8.3 Result Codes", 3007 is reserved for Error Provisioning to SMSB.
             * The more correct error code to use here would be 3009 -- "Error Provisioning BAS". */  
            ERLogger.logOutOfSync10339(ctx, oldSub, e, this, 3009);

            final String msg = e.getMessage() + " while updating subscription '" + oldSub.getId() + "'";

            SubscriberProvisionResultCode.setProvisionLastResultCode(subCtx, -1);

            logOMFail(ctx, oldSub, newSub);

            if(LogSupport.isDebugEnabled(ctx))
                LogSupport.debug(ctx, this, "Got Exception internally, while saving the subscriber.", e);
            
            exp = new HomeException(msg, e);

        }

        final ProvisionResultCode rCode = (ProvisionResultCode) subCtx.get(ProvisionResultCode.class);
        final int lastResult = rCode.getResultLast();
        /*
         * TT#11060105052: When we encounter HLR provisioning error, we want to exempt the
         * state reversal. The HLR out-of-sync is already logged.
         */
        boolean hasError = exp != null || lastResult != 0;
        if (hasError && lastResult != SubscriberProvisionHlrGatewayHome.HLR_ERROR)
        {
            if (oldSub != null && newSub != null)
            {
                logOMFail(ctx, oldSub, newSub);
            }
            // We need to update to the original state for creation error
            if (!EnumStateSupportHelper.get(ctx).stateEquals(oldSub, newSub))
            {
                /* Write Note to indicate failure to perform State Change.
                 * Once we revert the State Change, the SubscriberHomeNoteHome will not write
                 * a note for the intended change. */
                SubscriberNoteSupport.createFailedStateChangeNote(ctx, this, oldSub, newSub);
                
                if (SubscriberStateEnum.ACTIVE.equals(newSub.getState())
                        && (SubscriberStateEnum.PENDING.equals(oldSub.getState()) || 
                            SubscriberStateEnum.AVAILABLE.equals(oldSub.getState())))
                {
                    logActivationFailureNotes(subCtx, newSub);
                }
                
                newSub.setState(oldSub.getState());
                newSub = (Subscriber) getDelegate().cmd(ctx, new CreateSubscriberCmd(newSub, HomeOperationEnum.STORE));
            }
        }
        else if (oldSub != null && newSub != null)
        {
            logOMSuccess(ctx, oldSub, newSub);
        }

        logDunningEr(ctx, localAccount, oldSub, newSub, rCode);
        printCapturedExceptions(subCtx, newSub);

        if (exp != null)
        {
            throw exp;
        }

        return newSub;
    }


    /**
     * Logs dunning ER.
     *
     * @param ctx
     *            The operating context.
     * @param localAccount
     *            Local account.
     * @param oldSub
     *            Old subscriber.
     * @param newSub
     *            New subscriber.
     * @param rCode
     *            Result code.
     */
    void logDunningEr(final Context ctx, final Account localAccount, final Subscriber oldSub, final Subscriber newSub,
        final ProvisionResultCode rCode)
    {

        try
        {
            final DunningProcess dunningProcess = (DunningProcess) ctx.get(DunningProcess.class);
            if (dunningProcess.isInDunningProcess(ctx) && !EnumStateSupportHelper.get(ctx).stateEquals(oldSub, newSub))
            {
                boolean overallSuccess = false;
                
                AccountStateEnum state = (AccountStateEnum) ctx.get(DunningConstants.DUNNING_ACTION_KEY);
                if (state == null )
                {
                    state = localAccount.getState();
                }
                if (rCode.getResultLast() == 0)
                {
                    dunningProcess.logEventRecord(ctx, newSub, state,
                        DunningConstants.ALL_SUCCESS_ER, localAccount);

                    overallSuccess = true;
                }
                else
                {
                    if (rCode.getResultEcp() != 0)
                    {
                        dunningProcess.logEventRecord(ctx, newSub, state,
                            DunningConstants.ECP_FAILURE_ER, localAccount);
                    }
                    else
                    {
                        dunningProcess.logEventRecord(ctx, newSub, state,
                            DunningConstants.SOME_FAILURE_ER, localAccount);
                    }
                }

                dunningProcess.doSubscriberDunningResultOm(ctx, state, newSub, overallSuccess, this);
            }
        }
        catch (final DunningProcessException dpEx)
        {
            new MinorLogMsg(this, "fail to log dunning event record", dpEx).log(ctx);
        }
        catch (final Throwable t)
        {
            new MinorLogMsg(this, "System Error. fail to log dunning event record", t).log(ctx);
        }
        

    }


    /**
     * Logs dunning start attempt.
     *
     * @param ctx
     *            The operating context.
     * @param account
     *            The account owning the subscriber.
     * @param oldSub
     *            The old subscriber.
     * @param newSub
     *            The new subscriber.
     */
    void logDunningStartAttempt(final Context ctx, final Account account, final Subscriber oldSub,
        final Subscriber newSub)
    {
        try
        {
            final DunningProcess dunningProcess = (DunningProcess) ctx.get(DunningProcess.class);
            if (dunningProcess.isInDunningProcess(ctx) && !EnumStateSupportHelper.get(ctx).stateEquals(oldSub, newSub))
            {
                dunningProcess.omLoggerAttempt(ctx, account.getState(), this);
            }
        }
        catch (final DunningProcessException dpEx)
        {
            new MinorLogMsg(this, "fail to log om logger attmpet", dpEx).log(ctx);
        }
    }


    /**
     * Logs OMs for attempt.
     *
     * @param ctx
     *            The operating context.
     * @param oldSub
     *            The old subscriber.
     * @param newSub
     *            The new subscriber.
     */
    private void logOMAttempt(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
    {
        if (oldSub.getAbstractState() != newSub.getAbstractState())
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_STATE_CHANGE_ATTEMPT).log(ctx);
        }

        if (ctx.get(Common.PREPAID_POSTPAID_CONVERSION_SUBCRIBER) != null
            || ctx.get(Common.POSTPAID_PREPAID_CONVERSION_SUBCRIBER) != null)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_CONVERSION_ATTEMPT).log(ctx);
            new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_ADJUSTMENT).log(ctx);
        }

        if (oldSub.getAbstractState() != SubscriberStateEnum.ACTIVE
            && newSub.getAbstractState() == SubscriberStateEnum.ACTIVE)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_ACTIVATE_ATTEMPT).log(ctx);
        }
        else if (oldSub.getAbstractState() != SubscriberStateEnum.SUSPENDED
            && newSub.getAbstractState() == SubscriberStateEnum.SUSPENDED)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_SUSPEND_ATTEMPT).log(ctx);
        }
        else if (oldSub.getAbstractState() != SubscriberStateEnum.INACTIVE
            && newSub.getAbstractState() == SubscriberStateEnum.INACTIVE)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_DEACTIVATE_ATTEMPT).log(ctx);
        }
    }


    /**
     * Logs OMs for success.
     *
     * @param ctx
     *            The operating context.
     * @param oldSub
     *            The old subscriber.
     * @param newSub
     *            The new subscriber.
     */
    private void logOMSuccess(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
    {
        if (oldSub.getAbstractState() != newSub.getAbstractState())
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_STATE_CHANGE_SUCCESS).log(ctx);
        }

        if (ctx.get(Common.PREPAID_POSTPAID_CONVERSION_SUBCRIBER) != null
            || ctx.get(Common.POSTPAID_PREPAID_CONVERSION_SUBCRIBER) != null)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_CONVERSION_SUCCESS).log(ctx);
        }

        if (oldSub.getAbstractState() != SubscriberStateEnum.ACTIVE
            && newSub.getAbstractState() == SubscriberStateEnum.ACTIVE)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_ACTIVATE_SUCCESS).log(ctx);
        }
        else if (oldSub.getAbstractState() != SubscriberStateEnum.SUSPENDED
            && newSub.getAbstractState() == SubscriberStateEnum.SUSPENDED)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_SUSPEND_SUCCESS).log(ctx);
        }
        else if (oldSub.getAbstractState() != SubscriberStateEnum.INACTIVE
            && newSub.getAbstractState() == SubscriberStateEnum.INACTIVE)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_DEACTIVATE_SUCCESS).log(ctx);
        }
        else if (oldSub.getAbstractState() != SubscriberStateEnum.LOCKED
            && newSub.getAbstractState() == SubscriberStateEnum.LOCKED)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_LOCKING_SUCCESS).log(ctx);
        }
    }


    /**
     * Logs OMs for failure.
     *
     * @param ctx
     *            The operating context.
     * @param oldSub
     *            The old subscriber.
     * @param newSub
     *            The new subscriber.
     */
    private void logOMFail(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
    {
        if (oldSub.getAbstractState() != newSub.getAbstractState())
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_STATE_CHANGE_FAIL).log(ctx);
        }

        if (ctx.get(Common.PREPAID_POSTPAID_CONVERSION_SUBCRIBER) != null
            || ctx.get(Common.POSTPAID_PREPAID_CONVERSION_SUBCRIBER) != null)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_CONVERSION_FAIL).log(ctx);
        }

        if (oldSub.getAbstractState() != SubscriberStateEnum.ACTIVE
            && newSub.getAbstractState() == SubscriberStateEnum.ACTIVE)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_ACTIVATE_FAIL).log(ctx);
        }
        else if (oldSub.getAbstractState() != SubscriberStateEnum.SUSPENDED
            && newSub.getAbstractState() == SubscriberStateEnum.SUSPENDED)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_SUSPEND_FAIL).log(ctx);
        }
        else if (oldSub.getAbstractState() != SubscriberStateEnum.INACTIVE
            && newSub.getAbstractState() == SubscriberStateEnum.INACTIVE)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_DEACTIVATE_FAIL).log(ctx);
        }
        else if (oldSub.getAbstractState() != SubscriberStateEnum.LOCKED
            && newSub.getAbstractState() == SubscriberStateEnum.LOCKED)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_LOCKING_FAIL).log(ctx);
        }
    }


    /**
     * Prints currently captured exceptions.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The subscriber being processed.
     */
    public static void printCapturedExceptions(final Context ctx, final Subscriber subscriber)
    {
        // print the warnings to the screen if the screen is available
        final HTMLExceptionListener el = SubscriberProvisionResultCode.getExceptionListener(ctx);
        final PrintWriter out = FrameworkSupportHelper.get(ctx).getWriter(ctx);
        if (el != null && el.hasErrors())
        {
            if (out != null)
            {
                /*
                 * if we still need to track provisioning error, we can use this field for
                 * some historical reasons, Sub home ignores provision error but I think
                 * at least we need to track it, make CSR aware of it from bulk create as
                 * data are not consistent any more. -JoeC Jan 31, 2005
                 */
                subscriber.setLastExp(new HomeException("Provision Error."));
                if (el.numOfErrors() <= 10)
                {
                    out.println("<center><font color=\"red\"><b>Account not charged due to warning in saved entry:</b></font></center>");
                    el.toWeb(ctx, out, null, null);
                }
                else
                {
                    out.println("<center><font color=\"red\"><b>Account not charged. Please check OM/Log/Alarm files. </b></font></center>");
                }
            }
            else
            {
                captureExceptionsForApi(ctx, subscriber, el);
            }
        }
    }
    
    
    private static void captureExceptionsForApi(final Context ctx, final Subscriber subscriber, HTMLExceptionListener el)
    {
        Object fromAPI = ctx.get(DefaultConfigChangeRequestSupport.API_SOURCE_USERNAME);
        if (fromAPI != null)
        {
            List<Throwable> list = el.getExceptions();
            if (! list.isEmpty())
            {
                StringBuilder strBuilder = new StringBuilder("ERRORS: ");
                for (Throwable ex : list)
                {
                    strBuilder.append(ex.getMessage());
                    strBuilder.append("\n");
                }
                HomeException homeEx = new HomeException(strBuilder.toString());
                subscriber.setLastExp(homeEx);
            }
        }
    }
}
