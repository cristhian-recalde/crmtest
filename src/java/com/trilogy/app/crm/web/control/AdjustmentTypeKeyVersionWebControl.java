package com.trilogy.app.crm.web.control;

import com.trilogy.app.crm.bean.AdjustmentTypeIdentitySupport;

import java.io.PrintWriter;
import java.util.Set;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.bean.AdjustmentTypeIdentitySupport;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.framework.xhome.beans.Child;
import com.trilogy.framework.xhome.beans.ChildComposer;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.webcontrol.tree.KeyTreeWebControl;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.bean.ui.AdjustmentTypeN;
import com.trilogy.app.crm.bean.ui.AdjustmentTypeNHome;
import com.trilogy.app.crm.bean.ui.AdjustmentTypeNIdentitySupport;
import com.trilogy.app.crm.bean.ui.AdjustmentTypeNVersionHome;
import com.trilogy.app.crm.bean.ui.AdjustmentTypeNVersionIdentitySupport;

public class AdjustmentTypeKeyVersionWebControl extends KeyTreeWebControl	 {

	
	
	public AdjustmentTypeKeyVersionWebControl()
    {
        this(AdjustmentTypeHome.class);
    }
    
    /**
     * Creates a new AdjustmentTypeKeyTreeWebControl.
     */
    public AdjustmentTypeKeyVersionWebControl(final Object homeKey)
    {
        this(homeKey, true, false, true);
    }

    /**
     * Creates a new AdjustmentTypeKeyTreeWebControl.
     */
    public AdjustmentTypeKeyVersionWebControl(final Object homeKey, final boolean showAll)
    {
        this(homeKey, showAll, false, showAll);
    }

    /**
     * Creates a new AdjustmentTypeKeyTreeWebControl.
     */
    public AdjustmentTypeKeyVersionWebControl(final Object homeKey, final boolean showAll,
            final boolean showCategoriesOnly)
    {
        this(homeKey, showAll, showCategoriesOnly, showAll);
    }
    
    /**
     * Creates a new AdjustmentTypeKeyTreeWebControl.
     */
    public AdjustmentTypeKeyVersionWebControl(final Object homeKey, final boolean showAll,
            final boolean showCategoriesOnly, final boolean showRoot)
    {
        super(AdjustmentTypeIdentitySupport.instance(), homeKey, true);
        showAll_ = showAll;
        showRoot_ = showRoot;
        showCategoriesOnly_ = showCategoriesOnly;
    }

    /**
     * Creates a new AdjustmentTypeKeyTreeWebControl.
     */
    public AdjustmentTypeKeyVersionWebControl(final Object homeKey, final boolean showAll,
            final boolean showCategoriesOnly, final boolean showRoot,
            final boolean isPreview)
    {
        super(AdjustmentTypeIdentitySupport.instance(), homeKey, isPreview);
        showAll_ = showAll;
        showRoot_ = showRoot;
        showCategoriesOnly_ = showCategoriesOnly;
    }

    /**
     * Creates a new AdjustmentTypeKeyTreeWebControl.
     */
    public AdjustmentTypeKeyVersionWebControl(final Object homeKey,
            final boolean showCategoriesOnly, final boolean showRoot,
            final boolean isPreview, final AdjustmentTypeEnum... filteredCategories)
    {
        super(AdjustmentTypeIdentitySupport.instance(), homeKey, isPreview);
        showAll_ = false;
        filteredCategories_ = filteredCategories;
        showRoot_ = showRoot;
        showCategoriesOnly_ = showCategoriesOnly;
    }

    @Override
    public String getDesc(Context ctx, Object child)
    {
        final AdjustmentType type = (AdjustmentType) child;

        return type.getName();
    }

    protected void outputRoot(final Context ctx, final PrintWriter out,
            final Object obj)
    {
        if (showRoot_)
        {
            out.print("<option value=\"0\">");
            out.print(getRoot());
            out.print("</option>");
        }
    }

    @Override
    protected void outputVirtualRoot(Context ctx, PrintWriter out)
    {
       out.print("<option value=\"0\">");
       out.print("[root]");
       out.print("</option>");
    }

    @Override
    protected void outputChild(final Context ctx, final PrintWriter out,
            final ChildComposer relationships, final Child child,
            final int numOfIndent, final Object obj, final int mode)
    {
        final AdjustmentType type = (AdjustmentType) child;

        if (!showAll_
                && (type == null
                        || (type.isInOneOfCategories(ctx, filteredCategories_))))
        {
            return;
        }

        if (showCategoriesOnly_ && !type.isCategory())
        {
            return;
        }


        Set<AdjustmentTypeEnum> disableCats = AdjustmentTypeSupportHelper.get(ctx).getHiddenAdjustmentTypes(ctx);

        for (AdjustmentTypeEnum category : disableCats)
        {
            if (type.isInCategory(ctx, category))
            {
                return;
            }
        }

        super.outputChild(ctx, out, relationships, child, numOfIndent, obj,
                mode);
    }

    @Override
    public Object fromWeb(Context ctx, ServletRequest req, String name)
    {
        final String parameterValue = req.getParameter(name);

        if (name.equals(".parentCode"))
        {
            if (parameterValue == null || "".equals(parameterValue))
            {
                return Integer.valueOf(0);
            }
        }

        return super.fromWeb(ctx, req, name);
    }

    // INHERIT
    @Override
    public void toWeb(final Context ctx, final PrintWriter out,
            final String name, final Object obj)
    {
        final int mode = ctx.getInt("MODE", DISPLAY_MODE);

        if (mode == DISPLAY_MODE)
        {
            try
            {
                final Home home = (Home) ctx.get(AdjustmentTypeHome.class);
                final AdjustmentType type = (AdjustmentType) home
                        .find(ctx, obj);

                // TODO 2007-05-28 handle missing types, i.e. type is null
                out.println(type.getName());
            }
            catch (final Throwable throwable)
            {
                new MinorLogMsg(this, "Unable to determine how to present "
                        + obj, throwable).log(ctx);
            }
        }
        else
        {
            super.toWeb(ctx, out, name, obj);
        }
    }

    /**
     * True if all types should be shown; false if some types should be filtered
     * out.
     */
    protected final boolean showAll_;

    /**
     * True if only categories should be shown
     */
    protected final boolean showCategoriesOnly_;

    /**
     * True if root should be shown.
     */
    protected final boolean showRoot_;
    
    /**
     * Categories to be filtered if showAll_ is set to false.
     */
    protected AdjustmentTypeEnum[] filteredCategories_ = new AdjustmentTypeEnum[]
        {AdjustmentTypeEnum.RecurringCharges, AdjustmentTypeEnum.AuxiliaryServices,
                AdjustmentTypeEnum.AuxiliaryBundles, AdjustmentTypeEnum.DiscountAuxiliaryServices,
                AdjustmentTypeEnum.InterestPayment};
}
