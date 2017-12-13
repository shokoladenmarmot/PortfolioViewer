package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.json.JSONPointerException;

public class JSONFactory {
	
	private static final Logger LOGGER = Logger.getLogger( JSONFactory.class.getName() );

	public static JSONObject getJSONObject(String url) {

		/* synchronized (JSONFactory.class) */ {
			InputStream input;
			try {

				URLConnection conn = new URL(url).openConnection();
				conn.setConnectTimeout(60000);
				input = conn.getInputStream();
				
				// input = new URL(url).openStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(input, Charset.forName("UTF-8")), 8);
				try {

					// Build up the JSON as string and create JSONObject
					StringBuilder sb = new StringBuilder();
					String line = null;

					while ((line = reader.readLine()) != null) {
						sb.append(line + "\n");
					}

					return new JSONObject(sb.toString());

				} catch (JSONPointerException | IOException | IllegalArgumentException e) {
					e.printStackTrace();
				} finally {
					reader.close();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
