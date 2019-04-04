package com.android.learning.weatherforecast.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;

import com.android.learning.weatherforecast.activities.MainActivity;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class GenericRequestTask extends AsyncTask<String, String, TaskOutput> {
    private ProgressDialog progressDialog;
    Context context;
    MainActivity activity;
    private int loading = 0;

    public GenericRequestTask(Context context, MainActivity activity, ProgressDialog progressDialog) {
        this.context = context;
        this.activity = activity;
        this.progressDialog = progressDialog;
    }

    @Override
    protected void onPreExecute() {
        loading++;
        if(!progressDialog.isShowing()) {
            progressDialog.setMessage("Downloading your data ...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }

    @Override
    protected TaskOutput doInBackground(String... params) {
        TaskOutput output = new TaskOutput();
        String response = "";
        String[] reqParams = new String[]{};

        if(params!=null && params.length>0) {
            if(params[0].equals("cachedResponse")) {
                response = params[1];
                // Không làm gì trong trường hợp này
                output.taskResult = TaskResult.SUCCESS;
            } else if(params[0].equals("coords")) {
                String lat = params[1];
                String lon = params[2];
                reqParams = new String[]{"coords", lat, lon};
            } else  if(params[0].equals("city")) {
                reqParams = new String[]{"city", params[1]};
            }
        }
        if (response.isEmpty()) {
            try {
                URL url = provideURL(reqParams);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                if (urlConnection.getResponseCode() == 200) {
                    InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
                    BufferedReader r = new BufferedReader(inputStreamReader);

                    int responseCode = urlConnection.getResponseCode();
                    String line = null;
                    while ((line = r.readLine()) != null) {
                        response += line + "\n";
                    }
                    close(r);
                    urlConnection.disconnect();
                    // Background work finished successfully
                    Log.i("Task", "done successfully");
                    output.taskResult = TaskResult.SUCCESS;
                    // Save date/time for latest successful result
                    activity.saveLastUpdateTime(PreferenceManager.getDefaultSharedPreferences(context));
                } else if (urlConnection.getResponseCode() == 429) {
                    // Too many requests
                    Log.i("Task", "too many requests");
                    output.taskResult = TaskResult.TOO_MANY_REQUESTS;
                } else {
                    // Bad response from server
                    Log.i("Task", "bad response " + urlConnection.getResponseCode());
                    output.taskResult = TaskResult.BAD_RESPONSE;
                }
            } catch (IOException e) {
                Log.e("IOException Data", response);
                e.printStackTrace();
                // Exception while reading data from url connection
                output.taskResult = TaskResult.IO_EXCEPTION;
            }
        }

        if (TaskResult.SUCCESS.equals(output.taskResult)) {
            // Parse JSON data
            ParseResult parseResult = parseResponse(response);
            if (ParseResult.CITY_NOT_FOUND.equals(parseResult)) {
                // Retain previously specified city if current one was not recognized
                restorePreviousCity();
            }
            output.parseResult = parseResult;
        }
        return null;
    }

    @Override
    protected void onPostExecute(TaskOutput taskOutput) {
        if(loading==1)
            progressDialog.dismiss();
        loading--;
        handleTaskOutput(taskOutput);
    }

    private void handleTaskOutput(TaskOutput output) {
        switch (output.taskResult) {
            case SUCCESS: {
                ParseResult parseResult = output.parseResult;
                if(ParseResult.CITY_NOT_FOUND.equals(parseResult)) {
                    Snackbar.make(activity.findViewById(android.R.id.content), "Không tìm thấy thành phố", Snackbar.LENGTH_LONG).show();
                } else if(ParseResult.JSON_EXCEPTION.equals(parseResult)) {
                    Snackbar.make(activity.findViewById(android.R.id.content), "Lỗi parse JSON", Snackbar.LENGTH_LONG).show();
                }
                break;
            }
            case TOO_MANY_REQUESTS: {
                Snackbar.make(activity.findViewById(android.R.id.content), "Quá nhiều requests", Snackbar.LENGTH_LONG).show();
                break;
            }
            case BAD_RESPONSE: {
                Snackbar.make(activity.findViewById(android.R.id.content), "Vấn đề kết nối internet", Snackbar.LENGTH_LONG).show();
                break;
            }
            case IO_EXCEPTION: {
                Snackbar.make(activity.findViewById(android.R.id.content), "Lỗi kết nối", Snackbar.LENGTH_LONG).show();
                break;
            }
        }
    }

    /**
     * Tạo URL để gọi api từ openweathermap
     * @param reqParams các thông số của request
     * @return URL
     * @throws UnsupportedEncodingException bắt ngoại lệ
     * @throws MalformedURLException bắt ngoại lệ
     */
    private URL provideURL(String[] reqParams) throws UnsupportedEncodingException, MalformedURLException {
        try {
            String apiKey = "b4eefd81f07962985a467d1faf199755";

            StringBuilder urlBuilder = new StringBuilder("https://api.openweathermap.org/data/2.5/");
            urlBuilder.append(getAPIName()).append("?");
            if(reqParams.length>0) {
                final  String zeroParam = reqParams[0];
                if(reqParams[0].equals("city")) { // theo tên thành phố
                    urlBuilder.append("q=").append(reqParams[1]);
                } else if(reqParams[0].equals("coords")) { // theo toạ độ
                    urlBuilder.append("lat=").append(reqParams[1]).append("&lon=").append(reqParams[2]);
                }
            } else {
                urlBuilder.append("id=1581130");

            }
            urlBuilder.append("&lang=vi");
            urlBuilder.append("&mode=json");
            urlBuilder.append("&appid=").append(apiKey);
            return new URL(urlBuilder.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
            return null;
        }
    }

    private static void close(Closeable x) {
        try {
            if (x != null) {
                x.close();
            }
        } catch (IOException e) {
            Log.e("IOException Data", "Error occurred while closing stream");
        }
    }

    private void restorePreviousCity() {
        if (!TextUtils.isEmpty(activity.recentCityId)) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putString("cityId", activity.recentCityId);
            editor.apply();
            activity.recentCityId = "";
        }
    }

    abstract ParseResult parseResponse(String response);

    abstract String getAPIName();
}
