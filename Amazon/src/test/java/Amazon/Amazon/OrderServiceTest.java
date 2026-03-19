package Amazon.Amazon;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import entity.Cart;
import service.CartService;
import service.OrderService;
import util.DBConnection;

public class OrderServiceTest {

	@Test
	void testNewOrderForCustomer() throws Exception {

		Connection mockConnection = mock(Connection.class);
		PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
		ResultSet mockResultSet = mock(ResultSet.class);

		when(mockConnection.prepareStatement("insert into orders(user_id) values(?)",
				PreparedStatement.RETURN_GENERATED_KEYS)).thenReturn(mockPreparedStatement);

		when(mockPreparedStatement.executeUpdate()).thenReturn(1);

		when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
		when(mockResultSet.next()).thenReturn(true);
		when(mockResultSet.getInt(1)).thenReturn(101);

		OrderService orderService = new OrderService();
		int orderId = orderService.newOrderForCustomer(mockConnection, 1); 

		assertEquals(101, orderId);

		verify(mockPreparedStatement).setInt(1, 1);
		verify(mockPreparedStatement).executeUpdate();
		verify(mockPreparedStatement).getGeneratedKeys();
	}

	@Test
	void testValidateStock_Success() throws Exception {

		Connection mockConnection = mock(Connection.class);
		PreparedStatement mockPs = mock(PreparedStatement.class);
		ResultSet mockRs = mock(ResultSet.class);

		when(mockConnection.prepareStatement("select quantity from products where id=?")).thenReturn(mockPs);

		when(mockPs.executeQuery()).thenReturn(mockRs);

		when(mockRs.next()).thenReturn(true);
		when(mockRs.getInt("quantity")).thenReturn(10);

		Cart cart = new Cart(); // productId=1 quantity=2
		List<Cart> cartItems = List.of(cart);

		OrderService orderService = new OrderService();

		assertDoesNotThrow(() -> orderService.validateStockBeforeConfirmingOrder(mockConnection, cartItems));
	}

	@Test
	void testValidateStock_MultipleItems() throws Exception {

		Connection mockConnection = mock(Connection.class);
		PreparedStatement mockPs = mock(PreparedStatement.class);
		ResultSet mockRs = mock(ResultSet.class);

		when(mockConnection.prepareStatement("select quantity from products where id=?")).thenReturn(mockPs);

		when(mockPs.executeQuery()).thenReturn(mockRs);

		when(mockRs.next()).thenReturn(true);
		when(mockRs.getInt("quantity")).thenReturn(20);

		Cart cart1 = new Cart();
		Cart cart2 = new Cart();

		List<Cart> cartItems = List.of(cart1, cart2);

		OrderService orderService = new OrderService();

		assertDoesNotThrow(() -> orderService.validateStockBeforeConfirmingOrder(mockConnection, cartItems));

		verify(mockPs, times(2)).executeQuery();
	}

	@Test
	void testSaveItemsOrder_Success() throws Exception {

		Connection mockConnection = mock(Connection.class);
		PreparedStatement mockPs = mock(PreparedStatement.class);

		when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);

		Cart cart1 = new Cart();
		Cart cart2 = new Cart();

		List<Cart> cartItems = List.of(cart1, cart2);

		OrderService service = new OrderService();

		service.saveItemsOrder(mockConnection, 100, cartItems);

