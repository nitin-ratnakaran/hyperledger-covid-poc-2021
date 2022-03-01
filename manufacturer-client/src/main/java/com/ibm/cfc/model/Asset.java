package com.ibm.cfc.model;

import com.ibm.cfc.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Asset {
    public String id;
    public String manufacturer;
    public String manufactureDateTime;
    public String expiryDate;
    public String status;
    public String owner;

    public static Asset fromJSONString(String json) {
        return JsonUtils.getGenson().deserialize(json, Asset.class);
    }
}
