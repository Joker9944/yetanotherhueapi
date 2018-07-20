package com.github.zeroone3010.yahueapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonInclude(Include.NON_NULL)
public final class RoomAction {
  private final Boolean on;
  private final Integer bri;
  private final List<Float> xy;

  /**
   * @param on         Set to {@code true} to turn all lights on. Set to {@code false} to turn all lights off.
   * @param brightness A value from {@code 0} (minimum brightness) to {@code 254} (maximum brightness).
   */
  public RoomAction(final boolean on,
                    final int brightness) {
    this.on = on;
    this.bri = brightness;
    this.xy = null;
  }

  /**
   * @param on       Set to {@code true} to turn all lights on. Set to {@code false} to turn all lights off.
   * @param hexColor A hexadecimal color value to be set for the lights -- for example, {@code 00FF00} for green.
   */
  @JsonCreator
  public RoomAction(@JsonProperty("on") final boolean on,
                    @JsonProperty("color") final String hexColor) {
    this.on = on;
    final XAndYAndBrightness xAndYAndBrightness = Optional.ofNullable(hexColor)
        .map(hex -> Integer.parseInt(hex, 16))
        .map(Color::new)
        .map(RoomAction::rgbToXy)
        .orElse(null);
    final List<Float> xyColor = new ArrayList<>();
    xyColor.add(xAndYAndBrightness.getX());
    xyColor.add(xAndYAndBrightness.getY());
    this.xy = xyColor;
    this.bri = xAndYAndBrightness.getBrightness();
  }

  public Boolean getOn() {
    return on;
  }

  public Integer getBri() {
    return bri;
  }

  public List<Float> getXy() {
    return xy;
  }

  private static XAndYAndBrightness rgbToXy(final Color color) {
    final float red = color.getRed() / 255f;
    final float green = color.getGreen() / 255f;
    final float blue = color.getBlue() / 255f;
    final double r = gammaCorrection(red);
    final double g = gammaCorrection(green);
    final double b = gammaCorrection(blue);
    final double rgbX = r * 0.664511f + g * 0.154324f + b * 0.162028f;
    final double rgbY = r * 0.283881f + g * 0.668433f + b * 0.047685f;
    final double rgbZ = r * 0.000088f + g * 0.072310f + b * 0.986039f;
    final float x = (float) (rgbX / (rgbX + rgbY + rgbZ));
    final float y = (float) (rgbY / (rgbX + rgbY + rgbZ));
    return new XAndYAndBrightness(x, y, (int) (rgbY * 255f));
  }

  private static double gammaCorrection(float component) {
    return (component > 0.04045f) ? Math.pow((component + 0.055f) / (1.0f + 0.055f), 2.4f) : (component / 12.92f);
  }

  private static class XAndYAndBrightness {
    final float x;
    final float y;
    final int brightness;

    XAndYAndBrightness(final float x, final float y, final int brightness) {
      this.x = x;
      this.y = y;
      this.brightness = brightness;
    }

    float getX() {
      return x;
    }

    float getY() {
      return y;
    }

    int getBrightness() {
      return brightness;
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this);
    }
  }
}
