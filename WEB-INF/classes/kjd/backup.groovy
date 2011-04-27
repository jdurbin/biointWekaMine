package kjd

import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.data.*;
import com.vaadin.ui.Form;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

import com.vaadin.ui.Table;

import org.vaadin.ui.JFreeChartWrapper;

import groovy.sql.Sql
import durbin.util.*
import durbin.weka.*
import durbin.bioInt.*
import durbin.bioInt.BioIntDB
import durbin.charts.*

import org.jfree.data.xy.*;
import org.jfree.ui.*;
import org.jfree.chart.title.*;
import org.jfree.chart.axis.*;
import org.jfree.data.statistics.*;

//Button.ClickListener,
@SuppressWarnings("serial")
public class redteam extends com.vaadin.Application 
implements  Property.ValueChangeListener, TabSheet.SelectedTabChangeListener
{
			
	// UI components
	private Label label;
	private Window window;
	private final ListSelect attributeList = new ListSelect("Select attributes to classify on:");
	private final ComboBox datasetBox = new ComboBox("Please select a dataset:");
	String NOTIFICATION = "BUTTON PRESSED";
	Table table;
	TabSheet ts;
	def l1
	def l2
	def l3
	
	// Control
	private boolean toggle = true;
	def datasetName="chin2006Exp";
	
	// IO	
	def bioint
	def err = System.err // sugar
		
	// Data
	def featuresVsSamplesMap
	def dataTables

	/****************************
	* Called by vaadin Application entry point to set up GUI. 
	*/ 
	public void init() {		
		
		try{
		window = new Window("RED TEAM", new Panel());
		setMainWindow(window);
		
		//label = new Label('<FONT COLOR="#0000FF"><h2>BLUE TEAM TABBED!</h2>', Label.CONTENT_RAW);		
		//window.addComponent(label);
		
		//------------------------------------------------------------
		// Connect to DB
		def confName = ".hg.conf.entropy"
		def dbName = "bioInt"
		bioint = new BioIntDB(confName,3306,dbName)
	
		//--------------------------------------------------------
		// Get list of data tables and create ComboBox listing them...
		def dataTables = bioint.getDataTables()
		err.println("dataTables.size= ${dataTables.size()}")
		//		dataTables.each{System.err.println it}
		for(int i = 0;i < dataTables.size();i++){
			def tableName = dataTables[i] as String
			datasetBox.addItem(tableName)
		}		
		datasetBox.setFilteringMode(Filtering.FILTERINGMODE_OFF);
		datasetBox.setImmediate(true);
		datasetBox.addListener(this);
		window.addComponent(datasetBox);
	
		Label brlabel =  new Label("<br><br><br>", Label.CONTENT_RAW);
		window.addComponent(brlabel);

		//-----------------------------------------------------------
		// Add table of results...
		// 
		// Just a place holder for now...
		table = new Table("Clinical data...");

		//----------------------------------------------------------
		// Create some tabs to hold different views of bioInt DB
		// 
		l1 = new VerticalLayout();
		l1.setMargin(true)
		l1.addComponent(table)
	 	l2 = new VerticalLayout();
		l2.setMargin(true)
		
		def values = [1,1,2,2,3,3,3,3,4,4,4,4,4,4,4,4,4,5,5,5,5,5,5,5,5,5,5,5,6,6,6,6,7,7,8,8,8,8,8,9,9,9,9,10]		
		testChart = createHistogramFromValues(values,10,"Hello Chart")		
		l2.addComponent(new JFreeChartWrapper(testChart))
		l3 = new VerticalLayout();
		l3.setMargin(true)
		l3.addComponent(new Label("MORE XRBLK"))

		ts = new TabSheet();
		ts.setHeight("600px")
		ts.setWidth("100%")
		ts.addTab(l1,"Clinical Data",null)
		ts.addTab(l2,"Pre-run classifiers",null)
		ts.addTab(l3,"Run custom classifier",null)
		ts.addListener(this);
		window.addComponent(ts)
				
		window.showNotification("SETUP COMPLETE");	
		
	}catch(Exception e){
		
		System.err.println e
		//if (pf != null) println "pretend disconnect"//pf.disconnect()
	}
	}
	
	
	/*******************************
	* Handle tab change events...
	*/
	public void selectedTabChange(SelectedTabChangeEvent event) {
			TabSheet tabsheet = event.getTabSheet();
			if (tabsheet != null){
				Tab tab = tabsheet.getTab(tabsheet.getSelectedTab());
				if (tab != null) {
					getWindow().showNotification("Selected tab: " + tab.getCaption());
	    	}else{
					err.println "selected tab from tab sheet is null"
				}
			}else{
				err.println "tabsheet from event is null."
			}
	 }
	
		
	/*******************************
	* add a table of results.
	*/
	 public void valueChange(Property.ValueChangeEvent event) {
			String msg = event.getProperty() as String
			getMainWindow().showNotification("Dataset selected:"+msg as String)
			datasetName = msg
			// get the data from the DB...
			featuresVsSamplesMap = updateClinicalData(datasetName)
			
			// Create a new table with appropriate rows and columns...
			//window.removeComponent(table);
			l1.removeComponent(table)
			table = createClinicalTable(featuresVsSamplesMap)
			l1.addComponent(table)			
			
			table.requestRepaint()
	 }	
	
	/*****************************
	* Creates a Vaadin table with features for columns 
	* and samples as row labels...
	*/ 
	def createClinicalTable(featuresVsSamplesMap){
			table = new Table(datasetName);
					
			table.setWidth("100%");
		  table.setHeight("300px");
			// turn on column reordering and collapsing
			table.setColumnReorderingAllowed(true);
			table.setColumnCollapsingAllowed(true);	
			table.setSelectable(true);

			def features = featuresVsSamplesMap.rowKeySet()
			
			// Create the table columns.  Vaadin works, but the rendering of other components gets
			// laggy if there are too many columns... 
			//def featuresubset
			//if (features.size() > 20) featuresubset = (features as List)[0..20]
			//else featuresubset = features
			
			// Always add a Sample column.
			table.addContainerProperty("Sample" as String,String.class,null);
			
			def columnNum = 0;
			for(feature in features){
				err.println "Adding $feature as column to table."				
				table.addContainerProperty(feature as String,Double.class,null);
				
				// Collapse all but the first 10 columns (for performance)
				if (columnNum > 10) table.setColumnCollapsed(feature as String,true);
				columnNum++
			}	
						
			table.setFooterVisible(true);			
			table.setColumnFooter("Sample","Mean/StDev:");
			
			populateClinicalTable(table,featuresVsSamplesMap)
			
			return(table)
	}
	
	
	/************************
	* Perform DB query and update table of values
	*/
	def populateClinicalTable(table,featuresVsSamplesMap){	
		
		err.println "populateTable"
		
		table.removeAllItems()
		
		def idx = 0;		
		def samples = featuresVsSamplesMap.colKeySet()			
		samples.each{sample->
			def row = []			
			def features = featuresVsSamplesMap.rowKeySet()
			
		//	def featuresubset
		//	if (features.size() > 20) featuresubset = (features as List)[0..20]
		//	else featuresubset = features	
			row << sample		
			features.each{feature-> 
				def val = featuresVsSamplesMap[feature][sample]
				if (val.class != Double) val = Double.NaN
				err.println "val: $val  ${val.class}"				
				row << val
			}
			table.addItem(row as Object[],new Integer(idx));
			idx++;			
			err.println "addItem ${row as Object[]} row: $idx"			
		}
	}
	
	/********************
	* Query the database and get a table of features vs samples...
	*/ 
	def updateClinicalData(datasetName){
		System.err.print "Reading clinical data..."
		featuresVsSamplesMap = bioint.getFeaturesVsSamplesTwoDMap(datasetName)
		System.err.println "done. ${featuresVsSamplesMap.rows()} features x ${featuresVsSamplesMap.cols()} samples"
		return(featuresVsSamplesMap)
	}


	/**
	* Perform a query returning x,y values pairs and create a plot
	* based on this query. 
	*/ 
	def createHistogramFromValues(values,binmax,cName){
		
			err.println "createHistogramFromValues"

	    series = new HistogramDataset()
	    valarray = new double[values.size()]
	    values.eachWithIndex{v,i->valarray[i] = v as double}

	    series.addSeries("Series1",valarray,20,0,binmax as int)
	    chartTitle = "$cName"
	    chartpanel = BasicCharts.histogram(chartTitle,"Value","Count",series,250,166) 
	    // Make font smaller than default...   
	
			err.println "make fancy font..."
	 
	    titleFont = new java.awt.Font("SansSerif", java.awt.Font.BOLD,9)
	    title = new org.jfree.chart.title.TextTitle(chartTitle,titleFont)
	    chart = chartpanel.getChart()
	    chart.setTitle(title);

	    return(chartpanel)

	    // OK, see if it improves performance to precompute each image and discard
	    // the chart... Answer:  definitely. 
	    //image = ImageUtils.getPanelImage(chartpanel)
	    //label = new JLabel(new ImageIcon(image))    
	    //return(label)
	}
	
}