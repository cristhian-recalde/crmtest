package com.trilogy.app.crm.api.generic.entity.support;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.xerces.dom.ElementNSImpl;

import com.trilogy.esb.schema.genericrequestresponse.GenericParameterType;
import com.trilogy.esb.schema.genericrequestresponse.RequestType;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * <p>
 * this class provides method which makes a mapping of some configuration with the External System Data.
 * here it is included for BYOSD feature where we have to keep a mapping of SPID and Mvno in the BSS.
 * Sprint needs Mvno in request parameter which is mapped in BSS in an Xml file residing in etc folder.
 * ----------------
 * Note- This class is being used from some GRR Key, so please don't delete it if u don't find any reference in the project
 * </p>
 *
 * @author atul.mundra@redknee.com
 */

public class ExternalConfigurationMappingSupport {
	
	public static RequestType request = new RequestType();
	public static Map spidLevelConfigurationMap  = null;
	
	public static List<Object> getMappingForSpid(String spid, String mappingType, String returnParamName, boolean isUniqueValue, String fileName) {
		List value =new ArrayList();
		if(spidLevelConfigurationMap == null)
		{
			spidLevelConfigurationMap = new HashMap<String , HashMap<String, HashMap<String, List<String>>>>();
			request	=	unmarshalFromFile(RequestType.class, CoreSupport.getFile(ContextLocator.locate(), fileName));
			if(request != null)
			{
				List<GenericParameterType> parameter=request.getParameter();
				for (Iterator iterator = parameter.iterator(); iterator.hasNext();)
				{
					GenericParameterType genericParameterType = (GenericParameterType) iterator
							.next();
					String sp = ((org.apache.xerces.dom.ElementNSImpl)genericParameterType.getValue().get(0)).getFirstChild().getNodeValue();
					Map<String, Map<String, List<String>>> mappingTypeBasedDataMap= new HashMap<String, Map<String, List<String>>>();
					for (GenericParameterType genericParameterType2 : genericParameterType.getPart())
					{
						String mappingName	= genericParameterType2.getName();
						Map<String, List<String>> actualDataMap = new HashMap<String, List<String>>();
						for(GenericParameterType genericParameterType3 : genericParameterType2.getPart())
						{
							List<String> valueList = new ArrayList<String>();
							if(isUniqueValue)
							{
								valueList.add(((org.apache.xerces.dom.ElementNSImpl)genericParameterType3.getValue().get(0)).getFirstChild().getNodeValue());
								actualDataMap .put(genericParameterType3.getName(),valueList);
							}
							else
							{
								for (Object element : genericParameterType3.getValue()) 
								{
									ElementNSImpl elem = (ElementNSImpl)element;
									valueList.add(elem.getFirstChild().getNodeValue());
								}
								actualDataMap .put(genericParameterType3.getName(),valueList);
							}
						}
						mappingTypeBasedDataMap.put(mappingName, actualDataMap);
					}
					spidLevelConfigurationMap.put(sp, mappingTypeBasedDataMap);
				}
			}
		}
		if(spidLevelConfigurationMap .get(spid)!=null)
		{
			if(((HashMap<String, HashMap<String, List<Object>>>)spidLevelConfigurationMap .get(spid)).get(mappingType)	!=	null)
			{
				value	=	((HashMap<String,List<Object>>)((HashMap<String, HashMap<String, List<Object>>>)spidLevelConfigurationMap .get(spid)).get(mappingType)).get(returnParamName);
			}
		}
		return value;
		
	}
	
	 public static <T> T unmarshalFromFile(Class<T> klass, String fileName)
		{
			StreamSource source;
			JAXBElement<T> element	=	null;
			try {
				source = new StreamSource(new FileReader(fileName));
				JAXBContext context = JAXBContext.newInstance(klass.getPackage().getName());
				Unmarshaller um = context.createUnmarshaller();
				element = um.unmarshal(source, klass);
			} 
			catch (FileNotFoundException e)
			{
				 new MajorLogMsg(ExternalConfigurationMappingSupport.class, "File not find for External Configuration Mapping" + e.getMessage(), e).log(ContextLocator.locate());
			}
			catch (JAXBException e)
			{
				new MajorLogMsg(ExternalConfigurationMappingSupport.class, e.getMessage(), e).log(ContextLocator.locate());
			}
			catch(Exception e)
			{
				new MajorLogMsg(ExternalConfigurationMappingSupport.class, e.getMessage(), e).log(ContextLocator.locate());
			}
			return element!=null?element.getValue():null;
		}
	 
}