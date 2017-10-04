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
package com.trilogy.app.crm.checking.web;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.CheckingFixingForm;
import com.trilogy.app.crm.bean.CheckingFixingFormWebControl;
import com.trilogy.app.crm.bean.SubscriberReferenceTypeEnum;
import com.trilogy.app.crm.checking.AbstractIntegrityValidation;
import com.trilogy.app.crm.checking.FileMessageHandler;
import com.trilogy.app.crm.checking.IntegrityValidation;
import com.trilogy.app.crm.checking.MessageHandlerCollectionProxy;
import com.trilogy.app.crm.checking.MessageHandlerToExceptionListenerAdapter;
import com.trilogy.app.crm.checking.SimpleMessageHandler;
import com.trilogy.app.crm.util.SubscriberCollectionProcessor;
import com.trilogy.app.crm.util.SubscriberProcessingInterruptionException;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * @author lxia
 * @author skushwaha
 */
public class SubPricePlanServiceChechingServicer   implements RequestServicer
{

	public final static String DEFAULT_TITLE = "<b>PricePlan/Service Checking-Fix: </b>";
	protected String title_ = DEFAULT_TITLE;
	protected String buttonString_ = "Process";
	protected String request_ = "upload";
	WebControl wc_ = new CheckingFixingFormWebControl();



    public SubPricePlanServiceChechingServicer()
    {
    }


    public SubPricePlanServiceChechingServicer(String title)
    {
        setTitle(title);
    }

    public String getTitle()
    {
        return title_;
    }


    public void setTitle(String title)
    {
        title_ = title;
    }

	/**
	 * Returns the buttonString_.
	 * @return String
	 */
	public String getButtonString() {
		return buttonString_;
	}

	/**
	 * Returns the request_.
	 * @return String
	 */
	public String getRequest() {
		return request_;
	}

	/**
	 * Sets the buttonString_.
	 * @param buttonString The buttonString_ to set
	 */
	public void setButtonString(String buttonString) {
		this.buttonString_ = buttonString;
	}

	/**
	 * Sets the request_.
	 * @param request The request_ to set
	 */
	public void setRequest(String request) {
		this.request_ = request;
	}


