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
package capitalism.editor.model;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import capitalism.controller.Simulation;
import capitalism.editor.EditorControlBar;
import capitalism.editor.EditorLoader;
import capitalism.model.Commodity.FUNCTION;
import capitalism.reporting.Dialogues;
import capitalism.model.SocialClass;
import capitalism.model.Stock;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

public class EditableSocialClass {
	private static final Logger logger = LogManager.getLogger("EditableSocialClass");

	private StringProperty name;
	private DoubleProperty size;
	private DoubleProperty participationRatio;
	private DoubleProperty revenue;
	private EditableStock money;
	private EditableStock sales;
	private HashMap<String, EditableStock> consumptionStocks;

	public enum ESC_ATTRIBUTE {
		NAME("Class Name"), SIZE("Population"), PR("Participation Ratio"), REVENUE("Revenue"), MONEY("Money"), SALES("Sales Inventory"), CONSUMPTION(
				"Consumer Goods");
		protected String text;

		private ESC_ATTRIBUTE(String text) {
			this.text = text;
		}
	}

	/**
	 * Create an empty new EditableSocialClass entity
	 */
	public EditableSocialClass() {
		name = new SimpleStringProperty();
		revenue = new SimpleDoubleProperty();
		size=new SimpleDoubleProperty();
		participationRatio = new SimpleDoubleProperty();
		consumptionStocks = new HashMap<String, EditableStock>();
	}

	/**
	 * Create a populated EditableSocialClass entity and also create its salesStock, moneyStock and consumptionStock
	 * 
	 * @param name
	 *            the name of the social Class
	 * @param size
	 *            the size(population) of this social class
	 * @param participationRatio
	 *            the proportion of this class that sells labour power
	 * @return the populated EditableSocialClass
	 */
	public static EditableSocialClass makeSocialClass(String name,  double size, double participationRatio) {
		EditableSocialClass socialClass = new EditableSocialClass();
		socialClass.setName(name);
		socialClass.setSize(size);
		socialClass.setParticipationRatio(participationRatio);
		socialClass.money = new EditableStock("Money");
		socialClass.sales = new EditableStock("Labour Power");
		for (EditableCommodity e : EditorLoader.getCommodityData()) {
			logger.debug("Trying the consumption stock {}", e.getName());
			if (e.getFunction().equals(FUNCTION.CONSUMER_GOOD.text())) {
				logger.debug("Creating a consumption stock of {} for social class {}", e.getName(), name);
				socialClass.consumptionStocks.put(e.getName(), new EditableStock(e.getName()));
			}
		}
		return socialClass;
	}

	/**
	 * Create an observable list of EditableSocialClass entities (normally for display in the SocialClass Table) from the
	 * project identified by the current projectID and timeStamp.
	 * 
	 * @param projectID
	 *            the ID of the project containing the social classes whose observable representation is to be constructed
	 * @return an observableList of EditableSocialClass entities identified by projectID and its current timeStampID
	 */
	public static ObservableList<EditableSocialClass> editableSocialClasses(int projectID) {
		ObservableList<EditableSocialClass> result = FXCollections.observableArrayList();
		for (SocialClass sc : SocialClass.all(projectID, Simulation.timeStampIDCurrent())) {
			EditableSocialClass oneRecord = new EditableSocialClass();
			oneRecord.setName(sc.name());
			oneRecord.setSize(sc.getSize());
			oneRecord.setParticipationRatio(sc.getparticipationRatio());
			oneRecord.setRevenue(sc.getRevenue());
			oneRecord.sales = new EditableStock("Labour Power");
			oneRecord.money = new EditableStock("Money");
			result.add(oneRecord);
		}
		return result;
	}

	/**
	 * Set one or other of the fields of this social class entity, depending on ESC_ATTRIBUTE
	 * 
	 * @param attribute
	 *            the attribute selects which field to modify (PR, REVENUE, MONEY, SALES)
	 * 
	 * @param d
	 *            the value to be placed in the field
	 */
	public void setDouble(ESC_ATTRIBUTE attribute, double d) {
		switch (attribute) {
		case PR:
			participationRatio.set(d);
			break;
		case SIZE:
			size.set(d);
		case REVENUE:
			revenue.set(d);
			break;
		case MONEY:
			money.getActualQuantityProperty().set(d);
			break;
		case SALES:
			sales.getActualQuantityProperty().set(d);
			break;
		default:
		}
	}

