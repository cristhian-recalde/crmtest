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

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.Deposit;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.util.format.ThreadLocalSimpleDateFormat;
import com.trilogy.framework.xhome.visitor.FunctionVisitor;
import com.trilogy.framework.xhome.visitor.ListBuildingVisitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xhome.xenum.AbstractEnum;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.api.queryexecutor.subscription.SubscriptionQueryExecutors;
import com.trilogy.app.crm.bean.AbstractNote;
import com.trilogy.app.crm.bean.Note;
import com.trilogy.app.crm.bean.NoteHome;
import com.trilogy.app.crm.bean.NoteXInfo;
import com.trilogy.app.crm.bean.PortingTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.poller.agent.VRAFraudERAgent;
import com.trilogy.app.crm.report.ReportUtilities;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.util.snippet.log.Logger;


/**
 * Support class for subscriber note.
 *
 * @author larry.xia@redknee.com
 */
public final class SubscriberNoteSupport
{

    /**
     * Log message prefix for failing to save subscriber note.
     */
    private static final String FAIL_TO_SAVE_SUBSCRIBER_NOTE = "Fail to save Note for Subscriber [Subscriber=";
    /**
     * Log message for finishing the note creation.
     */
    private static final String FINISH_CREATING_SUBSCRIBER_NOTE = "FINISH - Creating Subscriber Note";
    /**
     * Log message for starting the note creation.
     */
    private static final String START_CREATING_SUBSCRIBER_NOTE = "START- Creating Subscriber Note";
    /**
     * Displayed field name when subscriber fraud state is updated.
     */
    private static final String SUBSCRIBER_FRAUD_STATE_UPDATE = "**Subscriber Fraud State Update**";
    /**
     * Displayed field name when subscriber PIN is being manually unlocked.
     */
    private static final String MANUAL_PIN_UNLOCK = "Manual PIN unlock";
    /**
     * Displayed field name when subscriber PIN is being manually locked.
     */
    private static final String MANUAL_PIN_LOCK = "Manual PIN lock";
    /**
     * Displayed state name when subscriber PIN is locked.
     */
    private static final String PIN_LOCKED = "Locked";
    /**
     * Displayed state name when subscriber PIN is unlocked.
     */
    private static final String PIN_UNLOCKED = "Unlocked";


    /**
     * Creates a new <code>SubscriberNoteSupport</code> instance. This method is made
     * private to prevent instantiation of utility class.
     */
    private SubscriberNoteSupport()
    {
        // empty
    }


    /**
     * Creates a subscriber creation note.
     *
     * @param ctx
     *            The operating context.
     * @param newSub
     *            The subscriber this note is set for.
     * @param source
     *            The caller of this note creation.
     */
    public static void createSubscriberCreationNote(final Context ctx, final Subscriber newSub, final Object source)
    {
        // Set the Note for this transaction.
        final StringBuilder sb = new StringBuilder();
        sb.append("Subscriber created in ");
        sb.append(newSub.getState().getDescription());
        sb.append(" state\n");
        
        try
        {
            final PricePlanVersion newPricePlan = newSub.getPricePlan(ctx);
                    
            // services and bundles are a little complicated to use XInfo
            final Set newServices = newPricePlan.getServices(ctx);
            if (newServices != null && !newServices.isEmpty())
            {
                // The label for Services in the Subscriber model file is purposely blank. Don't use PropertyXInfo.
                appendNoteContent("Services", "[]", newServices.toString(), sb);
            }
    
            final Map<Long, BundleFee> newBundles = SubscriberBundleSupport.getSubscribedBundles(ctx, newSub);
            if (newBundles != null && !newBundles.isEmpty())
            {
            	int bundleCounter = 0;
            	int newBundleSize = newBundles.keySet().size();
            	StringBuilder sbNewBundles = new StringBuilder();
            	try {
            		sbNewBundles.append("[");
            		for(Long bundleId : newBundles.keySet())
            		{
            			sbNewBundles.append(bundleId + "-" + newBundles.get(bundleId).getBundleProfile(ctx, newSub.getSpid()).getName());
            			bundleCounter++;
            			if(bundleCounter < newBundleSize) 
            			{
            				sbNewBundles.append(", ");
            			}	
            		}
            		sbNewBundles.append("]");
            	} catch (Exception e) {
            		LogSupport.minor(ctx, e, "Unable to get the bundle profile details.");
            	}
            	appendNoteContent("Bundles", "[]", sbNewBundles.toString(), sb);
            }	
        }
        catch (HomeException e)
        {
            Logger.minor(ctx, SubscriberNoteSupport.class, "Unable to get price plan for subscription "
                    + newSub.getId(), e);
        }

        final List<SubscriberAuxiliaryService> newAuxServices = newSub.getAuxiliaryServices(ctx);
        final ListBuildingVisitor newAuxSrvIDs = new ListBuildingVisitor();

        try
        {
            Visitors.forEach(ctx, newAuxServices,
                    new FunctionVisitor(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, newAuxSrvIDs));
        }
        catch (AgentException e)
        {
            Logger.minor(ctx, SubscriberNoteSupport.class, "Unable to extract IDs from subscription "
                    + newSub.getId() + " auxiliary services", e);
        }

        if (newAuxSrvIDs != null && !newAuxSrvIDs.isEmpty())
        {
            // The label for Auxiliary Services in the Subscriber model is purposely blank. Don't use PropertyXInfo.
            appendNoteContent("Auxiliary Services", "[]", new HashSet(newAuxSrvIDs).toString(), sb);
        }
        
        
        if(newSub.getGroupScreeningTemplateId() != Subscriber.DEFAULT_GROUPSCREENINGTEMPLATEID)
        {
            sb.append(" Associated GroupScreeningTemplateId : " + newSub.getGroupScreeningTemplateId());
        }
        
        createSubscriberNote(ctx, source, getCsrAgent(ctx, null), newSub.getId(), SystemNoteTypeEnum.EVENTS,
            SystemNoteSubTypeEnum.SUBACTIVE, sb);
    }


