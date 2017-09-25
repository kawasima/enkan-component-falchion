package enkan.component.falchion;

import enkan.component.ComponentLifecycle;
import enkan.component.SystemComponent;
import enkan.exception.FalteringEnvironmentException;
import enkan.exception.MisconfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class FalchionStartNotifier extends SystemComponent {
    private String containerUrl = "http://localhost:44010";
    private int connectionTimeout = 3000;
    private int socketTimeout = 3000;

    @Override
    protected ComponentLifecycle lifecycle() {
        return new ComponentLifecycle<FalchionStartNotifier>() {
            @Override
            public void start(FalchionStartNotifier component) {
                String vmName = ManagementFactory.getRuntimeMXBean().getName();
                long pid = Long.valueOf(vmName.split("@")[0]);

                HttpURLConnection conn = null;
                try {
                    URL url = new URL(containerUrl + "/jvm/" + pid + "/ready");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setUseCaches(false);
                    conn.setConnectTimeout(connectionTimeout);
                    conn.setReadTimeout(socketTimeout);
                    conn.connect();
                    int status = conn.getResponseCode();

                    if (status != HttpURLConnection.HTTP_NO_CONTENT)
                        throw new IOException("The respond form the container was not 204");
                    try (InputStream in = conn.getInputStream()) {
                        while(in.read() >= 0) {
                            // Skip
                        }
                    }
                } catch (MalformedURLException e) {
                    throw new MisconfigurationException("falchion.MALFORMED_URL", e);
                } catch (IOException e) {
                    throw new FalteringEnvironmentException(e);
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }

            @Override
            public void stop(FalchionStartNotifier component) {

            }
        };
    }

    /**
     * Set a container URL.
     *
     * @param containerUrl a container URL
     */
    public void setContainerUrl(String containerUrl) {
        this.containerUrl = containerUrl;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }
}
