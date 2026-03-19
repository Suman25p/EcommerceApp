package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;

import entity.Cart;
import exceptions.CartEmptyException;
import exceptions.OutOfStockException;
import exceptions.ProductNotFoundException;
import util.DBConnection;

public class CartService {

    private static final Logger logger = LogManager.getLogger(CartService.class);

    public void addProductToCart(int userId, int productId, int quantity) throws Exception {

        logger.info("User {} trying to add product {} with quantity {} to cart", userId, productId, quantity);

        Connection con = DBConnection.getConnection();

        String productCheck = "SELECT quantity FROM products WHERE id=?";
        PreparedStatement ps = con.prepareStatement(productCheck);

        ps.setInt(1, productId);

        ResultSet rs = ps.executeQuery();

        if (!rs.next()) {

            logger.error("Product {} not found while adding to cart", productId);

            throw new ProductNotFoundException("Product not found");
        }

        int stock = rs.getInt("quantity");

        if (stock < quantity) {

            logger.warn("Not enough stock for product {}. Available: {}, Requested: {}", productId, stock, quantity);

            throw new OutOfStockException("Not enough stock");
        }

        String cartCheck = "SELECT quantity FROM cart WHERE user_id=? AND product_id=?";
        PreparedStatement psCheck = con.prepareStatement(cartCheck);

        psCheck.setInt(1, userId);
        psCheck.setInt(2, productId);

        ResultSet rsCart = psCheck.executeQuery();

        if (rsCart.next()) {

            String update = "UPDATE cart SET quantity = quantity + ? WHERE user_id=? AND product_id=?";
            PreparedStatement psUpdate = con.prepareStatement(update);

            psUpdate.setInt(1, quantity);
            psUpdate.setInt(2, userId);
            psUpdate.setInt(3, productId);

            psUpdate.executeUpdate();

            logger.info("Cart updated: user {} product {} quantity increased by {}", userId, productId, quantity);

            System.out.println("Product quantity updated in cart");

            psUpdate.close();

        } else {

            String insert = "INSERT INTO cart(user_id, product_id, quantity) VALUES(?,?,?)";

            PreparedStatement psInsert = con.prepareStatement(insert);

            psInsert.setInt(1, userId);
            psInsert.setInt(2, productId);
            psInsert.setInt(3, quantity);

            psInsert.executeUpdate();

            logger.info("Product {} added to cart for user {} with quantity {}", productId, userId, quantity);

            System.out.println("Product added to cart");

            psInsert.close();
        }

        rs.close();
        rsCart.close();
        ps.close();
        psCheck.close();
        con.close();
    }

    public void viewItemsCurrentlyInCart(int userId) throws Exception {

        logger.info("Fetching cart items for user {}", userId);

        List<Cart> cartItems = getCartItems(userId);

        if (cartItems.isEmpty()) {

            logger.warn("Cart is empty for user {}", userId);

            System.out.println("Cart is empty. Please select products.");
            return;
        }

        double total = 0;

        System.out.println("\n================ YOUR CART ================");

        for (Cart cart : cartItems) {

            double subTotal = cart.getPrice() * cart.getQuantity();

            total += subTotal;

            logger.debug("Cart item: product {} quantity {}", cart.getProduct_name(), cart.getQuantity());

            System.out.println("Cart ID: " + cart.getCart_id() + " | Product ID: " + cart.getProduct_id()
                    + " |Product Name: " + cart.getProduct_name() + " | Qty: " + cart.getQuantity() + " | Price: "
                    + cart.getPrice() + " | SubTotal: " + subTotal);
        }

        double gst = total * 0.10;
        double finalTotal = total + gst;

        logger.info("Cart calculation for user {} Total={} GST={} Final={}", userId, total, gst, finalTotal);

        System.out.println("\n-------------------------------------------");
        System.out.println("Cart Total      : " + total);
        System.out.println("GST (10%)       : " + gst);
        System.out.println("Final Amount    : " + finalTotal);
        System.out.println("-------------------------------------------");
    }

    public List<Cart> getCartItems(int userId) throws Exception {

        logger.debug("Loading cart items from database for user {}", userId);

        Connection con = DBConnection.getConnection();

        String query = "SELECT c.cart_id, c.user_id, c.product_id, p.product_name, c.quantity, p.price "
                + "FROM cart c JOIN products p ON c.product_id = p.id WHERE c.user_id = ?";

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

        if (cartItems.isEmpty()) {

            logger.warn("Cart empty for user {}", userId);

            throw new CartEmptyException("Your cart is empty. Please add products first.");
        }

        return cartItems;
    }

    public void removeProductFromCart(int userId, int productId, int removeQty) throws Exception {

        logger.info("User {} removing product {} quantity {} from cart", userId, productId, removeQty);

        Connection con = DBConnection.getConnection();

        String check = "SELECT quantity FROM cart WHERE user_id=? AND product_id=?";
        PreparedStatement ps1 = con.prepareStatement(check);

        ps1.setInt(1, userId);
        ps1.setInt(2, productId);

        ResultSet rs = ps1.executeQuery();

        if (!rs.next()) {

            logger.error("Product {} not found in cart for user {}", productId, userId);

            throw new ProductNotFoundException("Select proper userId/product id to remove!");
        }

        int currentQty = rs.getInt("quantity");

        if (removeQty >= currentQty) {

            String delete = "DELETE FROM cart WHERE user_id=? AND product_id=?";
            PreparedStatement ps2 = con.prepareStatement(delete);

            ps2.setInt(1, userId);
            ps2.setInt(2, productId);

            ps2.executeUpdate();

            logger.info("Product {} completely removed from cart of user {}", productId, userId);

            System.out.println("Product removed from cart");

            ps2.close();

        } else {

            String update = "UPDATE cart SET quantity = quantity - ? WHERE user_id=? AND product_id=?";
            PreparedStatement ps3 = con.prepareStatement(update);

            ps3.setInt(1, removeQty);
            ps3.setInt(2, userId);
            ps3.setInt(3, productId);

            ps3.executeUpdate();

            logger.info("Cart updated for user {} product {} quantity reduced by {}", userId, productId, removeQty);

            System.out.println("Quantity updated in cart");

            ps3.close();
        }

        rs.close();
        ps1.close();
        con.close();
    }
}