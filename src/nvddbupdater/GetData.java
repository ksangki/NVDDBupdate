/**
 * @file	Get_data.java
 * @date	2018/10/29
 * @author	skt.1519040
 * @brief	This file get data from NVD(National Vulnerability Database)
 * @date
 * 	- 2018. 10. 29	Joh Rang Hyun
 */
package nvddbupdater;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;

/**
 * @class	Get_data
 * @brief	Connect to NVD data feed and download .xml.zip file
 * @warning	URL of NVD data feed should be checked first
 */
public class GetData {
	static ZipTagXml ztx = new ZipTagXml();
	static Logwriter logwriter = new Logwriter();
	
	public static void getData(String fpath) throws Exception {
		logwriter.writeConsole(" ");
		try (InputStream in = new URL("https://nvd.nist.gov/feeds/xml/cve/1.2/"+fpath).openStream()) {
			Files.copy(in, Paths.get("./" + ZipTagXml.original + "/"+fpath), StandardCopyOption.REPLACE_EXISTING);
			
			String zipfilepath = "./" + ZipTagXml.original + "/"+fpath;
			String dest = "./" + ZipTagXml.nvdcve;
			ztx.unzip(zipfilepath, dest);
			logwriter.writeConsole(" Success Getting "+ fpath+" from NVD");
		} catch (Exception e) {
			logwriter.writeConsole(" Get "+ fpath+" from NVD failed");
			throw e;
		}
	}
	
	
	public static void makeTranslatedFile(String fname) throws Exception {
		String filePath = "./"+ZipTagXml.nvdcve + "/" + fname + ".xml";
		try {
			File inputFile = new File(filePath);
			
			if(inputFile.isFile()) {
				// normalization
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(inputFile);
				doc.getDocumentElement().normalize();
			
		       	// make translated files
		       	MakeBase.makeBase(doc, "./"+ZipTagXml.translated + "/" + fname + "_base.xml");
		       	MakeRefs.makeRefs(doc, "./"+ZipTagXml.translated+"/"+fname + "_refs.xml");
		       	MakeVuln.makeVuln(doc, "./"+ZipTagXml.translated+"/"+fname+"_vuln.xml");
		       	logwriter.writeConsole(" Success Translating from "+fname+" to "+fname+"_base, "+fname+"_refs, "+fname+"_vuln");
			}
			else {
				logwriter.writeConsole(" There is no original file. ("+fname+")");
			}
		}
		catch (Exception e){
			logwriter.writeConsole(" Translating from "+fname+" to "+fname+"_base, "+fname+"_refs, "+fname+"_vuln failed");
			throw e;
		}
		
	}
}

