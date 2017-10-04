package com.trilogy.app.crm.voicemail;

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.AbstractNote;
import com.trilogy.app.crm.bean.Note;
import com.trilogy.app.crm.bean.NoteHome;
import com.trilogy.app.crm.bean.ServiceBase;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.client.ConnectionStatus;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.app.crm.voicemail.client.ExternalProvisionResult;

public class VoiceMailServer extends ContextAwareSupport implements VoiceMailService, VoiceMailConstants
{
   
    private static final String SERVICE_NAME = "VoiceMailServer";
    private static final String SERVICE_DESCRIPTION = "Generic voicemail server";


    public VoiceMailServer(Context ctx)
    {
        setContext(ctx);
    }
    
    
    public ExternalProvisionResult provision(
            final Context ctx, 
            final Subscriber sub, 
            final ServiceBase service)
    {
        ExternalProvisionResult ret =  null;
        
        
        VoiceMailService client = (VoiceMailService)ctx.get(VoiceMailService.class); 
        
        if ( client == null )
        {
            new MajorLogMsg(this,"System error, no VM client found in context, fail to add Voicemail profile for sub = " +
                    sub.getId(), null).log(ctx);
            ret =  new ExternalProvisionResult(RESULT_FAIL_VM_CLIENT_NOT_FOUND, ORIG_VM_RETURN_UNKNOWN); 
           
        } 
        else 
        {
            
            ret = client.provision(ctx, sub, service);
           
        }

         
        if ( ret.getCrmVMResultCode() != RESULT_SUCCESS)
        {    
            new OMLogMsg(Common.OM_MODULE, Common.OM_VOICEMAIL_ERROR).log(ctx);
            //SNMP trap VoiceMailServer (VoicemailProvisionAgent) provision - fail to add subcriber - result code = {0}
            new EntryLogMsg(13781, this, "", sub.getBAN(), new java.lang.String[]{String.valueOf(ret)}, null).log(ctx);
        }
        
        return ret; 
    } 
    
    public ExternalProvisionResult unprovision(
            final Context ctx, 
            final Subscriber sub, 
            final ServiceBase service)
    {
        ExternalProvisionResult ret = null;
        
        VoiceMailService client = (VoiceMailService)ctx.get(VoiceMailService.class); 
        
        if ( client == null )
        {
            new MajorLogMsg(this,"System error, no VM client found in context, fail to delete Voicemail profile for sub = " +
                    sub.getId(), null).log(ctx);
            ret =  new ExternalProvisionResult(RESULT_FAIL_VM_CLIENT_NOT_FOUND,ORIG_VM_RETURN_UNKNOWN);
           
        } 
        else 
        {
            
            ret = client.unprovision(ctx, sub, service);
           
        }

        if ( ret.getCrmVMResultCode() != RESULT_SUCCESS)
        {    
            new OMLogMsg(Common.OM_MODULE, Common.OM_VOICEMAIL_ERROR).log(ctx);
            //SNMP trap: VoiceMailServer (VoicemailUnprovisionAgent) unprovision - fail to unprovision subcriber service - result code = {0}
            new EntryLogMsg(13786, this, "", sub.getBAN(), new java.lang.String[]{String.valueOf(ret)}, null).log(ctx);
         }
        
        return ret; 
    }
    
    public ExternalProvisionResult deactivate(
            final Context ctx,
            final Subscriber sub, 
            final ServiceBase service)
    {
        ExternalProvisionResult ret;

        VoiceMailService client = (VoiceMailService)ctx.get(VoiceMailService.class); 
        
        if ( client == null )
        {
            new MajorLogMsg(this,"System error, no VM client found in context, fail to deactivate Voicemail for sub = " + sub.getId(), null).log(ctx);
            ret =  new ExternalProvisionResult(RESULT_FAIL_VM_CLIENT_NOT_FOUND, ORIG_VM_RETURN_UNKNOWN) ;
           
        } 
        else 
        {
            
            ret = client.deactivate(ctx, sub, service);
           
        }
        
        if ( ret.getCrmVMResultCode() != RESULT_SUCCESS)
        {    
            new OMLogMsg(Common.OM_MODULE, Common.OM_VOICEMAIL_ERROR).log(ctx);
         }
 
        return ret;     
    }
    
