package com.illusivesoulworks.spectrelib.config.client.screen;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.illusivesoulworks.spectrelib.config.SpectreConfig;
import com.illusivesoulworks.spectrelib.config.SpectreConfigSpec;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ModConfigSelectScreen extends Screen {

  protected final Map<SpectreConfig.Type, Set<SpectreConfig>> configs;
  protected final Screen lastScreen;

  private ModConfigSelectionList configSelectionList;

  public ModConfigSelectScreen(Map<SpectreConfig.Type, Set<SpectreConfig>> configs,
                               Screen lastScreen, Component title) {
    super(title);
    this.configs = configs;
    this.lastScreen = lastScreen;
  }

  protected void init() {
    this.configSelectionList = new ModConfigSelectionList(this.minecraft);
    this.addWidget(this.configSelectionList);
    this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> this.onDone())
        .bounds(this.width / 2 - 75, this.height - 28, 150, 20).build());
    super.init();
  }

  void onDone() {

    if (this.minecraft != null) {
      this.minecraft.setScreen(this.lastScreen);
    }
  }

  @Override
  public void render(@Nonnull GuiGraphics guiGraphics, int x, int y, float delta) {
    super.render(guiGraphics, x, y, delta);
    guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 16777215);
    this.configSelectionList.render(guiGraphics, x, y, delta);
  }

  public void onClose() {

    if (this.minecraft != null) {
      this.minecraft.setScreen(this.lastScreen);
    }
  }

  class ModConfigSelectionList extends ObjectSelectionList<ModConfigSelectionList.Entry> {
    public ModConfigSelectionList(Minecraft mc) {
      super(mc, ModConfigSelectScreen.this.width, ModConfigSelectScreen.this.height, 43,
          ModConfigSelectScreen.this.height - 32, 24);

      ModConfigSelectScreen.this.configs.values().forEach((configs) -> {

        for (SpectreConfig config : configs) {
          ModConfigSelectionList.Entry entry = new ModConfigSelectionList.Entry(config);
          this.addEntry(entry);
        }
      });

      if (this.getSelected() != null) {
        this.centerScrollOn(this.getSelected());
      }
    }

    protected int getScrollbarPosition() {
      return super.getScrollbarPosition() + 20;
    }

    public int getRowWidth() {
      return super.getRowWidth() + 50;
    }

    @Override
    protected void renderItem(@Nonnull GuiGraphics $$0, int $$1, int $$2, float $$3, int $$4,
                              int $$5, int $$6, int $$7, int $$8) {
      Entry $$9 = this.getEntry($$4);
      $$9.render($$0, $$4, $$6, $$5, $$7, $$8, $$1, $$2, Objects.equals(this.getHovered(), $$9),
          $$3);
    }

    public class Entry extends ObjectSelectionList.Entry<ModConfigSelectionList.Entry> {

      final String type;
      final String fileName;
      final Button button;

      public Entry(SpectreConfig config) {
        this.type = config.getType().toString();
        this.fileName = config.getFileName();
        this.button = Button.builder(Component.literal(fileName),
            (button) -> {
              CommentedConfig commentedConfig =
                  config.getConfigData(SpectreConfig.InstanceType.GLOBAL);
              Consumer<Map<String, Object>> consumer = (values) -> {
                commentedConfig.valueMap().putAll(values);
                config.setConfigData(SpectreConfig.InstanceType.GLOBAL, commentedConfig, false);
                config.fireLoad(true);
              };
              SpectreConfigSpec spec = config.getSpec();
              EditConfigScreen editConfigScreen =
                  new EditConfigScreen(Component.literal(this.fileName), Component.empty(),
                      spec.getSpec().valueMap(), spec.getValues().valueMap(),
                      commentedConfig.valueMap(), ModConfigSelectionList.this.minecraft.screen,
                      consumer);
              ModConfigSelectionList.this.minecraft.setScreen(editConfigScreen);
            }).build();
      }

      public void render(@Nonnull GuiGraphics guiGraphics, int x, int y, int $$3, int $$4, int $$5,
                         int mouseX, int mouseY, boolean $$8, float delta) {
        this.button.setWidth(ModConfigSelectionList.this.getRowWidth() - 10);
        this.button.setPosition(ModConfigSelectionList.this.getRowLeft(), y);
        this.button.render(guiGraphics, mouseX, mouseY, delta);
        guiGraphics.drawString(ModConfigSelectScreen.this.font, this.type,
            ModConfigSelectScreen.this.width / 2 - 180, y + this.button.getHeight() / 2 - 3,
            16777215);
      }

      public boolean mouseClicked(double x, double y, int button) {
        return this.button.mouseClicked(x, y, button);
      }

      public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.button.keyPressed(keyCode, scanCode, modifiers) ||
            super.keyPressed(keyCode, scanCode, modifiers);
      }

      @Nonnull
      public Component getNarration() {
        return Component.literal(this.fileName);
      }
    }
  }
}
