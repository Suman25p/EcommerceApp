package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;


import entity.Cart;
import exceptions.OrderNotFoundException;
import exceptions.OutOfStockException;
import util.DBConnection;

public class OrderService {

	private static final Logger logger = LogManager.getLogger(OrderService.class);

	public int newOrderForCustomer(Connection con, int userId) throws Exception {

		logger.info("Creating new order for user {}", userId);

		String insert = "insert into orders(user_id) values(?)";

		PreparedStatement ps = con.prepareStatement(insert, PreparedStatement.RETURN_GENERATED_KEYS);

		ps.setInt(1, userId);

		ps.executeUpdate();

		ResultSet rs = ps.getGeneratedKeys();

		int orderId = 0;

		if (rs.next()) {
			orderId = rs.getInt(1);
		}

		logger.debug("Generated order id {} for user {}", orderId, userId);

		rs.close();
		ps.close();

		return orderId;
	}

	public void validateStockBeforeConfirmingOrder(Connection con, List<Cart> cartItems) throws Exception {

		logger.info("Validating stock before confirming order");

		String query = "select quantity from products where id=?";

		PreparedStatement ps = con.prepareStatement(query);

		for (Cart cart : cartItems) {

			ps.setInt(1, cart.getProduct_id());

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {

				int stock = rs.getInt("quantity");

				logger.debug("Checking stock for product {} available={} requested={}", cart.getProduct_id(), stock,
						cart.getQuantity());

				if (stock < cart.getQuantity()) {

					logger.error("Stock insufficient for product {}", cart.getProduct_id());

					throw new OutOfStockException("Stock not available for product " + cart.getProduct_id());
				}
			}

			rs.close();
		}

		ps.close();
	}

	public void saveItemsOrder(Connection con, int orderId, List<Cart> cartItems) throws Exception {

		logger.info("Saving order items for order {}", orderId);

		String insert = "INSERT INTO order_items(order_id, product_id, quantity, price) "
				+ "SELECT ?, id, ?, price FROM products WHERE id=?";

		PreparedStatement ps = con.prepareStatement(insert);

		for (Cart cart : cartItems) {

			logger.debug("Adding product {} quantity {} to order {}", cart.getProduct_id(), cart.getQuantity(),
					orderId);

			ps.setInt(1, orderId);
			ps.setInt(2, cart.getQuantity());
			ps.setInt(3, cart.getProduct_id());

			ps.addBatch();
		}

		ps.executeBatch();

		ps.close();
	}

	public void reduceProductStockAfterConfirmation(Connection con, List<Cart> cartItems) throws Exception {

		logger.info("Reducing product stock after order confirmation");

		String update = "UPDATE products SET quantity = quantity - ? WHERE id = ?";

		PreparedStatement ps = con.prepareStatement(update);

		for (Cart cart : cartItems) {

			logger.debug("Reducing stock for product {} by {}", cart.getProduct_id(), cart.getQuantity());

			ps.setInt(1, cart.getQuantity());
			ps.setInt(2, cart.getProduct_id());

			ps.addBatch();
		}

		ps.executeBatch();

		ps.close();
	}

	public void viewPreviousOrdersOfCustomers(int userId) throws Exception {

		logger.info("Fetching previous orders for user {}", userId);

		Connection con = DBConnection.getConnection();

		String query = "SELECT o.order_id, o.user_id, u.user_name, " + "oi.product_id, p.product_name, "
				+ "(oi.quantity * oi.price) AS total_amount, " + "o.order_date " + "FROM orders o "
				+ "JOIN users u ON o.user_id = u.user_id " + "JOIN order_items oi ON o.order_id = oi.order_id "
				+ "JOIN products p ON oi.product_id = p.id " + "WHERE o.user_id = ? " + "ORDER BY o.order_date DESC";

		PreparedStatement ps = con.prepareStatement(query);

		ps.setInt(1, userId);

		ResultSet rs = ps.executeQuery();

		boolean hasData = false;

		while (rs.next()) {

			hasData = true;

			logger.debug("Order {} product {} fetched", rs.getInt("order_id"), rs.getString("product_name"));

			System.out.println("Order ID: " + rs.getInt("order_id") + " | User: " + rs.getString("user_name")
					+ " | Product ID: " + rs.getInt("product_id") + " | Product Name: " + rs.getString("product_name")
					+ " | Amount: " + rs.getInt("total_amount") + " | Date: " + rs.getTimestamp("order_date"));
		}

		if (!hasData) {

			logger.warn("No orders found for user {}", userId);

			throw new OrderNotFoundException("No orders found !!.");
		}

		rs.close();
		ps.close();
		con.close();
	}

