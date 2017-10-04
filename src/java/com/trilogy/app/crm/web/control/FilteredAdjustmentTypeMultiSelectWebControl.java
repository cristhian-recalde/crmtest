package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.bean.AdjustmentTypeIdentitySupport;
import com.trilogy.app.crm.bean.AdjustmentTypeXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.webcontrol.MultiSelectWebControl;
import com.trilogy.framework.xlog.log.MajorLogMsg;

public class FilteredAdjustmentTypeMultiSelectWebControl extends MultiSelectWebControl {

	/**
	 * 
	 */
	public FilteredAdjustmentTypeMultiSelectWebControl() {
		super(AdjustmentTypeHome.class, AdjustmentTypeIdentitySupport.instance(),
				new com.redknee.framework.xhome.webcontrol.OutputWebControl() {
					@Override
					public void toWeb(Context ctx, PrintWriter out, String name, Object obj) {
						AdjustmentType adjustmentType = (AdjustmentType) obj;
						out.print(adjustmentType.getCode() + " - " + adjustmentType.getName());
					}
				});
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.redknee.framework.xhome.webcontrol.MultiSelectWebControl#
	 * outputWholeList(com.redknee.framework.xhome.context.Context,
	 * java.io.PrintWriter, com.redknee.framework.xhome.home.Home,
	 * java.lang.String, java.util.Set)
	 */
	@Override
	protected void outputWholeList(Context ctx, PrintWriter out, Home home, String name, Set selected) {
		// DO NOT call the super method
		// super.outputWholeList(ctx, out, home, name, selected);

		if (selectWebWidth_ > 0 && selectWebWidth_ <= 50) {
			out.println("<td width=\"" + selectWebWidth_ + "%\">");
		} else {
			out.println("<td>");
		}

		out.print("<table><tr><th>" + leftListTitle_ + "</th></tr><tr><td>");
		out.println("<select name=\"" + name + "\" multiple=\"multiple\" size=\"" + getSize() + "\" width=\"250\">");

		Home sortingHome = new SortingHome(home, getComparator());

		try {

			// prepare a filtered list of Adjustment Types
			Collection<AdjustmentType> filteredAdjTypes = fetchFilteredAdjTypes(ctx, sortingHome,
					AdjustmentTypeEnum.StandardPayments_INDEX);
			
			// adding dispute adjustment types
			AdjustmentType customerDisputeAdjustmentType=(AdjustmentType) HomeSupportHelper.get(ctx).findBean(ctx,
					AdjustmentType.class, new EQ(AdjustmentTypeXInfo.CODE, AdjustmentTypeEnum.CustomerDispute_INDEX));
			if(customerDisputeAdjustmentType !=null){
				filteredAdjTypes.add(customerDisputeAdjustmentType);
			}
			
			AdjustmentType customerDisputeAdjustmentTypeDebit=(AdjustmentType) HomeSupportHelper.get(ctx).findBean(ctx,
					AdjustmentType.class, new EQ(AdjustmentTypeXInfo.CODE, AdjustmentTypeEnum.CustomerDisputeDebit_INDEX));
			if(customerDisputeAdjustmentTypeDebit !=null){
				filteredAdjTypes.add(customerDisputeAdjustmentTypeDebit);
			}
			
			AdjustmentType disputeResolution=(AdjustmentType) HomeSupportHelper.get(ctx).findBean(ctx,
					AdjustmentType.class, new EQ(AdjustmentTypeXInfo.CODE, AdjustmentTypeEnum.DisputeResolution_INDEX));
			if(disputeResolution !=null){
				filteredAdjTypes.add(disputeResolution);
			}
			
			AdjustmentType disputeResolutionDebit=(AdjustmentType) HomeSupportHelper.get(ctx).findBean(ctx,
					AdjustmentType.class, new EQ(AdjustmentTypeXInfo.CODE, AdjustmentTypeEnum.DisputeResolutionDebit_INDEX));
			if(disputeResolutionDebit !=null){
				filteredAdjTypes.add(disputeResolutionDebit);
			}
			

			if (filteredAdjTypes != null && filteredAdjTypes.size() > 0) {
				for (AdjustmentType adjustmentType : filteredAdjTypes) {
					String string_key = toStringId(ctx, adjustmentType);

					if (selected.contains(adjustmentType)) {
						continue;
					}

					out.print("<option");
					out.print(" value=\"");
					out.print(string_key.trim());
					out.print("\">");

					outputBean(ctx, out, adjustmentType);

					out.print("</option>");
				}
			}
		} catch (HomeException e) {
			new MajorLogMsg(this, e.getMessage(), e).log(ctx);
		}

		out.println("</select>");
		out.print("</td></tr></table>");
		out.println("</td>");
	}

	/**
	 * 
	 * @param ctx
	 * @param home
	 * @param parentCode
	 * @return Collection<AdjustmentType>
	 * @throws HomeException
	 */
	private Collection<AdjustmentType> fetchFilteredAdjTypes(Context ctx, Home home, int parentCode)
			throws HomeException {
		Collection<AdjustmentType> adjustmentTypes = new ArrayList<AdjustmentType>();

		Collection<AdjustmentType> rootAdjTypes = (Collection<AdjustmentType>) HomeSupportHelper.get(ctx).getBeans(ctx,
				AdjustmentType.class, new EQ(AdjustmentTypeXInfo.PARENT_CODE, parentCode));
		if (rootAdjTypes != null && rootAdjTypes.size() > 0) {
			for (AdjustmentType adjustmentType : rootAdjTypes) {
				adjustmentTypes.add(adjustmentType);
				adjustmentTypes.addAll(fetchFilteredAdjTypes(ctx, home, adjustmentType.getCode()));
			}
		}

		return adjustmentTypes;
	}
}