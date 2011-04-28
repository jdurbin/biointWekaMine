package kjd

import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.data.*;
import com.vaadin.ui.Form;

import com.vaadin.event.ItemClickEvent.ItemClickListener;

import com.vaadin.ui.VerticalLayout;

import com.vaadin.ui.Table;


/*********************************************
* A Vaadin Table showing clinical data for a given dataset. 
* 
* Note: latency of other GUI elements increases as the number of 
* columns in the table increase.  One can easily create a table 
* with ten thousand rows with no performance hit, but a hundred
* columns will cause noticable degredation.  Needs lazy-loading
* for columns as well as rows but current vaadin component doesn't
* support that.  In the interim, I only display first ten features 
* leaving the others to be user selectable.  
* 
*/ 
public class ClinicalDataTable extends Table{
	
	def featuresVsSamplesMap
	def bioint
	def datasetName
	
	def err = System.err // sugar
	
	def ClinicalDataTable(){
		super();
	}		
		
	/*****************************
	* Creates a Vaadin table with features for columns 
	* and samples as row labels... Assumes bioint has already 
	* been connected to the database by the controlling app. 
	*/ 
	def ClinicalDataTable(bioint,datasetName){
			//super(datasetName);
			
			this.bioint = bioint
			this.datasetName = datasetName
			
			// Get clinical data for datasetName from the bioIntegrator 
			// database.  
			featuresVsSamplesMap = updateClinicalData(datasetName)	
								
			//setWidth("1150px");
			setWidth("100%")
		  setHeight("350px");
			// turn on column reordering and collapsing
			setColumnReorderingAllowed(true);
			setColumnCollapsingAllowed(true);	
			//setSelectable(true);
			//setImmediate(true);

			def features = featuresVsSamplesMap.rowKeySet()
			
			// Always add a Sample column.
			addContainerProperty("Sample" as String,String.class,null);
			
			def columnNum = 0;
			for(feature in features){
			//	err.println "Adding $feature as column to table."				
				addContainerProperty(feature as String,Double.class,null);
				
				// Collapse all but the first 10 columns (for performance)
				if (columnNum > 10) setColumnCollapsed(feature as String,true);
				columnNum++
			}							
			//setFooterVisible(true);			
			//setColumnFooter("Sample","Mean/StDev:");			
			populateTable()			
	}
	
	
	
	/************************
	* populate table with prefetched results
	*/
	def populateTable(){		
		
		removeAllItems()
		
		def idx = 0;		
		def samples = featuresVsSamplesMap.colKeySet()			
		samples.each{sample->
			def row = []			
			def features = featuresVsSamplesMap.rowKeySet()
	
			row << sample		
			features.each{feature-> 
				def val = featuresVsSamplesMap[feature][sample]
				if (val.class != Double) val = Double.NaN
				//err.println "val: $val  ${val.class}"				
				row << val
			}
			addItem(row as Object[],new Integer(idx));
			idx++;			
			//err.println "addItem ${row as Object[]} row: $idx"			
		}
	}
	
	
	/***
	* Retrieves the values for a named column
	*/ 
	def getValuesForColumn(String colName){			
			def values = []
			
			def samples = featuresVsSamplesMap.colKeySet() 
			for(sample in samples){
				def val = featuresVsSamplesMap[colName][sample]
				if (val.class == String) return(null)  // Column we shouldn't be trying to plot
				if (val.class == Double){  // NaN or missing values...
					values << val;
				}
			}
			return(values)			
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
	
}