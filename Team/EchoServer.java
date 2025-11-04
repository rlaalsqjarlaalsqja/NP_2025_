import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/** 프로토콜 오류 */
class ProtocolError extends Exception {
    public ProtocolError(String msg) { super(msg); }
}

/** 요청 모델: n과 message */
class MessageRequest {
    final int n;
    final String message;
    MessageRequest(int n, String message) { this.n = n; this.message = message; }
}

/** "<n> <message>" 한 줄 파싱 */
class RequestParser {
    static MessageRequest parse(String line) throws ProtocolError {
        if (line == null) throw new ProtocolError("Empty line");
        line = line.trim();
        int sp = line.indexOf(' ');
        if (sp < 0) throw new ProtocolError("Format: '<n> <message>'");
        int n;
        try { n = Integer.parseInt(line.substring(0, sp)); }
        catch (NumberFormatException e) { throw new ProtocolError("n must be an integer"); }
        if (n < 0) throw new ProtocolError("n must be >= 0");
        String msg = line.substring(sp + 1);
        return new MessageRequest(n, msg);
    }
}

/** 응답 생성: message를 n번 줄바꿈으로 */
class EchoResponder {
    static String respond(MessageRequest req) {
        if (req.n == 0) return ""; // n==0이면 빈 응답
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < req.n; i++) sb.append(req.message).append('\n');
        return sb.toString();
    }
}

/** 단일 클라이언트 세션 처리 */
class ClientSession implements Runnable {
    private final Socket sock;
    ClientSession(Socket sock) { this.sock = sock; }

    @Override public void run() {
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(sock.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(sock.getOutputStream(), StandardCharsets.UTF_8))
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                try {
                    MessageRequest req = RequestParser.parse(line);
                    String resp = EchoResponder.respond(req);
                    if (!resp.isEmpty()) out.write(resp);
                    out.flush();
                } catch (ProtocolError e) {
                    out.write("ERROR: " + e.getMessage() + "\n");
                    out.flush();
                }
            }
        } catch (IOException ignored) {
        } finally {
            try { sock.close(); } catch (IOException ignored) {}
        }
    }
}

/** N-Echo 서버 (host/port만 사용) */
public class EchoServer {
    private final String host;
    private final int port;

    public EchoServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws IOException {
        try (ServerSocket server = new ServerSocket()) {
            server.bind(new InetSocketAddress(host, port));
            System.out.println("[INFO] listening on " + host + ":" + port);
            while (true) {
                Socket client = server.accept();
                new Thread(new ClientSession(client)).start();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new EchoServer("0.0.0.0", 5000).start();
    }
}
