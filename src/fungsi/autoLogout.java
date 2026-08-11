package fungsi;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.WindowConstants;

/**
 * Auto logout saat pengguna tidak aktif.
 *
 * Menggunakan satu AWTEventListener global (mouse klik/scroll + keyboard) yang
 * dihitung sebagai aktivitas; gerakan mouse TIDAK dihitung. Setelah melewati
 * batas waktu (TIMEOUTLOGOUT, default 5 menit) muncul dialog peringatan dengan
 * hitung mundur 60 detik. Jika tidak ada aktivitas sampai hitung mundur habis,
 * callback logout dijalankan (frmUtama akan logout lalu menutup aplikasi).
 *
 * Nilai TIMEOUTLOGOUT negatif berarti fitur dinonaktifkan (tidak pernah
 * logout otomatis). Nilai 0 atau tidak valid kembali ke default 5 menit.
 *
 * @author khanza
 */
public final class autoLogout {

    private static final long TICK_MS = 5000L;
    private static final long WARNING_MS = 60000L;
    private static final long DEFAULT_TIMEOUT_MS = 5L * 60L * 1000L;

    private static volatile boolean aktif = false;
    private static volatile boolean peringatanDitampilkan = false;
    private static volatile long aktivitasTerakhir = 0L;
    private static volatile long timeoutMs = DEFAULT_TIMEOUT_MS;
    private static volatile int sisaDetik = 0;

    private static Window parent;
    private static Runnable onLogout;
    private static JDialog peringatan;
    private static JLabel lblHitungMundur;
    private static Timer timerUtama;
    private static Timer timerPeringatan;
    private static boolean listenerTerdaftar = false;

    private static final AWTEventListener listener = new AWTEventListener() {
        @Override
        public void eventDispatched(AWTEvent event) {
            if (!aktif) {
                return;
            }
            if (event instanceof MouseWheelEvent) {
                aktivitas();
            } else if (event instanceof MouseEvent) {
                int id = event.getID();
                if (id == MouseEvent.MOUSE_PRESSED || id == MouseEvent.MOUSE_RELEASED || id == MouseEvent.MOUSE_CLICKED) {
                    aktivitas();
                }
            } else if (event instanceof KeyEvent) {
                aktivitas();
            }
        }
    };

    private autoLogout() {
    }

    /**
     * Memulai pemantauan aktivitas. Idempoten: jika sudah berjalan, direset
     * dulu lalu dijalankan ulang.
     */
    public static void start(Window window, Runnable runnable) {
        stop();
        parent = window;
        onLogout = runnable;
        aktif = true;
        aktivitasTerakhir = System.currentTimeMillis();
        timeoutMs = konfigurasiTimeout();
        if (!listenerTerdaftar) {
            Toolkit.getDefaultToolkit().addAWTEventListener(listener,
                    AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
            listenerTerdaftar = true;
        }
        timerUtama = new Timer((int) TICK_MS, e -> cekIdle());
        timerUtama.start();
    }

    /**
     * Menghentikan pemantauan dan menutup dialog peringatan jika terbuka.
     */
    public static void stop() {
        aktif = false;
        if (timerUtama != null) {
            timerUtama.stop();
            timerUtama = null;
        }
        if (timerPeringatan != null) {
            timerPeringatan.stop();
            timerPeringatan = null;
        }
        peringatanDitampilkan = false;
        if (peringatan != null && peringatan.isVisible()) {
            peringatan.dispose();
        }
        onLogout = null;
        parent = null;
    }

    private static long konfigurasiTimeout() {
        try {
            long menit = Long.parseLong(koneksiDB.TIMEOUTLOGOUT().trim());
            if (menit < 0) {
                return -1L;
            }
            if (menit > 0) {
                return menit * 60L * 1000L;
            }
        } catch (Exception e) {
        }
        return DEFAULT_TIMEOUT_MS;
    }

    private static void aktivitas() {
        aktivitasTerakhir = System.currentTimeMillis();
    }

    private static void cekIdle() {
        if (!aktif || peringatanDitampilkan) {
            return;
        }
        if (timeoutMs <= 0) {
            return;
        }
        if (System.currentTimeMillis() - aktivitasTerakhir >= timeoutMs) {
            tampilPeringatan();
        }
    }

    private static void tampilPeringatan() {
        peringatanDitampilkan = true;
        sisaDetik = (int) (WARNING_MS / 1000L);
        if (peringatan == null) {
            peringatan = new JDialog(parent, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
            peringatan.setUndecorated(true);
            peringatan.setResizable(false);
            peringatan.setAlwaysOnTop(true);
            peringatan.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            peringatan.setLayout(new BorderLayout(12, 12));
            peringatan.getContentPane().setBackground(Color.WHITE);
            peringatan.getRootPane().setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50), 2));

            JLabel judul = new JLabel("Anda tidak aktif selama beberapa menit.", SwingConstants.CENTER);
            judul.setFont(new Font("Tahoma", Font.BOLD, 14));
            judul.setForeground(new Color(50, 50, 50));
            peringatan.add(judul, BorderLayout.NORTH);

            JLabel info = new JLabel("Sesi akan ditutup otomatis jika tidak ada aktivitas.", SwingConstants.CENTER);
            info.setFont(new Font("Tahoma", Font.PLAIN, 11));
            info.setForeground(new Color(50, 50, 50));
            peringatan.add(info, BorderLayout.CENTER);

            lblHitungMundur = new JLabel("", SwingConstants.CENTER);
            lblHitungMundur.setFont(new Font("Tahoma", Font.PLAIN, 12));
            lblHitungMundur.setForeground(new Color(200, 0, 0));
            peringatan.add(lblHitungMundur, BorderLayout.SOUTH);

            javax.swing.JPanel panelTombol = new javax.swing.JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
            panelTombol.setOpaque(false);
            JButton btnTetap = new JButton("Saya masih di sini");
            btnTetap.setFont(new Font("Tahoma", Font.PLAIN, 11));
            btnTetap.addActionListener(e -> batalkan());
            panelTombol.add(btnTetap);
            peringatan.add(panelTombol, BorderLayout.EAST);
        }

        lblHitungMundur.setText("Logout otomatis dalam " + sisaDetik + " detik...");
        peringatan.pack();
        peringatan.setLocationRelativeTo(parent);

        timerPeringatan = new Timer(1000, e -> {
            if (!aktif || !peringatanDitampilkan) {
                return;
            }
            if (System.currentTimeMillis() - aktivitasTerakhir < timeoutMs) {
                batalkan();
                return;
            }
            sisaDetik--;
            if (sisaDetik <= 0) {
                logout();
                return;
            }
            lblHitungMundur.setText("Logout otomatis dalam " + sisaDetik + " detik...");
        });
        timerPeringatan.start();

        peringatan.setVisible(true);
    }

    private static void batalkan() {
        if (timerPeringatan != null) {
            timerPeringatan.stop();
            timerPeringatan = null;
        }
        peringatanDitampilkan = false;
        aktivitasTerakhir = System.currentTimeMillis();
        if (peringatan != null && peringatan.isVisible()) {
            peringatan.dispose();
        }
    }

    private static void logout() {
        if (timerPeringatan != null) {
            timerPeringatan.stop();
            timerPeringatan = null;
        }
        if (timerUtama != null) {
            timerUtama.stop();
            timerUtama = null;
        }
        peringatanDitampilkan = false;
        if (peringatan != null && peringatan.isVisible()) {
            peringatan.dispose();
        }
        aktif = false;
        Runnable run = onLogout;
        onLogout = null;
        parent = null;
        if (run != null) {
            run.run();
        }
    }
}