		verify(mockPs, times(2)).addBatch();
		verify(mockPs).executeBatch();
	}

	@Test
	void testSaveItemsOrder_MultipleItems() throws Exception {

		Connection mockConnection = mock(Connection.class);
		PreparedStatement mockPs = mock(PreparedStatement.class);

		when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);

		Cart cart1 = new Cart();
		Cart cart2 = new Cart();
		Cart cart3 = new Cart();

		List<Cart> cartItems = List.of(cart1, cart2, cart3);

		OrderService service = new OrderService();

		service.saveItemsOrder(mockConnection, 300, cartItems);

		verify(mockPs, times(3)).addBatch();
		verify(mockPs).executeBatch();
	}

	@Test
	void testReduceProductStockAfterConfirmation_Success() throws Exception {

		Connection mockConnection = mock(Connection.class);
		PreparedStatement mockPs = mock(PreparedStatement.class);

		when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);

		Cart cart1 = new Cart();
		Cart cart2 = new Cart();

		List<Cart> cartItems = List.of(cart1, cart2);

		OrderService service = new OrderService();

		service.reduceProductStockAfterConfirmation(mockConnection, cartItems);

		verify(mockPs, times(2)).addBatch();
		verify(mockPs).executeBatch();
	}

	@Test
	void testReduceProductStockAfterConfirmation_MultipleItems() throws Exception {

		Connection mockConnection = mock(Connection.class);
		PreparedStatement mockPs = mock(PreparedStatement.class);

		when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);

		Cart cart1 = new Cart();
		Cart cart2 = new Cart();
		Cart cart3 = new Cart();

		List<Cart> cartItems = List.of(cart1, cart2, cart3);

		OrderService service = new OrderService();

		service.reduceProductStockAfterConfirmation(mockConnection, cartItems);

		verify(mockPs, times(3)).addBatch();
		verify(mockPs).executeBatch();
	}

//    @Test
//    void testGetOrderDetails_WithItems() throws Exception {
//
//        Connection mockConnection = mock(Connection.class);
//        PreparedStatement mockPs = mock(PreparedStatement.class);
//        ResultSet mockRs = mock(ResultSet.class);
//
//        when(DBConnection.getConnection()).thenReturn(mockConnection);
//        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
//        when(mockPs.executeQuery()).thenReturn(mockRs);
//
//        when(mockRs.next()).thenReturn(true, false);
//        when(mockRs.getString("product_name")).thenReturn("Laptop");
//        when(mockRs.getInt("quantity")).thenReturn(2);
//        when(mockRs.getInt("price")).thenReturn(50000);
//
//        OrderService service = new OrderService();
//
//        service.getOrderDetailsAndItemsPurchased(1);
//
//        verify(mockPs).setInt(1,1);
//    }

//    @Test
//    void testPlaceOrder_Success() throws Exception {
//
//        OrderService service = spy(new OrderService());
//
//        Connection mockConnection = mock(Connection.class);
//
//        when(DBConnection.getConnection()).thenReturn(mockConnection);
//
//        List<Cart> cartList = List.of(new Cart());
//
//        doReturn(cartList).when(service).getCartItems(any(), anyInt());
//        doReturn(101).when(service).newOrderForCustomer(any(), anyInt());
//
//        service.placeOrder(1);
//
//        verify(mockConnection).commit();
//    }

//    @Test
//    void testPlaceOrder_EmptyCart() throws Exception {
//
//        OrderService service = spy(new OrderService());
//
//        Connection mockConnection = mock(Connection.class);
//
//        when(DBConnection.getConnection()).thenReturn(mockConnection);
//
//        doReturn(Collections.emptyList()).when(service).getCartItems(any(), anyInt());
//
//        service.placeOrder(1);
//
//        verify(mockConnection, never()).commit();
//    }
//    

//    @Test
//    void testPlaceOrder_SaveItemsFailure() throws Exception {
//
//        OrderService service = spy(new OrderService());
//
//        Connection mockConnection = mock(Connection.class);
//
//        when(DBConnection.getConnection()).thenReturn(mockConnection);
//
//        List<Cart> cartList = List.of(new Cart());
//
//        doReturn(cartList).when(service).getCartItems(any(), anyInt());
//        doReturn(101).when(service).newOrderForCustomer(any(), anyInt());
//
//        doThrow(new RuntimeException("DB Error"))
//                .when(service).saveItemsOrder(any(), anyInt(), any());
//
//        service.placeOrder(1);
//
//        verify(mockConnection).rollback();
//    }