	public void getOrderDetailsAndItemsPurchased(int orderId) throws Exception {

		logger.info("Fetching order details for order {}", orderId);

		Connection con = DBConnection.getConnection();

		String query = "select p.product_name, oi.quantity, oi.price, p.id as product_id " + "from order_items oi "
				+ "join products p on oi.product_id = p.id " + "where oi.order_id = ?";

		PreparedStatement ps = con.prepareStatement(query);

		ps.setInt(1, orderId);

		ResultSet rs = ps.executeQuery();

		while (rs.next()) {

			logger.debug("Order {} contains product {}", orderId, rs.getString("product_name"));

			System.out.println(rs.getString("product_name") + " " + rs.getInt("quantity") + " " + rs.getInt("price")
					+ " " + rs.getInt("product_id"));
		}
	}

	private double calculateTotalOrderValue(List<Cart> cartItems) {

		double total = 0;

		for (Cart cart : cartItems) {
			total += cart.getPrice() * cart.getQuantity();
		}

		return total;
	}

	public List<Cart> getCartItems(Connection con, int userId) throws Exception {

		logger.debug("Loading cart items for user {}", userId);

		String query = "SELECT c.cart_id, c.user_id, c.product_id, p.product_name, c.quantity, p.price "
				+ "FROM cart c JOIN products p ON c.product_id = p.id " + "WHERE c.user_id = ?";

		PreparedStatement ps = con.prepareStatement(query);

		ps.setInt(1, userId);

		ResultSet rs = ps.executeQuery();

		List<Cart> cartItems = new ArrayList<>();

		while (rs.next()) {

			Cart cart = new Cart();

			cart.setCart_id(rs.getInt("cart_id"));
			cart.setUser_id(rs.getInt("user_id"));
			cart.setProduct_id(rs.getInt("product_id"));
			cart.setProduct_name(rs.getString("product_name"));
			cart.setQuantity(rs.getInt("quantity"));
			cart.setPrice(rs.getInt("price"));

			cartItems.add(cart);
		}

		rs.close();
		ps.close();

		return cartItems;
	}

	public void placeOrder(int userId) throws Exception {

		logger.info("User {} attempting to place order", userId);

		Connection con = DBConnection.getConnection();

		try {

			con.setAutoCommit(false);

			List<Cart> cartItems = getCartItems(con, userId);

			if (cartItems.isEmpty()) {

				logger.warn("User {} attempted order with empty cart", userId);

				System.out.println("Cart is empty. Add products first.");
				con.rollback();
				return;
			}

			int orderId = newOrderForCustomer(con, userId);

			validateStockBeforeConfirmingOrder(con, cartItems);

			saveItemsOrder(con, orderId, cartItems);

			reduceProductStockAfterConfirmation(con, cartItems);

			clearCart(con, userId);

			con.commit();

			logger.info("Order {} successfully placed for user {}", orderId, userId);

			double total = calculateTotalOrderValue(cartItems);
			double gst = total * 0.10;
			double finalAmount = total + gst;

			System.out.println("\n=========== INVOICE BILL ===========");

			for (Cart cart : cartItems) {

				double subtotal = cart.getPrice() * cart.getQuantity();

				System.out.println("Product Id: " + cart.getProduct_id() + " |Quantity: " + cart.getQuantity()
						+ " | Price: " + cart.getPrice() + " | Subtotal: " + subtotal);
			}

			System.out.println("------------------------------------");
			System.out.println("Cart Total  : " + total);
			System.out.println("GST (10%)   : " + gst);
			System.out.println("Final Total : " + finalAmount);
			System.out.println("------------------------------------");

			System.out.println("Order placed successfully!");
			System.out.println("Your Order ID: " + orderId);

			getOrderDetailsAndItemsPurchased(orderId);

		} catch (Exception e) {

			logger.error("Order failed for user {}. Rolling back transaction", userId, e);

			con.rollback();

			System.out.println("Order failed. Transaction rolled back.");

		} finally {

			con.close();

			logger.debug("Database connection closed after order process");
		}
	}

	private void clearCart(Connection con, int userId) throws Exception {

		logger.info("Clearing cart for user {}", userId);

		String delete = "DELETE FROM cart WHERE user_id=?";

		PreparedStatement ps = con.prepareStatement(delete);

		ps.setInt(1, userId);

		ps.executeUpdate();

		ps.close();
	}
}