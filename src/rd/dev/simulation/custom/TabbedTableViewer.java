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

package rd.dev.simulation.custom;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import rd.dev.simulation.Capitalism;
import rd.dev.simulation.datamanagement.DataManager;
import rd.dev.simulation.datamanagement.ObservableListProvider;
import rd.dev.simulation.model.Circuit;
import rd.dev.simulation.model.SocialClass;
import rd.dev.simulation.model.Stock;
import rd.dev.simulation.model.UseValue;
import rd.dev.simulation.view.ViewManager;

public class TabbedTableViewer extends VBox {
	static final Logger logger = LogManager.getLogger("TableViewer");
	private ObservableListProvider olProvider = Capitalism.olProvider;

	// selects whether to display quantities, values or prices, where appropriate

	public static Stock.ValueExpression displayAttribute = Stock.ValueExpression.PRICE;

	// Stock Tables and header coulumns

	@FXML private TableView<Stock> productiveStockTable;
	@FXML private TableColumn<Stock, String> productiveStockHeaderColumn;
	@FXML private TableView<Stock> moneyStockTable;
	@FXML private TableColumn<Stock, String> moneyStockHeaderColumn;
	@FXML private TableView<Stock> salesStockTable;
	@FXML private TableColumn<Stock, String> salesStockHeaderColumn;
	@FXML private TableView<Stock> consumptionStockTable;
	@FXML private TableColumn<Stock, String> consumptionStockHeaderColumn;

	// The UseValues table and its header columns

	@FXML protected TableView<UseValue> useValuesTable;
	private TableColumn<UseValue, String> useValueDemandSupplySuperColumn;
	private TableColumn<UseValue, String> useValueCapitalProfitSuperColumn;
	private TableColumn<UseValue, String> useValueValuePriceSuperColumn;

	private TableColumn<UseValue, String> useValueTotalPriceColumn;
	private TableColumn<UseValue, String> useValueAllocationShareColumn;
	private TableColumn<UseValue, String> useValueProfitRateColumn;

	// Circuits Tables and their header columns

	@FXML private TableView<Circuit> circuitsTable;
	@FXML private TableView<Circuit> dynamicCircuitTable;
	@FXML private TableView<SocialClass> socialClassesTable;

	/**
	 * Simple static lists of tables, so utilities can get at them
	 */

	private static ArrayList<TableView<?>> tabbedTables = new ArrayList<TableView<?>>();

	public static enum HEADER_TOOL_TIPS {
		// @formatter:off
		USEVALUE("A commodity is anything that that society makes use of, and has established a quantitative measure for"), 
		INDUSTRY("A producer is a business, or group of businesses, who make one commodity with similar technologies. \n" +
				 "Two producers can make the same commodity, but would normally be distinguished apart because their technology differs.\n" +
				 "This can help study the effect of technological change"), 
		SOCIALCLASS("A social class is a group of people with the same source of revenue, defined by the type of property that they specialise in");
		// @formatter:on

		String text;

		HEADER_TOOL_TIPS(String text) {
			this.text = text;
		}

		public String text() {
			return text;
		}
	}

