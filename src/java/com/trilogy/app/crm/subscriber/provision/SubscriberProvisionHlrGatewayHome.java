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

package com.trilogy.app.crm.subscriber.provision;

import java.util.Arrays;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.DunningConfigurationEnum;
import com.trilogy.app.crm.bean.ProvisionCommand;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.hlr.HlrSupport;
import com.trilogy.app.crm.home.sub.HLRConstants;
import com.trilogy.app.crm.home.sub.StateChangeAuxiliaryServiceSupport;
import com.trilogy.app.crm.provision.HLRRemoveSubAgent;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.report.ReportUtilities;
import com.trilogy.app.crm.support.CreditCategorySupport;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * @author jchen
 */
public class SubscriberProvisionHlrGatewayHome extends HomeProxy
{
    /**
     * 
     */
    private static final long serialVersionUID = 864189130323861183L;
    
    public static final int HLR_ERROR = 3011;
    
    /**
     * @param ctx
     * @param delegate
     */
    public SubscriberProvisionHlrGatewayHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }
    
    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#create(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        new DebugLogMsg(this, "SubscriberProvisionHlrGatewayHome.create(ctx,sub)", null).log(ctx);
        final Subscriber newSub = (Subscriber)obj;
        final Subscriber oldSub = null;
        Object returnedSubscriber = null;

        boolean pipelineInAdvance =false;
        if (SystemSupport.needsHlr(ctx))
        {
            String hlrCommand = HLRConstants.PRV_CMD_TYPE_CREATE;
            final ProvisionCommand provisionCommand = HlrSupport.findCommand(ctx, newSub, hlrCommand);
            pipelineInAdvance = provisionCommand != null && !provisionCommand.isInAdvanceCmd();
            if (provisionCommand != null)
            {
                if (pipelineInAdvance)
                {
                    returnedSubscriber = super.create(ctx, newSub);
                }
                try
                {
                    updateHLRForSubscriber(ctx, newSub, provisionCommand);
                }
                catch (Exception e)
                {
                    SubscriberProvisionResultCode.setProvisionHlrResultCode(ctx, -1);
                    SubscriberProvisionResultCode.addException(ctx, e.getMessage() + " (Command = " + hlrCommand + ")",
                            e, oldSub, newSub);
                    // if HLR create command failed, services HLR provisioning will fail.
                    // We rely on error handling from SubscriberProvisionLogHome.
                }
            }
        }
        if (!pipelineInAdvance)
        {
            return super.create(ctx, newSub);
        }
        
        return returnedSubscriber;
        
    }
    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        new DebugLogMsg(this, "SubscriberProvisionHlrGatewayHome.store(ctx,sub)", null).log(ctx);
        final Subscriber newSub = (Subscriber)obj;
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

        int lastResult = 0;
        int hlrResult = 0;

        Object ret = null;
        

        try
        {
            if (SystemSupport.needsHlr(ctx))
            {
                sendHLRCommands(ctx, oldSub, newSub, true);
            }
        }
        catch (Exception e)
        {
            //No exception throwing in Ibis
            //throw new ProvisioningHomeException("RemoveSub from hlr error.", -1, Common.OM_HLR_PROV_ERROR, e);

            //TODO, check does it set last error code ??
            lastResult = HLR_ERROR;

            hlrResult = getHlrResponsibleCode(getContext(), e, this);
            
            LogSupport.major(ctx,  this, "Sending HLR command failed with result "
                    + hlrResult
                    + " : failed to send HLR command in advance of service provisioning/unprovisioning for subscriber with IMSI ["
                    + oldSub.getIMSI()
                    + "] due to ["
                    + e.getMessage()
                    + "]. Please check log file for detailed information.", e);
            
            SubscriberSupport.generateOM(ctx,
                    SubscriberProvisionResultCode.getExceptionListener(ctx),
                    lastResult,
                    e.getMessage(),
                    hlrResult,
                Common.OM_HLR_PROV_ERROR,
                this, e);
        }
        
        setProvisioningHlrResultCode(ctx, hlrResult, lastResult);


        ret = super.store(ctx, newSub);
        
        try
        {
            if (SystemSupport.needsHlr(ctx))
            {
                sendHLRCommands(ctx, oldSub, newSub, false);
            }
        }
        catch (Exception e)
        {
            //No exception throwing in Ibis
            //throw new ProvisioningHomeException("RemoveSub from hlr error.", -1, Common.OM_HLR_PROV_ERROR, e);

            //TODO, check does it set last error code ??
            lastResult = HLR_ERROR;

            hlrResult = getHlrResponsibleCode(getContext(), e, this);

            SubscriberSupport.generateOM(ctx,
                    SubscriberProvisionResultCode.getExceptionListener(ctx),
                    lastResult,
                    "Sending HLR command failed with result "
                    + lastResult
                    + " : failed to send HLR command after of service provisioning/unprovisioning for subscriber with IMSI ["
                    + oldSub.getIMSI()
                    + "] due to ["
                    + e.getMessage()
                    + "]. Please check log file for detailed information.",
                    hlrResult,
                Common.OM_HLR_PROV_ERROR,
                this,
                e);
        }
        
        setProvisioningHlrResultCode(ctx, hlrResult, lastResult);        

        //Senthooran added to address TT 7082300030
        if (oldSub.getPricePlan()!=newSub.getPricePlan() && oldSub.getState()==newSub.getState() && SystemSupport.needsHlr(ctx)
                && SystemSupport.isReProvisionAuxServiceOnPricePlanChange(ctx))
        {
            StateChangeAuxiliaryServiceSupport.updateHlrToProvisioning(ctx, newSub,this);
        }

        return ret;
    }
    
    private void setProvisioningHlrResultCode(Context ctx, int hlrResult, int lastResult)
    {
        if (hlrResult != 0)
        {
            SubscriberProvisionResultCode.setProvisionHlrResultCode(ctx, hlrResult);
        }

        if (lastResult != 0)
        {
            SubscriberProvisionResultCode.setProvisionLastResultCode(ctx, HLR_ERROR);
        }
    }
    
    private void sendHLRCommands(Context ctx, Subscriber oldSub, Subscriber newSub, boolean inAdvance) throws Exception
    {
            if (!SafetyUtil.safeEquals(oldSub.getMSISDN(), newSub.getMSISDN()))
            {
                onChangeMsisdn(ctx, oldSub, newSub, inAdvance);
            }

            if (!SafetyUtil.safeEquals(oldSub.getPackageId(), newSub.getPackageId()))
            {
                onChangePackage(ctx, oldSub, newSub, inAdvance);
            }
            else if (!SafetyUtil.safeEquals(oldSub.getIMSI(), newSub.getIMSI()))
            {
                onChangePackage(ctx, oldSub, newSub, inAdvance);
            }

            if (!oldSub.getSubscriberType().equals(newSub.getSubscriberType()))
            {
                onConversion(ctx, oldSub, newSub, inAdvance);
            }
            if (!(oldSub.getState().equals(newSub.getState()))){	

            	onSubscriberStateChange(ctx, oldSub, newSub, inAdvance);
            }
            
            if (!SubscriberSupport.isSamePricePlanVersion(ctx, oldSub, newSub))
            {
                onPricePlanChange(ctx, oldSub, newSub, inAdvance);
            }
            
            if (!(oldSub.getBillingLanguage().equals(newSub.getBillingLanguage())))
            {	
            	onBillingLanguageChange(ctx, oldSub, newSub, inAdvance);
            }
            
    }    


    private void onBillingLanguageChange(final Context ctx, final Subscriber oldSub, final Subscriber newSub, final boolean inAdvance) throws Exception
    {
        String hlrCommand = HLRConstants.PRV_CMD_TYPE_LANG_CHANGE;

        ProvisionCommand command = HlrSupport.findCommand(ctx, newSub, hlrCommand);
        if (command!=null && command.isInAdvanceCmd()==inAdvance)
        {
            setProvisionCommand(ctx, command);
            updateHLRForSubscriber(ctx, newSub, hlrCommand);
        }
    }

    
    private void onPricePlanChange(final Context ctx, final Subscriber oldSub, final Subscriber newSub, final boolean inAdvance) throws Exception
    {
        String hlrCommand = HLRConstants.PRV_CMD_TYPE_CLEAN_PROFILE;

        ProvisionCommand command = HlrSupport.findCommand(ctx, newSub, hlrCommand);
        if (command!=null && command.isInAdvanceCmd()==inAdvance)
        {
            setProvisionCommand(ctx, command);
            updateHLRForSubscriber(ctx, newSub, hlrCommand);
        }
    }

    /**
     * @param oldSub
     * @param newSub
     * @return
     */
    public boolean subscriberTypeEquals(final Subscriber oldSub, final Subscriber newSub)
    {
        return oldSub.getSubscriberType().equals(newSub.getSubscriberType());
    }

    
    public void onSubscriberStateChange(final Context ctx, final Subscriber oldSub,
            final Subscriber newSub, final boolean inAdvance) throws HomeException
    {
        
        ProvisionCommand command = retrieveStateChangeProvisionCommand(ctx, oldSub, newSub);
        
        if (command!=null && inAdvance == command.isInAdvanceCmd())
        {
            try
            {
                setProvisionCommand(ctx, command);
                updateHLRForSubscriber(ctx, newSub, command);
            }
            catch (Exception e)
            {
                SubscriberProvisionResultCode.setProvisionHlrResultCode(ctx, -1);
                throw new ProvisioningHomeException(e.getMessage(), -1, Common.OM_HLR_PROV_ERROR, e);
            }
        }
        
        if (inAdvance) 
        {
            if (needsSuspendHlr(ctx, oldSub, newSub) || needsLockHLR(ctx, oldSub, newSub))
            {
                StateChangeAuxiliaryServiceSupport.updateHlrToSuspend(ctx, newSub, this);
            }
            else if (needsActiveHlr(ctx, oldSub, newSub))
            {
                StateChangeAuxiliaryServiceSupport.updateHlrToUnSuspend(ctx, newSub, this);
            }
            
            if (isSendHlrForProvisionAuxServices(ctx, oldSub, newSub))
            {
                StateChangeAuxiliaryServiceSupport.updateHlrToProvisioning(ctx, newSub,this);
            }
            else if (isSendHlrForUnProvisionAuxServices(ctx, oldSub, newSub))
            {
                StateChangeAuxiliaryServiceSupport.updateHlrToUnProvisioning(ctx, newSub, this);
            }
        }
    }

    

    public ProvisionCommand retrieveStateChangeProvisionCommand(final Context ctx, final Subscriber oldSub,
            final Subscriber newSub) throws HomeException
    {
        ProvisionCommand result = null;
        String hlrCommand = null;

        final Subscriber safeSub = newSub == null ? oldSub : newSub;
        if (needsInactiveHlr(ctx, oldSub, newSub))
        {
            hlrCommand =  HLRConstants.PRV_CMD_TYPE_INACTIVE;
        }
        else if (needsPostToPrepaidConversionHlr(ctx, oldSub, newSub))
        {
            hlrCommand = HLRConstants.PRV_CMD_TYPE_CONVERSION;
        }
        else if (needsCreateHlr(ctx, oldSub, newSub))
        {
            hlrCommand = HLRConstants.PRV_CMD_TYPE_CREATE;
        }
        else if (needsSuspendHlr(ctx, oldSub, newSub) || needsLockHLR(ctx, oldSub, newSub))
        {
            hlrCommand = HLRConstants.PRV_CMD_TYPE_BARRED;
        }
        else if (needsActiveHlr(ctx, oldSub, newSub))
        {
            hlrCommand = HLRConstants.PRV_CRM_TYPE_ACTIVATE;
        }
        else if (needsDromantHlr(ctx, oldSub, newSub))
        {
            hlrCommand = HLRConstants.PRV_CMD_TYPE_DORMANT;
        }
        else if (needsDromantToActive(ctx, oldSub, newSub))
        {
            hlrCommand = HLRConstants.PRV_CMD_TYPE_DORMANTTOACTIVE;
        }
        else if (needsDunningHlr(ctx, oldSub, newSub))
        {
            result = getDunningHlrCmd(ctx, oldSub, newSub);
        }

        if (hlrCommand != null && hlrCommand.length() > 0)
        {
            result = HlrSupport.findCommand(ctx, safeSub, hlrCommand);
        }
        
        return result;
    }

    private boolean needsLockHLR(Context ctx, Subscriber oldSub, Subscriber newSub)
    {
        boolean needs = false;
        if (oldSub == null)
        {
            needs = false;
        }
        else
        {
            //ONLY for prepaid
            if ((newSub.getSubscriberType() == SubscriberTypeEnum.PREPAID &&
                    EnumStateSupportHelper.get(ctx).stateEquals(newSub, SubscriberStateEnum.LOCKED))
                && !EnumStateSupportHelper.get(ctx).isLeavingState(oldSub, newSub, SubscriberStateEnum.PENDING))
            {
                needs = true;
            }
        }
        return needs;
    }
    
    private boolean isSendHlrForUnProvisionAuxServices(final Context ctx,
                                                       final Subscriber oldSub,
                                                       final Subscriber newSub)
    {
        if (newSub != null && newSub.isPostpaid())
        {
            if( EnumStateSupportHelper.get(ctx).isTransition(
                    oldSub, newSub, 
                    SubscriberStateEnum.ACTIVE, SubscriberStateEnum.SUSPENDED) )
            {
                return true;
            }
        }

        return false;
    }

    private boolean isSendHlrForProvisionAuxServices(final Context ctx,
                                                     final Subscriber oldSub,
                                                     final Subscriber newSub)
    {
        if (newSub != null && newSub.isPostpaid())
        {
            if( EnumStateSupportHelper.get(ctx).isTransition(
                    oldSub, newSub, 
                    SubscriberStateEnum.SUSPENDED, SubscriberStateEnum.ACTIVE) )
            {
                return true;
            }
        }

        return false;
    }

    /**
     *
     */
    public void onChangeMsisdn(final Context ctx, final Subscriber oldSub, final Subscriber newSub, final boolean inAdvance)
    throws HomeException, AgentException
{
    new DebugLogMsg(this, "SubscriberProvisionHlrGatewayHome.store(ctx,sub).onChangeMsisdn(ctx,oldSub)", null).log(ctx);
    //removeSubscriberFromHLR(ctx, oldSub, newSub);

    // because the imsi is the same, it will change the existing msisdn to the new one.
    final String hlrCommand = HLRConstants.PRV_CMD_TYPE_MSISDN_CHANGE;

    if (hlrCommand != null && hlrCommand.length() > 0)
    {
        ProvisionCommand command = HlrSupport.findCommand(ctx, newSub, hlrCommand);
        if (command!=null && command.isInAdvanceCmd()==inAdvance)
        {
            setProvisionCommand(ctx, command);
            updateHLRForSubscriber(ctx, newSub, hlrCommand);
        }
    }
    else
    {
        new MajorLogMsg(this, "Cannot find changeMsisdn hlr command", null).log(ctx);
    }
}


