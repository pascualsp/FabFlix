package cs122b.team96.fabflixmobile;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONObject;

public class MoviePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_page);

        Bundle bundle = getIntent().getExtras();
        try {
            JSONObject obj = new JSONObject((String) bundle.get("movie"));

            ((TextView) findViewById(R.id.movieTitle)).setText(obj.getString("title"));
            ((TextView) findViewById(R.id.movieYear)).setText(obj.getString("year"));
            ((TextView) findViewById(R.id.movieDirector)).setText(obj.getString("director"));
            ((TextView) findViewById(R.id.movieGenres)).setText(obj.getString("genres"));
            ((TextView) findViewById(R.id.movieStars)).setText(obj.getString("stars"));

        } catch (Exception ex) {

        }
    }
}
