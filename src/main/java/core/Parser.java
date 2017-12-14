package core;

import java.io.File;
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
import core.Order.OrderType;

public class Parser {
	private static final Logger LOGGER = Logger.getLogger(Parser.class.getName());

	public static List<Order> loadXMLOrderList() {
		LinkedList<Order> list = new LinkedList<Order>();

		File file = Main.getInstance().openFile();
		if (file != null) {
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
					// NodeList version = top.getElementsByTagName("version");
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
									Double amount = Double.parseDouble(
											asElement.getElementsByTagName("amount").item(0).getTextContent());
									OrderType type = OrderType
											.get(asElement.getElementsByTagName("type").item(0).getTextContent());
									long date = Long
											.parseLong(asElement.getElementsByTagName("date").item(0).getTextContent());
									Order newOrder = new Order(symbol, amount, type, date);
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
		}
		return list;
	}
}
