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
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.VoiceMailPINReset;
import com.trilogy.app.crm.bean.VoiceMailPINResetWebControl;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.extension.auxiliaryservice.core.SPGAuxSvcExtension;
import com.trilogy.app.crm.hlr.HlrSupport;
import com.trilogy.app.crm.home.sub.HLRConstants;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.provision.gateway.ServiceProvisioningGatewaySupport;
import com.trilogy.app.crm.provision.service.ErrorCode;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.web.agent.VMPasswordResetWebAgent;
import com.trilogy.framework.xhome.beans.DefaultExceptionListener;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.webcontrol.BeanWebController;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.util.snippet.log.Logger;
import com.trilogy.app.crm.bean.AbstractNote;
import com.trilogy.app.crm.bean.Note;
import com.trilogy.app.crm.bean.NoteHome;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;



/**
 * Request Servicer that calls PIN reset for the appropriate voice mail service.
 * 
 * @author victor.stratan@redknee.com
 * @since
 */
public class VoiceMailPINResetServicer implements RequestServicer
{

    public static final String GATEWAY_TYPE = "Gateway";
    public static final String GENERIC_TYPE = "Generic";
    private static final String SERVICE_TYPE = "Service";
    private static final String AUXILIARY_TYPE = "Auxiliary";
    private static final String RESET_PIN = "Reset PIN";
    private static final String ERROR = "Error!";
    private static final long INTERVOICE_ID = 1;

    /**
     * {@inheritDoc}
     */
    @Override
    public void service(final Context ctx, final HttpServletRequest req, final HttpServletResponse res)
    throws ServletException, IOException
    {
        final PrintWriter out = res.getWriter();
        final HttpSession session = req.getSession();
        final Context subCtx = ctx.createSubContext();
        final MessageMgr manager = new MessageMgr(subCtx, this);

        final ButtonRenderer buttonRenderer = (ButtonRenderer) subCtx.get(ButtonRenderer.class,
                DefaultButtonRenderer.instance());

        HTMLExceptionListener exceptions = (HTMLExceptionListener) subCtx.get(HTMLExceptionListener.class);
        if (exceptions == null)
        {
            exceptions = new HTMLExceptionListener(manager);
            subCtx.put(HTMLExceptionListener.class, exceptions);
        }
        subCtx.put(ExceptionListener.class, exceptions);

        final VoiceMailPINReset bean = new VoiceMailPINReset();
        boolean displayMessage = false;

        if (buttonRenderer.isButton(subCtx, RESET_PIN))
        {
            wc_.fromWeb(subCtx, bean, req, "");
            checkAndResetPIN(subCtx, manager, bean, exceptions);
            displayMessage = true;
        }
        else
        {
            Subscriber sub = (Subscriber) subCtx.get(Subscriber.class);
            if (sub != null)
            {
                populateBean(subCtx, sub.getId(), bean, exceptions);
            }
            else
            {
                bean.setSubscriptionID(ERROR);
            }
        }

        out.print("<table><tr><td>");
        FrameworkSupportHelper.get(subCtx).printCapturedExceptions(subCtx);
        if (!exceptions.hasErrors() && displayMessage)
        {
            out.print("<pre><center><font size=\"1\" face=\"Verdana\" color=\"green\"><b>Voicemail PIN successfuly reset.</b></font></center></pre>");
        }

        FormRenderer formRend = (FormRenderer) subCtx.get(FormRenderer.class, DefaultFormRenderer.instance());
        formRend.Form(out, subCtx);

        out.print("<table>");
        out.print("<tr><td>");
        wc_.toWeb(subCtx, out, "", bean);

        out.print("</td></tr><tr><th align=\"right\">");
        if (bean.getServiceID() != VoiceMailPINReset.DEFAULT_SERVICEID)
        {
            buttonRenderer.inputButton(out, subCtx, this.getClass(), RESET_PIN, false);
        }
        BeanWebController.outputHelpLink(subCtx, null, null, out, buttonRenderer);
        out.print("</th></tr></table>");

        formRend.FormEnd(out);
        out.print("</td></tr></table>");
    }

    public static void resetVoicemailPIN(final Context ctx, final String subscriptionID,
            final DefaultExceptionListener el)
    {
        final VoiceMailPINResetServicer servicer = new VoiceMailPINResetServicer();
        final VoiceMailPINReset bean = new VoiceMailPINReset();
        final Context subCtx = ctx.createSubContext();
        final MessageMgr manager = new MessageMgr(subCtx, servicer);

        servicer.populateBean(ctx, subscriptionID, bean, el);
        if (!el.hasErrors())
        {
            servicer.checkAndResetPIN(subCtx, manager, bean, el);
        }
    }

