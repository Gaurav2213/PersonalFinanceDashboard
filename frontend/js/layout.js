/*
   layout.js — renders the shared app shell (sidebar + topbar) into #app-shell.
   Depends on api.js (getTokenClaims, logout). Load api.js BEFORE this file.

   Usage in a page:
     <div id="app-shell"></div>
     ...
     renderShell("transactions");   // highlights the active nav item
   Then the page injects its own content into <main class="content"> (id="page").
*/

const NAV_ITEMS = [
  { key: "dashboard", label: "Dashboard", icon: "▦", href: "dashboard.html" },
  { key: "transactions", label: "Transactions", icon: "⇄", href: "transactions.html" },
  { key: "budgets", label: "Budgets", icon: "◎", href: "budgets.html" },
  { key: "analytics", label: "Analytics", icon: "◩", href: "analytics.html" },
];

function renderShell(activeKey) {
  const mount = document.getElementById("app-shell");
  if (!mount) return;

  const claims = getTokenClaims();
  const email = (claims && claims.email) || "";
  const activeItem = NAV_ITEMS.find((i) => i.key === activeKey);
  const pageTitle = activeItem ? activeItem.label : "Dashboard";

  const navHtml = NAV_ITEMS.map(
    (item) => `
      <a class="nav-link ${item.key === activeKey ? "active" : ""}" href="${item.href}">
        <span class="nav-icon">${item.icon}</span>
        <span>${item.label}</span>
      </a>`
  ).join("");

  mount.className = "app-shell";
  mount.innerHTML = `
    <aside class="sidebar">
      <div class="sidebar-brand">
        <span class="brand-mark">$</span>
        <span>FinDash</span>
      </div>
      <nav class="sidebar-nav">
        ${navHtml}
      </nav>
    </aside>

    <div class="app-main">
      <header class="topbar">
        <h1 class="topbar-title">${pageTitle}</h1>
        <div class="topbar-user">
          <span class="topbar-email">${escapeHtml(email)}</span>
          <button type="button" class="btn-logout" id="logoutBtn">Log out</button>
        </div>
      </header>
      <main class="content" id="page"></main>
    </div>
  `;

  const logoutBtn = document.getElementById("logoutBtn");
  if (logoutBtn) logoutBtn.addEventListener("click", () => logout());
}
