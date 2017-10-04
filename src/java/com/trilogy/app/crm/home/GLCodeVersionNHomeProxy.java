package com.trilogy.app.crm.home;


import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.GLCodeN;
import com.trilogy.app.crm.bean.GLCodeVersionN;
import com.trilogy.app.crm.bean.GLCodeVersionNHome;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.app.crm.home.GLCodeVersionNSupport;

public class GLCodeVersionNHomeProxy extends HomeProxy{

	public GLCodeN glCodeN_ = null;

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new GLCodeVersionNHomeProxy for the given delegate.
	 * 
	 * @param ctx
	 * @param delegate
	 * The home to which this decorator delegates.
	 */


	public GLCodeVersionNHomeProxy(final Context ctx, final Home delegate)
	{
		super(ctx, delegate);
	}


	/**
	 * Creates the new version and updated the "NextVersion" property of the
	 * owning GLCodeN.
	 */
	@Override
	public Object create(final Context ctx, final Object obj)
	throws HomeException
	{
		final GLCodeVersionN proposedVersion = (GLCodeVersionN) obj;
		final boolean isNewGLCodeN = (proposedVersion.getVersionId() == 0);


		try
		{
			final GLCodeN plan = getGlCodeNForVersion(ctx, proposedVersion);
			
			// claim the version number first
			claimNextVersionIdentifier(ctx, plan, proposedVersion);

			final GLCodeVersionN newVersion =
				(GLCodeVersionN) super.create(ctx, proposedVersion);

			return newVersion;
		}
		catch (final HomeException exception)
		{
			if (isNewGLCodeN)
			{
				new OMLogMsg(Common.OM_MODULE,
						Common.OM_GLCODEN_VERSIONN_CREATION_FAIL).log(ctx);
			}
			Common.OM_GLCODEN_VERSION_MODIFICATION.failure(ctx);
			throw exception;
		}
	}




	/**
	 * Only unactivated versions can be removed.
	 */
	@Override
	public void remove(final Context ctx, final Object obj)
	throws HomeException
	{
		try
		{
			Common.OM_GLCODE_VERSION_DELETION.attempt(ctx);

			final GLCodeVersionN version = (GLCodeVersionN) obj;
			final GLCodeN plan = getGlCodeNForVersion(ctx, version);


			if (plan.getCurrentVersion() >= version.getVersionId())
			{
				throw new HomeException(
						"Cannot remove an active, or previously activated version.");
			}

			rollback(ctx, plan, version);

			Common.OM_GLCODE_VERSION_DELETION.success(ctx);
		}
		catch (final HomeException exception)
		{
			Common.OM_GLCODE_VERSION_DELETION.failure(ctx);
			throw exception;
		}
	}




	/**
	 * GLCodeVersionN can now be updated.
	 */
	@Override
	public Object store(final Context ctx, final Object obj)
	throws HomeException
	{
		Common.OM_GLCODEN_VERSION_MODIFICATION.attempt(ctx);
		final Home home = (Home) ctx.get(GLCodeVersionNHome.class);
		final GLCodeVersionN oldVersion = (GLCodeVersionN) home.find(ctx, obj);

		if (LogSupport.isDebugEnabled(ctx))
		{
			new DebugLogMsg(this, "Attempt to update glcode version from "
					+ oldVersion + " to " + obj, null).log(ctx);
		}

		GLCodeVersionN newVersion;

		newVersion = (GLCodeVersionN) obj;
		final GLCodeN plan = getGlCodeNForVersion(ctx, newVersion);
		final GLCodeVersionN previousVersion =GLCodeVersionNSupport.findVersionBefore(ctx, plan,newVersion.getVersionId());
		final GLCodeVersionN nextVersion =GLCodeVersionNSupport.findVersionAfter(ctx, plan,newVersion.getVersionId());
		newVersion = (GLCodeVersionN) super.store(ctx, obj);
		return newVersion;
	}




	/**
	 * Claims the next version number, and updates the plan accordingly. The
	 * version number is set in the given proposed version.
	 * 
	 * @param plan
	 *            The GLCodeN to which the version belongs.
	 * @param proposedVersion
	 *            The proposed new version of the plan.
	 * @exception HomeException
	 *                Thrown if there are problems accessing Home data
	 *                in the context.
	 */
	private void claimNextVersionIdentifier(final Context ctx,
			final GLCodeN plan, final GLCodeVersionN proposedVersion)
	throws HomeException
	{
		
		final int version = plan.getNextVersion();
		
		plan.setNextVersion(version + 1);
		HomeSupportHelper.get(ctx).storeBean(ctx, plan);
		// Explicitly override whatever version might have been set -- the
		// proposed version could not know for sure what it is to be.
		proposedVersion.setVersionId(version);

	}


	/**
	 * Gets the GLCodeN for a given GLCodeVersionN.
	 * 
	 * @param version
	 *            The GLCodeVersionN for which to get the GLCodeN.
	 * @return The GLCodeN.
	 * @exception HomeException
	 *                Thrown if the GLCodeN cannot be found in the
	 *                context.
	 */	

	private GLCodeN getGlCodeNForVersion(final Context ctx,final GLCodeVersionN version) throws HomeException
	{

		glCodeN_ = HomeSupportHelper.get(ctx).findBean(ctx, GLCodeN.class,version.getGlCode());
		final GLCodeN glcode = glCodeN_;

		if (glcode == null)
		{
			throw new HomeException("Invalid GLCodeN ID " + version.getGlCode());  
		}

		return glcode;
	}	

	

	private void rollback(final Context ctx, final GLCodeN plan,final GLCodeVersionN badVersion) throws HomeException
	{
		super.remove(ctx, badVersion);
	}

}
