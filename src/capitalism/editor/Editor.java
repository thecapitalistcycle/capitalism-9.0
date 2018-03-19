/*
 * Copyright (C) Alan Freeman 2017-2019
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
package capitalism.editor;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.controller.Parameters;
import capitalism.editor.EditableCommodity.EC_ATTRIBUTE;
import capitalism.editor.EditableIndustry.EI_ATTRIBUTE;
import capitalism.editor.EditableSocialClass.ESC_ATTRIBUTE;
import capitalism.model.Commodity;
import capitalism.model.Commodity.FUNCTION;
import capitalism.model.Commodity.ORIGIN;
import capitalism.model.Industry;
import capitalism.model.OneProject;
import capitalism.model.Project;
import capitalism.model.SocialClass;
import capitalism.model.Stock;
import capitalism.model.Stock.OWNERTYPE;
import capitalism.model.TimeStamp;
import capitalism.utils.Reporter;
import capitalism.view.custom.RadioButtonPair;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.VBox;

/*
 * with acknowledgements to http://java-buddy.blogspot.ca/2012/04/javafx-2-editable-tableview.html
 */

public class Editor extends VBox {
	private static final Logger logger = LogManager.getLogger("Editor");

	private static EditorControlBar ecb = new EditorControlBar();

	protected static ObservableList<EditableCommodity> commodityData = null;
	protected static TableView<EditableCommodity> commodityTable = new TableView<EditableCommodity>();
	protected static ObservableList<EditableIndustry> industryData = null;
	protected static TableView<EditableIndustry> industryTable = new TableView<EditableIndustry>();
	protected static ObservableList<EditableSocialClass> socialClassData = null;
	protected static TableView<EditableSocialClass> socialClassTable = new TableView<EditableSocialClass>();
	protected static EditableTimeStamp editableTimeStamp = new EditableTimeStamp();
	private static TabPane tabPane = null;
	private static Tab commodityTab = null;
	private static Tab industryTab = null;
	private static Tab socialClassTab = null;
	private static VBox commodityBox = null;
	private static VBox industryBox = null;
	private static VBox socialClassBox = null;

	private static TextField industryField;
	private static TextField industryCommodityField;
	private static TextField socialClassField;
	private static TextField commodityField;

	private static RadioButtonPair commodityFunction;
	private static RadioButtonPair commodityOrigin;
	
	private static EditorDialogueBox industryDialogueBox;
	private static EditorDialogueBox socialClassDialogueBox;
	private static EditorDialogueBox commodityDialogueBox;

	// the next three are a complete botch to get things going while we figure
	// out how to get inherited disable working
	public static ArrayList<Node> industryDisableList = new ArrayList<Node>();
	public static ArrayList<Node> commodityDisableList = new ArrayList<Node>();
	public static ArrayList<Node> socialClassDisableList = new ArrayList<Node>();

	public enum TAB_SELECTION {
		COMMODITY, INDUSTRY, SOCIALCLASS, UNKNOWN;
	}