    private void checkAndResetPIN(Context ctx, MessageMgr manager, VoiceMailPINReset bean, final ExceptionListener el)
    {
        if (ERROR.equals(bean.getSubscriptionID())
                || ERROR.equals(bean.getServiceName()))
        {
            IllegalStateException ex = new IllegalStateException("Unable to locate Voice Mail Service!");
            el.thrown(ex);
        }
        else
        {
            resetPIN(ctx, manager, bean, el);
        }
    }

    /**
     * @param ctx
     * @param string
     */
    private void resetPIN(Context ctx, MessageMgr manager, VoiceMailPINReset bean, final ExceptionListener el)
    {
        try
        {
            final EQ condition = new EQ(SubscriberXInfo.ID, bean.getSubscriptionID());
            final Subscriber sub = HomeSupportHelper.get(ctx).findBean(ctx, Subscriber.class, condition);
            final EQ serviceCondition = new EQ(ServiceXInfo.ID, bean.getServiceID());
            final Service srv=HomeSupportHelper.get(ctx).findBean(ctx, Service.class, serviceCondition);
            long result = ErrorCode.SUCCESS;
// put some code here to take care telus pin reset. keep the back door, since, provision command is not 
// refactored to support provision other network element. 
            
            boolean TELUS_LICENSE = LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.TELUS_GATEWAY_LICENSE_KEY);
            
            if (GATEWAY_TYPE.equals(bean.getServiceType()))
            {
                result = ServiceProvisioningGatewaySupport.resetPIN(ctx, sub, bean.getGatewayServiceID());
                if(result != ErrorCode.SUCCESS)
                {
                    el.thrown(new ProvisionAgentException(ctx, 
                            ExternalAppSupportHelper.get(ctx).getErrorCodeMessage(ctx, ExternalAppEnum.SPG, (int)result),(int)result,
                                    ExternalAppEnum.SPG));
                    
                }
            } 
            // Correcting below fix. For voiceMailProvision = false, HLR call should not go to reset the pin, even though license is enabled.
            
            //Fix for TT# 14092619002 When using Mpathix, pin reset call was not reaching to ESB
    		//For Mpathix Voicemail- Service Should be of Type GENERIC and VoiceMail Flag should be CHECKED,
        	//then only Voicemail Pin Reset Call for Mpathix will go to ESB
           
            else if ((TELUS_LICENSE	&& (ServiceTypeEnum.VOICEMAIL.equals(srv.getType()) || (ServiceTypeEnum.GENERIC.equals(srv.getType()) && srv.isVoiceMailProvision()))) 
                    || (!TELUS_LICENSE	&& (ServiceTypeEnum.GENERIC.equals(srv.getType()) && srv.isVoiceMailProvision()))
            		)
        	{
            	if(!HlrSupport.updateHlr(ctx, sub, HLRConstants.PRV_CMD_TYPE_RESET_VM_PIN))
                {
            		el.thrown(new AgentException("Error occurred in PIN Reset"));
                }else
                {
                	String note = "Password reset successfully in voicemail service for subscriber:" + 
                            sub.getId() + 
                            "[MSISDN:" + sub.getMSISDN() + "]";
                		addSubscriberNote(ctx, sub, note); 
                	
                }
            }
            else
            {
            	VMPasswordResetWebAgent.resetPINThroughVoiceMailServer(ctx, this, manager, sub, sub.getMSISDN());
            }
        }
        catch (Exception e)
        {
            Logger.minor(ctx, this, "Error occurred in resetPIN(): " + e.getMessage(), e);
            el.thrown(e);
        }
    }

    /**
     * @param ctx
     * @param bean
     */
    private void populateBean(final Context ctx, final String subscriptionID, final VoiceMailPINReset bean,
            final ExceptionListener el)
    {
        final EQ condition = new EQ(SubscriberXInfo.ID, subscriptionID);
        try
        {
            final Subscriber sub = HomeSupportHelper.get(ctx).findBean(ctx, Subscriber.class, condition);

            if (sub == null)
            {
                bean.setSubscriptionID(ERROR);
            }
            else
            {
                bean.setSubscriptionID(subscriptionID);
                final Collection<SubscriberServices> subSrvList = sub.computeProvisionServices(ctx, sub.getElegibleForProvision(ctx));
                for (SubscriberServices subSrv : subSrvList)
                {
                    final Service srv = subSrv.getService(ctx);
                    if (srv != null)
                    {
                        if (ServiceTypeEnum.SERVICE_PROVISIONING_GATEWAY.equals(srv.getType())
                                && srv.getSPGServiceType() == INTERVOICE_ID)
                        {
                            bean.setServiceID(srv.getID());
                            bean.setServiceName(srv.getName());
                            bean.setServiceType(GATEWAY_TYPE);
                            bean.setGatewayServiceID(srv.getSPGServiceType());
                            return;
                        }
                        else if (ServiceTypeEnum.VOICEMAIL.equals(srv.getType()))
                        {
                            bean.setServiceID(srv.getID());
                            bean.setServiceName(srv.getName());
                            bean.setServiceType(SERVICE_TYPE);
                            return;
                        }
                        if (ServiceTypeEnum.GENERIC.equals(srv.getType())&&srv.isVoiceMailProvision())
                        {
                        	
                            bean.setServiceID(srv.getID());
                            bean.setServiceName(srv.getName());
                            bean.setServiceType(SERVICE_TYPE);
                            
                            return;
                        }
                    }
                }

                final List<SubscriberAuxiliaryService> auxSrvList = sub.getAuxiliaryServices(ctx);
                for (SubscriberAuxiliaryService subAuxSrv : auxSrvList)
                {
                    final AuxiliaryService auxSrv = subAuxSrv.getAuxiliaryService(ctx);
                    if (auxSrv != null)
                    {
                        if (AuxiliaryServiceTypeEnum.SERVICE_PROVISIONING_GATEWAY.equals(auxSrv.getType()))
                        {
                            SPGAuxSvcExtension spgAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, auxSrv, SPGAuxSvcExtension.class);
                            if (spgAuxSvcExtension!=null && spgAuxSvcExtension.getSPGServiceType() == INTERVOICE_ID)
                            {
                                bean.setServiceID(auxSrv.getIdentifier());
                                bean.setServiceName(auxSrv.getName());
                                bean.setServiceType(GATEWAY_TYPE);
                                bean.setGatewayServiceID(spgAuxSvcExtension.getSPGServiceType());
                                return;
                            }
                            else if (spgAuxSvcExtension==null)
                            {
                                LogSupport.minor(ctx, this,
                                        "Unable to find required extension of type '" + SPGAuxSvcExtension.class.getSimpleName()
                                                + "' for auxiliary service " + auxSrv.getIdentifier());
                            }
                            
                        }
                        else if (AuxiliaryServiceTypeEnum.Voicemail.equals(auxSrv.getType()))
                        {
                            bean.setServiceID(auxSrv.getIdentifier());
                            bean.setServiceName(auxSrv.getName());
                            bean.setServiceType(AUXILIARY_TYPE);
                            return;
                        }
                    }
                }
            }
        }
        catch (HomeException e)
        {
            Logger.minor(ctx, this, "Error occurred in populateBean(): " + e.getMessage(), e);
            el.thrown(e);
            bean.setServiceName(ERROR);
        }
    }
    
    protected void addSubscriberNote(Context ctx, Subscriber sub, String notemsg)
    {
        final Home home = (Home)ctx.get(NoteHome.class);
        if (home == null)
        {
            new MajorLogMsg(this,"System error: no NoteHome found in context.",null).log(ctx);

            return;
        }

        //Subscriber note.

        final Note note = new Note();
        note.setIdIdentifier(sub.getId());
        note.setAgent(SystemSupport.getAgent(ctx));
        note.setCreated(new Date());
        note.setType(SystemNoteTypeEnum.EVENTS.getDescription());
        note.setSubType(SystemNoteSubTypeEnum.VMPwdReset.getDescription());

        if(notemsg.length()> AbstractNote.NOTE_WIDTH)
        {
            notemsg=notemsg.substring(0,AbstractNote.NOTE_WIDTH);
        }
        note.setNote(notemsg);         
        
        try
        {
            home.create(ctx,note);
        }
        catch (final HomeException exception)
        {
            new MajorLogMsg(
                    this,
                    "Failed to create subscriber note for VM Password Reset",
                    exception).log(ctx);
        }
        
    }    

    protected WebControl wc_ = new VoiceMailPINResetWebControl();
}
