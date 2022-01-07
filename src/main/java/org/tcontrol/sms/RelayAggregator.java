package org.tcontrol.sms;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.config.props.RelayAggregatorConfig;

@Component
@Slf4j
public class RelayAggregator implements IRelayAggregator {

  private RelayAggregatorConfig relayAggregatorConfig;
  private IRelayController relayController;

  private Map<String, List<Boolean>> currentState = new HashMap<>();

  public RelayAggregator(RelayAggregatorConfig relayAggregatorConfig,
      IRelayController relayController) {
    this.relayAggregatorConfig = relayAggregatorConfig;
    this.relayController = relayController;
  }

  @Scheduled(cron = "${relay-aggregator.schedule}")
  public void cleanCurrentState() {
    currentState.clear();
  }

  @Override
  public PinState turnOnRelay(Pin controlPin) {
    Integer n = relayAggregatorConfig.getPinAggregates().get(controlPin.getName());
    if (n == null) {
      relayController.turnOnRelay(controlPin);
      log.info("delegated turnOnRelay: {}", controlPin.getName());
    } else {
      List<Boolean> pinStates = currentState
          .computeIfAbsent(controlPin.getName(), k -> new ArrayList<>());
      pinStates.add(true);
      log.info("aggregating turnOnRelay: {}  size {}", controlPin.getName(), pinStates.size());
      if (pinStates.size() == n) {
        relayController.turnOnRelay(controlPin);
        log.info("aggregated delegation turnOnRelay: {}", controlPin.getName());
      }
    }
    return null;
  }

  @Override
  public PinState turnOffRelay(Pin controlPin) {
    Integer n = relayAggregatorConfig.getPinAggregates().get(controlPin.getName());
    if (n == null) {
      relayController.turnOffRelay(controlPin);
      log.info("delegated turnOffRelay: {}", controlPin.getName());
      return PinState.LOW;
    } else {
      List<Boolean> pinStates = currentState
          .computeIfAbsent(controlPin.getName(), k -> new ArrayList<>());
      pinStates.add(false);
      log.info("aggregating turnOffRelay: {}  size {}", controlPin.getName(), pinStates.size());
      if (pinStates.size() == n) {
        boolean or = pinStates.stream().reduce(false, (a, b) -> a || b);
        if (!or) {
          relayController.turnOffRelay(controlPin);
          log.info("aggregated delegation turnOffRelay: {}", controlPin.getName());
          return PinState.LOW;
        } else {
          relayController.turnOnRelay(controlPin);
          log.info("aggregated delegation turnOnRelay: {}", controlPin.getName());
          return PinState.HIGH;
        }
      }
    }
    return null;
  }

  @Override
  public PinState getPinState(Pin controlPin) {
    Integer n = relayAggregatorConfig.getPinAggregates().get(controlPin.getName());
    if (n == null) {
      log.info("delegated getPinState: {}", controlPin.getName());
      return relayController.getPinState(controlPin);
    } else {
      List<Boolean> pinStates = currentState.get(controlPin.getName());
      if (pinStates != null && pinStates.size() == n) {
        boolean or = pinStates.stream().reduce(false, (a, b) -> a || b);
        PinState pinState = or ? PinState.HIGH : PinState.LOW;
        log.info("aggregated delegation getPinState: {}, value: {}", controlPin.getName(),
            pinState);
        return pinState;
      }
    }
    log.info("not aggregated getPinState: {}", controlPin.getName());
    return null;
  }
}
