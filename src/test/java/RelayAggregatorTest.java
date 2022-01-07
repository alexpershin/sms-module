import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.tcontrol.sms.IRelayController;
import org.tcontrol.sms.RelayAggregator;
import org.tcontrol.sms.config.props.RelayAggregatorConfig;

public class RelayAggregatorTest {

  public static final String GPIO_0 = "GPIO 0";
  public static final String GPIO_1 = "GPIO 1";
  private RelayAggregator relayAggregator;
  private IRelayController relayController;

  @Before
  public void before() {
    RelayAggregatorConfig relayAggregatorConfig = new RelayAggregatorConfig();
    relayAggregatorConfig.getPinAggregates().put(GPIO_0, 3);
    relayController = mock(IRelayController.class);
    relayAggregator = new RelayAggregator(relayAggregatorConfig, relayController);
  }

  @Test
  public void test1() {
    Pin pin = RaspiPin.getPinByName(GPIO_0);
    relayAggregator.turnOnRelay(pin);
    Mockito.verify(relayController, never()).turnOnRelay(pin);
  }

  @Test
  public void test2() {
    Pin pin = RaspiPin.getPinByName(GPIO_0);
    relayAggregator.turnOnRelay(pin);
    relayAggregator.turnOnRelay(pin);
    Mockito.verify(relayController, never()).turnOnRelay(pin);
    assertNull(relayAggregator.getPinState(pin));
  }

  @Test
  public void test3() {
    Pin pin = RaspiPin.getPinByName(GPIO_0);
    relayAggregator.turnOnRelay(pin);
    relayAggregator.turnOnRelay(pin);
    relayAggregator.turnOnRelay(pin);
    Mockito.verify(relayController).turnOnRelay(pin);
  }

  @Test
  public void testOneOn() {
    Pin pin = RaspiPin.getPinByName(GPIO_0);
    relayAggregator.turnOffRelay(pin);
    relayAggregator.turnOnRelay(pin);
    relayAggregator.turnOffRelay(pin);
    Mockito.verify(relayController).turnOnRelay(pin);
    Mockito.verify(relayController, times(0)).turnOffRelay(pin);
  }

  @Test
  public void test4() {
    Pin pin = RaspiPin.getPinByName(GPIO_0);
    for (int i = 0; i < 4; i++) {
      relayAggregator.turnOnRelay(pin);
    }
    Mockito.verify(relayController, times(1)).turnOnRelay(pin);
  }

  @Test
  public void test5() {
    Pin pin = RaspiPin.getPinByName(GPIO_0);
    for (int i = 0; i < 3; i++) {
      relayAggregator.turnOnRelay(pin);
    }
    relayAggregator.cleanCurrentState();
    for (int i = 0; i < 3; i++) {
      relayAggregator.turnOnRelay(pin);
    }
    Mockito.verify(relayController, times(2)).turnOnRelay(pin);

    assertTrue(relayAggregator.getPinState(pin).isHigh());
  }

  @Test
  public void testOff() {
    Pin pin = RaspiPin.getPinByName(GPIO_0);
    for (int i = 0; i < 3; i++) {
      relayAggregator.turnOffRelay(pin);
    }
    relayAggregator.cleanCurrentState();
    for (int i = 0; i < 3; i++) {
      relayAggregator.turnOffRelay(pin);
    }
    Mockito.verify(relayController, times(2)).turnOffRelay(pin);

    assertTrue(relayAggregator.getPinState(pin).isLow());
  }

  @Test
  public void testOnNotAggregated() {
    Pin pin = RaspiPin.getPinByName(GPIO_1);
    relayAggregator.turnOnRelay(pin);
    Mockito.verify(relayController, times(1)).turnOnRelay(pin);
  }
  @Test
  public void testOffNotAggregated() {
    Pin pin = RaspiPin.getPinByName(GPIO_1);
    relayAggregator.turnOffRelay(pin);
    Mockito.verify(relayController, times(1)).turnOffRelay(pin);
  }
  @Test
  public void testStatusNotAggregated() {
    Pin pin = RaspiPin.getPinByName(GPIO_1);
    relayAggregator.getPinState(pin);
    Mockito.verify(relayController, times(1)).getPinState(pin);
  }
}