	/**
	 * Construct an editor window. This will be displayed modally (using {@code showandwait()}) by {@link EditorManager}
	 */
	public Editor() {
		// start from scratch every time
		commodityData = FXCollections.observableArrayList();
		industryData = FXCollections.observableArrayList();
		socialClassData = FXCollections.observableArrayList();
		commodityTable.getColumns().clear();
		industryTable.getColumns().clear();
		socialClassTable.getColumns().clear();

		makeCommodityTable();
		makeIndustryTable();
		makeSocialClassTable();

		// box for the commodity table
		commodityBox = new VBox();
		commodityBox.setPrefHeight(300);
		commodityBox.setPrefWidth(7600);
		commodityBox.getChildren().add(commodityTable);

		// commodityBox associated dialogue box
		List<Node> commodityList = new ArrayList<Node>();
		commodityField = new TextField();
		commodityField.setPromptText("Name of the new commodity");
		commodityField = new TextField();
		commodityField.setPromptText("Name of the rose");
		commodityFunction = new RadioButtonPair("Production","Consumption","Used as an input to production","Consumed by a social class");
		commodityOrigin = new RadioButtonPair("Industrial","Social","Produced by an industry","Produced by a class");
		commodityList.add(commodityField);
		commodityList.add(commodityFunction);
		commodityList.add(commodityOrigin);
		commodityDialogueBox = new EditorDialogueBox(commodityList,"Enter the name of the new commodity", commoditySaveHandler, commodityDisableList);
		commodityBox.getChildren().add(commodityDialogueBox);

		// box for the industry table
		industryBox = new VBox();
		industryBox.setPrefHeight(300);
		industryBox.setPrefWidth(7600);
		industryBox.getChildren().add(industryTable);

		// industryBox associated dialogue box
		List<Node> industryList = new ArrayList<Node>();
		industryField = new TextField();
		industryField.setPromptText("Name of the new industry");
		industryCommodityField = new TextField();
		industryCommodityField.setPromptText("Name of the commodity that this industry produces");

		industryList.add(industryField);
		industryList.add(industryCommodityField);
		industryDialogueBox = new EditorDialogueBox(industryList,"Enter the name of the new industry", industrySaveHandler, industryDisableList);
		industryBox.getChildren().add(industryDialogueBox);

		// box for the social class table
		socialClassBox = new VBox();
		socialClassBox.setPrefHeight(300);
		socialClassBox.setPrefWidth(7600);
		socialClassBox.getChildren().add(socialClassTable);

		// socialClassBox associated dialogue box
		// get the name of the social class and its participation ratio
		List<Node> socialClassList = new ArrayList<Node>();
		socialClassField = new TextField();
		socialClassField.setPromptText("Name of the new social Class");
		socialClassList.add(socialClassField);
		socialClassDialogueBox = new EditorDialogueBox(socialClassList,"Enter the name of the new social class", socialClassSaveHandler, socialClassDisableList);
		socialClassBox.getChildren().add(socialClassDialogueBox);

		// the tabbed pane
		tabPane = new TabPane();

		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		commodityTab = new Tab("Commodities");

		commodityTab.setContent(commodityBox);
		industryTab = new Tab("Industries");
		industryTab.setContent(industryBox);
		socialClassTab = new Tab("Classes");
		socialClassTab.setContent(socialClassBox);

		tabPane.getTabs().addAll(commodityTab, industryTab, socialClassTab);
		getChildren().addAll(ecb, tabPane);

		// TODO the below nine lines just a bodge to get things going until I can figure out disable inheritance.
		// general apology to the world of mentation.
		industryDisableList.add(commodityBox);
		industryDisableList.add(socialClassBox);
		industryDisableList.add(industryTable);
		socialClassDisableList.add(industryBox);
		socialClassDisableList.add(commodityBox);
		socialClassDisableList.add(socialClassTable);
		commodityDisableList.add(industryBox);
		commodityDisableList.add(socialClassBox);
		commodityDisableList.add(commodityTable);

		industryDialogueBox.hideDialogue();
		socialClassDialogueBox.hideDialogue();
		commodityDialogueBox.hideDialogue();
	}

	/**
	 * create the columns for the Commodity table and populate the table
	 */
	private static void makeCommodityTable() {
		commodityTable.getColumns().clear();
		commodityTable.setEditable(true);
		commodityTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		commodityTable.getColumns().add(EditableCommodity.makeStringColumn(EC_ATTRIBUTE.NAME));
		commodityTable.getColumns().add(EditableCommodity.makeStringColumn(EC_ATTRIBUTE.FUNCTION));
		commodityTable.getColumns().add(EditableCommodity.makeStringColumn(EC_ATTRIBUTE.ORIGIN));
		commodityTable.getColumns().add(EditableCommodity.makeDoubleColumn(EC_ATTRIBUTE.UNIT_VALUE));
		commodityTable.getColumns().add(EditableCommodity.makeDoubleColumn(EC_ATTRIBUTE.UNIT_PRICE));
		commodityTable.getColumns().add(EditableCommodity.makeDoubleColumn(EC_ATTRIBUTE.TURNOVER));
		commodityTable.setItems(commodityData);
	}

