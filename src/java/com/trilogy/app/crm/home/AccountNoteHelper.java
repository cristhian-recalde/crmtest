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
package com.trilogy.app.crm.home;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.AbstractNote;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.Note;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.support.PaymentPlanSupportHelper;
import com.trilogy.app.crm.support.TaxAuthoritySupportHelper;

/**
 * @author lxia
 */
public class AccountNoteHelper {

    static public void createAccountNote(Account newAccount, 
    		Context ctx, Object source)
    {
        Note accNote = new Note();

        accNote.setIdIdentifier(newAccount.getBAN());
        //Set the Type and Sub-Type for the note
        accNote.setType(SystemNoteTypeEnum.EVENTS.getDescription());
        accNote.setSubType(SystemNoteSubTypeEnum.ACCACTIVE.getDescription());

        accNote.setCreated(new Date());
        accNote.setLastModified(new Date());
        accNote.setAgent(getAgent(ctx));
        
        //Set Note content
        accNote.setNote("Account "+newAccount.getState().getDescription()+": "+newAccount.getReason());

        Home noteHome = (Home) ctx.get(Common.ACCOUNT_NOTE_HOME);

        //Store the Note in the Account Note Home.
        try
        {
           noteHome.create(accNote);
        }
        catch (HomeException e)
        {
           new MinorLogMsg(
              source,
              "Fail to save Note for Account [BAN=" + newAccount.getBAN() + "]",
              e).log(
              ctx);
        }

    }

	
	
