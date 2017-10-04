package com.trilogy.app.crm.hlr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.interfaces.crm.hlr.CrmHlrResponse;
import com.trilogy.interfaces.crm.hlr.HlrID;
import com.trilogy.interfaces.crm.hlr.HlrIDHome;
import com.trilogy.interfaces.crm.hlr.InterfaceCrmHlrConstants;
import com.trilogy.interfaces.crm.hlr.RMIHLRProvisioningClient;


/**
 * This class will be no longer needed after move service provisioning to SPG.
 * basically this class is useless, just make it compatible before service 
 * provisioning is move to SPG.
 * @author lxia
 *
 */
public class CrmHlrServiceImpl 
{
    /**yyyyMMddHH
     * TRANSID substitution variable. new java.util.Random(seed).nextLong()
     */
    public static final String TRANSID_VARIABLE = "%TRANSID%";
    
	private CrmHlrServiceImpl()
	{
		
	}
	
	static public CrmHlrServiceImpl instance()
	{
		
		return instance_; 
	}
	
	

	public CrmHlrResponse process( final Context ctx, 
			final short hlrID, final String cmd) 
	{
	    CrmHlrResponse response = new CrmHlrResponse(); 

		if (ctx.has(HLR_SKIPPED) || cmd == null || 
				cmd.trim().length()< 1)
		{
			response.setCrmHlrCode(InterfaceCrmHlrConstants.HLR_SUCCESS); 
			return response; 
		}
		
		final RMIHLRProvisioningClient hlrClient = (RMIHLRProvisioningClient) ctx.get(RMIHLRProvisioningExternalService.class);
		final Random r = new Random(); 
		
		try 
		{
			
			String splitChar = System.getProperty("hlr.commands.split.char");
			if(splitChar == null  || splitChar.trim().isEmpty())
			{
				splitChar = "\r\n";
			}
	        final String[] commands = cmd.split(splitChar);
	        for (int n = 0; n < commands.length; ++n)
	        {
	            if (commands[n].trim().length() == 0)
	            {
	                continue;
	            }
		    	
	    		commands[n]  = commands[n].replaceAll(TRANSID_VARIABLE, String.valueOf(Math.abs(r.nextLong())));

	            response = hlrClient.provision(Long.valueOf(hlrID), commands[n]);
	            
	            if (  response.getCrmHlrCode()!= InterfaceCrmHlrConstants.HLR_SUCCESS)
	            {
	            	return response; 
	            }
			
	        }
		} catch (Exception e)
		{
			LogSupport.major(ctx, this, "HLR Provision Exception on execute Command " + e.getMessage()
                    + " reqest " + cmd, e);
            response.setCrmHlrCode(InterfaceCrmHlrConstants.HLR_FAILURE_INTERNAL); 
            response.setMessage("exception caught from HLR rmi service " + e.getMessage());               
		}
		
	

        return response; 
	
	}

	
 
    
    
	public Collection<String> getHlrIdList(final Context ctx)
	{
		final ArrayList<String> ret = new ArrayList<String>(); 
		final Home home = (Home) ctx.get(HlrIDHome.class);
		
		try
		{
			Collection c = home.selectAll(ctx);
		
			for(Iterator i = c.iterator(); i.hasNext(); )
			{
				
				HlrID config = (HlrID) i.next();
				ret.add(String.valueOf(config.getId())); 
			}
		} catch (Exception e)
		{
			new MinorLogMsg(this, "fail to get HLR configuration list", e).log(ctx);
		}
		
		return ret; 
	}

    private static final int NO_RESPONSE = -1;
	static CrmHlrServiceImpl instance_ = new CrmHlrServiceImpl(); 
	public static final String HLR_SKIPPED = "HLR_SKIPPED"; 
}
