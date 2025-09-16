package com.smart.contact.entities;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ChangePasswordRequest {
	
	    @NotBlank(message = "Current password is required")
	    private String currentPassword;

	    @NotBlank(message = "New password is required")
	    @Size(min = 8, max = 72, message = "Password must be 8–72 characters")
	    private String newPassword;

	    @NotBlank(message = "Confirm new password is required")
	    private String confirmNewPassword;

		/**
		 * @return the currentPassword
		 */
		public String getCurrentPassword() {
			return currentPassword;
		}

		/**
		 * @param currentPassword the currentPassword to set
		 */
		public void setCurrentPassword(String currentPassword) {
			this.currentPassword = currentPassword;
		}

		/**
		 * @return the newPassword
		 */
		public String getNewPassword() {
			return newPassword;
		}

		/**
		 * @param newPassword the newPassword to set
		 */
		public void setNewPassword(String newPassword) {
			this.newPassword = newPassword;
		}

		/**
		 * @return the confirmNewPassword
		 */
		public String getConfirmNewPassword() {
			return confirmNewPassword;
		}

		/**
		 * @param confirmNewPassword the confirmNewPassword to set
		 */
		public void setConfirmNewPassword(String confirmNewPassword) {
			this.confirmNewPassword = confirmNewPassword;
		}
	    
	    

	
}
