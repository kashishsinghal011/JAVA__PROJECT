export default function TopBar({ title, icon, onToggleSidebar }) {
  const today = new Date().toLocaleDateString("en-IN", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric",
  });
  return (
    <header className="topbar">
      <button
        className="icon-btn topbar-burger"
        onClick={onToggleSidebar}
        aria-label="Toggle navigation menu"
      >
        ☰
      </button>
      <h1 className="topbar-title">
        <span aria-hidden="true">{icon}</span> {title}
      </h1>
      <div className="topbar-right">
        <span className="topbar-date">{today}</span>
        <div className="avatar" title="Sports Administrator">
          SA
        </div>
      </div>
    </header>
  );
}
