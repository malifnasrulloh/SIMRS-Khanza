/*
 * Base code inspired by Simple Paint Application (https://github.com/GehadAshry/SimplePaintApp)
 */
package rekammedis;

import fungsi.koneksiDB;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Stack;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class LokalisCanvas extends JDialog {
    private final Connection koneksi;
    private final String noRawat;
    private final String jenisForm;
    private final Image baseImage;
    private Image savedOverlayImage;
    private final Runnable onSaveCallback;

    private DrawingPanel drawingPanel;
    private Color currentColor = Color.RED;
    private float strokeWidth = 3.0f;
    private Tool currentTool = Tool.FREE_HAND;

    public enum Tool {
        FREE_HAND,
        ERASER,
        CIRCLE
    }

    public interface Drawable {
        void draw(Graphics2D g2);
    }

    public class DrawableLine implements Drawable {
        private final List<Point> points;
        private final Color color;
        private final float width;
        private final boolean isEraser;

        public DrawableLine(List<Point> points, Color color, float width, boolean isEraser) {
            this.points = new ArrayList<>(points);
            this.color = color;
            this.width = width;
            this.isEraser = isEraser;
        }

        @Override
        public void draw(Graphics2D g2) {
            g2.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (isEraser) {
                // Eraser: use AlphaComposite.Clear to erase only from the annotation layer
                g2.setComposite(AlphaComposite.Clear);
            } else {
                g2.setComposite(AlphaComposite.SrcOver);
                g2.setColor(color);
            }

            if (points.size() == 1) {
                // Fix single-click / dot tap
                Point p = points.get(0);
                g2.drawLine(p.x, p.y, p.x, p.y);
            } else {
                for (int i = 1; i < points.size(); i++) {
                    Point p1 = points.get(i - 1);
                    Point p2 = points.get(i);
                    g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
            g2.setComposite(AlphaComposite.SrcOver);
        }
    }

    public class DrawableCircle implements Drawable {
        private final int x, y, width, height;
        private final Color color;
        private final float strokeWidth;

        public DrawableCircle(int x, int y, int width, int height, Color color, float strokeWidth) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
            this.strokeWidth = strokeWidth;
        }

        @Override
        public void draw(Graphics2D g2) {
            g2.setComposite(AlphaComposite.SrcOver);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(strokeWidth));
            g2.drawOval(x, y, width, height);
        }
    }

    public LokalisCanvas(Frame parent, boolean modal, Connection koneksi, String noRawat, String jenisForm, javax.swing.Icon defaultAnatomyIcon, Runnable onSaveCallback) {
        super(parent, modal);
        this.koneksi = koneksi;
        this.noRawat = noRawat;
        this.jenisForm = jenisForm;
        this.onSaveCallback = onSaveCallback;

        // Base image is strictly the clean default anatomy diagram
        Image tempImage = null;
        if (defaultAnatomyIcon instanceof javax.swing.ImageIcon) {
            tempImage = ((javax.swing.ImageIcon) defaultAnatomyIcon).getImage();
        } else if (defaultAnatomyIcon != null) {
            int w = defaultAnatomyIcon.getIconWidth();
            int h = defaultAnatomyIcon.getIconHeight();
            if (w > 0 && h > 0) {
                BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = bi.createGraphics();
                defaultAnatomyIcon.paintIcon(null, g, 0, 0);
                g.dispose();
                tempImage = bi;
            }
        }
        this.baseImage = tempImage;

        // Fetch existing saved drawing if available
        loadSavedOverlay();

        setTitle("Canvas / Paint Status Lokalis - " + noRawat);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
    }

    private void loadSavedOverlay() {
        try {
            String query = "select lokasi_gambar from gambar_lokalis where no_rawat=? and jenis_form=?";
            PreparedStatement psG = koneksi.prepareStatement(query);
            psG.setString(1, noRawat);
            psG.setString(2, jenisForm);
            ResultSet rsG = psG.executeQuery();
            if (rsG.next()) {
                String path = "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/lokalis/" + rsG.getString("lokasi_gambar");
                ImageIcon icon = new ImageIcon(new URL(path));
                if (icon.getImage() != null && icon.getIconWidth() > 0) {
                    this.savedOverlayImage = icon.getImage();
                }
            }
            rsG.close();
            psG.close();
        } catch (Exception e) {
            System.out.println("Error fetching saved lokalis overlay: " + e);
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Get clean image dimensions safely
        int imgW = 809;
        int imgH = 300;
        if (baseImage != null) {
            ImageIcon tracker = new ImageIcon(baseImage);
            int w = tracker.getIconWidth();
            int h = tracker.getIconHeight();
            if (w > 0 && h > 0) {
                imgW = w;
                imgH = h;
            }
        }

        drawingPanel = new DrawingPanel(imgW, imgH);
        JScrollPane scrollPane = new JScrollPane(drawingPanel);
        scrollPane.setPreferredSize(new Dimension(imgW + 20, imgH + 20));
        add(scrollPane, BorderLayout.CENTER);

        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton btnColor = new JButton("Warna");
        btnColor.setForeground(currentColor);
        btnColor.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Pilih Warna Gambar", currentColor);
            if (chosen != null) {
                currentColor = chosen;
                btnColor.setForeground(currentColor);
            }
        });
        toolBar.add(btnColor);

        toolBar.addSeparator();
        toolBar.add(new JLabel(" Ukuran: "));
        JSlider sliderWidth = new JSlider(1, 15, (int) strokeWidth);
        sliderWidth.setPreferredSize(new Dimension(80, 20));
        sliderWidth.addChangeListener(e -> strokeWidth = sliderWidth.getValue());
        toolBar.add(sliderWidth);

        toolBar.addSeparator();
        ButtonGroup group = new ButtonGroup();
        JToggleButton btnPen = new JToggleButton("Pen", true);
        btnPen.addActionListener(e -> currentTool = Tool.FREE_HAND);
        group.add(btnPen);
        toolBar.add(btnPen);

        JToggleButton btnCircle = new JToggleButton("Lingkaran");
        btnCircle.addActionListener(e -> currentTool = Tool.CIRCLE);
        group.add(btnCircle);
        toolBar.add(btnCircle);

        JToggleButton btnEraser = new JToggleButton("Penghapus");
        btnEraser.addActionListener(e -> currentTool = Tool.ERASER);
        group.add(btnEraser);
        toolBar.add(btnEraser);

        toolBar.addSeparator();
        JButton btnUndo = new JButton("Undo");
        btnUndo.addActionListener(e -> drawingPanel.undo());
        toolBar.add(btnUndo);

        JButton btnRedo = new JButton("Redo");
        btnRedo.addActionListener(e -> drawingPanel.redo());
        toolBar.add(btnRedo);

        JButton btnClear = new JButton("Clear");
        btnClear.addActionListener(e -> drawingPanel.clearCanvas());
        toolBar.add(btnClear);

        toolBar.addSeparator();
        JButton btnSave = new JButton("Simpan");
        btnSave.addActionListener(e -> saveAndUpload());
        toolBar.add(btnSave);

        JButton btnClose = new JButton("Tutup");
        btnClose.addActionListener(e -> dispose());
        toolBar.add(btnClose);

        add(toolBar, BorderLayout.NORTH);
        pack();
        setLocationRelativeTo(null);
    }

    private void saveAndUpload() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        BufferedImage composite = drawingPanel.getCompositeImage();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/lokalis/pages/upload/service.php");
            post.setHeader("Content-Type", "application/json");
            post.addHeader("username", koneksiDB.USERHYBRIDWEB());
            post.addHeader("password", koneksiDB.PASHYBRIDWEB());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(composite, "png", baos);
            byte[] fileContent = baos.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(fileContent);

            String filename = noRawat.replace("/", "-") + "_" + jenisForm + ".png";

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("file", base64Image);
            node.put("namafile", filename);
            node.put("norawat", noRawat);
            node.put("jenisform", jenisForm);
            String json = mapper.writeValueAsString(node);

            post.setEntity(new StringEntity(json));
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                String resJson = EntityUtils.toString(response.getEntity());
                JsonNode root = mapper.readTree(resJson);
                int code = root.path("metadata").path("code").asInt();
                String message = root.path("metadata").path("message").asText();
                if (code == 200) {
                    JOptionPane.showMessageDialog(this, "Gambar Lokalis berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    if (onSaveCallback != null) {
                        onSaveCallback.run();
                    }
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + message, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            System.out.println("Error saving/uploading canvas: " + e);
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private class DrawingPanel extends JPanel {
        private final int canvasWidth, canvasHeight;
        private final List<Drawable> drawables = new ArrayList<>();
        private final Stack<List<Drawable>> history = new Stack<>();
        private final Stack<List<Drawable>> redoStack = new Stack<>();

        private BufferedImage annotationLayer;

        private Point startPoint;
        private Point endPoint;
        private List<Point> currentLine = new ArrayList<>();

        public DrawingPanel(int width, int height) {
            this.canvasWidth = width;
            this.canvasHeight = height;
            setPreferredSize(new Dimension(width, height));
            setBackground(Color.WHITE);
            rebuildAnnotationLayer();
            addMouseListeners();
        }

        private void addMouseListeners() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    saveState();
                    if (currentTool == Tool.FREE_HAND || currentTool == Tool.ERASER) {
                        currentLine = new ArrayList<>();
                        currentLine.add(e.getPoint());
                    } else if (currentTool == Tool.CIRCLE) {
                        startPoint = e.getPoint();
                        endPoint = e.getPoint();
                    }
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (currentTool == Tool.FREE_HAND) {
                        drawables.add(new DrawableLine(currentLine, currentColor, strokeWidth, false));
                    } else if (currentTool == Tool.ERASER) {
                        drawables.add(new DrawableLine(currentLine, currentColor, strokeWidth, true));
                    } else if (currentTool == Tool.CIRCLE && startPoint != null && endPoint != null) {
                        int x = Math.min(startPoint.x, e.getX());
                        int y = Math.min(startPoint.y, e.getY());
                        int w = Math.abs(startPoint.x - e.getX());
                        int h = Math.abs(startPoint.y - e.getY());
                        drawables.add(new DrawableCircle(x, y, w, h, currentColor, strokeWidth));
                    }
                    currentLine.clear();
                    startPoint = null;
                    endPoint = null;
                    rebuildAnnotationLayer();
                    repaint();
                }
            });

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (currentTool == Tool.FREE_HAND || currentTool == Tool.ERASER) {
                        currentLine.add(e.getPoint());
                    } else if (currentTool == Tool.CIRCLE) {
                        endPoint = e.getPoint();
                    }
                    repaint();
                }
            });
        }

        /**
         * Rebuilds the annotation overlay layer.
         * If an existing saved drawing was loaded, it is drawn as the initial layer content,
         * followed by all active drawables (pen strokes, circles, erasers).
         */
        private void rebuildAnnotationLayer() {
            annotationLayer = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = annotationLayer.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (savedOverlayImage != null) {
                g2.drawImage(savedOverlayImage, 0, 0, canvasWidth, canvasHeight, null);
            }

            for (Drawable drawable : drawables) {
                drawable.draw(g2);
            }
            g2.dispose();
        }

        private void saveState() {
            history.push(new ArrayList<>(drawables));
            redoStack.clear();
        }

        public void undo() {
            if (!history.isEmpty()) {
                redoStack.push(new ArrayList<>(drawables));
                drawables.clear();
                drawables.addAll(history.pop());
                rebuildAnnotationLayer();
                repaint();
            }
        }

        public void redo() {
            if (!redoStack.isEmpty()) {
                history.push(new ArrayList<>(drawables));
                drawables.clear();
                drawables.addAll(redoStack.pop());
                rebuildAnnotationLayer();
                repaint();
            }
        }

        public void clearCanvas() {
            saveState();
            drawables.clear();
            savedOverlayImage = null; // Reset saved overlay as well
            rebuildAnnotationLayer();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            // Layer 1: Draw clean base anatomy image (read-only, never modified)
            if (baseImage != null) {
                g2.drawImage(baseImage, 0, 0, canvasWidth, canvasHeight, this);
            }

            // Layer 2: Draw annotation overlay layer on top
            g2.drawImage(annotationLayer, 0, 0, null);

            // Layer 3: Draw live drag preview
            g2.setComposite(AlphaComposite.SrcOver);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (!currentLine.isEmpty()) {
                if (currentTool == Tool.ERASER) {
                    g2.setColor(new Color(255, 255, 255, 128));
                } else {
                    g2.setColor(currentColor);
                }
                g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                if (currentLine.size() == 1) {
                    Point p = currentLine.get(0);
                    g2.drawLine(p.x, p.y, p.x, p.y);
                } else {
                    for (int i = 1; i < currentLine.size(); i++) {
                        Point p1 = currentLine.get(i - 1);
                        Point p2 = currentLine.get(i);
                        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                    }
                }
            }

            if (currentTool == Tool.CIRCLE && startPoint != null && endPoint != null) {
                g2.setColor(currentColor);
                g2.setStroke(new BasicStroke(strokeWidth));
                int x = Math.min(startPoint.x, endPoint.x);
                int y = Math.min(startPoint.y, endPoint.y);
                int w = Math.abs(startPoint.x - endPoint.x);
                int h = Math.abs(startPoint.y - endPoint.y);
                g2.drawOval(x, y, w, h);
            }
        }

        public BufferedImage getCompositeImage() {
            BufferedImage image = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            if (baseImage != null) {
                g2.drawImage(baseImage, 0, 0, canvasWidth, canvasHeight, null);
            } else {
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, canvasWidth, canvasHeight);
            }
            g2.setComposite(AlphaComposite.SrcOver);
            g2.drawImage(annotationLayer, 0, 0, null);
            g2.dispose();
            return image;
        }
    }
}
