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

package com.trilogy.app.crm.bas.recharge;

import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.app.crm.bean.SubscriberBalances;
import com.trilogy.app.crm.bean.SubscriberBalancesForm;
import com.trilogy.app.crm.bean.SubscriberBalancesFormWebControl;
import com.trilogy.app.crm.bean.paymentgatewayintegration.PostpaidSubscriberBalancesFileWriter;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.pipe.ThreadPool;
import com.trilogy.framework.xhome.web.agent.ServletBridge;
import com.trilogy.framework.xhome.web.agent.WebAgent;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Web agent to get Subscriber Balances.
 * 
 * @author Vijay.Gote
 * 
 * @since 9_7_2
 *
 */
public class SubscriberBalancesWebAgent extends ServletBridge implements WebAgent
{
    /**
     * Module name.
     */
    public static final String MODULE = "SubscriberBalances";

    /**
     * Web control for Subscriber Balances Form.
     */
    private final SubscriberBalancesFormWebControl webControl_ = new SubscriberBalancesFormWebControl();


    /**
     * {@inheritDoc}
     */
    public void execute(final Context context) throws AgentException
    {
    	
        final PrintWriter out = getWriter(context);
        final SubscriberBalancesForm form = new SubscriberBalancesForm();
        final Context subContext = context.createSubContext();
        final MessageMgr messageManager = new MessageMgr(subContext, MODULE);
        final HTMLExceptionListener listener = new HTMLExceptionListener(messageManager);
        final ButtonRenderer buttonRenderer =
            (ButtonRenderer) context.get(
                    ButtonRenderer.class,
                    DefaultButtonRenderer.instance());

        subContext.put(ExceptionListener.class, listener);
        boolean displayMessage = false;

        subContext.put("MODE", OutputWebControl.EDIT_MODE);
        
        if (buttonRenderer.isButton(context, RUN_BUTTON))
        {
        	displayMessage = false;
            this.webControl_.fromWeb(subContext, form, getRequest(context), "");
            
        	LogSupport.info(context, this, "Process to ger Subscriber balances started @ " + new Date());
        	final ThreadPool subscriberBalancesThreadPool = new ThreadPool("SUBSCRIBER_BALANCES_THREADPOOL", form.getQueue(), form.getThreads(), new SubscriberBalancesProcessorAgent());
            
            try
            {
            	Map<String, SubscriberBalances> subscriberBalancesMap = new HashMap<String, SubscriberBalances>();
        		PostpaidSubscriberBalancesFileWriter postpaidSubscriberBalancesFileWriter = new PostpaidSubscriberBalancesFileWriter();
        		try 
        		{
        			postpaidSubscriberBalancesFileWriter.init(context, form.getSubscriberBalancesDir(), getCsvFileName());
        		} 
        		catch (Exception e) 
        		{
        			if(LogSupport.isDebugEnabled(context))
        			{
        				LogSupport.debug(context, this, "Unbale to initilize file writer" + e.getMessage());
        			}
        		}

        		try
        		{
        			Map<String, String> subcriberMsisdn = SubscriberSupport.getSubscriberMsisdn(context, form.getSpid(), form.getCreditCategory(), form.getBillCycleID(), form.getSubscriptionType());
        			Iterator iterator = subcriberMsisdn.keySet().iterator();
        			while(iterator.hasNext()) 
        			{
        				String subscriberId = (String)iterator.next();
        				String msisdn = (String)subcriberMsisdn.get(subscriberId);
						subContext.put(SUBSCRIBER_ID, subscriberId);
						subContext.put(SUBSCRIBER_MSISDN, msisdn);
						subContext.put(SUBSCRIBER_BALANCES_MAP, subscriberBalancesMap);
						subContext.put(SUBSCRIBER_BALANCES_FORM, form);
						subscriberBalancesThreadPool.execute(subContext);
        			}
        			subscriberBalancesMap = (Map<String, SubscriberBalances>)subContext.get(SubscriberBalancesWebAgent.SUBSCRIBER_BALANCES_MAP);
        			postpaidSubscriberBalancesFileWriter.printLine(context, subscriberBalancesMap);
        			displayMessage = true;
        			LogSupport.info(context, this, "Process to ger Subscriber balances ended @ " + new Date());
        		}
        		catch (final Throwable throwable)
        		{
        			if(LogSupport.isDebugEnabled(context))
        			{
        				LogSupport.debug(context, this, "Exception while writing in csv file" + throwable.getMessage());
        			}
        		}
        		finally
        		{
        			postpaidSubscriberBalancesFileWriter.close();
        		}
            }
            catch (final Throwable throwable)
            {
                listener.thrown(throwable);
            }

        }

        final FormRenderer renderer = (FormRenderer) context.get(FormRenderer.class, DefaultFormRenderer.instance());
        renderer.Form(out, context);

        if (listener.hasErrors())
        {
            listener.toWeb(subContext, out, "", form);
        }
        else if (displayMessage)
        {
            out.print("<pre><center><font size=\"1\" face=\"Verdana\" color=\"green\">");
            out.print("<b>Process to get Subscriber balances finished.</b></font></center></pre>");
        }

        out.println("<table><tr><td>");
        this.webControl_.toWeb(subContext, out, "", form);
        out.println("</td></tr></table>");
        buttonRenderer.inputButton(out, context, this.getClass(), RUN_BUTTON, false);   
        buttonRenderer.inputButton(out, context, this.getClass(), RESET_BUTTON, false);   
        renderer.FormEnd(out);
        out.println("<br />");
    }
    
    /**
     * This method returns the name of the file to write csv records.
     * 
     * @return The file name.
     */
    public static String getCsvFileName() 
    {
        balancesFilename_ = subscriptionBalancePrfix_ + System.currentTimeMillis() + ".csv";

        return balancesFilename_;
    }
    
    public static final String RUN_BUTTON  = "Run";
    public static final String RESET_BUTTON  = "Reset";
    public static final String SUBSCRIBER_ID = "SUBSCRIBER_ID";
	public static final String SUBSCRIBER_MSISDN = "SUBSCRIBER_MSISDN";
	public static final String SUBSCRIBER_BALANCES_MAP = "SUBSCRIBER_BALANCES_MAP";
	public static final String SUBSCRIBER_BALANCES_FORM = "SUBSCRIBER_BALANCES_FORM";
	private static String subscriptionBalancePrfix_ = "subscriberbalances";
	private static String balancesFilename_ = "";
	

}
