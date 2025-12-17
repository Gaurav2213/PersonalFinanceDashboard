document.addEventListener("DOMContentLoaded", () => {
  const registerForm = document.getElementById("registerForm");
  if (!registerForm) return;

  const fullNameEl = document.getElementById("fullName");
  const emailEl = document.getElementById("email");
  const passwordEl = document.getElementById("password");
  const confirmPasswordEl = document.getElementById("confirmPassword");

  // Normalize multiple spaces: "A   B" -> "A B"
  const normalizeSpaces = (str) => str.trim().replace(/\s+/g, " ");

  // Password strength (adjust rules to match your backend if needed)
  const passwordStrengthError = (pwd) => {
    if (pwd.length < 8) return "Password must be at least 8 characters.";
    if (!/[A-Z]/.test(pwd)) return "Password must include at least 1 uppercase letter.";
    if (!/[a-z]/.test(pwd)) return "Password must include at least 1 lowercase letter.";
    if (!/[0-9]/.test(pwd)) return "Password must include at least 1 number.";
    if (!/[!@#$%^&*()_\-+=\[\]{};:'",.<>/?\\|`~]/.test(pwd))
      return "Password must include at least 1 special character.";
    return null;
  };

  registerForm.addEventListener("submit", (e) => {
    e.preventDefault();
    clearMessage("registerMessage");

    // 1) HTML validation (required, minlength, pattern, type=email)
    if (!registerForm.checkValidity()) {
      const firstInvalid = registerForm.querySelector(":invalid");
      if (firstInvalid) firstInvalid.focus();
      showMessage("registerMessage", "Please fix the highlighted fields and try again.", true);
      return;
    }

    // 2) Read + normalize values
    const fullName = normalizeSpaces(fullNameEl.value);
    const email = emailEl.value.trim().toLowerCase();
    const password = passwordEl.value;
    const confirmPassword = confirmPasswordEl.value;

    // Write cleaned values back (so user sees cleaned version)
    fullNameEl.value = fullName;
    emailEl.value = email;

    // 3) Password strength FIRST (as you requested)
    const pwdErr = passwordStrengthError(password);
    if (pwdErr) {
      showMessage("registerMessage", pwdErr, true);
      passwordEl.focus();
      return;
    }

    // 4) Confirm password AFTER strength (as you requested)
    if (password !== confirmPassword) {
      showMessage("registerMessage", "Passwords do not match.", true);
      confirmPasswordEl.focus();
      return;
    }

    // ✅ Passed local validation
    showMessage("registerMessage", "Validation passed ✅ Ready to register.", false);

    // Next step: call backend /auth/register with fetch()
  });
});
