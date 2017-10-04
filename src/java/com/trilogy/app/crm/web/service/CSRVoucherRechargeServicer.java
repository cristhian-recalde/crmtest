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

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.api.Constants;
import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.bean.ManualVourcherRechargingForm;
import com.trilogy.app.crm.bean.ManualVourcherRechargingFormWebControl;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.client.urcs.UrcsClientInstall;
import com.trilogy.app.crm.config.CRMConfigInfoForVRA;
import com.trilogy.app.crm.support.VRASupport;
import com.trilogy.app.crm.external.vra.PricePlanVoucherTypeMappingServiceRmiClient;
import com.trilogy.app.crm.external.vra.VoucherRedemptionServiceRmiClient;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.vra.interfaces.PricePlanVoucherTypeMapping;
import com.trilogy.app.vra.interfaces.PricePlanVoucherTypeMappingList;
import com.trilogy.app.vra.interfaces.PricePlanVoucherTypeMappingService;
import com.trilogy.app.vra.interfaces.VRAResultCodes;
import com.trilogy.app.vra.interfaces.VoucherRechargeInput;
import com.trilogy.app.vra.interfaces.VoucherRechargeOutput;
import com.trilogy.app.vra.interfaces.VoucherRedemptionException;
import com.trilogy.app.vra.util.InvalidParamSetException;
import com.trilogy.app.vra.util.ParamSetKeys;
import com.trilogy.app.vra.util.ParamSetParser;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultTableRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.web.renderer.TableRenderer;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.ERLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.app.crm.config.VRALoginConfig;

/**
 * RequestService to manaually process voucher recharging.
 *
 * @Author: lily.zou@redknee.com
 */

public class CSRVoucherRechargeServicer implements RequestServicer
{
    private static final String ERROR_MSG_182 = "Error 182: Invalid voucher type.";
    private static final String ERROR_MSG_TITLE = "Error";

    private static final String EMPTY_DATE_STR = "---";
    
    // error msgs
    private static final String EXCEPTION_ROOT_MSG = "Exception occured during ";
    private static final String VOUCHER_RECHARGING = "voucher recharging";
    private static final String VOUCHER_VALIDATING = "voucher validating";
    private static final String VOUCHER_VALIDATION_TITLE = "Voucher Validation";
    private static final String VOUCHER_RECHARGE_PROCESS_FAILED = "Voucher Recharge Process failed.";
    private static final String FAILED_TO_GET_MAPPING_LIST = "Failed to retrieve PricePlan-VoutcherType mapping list.";
    
    // context keys
    public static final String VRA_VOUCHER_VALIDATING_PRICE_PLAN = "VRA Voucher Validating Price Plan";
    public static final String VRA_INVALID_VOUCHER = "VRA Voucher Validating Failure";
    
    // buttons
    private static final String BUTTON_VALIDATE = "Validate";
    private static final String BUTTON_CLEAR = "Clear";
    private static final String BUTTON_PROCESS = "Process";
    
    public static final String VOUCHAR_RECHARGING_SUCCESS_RESULT = "VRA_SUCCESS_RESULT";
    public static final String DEFAULT_TITLE = "<b>Manual Vourcher Recharging</b>";

    protected static final int ER_IDENTIFIER = 1101;
    protected static final int ER_CLASS = 1100;
    protected static final String ER_TITLE = "CRM Voucher Recharging Event";
    
    private String title_ = DEFAULT_TITLE;
    private String buttonString_ = BUTTON_PROCESS;
    private String request_ = BUTTON_PROCESS;
    
    private WebControl wc_ = new ManualVourcherRechargingFormWebControl();

    public CSRVoucherRechargeServicer()
    {
    }

    public CSRVoucherRechargeServicer(final String title)
    {
        setTitle(title);
    }

    public String getTitle()
    {
        return title_;
    }

    public void setTitle(final String title)
    {
        title_ = title;
    }

    /**
     * Returns the buttonString_.
     *
     * @return String
     */
    public String getButtonString()
    {
        return buttonString_;
    }

