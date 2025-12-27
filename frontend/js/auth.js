document.addEventListener("DOMContentLoaded", () => {
  const registerForm = document.getElementById("registerForm");
  if (!registerForm) return;

  const fullNameEl = document.getElementById("fullName");
  const emailEl = document.getElementById("email");
  const passwordEl = document.getElementById("password");
  const confirmPasswordEl = document.getElementById("confirmPassword");

  const fieldIds = ["fullName", "email", "password", "confirmPassword"];

  // Normalize multiple spaces: "A   B" -> "A B"
  const normalizeSpaces = (str) => str.trim().replace(/\s+/g, " ");

  // ---------- Inline error helpers ----------
  const setFieldError = (fieldId, message) => {
    const input = document.getElementById(fieldId);
    const err = document.getElementById(fieldId + "Error");
    if (!input || !err) return;

    err.textContent = message;
    input.classList.add("invalid");
    input.setAttribute("aria-invalid", "true");
  };

  const clearFieldError = (fieldId) => {
    const input = document.getElementById(fieldId);
    const err = document.getElementById(fieldId + "Error");
    if (!input || !err) return;

    err.textContent = "";
    input.classList.remove("invalid");
    input.removeAttribute("aria-invalid");
  };

  const clearAllFieldErrors = () => fieldIds.forEach(clearFieldError);

  // Clear inline error when user types (keeps UX clean)
  fieldIds.forEach((id) => {
    const el = document.getElementById(id);
    if (!el) return;
    el.addEventListener("input", () => clearFieldError(id));
  });

  // Password strength (keep aligned with backend)
const passwordStrengthError = (pwd) => {
  const errors = [];

  if (pwd.length < 8)
    errors.push("at least 8 characters");

  if (!/[A-Z]/.test(pwd))
    errors.push("1 uppercase letter");

  if (!/[a-z]/.test(pwd))
    errors.push("1 lowercase letter");

  if (!/[0-9]/.test(pwd))
    errors.push("1 number");

  if (!/[!@#$%^&*()_\-+=\[\]{};:'\",.<>/?\\|`~]/.test(pwd))
    errors.push("1 special character");

  if (errors.length === 0) return null;

  return `Password must include ${errors.join(", ")}.`;
};


  // Map backend message to a field (best-effort, works with your current backend format)
  const applyBackendErrorToField = (msg) => {
    const m = (msg || "").toLowerCase();

    if (m.includes("email")) {
      setFieldError("email", msg);
      emailEl.focus();
      return true;
    }
    if (m.includes("password")) {
      setFieldError("password", msg);
      passwordEl.focus();
      return true;
    }
    if (m.includes("name")) {
      setFieldError("fullName", msg);
      fullNameEl.focus();
      return true;
    }
    return false; // fallback to global message
  };

  registerForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    clearMessage("registerMessage");
    clearAllFieldErrors();

    // 1) HTML validation first (required, minlength, pattern, type=email)
    // If invalid, let the browser show its own tooltip messages.
    if (!registerForm.checkValidity()) {
      registerForm.reportValidity(); // ✅ shows native message near the field
      return;
    }

    // 2) Read + normalize values
    const fullName = normalizeSpaces(fullNameEl.value);
    const email = emailEl.value.trim().toLowerCase();
    const password = passwordEl.value;
    const confirmPassword = confirmPasswordEl.value;

    // Write cleaned values back
    fullNameEl.value = fullName;
    emailEl.value = email;

    // 3) JS custom validation → inline error under password
    const pwdErr = passwordStrengthError(password);
    if (pwdErr) {
      setFieldError("password", pwdErr);
      passwordEl.focus();
      return;
    }

    // 4) Confirm password match → inline error under confirmPassword
    if (password !== confirmPassword) {
      setFieldError("confirmPassword", "Passwords do not match.");
      confirmPasswordEl.focus();
      return;
    }

    // 5) Payload (backend expects name/email/password)
    const payload = {
      name: fullName,
      email,
      password,
    };

    try {
      showMessage("registerMessage", "Creating account...", false);

      const response = await fetch(`${API_BASE_URL}/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      const data = await response.json(); // { success: true/false, message: "..." }

      // 6) Backend validation -> show under field if possible, else global fallback
      if (!data.success) {
        const applied = applyBackendErrorToField(data.message);
        if (!applied) showMessage("registerMessage", data.message || "Registration failed.", true);
        return;
      }

      // 7) Success -> global message + redirect
      showMessage("registerMessage", data.message || "Account created successfully.", false);

      setTimeout(() => {
        window.location.href = "login.html";
      }, 1200);

    } catch (error) {
      showMessage("registerMessage", "Unable to connect to server. Please try again later.", true);
    }
  });
});
