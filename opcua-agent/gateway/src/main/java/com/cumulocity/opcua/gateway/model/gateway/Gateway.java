package com.cumulocity.opcua.gateway.model.gateway;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.gateway.model.core.Alarms;
import com.cumulocity.opcua.gateway.model.core.Credentials;
import com.cumulocity.opcua.gateway.model.core.HasKey;
import com.cumulocity.opcua.gateway.model.core.HasTenant;
import com.cumulocity.opcua.gateway.repository.core.PersistableType;
import com.cumulocity.opcua.platform.model.annotation.ExtensibleRepresentationView;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Wither;

import javax.annotation.Nullable;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Data
@Builder(builderMethodName = "gateway")
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = "name")
@EqualsAndHashCode(of = {"tenant", "name", "password", "id", "currentDeviceIds"})
@PersistableType(value = "Gateway")
@ExtensibleRepresentationView(fragment = Gateway.c8y_OPCUAGateway, type = Gateway.TYPE)
public class Gateway implements HasKey, HasTenant, Credentials {

    public static final String TYPE = "c8y_OPCUA";
    public static final String c8y_OPCUAGateway = "c8y_OPCUAGateway";
    public static final String c8y_SetRegister = "c8y_SetRegister";

    @Wither
    @Nullable
    private String tenant;

    @Wither
    @Nullable
    private String name;

    @Wither
    @Nullable
    private String password;

    @Wither
    @Nullable
    private GId id;

    @Wither
    private Alarms alarms = new Alarms();

    @Wither
    @Nullable
    private List<GId> currentDeviceIds;

    @Wither
    @Nullable
    @JsonProperty
    private String url;

    @Nullable
    @JsonProperty
    private String applicationUri;

    @Nullable
    @JsonProperty
    private String productUri;

    @Nullable
    @JsonProperty("transmitRate")
    private Long transmitRateInSeconds;

    @Wither
    @Nullable
    @JsonProperty("pollingRate")
    private Double pollingRateInSeconds;

    @Nullable
    @JsonProperty
    private String securityMode;

    @Nullable
    @JsonProperty
    private String securityPolicy;

    @Nullable
    @JsonProperty
    private String messageSecurityMode;

    @Nullable
    @JsonProperty
    private String userIdentityType;

    @Nullable
    @JsonProperty
    private String userIdentityName;

    @Nullable
    @JsonProperty
    private String userIdentityPassword;

//    when there is 10 number of retries then we assume that gateway is removed
    @Nullable
    @JsonProperty
    private volatile int numberOfRetries = 0;

    @JsonIgnore
    public boolean isUrlValid() {
        return isNotBlank(getUrl());
    }

    public Alarms getAlarms() {
        if (alarms == null) {
            alarms = new Alarms();
        }
        return alarms;
    }

    @JsonIgnore
    public int increaseNumberOfRetries() {
        return ++ numberOfRetries;
    }
}

