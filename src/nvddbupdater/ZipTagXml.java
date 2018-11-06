/**
 * @file	ZipTagXml.java
 * @date	2018/11/02
 * @author	skt.1519040
 * @brief	This file defines file path, DB server information and methods affect local storage
 * @date
 * 	- 2018. 11. 02	Joh Rang Hyun
 */
package nvddbupdater;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
 * @class	ZipTagXml
 * @brief	The fields of this class define file path and DB server information.
 * @warning	Check the file path and DB server information are right.
 */
public class ZipTagXml {
	
		/// file path (directory)
		static String original = "original";
		static String nvdcve = "nvdcve";
		static String translated = "translated";
		static String log = "log";
		
	 
		/// DB server information
		static String host = "113.217.254.196";  
		static String port = "3306";             
		static String dbId = "sktelecom";       
		static String dbPw = "sktelecom";       

		/**
		 * @brief	Encode document in UTF-8 and save it by .xml file
		 * @param	document
		 * 			type: Document
		 * 			Unencoded document
		 * @param	outXmlDest
		 * 			type: String
		 * 			File path of .xml file
		 * @throws	IOException
		 * @throws	TransformerException
		 */
		void transforming(Document document, String outXmlDest) throws IOException, TransformerException
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
	 		
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(out);
			
			TransformerFactory transFactory = TransformerFactory.newInstance();
			transFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			Transformer transformer = transFactory.newTransformer();
			
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
			
			String finalString = new String(out.toByteArray(), StandardCharsets.UTF_8);
			 
