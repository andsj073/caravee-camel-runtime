package io.caravee.runtime;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

import jakarta.enterprise.context.ApplicationScoped;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.spi.CamelEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.jboss.logging.Logger;

/**
 * Writes a JSON-lines file for every completed/failed exchange so the
 * Caravee agent can record individual runs with exact timestamps.
 *
 * Output: /data/events/exchanges.jsonl (one JSON object per line).
 *
 * The agent tails this file and creates a run entry per line.
 */
@ApplicationScoped
public class ExchangeEventNotifier extends EventNotifierSupport {

    private static final Logger LOG = Logger.getLogger(ExchangeEventNotifier.class);
    private static final Path EVENTS_DIR = Path.of("/data/events");
    private static final Path EVENTS_FILE = EVENTS_DIR.resolve("exchanges.jsonl");
    private static final int MAX_BODY_PREVIEW = 200;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        // Ignore route events, lifecycle events — only exchange events
        setIgnoreCamelContextEvents(true);
        setIgnoreRouteEvents(true);
        setIgnoreServiceEvents(true);
        setIgnoreExchangeCreatedEvent(true);
        setIgnoreExchangeSentEvents(true);
        setIgnoreExchangeRedeliveryEvents(true);

        Files.createDirectories(EVENTS_DIR);
        LOG.info("Exchange event notifier started — writing to " + EVENTS_FILE);
    }

    @Override
    public void notify(CamelEvent event) throws Exception {
        if (event instanceof CamelEvent.ExchangeCompletedEvent completed) {
            writeEvent(completed.getExchange(), "completed");
        } else if (event instanceof CamelEvent.ExchangeFailedEvent failed) {
            writeEvent(failed.getExchange(), "failed");
        }
    }

    private void writeEvent(Exchange exchange, String status) {
        try {
            String routeId = exchange.getFromRouteId();
            if (routeId == null) routeId = "";

            String exchangeId = exchange.getExchangeId();
            long created = exchange.getCreated();
            long durationMs = System.currentTimeMillis() - created;

            String bodyPreview = "";
            try {
                Object body = exchange.getMessage().getBody();
                if (body != null) {
                    String s = body.toString();
                    bodyPreview = s.length() > MAX_BODY_PREVIEW
                            ? s.substring(0, MAX_BODY_PREVIEW) + "..."
                            : s;
                    // Escape for JSON
                    bodyPreview = bodyPreview
                            .replace("\\", "\\\\")
                            .replace("\"", "\\\"")
                            .replace("\n", "\\n")
                            .replace("\r", "\\r")
                            .replace("\t", "\\t");
                }
            } catch (Exception e) {
                bodyPreview = "(unreadable)";
            }

            String error = "";
            if ("failed".equals(status) && exchange.getException() != null) {
                error = exchange.getException().getMessage();
                if (error == null) error = exchange.getException().getClass().getSimpleName();
                error = error.replace("\"", "\\\"").replace("\n", " ");
                if (error.length() > 200) error = error.substring(0, 200) + "...";
            }

            String json = String.format(
                    "{\"ts\":\"%s\",\"routeId\":\"%s\",\"exchangeId\":\"%s\",\"status\":\"%s\",\"durationMs\":%d,\"bodyPreview\":\"%s\",\"error\":\"%s\"}",
                    Instant.now().toString(),
                    routeId,
                    exchangeId,
                    status,
                    durationMs,
                    bodyPreview,
                    error
            );

            // Rotate if file too large
            if (Files.exists(EVENTS_FILE) && Files.size(EVENTS_FILE) > MAX_FILE_SIZE) {
                Path backup = EVENTS_DIR.resolve("exchanges.jsonl.1");
                Files.deleteIfExists(backup);
                Files.move(EVENTS_FILE, backup);
            }

            synchronized (this) {
                try (BufferedWriter w = Files.newBufferedWriter(EVENTS_FILE,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                    w.write(json);
                    w.newLine();
                }
            }
        } catch (IOException e) {
            LOG.warn("Failed to write exchange event", e);
        }
    }
}
