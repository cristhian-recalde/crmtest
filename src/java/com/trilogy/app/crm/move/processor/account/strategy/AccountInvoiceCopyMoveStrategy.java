/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.move.processor.account.strategy;

import java.util.Collection;
import java.util.HashSet;

import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.InvoiceHome;
import com.trilogy.app.crm.bean.InvoiceTaxComponents;
import com.trilogy.app.crm.bean.InvoiceTaxComponentsHome;
import com.trilogy.app.crm.bean.InvoiceTaxComponentsXDBHome;
import com.trilogy.app.crm.bean.InvoiceTaxComponentsXInfo;
import com.trilogy.app.crm.bean.InvoiceXDBHome;
import com.trilogy.app.crm.bean.InvoiceXInfo;
import com.trilogy.app.crm.invoice.delivery.InvoiceDelivery;
import com.trilogy.app.crm.invoice.delivery.InvoiceDeliveryHome;
import com.trilogy.app.crm.invoice.delivery.InvoiceDeliveryXDBHome;
import com.trilogy.app.crm.invoice.delivery.InvoiceDeliveryXInfo;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveWarningException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.ContextualizingHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * This class will copy following business entities for old Account and create them for new account.
 * 1. Account Invoice
 * 2. Invoice Tax Components
 * 3. Invoice Delivery
 * 
 * It doesn't remove the old entities.
 * @author sgaidhani
 * @since 9.5.1
 */
public class AccountInvoiceCopyMoveStrategy<AMR extends AccountMoveRequest> extends CopyMoveStrategyProxy<AMR> 
{
	public static final String INVOICE_COPY_OPERATION = "Invoice";
	public static final String INVOICE_TAX_COMPONENT_COPY_OPERATION = "InvoiceTaxComponents";
	public static final String INVOICE_DELIVERY_OPERATION = "InvoiceDelivery";
	public static final String NEW_INVOICE_ID_SUFFIX = "_P";
	
	public AccountInvoiceCopyMoveStrategy(CopyMoveStrategy<AMR> delegate)
    {
        super(delegate);
    }
	
	@Override
	public void initialize(Context ctx, AMR request) {
		super.initialize(ctx, request);
	}

	@Override
	public void validate(Context ctx, AMR request) throws IllegalStateException {
		super.validate(ctx, request);
	}

	@Override
	public void createNewEntity(Context ctx, AMR request) throws MoveException 
	{
        final String  oldIdentifier = ((AccountMoveRequest) request).getOldAccount(ctx).getBAN();
        final String  newIdentifier = ((AccountMoveRequest) request).getNewAccount(ctx).getBAN();
        
        copyAccountInvoiceRecords(ctx, request, oldIdentifier, newIdentifier);
        
        copyInvoiceTaxComponentRecords(ctx, request, oldIdentifier,
				newIdentifier);
        
        copyInvoiceDeliveryRecords(ctx, request, oldIdentifier, newIdentifier);

        
        //Move to next delegate.
        super.createNewEntity(ctx, request);
	}

	@Override
	public void removeOldEntity(Context ctx, AMR request) throws MoveException {
		super.removeOldEntity(ctx, request);
	}
	
	private void copyInvoiceDeliveryRecords(Context ctx, AMR request,
			final String oldIdentifier, final String newIdentifier) {
		Collection<MoveWarningException> warnings = new HashSet<MoveWarningException>();
		int size = 0;
		try
		{
			Home home = (Home) ctx.get(InvoiceDeliveryHome.class);
			if(home != null && home instanceof HomeProxy)
			{
				Home xdbHome = ((HomeProxy)home).findDecorator(InvoiceDeliveryXDBHome.class);
				if(xdbHome != null)
				{

					Collection<InvoiceDelivery> data = xdbHome.select(ctx, new EQ(InvoiceDeliveryXInfo.BAN, oldIdentifier));

					for (InvoiceDelivery deliveryRecord : data)
					{
						size ++;
						try
						{
							InvoiceDelivery newRecord = (InvoiceDelivery) deliveryRecord.deepClone(); 
							newRecord.setBan(newIdentifier);

							xdbHome.create(ctx, newRecord);
						}
						catch (Exception e)
						{
							String warningMesssage = "Error occured while trying to copy an Invoice Delivery Record "
									+ deliveryRecord.ID().toString()
									+ " to new BAN :" + newIdentifier;
							LogSupport.minor(ctx, this, warningMesssage, e);
							request.reportWarning(ctx,
									new MoveWarningException(request,warningMesssage, e));
						}
					}
				}
			}

		}
        catch (HomeException e)
        {
        	String warningMesssage = "Error occured while trying to copy all invoice delivery Records for BAN : " + oldIdentifier
            		+ " to new BAN :" + newIdentifier;
        	LogSupport.minor(ctx, this, warningMesssage, e);		
            request.reportWarning(ctx,
                    new MoveWarningException(request,warningMesssage , e));
        }
		
		if(size == 0)
		{
			request.reportStatusMessages(ctx,INVOICE_DELIVERY_OPERATION + " "+ COPY_NOT_APPLICABLE);
		}
		else
		{
			if(warnings.size() > 0)
			{
				request.reportStatusMessages(ctx,INVOICE_DELIVERY_OPERATION + " "+ COPY_FAILED);
				for(MoveWarningException warning : warnings)
				{
					request.reportWarning(ctx, warning);
				}
			}
			else
			{
				request.reportStatusMessages(ctx,INVOICE_DELIVERY_OPERATION + " "+ COPY_SUCCESS);
			}
		}
	}

