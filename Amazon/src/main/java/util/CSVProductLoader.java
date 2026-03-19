package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class CSVProductLoader {

	public void loadProducts() {

		String filePath = "products.csv";
		int batchSize = 50;
		int count = 0;

		Connection con = null;

		try {

			con = DBConnection.getConnection();
			con.setAutoCommit(false);

			BufferedReader br = new BufferedReader(new FileReader(filePath));

			String sql = "INSERT IGNORE INTO products(id,product_name,price,quantity) VALUES(?,?,?,?)";

			PreparedStatement ps = con.prepareStatement(sql);

			String line;

			br.readLine();

			while ((line = br.readLine()) != null) {

				String[] data = line.split(",");

				if (data.length < 4) {
					System.out.println("Invalid row skipped: " + line);
					continue;
				}

				if (data[0].isEmpty() || data[1].isEmpty() || data[2].isEmpty() || data[3].isEmpty()) {
					System.out.println("Null value row skipped: " + line);
					continue;
				}

				try {

					int id = Integer.parseInt(data[0]);
					String name = data[1];
					int price = Integer.parseInt(data[2]);
					int quantity = Integer.parseInt(data[3]);

					ps.setInt(1, id);
					ps.setString(2, name);
					ps.setInt(3, price);
					ps.setInt(4, quantity);

					ps.addBatch();
					count++;

					if (count % batchSize == 0) {
						ps.executeBatch();
					}

				} catch (NumberFormatException e) {

					System.out.println("Invalid numeric data skipped: " + line);
				}
			}

			ps.executeBatch();
			con.commit();

			System.out.println("Valid products inserted successfully!");

		} catch (Exception e) { 

			try {
				if (con != null)
					con.rollback();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			e.printStackTrace();
		}
	}
}