	/**
	 * Create the columns for the Industry table and populate the table
	 */
	private static void makeIndustryTable() {
		industryTable.getColumns().clear();
		industryTable.setEditable(true);
		industryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		industryTable.getColumns().add(EditableIndustry.makeStringColumn(EI_ATTRIBUTE.NAME));
		industryTable.getColumns().add(EditableIndustry.makeStringColumn(EI_ATTRIBUTE.COMMODITY_NAME));
		industryTable.getColumns().add(EditableIndustry.makeDoubleColumn(EI_ATTRIBUTE.OUTPUT));
		industryTable.getColumns().add(EditableIndustry.makeDoubleColumn(EI_ATTRIBUTE.SALES));
		industryTable.getColumns().add(EditableIndustry.makeDoubleColumn(EI_ATTRIBUTE.MONEY));
		industryTable.setItems(industryData);
	}

	/**
	 * Add one column for each productive stock in the industry Table.
	 * public because it is invoked post-hoc by the Editor Manager
	 * TODO do something more elegant
	 */
	public static void addIndustryStockColumns() {
		for (EditableCommodity commodity : commodityData) {
			// TODO bit of a developer leak here: need to make sure the
			// data exists. May be Better to drive from the data table than just
			// assume that just because there is a commodity, the data has been supplied.
			if (commodity.getFunction().equals(Commodity.FUNCTION.PRODUCTIVE_INPUT.text()))
				industryTable.getColumns().add(EditableIndustry.makeStockColumn(commodity.getName()));
		}
	}

	/**
	 * Create the columns for the Social Class table and populate the table
	 */
	private static void makeSocialClassTable() {
		socialClassTable.getColumns().clear();
		socialClassTable.setEditable(true);
		socialClassTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		socialClassTable.getColumns().add(EditableSocialClass.makeStringColumn(ESC_ATTRIBUTE.NAME));
		socialClassTable.getColumns().add(EditableSocialClass.makeDoubleColumn(ESC_ATTRIBUTE.PR));
		socialClassTable.getColumns().add(EditableSocialClass.makeDoubleColumn(ESC_ATTRIBUTE.REVENUE));
		socialClassTable.getColumns().add(EditableSocialClass.makeDoubleColumn(ESC_ATTRIBUTE.SALES));
		socialClassTable.getColumns().add(EditableSocialClass.makeDoubleColumn(ESC_ATTRIBUTE.MONEY));
		socialClassTable.setItems(socialClassData);
	}

	/**
	 * Add one column for each consumption stock in the Social Class table.
	 * public because it is invoked post-hoc by the Editor Manager
	 * TODO do something more elegant
	 */
	public static void addSocialClassStockColumns() {
		for (EditableCommodity commodity : commodityData) {
			logger.debug("Adding columns for consumption goods: trying {}", commodity.getName());
			// TODO bit of a developer leak here: need to make sure the
			// data exists. May be Better to drive from the data table than just
			// assume that just because there is a commodity, the data has been supplied.
			if (commodity.getFunction().equals(Commodity.FUNCTION.CONSUMER_GOOD.text()))
				socialClassTable.getColumns().add(EditableSocialClass.makeStockColumn(commodity.getName()));
		}
	}

	/**
	 * Remake the tables. This has to be done whenever the stock columns change, because the
	 * commodities they represent have new names. There may be a better way but this is
	 * the simplest and I don't think it's expensive because there are no database operations involved.
	 */
	public static void rebuildTables() {
		makeCommodityTable();
		makeIndustryTable();
		makeSocialClassTable();
	}

	/**
	 * Refresh the tables by requesting a reload of their data
	 * TODO check empirically if this is really needed because these are observables
	 * so in principle refresh should be automatic
	 */
	public static void refresh() {
		industryTable.refresh();
		socialClassTable.refresh();
		commodityTable.refresh();
	}

