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
public class Get_data {
	static ZipTagXml ztx = new ZipTagXml();
	
	public static void get_data(String fpath) throws Exception {
		System.out.println(" ");
		try (InputStream in = new URL("https://nvd.nist.gov/feeds/xml/cve/1.2/"+fpath).openStream()) {
			Files.copy(in, Paths.get("./" + ZipTagXml.original + "/"+fpath), StandardCopyOption.REPLACE_EXISTING);
			in.close();
			String zipfilepath = "./" + ZipTagXml.original + "/"+fpath;
			String dest = "./" + ZipTagXml.nvdcve;
			ztx.unzip(zipfilepath, dest);
			System.out.println(" Success Getting "+ fpath+" from NVD");
		} catch (Exception e) {
			System.out.println(" Get "+ fpath+" from NVD failed");
			throw e;
		}
	}
	
	
	public static void make_translated_file(String fname) throws Exception {
		String file_path = "./"+ZipTagXml.nvdcve + "/" + fname + ".xml";
		try {
			File inputFile = new File(file_path);
			
			if(inputFile.isFile()) {
				// normalization
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(inputFile);
				doc.getDocumentElement().normalize();
			
		       	// make translated files
		       	Make_base.make_base_b(doc, "./"+ZipTagXml.translated + "/" + fname + "_base.xml");
		       	Make_refs.make_refs_b(doc, "./"+ZipTagXml.translated+"/"+fname + "_refs.xml");
		       	Make_vuln.make_vuln_b(doc, "./"+ZipTagXml.translated+"/"+fname+"_vuln.xml");
		       	System.out.println(" Success Translating from "+fname+" to "+fname+"_base, "+fname+"_refs, "+fname+"_vuln");
			}
			else {
				System.out.println(" There is no original file. ("+fname+")");
			}
		}
		catch (Exception e){
			System.out.println(" Translating from "+fname+" to "+fname+"_base, "+fname+"_refs, "+fname+"_vuln failed");
			throw e;
		}
		
	}
}

