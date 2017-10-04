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

import java.security.Principal;
import java.text.MessageFormat;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.calculation.service.CalculationServiceInternalException;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.NoteSupport;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.core.locale.CurrencyHome;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author amedina
 *
 * For every operation to the Invoice the invoice hoe will need to create an Account Note
 */
public class InvoiceAccountNoteHome extends HomeProxy 
{


	public InvoiceAccountNoteHome(
    		Context ctx,
            final Home delegate)
            throws HomeException
    {
            super(delegate);
    }
    
    public Object create (Context ctx, Object obj) 
    throws HomeInternalException, HomeException
    {
    	Invoice inv = (Invoice) super.getDelegate().create(ctx, obj);
    	
    	addNote(ctx, inv, " - Create - ");
    	
    	return inv;
    }


	public Object store (Context ctx, Object obj)
	throws HomeInternalException, HomeException
	{
		Invoice inv = (Invoice) obj;
		
		String fields = getFieldEdited(ctx, inv);

    	addNote(ctx, inv, " - Edit - " + " " + fields + " ");

    	return super.getDelegate().store(ctx, obj);
	}
	
	public Object find (Context ctx, Object obj) 
	throws HomeInternalException, HomeException
	{
		Invoice inv = (Invoice) super.find(ctx, obj);
		
		if(inv!=null)
		{
			if (ctx.has(Common.VIEW_MSG))
			{
				String viewMessage = (String) ctx.get(Common.VIEW_MSG);
				addNote(ctx, inv, viewMessage);
			}
		}
		
		return inv; 
	}
	
	public void remove(Context ctx, Object obj) throws HomeInternalException, HomeException
	{
		Invoice inv = (Invoice) obj;
		
    	addNote(ctx, inv, " - Delete - ");
		
		super.remove(ctx, obj);
	}


	/**
	 * @param ctx
	 * @param inv
	 * @param string
	 * @throws HomeException
	 */
	private void addNote(Context ctx,  Invoice inv, String message) 
	throws HomeException 
	{
    	Account acct = AccountSupport.getAccount(ctx, inv.getBAN());
    	
    	String user = findUser(ctx);
    	NoteSupportHelper.get(ctx).addAccountNote(ctx, acct.getBAN(), user + message + inv.getInvoiceId() + "-" + inv.getInvoiceDate(), SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.ACCACTIVE);
	}

	
	/**
	 * @param ctx
	 * @param inv
	 * @return
	 */
	private String getFieldEdited(Context ctx, Invoice inv) 
	{
	    CalculationService service = (CalculationService) ctx.get(CalculationService.class);
        Invoice oldInvoice = null;
        StringBuilder fields = new StringBuilder();
        try
        {
            oldInvoice = service.getInvoiceForAccount(ctx, inv.getBAN(), inv.getInvoiceDate());
            editMessage(ctx, oldInvoice.getBAN(), inv.getBAN(), "BAN", fields);
            editMessage(ctx, oldInvoice.getDueDate(), inv.getDueDate(), "Due Date", fields);
            editMessage(ctx, oldInvoice.getGeneratedDate(), inv.getGeneratedDate(), "Generated Date", fields);
            editMessage(ctx, oldInvoice.getInvoiceDate(), inv.getInvoiceDate(), "Invoice Date", fields);
            editMessage(ctx, oldInvoice.getInvoiceId(), inv.getInvoiceId(), "Invoice Id", fields);
            editMessage(ctx, oldInvoice.getMSISDN(), inv.getMSISDN(), "MSISDN", fields);
            editMessage(ctx, oldInvoice.getRootInvoiceId(), inv.getRootInvoiceId(), "Root Invoice Id", fields);
            editMessage(ctx, oldInvoice.getSettlementState(), inv.getSettlementState(), "Settlement State", fields);
            editMessage(ctx, oldInvoice.getURL(), inv.getURL(), "URL", fields);
            editMessage(ctx, inv, oldInvoice.getCurrentAmount(), inv.getCurrentAmount(), "Current Amount", fields);
            editMessage(ctx, inv, oldInvoice.getCurrentTaxAmount(), inv.getCurrentTaxAmount(), "Current TaxAmount", fields);
            editMessage(ctx, inv, oldInvoice.getDataAmount(), inv.getDataAmount(), "Data Amount", fields);
            editMessage(ctx, inv, oldInvoice.getDebtLastBillCycle1(), inv.getDebtLastBillCycle1(), "Debt Last bill cycle 1", fields);
            editMessage(ctx, inv, oldInvoice.getDebtLastBillCycle2(), inv.getDebtLastBillCycle2(), "Debt Last bill cycle 2", fields);
            editMessage(ctx, inv, oldInvoice.getDebtLastBillCycle3(), inv.getDebtLastBillCycle3(), "Debt Last bill cycle 3", fields);
            editMessage(ctx, inv, oldInvoice.getDiscountAmount(), inv.getDiscountAmount(), "Discount Amount", fields);
            editMessage(ctx, inv, oldInvoice.getDomesticCallsAmount(), inv.getDomesticCallsAmount(), "Domestic Calls Amount", fields);
            editMessage(ctx, inv, oldInvoice.getDroppedCallCreditAmount(), inv.getDroppedCallCreditAmount(), "Dropped Calls Amount", fields);
            editMessage(ctx, inv, oldInvoice.getInternationalCallsAmount(), inv.getInternationalCallsAmount(), "International Calls Amount", fields);
            editMessage(ctx, inv, oldInvoice.getOneTimeChargesAmount(), inv.getOneTimeChargesAmount(), "One Time Charges Amount", fields);
            editMessage(ctx, inv, oldInvoice.getOtherChargesAmount(), inv.getOtherChargesAmount(), "Other Charges Amount", fields);
            editMessage(ctx, inv, oldInvoice.getPaymentAmount(), inv.getPaymentAmount(), "Payment Amount", fields);
            editMessage(ctx, inv, oldInvoice.getPreviousBalance(), inv.getPreviousBalance(), "Previous Balance", fields);
            editMessage(ctx, inv, oldInvoice.getRecurringCharges(), inv.getRecurringCharges(), "Recurring Charges", fields);
            editMessage(ctx, inv, oldInvoice.getRoamingAmount(), inv.getRoamingAmount(), "Roaming Amount", fields);
            editMessage(ctx, inv, oldInvoice.getSpid(), inv.getSpid(), "SPID", fields);
            editMessage(ctx, inv, oldInvoice.getSupplementaryChargesAmount(), inv.getSupplementaryChargesAmount(), "Supplementary Charges Amount", fields);
            editMessage(ctx, inv, oldInvoice.getTaxAmount(), inv.getTaxAmount(), "Tax Amount", fields);
        }
        catch (CalculationServiceException e)
        {
        }
		return fields.toString();
	}