    public ExternalProvisionResult activate(
            final Context ctx,
            final Subscriber sub, 
            final ServiceBase service)
    {
        ExternalProvisionResult ret;
 
        VoiceMailService client = (VoiceMailService)ctx.get(VoiceMailService.class); 
        
        if ( client == null )
        {
            new MajorLogMsg(this,"System error, no VM client found in context, fail to activate Voicemail for sub = " +
                    sub.getId(), null).log(ctx);
            ret =  new  ExternalProvisionResult(RESULT_FAIL_VM_CLIENT_NOT_FOUND,ORIG_VM_RETURN_UNKNOWN);
           
        } 
        else 
        {
            
            ret = client.activate(ctx, sub, service);
           
        }
        
        if ( ret.getCrmVMResultCode() != RESULT_SUCCESS)
        {    
            new OMLogMsg(Common.OM_MODULE, Common.OM_VOICEMAIL_ERROR).log(ctx);
         }
 
        return ret; 
    }
    
    public ExternalProvisionResult changeMsisdn(
            final Context ctx, 
            final Subscriber sub, 
            final String newMsisdn) 
    {
        String note = "MSISDN updated successfully in voicemail service for subscriber:" + 
                sub.getId() + 
                "[new MSISDN:" + newMsisdn + "]";

        ExternalProvisionResult ret;
        VoiceMailService client = (VoiceMailService)ctx.get(VoiceMailService.class); 
        
        if ( client == null )
        {
            new MajorLogMsg(this,"System error, no VM client found in context, fail to update Voicemail password for sub = " +
                    sub.getId(), null).log(ctx);
            ret =  new ExternalProvisionResult(RESULT_FAIL_VM_CLIENT_NOT_FOUND, ORIG_VM_RETURN_UNKNOWN);
           
        } 
        else 
        {
            
            ret = client.changeMsisdn(ctx, sub, newMsisdn);
           
        }
        
        if ( ret.getCrmVMResultCode() != RESULT_SUCCESS)
        {    
            new OMLogMsg(Common.OM_MODULE, Common.OM_VOICEMAIL_ERROR).log(ctx);
            note = "MSISDN failed to update in voicemail service for subscriber:" + sub.getId() + 
                   "[MSISDN:" + newMsisdn + "]" + 
                   " return code is " + ret;
        }
        
        addSubscriberNote(ctx, sub, note);  
        return ret; 
        
    }
    
    
    public ExternalProvisionResult resetPassword(Context ctx, Subscriber sub,  String password)
    {
        String note = "Password updated successfully in voicemail service for subscriber:" + 
                sub.getId() + 
                "[MSISDN:" + sub.getMSISDN() + "]";
        
        ExternalProvisionResult ret;
        
        VoiceMailService client = (VoiceMailService)ctx.get(VoiceMailService.class); 
        
        if ( client == null )
        {
            new MajorLogMsg(this,"System error, no VM client found in context, fail to update Voicemail password for sub = " +
                    sub.getId(), null).log(ctx);
            ret = new ExternalProvisionResult(RESULT_FAIL_VM_CLIENT_NOT_FOUND, ORIG_VM_RETURN_UNKNOWN);
           
        } 
        else 
        {
        
            ret = client.resetPassword(ctx, sub, password); 
        }
        
        if ( ret.getCrmVMResultCode() != RESULT_SUCCESS)
        {    
            new OMLogMsg(Common.OM_MODULE, Common.OM_VOICEMAIL_ERROR).log(ctx);
            note = "Password failed to update in voicemail service for subscriber:" + 
                    sub.getId() + 
                   "[MSISDN:" + sub.getMSISDN() + "]" + 
                   " return code is " + ret;
        }
        
        addSubscriberNote(ctx, sub, note); 
        
        return ret; 
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



    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.ExternalService#getServiceDescription()
     */
    public String getDescription()
    {
        VoiceMailService client = (VoiceMailService)getContext().get(VoiceMailService.class); 
        
        if ( client != null )
        {
            return client.getDescription();
        }
        
        return SERVICE_DESCRIPTION;
    }


    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.ExternalService#getServiceName()
     */
    public String getName()
    {
        VoiceMailService client = (VoiceMailService)getContext().get(VoiceMailService.class); 
        
        if ( client != null )
        {
            return client.getName();
        }
        
        return SERVICE_NAME;
    }


    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.ExternalService#isServiceAlive()
     */
    public boolean isAlive()
    {
        VoiceMailService client = (VoiceMailService)getContext().get(VoiceMailService.class); 
        
        if ( client != null )
        {
            return client.isAlive();
        }
        
        return false;
    }


    @Override
    public ConnectionStatus[] getConnectionStatus()
    {
        VoiceMailService client = (VoiceMailService)getContext().get(VoiceMailService.class);
        
        if (client!=null)
        {
            return client.getConnectionStatus();
        }
        else
        {
            return SystemStatusSupportHelper.get().generateConnectionStatus("", isAlive());
        }
    }


    @Override
    public String getServiceStatus()
    {
        return SystemStatusSupportHelper.get().generateServiceStatusString(isAlive());
    }
    
    
 }
