package com.trilogy.app.crm.home;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import com.trilogy.app.crm.bean.GLCodeN;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.bean.GLCodeVersionN;
import com.trilogy.app.crm.bean.GLCodeVersionNID;
import com.trilogy.app.crm.bean.GLCodeVersionNXInfo;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.ServiceSubTypeEnum;
import com.trilogy.app.crm.support.HomeSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;
import com.trilogy.app.crm.xhome.adapter.ServicePackageVersionBeanAdapter;



public class GLCodeVersionNSupport extends GLCodeVersionN{

	public GLCodeN glCodeN_ = null;
	public static final int   MAX_VALUE = 0x7fffffff;



	/**
	 * Finds the GLCodeN version of a given GLCodeN with the highest
	 * version number.
	 * 
	 * @param context
	 *            The operating context.
	 * @param plan
	 *            The plan for which to look up the most recent GLCodeN
	 *            version.
	 * @return The GLCodeN version with the highest version number.
	 * @throws HomeException
	 *             Thrown if there are problems accessing GLCodeN or GLCodeN
	 *              version home.
	 */
	public static GLCodeVersionN findHighestVersion(final Context context,final GLCodeN plan) throws HomeException
	{
		return findVersionBefore(context, plan, Integer.MAX_VALUE);
	}




	/**
	 * Finds the GLCodeN version of a given GLCodeN with the highest
	 * version number prior to the given one.
	 * 
	 * @param context
	 *            The operating context.
	 * @param plan
	 *            The plan for which to look up the most recent GLCodeN
	 *            version.
	 * @param version
	 *            The upper bound of the version number.
	 * @return The GLCodeN version with the highest version number prior to
	 *         <code>version</code>.
	 * @throws HomeException
	 *             Thrown if there are problems accessing GLCodeN or GLCodeN version home.
	 */

	public static GLCodeVersionN findVersionBefore(final Context context,final GLCodeN plan, long version) throws HomeException
	{
		HomeSupport homeSupport = HomeSupportHelper.get(context);

		And filter = new And();
		filter.add(new EQ(GLCodeVersionNXInfo.GL_CODE, plan.getGlCode()));
		filter.add(new LT(GLCodeVersionNXInfo.VERSION_ID, version));

		Object maxVersionObj = homeSupport.max(context, GLCodeVersionNXInfo.VERSION_ID, filter);
		if (maxVersionObj instanceof Number)
		{
			Number maxVersion = (Number) maxVersionObj;
			return homeSupport.findBean(context, 
					GLCodeVersionN.class, 
					new GLCodeVersionNID(plan.getGlCode(), maxVersion.intValue()));
		}

		return null;
	}






	public static GLCodeVersionN findVersionAfter(final Context context,final GLCodeN plan, long version) throws HomeException
	{
		HomeSupport homeSupport = HomeSupportHelper.get(context);

		And filter = new And();
		filter.add(new EQ(GLCodeVersionNXInfo.GL_CODE, plan.getGlCode()));
		filter.add(new LT(GLCodeVersionNXInfo.VERSION_ID, version));

		Object minVersionObj = homeSupport.min(context, GLCodeVersionNXInfo.VERSION_ID, filter);
		if (minVersionObj instanceof Number)
		{
			Number minVersion = (Number) minVersionObj;
			return homeSupport.findBean(context, 
					GLCodeVersionN.class, 
					new GLCodeVersionNID(plan.getGlCode(), minVersion.intValue()));
		}

		return null;
	}



}
