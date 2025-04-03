/**
 * Author 			: prasanths 
 * Last Modified By : prasanths
 */
package com.friendlycafe.dto;

import java.util.ArrayList;

import com.friendlycafe.pojo.Report;

/**
 * 
 */
public class ReportDTO {

	public ArrayList<Report> data;
	public double earningForTheDay;
	
	public ReportDTO(ArrayList<Report> data, double earning) {
		this.data = data;
		earningForTheDay = earning;
	}
}
