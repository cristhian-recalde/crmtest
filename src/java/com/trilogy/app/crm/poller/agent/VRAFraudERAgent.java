/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.poller.agent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.home.SubscriberVRAFraudSingleFieldUpdateHome;
import com.trilogy.app.crm.home.sub.SubscriberNoteSupport;
import com.trilogy.app.crm.poller.FilterOutException;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.VRAPoller;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * updates the Subscriber Fraud state to either true/false & updates the state of the
 * subscriber depending upon the SPID configuration
 * 
 * @author amit.baid@redknee.com
 * 
 */
public class VRAFraudERAgent implements ContextAgent
{
    
    public VRAFraudERAgent(CRMProcessor processor)
    {
        super();
        processor_ = processor;
    }
    
    
    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    @Override
    public void execute(Context ctx) throws AgentException
    {
        List params = new ArrayList();
        ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute()");
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "VRA Poller ER " + info.getErid(), null).log(ctx);
        }
        try
        {

        	try {
        		CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',',info.getErid(), this);
           	} catch ( FilterOutException e){
				return; 
			}

        	switch (Integer.parseInt(info.getErid()))
            {
                case VRAPoller.VRA_VOUCHERFRAUD_ER_IDENTIFIER: {
                    updateSubFraudProfile(ctx, info, params);
                    break;
                }
                default: {
                    // Unknown VRA ER -- Ignore.
                }
            }
        }
        catch (final Throwable t)
        {
            new MinorLogMsg(this, "Failed to process ER " + info.getErid() + " because of Exception " + t.getMessage(),
                    t).log(ctx);
            processor_.saveErrorRecord(ctx, info.getRecord());
        }
        finally
        {
            pmLogMsg.log(ctx);
            CRMProcessor.playNice(ctx, CRMProcessor.MEDIUM_ER_THROTTLING);
        }
    }
    
    
    /**
     * Based upon the Action field in the ER1051 , the subscriber's VRA Fraud profile is
     * Locked/Unlocked. Next,Depending upon the Auto-Block configuration defined at
     * corresponding SPID, the Subscriber's state change is trigerred.
     * 
     * @param ctx
     * @param info
     * @param params
     */
    private void updateSubFraudProfile(Context ctx, ProcessorInfo info, List params)
    {
        boolean isValid = validateErContents(ctx, info, params);
        if (isValid)
        {
            // ER is already validated, go ahead taking the appropritate action on the
            // subscriber corresponding to the msisdn specified in the ER.
            try
            {
                final String msisdn = CRMProcessorSupport.getField(params, VRA_ER1051_INDEX_MSISDN);
                final int action = Integer.parseInt(CRMProcessorSupport.getField(params, VRA_ER1051_INDEX_ACTION));
                Context subCtx = ctx.createSubContext();
                Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDN(subCtx, msisdn, new Date(info.getDate()));
                switch (action)
                {
                    case VRA_ER1051_ACTION_UNLOCK:
                        unlockSub(subCtx, info, sub, msisdn);
                        break;
                    case VRA_ER1051_ACTION_LOCK:
                        lockSub(subCtx, info, sub, msisdn);
                        break;
                }
            }
            catch (HomeException he)
            {
                new MajorLogMsg(this, "Error occurred while processing ER1051 " + info.getRecord().toString(), he)
                .log(ctx);
                processor_.saveErrorRecord(ctx, info.getRecord());
                Common.OM_AUTOBLOCK_VOUCHERFRAUD.failure(ctx);
            }
        }
        else
        {
            new InfoLogMsg(this, "ER1051 [" + info.getRecord().toString() + "] is invalid.", null).log(ctx);
        }
    }
    
    
    /**
     * Set VraFraudProfile of the corresponding subscriber to true depending upon the
     * subscriber's SPID & its 'Auto-Block' configuration, Suspend /(Lock/Bar) the
     * subscriber.
     * 
     * @param ctx
     * @param info
     * @param sub
     */
    private void lockSub(Context ctx, ProcessorInfo info, Subscriber sub, String msisdn)
    {
        Subscriber subClone = null;
        final CRMSpid sp;
        final boolean autoBlockRequired;
        boolean changeStateAttempted = false, isSuccess = false , wrongOperation = false , rollback = false;
        SubscriberTypeEnum subType = null;
        ctx.put(OLD_SUB_STATE , sub.getState().getDescription());
        
        try
        {
            subClone = (Subscriber) sub.clone();
            if (subClone.getVraFraudProfile())
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Voucher Fraud Profile for " + sub.getMSISDN()
                            + " is already TRUE,Fraud Profile updation not needed.", null).log(ctx);
                }
                wrongOperation = true;
                return;
            }
            sp = SpidSupport.getCRMSpid(ctx, subClone.getSpid());
            if (sp != null)
            {
                autoBlockRequired = sp.getAutoBlockOnVoucherFraud();
            }
            else
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Voucher Fraud Profile Update check for " + sub.getMSISDN()
                            + " will not be made." + "Reason: Spid with ID " + sub.getSpid() + "doesn't exist.", null)
                            .log(ctx);
                }
                return;
            }
            subType = subClone.getSubscriberType();
            Common.OM_AUTOBLOCK_VOUCHERFRAUD.attempt(ctx);
            subClone.setVraFraudProfile(true);
            if (autoBlockRequired)
            {
                // If the Subscriber is Prepaid & State in Active/available the Subscriber
                // is Barred.
                if (subType.getIndex() == SubscriberTypeEnum.PREPAID_INDEX
                        && (subClone.getState().getIndex() == SubscriberStateEnum.ACTIVE_INDEX))
                {
                    subClone.setState(SubscriberStateEnum.LOCKED);
                    changeStateAttempted = true;
                }
                else if (subType.getIndex() == SubscriberTypeEnum.POSTPAID_INDEX && isBlockRelatedStateChange(subClone))
                {
                    subClone.setState(SubscriberStateEnum.SUSPENDED);
                    changeStateAttempted = true;
                }
            }
            else
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Auto-Blocking not required ,only updating the Fraud profile of subscriber " + subClone.getMSISDN(), null).log(ctx);
                }
                changeStateAttempted = false;
            }
            Home subHome = (Home) ctx.get(SubscriberHome.class);
            if (subHome != null)
            {
                ctx.put(NOTE_HANDLED_BY_VRAFRAUDERAGENT , true);
                ctx.put(NEW_SUB_STATE , subClone.getState().getDescription());
                Subscriber modifiedSub = null;

                if ( changeStateAttempted )
                {
                    modifiedSub = (Subscriber)subHome.store(ctx, subClone);
                }
                else
                {
                    Home vraSingleFieldUpdate = new SubscriberVRAFraudSingleFieldUpdateHome(ctx);
                    modifiedSub = (Subscriber) vraSingleFieldUpdate.cmd(ctx, subClone);
                }

                //Few exceptions are being thrown down the pipeline preventing the state change & don't get bubbled up till here.Hence 
                // explicit checking being done.
                if (modifiedSub != null)
                {
                    if (changeStateAttempted && modifiedSub.getState().getIndex() == subClone.getState().getIndex())
                    {
                        isSuccess = true;
                    }
                    else if((!changeStateAttempted) && (modifiedSub.getVraFraudProfile() == subClone.getVraFraudProfile()))
                    {
                        isSuccess = true;
                    }
                }
                else
                {
                    isSuccess = false;
                    rollback = true;
                }
            }
            else
            {
                new MinorLogMsg(this,
                        "SubscriberHome.class not found in Context,Voucher Fraud Profile can't be updated for "
                        + sub.getMSISDN(), null).log(ctx);
                isSuccess = false;
            }
        }
        catch (CloneNotSupportedException he)
        {
            new MajorLogMsg(this, "Error occurred while performing Auto-Block action on subscriber " + sub.getMSISDN(),
                    he).log(ctx);
            isSuccess = false;
            rollback = true;
        }
        catch (HomeException he)
        {
            new MajorLogMsg(this, "Error occurred while performing Auto-Block action on subscriber " + sub.getMSISDN(),
                    he).log(ctx);
            isSuccess = false;
            rollback = true;
        }
        catch (Exception e)
        {
            new MajorLogMsg(this, "Error occurred while performing Auto-Block action on subscriber " + sub.getMSISDN(),
                    e).log(ctx);
            isSuccess = false;
            rollback = true;
        }
        finally
        {
            if (isSuccess)
            {
                Common.OM_AUTOBLOCK_VOUCHERFRAUD.success(ctx);
                handleNotes(ctx, VRA_ER1051_ACTION_UNLOCK, changeStateAttempted, subClone.getId(), subType, true);
            }
            else if (!wrongOperation && rollback)
            {
                // Rollback to the unmodified 'sub' object
                try
                {
                    Home subHome = (Home) ctx.get(SubscriberHome.class);
                    Subscriber modifiedSub = (Subscriber)subHome.store(ctx, sub);
                }
                catch (HomeException he)
                {
                    //eat
                }
                processor_.saveErrorRecord(ctx, info.getRecord());
                Common.OM_AUTOBLOCK_VOUCHERFRAUD.failure(ctx);
                handleNotes(ctx, VRA_ER1051_ACTION_UNLOCK, changeStateAttempted, subClone.getId(), subType, false);
            }
        }
    }
    
    
    private boolean isBlockRelatedStateChange(Subscriber subClone)
    {
        return (subClone.getState().getIndex() == SubscriberStateEnum.ACTIVE_INDEX
                || subClone.getState().getIndex() == SubscriberStateEnum.PROMISE_TO_PAY_INDEX
                || subClone.getState().getIndex() == SubscriberStateEnum.NON_PAYMENT_WARN_INDEX
                || subClone.getState().getIndex() == SubscriberStateEnum.NON_PAYMENT_SUSPENDED_INDEX || subClone
                .getState().getIndex() == SubscriberStateEnum.IN_ARREARS_INDEX);
    }
    
    
    /**
     * @param ctx
     * @param changeStateAttempted
     * @param id
     * @param subType
     * @param isSuccess
     */
    private void handleNotes(Context ctx, int action, boolean changeStateAttempted, String id,
            SubscriberTypeEnum subType, boolean isSuccess)
    {
        if (action == VRA_ER1051_ACTION_LOCK)
        {
            if (changeStateAttempted)
            {
                SubscriberNoteSupport.createFraudLockStateChangeNote(ctx, id, isSuccess);
            }
            else
            {
                SubscriberNoteSupport.createFraudLockNoStateChangeNote(ctx, id, isSuccess);
            }
        }
        else if (action == VRA_ER1051_ACTION_UNLOCK)
        {
            if (changeStateAttempted)
            {
                SubscriberNoteSupport.createFraudUnLockStateChangeNote(ctx, id, isSuccess);
            }
            else
            {
                SubscriberNoteSupport.createFraudUnLockNoStateChangeNote(ctx, id, isSuccess);
            }
        }
    }
    
    
    /**
     * Set VraFraudProfile of the corresponding subscriber to false. Depending upon the
     * subscriber's SPID & its 'Auto-Block' configuration, change the state of the
     * subscriber appropriately
     * 
     * @param ctx
     * @param info
     * @param sub
     */
    private void unlockSub(Context ctx, ProcessorInfo info, Subscriber sub, String msisdn)
    {
        Subscriber subClone = null;
        final CRMSpid sp;
        boolean autoBlockRequired = false;
        boolean changeStateAttempted = false, isSuccess = false, wrongOperation = false, rollback = false;
        SubscriberTypeEnum subType = null;
        ctx.put(OLD_SUB_STATE , sub.getState().getDescription());
        
        try
        {
            subClone = (Subscriber) sub.clone();
            if (!subClone.getVraFraudProfile())
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Voucher Fraud Profile for " + sub.getMSISDN()
                            + " is already FALSE,Fraud Profile updation not needed.", null).log(ctx);
                }
                wrongOperation = true;
                return;
            }
            sp = SpidSupport.getCRMSpid(ctx, subClone.getSpid());
            if (sp != null)
            {
                autoBlockRequired = sp.getAutoBlockOnVoucherFraud();
            }
            else
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Voucher Fraud Profile Update check for " + sub.getMSISDN()
                            + " will not be made." + "Reason: Spid with ID " + sub.getSpid() + " doesn't exist.", null)
                            .log(ctx);
                }
                return;
            }
            subType = subClone.getSubscriberType();
            Common.OM_AUTOBLOCK_VOUCHERFRAUD.attempt(ctx);
            subClone.setVraFraudProfile(false);
            if (autoBlockRequired)
            {
                Account account = AccountSupport.getAccountByMsisdn(ctx, msisdn);
                if (account == null)
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "Account for msisdn " + sub.getMSISDN() + " doesn't exist.", null)
                        .log(ctx);
                    }
                    return;
                }
                // If Account-Type is Individual
				if (account.isIndividual(ctx))
                {
                    // If the Subscriber is Prepaid and is in Barred State the Subscriber
                    // State is changed to Active.
                    // OR
                    // If the Subscriber is Postpaid and is in Suspend State the
                    // Subscriber State is changed to Active.
                    // #Note : Currently Individual Account dont support PTP. If PTP is
                    // supported for Individual Accounts then before changing the account
                    // state to active if PTP end date > Current date set the Account
                    // state to PTP
                    if ((subType.getIndex() == SubscriberTypeEnum.PREPAID_INDEX && subClone.getState().getIndex() == SubscriberStateEnum.LOCKED_INDEX)
                            || (subType.getIndex() == SubscriberTypeEnum.POSTPAID_INDEX && subClone.getState()
                                    .getIndex() == SubscriberStateEnum.SUSPENDED_INDEX))
                    {
                        subClone.setState(SubscriberStateEnum.ACTIVE);
                        changeStateAttempted = true;
                    }
                }
                // If Account-Type is Non-Individual
                else
                {
                    // If the Subscriber is Prepaid and is in Barred State the Subscriber
                    // State is changed to Subscriber Account State.
                    // OR
                    // If the Subscriber is Postpaid and is in Suspend State the
                    // Subscriber State is changed to Subscriber Account State.
                    if ((subType.getIndex() == SubscriberTypeEnum.PREPAID_INDEX && subClone.getState().getIndex() == SubscriberStateEnum.LOCKED_INDEX)
                            || (subType.getIndex() == SubscriberTypeEnum.POSTPAID_INDEX && subClone.getState()
                                    .getIndex() == SubscriberStateEnum.SUSPENDED_INDEX))
                    {
                        subClone.setState(AccountSupport.accountStateToSubStateMapping(account.getState(), subType
                                .getIndex()));
                        changeStateAttempted = true;
                    }
                }
            }
            else
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Auto-Blocking disabled for Spid " + subClone.getSpid()
                            + " ,only updating the Fraud profile of subscriber " + subClone.getMSISDN(), null).log(ctx);
                }
                changeStateAttempted = false;
            }
            Home subHome = (Home) ctx.get(SubscriberHome.class);
            if (subHome != null)
            {
                ctx.put(NOTE_HANDLED_BY_VRAFRAUDERAGENT, true);
                ctx.put(NEW_SUB_STATE , subClone.getState().getDescription());
                Subscriber modifiedSub = null;

                if ( changeStateAttempted )
                {
                    modifiedSub = (Subscriber)subHome.store(ctx, subClone);
                }
                else
                {
                    Home vraSingleFieldUpdate = new SubscriberVRAFraudSingleFieldUpdateHome(ctx);
                    modifiedSub = (Subscriber) vraSingleFieldUpdate.cmd(ctx, subClone);
                }

                //Few exceptions are being thrown down the pipeline preventing the state change & don't get bubbled up till here.Hence 
                // explicit checking being done.
                if (modifiedSub != null)
                {
                    if (changeStateAttempted && modifiedSub.getState().getIndex() == subClone.getState().getIndex())
                    {
                        isSuccess = true;
                    }
                    else if((!changeStateAttempted) && (modifiedSub.getVraFraudProfile() == subClone.getVraFraudProfile()))
                    {
                        isSuccess = true;
                    }
                }
                else
                {
                    isSuccess = false;
                    rollback = true;
                }
            }
        }
        catch (CloneNotSupportedException cnse)
        {
            new MajorLogMsg(this, "Error occurred while performing Auto-Block action on subscriber " + sub.getMSISDN(),
                    cnse).log(ctx);
            isSuccess = false;
            rollback = true;
        }
        catch (HomeException he)
        {
            new MajorLogMsg(this, "Error occurred while performing Auto-Block action on subscriber " + sub.getMSISDN(),
                    he).log(ctx);
            isSuccess = false;
            rollback = true;
        }
        catch (Exception e)
        {
            new MajorLogMsg(this, "Error occurred while performing Auto-Block action on subscriber " + sub.getMSISDN(),
                    e).log(ctx);
            isSuccess = false;
            rollback = true;
        }
        finally
        {
            if (isSuccess)
            {
                Common.OM_AUTOBLOCK_VOUCHERFRAUD.success(ctx);
                handleNotes(ctx, VRA_ER1051_ACTION_UNLOCK, changeStateAttempted, subClone.getId(), subType, true);
            }
            else if (!wrongOperation && rollback)
            {
                // Rollback to the unmodified 'sub' object
                try
                {
                    Home subHome = (Home) ctx.get(SubscriberHome.class);
                    Subscriber modifiedSub = (Subscriber)subHome.store(ctx, sub);
                }
                catch (HomeException he)
                {
                    //eat
                }
                processor_.saveErrorRecord(ctx, info.getRecord());
                Common.OM_AUTOBLOCK_VOUCHERFRAUD.failure(ctx);
                handleNotes(ctx, VRA_ER1051_ACTION_UNLOCK, changeStateAttempted, subClone.getId(), subType, false);
            }
        }
    }
    
    
    /**
     * Validate ONLY the Action,Msisdn field of the ER1051.
     * 
     * @param ctx
     * @param info
     * @param params
     * @return
     */
    private boolean validateErContents(Context ctx, final ProcessorInfo info, final List params)
    {
        final String msisdn = CRMProcessorSupport.getField(params, VRA_ER1051_INDEX_MSISDN);
        final String actionField = CRMProcessorSupport.getField(params, VRA_ER1051_INDEX_ACTION);
        final String erString = info.getRecord().toString();
        // Validate the "Action" to be performed upon receving the ER [ 1 - Add,2 - Edit,3
        // - Delete/Unlock,4 - Lock ]
        // Action should be taken by CRM only for action field equalling
        // 3-Delete/Unlock,4-Lock
        {
            try
            {
                int action = Integer.parseInt(actionField);
                if (action < VRA_ER1051_ACTION_MINVALUE || action > VRA_ER1051_ACTION_MAXVALUE)
                {
                    new MinorLogMsg(this, "Invalid ER1051 [" + erString
                            + "],Reason: Action field of the ER is not in the range " + VRA_ER1051_ACTION_MINVALUE
                            + "-" + VRA_ER1051_ACTION_MAXVALUE + ".", null).log(ctx);
                    processor_.saveErrorRecord(ctx, info.getRecord());
                    Common.OM_AUTOBLOCK_VOUCHERFRAUD.failure(ctx);
                    return false;
                }
            }
            catch (NumberFormatException nfe)
            {
                new MinorLogMsg(this, "Invalid ER1051 [" + erString
                        + "],Reason: Failed to parse the 'Action' field of the ER.", nfe).log(ctx);
                processor_.saveErrorRecord(ctx, info.getRecord());
                Common.OM_AUTOBLOCK_VOUCHERFRAUD.failure(ctx);
                return false;
            }
        }
        // Validate the MSISDN
        {
            try
            {
                Msisdn mobNum = MsisdnSupport.getMsisdn(ctx, msisdn);
                if (mobNum == null)
                {
                    new MinorLogMsg(this, "Invalid ER1051 [" + erString
                            + "],Reason: Subscriber corresponding to MSISDN [" + msisdn + " doesn't exist.", null)
                            .log(ctx);
                    processor_.saveErrorRecord(ctx, info.getRecord());
                    Common.OM_AUTOBLOCK_VOUCHERFRAUD.failure(ctx);
                    return false;
                }
            }
            catch (HomeException he)
            {
                new MajorLogMsg(this, "Error ocurred while validating the MSISDN in the ER " + erString, he).log(ctx);
                processor_.saveErrorRecord(ctx, info.getRecord());
                Common.OM_AUTOBLOCK_VOUCHERFRAUD.failure(ctx);
                return false;
            }
        }
        return true;
    }
    
    private final CRMProcessor processor_;
    private static final String PM_MODULE = VRAFraudERAgent.class.getName();
    private static final int VRA_ER1051_INDEX_MSISDN = 5;
    private static final int VRA_ER1051_INDEX_ACTION = 6;
    private final int VRA_ER1051_ACTION_MINVALUE = 1;
    private final int VRA_ER1051_ACTION_MAXVALUE = 4;
    private final int VRA_ER1051_ACTION_UNLOCK = 3;
    private final int VRA_ER1051_ACTION_LOCK = 4;
    public static String SUSPEND_DUE_TO_FRAUD_PROFILE = "Suspend/Barred due to Fraud profile";
    public static String NOTE_HANDLED_BY_VRAFRAUDERAGENT = "Note_Handled_By_VraFraudErAgent";
    public static String OLD_SUB_STATE = "Old subscriber state";
    public static String NEW_SUB_STATE = "New subscriber state";
}
