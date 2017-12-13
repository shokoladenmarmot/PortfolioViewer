package core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

public class JSONFactory {

	public static JSONObject getJSONObject(String url) {

		InputStream input;
		try {
			input = new URL(url).openStream();

			BufferedReader reader = new BufferedReader(new InputStreamReader(input, Charset.forName("UTF-8")), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			return new JSONObject(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
