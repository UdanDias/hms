package com.hms.web;

import com.hms.entities.*;
import com.hms.security.ServiceSecurity;
import jakarta.persistence.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class WebMediaService {
  @PersistenceContext private EntityManager em;
  private final ServiceSecurity security;

  @Value("${app.web.hotel-id:0}")
  private Long hotelId;

  @Value("${app.web.media-directory:./uploads/piyum}")
  private String directory;

  private Path folder() {
    return Path.of(directory).toAbsolutePath().normalize();
  }

  private WebMediaEntity get(Long id) {
    var m = em.find(WebMediaEntity.class, id);
    if (m == null || !m.getHotelId().equals(hotelId))
      throw new IllegalArgumentException("Media not found");
    return m;
  }

  private void owner(String type, Long id) {
    if (id == null) throw new IllegalArgumentException("Owner ID required");
    boolean valid =
        switch (type) {
          case "hotel" -> id.equals(hotelId);
          case "room" -> {
            var r = em.find(RoomEntity.class, id);
            yield r != null && r.getHotel().getId().equals(hotelId);
          }
          case "room-type" -> {
            var r = em.find(RoomTypeEntity.class, id);
            yield r != null && r.getHotel().getId().equals(hotelId);
          }
          default -> false;
        };
    if (!valid) throw new IllegalArgumentException("Invalid media owner");
  }

  private Map<String, Object> dto(WebMediaEntity m) {
    return Map.of(
        "id",
        m.getId(),
        "ownerType",
        m.getOwnerType(),
        "ownerId",
        m.getOwnerId(),
        "altText",
        m.getAltText(),
        "path",
        "/web/public/media/" + m.getId());
  }

  public Object list() {
    return em
        .createQuery(
            "from WebMediaEntity m where m.hotelId=:id order by m.id", WebMediaEntity.class)
        .setParameter("id", hotelId)
        .getResultList()
        .stream()
        .map(this::dto)
        .toList();
  }

  public Object upload(String type, Long id, String alt, MultipartFile file) {
    security.requireRoles("ADMIN", "MANAGER");
    owner(type, id);
    if (alt == null
        || alt.isBlank()
        || alt.length() > 200
        || file.isEmpty()
        || file.getSize() > 5 * 1024 * 1024)
      throw new IllegalArgumentException(
          "Provide descriptive alt text and a JPEG/PNG image up to 5 MB");
    try (var input = ImageIO.createImageInputStream(file.getInputStream())) {
      var readers = ImageIO.getImageReaders(input);
      if (!readers.hasNext()) throw new IllegalArgumentException("Invalid image");
      var reader = readers.next();
      BufferedImage image;
      try {
        reader.setInput(input);
        String format = reader.getFormatName();
        if (!format.equalsIgnoreCase("jpeg") && !format.equalsIgnoreCase("png"))
          throw new IllegalArgumentException("Only JPEG and PNG supported");
        long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
        if (pixels > 16000000 || reader.getWidth(0) > 6000 || reader.getHeight(0) > 6000)
          throw new IllegalArgumentException(
              "Maximum image size is 16 megapixels and 6000 pixels per side");
        image = reader.read(0);
      } finally {
        reader.dispose();
      }
      var rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
      var g = rgb.createGraphics();
      g.setColor(Color.WHITE);
      g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
      g.drawImage(image, 0, 0, null);
      g.dispose();
      Files.createDirectories(folder());
      String key = UUID.randomUUID() + ".jpg";
      Path target = folder().resolve(key);
      ImageIO.write(rgb, "jpg", target.toFile());
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
              if (status != STATUS_COMMITTED)
                try {
                  Files.deleteIfExists(target);
                } catch (IOException ignored) {
                }
            }
          });
      var media = new WebMediaEntity();
      media.setHotelId(hotelId);
      media.setOwnerType(type);
      media.setOwnerId(id);
      media.setAltText(alt);
      media.setStorageKey(key);
      em.persist(media);
      em.flush();
      return dto(media);
    } catch (IOException e) {
      throw new IllegalArgumentException("Image could not be processed", e);
    }
  }

  public byte[] bytes(Long id) {
    try {
      return Files.readAllBytes(folder().resolve(get(id).getStorageKey()));
    } catch (IOException e) {
      throw new IllegalArgumentException("Media file unavailable");
    }
  }

  public void delete(Long id) {
    security.requireRoles("ADMIN", "MANAGER");
    var media = get(id);
    Path path = folder().resolve(media.getStorageKey());
    em.remove(media);
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            try {
              Files.deleteIfExists(path);
            } catch (IOException ignored) {
            }
          }
        });
  }
}