	/**
	 * 
	 * @param attribute
	 *            the attribute selects which field to retrieve (PR, REVENUE, MONEY, SALES)
	 * @return a numeric field selected by the attribute (PR, REVENUE, MONEY, SALES)
	 */
	public double getDouble(ESC_ATTRIBUTE attribute) {
		switch (attribute) {
		case PR:
			return participationRatio.get();
		case SIZE:
			return size.get();
		case REVENUE:
			return participationRatio.get();
		case MONEY:
			return money.getActualQuantityProperty().get();
		case SALES:
			return sales.getActualQuantityProperty().get();
		default:
			return Double.NaN;
		}
	}

	/**
	 * @param attribute
	 *            the attribute selects which field to retrieve (PR, REVENUE, MONEY, SALES)
	 * @return a text field selected by the attribute (NAME is the only text attribute right now
	 *         but this might change so provided for completeness)
	 */
	public String getString(ESC_ATTRIBUTE attribute) {
		switch (attribute) {
		case NAME:
			return getName();
		default:
			return "";
		}
	}

	/**
	 * Set one or other of the text fields of this social class entity, depending on ESC_ATTRIBUTE
	 * 
	 * @param attribute
	 *            the attribute selects which field to modify (NAME is the only text attribute right now
	 *            but this might change so provided for completeness)
	 * 
	 * @param newValue
	 *            the value to be placed in the field
	 */
	public void setString(ESC_ATTRIBUTE attribute, String newValue) {
		switch (attribute) {
		case NAME:
			name.set(newValue);
			break;
		default:
			break;
		}
	}

	/**
	 * Create a new EditableStock of the commodity called commodityName and place it in the list
	 * of consumption good stocks owned by members of this class
	 * 
	 * @param commodityName
	 *            the name of the commodity
	 */
	public void addConsumptionStock(String commodityName) {
		EditableStock stock = new EditableStock(commodityName);
		consumptionStocks.put(commodityName, stock);
	}

	/**
	 * Create a column dynamically, ready to be added to the socialClasses table, which refers to a
	 * stock owned by the social Class entity that owns this stock.
	 * 
	 * @param commodityName
	 *            the name of the commodity to which this refers
	 * @return an editable TableColumn entity that will display this stock, and allow it to be edited.
	 */
	public static TableColumn<EditableSocialClass, Double> makeStockColumn(String commodityName) {
		logger.debug("Making a stock column for the commodity called {} ", commodityName);
		TableColumn<EditableSocialClass, Double> col = new TableColumn<EditableSocialClass, Double>(commodityName);
		col.getStyleClass().add("table-column-right");
		Callback<TableColumn<EditableSocialClass, Double>, TableCell<EditableSocialClass, Double>> cellFactory;
		cellFactory = new Callback<TableColumn<EditableSocialClass, Double>, TableCell<EditableSocialClass, Double>>() {
			public TableCell<EditableSocialClass, Double> call(TableColumn<EditableSocialClass, Double> p) {
				return new EditableSocialClassCell();
			}
		};
		col.setCellValueFactory(cellData -> cellData.getValue().stockDoubleProperty(commodityName));
		col.setCellFactory(cellFactory);
		col.setOnEditCommit(
				new EventHandler<TableColumn.CellEditEvent<EditableSocialClass, Double>>() {
					@Override public void handle(TableColumn.CellEditEvent<EditableSocialClass, Double> t) {
						((EditableSocialClass) t.getTableView().getItems().get(
								t.getTablePosition().getRow())).setStockDouble(commodityName, t.getNewValue());
					}
				});
		return col;
	}

	/**
	 * Create a column for the socialClass table that refers to a numeric attribute (field) of this EditableSocialClass
	 * entity. The field is selected by the attribute parameter.
	 * 
	 * @param attribute
	 *            the attribute which selects the (fixed) field in question
	 * @return an editable TableColumn entity that will display this field, and allow it to be edited.
	 */
	public static TableColumn<EditableSocialClass, Double> makeDoubleColumn(ESC_ATTRIBUTE attribute) {
		TableColumn<EditableSocialClass, Double> col = new TableColumn<EditableSocialClass, Double>(attribute.text);
		col.getStyleClass().add("table-column-right");
		Callback<TableColumn<EditableSocialClass, Double>, TableCell<EditableSocialClass, Double>> cellFactory;
		cellFactory = new Callback<TableColumn<EditableSocialClass, Double>, TableCell<EditableSocialClass, Double>>() {
			public TableCell<EditableSocialClass, Double> call(TableColumn<EditableSocialClass, Double> p) {
				return new EditableSocialClassCell();
			}
		};
		col.setCellValueFactory(
				cellData -> cellData.getValue().doubleProperty(attribute));
		col.setCellFactory(cellFactory);
		col.setOnEditCommit(
				new EventHandler<TableColumn.CellEditEvent<EditableSocialClass, Double>>() {
					@Override public void handle(TableColumn.CellEditEvent<EditableSocialClass, Double> t) {
						((EditableSocialClass) t.getTableView().getItems().get(
								t.getTablePosition().getRow())).setDouble(attribute, t.getNewValue());
					}
				});
		return col;
	}