    static public void createAccountNote(
            Account oldAccount,
            Account newAccount,
 			boolean crmResult, 
			Context ctx, 
			Object source)
        {
    	
        Note accNote = new Note();

        accNote.setIdIdentifier(newAccount.getBAN());
        //Set the Type and Sub-Type for the note
        accNote.setType(SystemNoteTypeEnum.EVENTS.getDescription());
        accNote.setSubType(SystemNoteSubTypeEnum.ACCUPDATE.getDescription());

        accNote.setCreated(new Date());
        accNote.setLastModified(new Date());
        accNote.setAgent(getAgent(ctx));

        //Set Note content
        StringBuilder noteBuff = new StringBuilder();
        if ( crmResult)
        {
            noteBuff.append("Account Update succeeded\n" );
        }
        else
        {
            noteBuff.append("Account Update failed\n" );
        }
        
        //Create the Comment with the fields that have changed
		AccountCategory oldCategory = oldAccount.getAccountCategory(ctx);
		AccountCategory newCategory = newAccount.getAccountCategory(ctx);
		createNote("Type", oldCategory == null ? null : oldCategory.getName(),
		    newCategory == null ? null : newCategory.getName(), noteBuff);
        // pool MSISDN is no longer significant since it is a face msisdn
//        createNote("Group MSISDN", oldAccount.getGroupMSISDN(),newAccount.getGroupMSISDN(), noteBuff);
        createNote("First Name", oldAccount.getFirstName(),newAccount.getFirstName(), noteBuff); 
        createNote("Last Name", oldAccount.getLastName(),newAccount.getLastName(), noteBuff); 
        createNote("Initials", oldAccount.getInitials(),newAccount.getInitials(), noteBuff); 
        createNote("Tax Authority", String.valueOf( oldAccount.getTaxAuthority()) ,String.valueOf(newAccount.getTaxAuthority() ), noteBuff); 
        createNote("Credit Category", String.valueOf( oldAccount.getCreditCategory()), String.valueOf( newAccount.getCreditCategory()), noteBuff); 
        createNote("Dealer Code", oldAccount.getDealerCode(),newAccount.getDealerCode(), noteBuff); 
        createNote("State", oldAccount.getState().getDescription(),newAccount.getState().getDescription(), noteBuff); 
        createNote("Debt Collection Agency", String.valueOf( oldAccount.getDebtCollectionAgencyId()),String.valueOf( newAccount.getDebtCollectionAgencyId()), noteBuff); 
        createNote("Promise To Pay Date", oldAccount.getPromiseToPayDate()==null?null:oldAccount.getPromiseToPayDate().toString(),
        		newAccount.getPromiseToPayDate()==null?null:newAccount.getPromiseToPayDate().toString(), noteBuff); 
        createNote("Reason", oldAccount.getReason(), newAccount.getReason(), noteBuff); 
        createNote("Discount Class", String.valueOf( oldAccount.getDiscountClass()),String.valueOf( newAccount.getDiscountClass()), noteBuff); 
        createNote("Language", oldAccount.getLanguage(),newAccount.getLanguage(), noteBuff); 
        createNote("Currency", oldAccount.getCurrency(),newAccount.getCurrency(), noteBuff); 
        createNote("Last Modified", oldAccount.getLastModified()==null?null:oldAccount.getLastModified().toString(), 
        		newAccount.getLastModified()==null?null:newAccount.getLastModified().toString(), noteBuff); 
        createNote("Bill Cycle ID", String.valueOf(oldAccount.getBillCycleID()),
        		String.valueOf(newAccount.getBillCycleID()), noteBuff); 
        createNote("Last Bill Date", oldAccount.getLastBillDate()==null?null:oldAccount.getLastBillDate().toString(),
        		newAccount.getLastBillDate()==null?null:newAccount.getLastBillDate().toString(), noteBuff); 
        createNote("Payment Due Date", oldAccount.getPaymentDueDate()==null?null:oldAccount.getPaymentDueDate().toString(),
        		newAccount.getPaymentDueDate()==null?null:newAccount.getPaymentDueDate().toString(), noteBuff); 
        createNote("Billing Address 1", oldAccount.getBillingAddress1(),newAccount.getBillingAddress1(), noteBuff); 
        createNote("Billing Address 2", oldAccount.getBillingAddress2() ,newAccount.getBillingAddress2(), noteBuff); 
        createNote("Billing City", oldAccount.getBillingCity(),newAccount.getBillingCity(), noteBuff);
        createNote("Billing Postal Code", oldAccount.getBillingPostalCode(),newAccount.getBillingPostalCode(), noteBuff);
        createNote("Billing Province", oldAccount.getBillingProvince(),newAccount.getBillingProvince(), noteBuff); 
        createNote("Billing Country", oldAccount.getBillingCountry(),newAccount.getBillingCountry(), noteBuff); 
        createNote("Contact Name", oldAccount.getContactName(),newAccount.getContactName(), noteBuff); 
        createNote("Contact Tel", oldAccount.getContactTel(),newAccount.getContactTel(), noteBuff); 
        createNote("Contact Fax", oldAccount.getContactFax(),newAccount.getContactFax(), noteBuff); 
        createNote("Employer", oldAccount.getEmployer(),newAccount.getEmployer(), noteBuff); 
        createNote("Employer Address", oldAccount.getEmployerAddress(),newAccount.getEmployerAddress(), noteBuff); 

        // TODO: need to properly handle the ned accountId structure
//        createNote("ID Type 1", String.valueOf( oldAccount.getIdType1()), String.valueOf( newAccount.getIdType1()), noteBuff); 
//        createNote("ID Number 1", oldAccount.getIdNumber1(), newAccount.getIdNumber1(), noteBuff); 
//        createNote("ID Type 2", String.valueOf( oldAccount.getIdType2()), String.valueOf( newAccount.getIdType2()), noteBuff); 
//        createNote("ID Number 2", oldAccount.getIdNumber2(), newAccount.getIdNumber2(), noteBuff);        

        createNote("Date of Birth", oldAccount.getDateOfBirth()==null?null:oldAccount.getDateOfBirth().toString(),
        		newAccount.getDateOfBirth()==null?null:newAccount.getDateOfBirth().toString(), noteBuff); 
        createNote("Occupation", String.valueOf( oldAccount.getOccupation()),String.valueOf( newAccount.getOccupation()), noteBuff); 

        // TODO: need to handle the new security question structure
//        createNote("Question", oldAccount.getQuestion(), newAccount.getQuestion(), noteBuff); 
//        createNote("Answer", oldAccount.getAnswer(),newAccount.getAnswer(), noteBuff); 

        createNote("Company Name", oldAccount.getCompanyName(),newAccount.getCompanyName(), noteBuff); 
        createNote("Trading Name", oldAccount.getTradingName(),newAccount.getTradingName(), noteBuff); 
        createNote("Registration Number", oldAccount.getRegistrationNumber(),newAccount.getRegistrationNumber(), noteBuff); 
        createNote("Company Tel", oldAccount.getCompanyTel(),newAccount.getCompanyTel(), noteBuff); 
        createNote("Company Fax", oldAccount.getCompanyFax(),newAccount.getCompanyFax(), noteBuff); 
        createNote("Company Address 1", oldAccount.getCompanyAddress1(),newAccount.getCompanyAddress1(), noteBuff);         
        createNote("Company Address 2", oldAccount.getCompanyAddress2(),newAccount.getCompanyAddress2(), noteBuff); 
        createNote("Company City", oldAccount.getCompanyCity(),newAccount.getCompanyCity(), noteBuff); 
        createNote("Company Province", oldAccount.getCompanyProvince(),newAccount.getCompanyProvince(), noteBuff); 
        createNote("Company Country", oldAccount.getCompanyCountry(),newAccount.getCompanyCountry(), noteBuff); 
        createNote("Next ID", String.valueOf( oldAccount.getNextSubscriberId()), String.valueOf( newAccount.getNextSubscriberId()), noteBuff);         
        /***
        createNote("Accumulated Bundle Messages", String.valueOf(oldAccount.getAccumulatedBundleMessages()),
        		String.valueOf(newAccount.getAccumulatedBundleMessages()), noteBuff); 
        createNote("Accumulated Bundle Minutes", String.valueOf(oldAccount.getAccumulatedBundleMinutes()),
        		String.valueOf(newAccount.getAccumulatedBundleMinutes()), noteBuff); 
        createNote("Accumulated Balance", String.valueOf( oldAccount.getAccumulatedBalance()),
        		String.valueOf( newAccount.getAccumulatedBalance()), noteBuff); 
        createNote("Accumulated MD Usage", String.valueOf(oldAccount.getAccumulatedMDUsage()),
        		String.valueOf(newAccount.getAccumulatedMDUsage()), noteBuff); 
        **/
        
        createNote("Tax Exemption", String.valueOf( oldAccount.getTaxExemption()), String.valueOf( newAccount.getTaxExemption()), noteBuff);  
        createNote("Owner MSISDN", oldAccount.getOwnerMSISDN(),newAccount.getOwnerMSISDN(), noteBuff); 
        createNote("Payment Plan", PaymentPlanSupportHelper.get(ctx).getPaymentPlanName(ctx, oldAccount.getPaymentPlan()),
        		PaymentPlanSupportHelper.get(ctx).getPaymentPlanName(ctx, newAccount.getPaymentPlan()), noteBuff); 
        
        if (TaxAuthoritySupportHelper.get(ctx).isTEICEnabled(ctx, newAccount.getSpid()))
        {
            createNote("TEIC", String.valueOf(oldAccount.getTEIC()),String.valueOf(newAccount.getTEIC()), noteBuff);
        }
        
        String note = noteBuff.toString(); 
        
        if (note.length() > AbstractNote.NOTE_WIDTH)
        {
            new MinorLogMsg(source, "note length is larger than:" +  AbstractNote.NOTE_WIDTH + "\n" + note, null).log(ctx);
        }
        
        accNote.setNote( note.length()<  AbstractNote.NOTE_WIDTH? note : note.substring(0,  AbstractNote.NOTE_WIDTH));
        
        Home noteHome = (Home) ctx.get(Common.ACCOUNT_NOTE_HOME);
        try
        {
           noteHome.create(accNote);
        }
        catch (HomeException e)
        {
           new MinorLogMsg(
              source,
              "Fail to save Note for Account [BAN=" + newAccount.getBAN() + "]",
              e).log(ctx);
        }

   }
	
