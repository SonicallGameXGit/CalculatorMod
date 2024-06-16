package me.sgx.calc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;
import org.lwjgl.glfw.GLFW;

public class CalculatorMod implements ClientModInitializer {
	public static final String MOD_ID = "calc";

	public static final KeyBinding OPEN_BINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.calc.toggle",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_O,
			"category.calc"
	));

	private static class CalcScreen extends Screen {
		public static final Identifier BACKGROUND_IDENTIFIER = Identifier.of(CalculatorMod.MOD_ID, "textures/gui/container/calculator.png");
		public static final int BACKGROUND_WIDTH = 150, BACKGROUND_HEIGHT = 216;

		private int backgroundX = 0, backgroundY = 0;
		public String message = "0";

		private boolean invalid = false;

		public final ButtonWidget AC_BUTTON = ButtonWidget.builder(
				Text.literal("AC"), button -> {
					this.invalid = false;
					this.message = "0";
				}
		).size(24, 24).build();

		public final ButtonWidget ZERO_BUTTON = ButtonWidget.builder(
				Text.literal("0"), button -> {
					this.invalid = false;

					if (this.message.length() != 1 || this.message.charAt(0) != '0') {
						this.message += "0";
					}
				}
		).size(56, 24).build();

		public final ButtonWidget COMMA_BUTTON = ButtonWidget.builder(
				Text.literal(","), button -> {
					this.invalid = false;

					if(this.message.isEmpty()) {
						return;
					}
					this.message += ",";
				}
		).size(24, 24).build();

		public final ButtonWidget PLUS_BUTTON = ButtonWidget.builder(
				Text.literal("+"), button -> {
					this.invalid = false;

					if(this.message.isEmpty()) {
						return;
					}

					char prevChar = this.message.charAt(this.message.length() - 1);
					if (Character.isDigit(prevChar)) {
						this.message += "+";
					}
				}
		).size(24, 56).build();

		public final ButtonWidget MINUS_BUTTON = ButtonWidget.builder(
				Text.literal("-"), button -> {
					this.invalid = false;

					if(this.message.isEmpty()) {
						return;
					}

					char prevChar = this.message.charAt(this.message.length() - 1);
					if (Character.isDigit(prevChar)) {
						this.message += "-";
					}
				}
		).size(24, 24).build();

		public final ButtonWidget MULTIPLY_BUTTON = ButtonWidget.builder(
				Text.literal("*"), button -> {
					this.invalid = false;

					if(this.message.isEmpty()) {
						return;
					}

					char prevChar = this.message.charAt(this.message.length() - 1);
					if (Character.isDigit(prevChar)) {
						this.message += "*";
					}
				}
		).size(24, 24).build();

		public final ButtonWidget DIVIDE_BUTTON = ButtonWidget.builder(
				Text.literal("/"), button -> {
					this.invalid = false;

					if(this.message.isEmpty()) {
						return;
					}

					char prevChar = this.message.charAt(this.message.length() - 1);
					if (Character.isDigit(prevChar)) {
						this.message += "/";
					}
				}
		).size(24, 24).build();

		public final ButtonWidget ENTER_BUTTON = ButtonWidget.builder(
				Text.literal("="), button -> {
					this.invalid = false;

					if (!Character.isDigit(this.message.charAt(this.message.length() - 1))) {
						this.message = this.message.substring(0, this.message.length() - 1);
					}

					Expression expression = new ExpressionBuilder(this.message.replace(',', '.')).build();
					ValidationResult result = expression.validate();

					if (!result.isValid()) {
						return;
					}

					double value;
					try {
						value = expression.evaluate();
					} catch (Exception e) {
						this.invalid = true;
						return;
					}

					int intValue = (int) value;

					if (Math.abs(value - intValue) < 0.0000001) {
						this.message = String.valueOf(intValue).replace('.', ',');
					} else {
						this.message = String.valueOf(value).replace('.', ',');
					}
				}
		).size(24, 56).build();

		public final ButtonWidget[] NUMBER_BUTTONS = new ButtonWidget[9];

		public CalcScreen() {
			super(Text.translatable("category.calc"));

			for (int i = 0; i < this.NUMBER_BUTTONS.length; i++) {
				int number = i + 1;
				this.NUMBER_BUTTONS[i] = ButtonWidget.builder(
						Text.literal(String.valueOf(number)), button -> {
							if (this.message.length() == 1 && this.message.charAt(0) == '0') {
								this.message = "";
							}

							this.message += String.valueOf(number);
						}
				).size(24, 24).build();
			}
		}

		@Override
		protected void init() {
			super.init();

			this.addDrawableChild(this.AC_BUTTON);
			this.addDrawableChild(this.ZERO_BUTTON);

			this.addDrawableChild(this.COMMA_BUTTON);
			this.addDrawableChild(this.DIVIDE_BUTTON);
			this.addDrawableChild(this.ENTER_BUTTON);
			this.addDrawableChild(this.MINUS_BUTTON);
			this.addDrawableChild(this.PLUS_BUTTON);
			this.addDrawableChild(this.MULTIPLY_BUTTON);

			for (ButtonWidget button : this.NUMBER_BUTTONS) {
				this.addDrawableChild(button);
			}
		}

		@Override
		public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
			super.renderBackground(context, mouseX, mouseY, delta);

			this.backgroundX = (context.getScaledWindowWidth() - BACKGROUND_WIDTH) / 2;
			this.backgroundY = (context.getScaledWindowHeight() - BACKGROUND_HEIGHT) / 2;

			context.drawTexture(
					CalcScreen.BACKGROUND_IDENTIFIER,
					this.backgroundX, this.backgroundY,
					0, 0, CalcScreen.BACKGROUND_WIDTH, CalcScreen.BACKGROUND_HEIGHT,
					CalcScreen.BACKGROUND_WIDTH, CalcScreen.BACKGROUND_HEIGHT
			);

			this.AC_BUTTON.setX(this.backgroundX + 14);
			this.AC_BUTTON.setY(this.backgroundY + 50);

			this.ZERO_BUTTON.setX(this.backgroundX + 14);
			this.ZERO_BUTTON.setY(this.backgroundY + 178);

			for (int x = 0; x < 3; x++) {
				for (int y = 0; y < 3; y++) {
					this.NUMBER_BUTTONS[x + y * 3].setX(this.backgroundX + 14 + x * 32);
					this.NUMBER_BUTTONS[x + y * 3].setY(this.backgroundY + 146 - y * 32);
				}
			}

			this.COMMA_BUTTON.setX(this.backgroundX + 78);
			this.COMMA_BUTTON.setY(this.backgroundY + 178);

			this.ENTER_BUTTON.setX(this.backgroundX + 112);
			this.ENTER_BUTTON.setY(this.backgroundY + 146);

			this.DIVIDE_BUTTON.setX(this.backgroundX + 46);
			this.DIVIDE_BUTTON.setY(this.backgroundY + 50);

			this.MULTIPLY_BUTTON.setX(this.backgroundX + 78);
			this.MULTIPLY_BUTTON.setY(this.backgroundY + 50);

			this.MINUS_BUTTON.setX(this.backgroundX + 112);
			this.MINUS_BUTTON.setY(this.backgroundY + 50);

			this.PLUS_BUTTON.setX(this.backgroundX + 112);
			this.PLUS_BUTTON.setY(this.backgroundY + 82);
		}

		@Override
		public void render(DrawContext context, int mouseX, int mouseY, float delta) {
			super.render(context, mouseX, mouseY, delta);

			context.getMatrices().peek().getPositionMatrix().translate(
					this.backgroundX + 22.0f,
					this.backgroundY + 22.0f + this.textRenderer.fontHeight * 0.125f,
					0.0f
			);
			context.getMatrices().peek().getPositionMatrix().scale(1.5f, 1.5f, 1.0f);
			context.drawText(
					this.textRenderer, this.message,
					0, 0,
					invalid ? 0xee2200 : 0x000000, false
			);
		}
	}

	private static final CalcScreen calcScreen = new CalcScreen();

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ClientPlayerEntity player = client.player;
			if (player != null && CalculatorMod.OPEN_BINDING.wasPressed()) {
				client.setScreen(calcScreen);
			}
		});
	}
}