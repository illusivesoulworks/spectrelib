package com.illusivesoulworks.spectrelib.config.client.screen;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.illusivesoulworks.spectrelib.config.SpectreConfig;
import com.illusivesoulworks.spectrelib.config.SpectreConfigSpec;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

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
      this.minecraft.gui.setScreen(this.lastScreen);
    }
  }

  @Override
  public void extractRenderState(@NotNull GuiGraphicsExtractor guiGraphics, int x, int y,
                                 float delta) {
    super.extractRenderState(guiGraphics, x, y, delta);
    guiGraphics.centeredText(this.font, this.title, this.width / 2, 16, -1);
    this.configSelectionList.extractRenderState(guiGraphics, x, y, delta);
  }

  public void onClose() {

    if (this.minecraft != null) {
      this.minecraft.gui.setScreen(this.lastScreen);
    }
  }

  class ModConfigSelectionList extends ObjectSelectionList<ModConfigSelectionList.Entry> {

    public ModConfigSelectionList(Minecraft mc) {
      super(mc, ModConfigSelectScreen.this.width, ModConfigSelectScreen.this.height - 75, 43, 24);

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

    @Override
    protected void extractItem(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY,
                               float partialTicks, @NotNull ModConfigSelectionList.Entry entry) {
      entry.extractContent(guiGraphics, mouseX, mouseY, Objects.equals(this.getHovered(), entry),
                          partialTicks);
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
                                         config.setConfigData(SpectreConfig.InstanceType.GLOBAL,
                                                              commentedConfig, false);
                                         config.fireLoad(true);
                                       };
                                       SpectreConfigSpec spec = config.getSpec();
                                       EditConfigScreen editConfigScreen =
                                           new EditConfigScreen(Component.literal(this.fileName),
                                                                Component.empty(),
                                                                spec.getSpec().valueMap(),
                                                                spec.getValues().valueMap(),
                                                                commentedConfig.valueMap(),
                                                                ModConfigSelectionList.this.minecraft.gui.screen(),
                                                                consumer);
                                       ModConfigSelectionList.this.minecraft.gui.setScreen(
                                           editConfigScreen);
                                     }).build();
      }

      public void extractContent(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY,
                                 boolean isHovering, float delta) {
        this.button.setWidth(ModConfigSelectionList.this.getRowWidth() - 10);
        this.button.setPosition(ModConfigSelectionList.this.getRowLeft(), this.getContentY());
        this.button.extractRenderState(guiGraphics, mouseX, mouseY, delta);
        guiGraphics.text(ModConfigSelectScreen.this.font, this.type,
                         ModConfigSelectScreen.this.width / 2 - 180,
                         this.getContentY() + this.button.getHeight() / 2 - 3, -1);
      }

      public boolean mouseClicked(@NotNull MouseButtonEvent evt, boolean isClicked) {
        return this.button.mouseClicked(evt, isClicked);
      }

      public boolean keyPressed(@NotNull KeyEvent keyEvent) {
        return this.button.keyPressed(keyEvent) || super.keyPressed(keyEvent);
      }

      @NotNull
      public Component getNarration() {
        return Component.literal(this.fileName);
      }
    }
  }
}