	/**
	 * Repopulate the tables by resetting their data
	 * TODO check if this is automatic
	 */
	public static void rePopulate() {
		commodityTable.setItems(commodityData);
		industryTable.setItems(industryData);
		socialClassTable.setItems(socialClassData);
	}

	/**
	 * Load the current project into the editor
	 */
	public static void load() {
		// First find out which commodities we have
		setCommodityData(EditableCommodity.editableCommodities());

		// Next, find out which industries we have
		setIndustryData(EditableIndustry.editableIndustries());

		// Now add the productive stocks that these industries own
		for (EditableIndustry industry : getIndustryData()) {
			for (EditableCommodity commodity : getCommodityData()) {
				if (commodity.getFunction().equals("Productive Inputs"))
					industry.addProductiveStock(commodity.getName());
			}
		}
		// and create columns for the productive stocks
		addIndustryStockColumns();

		// Populate the EditableStocks from the simulation.
		// The money and sales stocks were created by the EditableIndustry constructor
		// We just added the productive stocks
		EditableIndustry.loadAllStocksFromSimulation();

		setSocialClassData(EditableSocialClass.editableSocialClasses());
		// Now add the consumption stocks that these industries classes own
		for (EditableSocialClass socialClass : getSocialClassData()) {
			for (EditableCommodity commodity : getCommodityData()) {
				if (commodity.getFunction().equals("Consumer Goods"))
					socialClass.addConsumptionStock(commodity.getName());
			}
		}

		// and create the columns for consumption stocks
		addSocialClassStockColumns();
		EditableSocialClass.loadAllStocksFromSimulation();

	}

	/**
	 * Create a skeleton project, which contains the minimum necessary for a viable project
	 */
	public static void createSkeletonProject() {
		Reporter.report(logger, 0, "CREATING A PROJECT");
		// Clear the decks
		clearDecks();

		// Create the commodities money, necessities, labour power, and means of production
		Reporter.report(logger, 1, "Creating the basic commodities");
		EditableCommodity moneyCommodity = EditableCommodity.makeCommodity("Money", ORIGIN.MONEY, FUNCTION.MONEY);
		EditableCommodity necessityCommodity = EditableCommodity.makeCommodity("Necessities", ORIGIN.INDUSTRIALLY_PRODUCED, FUNCTION.CONSUMER_GOOD);
		EditableCommodity meandOfProductionCommodity = EditableCommodity.makeCommodity("Means of Production", ORIGIN.INDUSTRIALLY_PRODUCED,
				FUNCTION.PRODUCTIVE_INPUT);
		EditableCommodity labourPowerCommodity = EditableCommodity.makeCommodity("Labour Power", ORIGIN.SOCIALLY_PRODUCED, FUNCTION.PRODUCTIVE_INPUT);
		getCommodityData().addAll(moneyCommodity, necessityCommodity, meandOfProductionCommodity, labourPowerCommodity);

		// Create the social classes Capitalists, Workers and their stocks
		Reporter.report(logger, 1, "Creating the minimum social Classes");
		EditableSocialClass capitalists = EditableSocialClass.makeSocialClass("Capitalists", 0);
		EditableSocialClass workers = EditableSocialClass.makeSocialClass("Workers", 1);
		getSocialClassData().addAll(workers, capitalists);

		// Create the two industries means of production and consumption and their stocks
		Reporter.report(logger, 1, "Creating the minimum industries");
		EditableIndustry dI = EditableIndustry.makeIndustry("Department I", "Means of production", 0);
		EditableIndustry dII = EditableIndustry.makeIndustry("Departmment II", "Necessities", 0);
		getIndustryData().addAll(dI, dII);
		// repopulate the display
		rebuildTables();
	}

	/**
	 * Add a commodity
	 */
	public static void addCommodity() {
		commodityDialogueBox.showDialogue();
	}

	/**
	 * Create a new commodity.
	 * Get the name of the commodity, its origin and its function from the {@link Editor#commodityDialogueBox}.
	 * Validate that no other commodity has the same name. If productive, iterate adding stocks of it to all industries.
	 * If consumption, iterate adding stocks to all social classes.
	 * Create at least one industry that makes this commodity and report on this fact.
	 * 
	 */

