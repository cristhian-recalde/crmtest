package com.trilogy.app.crm.hlr;

import java.util.Collection;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.report.ReportUtilities;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.interfaces.crm.hlr.CrmHlrResponse;
import com.trilogy.interfaces.crm.hlr.InterfaceCrmHlrConstants;

public class CrmHlrServicePipelineImpl 
{

	private CrmHlrServicePipelineImpl()
	{
		
	}
	
	static public CrmHlrServicePipelineImpl instance()
	{
		
		
		return instance_; 
	}
	
		
	public Collection<String> getHlrIdList(Context ctx)
	{
		return CrmHlrServiceImpl.instance_.getHlrIdList(ctx); 
	}


	public CrmHlrResponse process(Context ctx, final short hlrID, final String cmd) 
	throws ProvisionAgentException
	{
		CrmHlrResponse ret = new CrmHlrResponse();
		ret.setCrmHlrCode(InterfaceCrmHlrConstants.HLR_SUCCESS); 
		
		if ( SystemSupport.needsHlr(ctx)) 
		{
			ret =  CrmHlrServiceImpl.instance_.process(ctx, hlrID, cmd);
			handleResult(ctx, ret, cmd); 
		}
		
		return ret; 
		
	}
	
	
	
	   /**
     * Handles HLRTask, based on whether it was an UPDATE or EXECUTE.
     *
     * @param ctx
     *            The operating context.
     * @param resp
     *            HLRTask returned from ServiceHLR
     * @param command
     *            HLR command string used to provision
     * @param updateHLR
     *            Whether HLR should be updated.
      */
    private void handleResult(final Context ctx, final CrmHlrResponse resp, final String command)
    throws ProvisionAgentException
    {
        final String exceptionString = (resp.getMessage()==null)?"":resp.getMessage();
        if (resp.getCrmHlrCode() != InterfaceCrmHlrConstants.HLR_SUCCESS)
        {
            ReportUtilities.logMajor(ctx, getClass().getName(),
                "Request to HLR failed for command \"{0}\". Response: \"{1}\" " + "Driver code: \"{2}\". Raw HLR reply: \"{3}\". ", new String[]
                {
                    command, String.valueOf(resp.getCrmHlrCode()),
                    String.valueOf(resp.getDriverHlrCode()),
                    String.valueOf(resp.getRawHlrData())
                }, null);

    /*             if( task.getResponse() == null
                        && !task.isProcessed()
                        && task.getException() != null )
                {
                    // TT7102900030 - Must set result code so subscriber goes to PENDING state
                    SubscriberProvisionResultCode.setProvisionLastResultCode(ctx, NO_RESPONSE);    
                }
      */
            new OMLogMsg(Common.OM_MODULE, Common.OM_HLR_PROV_ERROR).log(ctx);

            new EntryLogMsg(10359, this, "", resp.getCrmHlrCode(), new java.lang.String[]
            {
                exceptionString,
            }, null).log(ctx);
            
            String language = null;
            
            throw new ProvisionAgentException(ctx, "HLR command execution failed: "
                    + ExternalAppSupportHelper.get(ctx).getErrorCodeMessage(ctx, ExternalAppEnum.HLR,
                            resp.getCrmHlrCode())
                    + (exceptionString.isEmpty() ? "" : (" - " + exceptionString)), resp.getCrmHlrCode(),
                    ExternalAppEnum.HLR);

         }

        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Response from ServiceHLR: " + command + " , " + resp.getCrmHlrCode());
        }
    }
	
	static CrmHlrServicePipelineImpl instance_ = new CrmHlrServicePipelineImpl(); 

}
