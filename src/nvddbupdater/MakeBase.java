/**
 * @file	Make_base.java
 * @date	2018/11/02
 * @author	skt.1519040
 * @brief	With xml file, make relational base data file
 * @date
 * 	- 2018. 11. 02	Joh Rang Hyun
 */
package nvddbupdater;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;

/**
 * @class	Make_base
 * @brief	There is a method that make base file
 * @warning	NULL
 */
public class MakeBase {
	static ZipTagXml ztx = new ZipTagXml();
	
	/**
	 * @brief	Parse xml file and get base data and then encode in UTF-8
	 * @param	nList
	 * 			type: NodeList
	 * 			The list of nodes of original xml file 
	 * @param	document
	 * 			type: Document
	 * 			The document that base data will be saved
	 * @param	root
	 * 			type: Element
	 * 			The root node of document
	 * @param	fpath
	 * 			type: String
	 * 			The file path of base file
	 * @throws	Exception
	 */
	public static void makeBase (Document doc, String fpath) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		Logwriter logwriter = new Logwriter();
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			
			throw e1;
		}
		// document for base
		Document document = dBuilder.newDocument(); // new xml
       	Element root = document.createElement(doc.getDocumentElement().getNodeName()); // nvd
		document.appendChild(root);
		NodeList nList = doc.getElementsByTagName("entry");
		try {
			 
			for (int temp = 0; temp < nList.getLength(); temp++) {
				        	 
			Node nNode = nList.item(temp);	
			Element eElement = (Element) nNode;
			Element entry = document.createElement("entry");
			            
			ztx.setTagAll(eElement, entry);
				            
			Element desc = (Element) eElement.getElementsByTagName("desc").item(0);
			ztx.setTagfromText(desc, entry, "desc");
			root.appendChild(entry);	
			}
			ztx.transforming(document, fpath); 	    
		}
		catch (Exception e) {
			logwriter.writeConsole(" Make_base failed");
			
			throw e;
		}	
	}
}
