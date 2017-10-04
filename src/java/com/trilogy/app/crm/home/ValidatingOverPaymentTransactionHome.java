package com.trilogy.app.crm.home;

import com.trilogy.app.crm.transaction.OverPaymentTransactionValidator;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.ValidatingHome;

/**
 * Validation to restrict OverPayment transaction through BSS GUI and API.
 * @author shailesh.makhijani
 * @since 9.7.1.1
 *
 */
public class ValidatingOverPaymentTransactionHome extends ValidatingHome {

	/**
	 * serial version id
	 */
	private static final long serialVersionUID = 6979104049156978345L;
	
	private static CompoundValidator validator = new CompoundValidator();

	static 
	{
		validator.add(new OverPaymentTransactionValidator());
	}

	public ValidatingOverPaymentTransactionHome(Home delegate) {
		super(delegate, validator);	
	}

}
