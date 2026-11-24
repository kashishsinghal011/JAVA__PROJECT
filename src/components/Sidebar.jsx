const NAV = [
  { key: "dashboard", label: "Dashboard", icon: "📊" },
  { key: "players", label: "Players", icon: "🏃" },
  { key: "coaches", label: "Coaches", icon: "🎓" },
  { key: "teams", label: "Teams & Rosters", icon: "👥" },
  { key: "tournaments", label: "Tournaments", icon: "🏆" },
  { key: "facilities", label: "Facilities & Booking", icon: "📅" },
  { key: "equipment", label: "Equipment", icon: "🎽" },
  { key: "matches", label: "Matches & Scores", icon: "⚽" },
  { key: "analytics", label: "Analytics", icon: "📈" },
  { key: "reports", label: "Reports", icon: "🧾" },
  { key: "data", label: "Save / Load Data", icon: "💾" },
];

export default function Sidebar({ page, onNavigate, open }) {
  return (
    <>
      {open && (
        <div
          className="sidebar-backdrop"
          onClick={() => onNavigate(page)}
          aria-hidden="true"
        />
      )}
      <aside
        className={`sidebar ${open ? "sidebar--open" : ""}`}
        aria-label="Main navigation"
      >
        <div className="brand">
          <span className="brand-mark">⚜</span>
          <div>
            <div className="brand-name">SportsSphere</div>
            <div className="brand-sub">Thapar Institute</div>
          </div>
        </div>
        <nav className="nav">
          {NAV.map((n) => (
            <button
              key={n.key}
              className={`nav-item ${page === n.key ? "nav-item--active" : ""}`}
              onClick={() => onNavigate(n.key)}
              aria-current={page === n.key ? "page" : undefined}
            >
              <span className="nav-icon" aria-hidden="true">
                {n.icon}
              </span>
              {n.label}
            </button>
          ))}
        </nav>
        <div className="sidebar-footer">
          <span className="badge badge--amber">PREVIEW MODE</span>
          <p className="sidebar-note">
            Mock data only — no backend connection.
          </p>
        </div>
      </aside>
    </>
  );
}
