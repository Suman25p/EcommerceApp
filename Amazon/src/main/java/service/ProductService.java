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

	    Connection con = DBConnection.getConnection();

	    int offset = (page - 1) * size;

	    String query = "SELECT * FROM products ORDER BY product_name ASC LIMIT ? OFFSET ?";

	    PreparedStatement ps = con.prepareStatement(query);

	    ps.setInt(1, size);
	    ps.setInt(2, offset);

	    ResultSet rs = ps.executeQuery();

	    System.out.println("\n===== PRODUCT LIST (Page " + page + ") =====");
	    System.out.println("ID | Product Name | Price | Quantity");
	    System.out.println("--------------------------------------");

	    boolean hasData = false;

	    while (rs.next()) {

	        hasData = true;

	        int id = rs.getInt("id");
	        String name = rs.getString("product_name");
	        int price = rs.getInt("price");
	        int quantity = rs.getInt("quantity");

	        logger.debug("Product fetched: ID={}, Name={}", id, name);

	        System.out.println(id + " | " + name + " | " + price + " | " + quantity);
	    }

	    if (!hasData) {
	        System.out.println("No products found on this page.");
	    }

	    rs.close();
	    ps.close();
	    con.close();
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
