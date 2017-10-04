package com.trilogy.app.crm.integration.pc;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ui.CompatibilitySpecs;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplate;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * Checks if the Compatibility Specs is not null
 * 
 * @author dinesh.valsatwar@redknee.com
 *
 */
public class TechnicalServiceCompSpecCheckingHome extends HomeProxy {

	private static final long serialVersionUID = 1L;

	public final static Pattern COMPATIBILITY_SPECS = Pattern
			.compile("^[^\\s_%#&<>][^%#&<>]*$");

	public TechnicalServiceCompSpecCheckingHome(Context ctx, Home delegate) {
		super(ctx, delegate);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
			HomeInternalException {
		
		TechnicalServiceTemplate service = (TechnicalServiceTemplate) obj;
		
		LogSupport.info(ctx, this, "[TechnicalServiceCompSpecCheckingHome.create] creating the Technical Service template serviceID:" + service.getID());
		
		// LogSupport.info(ctx, this, "creating the Technical Service template ");
		
		List compatibilitySpecs = service.getCompatibilitySpecs();
		/*
		 * if (compatibilitySpecs.size()==0) throw new HomeException("Compatibility Specs can not be empty!");
		 */
		
		if (compatibilitySpecs.size() > 0) {
			Set<String> set = new HashSet<String>();
			Iterator itr = compatibilitySpecs.iterator();
			CompatibilitySpecs spec = null;
			
			while (itr.hasNext()) {
				spec = (CompatibilitySpecs) itr.next();
				
				if (spec.getCompatibilitySpecsName().equals("")
						|| (spec.getCompatibilitySpecsName().trim().isEmpty()))
					throw new HomeException( "Compatibility Specs name can not be empty!");
				if (!set.add(spec.getCompatibilitySpecsName()))
					throw new HomeException( "Compatibility Specs name must be Unique!");
			}
		}
		/*
		 * else if(compatibilitySpecs.size() == 0){ 
		 * LogSupport.info(ctx, this, "Atleast one Compatibility Specs is required"); 
		 * throw new HomeException("Atleast one Compatibility Specs is reuired!"); }
		 */
		return super.create(ctx, obj);
	}

	@Override
	public Object store(Context ctx, Object obj) throws HomeException {
		
		TechnicalServiceTemplate service = (TechnicalServiceTemplate) obj;
		
		LogSupport.info(ctx, this, "[TechnicalServiceCompSpecCheckingHome.store] Updating the Technical Service template " + service.getID());
		
		List compatibilitySpecs = service.getCompatibilitySpecs();
		
		if (compatibilitySpecs.size() > 0) {
			CompatibilitySpecs spec = null;
			Set<String> set = new HashSet<String>();
			Iterator itr = compatibilitySpecs.iterator();
			
			while (itr.hasNext()) {
				spec = (CompatibilitySpecs) itr.next();
				
				if (spec != null) {
					if (spec.getCompatibilitySpecsName().equals("")
							|| (spec.getCompatibilitySpecsName().trim().isEmpty()))
						throw new HomeException("Compatibility Specs name can not be empty!");
					if (spec.getCompatibilitySpecsName() != null
							&& !COMPATIBILITY_SPECS.matcher(String.valueOf(spec.getCompatibilitySpecsName())).matches()) {
						throw new HomeException("Compatibility Specs name should not contain '%', '#', '&', '<' or '>' and cannot start with _ or blank space.");
					}
					if (spec.getCompatibilitySpecsDescription() != null
							&& !COMPATIBILITY_SPECS.matcher(String.valueOf(spec.getCompatibilitySpecsDescription())).matches()) {
						throw new HomeException("Compatibility Specs description should not contain '%', '#', '&', '<' or '>' and cannot start with _ or blank space.");
					}
					if (!set.add(spec.getCompatibilitySpecsName()))
						throw new HomeException("Compatibility Specs name must be Unique!");
				}
			}
		}
		
		/*
		 * else if(compatibilitySpecs.size() == 0){ 
		 * LogSupport.info(ctx, this, "Atleast one Compatibility Specs is required "); 
		 * throw new HomeException("Atleast one Compatibility Specs is required!"); }
		 */
		
		return super.store(ctx, service);
	}

}
