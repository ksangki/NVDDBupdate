/**
 * @file	MakeDataStructure.java
 * @date	2018/11/02
 * @author	skt.1519040
 * @brief	With xml file, make relational translated data file
 * @date
 * 	- 2018. 11. 02	Joh Rang Hyun
 * 	- 2018. 12. 14	Joh Rang Hyun	StAX version
 */
package nvddbupdater;
//StAX version
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @class	MakeDataStructure
 * @brief	There is a method that make translated file
 * @warning	NULL
 */
public class MakeDataStructure {
	static String[] baseAtt = {"name", "published", "modified", "seq", "type", "severity", "CVSS_base_score", "CVSS_exploit_subscore", "CVSS_impact_subscore", "CVSS_vector", "CVSS_version"};
	static String[] refsAtt = {"source", "url"};
	static String[] vulnAtt = {"num", "edition"};
	static String baseType = "_base";
	static String refsType = "_refs";
	static String vulnType = "_vuln";
	String andChar = "&amp;";		// &
	String quotChar = "&quot;";		// '
	String ltChar = "&lt;";			// <
	String gtChar = "&gt;";			// >
	
	/**
	 * @brief	get an event reader that read xml file
	 * @param	fPath
	 * 			type: String
	 * 			file path of xml file
	 * @return	XMLEventReader
	 */
	public XMLEventReader getEventReader(String fPath) {
		XMLInputFactory factory = XMLInputFactory.newInstance();
	    XMLEventReader eventReader;
		try {
			eventReader = factory.createXMLEventReader(new FileReader(fPath));
			return eventReader;
		} catch (FileNotFoundException|XMLStreamException e) {
			return null;
		}
	}
	
	/**
	 * @brief	save temporary String buffer as XML file 
	 * @param	tempString
	 * 			type: StringBuilder
	 * 			temporary String
	 * @param	filePath
	 * 			type: String
	 * 			file path of XML file
	 * @param	appending
	 * 			type: boolean
	 * 			if true, append temporary string behind the file. if false, create new file 
	 * @return	true if success
	 */
	public boolean saveDocumentAsXML (StringBuilder tempString, String filePath, boolean appending) {
		try (PrintWriter fw = new PrintWriter(new FileWriter(filePath,appending))){
			fw.println(tempString.toString());
			return true;
		} catch (Exception e) {
			return false;
			
		} 
				
	}
	
	/**
	 * @brief	parse cve name and save base data into cveMap (all .xml file)
	 * @param	startElement
	 * 			type: StartElement
	 * 			indicate start of element created by XMLEventReader
	 * @param	cveMap
	 * 			type: Map<String, String>
	 * 			save data
	 */
	@SuppressWarnings("unchecked")
	public void parseNameCVE(StartElement startElement, Map<String, String> cveMap) {
		Iterator<Attribute> attributes = startElement.getAttributes();
        while (attributes.hasNext()){
            Attribute baseE = attributes.next();
            cveMap.put(baseE.getName().toString(), baseE.getValue());
        }
	}
	
	/**
	 * @brief	parse cve name and save refs data into cveMap (_refs.xml file) and create temporary string buffer
	 * @param	startElement
	 * 			type: StartElement
	 * 			indicate start of element created by XMLEventReader
	 * @param	cveMap
	 * 			type: Map<String, String>
	 * 			save data
	 * @param	tempString
	 * 			type: StringBuilder
	 * 			temporary String buffer of _refs.xml file
	 * @param	lineSeparator
	 * 			type: String
	 * 			\n or \r\n (depend on OS)
	 * @param	cveAtt
	 * 			type: String[]
	 * 			attribute names of _refs.xml file
	 */
	@SuppressWarnings("unchecked")
	public void parseRefs(StartElement startElement, Map<String, String> cveMap, StringBuilder tempString, String lineSeparator, String[] cveAtt) {
		Iterator<Attribute> attributes = startElement.getAttributes();
  	  
  	  	tempString.append("<entry name=\""+cveMap.get("name")+"\"");
	  	  List<String> refsList = new ArrayList<>();
	  	  Collections.addAll(refsList, cveAtt);
	  	  while(attributes.hasNext()) {
	  		  Attribute refsE = attributes.next();
	  		  if (refsList.contains(refsE.getName().toString())) {
	  			  
	  			  tempString.append(" "+refsE.getName().toString()+"=\""+refsE.getValue().replaceAll("&", andChar)+"\" ");
	  			  refsList.remove(refsE.getName().toString());
	  		  }
	  	  }
	  	  for (int i=0; i < refsList.size(); i++) {
	  		  
	  		  tempString.append(" "+refsList.get(i)+"=\"\"");
	  	  }
	  	  tempString.append("/>"+lineSeparator);
	  	  
	  	  
	}
	
