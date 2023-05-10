package com.sai.veggipatch;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Features in this demo:
 * <pre>
 * Farmer Navigation:
 *      * Farmer can be moved using up-down-right-left arrow keys. And also with the mouse pointer click.
 *
 * Applying vegetables:
 *      * Pressing 'G' : will apply grass
 *      * Pressing 'F' : will apply fertilizer
 *      * Pressing 'C' : will add Carrot to the list
 *      * Pressing 'T' : will add Turnip to the list
 *      * Pressing 'R' : will remove/harvest all vegetables in the list -> then fertilizer -> then grass (for each press)
 *
 * Changing Vegetable state:
 *      * On add of Carrot or Turnip it is in RAW state. Adding a fertilizer will change the state of vegetable in 10 seconds to RIPE and will remove the fertilizer.
 *
 */
public class VeggiePatch_Demo extends Application {

    public static final double CELL_SIZE = 50;

    public static final double COLS = 18;

    public static final double ROWS = 14;

    private IntegerProperty x = new SimpleIntegerProperty();
    private IntegerProperty y = new SimpleIntegerProperty();

    private Map<String, Cell> cells = new HashMap<>();

    @Override
    public void start(final Stage stage) throws Exception {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        GridPane field = new GridPane();
        field.getStyleClass().add("field");
        field.setMinSize(COLS * CELL_SIZE, ROWS * CELL_SIZE);
        field.setMaxSize(COLS * CELL_SIZE, ROWS * CELL_SIZE);
        field.setOnMouseClicked(e -> {
            x.set((int) (e.getX() / CELL_SIZE));
            y.set((int) (e.getY() / CELL_SIZE));
        });

        Farmer farmer = new Farmer();
        farmer.layoutXProperty().bind(x.multiply(CELL_SIZE));
        farmer.layoutYProperty().bind(y.multiply(CELL_SIZE));

        buildBase(root, field, farmer);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("veggiepatch.css").toExternalForm());
        addHandlers(scene);
        stage.setScene(scene);
        stage.setTitle("Veggie Patch");
        stage.show();
    }

    private void addHandlers(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            KeyCode code = e.getCode();
            switch (code) {
                case UP:
                    if (y.get() > 0) {
                        y.set(y.get() - 1);
                    }
                    break;

                case RIGHT:
                    if (x.get() < COLS - 1) {
                        x.set(x.get() + 1);
                    }
                    break;

                case LEFT:
                    if (x.get() > 0) {
                        x.set(x.get() - 1);
                    }
                    break;

                case DOWN:
                    if (y.get() < ROWS - 1) {
                        y.set(y.get() + 1);
                    }
                    break;

                case G:
                    getCell().applyGrass();
                    break;

                case C:
                    getCell().addVegetable(new Carrot());
                    break;

                case T:
                    getCell().addVegetable(new Turnip());
                    break;

                case F:
                    getCell().applyFertilizer();
                    break;

                case R:
                    getCell().remove();
                    break;
            }
        });
    }

    private Cell getCell() {
        return cells.get(x.get() + "-" + y.get());
    }

    private void buildBase(final BorderPane root, final GridPane field, Farmer farmer) {
        GridPane base = new GridPane();

        // Build column index
        int cols = (int) (field.getMinWidth() / CELL_SIZE);
        for (int i = -1; i < cols; i++) {
            Label lbl = new Label(i > -1 ? i + "" : "");
            lbl.setStyle("-fx-font-weight:bold;");
            if (i > -1) {
                lbl.setPrefWidth(CELL_SIZE);
            }
            lbl.setAlignment(Pos.CENTER);
            base.add(lbl, i + 1, 0);
        }

        // Build row index
        int rows = (int) (field.getMinHeight() / CELL_SIZE);
        for (int i = 1; i <= rows; i++) {
            Label lbl = new Label((i - 1) + "");
            lbl.setStyle("-fx-font-weight:bold;");
            lbl.setPrefHeight(CELL_SIZE);
            lbl.setAlignment(Pos.CENTER_RIGHT);
            lbl.setPadding(new Insets(0, 3, 0, 0));
            base.add(lbl, 0, i);
        }

        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                Cell cell = new Cell();
                CellViewModel cellViewModel = new CellViewModel(cell);
                CellView view = new CellView(cellViewModel);
                field.add(view, i, j);
                cells.put(i + "-" + j, cell);
            }
        }

        Pane layout = new Pane(field, farmer);
        base.add(layout, 1, 1, cols, rows);
        root.setCenter(base);
    }

    class Farmer extends StackPane {
        public Farmer() {
            Circle c = new Circle(CELL_SIZE / 2, Color.RED);
            c.setOpacity(.2);
            getChildren().add(c);
        }
    }

    enum Status {
        RAW, RIPE;
    }

    interface Vegetable {
        String getType();

        Status getStatus();

        void setStatus(Status status);
    }

    class Carrot implements Vegetable {
        private Status status = Status.RAW;

        @Override
        public String getType() {
            return "carrot";
        }

        @Override
        public Status getStatus() {
            return status;
        }

        @Override
        public void setStatus(final Status status) {
            this.status = status;
        }
    }

    class Turnip implements Vegetable {
        private Status status = Status.RAW;

        @Override
        public String getType() {
            return "turnip";
        }

        @Override
        public Status getStatus() {
            return status;
        }

        @Override
        public void setStatus(final Status status) {
            this.status = status;
        }
    }

    // MVVM PATTERN for Cell

    class Cell {
        private List<Vegetable> veggieList = new ArrayList<>();

        private boolean fertilizerAdded;

        private boolean appliedGrass;

        private BooleanProperty modified = new SimpleBooleanProperty();

        public void addVegetable(Vegetable veg) {
            veggieList.add(veg);
            change();
        }

        public void applyFertilizer() {
            fertilizerAdded = true;
            change();
        }

        public void applyGrass() {
            appliedGrass = true;
            change();
        }

        private void change() {
            setModified(true);
        }

        public void setModified(final boolean modified) {
            this.modified.set(modified);
        }

        public BooleanProperty modifiedProperty() {
            return modified;
        }

        public boolean isAppliedGrass() {
            return appliedGrass;
        }

        public void remove() {
            if (!veggieList.isEmpty()) {
                veggieList.remove(veggieList.get(veggieList.size() - 1));
                change();
            } else if (fertilizerAdded) {
                fertilizerAdded = false;
                change();
            } else if (appliedGrass) {
                appliedGrass = false;
                change();
            }
        }
    }

    class CellViewModel {
        private Cell cell;

        private ObjectProperty<Vegetable> currentVegetable = new SimpleObjectProperty<>();

        private ObjectProperty<Status> currentVegetableStatus = new SimpleObjectProperty<>();

        private BooleanProperty fertilzerInuse = new SimpleBooleanProperty();

        private BooleanProperty appliedGrass = new SimpleBooleanProperty();

        public CellViewModel(Cell cell) {
            this.cell = cell;
            cell.modifiedProperty().addListener((obs, old, modified) -> {
                if (modified) {
                    updateGrass();
                    updateFertilizer();
                    updateVegetable();
                    cell.setModified(false);
                }
            });
        }

        private void updateVegetable() {
            currentVegetable.set(getLastVegetable());
            currentVegetableStatus.set(currentVegetable.get() != null ? currentVegetable.get().getStatus() : null);
        }

        private void updateGrass() {
            appliedGrass.set(cell.appliedGrass);
        }

        private void updateFertilizer() {
            fertilzerInuse.set(cell.fertilizerAdded);
            if (cell.fertilizerAdded) {
                Vegetable lastVeg = getLastVegetable();
                if (lastVeg != null) {
                    Timeline tl = new Timeline(new KeyFrame(Duration.seconds(10), e -> {
                        lastVeg.setStatus(Status.RIPE);
                        currentVegetableStatus.set(Status.RIPE);
                        fertilzerInuse.set(false);
                    }));
                    tl.play();
                }
            }
        }

        private Vegetable getLastVegetable() {
            return cell.veggieList.isEmpty() ? null : cell.veggieList.get(cell.veggieList.size() - 1);
        }

        public ObjectProperty<Vegetable> currentVegetableProperty() {
            return currentVegetable;
        }

        public Vegetable getCurrentVegetable() {
            return currentVegetable.get();
        }

        public BooleanProperty fertilzerInuseProperty() {
            return fertilzerInuse;
        }

        public boolean isFertilzerInuse() {
            return fertilzerInuse.get();
        }

        public BooleanProperty appliedGrassProperty() {
            return appliedGrass;
        }

        public boolean isAppliedGrass() {
            return appliedGrass.get();
        }

        public ObjectProperty<Status> currentVegetableStatusProperty() {
            return currentVegetableStatus;
        }

        public Status getCurrentVegetableStatus() {
            return currentVegetableStatus.get();
        }
    }

    class CellView extends StackPane {
        private Pane grass = new Pane();
        private Pane fertilizer = new Pane();
        private Pane vegetable = new Pane();

        private final CellViewModel viewModel;

        public CellView(CellViewModel vm) {
            Stream.of(grass, fertilizer, vegetable).forEach(p -> {
                p.setPrefSize(CELL_SIZE, CELL_SIZE);
                p.getStyleClass().add("layer");
            });
            viewModel = vm;
            getChildren().addAll(grass, fertilizer, vegetable);

            viewModel.currentVegetableProperty().addListener((obs, old, val) -> {
                for (PseudoClass ps : vegetable.getPseudoClassStates()) {
                    vegetable.pseudoClassStateChanged(ps, false);
                }
                if (val != null) {
                    vegetable.pseudoClassStateChanged(PseudoClass.getPseudoClass(val.getType()), true);
                }
            });

            viewModel.currentVegetableStatusProperty().addListener((obs, old, status) -> {
                Stream.of(Status.values())
                        .map(s -> s.toString().toLowerCase())
                        .forEach(s -> vegetable.pseudoClassStateChanged(PseudoClass.getPseudoClass(s), false));
                if (status != null) {
                    vegetable.pseudoClassStateChanged(PseudoClass.getPseudoClass(status.name().toLowerCase()), true);
                }
            });

            viewModel.fertilzerInuseProperty().addListener((obs, old, inUse) -> {
                fertilizer.pseudoClassStateChanged(PseudoClass.getPseudoClass("fertilizer"), inUse);
            });

            viewModel.appliedGrassProperty().addListener((obs, old, applied) -> {
                grass.pseudoClassStateChanged(PseudoClass.getPseudoClass("grass"), applied);
            });
        }
    }
}
