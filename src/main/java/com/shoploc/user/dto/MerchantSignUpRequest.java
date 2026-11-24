package com.shoploc.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MerchantSignUpRequest {
	@NotBlank
	@Email
	@Size(max = 255)
	private String email;
	@NotBlank
	private String password;
	@NotBlank
	@Size(max = 100)
	private String firstName;
	@NotBlank
	@Size(max = 100)
	private String lastName;
	@NotBlank
	@Size(max = 150)
	private String storeName;
	@NotBlank
	@Size(max = 255)
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