	/**
	 * @param ban
	 * @param ban2
	 * @param string
	 * @param fields
	 */
	private void editMessage(Context ctx, Object arg1, Object arg2, String attribute, StringBuilder fields)
	{
		if (!arg1.equals(arg2))
		{
			printMessage(arg1, arg2, attribute, fields);
		}
	}
	
	/**
	 * @param inv
	 * @param ban
	 * @param ban2
	 * @param string
	 * @param fields
	 */
	private void editMessage(Context ctx, Invoice inv, int arg1, int arg2, String attribute, StringBuilder fields)
	{
		if (arg1 != arg2)
		{
    		try 
			{
		    	Account account = (Account) ctx.get(Account.class);
		    	if (account == null)
		    	{
						account = AccountSupport.getAccount(ctx, inv.getBAN());
		    	}
		    	
		    	if (account != null)
		    	{
					Currency currency = (Currency) ((Home) ctx.get(CurrencyHome.class)).find(account.getCurrency());
					if (currency != null)
					{
						printMessage(currency.formatValue(arg1), currency.formatValue(arg2), attribute, fields);
					}
		    	}
			}
    		catch (HomeException e) 
			{
    			LogSupport.major(ctx,this, "HomeException on editMessage : " + e, e);
			}
		}
	}
	
	private void editMessage(Context ctx, Invoice inv, long arg1, long arg2, String attribute, StringBuilder fields)
    {
        if (arg1 != arg2)
        {
            try 
            {
                Account account = (Account) ctx.get(Account.class);
                if (account == null)
                {
                        account = AccountSupport.getAccount(ctx, inv.getBAN());
                }
                
                if (account != null)
                {
                    Currency currency = (Currency) ((Home) ctx.get(CurrencyHome.class)).find(account.getCurrency());
                    if (currency != null)
                    {
                        printMessage(currency.formatValue(arg1), currency.formatValue(arg2), attribute, fields);
                    }
                }
            }
            catch (HomeException e) 
            {
                LogSupport.major(ctx,this, "HomeException on editMessage : " + e, e);
            }
        }
    }

	

	/**
	 * @param arg1
	 * @param arg2
	 * @param attribute
	 * @param fields
	 */
	private void printMessage(Object arg1, Object arg2, String attribute, StringBuilder fields) 
	{
		fields.append(MessageFormat.format(EDIT_MESSAGE, attribute, arg1, arg2));
	}

	/**
	 * @param ctx
	 * @return
	 */
	private String findUser(Context ctx)
	{
    	User user = (User) ctx.get(Principal.class);
    	String userName = SYSTEM;

    	if (user != null)
    	{
    		userName = user.getId();
    	}
    	
		return userName;
	}

	private static final String SYSTEM = "system";
	private static String EDIT_MESSAGE = "changed {0} from {1} to {2} ";

}
