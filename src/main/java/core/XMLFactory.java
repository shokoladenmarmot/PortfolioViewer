package core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import Start.Main;

public class XMLFactory {
	private static final Logger LOGGER = Logger.getLogger(XMLFactory.class.getName());
	public static final float version = 1.0f;

	public static void loadOrderListFromXML() {

		File file = Main.getInstance().openFile();
		if (file != null) {
			LinkedList<Order> list = new LinkedList<Order>();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(file);
				doc.getDocumentElement().normalize();

				// Top node = "viewer"
				Element top = doc.getDocumentElement();
				if (top.getNodeName().equalsIgnoreCase("viewer")) {

					// TODO Import configurations
					// NodeList configs = top.getElementsByTagName("configs");
					NodeList parsedVersion = top.getElementsByTagName("version");
					for (int i = 0; i < parsedVersion.getLength(); i++) {
						Node versionNode = parsedVersion.item(i);
						if (versionNode.getNodeType() == Node.ELEMENT_NODE) {
							float ver = Float.parseFloat(((Element) versionNode).getTextContent());
							if (version < ver) {
								LOGGER.warning("Loading FAILED.\nIncompatable XML version: " + ver);
								return;
							}
						}
					}

					NodeList orders = top.getElementsByTagName("orders");

					for (int i = 0; i < orders.getLength(); i++) {

						Node orderList = orders.item(i);
						for (int j = 0; j < orderList.getChildNodes().getLength(); j++) {
							// An actual Order
							Node order = orderList.getChildNodes().item(j);

							if (order.getNodeType() == Node.ELEMENT_NODE) {

								Element asElement = (Element) order;
								try {
									String symbol = asElement.getElementsByTagName("symbol").item(0).getTextContent();
									String market = asElement.getElementsByTagName("market").item(0).getTextContent();
									String from = asElement.getElementsByTagName("from").item(0).getTextContent();
									String to = asElement.getElementsByTagName("to").item(0).getTextContent();
									Double amountspend = Double.parseDouble(
											asElement.getElementsByTagName("amountspend").item(0).getTextContent());
									Double amountrecieved = Double.parseDouble(
											asElement.getElementsByTagName("amountrecieved").item(0).getTextContent());
									long date = Long
											.parseLong(asElement.getElementsByTagName("date").item(0).getTextContent());

									Order newOrder = new Order(symbol, market, from, to, amountspend, amountrecieved,
											date);
									list.add(newOrder);
								} catch (Exception e) {
									LOGGER.warning(e.getMessage());
									e.printStackTrace();
								}
							}
						}
					}
				}

			} catch (SAXException | IOException | ParserConfigurationException e) {
				LOGGER.warning(e.getMessage());
				e.printStackTrace();
			}
			TradeLibrary.getInstance().addOrders(list);
		}
	}

	public static void saveLibraryToXML() {

		File file = Main.getInstance().saveToFile();
		if (file != null) {
			List<Order> list = TradeLibrary.getInstance().getOrders();
			StringBuilder sb = new StringBuilder();
			sb.append("<viewer>\n");
			sb.append("<version>");
			sb.append(version);
			sb.append("</version>\n");
			sb.append("<configs>\n");
			sb.append("</configs>\n");
			sb.append("<orders>\n");
			for (Order o : list) {
				sb.append(o.toXML());
			}
			sb.append("</orders>\n");
			sb.append("</viewer>");
			BufferedWriter output = null;
			try {
				output = new BufferedWriter(new FileWriter(file));
				output.write(sb.toString());
				output.close();
			} catch (IOException e) {
				LOGGER.info(e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