    /**
     * Create a subscriber update note.
     *
     * @param ctx
     *            The operating context.
     * @param oldSub
     *            The old subscriber.
     * @param newSub
     *            The new subscriber.
     * @param oldPricePlan
     *            The old subscriber price plan.
     * @param newPricePlan
     *            The new subscribe price plan.
     * @param crmResult
     *            Provisioning result.
     * @param source
     *            Caller of this note creation.
     */
    public static void createSubscriberUpdateNote(final Context ctx, final Subscriber oldSub, final Subscriber newSub,
        final PricePlanVersion oldPricePlan, final PricePlanVersion newPricePlan, final boolean crmResult,
        final Object source)
    {
        final StringBuilder noteBuff = new StringBuilder();
        // Create the Comment with the fields that have changed
        if (crmResult)
        {
            noteBuff.append("Subscriber updating succeeded\n");
        }
        else
        {
            noteBuff.append("Subscriber updating failed\n");
        }

        final PropertyInfo[] properties = new PropertyInfo[]
        {
            SubscriberXInfo.MSISDN, 
            SubscriberXInfo.FAX_MSISDN, 
            SubscriberXInfo.DATA_MSISDN, 
            SubscriberXInfo.IMSI,
            SubscriberXInfo.SPID, 
            SubscriberXInfo.BAN, 
            SubscriberXInfo.PRICE_PLAN, 
            SubscriberXInfo.PRICE_PLAN_VERSION,
            SubscriberXInfo.SECONDARY_PRICE_PLAN, 
            SubscriberXInfo.STATE, 
            SubscriberXInfo.SUBSCRIBER_TYPE,
            SubscriberXInfo.HLR_ID, 
            SubscriberXInfo.PACKAGE_ID,
            SubscriberXInfo.BILLING_OPTION, 
            SubscriberXInfo.CHARGE_PPSM, 
            SubscriberXInfo.EXPIRY_DATE,
            SubscriberXInfo.PRE_EXPIRY_SMS_SENT, 
            SubscriberXInfo.FIRST_INIT_CLTC,
            SubscriberXInfo.GROUP_SCREENING_TEMPLATE_ID
        };

        final PropertyInfo[] currencyProperties = new PropertyInfo[]
        {
            SubscriberXInfo.DEPOSIT,
            SubscriberXInfo.CREDIT_LIMIT,
            SubscriberXInfo.INITIAL_BALANCE,
            SubscriberXInfo.MAX_BALANCE, 
            SubscriberXInfo.MAX_RECHARGE, 
            SubscriberXInfo.REACTIVATION_FEE,
        };
        for (final PropertyInfo property : properties)
        {
            appendSubscriberPropertyChange(ctx, property, oldSub, newSub, noteBuff, false);
        }
        
        if((Boolean)ctx.get(SubscriptionQueryExecutors.SubscriptionUpdateWithStateTransitionQueryExecutor.PORTOUT_AND_INACTIVE_STATE,false))
        {		
        		noteBuff.append("due to subscriber port-out.\n");
        }
	

        for (final PropertyInfo property : currencyProperties)
        {
            appendSubscriberPropertyChange(ctx, property, oldSub, newSub, noteBuff, true);
        }
        // services and bundles are a little complicated to use XInfo
        final Set newServices = newPricePlan.getServices(ctx);
        final Set oldServices = oldPricePlan.getServices(ctx);
        if (newServices != null && !newServices.equals(oldServices))
        {
            // The label for Services in the Subscriber model file is purposely blank. Don't use PropertyXInfo.
            appendNoteContent("Services", oldServices.toString(), newServices.toString(), noteBuff);
        }

        final Map<Long, BundleFee> newBundles = SubscriberBundleSupport.getSubscribedBundles(ctx, newSub);
        final Map<Long, BundleFee> oldBundles = SubscriberBundleSupport.getSubscribedBundles(ctx, oldSub);
        final Set newBundlesIds = newBundles.keySet();
        final Set oldBundlesIds = oldBundles.keySet();
        if (newBundlesIds != null && !newBundlesIds.equals(oldBundlesIds))
        {
        	int bundleCounter = 0;
        	int bundleSize = 0;
        	StringBuilder sbNewBundles = new StringBuilder();
        	StringBuilder sbOldBundles = new StringBuilder();
        	try {
        		//-- prepare the string of old bundle's id and its name
        		bundleSize = newBundles.keySet().size();
        		sbOldBundles.append("[");
 	            for(Long bundleId : oldBundles.keySet())
 	            {
 	            	sbOldBundles.append(bundleId +"-"+ oldBundles.get(bundleId).getBundleProfile(ctx, newSub.getSpid()).getName());
 	            	bundleCounter++;
 	            	if(bundleCounter < bundleSize) {
 	            		sbOldBundles.append(", ");
 	            	}
 	            }
 	            sbOldBundles.append("]");
 	            
 	           //-- prepare the string of new bundle's id and its name
 	            bundleCounter = 0;
 	            bundleSize = newBundles.keySet().size();
        		sbNewBundles.append("[");
	            for(Long bundleId : newBundles.keySet())
	            {
	            	sbNewBundles.append(bundleId +"-"+ newBundles.get(bundleId).getBundleProfile(ctx, newSub.getSpid()).getName());
	            	bundleCounter++;
	            	if(bundleCounter < bundleSize) {
	            		sbNewBundles.append(", ");
	            	}
	            }
	            sbNewBundles.append("]");
	           
            } catch (Exception e) {
            	LogSupport.minor(ctx, e, "Unable to get the bundle profile details.");
			}
        	// The label for Bundles in the Subscriber model file is purposely blank.  Don't use PropertyXInfo.
        	boolean unprovisionedMessage = ctx.getBoolean(PrepaidEntitySuspensionPreventionHome.UNPROV_NOTE_MSG, false);
            appendNoteContent("Bundles", sbOldBundles.toString(), sbNewBundles.toString() + (unprovisionedMessage ? "Unprovisioned due to insufficient funds - Restrict Provisioning mode" : "") , noteBuff);
        }

        final List<SubscriberAuxiliaryService> newAuxServices = newSub.getAuxiliaryServices(ctx);
        final List<SubscriberAuxiliaryService> oldAuxServices = oldSub.getAuxiliaryServices(ctx);
        final ListBuildingVisitor newAuxSrvIDs = new ListBuildingVisitor();
        final ListBuildingVisitor oldAuxSrvIDs = new ListBuildingVisitor();

        try
        {
            Visitors.forEach(ctx, newAuxServices,
                    new FunctionVisitor(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, newAuxSrvIDs));
            Visitors.forEach(ctx, oldAuxServices,
                    new FunctionVisitor(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, oldAuxSrvIDs));
        }
        catch (AgentException e)
        {
            Logger.minor(ctx, SubscriberNoteSupport.class, "Unable to extract IDs from subscription "
                    + newSub.getId() + " auxiliary services", e);
        }

        if (newAuxSrvIDs != null && !newAuxSrvIDs.equals(oldAuxSrvIDs))
        {
            // The label for Auxiliary Services in the Subscriber model is purposely blank. Don't use PropertyXInfo.
            appendNoteContent("Auxiliary Services", new HashSet(oldAuxSrvIDs).toString(), new HashSet(newAuxSrvIDs).toString(), noteBuff);
        }

        if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.RK_DEV_LICENSE))
        {
            /*
             * The following section is for debugging information until all bundle switch bugs are found
             */
            final Set newSelected = newSub.getBundles().keySet();
            final Set oldSelected = oldSub.getBundles().keySet();
            if (newSelected != null && !newSelected.equals(oldSelected))
            {
                appendNoteContent("[DEV] selected bundles", oldSelected.toString(), newSelected.toString(), noteBuff);
            }
        }

        if (ctx.getBoolean(VRAFraudERAgent.SUSPEND_DUE_TO_FRAUD_PROFILE))
        {
            noteBuff.append(newSub.getState().getDescription(ctx));
            noteBuff.append(" due to Fraud Profile\n");
        }

        createSubscriberNote(ctx, source, getCsrAgent(ctx, newSub), newSub.getId(), SystemNoteTypeEnum.EVENTS,
            SystemNoteSubTypeEnum.SUBUPDATE, noteBuff);
    }


    
    /**
     * Append changes to subscriber property.
     *
     * @param property
     *            The property being tested.
     * @param oldSub
     *            The old subscriber.
     * @param newSub
     *            The new subscriber.
     * @param noteBuff
     *            Note buffer.
     * @param formatCurrency
     */
    private static void appendSubscriberPropertyChange(final Context ctx, final PropertyInfo property, final Subscriber oldSub,
        final Subscriber newSub, final StringBuilder noteBuff, final boolean formatCurrency)
    {
        String oldValue = null;
        String newValue = null;

        if (formatCurrency)
        {
            Currency oldCurrency = null;
            Currency defaultCurrency = (Currency) ctx.get(Currency.class, Currency.DEFAULT);
            
            if (oldSub!=null && Number.class.isAssignableFrom(property.getType()))
            {
                oldCurrency = ReportUtilities.getCurrency(ctx, oldSub.getCurrency(ctx));
                if (oldCurrency == null)
                {
                    oldCurrency = defaultCurrency;
                }

                oldValue = oldCurrency.formatValue(((Number)property.get(oldSub)).longValue());
            }

            if (newSub!=null && Number.class.isAssignableFrom(property.getType()))
            {
                Currency newCurrency = ReportUtilities.getCurrency(ctx, newSub.getCurrency(ctx));
                if (newCurrency == null)
                {
                    newCurrency = defaultCurrency;
                }

                newValue = newCurrency.formatValue(((Number)property.get(newSub)).longValue());
            }
        }
        
        if (oldValue == null)
        {
            oldValue = property.toString(oldSub);
            if (AbstractEnum.class.isAssignableFrom(property.getType()))
            {
                oldValue = ((AbstractEnum) property.get(oldSub)).getDescription() + " (" + oldValue + ")";
            }
        }
        
        if (newValue == null)
        {
            newValue = property.toString(newSub);
            if (AbstractEnum.class.isAssignableFrom(property.getType()))
            {
                newValue = ((AbstractEnum) property.get(newSub)).getDescription() + " (" + newValue + ")";
            }
        }

        appendNoteContent(property, oldValue, newValue, noteBuff);
    }


    /**
     * Returns the current CSR agent.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being updated.
     * @return The current CSR agent.
     */
    public static String getCsrAgent(final Context ctx, final Subscriber sub)
    {
        String csr = null;
        if (sub != null)
        {
            csr = sub.getUser();
        }
        if (csr == null || csr.trim().length() == 0)
        {
            csr = SystemSupport.getAgent(ctx);
        }
        return csr;
    }


    /**
     * Append field update to note content.
     *
     * @param fieldChanged
     *            Field updated.
     * @param oldVal
     *            Old value of the field.
     * @param newVal
     *            New value of the field.
     * @param noteBuff
     *            Buffer to append to.
     */
    public static void appendNoteContent(final PropertyInfo fieldChanged, final String oldVal, final String newVal,
        final StringBuilder noteBuff)
    {
        appendNoteContent(fieldChanged.getLabel(), oldVal, newVal, noteBuff);
    }


    /**
     * Append field update to note content.
     *
     * @param fieldChanged
     *            Name of the field updated.
     * @param oldVal
     *            Old value of the field.
     * @param newVal
     *            New value of the field.
     * @param noteBuff
     *            Buffer to append to.
     */
    public static void appendNoteContent(final String fieldChanged, final String oldVal, final String newVal,
        final StringBuilder noteBuff)
    {
        final String oldValue;
        if (oldVal == null)
        {
            oldValue = "";
        }
        else
        {
            oldValue = oldVal;
        }

        final String newValue;
        if (newVal == null)
        {
            newValue = "";
        }
        else
        {
            newValue = newVal;
        }

        if (!newValue.equals(oldValue))
        {
            noteBuff.append("Subscriber ");
            noteBuff.append(fieldChanged);
            noteBuff.append(" : ");
            noteBuff.append(oldValue);
            noteBuff.append("->");
            noteBuff.append(newValue);
            noteBuff.append("\n");
        }

    }


    /**
     * Remove entries in Note table for this Subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            Subscriber being removed.
     */
    public static void removeSubscriberNotes(final Context ctx, final Subscriber sub)
    {
        final Home noteHome = (Home) ctx.get(NoteHome.class);
        final String id = sub.getId();

        try
        {

            noteHome.where(ctx, new EQ(NoteXInfo.ID_IDENTIFIER, id)).removeAll(ctx);
        }
        catch (final HomeException e)
        {
            new MinorLogMsg(SubscriberNoteSupport.class,
                "Fail to remove Notes for Subscriber [id=" + sub.getId() + "]", e).log(ctx);
        }
    }


    /**
     * Create subscriber single field update note.
     *
     * @param ctx
     *            The operating context.
     * @param subscriberId
     *            The subscriber ID.
     * @param source
     *            The caller of this note creation.
     * @param subType
     *            Note sub-type.
     * @param field
     *            Field to be displayed in the note.
     * @param oldValue
     *            Old value of the field.
     * @param newValue
     *            New value of the field.
     */
    private static void createSubscriberPINNote(final Context ctx, final String subscriberId, final Object source,
        final SystemNoteSubTypeEnum subType, final String field, final String oldValue, final String newValue)
    {
        final StringBuilder sb = new StringBuilder();
        SubscriberNoteSupport.appendNoteContent(field, oldValue, newValue, sb);
        createSubscriberNote(ctx, source, getCsrAgent(ctx, null), subscriberId, SystemNoteTypeEnum.EVENTS, subType, sb);
    }


    /**
     * Create subscriber manual PIN lock note.
     *
     * @param ctx
     *            The operating context.
     * @param subscriberId
     *            The subscriber ID.
     * @param source
     *            The caller of this note creation.
     */
    public static void createSubscriberPINLockNote(final Context ctx, final String subscriberId, final Object source)
    {
        createSubscriberPINNote(ctx, subscriberId, source, SystemNoteSubTypeEnum.PINLOCK, MANUAL_PIN_LOCK,
            PIN_UNLOCKED, PIN_LOCKED);
    }


    /**
     * Create subscriber manual PIN unlock note.
     *
     * @param ctx
     *            The operating context.
     * @param subscriberId
     *            The subscriber ID.
     * @param source
     *            The caller of this note creation.
     */
    public static void createSubscriberPINUnlockNote(final Context ctx, final String subscriberId, final Object source)
    {
        createSubscriberPINNote(ctx, subscriberId, source, SystemNoteSubTypeEnum.PINUNLOCK, MANUAL_PIN_UNLOCK,
            PIN_LOCKED, PIN_UNLOCKED);
    }


    /**
     * Create subscriber PIN reset note.
     *
     * @param ctx
     *            The operating context.
     * @param subscriberId
     *            The subscriber ID.
     * @param source
     *            The caller of this note creation.
     */
    public static void createSubscriberPINResetNote(final Context ctx, final String subscriberId, final Object source)
    {
        createSubscriberPINNote(ctx, subscriberId, source, SystemNoteSubTypeEnum.PINRESET, "PIN", "", "Reset");
    }


    /**
     * Create note for locked subscriber due to fraud, with state change.
     *
     * @param ctx
     *            The operating context.
     * @param subscriberId
     *            The subscriber ID.
     * @param stateChangeSuccess
     *            Whether the state change was successful.
     */
    public static void createFraudLockStateChangeNote(final Context ctx, final String subscriberId,
        final boolean stateChangeSuccess)
    {
        final String fromState = (String) ctx.get(VRAFraudERAgent.OLD_SUB_STATE);
        final String toState = (String) ctx.get(VRAFraudERAgent.NEW_SUB_STATE);
        createVoucherFraudNote(ctx, subscriberId, fromState, toState, true, stateChangeSuccess);
    }


    /**
     * Create note for locked subscriber due to fraud, with no state change.
     *
     * @param ctx
     *            The operating context.
     * @param subscriberId
     *            The subscriber ID.
     * @param isSuccess
     *            Whether the operation was successful.
     */
    public static void createFraudLockNoStateChangeNote(final Context ctx, final String subscriberId,
        final boolean isSuccess)
    {
        createVoucherFraudNote(ctx, subscriberId, "", "", false, isSuccess);
    }


    /**
     * Create note for unlocked subscriber from fraud, with state change.
     *
     * @param ctx
     *            The operating context.
     * @param id
     *            The subscriber ID.
     * @param isSuccess
     *            Whether the state change was successful.
     */
    public static void createFraudUnLockStateChangeNote(final Context ctx, final String id, final boolean isSuccess)
    {
        final String fromState = (String) ctx.get(VRAFraudERAgent.OLD_SUB_STATE);
        final String toState = (String) ctx.get(VRAFraudERAgent.NEW_SUB_STATE);

        createVoucherFraudNote(ctx, id, fromState, toState, true, isSuccess);
    }


    /**
     * Create note for unlocked subscriber from fraud, with no state change.
     *
     * @param ctx
     *            The operating context.
     * @param id
     *            The subscriber ID.
     * @param isSuccess
     *            Whether the operation was successful.
     */
    public static void createFraudUnLockNoStateChangeNote(final Context ctx, final String id, final boolean isSuccess)
    {
        createVoucherFraudNote(ctx, id, "", "", false, isSuccess);
    }


    /**
     * Append content of voucher fraud note to buffer.
     *
     * @param label
     *            note Label.
     * @param oldVal
     *            Old subscriber state.
     * @param newVal
     *            New subscriber state.
     * @param stateChange
     *            Whether state has changed.
     * @param isSuccess
     *            Whether the stage change was successful.
     * @param noteBuff
     *            Buffer to append to.
     */
    public static void appendVoucherFraudNoteContent(final String label, final String oldVal, final String newVal,
        final boolean stateChange, final boolean isSuccess, final StringBuilder noteBuff)
    {
        if (stateChange)
        {
            noteBuff.append(label);
            noteBuff.append('\n');
            appendNoteContent("State", oldVal, newVal, noteBuff);
            if (isSuccess)
            {
                noteBuff.append("State change Successful");
            }
            else
            {
                noteBuff.append("State change Failed");
            }
        }
        else
        {
            if (isSuccess)
            {
                noteBuff.append(label);
                noteBuff.append('\n');
                noteBuff.append("Updating Fraud profile Succeeded.");
            }
            else
            {
                noteBuff.append(label);
                noteBuff.append('\n');
                noteBuff.append("Updating Fraud profile Failed.");
            }
        }
    }


    /**
     * Create subscriber voucher fraud note.
     *
     * @param ctx
     *            The operating context.
     * @param subscriberId
     *            The subscriber ID.
     * @param oldState
     *            Old subscriber state.
     * @param newState
     *            New subscriber state.
     * @param stateChange
     *            Has state changed.
     * @param isSuccess
     *            Whether the state change was successful.
     */
    public static void createVoucherFraudNote(final Context ctx, final String subscriberId, final String oldState,
        final String newState, final boolean stateChange, final boolean isSuccess)
    {
        final StringBuilder buf = new StringBuilder();
        SubscriberNoteSupport.appendVoucherFraudNoteContent(SUBSCRIBER_FRAUD_STATE_UPDATE, oldState, newState,
            stateChange, isSuccess, buf);
        createSubscriberNote(ctx, SubscriberNoteSupport.class, getCsrAgent(ctx, null), subscriberId,
            SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.SUBUPDATE, buf);
    }


    /**
     * Creates a subscriber note when service is enabled/disabled due to payment/low
     * balance.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being updated.
     * @param serviceUpdateResult
     *            Whether service update was successful.
     * @param enable
     *            Whether the service was enabled.
     * @param noteBuff
     *            Buffer containing the note content.
     * @param source
     *            Caller of this method.
     */
    public static void createSubscriberServiceNote(final Context ctx, final Subscriber sub,
        final boolean serviceUpdateResult, final boolean enable, final StringBuilder noteBuff, final Object source)
    {
        // Added to address TT6062636079

        if (enable)
        {
            if (serviceUpdateResult)
            {
                noteBuff.append(" Enabled afer payment");
            }
            else
            {
                noteBuff.append(" Could not be Enabled after payment");
            }
        }
        else
        {
            if (serviceUpdateResult)
            {
                noteBuff.append(" Disabled due to Low Balance");
            }
            else
            {
                noteBuff.append(" Could not be Disabled");
            }
        }

        createSubscriberNote(ctx, source, getCsrAgent(ctx, null), sub.getId(), SystemNoteTypeEnum.EVENTS,
            SystemNoteSubTypeEnum.SUBUPDATE, noteBuff);
    }


    /**
     * Creates and stores a subscriber note.
     * 
     * @param context
     *            The operating context.
     * @param src
     *            Caller of the subscriber note. If none is provided, this class will be
     *            used.
     * @param agent
     *            CSR agent creating this note.
     * @param subscriberId
     *            Subscriber associated with this note.
     * @param noteType
     *            Note type.
     * @param noteSubType
     *            Note sub-type.
     * @param noteBuff
     *            Content of the note.
     */
    public static void createSubscriberNote(final Context context, final Object src, final String agent,
            final String subscriberId, final SystemNoteTypeEnum noteType, final SystemNoteSubTypeEnum noteSubType,
            final StringBuilder noteBuff)
    {
        createSubscriberNote(context, src, agent, subscriberId, noteType, noteSubType, noteBuff, false);
    }
    /**
     * Creates and stores a subscriber note.
     *
     * @param context
     *            The operating context.
     * @param src
     *            Caller of the subscriber note. If none is provided, this class will be
     *            used.
     * @param agent
     *            CSR agent creating this note.
     * @param subscriberId
     *            Subscriber associated with this note.
     * @param noteType
     *            Note type.
     * @param noteSubType
     *            Note sub-type.
     * @param noteBuff
     *            Content of the note.
     */
    public static void createSubscriberNote(final Context context, final Object src, final String agent,
        final String subscriberId, final SystemNoteTypeEnum noteType, final SystemNoteSubTypeEnum noteSubType,
        final StringBuilder noteBuff, final boolean showOnInvoice)
    {
        final Object caller;
        if (src == null)
        {
            caller = SubscriberNoteSupport.class;
        }
        else
        {
            caller = src;
        }

        // This note bean will store all the information for the note
        final Note note = new Note();

        if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(caller, START_CREATING_SUBSCRIBER_NOTE, null).log(context);
        }
        // Set the Subscriber's id in the note
        note.setIdIdentifier(subscriberId);
        // Set the User that modified the subscriber
        note.setAgent(agent);
        // Set the creation date
        note.setCreated(new Date());
        note.setType(noteType.getDescription());
        note.setSubType(noteSubType.getDescription());
        note.setShowOnInvoice(showOnInvoice);

        if (noteBuff.length() > AbstractNote.NOTE_WIDTH)
        {
            new MinorLogMsg(caller, "Note length exceeded maximum " + AbstractNote.NOTE_WIDTH + ", will be truncated: "
                + noteBuff, null).log(context);
            noteBuff.delete(AbstractNote.NOTE_WIDTH, noteBuff.length());
        }
        note.setNote(noteBuff.toString());
        final Home noteHome = (Home) context.get(NoteHome.class);
        try
        {
            noteHome.create(note);
        }
        catch (final HomeException e)
        {
            new MinorLogMsg(caller, FAIL_TO_SAVE_SUBSCRIBER_NOTE + subscriberId + "]", e).log(context);
        }
        if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(caller, FINISH_CREATING_SUBSCRIBER_NOTE, null).log(context);
        }
    }
    
    /**
     * Creates a Subscriber Note to indicate a failed Attempt to Change State.  
     * @param context
     * @param source
     * @param oldSub
     * @param newSub
     */
    public static void createFailedStateChangeNote(final Context context, final Object source, 
            final Subscriber oldSub, final Subscriber newSub)
    {
        StringBuilder buf = new StringBuilder();
        buf.append("Failed to update Subscriber State from '");
        buf.append(oldSub.getState().getDescription());  
        buf.append("' -> '");
        buf.append(newSub.getState().getDescription());  
        buf.append("' . Subscriber remains in '");
        buf.append(oldSub.getState().getDescription());  
        buf.append("' state\n");
        createSubscriberNote(context, source, getCsrAgent(context, newSub), newSub.getId(), SystemNoteTypeEnum.EVENTS,
            SystemNoteSubTypeEnum.SUBUPDATE, buf);
    }
    
    public static void createDepositRealeaseNote(final Context context, final Object source, 
            Deposit deposit, StringBuilder buf)
    {
        buf.append("\n");
        createSubscriberNote(context, source, getCsrAgent(context, null), deposit.getSubscriptionID(), SystemNoteTypeEnum.DEPOSIT,SystemNoteSubTypeEnum.DEPOSIT_RELEASE,buf);
    }
    
    /**
     * Creates a Subscriber Note to indicate subscription contract has been assigned to the subscription  
     * @param context
     * @param source
     * @param oldSub
     * @param newSub
     */
    public static void createAssignSubscriptionContractNote(final Context context, final Object source, 
            final Subscriber oldSub, final Subscriber newSub, final String contractName, final long contractId)
    {
        StringBuilder buf = new StringBuilder();
        buf.append( contractName);
        buf.append(',');
        buf.append(contractId);
        createSubscriberNote(context, source, getCsrAgent(context, newSub), newSub.getId(), SystemNoteTypeEnum.CONTRACT_UPDATES,
            SystemNoteSubTypeEnum.CONTRACT_ADD, buf,true);
    }


    /**
     * Creates a Subscriber Note to indicate subscription contract has been removed from
     * subscription
     * 
     * @param context
     * @param source
     * @param oldSub
     * @param newSub
     */
    public static void createRemoveSubscriptionContractNote(final Context context, final Object source,
            final Subscriber sub, final String contractName)
    {
        StringBuilder buf = new StringBuilder();
        buf.append( contractName);
        createSubscriberNote(context, source, getCsrAgent(context, sub), sub.getId(), SystemNoteTypeEnum.CONTRACT_UPDATES,
            SystemNoteSubTypeEnum.CONTRACT_REMOVE, buf,true);
    }


    /**
     * Creates a Subscriber Note to indicate subscription's contract bonus has been
     * applied
     * 
     * @param context
     * @param source
     * @param newSub
     */
    public static void createSubscriptionContractBonusAppliedNote(final Context context, final Object source,
            final Subscriber newSub, final String contractName)
    {
        StringBuilder buf = new StringBuilder();
        buf.append(contractName);
        createSubscriberNote(context, source, getCsrAgent(context, newSub), newSub.getId(),
                SystemNoteTypeEnum.CONTRACT_UPDATES, SystemNoteSubTypeEnum.CONTRACT_BONUS, buf, true);
    }


    /**
     * Creates a Subscriber Note to indicate subscription contract is about to expire
     * wihtin a month
     * 
     * @param context
     * @param source
     * @param newSub
     * @param expireDate
     */
    public static void createSubscriptionContractExpireWarningNote(final Context context, final Object source,
            final Subscriber newSub, final Date expiryDate, final String contractName)
    {
        StringBuilder str = new StringBuilder();
        final ThreadLocalSimpleDateFormat dateFormatter = new ThreadLocalSimpleDateFormat("yyyy/MM/dd");
        String date = dateFormatter.format(expiryDate);
        str.append(contractName);        
        str.append(',');
        str.append(date);        
        createSubscriberNote(context, source, getCsrAgent(context, newSub), newSub.getId(),
                SystemNoteTypeEnum.CONTRACT_UPDATES, SystemNoteSubTypeEnum.CONTRACT_EXPIRE_WARN, str,true);
    }
    /**
     * Creates a Subscriber Note to indicate a failed Attempt to Change State.  
     * @param context
     * @param source
     * @param oldSub
     * @param newSub
     */
    public static void createScheduledPriceplanChangeNote(final Context context, final Object source, 
            final Subscriber sub, int result, String responseString)
    {
        StringBuilder buf = new StringBuilder();
        buf.append("Scheduled Priceplan change for subscriber: [");
        buf.append(sub.getId());  
        buf.append("]. Result:[");
        buf.append(result);  
        buf.append("]. Reason:[");
        buf.append(responseString);  
        buf.append("].");
        createSubscriberNote(context, source, getCsrAgent(context, sub), sub.getId(), SystemNoteTypeEnum.EVENTS,
            SystemNoteSubTypeEnum.SUBUPDATE, buf);
    }

}
