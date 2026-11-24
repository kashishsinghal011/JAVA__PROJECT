import { useState } from "react";
import useApi, { useToasts } from "../hooks/useApi.js";
import { api, SPORTS, TOURNAMENT_STATUSES } from "../services/mockApi.js";
import Toasts from "../components/Toast.jsx";
import Empty from "../components/Empty.jsx";
import { Loading, ErrorBox } from "./Dashboard.jsx";

const statusClass = (s) =>
  ({
    DRAFT: "badge--gray",
    REGISTRATION_OPEN: "badge--green",
    REGISTRATION_CLOSED: "badge--amber",
    IN_PROGRESS: "badge--violet",
    COMPLETED: "badge--blue",
  })[s];
const pretty = (s) =>
  s
    .replaceAll("_", " ")
    .toLowerCase()
    .replace(/^\w/, (c) => c.toUpperCase());

function Tournaments() {
  const {
    data: tournaments,
    loading,
    error,
    reload,
  } = useApi(() => api.getTournaments(), []);
  const { toasts, push } = useToasts();
  const [filter, setFilter] = useState("ALL");
  const [form, setForm] = useState({
    name: "",
    sport: SPORTS[0],
    registrationDeadline: "",
    maxEntries: 16,
  });
  const [formError, setFormError] = useState(null);
  const [regFor, setRegFor] = useState(null);
  const [regName, setRegName] = useState("");
  const [regType, setRegType] = useState("PLAYER");
  const [regError, setRegError] = useState(null);
  if (loading) return <Loading />;
  if (error) return <ErrorBox message={error} />;

  const visible =
    filter === "ALL"
      ? tournaments
      : tournaments.filter((t) => t.status === filter);

  const addTournament = async (e) => {
    e.preventDefault();
    try {
      await api.addTournament({ ...form, maxEntries: Number(form.maxEntries) });
      push("Tournament created");
      setFormError(null);
      reload();
    } catch (err) {
      setFormError(err.message);
    }
  };
  const register = async (e) => {
    e.preventDefault();
    try {
      await api.registerForTournament(regFor.id, regName, regType);
      push("Entry submitted");
      setRegError(null);
      setRegFor(null);
      reload();
    } catch (err) {
      setRegError(err.message);
    }
  };

  return (
    <>
      <div className="page-head">
        <div>
          <h2>Tournaments</h2>
          <p>Lifecycle from draft to completed, with entry registration.</p>
        </div>
      </div>

      <form className="card card-pad" onSubmit={addTournament}>
        <h3>New tournament</h3>
        <div className="form-grid">
          <div className="field">
            <label htmlFor="to-name">Name</label>
            <input
              id="to-name"
              className="input"
              required
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
            />
          </div>
          <div className="field">
            <label htmlFor="to-sport">Sport</label>
            <select
              id="to-sport"
              className="select"
              value={form.sport}
              onChange={(e) => setForm({ ...form, sport: e.target.value })}
            >
              {SPORTS.map((s) => (
                <option key={s}>{s}</option>
              ))}
            </select>
          </div>
          <div className="field">
            <label htmlFor="to-deadline">Registration deadline</label>
            <input
              id="to-deadline"
              className="input"
              type="date"
              required
              value={form.registrationDeadline}
              onChange={(e) =>
                setForm({ ...form, registrationDeadline: e.target.value })
              }
            />
          </div>
          <div className="field">
            <label htmlFor="to-max">Max entries (2–64)</label>
            <input
              id="to-max"
              className="input"
              type="number"
              min="2"
              max="64"
              required
              value={form.maxEntries}
              onChange={(e) => setForm({ ...form, maxEntries: e.target.value })}
            />
          </div>
          <button className="btn" type="submit">
            + Create
          </button>
        </div>
        {formError && (
          <div className="form-error" role="alert">
            {formError}
          </div>
        )}
      </form>

      <div className="checks">
        <label>
          <input
            type="radio"
            name="tfilter"
            checked={filter === "ALL"}
            onChange={() => setFilter("ALL")}
          />{" "}
          All
        </label>
        {TOURNAMENT_STATUSES.map((s) => (
          <label key={s}>
            <input
              type="radio"
              name="tfilter"
              checked={filter === s}
              onChange={() => setFilter(s)}
            />{" "}
            {pretty(s)}
          </label>
        ))}
      </div>

      <div className="grid-cards">
        {visible.map((t) => (
          <div className="card card-pad" key={t.id}>
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                gap: 8,
                alignItems: "flex-start",
              }}
            >
              <h3 style={{ margin: 0 }}>{t.name}</h3>
              <span className={`badge ${statusClass(t.status)}`}>
                {pretty(t.status)}
              </span>
            </div>
            <p
              style={{
                color: "var(--text-2)",
                fontSize: 13,
                margin: "8px 0 12px",
              }}
            >
              {t.sport} · deadline {t.registrationDeadline} ·{" "}
              <strong>
                {t.entries}/{t.maxEntries}
              </strong>{" "}
              entries
            </p>
            {t.registrations.length > 0 && (
              <ul className="feed" style={{ marginBottom: 12 }}>
                {t.registrations.map((r) => (
                  <li key={r.id} style={{ padding: "5px 0" }}>
                    <span>{r.entrant}</span>
                    <span
                      className={`badge ${r.status === "CONFIRMED" ? "badge--green" : "badge--amber"}`}
                      style={{ marginLeft: "auto" }}
                    >
                      {r.status.toLowerCase()}
                    </span>
                  </li>
                ))}
              </ul>
            )}
            {!t.registrations.length && <Empty text="No entries yet." />}
            <div className="actions">
              {t.status !== "COMPLETED" && (
                <button
                  className="btn btn--sm"
                  onClick={async () => {
                    try {
                      await api.advanceTournament(t.id);
                      push("Status advanced");
                      reload();
                    } catch (err) {
                      push(err.message, "error");
                    }
                  }}
                >
                  Advance status →
                </button>
              )}
              <button
                className="btn btn--ghost btn--sm"
                onClick={() => {
                  setRegFor(t);
                  setRegError(null);
                  setRegName("");
                }}
              >
                Register entry
              </button>
            </div>
          </div>
        ))}
        {!visible.length && (
          <div className="card" style={{ gridColumn: "1/-1" }}>
            <Empty text="No tournaments with this status." />
          </div>
        )}
      </div>

      {regFor && (
        <form className="card card-pad" onSubmit={register}>
          <h3>Register entry — {regFor.name}</h3>
          <div className="form-grid">
            <div className="field">
              <label htmlFor="r-name">Team or player name</label>
              <input
                id="r-name"
                className="input"
                required
                value={regName}
                onChange={(e) => setRegName(e.target.value)}
              />
            </div>
            <div className="field">
              <label htmlFor="r-type">Entry type</label>
              <select
                id="r-type"
                className="select"
                value={regType}
                onChange={(e) => setRegType(e.target.value)}
              >
                <option value="PLAYER">Individual player</option>
                <option value="TEAM">Team</option>
              </select>
            </div>
            <button className="btn" type="submit">
              Submit entry
            </button>
            <button
              className="btn btn--ghost"
              type="button"
              onClick={() => setRegFor(null)}
            >
              Cancel
            </button>
          </div>
          {regError && (
            <div className="form-error" role="alert">
              {regError}
            </div>
          )}
        </form>
      )}
      <Toasts toasts={toasts} />
    </>
  );
}
Tournaments.title = "Tournament Management";
Tournaments.icon = "🏆";
export default Tournaments;
