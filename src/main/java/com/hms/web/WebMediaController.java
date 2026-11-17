package com.hms.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/web")
@RequiredArgsConstructor
public class WebMediaController {
  private final WebMediaService service;

  @GetMapping("/public/media")
  public Object list() {
    return service.list();
  }

  @GetMapping(value = "/public/media/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
  public ResponseEntity<byte[]> image(@PathVariable Long id) {
    return ResponseEntity.ok()
        .header("X-Content-Type-Options", "nosniff")
        .cacheControl(CacheControl.noCache())
        .body(service.bytes(id));
  }

  @PostMapping(value = "/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Object upload(
      @RequestParam String ownerType,
      @RequestParam Long ownerId,
      @RequestParam String altText,
      @RequestParam MultipartFile file) {
    return service.upload(ownerType, ownerId, altText, file);
  }

  @DeleteMapping("/media/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
