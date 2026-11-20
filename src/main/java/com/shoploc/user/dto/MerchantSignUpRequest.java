package com.shoploc.user.dto;

public class MerchantSignUpRequest {
	private String email;
	private String password;
	private String firstName;
	private String lastName;
	private String storeName;
	private String storeAddress;

	public MerchantSignUpRequest() {}

	public MerchantSignUpRequest(String email, String password, String firstName, String lastName,
			String storeName, String storeAddress) {
		this.email = email;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.storeName = storeName;
		this.storeAddress = storeAddress;
	}

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

	public String getFirstName() { return firstName; }
	public void setFirstName(String firstName) { this.firstName = firstName; }

	public String getLastName() { return lastName; }
	public void setLastName(String lastName) { this.lastName = lastName; }

	public String getStoreName() { return storeName; }
	public void setStoreName(String storeName) { this.storeName = storeName; }

	public String getStoreAddress() { return storeAddress; }
	public void setStoreAddress(String storeAddress) { this.storeAddress = storeAddress; }
}