	/**
	 * Custom control handles the main ViewTables
	 */
	public TabbedTableViewer() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TabbedTableViewer.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		ViewManager.graphicsState = ContentDisplay.TEXT_ONLY;		// initialize so start state is text only
		setDisplayAttribute(Stock.ValueExpression.PRICE);			// start off displaying prices
		buildTables();
	}

	/**
	 * completely reconstruct all the tables from scratch
	 * Called at startup, and when switching a project (because the dynamic columns may change)
	 */

	public void buildTables() {
		makeProductiveStocksViewTable();
		makeMoneyStocksViewTable();
		makeSalesStocksViewTable();
		makeConsumptionStocksViewTable();
		makeUseValuesViewTable();
		makeCircuitsViewTable();
		makeDynamicCircuitsTable();
		makeSocialClassesViewTable();

		tabbedTables.clear();
		tabbedTables.add(productiveStockTable);
		tabbedTables.add(moneyStockTable);
		tabbedTables.add(salesStockTable);
		tabbedTables.add(salesStockTable);
		tabbedTables.add(consumptionStockTable);
		tabbedTables.add(useValuesTable);
		tabbedTables.add(circuitsTable);
		tabbedTables.add(socialClassesTable);
		tabbedTables.add(dynamicCircuitTable);

		TableUtilities.setSuperColumnHandler(useValueValuePriceSuperColumn, useValueTotalPriceColumn);
		TableUtilities.setSuperColumnHandler(useValueDemandSupplySuperColumn, useValueAllocationShareColumn);
		TableUtilities.setSuperColumnHandler(useValueCapitalProfitSuperColumn, useValueProfitRateColumn);
	}

	/**
	 * Initialize the Productive Stocks tableView and cellFactories
	 */
	public void makeProductiveStocksViewTable() {
		productiveStockHeaderColumn.getColumns().clear();
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.CIRCUIT,true));
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.USEVALUE,true));
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.QUANTITY,false));
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.VALUE,false));
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.PRICE,false));
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.PRODUCTION_COEFFICIENT,false));
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.QUANTITYDEMANDED,false));
	}

	/**
	 * Initialize the Money Stocks tableView and cellFactories
	 */
	public void makeMoneyStocksViewTable() {
		moneyStockHeaderColumn.getColumns().clear();
		moneyStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.OWNERTYPE,true));
		moneyStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.CIRCUIT,true));
		moneyStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.QUANTITY,false));
		moneyStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.VALUE,false));
		moneyStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.PRICE,false));
	}

	/**
	 * Initialize the Sales Stocks tableView and cellFactories
	 */
	public void makeSalesStocksViewTable() {
		salesStockHeaderColumn.getColumns().clear();
		salesStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.OWNERTYPE,true));
		salesStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.CIRCUIT,true));
		salesStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.USEVALUE,false));
		salesStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.QUANTITY,false));
		salesStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.VALUE,false));
		salesStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.PRICE,false));
	}

	/**
	 * Initialize the Consumption Stocks tableView and cellFactories
	 */
	public void makeConsumptionStocksViewTable() {
		consumptionStockHeaderColumn.getColumns().clear();
		consumptionStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.CIRCUIT,true));
		consumptionStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.USEVALUE,true));
		consumptionStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.QUANTITY,false));
		consumptionStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.VALUE,false));
		consumptionStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.PRICE,false));
		consumptionStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.QUANTITYDEMANDED,false));
		consumptionStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.CONSUMPTION_COEFFICIENT,false));
	}

	/**
	 * Initialize the UseValues tableView and cellFactories
	 */
	public void makeUseValuesViewTable() {
		useValuesTable.getColumns().clear();

		// Create the header columns
		// Assume Garbage collector will dispose of the detached subColumns
		useValueValuePriceSuperColumn=new TableColumn<UseValue,String>("Values and Prices");
		useValueDemandSupplySuperColumn=new TableColumn<UseValue,String>("Demand and Supply");
		useValueCapitalProfitSuperColumn=new TableColumn<UseValue,String>("CapitalAndProfit");
		
		useValuesTable.getColumns().add(new UseValueColumn(UseValue.USEVALUE_SELECTOR.USEVALUENAME, true));
		useValuesTable.getColumns().add(new UseValueColumn(UseValue.USEVALUE_SELECTOR.USEVALUETYPE, true));
		useValuesTable.getColumns().add(new UseValueColumn(UseValue.USEVALUE_SELECTOR.TOTALQUANTITY, false));
		useValuesTable.getColumns().add(new UseValueColumn(UseValue.USEVALUE_SELECTOR.TURNOVERTIME, false));

		useValuesTable.getColumns().add(useValueValuePriceSuperColumn);
		useValueValuePriceSuperColumn.getColumns().add(new UseValueColumn(UseValue.USEVALUE_SELECTOR.UNITVALUE, false));
		useValueValuePriceSuperColumn.getColumns().add(new UseValueColumn(UseValue.USEVALUE_SELECTOR.UNITPRICE, false));
		useValueValuePriceSuperColumn.getColumns().add(new UseValueColumn(UseValue.USEVALUE_SELECTOR.TOTALVALUE, false));
		useValueTotalPriceColumn = new UseValueColumn(UseValue.USEVALUE_SELECTOR.TOTALPRICE, false);
		useValueValuePriceSuperColumn.getColumns().add(useValueTotalPriceColumn);

		useValuesTable.getColumns().add(useValueDemandSupplySuperColumn);
		useValueDemandSupplySuperColumn.getColumns().add(new UseValueColumn(UseValue.USEVALUE_SELECTOR.TOTALSUPPLY, false));
		useValueDemandSupplySuperColumn.getColumns().add(new UseValueColumn(UseValue.USEVALUE_SELECTOR.TOTALDEMAND, false));
		useValueAllocationShareColumn = new UseValueColumn(UseValue.USEVALUE_SELECTOR.ALLOCATIONSHARE, false);
		useValueDemandSupplySuperColumn.getColumns().add(useValueAllocationShareColumn);

		useValuesTable.getColumns().add(useValueCapitalProfitSuperColumn);
		useValueCapitalProfitSuperColumn.getColumns().add(new UseValueColumn(UseValue.USEVALUE_SELECTOR.INITIALCAPITAL, false));
		useValueCapitalProfitSuperColumn.getColumns().add(new UseValueColumn(UseValue.USEVALUE_SELECTOR.PROFIT, false));
		useValueProfitRateColumn = new UseValueColumn(UseValue.USEVALUE_SELECTOR.PROFITRATE, false);
		useValueCapitalProfitSuperColumn.getColumns().add(useValueProfitRateColumn);
		useValueCapitalProfitSuperColumn.getColumns().add(new UseValueColumn(UseValue.USEVALUE_SELECTOR.SURPLUS, false));
	}

	/**
	 * Initialize the Social Classes tableView and cellFactories
	 */
	public void makeSocialClassesViewTable() {
		socialClassesTable.getColumns().clear();
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.Selector.SOCIALCLASSNAME,true));
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.Selector.SIZE,false));
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.Selector.SALES,false));
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.Selector.MONEY,false));
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.Selector.TOTAL,false));
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.Selector.REVENUE,false));
		for (UseValue u : DataManager.useValuesByType(UseValue.USEVALUETYPE.CONSUMPTION)) {
			socialClassesTable.getColumns().add(new SocialClassColumn(u.getUseValueName()));
		}
	}

	/**
	 * Build the Circuits tableView and cellFactories.
	 * Only call this when we want to rebuild the display from scratch, for example when starting up or switching projects
	 */
	public void makeCircuitsViewTable() {
		circuitsTable.getColumns().clear();
		circuitsTable.getColumns().add(new CircuitColumn(Circuit.Selector.PRODUCTUSEVALUENAME,true));
		circuitsTable.getColumns().add(new CircuitColumn(Circuit.Selector.INITIALCAPITAL,false));
		circuitsTable.getColumns().add(new CircuitColumn(Circuit.Selector.SALESSTOCK,false));
		circuitsTable.getColumns().add(new CircuitColumn(Circuit.Selector.PRODUCTIVESTOCKS,false));
		circuitsTable.getColumns().add(new CircuitColumn(Circuit.Selector.MONEYSTOCK,false));
		circuitsTable.getColumns().add(new CircuitColumn(Circuit.Selector.CURRENTCAPITAL,false));
		circuitsTable.getColumns().add(new CircuitColumn(Circuit.Selector.PROFIT,false));
		circuitsTable.getColumns().add(new CircuitColumn(Circuit.Selector.PROFITRATE,false));
	}

	/**
	 * Build the DynamicCircuitTable and cellFactories.
	 * Only call this when we want to rebuild the display from scratch, for example when starting up or switching projects
	 */
	private void makeDynamicCircuitsTable() {
		dynamicCircuitTable.getColumns().clear();
		dynamicCircuitTable.getColumns().add(new CircuitColumn(Circuit.Selector.PRODUCTUSEVALUENAME,true));
		dynamicCircuitTable.getColumns().add(new CircuitColumn(Circuit.Selector.PROPOSEDOUTPUT,false));
		dynamicCircuitTable.getColumns().add(new CircuitColumn(Circuit.Selector.CONSTRAINEDOUTPUT,false));
		dynamicCircuitTable.getColumns().add(new CircuitColumn(Circuit.Selector.GROWTHRATE,false));

		for (UseValue u : DataManager.useValuesByType(UseValue.USEVALUETYPE.PRODUCTIVE)) {
			dynamicCircuitTable.getColumns().add(new CircuitColumn(u.getUseValueName()));
		}
		for (UseValue u : DataManager.useValuesByType(UseValue.USEVALUETYPE.LABOURPOWER)) {
			dynamicCircuitTable.getColumns().add(new CircuitColumn(u.getUseValueName()));
		}
	}

	/**
	 * refresh the data in all the tabbed tables. Do not rebuild them.
	 * 
	 */
	public void repopulateTabbedTables() {
		productiveStockTable.setItems(olProvider.stocksByStockTypeObservable("Productive"));
		moneyStockTable.setItems(olProvider.stocksByStockTypeObservable("Money"));
		salesStockTable.setItems(olProvider.stocksByStockTypeObservable("Sales"));
		consumptionStockTable.setItems(olProvider.stocksByStockTypeObservable("Consumption"));
		useValuesTable.setItems(olProvider.useValuesObservable());
		circuitsTable.setItems(olProvider.circuitsObservable());
		socialClassesTable.setItems(olProvider.socialClassesObservable());
		dynamicCircuitTable.setItems(olProvider.circuitsObservable());
	}

	/**
	 * we have to force a refresh of the display because if the data has not changed, it may not be observed by the table
	 * see https://stackoverflow.com/questions/11065140/javafx-2-1-tableview-refresh-items
	 * i have kept this method separate from {@code populateTabbedTables()} because the issue merits further study.
	 */

	public void refreshTables() {
		for (TableView<?> table : tabbedTables) {
			table.refresh();
		}
	}

	/**
	 * switch the header displays (between graphics and text) for all our tables
	 */

	public void switchHeaderDisplays() {
		TableUtilities.switchHeaderDisplays(tabbedTables);
	}

	/**
	 * @param displayAttribute
	 *            the displayAttribute to set
	 */
	public void setDisplayAttribute(Stock.ValueExpression displayAttribute) {
		TabbedTableViewer.displayAttribute = displayAttribute;
	}

}