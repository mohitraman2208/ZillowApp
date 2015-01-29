package com.example.zillowapplication;


public enum PropertyDetailsRowsConstants
{
	/*STATUS("status","status"),
	ERROR_MSG("errorMsg","Error:"),
	HOME_LINK("homeLink","Click -"),
	COMPLETE_ADDR("completeAddr","Complete Address"),
	DATELASTUPDATED("dateLastUpdated",""),
	*/
	
	Property_Type("propertyType","Property Type"),
	YEAR_BUILT("yearBuilt","Year Built"),
	LOT_SIZE("lotSize","Lot Size"),
	FINISHED_AREA("finishedArea","Finished Area"),
	BATHROOMS("bathrooms","Bathrooms"),
	BEDROOMS("bedrooms","Bedrooms"),
	TAX_ASSESSMENT_YEAR("taxAssessmentYear","Tax Assessment Year"),
	TAX_ASSESSMENT("taxAssessment","Tax Assessment"),
	LAST_SOLD_PRICE("lastSoldPrice","Last Sold Price"),
	LASTSOLDDATE("lastSoldDate","Last Sold Date"),
	ZESTIMATEPROPERTYESTIMATE("zestimatePropertyEstimate","Zestimate ® Property Estimate as of "),
	DAYSOVERALLCHANGE("daysOverallChange","30 Days Overall Change"),
	ALLTIMEPROPERTYRANGE("allTimePropertyRange","All Time Property Range"),
	RENTZESTIMATE("rentZestimate","Rent Zestimate ® Rent Valuation\n as of "),
	DAYSRENTCHANGE("daysRentChange","30 Days Rent Change"),
	ALLTIMERENTRANGE("allTimeRentRange","All Time Rent Range");
	
	PropertyDetailsRowsConstants(String key,String label)
	{
		this.label = label;
		this.key = key;
	}
	
	private final String label;
	private final String key;
	public String getLabel() {
		return label;
	}
	public String getKey() {
		return key;
	}

}
