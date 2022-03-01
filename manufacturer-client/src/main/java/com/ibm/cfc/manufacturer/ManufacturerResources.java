package com.ibm.cfc.manufacturer;

import com.ibm.cfc.model.Asset;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Path("/assets")
public class ManufacturerResources {

    public static final String PNG = "png";
    @Inject
    ManufacturerService manufacturerService;

    @ConfigProperty(name = "app.manufacturer.id")
    private String manufacturerId;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAsset() throws IOException {
        log.info(">>> POST assets/");
        Asset asset = manufacturerService.createAsset(manufacturerId);
        return Response.status(Response.Status.CREATED).entity(asset).build();
    }

    @POST
    @Path("/{id}/qrcode")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createQRCode(@PathParam("id") String assetId) {
        log.info(">>> POST assets/{}/qrcode", assetId);
        BufferedImage qrCode = manufacturerService.createQrCode(assetId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(qrCode, PNG, baos);
            byte[] png = baos.toByteArray();
            return Response.ok(png).build();
        } catch (IOException e) {
            return Response.noContent().build();
        }
    }

    @POST
    @Path("/{id}/transfer/{newOwner}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response transferAsset(@PathParam("id") String assetId, @PathParam("newOwner") String newOwner) {
        log.info(">>> POST assets/{}/transfer/{}", assetId, newOwner);

        Asset asset = manufacturerService.transferAsset(assetId, newOwner);
        if (asset == null) return Response.status(Response.Status.NOT_FOUND).build();

        return Response.ok(asset).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findAsset(@PathParam("id") String assetId) {
        log.info(">>> GET assets/{}", assetId);

        Asset asset = manufacturerService.findAsset(assetId);
        if (asset == null) return Response.status(Response.Status.NOT_FOUND).build();

        return Response.ok(asset).build();
    }

    @GET
    @Path("/{id}/history")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAssetHistory(@PathParam("id") String assetId) {
        log.info(">>> GET assets/{}/history", assetId);

        String asset = manufacturerService.findAssetHistory(assetId);
        if (asset == null) return Response.status(Response.Status.NOT_FOUND).build();

        return Response.ok(asset).build();
    }
}