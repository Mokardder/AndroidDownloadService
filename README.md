# 🧩 DownloadService (Android)

A lightweight, builder-style utility for downloading APKs (or any file) in the background using `AsyncTask`.  
It handles progress updates, success callbacks, and error reporting — all through simple, chainable methods.

---

## 🚀 Features
- Download files from any HTTP/HTTPS URL  
- Progress callback (`onProgress`) with percentage updates  
- Completion callback (`onDownloadCompleted`) with the downloaded file  
- Error callback (`onError`) with exception details  
- Auto-fallback: saves file to `Downloads` or app’s private directory if access fails  
- Runs cleanly on Android 5.0+  

---

## 💡 Example Usage
```java
DownloadService.with(context)
    .downloadFromUrl("https://example.com/app.apk")
    .onProgress(percent -> Log.d("Download", "Progress: " + percent + "%"))
    .onDownloadCompleted(file -> Log.d("Download", "Saved to: " + file.getAbsolutePath()))
    .onError(e -> Log.e("Download", "Failed: " + e.getMessage()))
    .start();
