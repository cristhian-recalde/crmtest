package com.trilogy.app.crm.bean.paymentgatewayintegration;

import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;

import billsoft.eztax.ZipAddress;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.TaxAdapters;
import com.trilogy.app.crm.bean.TaxAdaptersXInfo;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.spid.TaxAdaptersSpidExtension;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.taxation.LocalTaxAdapter;
import com.trilogy.app.crm.taxation.TaxAdapter;

/**
 * 
 * Taxation support methods for Payment Gateway Credit Card Top Up.
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public final class TaxSupport {

	private TaxSupport()
	{
		
	}
    
    public static final long getTaxAmount(Context ctx , int spid, String msisdn, long amount, ZipAddress zipAdresss) throws HomeException
    {
    	
        CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, spid);
        if(crmSpid == null)
        {
            throw new HomeException("SPID " + spid + " is not configured in billing system!");
        }
        Collection<Extension> extensionList = crmSpid.getExtensions();
        TaxAdaptersSpidExtension taxAdapterSpidExt = null;
        for (Extension extension : extensionList)
        {
            if(extension instanceof TaxAdaptersSpidExtension)
            {
                taxAdapterSpidExt = (TaxAdaptersSpidExtension)extension;
                break;
            }
        }
        
        if(taxAdapterSpidExt != null)
        {
            TaxAdapters adapter = HomeSupportHelper.get(ctx).findBean(ctx, TaxAdapters.class, 
                    new EQ(TaxAdaptersXInfo.ID, taxAdapterSpidExt.getTaxAdapter()));
            
            if(adapter == null)
            {
                throw new HomeException("Tax Adapter for spid"+spid+" is null . Please check if you have configured correct Tax Adapter at spid"); 
            }
            TaxAdapter tm=null;
			try {
				tm = (TaxAdapter) Class.forName(adapter.getAdapter()).getConstructor(new Class[]{}).newInstance(new Object[]{});
			} catch (Exception e){
				throw new HomeException(e.getMessage(), e);
			}
            if(tm != null)
            {
            	 return tm.calculatePaymentTax(ctx, spid, msisdn, amount, zipAdresss);
            }
        }
        else
        {
            throw new HomeException("No TaxAdapterSpidExtensions defined");
        }
    	return 0;
    }
    
}