	public static void createStatementGenerationNote(final Context ctx, final String account, Date start, Date end, Collection<Short> subscriptionTypeSelection, Object source)
	{
        
        
	    Note accNote = new Note();
	    
	    accNote.setIdIdentifier(account);
        accNote.setType(SystemNoteTypeEnum.EVENTS.getDescription());
        accNote.setSubType(SystemNoteSubTypeEnum.GENERATESTATEMENT.getDescription());

        accNote.setCreated(new Date());
        accNote.setLastModified(new Date());
        accNote.setAgent(getAgent(ctx));

        //Set Note content
        StringBuilder noteBuff = new StringBuilder();
        noteBuff.append("Succesful Statement Generation");
        noteBuff.append("\nAccount Number: ");
        noteBuff.append(String.valueOf(account));
        noteBuff.append("\nPeriod Start: ");
        // TODO 2010-10-01 DateFormat access needs synchronization
        noteBuff.append(DATE_FORMAT.format(start));
        noteBuff.append("\nPeriod End: ");
        // TODO 2010-10-01 DateFormat access needs synchronization
        noteBuff.append(DATE_FORMAT.format(end));
        noteBuff.append("\nSubscription Type Selection: ");
        Iterator<Short> iter = subscriptionTypeSelection.iterator();
        while (iter.hasNext())
        {
            Short enumIndex = iter.next();
            if (enumIndex != null)
            {
                noteBuff.append(SubscriptionTypeEnum.get(enumIndex).getDescription());
                if (iter.hasNext())
                {
                    noteBuff.append(", ");   
                }     
            }       
        }
        
        String note = noteBuff.toString(); 
        
        if (note.length() > AbstractNote.NOTE_WIDTH)
        {
            new MinorLogMsg(source, "note length is larger than:" +  AbstractNote.NOTE_WIDTH + "\n" + note, null).log(ctx);
        }
        
        accNote.setNote( note.length()<  AbstractNote.NOTE_WIDTH? note : note.substring(0,  AbstractNote.NOTE_WIDTH));
        
        Home noteHome = (Home) ctx.get(Common.ACCOUNT_NOTE_HOME);
        try
        {
           noteHome.create(accNote);
        }
        catch (HomeException e)
        {
           new MinorLogMsg(
              source,
              "Fail to save Note for Account [BAN=" + account + "]",
              e).log(ctx);
        }
	}
	
