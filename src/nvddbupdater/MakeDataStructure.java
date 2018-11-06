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
public class MakeDataStructure {
	static ZipTagXml ztx = new ZipTagXml();
	static Logwriter logwriter = new Logwriter();
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
	public static Document createDocument () throws ParserConfigurationException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			
			throw e1;
		}
		// document for base
		Document document = dBuilder.newDocument(); // new xml
       	
		return document;
	}
	
	public static Element createElement (Document document, Document doc) {
		Element root = document.createElement(doc.getDocumentElement().getNodeName()); // nvd
		document.appendChild(root);
		return root;
	}
	
	public static void makeBase (Document doc, String fpath) throws Exception {
		Document document = createDocument();
		Element root = createElement (document, doc);
		NodeList nList = doc.getElementsByTagName("entry");
		try {
			for (int temp = 0; temp < nList.getLength(); temp++) {	        	 
			Element eElement = (Element) nList.item(temp);
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
	
	public static void makeRefs  (Document doc, String fpath) throws Exception {
		
		Document document = createDocument(); // new xml
       	Element root = createElement(document,doc); // nvd
		NodeList nList = doc.getElementsByTagName("entry");
		try {
			//document for refs
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Element eElement = (Element) nList.item(temp);
				NodeList refList = eElement.getElementsByTagName("ref");

				for (int reftmp = 0; reftmp < refList.getLength(); reftmp++) {
					Element entry = document.createElement("entry"); 
					Node nRef = refList.item(reftmp);
					Element refElement = (Element) nRef;

					ztx.setTag(eElement, entry, "name");
					ztx.setTag(refElement, entry, "source");
					ztx.setTag(refElement, entry, "url");

					root.appendChild(entry);
				}
			}
			
			ztx.transforming(document, fpath);    
		}
		catch (Exception e) {
			logwriter.writeConsole(" Make_refs failed");
			
			throw e;
		}	
	}
	
	public static void makeVuln (Document doc, String fpath) throws Exception {
		
		Document document = createDocument(); // new xml
       	Element root = createElement(document, doc); // nvd
		NodeList nList = doc.getElementsByTagName("entry");
		try {
			//document for vuln
			for (int temp = 0; temp < nList.getLength(); temp++) { 
				Element eElement = (Element) nList.item(temp);
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
