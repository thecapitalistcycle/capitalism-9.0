/*
 *  Copyright (C) Alan Freeman 2017-2019
 *  
 *  This file is part of the Capitalism Simulation, abbreviated to CapSim
 *  in the remainder of this project
 *
 *  Capsim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either project 3 of the License, or
 *  (at your option) any later project.
*
*   Capsim is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with Capsim.  If not, see <http://www.gnu.org/licenses/>.
*/
package capitalism.view.custom;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.Simulation;
import capitalism.model.Global;
import capitalism.model.Project;
import capitalism.view.custom.command.ColourHintsCommand;
import capitalism.view.custom.command.LoadCommand;
import capitalism.view.custom.command.OpenLogWindow;
import capitalism.view.custom.command.RestartCommand;
import capitalism.view.custom.command.SaveCommand;
import capitalism.view.custom.command.ToggleDecimals;
import capitalism.view.custom.command.ToggleGraphicsCommand;
import capitalism.view.custom.command.TogglePricesCommand;
import capitalism.view.custom.command.ToggleValuesCommand;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;

/**
 * The bar where all the visual display controls are put.
 * Fairly rough and ready. This class contains all the state variables such as the graphicsState,
 * and assigns DisplayCommands to ImageButtons. Both these are 'in-house' classes.
 */

public class DisplayControlsBox extends HBox {
	final Logger logger = LogManager.getLogger("DisplayControlsBar");
	public static ContentDisplay graphicsState = ContentDisplay.TEXT_ONLY; // Whether to display graphics, text, or both

	public static enum DISPLAY_AS_EXPRESSION {
		MONEY, TIME;
	}

	public static DISPLAY_AS_EXPRESSION valuesExpressionDisplay = DISPLAY_AS_EXPRESSION.MONEY;
	public static DISPLAY_AS_EXPRESSION pricesExpressionDisplay = DISPLAY_AS_EXPRESSION.MONEY;

	public static String moneyExpressionSymbol = "$";
	public static String quantityExpressionSymbol = "#";
	public static String pricesExpressionSymbol = moneyExpressionSymbol;
	public static String valuesExpressionSymbol = moneyExpressionSymbol;
	public static boolean displayHints = false;

	private static ImageButton colourHintsButton = new ImageButton("hinton.png", "hintoff.png", new ColourHintsCommand(), "No colour hints",
			"Show colour hints");
	private static ImageButton restartButton = new ImageButton("restart.png", null, new RestartCommand(), "Restart this project", "Restart this project");
	private static ImageButton toggleGraphicsButton = new ImageButton("graphics.png", "text.png", new ToggleGraphicsCommand(), "Text column headers",
			"Graphic column headers");
	private static ImageButton toggleValuesButton = new ImageButton("timevalue.png", "dollarvalue.png", new ToggleValuesCommand(), "Money values",
			"Labour Time Values");
	private static ImageButton togglePricesButton = new ImageButton("timeprice.png", "dollarprice.png", new TogglePricesCommand(), "Money prices",
			"Labour Time Prices");
	private static ImageButton logButton = new ImageButton("log.png", null, new OpenLogWindow(), "Hide Log Window", "Show Log Window");
	private static ImageButton decimalsButton = new ImageButton("more.png", null, new ToggleDecimals(), "More digits after the decimal",
			"Fewer digits after the decimal");
	private static ImageButton dataLoadButton = new ImageButton("loadbw.png", null, new LoadCommand(), "", "Load data from your computer");
	private static ImageButton dataDumpButton = new ImageButton("savebw.png", null, new SaveCommand(), "", "Save the database to your computer");

	private static ArrayList<ImageButton> imageButtons=new ArrayList<ImageButton>();

	// convenient list in case we want to do something to all the buttons (at present, not much except add them to the HBox)
	static {
		imageButtons.addAll(Arrays.asList(toggleGraphicsButton, colourHintsButton, toggleValuesButton, togglePricesButton, logButton,
				decimalsButton, dataLoadButton, dataDumpButton,restartButton));
	}
	
	// this is the projectCombo box
	private static ProjectCombo projectCombo = null;
	private static ComboBox<String> meltCombo = null;
	private static ComboBox<String> labourSupplyCombo = null;
	private static ComboBox<String> pricingCombo = null;
	private ImageView meltResponseImage = new ImageView(new Image("time.png"));
	private ImageView labourResponseImage = new ImageView(new Image("labour.png"));
	private ImageView pricingResponseImage = new ImageView(new Image("dollar.png"));
	private static HBox buttonBar = new HBox();
	private static Pane spacer = new Pane();

