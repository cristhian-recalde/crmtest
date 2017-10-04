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
package com.trilogy.app.crm.extension.auxiliaryservice.core.custom;

import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.extension.AssociableExtension;
import com.trilogy.app.crm.extension.ExtensionAssociationException;
import com.trilogy.framework.xhome.context.Context;

/*
 * @since 9.4.0
 * @author bhagyashree.dhavalshankh@redknee.com
 *  
 */
public class TFAAuxSvcExtension extends
        com.redknee.app.crm.extension.auxiliaryservice.core.TFAAuxSvcExtension implements
        AssociableExtension<SubscriberAuxiliaryService>
{
 

  

    private static final long                            serialVersionUID    = 1L;

	@Override
	public String getName(Context ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription(Context ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSummary(Context ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object ID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void associate(Context ctx, SubscriberAuxiliaryService associatedBean)
			throws ExtensionAssociationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateAssociation(Context ctx,
			SubscriberAuxiliaryService associatedBean)
			throws ExtensionAssociationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dissociate(Context ctx,
			SubscriberAuxiliaryService associatedBean)
			throws ExtensionAssociationException {
		// TODO Auto-generated method stub
		
	}
}
