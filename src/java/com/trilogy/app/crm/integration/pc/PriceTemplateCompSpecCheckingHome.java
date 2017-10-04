package com.trilogy.app.crm.integration.pc;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import com.trilogy.app.crm.bean.ui.CompatibilitySpecs;
import com.trilogy.app.crm.bean.ui.PriceTemplate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author AChatterjee
 *
 */
public class PriceTemplateCompSpecCheckingHome extends HomeProxy 
{

	private static final long serialVersionUID = 1L;

	public final static Pattern COMPATIBILITY_SPECS = Pattern.compile("^[^\\s_%#&<>][^%#&<>]*$");


	public PriceTemplateCompSpecCheckingHome(Context ctx, Home delegate)
	{
		super(ctx, delegate);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
	HomeInternalException 
	{
		LogSupport.debug(ctx, this, "[PriceTemplateCompSpecCheckingHome.create], Creating the Price Template after validating Compatibility Specs");
		
		PriceTemplate template = (PriceTemplate)obj;
		List compatibilitySpecs = template.getCompatibilitySpecs();
		if (compatibilitySpecs.size() > 0)
		{
			validateCompatibilitySpecs(ctx, compatibilitySpecs);
		}
		else if(compatibilitySpecs.size() == 0){
			LogSupport.info(ctx, this, "Atleast one Compatibility Specs is reuired");
			throw new HomeException("Atleast one Compatibility Specs is reuired");
		}
		return super.create(ctx, obj);
	}

	

	@Override
	public Object store(Context ctx, Object obj) throws HomeException
	{
		LogSupport.debug(ctx, this, "[PriceTemplateCompSpecCheckingHome.store] Updating the Price Template after validating Compatibility Specs");
		
		PriceTemplate template = (PriceTemplate)obj;	
		List compatibilitySpecs = template.getCompatibilitySpecs();
		if (compatibilitySpecs.size() > 0)
		{
			validateCompatibilitySpecs(ctx, compatibilitySpecs);
		}
		else if(compatibilitySpecs.size() == 0){
			LogSupport.info(ctx, this, "Atleast one Compatibility Specs is reuired");
			throw new HomeException("Atleast one Compatibility Specs is reuired!");
		}
		return super.store(ctx, template);
	}
	
	/**
	 * @param compatibilitySpecs
	 * @throws HomeException
	 */
	private void validateCompatibilitySpecs(Context ctx, List compatibilitySpecs) throws HomeException {
		
		LogSupport.debug(ctx, this, "[validateCompatibilitySpecs] validating compatibilitySpecs");
		
		Set<String> set = new HashSet<String>();
		
		Iterator itr = compatibilitySpecs.iterator();
		CompatibilitySpecs spec = null;
		while(itr .hasNext())
		{		
			spec = (CompatibilitySpecs) itr.next();
			if(spec != null){
				if (spec.getCompatibilitySpecsName().equals("") || (spec.getCompatibilitySpecsName().trim().isEmpty()))
				{
					LogSupport.info(ctx, this, "[validateCompatibilitySpecs] Compatibility Specs name can not be empty");
					throw new HomeException("Compatibility Specs name can not be empty");
				}
				if(spec.getCompatibilitySpecsName() != null && !COMPATIBILITY_SPECS.matcher(String.valueOf(spec.getCompatibilitySpecsName())).matches() ){
					LogSupport.info(ctx, this, "[validateCompatibilitySpecs] Compatibility Specs name should not contain '%', '#', '&', '<' or '>' and cannot start with _ or blank space.");
					throw new HomeException("Compatibility Specs name should not contain '%', '#', '&', '<' or '>' and cannot start with _ or blank space.");	
				}
				if(spec.getCompatibilitySpecsDescription() != null && !COMPATIBILITY_SPECS.matcher(String.valueOf(spec.getCompatibilitySpecsDescription())).matches()){
					LogSupport.info(ctx, this, "[validateCompatibilitySpecs] Compatibility Specs description should not contain '%', '#', '&', '<' or '>' and cannot start with _ or blank space.");
					throw new HomeException("Compatibility Specs description should not contain '%', '#', '&', '<' or '>' and cannot start with _ or blank space.");
				}
				if (!set.add(spec.getCompatibilitySpecsName()))
				{
					LogSupport.info(ctx, this, "[validateCompatibilitySpecs] Compatibility Specs name must be Unique");
					throw new HomeException("Compatibility Specs name must be Unique");
				}
			}
		}
	}

}