    /**
     * Returns the request_.
     *
     * @return String
     */
    public String getRequest()
    {
        return request_;
    }

    /**
     * Sets the buttonString_.
     *
     * @param buttonString The buttonString_ to set
     */
    public void setButtonString(final String buttonString)
    {
        this.buttonString_ = buttonString;
    }

    /**
     * Sets the request_.
     *
     * @param request The request_ to set
     */
    public void setRequest(final String request)
    {
        this.request_ = request;
    }

    /**
     * Template method to be overriden by subclasses as required. *
     */
    public void outputPreForm(final Context ctx, final HttpServletRequest req, final HttpServletResponse res)
    {
        // nop
    }

    /**
     * Template method to be overriden by subclasses as required. *
     */
    public void outputPostForm(final Context ctx, final HttpServletRequest req, final HttpServletResponse res)
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

        ctx = ctx.createSubContext();
        ctx.put("MODE", OutputWebControl.EDIT_MODE);

        final ManualVourcherRechargingForm form = new ManualVourcherRechargingForm();

        final MessageMgr manager = new MessageMgr(ctx, this);

        final HTMLExceptionListener exceptions = new HTMLExceptionListener(manager);
        ctx.put(ExceptionListener.class, exceptions);

        final ButtonRenderer buttonRenderer;
        buttonRenderer = (ButtonRenderer) ctx.get(ButtonRenderer.class, DefaultButtonRenderer.instance());

        if (buttonRenderer.isButton(ctx, BUTTON_VALIDATE) || 
            buttonRenderer.isButton(ctx, BUTTON_PROCESS))
        {
            boolean bValidate = buttonRenderer.isButton(ctx, BUTTON_VALIDATE);
            final String processName = bValidate?VOUCHER_VALIDATING:VOUCHER_RECHARGING;

            wc_.fromWeb(ctx, form, req, "");
            
            boolean bSuccess = true;
            
            try
            {
                bSuccess = processForm(ctx, form, bValidate);
            }
            catch (Exception e)
            {
                bSuccess = false;
                processException(exceptions, e);
                LogSupport.minor(ctx, this, EXCEPTION_ROOT_MSG + processName, e);
            }
            
            showResults(ctx, out, form, exceptions, buttonRenderer, bSuccess);
        }

