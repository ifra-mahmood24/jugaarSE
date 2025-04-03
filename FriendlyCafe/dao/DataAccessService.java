/**
 * Author 			: prasanths 
 * Last Modified By : prasanths
 */
package com.friendlycafe.daoservice;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.logging.*;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.friendlycafe.dto.CustomerDTO;
import com.friendlycafe.dto.OrderDTO;
import com.friendlycafe.dto.ReportDTO;
import com.friendlycafe.dtoservice.DataService;
import com.friendlycafe.pojo.Customer;
import com.friendlycafe.pojo.Order;
import com.friendlycafe.pojo.Report;

/**
 * 
 */
public class DataAccessService {

	private static final Logger logger = Logger.getLogger(DataService.class.getName());

	public JSONArray readJSONFile(String Path, String JSONkey){
		
		byte[] bytes;
		try {
			bytes = Files.readAllBytes(Paths.get(Path));
			String content = new String(bytes, StandardCharsets.UTF_8);
			JSONObject object = new JSONObject(content);
			return object.getJSONArray(JSONkey);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param string
	 * @param string2
	 * @return
	 */
	public void writeJSONFileForOrders(String path, ArrayList<Order> list) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            logger.info("Writing... "+list);
        	OrderDTO orders = new OrderDTO();
        	orders.setOrders(list);
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(path), orders);
		} catch (StreamWriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabindException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeJSONFileForCustomers(String path, ArrayList<Customer> list) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            logger.info("Writing... "+list.size());
        	CustomerDTO customers = new CustomerDTO();
        	customers.setCustomers(list);
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(path), customers);
		} catch (StreamWriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabindException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void writeReport(ArrayList<Report> allOrderedItems) {
		
		ObjectMapper objectMapper = new ObjectMapper();
		LocalDate date = LocalDate.now();
		Double earningForTheDay = 0.0;
		for(Report itemTotalCost: allOrderedItems) {
			earningForTheDay += itemTotalCost.totalCost;
		}
		try {
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("src/main/resources/reports/report_"+date+".json"),
					new ReportDTO(allOrderedItems, earningForTheDay));
		} catch (Exception e) {
			logger.info("WRITE REPORT FAILING....");
			e.printStackTrace();
		}		
	}
}
