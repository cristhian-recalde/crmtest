package com.trilogy.app.crm.support;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.trilogy.framework.xhome.context.Context;

public class FileSupport 
{

	public static void persistObjectToXml(Context ctx, String xmlFile, Object ht) throws IOException
	{
		File newFile = new File(xmlFile);
		if (newFile.exists())
			newFile.delete();
		
		XMLEncoder e = new XMLEncoder(
			    new BufferedOutputStream(
			        new FileOutputStream(xmlFile, false)));
		e.writeObject(ht);
		e.close();
	}
	
	

	/**
	 * 
	 * @param ctx
	 * @param txtFile
	 * @return
	 */
	public static String readFileAsString(Context ctx, String txtFile) throws IOException
	{
		FileInputStream fis = new FileInputStream(txtFile);
		int x= fis.available();
		byte[] b= new byte[x];
		fis.read(b);
		return new String(b);
	}
	
	
	public static Object readObjectFromXml(String file) throws IOException
	{
		XMLDecoder xd = new XMLDecoder(new FileInputStream(file));
		return xd.readObject();		
	}
}
