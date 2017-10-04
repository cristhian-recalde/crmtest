package com.trilogy.app.crm.ban;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.extension.spid.BANGenerationSpidExtension;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;


public class BANGeneratorFactory
{

    /**
     * Returns default ban generator
     * 
     * @param ctx
     * @return
     * @throws HomeException
     */
    public static BANGenerator getBeanGenerator(final Context ctx) throws HomeException
    {
        return getBeanGenerator(ctx, -1);
    }


    /**
     * Returns Ban generator that spid, if nothing is specified, then it returns default
     * generator
     * 
     * @param ctx
     * @param spid
     * @return
     * @throws HomeException
     */
    public static BANGenerator getBeanGenerator(final Context ctx, final int spid) throws HomeException
    {
        if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.TELUS_GATEWAY_LICENSE_KEY))
        {
            return telusBanGenerator; 
        }
        
        if (!mapBanGenerators_.containsKey(spid))
        {
            mapBanGenerators_.put(Integer.valueOf(spid), getGeneratorForSpid(ctx, spid));
        }
        return mapBanGenerators_.get(Integer.valueOf(spid));
    }


    private static BANGenerator getGeneratorForSpid(final Context ctx, final int s) throws HomeException
    {
        
        // Default BAN generator
        BANGenerator generator = new DefaultBANGenerator();
        if (s > 0)
        {
            // look for the next BAN
            final Home spidHome = (Home) ctx.get(CRMSpidHome.class);
            if (spidHome == null)
            {
                throw new HomeException("System Error: CRMSpidHome does not exist in context");
            }
            final CRMSpid spid = (CRMSpid) spidHome.find(ctx, Integer.valueOf(s));
            if (spid == null)
            {
                throw new HomeException(
                        "Configuration Error: Service Provider is mandatory, make sure it exists before continuing");
            }
            /*
             * will only create a new account number if the ban is not set
             */
            for (Object o : spid.getExtensions())
            {
                try
                {
                    if (o instanceof BANGenerationSpidExtension)
                    {
                        BANGenerationSpidExtension banGenExt = (BANGenerationSpidExtension) o;
                        Class customInt = Class.forName(banGenExt.getGenerateInterface());
                        generator = (BANGenerator) customInt.newInstance();
                        break;
                    }
                }
                catch (Exception ex)
                {
                    new MinorLogMsg(BANGeneratorFactory.class,
                            " Unable to load the custom generator.  Hence, using the default generator", ex).log(ctx);
                }
            }
        }
        return generator;
    }

    public static Map<Integer, BANGenerator> mapBanGenerators_ = new HashMap<Integer, BANGenerator>();
    
    static TelusBANGenerator telusBanGenerator = new TelusBANGenerator();
}