	private EventHandler<ActionEvent> commoditySaveHandler = new EventHandler<ActionEvent>() {
		@Override public void handle(ActionEvent t) {
			// Check that there is a name for the commodity
			if (commodityField.getText().equals("")) {
				socialClassDialogueBox.warn("Commodity name cannot be blank");
				return;
			}
			if (isCommodity(commodityField.getText())) {
				commodityDialogueBox.warn("A commodity with this name already exists");
			}
			Commodity.ORIGIN origin=commodityOrigin.result()?Commodity.ORIGIN.INDUSTRIALLY_PRODUCED:Commodity.ORIGIN.SOCIALLY_PRODUCED;
			Commodity.FUNCTION function=commodityFunction.result()?Commodity.FUNCTION.PRODUCTIVE_INPUT:Commodity.FUNCTION.CONSUMER_GOOD;
			EditableCommodity newCommodity = EditableCommodity.makeCommodity(commodityField.getText(),origin, function);
			Editor.commodityData.add(newCommodity);
			Editor.commodityTable.refresh();
			commodityDialogueBox.hideDialogue();
		}
	};

	/**
	 * Add an industry
	 */
	public static void addIndustry() {
		industryDialogueBox.showDialogue();
	}

	private EventHandler<ActionEvent> industrySaveHandler = new EventHandler<ActionEvent>() {
		@Override public void handle(ActionEvent t) {
			// Check that there is a name for the industry
			if (industryField.getText().equals("")) {
				industryDialogueBox.warn("Industry name cannot be blank");
				return;
			}
			if (isIndustry(socialClassField.getText())) {
				industryDialogueBox.warn("An industry with this name already exists");
			}

			if (industryField.getText().equals("")) {
				industryDialogueBox.warn("Commodity name cannot be blank");
				return;
			}
			if (!Editor.isCommodity(industryCommodityField.getText())) {
				industryDialogueBox.warn("Commodity does not exist");
				return;
			}

			EditableIndustry newIndustry = EditableIndustry.makeIndustry(industryField.getText(), industryCommodityField.getText(), 0);
			Editor.industryData.add(newIndustry);
			Editor.industryTable.refresh();
			industryDialogueBox.hideDialogue();
		}
	};

	/**
	 * Add a social Class
	 */
	public static void addSocialClass() {
		socialClassDialogueBox.showDialogue();
	}

	/**
	 * Using the input from {@link Editor#socialClassDialogueBox}}, constructs a new
	 * social class in the {@link Editor#makeSocialClassTable()}.
	 */
	private EventHandler<ActionEvent> socialClassSaveHandler = new EventHandler<ActionEvent>() {
		@Override public void handle(ActionEvent t) {
			// Check that there is a name for the socialClass
			if (socialClassField.getText().equals("")) {
				socialClassDialogueBox.warn("The social class name cannot be blank");
				return;
			}
			if (isSocialClass(socialClassField.getText())) {
				socialClassDialogueBox.warn("A social class with this name already exists");
			}
			EditableSocialClass newsocialClass = EditableSocialClass.makeSocialClass(socialClassField.getText(), 0);
			Editor.socialClassData.add(newsocialClass);
			Editor.socialClassTable.refresh();
			socialClassDialogueBox.hideDialogue();
		}
	};

	/**
	 * Disable or enable the controls, tabs and tables- used when an xxxDialogueBox is open, to give the effect
	 * of a modal dialogue without the pother and complexity of a new modal window.
	 * TODO work out how to do this using inheritance, selectively
	 * TODO at present just for the industry pane
	 * 
	 * @param disabled
	 *            true if the controls are to be disabled, false if they should be enabled
	 */
	public static void setControlsDisabled(boolean disabled) {
		ecb.setDisable(disabled);
	}