	/**
	 * @brief	parse cve name and save prodname data into cveMap (_vuln.xml file)
	 * @param	startElement
	 * 			type: StartElement
	 * 			indicate start of element created by XMLEventReader
	 * @param	cveMap
	 * 			type: Map<String, String>
	 * 			save data
	 */
	@SuppressWarnings("unchecked")
	public void parseProd(StartElement startElement, Map<String, String> cveMap) {
		 Iterator<Attribute> attributes = startElement.getAttributes();
	  	   while(attributes.hasNext()) {
	  		   Attribute vulnE = attributes.next();
	  		   if (vulnE.getName().toString() == "name") {
	  			   cveMap.put("prodname", vulnE.getValue());
	  		   }
	  		   else {
	  			   cveMap.put(vulnE.getName().toString(), vulnE.getValue());
	  		   }
	  	   }
	}
	
	/**
	 * @brief	parse cve name and save vers data into cveMap (_vuln.xml file) and create temporary string buffer
	 * @param	startElement
	 * 			type: StartElement
	 * 			indicate start of element created by XMLEventReader
	 * @param	cveMap
	 * 			type: Map<String, String>
	 * 			save data
	 * @param	tempString
	 * 			type: StringBuilder
	 * 			temporary String buffer of _vuln.xml file
	 * @param	lineSeparator
	 * 			type: String
	 * 			\n or \r\n (depend on OS)
	 * @param	cveAtt
	 * 			type: String[]
	 * 			attribute names of _vuln.xml file
	 */
	@SuppressWarnings("unchecked")
	public void parseVers(StartElement startElement, Map<String, String> cveMap, StringBuilder tempString, String lineSeparator, String[] cveAtt) {
		Iterator<Attribute> attributes = startElement.getAttributes();
 	   
 	   
		   tempString.append("<entry name=\""+cveMap.get("name")+"\" prodname=\""+cveMap.get("prodname").replaceAll("&", andChar)+"\" vendor=\""+cveMap.get("vendor")+"\"");
 	   List<String> vulnList = new ArrayList<>();
 	   Collections.addAll(vulnList, cveAtt);
 	   while(attributes.hasNext()) {
 		   Attribute vulnD = attributes.next();
 		   if (vulnList.contains(vulnD.getName().toString())) {
 			   
 			   tempString.append(" "+vulnD.getName().toString()+"=\""+vulnD.getValue()+"\"");
 			   vulnList.remove(vulnD.getName().toString());
 		   }
 	   }
 	   for (int i=0; i < vulnList.size(); i++) {
     		  
 		   tempString.append(" "+vulnList.get(i)+"=\"\"");
     	  }
 	   
 	   tempString.append("/>"+lineSeparator);
 	   
 	   
    }
	
	
	/**
	 * @brief	parse cve name and save desc data into cveMap (_base.xml file) and create temporary string buffer
	 * @param	cveMap
	 * 			type: Map<String, String>
	 * 			save data
	 * @param	tempString
	 * 			type: StringBuilder
	 * 			temporary String buffer of _base.xml file
	 * @param	descript
	 * 			type: StringBuilder
	 * 			temporary String buffer of `desc` value of _base.xml file
	 * @param	lineSeparator
	 * 			type: String
	 * 			\n or \r\n (depend on OS)
	 * @param	cveAtt
	 * 			type: String[]
	 * 			attribute names of _base.xml file
	 */
	public void parseBase(Map<String, String> cveMap, StringBuilder tempString, StringBuilder descript, String lineSeparator, String[] cveAtt) {
		if(cveMap.get("reject") == null)
	 	   {
	 		 	
	 		   tempString.append("<entry ");
	 		   for (int i = 0; i < cveAtt.length; i++) {
	 			   tempString.append(cveAtt[i]+"=\""+cveMap.get(cveAtt[i])+"\" ");
	 		   }
	 		   tempString.append("desc=\""+descript.toString().replaceAll("&",andChar).replaceAll("\"",quotChar).replaceAll("<", ltChar).replaceAll(">", gtChar)+"\"/>"+lineSeparator);
		           
	 	   }
	 	  
	       
	}
	