//    @Test
//    void testGetOrderDetails_WithItems() throws Exception {
//
//        Connection mockConnection = mock(Connection.class);
//        PreparedStatement mockPs = mock(PreparedStatement.class);
//        ResultSet mockRs = mock(ResultSet.class);
//
//        when(DBConnection.getConnection()).thenReturn(mockConnection);
//        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
//        when(mockPs.executeQuery()).thenReturn(mockRs);
//
//        when(mockRs.next()).thenReturn(true, false);
//        when(mockRs.getString("product_name")).thenReturn("Laptop");
//        when(mockRs.getInt("quantity")).thenReturn(2);
//        when(mockRs.getInt("price")).thenReturn(50000);
//
//        OrderService service = new OrderService();
//
//        service.getOrderDetailsAndItemsPurchased(1);
//
//        verify(mockPs).setInt(1,1);
//    }
//	
//	
//	//  This test checks that the loop runs multiple times when multiple products are in the order.
//	
//	@Test
//void testGetOrderDetails_MultipleItems() throws Exception {
//
//    Connection mockConnection = mock(Connection.class);
//    PreparedStatement mockPs = mock(PreparedStatement.class);
//    ResultSet mockRs = mock(ResultSet.class);
//
//    when(DBConnection.getConnection()).thenReturn(mockConnection);
//    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
//    when(mockPs.executeQuery()).thenReturn(mockRs);
//
//    when(mockRs.next()).thenReturn(true, true, false);
//
//    OrderService service = new OrderService();
//
//    service.getOrderDetailsAndItemsPurchased(3);
//
//    verify(mockRs, times(3)).next();
//}
//
/////This test ensures that the correct orderId is passed to the SQL query
//
//@Test
//void testGetOrderDetails_VerifyOrderIdParameter() throws Exception {
//
//    Connection mockConnection = mock(Connection.class);
//    PreparedStatement mockPs = mock(PreparedStatement.class);
//    ResultSet mockRs = mock(ResultSet.class);
//
//    when(DBConnection.getConnection()).thenReturn(mockConnection);
//    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
//    when(mockPs.executeQuery()).thenReturn(mockRs);
//
//    when(mockRs.next()).thenReturn(false);
//
//    OrderService service = new OrderService();
//
//    service.getOrderDetailsAndItemsPurchased(10);
//
//    verify(mockPs).setInt(1,10);
//}

//    @Test
//    void testGetCartItems_WithData() throws Exception {
//
//        Connection mockConnection = mock(Connection.class);
//        PreparedStatement mockPs = mock(PreparedStatement.class);
//        ResultSet mockRs = mock(ResultSet.class);
//
//        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
//        when(mockPs.executeQuery()).thenReturn(mockRs);
//
//        when(mockRs.next()).thenReturn(true, false);
//
//        when(mockRs.getInt("cart_id")).thenReturn(1);
//        when(mockRs.getInt("user_id")).thenReturn(10);
//        when(mockRs.getInt("product_id")).thenReturn(5);
//        when(mockRs.getInt("quantity")).thenReturn(2);
//
//        CartService service = new CartService();
//
//        List<Cart> result = service.getCartItems(mockConnection,10);
//
//        assertEquals(1, result.size());
//    }
//
//    @Test
//        void testGetCartItems_NoData() throws Exception {
//
//            Connection mockConnection = mock(Connection.class);
//            PreparedStatement mockPs = mock(PreparedStatement.class);
//            ResultSet mockRs = mock(ResultSet.class);
//
//            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
//            when(mockPs.executeQuery()).thenReturn(mockRs);
//
//            when(mockRs.next()).thenReturn(false);
//
//            CartService service = new CartService();
//
//            List<Cart> result = service.getCartItems(mockConnection,10);
//
//            assertTrue(result.isEmpty());
//        }
//    	
//    	@Test
//        void testGetCartItems_VerifyUserIdParameter() throws Exception {
//
//            Connection mockConnection = mock(Connection.class);
//            PreparedStatement mockPs = mock(PreparedStatement.class);
//            ResultSet mockRs = mock(ResultSet.class);
//
//            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
//            when(mockPs.executeQuery()).thenReturn(mockRs);
//
//            when(mockRs.next()).thenReturn(false);
//
//            CartService service = new CartService();
//
//            service.getCartItems(mockConnection,20);
//
//            verify(mockPs).setInt(1,20);
//        }
//    	
//    	@Test
//    void testGetCartItems_ResourcesClosed() throws Exception {
//
//        Connection mockConnection = mock(Connection.class);
//        PreparedStatement mockPs = mock(PreparedStatement.class);
//        ResultSet mockRs = mock(ResultSet.class);
//
//        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
//        when(mockPs.executeQuery()).thenReturn(mockRs);
//
//        when(mockRs.next()).thenReturn(false);
//
//        CartService service = new CartService();
//
//        service.getCartItems(mockConnection,1);
//
//        verify(mockRs).close();
//        verify(mockPs).close();
//    }

