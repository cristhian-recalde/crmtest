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
package com.trilogy.app.crm.web.control;

import com.trilogy.app.crm.bean.SubSrvToAuxSrvMapTableWebControl;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;


/**
 * Support inner class thar displays the services of the subscriber.
 * @author rajith.attapattu@redknee.com
 */
public class CustomServiceToAuxServiceMappingWebControl extends ProxyWebControl
{

public CustomServiceToAuxServiceMappingWebControl() {
	super(new SubSrvToAuxSrvMapTableWebControl());
}
    /**
     * {@inheritDoc}
     */
  @Override
public Context wrapContext(Context ctx) {
	final Context subCtx = ctx.createSubContext();
	subCtx.put(AbstractWebControl.NUM_OF_BLANKS, AbstractWebControl.DEFAULT_BLANKS);
	return subCtx;
}

}
