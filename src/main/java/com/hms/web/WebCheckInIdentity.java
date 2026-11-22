package com.hms.web;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Separate encrypted identity per reservation, never embedded in guest-access snapshots. */
@Component
public class WebCheckInIdentity {
  private final JdbcTemplate jdbc;
  private final SecretKeySpec key;
  public WebCheckInIdentity(JdbcTemplate jdbc, @Value("${app.web.identity-key}") String encoded) {
    this.jdbc=jdbc;
    byte[] bytes=Base64.getDecoder().decode(encoded);
    if(bytes.length!=32) throw new IllegalArgumentException("IDENTITY_KEY must decode to 32 bytes");
    key=new SecretKeySpec(bytes,"AES");
  }
  public void save(long id, WebRequests.Identity input) {
    try {
      byte[] iv=new byte[12]; new SecureRandom().nextBytes(iv);
      Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE,key,new GCMParameterSpec(128,iv));
      cipher.updateAAD(Long.toString(id).getBytes(StandardCharsets.UTF_8));
      byte[] ciphertext=cipher.doFinal(input.documentNumber().trim().getBytes(StandardCharsets.UTF_8));
      byte[] payload=new byte[iv.length+ciphertext.length];
      System.arraycopy(iv,0,payload,0,iv.length); System.arraycopy(ciphertext,0,payload,iv.length,ciphertext.length);
      jdbc.update("INSERT INTO web_checkin_identity(reservation_id,document_type,encrypted_number) VALUES(?,?,?) ON DUPLICATE KEY UPDATE document_type=VALUES(document_type),encrypted_number=VALUES(encrypted_number)", id,input.documentType(),payload);
    } catch(java.security.GeneralSecurityException e) { throw new IllegalStateException("Unable to protect check-in document",e); }
  }
  public Map<String,String> read(long id) {
    var rows=jdbc.query("SELECT document_type,encrypted_number FROM web_checkin_identity WHERE reservation_id=?",(rs,n)->{
      try {
        byte[] payload=rs.getBytes("encrypted_number"); Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE,key,new GCMParameterSpec(128,Arrays.copyOf(payload,12)));
        cipher.updateAAD(Long.toString(id).getBytes(StandardCharsets.UTF_8));
        return Map.of("documentType",rs.getString("document_type"),"documentNumber",new String(cipher.doFinal(Arrays.copyOfRange(payload,12,payload.length)),StandardCharsets.UTF_8));
      } catch(java.security.GeneralSecurityException e) {throw new IllegalStateException("Check-in document key does not match stored data",e);}
    },id);
    return rows.isEmpty()?null:rows.get(0);
  }
}
