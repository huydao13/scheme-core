package com.twint.scheme.pairing.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import org.springframework.stereotype.Service;

@Service
public class QrCodeService {
  public String generateQrCodeBase64(String content) {
    try {
      QRCodeWriter writer = new QRCodeWriter();
      BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 200, 200);

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      MatrixToImageWriter.writeToStream(matrix, "PNG", out);

      String base64 = Base64.getEncoder().encodeToString(out.toByteArray());
      return "data:image/png;base64," + base64;
    } catch (WriterException | IOException e) {
      throw new RuntimeException("Failed to generate QR code", e);
    }
  }
}
