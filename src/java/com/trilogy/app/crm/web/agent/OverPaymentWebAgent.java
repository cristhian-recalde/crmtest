/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.web.agent;

import java.io.PrintWriter;
import java.util.Date;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.agent.ServletBridge;
import com.trilogy.framework.xhome.web.agent.WebAgent;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.BillCycleHome;
import com.trilogy.app.crm.bean.BillCycleXInfo;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.bean.LateFeeEarlyRewardForm;
import com.trilogy.app.crm.bean.LateFeeEarlyRewardFormWebControl;
import com.trilogy.app.crm.bean.OverPaymentForm;
import com.trilogy.app.crm.bean.OverPaymentFormWebControl;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.paymentprocessing.InvoicePaymentProcessingAgent;
import com.trilogy.app.crm.paymentprocessing.LateFeeAgent;
import com.trilogy.app.crm.support.AccountOverPaymentHistorySupport;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.TransactionSupport;

/**
 * @author vickhra.sanap@redknee.com
 * @since 9.7.2
 * 
 */
public class OverPaymentWebAgent extends ServletBridge implements WebAgent
{
    /**
     * Module name.
     */
    public static final String MODULE = "OverPayment";

    /**
     * Web control for Over Payment Run Form.
     */
    private final  OverPaymentFormWebControl webControl_ = new OverPaymentFormWebControl();

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final Context context) throws AgentException
    {
        final PrintWriter out = getWriter(context);
        final OverPaymentForm form = new OverPaymentForm();
        final Context subContext = context.createSubContext();
        final MessageMgr messageManager = new MessageMgr(subContext, MODULE);
        final HTMLExceptionListener listener = new HTMLExceptionListener(messageManager);
        subContext.put(ExceptionListener.class, listener);
        boolean flgStatus = true;

        subContext.put("MODE", OutputWebControl.EDIT_MODE);

        if (getParameter(context, "Run") != null)
        {
           
            this.webControl_.fromWeb(subContext, form, getRequest(context), "");

            out.print("<pre><center><font size=\"1\" face=\"Verdana\" color=\"green\"><b>");
    		out.println("Over Payment Run started.</b></font></center></pre>");
           

            final int spid = form.getServiceProvider();
			final String ban = form.getBAN();

            try
            {
                SpidSupport.getCRMSpid(context, spid);
            }
            catch (final HomeException exception)
            {
            	flgStatus = false;
    			recordFailures(context,out,"Cannot retrieve SPID ",ban,true);
                //throw new AgentException("Cannot retrieve SPID", exception);   
            }

			Account account = null;
			try
			{
				account = AccountSupport.getAccount(context, ban);
			}
			catch (HomeException exception)
			{
				flgStatus = false;
				recordFailures(context,out,"Fail to retrieve account ",ban,true);
				//throw new AgentException("Fail to retrieve account " + ban,exception);
    			
			}

			if (account == null)
			{
				flgStatus = false;
				recordFailures(context,out,"Cannot find account ",ban,true);
				//throw new AgentException("Cannot find account " + ban);
				
			}
			else if (!account.isResponsible())
			{
				flgStatus = false;
				recordFailures(context,out,"Cannot generate Over Payment fee for non-responsible account ",ban,true);
				//throw new AgentException("Cannot generate Over Payment fee for non-responsible account "+ ban);
    			
			}

			if (flgStatus != false)
			{
				executeTransaction(context,ban,spid,out);
			}
			

        }

        final FormRenderer renderer = (FormRenderer) context.get(FormRenderer.class, DefaultFormRenderer.instance());
        renderer.Form(out, context);

        if (listener.hasErrors())
        {
            listener.toWeb(subContext, out, "", form);

        }
        out.println("<table><tr><td>");
        this.webControl_.toWeb(subContext, out, "", form);
        out.println("</td></tr></table>");
        out.println("<input type=\"submit\" value=\"Run\" name=\"Run\" />");
        renderer.FormEnd(out);
        out.println("<br />");
    }
    
    public void executeTransaction(Context context,String BAN,int spid,PrintWriter out)
    {
    	// Run transaction for Over payment.

		try {
	    	Long amount = AccountOverPaymentHistorySupport.getOverPaymentBalance(context, BAN);
	    	if (amount != null && amount != 0 )
	    	{
				Transaction transaction =  TransactionSupport.createAccountPaymentTransaction(context, spid, amount, amount,
						AdjustmentTypeSupportHelper.get(context).getAdjustmentType(context, AdjustmentTypeEnum.OverPaymentCredit), true, false, "SYSTEM", new Date(), new Date(), "Over Payment Transaction", 0,BAN);
				recordFailures(context,out,"Over Payment Transaction is successful.",BAN,false);
	    	}
	    	else if (amount != null && amount == 0L)
	    	{
	    		recordFailures(context,out,"Over Payment Balance is "+amount,BAN,true);
	    	}
	    	else if (amount == null)
	    	{
	    		recordFailures(context,out," ",BAN,true);
	    	}
		        		
		} catch (HomeException e) {
			
            new MajorLogMsg(this, "Error occurred in visitor while processing Over Payment", e).log(context);
		}    	
    }
    
    public void recordFailures(Context context, PrintWriter out,String error, Object param,boolean isMajor)
    {
    	if (isMajor)
    	{
    		new MajorLogMsg(this, error+" "+param.toString()).log(context);
    	}
    	else 
    	{
    		new InfoLogMsg(this, error+" "+param.toString()).log(context);
    	}
    	displayOnUi(out,error,isMajor);
    }
    public void displayOnUi(PrintWriter out,String error,boolean isMajor)
    {
        out.print("<pre><center><font size=\"1\" face=\"Verdana\" ><b>");
    	if (isMajor)
    	{
    		out.println("Over Payment Run Failed."+error+"</b></font></center></pre>");
    	}
    	else 
    	{
    		out.println(error+"</b></font></center></pre>");
    	}
		
    }
}
