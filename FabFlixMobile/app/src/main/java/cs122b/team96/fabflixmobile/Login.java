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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void login(final View view){

        EditText username = findViewById(R.id.usernameInput);
        EditText password = findViewById(R.id.passwordInput);
        final boolean[] loginSuccess = {false};

        // Post request form data
        final Map<String, String> params = new HashMap<String, String>();
        params.put("username", username.getText().toString());
        params.put("password", password.getText().toString());

        // Use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        final StringRequest loginRequest = new StringRequest(Request.Method.POST, "https://52.14.189.127:8443/project2/api/android-login",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject obj = new JSONObject(response);
                            Log.d("response", obj.toString());
                            ((TextView) findViewById(R.id.loginResponse)).setText(obj.getString("message"));

                            if (obj.getString("message").equals("success")) {
                                Log.d("response", "login check passed");
                                loginSuccess[0] = true;
                                goToSearch(view);
                            }

                        } catch (Exception ex) {

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
            protected Map<String, String> getParams() {return params;}  // HTTP POST Form Data
        };

        queue.add(loginRequest);

    }

    public void goToSearch(View view) {
        Log.d("response", "entering search");
        Intent goToIntent = new Intent(this, Search.class);
        startActivity(goToIntent);
    }


}
