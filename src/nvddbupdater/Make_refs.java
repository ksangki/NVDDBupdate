/**
 * @file	Make_refs.java
 * @date	2018/11/02
 * @author	skt.1519040
 * @brief	With xml file, make relational refs data file
 * @date
 * 	- 2018. 11. 02	Joh Rang Hyun
 */
package nvddbupdater;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 * @class	Make_refs
 * @brief	There is a method that make refs file
 * @warning	NULL
 */
public class Make_refs {
	static ZipTagXml ztx = new ZipTagXml();
	
	/**
	 * @brief	Parse xml file and get reference data and then encode in UTF-8
	 * @param	nList
	 * 			type: NodeList
	 * 			The list of nodes of original xml file 
	 * @param	document
	 * 			type: Document
	 * 			The document that reference data will be saved
	 * @param	root
	 * 			type: Element
	 * 			The root node of document
	 * @param	fpath
	 * 			type: String
	 * 			The file path of reference file
	 * @throws	Exception
	 */
	public static void make_refs_b  (Document doc, String fpath) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			
			throw e1;
		}
		Document document = dBuilder.newDocument(); // new xml
       	Element root = document.createElement(doc.getDocumentElement().getNodeName()); // nvd
		document.appendChild(root);
		NodeList nList = doc.getElementsByTagName("entry");
		try {
			//document for refs
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
				        	 
				Node nNode = nList.item(temp);
				Element eElement = (Element) nNode;
				NodeList refList = eElement.getElementsByTagName("ref");

				for (int reftmp = 0; reftmp < refList.getLength(); reftmp++) {
					Element entry = document.createElement("entry"); 
					Node nRef = refList.item(reftmp);
					Element refElement = (Element) nRef;

					ztx.set_tag(eElement, entry, "name");
					ztx.set_tag(refElement, entry, "source");
					ztx.set_tag(refElement, entry, "url");

					root.appendChild(entry);
				}
			}
			
			ztx.transforming(document, fpath);    
		}
		catch (Exception e) {
			System.out.println(" Make_refs failed");
			
			throw e;
		}	
	}
}