/* (non-Javadoc)
 * @see com.redknee.app.crm.subscriber.provision.ISubscriberProvision#onChangePackage(com.redknee.framework.xhome.context.Context, com.redknee.app.crm.bean.Subscriber, com.redknee.app.crm.bean.Subscriber)
 */
public void onChangePackage(final Context ctx, final Subscriber oldSub,
        final Subscriber newSub, final boolean inAdvance) throws HomeException
{
    new DebugLogMsg(this, "SubscriberProvisionHlrGatewayHome.store(ctx,sub).onChangePackage(ctx,oldSub,newSub)", null).log(ctx);
    // TODO This is not a good design to put the old imsi to the ctx.
    //Due to the time, I have no choice. We need to refactor later.
    ctx.put(HLRConstants.HLR_PARAMKEY_OLD_IMSI_KEY, oldSub.getIMSI());
    final String hlrCommand = HLRConstants.PRV_CMR_TYPE_PACKAGEUPDATE;
    if (hlrCommand != null && hlrCommand.length() > 0)
    {
        ProvisionCommand command = HlrSupport.findCommand(ctx, newSub, hlrCommand);
        if (command!=null && command.isInAdvanceCmd()==inAdvance)
        {
            setProvisionCommand(ctx, command);
            updateHLRForSubscriber(ctx, newSub, hlrCommand);
        }
    }
    else
    {
        new MajorLogMsg(this, "Cannot find changePackage hlr command", null).log(ctx);
    }

}

