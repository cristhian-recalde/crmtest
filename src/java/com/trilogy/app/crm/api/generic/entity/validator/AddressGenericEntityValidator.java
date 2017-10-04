/**
 * 
 */
package com.trilogy.app.crm.api.generic.entity.validator;

import com.trilogy.app.crm.bean.Address;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Validator for Address CRUD entity 
 * @author isha.aderao
 *
 */
public class AddressGenericEntityValidator extends
		AbstractGenericEntityValidator implements Validator {

	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.beans.Validator#validate(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	@Override
	public void validate(Context ctx, Object obj) throws IllegalStateException 
	{
		Address address = (Address) obj;
		String ban = null;
		CompoundIllegalStateException el = new CompoundIllegalStateException();
		if(address == null)
		{
			LogSupport.minor(ctx, this, "Address request can not be null");
			el.thrown(new IllegalPropertyArgumentException("ADDRESS", "Address request can not be null"));
            el.throwAll();
		}
	}
	

}
