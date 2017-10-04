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
 * This validator is responsible for updating the PaymentFileTracker table
 * Depending on the prevalidation result the PaymentFileTracker table is updated
 *
 * @author Manish.Negi@redknee.com
 */

package com.trilogy.app.crm.inboundfile.validators;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.home.HomeAware;
import com.trilogy.util.poller.validate.IPostFileValidation;
import com.trilogy.app.crm.bean.PaymentFileTrackerHome;
import com.trilogy.app.crm.bean.PaymentFileTracker;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeException;
import java.util.Date;
import com.trilogy.framework.xhome.beans.AbstractBean;

public class PostPaymentFileValidation extends AbstractBean implements IPostFileValidation {

	private StringBuffer ERRORCODE = new StringBuffer("-1");
	private final String SUCCESS_NOTE = "PRE-VALIDATION FAILED";
	
	
	@Override
	public void postValidate(Context ctx, File file, boolean validationResult) {
	
		Home _hm = (Home)ctx.get(PaymentFileTrackerHome.class);
		PaymentFileTracker pTrackerBean = new PaymentFileTracker();
		
		//setting the paymenttrackerbean value in the DB table PaymentFileTracker
		//setting fields: FILENAME,DIRECTORYLOCATION, PROCESSEDDATE:putting current date when the prevalidation has happened, 
		//ERRORCODE=0: Success pre-validation -1: Failure pre-validation
		//NOTE: ERRORCODE=0: PREVALIDATION SUCCESSFULL, -1: either exception message from the ctx or PREVALIDATION FAILED 
		pTrackerBean.setFileName(file.getName());
		pTrackerBean.setDirectoryLocation(file.getParent());
		pTrackerBean.setProcessedDate(new Date());
		if(validationResult){
			ERRORCODE.append("0");
		}
		pTrackerBean.setErrorCode(ERRORCODE.toString());
		
		//Setting the error note
		String errorNote = SUCCESS_NOTE;
		if(ctx.get("VALIDATION_RESULT")!=null){
		Exception exp = (Exception)ctx.get("VALIDATION_RESULT");
	 	ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		exp.printStackTrace(ps);
		errorNote = new String(baos.toByteArray());
		
		}else{
			errorNote = SUCCESS_NOTE;
		}
		pTrackerBean.setNote(errorNote);
		
		//Saving the paymenttrackerbean
		try{
		_hm.create(ctx,pTrackerBean);
		}catch(HomeInternalException ex){
			ex.printStackTrace();
		}catch(HomeException ex){
			ex.printStackTrace();
		}
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
