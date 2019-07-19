package cs122b.team96.fabflixmobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class Search extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
    }

    public void search(final View view) {
        final EditText movieTitle = findViewById(R.id.searchQuery);

        final Map<String, String> params = new HashMap<String, String>();
        params.put("title", movieTitle.getText().toString());
        params.put("offset", "0");
        params.put("limit", "10");

        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        final StringRequest searchRequest = new StringRequest(Request.Method.POST, "https://52.14.189.127:8443/project2/android-search",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        goToResults(view, response, movieTitle.getText().toString());
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

    public void goToResults(View view, String response, String searchTerm) {
        Log.d("response", "entering search results");
        Intent goToIntent = new Intent(this, SearchResults.class);
        goToIntent.putExtra("results", response);
        goToIntent.putExtra("title", searchTerm);
        goToIntent.putExtra("offset", 0);
        goToIntent.putExtra("limit", 10);
        startActivity(goToIntent);
    }
}
