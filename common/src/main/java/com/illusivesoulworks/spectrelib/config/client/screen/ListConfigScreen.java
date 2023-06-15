package com.illusivesoulworks.spectrelib.config.client.screen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ListConfigScreen extends Screen {

  private final Set<Integer> invalidEntries = new HashSet<>();
  private final List<String> values;
  private final Consumer<List<String>> onDone;
  private final Predicate<Object> validator;
  private final Screen lastScreen;
  private final Component subtitle;

  private Button doneButton;
  private ListConfigScreen.ListConfig listConfig;

  public ListConfigScreen(Component title, Component subtitle, List<String> values,
                          Screen lastScreen, Predicate<Object> validator,
                          Consumer<List<String>> onDone) {
    super(title);
    this.subtitle = subtitle;
    this.values = new LinkedList<>(values);
    this.lastScreen = lastScreen;
    this.onDone = onDone;
    this.validator = validator;
  }

  protected void init() {
    this.listConfig = new ListConfigScreen.ListConfig();
    this.addWidget(this.listConfig);
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
    this.listConfig.render(guiGraphics, x, y, delta);
    guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 16777215);
    guiGraphics.drawCenteredString(this.font, this.subtitle, this.width / 2, 30, 16777215);
    super.render(guiGraphics, x, y, delta);
  }

  private void updateDoneButton() {
    this.doneButton.active = this.invalidEntries.isEmpty();
  }

  void markInvalid(int index) {
    this.invalidEntries.add(index);
    this.updateDoneButton();
  }

  void clearInvalid(int index) {
    this.invalidEntries.remove(index);
    this.updateDoneButton();
  }

  public class ListConfig extends ContainerObjectSelectionList<ListConfigScreen.Entry> {

    public ListConfig() {
      super(Objects.requireNonNull(ListConfigScreen.this.minecraft), ListConfigScreen.this.width,
          ListConfigScreen.this.height, 43, ListConfigScreen.this.height - 32, 24);

      for (int i = 0; i < ListConfigScreen.this.values.size(); i++) {
        this.addEntry(new ListConfigScreen.Entry(i, this.getRowWidth()));
      }

      if (this.children().size() == 0) {
        this.addEntry(new ListConfigScreen.Entry(-1, this.getRowWidth()));
      }
    }

    void addEmptyEntry(int index) {
      this.clearEntries();
      ListConfigScreen.this.values.add(index, "");

      for (int i = 0; i < ListConfigScreen.this.values.size(); i++) {
        this.addEntry(new ListConfigScreen.Entry(i, this.getRowWidth()));
      }

      if (this.children().size() == 0) {
        this.addEntry(new ListConfigScreen.Entry(-1, this.getRowWidth()));
      }
    }

    void removeEntry(int index) {
      this.clearEntries();
      ListConfigScreen.this.values.remove(index);

      for (int i = 0; i < ListConfigScreen.this.values.size(); i++) {
        this.addEntry(new ListConfigScreen.Entry(i, this.getRowWidth()));
      }

      if (this.children().size() == 0) {
        this.addEntry(new ListConfigScreen.Entry(-1, this.getRowWidth()));
      }
    }

    @Override
    public int getRowWidth() {
      return super.getRowWidth() + 100;
    }

    @Override
    protected int getScrollbarPosition() {
      return super.getScrollbarPosition() + 50;
    }
  }

  public final class Entry extends ContainerObjectSelectionList.Entry<ListConfigScreen.Entry> {
    private EditBox input = null;
    private final Button addButton;
    private Button removeButton = null;
    private final List<AbstractWidget> children = new ArrayList<>();

    public Entry(int index, int width) {

      if (index >= 0) {
        String currentValue = ListConfigScreen.this.values.get(index);
        this.input =
            new EditBox(Objects.requireNonNull(ListConfigScreen.this.minecraft).font, 10, 5,
                width - 45, 20, Component.literal(currentValue));
        this.input.setValue(currentValue);
        this.input.setResponder((newValue) -> {
          List<String> test = new ArrayList<>(ListConfigScreen.this.values);
          test.set(index, newValue);

          if (ListConfigScreen.this.validator.test(test)) {
            this.input.setTextColor(14737632);
            ListConfigScreen.this.values.set(index, newValue);
            ListConfigScreen.this.clearInvalid(index);
          } else {
            this.input.setTextColor(16711680);
            ListConfigScreen.this.markInvalid(index);
          }
        });
        this.removeButton = Button.builder(Component.literal("-"), (b) -> {
          ListConfigScreen.this.listConfig.removeEntry(index);
        }).bounds(10, 5, 20, 20).build();
      }
      this.addButton = Button.builder(Component.literal("+"), (b) -> {
        ListConfigScreen.this.listConfig.addEmptyEntry(index + 1);
      }).bounds(10, 5, 20, 20).build();

      if (this.input != null) {
        this.children.add(this.input);
      }
      this.children.add(this.addButton);

      if (this.removeButton != null) {
        this.children.add(this.removeButton);
      }
    }

    @Nonnull
    public List<? extends GuiEventListener> children() {
      return this.children;
    }

    @Nonnull
    public List<? extends NarratableEntry> narratables() {
      return this.children;
    }

    public void render(@Nonnull GuiGraphics guiGraphics, int p_281471_, int y,
                       int x, int offset, int p_283543_, int mouseX, int mouseY,
                       boolean p_283227_, float delta) {

      if (this.input != null) {
        this.input.setX(x);
        this.input.setY(y);
        this.input.render(guiGraphics, mouseX, mouseY, delta);
      }
      this.addButton.setX(x + offset - 42);
      this.addButton.setY(y);
      this.addButton.render(guiGraphics, mouseX, mouseY, delta);

      if (this.removeButton != null) {
        this.removeButton.setX(x + offset - 20);
        this.removeButton.setY(y);
        this.removeButton.render(guiGraphics, mouseX, mouseY, delta);
      }
    }
  }
}
