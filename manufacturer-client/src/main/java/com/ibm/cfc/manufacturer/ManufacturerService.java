package com.ibm.cfc.manufacturer;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.ibm.cfc.model.Asset;
import com.ibm.cfc.model.Status;
import com.ibm.cfc.repository.AssetRepository;
import com.ibm.cfc.utils.QRCodeUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class ManufacturerService {

    public static final String FWD_SLASH = "/";
    public static final String PNG = "png";
    @Inject
    AssetRepository assetRepository;

    @ConfigProperty(name = "app.vaccine.expiry.duration.in.weeks")
    Integer expiryDurationInWeeks;

    @ConfigProperty(name = "app.qrcode.url")
    String appQrCodeUrl;

    public Asset createAsset(String manufacturerId) throws IOException {
        Asset asset = Asset.builder()
                .id(manufacturerId + "-" + UUID.randomUUID())
                .manufacturer(manufacturerId)
                .manufactureDateTime(LocalDateTime.now().toString())
                .expiryDate(LocalDateTime.now().plusDays(expiryDurationInWeeks * 7L).toString())
                .status(Status.PRODUCED.name())
                .owner(manufacturerId)
                .build();
        assetRepository.createAsset(asset);
        return asset;
    }

    public BufferedImage createQrCode(String assetId) {
        try {
            BitMatrix bitMatrix = QRCodeUtils.qrCodeWriter().encode(qrUrlOf(assetId), BarcodeFormat.QR_CODE, 200, 200);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (WriterException ignored) {
            return null;
        }
    }

    private String qrUrlOf(String assetId) {
        return appQrCodeUrl.replace("ID", assetId);
    }

    public Asset transferAsset(String assetId, String newOwner) {
        Asset asset = findAsset(assetId);
        if (asset == null) return null;

        asset.setOwner(newOwner);
        asset.setStatus(Status.TRANSIT.name());
        assetRepository.updateAsset(asset);
        return asset;
    }

    public Asset findAsset(String assetId) {
        return assetRepository.findAsset(assetId);
    }

    public String findAssetHistory(String assetId) {
        return assetRepository.getAssetHistory(assetId);
    }
}
