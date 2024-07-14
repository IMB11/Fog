package dev.imb11.fog.client.util.math;

import net.minecraft.util.math.MathHelper;

// TODO: Remove unused methods
public class InterpolatedValue {
	private static final float DEFAULT_INTERPOLATION_SPEED = 0.05f;

	private final float interpolationSpeed;

	private float defaultValue;
	private float previousValue;
	private float currentValue;

	public InterpolatedValue(float defaultValue, float interpolationSpeed) {
		this.defaultValue = defaultValue;
		this.currentValue = defaultValue;
		this.interpolationSpeed = interpolationSpeed;
	}

	public InterpolatedValue(float defaultValue) {
		this(defaultValue, DEFAULT_INTERPOLATION_SPEED);
	}

	public void set(float value) {
		this.previousValue = this.currentValue;
		this.currentValue = value;
	}

	public void set(double value) {
		this.set((float) value);
	}

	public void setDefaultValue(float value) {
		this.defaultValue = value;
	}

	public void setDefaultValue(double value) {
		this.setDefaultValue((float) value);
	}

	public void interpolate(float value, float interpolationSpeed) {
		this.set(Float.isNaN(value) ? MathHelper.lerp(interpolationSpeed, currentValue, defaultValue) : MathHelper.lerp(interpolationSpeed,
				currentValue, value
		));
	}

	public void interpolate(double value, float interpolationSpeed) {
		this.interpolate((float) value, interpolationSpeed);
	}

	public void interpolate(float value) {
		this.interpolate(value, this.interpolationSpeed);
	}

	public void interpolate(double value) {
		this.interpolate((float) value);
	}

	public void interpolate() {
		this.set(MathHelper.lerp(interpolationSpeed, currentValue, defaultValue));
	}

	public float get(float deltaTick) {
		return MathHelper.lerp(deltaTick, previousValue, currentValue);
	}

	public float getDefaultValue() {
		return this.defaultValue;
	}

	public void resetTo(float initialFogStart) {
		this.defaultValue = initialFogStart;
		this.currentValue = initialFogStart;
	}
}
