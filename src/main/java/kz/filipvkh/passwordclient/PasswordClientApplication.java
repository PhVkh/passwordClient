package kz.filipvkh.passwordclient;


import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class PasswordClientApplication {

	private static String getPasswordServerAddress() throws IOException {
		Properties properties = new Properties();
		String propFileName = "application.properties";

		InputStream inputStream = PasswordClientApplication.class.getClassLoader().getResourceAsStream(propFileName);

		if (inputStream != null) properties.load(inputStream);

		return properties.getProperty("password.server.address");
	}

	public static void main(String[] args) {
		String serverAddress = "";
		try {
			serverAddress = getPasswordServerAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}

		switch (args[0]) {
			case "-p":
				PasswordGenerator passwordGenerator = new PasswordGenerator();
				Password password = new Password();

				Scanner scanner = new Scanner(System.in);
				System.out.println("enter resource name:");
				password.setResource(scanner.nextLine());

				System.out.println("enter tags");
				password.setTags(Set.of(scanner.nextLine().split(" ")));
				password.setValue(passwordGenerator.generateRandomPassword());

				try {
					Gson jsonMapper = new Gson();
					HttpClient client = HttpClient.newHttpClient();
					HttpRequest request = HttpRequest.newBuilder(new URI(serverAddress))
							.POST(HttpRequest.BodyPublishers.ofString(jsonMapper.toJson(password)))
							.setHeader("Content-type", "application/json")
							.build();
					client.send(request, HttpResponse.BodyHandlers.ofString());
					System.out.println("generated password:\n" + password.getValue());
				} catch (Exception e) {
					System.out.println("a error occurred during password sending");
				}
				break;
			case "-h":
				System.out.println("\"-p\" to create a new password\n");
				break;
			default:
				List<Password> passwords = new ArrayList<>();
				try {
					Gson jsonMapper = new Gson();
					HttpClient client = HttpClient.newHttpClient();
					HttpRequest request = HttpRequest.newBuilder(new URI(serverAddress + "?tag=" + args[0]))
							.GET()
							.build();
					HttpResponse<String> httpResponse= client.send(request, HttpResponse.BodyHandlers.ofString());
					passwords = Arrays.asList(jsonMapper.fromJson(httpResponse.body(), Password[].class));
				} catch (Exception e) {
					System.out.println("a error occurred during password sending");
				}

				if (passwords.size() == 1) {
					System.out.println(passwords.get(0).getValue());
				} else {
					for (Password p : passwords) {
						System.out.println(p.getResource() + ": " + p.getValue());
					}
				}
				//decode
		}
	}

}
