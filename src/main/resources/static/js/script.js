// =========================================
// SMART CONTACT MANAGER FRONTEND SCRIPT
// =========================================

// ⚠️ Removed `require("console")` because it's for Node.js, not browsers.
// Browsers already support `console.log`, `console.error`, etc.

// =========================================
// TOGGLE SIDEBAR FUNCTION
// =========================================
// This toggles the sidebar visibility when the user clicks a menu button.
function toggleSidebar() {
  if ($(".sidebar").is(":visible")) {
    // Hide sidebar
    $(".sidebar").css("display", "none");
    $(".contant").css("margin-left", "0%");
  } else {
    // Show sidebar
    $(".sidebar").css("display", "block");
    $(".contant").css("margin-left", "20%");
  }
}

// =========================================
// SHARE CONTACT FUNCTION
// =========================================
// Uses the Web Share API (if supported by the browser)
// to share contact details with other apps.
function shareContact() {
  const contact = {
    name: /*[[${contact.name}]]*/ "Contact Name", // Thymeleaf placeholder
    phone: /*[[${contact.phone}]]*/ "1234567890",
    email: /*[[${contact.email}]]*/ "test@example.com",
  };

  const shareData = {
    title: "Contact Info",
    text: `Name: ${contact.name}\nPhone: ${contact.phone}\nEmail: ${contact.email}`,
    url: window.location.href,
  };

  if (navigator.share) {
    navigator
      .share(shareData)
      .then(() => console.log("✅ Contact shared successfully"))
      .catch((error) => console.error("❌ Error sharing:", error));
  } else {
    alert(
      "Sharing not supported on this browser. Copy this info:\n\n" +
        shareData.text
    );
  }
}

// =========================================
// SEARCH FUNCTIONALITY
// =========================================
// When user types in the search box, fetch matching contacts from the server.
function search() {
  let query = document.getElementById("search-input").value.trim();

  if (query === "") {
    $(".search-result").hide();
    return;
  }

  console.log("Searching for:", query);

  let url = `http://localhost:7777/search/${query}`;

  fetch(url)
    .then((response) => {
      if (!response.ok) {
        throw new Error("Network error: " + response.status);
      }
      return response.json();
    })
    .then((data) => {
      console.log("Fetched data:", data);

      if (!Array.isArray(data) || data.length === 0) {
        $(".search-result")
          .html("<p class='text-muted'>No contacts found</p>")
          .show();
        return;
      }

      let text = `<div class='list-group'>`;

      data.forEach((contact) => {
        text += `<a href='/user/${contact.cId}/contact' 
                   class='list-group-item list-group-item-action'>
                   ${contact.name}
                 </a>`;
      });

      text += `</div>`;

      $(".search-result").html(text).show();
    })
    .catch((err) => {
      console.error("Error fetching:", err);
      $(".search-result")
        .html("<p class='text-danger'>Error fetching contacts</p>")
        .show();
    });
}

// =========================================
// PAYMENT START FUNCTION
// =========================================
// This is called when the user clicks the "Pay" button.
// 1. Sends AJAX request to server to create an order.
// 2. If order created, opens Razorpay payment popup.
function paymentStart() {
  console.log("💰 Payment started..");

  let amount = $("#payment_field").val();
  if (!amount) {
    swal("Failed!", "Amount is required!", "error");
    return;
  }

  // Create order on server
  $.ajax({
    url: "/user/create_order", // Spring Boot endpoint
    type: "POST",
    contentType: "application/json",
    data: JSON.stringify({ amount: amount, info: "order_request" }),

    success: function (response) {
      console.log("✅ Order created successfully:", response);

      if (response.status == "created") {
        // Setup Razorpay options
        let options = {
          key: "rzp_test_RF5NdX8bMwnpni", // Test key from Razorpay Dashboard
          amount: response.amount,
          currency: "INR",
          name: "Smart Contact Manager",
          description: "Donation",
          image:
            "https://upload.wikimedia.org/wikipedia/commons/a/a7/React-icon.svg",
          order_id: response.id, // Important: order_id from server

          // Handler for successful payment
          handler: function (res) {
            console.log("Payment Success ✅");
            console.log("Payment ID:", res.razorpay_payment_id);
            console.log("Order ID:", res.razorpay_order_id);
            console.log("Signature:", res.razorpay_signature);

            // Update server that payment was successful
            updatePaymentOnServer(
              res.razorpay_payment_id,
              res.razorpay_order_id,
              "paid"
            );
          },

          // Pre-filled user info (optional)
          prefill: {
            name: "",
            email: "",
            contact: "",
          },

          notes: {
            address: "Payment To Vipin Kumar Yadav",
          },

          theme: {
            color: "#3399cc",
          },
        };

        // Initialize Razorpay
        var rzp = new Razorpay(options);

        // Handler for payment failure
        rzp.on("payment.failed", function (res) {
          console.error("❌ Payment failed:", res.error);
          swal("Payment Failed!", res.error.description, "error");

          // Log failure to backend (optional)
          $.ajax({
            url: "/payment_failed",
            type: "POST",
            data: JSON.stringify(res.error),
            contentType: "application/json",
            success: function (res) {
              console.log("Failure logged on server:", res);
            },
          });
        });

        // Open Razorpay popup
        rzp.open();
      }
    },

    error: function (error) {
      console.error("❌ Error creating order:", error);
      swal("Failed!", "Oops! Payment failed!", "error");
    },
  });
}

// =========================================
// UPDATE PAYMENT ON SERVER FUNCTION
// =========================================
// After successful payment, we call this function to update payment status
// in our Spring Boot backend database.
function updatePaymentOnServer(payment_id, order_id, status) {
  $.ajax({
    url: "/user/update_order",
    type: "POST",
    data: JSON.stringify({ payment_id, order_id, status }),
    contentType: "application/json",
    dataType: "json",

    success: function (response) {
      console.log("✅ Payment updated on server:", response);
      swal("Payment Success!", "Congrats! Payment successful!", "success");
    },

    error: function (error) {
      console.error("❌ Error updating payment:", error);
      swal(
        "Failed!",
        "Payment was successful but not updated on server. We will contact you soon.",
        "error"
      );
    },
  });
}
