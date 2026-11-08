package bo.babylon_gate.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import javax.imageio.ImageIO;

public class ImageUtils {

    public static String procesarYComprimirBase64(String base64Original, int maxWidth, int maxHeight) {
        try {
            if (base64Original == null || base64Original.trim().isEmpty()) return null;

            String[] parts = base64Original.split(",");
            String imageString = parts.length > 1 ? parts[1] : parts[0];
            String prefix = parts.length > 1 ? parts[0] + "," : "data:image/jpeg;base64,";

            if (imageString.trim().isEmpty()) return null;

            byte[] imageBytes = Base64.getDecoder().decode(imageString);
            ByteArrayInputStream is = new ByteArrayInputStream(imageBytes);
            BufferedImage originalImage = ImageIO.read(is);

            if (originalImage == null) return base64Original;

            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            if (width <= maxWidth && height <= maxHeight) {
                return base64Original;
            }

            double ratio = Math.min((double) maxWidth / width, (double) maxHeight / height);
            int newWidth = (int) (width * ratio);
            int newHeight = (int) (height * ratio);

            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, newWidth, newHeight);
            g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g.dispose();

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "jpg", os); // Forzamos compresión a JPG

            return prefix + Base64.getEncoder().encodeToString(os.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return base64Original;
        }
    }
}