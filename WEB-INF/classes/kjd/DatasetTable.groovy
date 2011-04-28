package kjd

import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.data.*;
import com.vaadin.ui.Form;

import com.vaadin.ui.VerticalLayout;

import com.vaadin.ui.Table;


public class DatasetTable extends Table{
	
	def featuresVsDatasetMap
	def bioint
	
	def ID2Name = [:]
	
	def err = System.err // sugar
	
	def DatasetTable(){
		super();
	}
		
	def setDB(bioint){
		this.bioint = bioint
	}	
		
	/*****************************
	* Creates a Vaadin table displaying all of the datasets in the 
	* biointegrator database.  This table is used to select specific 
	* datasets to view.  
	*/ 
	def DatasetTable(bioint){
			super("");
			
			this.bioint = bioint
			
			// Get clinical data for datasetName
			def featuresVsDatasetMap = updateDatasetMap()	
								
			setWidth("1200px");
		  setHeight("150px");
			// turn on column reordering and collapsing
			setColumnReorderingAllowed(true);
			setColumnCollapsingAllowed(true);	
			setSelectable(true);
			setImmediate(true);

			def features = featuresVsDatasetMap.rowKeySet()
					
			for(feature in features){
				//err.println "Adding $feature as column to table."				
				
				if (feature == "NumSamples"){				
					addContainerProperty(feature as String,Integer.class,null);				
				}else{
					addContainerProperty(feature as String,String.class,null);				
				}
			}
			
			// Always add a Sample column.
			addContainerProperty("TableName" as String,String.class,null);			
										
			//err.println "addListener()"
			//addListener(new DatasetValueChangeListener())	
			//addListener((Property.ValueChangeListener) this);	
			//err.println "done addListener()"
										
			//setFooterVisible(true);			
		//	setColumnFooter("Sample","Mean/StDev:");			
			populateTable()			
	}
	
	
	
	
	/************************
	* Perform DB query and update table of values
	*/
	def populateTable(){		
		
		removeAllItems()
		
		def idx = 0;		
		def datasets = featuresVsDatasetMap.colKeySet()			
		datasets.each{dataset->
			def row = []			
			def features = featuresVsDatasetMap.rowKeySet()
			features.each{feature-> 				
				def val
				if (feature == "NumSamples"){
					val = featuresVsDatasetMap[feature][dataset] as Integer
				}else{
					val = featuresVsDatasetMap[feature][dataset]
				}			
				row << val
			}
			row << dataset	
			ID2Name[idx] = dataset // Record the mapping so we can look it up later...	
			addItem(row as Object[],new Integer(idx));
			idx++;			
//			err.println "addItem ${row as Object[]} row: $idx"			
		}
	}
	
	def getSelectedDatasetName(){
		def idx = this.getValue()
		return(ID2Name[idx])
	}
	
	/********************
	* Query the database and get a table of features vs samples...
	*/ 
	def updateDatasetMap(datasetName){
		System.err.print "Reading dataset data..."
		featuresVsDatasetMap = bioint.getDataSetInfoTwoDMap()
		System.err.println "done. ${featuresVsDatasetMap.rows()} features x ${featuresVsDatasetMap.cols()} samples"
		return(featuresVsDatasetMap)
	}
	
//	public void valueChange(ValueChangeEvent event) {
//		err.println "DatasetTable valueChange called: "+this.getValue()		
//	}
}