	/**
	 * Wrap the editor's observable entities in an instance of oneProject, for exporting or importing
	 * 
	 * @return a OneProject entity with all the editor entities stored in it as 'floating' JPA entities
	 *         that have not been persisted. These are purely transient objects which mediate between the editor
	 *         and the database, and should not be managed or persisted
	 */
	public static OneProject wrappedOneProject() {
		OneProject oneProject = new OneProject();

		ArrayList<Commodity> commodities = new ArrayList<Commodity>();
		ArrayList<Industry> industries = new ArrayList<Industry>();
		ArrayList<SocialClass> socialClasses = new ArrayList<SocialClass>();
		ArrayList<Stock> stocks = new ArrayList<Stock>();
		ArrayList<TimeStamp> timeStamps = new ArrayList<TimeStamp>();

		// The timeStamp - actually a list, even though in this case it only has one member
		TimeStamp timeStamp = new TimeStamp(1, 0, 1, "Revenue", 1, "Start");
		timeStamp.setPopulationGrowthRate(editableTimeStamp.getPopulationGrowthRate());
		timeStamp.setInvestmentRatio(editableTimeStamp.getInvestmentRatio());
		timeStamp.setLabourSupplyResponse(Parameters.LABOUR_RESPONSE.fromText(editableTimeStamp.getLabourSupplyResponse()));
		timeStamp.setPriceResponse(Parameters.PRICE_RESPONSE.fromText(editableTimeStamp.getPriceResponse()));
		timeStamp.setMeltResponse(Parameters.MELT_RESPONSE.fromText(editableTimeStamp.getMeltResponse()));
		timeStamp.setCurrencySymbol(editableTimeStamp.getCurrencySymbol());
		timeStamp.setQuantitySymbol(editableTimeStamp.getQuantitySymbol());
		timeStamps.add(timeStamp);
		oneProject.setTimeStamps(timeStamps);

		// the project
		Project project = new Project();
		project.setProjectID(0);
		project.setTimeStampID(1);
		project.setDescription("New Project");
		project.setTimeStampDisplayCursor(1);
		project.setTimeStampComparatorCursor(1);
		oneProject.setProject(project);

		// The commodities
		for (EditableCommodity c : commodityData) {
			Commodity pc = new Commodity();
			pc.setProjectID(0);
			pc.setTimeStampID(1);
			pc.setName(c.getName());
			pc.setTurnoverTime(c.getTurnoverTime());
			pc.setUnitValue(pc.getUnitValue());
			pc.setUnitPrice(pc.getUnitPrice());
			pc.setFunction(Commodity.FUNCTION.function(c.getFunction()));
			pc.setOrigin(Commodity.ORIGIN.origin(c.getOrigin()));
			logger.debug("Stashing the commodity called {} ", pc.name());
			commodities.add(pc);
		}
		oneProject.setCommodities(commodities);

		// The industries
		for (EditableIndustry ind : industryData) {
			Industry pind = new Industry();
			pind.setProjectID(0);
			pind.setTimeStamp(1);
			pind.setName(ind.getName());
			pind.setCommodityName(ind.getCommodityName());
			pind.setOutput(ind.getOutput());
			industries.add(pind);
			logger.debug("Saving the industry called {}, together with its money and sales stocks ", pind.name());
			// TODO retrieve the actual amounts
			Stock moneyStock = moneyStockBuilder(ind.getName(), ind.getDouble(EI_ATTRIBUTE.MONEY), OWNERTYPE.INDUSTRY);
			Stock salesStock = salesStockBuilder(ind.getName(), ind.getCommodityName(), ind.getDouble(EI_ATTRIBUTE.SALES), OWNERTYPE.INDUSTRY);
			stocks.add(salesStock);
			stocks.add(moneyStock);
			for (EditableStock ps : ind.getProductiveStocks().values()) {
				Stock pps = productiveStockBuilder(ind.getName(), ps.getName(), ps.getDesiredQuantity(), ps.getActualQuantity());
				logger.debug(" Stashing the  stock called {} belonging to industry {}", pps.name(), pind.name());
				stocks.add(pps);
			}
		}
		oneProject.setIndustries(industries);

		// the Social Classes
		for (EditableSocialClass sc : socialClassData) {
			SocialClass psc = new SocialClass();
			psc.setProjectID(0);
			psc.setTimeStamp(1);
			psc.setName(sc.getName());
			psc.setparticipationRatio(sc.getParticipationRatio());
			psc.setRevenue(psc.getRevenue());
			logger.debug("Stashing the social class called {} together with its money and sales stocks", psc.name());
			socialClasses.add(psc);
			Stock moneyStock = moneyStockBuilder(sc.getName(), sc.getDouble(ESC_ATTRIBUTE.MONEY), OWNERTYPE.CLASS);
			Stock salesStock = salesStockBuilder(sc.getName(), "Labour Power", sc.getDouble(ESC_ATTRIBUTE.SALES), OWNERTYPE.CLASS);
			stocks.add(salesStock);
			stocks.add(moneyStock);
			for (EditableStock ps : sc.getConsumptionStocks().values()) {
				Stock pps = consumptionStockBuilder(sc.getName(), ps.getName(), ps.getDesiredQuantity(), ps.getActualQuantity());
				logger.debug(" Stashing the  stock called {} belonging to class {} ", pps.name(), psc.name());
				stocks.add(pps);
			}
		}
		oneProject.setSocialClasses(socialClasses);

		// The stocks, which have all been created as we go along
		oneProject.setStocks(stocks);
		return oneProject;
	}

