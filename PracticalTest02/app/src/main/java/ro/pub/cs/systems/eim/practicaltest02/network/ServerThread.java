package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;
import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.model.CurrencyData;

public class ServerThread extends Thread {

    private int port = 0;
    private ServerSocket serverSocket = null;

    private HashMap<String, String> data = null;

    public ServerThread(int port) {
        this.port = port;
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
        this.data = new HashMap<>();
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public synchronized void setData(String currency, String rate) {
        this.data.put(currency, rate);
    }

    public synchronized HashMap<String, String> getData() {
        return data;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Log.i(Constants.TAG, "[SERVER THREAD] Waiting for a client invocation...");
                Socket socket = serverSocket.accept();
                Log.i(Constants.TAG, "[SERVER THREAD] A connection request was received from " + socket.getInetAddress() + ":" + socket.getLocalPort());

                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                // your code here
                                HttpClient httpClient = new DefaultHttpClient();
                                String pageSourceCode = "";
                                HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS);
                                HttpResponse httpGetResponse = null;
                                try {
                                    httpGetResponse = httpClient.execute(httpGet);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                HttpEntity httpGetEntity = httpGetResponse.getEntity();
                                if (httpGetEntity != null) {
                                    try {
                                        pageSourceCode = EntityUtils.toString(httpGetEntity);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                if (pageSourceCode == null) {
                                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                                    return;
                                } else
                                    Log.i(Constants.TAG, pageSourceCode);

                                try {
                                    JSONObject content = new JSONObject(pageSourceCode);

                                    JSONObject usdJSON = content.getJSONObject("bpi").getJSONObject("USD");
                                    JSONObject eurJSON = content.getJSONObject("bpi").getJSONObject("EUR");

                                    String eur_rate = eurJSON.getString("rate");
                                    String usd_rate = usdJSON.getString("rate");

                                    setData(Constants.EUR, eur_rate);
                                    setData(Constants.USD, usd_rate);


                                } catch (JSONException jsonException) {
                                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + jsonException.getMessage());
                                    if (Constants.DEBUG) {
                                        jsonException.printStackTrace();
                                    }
                                }
                            }
                        },
                        1000
                );

                CommunicationThread communicationThread = new CommunicationThread(this, socket);
                communicationThread.start();
            }
        } catch (ClientProtocolException clientProtocolException) {
            Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + clientProtocolException.getMessage());
            if (Constants.DEBUG) {
                clientProtocolException.printStackTrace();
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }

    public void stopThread() {
        interrupt();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            }
        }
    }

}
