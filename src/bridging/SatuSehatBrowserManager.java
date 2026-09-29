package bridging;

import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import javax.swing.JOptionPane;
import me.friwi.jcefmaven.CefAppBuilder;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;

/**
 * Central manager for Chromium (JCEF) lifecycle and browser launch fallbacks across SIMRS Khanza.
 * Guarantees process-wide singleton initialization, prevents native double-initialization crashes,
 * and supplies driver-safe flags for Linux OpenGL/VSync stability.
 */
public class SatuSehatBrowserManager {
    private static CefApp cefApp;
    private static boolean initialized = false;
    private static boolean failed = false;
    private static String failReason = "";

    public static synchronized CefApp getCefApp() {
        if (CefApp.getState() == CefApp.CefAppState.INITIALIZED) {
            try {
                cefApp = CefApp.getInstance();
                initialized = true;
                return cefApp;
            } catch (Throwable t) {
                failed = true;
                failReason = t.getMessage();
                return null;
            }
        }

        if (initialized && cefApp != null) {
            return cefApp;
        }
        if (failed) {
            return null;
        }

        try {
            File installDir = new File("jcef-bundle");
            // Self-healing: if jcef-bundle was interrupted or lacks install.lock, clean up temp files
            cleanIncompleteInstallDir(installDir);

            CefAppBuilder builder = new CefAppBuilder();
            builder.setInstallDir(installDir);

            // Configure cache directory
            File cacheDir = new File("cache" + File.separator + "jcef");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            builder.getCefSettings().cache_path = cacheDir.getAbsolutePath();
            builder.getCefSettings().windowless_rendering_enabled = false;

            // Stability flags for Linux and Windows: disable GPU vsync/hardware acceleration conflicts
            builder.addJcefArgs(
                "--disable-gpu",
                "--disable-gpu-compositing",
                "--disable-gpu-vsync",
                "--disable-software-rasterizer",
                "--disable-dev-shm-usage",
                "--no-sandbox"
            );

            cefApp = builder.build();
            initialized = true;
            System.out.println("SatuSehatBrowserManager: JCEF Chromium App initialized successfully!");
            return cefApp;
        } catch (Throwable t) {
            failed = true;
            failReason = t.getMessage();
            System.err.println("SatuSehatBrowserManager: JCEF initialization failed (" + t.getMessage() + "), using App Mode fallback.");
            return null;
        }
    }

    private static void cleanIncompleteInstallDir(File installDir) {
        if (installDir == null || !installDir.exists()) {
            return;
        }
        try {
            File[] files = installDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().endsWith(".temp") || f.getName().endsWith(".tmp")) {
                        try {
                            f.delete();
                            System.out.println("SatuSehatBrowserManager: Removed incomplete temporary file: " + f.getName());
                        } catch (Exception ignored) {}
                    }
                }
            }
            File installLock = new File(installDir, "install.lock");
            File metaJson = new File(installDir, "build_meta.json");
            // If install.lock or build_meta.json is missing, previous extraction failed
            if (!installLock.exists() || !metaJson.exists()) {
                System.out.println("SatuSehatBrowserManager: Incomplete or broken JCEF bundle detected. Resetting for clean extraction...");
                deleteRecursively(installDir);
                installDir.mkdirs();
            }
        } catch (Throwable t) {
            System.err.println("SatuSehatBrowserManager: Error during installDir cleanup: " + t.getMessage());
        }
    }

    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }

    public static boolean isJcefAvailable() {
        return getCefApp() != null;
    }

    public static String getFailReason() {
        return failReason;
    }

    public static void safeCloseBrowser(CefBrowser browser) {
        if (browser != null) {
            try {
                browser.close(false);
            } catch (Throwable ignored) {}
        }
    }

    public static void safeTerminateProcess(Process process) {
        if (process != null) {
            try {
                if (process.isAlive()) {
                    process.destroy();
                    try {
                        if (!process.waitFor(500, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                            process.destroyForcibly();
                        }
                    } catch (InterruptedException e) {
                        process.destroyForcibly();
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (Throwable ignored) {}
        }
    }

    public static Process launchAppModeOrBrowser(String url, Component parent) {
        if (url == null || url.trim().isEmpty()) {
            if (parent != null) {
                JOptionPane.showMessageDialog(parent, "Tautan URL belum tersedia.");
            }
            return null;
        }

        Process process = null;
        String os = System.getProperty("os.name", "").toLowerCase();

        try {
            if (os.contains("win")) {
                String localAppData = System.getenv("LOCALAPPDATA");
                String progFiles = System.getenv("PROGRAMFILES");
                String progFilesX86 = System.getenv("PROGRAMFILES(X86)");

                String[] candidatePaths = {
                    localAppData + "\\Google\\Chrome\\Application\\chrome.exe",
                    progFiles + "\\Google\\Chrome\\Application\\chrome.exe",
                    progFilesX86 + "\\Google\\Chrome\\Application\\chrome.exe",
                    localAppData + "\\Microsoft\\Edge\\Application\\msedge.exe",
                    progFiles + "\\Microsoft\\Edge\\Application\\msedge.exe",
                    progFilesX86 + "\\Microsoft\\Edge\\Application\\msedge.exe",
                    progFiles + "\\BraveSoftware\\Brave-Browser\\Application\\brave.exe"
                };

                for (String path : candidatePaths) {
                    File exe = new File(path);
                    if (exe.exists() && exe.canExecute()) {
                        process = new ProcessBuilder(path, "--app=" + url, "--window-size=1100,750").start();
                        break;
                    }
                }
            } else if (os.contains("mac")) {
                String[] macApps = {
                    "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
                    "/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge",
                    "/Applications/Brave Browser.app/Contents/MacOS/Brave Browser"
                };
                for (String bin : macApps) {
                    File exe = new File(bin);
                    if (exe.exists()) {
                        process = new ProcessBuilder(bin, "--app=" + url, "--window-size=1100,750").start();
                        break;
                    }
                }
            } else if (os.contains("linux") || os.contains("nix")) {
                String[] linuxBins = {"google-chrome", "google-chrome-stable", "chromium", "chromium-browser", "microsoft-edge", "brave-browser"};
                for (String bin : linuxBins) {
                    try {
                        Process p = new ProcessBuilder("which", bin).start();
                        if (p.waitFor() == 0) {
                            process = new ProcessBuilder(bin, "--app=" + url, "--window-size=1100,750").start();
                            break;
                        }
                    } catch (Exception ignored) {}
                }
            }
        } catch (Throwable t) {
            System.err.println("Gagal meluncurkan browser App Mode: " + t.getMessage());
        }

        if (process == null) {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                }
            } catch (Throwable t) {
                if (parent != null) {
                    JOptionPane.showMessageDialog(parent, "Gagal membuka browser eksternal: " + t.getMessage());
                }
            }
        }
        return process;
    }

    public static boolean openInAppModeOrBrowser(String url, Component parent) {
        Process p = launchAppModeOrBrowser(url, parent);
        return p != null || (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE));
    }
}
