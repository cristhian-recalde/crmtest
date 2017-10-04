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
package com.trilogy.app.crm.web.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.FacetMgr;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.BeanWebController;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.SubBulkCreate;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.hlr.CrmHlrServiceImpl;
import com.trilogy.app.crm.numbermgn.GenericPackage;
import com.trilogy.app.crm.numbermgn.MsisdnAlreadyAcquiredException;
import com.trilogy.app.crm.numbermgn.MsisdnManagement;
import com.trilogy.app.crm.numbermgn.NumberMgnSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.web.control.SubBulkCreateWebControlEx;

/**
 * This class is a http request servicer response for bulk creating subscribers
 * from mdn group and UIM group, it is for cdma only
 *
 * This class is only supposed to create prepaid subscribers. Thus msisdns should be filtered 
 * by the prepaid type then.
 *
 * @author jchen
 */
public class SubBulkCreateRequestServicer
    implements RequestServicer
{
   
    public  final static String DEFAULT_TITLE = "<b>MSISDN Bulk File: </b>";
    private final static String BUTTON_CREATE = "Create";


    private static WebControl wc_ = null;
    protected String title_        = DEFAULT_TITLE;
    protected String buttonString_ = "Process";
    protected String request_      = "upload";
    
    
    private void setWebControl(Context ctx){
    	FacetMgr fmgr = (FacetMgr) ctx.get(FacetMgr.class);
    	wc_= (WebControl)fmgr.getInstanceOf(ctx, SubBulkCreate.class, WebControl.class);
        
    }
    
    public SubBulkCreateRequestServicer()
    {   
    }


    public SubBulkCreateRequestServicer(String title)
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
    public String getButtonString()
    {
        return buttonString_;
    }


    /**
     * Returns the request_.
     * @return String
     */
    public String getRequest()
    {
        return request_;
    }


    /**
     * Sets the buttonString_.
     * @param buttonString The buttonString_ to set
     */
    public void setButtonString(String buttonString)
    {
        this.buttonString_ = buttonString;
    }


    /**
     * Sets the request_.
     * @param request The request_ to set
     */
    public void setRequest(String request)
    {
        this.request_ = request;
    }


    /** Template method to be overriden by subclasses as required. **/
    public void outputPreForm(Context ctx, HttpServletRequest req, HttpServletResponse res)
    {
        // nop
    }


    /** Template method to be overriden by subclasses as required. **/
    public void outputPostForm(Context ctx, HttpServletRequest req, HttpServletResponse res)
    {
        // nop
    }


    @Override
    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        PrintWriter out = res.getWriter();
        
        ctx = ctx.createSubContext();
        
		/*
		 * [Cindy Wong] 2011-02-11: The following code doesn't even make sense,
		 * selecting both individual=true and individual=false.
		 */
        
// Home acctTypeHome = (Home) ctx.get(AccountTypeHome.class);
		//
		// // Only take 'individual' account types
		// ctx.put(
		// AccountTypeHome.class,
		// acctTypeHome.where(
		// ctx,
		// new Or()
		// .add(new EQ(AccountTypeXInfo.INDIVIDUAL, Boolean.TRUE))
		// .add(new EQ(AccountTypeXInfo.INDIVIDUAL, Boolean.FALSE))
		// ));
		//
        // CREATE mode is required so that the Spid is editable
        ctx.put("MODE", OutputWebControl.CREATE_MODE);

        final SubBulkCreate form = new SubBulkCreate();
        try
        {
            form.setAccount((Account)XBeans.instantiate(Account.class, ctx));
        }
        catch (final Exception exception)
        {
            new MinorLogMsg(
                this,
                "Failed to override default instantiation of account.",
                exception).log(ctx);
        }

        final MessageMgr manager = new MessageMgr(ctx, this);

        final HTMLExceptionListener exceptions = new HTMLExceptionListener(manager);
        
        ctx.put(ExceptionListener.class, exceptions);

        final ButtonRenderer buttonRenderer =
            (ButtonRenderer)ctx.get(
                ButtonRenderer.class,
                DefaultButtonRenderer.instance());

        setWebControl(ctx);
        
        if ( ! buttonRenderer.isButton(ctx, "Clear") )
        {
            wc_.fromWeb(ctx, form, req, "");
            
            if (form.getTechnology() == null)
            {
            	form.setTechnology(getTechnology(ctx));
            }
            
            // Bulk provisioning is only for prepaid
            form.getAccount().setSystemType(SubscriberTypeEnum.PREPAID);
            
            if ( ! exceptions.hasErrors() )
            {
                try
                {
                   if (buttonRenderer.isButton(ctx, "Preview"))
                   {
                       validateInput(ctx, form);
                   }
                   else if (buttonRenderer.isButton(ctx, BUTTON_CREATE))
                   {
                       validateInput(ctx, form);
                       bulkCreate(ctx, out, form);
                   }
                }
                catch (Exception exc)
                {
                	out.println("<pre>");
                	out.println("Error: " + exc.getMessage());
                	out.println("</pre>");
                	exceptions.thrown(exc);
                }
            }
        }

        FormRenderer frend = (FormRenderer) ctx.get(FormRenderer.class, DefaultFormRenderer.instance());

        frend.Form(out, ctx);
        out.print("<table><tr><td>");
        
        AbstractWebControl.setMode(ctx, "Account.systemType", ViewModeEnum.NONE);
        
        wc_.toWeb(ctx, out, "", form);

        out.print("</td></tr><tr><th align=\"right\">");

        buttonRenderer.inputButton(out, ctx, this.getClass(), "Preview", false);
        buttonRenderer.inputButton(out, ctx, this.getClass(), BUTTON_CREATE, false);
        outputHelpLink(ctx, out, buttonRenderer);

        out.println("</th></tr></table>");

        frend.FormEnd(out);
        out.println("<br/>");
    }


    private TechnologyEnum getTechnology(Context ctx)
    {
    	TechnologyEnum techEnum = null;
        LicenseMgr lMgr = (LicenseMgr) ctx.get(LicenseMgr.class);
        if (lMgr != null && lMgr.isLicensed(ctx, LicenseConstants.TDMA_CDMA_LICENSE_KEY)
                && lMgr.isLicensed(ctx, LicenseConstants.GSM_LICENSE_KEY))
        {
        	techEnum = TechnologyEnum.GSM;
            
        	if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg("License for CDMA, TDMA and GSM Technologies are Active", null, null).log(ctx);
            }
        }
        else if (lMgr != null && lMgr.isLicensed(ctx, LicenseConstants.GSM_LICENSE_KEY))
        {
        	techEnum = TechnologyEnum.GSM;

            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg("License for GSM Technology is Active", null, null).log(ctx);
            }
        }
        else if (lMgr != null && lMgr.isLicensed(ctx, LicenseConstants.TDMA_CDMA_LICENSE_KEY))
        {
        	techEnum = TechnologyEnum.CDMA;

            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg("License for CDMA and TDMA Technologies are Active", null, null).log(ctx);
            }
        }
        else
        {
            new MajorLogMsg(this, "No license defined for GSM/TDMA/CDMA, either one of them should be enabled",
                    null).log(ctx);
        }
        
        return techEnum;
	}


    private void validateInput(final Context ctx, final SubBulkCreate form)
        throws IllegalStateException
    {
        try
        {
            //validate start msisdn first
            validateStartMsisdn(ctx, form);
            
            //validation of sub cnt requires valide start msisdn
            long subCnt = SubBulkCreateWebControlEx.validateCalcMatchSubscribers(ctx, form);
            //form.setSubCnt(subCnt);
            if (subCnt < 1)
            {
                throw new IllegalStateException(" No Mobile number or Package available.");
            }
                
            if (form.getSubNumToCreate() > subCnt)
            {
            	throw new IllegalStateException(" <font color='red'>Maximum Number of Subscribers Allowed to Create is: " + subCnt + " Please try again </font>");
            }
        }
        catch(IllegalStateException ie)
		{
            throw ie;
		}
        catch(Exception e)
        {
            throw new IllegalStateException("Invalide input or internal error: " + e);
        }
    }
    
    
    /**
     * Validate start msisdn is within the msisdnGroup
     * @param ctx
     * @param form
     */
    private void validateStartMsisdn(Context ctx, final SubBulkCreate form)
    {
        Msisdn firstMsisdn =null;
        //Msisdn lastMsisdn = null;
        
        String start = "";
        //String end = "";
        try
        {
	        String msisdn = form.getStartingMsisdn();
	        if (msisdn != null && msisdn.length() > 0)
	        {
	            firstMsisdn = NumberMgnSupport.getFirstAvailMsisdn(ctx, form.getSpid(), form.getMsisdnGroup(),SubscriberTypeEnum.PREPAID_INDEX);
	            if (firstMsisdn == null)
	            {
	                throw new IllegalStateException("No available number in Mobile Number group.");
	            }
	            
				start = firstMsisdn.getMsisdn() ;
				if (start.compareTo(form.getStartingMsisdn()) > 0)
				    throw new IllegalStateException("Starting mobile number should greater than " + start);
	            
//	            lastMsisdn  = NumberMgnSupport.getLastAvailMsisdn(ctx, form.getSpid(), form.getMsisdnGroup());
//	            end = lastMsisdn.getMsisdn() ;
//	            if (end.compareTo(form.getStartingMsisdn()) < 0)
//                    throw new IllegalStateException("Starting mobile number should not greater than " + end);
	        }
        }
        catch( IllegalStateException ie)
        {
            throw ie;
        }
        catch(Exception e)
        {
            throw new IllegalStateException("Internal error:" + e);
            
        }
    }
    
    
    /**
     * Gets 
     * @param ctx
     * @param form
     * @return
     * @throws HomeException
     */
    Collection getAvailMsisdns(Context ctx, final SubBulkCreate form) throws HomeException
    {
        return NumberMgnSupport.getAvailMsisdns(ctx, form.getSpid(), 
                					form.getMsisdnGroup(), 
                					SubscriberTypeEnum.PREPAID_INDEX,
                					form.getStartingMsisdn(),
                					-1, false);	
    }
        
    
    /**
     * 
     * @param ctx
     * @param form
     * @return
     * @throws HomeException
     */
    Collection getAvailPackages(Context ctx, final SubBulkCreate form) throws HomeException
    {
        final Collection packages =
            NumberMgnSupport.getAvailPackages(
                ctx,
                form.getTechnology(),
                form.getSpid(),
                form.getPackageGroup(),
                -1);

        return packages;
    }

    
    /**
     * Get MSISDN table name from jdbc home, instead of hardcoding
     * @param ctx
     * @return
     */
    public PrintStream getErrorPrintStream(
            Context ctx,
            SubBulkCreate form)
            throws IOException
        {
            File errDir = new File(form.getErrfile());
            if ( ! errDir.exists() )
            {
                if (!errDir.mkdir())
                {
                    throw new IOException( "can not create find and create error file directory when bulk load misdn");
                }
            }

            return  new PrintStream( new FileOutputStream(
                                         new File( form.getErrfile() + File.separator
                                                   + "SubBulkCreate_" + form.getMsisdnGroup()  + "-" + form.getPackageGroup() + "-" + form.getStartingMsisdn())));
        }

        
    /**
     * 
     * @param ctx
     * @param form
     * @return
     * @throws IOException
     * @throws NumberFormatException
     * @throws HomeException
     */
	public int createSubscribers(Context context, final SubBulkCreate form)
	    throws IOException, NumberFormatException, HomeException
    {
		Context ctx = context.createSubContext();
        Home acctHome    = (Home) ctx.get(AccountHome.class);
        Home subHome     = (Home) ctx.get(SubscriberHome.class);
        Home msisdnHome  = (Home) ctx.get(MsisdnHome.class);
        
        //we know it is ArrayList for the collection from FW.
        List msisdnList  = (List)getAvailMsisdns(ctx, form);
        List packageList = (List)getAvailPackages(ctx, form);
        long size        = Math.min(msisdnList.size(), packageList.size());
        
        size = Math.min(size, form.getSubNumToCreate());
        
        int subCnt     = 0;
        int subCreated = 0;
        int errCnt     = 0;
        
		boolean enableHlr = form.isEnableHlr();

		if (!enableHlr)
		{
			new DebugLogMsg(this, "Disabling HLR on subscription bulkloading",
			    null).log(ctx);
			ctx.put(CrmHlrServiceImpl.HLR_SKIPPED,
					CrmHlrServiceImpl.HLR_SKIPPED);
		}

		Account formAccount = form.getAccount();
        Subscriber formSubscriber = formAccount.getSubscriber();
        formAccount.setSubscriber(null);

        for ( subCnt = 0 ; subCnt < size ; subCnt++ )
        {
            boolean    created = false;
            Subscriber sub     = null;
            
            try
            {
                Account account = (Account) formAccount.clone();
                sub = (Subscriber) formSubscriber.clone();
                account.setSubscriber(null);
                account.setContext(ctx);
                sub.setContext(ctx);
                
                account = (Account) acctHome.create(account);

                sub.setBAN(account.getBAN());
                
                int     spid          = form.getSpid();
                Msisdn  msisdnRecord  = (Msisdn)  msisdnList.get(subCnt);
                GenericPackage packageRecord = (GenericPackage) packageList.get(subCnt);
                
                sub.setSpid(spid);
                sub.setMSISDN(msisdnRecord.getMsisdn());
                sub.setPackageId(packageRecord.getPackId());
                sub.setSubscriberType(SubscriberTypeEnum.PREPAID);
                sub.setTechnology(form.getTechnology());
                
                sub.setSatId(form.getSatId());
                sub.setDealerCode(packageRecord.getDealer());
                
                SubscriberSupport.applySubServiceActivationTemplate(ctx, sub, form.getSatId());

                sub.setDeposit(0);

                // this removes any provisioned services that might have been set using the template
                sub.resetTransientProvisionedServices();
                
                sub = (Subscriber) subHome.create(sub);
                
                //update Msisdn record
                try
                {
                    MsisdnManagement.claimMsisdn(ctx, msisdnRecord.getMsisdn(), account.getBAN(), msisdnRecord.getExternal(), "CRM - Subscriber Bulkloader");
                }
                catch(MsisdnAlreadyAcquiredException e)
                {
                    // ignore
                }
                MsisdnManagement.associateMsisdnWithSubscription(ctx, msisdnRecord.getMsisdn(), sub.getId(), "voiceMsisdn");
                
                // provision exception will not be able to hanlde correctly.
                if ( sub.getLastExp() != null )
                {
                   throw new HomeException("" + sub.getLastExp(), sub.getLastExp());
                }
                
	            created = true;
	            subCreated++;
            }
            catch(Exception e)
            {
                errCnt++;
                
                if ( errCnt > 10 )
                {
                    new MajorLogMsg(this, "fail bulk create subscribers, too many error", null).log(ctx);
                }
                
                new MajorLogMsg(this, "fail to create subscriber, Msisdn" + msisdnList.get(subCnt) + ", package="  + packageList.get(subCnt), e).log(ctx);
            }
            finally
            {
                getErrorPrintStream(ctx, form)
                		.println(((Msisdn)msisdnList.get(subCnt)).getMsisdn()
                			+ ", " + ((GenericPackage)packageList.get(subCnt)).getPackId()
                			+ ", " + (sub == null ? "null" : sub.getId())	
                			+ ", " + created);               
            }
        }

        return subCreated;
    }

    public void bulkCreate(Context ctx, PrintWriter out, SubBulkCreate mk)
    {
       try
       {
          int subCnts = createSubscribers(ctx, mk);
          out.println("<H3>Total of " + subCnts + " subscribers successfully created.</H3>");
       }
       catch ( Exception e )
       {
          out.println("<pre>");
          e.printStackTrace(out);
          out.println("</pre>");
       }
     }

    /**
     * Calls com.redknee.framework.xhome.webcontrol.BeanWebController.outputHelpLink()
     *
     * @param context the current context
     * @param out the current PrintWriter
     */
    private void outputHelpLink(final Context context, final PrintWriter out, final ButtonRenderer buttonRenderer)
    {
    	// in the future we might need to specify the HttpServletRequest and HttpServletResponse
        BeanWebController.outputHelpLink(context, null, null, out, buttonRenderer);
    }
} // class