			File finalFile = new File(outXmlDest);
			FileWriter filewriter = new FileWriter(finalFile);
			try {
				filewriter.write(finalString);
			} catch (Exception e) {
				System.out.println(" Cannot write log");
			} finally {
				filewriter.close();
			}
			
			
		}
		
		/**
		 * @brief	Get value of attribute of nNode by tag_name, and set attribute on parent by (tag_name, value)
		 * @param	nNode
		 * 			type: Element
		 * 			Element of original file
		 * @param	parent
		 * 			type: Element
		 * 			Element of _base, _refs or _vuln file
		 * @param	tagName
		 * 			type: String
		 * 			The name of attribute on nNode
		 */
		void setTag (Element nNode, Element parent, String tagName) {
			String temp = nNode.getAttribute(tagName); // tag contents
			parent.setAttribute(tagName, temp);
		}
		
		/**
		 * @brief	Get text content of nNode, and set attribute on parent by (tag_name, text content)
		 * @param	nNode
		 * 			type: Element
		 * 			Element of original file
		 * @param	parent
		 * 			type: Element
		 * 			Element of _base file
		 * @param	tag_name
		 * 			type: String
		 * 			The name of attribute on parent
		 */
		void setTagfromText (Element nNode, Element parent, String tag_name) {
			String temp = nNode.getTextContent(); // tag contents
			parent.setAttribute(tag_name, temp);
		}
		
		/**
		 * @brief	Get and set all values of attributes that need to set on _base file
		 * @param	nNode
		 * 			type: Element
		 * 			Element of original file
		 * @param	parent
		 * 			type: Element
		 * 			Element of _base file
		 */
		void setTagAll (Element nNode, Element parent) {
			this.setTag(nNode, parent, "modified");
			this.setTag(nNode, parent, "published");
			this.setTag(nNode, parent, "seq");
			this.setTag(nNode, parent, "name");
			this.setTag(nNode, parent, "type");
			this.setTag(nNode, parent, "CVSS_vector");
			this.setTag(nNode, parent, "CVSS_exploit_subscore");
			this.setTag(nNode, parent, "CVSS_impact_subscore");
			this.setTag(nNode, parent, "CVSS_base_score");
			this.setTag(nNode, parent, "CVSS_version");
			this.setTag(nNode, parent, "severity");
		
		}
		
		/**
		 * @brief	Check the necessary directories exist
		 * @return	true: if all directories exist, false: if any one directory does not exist
		 */
		boolean dirExist() {
			File nvdcve = new File("./"+ZipTagXml.nvdcve);
			File original = new File("./"+ZipTagXml.original);
			File translated =new File("./"+ZipTagXml.translated);
			if(!nvdcve.exists()){
				return false;
			}
			else if (!original.exists()) {
				return false;
			}
			else if (!translated.exists()) {
				return false;
			}
			else {
				return true;
			}
		}
		
		/**
		 * @brief	Make all necessary directories
		 */
		void makeDir () {
			File nvdcve = new File("./"+ZipTagXml.nvdcve);
			File original = new File("./"+ZipTagXml.original);
			File translated =new File("./"+ZipTagXml.translated);
			File log =new File("./"+ZipTagXml.log);
			// check and create directories
			System.out.println(" ");
			if(!nvdcve.exists()){
				if(nvdcve.mkdir()) {
					System.out.println(" nvdcve directory is successfully created.");
				}
				else {
					System.out.println(" Fail to create nvdcve directory.");
					System.exit(1);
				}
			}
			else{
				System.out.println(" nvdcve directory is already created.");
			}
			if(!original.exists()){
				if(original.mkdir()) {
					System.out.println(" original directory is successfully created.");
				}
				else {
					System.out.println(" Fail to create original directory.");
					System.exit(1);
				}
			}
			else{
				System.out.println(" original directory is already created.");
			}
			if(!translated.exists()){
				if(translated.mkdir()) {
					System.out.println(" translated directory is successfully created.");
				}
				else {
					System.out.println(" Fail to create translated directory.");
					System.exit(1);
				}
			}
			else{
				System.out.println(" translated directory is already created.");
			}
			if(!log.exists()){
				if(log.mkdir()) {
					System.out.println(" log directory is successfully created.");
				}
				else {
					System.out.println(" Fail to create log directory.");
					System.exit(1);
				}
			}
			else{
				System.out.println(" log directory is already created.");
			}
		}
		
		/**
		 * @brief	Unzip .xml.zip file on zipFilePath and store .xml file on destDir
		 * @param	zipFilePath
		 * 			type: String
		 * 			The file path of .xml.zip file
		 * @param	destDir
		 * 			type: String
		 * 			The file path of .xml file
		 * @throws IOException 
		 */
		void unzip(String zipFilePath, String destDir) throws IOException {
	        File targetDir = new File(destDir);
	        // create output directory if it doesn't exist
	        if(!targetDir.exists()) {
	        	if(targetDir.mkdirs()) {
	        		System.out.println(" "+destDir + " is successfully created.");
	        	}
	        	else {
	        		System.out.println(" Fail to create " + destDir);
	        		System.exit(1);
	        	}
	        }
	        FileInputStream fileStream = null;
	        ZipInputStream zipFiles = null;
	        FileOutputStream outputStream = null;
	        //buffer for read and write data to file
	        byte[] buffer = new byte[1024];
	        try {
	            fileStream = new FileInputStream(zipFilePath);
	            try {
	            	zipFiles = new ZipInputStream(fileStream);
		            ZipEntry entries = zipFiles.getNextEntry();
		            while(entries != null){
		                String fileName = entries.getName();
		                File newFile = new File(destDir + File.separator + fileName);
		                 //create directories for sub directories in zip
		                new File(newFile.getParent()).mkdirs();
		                try {
			                outputStream = new FileOutputStream(newFile);
			                int len;
			                while ((len = zipFiles.read(buffer)) > 0) {
			                	outputStream.write(buffer, 0, len);
			                }
		                } catch (Exception e) {
		                	System.out.println(" FileOutputStream error");
		                } finally {
		                	outputStream.close();
		                }
		                
		                //close this ZipEntry
		                zipFiles.closeEntry();
		                entries = zipFiles.getNextEntry();
		            } 
		            zipFiles.closeEntry();
		            } catch (Exception e) {
		            	
		            } finally {
		            	
		        		zipFiles.close();
		            }
	            //close last ZipEntry
	        } catch (IOException e) {
	        	System.out.println(" "+zipFilePath + "unzip failed");
	        } finally {
	        	fileStream.close();
	        }
	        
	    }
}