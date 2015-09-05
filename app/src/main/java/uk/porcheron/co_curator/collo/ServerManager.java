package uk.porcheron.co_curator.collo;

import android.util.Log;
import android.util.SparseArray;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.CCLog;
import uk.porcheron.co_curator.util.Event;
import uk.porcheron.co_curator.util.SoUtils;
import uk.porcheron.co_curator.val.Instance;

/**
 * All other users to connect to us.
 */
public class ServerManager {
    private static final String TAG = "CC:ColloServerManager";

    private static SparseArray<Server> mServers = new SparseArray<>();

    ServerManager() {
    }

    public static void update() {
        Log.v(TAG, "User[" + Instance.globalUserId + "] Update Servers");

        for(User user : Instance.users) {
            if(user.globalUserId == Instance.globalUserId) {
                continue;
            }

            Server s = mServers.get(user.globalUserId);
            if(s != null) {
                if (s.getPort() == uk.porcheron.co_curator.val.Collo.sPort(user.globalUserId)) {
                    continue;
                } else {
                    s.interrupt();
                }
            }

            Log.v(TAG, "User[" + Instance.globalUserId + "] Start listening at " + SoUtils.getIPAddress(true) + ":" + uk.porcheron.co_curator.val.Collo.sPort(user.globalUserId));
            s = new Server(uk.porcheron.co_curator.val.Collo.sPort(user.globalUserId));
            mServers.put(user.globalUserId, s);
            s.start();

        }
    }

    /**
     * Specific server instance
     */
    public static class Server extends Thread {
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

            try {
                mServerSocket = new ServerSocket(mPort);
                Log.v(TAG, "User[" + Instance.globalUserId + "] listening at " + mServerSocket.getInetAddress() + ":" + mServerSocket.getLocalPort());

                try {
                    while (true) {
                        socket = mServerSocket.accept();
                        dataInputStream = new DataInputStream(socket.getInputStream());

                        String messageFromClient = dataInputStream.readUTF();

                        mMessageCount++;
                        Log.v(TAG, "Mesg[" + mMessageCount + "] from " +
                                ":" + socket.getPort() + " = " + messageFromClient);

                        process(messageFromClient);

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
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }
        }

        private static boolean process(String mesg) {
            CCLog.write(Event.COLLO_RECEIVE, "{mesg=" + mesg + "}");

            String[] array = mesg.split(ColloDict.SEP_SPLIT);
            if(array.length < 2) {
                Log.e(TAG, "Message can't be processed: " + mesg);
                return false;
            }
            String[] data =  Arrays.copyOfRange(array, 2, array.length);

            String action = array[0];
            int globalUserId;

            try {
                globalUserId = Integer.parseInt(array[1]);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Aborting processing, invalid Global User ID received");
                return false;
            }

            return ColloManager.ResponseManager.respond(action, globalUserId, data);
        }
    }
}
