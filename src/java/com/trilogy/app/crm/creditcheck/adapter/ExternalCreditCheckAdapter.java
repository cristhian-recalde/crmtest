package com.trilogy.app.crm.creditcheck.adapter;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.api.generic.entity.adapter.DefaultGenericEntityAdapter;
import com.trilogy.app.crm.api.generic.entity.adapter.EntityParsingException;
import com.trilogy.app.crm.bean.CreditCheckResult;
import com.trilogy.app.crm.bean.ExternalCreditCheck;
import com.trilogy.app.crm.bean.ExternalCreditCheckXInfo;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.xi.AbstractXInfo;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.types.genericentity.Entity;

public class ExternalCreditCheckAdapter extends DefaultGenericEntityAdapter {

	@Override
	public Object adapt(Context ctx, Entity entity) throws EntityParsingException
    {
		AbstractBean bean = (AbstractBean) super.adapt(ctx, entity);
		return adaptExternalCreditCheckResult(entity, bean);
    }
	
	@Override
	public Entity unAdapt(Context ctx, Object entity) throws EntityParsingException{
		Entity bean = super.unAdapt(ctx, entity);
		return unadaptExternalCreditCheckResults(ctx, entity, bean);
	}

	private Entity unadaptExternalCreditCheckResults(Context ctx, Object entity, Entity bean)
			throws EntityParsingException {
		AbstractXInfo xInfo = getXInfo(ctx, bean, entity);
        List<PropertyInfo> propertieslist = xInfo.getProperties(ctx);
        for (PropertyInfo property : propertieslist)
        {
        	if(ExternalCreditCheckXInfo.CREDIT_CHECK_RESULT.getSQLName().equals(property.getSQLName()))
        	{
        		if(property.get(entity)!=null){
        			List<CreditCheckResult> resultListFromDb = (List) property.get(entity); 
        			if(resultListFromDb.size()>0 && resultListFromDb.get(0) instanceof CreditCheckResult){
        				CreditCheckResult result = resultListFromDb.get(0);
        				bean.addProperty(createGenericParameter(ExternalCreditCheckXInfo.CREDIT_CHECK_RESULT.getSQLName(),result.getResult()));
        			}
        		}
        		break;
        	}
        }
		return bean;
	}

	private ExternalCreditCheck adaptExternalCreditCheckResult(Entity entity, AbstractBean bean) {
		ExternalCreditCheck extBean = (ExternalCreditCheck) bean;
		GenericParameter[] genericParameters = entity.getProperty();
		List result = new ArrayList();
		
		for (GenericParameter param : genericParameters)
        {
            if (param.getName().equals(ExternalCreditCheckXInfo.CREDIT_CHECK_RESULT.getSQLName()))
            {
            	CreditCheckResult ccresult = new CreditCheckResult();
            	ccresult.setResult(param.getValue()!=null?param.getValue().toString():"");
            	result.add(ccresult);
                break;
            }
        }
		extBean.setCreditCheckResult(result);
		return extBean;
	}
}
