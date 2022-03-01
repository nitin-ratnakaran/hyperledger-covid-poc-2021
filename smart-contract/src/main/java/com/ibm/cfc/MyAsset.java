/*
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cfc;

import com.ibm.cfc.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType()
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyAsset {

    @Property()
    public String id;
    @Property()
    public String manufacturer;
    @Property()
    public String manufactureDateTime;
    @Property()
    public String expiryDate;
    @Property()
    public String status;
    @Property()
    public String owner;

    public static MyAsset fromJSONString(String json) {
        return JsonUtils.getGenson().deserialize(json, MyAsset.class);
    }

    public String toJSONString() {
        return JsonUtils.getGenson().serialize(this);
    }
}
