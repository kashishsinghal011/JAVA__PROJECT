export default function Empty({ icon = "🗂️", text }) {
  return (
    <div className="empty">
      <div className="empty-icon" aria-hidden="true">
        {icon}
      </div>
      <p>{text}</p>
    </div>
  );
}
