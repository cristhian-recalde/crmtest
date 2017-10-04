/**
 * 
 */
package com.trilogy.app.crm.adjustmenttype;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.trilogy.app.crm.bean.AdjustmentTypeLimitProperty;
import com.trilogy.app.crm.bean.AdjustmentTypeXInfo;
import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.app.crm.bean.CRMGroupHome;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.usergroup.AdjustmentTypeLimitUserGroupExtension;
import com.trilogy.app.crm.home.AdjustmentTypePermissionSettingHome;
import com.trilogy.app.crm.home.pipelineFactory.AdjustmentTypeHomePipelineFactory;
import com.trilogy.app.crm.support.AuthSupport;
import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author cindy.wong@redknee.com
 * @since 8.4
 */
public class AdjustmentTypeEnhancedGUI extends AbstractAdjustmentTypeEnhancedGUI 
{

	public List getAdjustmentTypes()
	{
		if (adjustmentTypes_ != null)
		{
			return adjustmentTypes_;
		}
		
		Home groupHome = (Home) getContext().get(CRMGroupHome.class);
        CRMGroup group;
        
        try
        {
        	group = (CRMGroup) groupHome.find(getContext(), getUserGroup());
        }
        catch (HomeException exception)
        {
        	LogSupport.minor(getContext(), this, 
        		"Fail to load adjustment types for user group " + getUserGroup(), 
        		exception);
        	return null;
        }

        // get limit extension
        Map limits = null;
        Collection<Extension> extensions = group.getExtensions();
        for (Extension e : extensions)
        {
            if (e instanceof AdjustmentTypeLimitUserGroupExtension)
            {
                limits = ((AdjustmentTypeLimitUserGroupExtension) e)
                        .getLimits();
            }
        }

        if (limits == null)
        {
            limits = new HashMap<Integer, AdjustmentTypeLimitProperty>();
        }

        boolean rootLimitSet = true;
        if (!limits.containsKey(new Integer(0)))
        {
            AdjustmentTypeLimitProperty property = new AdjustmentTypeLimitProperty();
            property.setAdjustmentType(0);
            property.setLimit(0);
            limits.put(new Integer(0), property);
            rootLimitSet = false;
        }

        // load adjustment types
        Home adjustmentTypeHome = (Home) getContext()
                .get(AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_READ_ONLY_HOME);
        Integer current = new Integer(0);

        boolean rootAllowed = AuthSupport
                .checkPermission(
                        (Home) getContext().get(CRMGroupHome.class),
                        group.getName(),
                        new SimplePermission(
                                AdjustmentTypePermissionSettingHome.ADJUSTMENT_TYPE_PERMISSION_ROOT + ".*"));

        AdjustmentTypeEnhancedGUIBuildingVisitor visitor = new AdjustmentTypeEnhancedGUIBuildingVisitor(
                group, limits, rootAllowed);
        try
        {
	        do
	        {
	            visitor = (AdjustmentTypeEnhancedGUIBuildingVisitor) adjustmentTypeHome
	                    .forEach(getContext(), visitor, new EQ(
	                            AdjustmentTypeXInfo.PARENT_CODE, current));
	            current = visitor.dequeue();
	        }
	        while (current != null);
        }
        catch (HomeException exception)
        {
        	LogSupport.minor(getContext(), this, 
        		"Cannot load adjustment types for user group " + getUserGroup(), 
        		exception);
        	return null;
        }
        Map<Integer, SortedSet<AdjustmentTypeEnhancedGUIProperty>> properties = visitor
                .getProperties();
        properties = addRoot(properties, rootAllowed,
                (AdjustmentTypeLimitProperty) (rootLimitSet ? limits
                        .get(new Integer(0)) : null));
        List<AdjustmentTypeEnhancedGUIProperty> orderedProperties = new LinkedList<AdjustmentTypeEnhancedGUIProperty>();
        Integer currentParent = new Integer(-1);
        orderedProperties = addAllChildren(properties, currentParent,
                orderedProperties);
        adjustmentTypes_ = orderedProperties;
        return adjustmentTypes_;
	}

    /**
     * Adds root node to the collection.
     * 
     * @param properties
     *            Collection of properties to be displayed.
     * @param rootAllowed
     *            Whether root's permission is ALLOWED.
     * @param limit
     *            Root's adjustment type limit, if set. Use null if it's not
     *            set.
     * @return Updated collection of properties to be displayed.
     */
    protected Map<Integer, SortedSet<AdjustmentTypeEnhancedGUIProperty>> addRoot(
            Map<Integer, SortedSet<AdjustmentTypeEnhancedGUIProperty>> properties,
            boolean rootAllowed, AdjustmentTypeLimitProperty limit)
    {
        AdjustmentTypeEnhancedGUIProperty p = createRoot(rootAllowed);
        if (limit != null)
        {
            p.setLimit(limit.getLimit());
            p.setLimitSet(AdjustmentTypeEnhancedGUILimitEnum.CUSTOM_INDEX);
        }
        SortedSet<AdjustmentTypeEnhancedGUIProperty> set = new TreeSet<AdjustmentTypeEnhancedGUIProperty>();
        set.add(p);
        properties.put(new Integer(-1), set);
        return properties;
    }

    /**
     * Adds all children of an adjustment type to the collection of properties
     * to be displayed.
     * 
     * @param properties
     *            Current collection of properties.
     * @param currentParent
     *            Parent of all adjustment types to be added.
     * @param orderedProperties
     *            Current list of properties, ordered by name.
     * @return Updated list of properties, ordered by name.
     */
    protected List<AdjustmentTypeEnhancedGUIProperty> addAllChildren(
            Map<Integer, SortedSet<AdjustmentTypeEnhancedGUIProperty>> properties,
            int currentParent,
            List<AdjustmentTypeEnhancedGUIProperty> orderedProperties)
    {
        Integer key = new Integer(currentParent);
        if (properties.containsKey(key))
        {
            for (AdjustmentTypeEnhancedGUIProperty p : properties.get(key))
            {
                orderedProperties.add(p);
                orderedProperties = addAllChildren(properties, p.getCode(),
                        orderedProperties);
            }
        }
        return orderedProperties;
    }

    /**
     * Creates tree root node.
     * 
     * @param rootPermitted
     *            Whether root's permission is ALLOWED.
     * @return Created tree root node.
     */
    public static AdjustmentTypeEnhancedGUIProperty createRoot(
            boolean rootPermitted)
    {
        AdjustmentTypeEnhancedGUIProperty p = new AdjustmentTypeEnhancedGUIProperty();
        p.setCode(0);
        p.setLevel(0);
        p.setLimitSet(AdjustmentTypeEnhancedGUILimitEnum.INHERIT_INDEX);
        p.setName("[root]");
        p.setParentCode(-1);
        p.setCategory(true);
        p
                .setPermission(rootPermitted ? AdjustmentTypeEnhancedGUIPermissionEnum.ALLOWED_INDEX
                        : AdjustmentTypeEnhancedGUIPermissionEnum.DENIED_INDEX);
        p.setPermissionEditable(true);
        return p;
    }

}
