/*
 * Copyright (c) 2007, REDKNEE.com. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of REDKNEE.com.
 * ("Confidential Information"). You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement you entered
 * into with REDKNEE.com.
 * 
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * REDKNEE.COM SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.trilogy.app.crm.api.generic.entity.adapter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.AbstractXInfo;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.beans.xi.XInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xenum.AbstractEnum;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.api.generic.entity.bean.GenericEntityApiConfiguration;
import com.trilogy.app.crm.api.generic.entity.support.GenericEntityApiConfigurationSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.types.genericentity.Entity;
import com.trilogy.app.crm.api.generic.entity.adapter.PropertyAdapter;


public abstract class AbstractGenericEntityAdapter implements GenericEntityAdapter
{

    @Override
    public AbstractBean getBean(Context ctx, Entity entity) throws Exception
    {
        String type = entity.getType();
        return (AbstractBean) XBeans.instantiate(type, ctx);
    }


    public Object adapt(Context ctx, Entity entity) throws EntityParsingException
    {
        AbstractBean bean = null;
        try
        {
            bean = getBean(ctx, entity);
        }
        catch (Exception e)
        {
            throw new EntityParsingException("Failed to create bean of type : " + entity.getType(), e);
        }
        return adapt(ctx, entity, bean);
    }

    public Object adaptQuery(Context ctx, Entity entity) throws EntityParsingException
    {
        AbstractBean bean = null;
        try
        {
            bean = getBean(ctx, entity);
        }
        catch (Exception e)
        {
            throw new EntityParsingException("Failed to create bean of type : " + entity.getType(), e);
        }
        return generateQuery(ctx, entity, bean);
    }
    

    public Object generateQuery(Context ctx, Entity entity, Object obj) throws EntityParsingException
    {
        AbstractBean bean = (AbstractBean) obj;
        And filter = new And();
        try
        {
            XInfo beankXInfo = XBeans.getInstanceOf(ctx, bean.getClass(), XInfo.class);
            
            for (GenericParameter param : entity.getProperty())
            {
                String strParamName = param.getName();
                PropertyInfo property = beankXInfo.getPropertyInfo(ctx, strParamName);
                if(property != null)
                {
                    PropertyAdapter propertyAdapter = getPropertyAdapter(ctx, property);
                    if (propertyAdapter == null)
                    {
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, this, "Property adapter not found for bean property : " + property.getSQLName());
                        }
                        continue;
                    }
                    propertyAdapter.adaptQuery(filter,property,param.getValue());
                }
                else
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "Not valid Property : " + param.getName());
                    }
                }
            }
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Print Generated Query: " + filter);
            }
        }
        catch (Exception e)
        {
            throw new EntityParsingException("Failed to parse entity parameters for bean : " + entity.getType(), e);
        }
        return filter;
    }
    public Object adapt(Context ctx, Entity entity, Object obj) throws EntityParsingException
    {
        AbstractBean bean = (AbstractBean) obj;
        try
        {
            XInfo beankXInfo = XBeans.getInstanceOf(ctx, bean.getClass(), XInfo.class);
            Collection<PropertyInfo> properties = beankXInfo.getProperties(ctx);
            for (PropertyInfo property : properties)
            {
                PropertyAdapter propertyAdapter = getPropertyAdapter(ctx, property); 
                if (propertyAdapter == null)
                {
                    // Do nothing
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "Property adapter not found for bean property : " + property.getSQLName());
                    }
                    continue;
                }
                propertyAdapter.adaptProperty(ctx, bean, entity.getProperty(), property);
            }
        }
        catch (Exception e)
        {
            throw new EntityParsingException("Failed to parse entity parameters for bean : " + entity.getType(), e);
        }
        return bean;
    }


    public PropertyAdapter getPropertyAdapter(Context ctx, PropertyInfo property)
    {
        PropertyAdapter propertyAdapter = null;
        if (String.class.isAssignableFrom(property.getType()))
        {
            propertyAdapter = new PremitiveClassAdapter<String>();
        }
        else if (Short.class.isAssignableFrom(property.getType()))
        {
            propertyAdapter = new PremitiveClassAdapter<Short>();
        }
        else if (Integer.class.isAssignableFrom(property.getType()))
        {
            propertyAdapter = new PremitiveClassAdapter<Integer>();
        }
        else if (Long.class.isAssignableFrom(property.getType()))
        {
            propertyAdapter = new PremitiveClassAdapter<Long>();
        }
        else if (Boolean.class.isAssignableFrom(property.getType()))
        {
            propertyAdapter = new PremitiveClassAdapter<Boolean>();
        }
        else if (GregorianCalendar.class.isAssignableFrom(property.getType()))
        {
            propertyAdapter = new PremitiveClassAdapter<GregorianCalendar>();
        }
        else if (Date.class.isAssignableFrom(property.getType()))
        {
            propertyAdapter = new PremitiveClassAdapter<Date>();
        }
        else if (AbstractEnum.class.isAssignableFrom(property.getType()))
        {
            propertyAdapter = new EnumClassAdapter();
        }
        else if (Float.class.isAssignableFrom(property.getType()))
        {
            propertyAdapter = new PremitiveClassAdapter<Float>();
        }
        else if (Double.class.isAssignableFrom(property.getType()))
        {
            propertyAdapter = new PremitiveClassAdapter<Double>();
        }
        else if (AbstractBean.class.isAssignableFrom(property.getType()))
        {
            propertyAdapter = new SimpleBeanAdapter();
        }
        return propertyAdapter;
    }


    protected AbstractXInfo getXInfo(Context ctx, Entity entity , Object obj) throws EntityParsingException{
        String typeXInfo = null;
        String type = null;
        try
        {
            if(obj.getClass().equals(Class.class))
            {
                type = ((Class)obj).getName();
            }
            else
            {
                type = obj.getClass().getName();
            }
            
            if(Object.class.equals(obj.getClass()))
            {
                throw new EntityParsingException("XInfo class not found " + typeXInfo);
            }
            entity.setType(type);
            typeXInfo = type + "XInfo";
            return (AbstractXInfo) XBeans.instantiate(typeXInfo, ctx);
        }
        catch (Exception e)
        {
            try
            {
                return getXInfo(ctx, entity, Class.forName(type).getSuperclass());
            }
            catch (ClassNotFoundException e1)
            {
                throw new EntityParsingException("XInfo class not found " + typeXInfo, e1);
            }
        }
    }
    public Entity unAdapt(Context ctx, Object obj) throws EntityParsingException
    {
        Entity entity = new Entity();
       
        AbstractXInfo xInfo = getXInfo(ctx, entity, obj);
        List<GenericParameter> paramList = new ArrayList<GenericParameter>();
        AbstractBean bean = (AbstractBean) obj;
        List<PropertyInfo> propertieslist = xInfo.getProperties(ctx);
        for (PropertyInfo property : propertieslist)
        {
            Object propertyValue = property.get(bean);
            if
            (   
                propertyValue instanceof java.util.Collection ||
                propertyValue instanceof AbstractBean 
            )
            {
                continue;
            }
            else if(AbstractEnum.class.isAssignableFrom(property.getType()))
            {
                paramList.add(createGenericParameter(property.getName(), ((AbstractEnum)property.get(bean)).getIndex()));
            }
            else
            {
                paramList.add(createGenericParameter(property.getName(), property.get(bean)));    
            }   
        }
        entity.setProperty(paramList.toArray(new GenericParameter[paramList.size()]));
        return entity;
    }


    protected GenericParameter createGenericParameter(String name, Object value)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(name);
        parameter.setValue(value);
        return parameter;
    }

    class PremitiveClassAdapter<T> implements PropertyAdapter
    {

        public void adaptQuery(And and, PropertyInfo info, Object value)
        {
            and.add(new EQ(info,  (T)value));
        }
        
        public void adaptProperty(Context ctx, Object bean, GenericParameter[] genericParameters, PropertyInfo property)
        {
            String paramName = property.getSQLName();
            for (GenericParameter param : genericParameters)
            {
                if (param.getName().equals(paramName))
                {
                    T value = (T) param.getValue();
                    if((value instanceof GregorianCalendar) && !Date.class.isAssignableFrom(property.getType())){
                        if(Long.class.isAssignableFrom(property.getType())){
                            long longDate = ((GregorianCalendar) value).getTimeInMillis();
                            property.set(bean, longDate);
                            break;
                        }
                    }else if(value instanceof GregorianCalendar){
                        value= (T) new Date(((GregorianCalendar) value).getTimeInMillis());    
                        property.set(bean, value);
                        break;
                    }
                    else
                    {
                        property.set(bean, value);
                        break;
                    }
                }
            }
        }
    }
    class EnumClassAdapter implements PropertyAdapter
    {

        public void adaptQuery(And and, PropertyInfo info, Object value)
        {
            and.add(new EQ(info, new Short(String.valueOf(value))));
        }
        
        public void adaptProperty(Context ctx, Object bean, GenericParameter[] genericParameters, PropertyInfo property)
        {
            try
            {
                Method method;
                method = property.getType().getMethod("get", new Class[]{Short.TYPE});
                Short value = null;
                String paramName = property.getSQLName();
                for (GenericParameter param : genericParameters)
                {
                    if (param.getName().equals(paramName))
                    {
                        value = (Short) param.getValue();
                        property.set(bean, value);
                        break;
                    }
                }
                if (value != null)
                {
                    method.invoke(null, new Object[]
                        {new Short(value)});
                }
            }
            catch (Exception e)
            {
            }
        }
    }
    class SimpleBeanAdapter implements PropertyAdapter
    {

        public void adaptQuery(And and, PropertyInfo info, Object value)
        {
        }
        
        public void adaptProperty(Context ctx, Object bean, GenericParameter[] genericParameters, PropertyInfo property)
        {
            Entity childEntity = null;
            String paramName = property.getSQLName();
            for (GenericParameter param : genericParameters)
            {
                if (param.getName().equals(paramName))
                {
                    childEntity = (Entity) param.getValue();
                    break;
                }
            }
            if (childEntity != null)
            {
                GenericEntityApiConfiguration config = null;
                try
                {
                    config = GenericEntityApiConfigurationSupport.getEntityConfiguration(ctx, childEntity);
                    if (config == null)
                    {
                        return;
                    }
                }
                catch (HomeException he)
                {
                    LogSupport.minor(ctx, this,
                            "HomeExcption encountred while trying to fetch entity configuration for entity :"
                                    + childEntity.getType(), he);
                    return;
                }
                // Step 2 : Adapt
                GenericEntityAdapter adapter = GenericEntityApiConfigurationSupport.getAdapter(ctx, config.getCreateOperation().getAdapter());
                if (adapter == null)
                {
                    return;
                }
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Using adapter : " + adapter);
                }
                Object childBean = null;
                try
                {
                    childBean = adapter.adapt(ctx, childEntity);
                    if (childBean == null)
                    {
                        return;
                    }
                }
                catch (EntityParsingException he)
                {
                    LogSupport.minor(ctx, this,
                            "EntityParsingException encountred while trying to adapt the entity to BSS bean. Entity Type :"
                                    + childEntity.getType(), he);
                    return;
                }
                property.set(bean, childBean);
            }
        }
    }
}