	private void copyInvoiceTaxComponentRecords(Context ctx, AMR request,
			final String oldIdentifier, final String newIdentifier) {
		Collection<MoveWarningException> warnings = new HashSet<MoveWarningException>();
		int size = 0;
		try
		{
			Home home = (Home) ctx.get(InvoiceTaxComponentsHome.class);
			if(home != null && home instanceof HomeProxy)
			{
				Home xdbHome = ((HomeProxy)home).findDecorator(InvoiceTaxComponentsXDBHome.class);
				if(xdbHome != null)
				{

					Collection<InvoiceTaxComponents> data = xdbHome.select(ctx,new EQ(InvoiceTaxComponentsXInfo.BAN, oldIdentifier));

					for (InvoiceTaxComponents itcRecord : data)
					{
						size++;
						try
						{
							InvoiceTaxComponents itcNewRecord = (InvoiceTaxComponents) itcRecord.deepClone();
							itcNewRecord.setBAN(newIdentifier);

							xdbHome.create(ctx, itcNewRecord);
						}
						catch (Exception e)
						{
							String warningMesssage = "Error occured while trying to copy an Invoice Tax Component Record "
									+ itcRecord.ID().toString()
									+ " to new BAN :" + newIdentifier;
							LogSupport.minor(ctx, this, warningMesssage, e);
							request.reportWarning(ctx,
									new MoveWarningException(request,warningMesssage , e));
						}
					}
				}
			}

		}
        catch (HomeException e)
        {
        	String warningMesssage = "Error occured while trying to copy all invoice tax component Records for BAN : " + oldIdentifier
            		+ " to new BAN :" + newIdentifier;
        	LogSupport.minor(ctx, this, warningMesssage, e);
            request.reportWarning(ctx,
                    new MoveWarningException(request,warningMesssage , e));
        }
		
		if(size == 0)
		{
			request.reportStatusMessages(ctx,INVOICE_TAX_COMPONENT_COPY_OPERATION + " "+ COPY_NOT_APPLICABLE);
		}
		else
		{
			if(warnings.size() > 0)
			{
				request.reportStatusMessages(ctx,INVOICE_TAX_COMPONENT_COPY_OPERATION + " "+ COPY_FAILED);
				for(MoveWarningException warning : warnings)
				{
					request.reportWarning(ctx, warning);
				}
			}
			else
			{
				request.reportStatusMessages(ctx,INVOICE_TAX_COMPONENT_COPY_OPERATION + " "+ COPY_SUCCESS);
			}
		}
	}

	private void copyAccountInvoiceRecords(Context ctx, AMR request,
			final String oldIdentifier, final String newIdentifier) {
		
		Collection<MoveWarningException> warnings = new HashSet<MoveWarningException>();
		int size = 0;
		try
		{
			Home home = (Home) ctx.get(InvoiceHome.class);
			if(home != null && home instanceof HomeProxy)
			{
				Home conversionHome = ((HomeProxy)home).findDecorator(InvoiceXDBHome.class);
				conversionHome =
				        new ContextualizingHome(ctx, conversionHome);
				conversionHome =
					    ConfigChangeRequestSupportHelper.get(ctx)
					        .registerHomeForConfigSharing(ctx, conversionHome, Invoice.class);
				
				if(conversionHome != null)
				{

					Collection<Invoice> data = conversionHome.select(ctx,new EQ(InvoiceXInfo.BAN, oldIdentifier));

					for (Invoice invoiceRecord : data)
					{
						size++;
						try
						{
							Invoice newInvoiceRecord = (Invoice) invoiceRecord.deepClone();
							newInvoiceRecord.setBAN(newIdentifier);
							String oldInvoiceId = newInvoiceRecord.getInvoiceId();
							newInvoiceRecord.setInvoiceId(oldInvoiceId+NEW_INVOICE_ID_SUFFIX);
							conversionHome.create(ctx, newInvoiceRecord);
						}
						catch (Exception e)
						{
							String warningMesssage = "Error occured while trying to copy an Invoice Record "
									+ "for BAN :'" + oldIdentifier
									+ "' and invoiceDate '" + invoiceRecord.getInvoiceDate() 
									+ " to new BAN :" + newIdentifier;
							LogSupport.minor(ctx, this, warningMesssage, e);
							warnings.add(
									new MoveWarningException(request,warningMesssage , e));
						}
					}
				}
			}

		}
        catch (HomeException e)
        {
        	String warningMesssage = "Error occured while trying to copy all invoice Records for BAN : " + oldIdentifier
            		+ " to new BAN :" + newIdentifier;
        	LogSupport.minor(ctx, this, warningMesssage, e);
        	warnings.add(
                    new MoveWarningException(request, warningMesssage, e));
        }
		
		if(size == 0)
		{
			request.reportStatusMessages(ctx,INVOICE_COPY_OPERATION + " "+ COPY_NOT_APPLICABLE);
		}
		else
		{
			if(warnings.size() > 0)
			{
				request.reportStatusMessages(ctx,INVOICE_COPY_OPERATION + " "+ COPY_FAILED);
				for(MoveWarningException warning : warnings)
				{
					request.reportWarning(ctx, warning);
				}
			}
			else
			{
				request.reportStatusMessages(ctx,INVOICE_COPY_OPERATION + " "+ COPY_SUCCESS);
			}
		}
	}
}
