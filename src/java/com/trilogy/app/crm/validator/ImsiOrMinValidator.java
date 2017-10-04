package com.trilogy.app.crm.validator;

import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;

import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.numbermgn.GenericPackage;
import com.trilogy.app.crm.support.PackageSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;

/*
 * This validator verifies if the IMSI is already being used
 */
public class ImsiOrMinValidator implements Validator
{
    int spid;
    public ImsiOrMinValidator()
    {
        super();
    }


    public static ImsiOrMinValidator instance()
    {
        return instance_;
    }
    /*
     * validate:
     * @ Validate is IMSI is already being used
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        
        HomeOperationEnum op = (HomeOperationEnum) ctx.get(HomeOperationEnum.class, HomeOperationEnum.CREATE);

        // Validation should only be done on creation
        if (op == HomeOperationEnum.CREATE)
        {
            if (obj == null)
            {
                return;
            }

            final GenericPackage genPackage = (GenericPackage) obj;

        
            String imsiOrMin = "";
            TechnologyEnum technology = genPackage.getTechnology();
            if ( TechnologyEnum.GSM.equals(technology))
            {
                GSMPackage gsmPackage = (GSMPackage) genPackage;
                imsiOrMin = gsmPackage.getIMSI();
                spid = gsmPackage.getSpid();
            }
            else if ( TechnologyEnum.TDMA.equals(technology) ||
                    TechnologyEnum.CDMA.equals(technology) )
            {
                TDMAPackage tdmaPackage = (TDMAPackage) genPackage;
                imsiOrMin = tdmaPackage.getMin();
                spid = tdmaPackage.getSpid();
            }
            if ( ! "".equals(imsiOrMin) )
            {
                try
                {
                    GenericPackage duplicatePackage = PackageSupportHelper.get(ctx).lookupPackageForIMSIOrMIN(ctx, technology, imsiOrMin, spid);
                    if (duplicatePackage != null)
                    {
                        throw new IllegalStateException("Package '" + duplicatePackage.getPackId()
                                + "' with IMSI '" + imsiOrMin + "' already exists in " + duplicatePackage.getState() + " state.");
                    }
                }
                catch (HomeException e)
                {
                    throw new IllegalStateException("Error looking for existing IMSI.  Unable to validate.");
                }
            }
        }
    }

    private final static ImsiOrMinValidator instance_ = new ImsiOrMinValidator();
}
