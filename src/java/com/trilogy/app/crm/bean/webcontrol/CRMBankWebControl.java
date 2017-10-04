package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.app.crm.bean.CRMSpidKeyWebControl;
import com.trilogy.app.crm.bean.bank.BankWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

public class CRMBankWebControl  extends BankWebControl 
{

   
    
    @Override
	public WebControl getSpidWebControl() 
    {
    	return CUSTOM_SPID_WC; 
	}
    

	public static final WebControl CUSTOM_SPID_WC = new CRMSpidKeyWebControl(true) ;
    
}
