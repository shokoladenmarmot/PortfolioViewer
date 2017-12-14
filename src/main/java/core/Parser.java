package core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import Start.Main;

public class Parser {
	private static final Logger LOGGER = Logger.getLogger(Parser.class.getName());

	public static List<Order> loadXMLOrderList() {

//		File file = Main.getInstance().openFile();
//
//		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//		try {
//			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//			Document doc = dBuilder.parse(file);
//			doc.getDocumentElement().normalize();
//			
//			doc.getDocumentElement().getNodeName()
//
//		} catch (SAXException | IOException | ParserConfigurationException e) {
//			LOGGER.warning(e.getMessage());
//			e.printStackTrace();
//		}

		return null;
	}
}
