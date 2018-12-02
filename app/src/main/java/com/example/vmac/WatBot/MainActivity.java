package com.example.vmac.WatBot;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import java.sql.Timestamp;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.developer_cloud.assistant.v2.Assistant;
import com.ibm.watson.developer_cloud.assistant.v2.model.CreateSessionOptions;
import com.ibm.watson.developer_cloud.assistant.v2.model.MessageContext;
import com.ibm.watson.developer_cloud.assistant.v2.model.MessageInput;
import com.ibm.watson.developer_cloud.assistant.v2.model.MessageOptions;
import com.ibm.watson.developer_cloud.assistant.v2.model.MessageResponse;
import com.ibm.watson.developer_cloud.assistant.v2.model.SessionResponse;
import com.ibm.watson.developer_cloud.http.ServiceCall;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.SynthesizeOptions;

import java.io.InputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {


  private RecyclerView recyclerView;
  private ChatAdapter mAdapter;
  private ArrayList messageArrayList;
  private EditText inputMessage;
  private ImageButton btnSend;
  private ImageButton btnRecord;
  StreamPlayer streamPlayer = new StreamPlayer();
  private boolean initialRequest;
  private boolean permissionToRecordAccepted = false;
  private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
  private static String TAG = "MainActivity";
  private static final int RECORD_REQUEST_CODE = 101;
  private boolean listening = false;
  private MicrophoneInputStream capture;
  private Context mContext;
  private MicrophoneHelper microphoneHelper;

  private Assistant watsonAssistant;
  private SessionResponse watsonAssistantSession;
  private SpeechToText speechService;
  private TextToSpeech textToSpeech;


  private FirebaseStorage storage;
  private StorageReference storageRef;
  private NBGApiHandler nbg;


    private void createServices() {
    watsonAssistant = new Assistant("2018-11-08", new IamOptions.Builder()
      .apiKey(mContext.getString(R.string.assistant_apikey))
      .build());

    textToSpeech = new TextToSpeech();
    textToSpeech.setIamCredentials(new IamOptions.Builder()
      .apiKey(mContext.getString(R.string.TTS_apikey))
      .build());

    speechService = new SpeechToText();
    speechService.setIamCredentials(new IamOptions.Builder()
      .apiKey(mContext.getString(R.string.STT_apikey))
      .build());
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mContext = getApplicationContext();

    inputMessage = findViewById(R.id.message);
    btnSend = findViewById(R.id.btn_send);
    btnRecord = findViewById(R.id.btn_record);
    String customFont = "Montserrat-Regular.ttf";
    Typeface typeface = Typeface.createFromAsset(getAssets(), customFont);
    inputMessage.setTypeface(typeface);
    recyclerView = findViewById(R.id.recycler_view);

    messageArrayList = new ArrayList<>();
    mAdapter = new ChatAdapter(messageArrayList);
    microphoneHelper = new MicrophoneHelper(this);

    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    layoutManager.setStackFromEnd(true);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setItemAnimator(new DefaultItemAnimator());
    recyclerView.setAdapter(mAdapter);
    this.inputMessage.setText("");
    this.initialRequest = true;

    storage = FirebaseStorage.getInstance();
    storageRef = storage.getReference();
    nbg = new NBGApiHandler();


    int permission = ContextCompat.checkSelfPermission(this,
      Manifest.permission.RECORD_AUDIO);

    if (permission != PackageManager.PERMISSION_GRANTED) {
      Log.i(TAG, "Permission to record denied");
      makeRequest();
    } else {
      Log.i(TAG, "Permission to record was already granted");
    }




    recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener() {
      @Override
      public void onClick(View view, final int position) {
        Message audioMessage = (Message) messageArrayList.get(position);
        if (audioMessage != null && !audioMessage.getMessage().isEmpty()) {
          new SayTask().execute(audioMessage.getMessage());
        }
      }

      @Override
      public void onLongClick(View view, int position) {
        recordMessage();

      }
    }));

    btnSend.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (checkInternetConnection()) {
          sendMessage();
        }
      }
    });

    btnRecord.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        recordMessage();
      }
    });

    createServices();
    sendMessage();

  }

  ;

  // Speech-to-Text Record Audio permission
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode) {
      case REQUEST_RECORD_AUDIO_PERMISSION:
        permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        break;
      case RECORD_REQUEST_CODE: {

        if (grantResults.length == 0
          || grantResults[0] !=
          PackageManager.PERMISSION_GRANTED) {

          Log.i(TAG, "Permission has been denied by user");
        } else {
          Log.i(TAG, "Permission has been granted by user");
        }
        return;
      }

      case MicrophoneHelper.REQUEST_PERMISSION: {
        if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
          Toast.makeText(this, "Permission to record audio denied", Toast.LENGTH_SHORT).show();
        }
      }
    }
    // if (!permissionToRecordAccepted ) finish();

  }

  protected void makeRequest() {
    ActivityCompat.requestPermissions(this,
      new String[]{Manifest.permission.RECORD_AUDIO},
      MicrophoneHelper.REQUEST_PERMISSION);
  }

  // Sending a message to Watson Assistant Service
  private void sendMessage() {

    final String inputmessage = this.inputMessage.getText().toString().trim();
    if (!this.initialRequest) {
      Message inputMessage = new Message();
      inputMessage.setMessage(inputmessage);
      inputMessage.setId("1");
      messageArrayList.add(inputMessage);
    } else {
      Message inputMessage = new Message();
      inputMessage.setMessage(inputmessage);
      inputMessage.setId("100");
      this.initialRequest = false;
      Toast.makeText(getApplicationContext(), "Tap on the message for Voice", Toast.LENGTH_LONG).show();

    }

    this.inputMessage.setText("");
    mAdapter.notifyDataSetChanged();

    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          if (watsonAssistantSession == null) {
            ServiceCall<SessionResponse> call = watsonAssistant.createSession(new CreateSessionOptions.Builder().assistantId(mContext.getString(R.string.assistant_id)).build());
            watsonAssistantSession = call.execute();
          }

          MessageInput input = new MessageInput.Builder()
            .text(inputmessage)
            .build();
          MessageOptions options = new MessageOptions.Builder()
            .assistantId(mContext.getString(R.string.assistant_id))
            .input(input)
            .sessionId(watsonAssistantSession.getSessionId())
            .build();
          MessageResponse response = watsonAssistant.message(options).execute();

           // final Message testMessage = new Message();

          final Message outMessage = new Message();
          if (response != null &&
            response.getOutput() != null &&
            !response.getOutput().getGeneric().isEmpty() &&
            "text".equals(response.getOutput().getGeneric().get(0).getResponseType())) {
            outMessage.setId("2");
            String textToVoice;
            // checking if the bot response has to call an internal function
            if(response.getOutput().getActions() != null){
                String textModifiedByApi[] = apiCaller(response.getOutput().getGeneric().get(0).getText(),
                        response.getOutput().getActions().get(0).getName());
                outMessage.setMessage(textModifiedByApi[0]);
                textToVoice = textModifiedByApi[1];
            }
            else{
                outMessage.setMessage(response.getOutput().getGeneric().get(0).getText());
                textToVoice = response.getOutput().getGeneric().get(0).getText();
            }

            System.out.println(outMessage.getMessage());

            messageArrayList.add(outMessage);
            uploadConversations(outMessage.getMessage());

            // speak the message
            new SayTask().execute(textToVoice);

            runOnUiThread(new Runnable() {
              public void run() {
                mAdapter.notifyDataSetChanged();
                if (mAdapter.getItemCount() > 1) {
                  recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);

                }

              }
            });
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    thread.start();

  }

  private class SayTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {
      streamPlayer.playStream(textToSpeech.synthesize(new SynthesizeOptions.Builder()
        .text(params[0])
        .voice(SynthesizeOptions.Voice.EN_US_LISAVOICE)
        .accept(SynthesizeOptions.Accept.AUDIO_WAV)
        .build()).execute());
      return "Did synthesize";
    }
  }

  //Record a message via Watson Speech to Text
  private void recordMessage() {
    if (listening != true) {
      capture = microphoneHelper.getInputStream(true);
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            speechService.recognizeUsingWebSocket(getRecognizeOptions(capture), new MicrophoneRecognizeDelegate());
          } catch (Exception e) {
            showError(e);
          }
        }
      }).start();
      listening = true;
      Toast.makeText(MainActivity.this, "Listening....Click to Stop", Toast.LENGTH_LONG).show();

    } else {
      try {
        microphoneHelper.closeInputStream();
        listening = false;
        Toast.makeText(MainActivity.this, "Stopped Listening....Click to Start", Toast.LENGTH_LONG).show();
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
  }

  /**
   * Check Internet Connection
   *
   * @return
   */
  private boolean checkInternetConnection() {
    // get Connectivity Manager object to check connection
    ConnectivityManager cm =
      (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    boolean isConnected = activeNetwork != null &&
      activeNetwork.isConnectedOrConnecting();

    // Check for network connections
    if (isConnected) {
      return true;
    } else {
      Toast.makeText(this, " No Internet Connection available ", Toast.LENGTH_LONG).show();
      return false;
    }

  }

  //Private Methods - Speech to Text
  private RecognizeOptions getRecognizeOptions(InputStream audio) {
    return new RecognizeOptions.Builder()
      .audio(audio)
      .contentType(ContentType.OPUS.toString())
      .model("en-US_BroadbandModel")
      .interimResults(true)
      .inactivityTimeout(2000)
      .build();
  }

  //Watson Speech to Text Methods.
  private class MicrophoneRecognizeDelegate extends BaseRecognizeCallback {
    @Override
    public void onTranscription(SpeechRecognitionResults speechResults) {
      if (speechResults.getResults() != null && !speechResults.getResults().isEmpty()) {
        String text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
        showMicText(text);
      }
    }

    @Override
    public void onError(Exception e) {
      showError(e);
      enableMicButton();
    }

    @Override
    public void onDisconnected() {
      enableMicButton();
    }

  }

  private void showMicText(final String text) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        inputMessage.setText(text);
      }
    });
  }

  private void enableMicButton() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        btnRecord.setEnabled(true);
      }
    });
  }

  private void showError(final Exception e) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        e.printStackTrace();
      }
    });
  }

    // One API example function
    private String balance() { return "5"; }
    private String iban() { return "3867328236"; }
    private String interest_rate() { return "6.5"; }
    private String expiration_date() { return "1/1/2022"; }
    private String card_info() { return "blah blah"; }
    private String account_info() { return "blah blah"; }
    private String transaction_history() { return "blah blah"; }
    private String pending_transactions() { return "blah blah"; }
    private String recent_transaction() { return "blah blah"; }

   // Function that calls the API functions of the NBG
   private String[] apiCaller(String text, String function){
      String[] response = new String[2];
       String raw_response;
      switch (function){
          case "balance":
              raw_response = nbg.getAccountBalance();
              response[0] = text.replaceFirst("var", raw_response);
              response[1] = text.replaceFirst("var", raw_response);
              break;
          case "iban":
              raw_response = nbg.getAccountIBAN();
              response[0] = text.replaceFirst("var", raw_response);
              response[1] = "<speak>" + text.replaceFirst("var",
                      "<prosody rate=\"x-slow\"><say-as interpret-as=\"digits\">"
                              + raw_response + "</say-as></prosody>") + "</speak>";
              break;
          case "interest_rate":
              raw_response = nbg.getAccountInterestRate();
              response[0] = text.replaceFirst("var", raw_response);
              response[1] = text.replaceFirst("var", raw_response);
              break;
          case "expiration_date":
              raw_response = nbg.getCardExpiresDate();
              response[0] = text.replaceFirst("var", raw_response);
              response[1] = "<speak>" + text.replaceFirst("var",
                      "<say-as interpret-as=\"date\" format=\"dmy\">"
                              + raw_response + "</say-as>") + "</speak>";
              break;
          case "card_info":
              raw_response = nbg.getCardInformation();
              response[0] = text.replaceFirst("var", raw_response);
              response[1] = text.replaceFirst("var", raw_response);
              break;
          case "account_info":
              raw_response = nbg.getAccountInformation();
              response[0] = text.replaceFirst("var", raw_response);
              response[1] = text.replaceFirst("var", raw_response);
              break;
          case "transaction_history":
              raw_response = nbg.getAllTransactionRequests();
              response[0] = text.replaceFirst("var", raw_response);
              response[1] = text.replaceFirst("var", raw_response);
              break;
          case "pending_transactions":
              raw_response = nbg.getInitiatedTransactionRequests();
              response[0] = text.replaceFirst("var", raw_response);
              response[1] = text.replaceFirst("var", raw_response);
              break;
          case "recent_transaction":
              raw_response = nbg.getRecentTransactionRequest();
              response[0] = text.replaceFirst("var", raw_response);
              response[1] = text.replaceFirst("var", raw_response);
              break;
          default:
              response[0] = response[1] = text;
      }
      return response;
   }

    /*public static AmazonS3 createClient(String api_key, String service_instance_id, String endpoint_url, String location)
    {
        AWSCredentials credentials = new BasicIBMOAuthCredentials(api_key, service_instance_id);
        ClientConfiguration clientConfig = new ClientConfiguration().withRequestTimeout(5000);
        clientConfig.setUseTcpKeepAlive(true);

        AmazonS3 cos = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint_url, location)).withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfig).build();

        return cos;
    }*/

    public void uploadConversations(String message){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        StorageReference mountainsRef = storageRef.child(timestamp.toString() + ".txt");
        byte[] data = message.getBytes();
        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });
    }

}