	public static void createSubBundleOverUsageNote(final Context ctx, final Subscriber sub, final long bundleId,
            final long amount, final Date transDate, Object source)
    {
        final Note accNote = new Note();
        accNote.setIdIdentifier(sub.getBAN());
        accNote.setType(SystemNoteTypeEnum.ADJUSTMENT.getDescription());
        accNote.setSubType(SystemNoteSubTypeEnum.SUBUPDATE.getDescription());
        accNote.setCreated(new Date());
        accNote.setLastModified(new Date());
        accNote.setAgent(getAgent(ctx));
        // Set Note content
        final StringBuilder noteBuff = new StringBuilder();
        noteBuff.append("Bundle over usage occurred for Subscription [").append(sub.getId()).append("]");
        noteBuff.append("\nMSISDN [").append(sub.getMSISDN()).append("] -#- Bundle ID [").append(
                String.valueOf(bundleId)).append("]");
        // TODO 2010-10-01 DateFormat access needs synchronization
        noteBuff.append("\nAmount [").append(amount).append("] -#- Transaction Date [").append(
                String.valueOf(DATE_FORMAT.format(transDate))).append("]");
        String note = noteBuff.toString();
        if (note.length() > AbstractNote.NOTE_WIDTH)
        {
            new MinorLogMsg(source, "note length is larger than:" + AbstractNote.NOTE_WIDTH + "\n" + note, null)
                    .log(ctx);
        }
        accNote.setNote(note.length() < AbstractNote.NOTE_WIDTH ? note : note.substring(0, AbstractNote.NOTE_WIDTH));
        Home noteHome = (Home) ctx.get(Common.ACCOUNT_NOTE_HOME);
        try
        {
            noteHome.create(accNote);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(source, "Fail to save Note for Account [BAN=" + sub.getBAN() + "]", e).log(ctx);
        }
    }


    static public void createNote(String label, String oldVal, String newVal, StringBuilder noteBuff)
    {
        if (oldVal == null)
        {
            oldVal = "";
        }
        if (newVal == null)
        {
            newVal = "";
        }
        if (!newVal.equals(oldVal))
        {
            noteBuff.append("Subscriber ").append(label).append(": ");
            noteBuff.append(oldVal);
            noteBuff.append("->");
            noteBuff.append(newVal);
            noteBuff.append("\n");
        }
    }
    
    
   static  public String getAgent(Context ctx)
    {
        User principal = (User) ctx.get(java.security.Principal.class, new User());
        return principal.getId();
    }

   private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
    
}
