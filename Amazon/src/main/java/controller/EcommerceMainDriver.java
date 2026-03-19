package controller;

import java.util.Scanner;

import exceptions.CartEmptyException;
import exceptions.OrderNotFoundException;
import exceptions.OutOfStockException;
import exceptions.ProductNotFoundException;
import exceptions.UserNotFoundException;
import service.CartService;
import service.OrderService;
import service.ProductService;
import service.UserService;
import util.CSVProductLoader;

import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;


public class EcommerceMainDriver {
    private static final Logger logger = LogManager.getLogger(CartService.class);


	public static int getValidInt(Scanner sc) {
		while (true) {
			try {
				return Integer.parseInt(sc.nextLine());
			} catch (Exception e) {
				System.out.print("Invalid input. Enter a number: ");
			}
		}
	}

	public static void main(String[] args) throws Exception {
		logger.info("main method startd");

		Scanner sc = new Scanner(System.in);

		ProductService productService = new ProductService();
		CartService cartService = new CartService();
		OrderService orderService = new OrderService();
		UserService userService = new UserService();

		CSVProductLoader csvProductLoader = new CSVProductLoader();
		csvProductLoader.loadProducts();

		int loggedInUser = -1;

		while (loggedInUser == -1) {

			System.out.println("\n===== WELCOME TO ECOMMERCE APP =====");
			System.out.println("1 Register");
			System.out.println("2 Login");
			System.out.println("3 Exit");

			System.out.print("Enter your choice: ");
			int option = getValidInt(sc);

			switch (option) {

			case 1:

				System.out.print("Enter Name: ");
				String name = sc.nextLine();

				System.out.print("Enter Email: ");
				String email = sc.nextLine();

				System.out.print("Enter Phone: ");
				String phone = sc.nextLine();

				System.out.print("Enter Password: ");
				String password = sc.nextLine();

				userService.registerUser(name, email, phone, password);

				break;

			case 2:

				try {

					System.out.print("Enter Email: ");
					String loginEmail = sc.nextLine();

					System.out.print("Enter Password: ");
					String loginPassword = sc.nextLine();

					loggedInUser = userService.loginUser(loginEmail, loginPassword);

					System.out.println("Login Successful. UserID = " + loggedInUser);

				} catch (UserNotFoundException e) {
					logger.error("user not found exception "+e);

					//System.out.println(e.getMessage());

				}

				break;

			case 3:
				System.out.println("Thank you for visiting Amazon....");
				return;

			default:
				System.out.println("Invalid choice");
			}
		}

		while (true) {

			System.out.println("\n========= ECOMMERCE MENU =========");
			System.out.println("1 View All Products");
			System.out.println("2 Search Product");
			System.out.println("3 Add Product To Cart");
			System.out.println("4 View Cart");
			System.out.println("5 Remove Product From Cart");
			System.out.println("6 Place Order");
			System.out.println("7 View Order History");
			System.out.println("8 Exit");

			System.out.print("Enter your choice: ");
			int choice = getValidInt(sc);

			switch (choice) {

			case 1:

				int page = 1;
				int size = 15;

				try {

					while (true) {

						productService.viewProducts(page, size);

						System.out.println("\n1 Next Page");
						System.out.println("2 Previous Page");
						System.out.println("3 Exit");

						int option = getValidInt(sc);

						if (option == 1) {
							page++;
						} else if (option == 2) {
							if (page > 1)
								page--;
						} else {
							break;
						}
					}

				} catch (ProductNotFoundException e) {

					System.out.println(e.getMessage());

				}

				break;

			case 2:

				try {

					String pname;

					while (true) {

						System.out.print("Enter product name: ");
						pname = sc.nextLine();

						if (!pname.trim().isEmpty()) {
							break;
						}

						System.out.println("Product name cannot be empty. Try again.");
					}

					productService.searchProduct(pname);

				} catch (ProductNotFoundException e) {

					System.out.println(e.getMessage());

				}

				break;
			case 3:

				try {
					System.out.print("Enter Product ID: ");
					int productId = getValidInt(sc);

					System.out.print("Enter Quantity: ");
					int quantity = getValidInt(sc);

					if (quantity <= 0) {
						System.out.println("Quantity must be greater than 0");
						break;
					}

					cartService.addProductToCart(loggedInUser, productId, quantity);
				} catch (ProductNotFoundException e) {

					System.out.println(e.getMessage());
				} catch (Exception e) {
					System.out.println("Not enough stock");
				}

				break;

			case 4:

				try {

					cartService.viewItemsCurrentlyInCart(loggedInUser);

				} catch (CartEmptyException e) {

					System.out.println(e.getMessage());

				}

				break;

			case 5:
				try {
					System.out.print("Enter Product ID to remove: ");
					int productId1 = getValidInt(sc);

					System.out.print("Enter quantity to remove: ");
					int removeQty = getValidInt(sc);

					cartService.removeProductFromCart(loggedInUser, productId1, removeQty);
				} catch (ProductNotFoundException e) {

					System.out.println(e.getMessage());

				}
				break;

			case 6:

				try {

					orderService.placeOrder(loggedInUser);

				} catch (CartEmptyException e) {

					System.out.println(e.getMessage());

				} catch (OutOfStockException e) {

					System.out.println(e.getMessage());

				} catch (Exception e) {

					System.out.println("Something went wrong while placing order.");
				}

				break;
			case 7:

				try {

					orderService.viewPreviousOrdersOfCustomers(loggedInUser);

				} catch (OrderNotFoundException e) {

					System.out.println(e.getMessage());

				}

				break;

			case 8:

				System.out.println("Thank you for using our platform!!");
				return;

			default:

				System.out.println("Invalid choice");
			}
		}
	}
}