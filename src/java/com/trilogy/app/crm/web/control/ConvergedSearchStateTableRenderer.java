/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.ConvergedAccountSubscriber;
import com.trilogy.app.crm.bean.ConvergedStateEnum;
import com.trilogy.app.crm.bean.SearchTypeEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.renderer.TableRenderer;
import com.trilogy.framework.xhome.web.renderer.TableRendererProxy;
/**
 * Renderer that actually changes the row depending on the state
 * 
 * @author amedina
 *
 */
public class ConvergedSearchStateTableRenderer extends TableRendererProxy 
{

	public ConvergedSearchStateTableRenderer(Context ctx, TableRenderer delegate) 
	{
		super(delegate);
		ctx_  = ctx;
	}

	/*
	 *  (non-Javadoc)
	 * @see com.redknee.framework.xhome.web.renderer.TableRenderer#TR(java.io.PrintWriter, java.lang.Object, int)
	 */
	
	  public TableRenderer TR(final Context ctx, PrintWriter out, Object bean, int i)
	  {
		  /*
		   * com.redknee.app.crm.web.control.ConvergedSearchStateTableRenderer.ACTIVE.colour active states:
		   * Active 
		   * Promise to pay
		   * 
		   * com.redknee.app.crm.web.control.ConvergedSearchStateTableRenderer.SUSPEND.colour suspend states:
		   * SUSPENDED
		   * EXPIRED
		   * 
		   * com.redknee.app.crm.web.control.ConvergedSearchStateTableRenderer.DUNNED.colour dunned states:
		   * Warned
		   * Dunned
		   * In arrears
		   * In collection
		   * 
		   * com.redknee.app.crm.web.control.ConvergedSearchStateTableRenderer.INACTIVE.colour inactive states:
		   * Locked/Barred
		   * Inactive
		   * Deactivated
		   * 
		   * 
		   * com.redknee.app.crm.web.control.ConvergedSearchStateTableRenderer.Account.colour if the search type is account and subscriber
		   * The application will put a special colour to the account rows
		   */
		  ConvergedAccountSubscriber ctl = (ConvergedAccountSubscriber) bean;
	      MessageMgr mmgr=new MessageMgr(ctx_, this);
	      switch(ctl.getState().getIndex())
	      {
	        case ConvergedStateEnum.ACTIVE_INDEX:
	        case ConvergedStateEnum.PROMISE_TO_PAY_INDEX:
		          out.print("<tr bgcolor=\"");
		          out.print(getColour(ctl, mmgr,this.getClass().getName()+".ACTIVE.colour", "lightgreen"));
		          out.print("\">");
		          break;
	        	
	        case ConvergedStateEnum.SUSPENDED_INDEX:
	        case ConvergedStateEnum.EXPIRED_INDEX:
		          out.print("<tr bgcolor=\"");
		          out.print(getColour(ctl, mmgr,this.getClass().getName()+".SUSPEND.colour", "orange"));
		          out.print("\">");
		          break;
	        	
	        	
	        case ConvergedStateEnum.NON_PAYMENT_SUSPENDED_INDEX:
	        case ConvergedStateEnum.NON_PAYMENT_WARN_INDEX:
	        case ConvergedStateEnum.IN_ARREARS_INDEX:
	        case ConvergedStateEnum.PENDING_INDEX:
	        case ConvergedStateEnum.IN_COLLECTION_INDEX:
	          out.print("<tr bgcolor=\"");
	          out.print(getColour(ctl, mmgr,this.getClass().getName()+".DUNNED.colour", "amber"));
	          out.print("\">");
	          break;
	  
	        case ConvergedStateEnum.LOCKED_INDEX:
	        case ConvergedStateEnum.INACTIVE_INDEX:
	        case ConvergedStateEnum.DORMANT_INDEX:
	          out.print("<tr bgcolor=\"");
	          out.print(getColour(ctl, mmgr,this.getClass().getName()+".INACTIVE.colour", "red"));
	          out.print("\">");	          
	          break;
	  
	        default:
	          return getDelegate().TR(ctx, out, bean, i);
	      }
	      return this;

	  }
	  
	  protected String getColour(ConvergedAccountSubscriber bean, MessageMgr mmgr, String index,String value)
	  {
		  String colour = mmgr.get(index, value);
		  if (bean.getType() == SearchTypeEnum.Both)
		  {
			  if (bean.getMSISDN() == null || bean.getMSISDN().trim().length() <= 0)
			  {
				  colour = mmgr.get(this.getClass().getName()+".ACCOUNT.colour", "#C8C8C8");
			  }
		  }
		  return colour;
	  }

	  protected Context ctx_;
	
}
