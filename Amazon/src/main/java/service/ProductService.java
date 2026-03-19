package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;


import exceptions.ProductNotFoundException;
import util.DBConnection;

public class ProductService {

	public static final Logger logger =  LogManager.getLogger(ProductService.class);
	
	public void viewProducts(int page, int size) throws Exception {
		
		logger.info("Fetching products for page {} with size {}", page, size);
		
		Connection con = DBConnection.getConnection();

		int offset = (page - 1) * size;

		String query = "SELECT * FROM products LIMIT ? OFFSET ?";

		PreparedStatement ps = con.prepareStatement(query);

		ps.setInt(1, size);
		ps.setInt(2, offset);

		ResultSet rs = ps.executeQuery();

		System.out.println("\n===== PRODUCT LIST (Page " + page + ") =====");
		System.out.println("ID | Product Name | Price | Quantity");
		System.out.println("--------------------------------------");

		boolean hasData = false;
		
		logger.debug("Product fetched: ID={}, Name={}", rs.getInt("id"), rs.getString("product_name"));
		
		while (rs.next()) {

			hasData = true;

			System.out.println(rs.getInt("id") + " | " + rs.getString("product_name") + " | " + rs.getInt("price")
					+ " | " + rs.getInt("quantity"));
		}

		if (!hasData) {
			logger.warn("No products found on page {}", page);
			System.out.println("No products found on this page.");
		}

		rs.close();
		ps.close();
		con.close();
		
		logger.info("Finished fetching products for page {}", page);
	}

	public void searchProduct(String name) throws Exception {
		
		logger.info("Searching for product with name: {}", name);
		
		if (name == null || name.trim().isEmpty()) {
			logger.warn("Product search attempted with empty name");
			System.out.println("Product name cannot be empty");
			return;
		}

		Connection con = DBConnection.getConnection();

		String query = "select * from products where product_name like ?";

		PreparedStatement ps = con.prepareStatement(query);

		ps.setString(1, "%" + name + "%");

		ResultSet rs = ps.executeQuery();

		boolean found = false;

		while (rs.next()) {

			found = true;
			
			logger.info("Product found: {}", rs.getString("product_name"));
			System.out.println(rs.getInt("id") + " | " + rs.getString("product_name") + " | " + rs.getInt("price")
					+ " | " + rs.getInt("quantity"));
		}

		if (!found) {
			throw new ProductNotFoundException("No product found with name: " + name);
		}
		
		 rs.close();
	     ps.close();
	     con.close();

	     logger.info("Product search completed for {}", name);
	}

}
