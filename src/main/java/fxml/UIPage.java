package fxml;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Logger;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Pair;

public enum UIPage {

	INSTANCE();
	private static final Logger LOGGER = Logger.getLogger( UIPage.class.getName() );

	public static final String fxmlSuffix = ".fxml";
	public static final String fxmlPackage = "/fxml/";
	public static final String cssSuffix = ".css";
	public static final String cssPackage = "/css/";

	public static final String defaultStyleSheet = getCSS("default").toExternalForm();

	public enum Page {
		START("Start"), VIEW("View"), CHART("Charts");

		public final String name;
		public final URL url;
		public final URL css;

		private Page(String n) {
			name = fxmlPackage + n + fxmlSuffix;
			url = UIPage.class.getResource(name);
			css = UIPage.class.getResource(cssPackage + n + cssSuffix);
		}
	}

	private final HashMap<String, Pair<Parent, URL>> pages;

	UIPage() {
		pages = new HashMap<String, Pair<Parent, URL>>();

		for (UIPage.Page p : UIPage.Page.values()) {
			pages.put(p.name, new Pair<Parent, URL>(null, p.url));
		}
	}

	public Parent getParent(final UIPage.Page s) {
		Pair<Parent, URL> pair = pages.get(s.name);
		if (pair != null && pair.getKey() != null) {
			return pages.get(s.name).getKey();
		} else {
			try {
				pages.put(s.name, new Pair<Parent, URL>(FXMLLoader.load(s.url), s.url));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return pages.get(s.name).getKey();
	}

	public static URL getCSS(final String name) {
		return getUrl(cssPackage + name + cssSuffix);
	}

	public static URL getUrl(final String url) {
		return UIPage.class.getResource(url);
	}
}