	/**
	 * Create and populate one persistent money Stock entity
	 * 
	 * @param owner
	 *            the owner of the stock
	 * @param actualQuantity
	 *            the actual quantity of this commodity in existence
	 * @param ownerType
	 *            the ownerType of this stock (CLASS, INDUSTRY)
	 * @return one persistent Stock entity
	 */
	public static Stock moneyStockBuilder(String owner, double actualQuantity, OWNERTYPE ownerType) {
		Stock moneyStock = new Stock();
		moneyStock.setTimeStamp(1);
		moneyStock.setProjectID(0);
		moneyStock.setCommodityName("Money");
		moneyStock.setStockType("Money");
		moneyStock.setOwner(owner);
		moneyStock.setOwnerType(ownerType);
		moneyStock.setQuantity(actualQuantity);
		return moneyStock;
	}

	/**
	 * Create and populate one persistent sales Stock entity
	 * 
	 * @param owner
	 *            the owner of the stock, which may be a class or an industry
	 * @param commodityName
	 *            the name of its commodity
	 * @param actualQuantity
	 *            the actual quantity of this commodity in existence
	 * @param ownerType
	 *            the type of the owner (CLASS or INDUSTRY)
	 * @return one persistent Stock entity
	 */
	public static Stock salesStockBuilder(String owner, String commodityName, double actualQuantity, OWNERTYPE ownerType) {
		Stock salesStock = new Stock();
		salesStock.setTimeStamp(1);
		salesStock.setProjectID(0);
		salesStock.setStockType("Sales");
		salesStock.setOwner(owner);
		salesStock.setQuantity(actualQuantity);
		salesStock.setCommodityName(commodityName);
		salesStock.setOwnerType(ownerType);
		return salesStock;
	}

	/**
	 * Checks whether a commodity already exists
	 * 
	 * @param commodityName
	 *            the name of the commodity to check
	 * @return true if commodityName is in the current list of commodities, false otherwise
	 */
	public static boolean isCommodity(String commodityName) {
		for (EditableCommodity e : commodityData) {
			if (e.getName().equals(commodityName))
				return true;
		}
		return false;
	}

	/**
	 * Checks whether an industry already exists
	 * 
	 * @param industryName
	 *            the name of the industry to check
	 * @return true if industryName is in the current list of industries, false otherwise
	 */
	public static boolean isIndustry(String industryName) {
		for (EditableIndustry i : industryData) {
			if (i.getName().equals(industryName))
				return true;
		}
		return false;
	}

	/**
	 * @param socialClassName
	 *            the name of the social class to check
	 * @return true if socialClass is in the current list of socialClasses, false otherwise
	 */
	public static boolean isSocialClass(String socialClassName) {
		for (EditableSocialClass s : socialClassData) {
			if (s.getName().equals(socialClassName))
				return true;
		}
		return false;
	}