        showQueryForm(ctx, out, form, buttonRenderer);
    }


    private void showResults(Context ctx, final PrintWriter out, final ManualVourcherRechargingForm form,
            final HTMLExceptionListener exceptions, final ButtonRenderer buttonRenderer, boolean bSuccess)
    {
        if (bSuccess)
        {
            if (!buttonRenderer.isButton(ctx, BUTTON_CLEAR))
            {
                if (buttonRenderer.isButton(ctx, BUTTON_PROCESS))
                {
                    final TableRenderer tr = (TableRenderer) ctx.get(TableRenderer.class, DefaultTableRenderer.instance());
                    
                    final VoucherRechargeOutput output = 
                        (VoucherRechargeOutput) ctx.get(VOUCHAR_RECHARGING_SUCCESS_RESULT);
                    if (output != null)
                    {
                        outputSuccessVRAInfo(ctx, out, output, tr);
                        ctx.put(VOUCHAR_RECHARGING_SUCCESS_RESULT, null);
                    }
                }
                else if (buttonRenderer.isButton(ctx, BUTTON_VALIDATE))
                {
                    showMessageBox(ctx, out, VOUCHER_VALIDATION_TITLE, "Voucher validation succeeded!");
                }
            }
        }
        else
        {
            boolean bInvalidVoucher = ctx.getBoolean(CSRVoucherRechargeServicer.VRA_INVALID_VOUCHER, false);
            
            if (bInvalidVoucher)
            {
                ErrorMsgHolder errors = new ErrorMsgHolder();
                PricePlanVoucherTypeMapping[] mapList = retrievePricePlanVoucherTypeMappingList(ctx, out, errors);
                
                if (mapList!=null && mapList.length>0)
                {
                    showMessageBox(ctx, out, ERROR_MSG_TITLE, ERROR_MSG_182);
                    out.print("<br><br>");
                    
                    final long pricePlanId = ctx.getLong(CSRVoucherRechargeServicer.VRA_VOUCHER_VALIDATING_PRICE_PLAN);
                    String pricePlan = String.format("%d - %s", pricePlanId, mapList[0].getPricePlanDescription());
                    displayMappingList(ctx, out, mapList, pricePlan);
                }
                else
                {
                    String msg = ERROR_MSG_182 + (errors.hasError() ? errors.getError() : "");
                    showMessageBox(ctx, out, ERROR_MSG_TITLE, msg);
                }
            }
            else
            {
                if (exceptions.hasErrors())
                {
                    exceptions.toWeb(ctx, out, "", form);
                }
            }
        }
        
        // make some space
        out.print("<br><br><br>");
    }
    
    private static final class ErrorMsgHolder
    {
        private String _msg = null;
        
        public void postError(String msg)
        {
            _msg = msg;
        }
        
        public String getError()
        {
            return _msg;
        }
        
        public boolean hasError()
        {
            return _msg != null;
        }
    }

    private void showQueryForm(Context ctx, final PrintWriter out, final ManualVourcherRechargingForm form,
            final ButtonRenderer buttonRenderer)
    {
        final FormRenderer frend = (FormRenderer) ctx.get(FormRenderer.class, DefaultFormRenderer.instance());

        frend.Form(out, ctx);
        out.print("<table><tr><td>");
        wc_.toWeb(ctx, out, "", form);

        buttonRenderer.inputButton(out, ctx, BUTTON_PROCESS);
        buttonRenderer.inputButton(out, ctx, BUTTON_VALIDATE);
        buttonRenderer.inputButton(out, ctx, BUTTON_CLEAR);

        out.println("</td></tr></table>");

        frend.FormEnd(out);
        out.println("<br/>");
    }

    /**
     * Processes the form and takes the appropriate actions of applying the
     * selected validation action to the referenced subscribers.
     *
     * @param context The operating context.
     * @param form The form containing the parameters of the activity.
     */
    public static boolean processForm(
            final Context context,
            final ManualVourcherRechargingForm form,
            final boolean bValidatingOnly)
        throws VoucherRedemptionException, HomeException
    {
        boolean bSuccess = false;
        
        final ExceptionListener listener = (ExceptionListener) context.get(ExceptionListener.class);

        final Subscriber subscriber = SubscriberSupport.lookupSubscriberForMSISDN(context, form.getMSISDN());
        
        SysFeatureCfg config = (SysFeatureCfg) context.get(SysFeatureCfg.class);

        if (subscriber == null)
        {
            throw new IllegalArgumentException(
                    String.format("Could not find MSISDN record for [%s].", form.getMSISDN()));
        }
        // barred - LOCKED
        // deactivated - INACTIVE
        else if ((subscriber.getState().equals(SubscriberStateEnum.LOCKED)
                || subscriber.getState().equals(SubscriberStateEnum.INACTIVE)) && !config.isEnableVoucherRechargeForLockedSubscribers())
        {
            throw new IllegalArgumentException("Subscriber is in " + subscriber.getState().getDescription()
                    + " state. Voucher Recharging is not allowed. ");
        }

        VoucherRedemptionServiceRmiClient client = (VoucherRedemptionServiceRmiClient) context.get(UrcsClientInstall.VOUCHER_REDEPTION_SERVICE_RMI_KEY);
        if( client != null )
        {
            VoucherRechargeInput rechargeInput = null;

            final VRALoginConfig vraLoginConfig = (VRALoginConfig) context.get(VRALoginConfig.class);
            final CRMConfigInfoForVRA vraCrmInfoConfig = VRASupport.getCRMConfigInfoForVRA(context,subscriber.getSpid());

            // Assemble the VRA input parameter
            rechargeInput = new VoucherRechargeInput();

            // VMS user name
            rechargeInput.setUserName(vraLoginConfig.getUserName());
            // VMS password
            rechargeInput.setPassword(vraLoginConfig.getPassword(context));
            // subscriber MSISDN
            rechargeInput.setMsisdn(Long.parseLong(form.getMSISDN()));
            // subscriber Ref ID
            rechargeInput.setRefID(subscriber.getId());
            // subscriber Location, required Comverse by VRA app , value might be Comverse, CrmRedknee or Canada
            rechargeInput.setSubLocation(vraCrmInfoConfig.getSubLocation());
            // voucher Number
            rechargeInput.setVoucherNum(form.getVoucherNum());
            // voucher Location, could be 1 or 2 required by VRA app
            rechargeInput.setVoucherLocation(vraCrmInfoConfig.getVoucherLocation());
            rechargeInput.setHomeLocation(vraCrmInfoConfig.getHomeLocation());
            rechargeInput.setCallingMsisdn(rechargeInput.getMsisdn());

            
            long pricePlanId = subscriber.getPricePlan();
            context.put(VRA_VOUCHER_VALIDATING_PRICE_PLAN, pricePlanId);
            
            Integer category_id = context.getInt(APIGenericParameterSupport.CATEGORY_ID, -1);
            Boolean isSecondaryBalance = context.getBoolean(APIGenericParameterSupport.SECONDARY_BALANCE);
            Boolean applyTax =  context.getBoolean(APIGenericParameterSupport.APPLY_TAX);
            
            ParamSetParser paramParser = 
                new ParamSetParser(ParamSetKeys.DEFAULT_ASSIGNMENT_OPERATOR, 
                ParamSetKeys.DEFAULT_SEPERATOR);
            Map<String,String> paramMap = new HashMap<String,String>();
            paramMap.put(ParamSetKeys.IS_PCC_CALL, String.valueOf(false));
            paramMap.put(ParamSetKeys.PRICE_PLAN, String.valueOf(pricePlanId));
            paramMap.put(ParamSetKeys.VALIDATE_ONLY, String.valueOf(bValidatingOnly));
            paramMap.put(ParamSetKeys.CATEGORY_ID, String.valueOf(category_id));
            paramMap.put(ParamSetKeys.IS_SECONDARY_BALANCE, String.valueOf(isSecondaryBalance));
            paramMap.put(ParamSetKeys.APPLY_TAX, String.valueOf(applyTax));
            
            try
            {
                String inParamSet = paramParser.parseParamSet(paramMap);
                rechargeInput.setInParamSet(inParamSet);
            }
            catch (InvalidParamSetException e1)
            {
                LogSupport.minor(context, 
                    CSRVoucherRechargeServicer.class.getName(), "Invalid parameter found when constructing paramset.");
            }
            
            generateOM(context, Common.OM_VOUCHER_RECHARGE_ATTEMPT);

            // Make the call to VRA
            final VoucherRechargeOutput output = client.voucherRedeem(rechargeInput);
            
            short resultCode = output.getResultCode();
            bSuccess = resultCode == 0;
            
            if (bSuccess)
            {
                new InfoLogMsg(
                        CSRVoucherRechargeServicer.class,
                        "Obtained VourcherRechargeOutput From VRA: \"" + output + "\".",
                        null).log(context);

                context.put(VOUCHAR_RECHARGING_SUCCESS_RESULT, output);

                generateOM(context, Common.OM_VOUCHER_RECHARGE_SUCCESS);
            }
            else
            {
                if (isInvalidVoucher(resultCode))
                {
                    context.put(VRA_INVALID_VOUCHER, true);
                }
                
                //something went wrong within VRA Application
                final String msg = String.format("Error %d: %s", resultCode, VRAReturnCodeMsgMapping.getMessage(context, resultCode));
                postErrorMessage(listener, msg);
                
                generateOM(context, Common.OM_VOUCHER_RECHARGE_FAILURE);
            }
            
            generateER(context, subscriber, form.getVoucherNum(), resultCode);
        }
        else
        {
            String msg = VOUCHER_RECHARGE_PROCESS_FAILED + " VRA is not reachable (RMI Client null).";
            postErrorMessage(listener, msg);
            LogSupport.minor(context, CSRVoucherRechargeServicer.class, msg);
        }
        
        return bSuccess;
    }

    private static boolean isInvalidVoucher(short resultCode)
    {
        return resultCode==VRAResultCodes.INVALID_VOUCHER_TYPE;
            //|| resultCode==VRAResultCodes.VOUCHER_TYPE_NOT_FOUND;
    }

    private static void processException(final ExceptionListener listener, final Exception e)
    {
        listener.thrown(e);

        /*
        new MajorLogMsg(this, e.getMessage(), e).log(ctx);
        */
    }

    private void outputSuccessVRAInfo(final Context ctx, final PrintWriter out, final VoucherRechargeOutput output, final TableRenderer tr)
    {
        out.println("<table><tr><td>");
        tr.Table(ctx, out, "Redeem Succeeded!");

        tr.TR(ctx,out, null, 0);
        tr.TD(ctx,out);
        out.print("Current Balance");
        tr.TDEnd(ctx,out);
        tr.TD(ctx,out);
        out.print(output.getNewBalance());
        tr.TDEnd(ctx,out);
        tr.TREnd(ctx,out);

        tr.TR(ctx,out, null, 1);
        tr.TD(ctx,out);
        out.print("Current Expired Date");
        tr.TDEnd(ctx,out);
        tr.TD(ctx,out);
        out.print(output.getNewExpiry());
        tr.TDEnd(ctx,out);
        tr.TREnd(ctx,out);

        tr.TableEnd(ctx, out);
        out.println("</td></tr></table>");
    }
    
    static private void showMessageBox(Context ctx, PrintWriter out, String title, String msg)
    {
        final TableRenderer tr = (TableRenderer) ctx.get(TableRenderer.class, DefaultTableRenderer.instance());
        
        tr.Table(ctx, out, title);
        
        tr.TR(ctx,out, null, 0);
        tr.TD(ctx,out);
        out.print(msg);
        tr.TDEnd(ctx,out);
        tr.TREnd(ctx,out);
        
        tr.TableEnd(ctx, out);
    }

    
    private PricePlanVoucherTypeMapping[] retrievePricePlanVoucherTypeMappingList(Context ctx, PrintWriter out, ErrorMsgHolder errors)
    {
        PricePlanVoucherTypeMappingServiceRmiClient mappingClient = (PricePlanVoucherTypeMappingServiceRmiClient) ctx.get(UrcsClientInstall.PRICE_PLAN_VOUCHER_TYPE_MAPPING_SERVICE_RMI_KEY); 
        if (mappingClient == null)
        {
            errors.postError(FAILED_TO_GET_MAPPING_LIST + " VRA not reachable.");
            return null;
        }

        final VRALoginConfig vraLoginConfig = (VRALoginConfig) ctx.get(VRALoginConfig.class);
        final long pricePlan = ctx.getLong(CSRVoucherRechargeServicer.VRA_VOUCHER_VALIDATING_PRICE_PLAN);

        PricePlanVoucherTypeMapping[] mapList = null;
        try
        {
            PricePlanVoucherTypeMappingList mapping = mappingClient.getMappingsForPricePlan( vraLoginConfig
                    .getUserName(), vraLoginConfig.getPassword(), pricePlan);
            short rc = mapping.getResultCode();
            if (isSuccessful(rc))
            {
                mapList = mapping.getPricePlanVoucherTypeMappingList();
                if (mapList == null || mapList.length == 0)
                {
                    errors.postError("There is no voucher type available for the subscriber.");
                    return null;
                }
            }
            else
            {
                errors.postError(FAILED_TO_GET_MAPPING_LIST + " Error code: " + rc);
                return null;
            }
        }
        catch (Exception e)
        {
            LogSupport.minor(ctx, this, FAILED_TO_GET_MAPPING_LIST, e);
            errors.postError(FAILED_TO_GET_MAPPING_LIST);
            return null;
        }

        // test
        // PricePlanVoucherTypeMapping[] mapList = new PricePlanVoucherTypeMapping[2];
        // PricePlanVoucherTypeMapping e1 = new PricePlanVoucherTypeMapping();
        // e1.setPricePlanID(pricePlan);
        // e1.setPricePlanDescription("Price Plan 1");
        // e1.setVoucherTypeID(10);
        // e1.setVoucherTypeDescription("Voucher Type 10");
        // e1.setStartDate("2009/03/02");
        // e1.setEndDate("2009/03/31");
        // mapList[0] = e1;
        // PricePlanVoucherTypeMapping e2 = new PricePlanVoucherTypeMapping();
        // e2.setPricePlanID(pricePlan);
        // e2.setPricePlanDescription("Price Plan 1");
        // e2.setVoucherTypeID(20);
        // e2.setVoucherTypeDescription("Voucher Type 20");
        // e2.setStartDate("2009/03/02");
        // e2.setEndDate("2009/03/31");
        // mapList[1] = e2;
        // test

        return mapList;
    }

    private void displayMappingList(Context ctx, PrintWriter out, PricePlanVoucherTypeMapping[] mapList, String pricePlan)
    {
        final TableRenderer renderer = (TableRenderer) ctx.get(TableRenderer.class, DefaultTableRenderer.instance());

        renderer.Table(ctx, out, String.format("The subscriber with plan <%s> has access to the following voucher types:", pricePlan));

        generateMappingTableHead(ctx, out, renderer);

        int index = 0;
        for (PricePlanVoucherTypeMapping entry : mapList)
        {
            renderer.TR(ctx,out, null, index++);

            renderer.TD(ctx,out);
            out.println(entry.getVoucherTypeDescription());
            renderer.TDEnd(ctx,out);

            renderer.TD(ctx,out);
            out.println(dateToString(entry.getStartDate()));
            renderer.TDEnd(ctx,out);

            renderer.TD(ctx,out);
            out.println(dateToString(entry.getEndDate()));
            renderer.TDEnd(ctx,out);

            renderer.TREnd(ctx,out);
        }

        renderer.TableEnd(ctx, out);
    }
    
    private String dateToString(long date)
    {
        String ret = EMPTY_DATE_STR;
        
        if (date!=0)
        {
            ret = DateFormat.getDateInstance().format
                (new Date(date));
        }
        
        return ret;
    }
    
    private static void showErrorMessage(Context ctx, PrintWriter out, String msg)
    {
        showMessageBox(ctx, out, "Errors", msg);
    }
    
    private static void postErrorMessage (ExceptionListener listener, String msg)
    {
        final Exception e = new Exception(msg);
        processException(listener, e);
    }

    private boolean isSuccessful(short rc)
    {
        return rc==VRAResultCodes.SUCCESS;
    }

    private void generateMappingTableHead(final Context ctx, PrintWriter out, final TableRenderer renderer)
    {
        renderer.TR(ctx,out, null, 0);
        renderer.TD(ctx,out);
        out.println("Voucher Type Description");
        renderer.TDEnd(ctx,out);
        renderer.TD(ctx,out);
        out.println("Start Date");
        renderer.TDEnd(ctx,out);
        renderer.TD(ctx,out);
        out.println("End Date");
        renderer.TDEnd(ctx,out);
        renderer.TREnd(ctx,out);
    }

    private static void generateOM(final Context ctx, final String msg)
    {
        new OMLogMsg(Common.OM_MODULE, msg).log(ctx);
    }

    private static void generateER(final Context ctx, final Subscriber subscriber, final String voucherNum,
            final int vraResult)
    {
        new ERLogMsg(
                ER_IDENTIFIER,
                ER_CLASS,
                ER_TITLE,
                subscriber.getSpid(),
                new String[]{subscriber.getMSISDN(), voucherNum, String.valueOf(vraResult)}).log(ctx);
    }
} // class
