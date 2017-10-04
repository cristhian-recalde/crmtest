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
package com.trilogy.app.crm.api.generic.entity.support;

import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.troubleticket.bean.Dispute;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.InvoiceXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.troubleticket.bean.DisputeStateEnum;
import com.trilogy.app.crm.troubleticket.bean.DisputeXInfo;

/**
* @author monami.pakira@redknee.com
* @since 9.11.4
*  
*/

public class DisputeSupport {
	
	public static void validateSpid(Context ctx, Dispute dispute)
			throws HomeException 
			{
				if(dispute.getSpid()==-1 || "".equals(dispute.getSpid()))
				{
					new MinorLogMsg(DisputeSupport.class, "Dispute: Spid is Mandatory and cannot be null.").log(ctx);
					throw new HomeException("Please provide Spid.");
				}
			}
			
			public static void validateBan(Context ctx, Dispute dispute)
				throws HomeException 
			{
				if(dispute.getBAN() == null || "".equals(dispute.getBAN()))
				{
					new MinorLogMsg(Dispute.class, "Dispute: BAN is Mandatory and cannot be null.").log(ctx);
					throw new HomeException("Please provide BAN.");
				}
			}
			
			public static void validateInvoiceId(Context ctx, Dispute dispute)
				throws HomeException
			{
				if(dispute.getInvoiceId() == null || "".equals(dispute.getInvoiceId()))
				{
					new MinorLogMsg(DisputeSupport.class, "Dispute: InvoiceId is Mandatory.").log(ctx);
					throw new HomeException("Please provide InvoiceId.");
				}
				/*else if (dispute.getInvoiceId() != null)
				{
					And filter = new And();
					filter.add(new EQ(InvoiceXInfo.SPID, dispute.getSpid()));
					filter.add(new EQ(InvoiceXInfo.BAN, dispute.getBAN()));
					filter.add(new EQ(InvoiceXInfo.INVOICE_ID, dispute.getInvoiceId()));
					List<Invoice> invoice = (List<Invoice>) HomeSupportHelper.get(ctx).getBeans(ctx, Invoice.class, filter);
					if(invoice!=null && invoice.isEmpty()){
						new MinorLogMsg(DisputeSupport.class, "Dispute: BAN/Spid/InvoiceId is invalid.").log(ctx);
						throw new HomeException("No such entry found in Invoice for provided BAN,Spid and InvoiceId.");
				}
			  }*/
			}
			
			public static void validateSubscriberId(Context ctx, Dispute dispute)
				throws HomeException 
			{
				if(dispute.getSubscriberId() == null || "".equals(dispute.getSubscriberId()))
				{
					new MinorLogMsg(Dispute.class, "Dispute: SubscriberId is Mandatory.").log(ctx);
					throw new HomeException("Please provide SubscriberId.");
				}
				else if (dispute.getSubscriberId() != null)
				{
					And filter = new And();
					filter.add(new EQ(SubscriberXInfo.SPID, dispute.getSpid()));
					filter.add(new EQ(SubscriberXInfo.BAN, dispute.getBAN()));
					filter.add(new EQ(SubscriberXInfo.ID, dispute.getSubscriberId()));
					List<Subscriber> subscriber = (List<Subscriber>) HomeSupportHelper.get(ctx).getBeans(ctx, Subscriber.class, filter);
					if(subscriber!=null && subscriber.isEmpty()){
						new MinorLogMsg(DisputeSupport.class, "Dispute: BAN/Spid/SubscriberId is invalid.").log(ctx);
						throw new HomeException("No such entry found in Subscriber for provided BAN,Spid and SubscriberId.");
				}
		      }
			}
			
			public static void validateDisputedAmount(Context ctx, Dispute dispute)
				throws HomeException 
			{
				if(dispute.getDisputedAmount()==-1 || "".equals(dispute.getDisputedAmount()))
				{
					new MinorLogMsg(Dispute.class, "Dispute: DisputedAmount is Mandatory.").log(ctx);
					throw new HomeException("Please provide DisputedAmount.");
				}
			}
			
			public static void validateTroubleTicketId(Context ctx, Dispute dispute)
				throws HomeException
			{
				if(dispute.getTroubleTicketId()== null || "".equals(dispute.getTroubleTicketId()))
				{
					new MinorLogMsg(Dispute.class, "Dispute: TroubleTicketId is Mandatory.").log(ctx);
					throw new HomeException("Please provide TroubleTicketId.");
				}
				else
				{
					And filter = new And();
					filter.add(new EQ(DisputeXInfo.TROUBLE_TICKET_ID, dispute.getTroubleTicketId()));
					List<Dispute> disputeBean = (List<Dispute>) HomeSupportHelper.get(ctx).getBeans(ctx, Dispute.class, filter);
					if(disputeBean!=null && !disputeBean.isEmpty())
					{
						new MinorLogMsg(DisputeSupport.class, "Dispute: TroubleTicketId already exists.").log(ctx);
						throw new HomeException("TroubleTicketId already exists.");
					}	
				}
			}
			
