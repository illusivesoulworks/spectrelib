package com.illusivesoulworks.spectrelib.config.client.screen;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.google.common.collect.ImmutableList;
import com.illusivesoulworks.spectrelib.config.SpectreConfigSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.EnumUtils;

public class EditConfigScreen extends Screen {
  private final Set<String> invalidEntries = new HashSet<>();
  private final Map<String, Object> spec;
  private final Map<String, Object> specValues;
  private final Map<String, Object> values;
  private final Consumer<Map<String, Object>> onDone;
  private final Screen lastScreen;
  private final Component subtitle;

  private Button doneButton;
  private EditConfigScreen.ConfigList configList;

  public EditConfigScreen(Component title, Component subtitle, Map<String, Object> spec,
                          Map<String, Object> specValues, Map<String, Object> values,
                          Screen lastScreen, Consumer<Map<String, Object>> onDone) {
    super(title);
    this.subtitle = subtitle;
    this.spec = spec;
    this.specValues = specValues;
    this.values = new HashMap<>(values);
    this.lastScreen = lastScreen;
    this.onDone = onDone;
  }

  protected void init() {
    this.configList = new EditConfigScreen.ConfigList(this.spec, this.specValues);
    this.addWidget(this.configList);
    GridLayout.RowHelper gridlayout$rowhelper =
        (new GridLayout()).columnSpacing(10).createRowHelper(2);
    this.doneButton =
        gridlayout$rowhelper.addChild(Button.builder(CommonComponents.GUI_DONE, (button) -> {
          onDone.accept(this.values);

          if (this.minecraft != null) {
            this.minecraft.setScreen(this.lastScreen);
          }
        }).build());
    gridlayout$rowhelper.addChild(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {

      if (this.minecraft != null) {
        this.minecraft.setScreen(this.lastScreen);
      }
    }).build());
    gridlayout$rowhelper.getGrid().visitWidgets(this::addRenderableWidget);
    gridlayout$rowhelper.getGrid().setPosition(this.width / 2 - 155, this.height - 28);
    gridlayout$rowhelper.getGrid().arrangeElements();
  }

  public void onClose() {

    if (this.minecraft != null) {
      this.minecraft.setScreen(this.lastScreen);
    }
  }

  public void render(@Nonnull GuiGraphics guiGraphics, int x, int y, float delta) {
    this.configList.render(guiGraphics, x, y, delta);
    guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 16777215);
    guiGraphics.drawCenteredString(this.font, this.subtitle, this.width / 2, 30, 16777215);
    super.render(guiGraphics, x, y, delta);
  }

  private void updateDoneButton() {
    this.doneButton.active = this.invalidEntries.isEmpty();
  }

  void markInvalid(String path) {
    this.invalidEntries.add(path);
    this.updateDoneButton();
  }

  void clearInvalid(String path) {
    this.invalidEntries.remove(path);
    this.updateDoneButton();
  }

  public class ConfigList extends ContainerObjectSelectionList<EditConfigScreen.ConfigEntry> {

    public ConfigList(final Map<String, Object> spec, final Map<String, Object> specValues) {
      super(Objects.requireNonNull(EditConfigScreen.this.minecraft), EditConfigScreen.this.width,
          EditConfigScreen.this.height, 43, EditConfigScreen.this.height - 32, 24);
      spec.forEach((key, obj) -> {

        if (obj instanceof SpectreConfigSpec.ValueSpec value) {
          Component nameComponent =
              Component.translatableWithFallback(value.getLocalizationKey() + ".name", key);
          Component defaultComponent = Component.translatable("editGamerule.default",
              Component.literal(value.getDefault().toString())).withStyle(ChatFormatting.GRAY);
          String s1 = value.getLocalizationKey() + ".description";
          List<FormattedCharSequence> list;
          String s2;

          if (I18n.exists(s1)) {
            ImmutableList.Builder<FormattedCharSequence> builder = ImmutableList.builder();
            Component component3 = Component.translatable(s1);
            EditConfigScreen.this.font.split(component3, 150).forEach(builder::add);
            list = builder.add(defaultComponent.getVisualOrderText()).build();
            s2 = component3.getString() + "\n" + defaultComponent.getString();
          } else {
            ImmutableList.Builder<FormattedCharSequence> builder = ImmutableList.builder();
            List<Component> commentComponents = new ArrayList<>();

            for (String s : value.getComment().split("\n")) {
              commentComponents.add(Component.literal(s).withStyle(ChatFormatting.YELLOW));
            }
            builder.addAll(commentComponents.stream().map(Component::getVisualOrderText).toList());
            list = builder.add(defaultComponent.getVisualOrderText()).build();
            s2 = defaultComponent.getString();
          }
          Object current = specValues.get(key);

          if (current instanceof SpectreConfigSpec.IntValue) {
            this.addEntry(new IntegerConfigEntry(nameComponent, list, s2, key));
          } else if (current instanceof SpectreConfigSpec.BooleanValue) {
            this.addEntry(new BooleanConfigEntry(nameComponent, list, s2, key));
          } else if (current instanceof SpectreConfigSpec.DoubleValue) {
            this.addEntry(new DoubleConfigEntry(nameComponent, list, s2, key));
          } else if (current instanceof SpectreConfigSpec.LongValue) {
            this.addEntry(new LongConfigEntry(nameComponent, list, s2, key));
          } else if (current instanceof SpectreConfigSpec.EnumValue<?> enumValue) {
            this.addEntry(
                new EnumConfigEntry<>(nameComponent, list, s2, key, enumValue.getEnumClass()));
          } else if (current instanceof SpectreConfigSpec.ConfigValue<?>) {
            Object actualValue = EditConfigScreen.this.values.get(key);

            if (actualValue instanceof List<?>) {
              this.addEntry(new ListConfigEntry(nameComponent, list, s2, key));
            } else {
              this.addEntry(new StringConfigEntry(nameComponent, list, s2, key));
            }
          }
        } else if (obj instanceof AbstractConfig abstractConfig &&
            specValues.get(key) instanceof AbstractConfig abstractConfig1 &&
            EditConfigScreen.this.values.get(key) instanceof AbstractConfig abstractConfig2) {
          Component nameComponent = Component.literal(key);
          this.addEntry(
              new SectionEntry(new ArrayList<>(), nameComponent, abstractConfig.valueMap(),
                  abstractConfig1.valueMap(), abstractConfig2.valueMap()));
        }
      });
    }

    public void render(@Nonnull GuiGraphics guiGraphics, int x, int y, float delta) {
      super.render(guiGraphics, x, y, delta);
      ConfigEntry configEntry = this.getHovered();

      if (configEntry != null && configEntry.tooltip != null) {
        EditConfigScreen.this.setTooltipForNextRenderPass(configEntry.tooltip);
      }
    }
  }

  public class SectionEntry extends EditConfigScreen.ConfigEntry {

    private final Button button;

    public SectionEntry(@Nullable List<FormattedCharSequence> pTooltip, Component pLabel,
                        Map<String, Object> spec, Map<String, Object> specValues,
                        Map<String, Object> values) {
      super(pTooltip, pLabel);
      this.button = Button.builder(pLabel, (b) -> {
        Consumer<Map<String, Object>> consumer = values::putAll;
        EditConfigScreen newScreen =
            new EditConfigScreen(EditConfigScreen.this.title, pLabel, spec, specValues, values,
                EditConfigScreen.this, consumer);
        Minecraft.getInstance().setScreen(newScreen);
      }).build();
      this.children.add(this.button);
    }

    public void render(@Nonnull GuiGraphics guiGraphics, int p_281471_, int y,
                       int x, int offset, int p_283543_, int mouseX, int mouseY,
                       boolean p_283227_, float delta) {
      this.button.setX(x);
      this.button.setY(y);
      this.button.setWidth(EditConfigScreen.this.configList.getRowWidth() - 5);
      this.button.render(guiGraphics, mouseX, mouseY, delta);
    }
  }

  public class ListConfigEntry extends EditConfigScreen.ConfigEntry {
    private final Button button;

    public ListConfigEntry(Component pLabel, List<FormattedCharSequence> pTooltip,
                           String p_101103_, String key) {
      super(pTooltip, pLabel);
      List<String> list = (List<String>) EditConfigScreen.this.values.get(key);
      String result = String.join(",", list.stream().map(Object::toString).toList());
      this.button = Button.builder(Component.literal(result), (b) -> {

        if (EditConfigScreen.this.spec.get(key) instanceof SpectreConfigSpec.ValueSpec valueSpec) {
          ListConfigScreen listConfigScreen =
              new ListConfigScreen(EditConfigScreen.this.title, EditConfigScreen.this.subtitle,
                  list, EditConfigScreen.this, valueSpec::test, (l) -> {
                list.clear();
                list.addAll(l);
              });
          Minecraft.getInstance().setScreen(listConfigScreen);
        }
      }).bounds(10, 5, 88, 20).build();
      this.children.add(this.button);
    }

    public void render(@Nonnull GuiGraphics guiGraphics, int p_281471_, int y,
                       int x, int offset, int p_283543_, int mouseX, int mouseY,
                       boolean p_283227_, float delta) {
      this.renderLabel(guiGraphics, y, x);
      this.button.setX(x + offset - 89);
      this.button.setY(y);
      this.button.render(guiGraphics, mouseX, mouseY, delta);
    }
  }

  public class BooleanConfigEntry extends EditConfigScreen.ConfigEntry {
    private final CycleButton<Boolean> checkbox;

    public BooleanConfigEntry(Component pLabel, List<FormattedCharSequence> pTooltip,
                              String p_101103_, String key) {
      super(pTooltip, pLabel);
      this.checkbox = CycleButton.onOffBuilder(
              ((SpectreConfigSpec.BooleanValue) EditConfigScreen.this.specValues.get(key)).get())
          .displayOnlyValue().withCustomNarration(
              (cycle) -> cycle.createDefaultNarrationMessage().append("\n").append(p_101103_))
          .create(10, 5, 44, 20, pLabel,
              (button, value) -> EditConfigScreen.this.values.put(key, value));
      this.children.add(this.checkbox);
    }

    public void render(@Nonnull GuiGraphics guiGraphics, int p_281471_, int y,
                       int x, int offset, int p_283543_, int mouseX, int mouseY,
                       boolean p_283227_, float delta) {
      this.renderLabel(guiGraphics, y, x);
      this.checkbox.setX(x + offset - 45);
      this.checkbox.setY(y);
      this.checkbox.render(guiGraphics, mouseX, mouseY, delta);
    }
  }

  public class IntegerConfigEntry extends EditConfigScreen.ConfigEntry {
    private final EditBox input;

    public IntegerConfigEntry(Component pLabel, List<FormattedCharSequence> pTooltip,
                              String p_101177_, String key) {
      super(pTooltip, pLabel);
      this.input =
          new EditBox(Objects.requireNonNull(EditConfigScreen.this.minecraft).font, 10, 5, 42, 20,
              pLabel.copy().append("\n").append(p_101177_).append("\n"));
      this.input.setValue(
          ((SpectreConfigSpec.IntValue) EditConfigScreen.this.specValues.get(key)).get()
              .toString());
      this.input.setResponder((newValue) -> {
        Object obj = EditConfigScreen.this.spec.get(key);

        if (obj instanceof SpectreConfigSpec.ValueSpec valueSpec) {
          boolean flag = true;
          int i = 0;

          try {
            i = Integer.parseInt(newValue);
          } catch (NumberFormatException e) {
            flag = false;
          }

          if (flag && valueSpec.test(i)) {
            this.input.setTextColor(14737632);
            EditConfigScreen.this.values.put(key, i);
            EditConfigScreen.this.clearInvalid(key);
          } else {
            this.input.setTextColor(16711680);
            EditConfigScreen.this.markInvalid(key);
          }
        }
      });
      this.children.add(this.input);
    }

    public void render(@Nonnull GuiGraphics guiGraphics, int p_281471_, int y,
                       int x, int offset, int p_283543_, int mouseX, int mouseY,
                       boolean p_283227_, float delta) {
      this.renderLabel(guiGraphics, y, x);
      this.input.setX(x + offset - 44);
      this.input.setY(y);
      this.input.render(guiGraphics, mouseX, mouseY, delta);
    }
  }

  public class LongConfigEntry extends EditConfigScreen.ConfigEntry {
    private final EditBox input;

    public LongConfigEntry(Component pLabel, List<FormattedCharSequence> pTooltip,
                           String p_101177_, String key) {
      super(pTooltip, pLabel);
      this.input =
          new EditBox(Objects.requireNonNull(EditConfigScreen.this.minecraft).font, 10, 5, 42, 20,
              pLabel.copy().append("\n").append(p_101177_).append("\n"));
      this.input.setValue(
          ((SpectreConfigSpec.LongValue) EditConfigScreen.this.specValues.get(key)).get()
              .toString());
      this.input.setResponder((newValue) -> {
        Object obj = EditConfigScreen.this.spec.get(key);

        if (obj instanceof SpectreConfigSpec.ValueSpec valueSpec) {
          boolean flag = true;
          long i = 0;

          try {
            i = Long.parseLong(newValue);
          } catch (NumberFormatException e) {
            flag = false;
          }

          if (flag && valueSpec.test(i)) {
            this.input.setTextColor(14737632);
            EditConfigScreen.this.values.put(key, i);
            EditConfigScreen.this.clearInvalid(key);
          } else {
            this.input.setTextColor(16711680);
            EditConfigScreen.this.markInvalid(key);
          }
        }
      });
      this.children.add(this.input);
    }

    public void render(@Nonnull GuiGraphics guiGraphics, int p_281471_, int y,
                       int x, int offset, int p_283543_, int mouseX, int mouseY,
                       boolean p_283227_, float delta) {
      this.renderLabel(guiGraphics, y, x);
      this.input.setX(x + offset - 44);
      this.input.setY(y);
      this.input.render(guiGraphics, mouseX, mouseY, delta);
    }
  }

  public class DoubleConfigEntry extends EditConfigScreen.ConfigEntry {
    private final EditBox input;

    public DoubleConfigEntry(Component pLabel, List<FormattedCharSequence> pTooltip,
                             String p_101177_, String key) {
      super(pTooltip, pLabel);
      this.input =
          new EditBox(Objects.requireNonNull(EditConfigScreen.this.minecraft).font, 10, 5, 42, 20,
              pLabel.copy().append("\n").append(p_101177_).append("\n"));
      this.input.setValue(
          ((SpectreConfigSpec.DoubleValue) EditConfigScreen.this.specValues.get(key)).get()
              .toString());
      this.input.setResponder((newValue) -> {
        Object obj = EditConfigScreen.this.spec.get(key);

        if (obj instanceof SpectreConfigSpec.ValueSpec valueSpec) {
          boolean flag = true;
          double i = 0;

          try {
            i = Double.parseDouble(newValue);
          } catch (NumberFormatException e) {
            flag = false;
          }

          if (flag && valueSpec.test(i)) {
            this.input.setTextColor(14737632);
            EditConfigScreen.this.values.put(key, i);
            EditConfigScreen.this.clearInvalid(key);
          } else {
            this.input.setTextColor(16711680);
            EditConfigScreen.this.markInvalid(key);
          }
        }
      });
      this.children.add(this.input);
    }

    public void render(@Nonnull GuiGraphics guiGraphics, int p_281471_, int y,
                       int x, int offset, int p_283543_, int mouseX, int mouseY,
                       boolean p_283227_, float delta) {
      this.renderLabel(guiGraphics, y, x);
      this.input.setX(x + offset - 44);
      this.input.setY(y);
      this.input.render(guiGraphics, mouseX, mouseY, delta);
    }
  }

  public class StringConfigEntry extends EditConfigScreen.ConfigEntry {
    private final EditBox input;

    public StringConfigEntry(Component pLabel, List<FormattedCharSequence> pTooltip,
                             String p_101177_, String key) {
      super(pTooltip, pLabel);
      this.input =
          new EditBox(Objects.requireNonNull(EditConfigScreen.this.minecraft).font, 10, 5, 84, 20,
              pLabel.copy().append("\n").append(p_101177_).append("\n"));
      this.input.setValue(
          ((SpectreConfigSpec.ConfigValue<?>) EditConfigScreen.this.specValues.get(key)).get()
              .toString());
      this.input.setResponder((newValue) -> {
        Object obj = EditConfigScreen.this.spec.get(key);

        if (obj instanceof SpectreConfigSpec.ValueSpec valueSpec) {

          if (valueSpec.test(newValue)) {
            this.input.setTextColor(14737632);
            EditConfigScreen.this.values.put(key, newValue);
            EditConfigScreen.this.clearInvalid(key);
          } else {
            this.input.setTextColor(16711680);
            EditConfigScreen.this.markInvalid(key);
          }
        }
      });
      this.children.add(this.input);
    }

    public void render(@Nonnull GuiGraphics guiGraphics, int p_281471_, int y,
                       int x, int offset, int p_283543_, int mouseX, int mouseY,
                       boolean p_283227_, float delta) {
      this.renderLabel(guiGraphics, y, x);
      this.input.setX(x + offset - 86);
      this.input.setY(y);
      this.input.render(guiGraphics, mouseX, mouseY, delta);
    }
  }

  public class EnumConfigEntry<T extends Enum<T>> extends EditConfigScreen.ConfigEntry {
    private final CycleButton<Object> checkbox;

    public EnumConfigEntry(Component pLabel, List<FormattedCharSequence> pTooltip,
                           String p_101103_, String key, Class<T> clazz) {
      super(pTooltip, pLabel);
      this.checkbox = CycleButton.builder((t) -> {
            T en = EnumUtils.getEnum(clazz, t.toString());
            if (en != null) {
              return Component.literal(en.name());
            }
            return Component.literal("ERROR");
          })
          .withInitialValue(EditConfigScreen.this.values.get(key).toString())
          .withValues(clazz.getEnumConstants())
          .displayOnlyValue().withCustomNarration(
              (cycle) -> cycle.createDefaultNarrationMessage().append("\n").append(p_101103_))
          .create(10, 5, 88, 20, pLabel,
              (button, value) -> EditConfigScreen.this.values.put(key, value));
      this.children.add(this.checkbox);
    }

    public void render(@Nonnull GuiGraphics guiGraphics, int p_281471_, int y,
                       int x, int offset, int p_283543_, int mouseX, int mouseY,
                       boolean p_283227_, float delta) {
      this.renderLabel(guiGraphics, y, x);
      this.checkbox.setX(x + offset - 89);
      this.checkbox.setY(y);
      this.checkbox.render(guiGraphics, mouseX, mouseY, delta);
    }
  }

  public abstract class ConfigEntry
      extends ContainerObjectSelectionList.Entry<EditConfigScreen.ConfigEntry> {
    private final List<FormattedCharSequence> label;
    @Nullable
    private final List<FormattedCharSequence> tooltip;
    protected final List<AbstractWidget> children = new ArrayList<>();

    public ConfigEntry(@Nullable List<FormattedCharSequence> pTooltip, Component pLabel) {
      this.tooltip = pTooltip;
      this.label = Objects.requireNonNull(EditConfigScreen.this.minecraft).font.split(pLabel, 175);
    }

    @Nonnull
    public List<? extends GuiEventListener> children() {
      return this.children;
    }

    @Nonnull
    public List<? extends NarratableEntry> narratables() {
      return this.children;
    }

    protected void renderLabel(GuiGraphics guiGraphics, int y, int x) {

      if (this.label.size() == 1) {
        guiGraphics.drawString(Objects.requireNonNull(EditConfigScreen.this.minecraft).font,
            this.label.get(0), x, y + 5, 16777215, false);
      } else if (this.label.size() >= 2) {
        guiGraphics.drawString(Objects.requireNonNull(EditConfigScreen.this.minecraft).font,
            this.label.get(0), x, y, 16777215, false);
        guiGraphics.drawString(Objects.requireNonNull(EditConfigScreen.this.minecraft).font,
            this.label.get(1), x, y + 10, 16777215, false);
      }
    }
  }
}