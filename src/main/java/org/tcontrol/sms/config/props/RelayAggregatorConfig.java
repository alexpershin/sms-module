package org.tcontrol.sms.config.props;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RelayAggregatorConfig {
    private Map<String,Integer> pinAggregates = new HashMap<>();
}
