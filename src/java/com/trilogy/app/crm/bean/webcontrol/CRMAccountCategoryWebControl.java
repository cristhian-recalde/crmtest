package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.app.crm.bean.AccountCategoryWebControl;
import com.trilogy.app.crm.billing.message.GenericBillingMessageTableWebControl;
import com.trilogy.app.crm.web.control.CustomizedGlcodeKeyWebControl;
import com.trilogy.framework.xhome.msp.SetSpidProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

public class CRMAccountCategoryWebControl extends AccountCategoryWebControl {
	@Override
	public WebControl getGLCodeWebControl() {
		return CUSTOM_GL_CODE_WC;
	}

	@Override
	public WebControl getBillingMessagesWebControl() {
		return CUSTOM_BILLING_MESSAGES_WC;
	}

	public static final WebControl CUSTOM_BILLING_MESSAGES_WC = new GenericBillingMessageTableWebControl();
	public static final WebControl CUSTOM_GL_CODE_WC = new SetSpidProxyWebControl(
			new CustomizedGlcodeKeyWebControl());
    
  
    
}
