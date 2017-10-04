package com.trilogy.app.crm.home;



import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.ui.SubGLCodeN;
import com.trilogy.app.crm.bean.ui.SubGLCodeVersionN;
import com.trilogy.app.crm.bean.ui.SubGLCodeVersionNHome;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;

public class SubGLCodeVersionNHomeProxy extends HomeProxy{
	
	
	public SubGLCodeN subGlCodeN_ = null;
	
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new SubGLCodeVersionNHomeProxy for the given delegate.
	 * 
	 * @param ctx
	 * @param delegate
	 * The home to which this decorator delegates.
	 */


	public SubGLCodeVersionNHomeProxy(final Context ctx, final Home delegate)
	{
		super(ctx, delegate);
	}


	/**
	 * Creates the new version and updated the "NextVersion" property of the
	 * owning SubGLCodeN.
	 */
	@Override
	public Object create(final Context ctx, final Object obj)
	throws HomeException
	{
		final SubGLCodeVersionN proposedVersion = (SubGLCodeVersionN) obj;
		final boolean isNewGLCodeN = (proposedVersion.getVersionId() == 0);


		try
		{
			final SubGLCodeN subglcode = getSubGlCodeNForVersion(ctx, proposedVersion);
			
			// claim the version number first
			claimNextVersionIdentifier(ctx, subglcode, proposedVersion);

			final SubGLCodeVersionN newVersion =
				(SubGLCodeVersionN) super.create(ctx, proposedVersion);

			return newVersion;
		}
		catch (final HomeException exception)
		{
			if (isNewGLCodeN)
			{
				new OMLogMsg(Common.OM_MODULE,
						Common.OM_SUBGLCODEN_VERSIONN_CREATION_FAIL).log(ctx);
			}
			Common.OM_SUBGLCODEN_VERSION_MODIFICATION.failure(ctx);
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
			Common.OM_SUBGLCODEN_VERSION_DELETION.attempt(ctx);

			final SubGLCodeVersionN version = (SubGLCodeVersionN) obj;
			final SubGLCodeN subglcode = getSubGlCodeNForVersion(ctx, version);


			if (subglcode.getCurrentVersion() >= version.getVersionId())
			{
				throw new HomeException(
						"Cannot remove an active, or previously activated version.");
			}

			rollback(ctx, subglcode, version);

			Common.OM_SUBGLCODEN_VERSION_DELETION.success(ctx);
		}
		catch (final HomeException exception)
		{
			Common.OM_SUBGLCODEN_VERSION_DELETION.failure(ctx);
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
		final Home home = (Home) ctx.get(SubGLCodeVersionNHome.class);
		final SubGLCodeVersionN oldVersion = (SubGLCodeVersionN) home.find(ctx, obj);

		if (LogSupport.isDebugEnabled(ctx))
		{
			new DebugLogMsg(this, "Attempt to update sub glcode version from "
					+ oldVersion + " to " + obj, null).log(ctx);
		}

		SubGLCodeVersionN newVersion;

		newVersion = (SubGLCodeVersionN) obj;
		//final SubGLCodeN subglcode = getSubGlCodeNForVersion(ctx, newVersion);
		//final GLCodeVersionN previousVersion =GLCodeVersionNSupport.findVersionBefore(ctx, plan,newVersion.getVersionId());
		//final GLCodeVersionN nextVersion =GLCodeVersionNSupport.findVersionAfter(ctx, plan,newVersion.getVersionId());
		newVersion = (SubGLCodeVersionN) super.store(ctx, obj);
		return newVersion;
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
	private SubGLCodeN getSubGlCodeNForVersion(final Context ctx,final SubGLCodeVersionN version) throws HomeException
	{

		subGlCodeN_ = HomeSupportHelper.get(ctx).findBean(ctx, SubGLCodeN.class,version.getId());
		final SubGLCodeN subglcode = subGlCodeN_;

		if (subglcode == null)
		{
			throw new HomeException("Invalid SubGLCodeN ID " + version.getId());  
		}

		return subglcode;
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
			final SubGLCodeN subglcode, final SubGLCodeVersionN proposedVersion)
	throws HomeException
	{
		
		final int version = subglcode.getNextVersion();
		
		subglcode.setNextVersion(version + 1);
		HomeSupportHelper.get(ctx).storeBean(ctx, subglcode);
		// Explicitly override whatever version might have been set -- the
		// proposed version could not know for sure what it is to be.
		proposedVersion.setVersionId(version);

	}
	
	
	private void rollback(final Context ctx, final SubGLCodeN plan,final SubGLCodeVersionN badVersion) throws HomeException
	{
		super.remove(ctx, badVersion);
	}

}