	/**
	 * @brief	main translation process 
	 * @param	fileName
	 * 			type: String
	 * 			file name of .xml file
	 * @param	cveAtt
	 * 			type: String[]
	 * 			name of attributes that translated xml file should contain  
	 * @param	tableType
	 * 			type: String
	 * 			_base, _refs, vuln
	 * @return	true if success
	 */
	public boolean saveDocument (String fileName, String[] cveAtt, String tableType) {
		boolean desc = false;
		Map<String, String> cveMap = new HashMap<>();
		int count = 0;
		StringBuilder tempString = new StringBuilder();
		StringBuilder descript = new StringBuilder();
		String lineSeparator = System.getProperty("line.separator");
		boolean appending = false;
		Logwriter.writeConsole(" saveDocument="+fileName+"|");

		try {
		     XMLEventReader eventReader = getEventReader("./"+ZipTagXml.nvdcve + "/" + fileName + ".xml");
		     System.out.println("path="+"./"+ZipTagXml.nvdcve + "/" + fileName + ".xml");
		     tempString.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"+lineSeparator);
		     tempString.append("<nvd>"+lineSeparator);
		     
		     while(eventReader.hasNext()) {
			        XMLEvent event = eventReader.nextEvent();
			           
			        switch(event.getEventType()) {
			           
			           case XMLStreamConstants.START_ELEMENT:
			              StartElement startElement = event.asStartElement();
			              String qName = startElement.getName().getLocalPart();
			              
				           if (qName.equalsIgnoreCase("entry")) {
				        	   parseNameCVE(startElement, cveMap);
				           } else if (qName.equalsIgnoreCase("ref") && tableType.equals(refsType)) {
				        	   parseRefs(startElement, cveMap, tempString, lineSeparator, cveAtt);
				        	   count = count + 1;
				           } else if (qName.equalsIgnoreCase("prod") && tableType.equals(vulnType)) {
				        	   parseProd(startElement, cveMap);
				           } else if (qName.equalsIgnoreCase("vers") && tableType.equals(vulnType)) {
				        	   parseVers(startElement, cveMap, tempString, lineSeparator, cveAtt);
				        	   count = count + 1;
				           } else if (qName.equalsIgnoreCase("descript")) {
					              desc = true;
					       } 
			           
			           break;
			
			           case XMLStreamConstants.CHARACTERS:
			        	   if(desc && tableType.equals(baseType)) {
					        	  descript.append(event.asCharacters());
					              
					           }
			           break;
			
			           case XMLStreamConstants.END_ELEMENT:
			              EndElement endElement = event.asEndElement();
			              
			           if(endElement.getName().getLocalPart().equalsIgnoreCase("entry")) {
			        	   if(tableType.equals(baseType)) {
			        		   parseBase(cveMap, tempString, descript, lineSeparator, cveAtt);
			        		   descript = new StringBuilder();
			        		   count = count + 1;
			        	   }
			               cveMap.clear();
			              
			           } else if (endElement.getName().getLocalPart().equalsIgnoreCase("descript")) {
			        	   desc = false;
			           }
			           
			           break;
			           
			           default:
			        	   
			           break;
			        } 
			        if (count >= 1000) {
			        	if(!saveDocumentAsXML (tempString, "./"+ZipTagXml.translated + File.separator + fileName + tableType + ".xml",appending)) {
				     		return false;
				     	}
			        	count = 0;
			        	
			        	tempString = new StringBuilder();
					    appending = true;
			        }
			        
			     }
		     	tempString.append("</nvd>");
		     	if(!saveDocumentAsXML (tempString, "./"+ZipTagXml.translated + File.separator + fileName + tableType + ".xml",appending)) {
		     		return false;
		     	}
		     	
		} catch (XMLStreamException e) {
			System.out.println(e);
			return false;
		}
		return true;
	}
}
