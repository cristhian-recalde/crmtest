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

	/**
	 * This validator is used a dummy validator to show the drop down in the external file validation configuration
	 *
	 * @author Manish.Negi@redknee.com
	 */

package com.trilogy.app.crm.inboundfile.validators;
import com.trilogy.util.poller.validate.IPostFileValidation;
import com.trilogy.framework.xhome.context.Context;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
	
import com.trilogy.framework.xhome.beans.AbstractBean;


public class NoPostPaymentFileValidation extends AbstractBean implements IPostFileValidation {
	
		@Override
		public void postValidate(Context ctx, File file, boolean validationResult) {
			return;
		}
		
		@Override
		public boolean transientEquals(Object paramObject) {
			return true;
		}

		@Override
		public boolean persistentEquals(Object paramObject) {
			return true;
		}
		

}