/**
 * TODO: in Ibis v5.0, conversion hlr is sent after services provisioning
 * but here we change the sequence, will that causing problem
 * @param ctx
 * @param oldSub
 * @param newSub
 * @throws HomeException
 */
public void onConversion(final Context ctx, final Subscriber oldSub, final Subscriber newSub, final boolean inAdvance) throws HomeException
{
    new DebugLogMsg(this, "SubscriberProvisionHlrGatewayHome.store(ctx,sub).onConversion(ctx,newSub)", null).log(ctx);
    String hlrCommand = null;

    hlrCommand = HLRConstants.PRV_CMD_TYPE_CONVERSION;
    if (hlrCommand != null && hlrCommand.length() > 0)
    {
        ProvisionCommand command = HlrSupport.findCommand(ctx, newSub, hlrCommand);
        if (command!=null && command.isInAdvanceCmd()==inAdvance)
        {
            setProvisionCommand(ctx, command);
            updateHLRForSubscriber(ctx, newSub, hlrCommand);
        }
    }
    else
    {
        new MajorLogMsg(this, "Cannot find conversion hlr command", null).log(ctx);
    }

}

    private void updateHLRForSubscriber(
            final Context ctx,
            final Subscriber sub,
            final ProvisionCommand cmdId)
        throws ProvisionAgentException
    {
        final String message = "attempt to send an HLR Command with Command-ID[" + String.valueOf(cmdId)
                + "] for Subscriber[ id (" + sub.getId() + ") , msisdn (" + sub.getMSISDN() + ")] ";
        new InfoLogMsg(this, message, null).log(ctx);
        HlrSupport.updateHlr(ctx, sub, cmdId);
    }

      /**
     * Update HLR for the given subscriber.
     *
     * @param ctx The operating context.
     * @param ctx The given subscriber.
     */
    private void updateHLRForSubscriber(
        final Context ctx,
        final Subscriber sub,
        final String cmdId)
        throws HomeException
    {
        try
        {
            HlrSupport.updateHlr(ctx, sub, cmdId);
        }
        catch (ProvisionAgentException e)
        {
            SubscriberProvisionResultCode.setProvisionHlrResultCode(ctx, -1);
            throw new ProvisioningHomeException(e.getMessage() + " (Command = " + cmdId + ")", -1, Common.OM_HLR_PROV_ERROR, e);
        }
    }

    /**
     * We need to create HLR profile when subscriber is created.
     * @param ctx the operating context
     * @param oldSub old subscriber object
     * @param newSub new subscriber object
     * @return true if HLR profile creation is necesary
     */
    boolean needsCreateHlr(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
    {
        // we need to send create command when creating the subscriber
        // we need to send create command when re-activating the subscriber or we are recovering from an error
        if( oldSub == null
                || EnumStateSupportHelper.get(ctx).isTransition(
                        oldSub, newSub, 
                        SubscriberStateEnum.PENDING, 
                        SubscriberStateEnum.ACTIVE) 
                || EnumStateSupportHelper.get(ctx).isLeavingState(oldSub, newSub, 
                        SubscriberStateEnum.INACTIVE))
        {
            return true;
        }

        return false;
    }

    /***
     * Hybrid prepaid follow mostly postpaid state model, except dunned states
     * but it will never go to dunned states
     * @param ctx
     * @param oldSub
     * @param newSub
     * @return
     */
    boolean needsActiveHlr(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
    {
        boolean needs = false;
        if (oldSub == null)
        {
            // NO need to send the active command when we create a new sub.
            needs = false;
        }
        else if (newSub == null)
        {
            // Subscriber being removed, no need to send command
            needs = false;
        }
        else if (oldSub.getState() != newSub.getState())
        {
            if (newSub.isSameSubscriberType(oldSub))
            {
                if (oldSub.isPostpaid())
                {
                    if (EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub,
                                Arrays.asList(
                                    SubscriberStateEnum.SUSPENDED,
                                    SubscriberStateEnum.NON_PAYMENT_WARN,
                                    SubscriberStateEnum.NON_PAYMENT_SUSPENDED,
                                    SubscriberStateEnum.IN_COLLECTION,
                                    SubscriberStateEnum.IN_ARREARS
                                ),
                                Arrays.asList(
                                    SubscriberStateEnum.ACTIVE,
                                    SubscriberStateEnum.PROMISE_TO_PAY
                                )))
                    {
                        needs = true;
                    }
                }

                //if(oldSub.isTruePrepaid(ctx))
                else
                {
                    if (EnumStateSupportHelper.get(ctx).isTransition(
                            oldSub, newSub,
                            SubscriberStateEnum.LOCKED,
                            SubscriberStateEnum.ACTIVE) ||
                        EnumStateSupportHelper.get(ctx).isLeavingState(oldSub, newSub, 
                            SubscriberStateEnum.INACTIVE))
                    {
                        needs = true;
                    }
                }
            }
        }
        return needs;

    }

    /**
     * Hybrid prepaid follows postpaid
     * @param ctx
     * @param oldSub
     * @param newSub
     * @return
     */
    boolean needsSuspendHlr(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
    {
        boolean needs = false;
        if (oldSub == null)
        {
            needs = false;
        }
        else if ((newSub != null) && (newSub.isPrepaid()))
        {
            return false;
        }
        else
        {
            //postpaid or hybrid prepaid
            if (EnumStateSupportHelper.get(ctx).stateEquals(newSub, SubscriberStateEnum.SUSPENDED)
                && !EnumStateSupportHelper.get(ctx).isLeavingState(oldSub, newSub, SubscriberStateEnum.PENDING))
            {
                needs = true;
            }
        }
        return needs;
    }

    /**
     * Note that for the fix to 5102025620, as request from HLD, when post-->pre conversion
     * We will first deactivate postpaid sub, instead of send out inactive cmd, we actually need
     * to send out conversion hlr during the state change
     * @param ctx
     * @param oldSub
     * @param newSub
     * @return
     */
     public boolean needsInactiveHlr(Context ctx, Subscriber oldSub, Subscriber newSub)
     {
		if ( ctx.get(Common.POSTPAID_PREPAID_CONVERSION_SUBCRIBER) != null)
		{
            return false;   
		} 
		
		//when create sub.
		if ( oldSub == null)
		{
            return false;   
		} 
				
		return EnumStateSupportHelper.get(ctx).isEnteringState(oldSub, newSub, SubscriberStateEnum.INACTIVE);
     }
     

    /**
     * @see needsInactiveHlr
     * @param ctx
     * @param oldSub
     * @param newSub
     * @return
     */
    boolean needsPostToPrepaidConversionHlr(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
    {
        boolean needs = false;
        if (oldSub == null)
        {
            needs = false;
        }
        else
        {
            if (EnumStateSupportHelper.get(ctx).isEnteringState(oldSub, newSub, SubscriberStateEnum.INACTIVE)
                    && ctx.get(Common.POSTPAID_PREPAID_CONVERSION_SUBCRIBER) != null)
            {
                needs = true;
            }
        }
        return needs;
    }

    boolean needsDunningHlr(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
    {
        boolean needs = false;

        if (EnumStateSupportHelper.get(ctx).isEnteringState(oldSub, newSub, 
                SubscriberStateEnum.NON_PAYMENT_WARN,
                SubscriberStateEnum.NON_PAYMENT_SUSPENDED,
                SubscriberStateEnum.IN_ARREARS))
        {
            needs = true;
        }
        
        return needs;
    }


    boolean needsDromantHlr(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
    {
        return (EnumStateSupportHelper.get(ctx).isEnteringState(oldSub, newSub, SubscriberStateEnum.DORMANT));
    }


    boolean needsDromantToActive(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
    {
        return (EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, SubscriberStateEnum.DORMANT, SubscriberStateEnum.ACTIVE));
    }
    
    ProvisionCommand getDunningHlrCmd(final Context ctx,
                                      final Subscriber oldSub,
                                      final Subscriber newSub)
        throws HomeException
    {
        ProvisionCommand cmd = null;
        final Account account = (Account) ctx.get(Lookup.ACCOUNT);
        final CreditCategory creditCategory =
            CreditCategorySupport.findCreditCategory(ctx, account.getCreditCategory());
        final CRMSpid spid =
            (CRMSpid) ReportUtilities.findByPrimaryKey(ctx_, CRMSpidHome.class, Integer.valueOf(oldSub.getSpid()));

        if (newSub != null)
        {
            final int state = newSub.getState().getIndex();

            switch(state)
            {
            case SubscriberStateEnum.NON_PAYMENT_WARN_INDEX:
                cmd = (creditCategory.getDunningConfiguration() == DunningConfigurationEnum.SERVICE_PROVIDER) ? spid.getWarningAction() : creditCategory.getWarningAction();
                break;
            case SubscriberStateEnum.NON_PAYMENT_SUSPENDED_INDEX:
                cmd = (creditCategory.getDunningConfiguration() == DunningConfigurationEnum.SERVICE_PROVIDER) ? spid.getDunningAction() : creditCategory.getDunningAction();
                break;
            case SubscriberStateEnum.IN_ARREARS_INDEX:
                cmd = (creditCategory.getDunningConfiguration() == DunningConfigurationEnum.SERVICE_PROVIDER) ? spid.getInArrearsAction() : creditCategory.getInArrearsAction();
                break;
            default:
                break;
            }
        }
        return cmd;
    }

    /**
     * @param sub
     * @param ctx
     * @throws HomeException
     * @throws AgentException
     */
    public static void removeSubscriberFromHLR(final Context ctx, final Subscriber sub, final Subscriber referenceSub)
        throws HomeException, AgentException
    {
        final String cmdId = HLRConstants.PRV_CMD_TYPE_DEACTIVE;
        final ProvisionCommand provCmd =  HlrSupport.findCommand(ctx, sub,  cmdId);

        final HLRRemoveSubAgent agent = new HLRRemoveSubAgent();

        final Context subCtx = ctx.createSubContext();
        subCtx.put(Subscriber.class, sub);
        subCtx.put(ProvisionCommand.class, provCmd);

        agent.execute(subCtx);

        setMsisdnRemovedFromHlrGateway(ctx, sub, true);
    }

    /**
     * @param ctx
     * @param e
     * @param caller
     * @return result code
     */
    public static int getHlrResponsibleCode(final Context ctx, final Exception e, final Object caller)
    {
        int hlrCode = 301;
        try
        {
            if (e instanceof AgentException)
            {
                String responseCodeStr = "";

                //should contain ==>  RESPONSECODE:1111 ]
                final String errorString = e.getMessage();
                if (errorString.indexOf("RESPONSECODEFROMHLR:") != 0)
                {
                    final int startPos = errorString.indexOf("RESPONSECODEFROMHLR:") + 20;
                    final char endSign = ']';
                    final int endSignPos = errorString.lastIndexOf(endSign);

                    responseCodeStr = errorString.substring(startPos, endSignPos);
                    hlrCode = Integer.parseInt(responseCodeStr.trim());
                }
            }
        }
        catch (Exception exp)
        {
            new MinorLogMsg(caller, "Failed to parse hlr error, hlrError" + e + ", parsing error:" + exp, exp).log(ctx);
        }


        return hlrCode;
    }

    /**
     *If a msisdn is removed from hlr gateway, we should not unprovision services HLR individually
     * so we pass around this context key, and this key will be dropped once this subContext discarded.
     */
    public static final String HLR_GATEWAY_SUB_JUSTREMOVED = "SubscriberProvisionHlrGatewayAgent.hlrRemove.sub.";
    
    public static ProvisionCommand getProvisionCommand(final Context ctx)
    {
        if(ctx.get(ProvisionCommand.class) instanceof ProvisionCommand)
            return (ProvisionCommand)ctx.get(ProvisionCommand.class);
        
        return null;
    }
    
    public static String getProvisionCommandName(final Context ctx)
    {
        if(ctx.get(ProvisionCommand.class) instanceof ProvisionCommand)
            return ((ProvisionCommand)ctx.get(ProvisionCommand.class)).getName();
        
        return "null";
    }
    
    public static void setProvisionCommand(final Context ctx, ProvisionCommand cmd)
    {
        ctx.put(ProvisionCommand.class, cmd);
    }
    
    /**
     * Checks if the ProvisionCommand instance is configured to delete MSISDN from 
     * HLR. This is 'false' usually - 'true' only for few deployments like Dory.
     * @return
     */
    public static boolean isIntentToRemoveMsisnFromHlrGateway(final Context ctx)
    {
        ProvisionCommand cmd = getProvisionCommand(ctx);
        return cmd!=null && cmd.getRemovesMsisdnFromHlr();
    }

    private static Object getContextKeyMsisdnRemoved(final Subscriber sub)
    {
        return HLR_GATEWAY_SUB_JUSTREMOVED + sub.getMSISDN();
    }
    public static boolean hasMsisdnRemovedFromHlrGateway(final Context ctx, final Subscriber sub)
    {
        return ctx.getBoolean(getContextKeyMsisdnRemoved(sub), false);
    }
    public static void setMsisdnRemovedFromHlrGateway(final Context ctx, final Subscriber sub, final boolean value)
    {
        ctx.put(getContextKeyMsisdnRemoved(sub), value);
    }
}
