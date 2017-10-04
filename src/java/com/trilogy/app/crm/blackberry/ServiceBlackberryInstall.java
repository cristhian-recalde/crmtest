package com.trilogy.app.crm.blackberry;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.client.blackberry.ServiceBlackberryClient;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.app.crm.web.service.SystemStatusRequestServicer;
import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.service.blackberry.ServiceBlackberryFactory;
import com.trilogy.service.blackberry.model.BBConfig;
import com.trilogy.service.blackberry.model.ServiceMappingHome;
import com.trilogy.service.blackberry.model.BBConfigHome;

/**
 * Service BlackBerry Installation logic.
 * 
 * This code was taken out of Service Install so that it could be reused in Invoice Server installation.
 * @author angie.li
 *
 */
public class ServiceBlackberryInstall 
{

    /**
     * Install the Blackberry Service in CRM.
     * The client serving the requests is controlled by Licensing.
     * @param ctx
     * @throws IOException 
     * @throws HomeException 
     * @throws RemoteException 
     */
    public static void execute(Context ctx) throws RemoteException, HomeException, IOException
    {
        ServiceBlackberryFactory.install(ctx);
        /* For CRM testing we'll need Blackberry to be enabled in CRM, but the 
         * ServiceBlackberry to use the NullClient.  Prior to shipping a production
         * build, we'll have to set the License Keys to be the same license key, so 
         * that we may turn on the feature at once.  Common License installed. */
        ServiceBlackberryFactory.setLicenseKey(LicenseConstants.BLACKBERRY_LICENSE);

        Home home = ServiceBlackberryFactory.getServiceMapping(ctx);
        home = new RMIClusteredHome(ctx, ServiceMappingHome.class.getName(), home);

        ctx.put(ServiceMappingHome.class, home);
        
        home = ServiceBlackberryFactory.getBlackberryConfigHome(ctx);
        home = new RMIClusteredHome(ctx, BBConfigHome.class.getName(), home);

        ctx.put(BBConfigHome.class, home);
        
        Collection<BBConfig> blackberryConfig = null;
        try 
        {
			  blackberryConfig = home.selectAll(ctx);
		} 
        catch (Exception e)
		{
			if(LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(e.getMessage(),null, e).log(ctx);
			}
		}
        
		for (BBConfig bbConfig : blackberryConfig) 
        {
	        ServiceBlackberryClient serviceBBclient = new ServiceBlackberryClient(ctx, bbConfig);
	        String key = bbConfig.getSpid() + bbConfig.getUrl();
	        ctx.put(key, serviceBBclient);
	
	        SystemStatusSupportHelper.get(ctx).registerExternalService(ctx, key);   
        }
     }

}
