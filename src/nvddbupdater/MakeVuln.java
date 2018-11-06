/**
 * @file	Make_vuln.java
 * @date	2018/11/02
 * @author	skt.1519040
 * @brief	With xml file, make relational vuln data file
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
 * @class	Make_vuln
 * @brief	There is a method that make vuln file
 * @warning	NULL
 */
public class MakeVuln {
	static ZipTagXml ztx = new ZipTagXml();
	
	/**
	 * @brief	Parse xml file and get vulnerability data and then encode in UTF-8
	 * @param	nList
	 * 			type: NodeList
	 * 			The list of nodes of original xml file 
	 * @param	document
	 * 			type: Document
	 * 			The document that vulnerability data will be saved
	 * @param	root
	 * 			type: Element
	 * 			The root node of document
	 * @param	fpath
	 * 			type: String
	 * 			The file path of vulnerability file
	 * @throws	Exception
	 */
	public static void makeVuln (Document doc, String fpath) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		Logwriter logwriter = new Logwriter();
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
			//document for vuln
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
				        	 
				Node nNode = nList.item(temp); 
				Element eElement = (Element) nNode;
				NodeList prodList = eElement.getElementsByTagName("prod");

				for (int prodtmp = 0; prodtmp < prodList.getLength(); prodtmp++) {
					
					Node nProd = prodList.item(prodtmp);
					Element prodElement = (Element) nProd;
					NodeList versList = prodElement.getElementsByTagName("vers");
					for (int verstmp = 0; verstmp < versList.getLength(); verstmp++) {
						Node nVers = versList.item(verstmp);
						Element versElement = (Element) nVers;

						Element entry = document.createElement("entry");

						ztx.setTag(eElement, entry, "name");
						String prodname = prodElement.getAttribute("name"); // tag contents
						entry.setAttribute("prodname", prodname);
						ztx.setTag(prodElement, entry, "vendor");
						ztx.setTag(versElement, entry, "num");
						ztx.setTag(versElement, entry, "edition");
						root.appendChild(entry); // create child
					}
				}
			}
			ztx.transforming(document, fpath); 	    
		}
		catch (Exception e) {
			logwriter.writeConsole(" Make_vuln failed");
			
			throw e;
		}	
	}
}