	/**
	 * Create a column for the socialClass table that refers to a text attribute (field) of this EditableSocialClass
	 * entity. The field is selected by the attribute parameter.
	 * 
	 * @param attribute
	 *            the attribute which selects the (fixed) field in question
	 * @return an editable TableColumn entity that will display this field, and allow it to be edited.
	 */
	public static TableColumn<EditableSocialClass, String> makeStringColumn(ESC_ATTRIBUTE attribute) {
		TableColumn<EditableSocialClass, String> col = new TableColumn<EditableSocialClass, String>(attribute.text);
		col.getStyleClass().add("table-column-right");
		Callback<TableColumn<EditableSocialClass, String>, TableCell<EditableSocialClass, String>> cellFactory = new Callback<TableColumn<EditableSocialClass, String>, TableCell<EditableSocialClass, String>>() {
			public TableCell<EditableSocialClass, String> call(TableColumn<EditableSocialClass, String> p) {
				return new EditableSocialClassStringCell();
			}
		};
		col.setCellValueFactory(
				cellData -> cellData.getValue().stringProperty(attribute));
		col.setCellFactory(cellFactory);
		col.setOnEditCommit(
				new EventHandler<TableColumn.CellEditEvent<EditableSocialClass, String>>() {
					@Override public void handle(TableColumn.CellEditEvent<EditableSocialClass, String> t) {
						((EditableSocialClass) t.getTableView().getItems().get(
								t.getTablePosition().getRow())).setString(attribute, t.getNewValue());
					}
				});
		return col;
	}

	private ObservableValue<Double> doubleProperty(ESC_ATTRIBUTE attribute) {
		switch (attribute) {
		case PR:
			return participationRatio.asObject();
		case SIZE:
			return size.asObject();
		case REVENUE:
			return revenue.asObject();
		case SALES:
			return sales.getActualQuantityProperty().asObject();
		case MONEY:
			return money.getActualQuantityProperty().asObject();
		default:
			return new SimpleDoubleProperty(Double.NaN).asObject();
		}
	}

	private ObservableValue<String> stringProperty(ESC_ATTRIBUTE attribute) {
		switch (attribute) {
		case NAME:
			return name;
		default:
			return new SimpleStringProperty("");
		}
	}

	private ObservableValue<Double> stockDoubleProperty(String commodityName) {
		EditableStock stock = consumptionStocks.get(commodityName);
		if (stock == null) {
			logger.debug("The stock of {} in social class {} does not exist", commodityName, name.get());
			return new SimpleDoubleProperty(-99).asObject();
		}
		if (EditorControlBar.displayActuals()) {
			return stock.getActualQuantityProperty().asObject();
		} else {
			return stock.getDesiredQuantityProperty().asObject();
		}
	}

	private void setStockDouble(String commodityName, Double newValue) {
		EditableStock stock = consumptionStocks.get(commodityName);
		if (EditorControlBar.displayActuals()) {
			stock.setActualQuantity(newValue);
		} else {
			stock.setDesiredQuantity(newValue);
		}
	}

	private static class EditableSocialClassStringCell extends TableCell<EditableSocialClass, String> {
		private TextField textField;

		public EditableSocialClassStringCell() {
		}

		@Override public void startEdit() {
			super.startEdit();
			if (textField == null) {
				createTextField();
			}
			setGraphic(textField);
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			textField.selectAll();
		}

		@Override public void cancelEdit() {
			super.cancelEdit();
			setText(String.valueOf(getItem()));
			setContentDisplay(ContentDisplay.TEXT_ONLY);
		}

