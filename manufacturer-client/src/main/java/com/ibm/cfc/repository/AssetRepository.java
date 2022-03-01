package com.ibm.cfc.repository;

import com.ibm.cfc.model.Asset;

public interface AssetRepository {

    void createAsset(Asset asset);

    Asset findAsset(String assetid);

    void updateAsset(Asset asset);

    String getAssetHistory(String assetid);
}
