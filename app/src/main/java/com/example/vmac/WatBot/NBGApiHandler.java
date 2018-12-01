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
                .addHeader("x-ibm-client-id", "REPLACE_THIS_VALUE")
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
                .addHeader("x-ibm-client-id", "REPLACE_THIS_VALUE")
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

    public String getAccountInformation() {
        final String account = getAccount();
        try {
            JSONObject accountJsonObject = new JSONObject(account);
            String clientResponse = "";
            final JSONArray ownersJsonArray = accountJsonObject.getJSONArray("owners");
            clientResponse += "Owners: ";
            for (int i = 0; i < ownersJsonArray.length(); i++) {
                clientResponse += ownersJsonArray.getJSONObject(i).getString("display_name");
                if (i + 1 < ownersJsonArray.length()) {
                    clientResponse += ", ";
                }
            }
            clientResponse += "\n";
            clientResponse += "Account Balance: " +
                    accountJsonObject.getJSONObject("AccountBalance").getString("amount") + " " +
                    accountJsonObject.getJSONObject("AccountBalance").getString("currency");
            clientResponse += "\n";
            clientResponse += "IBAN: " +
                    accountJsonObject.getString("IBAN");
            return clientResponse;
        } catch (JSONException e) {
            e.printStackTrace();
            return "Error";
        }
    }

    public String getAccountBalance() {
        final String account = getAccount();
        try {
            JSONObject accountJsonObject = new JSONObject(account);
            final String clientResponse = accountJsonObject.getJSONObject("AccountBalance").getString("amount");
            return clientResponse;
        } catch (JSONException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
            return "Does not exist";
        }
    }

    public String getAccountInterestRate() {
        final String account = getAccount();
        try {
            JSONObject accountJsonObject = new JSONObject(account);
            final JSONObject extensions = accountJsonObject.getJSONObject("extensions");
            final String clientResponse = extensions.getString("interestRate");
            return clientResponse;

        } catch (JSONException e) {
            e.printStackTrace();
            return "Does not exist";
        }
    }

    public String getAccountBenefeciaries() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final String[] clientResponse = new String[1];
        clientResponse[0] = "Not working";

        Request request = new Request.Builder()
                .url("https://apis.nbg.gr/public/sandbox/obp.account.sandbox/v1.1/obp/my/banks/" + bankID + "/accounts/" + accountID + "/beneficiaries")
                .get()
                .addHeader("x-ibm-client-id", "REPLACE_THIS_VALUE")
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
                JSONArray tempBenefeciariesObject = null;
                try {
                    tempBenefeciariesObject = new JSONArray(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject benefeciaryName = null;
                try {
                    benefeciaryName = tempBenefeciariesObject.getJSONObject(0); // first name!
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    clientResponse[0] = benefeciaryName.getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

    public String getAccountBenefeciariesFromAccountId(String userAccountID) {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final String[] clientResponse = new String[1];
        clientResponse[0] = "Not working";

        Request request = new Request.Builder()
                .url("https://apis.nbg.gr/public/sandbox/obp.account.sandbox/v1.1/obp/my/banks/" + bankID + "/accounts/" + userAccountID + "/beneficiaries")
                .get()
                .addHeader("x-ibm-client-id", "REPLACE_THIS_VALUE")
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
                JSONArray tempBenefeciariesObject = null;
                try {
                    tempBenefeciariesObject = new JSONArray(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject benefeciaryName = null;
                try {
                    benefeciaryName = tempBenefeciariesObject.getJSONObject(0); // first name!
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    clientResponse[0] = benefeciaryName.getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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


    public String getAccountCards() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final String[] clientResponse = new String[1];
        clientResponse[0] = "Not working";

        Request request = new Request.Builder()
                .url("https://apis.nbg.gr/public/sandbox/obp.card.sandbox/v1/obp/cards")
                .get()
                .addHeader("x-ibm-client-id", "REPLACE_THIS_VALUE")
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

    public String getCardInformation() {
        final String cards = getAccountCards();
        try {
            JSONObject cardsJsonObject = new JSONObject(cards);
            JSONArray cardsJsonArray = new JSONArray(cardsJsonObject.getString("cards"));
            // Get first card
            final JSONObject cardJsonObject = cardsJsonArray.getJSONObject(0);
            String clientResponse = "";
            clientResponse += "Name on Card: " + cardJsonObject.getString("name_on_card");
            clientResponse += "\n";
            clientResponse += "Bank Card Number: " + cardJsonObject.getString("bank_card_number");
            clientResponse += "\n";
            clientResponse += "Issue Number: " + cardJsonObject.getString("issue_number");
            clientResponse += "\n";
            clientResponse += "Serial Number: " + cardJsonObject.getString("serial_number");
            clientResponse += "\n";
            clientResponse += "Expiration Date: " + simplifyNBGDate(cardJsonObject.getString("expires_date"));
            return clientResponse;
        } catch (JSONException e) {
            e.printStackTrace();
            return "Error";
        }
    }

    public String getCardName() {
        final String cards = getAccountCards();
        try {
            JSONObject cardsJsonObject = new JSONObject(cards);
            JSONArray cardsJsonArray = new JSONArray(cardsJsonObject.getString("cards"));
            // Get first card
            final JSONObject cardJsonObject = cardsJsonArray.getJSONObject(0);
            final String clientResponse = cardJsonObject.getString("name_on_card");
            return clientResponse;

        } catch (JSONException e) {
            e.printStackTrace();
            return "Does not exist";
        }
    }

    public String getCardId() {
        final String cards = getAccountCards();
        try {
            JSONObject cardsJsonObject = new JSONObject(cards);
            JSONArray cardsJsonArray = new JSONArray(cardsJsonObject.getString("cards"));
            // Get first card
            final JSONObject cardJsonObject = cardsJsonArray.getJSONObject(0);
            final String clientResponse = cardJsonObject.getString("cardId");
            return clientResponse;

        } catch (JSONException e) {
            e.printStackTrace();
            return "Does not exist";
        }
    }

    public String getCardBankNumber() {
        final String cards = getAccountCards();
        try {
            JSONObject cardsJsonObject = new JSONObject(cards);
            JSONArray cardsJsonArray = new JSONArray(cardsJsonObject.getString("cards"));
            // Get first card
            final JSONObject cardJsonObject = cardsJsonArray.getJSONObject(0);
            final String clientResponse = cardJsonObject.getString("bank_card_number");
            return clientResponse;

        } catch (JSONException e) {
            e.printStackTrace();
            return "Does not exist";
        }
    }

    public String getCardIssueNumber() {
        final String cards = getAccountCards();
        try {
            JSONObject cardsJsonObject = new JSONObject(cards);
            JSONArray cardsJsonArray = new JSONArray(cardsJsonObject.getString("cards"));
            // Get first card
            final JSONObject cardJsonObject = cardsJsonArray.getJSONObject(0);
            final String clientResponse = cardJsonObject.getString("issue_number");
            return clientResponse;

        } catch (JSONException e) {
            e.printStackTrace();
            return "Does not exist";
        }
    }

    public String getCardSerialNumber() {
        final String cards = getAccountCards();
        try {
            JSONObject cardsJsonObject = new JSONObject(cards);
            JSONArray cardsJsonArray = new JSONArray(cardsJsonObject.getString("cards"));
            // Get first card
            final JSONObject cardJsonObject = cardsJsonArray.getJSONObject(0);
            final String clientResponse = cardJsonObject.getString("serial_number");
            return clientResponse;

        } catch (JSONException e) {
            e.printStackTrace();
            return "Does not exist";
        }
    }

    public String getCardExpiresDate() {
        final String cards = getAccountCards();
        try {
            JSONObject cardsJsonObject = new JSONObject(cards);
            JSONArray cardsJsonArray = new JSONArray(cardsJsonObject.getString("cards"));
            // Get first card
            final JSONObject cardJsonObject = cardsJsonArray.getJSONObject(0);
            final String clientResponse = simplifyNBGDate(cardJsonObject.getString("expires_date"));
            return clientResponse;

        } catch (JSONException e) {
            e.printStackTrace();
            return "Does not exist";
        }
    }

    public String getTransactionRequests() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final String[] clientResponse = new String[1];
        clientResponse[0] = "Not working";

        Request request = new Request.Builder()
                .url("https://apis.nbg.gr/public/sandbox/obp.payment.sandbox/v1.1/obp/banks/" + bankID + "/accounts/" + accountID + "/" + viewID + "/transaction-requests")
                .get()
                .addHeader("x-ibm-client-id", "REPLACE_THIS_VALUE")
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

    public String getAllTransactionRequests() {
        final String transactionRequests = getTransactionRequests();
        try {
            JSONObject transactionRequestsJsonObject = new JSONObject(transactionRequests);
            JSONArray transactionRequestsJsonArray = transactionRequestsJsonObject.getJSONArray("transaction_requests_with_charges");
            String clientResponse = "";
            for (int i = 0; i < transactionRequestsJsonArray.length(); i++) {
                JSONObject transactionRequestJsonObject = transactionRequestsJsonArray.getJSONObject(i);
                clientResponse += "From: " + transactionRequestJsonObject.getJSONObject("from").getString("account_id");
                clientResponse += "\n";
                clientResponse += "To: " + transactionRequestJsonObject.getJSONObject("details").getJSONObject("to").getString("account_id");
                clientResponse += "\n";
                clientResponse += "Amount: " +
                        transactionRequestJsonObject.getJSONObject("details").getJSONObject("value").getString("amount") + " " +
                        transactionRequestJsonObject.getJSONObject("details").getJSONObject("value").getString("currency");
                clientResponse += "\n";
                clientResponse += "Charge Policy: " + transactionRequestJsonObject.getJSONObject("charge").getString("summary");
                clientResponse += "\n";
                clientResponse += "Request Date: " + simplifyNBGDate(transactionRequestJsonObject.getString("start_date"));
                clientResponse += "\n";
                clientResponse += "Status: " + transactionRequestJsonObject.getString("status");
                clientResponse += "\n\n";
            }
            if (clientResponse.length() <= 1) {
                return "No pending transactions";
            }
            return clientResponse;

        } catch (JSONException e) {
            e.printStackTrace();
            return "Does not exist";
        }
    }

    public String getInitiatedTransactionRequests() {
        final String transactionRequests = getTransactionRequests();
        try {
            JSONObject transactionRequestsJsonObject = new JSONObject(transactionRequests);
            JSONArray transactionRequestsJsonArray = transactionRequestsJsonObject.getJSONArray("transaction_requests_with_charges");
            String clientResponse = "";
            for (int i = 0; i < transactionRequestsJsonArray.length(); i++) {
                JSONObject transactionRequestJsonObject = transactionRequestsJsonArray.getJSONObject(i);
                if (transactionRequestJsonObject.getString("status").equals("INITIATED")) {
                    clientResponse += "From: " + transactionRequestJsonObject.getJSONObject("from").getString("account_id");
                    clientResponse += "\n";
                    clientResponse += "To: " + transactionRequestJsonObject.getJSONObject("details").getJSONObject("to").getString("account_id");
                    clientResponse += "\n";
                    clientResponse += "Amount: " +
                            transactionRequestJsonObject.getJSONObject("details").getJSONObject("value").getString("amount") + " " +
                            transactionRequestJsonObject.getJSONObject("details").getJSONObject("value").getString("currency");
                    clientResponse += "\n";
                    clientResponse += "Charge Policy: " + transactionRequestJsonObject.getJSONObject("charge").getString("summary");
                    clientResponse += "\n";
                    clientResponse += "Request Date: " + simplifyNBGDate(transactionRequestJsonObject.getString("start_date"));
                    clientResponse += "\n\n";
                }
            }
            if (clientResponse.length() <= 1) {
                return "No pending transactions";
            }
            return clientResponse;

        } catch (JSONException e) {
            e.printStackTrace();
            return "Does not exist";
        }
    }

    public String getRecentTransactionRequest() {
        final String transactionRequests = getTransactionRequests();
        try {
            JSONObject transactionRequestsJsonObject = new JSONObject(transactionRequests);
            JSONArray transactionRequestsJsonArray = transactionRequestsJsonObject.getJSONArray("transaction_requests_with_charges");
            JSONObject recentTransactionRequestJsonArray = null;
            for (int i = 0; i < transactionRequestsJsonArray.length(); i++) {
                JSONObject transactionRequestJsonObject = transactionRequestsJsonArray.getJSONObject(i);
                if (transactionRequestJsonObject.getString("status").equals("INITIATED")) {
                    recentTransactionRequestJsonArray = transactionRequestJsonObject;
                }
            }
            String clientResponse = "";
            if (recentTransactionRequestJsonArray != null) {
                clientResponse += "From: " + recentTransactionRequestJsonArray.getJSONObject("from").getString("account_id");
                clientResponse += "\n";
                clientResponse += "To: " + recentTransactionRequestJsonArray.getJSONObject("details").getJSONObject("to").getString("account_id");
                clientResponse += "\n";
                clientResponse += "Amount: " +
                        recentTransactionRequestJsonArray.getJSONObject("TransactionAmount").getString("amount") + " " +
                        recentTransactionRequestJsonArray.getJSONObject("TransactionAmount").getString("currency");
                clientResponse += "\n";
                clientResponse += "Charge Policy: " + recentTransactionRequestJsonArray.getJSONObject("charge").getString("summary");
                clientResponse += "\n";
                clientResponse += "Request Date: " + simplifyNBGDate(recentTransactionRequestJsonArray.getString("start_date"));
            }
            else {
                return "No pending transactions";
            }
            return clientResponse;

        } catch (JSONException e) {
            e.printStackTrace();
            return "Does not exist";
        }
    }

    public String answerTransactionRequest() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final String[] clientResponse = new String[1];
        clientResponse[0] = "Not working";

        String recentTransactionRequest = getRecentTransactionRequest();


        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"id\":\"8f3b7cbb-4c2d-4dec-8039-a38e2e673a58\",\"answer\":12345}");
        Request request = new Request.Builder()
                .url("https://apis.nbg.gr/public/sandbox/obp.payment.sandbox/v1.1/obp/banks/" + bankID + "/accounts/" + accountID + "/" + viewID + "/transaction-request-types/" + "SEPA" + "/transaction-requests/" + ""  + "/challenge")
                .post(body)
                .addHeader("x-ibm-client-id", "REPLACE_THIS_VALUE")
                .addHeader("request_id", "REPLACE_THIS_VALUE")
                .addHeader("application_id", "REPLACE_THIS_VALUE")
                .addHeader("provider_username", "NBG")
                .addHeader("provider_id", "NBG.gr")
                .addHeader("provider", "NBG")
                .addHeader("sandbox_id", sandboxID)
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

    public String createSepaPayment() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final String[] clientResponse = new String[1];
        clientResponse[0] = "Not working";

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"extensions\":{\"beneficiaryName\":\"" + getAccountBenefeciaries() + "\",\"challengeExpiration\":\"0001-01-01T00:00:00\",\"challengeText\":\"Test challenge text\",\"challengeType\":\"SMS\"},\"to\":{\"iban\":\"" + getAccountIBAN() + "\"}, \"value\":{\"currency\":\"EUR\",\"amount\":10.0},\"charge_policy\":\"OUR\",\"description\":\"Test SEPA payment.\"}");
        // TODO: get user IBAN and benefeciaries
        // RequestBody body = RequestBody.create(mediaType, "{\"extensions\":{\"beneficiaryName\":\"" + getAccountBenefeciaries() + "\"},\"to\":{\"iban\":\"" + getAccountIBAN() + "\"},\"value\":{\"currency\":\"EUR\",\"amount\":10.0},\"charge_policy\":\"OUR\",\"description\":\"TEST\"}");
        Request request = new Request.Builder()
                .url("https://apis.nbg.gr/public/sandbox/obp.payment.sandbox/v1.1/obp/banks/" + bankID + "/accounts/" + accountID + "/" + viewID + "/transaction-request-types/sepa/transaction-requests")
                .post(body)
                .addHeader("x-ibm-client-id", "REPLACE_THIS_VALUE")
                .addHeader("request_id", "REPLACE_THIS_VALUE")
                .addHeader("application_id", "REPLACE_THIS_VALUE")
                .addHeader("provider_username", "NBG")
                .addHeader("provider_id", "NBG.gr")
                .addHeader("provider", "NBG")
                .addHeader("sandbox_id", sandboxID)
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

    private String simplifyNBGDate(String date) {
        String simplifiedDate = date.split("T")[0].replaceAll("-", "/");
        String[] yearMonthDay = simplifiedDate.split("/");
        return yearMonthDay[2] + "/" + yearMonthDay[1] + "/" + yearMonthDay[0];
    }
}