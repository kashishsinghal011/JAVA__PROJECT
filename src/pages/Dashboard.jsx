import useApi from "../hooks/useApi.js";
import { api } from "../services/mockApi.js";

function Dashboard() {
  const { data, loading, error } = useApi(() => api.getDashboard(), []);
  if (loading) return <Loading />;
  if (error) return <ErrorBox message={error} />;
  return (
    <>
      <div className="stat-grid">
        {data.stats.map((s) => (
          <div className="card stat" key={s.label}>
            <div className={`stat-icon ${s.tone}`}>{s.icon}</div>
            <div>
              <div className="stat-value">{s.value}</div>
              <div className="stat-label">{s.label}</div>
              <div className="stat-sub">{s.sub}</div>
            </div>
          </div>
        ))}
      </div>
      <div className="card card-pad">
        <h3>Recent activity</h3>
        <ul className="feed">
          {data.activity.map((a) => (
            <li key={a.id}>
              <span className="feed-icon" aria-hidden="true">
                {a.icon}
              </span>
              <span>{a.text}</span>
              <span className="feed-time">{a.time}</span>
            </li>
          ))}
        </ul>
      </div>
    </>
  );
}
Dashboard.title = "Dashboard";
Dashboard.icon = "📊";
export default Dashboard;

export function Loading() {
  return (
    <div className="loading">
      <span className="spinner" />
      <br />
      Loading…
    </div>
  );
}
export function ErrorBox({ message }) {
  return (
    <div className="form-error" role="alert">
      {message}
    </div>
  );
}
