package android.iocl.dac_collector.Services;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;


/*


Author: Mokardder Hossain
Date: 04-10-2025 - 19:46
N24, Wb, India

*/



///***  USAGE ***///
/*

DownloadService.with(context)
    .downloadFromUrl("https://example.com/app.apk")
    .onProgress(percent -> Log.d("Download", "Progress: " + percent + "%"))
    .onDownloadCompleted(file -> Log.d("Download", "Saved to: " + file.getAbsolutePath()))
    .onError(e -> Log.e("Download", "Failed: " + e.getMessage()))
    .start();


*/

public class DownloadService {
    private final Context context;
    private String apkUrl;
    private Consumer<Integer> progressListener;
    private Consumer<File> successListener;
    private Consumer<Exception> errorListener;

    private DownloadService(Context context) {
        this.context = context.getApplicationContext();
    }

    /** Entry point for builder */
    public static DownloadService with(Context context) {
        return new DownloadService(context);
    }

    /** Specify URL */
    public DownloadService downloadFromUrl(String url) {
        this.apkUrl = url;
        return this;
    }

    /** Progress callback: percent [0–100] */
    public DownloadService onProgress(Consumer<Integer> listener) {
        this.progressListener = listener;
        return this;
    }

    /** Completion callback */
    public DownloadService onDownloadCompleted(Consumer<File> listener) {
        this.successListener = listener;
        return this;
    }

    /** Error callback */
    public DownloadService onError(Consumer<Exception> listener) {
        this.errorListener = listener;
        return this;
    }

    /** Kick off the download */
    public void start() {
        new DownloadTask().execute(apkUrl);
    }

    /** Internal AsyncTask */
    private class DownloadTask extends AsyncTask<String, Integer, File> {
        private Exception error;

        @Override
        protected File doInBackground(String... params) {
            InputStream in = null;
            FileOutputStream out = null;
            HttpURLConnection conn = null;
            try {
                URL url = new URL(params[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10_000);
                conn.setReadTimeout(15_000);
                conn.connect();

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new Exception("HTTP " + conn.getResponseCode()
                            + " " + conn.getResponseMessage());
                }

                int fileLength = conn.getContentLength();
                in = new BufferedInputStream(conn.getInputStream());

                File apkFile;
                try {
                    apkFile = new File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            "downloaded_app.apk"
                    );
                }catch (Exception e) {

                    apkFile = new File(
                            context.getExternalFilesDir(null),
                            "downloaded_app.apk"
                    );

                }



                out = new FileOutputStream(apkFile);

                byte[] buffer = new byte[8 * 1024];
                long total = 0;
                int count;
                while ((count = in.read(buffer)) != -1) {
                    if (isCancelled()) {
                        return null;
                    }
                    total += count;
                    if (fileLength > 0) {
                        publishProgress((int) (total * 100 / fileLength));
                    }
                    out.write(buffer, 0, count);
                }
                out.flush();
                return apkFile;

            } catch (Exception e) {
                error = e;
                return null;
            } finally {
                try { if (out != null) out.close(); } catch (Exception ignored) {}
                try { if (in != null) in.close(); } catch (Exception ignored) {}
                if (conn != null) conn.disconnect();
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onProgressUpdate(Integer... values) {
            if (progressListener != null) {
                progressListener.accept(values[0]);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(File result) {
            if (error != null) {
                if (errorListener != null) errorListener.accept(error);
            } else {
                if (successListener != null) successListener.accept(result);
            }
        }
    }
}
