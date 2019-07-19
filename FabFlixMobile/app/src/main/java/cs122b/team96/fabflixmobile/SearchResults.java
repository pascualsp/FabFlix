package cs122b.team96.fabflixmobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SearchResults extends AppCompatActivity {

    String searchResults;
    String pTitle;
    int pOffset;
    int pLimit;
    final Map<Integer, String> resultKey = new HashMap<Integer, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        Bundle bundle = getIntent().getExtras();
        searchResults = (String) bundle.get("results");
        Log.d("response", searchResults);

        pTitle = (String) bundle.get("title");
        pOffset = (int) bundle.get("offset");
        pLimit = (int) bundle.get("limit");

        try {
            JSONObject obj = new JSONObject(searchResults);
            Log.d("response", obj.toString());
            createList(obj);

        } catch (Exception ex) {

        }


    }

    public void createList(JSONObject obj) {
        Iterator<?> keys = obj.keys();
        int count = 0;

        while(keys.hasNext()) {
            String key = (String)keys.next();
            try {
                JSONObject movieEntry = (JSONObject) obj.get(key);
                String entry = movieEntry.getString("title") + "\n"
                        + movieEntry.getString("year") + "\nDirected By: "
                        + movieEntry.getString("director") + "\nGenre(s): "
                        + movieEntry.getString("genres") + "\nStarring: "
                        + movieEntry.getString("stars") + "\n";
                resultKey.put(R.id.result0 + count, key);
                ((TextView) findViewById(R.id.result0 + count)).setText(entry);
            } catch (Exception ex) {

            }

            count++;
        }

    }

    public void nextPage(final View view){

        final Map<String, String> params = new HashMap<String, String>();
        params.put("title", pTitle);
        params.put("offset", Integer.toString(pOffset + pLimit));
        params.put("limit", "10");

        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        final StringRequest searchRequest = new StringRequest(Request.Method.POST, "https://52.14.189.127:8443/project2/android-search",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!response.equals("{}")) {
                            goToResults(view, response);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "No more search results available", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("security.error", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }  // HTTP POST Form Data
        };

        queue.add(searchRequest);

    }

    public void goToResults(View view, String response) {
        Log.d("response", "entering search results");
        Intent goToIntent = new Intent(this, SearchResults.class);
        goToIntent.putExtra("results", response);
        goToIntent.putExtra("title", pTitle);
        goToIntent.putExtra("offset", pOffset + pLimit);
        goToIntent.putExtra("limit", 10);
        startActivity(goToIntent);
    }

    public void goToMoviePage(View view) {
        int resultID = view.getId();
        try {
            JSONObject obj = new JSONObject(searchResults);
            JSONObject movieEntry = (JSONObject) obj.get(resultKey.get(resultID));

            Log.d("response", "entering movie page from result " + Integer.toString(resultID));
            Intent goToIntent = new Intent(this, MoviePage.class);
            goToIntent.putExtra("movie", movieEntry.toString());
            startActivity(goToIntent);
        } catch (Exception ex) {

        }
    }

    public void goToSearch(View view) {
        Log.d("response", "entering search");
        Intent goToIntent = new Intent(this, Search.class);
        startActivity(goToIntent);
    }

    public void endActivity(View view){
        finish();
    }

}
