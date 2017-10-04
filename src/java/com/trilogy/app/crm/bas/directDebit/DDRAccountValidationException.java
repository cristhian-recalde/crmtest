package com.trilogy.app.crm.bas.directDebit;

import com.trilogy.framework.xhome.home.HomeException;

public class DDRAccountValidationException 
extends HomeException 
{

	public DDRAccountValidationException(String s, Throwable t) {
		super(s, t);
		
	}

	public DDRAccountValidationException(String s) {
		super(s);
		
	}

	public DDRAccountValidationException(Throwable t) {
		super(t);
	}

}
