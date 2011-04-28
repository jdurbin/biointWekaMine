package kjd

import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.data.*;
import com.vaadin.ui.Form;

import com.vaadin.event.Action;

import com.vaadin.ui.VerticalLayout;

import com.vaadin.ui.Table;

import durbin.weka.*

/****************
TODO: 

reorder columns...
confusion matrix

hide columns id, dataset_id
order columns pctcorrect, roc, classattribute, classifier, attriburteeval, attributesearch, samples, etc.

column for cutoff type (string)

*/ 


/*********************************************
* A Vaadin Table showing wekaMine results for a given dataset. 
* 
* Would be nice to add a confusion matrix to the display!!
*/ 
public class  WekaMineTable extends Table 
implements Action.Handler{
	
	def wekaMineResults
	def bioint
	def datasetName
	
	def err = System.err // sugar
	
	def colName2Idx = [:]
	def limitRows = "ALL"
	def limitRow2Row = [:]  // When viewing subset of rows, need to map idx
	
	def limitAction 
	def allAction 
	def limitClass
		
	
	def WekaMineTable(){
		super();
	}
	
	def columnOrder = [

	]
	
		
	/*****************************
	* Creates a Vaadin table with features for columns 
	* and samples as row labels... Assumes bioint has already 
	* been connected to the database by the controlling app. 
	*/ 
	def WekaMineTable(bioint,datasetName){
			super(datasetName);
			
			this.bioint = bioint
			this.datasetName = datasetName
			
			// Get wekaMineResults...
			wekaMineResults = updateWekaMineData(datasetName)	
			err.println "wekaMineResults.size() = "+wekaMineResults.size()
			
			if (wekaMineResults.size() == 0) return; // Can't do anything with empty set.
								
			setWidth("730px");
		  setHeight("450px");
			// turn on column reordering and collapsing
			setColumnReorderingAllowed(true);
			setColumnCollapsingAllowed(true);	
			setSelectable(true);
			setImmediate(true);

			def resultStats = wekaMineResults[0].keySet()	as ArrayList
			def orderedresultStats = orderResultStats(resultStats)
			def columnNum = 0;						
			
			// Last value in a result is the list of top features 
			// which will be handled separately in it's own table. 
			for(stat in orderedresultStats[0..-2]){					
							
				def dataType = getTypeForStat(stat)	
				colName2Idx[stat] = columnNum																																																			
				addContainerProperty(stat as String,dataType,null);				
				// Collapse all but the first 10 columns (for performance)
				if (columnNum > 10) setColumnCollapsed(stat as String,true);								
				columnNum++
			}
			
			this.addActionHandler(this)
			
			//setColumnCollapsed("jobID",true)
			//setColumnCollapsed("dataset_id",true)
										
										
			//setFooterVisible(true);			
			//setColumnFooter("Sample","Mean/StDev:");		
			err.println "WekaMineTable created."	
			populateTable()			
	}
	
	/***
	* Change the order of the resultStats to fit my liking.
	* Hard code for the moment...
	*
	0 id
	1 dataset_id
	2 samples
	3 classAttribute
	4 pctCorrect
	5 precision0
	6 recall0
	7 precision1
	8 recall1
	9 tp1
	10 fp1
	11 tn1
	12 fn1
	13 rms
	14 roc
	15 classifier
	16 attributeEval
	17 attributeSearch
	18 numAttributes
	19 discretization
	20 features
	*/
	def orderResultStats(resultStats){
		def rval = []
		rval << resultStats[4]
		rval << resultStats[14]
		rval << resultStats[3]
		rval << "Cl Type"		
		rval << resultStats[19]
		rval << resultStats[2]
		rval << resultStats[5]
		rval << resultStats[6]
		rval << resultStats[16]
		rval << resultStats[9]
		rval << resultStats[10]
		rval << resultStats[11]
		rval << resultStats[12]
		rval << resultStats[13]
		rval << resultStats[7]
		rval << resultStats[8]
		rval << resultStats[15]
		rval << resultStats[0]
		rval << resultStats[1]
		rval << resultStats[17]
		rval << resultStats[18]
		rval << resultStats[20]	
		return(rval)	
	}
	
	def orderResults(results){
			def rval = []
			rval << results[4]
			rval << results[14]
			rval << results[3]
			rval << WekaNames.getBaseClassifierType(results[15])	
			rval << results[19]
			rval << results[2]
			rval << results[5]
			rval << results[6]
			rval << results[16]
			rval << results[9]
			rval << results[10]
			rval << results[11]
			rval << results[12]
			rval << results[13]
			rval << results[7]
			rval << results[8]
			rval << results[15]
			rval << results[0]
			rval << results[1]
			rval << results[17]
			rval << results[18]
			rval << results[20]		
			return(rval)
	}
 
	
	/***
	* returns the type of value this statistic represents.
	*/
	def getTypeForStat(stat){
		
		switch(stat){
			
			case "jobID":
			case "dataset_id":
			case "tp1":
			case "fp1":
			case "tn1":
			case "fn1":
			case "numAttributes":
			 return(Integer.class)
			
			case "pctCorrect":
			case "precision0":
			case "recall0":
			case "precision1":
			case "recall1":
			case "rms":
			case "roc":
				return(Float.class)
			
			default: 
				return(String.class)									
		}
	}
	
	
	/************************
	* populate the table with prefetched results
	*/
	def populateTable(){	
		
		limitRow2Row = [:]	
		
		err.println "populateTable: wekaMineTable"
		err.println "populateTable before remove: $limitRows"
		
		//removeAllActionHandlers()
		removeAllItems()
		err.println "populateTable: wekaMineTable removeAllItems done."
		err.println "populateTable: wekaMineResults.size(): "+wekaMineResults.size()
		
		def idx = 0;		

		wekaMineResults.eachWithIndex{result,originalRow->
			def values = result.values() as ArrayList
			def orderedValues = orderResults(values)

			if (limitRows == "ALL"){
				//err.println "populateTable ALL orderedValues.size="+orderedValues.size()
				//err.println values[0..-2]
				addItem(orderedValues[0..-2] as Object[],new Integer(idx))
				//err.println "ALL $idx = $originalRow"
				limitRow2Row[idx] = originalRow
				idx++			
			}else{				
				def colIdx = colName2Idx['classAttribute']
				//err.println "populateTable ($limitRows) =?= ${orderedValues[colIdx]}}"
				if (orderedValues[colIdx] == limitRows){
					limitRow2Row[idx] = originalRow					
					err.println "\tAdd ITEM"
					addItem(orderedValues[0..-2] as Object[],new Integer(idx))
					idx++			
				}else{
					//err.println "HOW CAN I GET HERE? $idx $limitRows ? "+orderedValues[idx]
				}
			}			
		}		
		//addActionHandler(this)
		err.println "populateTable: done populate wekaMineTable"
	}
	
	/**************************
	* Create a table that lists all of the features. 
	*/ 
	def getFeaturesTable(){
		def rowIdx = this.getValue()
		
		err.println "wekaMineResults.size()="+wekaMineResults.size()
	
		// Map subset index onto actual index		
		def actualRowIdx = limitRow2Row[rowIdx]
		
		err.println "getFeaturesTable: $actualRowIdx  $rowIdx"
	
		def wmRow = wekaMineResults[actualRowIdx]
		//err.println "wekaMineRow: "+wmRow
		def features = wmRow.features.split(",")
				
		def ftable = new Table("Top Features")
		ftable.setWidth("20%");
		ftable.setHeight("450px");
		ftable.addContainerProperty("Gene" as String,String.class,null);				
		ftable.addContainerProperty("Score" as String,Float.class,null);				
				
		ftable.setSelectable(true);
		ftable.setImmediate(true);		
						
		def idx = 0;
		features.each{f->
			//err.println "f: $f"
			def cols = f.split("~")
			//err.println "cols: "+cols
			def gene = cols[0]
			def score = cols[1]
			def row = [gene as String,score as Float]
			ftable.addItem(row as Object[],new Integer(idx))
			idx++;
		}
		return(ftable);
	}
	

	public Action[] getActions(Object target, Object sender) {
		def actions = []
		
		def selectedGeneIdx = target
		def property = getContainerProperty(selectedGeneIdx,"classAttribute")
		def className = property.getValue()
		
		limitAction = new Action("View only $className" as String)
		allAction = new Action("View all")
		//limitClass = className	
		//limitRows = className					
		
		actions << limitAction
		actions << allAction
		return(actions as Action[])
  }
	
	
	public void handleAction(Action action,Object sender, Object target){
		err.println "handleAction $limitRows $limitClass"
		err.println "Action: "+action.getCaption()
		if (action == allAction){
			//KJD... why doesn't this match?  Oh well, for now, will 
			// just grab ALL out of the caption...
			err.println "action == allAction"
			limitRows = "ALL"			
		}else{
			def fields = action.getCaption().split(" ")
			if (fields.size() >= 3){								
				limitRows = fields[2]
			}else{
				limitRows = "ALL"
			}
		}	
		err.println "handleAction: populateTable $limitRows"	
		removeListener((Property.ValueChangeListener)getParent())
		populateTable()
		addListener((Property.ValueChangeListener)getParent())
		err.println "handleAction: requestRepaint"
		requestRepaint()
		err.println "Limit display to $limitRows"
	}


	
	/********************
	* Query the database and get a table of features vs samples...
	*/ 
	def updateWekaMineData(datasetName){				
		System.err.print "Reading wekaMine data..."
		def datasetID = bioint.getDatasetID(datasetName)
		wekaMineResults = bioint.getWekaMineResults(datasetID)
		System.err.println "done. ${wekaMineResults.size()} rows."
		return(wekaMineResults)
	}
	
	
	
	
	
}