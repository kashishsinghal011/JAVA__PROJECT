import { useState } from "react";
import useApi, { useToasts } from "../hooks/useApi.js";
import { api, SPORTS } from "../services/mockApi.js";
import Toasts from "../components/Toast.jsx";
import Empty from "../components/Empty.jsx";
import { Loading, ErrorBox } from "./Dashboard.jsx";

function Teams() {
  const {
    data: teams,
    loading,
    error,
    reload,
  } = useApi(() => api.getTeams(), []);
  const { data: players } = useApi(() => api.getPlayers(), []);
  const { toasts, push } = useToasts();
  const [form, setForm] = useState({ name: "", sport: SPORTS[0] });
  const [rowError, setRowError] = useState(null);
  if (loading) return <Loading />;
  if (error) return <ErrorBox message={error} />;

  const addTeam = async (e) => {
    e.preventDefault();
    try {
      await api.addTeam(form);
      push("Team created");
      setForm({ name: "", sport: SPORTS[0] });
      reload();
    } catch (err) {
      setRowError(err.message);
    }
  };
  const act = async (fn, okMsg) => {
    try {
      await fn();
      setRowError(null);
      push(okMsg);
      reload();
    } catch (err) {
      setRowError(err.message);
    }
  };

  return (
    <>
      <div className="page-head">
        <div>
          <h2>Teams &amp; Rosters</h2>
          <p>Create teams, manage rosters and appoint captains.</p>
        </div>
      </div>
      <form className="card card-pad" onSubmit={addTeam}>
        <h3>Create team</h3>
        <div className="form-grid">
          <div className="field">
            <label htmlFor="t-name">Team name</label>
            <input
              id="t-name"
              className="input"
              required
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
            />
          </div>
          <div className="field">
            <label htmlFor="t-sport">Sport</label>
            <select
              id="t-sport"
              className="select"
              value={form.sport}
              onChange={(e) => setForm({ ...form, sport: e.target.value })}
            >
              {SPORTS.map((s) => (
                <option key={s}>{s}</option>
              ))}
            </select>
          </div>
          <button className="btn" type="submit">
            + Create team
          </button>
        </div>
        {rowError && (
          <div className="form-error" role="alert">
            {rowError}
          </div>
        )}
      </form>
      <div className="grid-cards">
        {teams.map((t) => (
          <div className="card card-pad" key={t.id}>
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
              }}
            >
              <h3 style={{ margin: 0 }}>{t.name}</h3>
              <span className="badge badge--blue">{t.sport}</span>
            </div>
            <p
              style={{
                color: "var(--text-2)",
                fontSize: 13,
                margin: "6px 0 10px",
              }}
            >
              Captain: <strong>{t.captainName}</strong> · {t.roster.length}/
              {t.maxTeamSize} players
            </p>
            <ul className="feed" style={{ marginBottom: 10 }}>
              {t.rosterDetails.map((p) => (
                <li key={p.id} style={{ padding: "6px 0" }}>
                  <span>{p.name}</span>
                  <div className="actions" style={{ marginLeft: "auto" }}>
                    {t.captain !== p.id && (
                      <button
                        className="btn btn--ghost btn--sm"
                        onClick={() =>
                          act(
                            () => api.setCaptain(t.id, p.id),
                            "Captain appointed",
                          )
                        }
                      >
                        Captain
                      </button>
                    )}
                    <button
                      className="btn btn--danger btn--sm"
                      onClick={() =>
                        act(
                          () => api.removeRosterPlayer(t.id, p.id),
                          "Player removed",
                        )
                      }
                    >
                      ✕
                    </button>
                  </div>
                </li>
              ))}
            </ul>
            {!t.rosterDetails.length && <Empty text="Roster is empty." />}
            <RosterPicker
              players={players}
              team={t}
              onAdd={(pid) =>
                act(
                  () => api.addRosterPlayer(t.id, pid),
                  "Player added to roster",
                )
              }
            />
          </div>
        ))}
      </div>
      <Toasts toasts={toasts} />
    </>
  );
}

function RosterPicker({ players, team, onAdd }) {
  const [pid, setPid] = useState("");
  const available = players.filter((p) => !team.roster.includes(p.id));
  return (
    <div style={{ display: "flex", gap: 8 }}>
      <select
        className="select"
        value={pid}
        onChange={(e) => setPid(e.target.value)}
        aria-label={`Add player to ${team.name}`}
      >
        <option value="">Select player…</option>
        {available.map((p) => (
          <option key={p.id} value={p.id}>
            {p.name}
          </option>
        ))}
      </select>
      <button
        className="btn btn--sm"
        disabled={!pid}
        onClick={() => {
          onAdd(pid);
          setPid("");
        }}
      >
        Add
      </button>
    </div>
  );
}
Teams.title = "Teams & Roster Management";
Teams.icon = "👥";
export default Teams;
