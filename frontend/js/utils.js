function showMessage(elementId, message, isError) {
  const el = document.getElementById(elementId);
  if (!el) return;

  el.style.color = isError ? "#dc2626" : "#16a34a"; // red / green
  el.textContent = message;
}

function clearMessage(elementId) {
  const el = document.getElementById(elementId);
  if (!el) return;
  el.textContent = "";
}