    /** Template method to be overriden by subclasses as required. **/
    public void outputPreForm(Context ctx, HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
    {
      // nop
    }

    /** Template method to be overriden by subclasses as required. **/
    public void outputPostForm(Context ctx, HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
    {
      // nop
    }


    /**
     * {@inheritDoc}
     */
    public void service(
        Context ctx,
        final HttpServletRequest req,
        final HttpServletResponse res)
        throws ServletException, IOException
    {
        final PrintWriter out = res.getWriter();
        boolean showQuery = true;

        ctx = ctx.createSubContext();
        ctx.put("MODE", WebControl.EDIT_MODE);

        String action = req.getParameter("Submit");
        String url = req.getRequestURI();

        final CheckingFixingForm form = new CheckingFixingForm();

        final MessageMgr manager = new MessageMgr(ctx, this);

        final HTMLExceptionListener exceptions = new HTMLExceptionListener(manager);
        ctx.put(ExceptionListener.class, exceptions);

        final ButtonRenderer buttonRenderer =
            (ButtonRenderer)ctx.get(
                ButtonRenderer.class,
                DefaultButtonRenderer.instance());

        if (!buttonRenderer.isButton(ctx, "Clear"))
        {
            wc_.fromWeb(ctx, form, req, "");

            if (!exceptions.hasErrors() && !buttonRenderer.isButton(ctx, "Preview"))
            {
                try
                {
                	if (buttonRenderer.isButton(ctx, "OK"))
                    {
                        processForm(ctx, form, out);
    					showQuery = false;
                    }
                }
                catch (Exception exc)
                {
                    exceptions.thrown(exc);
                    new MajorLogMsg(
                        this,
                        "Unanticipated exception during subscriber processing.",
                        exc).log(ctx);
                }
            }
        }

        if (exceptions.hasErrors())
        {
            exceptions.toWeb(ctx, out, "", form);
        }

   		if ( showQuery)
        {
   			FormRenderer frend = (FormRenderer) ctx.get(FormRenderer.class, DefaultFormRenderer.instance());

   			frend.Form(out, ctx);
   			out.print("<table><tr><td>");
   			wc_.toWeb(ctx, out, "", form);

   			buttonRenderer.inputButton(out, ctx, "OK");
   			buttonRenderer.inputButton(out, ctx, "Clear");
   			buttonRenderer.inputButton(out, ctx, "Preview");

   			out.println("</td></tr></table>");

   			frend.FormEnd(out);
   			out.println("<br/>");
   		}
	}


    /**
     * Creates the FileMessageHandler for the given form.
     *
     * @param rootDirectoryName The name of the root directory of the report.
     * @param validationTypeName The name of the type of validation.
     */
    protected FileMessageHandler createFileMessageHandler(
        final String rootDirectoryName,
        final String validationTypeName)
        throws IOException
    {
        final File reportDirectory =
            new File(rootDirectoryName, validationTypeName);

        final FileMessageHandler fileHandler =
            new FileMessageHandler(reportDirectory);

        return fileHandler;
    }


    /**
     * Creates and returns a SubscriberCollectionProcessor configured for the
     * given form.  The cnfiguration is based on which subscriber reference type
     * is chosen, and the values entered into the corresponding text area.
     *
     * @param context The operating context.
     * @param form The form from which to get input configuration for the
     * processor.
     */
    protected SubscriberCollectionProcessor getProcessor(
        final Context context,
        final CheckingFixingForm form)
    {
        final SubscriberCollectionProcessor processor =
            new SubscriberCollectionProcessor(context);

        final SubscriberReferenceTypeEnum type = form.getReferenceType();

        switch (type.getIndex())
        {
            case SubscriberReferenceTypeEnum.SPID_INDEX:
            {
                final List spids = new ArrayList();
                spids.add(Integer.valueOf(form.getSpid()));
                processor.addSPIDs(spids);
                break;
            }
            case SubscriberReferenceTypeEnum.IDENTIFIER_INDEX:
            {
                processor.addIdentifiers(form.getSubscriberIdentifiers());
                break;
            }
            case SubscriberReferenceTypeEnum.MSISDN_INDEX:
            {
                processor.addMSISDNs(form.getMsisdns());
                break;
            }
            case SubscriberReferenceTypeEnum.ACCOUNT_INDEX:
            {
            	processor.addAccounts(form.getAccountIdentifiers());
            	break;
            }
            default:
            {
                // TODO - 2004-10-18 - Throw a custom exception here.
            }
        }

        return processor;
    }





    /**
     * Processes the form and takes the appropriate actions of applying the
     * selected validation action to the referenced subscribers.
     *
     * @param context The operating context.
     * @param form The form containing the parameters of the activity.
     * @param out The writer to which output should be sent.
     */
    protected void processForm(
        final Context context,
        final CheckingFixingForm form,
        final PrintWriter out)
        throws SubscriberProcessingInterruptionException
    {
        final MessageHandlerCollectionProxy handler =
            new MessageHandlerCollectionProxy(context);

        final ExceptionListener exceptionListener =
            new MessageHandlerToExceptionListenerAdapter(handler);

        final SimpleMessageHandler simpleHandler = new SimpleMessageHandler();
        handler.addHandler(simpleHandler);

        FileMessageHandler fileHandler;

        try
        {
            fileHandler =
                createFileMessageHandler(
                    form.getReportDirectory(),
                    form.getID().getDescription());

            handler.addHandler(fileHandler);
        }
        catch (final IOException exception)
        {
            fileHandler = null;
            exceptionListener.thrown(exception);
        }

        final IntegrityValidation validator = AbstractIntegrityValidation.getValidator(form.getID());

        validator.setMessageHandler(handler);
        validator.setRepairEnabled(form.isFix());

        final SubscriberCollectionProcessor processor =
            getProcessor(context, form);

        processor.setExceptionListener(exceptionListener);

        try
        {
            processor.process(validator);
            validator.printResults();

            out.println("<pre>");
            out.println(simpleHandler.getMessage());
            out.println("</pre>");
        }
        finally
        {
            if (fileHandler != null)
            {
                fileHandler.close();
            }
        }
    }

} // class