	/**
	 * Create and populate one persistent production Stock entity
	 * 
	 * @param owner
	 *            the owner of the stock (which will be an industry)
	 * @param commodityName
	 *            the name of its commodity
	 * @param desiredQuantity
	 *            the quantity of this commodity required, given the output level
	 * @param actualQuantity
	 *            the actual quantity of this commodity in existence
	 * @return one persistent Stock entity
	 */
	public static Stock productiveStockBuilder(String owner, String commodityName, double desiredQuantity, double actualQuantity) {
		Stock productiveStock = new Stock();
		productiveStock.setTimeStamp(1);
		productiveStock.setProjectID(0);
		productiveStock.setStockType("Productive");
		productiveStock.setOwner(owner);
		productiveStock.setOwnerType(OWNERTYPE.INDUSTRY);
		productiveStock.setCommodityName(commodityName);
		productiveStock.setQuantity(actualQuantity);
		productiveStock.setProductionQuantity(desiredQuantity);
		return productiveStock;
	}

	/**
	 * Create and populate one persistent consumption Stock entity
	 * 
	 * @param owner
	 *            the owner of the stock(which will be a social class)
	 * @param commodityName
	 *            the name of its commodity
	 * @param desiredQuantity
	 *            the quantity of this commodity required, given the size of the class that owns it
	 * @param actualQuantity
	 *            the actual quantity of this commodity in existence
	 * @return one persistent Stock entity
	 */
	public static Stock consumptionStockBuilder(String owner, String commodityName, double desiredQuantity, double actualQuantity) {
		Stock consumptionStock = new Stock();
		consumptionStock.setTimeStamp(1);
		consumptionStock.setProjectID(0);
		consumptionStock.setStockType("Consumption");
		consumptionStock.setOwner(owner);
		consumptionStock.setOwnerType(OWNERTYPE.CLASS);
		consumptionStock.setCommodityName(commodityName);
		consumptionStock.setQuantity(actualQuantity);
		consumptionStock.setConsumptionQuantity(desiredQuantity);
		return consumptionStock;
	}

	/**
	 * Clear the decks. Empty all the data lists
	 */
	public static void clearDecks() {
		commodityData = FXCollections.observableArrayList();
		commodityTable.setItems(commodityData);
		industryData = FXCollections.observableArrayList();
		industryTable.setItems(industryData);
		socialClassData = FXCollections.observableArrayList();
		socialClassTable.setItems(socialClassData);
		editableTimeStamp = new EditableTimeStamp();
		refresh();
	}

	/**
	 * @return an observable list of commodities (used to populate the commodities table)
	 */
	public static ObservableList<EditableCommodity> getCommodityData() {
		return commodityData;
	}

	/**
	 * Tells which tab is selected.
	 * 
	 * @return an enum representing which tab has been selected (COMMODITY, INDUSTRY, SOCIALCLASS)
	 */
	public static TAB_SELECTION selectedTab() {
		if (commodityTab.isSelected())
			return TAB_SELECTION.COMMODITY;
		if (industryTab.isSelected())
			return TAB_SELECTION.INDUSTRY;
		if (socialClassTab.isSelected())
			return TAB_SELECTION.SOCIALCLASS;
		return TAB_SELECTION.UNKNOWN;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public static void setCommodityData(ObservableList<EditableCommodity> data) {
		commodityData = data;
		commodityTable.setItems(commodityData);
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public static void setIndustryData(ObservableList<EditableIndustry> data) {
		industryData = data;
		industryTable.setItems(industryData);
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public static void setSocialClassData(ObservableList<EditableSocialClass> data) {
		socialClassData = data;
		socialClassTable.setItems(socialClassData);
	}

	/**
	 * @return the industryData
	 */
	public static ObservableList<EditableIndustry> getIndustryData() {
		return industryData;
	}

	/**
	 * @return the socialClassData
	 */
	public static ObservableList<EditableSocialClass> getSocialClassData() {
		return socialClassData;
	}

}
