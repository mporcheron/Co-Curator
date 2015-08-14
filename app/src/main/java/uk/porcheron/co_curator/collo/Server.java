package uk.porcheron.co_curator.collo;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import uk.porcheron.co_curator.db.WebLoader;
import uk.porcheron.co_curator.val.Collo;
import uk.porcheron.co_curator.val.Instance;

/**
 * Created by map on 14/08/15.
 */
public class Server extends Thread {
    private static final String TAG = "CC:ColloServer";

    private ServerSocket mServerSocket;
    private int mMessageCount = 0;
    private int mPort;

    Server(int port) {
        mPort = port;
    }

    public int getPort() {
        return mPort;
    }

    @Override
    public void run() {
        Socket socket = null;
        DataInputStream dataInputStream = null;
        DataOutputStream dataOutputStream = null;

        try {
            mServerSocket = new ServerSocket(mPort);
            Log.d(TAG, "User[" + Instance.globalUserId + "] listening at " + mServerSocket.getInetAddress() + ":" + mServerSocket.getLocalPort());

            try {
                while (true) {
                    socket = mServerSocket.accept();
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    String messageFromClient = dataInputStream.readUTF();

                    mMessageCount++;
                    Log.d(TAG, "Mesg[" + mMessageCount + "] from " +
                            ":" + socket.getPort() + " = " + messageFromClient);

                    process(messageFromClient);

                    String msgReply = "Thank you.";
                    dataOutputStream.writeUTF(msgReply);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                        e.printStackTrace();
                    }
                }

                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                        e.printStackTrace();
                    }
                }

                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }

    private static void process(String mesg) {
        String[] array = mesg.split("\\|");
        switch (array[0]) {
            case "newitem":
                try {
                    WebLoader.loadItemFromWeb(Integer.parseInt(array[1]), Integer.parseInt(array[2]));
                } catch (NumberFormatException e) {
                    Log.d(TAG, "Invalid Ids received");
                }
                break;
        }
    }
}