//    @Test
//    void testViewPreviousOrders_WithOrders() throws Exception {
//
//        Connection mockConnection = mock(Connection.class);
//        PreparedStatement mockPs = mock(PreparedStatement.class);
//        ResultSet mockRs = mock(ResultSet.class);
//
//        when(DBConnection.getConnection()).thenReturn(mockConnection);
//        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
//        when(mockPs.executeQuery()).thenReturn(mockRs);
//
//        when(mockRs.next()).thenReturn(true, false);
//        when(mockRs.getInt("order_id")).thenReturn(101);
//        when(mockRs.getTimestamp("order_date")).thenReturn(new Timestamp(System.currentTimeMillis()));
//
//        OrderService service = new OrderService();
//        service.viewPreviousOrdersOfCustomers(1,1,5);
//
//        verify(mockPs).setInt(1,1);
//        verify(mockPs).executeQuery();
//    }
//	
//	
//	@Test
//    void testViewPreviousOrders_NoOrders() throws Exception {
//
//        Connection mockConnection = mock(Connection.class);
//        PreparedStatement mockPs = mock(PreparedStatement.class);
//        ResultSet mockRs = mock(ResultSet.class);
//
//        when(DBConnection.getConnection()).thenReturn(mockConnection);
//        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
//        when(mockPs.executeQuery()).thenReturn(mockRs);
//
//        when(mockRs.next()).thenReturn(false);
//
//        OrderService service = new OrderService();
//        service.viewPreviousOrdersOfCustomers(1,2,5);
//
//        verify(mockPs).executeQuery();
//    }
//    
//	
//	
//	
//	@Test
//    void testViewPreviousOrders_Pagination() throws Exception {
//
//        Connection mockConnection = mock(Connection.class);
//        PreparedStatement mockPs = mock(PreparedStatement.class);
//        ResultSet mockRs = mock(ResultSet.class);
//
//        when(DBConnection.getConnection()).thenReturn(mockConnection);
//        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
//        when(mockPs.executeQuery()).thenReturn(mockRs);
//
//        when(mockRs.next()).thenReturn(false);
//
//        OrderService service = new OrderService();
//        service.viewPreviousOrdersOfCustomers(1,2,5);
//
//        verify(mockPs).setInt(2,5); // limit
//        verify(mockPs).setInt(3,5); // offset
//    }
//	
//	@Test
//void testViewPreviousOrders_ConnectionClosed() throws Exception {
//
//    Connection mockConnection = mock(Connection.class);
//    PreparedStatement mockPs = mock(PreparedStatement.class);
//    ResultSet mockRs = mock(ResultSet.class);
//
//    when(DBConnection.getConnection()).thenReturn(mockConnection);
//    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
//    when(mockPs.executeQuery()).thenReturn(mockRs);
//
//    when(mockRs.next()).thenReturn(false);
//
//    OrderService service = new OrderService();
//    service.viewPreviousOrdersOfCustomers(1,1,5);
//
//    verify(mockConnection).close();
//}
//    

//    @Test
//    void testClearCart_Success() throws Exception {
//
//        Connection mockConnection = mock(Connection.class);
//        PreparedStatement mockPs = mock(PreparedStatement.class);
//
//        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
//
//        OrderService service = new OrderService();
//
//        service.clearCart(mockConnection,1);
//
//        verify(mockPs).executeUpdate();
//    }

//    @Test
//    void testClearCart_VerifyUserIdParameter() throws Exception {
//
//        Connection mockConnection = mock(Connection.class);
//        PreparedStatement mockPs = mock(PreparedStatement.class);
//
//        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
//
//        OrderService service = new OrderService();
//
//        service.clearCart(mockConnection,5);
//
//        verify(mockPs).setInt(1,5);
//    }

//    
//    @Test
//    void testClearCart_Exception() throws Exception {
//
//        Connection mockConnection = mock(Connection.class);
//        PreparedStatement mockPs = mock(PreparedStatement.class);
//
//        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
//
//        doThrow(new RuntimeException("Database error")).when(mockPs).executeUpdate();
//
//        OrderService service = new OrderService();
//
//        assertThrows(RuntimeException.class, () -> {
//            service.clearCart(mockConnection,1);
//        });
//    }

}
