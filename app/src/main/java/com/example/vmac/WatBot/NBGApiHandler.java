package com.example.vmac.WatBot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NBGApiHandler {
    OkHttpClient client;

    String sandboxID = "8526967733742300";
    String accountID = "d9cf0f2d-9d9d-41ee-b256-fdc06fbd0c3e";
    String bankID = "DB173089-A8FE-43F1-8947-F1B2A8699829";
    String viewID = "owner";

    NBGApiHandler() {
        client = new OkHttpClient();
    }

    public String createSandbox() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final String[] clientResponse = new String[1];
        clientResponse[0] = "Not working";

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"sandbox_id\":\"8526967733742300\"}");
        Request request = new Request.Builder()
                .url("https://apis.nbg.gr/public/sandbox/obp.account.sandbox/v1.1/sandbox")
                .post(body)
                .addHeader("x-ibm-client-id", "REPLACE_THIS_VALUE")
                .addHeader("request_id", "REPLACE_THIS_VALUE")
                .addHeader("application_id", "REPLACE_THIS_VALUE")
                .addHeader("provider_username", "NBG")
                .addHeader("provider_id", "NBG.gr")
                .addHeader("provider", "NBG")
                .addHeader("content-type", "text/json")
                .addHeader("accept", "text/json")
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                countDownLatch.countDown();
                clientResponse[0] = "Network Error";
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String json = response.body().string();
                clientResponse[0] = json;
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return clientResponse[0];
    }

    public String getSandbox() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final String[] clientResponse = new String[1];
        clientResponse[0] = "Not working";

        Request request = new Request.Builder()
                .url("https://apis.nbg.gr/public/sandbox/obp.account.sandbox/v1.1/sandbox/" + sandboxID)
                .get()
                .addHeader("x-ibm-client-id", "")
                .addHeader("request_id", "REPLACE_THIS_VALUE")
                .addHeader("application_id", "REPLACE_THIS_VALUE")
                .addHeader("provider_username", "NBG")
                .addHeader("provider_id", "NBG.gr")
                .addHeader("provider", "NBG")
                .addHeader("accept", "text/json")
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                countDownLatch.countDown();
                clientResponse[0] = "Network Error";
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String json = response.body().string();
                clientResponse[0] = json;
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return clientResponse[0];
    }

    public String getAccount() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final String[] clientResponse = new String[1];
        clientResponse[0] = "Not working";

        Request request = new Request.Builder()
                .url("https://apis.nbg.gr/public/sandbox/obp.account.sandbox/v1.1/obp/my/banks/" + bankID + "/accounts/" + accountID + "/account")
                .get()
                .addHeader("x-ibm-client-id", "f05576d7-0b56-4080-af58-a44cb8c47f8f")
                .addHeader("request_id", "REPLACE_THIS_VALUE")
                .addHeader("application_id", "REPLACE_THIS_VALUE")
                .addHeader("provider_username", "NBG")
                .addHeader("provider_id", "NBG.gr")
                .addHeader("provider", "NBG")
                .addHeader("sandbox_id", sandboxID)
                .addHeader("accept", "text/json")
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                countDownLatch.countDown();
                clientResponse[0] = "Network Error";
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String json = response.body().string();
                clientResponse[0] = json;
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return clientResponse[0];
    }

    public String getAccountBalance() {
        final String account = getAccount();
        try {
            JSONObject accountJsonObject = new JSONObject(account);
            final String clientResponse = accountJsonObject.getJSONObject("AccountBalance").getString("amount");
            return clientResponse;
        } catch (JSONException e) {
            return "Error";
        }
    }

    public String getAccountIBAN() {
        final String account = getAccount();
        try {
            JSONObject accountJsonObject = new JSONObject(account);
            final String clientResponse = accountJsonObject.getString("IBAN");
            return clientResponse;
        } catch (JSONException e) {
            return "Error";
        }
    }

    public String getAccountLedger() {
        final String account = getAccount();
        try {
            JSONObject accountJsonObject = new JSONObject(account);
            final JSONObject extensions = accountJsonObject.getJSONObject("extensions");
            final String clientResponse = extensions.getString("ledgerBalance");
            return clientResponse;

        } catch (JSONException e) {
            return "Does not exist";
        }
    }
}
