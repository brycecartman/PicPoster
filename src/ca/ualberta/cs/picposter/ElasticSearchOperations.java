package ca.ualberta.cs.picposter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.renderscript.Type;
import android.util.Log;
import ca.ualberta.cs.picposter.model.PicPostModel;
import ca.ualberta.cs.picposter.model.PicPosterModelList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ElasticSearchOperations {
	
	public static void pushPicPostModel(final PicPostModel model){
		Thread thread = new Thread(){
		
			@Override
			public void run() {
				Gson gson = new Gson();
				HttpClient client = new DefaultHttpClient();
				HttpPost request = new HttpPost("http://cmput301.softwareprocess.es:8080/testing/cartman/");
				
				try {
					String jsonString = gson.toJson(model);	
					request.setEntity(new StringEntity(jsonString));
					
					HttpResponse response = client.execute(request);
					Log.w("ElasticSearch", response.getStatusLine().toString());
					
					response.getStatusLine().toString();
					HttpEntity entity = response.getEntity();
					
					BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
					String output = reader.readLine();
					while(output != null){
						Log.w("ElasticSearch", output);
						output = reader.readLine();
					}
					
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
		};
		
		thread.start();
	}
	
//not ui thread
	public static void searchPicPostModel(final String searchTerm, final PicPosterModelList model) throws ClientProtocolException, IOException{
		Runnable run = new Runnable() {

			@Override
			public void run() {

				HttpGet searchRequest = null;
				try {
					searchRequest = new HttpGet("http://cmput301.softwareprocess.es:8080/testing/cartman/_search?q=" +
							java.net.URLEncoder.encode(searchTerm,"UTF-8"));
				} catch (UnsupportedEncodingException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

				PicPostModel modelGood = null;

				HttpClient client = new DefaultHttpClient();
				Gson gson = new Gson();

				searchRequest.setHeader("Accept","application/json");	
				HttpResponse response = null;
				try {
					response = client.execute(searchRequest);
				} catch (ClientProtocolException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				String status = response.getStatusLine().toString();

				System.out.println(status);

				String json = null;
				try {
					json = getEntityContent(response);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Type elasticSearchSearchResponseType = (Type) new TypeToken<ElasticSearchSearchResponse<PicPostModel>>(){}.getType();
				ElasticSearchSearchResponse<PicPostModel> esResponse = gson.fromJson(json, (java.lang.reflect.Type) elasticSearchSearchResponseType);

				for (ElasticSearchResponse<PicPostModel> r : esResponse.getHits()) {
					modelGood = r.getSource();
					model.appendAdd(modelGood);
				}
			
			};
			//model fields set with constructor
		};

	}	
	
	/**
	 * get the http response and return json string
	 */
	static String getEntityContent(HttpResponse response) throws IOException {
		BufferedReader br = new BufferedReader(
				new InputStreamReader((response.getEntity().getContent())));
		String output;
		System.err.println("Output from Server -> ");
		String json = "";
		while ((output = br.readLine()) != null) {
			System.err.println(output);
			json += output;
		}
		System.err.println("JSON:"+json);
		return json;
	}

}
