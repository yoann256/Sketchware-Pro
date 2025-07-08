package mod.yoann256.github;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
public class GithubHelper {
    @NonNull
    public String getLastCommit() {
        try {
            HttpURLConnection commitConn = createConnection("https://api.github.com/repos/Skecthware-Pro/Sketchware-Pro/commits/main", "");
            if (commitConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Error: " + commitConn.getResponseCode();
            }
            JSONObject commit = new JSONObject(readResponse(commitConn));
            String fullSha = commit.getString("sha");
            return fullSha.substring(0, 7);

        } catch (Exception e) {
            return "Exception: " + e;
        }
    }

    private HttpURLConnection createConnection(String urlString, @Nullable String token) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (token != null) {
            conn.setRequestProperty("Authorization", "token " + token);
        }
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
        return conn;
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }
}