			public static void validateState(Context ctx, Dispute dispute, String mode)
				throws HomeException 
			{
				if(dispute.getState()== null || "".equals(dispute.getState()))
				{
					throw new HomeException("Please provide State.");
				}
				else 
				{
				 if(mode.equals("createMode"))
				  {
					if( !(dispute.getState().equals(DisputeStateEnum.ACTIVE)))
				    {
					   new MinorLogMsg(Dispute.class, "Dispute: State must be Active(i.e. : 0).").log(ctx);
					   throw new HomeException("Value of State must be Active(i.e. : 0).");
				    }
				  }
				 else if(mode.equals("updateMode"))
				 {
					if( !(dispute.getState().equals(DisputeStateEnum.RESOLVED)))
				    {
					   new MinorLogMsg(Dispute.class, "Dispute: State must be Resolved(i.e. : 1).").log(ctx);
					   throw new HomeException("Value of State must be Resolved(i.e. : 1).");
				    }
				  }
				}		
			}
			
			public static void validateAdjustmentType(Context ctx, Dispute dispute)
				throws HomeException 
			{
				{		
					boolean flag = false;
					if(dispute.getDisputedAmountAdjustmentType()!= -1)
					{
					 for (Iterator iterator = AdjustmentTypeEnum.COLLECTION.iterator(); iterator.hasNext();) 
					 {
						if(dispute.getDisputedAmountAdjustmentType() == ((AdjustmentTypeEnum)iterator.next()).getIndex())
						{
							flag = true;
							new MinorLogMsg(Dispute.class, "flag"+flag).log(ctx);
							break;
						}	
					 }
					}
					else if(dispute.getDisputedAmountAdjustmentType()== -1 || "".equals(dispute.getDisputedAmountAdjustmentType()))
					{
						throw new HomeException("Please provide DisputedAmountAdjustmentType.");
					}
					new MinorLogMsg(Dispute.class, "flag"+flag).log(ctx);
					if(!flag)
					{
					  new MinorLogMsg(DisputeSupport.class, "Dispute: DisputedAmountAdjustmentType is invalid.").log(ctx);
					  throw new HomeException("DisputedAmountAdjustmentType is invalid.");
					}
				   }
			}
			
			public static void validateResolvedAmount(Context ctx, Dispute dispute)
					throws HomeException 
				{
					if(dispute.getResolvedAmount()==-1)
					{
						new MinorLogMsg(Dispute.class, "Dispute: ResolvedAmount is Mandatory.").log(ctx);
						throw new HomeException("Please provide ResolvedAmount.");
					}
				}
			public static void validateResolvedAdjustmentType(Context ctx, Dispute dispute)
					throws HomeException 
				{
					{		
						boolean flag = false;
						if(dispute.getResolvedAmountAdjustmentType()!= -1)
						{
						 for (Iterator iterator = AdjustmentTypeEnum.COLLECTION.iterator(); iterator.hasNext();) 
						 {
							if(dispute.getResolvedAmountAdjustmentType() == ((AdjustmentTypeEnum)iterator.next()).getIndex())
							{
								flag = true;
								new MinorLogMsg(Dispute.class, "flag"+flag).log(ctx);
								break;
							}	
						 }
						}
						else if(dispute.getResolvedAmountAdjustmentType()== -1 || "".equals(dispute.getResolvedAmountAdjustmentType()))
						{
							throw new HomeException("Please provide ResolvedAmountAdjustmentType.");
						}
						new MinorLogMsg(Dispute.class, "flag"+flag).log(ctx);
						if(!flag)
						{
						  new MinorLogMsg(DisputeSupport.class, "Dispute: ResolvedAmountAdjustmentType is invalid.").log(ctx);
						  throw new HomeException("ResolvedAmountAdjustmentType is invalid.");
						}
					   }
				}
			
			public static void validateId(Context ctx, Dispute dispute)
					throws HomeException 
					{
						if(dispute.getId()==-1 || "".equals(dispute.getId()))
						{
							new MinorLogMsg(DisputeSupport.class, "Dispute: Id is Mandatory and cannot be null.").log(ctx);
							throw new HomeException("Please provide Id.");
						}
						else if (dispute.getId() != -1)
						{
							And filter = new And();
							filter.add(new EQ(DisputeXInfo.SPID, dispute.getSpid()));
							filter.add(new EQ(DisputeXInfo.BAN, dispute.getBAN()));
							filter.add(new EQ(DisputeXInfo.ID, dispute.getId()));
							List<Dispute> disputeBean = (List<Dispute>) HomeSupportHelper.get(ctx).getBeans(ctx, Dispute.class, filter);
							if(disputeBean!=null && disputeBean.isEmpty()){
								new MinorLogMsg(DisputeSupport.class, "Dispute: BAN/Spid/Id is invalid.").log(ctx);
								throw new HomeException("No such entry found in Dispute for provided BAN,Spid and Id.");
						}
					 }
					}
}