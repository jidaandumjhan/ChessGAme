package game.LAN;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;

import game.Game;

/**
 * Class used to send out requests for {@link Challenge}s on the local network.
 */
public class ChallengeSearcher {

    /** The amount of times the searcher will search before giving up. */
    private static final int SEARCH_RETRIES = 3;

    /** The amount of time in between successive searches. */
    private static final int SEARCH_MILLIS_BETWEEN = 1000;

    /** The socket used to send feelers and receive challenges. */
    private DatagramSocket socket;

    /** The thread used to listen for challenges. */
    private Thread listenThread;

    /** The thread used to send feelers for challenges. */
    private Thread emitThread;

    /** A list of challenges that were received. */
    private ArrayList<Challenge> challenges;

    /** The searcher's own address. */
    private InetAddress ownAddress;

    /** A callback to be executed when the search for challenges is complete. */
    private Runnable searchDoneCallback;

    /**
     * The task to be executed on {@link #emitThread}, which sends out feelers on
     * the broadcast channel for challenges.
     */
    private Runnable emitter = () -> {

        try {

            String send = Game.VERSION;
            DatagramPacket packet = new DatagramPacket(send.getBytes(), send.length(),
                    InetAddress.getByName("255.255.255.255"), Client.PORT);

            for (int i = 0; i < SEARCH_RETRIES; i++) {

                try {

                    socket.send(packet);
                    Thread.sleep(SEARCH_MILLIS_BETWEEN);

                } catch (Exception e) {

                }

            }

        } catch (Exception e) {

        }

        if (searchDoneCallback != null)
            searchDoneCallback.run();

    };

    /**
     * The task to be executed by {@link #listenThread}, which listens for
     * challenges sent by other lients on the network.
     */
    private Runnable listener = () -> {

        try {

            while (true) {

                byte[] buf = new byte[100];
                DatagramPacket packet = new DatagramPacket(buf, 100);
                socket.receive(packet);

                try {
                    Challenge add = new Challenge(packet);
                    if (add.getVersion().equals(Game.VERSION) && !packet.getAddress().equals(ownAddress)
                            && !challenges.contains(add))
                        challenges.add(add);
                } catch (Exception e) {
                    continue;
                }

            }

        } catch (Exception e) {
        }

    };

    /**
     * Creates a new searcher object, which can be used to search for challenges on
     * the local network.
     * 
     * @throws Exception If there is an issue with getting the client's address.
     */
    public ChallengeSearcher() throws Exception {

        ownAddress = getOwnAddress();

        challenges = new ArrayList<Challenge>();

        searchDoneCallback = null;

    }

    /**
     * Gets the list of challenges found by the searcher.
     * 
     * @return {@link #challenges}
     */
    public ArrayList<Challenge> getChallenges() {
        return challenges;
    }

    /**
     * Searches for challenges on the local network.
     * 
     * @param searchDoneCallback A callback to be executed when the search is
     *                           completed.
     * @throws Exception If there is an error connecting to the socket.
     */
    public void search(Runnable searchDoneCallback) throws Exception {

        this.searchDoneCallback = searchDoneCallback;
        challenges.clear();

        socket = new DatagramSocket(Client.PORT);
        socket.setBroadcast(true);

        listenThread = new Thread(listener, "Challenge Search Listener");
        listenThread.start();

        emitThread = new Thread(emitter, "Challenge Search Emitter");
        emitThread.start();

    }

    /**
     * Stops searching for challenges and closes the socket.
     */
    public void stop() {

        try {
            socket.close();
        } catch (Exception e) {

        }

    }

    /**
     * Gets the client's address. May get the wrong address, particularly on
     * Windows, if there are multiple enabled network adapters. Disable network
     * adapters that are not in use to fix.
     * 
     * @return The address of the client.
     * @throws IOException If there is an issue getting the network interfaces of
     *                     the client.
     */
    private InetAddress getOwnAddress() throws IOException {

        Enumeration<NetworkInterface> is = NetworkInterface.getNetworkInterfaces();

        while (is.hasMoreElements()) {

            NetworkInterface ifsInterface = is.nextElement();
            Enumeration<InetAddress> ads = ifsInterface.getInetAddresses();

            while (ads.hasMoreElements()) {

                InetAddress a = ads.nextElement();

                if (a.isSiteLocalAddress())
                    return a;

            }

        }

        return null;

    }

}