	public DisplayControlsBox() {
		setMaxWidth(Double.MAX_VALUE);
		buildRightBar();
		spacer.setPrefHeight(50);
		spacer.setPrefWidth(50);
		setHgrow(spacer, Priority.ALWAYS);
		meltResponseImage.setFitWidth(15);
		meltResponseImage.setFitHeight(15);
		labourResponseImage.setFitWidth(15);
		labourResponseImage.setFitHeight(15);
		pricingResponseImage.setFitWidth(15);
		pricingResponseImage.setFitHeight(15);
		getChildren().addAll(projectCombo, labourResponseImage, labourSupplyCombo, pricingResponseImage, pricingCombo, meltResponseImage, meltCombo, spacer,
				buttonBar);
	}

	private void buildRightBar() {
		buildCombos();
		// special settings, largely because the icons are not uniformly sized in the originals
		toggleValuesButton.setImageWidth(40);
		togglePricesButton.setImageWidth(40);
		logButton.setImageWidth(25);
		logButton.setImageHeight(25);
		dataDumpButton.setImageWidth(25);
		dataDumpButton.setImageHeight(25);
		dataLoadButton.setImageWidth(25);
		dataLoadButton.setImageHeight(25);
		buttonBar.getChildren().addAll(imageButtons);
	}

	private void buildCombos() {
		ObservableList<Project> projects = Project.observableProjects();
		Project currentProject = Project.projectSingle(Simulation.projectCurrent);
		String currentProjectDescription = currentProject.getDescription();
		projectCombo = new ProjectCombo(projects, currentProjectDescription);
		labourSupplyCombo = new ComboBox<String>(Simulation.LABOUR_RESPONSE.options());
		meltCombo = new ComboBox<String>(Simulation.MELT_RESPONSE.options());
		pricingCombo = new ComboBox<String>(Simulation.PRICE_RESPONSE.options());
		meltCombo.setMinWidth(80);
		labourSupplyCombo.setMinWidth(100);
		pricingCombo.setMinWidth(100);

		Global global = Global.getGlobal();
		meltCombo.setPromptText(global.getMeltResponse().text());
		labourSupplyCombo.setPromptText(global.getLabourSupplyResponse().text());
		pricingCombo.setPromptText(global.getPriceResponse().text());
		meltCombo.valueProperty().addListener(new ChangeListener<String>() {
			@Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				meltCombo.setPromptText(newValue);
				Global currentGlobal = Global.getGlobal();
				currentGlobal.setMeltResponse(Simulation.MELT_RESPONSE.fromText(newValue));
			}
		});
		labourSupplyCombo.valueProperty().addListener(new ChangeListener<String>() {
			@Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				labourSupplyCombo.setPromptText(newValue);
				Global currentGlobal = Global.getGlobal();
				currentGlobal.setLabourSupplyResponse(Simulation.LABOUR_RESPONSE.fromText(newValue));
			}
		});
		pricingCombo.valueProperty().addListener(new ChangeListener<String>() {
			@Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				pricingCombo.setPromptText(newValue);
				Global currentGlobal = Global.getGlobal();
				currentGlobal.setPriceResponse(Simulation.PRICE_RESPONSE.fromText(newValue));
			}
		});
	}

	/**
	 * extract the per-project currency and quantity symbols from the globals record.
	 * NOTE these should perhaps be in the project record not the globals record.
	 * But if a simulation involves a currency reform, it could be in the right place after all.
	 */
	public static void setExpressionSymbols() {
		Global global = Global.getGlobal();
		moneyExpressionSymbol = global.getCurrencySymbol();
		quantityExpressionSymbol = global.getQuantitySymbol();
	}

	/**
	 * adds a tooltip to a Button. Overloads {@link setTip}
	 * 
	 * @param button
	 *            the table
	 * @param text
	 *            the tooltip
	 */
	public static void setTip(Button button, String text) {
		Tooltip tip = new Tooltip();
		tip.setText(text);
		tip.setFont(new Font(15));
		button.setTooltip(tip);
	}

	/**
	 * @param graphicsState
	 *            the graphicsState to set
	 */
	public static void setGraphicsState(ContentDisplay graphicsState) {
		DisplayControlsBox.graphicsState = graphicsState;
	}

	/**
	 * @return the graphicsState
	 */
	public static ContentDisplay getGraphicsState() {
		return graphicsState;
	}
}
