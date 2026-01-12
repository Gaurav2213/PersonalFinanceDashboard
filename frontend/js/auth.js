document.addEventListener("DOMContentLoaded", () => {
  initRegister();
  initLogin();
});

/* =========================
   Shared inline error helpers
========================= */
function setFieldError(fieldId, message) {
  const input = document.getElementById(fieldId);
  const err = document.getElementById(fieldId + "Error");
  if (!input || !err) return;

  err.textContent = message;
  input.classList.add("invalid");
  input.setAttribute("aria-invalid", "true");
}

function clearFieldError(fieldId) {
  const input = document.getElementById(fieldId);
  const err = document.getElementById(fieldId + "Error");
  if (!input || !err) return;

  err.textContent = "";
  input.classList.remove("invalid");
  input.removeAttribute("aria-invalid");
}

function clearAllFieldErrors(fieldIds) {
  fieldIds.forEach(clearFieldError);
}

function attachClearOnInput(fieldIds) {
  fieldIds.forEach((id) => {
    const el = document.getElementById(id);
    if (!el) return;
    el.addEventListener("input", () => clearFieldError(id));
  });
}

/* =========================
   REGISTER
========================= */
function initRegister() {
  const registerForm = document.getElementById("registerForm");
  if (!registerForm) return; // ✅ only skip register init, not whole file

  const fullNameEl = document.getElementById("fullName");
  const emailEl = document.getElementById("email");
  const passwordEl = document.getElementById("password");
  const confirmPasswordEl = document.getElementById("confirmPassword");

  const fieldIds = ["fullName", "email", "password", "confirmPassword"];
  attachClearOnInput(fieldIds);

  const normalizeSpaces = (str) => str.trim().replace(/\s+/g, " ");

  const passwordStrengthError = (pwd) => {
    const errors = [];

    if (pwd.length < 8) errors.push("at least 8 characters");
    if (!/[A-Z]/.test(pwd)) errors.push("1 uppercase letter");
    if (!/[a-z]/.test(pwd)) errors.push("1 lowercase letter");
    if (!/[0-9]/.test(pwd)) errors.push("1 number");
    if (!/[!@#$%^&*()_\-+=\[\]{};:'\",.<>/?\\|`~]/.test(pwd))
      errors.push("1 special character");

    return errors.length ? `Password must include ${errors.join(", ")}.` : null;
  };

  const applyBackendErrorToRegisterField = (msg) => {
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
    return false;
  };

  registerForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    clearMessage("registerMessage");
    clearAllFieldErrors(fieldIds);

    // HTML validation first
    if (!registerForm.checkValidity()) {
      registerForm.reportValidity();
      return;
    }

    const fullName = normalizeSpaces(fullNameEl.value);
    const email = emailEl.value.trim().toLowerCase();
    const password = passwordEl.value;
    const confirmPassword = confirmPasswordEl.value;

    fullNameEl.value = fullName;
    emailEl.value = email;

    const pwdErr = passwordStrengthError(password);
    if (pwdErr) {
      setFieldError("password", pwdErr);
      passwordEl.focus();
      return;
    }

    if (password !== confirmPassword) {
      setFieldError("confirmPassword", "Passwords do not match.");
      confirmPasswordEl.focus();
      return;
    }

    const payload = { name: fullName, email, password };

    try {
      showMessage("registerMessage", "Creating account...", false);

      const response = await fetch(`${API_BASE_URL}/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      const data = await response.json();

      if (!data.success) {
        const applied = applyBackendErrorToRegisterField(data.message);
        if (!applied) showMessage("registerMessage", data.message || "Registration failed.", true);
        return;
      }

      showMessage("registerMessage", data.message || "Account created successfully.", false);

      setTimeout(() => {
        window.location.href = "login.html";
      }, 1200);

    } catch (error) {
      showMessage("registerMessage", "Unable to connect to server. Please try again later.", true);
    }
  });
}

/* =========================
   LOGIN
========================= */
function initLogin() {
  const loginForm = document.getElementById("loginForm");
  if (!loginForm) return;

  const emailEl = document.getElementById("email");
  const passwordEl = document.getElementById("password");

  const fieldIds = ["email", "password"];
  attachClearOnInput(fieldIds);

  const applyBackendErrorToLoginField = (msg) => {
    const m = (msg || "").toLowerCase();

    // common login failure messages
    if (m.includes("email") || m.includes("user") || m.includes("not found")) {
      setFieldError("email", msg);
      emailEl.focus();
      return true;
    }
    if (m.includes("password")) {
      setFieldError("password", msg);
      passwordEl.focus();
      return true;
    }
    if (m.includes("invalid") || m.includes("credentials") || m.includes("incorrect")) {
      // place generic auth failure under password (common UX)
      setFieldError("password", msg);
      passwordEl.focus();
      return true;
    }
    return false;
  };

  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    clearMessage("loginMessage");
    clearAllFieldErrors(fieldIds);

    // HTML validation first
    if (!loginForm.checkValidity()) {
      loginForm.reportValidity();
      return;
    }

    const email = emailEl.value.trim().toLowerCase();
    const password = passwordEl.value;
    emailEl.value = email;

    const payload = { email, password };

    try {
      showMessage("loginMessage", "Logging in...", false);

      const response = await fetch(`${API_BASE_URL}/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      const data = await response.json();

      if (!data.success) {
        const applied = applyBackendErrorToLoginField(data.message);
        if (!applied) showMessage("loginMessage", data.message || "Login failed.", true);
        return;
      }
      else{
        const token = data?.data?.token;
        if (token) localStorage.setItem("token", token);
      }

      // ✅ Store token if your backend returns one (adjust field name if needed)
      // Example patterns:
      // if (data.token) localStorage.setItem("token", data.token);
      // if (data.data?.token) localStorage.setItem("token", data.data.token);

      showMessage("loginMessage", data.message || "Login successful.", false);

      setTimeout(() => {
        // update this to your dashboard path
        window.location.href = "../pages/dashboard.html";
      }, 800);

    } catch (error) {
      showMessage("loginMessage", "Unable to connect to server. Please try again later.", true);
    }
  });
}