		@Override public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (textField != null) {
						textField.setText(getString());
					}
					setGraphic(textField);
					setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				} else {
					setText(getString());
					setContentDisplay(ContentDisplay.TEXT_ONLY);
				}
			}
		}

		private void createTextField() {
			textField = new TextField(getString());
			textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			textField.setOnKeyPressed(new EventHandler<KeyEvent>() {

				@Override public void handle(KeyEvent t) {
					if (t.getCode() == KeyCode.ENTER) {
						commitEdit(textField.getText());
					} else if (t.getCode() == KeyCode.ESCAPE) {
						cancelEdit();
					}
				}
			});
		}
		private String getString() {
			String value = getItem() == null ? "" : getItem().toString();
			return value;
		}
	}

	private static class EditableSocialClassCell extends TableCell<EditableSocialClass, Double> {
		private TextField textField;

		public EditableSocialClassCell() {
		}

		@Override public void startEdit() {
			super.startEdit();
			if (textField == null) {
				createTextField();
			}
			setGraphic(textField);
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			textField.selectAll();
		}

		@Override public void cancelEdit() {
			super.cancelEdit();
			setText(String.valueOf(getItem()));
			setContentDisplay(ContentDisplay.TEXT_ONLY);
		}

		@Override public void updateItem(Double item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (textField != null) {
						textField.setText(getString());
					}
					setGraphic(textField);
					setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				} else {
					setText(getString());
					setContentDisplay(ContentDisplay.TEXT_ONLY);
				}
			}
		}

		private void createTextField() {
			textField = new TextField(getString());
			textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			textField.setOnKeyPressed(new EventHandler<KeyEvent>() {

				@Override public void handle(KeyEvent t) {
					if (t.getCode() == KeyCode.ENTER) {
						commitEdit(Double.parseDouble(textField.getText()));
					} else if (t.getCode() == KeyCode.ESCAPE) {
						cancelEdit();
					}
				}
			});
		}

		private String getString() {
			String value = getItem() == null ? "" : getItem().toString();
			return value;
		}
	}

	public void loadStocksFromSimulation(int projectID) {
		SocialClass persistentSocialClass = SocialClass.single(projectID, Simulation.timeStampIDCurrent(), name.get());
		try {
			// A simple dodge: since there is no difference between desired and actual
			// money and sales stocks, set both to be the same
			Stock moneyStock = persistentSocialClass.moneyStock();
			money.setActualQuantity(moneyStock.getQuantity());
			money.setDesiredQuantity(moneyStock.getQuantity());
			Stock salesStock = persistentSocialClass.salesStock();
			sales.setActualQuantity(salesStock.getQuantity());
			sales.setDesiredQuantity(salesStock.getQuantity());
			for (Stock consumptionStock : persistentSocialClass.consumptionStocks()) {
				logger.debug("Loading the consumptionStock called {} belonging to the social Class called {}", consumptionStock.name(), name.get());
				EditableStock s = consumptionStocks.get(consumptionStock.name());
				s.setActualQuantity(consumptionStock.getQuantity());
				s.setDesiredQuantity(consumptionStock.getConsumptionQuantity());
			}
		} catch (NullPointerException e) {
			logger.debug("Null pointer error trapped");
			Dialogues.alert(logger, "The class called " + persistentSocialClass.name() + " is missing a stock. This is most likely a data error");
		}
	}

	public static void loadAllStocksFromSimulation(int projectID) {
		for (EditableSocialClass socialClass : EditorLoader.getSocialClassData()) {
			socialClass.loadStocksFromSimulation(projectID);
		}
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	/**
	 * @return the participationRatio
	 */
	public double getParticipationRatio() {
		return participationRatio.get();
	}

	/**
	 * @param participationRatio
	 *            the participationRatio to set
	 */
	public void setParticipationRatio(double participationRatio) {
		this.participationRatio.set(participationRatio);
	}
	/**
	 * @return the size
	 */
	public double getSize() {
		return size.get();
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(double size) {
		this.size.set(size);
	}


	/**
	 * @return the revenue
	 */
	public Double getRevenue() {
		return revenue.get();
	}

	/**
	 * @param revenue
	 *            the revenue to set
	 */
	public void setRevenue(double revenue) {
		this.revenue.set(revenue);
	}

	/**
	 * @return the consumptionStocks
	 */
	public HashMap<String, EditableStock> getConsumptionStocks() {
		return consumptionStocks;
	}

	/**
	 * @param consumptionStocks
	 *            the consumptionStocks to set
	 */
	public void setConsumptionStocks(HashMap<String, EditableStock> consumptionStocks) {
		this.consumptionStocks = consumptionStocks;
	}

}
