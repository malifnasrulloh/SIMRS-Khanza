package fungsi;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;

public final class NotifikasiEngine {
    private static volatile ScheduledExecutorService scheduler;
    private static volatile boolean running = false;
    private static volatile String currentIdUser = "";
    private static volatile String currentNik = "";
    private static final Set<String> assignedRoles = Collections.synchronizedSet(new HashSet<>());
    private static final Set<Long> dismissedIds = Collections.synchronizedSet(new HashSet<>());
    private static final List<NotifikasiModel> activeList = Collections.synchronizedList(new ArrayList<>());
    private static volatile Consumer<Integer> badgeCallback;
    private static volatile int previousCount = 0;
    private static volatile boolean soundEnabled = true;

    private NotifikasiEngine() {}

    public static synchronized void start(String idUser, String nik, Consumer<Integer> onCountChanged) {
        stop();
        currentIdUser = idUser == null ? "" : idUser.trim();
        currentNik = nik == null ? "" : nik.trim();
        badgeCallback = onCountChanged;
        dismissedIds.clear();
        activeList.clear();
        previousCount = 0;
        loadRoles();

        running = true;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Khanza-Notifikasi-Poller");
            t.setDaemon(true);
            return t;
        });

        // Initial poll after 2 seconds, then every 15 seconds
        scheduler.scheduleWithFixedDelay(NotifikasiEngine::poll, 2, 15, TimeUnit.SECONDS);
    }

    public static synchronized void stop() {
        running = false;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        assignedRoles.clear();
        dismissedIds.clear();
        activeList.clear();
        badgeCallback = null;
        previousCount = 0;
    }

    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }

    public static boolean isSoundEnabled() {
        return soundEnabled;
    }

    public static List<NotifikasiModel> getActiveNotifications() {
        synchronized (activeList) {
            return new ArrayList<>(activeList);
        }
    }

    public static void dismiss(long id) {
        dismissedIds.add(id);
        updateActiveListAndBadge();
    }

    public static void dismissAll() {
        synchronized (activeList) {
            for (NotifikasiModel m : activeList) {
                dismissedIds.add(m.getId());
            }
        }
        updateActiveListAndBadge();
    }

    public static void refreshNow() {
        if (running && scheduler != null && !scheduler.isShutdown()) {
            scheduler.execute(NotifikasiEngine::poll);
        }
    }

    private static void loadRoles() {
        assignedRoles.clear();
        if (currentIdUser.isEmpty()) return;
        Connection conn = koneksiDB.condb();
        if (conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT lab_request, prescription_request, radiology_request, suara_bell " +
                "FROM notifikasi_user " +
                "WHERE id_user = ? OR id_user = AES_ENCRYPT(?,'nur')")) {
            ps.setString(1, currentIdUser);
            ps.setString(2, currentIdUser);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if ("true".equalsIgnoreCase(rs.getString("lab_request"))) {
                        assignedRoles.add("LAB");
                    }
                    if ("true".equalsIgnoreCase(rs.getString("prescription_request"))) {
                        assignedRoles.add("FARMASI");
                    }
                    if ("true".equalsIgnoreCase(rs.getString("radiology_request"))) {
                        assignedRoles.add("RADIOLOGI");
                    }
                    String suara = rs.getString("suara_bell");
                    if (suara != null) {
                        soundEnabled = "true".equalsIgnoreCase(suara);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("NotifikasiEngine.loadRoles: " + e.getMessage());
        }
    }

    private static void poll() {
        if (!running) return;
        List<String> targetNiks = new ArrayList<>(assignedRoles);
        if (!currentNik.isEmpty() && !targetNiks.contains(currentNik)) {
            targetNiks.add(currentNik);
        }
        if (targetNiks.isEmpty()) {
            updateBadge(0);
            return;
        }

        StringBuilder sql = new StringBuilder(
                "SELECT id, event_type, title, body, source_table, source_pk, created_at " +
                "FROM notification_queue " +
                "WHERE deleted_at IS NULL AND created_at >= (NOW() - INTERVAL 1 DAY) AND nik IN (");
        for (int i = 0; i < targetNiks.size(); i++) {
            sql.append(i == 0 ? "?" : ", ?");
        }
        sql.append(") ORDER BY id DESC LIMIT 50");

        List<NotifikasiModel> fresh = new ArrayList<>();
        Connection conn = koneksiDB.condb();
        if (conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < targetNiks.size(); i++) {
                ps.setString(i + 1, targetNiks.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    if (!dismissedIds.contains(id)) {
                        fresh.add(new NotifikasiModel(
                                id,
                                rs.getString("event_type"),
                                rs.getString("title"),
                                rs.getString("body"),
                                rs.getString("source_table"),
                                rs.getString("source_pk"),
                                rs.getString("created_at")
                        ));
                    }
                }
            }
        } catch (Exception e) {
            // Table might not exist yet or connection transient error: fail silently
            return;
        }

        if (!running) return;

        synchronized (activeList) {
            activeList.clear();
            activeList.addAll(fresh);
        }

        int count = fresh.size();
        if (count > previousCount && soundEnabled) {
            playChime();
        }
        previousCount = count;
        updateBadge(count);
    }

    private static void updateActiveListAndBadge() {
        int count;
        synchronized (activeList) {
            activeList.removeIf(m -> dismissedIds.contains(m.getId()));
            count = activeList.size();
        }
        previousCount = count;
        updateBadge(count);
    }

    private static void updateBadge(int count) {
        Consumer<Integer> cb = badgeCallback;
        if (cb != null) {
            SwingUtilities.invokeLater(() -> cb.accept(count));
        }
    }

    private static void playChime() {
        try {
            String soundPath = "./suara/notifikasi.mp3";
            File f = new File(soundPath);
            if (!f.exists()) {
                soundPath = "./suara/alarm.mp3";
                f = new File(soundPath);
            }
            if (f.exists()) {
                BackgroundMusic chime = new BackgroundMusic(soundPath);
                chime.start();
            }
        } catch (Exception e) {
            System.out.println("NotifikasiEngine.playChime: " + e.getMessage());
        }
    }
}
