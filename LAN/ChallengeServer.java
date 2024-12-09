package game.LAN;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import game.GameSettings;

/**
 * Can be used to create a server which will make a {@link Challenge}
 * discoverable to other clients on the network.
 */
public class ChallengeServer {

    /**
     * The socket that is used to listen for other clients that are searching for
     * challenges on the network.
     */
    private DatagramSocket udpSocket;

    /**
     * The socket used to accept connections from clients that accept this
     * challenge.
     */
    private ServerSocket tcpSocket;

    /**
     * The thread used to listen for clients searching for challenges on the
     * network.
     */
    private Thread searchListenerThread;

    /**
     * The thread used to wait for and process client's that accept this challenge.
     */
    private Thread accepterThread;

    /**
     * The challenge this server is broadcasting.
     */
    private Challenge challenge;

    /**
     * A callback to be executed when the game is created after a user accepts the
     * challenge.
     */
    private Runnable gameCreatedCallback;

    /**
     * The client that is created when a user accepts the challenge.
     */
    private Client client;

    /**
     * The task that accepts other client's connections who accept this challenge.
     */
    private Runnable connectionListener = () -> {

        try {

            while (true) {

                Socket connection = tcpSocket.accept();

                client = new Client(connection,
                        challenge.getName(),
                        challenge.getColor(),
                        new GameSettings(challenge.getFen(),
                                challenge.getTimePerSide(),
                                challenge.getTimePerMove(),
                                false,
                                false,
                                true,
                                true),
                        gameCreatedCallback);

            }

        } catch (Exception e) {
        }

    };

    /**
     * The task that is to be run which listens for other clients sending out
     * feelers, and then sends the details of this challenge to them.
     */
    private Runnable searchListener = () -> {

        try {

            while (true) {

                byte[] buf = new byte[10];

                DatagramPacket packet = new DatagramPacket(buf, 10);
                udpSocket.receive(packet);

                new Thread(() -> {

                    try {

                        DatagramPacket pack = new DatagramPacket(challenge.toString().getBytes(),
                                challenge.toString().length(),
                                packet.getAddress(),
                                Client.PORT);

                        udpSocket.send(pack);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                },
                        "Challenge Sender - " + packet.getAddress()).start();

            }

        } catch (Exception e) {
        }

    };

    /**
     * Creates a new challenge server, which broadcasts a challenge to the local
     * network.
     * 
     * @param challenge           The challenge to broadcast.
     * @param gameCreatedCallback The callback to be executed when the game is
     *                            created.
     * @throws Exception If there is an issue establishing the socket connection.
     */
    public ChallengeServer(Challenge challenge, Runnable gameCreatedCallback) throws Exception {

        this.challenge = challenge;
        this.gameCreatedCallback = gameCreatedCallback;

        udpSocket = new DatagramSocket(Client.PORT);
        udpSocket.setBroadcast(true);

        tcpSocket = new ServerSocket(Client.PORT);

    }

    /**
     * Gets the client that is created as a result of another user accepting this
     * challenge.
     * 
     * @return {@link #client}
     */
    public Client getClient() {
        return client;
    }

    /**
     * Starts broadcasting the challenge on the local network.
     */
    public void start() {

        searchListenerThread = new Thread(searchListener, "Challenge Server Listener");
        searchListenerThread.start();

        accepterThread = new Thread(connectionListener, "Challenge Server Accepter");
        accepterThread.start();

    }

    /**
     * Stops broadcasting the challenge on the local network.
     */
    public void stop() {

        try {

            udpSocket.close();
            tcpSocket.close();

        } catch (Exception e) {
        }

    }

}
